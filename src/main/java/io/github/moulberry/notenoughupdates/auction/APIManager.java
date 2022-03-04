package io.github.moulberry.notenoughupdates.auction;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.moulberry.notenoughupdates.ItemPriceInformation;
import io.github.moulberry.notenoughupdates.NEUManager;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.miscgui.GuiPriceGraph;
import io.github.moulberry.notenoughupdates.recipes.Ingredient;
import io.github.moulberry.notenoughupdates.recipes.NeuRecipe;
import io.github.moulberry.notenoughupdates.util.Constants;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class APIManager {
	private final NEUManager manager;
	public final CustomAH customAH;

	private final TreeMap<String, Auction> auctionMap = new TreeMap<>();
	public HashMap<String, HashSet<String>> internalnameToAucIdMap = new HashMap<>();
	private final HashSet<String> playerBids = new HashSet<>();
	private final HashSet<String> playerBidsNotified = new HashSet<>();
	private final HashSet<String> playerBidsFinishedNotified = new HashSet<>();

	private JsonObject lowestBins = null;
	private JsonObject auctionPricesAvgLowestBinJson = null;

	private LinkedList<Integer> pagesToDownload = null;

	private JsonObject bazaarJson = null;
	private JsonObject auctionPricesJson = null;
	private final HashMap<String, CraftInfo> craftCost = new HashMap<>();

	public TreeMap<String, HashMap<Integer, HashSet<String>>> extrasToAucIdMap = new TreeMap<>();

	private boolean didFirstUpdate = false;
	private long lastAuctionUpdate = 0;
	private long lastShortAuctionUpdate = 0;
	private long lastCustomAHSearch = 0;
	private long lastCleanup = 0;
	private long lastAuctionAvgUpdate = 0;
	private long lastBazaarUpdate = 0;
	private long lastLowestBinUpdate = 0;

	private long lastApiUpdate = 0;
	private long firstHypixelApiUpdate = 0;

	public int activeAuctions = 0;
	public int uniqueItems = 0;
	public int totalTags = 0;
	public int internalnameTaggedAuctions = 0;
	public int taggedAuctions = 0;
	public int processMillis = 0;

	public APIManager(NEUManager manager) {
		this.manager = manager;
		customAH = new CustomAH(manager);
	}

	public TreeMap<String, Auction> getAuctionItems() {
		return auctionMap;
	}

	public HashSet<String> getPlayerBids() {
		return playerBids;
	}

	public HashSet<String> getAuctionsForInternalname(String internalname) {
		return internalnameToAucIdMap.computeIfAbsent(internalname, k -> new HashSet<>());
	}

	public class Auction {
		public String auctioneerUuid;
		public long end;
		public int starting_bid;
		public int highest_bid_amount;
		public int bid_count;
		public boolean bin;
		public String category;
		public String rarity;
		public int dungeonTier;
		public String item_tag_str;
		public NBTTagCompound item_tag = null;
		private ItemStack stack;

		public int enchLevel = 0; //0 = clean, 1 = ench, 2 = ench/hpb

		public Auction(
			String auctioneerUuid, long end, int starting_bid, int highest_bid_amount, int bid_count,
			boolean bin, String category, String rarity, int dungeonTier, String item_tag_str
		) {
			this.auctioneerUuid = auctioneerUuid;
			this.end = end;
			this.starting_bid = starting_bid;
			this.highest_bid_amount = highest_bid_amount;
			this.bid_count = bid_count;
			this.bin = bin;
			this.category = category;
			this.dungeonTier = dungeonTier;
			this.rarity = rarity;
			this.item_tag_str = item_tag_str;
		}

		public ItemStack getStack() {
			if (item_tag == null && item_tag_str != null) {
				try {
					item_tag = CompressedStreamTools.readCompressed(
						new ByteArrayInputStream(Base64.getDecoder().decode(item_tag_str)));
					item_tag_str = null;
				} catch (IOException e) {
					return null;
				}
			}
			if (stack != null) {
				return stack;
			} else {
				JsonObject item = manager.getJsonFromNBT(item_tag);
				ItemStack stack = manager.jsonToStack(item, false);

				JsonObject itemDefault = manager.getItemInformation().get(item.get("internalname").getAsString());

				if (stack != null && itemDefault != null) {
					ItemStack stackDefault = manager.jsonToStack(itemDefault, true);
					if (stack.isItemEqual(stackDefault)) {
						//Item types are the same, compare lore

						String[] stackLore = manager.getLoreFromNBT(stack.getTagCompound());
						String[] defaultLore = manager.getLoreFromNBT(stackDefault.getTagCompound());

						boolean loreMatches = stackLore != null && defaultLore != null && stackLore.length == defaultLore.length;
						if (loreMatches) {
							for (int i = 0; i < stackLore.length; i++) {
								if (!stackLore[i].equals(defaultLore[i])) {
									loreMatches = false;
									break;
								}
							}
						}
						if (loreMatches) {
							stack = stackDefault;
						}
					}
				}
				this.stack = stack;
				return stack;
			}
		}
	}

	public void markNeedsUpdate() {
		firstHypixelApiUpdate = 0;
		pagesToDownload = null;

		auctionMap.clear();
		internalnameToAucIdMap.clear();
		extrasToAucIdMap.clear();
	}

	public void tick() {
		customAH.tick();
		long currentTime = System.currentTimeMillis();
		if (NotEnoughUpdates.INSTANCE.config.neuAuctionHouse.enableNeuAuctionHouse &&
			NotEnoughUpdates.INSTANCE.config.apiKey.apiKey != null &&
			!NotEnoughUpdates.INSTANCE.config.apiKey.apiKey.isEmpty()) {
			if (currentTime - lastAuctionUpdate > 60 * 1000) {
				lastAuctionUpdate = currentTime;
				updatePageTick();
			}
			if (currentTime - lastShortAuctionUpdate > 10 * 1000) {
				lastShortAuctionUpdate = currentTime;
				updatePageTickShort();
				ahNotification();
			}
			if (currentTime - lastCleanup > 60 * 1000) {
				lastCleanup = currentTime;
				cleanup();
			}
			if (currentTime - lastCustomAHSearch > 60 * 1000) {
				lastCustomAHSearch = currentTime;
				if (Minecraft.getMinecraft().currentScreen instanceof CustomAHGui || customAH.isRenderOverAuctionView()) {
					customAH.updateSearch();
					calculateStats();
				}
			}
		}
		if (currentTime - lastAuctionAvgUpdate > 5 * 60 * 1000) { //5 minutes
			lastAuctionAvgUpdate = currentTime - 4 * 60 * 1000; //Try again in 1 minute if updateAvgPrices doesn't succeed
			updateAvgPrices();
		}
		if (currentTime - lastBazaarUpdate > 5 * 60 * 1000) { //5 minutes
			lastBazaarUpdate = currentTime;
			updateBazaar();
		}
		if (currentTime - lastLowestBinUpdate > 2 * 60 * 1000) {
			updateLowestBin();
		}
	}

	private String niceAucId(String aucId) {
		if (aucId.length() != 32) return aucId;

		StringBuilder niceAucId = new StringBuilder();
		niceAucId.append(aucId, 0, 8);
		niceAucId.append("-");
		niceAucId.append(aucId, 8, 12);
		niceAucId.append("-");
		niceAucId.append(aucId, 12, 16);
		niceAucId.append("-");
		niceAucId.append(aucId, 16, 20);
		niceAucId.append("-");
		niceAucId.append(aucId, 20, 32);
		return niceAucId.toString();
	}

	public Set<String> getLowestBinKeySet() {
		if (lowestBins == null) return new HashSet<>();
		HashSet<String> keys = new HashSet<>();
		for (Map.Entry<String, JsonElement> entry : lowestBins.entrySet()) {
			keys.add(entry.getKey());
		}
		return keys;
	}

	public int getLowestBin(String internalname) {
		if (lowestBins != null && lowestBins.has(internalname)) {
			JsonElement e = lowestBins.get(internalname);
			if (e.isJsonPrimitive() && e.getAsJsonPrimitive().isNumber()) {
				return e.getAsInt();
			}
		}
		return -1;
	}

	public void updateLowestBin() {
		manager.hypixelApi.getMyApiGZIPAsync("lowestbin.json.gz", (jsonObject) -> {
			if (lowestBins == null) {
				lowestBins = new JsonObject();
			}
			if (!jsonObject.entrySet().isEmpty()) {
				lastLowestBinUpdate = System.currentTimeMillis();
			}
			for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
				lowestBins.add(entry.getKey(), entry.getValue());
			}
			if (!didFirstUpdate) {
				ItemPriceInformation.updateAuctionableItemsList();
				didFirstUpdate = true;
			}
			GuiPriceGraph.addToCache(lowestBins, false);
		}, () -> {
		});
	}

	private void ahNotification() {
		playerBidsNotified.retainAll(playerBids);
		playerBidsFinishedNotified.retainAll(playerBids);
		if (NotEnoughUpdates.INSTANCE.config.neuAuctionHouse.ahNotification <= 0) {
			return;
		}
		for (String aucid : playerBids) {
			Auction auc = auctionMap.get(aucid);
			if (!playerBidsNotified.contains(aucid)) {
				if (auc != null &&
					auc.end - System.currentTimeMillis() <
						1000 * 60 * NotEnoughUpdates.INSTANCE.config.neuAuctionHouse.ahNotification) {
					ChatComponentText message = new ChatComponentText(
						EnumChatFormatting.YELLOW +
							"The " +
							auc.getStack().getDisplayName() +
							EnumChatFormatting.YELLOW +
							" you have bid on is ending soon! Click here to view.");
					ChatStyle clickEvent = new ChatStyle().setChatClickEvent(
						new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/viewauction " + niceAucId(aucid)));
					clickEvent.setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(
						EnumChatFormatting.YELLOW +
							"View auction")));
					message.setChatStyle(clickEvent);
					Minecraft.getMinecraft().thePlayer.addChatMessage(message);

					playerBidsNotified.add(aucid);
				}
			}
			if (!playerBidsFinishedNotified.contains(aucid)) {
				if (auc != null && auc.end < System.currentTimeMillis()) {
					ChatComponentText message = new ChatComponentText(
						EnumChatFormatting.YELLOW +
							"The " +
							auc.getStack().getDisplayName() +
							EnumChatFormatting.YELLOW +
							" you have bid on (might) have ended! Click here to view.");
					ChatStyle clickEvent = new ChatStyle().setChatClickEvent(
						new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/viewauction " + niceAucId(aucid)));
					clickEvent.setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(
						EnumChatFormatting.YELLOW +
							"View auction")));
					message.setChatStyle(clickEvent);
					Minecraft.getMinecraft().thePlayer.addChatMessage(message);

					playerBidsFinishedNotified.add(aucid);
				}
			}
		}
	}

	public long getLastLowestBinUpdateTime() {
		return lastLowestBinUpdate;
	}

	private final ExecutorService es = Executors.newSingleThreadExecutor();

	private void cleanup() {
		es.submit(() -> {
			try {
				long currTime = System.currentTimeMillis();
				Set<String> toRemove = new HashSet<>();
				for (Map.Entry<String, Auction> entry : auctionMap.entrySet()) {
					long timeToEnd = entry.getValue().end - currTime;
					if (timeToEnd < -120 * 1000) { //2 minutes
						toRemove.add(entry.getKey());
					}
				}
				toRemove.removeAll(playerBids);
				remove(toRemove);
			} catch (ConcurrentModificationException e) {
				lastCleanup = System.currentTimeMillis() - 110 * 1000;
			}
		});
	}

	private void remove(Set<String> toRemove) {
		for (String aucid : toRemove) {
			auctionMap.remove(aucid);
		}
		for (HashMap<Integer, HashSet<String>> extrasMap : extrasToAucIdMap.values()) {
			for (HashSet<String> aucids : extrasMap.values()) {
				for (String aucid : toRemove) {
					aucids.remove(aucid);
				}
			}
		}
		for (HashSet<String> aucids : internalnameToAucIdMap.values()) {
			aucids.removeAll(toRemove);
		}
	}

	private void updatePageTickShort() {
		if (pagesToDownload == null || pagesToDownload.isEmpty()) return;

		if (firstHypixelApiUpdate == 0 || (System.currentTimeMillis() - firstHypixelApiUpdate) % (60 * 1000) > 15 * 1000)
			return;

		JsonObject disable = Constants.DISABLE;
		if (disable != null && disable.has("auctions_new") && disable.get("auctions_new").getAsBoolean()) return;

		while (!pagesToDownload.isEmpty()) {
			try {
				int page = pagesToDownload.pop();
				getPageFromAPI(page);
			} catch (NoSuchElementException ignored) {
			} //Weird race condition?
		}
	}

	private void updatePageTick() {
		JsonObject disable = Constants.DISABLE;
		if (disable != null && disable.has("auctions_new") && disable.get("auctions_new").getAsBoolean()) return;

		if (pagesToDownload == null) {
			getPageFromAPI(0);
		}

		Consumer<JsonObject> process = jsonObject -> {
			if (jsonObject.get("success").getAsBoolean()) {
				JsonArray new_auctions = jsonObject.get("new_auctions").getAsJsonArray();
				for (JsonElement auctionElement : new_auctions) {
					JsonObject auction = auctionElement.getAsJsonObject();
					//System.out.println("New auction " + auction);
					processAuction(auction);
				}
				JsonArray new_bids = jsonObject.get("new_bids").getAsJsonArray();
				for (JsonElement newBidElement : new_bids) {
					JsonObject newBid = newBidElement.getAsJsonObject();
					String newBidUUID = newBid.get("uuid").getAsString();
					//System.out.println("new bid" + newBidUUID);
					int newBidAmount = newBid.get("highest_bid_amount").getAsInt();
					int end = newBid.get("end").getAsInt();
					int bid_count = newBid.get("bid_count").getAsInt();

					Auction auc = auctionMap.get(newBidUUID);
					if (auc != null) {
						//System.out.println("Setting auction " + newBidUUID + " price to " + newBidAmount);
						auc.highest_bid_amount = newBidAmount;
						auc.end = end;
						auc.bid_count = bid_count;
					}
				}
				Set<String> toRemove = new HashSet<>();
				JsonArray removed_auctions = jsonObject.get("removed_auctions").getAsJsonArray();
				for (JsonElement removedAuctionsElement : removed_auctions) {
					String removed = removedAuctionsElement.getAsString();
					toRemove.add(removed);
				}
				remove(toRemove);
			}
		};

		manager.hypixelApi.getMyApiGZIPAsync("auctionLast.json.gz", process, () ->
			System.out.println("Error downloading auction from Moulberry's jank API. :("));

		manager.hypixelApi.getMyApiGZIPAsync("auction.json.gz", jsonObject -> {
			if (jsonObject.get("success").getAsBoolean()) {
				long apiUpdate = (long) jsonObject.get("time").getAsFloat();
				if (lastApiUpdate == apiUpdate) {
					lastAuctionUpdate -= 30 * 1000;
				}
				lastApiUpdate = apiUpdate;

				process.accept(jsonObject);
			}
		}, () -> System.out.println("Error downloading auction from Moulberry's jank API. :("));

	}

	public void calculateStats() {
		try {
			uniqueItems = internalnameToAucIdMap.size();
			Set<String> aucs = new HashSet<>();
			for (HashSet<String> aucids : internalnameToAucIdMap.values()) {
				aucs.addAll(aucids);
			}
			internalnameTaggedAuctions = aucs.size();
			totalTags = extrasToAucIdMap.size();
			aucs = new HashSet<>();
			for (HashMap<Integer, HashSet<String>> extrasMap : extrasToAucIdMap.values()) {
				for (HashSet<String> aucids : extrasMap.values()) {
					aucs.addAll(aucids);
				}
			}
			taggedAuctions = aucs.size();
		} catch (Exception ignored) {
		}
	}

	//String[] rarityArr = new String[] {
	//   "COMMON", "UNCOMMON", "RARE", "EPIC", "LEGENDARY", "MYTHIC", "SPECIAL", "VERY SPECIAL", "SUPREME",
	//};

	public int checkItemType(String lore, boolean contains, String... typeMatches) {
		String[] split = lore.split("\n");
		for (int i = split.length - 1; i >= 0; i--) {
			String line = split[i];
			for (String rarity : Utils.rarityArr) {
				for (int j = 0; j < typeMatches.length; j++) {
					if (contains) {
						if (line.trim().contains(rarity + " " + typeMatches[j])) {
							return j;
						} else if (line.trim().contains(rarity + " DUNGEON " + typeMatches[j])) {
							return j;
						}
					} else {
						if (line.trim().endsWith(rarity + " " + typeMatches[j])) {
							return j;
						} else if (line.trim().endsWith(rarity + " DUNGEON " + typeMatches[j])) {
							return j;
						}
					}
				}
			}
		}
		return -1;
	}

	private final String[] romans = new String[]{
		"I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X", "XI",
		"XII", "XIII", "XIV", "XV", "XVI", "XVII", "XIX", "XX"
	};

	String[] categoryItemType = new String[]{
		"sword", "fishingrod", "pickaxe", "axe",
		"shovel", "petitem", "travelscroll", "reforgestone", "bow"
	};
	String playerUUID = null;

	private void processAuction(JsonObject auction) {
		if (playerUUID == null)
			playerUUID = Minecraft.getMinecraft().thePlayer.getUniqueID().toString().replaceAll("-", "");

		String auctionUuid = auction.get("uuid").getAsString();
		String auctioneerUuid = auction.get("auctioneer").getAsString();
		long end = auction.get("end").getAsLong();
		int starting_bid = auction.get("starting_bid").getAsInt();
		int highest_bid_amount = auction.get("highest_bid_amount").getAsInt();
		int bid_count = auction.get("bids").getAsJsonArray().size();
		boolean bin = false;
		if (auction.has("bin")) {
			bin = auction.get("bin").getAsBoolean();
		}
		String sbCategory = auction.get("category").getAsString();
		String extras = auction.get("extra").getAsString().toLowerCase();
		String item_name = auction.get("item_name").getAsString();
		String item_lore = Utils.fixBrokenAPIColour(auction.get("item_lore").getAsString());
		String item_bytes = auction.get("item_bytes").getAsString();
		String rarity = auction.get("tier").getAsString();
		JsonArray bids = auction.get("bids").getAsJsonArray();

		try {
			NBTTagCompound item_tag;
			try {
				item_tag = CompressedStreamTools.readCompressed(
					new ByteArrayInputStream(Base64.getDecoder().decode(item_bytes)));
			} catch (IOException e) {
				return;
			}

			NBTTagCompound tag = item_tag.getTagList("i", 10).getCompoundTagAt(0).getCompoundTag("tag");
			String internalname = manager.getInternalnameFromNBT(tag);

			NBTTagCompound display = tag.getCompoundTag("display");
			if (display.hasKey("Lore", 9)) {
				NBTTagList loreList = new NBTTagList();
				for (String line : item_lore.split("\n")) {
					loreList.appendTag(new NBTTagString(line));
				}
				display.setTag("Lore", loreList);
			}
			tag.setTag("display", display);
			item_tag.getTagList("i", 10).getCompoundTagAt(0).setTag("tag", tag);

			if (tag.hasKey("ExtraAttributes", 10)) {
				NBTTagCompound ea = tag.getCompoundTag("ExtraAttributes");

				if (ea.hasKey("enchantments", 10)) {
					NBTTagCompound enchantments = ea.getCompoundTag("enchantments");
					for (String key : enchantments.getKeySet()) {
						String enchantname = key.toLowerCase().replace("ultimate_", "").replace("_", " ");
						int enchantlevel = enchantments.getInteger(key);
						String enchantLevelStr;
						if (enchantlevel >= 1 && enchantlevel <= 20) {
							enchantLevelStr = romans[enchantlevel - 1];
						} else {
							enchantLevelStr = String.valueOf(enchantlevel);
						}
						extras = extras.replace(enchantname, enchantname + " " + enchantLevelStr);
					}
				}
			}

			int index = 0;
			for (String str : extras.split(" ")) {
				str = Utils.cleanColour(str).toLowerCase();
				if (str.length() > 0) {
					HashMap<Integer, HashSet<String>> extrasMap = extrasToAucIdMap.computeIfAbsent(str, k -> new HashMap<>());
					HashSet<String> aucids = extrasMap.computeIfAbsent(index, k -> new HashSet<>());
					aucids.add(auctionUuid);
				}
				index++;
			}

			for (int j = 0; j < bids.size(); j++) {
				JsonObject bid = bids.get(j).getAsJsonObject();
				if (bid.get("bidder").getAsString().equalsIgnoreCase(playerUUID)) {
					playerBids.add(auctionUuid);
				}
			}

			int dungeonTier = -1;
			if (checkItemType(item_lore, true, "DUNGEON") >= 0) {
				dungeonTier = 0;
				for (int i = 0; i < item_name.length(); i++) {
					char c = item_name.charAt(i);
					if (c == 0x272A) {
						dungeonTier++;
					}
				}
			}

			//Categories
			String category = sbCategory;
			int itemType = checkItemType(item_lore, true, "SWORD", "FISHING ROD", "PICKAXE",
				"AXE", "SHOVEL", "PET ITEM", "TRAVEL SCROLL", "REFORGE STONE", "BOW"
			);
			if (itemType >= 0 && itemType < categoryItemType.length) {
				category = categoryItemType[itemType];
			}
			if (category.equals("consumables") && extras.contains("enchanted book")) category = "ebook";
			if (category.equals("consumables") && extras.endsWith("potion")) category = "potion";
			if (category.equals("misc") && extras.contains("rune")) category = "rune";
			if (category.equals("misc") && item_lore.split("\n")[0].endsWith("Furniture")) category = "furniture";
			if (item_lore.split("\n")[0].endsWith("Pet") ||
				item_lore.split("\n")[0].endsWith("Mount")) category = "pet";

			Auction auction1 = new Auction(auctioneerUuid, end, starting_bid, highest_bid_amount,
				bid_count, bin, category, rarity, dungeonTier, item_bytes
			);

			if (tag.hasKey("ench")) {
				auction1.enchLevel = 1;
				if (tag.hasKey("ExtraAttributes", 10)) {
					NBTTagCompound ea = tag.getCompoundTag("ExtraAttributes");

					int hotpotatocount = ea.getInteger("hot_potato_count");
					if (hotpotatocount == 10) {
						auction1.enchLevel = 2;
					}
				}
			}

			auctionMap.put(auctionUuid, auction1);
			internalnameToAucIdMap.computeIfAbsent(internalname, k -> new HashSet<>()).add(auctionUuid);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void getPageFromAPI(int page) {
		//System.out.println("downloading page:"+page);
		//System.out.println("Trying to update page: " + page);
		HashMap<String, String> args = new HashMap<>();
		args.put("page", "" + page);
		manager.hypixelApi.getHypixelApiAsync(null, "skyblock/auctions",
			args, jsonObject -> {
				if (jsonObject == null) return;

				if (jsonObject.get("success").getAsBoolean()) {
					if (pagesToDownload == null) {
						int totalPages = jsonObject.get("totalPages").getAsInt();
						pagesToDownload = new LinkedList<>();
						for (int i = 0; i < totalPages + 2; i++) {
							pagesToDownload.add(i);
						}
					}
					if (firstHypixelApiUpdate == 0) {
						firstHypixelApiUpdate = jsonObject.get("lastUpdated").getAsLong();
					}
					activeAuctions = jsonObject.get("totalAuctions").getAsInt();

					long startProcess = System.currentTimeMillis();
					JsonArray auctions = jsonObject.get("auctions").getAsJsonArray();
					for (int i = 0; i < auctions.size(); i++) {
						JsonObject auction = auctions.get(i).getAsJsonObject();

						processAuction(auction);
					}
					processMillis = (int) (System.currentTimeMillis() - startProcess);
				} else {
					pagesToDownload.addLast(page);
				}
			}, () -> pagesToDownload.addLast(page)
		);
	}

	public void updateBazaar() {
		manager.hypixelApi.getHypixelApiAsync(
			NotEnoughUpdates.INSTANCE.config.apiKey.apiKey,
			"skyblock/bazaar",
			new HashMap<>(),
			(jsonObject) -> {
				if (!jsonObject.get("success").getAsBoolean()) return;

				craftCost.clear();
				bazaarJson = new JsonObject();
				JsonObject products = jsonObject.get("products").getAsJsonObject();
				for (Map.Entry<String, JsonElement> entry : products.entrySet()) {
					if (entry.getValue().isJsonObject()) {
						JsonObject productInfo = new JsonObject();

						JsonObject product = entry.getValue().getAsJsonObject();
						JsonObject quickStatus = product.get("quick_status").getAsJsonObject();
						productInfo.addProperty("avg_buy", quickStatus.get("buyPrice").getAsFloat());
						productInfo.addProperty("avg_sell", quickStatus.get("sellPrice").getAsFloat());

						for (JsonElement element : product.get("sell_summary").getAsJsonArray()) {
							if (element.isJsonObject()) {
								JsonObject sellSummaryFirst = element.getAsJsonObject();
								productInfo.addProperty("curr_sell", sellSummaryFirst.get("pricePerUnit").getAsFloat());
								break;
							}
						}

						for (JsonElement element : product.get("buy_summary").getAsJsonArray()) {
							if (element.isJsonObject()) {
								JsonObject sellSummaryFirst = element.getAsJsonObject();
								productInfo.addProperty("curr_buy", sellSummaryFirst.get("pricePerUnit").getAsFloat());
								break;
							}
						}

						bazaarJson.add(entry.getKey().replace(":", "-"), productInfo);
					}
				}
				GuiPriceGraph.addToCache(bazaarJson, true);
			}
		);
	}

	public void updateAvgPrices() {
		manager.hypixelApi.getMyApiGZIPAsync("auction_averages/3day.json.gz", (jsonObject) -> {
			craftCost.clear();
			auctionPricesJson = jsonObject;
			lastAuctionAvgUpdate = System.currentTimeMillis();
		}, () -> {
		});
		manager.hypixelApi.getMyApiGZIPAsync("auction_averages_lbin/1day.json.gz", (jsonObject) ->
			auctionPricesAvgLowestBinJson = jsonObject, () -> {
		});
	}

	public Set<String> getItemAuctionInfoKeySet() {
		if (auctionPricesJson == null) return new HashSet<>();
		HashSet<String> keys = new HashSet<>();
		for (Map.Entry<String, JsonElement> entry : auctionPricesJson.entrySet()) {
			keys.add(entry.getKey());
		}
		return keys;
	}

	public JsonObject getItemAuctionInfo(String internalname) {
		if (auctionPricesJson == null) return null;
		JsonElement e = auctionPricesJson.get(internalname);
		if (e == null) {
			return null;
		}
		return e.getAsJsonObject();
	}

	public float getItemAvgBin(String internalname) {
		if (auctionPricesAvgLowestBinJson == null) return -1;
		JsonElement e = auctionPricesAvgLowestBinJson.get(internalname);
		if (e == null) {
			return -1;
		}
		return Math.round(e.getAsFloat());
	}

	public Set<String> getBazaarKeySet() {
		if (bazaarJson == null) return new HashSet<>();
		HashSet<String> keys = new HashSet<>();
		for (Map.Entry<String, JsonElement> entry : bazaarJson.entrySet()) {
			keys.add(entry.getKey());
		}
		return keys;
	}

	public JsonObject getBazaarInfo(String internalname) {
		if (bazaarJson == null) return null;
		JsonElement e = bazaarJson.get(internalname);
		if (e == null) {
			return null;
		}
		return e.getAsJsonObject();
	}

	private static final List<String> hardcodedVanillaItems = Utils.createList(
		"WOOD_AXE", "WOOD_HOE", "WOOD_PICKAXE", "WOOD_SPADE", "WOOD_SWORD",
		"GOLD_AXE", "GOLD_HOE", "GOLD_PICKAXE", "GOLD_SPADE", "GOLD_SWORD",
		"ROOKIE_HOE"
	);

	public boolean isVanillaItem(String internalname) {
		if (hardcodedVanillaItems.contains(internalname)) return true;

		//Removes trailing numbers and underscores, eg. LEAVES_2-3 -> LEAVES
		String vanillaName = internalname.split("-")[0];
		if (manager.getItemInformation().containsKey(vanillaName)) {
			JsonObject json = manager.getItemInformation().get(vanillaName);
			if (json != null && json.has("vanilla") && json.get("vanilla").getAsBoolean()) return true;
		}
		return Item.itemRegistry.getObject(new ResourceLocation(vanillaName)) != null;
	}

	public static class CraftInfo {
		public boolean fromRecipe = false;
		public boolean vanillaItem = false;
		public float craftCost = -1;
	}

	public CraftInfo getCraftCost(String internalname) {
		return getCraftCost(internalname, new HashSet<>());
	}

	/**
	 * Recursively calculates the cost of crafting an item from raw materials.
	 */
	private CraftInfo getCraftCost(String internalname, Set<String> visited) {
		if (craftCost.containsKey(internalname)) return craftCost.get(internalname);
		if (visited.contains(internalname)) return null;
		visited.add(internalname);

		boolean vanillaItem = isVanillaItem(internalname);
		float craftCost = Float.POSITIVE_INFINITY;

		JsonObject auctionInfo = getItemAuctionInfo(internalname);
		float lowestBin = getLowestBin(internalname);
		JsonObject bazaarInfo = getBazaarInfo(internalname);

		if (bazaarInfo != null && bazaarInfo.get("curr_buy") != null) {
			craftCost = bazaarInfo.get("curr_buy").getAsFloat();
		}
		//Don't use auction prices for vanilla items cuz people like to transfer money, messing up the cost of vanilla items.
		if (!vanillaItem) {
			if (lowestBin > 0) {
				craftCost = Math.min(lowestBin, craftCost);
			} else if (auctionInfo != null) {
				float auctionPrice = auctionInfo.get("price").getAsFloat() / auctionInfo.get("count").getAsInt();
				craftCost = Math.min(auctionPrice, craftCost);
			}
		}

		Set<NeuRecipe> recipes = manager.getRecipesFor(internalname);
		boolean fromRecipe = false;
		if (recipes != null)
			RECIPE_ITER:
				for (NeuRecipe recipe : recipes) {
					if (recipe.hasVariableCost() || !recipe.shouldUseForCraftCost()) continue;
					float craftPrice = 0;
					for (Ingredient i : recipe.getIngredients()) {
						if (i.isCoins()) {
							craftPrice += i.getCount();
							continue;
						}
						CraftInfo ingredientCraftCost = getCraftCost(i.getInternalItemId(), visited);
						if (ingredientCraftCost == null)
							continue RECIPE_ITER; // Skip recipes with items further up the chain
						craftPrice += ingredientCraftCost.craftCost * i.getCount();
					}
					int resultCount = 0;
					for (Ingredient item : recipe.getOutputs())
						if (item.getInternalItemId().equals(internalname))
							resultCount += item.getCount();

					if (resultCount == 0)
						continue;
					float craftPricePer = craftPrice / resultCount;
					if (craftPricePer < craftCost) {
						fromRecipe = true;
						craftCost = craftPricePer;
					}
				}
		visited.remove(internalname);
		if (Float.isInfinite(craftCost)) {
			return null;
		}
		CraftInfo craftInfo = new CraftInfo();
		craftInfo.vanillaItem = vanillaItem;
		craftInfo.craftCost = craftCost;
		craftInfo.fromRecipe = fromRecipe;
		this.craftCost.put(internalname, craftInfo);
		return craftInfo;
	}

	/**
	 * Calculates the cost of enchants + other price modifiers such as pet xp, midas price, etc.
	 */
	public float getCostOfEnchants(String internalname, NBTTagCompound tag) {
		float costOfEnchants = 0;
		if (true) return 0;

		JsonObject info = getItemAuctionInfo(internalname);
		if (info == null || !info.has("price")) {
			return 0;
		}
		if (auctionPricesJson == null || !auctionPricesJson.has("ench_prices") || !auctionPricesJson.has("ench_maximums")) {
			return 0;
		}
		JsonObject ench_prices = auctionPricesJson.getAsJsonObject("ench_prices");
		JsonObject ench_maximums = auctionPricesJson.getAsJsonObject("ench_maximums");
		if (!ench_prices.has(internalname) || !ench_maximums.has(internalname)) {
			return 0;
		}
		JsonObject iid_variables = ench_prices.getAsJsonObject(internalname);
		float ench_maximum = ench_maximums.get(internalname).getAsFloat();

		int enchants = 0;
		float price = getItemAuctionInfo(internalname).get("price").getAsFloat();
		if (tag.hasKey("ExtraAttributes")) {
			NBTTagCompound ea = tag.getCompoundTag("ExtraAttributes");
			if (ea.hasKey("enchantments")) {

				NBTTagCompound enchs = ea.getCompoundTag("enchantments");
				for (String ench : enchs.getKeySet()) {
					enchants++;
					int level = enchs.getInteger(ench);

					for (Map.Entry<String, JsonElement> entry : iid_variables.entrySet()) {
						if (matchEnch(ench, level, entry.getKey())) {
							costOfEnchants += entry.getValue().getAsJsonObject().get("A").getAsFloat() * price +
								entry.getValue().getAsJsonObject().get("B").getAsFloat();
							break;
						}
					}
				}
			}
		}
		return costOfEnchants;
	}

	/**
	 * Checks whether a certain enchant (ench name + lvl) matches an enchant id
	 * eg. PROTECTION_GE6 will match -> ench_name = PROTECTION, lvl >= 6
	 */
	private boolean matchEnch(String ench, int level, String id) {
		if (!id.contains(":")) {
			return false;
		}

		String idEnch = id.split(":")[0];
		String idLevel = id.split(":")[1];

		if (!ench.equalsIgnoreCase(idEnch)) {
			return false;
		}

		if (String.valueOf(level).equalsIgnoreCase(idLevel)) {
			return true;
		}

		if (idLevel.startsWith("LE")) {
			int idLevelI = Integer.parseInt(idLevel.substring(2));
			return level <= idLevelI;
		} else if (idLevel.startsWith("GE")) {
			int idLevelI = Integer.parseInt(idLevel.substring(2));
			return level >= idLevelI;
		}

		return false;
	}

    /*ScheduledExecutorService auctionUpdateSES = Executors.newSingleThreadScheduledExecutor();
    private AtomicInteger auctionUpdateId = new AtomicInteger(0);
    public void updateAuctions() {
        HashMap<Integer, JsonObject> pages = new HashMap<>();

        HashMap<String, String> args = new HashMap<>();
        args.put("page", "0");
        AtomicInteger totalPages = new AtomicInteger(1);
        AtomicInteger currentPages = new AtomicInteger(0);
        manager.hypixelApi.getHypixelApiAsync(NotEnoughUpdates.INSTANCE.config.apiKey.apiKey, "skyblock/auctions",
                args, jsonObject -> {
                    if (jsonObject.get("success").getAsBoolean()) {
                        pages.put(0, jsonObject);
                        totalPages.set(jsonObject.get("totalPages").getAsInt());
                        currentPages.incrementAndGet();

                        for (int i = 1; i < totalPages.get(); i++) {
                            int j = i;
                            HashMap<String, String> args2 = new HashMap<>();
                            args2.put("page", "" + i);
                            manager.hypixelApi.getHypixelApiAsync(NotEnoughUpdates.INSTANCE.config.apiKey.apiKey, "skyblock/auctions",
                                    args2, jsonObject2 -> {
                                        if (jsonObject2.get("success").getAsBoolean()) {
                                            pages.put(j, jsonObject2);
                                            currentPages.incrementAndGet();
                                        } else {
                                            currentPages.incrementAndGet();
                                        }
                                    }
                            );
                        }
                    }
                }
        );

        long startTime = System.currentTimeMillis();

        int currentAuctionUpdateId = auctionUpdateId.incrementAndGet();

        auctionUpdateSES.schedule(new Runnable() {
            public void run() {
                if(auctionUpdateId.get() != currentAuctionUpdateId) return;
                System.out.println(currentPages.get() + "/" + totalPages.get());
                if (System.currentTimeMillis() - startTime > 20000) return;

                if (currentPages.get() == totalPages.get()) {
                    TreeMap<String, Auction> auctionItemsTemp = new TreeMap<>();

                    for (int pageNum : pages.keySet()) {
                        System.out.println(pageNum + "/" + pages.size());
                        JsonObject page = pages.get(pageNum);
                        JsonArray auctions = page.get("auctions").getAsJsonArray();
                        for (int i = 0; i < auctions.size(); i++) {
                            JsonObject auction = auctions.get(i).getAsJsonObject();

                            String auctionUuid = auction.get("uuid").getAsString();
                            String auctioneerUuid = auction.get("auctioneer").getAsString();
                            long end = auction.get("end").getAsLong();
                            int starting_bid = auction.get("starting_bid").getAsInt();
                            int highest_bid_amount = auction.get("highest_bid_amount").getAsInt();
                            int bid_count = auction.get("bids").getAsJsonArray().size();
                            boolean bin = false;
                            if(auction.has("bin")) {
                                bin = auction.get("bin").getAsBoolean();
                            }
                            String category = auction.get("category").getAsString();
                            String extras = auction.get("item_lore").getAsString().replaceAll("\n"," ") + " " +
                                    auction.get("extra").getAsString();
                            String item_bytes = auction.get("item_bytes").getAsString();

                            auctionItemsTemp.put(auctionUuid, new Auction(auctioneerUuid, end, starting_bid, highest_bid_amount,
                                    bid_count, bin, category, extras, item_bytes));
                        }
                    }
                    auctionItems = auctionItemsTemp;
                    customAH.updateSearch();
                    return;
                }

                auctionUpdateSES.schedule(this, 1000L, TimeUnit.MILLISECONDS);
            }
        }, 3000L, TimeUnit.MILLISECONDS);
    }*/
}
