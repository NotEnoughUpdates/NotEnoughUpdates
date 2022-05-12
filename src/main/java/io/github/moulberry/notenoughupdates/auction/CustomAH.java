package io.github.moulberry.notenoughupdates.auction;

import com.google.gson.JsonObject;
import io.github.moulberry.notenoughupdates.ItemPriceInformation;
import io.github.moulberry.notenoughupdates.NEUManager;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiEditSign;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.github.moulberry.notenoughupdates.util.GuiTextures.auction_accept;
import static io.github.moulberry.notenoughupdates.util.GuiTextures.auction_price;
import static io.github.moulberry.notenoughupdates.util.GuiTextures.auction_view;
import static io.github.moulberry.notenoughupdates.util.GuiTextures.auction_view_buttons;

public class CustomAH extends Gui {
	private enum PriceFilter {
		Greater,
		Less,
		Equal
	}

	private static final ResourceLocation creativeTabSearch =
		new ResourceLocation("textures/gui/container/creative_inventory/tab_item_search.png");
	private static final ResourceLocation creativeInventoryTabs =
		new ResourceLocation("textures/gui/container/creative_inventory/tabs.png");

	private HashSet<String> auctionIds = new HashSet<>();
	private List<String> sortedAuctionIds = new ArrayList<>();

	private boolean scrollClicked = false;

	private long lastUpdateSearch;
	private long lastSearchFieldUpdate;
	private boolean shouldUpdateSearch = false;
	private boolean shouldSortItems = false;

	private int startingBid = 0;
	private String currentAucId = null;

	private int clickedMainCategory = -1;
	private int clickedSubCategory = -1;

	private GuiTextField searchField = null;
	private GuiTextField priceField = null;
	private GuiTextField priceFilterField = null;
	private GuiTextField binPriceFilterField = null;

	private final int binPriceFilterYOffset = 86;

	private boolean renderOverAuctionView = false;
	private long lastRenderDisable = 0;

	private long currAucIdSetTimer = 0;
	private long resetCurrAucIdTimer = 0;

	private int eventButton;
	private long lastMouseEvent;
	public long lastOpen;
	public long lastGuiScreenSwitch;
	private PriceFilter currentPriceFilterType = PriceFilter.Greater;
	private PriceFilter currentBinPriceFilterType = PriceFilter.Greater;

	private final int splits = 2;

	public String latestBid;
	public long latestBidMillis;

	private final int ySplit = 35;
	private final int ySplitSize = 18;

	private float scrollAmount;

	public int guiLeft = -1;
	public int guiTop = -1;

	private final Category CATEGORY_SWORD = new Category("sword", "Swords", "diamond_sword");
	private final Category CATEGORY_ARMOR = new Category("armor", "Armor", "diamond_chestplate");
	private final Category CATEGORY_BOWS = new Category("bow", "Bows", "bow");
	private final Category CATEGORY_ACCESSORIES = new Category("accessories", "Accessories", "diamond");

	private final Category CATEGORY_FISHING_ROD = new Category("fishingrod", "Fishing Rods", "fishing_rod");
	private final Category CATEGORY_PICKAXE = new Category("pickaxe", "Pickaxes", "iron_pickaxe");
	private final Category CATEGORY_AXE = new Category("axe", "Axes", "iron_axe");
	private final Category CATEGORY_SHOVEL = new Category("shovel", "Shovels", "iron_shovel");

	private final Category CATEGORY_PET_ITEM = new Category("petitem", "Pet Items", "lead");

	private final Category CATEGORY_EBOOKS = new Category("ebook", "Enchanted Books", "enchanted_book");
	private final Category CATEGORY_POTIONS = new Category("potion", "Potions", "potion");
	private final Category CATEGORY_TRAVEL_SCROLLS = new Category("travelscroll", "Travel Scrolls", "map");

	private final Category CATEGORY_REFORGE_STONES = new Category("reforgestone", "Reforge Stones", "anvil");
	private final Category CATEGORY_RUNES = new Category("rune", "Runes", "magma_cream");
	private final Category CATEGORY_FURNITURE = new Category("furniture", "Furniture", "armor_stand");

	private final Category CATEGORY_COMBAT = new Category("weapon", "Combat", "golden_sword", CATEGORY_SWORD,
		CATEGORY_BOWS, CATEGORY_ARMOR, CATEGORY_ACCESSORIES
	);
	private final Category CATEGORY_TOOL = new Category(
		"",
		"Tools",
		"diamond_pickaxe",
		CATEGORY_FISHING_ROD,
		CATEGORY_PICKAXE,
		CATEGORY_AXE,
		CATEGORY_SHOVEL
	);
	private final Category CATEGORY_PET = new Category("pet", "Pets", "bone", CATEGORY_PET_ITEM);
	private final Category CATEGORY_CONSUMABLES = new Category(
		"consumables",
		"Consumables",
		"apple",
		CATEGORY_EBOOKS,
		CATEGORY_POTIONS,
		CATEGORY_TRAVEL_SCROLLS
	);
	private final Category CATEGORY_BLOCKS = new Category("blocks", "Blocks", "cobblestone");
	private final Category CATEGORY_MISC = new Category("misc", "Misc", "stick", CATEGORY_REFORGE_STONES, CATEGORY_RUNES,
		CATEGORY_FURNITURE
	);

	private final Category[] mainCategories = new Category[]{
		CATEGORY_COMBAT, CATEGORY_TOOL, CATEGORY_PET,
		CATEGORY_CONSUMABLES, CATEGORY_BLOCKS, CATEGORY_MISC
	};

	private static final int SORT_MODE_HIGH = 0;
	private static final int SORT_MODE_LOW = 1;
	private static final int SORT_MODE_SOON = 2;

	//    private static final String[] rarities = {"COMMON", "UNCOMMON", "RARE", "EPIC",
//            "LEGENDARY", "MYTHIC", "SPECIAL", "VERY SPECIAL", "SUPREME", "DIVINE"};
	private static final String[] rarityColours = {
		"" + EnumChatFormatting.WHITE,
		"" + EnumChatFormatting.GREEN, "" + EnumChatFormatting.BLUE, "" + EnumChatFormatting.DARK_PURPLE,
		"" + EnumChatFormatting.GOLD, "" + EnumChatFormatting.LIGHT_PURPLE, "" + EnumChatFormatting.RED,
		"" + EnumChatFormatting.RED, "" + EnumChatFormatting.AQUA, "" + EnumChatFormatting.AQUA
	};

	private static final int BIN_FILTER_ALL = 0;
	private static final int BIN_FILTER_BIN = 1;
	private static final int BIN_FILTER_AUC = 2;

	private static final int ENCH_FILTER_ALL = 0;
	private static final int ENCH_FILTER_CLEAN = 1;
	private static final int ENCH_FILTER_ENCH = 2;
	private static final int ENCH_FILTER_ENCHHPB = 3;

	private static final int DUNGEON_FILTER_ALL = 0;
	private static final int DUNGEON_FILTER_DUNGEON = 1;
	private static final int DUNGEON_FILTER_1 = 2;
	private static final int DUNGEON_FILTER_2 = 3;
	private static final int DUNGEON_FILTER_3 = 4;
	private static final int DUNGEON_FILTER_4 = 5;
	private static final int DUNGEON_FILTER_5 = 6;

	private int dungeonFilter = DUNGEON_FILTER_ALL;
	private int sortMode = SORT_MODE_HIGH;
	private int rarityFilter = -1;
	private boolean filterMyAuctions = false;
	private int binFilter = BIN_FILTER_ALL;
	private int enchFilter = ENCH_FILTER_ALL;

	private static final ItemStack DUNGEON_SORT = Utils.createItemStack(
		Item.getItemFromBlock(Blocks.deadbush),
		EnumChatFormatting.GREEN + "Dungeon Sorting"
	);
	private static final ItemStack CONTROL_SORT = Utils.createItemStack(
		Item.getItemFromBlock(Blocks.hopper),
		EnumChatFormatting.GREEN + "Sort"
	);
	private static final ItemStack CONTROL_TIER = Utils.createItemStack(
		Items.ender_eye,
		EnumChatFormatting.GREEN + "Item Tier"
	);
	private static final ItemStack CONTROL_MYAUC = Utils.createItemStack(
		Items.gold_ingot,
		EnumChatFormatting.GREEN + "My Auctions"
	);
	private static final ItemStack CONTROL_BIN = Utils.createItemStack(
		Item.getItemFromBlock(Blocks.golden_rail),
		EnumChatFormatting.GREEN + "BIN Filter"
	);
	private static final ItemStack CONTROL_ENCH = Utils.createItemStack(
		Items.enchanted_book,
		EnumChatFormatting.GREEN + "Enchant Filter"
	);
	private static final ItemStack CONTROL_STATS = Utils.createItemStack(
		Item.getItemFromBlock(Blocks.command_block),
		EnumChatFormatting.GREEN + "Stats for nerds"
	);
	private final ItemStack[] controls =
		{DUNGEON_SORT, CONTROL_SORT, CONTROL_TIER, null, CONTROL_MYAUC, null, CONTROL_BIN, CONTROL_ENCH, CONTROL_STATS};

	private final NEUManager manager;

	public CustomAH(NEUManager manager) {
		this.manager = manager;
	}

	public void clearSearch() {
		if (searchField == null || priceField == null) init();
		if (System.currentTimeMillis() - lastOpen < 1000) Mouse.setGrabbed(false);

		//sortMode = SORT_MODE_HIGH;
		rarityFilter = -1;
		filterMyAuctions = false;
		//binFilter = BIN_FILTER_ALL;
		enchFilter = ENCH_FILTER_ALL;
		dungeonFilter = DUNGEON_FILTER_ALL;

		searchField.setText("");
		searchField.setFocused(true);
		priceField.setText("");
	}

	public void setSearch(String search) {
		searchField.setText(search);
		updateSearch();
	}

	public void tick() {
		if (!NotEnoughUpdates.INSTANCE.config.neuAuctionHouse.enableNeuAuctionHouse) return;
		if (Minecraft.getMinecraft().currentScreen instanceof CustomAHGui || renderOverAuctionView) {
			if (shouldUpdateSearch) updateSearch();
			if (shouldSortItems) {
				sortItems();
				shouldSortItems = false;
			}
		}
	}

	public static class Category {
		public String categoryMatch;
		public Category[] subcategories;
		public String displayName;
		public ItemStack displayItem;

		public Category(String categoryMatch, String displayName, String displayItem, Category... subcategories) {
			this.categoryMatch = categoryMatch;
			this.subcategories = subcategories;
			this.displayName = displayName;
			this.displayItem = new ItemStack(Item.getByNameOrId(displayItem));
		}

		public String[] getTotalCategories() {
			String[] categories = new String[1 + subcategories.length];
			categories[0] = categoryMatch;

			for (int i = 0; i < subcategories.length; i++) {
				categories[i + 1] = subcategories[i].categoryMatch;
			}
			return categories;
		}
	}

	private void init() {
		FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
		this.searchField = new GuiTextField(0, fr, this.guiLeft + 82, this.guiTop + 6,
			84, fr.FONT_HEIGHT
		);
		this.priceField = new GuiTextField(1, fr, this.guiLeft + 82, this.guiTop + 6,
			84, fr.FONT_HEIGHT
		);
		this.priceFilterField = new GuiTextField(2, fr, this.guiLeft + 82, this.guiTop + 6,
			84, fr.FONT_HEIGHT
		);
		this.binPriceFilterField = new GuiTextField(3, fr, this.guiLeft + 82, this.guiTop - 6, 84, fr.FONT_HEIGHT);

		this.searchField.setMaxStringLength(30);
		this.searchField.setEnableBackgroundDrawing(false);
		this.searchField.setTextColor(16777215);
		this.searchField.setVisible(true);
		this.searchField.setCanLoseFocus(true);
		this.searchField.setFocused(true);
		this.searchField.setText("");

		this.priceField.setMaxStringLength(10);
		this.priceField.setEnableBackgroundDrawing(false);
		this.priceField.setTextColor(16777215);
		this.priceField.setVisible(true);
		this.priceField.setCanLoseFocus(true);
		this.priceField.setFocused(false);
		this.priceField.setText("");

		this.priceFilterField.setMaxStringLength(10);
		this.priceFilterField.setEnableBackgroundDrawing(false);
		this.priceFilterField.setTextColor(16777215);
		this.priceFilterField.setVisible(true);
		this.priceFilterField.setCanLoseFocus(true);
		this.priceFilterField.setFocused(false);
		this.priceFilterField.setText("");

		this.binPriceFilterField.setMaxStringLength(10);
		this.binPriceFilterField.setEnableBackgroundDrawing(false);
		this.binPriceFilterField.setTextColor(16777215);
		this.binPriceFilterField.setVisible(true);
		this.binPriceFilterField.setCanLoseFocus(true);
		this.binPriceFilterField.setFocused(false);
		this.binPriceFilterField.setText("");
	}

	public boolean isRenderOverAuctionView() {
		return renderOverAuctionView || (System.currentTimeMillis() - lastRenderDisable) < 500;
	}

	public void setRenderOverAuctionView(boolean renderOverAuctionView) {
		if (this.renderOverAuctionView && !renderOverAuctionView) lastRenderDisable = System.currentTimeMillis();
		this.renderOverAuctionView = renderOverAuctionView;
	}

	public int getXSize() {
		return 195;
	}

	public int getYSize() {
		return 136 + ySplitSize * splits;
	}

	private String prettyTime(long millis) {
		long seconds = millis / 1000 % 60;
		long minutes = (millis / 1000 / 60) % 60;
		long hours = (millis / 1000 / 60 / 60) % 24;
		long days = (millis / 1000 / 60 / 60 / 24);

		String endsIn = "";
		if (millis < 0) {
			endsIn += "Ended!";
		} else if (minutes == 0 && hours == 0 && days == 0) {
			endsIn += seconds + "s";
		} else if (hours == 0 && days == 0) {
			endsIn += minutes + "m" + seconds + "s";
		} else if (days == 0) {
			if (hours <= 6) {
				endsIn += hours + "h" + minutes + "m" + seconds + "s";
			} else {
				endsIn += hours + "h";
			}
		} else {
			endsIn += days + "d" + hours + "h";
		}

		return endsIn;
	}

	public List<String> getTooltipForAucId(String aucId) {
		APIManager.Auction auc = manager.auctionManager.getAuctionItems().get(aucId);

		List<String> tooltip = new ArrayList<>();

		for (String line : auc.getStack().getTooltip(Minecraft.getMinecraft().thePlayer, false)) {
			tooltip.add(EnumChatFormatting.GRAY + line);
		}

		long timeUntilEnd = auc.end - System.currentTimeMillis();
		String endsIn = EnumChatFormatting.YELLOW + prettyTime(timeUntilEnd);

		NumberFormat format = NumberFormat.getInstance(Locale.US);

		tooltip.add(EnumChatFormatting.DARK_GRAY + "" + EnumChatFormatting.STRIKETHROUGH + "-----------------");
		tooltip.add(EnumChatFormatting.GRAY + "Seller: [CLICK TO SEE]");

		if (auc.bin) {
			tooltip.add(EnumChatFormatting.GRAY + "Buy it now: " +
				EnumChatFormatting.GOLD + format.format(auc.starting_bid));
		} else {
			if (auc.bid_count > 0) {
				tooltip.add(EnumChatFormatting.GRAY + "Bids: " + EnumChatFormatting.GREEN + auc.bid_count + " bids");
				tooltip.add("");
				tooltip.add(EnumChatFormatting.GRAY + "Top bid: " +
					EnumChatFormatting.GOLD + format.format(auc.highest_bid_amount));
				tooltip.add(EnumChatFormatting.GRAY + "Bidder: [CLICK TO SEE]");
			} else {
				tooltip.add(EnumChatFormatting.GRAY + "Starting bid: " +
					EnumChatFormatting.GOLD + format.format(auc.starting_bid));
			}
		}

		if (NotEnoughUpdates.INSTANCE.config.tooltipTweaks.showPriceInfoAucItem) {
			String internalname = NotEnoughUpdates.INSTANCE.manager.getInternalNameForItem(auc.getStack());
			if (internalname != null) {
				if (!Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) && !Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
					tooltip.add("");
					tooltip.add(EnumChatFormatting.GRAY + "[SHIFT for Price Info]");
				} else {
					ItemPriceInformation.addToTooltip(tooltip, internalname, auc.getStack());
				}
			}
		}

		tooltip.add("");
		tooltip.add(EnumChatFormatting.GRAY + "Ends in: " + endsIn);
		tooltip.add("");
		tooltip.add(EnumChatFormatting.YELLOW + "Click to inspect!");

		return tooltip;
	}

	public boolean isEditingPrice() {
		return Minecraft.getMinecraft().currentScreen instanceof GuiEditSign;
	}

	private boolean isGuiFiller(ItemStack stack) {
		return stack == null || !stack.hasTagCompound() || !stack.getTagCompound().hasKey("AttributeModifiers");
	}

	private void drawCategorySide(int i) {
		boolean clicked = i == clickedSubCategory;

		int x = guiLeft - 28;
		int y = guiTop + 17 + 28 * (i + 1);
		float uMin = 28 / 256f;
		float uMax = 56 / 256f;
		float vMin = 0 + (clicked ? 32 / 256f : 0);
		float vMax = 32 / 256f + (clicked ? 32 / 256f : 0);
		float catWidth = 32;
		float catHeight = 28;

		GlStateManager.enableTexture2D();
		GlStateManager.enableBlend();
		GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);

		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);

		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer worldrenderer = tessellator.getWorldRenderer();
		worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
		worldrenderer
			.pos(x, y + catHeight, 0.0D)
			.tex(uMax, vMin).endVertex();
		worldrenderer
			.pos(x + catWidth, y + catHeight, 0.0D)
			.tex(uMax, vMax).endVertex();
		worldrenderer
			.pos(x + catWidth, y, 0.0D)
			.tex(uMin, vMax).endVertex();
		worldrenderer
			.pos(x, y, 0.0D)
			.tex(uMin, vMin).endVertex();
		tessellator.draw();

		GlStateManager.disableBlend();
	}

	private final HashMap<Pattern, Long> timeParseMap = new HashMap<Pattern, Long>() {{
		Pattern dayPattern = Pattern.compile("([0-9]+)d");
		Pattern hourPattern = Pattern.compile("([0-9]+)h");
		Pattern minutePattern = Pattern.compile("([0-9]+)m");
		Pattern secondPattern = Pattern.compile("([0-9]+)s");

		put(dayPattern, 24 * 60 * 60 * 1000L);
		put(hourPattern, 60 * 60 * 1000L);
		put(minutePattern, 60 * 1000L);
		put(secondPattern, 1000L);
	}};

	public long prettyTimeToMillis(String endsInStr) {
		if (endsInStr != null) {
			long timeUntilEnd = 0;

			String timeStr = Utils.cleanColour(endsInStr);

			for (Map.Entry<Pattern, Long> timeEntry : timeParseMap.entrySet()) {
				Matcher matcher = timeEntry.getKey().matcher(timeStr);
				if (matcher.find()) {
					String days = matcher.group(1);
					timeUntilEnd += Long.parseLong(days) * timeEntry.getValue();
				}
			}

			return timeUntilEnd;
		}

		return -1;
	}

	public String findStrStart(ItemStack stack, String toFind) {
		if (stack.hasTagCompound()) {
			//§7Ends in:
			for (String line : manager.getLoreFromNBT(stack.getTagCompound())) {
				if (line.trim().startsWith(toFind)) {
					return line.substring(toFind.length());
				}
			}
		}

		return null;
	}

	public String findEndsInStr(ItemStack stack) {
		return findStrStart(stack, EnumChatFormatting.GRAY + "Ends in: ");
	}

	public void drawScreen(int mouseX, int mouseY) {
		if (System.currentTimeMillis() - lastOpen < 1000) Mouse.setGrabbed(false);

		ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
		int width = scaledResolution.getScaledWidth();
		int height = scaledResolution.getScaledHeight();

		boolean hasPopup = false;

		if (searchField == null || priceField == null || priceFilterField == null) init();

		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

		guiLeft = (width - getXSize()) / 2;
		guiTop = (height - getYSize()) / 2;
		this.searchField.xPosition = guiLeft + 82;
		this.searchField.yPosition = guiTop + 6;

		if (!isEditingPrice()) priceField.setText("IAUSHDIUAH");

		if ((Minecraft.getMinecraft().currentScreen instanceof GuiChest ||
			Minecraft.getMinecraft().currentScreen instanceof GuiEditSign) && currentAucId == null) {
			Minecraft.getMinecraft().displayGuiScreen(null);
		}

		List<String> tooltipToRender = null;
		if (Minecraft.getMinecraft().currentScreen instanceof GuiChest) {
			resetCurrAucIdTimer = System.currentTimeMillis();
			GuiChest auctionView = (GuiChest) Minecraft.getMinecraft().currentScreen;
			ContainerChest container = (ContainerChest) auctionView.inventorySlots;
			String containerName = container.getLowerChestInventory().getDisplayName().getUnformattedText();

			float slideAmount = 1 - Math.max(0, Math.min(1, (System.currentTimeMillis() - lastGuiScreenSwitch) / 200f));
			int auctionViewLeft = guiLeft + getXSize() + 4 - (int) (slideAmount * (78 + 4));
			if (containerName.trim().equals("Auction View") || containerName.trim().equals("BIN Auction View")) {
				hasPopup = true;
				Minecraft.getMinecraft().getTextureManager().bindTexture(auction_view);
				this.drawTexturedModalRect(auctionViewLeft, guiTop, 0, 0, 78, 172);

				if (auctionViewLeft + 31 > guiLeft + getXSize()) {
					try {
						ItemStack topStack = auctionView.inventorySlots.getSlot(13).getStack();
						ItemStack leftStack = auctionView.inventorySlots.getSlot(29).getStack();
						ItemStack middleStack = auctionView.inventorySlots.getSlot(31).getStack();
						ItemStack rightStack = auctionView.inventorySlots.getSlot(33).getStack();

						boolean isBin = isGuiFiller(leftStack) || isGuiFiller(leftStack);

						if (isBin) {
							leftStack = middleStack;
							middleStack = null;
						}

						String endsInStr = findEndsInStr(topStack);
						if (endsInStr != null) {
							long auctionViewEndsIn = prettyTimeToMillis(endsInStr);
							if (auctionViewEndsIn > 0) {
								if (System.currentTimeMillis() - currAucIdSetTimer > 1000) {
									APIManager.Auction auc = manager.auctionManager.getAuctionItems().get(currentAucId);
									if (auc != null) {
										auc.end = auctionViewEndsIn + lastGuiScreenSwitch;
									}
								}
								endsInStr = EnumChatFormatting.DARK_PURPLE + prettyTime(
									auctionViewEndsIn + lastGuiScreenSwitch - System.currentTimeMillis());
							}
							Utils.drawStringCenteredScaledMaxWidth(endsInStr, Minecraft.getMinecraft().fontRendererObj,
								auctionViewLeft + 39, guiTop + 20, false, 70, 4210752
							);
						}

						Utils.drawItemStack(leftStack, auctionViewLeft + 31, guiTop + 100);

						if (!isGuiFiller(leftStack)) {
							NBTTagCompound tag = leftStack.getTagCompound();
							NBTTagCompound display = tag.getCompoundTag("display");
							if (display.hasKey("Lore", 9)) {
								NBTTagList list = display.getTagList("Lore", 8);
								String line2 = list.getStringTagAt(1);
								line2 = Utils.cleanColour(line2);
								StringBuilder priceNumbers = new StringBuilder();
								for (int i = 0; i < line2.length(); i++) {
									char c = line2.charAt(i);
									if ((int) c >= 48 && (int) c <= 57) {
										priceNumbers.append(c);
									}
								}
								if (priceNumbers.length() > 0) {
									startingBid = Integer.parseInt(priceNumbers.toString());
								}
							}
						}

						Utils.drawItemStack(topStack, auctionViewLeft + 31, guiTop + 35);

						if (!isGuiFiller(middleStack)) {
							Minecraft.getMinecraft().getTextureManager().bindTexture(auction_view_buttons);
							boolean hover = mouseX > auctionViewLeft + 31 && mouseX < auctionViewLeft + 31 + 16 &&
								mouseY > guiTop + 126 && mouseY < guiTop + 126 + 16;
							this.drawTexturedModalRect(auctionViewLeft + 31, guiTop + 126, hover ? 16 : 0, 0, 16, 16);
						} else {
							middleStack = null;
						}

						if (mouseX > auctionViewLeft + 31 && mouseX < auctionViewLeft + 31 + 16) {
							if (mouseY > guiTop + 35 && mouseY < guiTop + 35 + 16) {
								if (topStack != null) {
									tooltipToRender = topStack.getTooltip(Minecraft.getMinecraft().thePlayer, false);
									tooltipToRender.add("");
									tooltipToRender.add(EnumChatFormatting.YELLOW + "Click to copy seller name!");
								}
							} else if (mouseY > guiTop + 100 && mouseY < guiTop + 100 + 16) {
								if (leftStack != null)
									tooltipToRender = leftStack.getTooltip(Minecraft.getMinecraft().thePlayer, false);
							} else if (mouseY > guiTop + 61 && mouseY < guiTop + 61 + 16) {
								tooltipToRender = new ArrayList<>();
								APIManager.Auction auc = manager.auctionManager.getAuctionItems().get(currentAucId);
								if (auc != null) {
									tooltipToRender.add(EnumChatFormatting.WHITE + "Price Info");

									String internalname = manager.getInternalNameForItem(auc.getStack());
									JsonObject auctionInfo = manager.auctionManager.getItemAuctionInfo(internalname);
									JsonObject bazaarInfo = manager.auctionManager.getBazaarInfo(internalname);

									boolean hasAuctionPrice = auctionInfo != null;
									boolean hasBazaarPrice = bazaarInfo != null;

									int lowestBin = manager.auctionManager.getLowestBin(internalname);

									NumberFormat format = NumberFormat.getInstance(Locale.US);

									APIManager.CraftInfo craftCost = manager.auctionManager.getCraftCost(internalname);

									if (lowestBin > 0) {
										tooltipToRender.add(
											EnumChatFormatting.YELLOW.toString() + EnumChatFormatting.BOLD + "Lowest BIN: " +
												EnumChatFormatting.GOLD + EnumChatFormatting.BOLD + format.format(lowestBin) + " coins");
									}
									if (hasBazaarPrice) {
										int bazaarBuyPrice = (int) bazaarInfo.get("avg_buy").getAsFloat();
										tooltipToRender.add(
											EnumChatFormatting.YELLOW.toString() + EnumChatFormatting.BOLD + "Bazaar Buy: " +
												EnumChatFormatting.GOLD + EnumChatFormatting.BOLD + format.format(bazaarBuyPrice) + " coins");
										int bazaarSellPrice = (int) bazaarInfo.get("avg_sell").getAsFloat();
										tooltipToRender.add(
											EnumChatFormatting.YELLOW.toString() + EnumChatFormatting.BOLD + "Bazaar Sell: " +
												EnumChatFormatting.GOLD + EnumChatFormatting.BOLD + format.format(bazaarSellPrice) + " coins");
										int bazaarInstantBuyPrice = (int) bazaarInfo.get("curr_buy").getAsFloat();
										tooltipToRender.add(
											EnumChatFormatting.YELLOW.toString() + EnumChatFormatting.BOLD + "Bazaar Insta-Buy: " +
												EnumChatFormatting.GOLD + EnumChatFormatting.BOLD + format.format(bazaarInstantBuyPrice) +
												" coins");
										int bazaarInstantSellPrice = (int) bazaarInfo.get("curr_sell").getAsFloat();
										tooltipToRender.add(
											EnumChatFormatting.YELLOW.toString() + EnumChatFormatting.BOLD + "Bazaar Insta-Sell: " +
												EnumChatFormatting.GOLD + EnumChatFormatting.BOLD + format.format(bazaarInstantSellPrice) +
												" coins");
									}
									if (hasAuctionPrice) {
										int auctionPrice =
											(int) (auctionInfo.get("price").getAsFloat() / auctionInfo.get("count").getAsFloat());
										tooltipToRender.add(EnumChatFormatting.YELLOW.toString() + EnumChatFormatting.BOLD + "AH Price: " +
											EnumChatFormatting.GOLD + EnumChatFormatting.BOLD + format.format(auctionPrice) + " coins");
										tooltipToRender.add(EnumChatFormatting.YELLOW.toString() + EnumChatFormatting.BOLD + "AH Sales: " +
											EnumChatFormatting.GOLD + EnumChatFormatting.BOLD +
											format.format(auctionInfo.get("sales").getAsFloat()) + " sales/day");
										if (auctionInfo.has("clean_price")) {
											tooltipToRender.add(
												EnumChatFormatting.YELLOW.toString() + EnumChatFormatting.BOLD + "AH Price (Clean): " +
													EnumChatFormatting.GOLD + EnumChatFormatting.BOLD +
													format.format((int) auctionInfo.get("clean_price").getAsFloat()) + " coins");
											tooltipToRender.add(
												EnumChatFormatting.YELLOW.toString() + EnumChatFormatting.BOLD + "AH Sales (Clean): " +
													EnumChatFormatting.GOLD + EnumChatFormatting.BOLD +
													format.format(auctionInfo.get("clean_sales").getAsFloat()) + " sales/day");
										}

									}
									if (craftCost.fromRecipe) {
										tooltipToRender.add(
											EnumChatFormatting.YELLOW.toString() + EnumChatFormatting.BOLD + "Raw Craft Cost: " +
												EnumChatFormatting.GOLD + EnumChatFormatting.BOLD + format.format((int) craftCost.craftCost) +
												" coins");
									}
									tooltipToRender.add("");
								}
								if (rightStack != null)
									tooltipToRender.addAll(rightStack.getTooltip(Minecraft.getMinecraft().thePlayer, false));
							} else if (mouseY > guiTop + 126 && mouseY < guiTop + 126 + 16) {
								if (middleStack != null)
									tooltipToRender = middleStack.getTooltip(Minecraft.getMinecraft().thePlayer, false);
							}
						}
					} catch (NullPointerException e) { //i cant be bothered
					}
				}
			} else if (containerName.trim().equals("Confirm Bid") || containerName.trim().equals("Confirm Purchase")) {
				Minecraft.getMinecraft().getTextureManager().bindTexture(auction_accept);
				this.drawTexturedModalRect(auctionViewLeft, guiTop, 0, 0, 78, 172);

				if (auctionViewLeft + 31 > guiLeft + getXSize()) {
					try {
						ItemStack leftStack = auctionView.inventorySlots.getSlot(11).getStack();
						ItemStack middleStack = auctionView.inventorySlots.getSlot(13).getStack();
						ItemStack rightStack = auctionView.inventorySlots.getSlot(15).getStack();

						Utils.drawItemStack(middleStack, auctionViewLeft + 31, guiTop + 78);

						boolean topHovered = false;
						boolean bottomHovered = false;

						if (mouseX > auctionViewLeft + 31 && mouseX < auctionViewLeft + 31 + 16) {
							if (mouseY > guiTop + 31 && mouseY < guiTop + 31 + 16) {
								if (leftStack != null) {
									topHovered = true;
									tooltipToRender = leftStack.getTooltip(Minecraft.getMinecraft().thePlayer, false);
								}
							} else if (mouseY > guiTop + 125 && mouseY < guiTop + 125 + 16) {
								if (rightStack != null) {
									bottomHovered = true;
									tooltipToRender = rightStack.getTooltip(Minecraft.getMinecraft().thePlayer, false);
								}
							} else if (mouseY > guiTop + 78 && mouseY < guiTop + 78 + 16) {
								if (middleStack != null)
									tooltipToRender = middleStack.getTooltip(Minecraft.getMinecraft().thePlayer, false);
							}
						}
						GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
						Minecraft.getMinecraft().getTextureManager().bindTexture(auction_view_buttons);
						this.drawTexturedModalRect(auctionViewLeft + 31, guiTop + 31, topHovered ? 16 : 0, 48, 16, 16);
						this.drawTexturedModalRect(auctionViewLeft + 31, guiTop + 125, bottomHovered ? 16 : 0, 64, 16, 16);
					} catch (NullPointerException blah) { //i cant be bothered
					}
				}
			}

			Utils.drawStringCenteredScaledMaxWidth(containerName, Minecraft.getMinecraft().fontRendererObj,
				auctionViewLeft + 39, guiTop + 10, false, 70, 4210752
			);
		} else if (isEditingPrice()) {
			hasPopup = true;
			resetCurrAucIdTimer = System.currentTimeMillis();
			float slideAmount = 1 - Math.max(0, Math.min(1, (System.currentTimeMillis() - lastGuiScreenSwitch) / 200f));
			int auctionViewLeft = guiLeft + getXSize() + 4 - (int) (slideAmount * (96 + 4));

			if (priceField.getText().equals("IAUSHDIUAH")) priceField.setText("" + startingBid);

			searchField.setFocused(false);
			priceField.setFocused(true);
			priceField.xPosition = auctionViewLeft + 18;
			priceField.yPosition = guiTop + 18;

			Minecraft.getMinecraft().getTextureManager().bindTexture(auction_price);
			this.drawTexturedModalRect(auctionViewLeft, guiTop, 0, 0, 96, 99);
			priceField.drawTextBox();

			Utils.drawStringCenteredScaledMaxWidth("Bid Amount", Minecraft.getMinecraft().fontRendererObj,
				auctionViewLeft + 39, guiTop + 10, false, 70, 4210752
			);

			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			Minecraft.getMinecraft().getTextureManager().bindTexture(auction_view_buttons);
			this.drawTexturedModalRect(auctionViewLeft + 16, guiTop + 32, 0, 16, 30, 16);
			this.drawTexturedModalRect(auctionViewLeft + 16 + 34, guiTop + 32, 0, 16, 30, 16);
			this.drawTexturedModalRect(auctionViewLeft + 16, guiTop + 50, 0, 16, 30, 16);
			this.drawTexturedModalRect(auctionViewLeft + 16 + 34, guiTop + 50, 0, 16, 30, 16);
			this.drawTexturedModalRect(auctionViewLeft + 16, guiTop + 68, 0, 32, 64, 16);

			FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
			Utils.drawStringCentered("x2", fr, auctionViewLeft + 16 + 15, guiTop + 32 + 8, false,
				Color.BLACK.getRGB()
			);
			Utils.drawStringCentered("+50%", fr, auctionViewLeft + 16 + 34 + 15, guiTop + 32 + 8, false,
				Color.BLACK.getRGB()
			);
			Utils.drawStringCentered("+25%", fr, auctionViewLeft + 16 + 15, guiTop + 50 + 8, false,
				Color.BLACK.getRGB()
			);
			Utils.drawStringCentered("+10%", fr, auctionViewLeft + 16 + 34 + 15, guiTop + 50 + 8, false,
				Color.BLACK.getRGB()
			);
			Utils.drawStringCentered("Set Amount", fr, auctionViewLeft + 16 + 32, guiTop + 68 + 8, false,
				Color.BLACK.getRGB()
			);
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

			GuiEditSign editSign = (GuiEditSign) Minecraft.getMinecraft().currentScreen;
			TileEntitySign tes = (TileEntitySign) Utils.getField(GuiEditSign.class, editSign,
				"tileSign", "field_146848_f"
			);
			tes.lineBeingEdited = 0;
			tes.signText[0] = new ChatComponentText(priceField.getText());
		} else {
			if (System.currentTimeMillis() - resetCurrAucIdTimer > 500 &&
				System.currentTimeMillis() - currAucIdSetTimer > 500) {
				currentAucId = null;
				currAucIdSetTimer = System.currentTimeMillis();
			}
		}

		Minecraft.getMinecraft().getTextureManager().bindTexture(creativeInventoryTabs);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		if (mouseY > guiTop - 28 && mouseY < guiTop + 4) {
			if (mouseX > guiLeft && mouseX < guiLeft + 168) {
				int offset = mouseX - guiLeft;
				int hoveredCat = offset / 28;
				if (hoveredCat >= 0 && hoveredCat < mainCategories.length) {
					tooltipToRender = Utils.createList(mainCategories[hoveredCat].displayName);
				}
			}
		}
		if (clickedMainCategory == -1) {
			this.drawTexturedModalRect(guiLeft, guiTop - 28, 0, 0, 168, 32);
		} else {
			int selStart = clickedMainCategory * 28;
			this.drawTexturedModalRect(guiLeft, guiTop - 28, 0, 0, selStart, 32);
			this.drawTexturedModalRect(guiLeft + selStart + 28, guiTop - 28, selStart + 28, 0,
				168 - selStart - 28, 32
			);

			if (clickedMainCategory >= 0 && clickedMainCategory < mainCategories.length) {
				Category mainCategory = mainCategories[clickedMainCategory];

				if (mouseX > guiLeft - 28 && mouseX < guiLeft) {
					int offset = mouseY - (guiTop + 17);
					if (offset > 0) {
						int hovered = offset / 28 - 1;
						if (hovered < 0) {
							tooltipToRender = Utils.createList(mainCategory.displayName);
						} else if (hovered < mainCategory.subcategories.length) {
							tooltipToRender = Utils.createList(mainCategory.subcategories[hovered].displayName);
						}
					}
				}

				for (int i = -1; i < mainCategory.subcategories.length; i++) {
					if (i != clickedSubCategory) drawCategorySide(i);
				}
			}
		}

		//Main GUI
		Minecraft.getMinecraft().getTextureManager().bindTexture(creativeTabSearch);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		this.drawTexturedModalRect(guiLeft, guiTop, 0, 0, getXSize(), ySplit);
		int y = guiTop + ySplit;
		for (int i = 0; i < splits; i++) {
			this.drawTexturedModalRect(guiLeft, y, 0, ySplit, getXSize(), ySplit + ySplitSize);
			y += ySplitSize;
		}
		this.drawTexturedModalRect(guiLeft, y, 0, ySplit, getXSize(), 136 - ySplit);

		//GUI Name
		Utils.drawStringCenteredScaledMaxWidth("Auction House", Minecraft.getMinecraft().fontRendererObj, guiLeft + 42,
			guiTop + 10, false, 68, 4210752
		);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

		//Categories
		Minecraft.getMinecraft().getTextureManager().bindTexture(creativeInventoryTabs);
		GlStateManager.enableBlend();
		GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
		if (clickedMainCategory != -1) {
			int selStart = clickedMainCategory * 28;

			this.drawTexturedModalRect(guiLeft + selStart, guiTop - 28, clickedMainCategory == 0 ? 0 : 28, 32, 28, 32);

			if (clickedMainCategory >= 0 && clickedMainCategory < mainCategories.length) {
				Category mainCategory = mainCategories[clickedMainCategory];

				if (clickedSubCategory >= -1 && clickedSubCategory < mainCategory.subcategories.length) {
					drawCategorySide(clickedSubCategory);
				}
			}
		}

		//Category icons
		for (int i = 0; i < mainCategories.length; i++) {
			Category category = mainCategories[i];
			Utils.drawItemStack(category.displayItem, guiLeft + 28 * i + 6, guiTop - 28 + 9);
		}
		if (clickedMainCategory >= 0 && clickedMainCategory < mainCategories.length) {
			Category mainCategory = mainCategories[clickedMainCategory];

			Utils.drawItemStack(mainCategory.displayItem, guiLeft - 19, guiTop + 23);
			for (int i = 0; i < mainCategory.subcategories.length; i++) {
				Utils.drawItemStack(mainCategory.subcategories[i].displayItem, guiLeft - 19, guiTop + 23 + 28 * (i + 1));
			}
		}

		for (int i = 0; i < controls.length; i++) {
			Utils.drawItemStack(controls[i], guiLeft + 9 + 18 * i, guiTop + 112 + 18 * splits);
			if (mouseX > guiLeft + 9 + 18 * i && mouseX < guiLeft + 9 + 18 * i + 16) {
				if (mouseY > guiTop + 112 + 18 * splits && mouseY < guiTop + 112 + 18 * splits + 16) {
					tooltipToRender = getTooltipForControl(i);
				}
			}
		}

		int totalItems = auctionIds.size();
		int itemsScroll = (int) Math.floor((totalItems * scrollAmount) / 9f) * 9;

		int maxItemScroll = Math.max(0, totalItems - (5 + splits) * 9);
		itemsScroll = Math.min(itemsScroll, maxItemScroll);

		if (NotEnoughUpdates.INSTANCE.config.neuAuctionHouse.enableNeuAuctionHouse) {
			out:
			for (int i = 0; i < 5 + splits; i++) {
				int itemY = guiTop + i * 18 + 18;
				for (int j = 0; j < 9; j++) {
					int itemX = guiLeft + j * 18 + 9;
					int id = itemsScroll + i * 9 + j;
					if (auctionIds.size() <= id) break out;

					try {
						String aucid = sortedAuctionIds.get(id);

						GL11.glTranslatef(0, 0, 100);
						ItemStack stack = manager.auctionManager.getAuctionItems().get(aucid).getStack();
						Utils.drawItemStack(stack, itemX, itemY);
						GL11.glTranslatef(0, 0, -100);

						if (mouseX > itemX && mouseX < itemX + 16) {
							if (mouseY > itemY && mouseY < itemY + 16) {
								tooltipToRender = getTooltipForAucId(aucid);
							}
						}
					} catch (Exception ignored) {
					}
				}
			}
		}

		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		searchField.drawTextBox();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        /*if(auctionIds.size() == 0 && searchField.getText().length() == 0) {
            drawRect(guiLeft+8, guiTop+17, guiLeft+170, guiTop+107+18*splits,
                    new Color(100, 100, 100, 100).getRGB());

            FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
            int strWidth = fr.getStringWidth("Loading items...");
            fr.drawString("Loading items...", guiLeft+(8+170-strWidth)/2, guiTop+(17+107+18*splits)/2, Color.BLACK.getRGB());
        }*/

		Minecraft.getMinecraft().getTextureManager().bindTexture(creativeInventoryTabs);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		this.drawTexturedModalRect(guiLeft + 175, guiTop + 18 + (int) ((95 + ySplitSize * 2) * scrollAmount),
			256 - (scrollClicked ? 12 : 24), 0, 12, 15
		);

		if (!NotEnoughUpdates.INSTANCE.config.neuAuctionHouse.enableNeuAuctionHouse) {
			Utils.drawStringCentered(EnumChatFormatting.RED + "NEUAH is DISABLED! Enable in /neu.",
				Minecraft.getMinecraft().fontRendererObj, guiLeft + getXSize() / 2, guiTop + getYSize() / 2 - 5, true, 0
			);
		}

		if (tooltipToRender != null) {
			List<String> tooltipGray = new ArrayList<>();
			for (String line : tooltipToRender) {
				tooltipGray.add(EnumChatFormatting.GRAY + line);
			}
			Utils.drawHoveringText(tooltipGray, mouseX, mouseY, width,
				height, -1, Minecraft.getMinecraft().fontRendererObj
			);
		}
		float slideAmount = 1 - Math.max(0, Math.min(1, (System.currentTimeMillis() - lastGuiScreenSwitch) / 200f));
		int auctionViewLeft = guiLeft + getXSize() + 4 - (int) (slideAmount * (78 + 4));

		// Price filter
		if (!hasPopup && NotEnoughUpdates.INSTANCE.config.neuAuctionHouse.priceFiltering) {
			// Price bid filter
			priceFilterField.xPosition = auctionViewLeft + 18;
			priceFilterField.yPosition = guiTop + 18;

			Minecraft.getMinecraft().getTextureManager().bindTexture(auction_price);
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			this.drawTexturedModalRect(auctionViewLeft, guiTop, 0, 0, 96, 83);
			if (getPriceFilterAmount() == -1) {
				priceFilterField.setTextColor(Color.RED.getRGB());
			} else {
				priceFilterField.setTextColor(16777215);
			}
			priceFilterField.drawTextBox();

			Utils.drawStringCenteredScaledMaxWidth("Price Filter", Minecraft.getMinecraft().fontRendererObj,
				auctionViewLeft + 39, guiTop + 10, false, 70, 4210752
			);

			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			Minecraft.getMinecraft().getTextureManager().bindTexture(auction_view_buttons);
			if (currentPriceFilterType == PriceFilter.Less) {
				this.drawTexturedModalRect(auctionViewLeft + 16, guiTop + 32, 30, 16, 30, 16);
			} else {
				this.drawTexturedModalRect(auctionViewLeft + 16, guiTop + 32, 0, 16, 30, 16);
			}
			if (currentPriceFilterType == PriceFilter.Greater) {
				this.drawTexturedModalRect(auctionViewLeft + 16 + 34, guiTop + 32, 30, 16, 30, 16);
			} else {
				this.drawTexturedModalRect(auctionViewLeft + 16 + 34, guiTop + 32, 0, 16, 30, 16);
			}
			if (currentPriceFilterType == PriceFilter.Equal) {
				this.drawTexturedModalRect(auctionViewLeft + 16, guiTop + 50, 64, 32, 64, 16);
			} else {
				this.drawTexturedModalRect(auctionViewLeft + 16, guiTop + 50, 0, 32, 64, 16);
			}

			FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
			Utils.drawStringCentered("<=", fr, auctionViewLeft + 16 + 15, guiTop + 32 + 8, false,
				Color.BLACK.getRGB()
			);
			Utils.drawStringCentered(">=", fr, auctionViewLeft + 16 + 34 + 15, guiTop + 32 + 8, false,
				Color.BLACK.getRGB()
			);
			Utils.drawStringCentered("==", fr, auctionViewLeft + 16 + 32, guiTop + 50 + 8, false,
				Color.BLACK.getRGB()
			);
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

			// Bin average price filter
			binPriceFilterField.xPosition = auctionViewLeft + 18;
			binPriceFilterField.yPosition = guiTop + 18 + binPriceFilterYOffset;

			Minecraft.getMinecraft().getTextureManager().bindTexture(auction_price);
			this.drawTexturedModalRect(auctionViewLeft, guiTop + binPriceFilterYOffset, 0, 0, 96, 83);
			if (getBinPriceFilterAmount() == -1) {
				binPriceFilterField.setTextColor(Color.RED.getRGB());
			} else {
				binPriceFilterField.setTextColor(16777215);
			}
			binPriceFilterField.drawTextBox();

			Utils.drawStringCenteredScaledMaxWidth("BIN Average Filter", Minecraft.getMinecraft().fontRendererObj,
				auctionViewLeft + 39, guiTop + 10 + binPriceFilterYOffset, false, 70, 4210752
			);

			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			Minecraft.getMinecraft().getTextureManager().bindTexture(auction_view_buttons);
			if (currentBinPriceFilterType == PriceFilter.Less) {
				this.drawTexturedModalRect(auctionViewLeft + 16, guiTop + 32 + binPriceFilterYOffset, 30, 16, 30, 16);
			} else {
				this.drawTexturedModalRect(auctionViewLeft + 16, guiTop + 32 + binPriceFilterYOffset, 0, 16, 30, 16);
			}
			if (currentBinPriceFilterType == PriceFilter.Greater) {
				this.drawTexturedModalRect(auctionViewLeft + 16 + 34, guiTop + 32 + binPriceFilterYOffset, 30, 16, 30, 16);
			} else {
				this.drawTexturedModalRect(auctionViewLeft + 16 + 34, guiTop + 32 + binPriceFilterYOffset, 0, 16, 30, 16);
			}
			if (currentBinPriceFilterType == PriceFilter.Equal) {
				this.drawTexturedModalRect(auctionViewLeft + 16, guiTop + 50 + binPriceFilterYOffset, 64, 32, 64, 16);
			} else {
				this.drawTexturedModalRect(auctionViewLeft + 16, guiTop + 50 + binPriceFilterYOffset, 0, 32, 64, 16);
			}

			Utils.drawStringCentered("<=", fr, auctionViewLeft + 16 + 15, guiTop + 32 + 8 + binPriceFilterYOffset, false,
				Color.BLACK.getRGB()
			);
			Utils.drawStringCentered(">=", fr, auctionViewLeft + 16 + 34 + 15, guiTop + 32 + 8 + binPriceFilterYOffset, false,
				Color.BLACK.getRGB()
			);
			Utils.drawStringCentered("==", fr, auctionViewLeft + 16 + 32, guiTop + 50 + 8 + binPriceFilterYOffset, false,
				Color.BLACK.getRGB()
			);
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		}
	}

	public List<String> getTooltipForControl(int index) {
		List<String> lore = new ArrayList<>();
		if (index < 0 || index >= controls.length) return lore;
		if (controls[index] == null) return lore;

		lore.add(controls[index].getDisplayName());
		String arrow = "\u25b6";
		String selPrefixNC = " " + arrow + " ";
		String selPrefix = EnumChatFormatting.DARK_AQUA + selPrefixNC;
		String unselPrefix = EnumChatFormatting.GRAY.toString();
		switch (index) {
			case 0:
				lore.add("");
				String gold = EnumChatFormatting.GOLD.toString();
				String gray = EnumChatFormatting.GRAY.toString();
				char s = 0x272A;
				String[] linesDung = {
					"Show All", "Any Dungeon",
					gold + s + gray + s + s + s + s,
					gold + s + s + gray + s + s + s,
					gold + s + s + s + gray + s + s,
					gold + s + s + s + s + gray + s,
					gold + s + s + s + s + s
				};
				for (int i = 0; i < linesDung.length; i++) {
					String line = linesDung[i];
					if (i == dungeonFilter) {
						line = selPrefix + line.replace(gray, EnumChatFormatting.DARK_AQUA.toString());
					} else {
						line = unselPrefix + line;
					}
					lore.add(line);
				}
				lore.add("");
				lore.add(EnumChatFormatting.AQUA + "Right-Click to go backwards!");
				lore.add(EnumChatFormatting.YELLOW + "Click to switch sort!");
				return lore;
			case 1:
				lore.add("");
				String[] linesSort = {"Highest Bid", "Lowest Bid", "Ending soon"};
				for (int i = 0; i < linesSort.length; i++) {
					String line = linesSort[i];
					if (i == sortMode) {
						line = selPrefix + line;
					} else {
						line = unselPrefix + line;
					}
					lore.add(line);
				}
				lore.add("");
				lore.add(EnumChatFormatting.YELLOW + "Click to switch sort!");
				return lore;
			case 2:
				lore.add("");
				lore.add((rarityFilter == -1 ? EnumChatFormatting.DARK_GRAY + selPrefixNC : unselPrefix) + "No Filter");

				for (int i = 0; i < Utils.rarityArr.length; i++) {
					lore.add((rarityFilter == i ? rarityColours[i] + selPrefixNC : unselPrefix) +
						Utils.prettyCase(Utils.rarityArr[i]));
				}
				lore.add("");
				lore.add(EnumChatFormatting.AQUA + "Right-Click to go backwards!");
				lore.add(EnumChatFormatting.YELLOW + "Click to switch filter!");
				return lore;
			case 3:
				break;
			case 4:
				lore.add("");
				String off = EnumChatFormatting.RED + "OFF";
				String on = EnumChatFormatting.GREEN + "ON";
				lore.add(unselPrefix + "Filter Own Auctions: " + (filterMyAuctions ? on : off));
				lore.add("");
				lore.add(EnumChatFormatting.YELLOW + "Click to toggle!");
				return lore;
			case 5:
				break;
			case 6:
				lore.add("");
				String[] linesBin = {"Show All", "BIN Only", "Auctions Only"};
				for (int i = 0; i < linesBin.length; i++) {
					String line = linesBin[i];
					if (i == binFilter) {
						line = selPrefix + line;
					} else {
						line = unselPrefix + line;
					}
					lore.add(line);
				}
				lore.add("");
				lore.add(EnumChatFormatting.AQUA + "Right-Click to go backwards!");
				lore.add(EnumChatFormatting.YELLOW + "Click to switch filter!");
				return lore;
			case 7:
				lore.add("");
				String[] linesEnch = {"Show All", "Clean Only", "Ench Only", "Ench/HPB Only"};
				for (int i = 0; i < linesEnch.length; i++) {
					String line = linesEnch[i];
					if (i == enchFilter) {
						line = selPrefix + line;
					} else {
						line = unselPrefix + line;
					}
					lore.add(line);
				}
				lore.add("");
				lore.add(EnumChatFormatting.AQUA + "Right-Click to go backwards!");
				lore.add(EnumChatFormatting.YELLOW + "Click to switch filter!");
				return lore;
			case 8:
				lore.add("");
				lore.add("Current aucid: " + currentAucId);
				if (currentAucId != null) {
					APIManager.Auction auc = manager.auctionManager.getAuctionItems().get(currentAucId);
					if (auc != null) {
						lore.add("Current auc category: " + auc.category);
					}
				}
				lore.add(" --- Processing");
				lore.add("Page Process Millis: " + manager.auctionManager.processMillis);
				lore.add(" --- Auction Stats");
				lore.add("Active Auctions: " + manager.auctionManager.activeAuctions);
				lore.add("Tracked Auctions: " + manager.auctionManager.getAuctionItems().size());
				lore.add("Displayed Auctions: " + auctionIds.size());
				lore.add("Tracked Player Auctions: " + manager.auctionManager.getPlayerBids().size());
				lore.add("Unique Items: " + manager.auctionManager.uniqueItems);
				lore.add("ID Tagged Auctions: " + manager.auctionManager.internalnameTaggedAuctions);
				lore.add("Total Tags: " + manager.auctionManager.totalTags);
				lore.add("Tagged Auctions: " + manager.auctionManager.taggedAuctions);
				lore.add("");
				lore.add(EnumChatFormatting.AQUA + "Right-Click to copy current aucid to clipboard!");
				lore.add(EnumChatFormatting.YELLOW + "Click to refresh!");
				return lore;
		}
		return new ArrayList<>();
	}

	public void handleMouseInput() {
		if (!NotEnoughUpdates.INSTANCE.config.neuAuctionHouse.enableNeuAuctionHouse) {
			return;
		}

		ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
		int width = scaledResolution.getScaledWidth();
		int height = scaledResolution.getScaledHeight();

		int mouseX = Mouse.getEventX() * width / Minecraft.getMinecraft().displayWidth;
		int mouseY = height - Mouse.getEventY() * height / Minecraft.getMinecraft().displayHeight - 1;
		int mouseButton = Mouse.getEventButton();

		if (Mouse.getEventButtonState()) {
			this.eventButton = mouseButton;
			this.lastMouseEvent = Minecraft.getSystemTime();
			mouseClicked(mouseX, mouseY, this.eventButton);
		} else if (mouseButton != -1) {
			this.eventButton = -1;
			mouseReleased(mouseX, mouseY, mouseButton);
		} else if (this.eventButton != -1 && this.lastMouseEvent > 0L) {
			long l = Minecraft.getSystemTime() - this.lastMouseEvent;
			mouseClickMove(mouseX, mouseY, this.eventButton, l);
		}

		if (!NotEnoughUpdates.INSTANCE.config.neuAuctionHouse.disableAhScroll) {
			int dWheel = Mouse.getEventDWheel();
			dWheel = Math.max(-1, Math.min(1, dWheel));

			scrollAmount = scrollAmount - dWheel / (float) (auctionIds.size() / 9 - (5 + splits));
			scrollAmount = Math.max(0, Math.min(1, scrollAmount));
		}
	}

	public String niceAucId(String aucId) {
		if (aucId.length() != 32) return aucId;

		return aucId.substring(0, 8) +
			"-" + aucId.substring(8, 12) +
			"-" + aucId.substring(12, 16) +
			"-" + aucId.substring(16, 20) +
			"-" + aucId.substring(20, 32);
	}

	public Category getCurrentCategory() {
		if (clickedMainCategory < 0 || clickedMainCategory >= mainCategories.length) {
			return null;
		}
		Category mainCategory = mainCategories[clickedMainCategory];
		if (clickedSubCategory < 0 || clickedSubCategory >= mainCategory.subcategories.length) {
			return mainCategory;
		}
		return mainCategory.subcategories[clickedSubCategory];
	}

	private boolean doesAucMatch(APIManager.Auction auc) {
		if (auc == null) return false;

		Category currentCategory = getCurrentCategory();

		boolean match = true;
		if (currentCategory != null) {
			match = false;
			String[] categories = currentCategory.getTotalCategories();
			for (String category : categories) {
				match |= category.equalsIgnoreCase(auc.category);
			}
		}

		if (rarityFilter >= 0 && rarityFilter < Utils.rarityArr.length) {
			match &= Utils.rarityArr[rarityFilter].equals(auc.rarity);
		}

		if (binFilter == BIN_FILTER_BIN) {
			match &= auc.bin;
		} else if (binFilter == BIN_FILTER_AUC) {
			match &= !auc.bin;
		}

		if (enchFilter > ENCH_FILTER_ALL) {
			switch (enchFilter) {
				case ENCH_FILTER_CLEAN:
					match &= auc.enchLevel == 0;
					break;
				case ENCH_FILTER_ENCH:
					match &= auc.enchLevel >= 1;
					break;
				case ENCH_FILTER_ENCHHPB:
					match &= auc.enchLevel == 2;
					break;
			}
		}

		if (dungeonFilter > DUNGEON_FILTER_ALL) {
			if (dungeonFilter == DUNGEON_FILTER_DUNGEON && auc.dungeonTier < 0) {
				match = false;
			} else {
				match &= dungeonFilter == auc.dungeonTier + 1;
			}
		}

		if (getPriceFilterAmount() > -1) {
			if (currentPriceFilterType == PriceFilter.Greater) {
				match &= auc.highest_bid_amount != 0
					? (auc.highest_bid_amount >= getPriceFilterAmount())
					: auc.starting_bid >= getPriceFilterAmount();
			}
			if (currentPriceFilterType == PriceFilter.Less) {
				match &= auc.highest_bid_amount != 0
					? (auc.highest_bid_amount <= getPriceFilterAmount())
					: auc.starting_bid <= getPriceFilterAmount();
			}
			if (currentPriceFilterType == PriceFilter.Equal) {
				match &= auc.highest_bid_amount != 0
					? (auc.highest_bid_amount == getPriceFilterAmount())
					: auc.starting_bid == getPriceFilterAmount();
			}
		}
		if (getBinPriceFilterAmount() > -1) {
			int lowestBin =
				NotEnoughUpdates.INSTANCE.manager.auctionManager.getLowestBin(NotEnoughUpdates.INSTANCE.manager.getInternalNameForItem(
					auc.getStack()));
			if (lowestBin > 0) {
				if (currentBinPriceFilterType == PriceFilter.Greater) {
					match &= lowestBin >= getBinPriceFilterAmount();
				}
				if (currentBinPriceFilterType == PriceFilter.Less) {
					match &= lowestBin <= getBinPriceFilterAmount();
				}
				if (currentBinPriceFilterType == PriceFilter.Equal) {
					match &= lowestBin == getBinPriceFilterAmount();
				}
			} else {
				match = false;
			}
		}
		return match;
	}

	private HashSet<String> search(String query, Set<String> dontMatch) {
		query = query.trim();
		HashSet<String> matches = new HashSet<>();

		Set<String> itemMatches = manager.search(query);
		for (String internalname : itemMatches) {
			for (String aucid : manager.auctionManager.getAuctionsForInternalname(internalname)) {
				APIManager.Auction auc = manager.auctionManager.getAuctionItems().get(aucid);
				if (doesAucMatch(auc)) {
					//matches.add(aucid);
				} else {
					dontMatch.add(aucid);
				}
			}
		}

		HashMap<String, List<Integer>> extrasMatches = new HashMap<>();
		HashMap<String, List<Integer>> extrasMatchesCurrent = new HashMap<>();
		boolean first = true;
		for (String subQuery : query.split(" ")) {
			for (HashMap<Integer, HashSet<String>> extrasMap : manager.subMapWithKeysThatAreSuffixes(
				subQuery.toLowerCase(),
				manager.auctionManager.extrasToAucIdMap
			).values()) {
				for (int index : extrasMap.keySet()) {
					for (String aucid : extrasMap.get(index)) {
						APIManager.Auction auc = manager.auctionManager.getAuctionItems().get(aucid);
						if (!dontMatch.contains(aucid) && doesAucMatch(auc)) {
							if (first) {
								List<Integer> indexList = extrasMatchesCurrent.computeIfAbsent(aucid, k -> new ArrayList<>());
								indexList.add(index);
							} else {
								List<Integer> indexList = extrasMatches.computeIfAbsent(aucid, k -> new ArrayList<>());
								if (indexList.contains(index - 1)) {
									List<Integer> indexListCurrent = extrasMatchesCurrent.computeIfAbsent(aucid, k -> new ArrayList<>());
									indexListCurrent.add(index);
								}
							}
						} else {
							dontMatch.add(aucid);
						}
					}
				}

			}

			extrasMatches = (HashMap<String, List<Integer>>) extrasMatchesCurrent.clone();
			extrasMatchesCurrent.clear();
			first = false;
		}
		matches.addAll(extrasMatches.keySet());

		return matches;
	}

	private final ExecutorService es = Executors.newSingleThreadExecutor();

	public void updateSearch() {
		if (!NotEnoughUpdates.INSTANCE.config.neuAuctionHouse.enableNeuAuctionHouse) {
			return;
		}

		if (searchField == null || priceField == null || priceFilterField == null || binPriceFilterField == null)
			init();
		long currentTime = System.currentTimeMillis();

		es.submit(() -> {
			if (currentTime - lastUpdateSearch < 100) {
				shouldUpdateSearch = true;
				return;
			}

			lastUpdateSearch = currentTime;
			shouldUpdateSearch = false;

			scrollAmount = 0;
			try {
				HashSet<String> auctionIdsNew = new HashSet<>();
				auctionIdsNew.clear();
				if (filterMyAuctions) {
					for (String aucid : manager.auctionManager.getPlayerBids()) {
						APIManager.Auction auc = manager.auctionManager.getAuctionItems().get(aucid);
						if (doesAucMatch(auc)) {
							auctionIdsNew.add(aucid);
						}
					}
				} else if (searchField.getText().length() == 0) {
					for (Map.Entry<String, APIManager.Auction> entry : manager.auctionManager.getAuctionItems().entrySet()) {
						if (doesAucMatch(entry.getValue())) {
							auctionIdsNew.add(entry.getKey());
						}
					}
				} else {
					String query = searchField.getText();
					Set<String> dontMatch = new HashSet<>();

					HashSet<String> allMatch = new HashSet<>();
					if (query.contains("!")) { //only used for inverted queries, so dont need to populate unless ! in query
						for (Map.Entry<String, APIManager.Auction> entry : manager.auctionManager.getAuctionItems().entrySet()) {
							if (doesAucMatch(entry.getValue())) {
								allMatch.add(entry.getKey());
							} else {
								dontMatch.add(entry.getKey());
							}
						}
					}

					boolean invert = false;

					StringBuilder query2 = new StringBuilder();
					char lastOp = '|';
					for (char c : query.toCharArray()) {
						if (query2.toString().trim().isEmpty() && c == '!') {
							invert = true;
						} else if (c == '|' || c == '&') {
							if (lastOp == '|') {
								HashSet<String> result = search(query2.toString(), dontMatch);
								if (!invert) {
									auctionIdsNew.addAll(result);
								} else {
									HashSet<String> allClone = (HashSet<String>) allMatch.clone();
									allClone.removeAll(result);
									auctionIdsNew.addAll(allClone);
								}
							} else if (lastOp == '&') {
								HashSet<String> result = search(query2.toString(), dontMatch);
								if (!invert) {
									auctionIdsNew.retainAll(result);
								} else {
									auctionIdsNew.removeAll(result);
								}
							}

							query2 = new StringBuilder();
							invert = false;
							lastOp = c;
						} else {
							query2.append(c);
						}
					}
					if (lastOp == '|') {
						HashSet<String> result = search(query2.toString(), dontMatch);
						if (!invert) {
							auctionIdsNew.addAll(result);
						} else {
							HashSet<String> allClone = (HashSet<String>) allMatch.clone();
							allClone.removeAll(result);
							auctionIdsNew.addAll(allClone);
						}
					} else if (lastOp == '&') {
						HashSet<String> result = search(query2.toString(), dontMatch);
						if (!invert) {
							auctionIdsNew.retainAll(result);
						} else {
							auctionIdsNew.removeAll(result);
						}
					}
				}
				auctionIds = auctionIdsNew;
				sortItems();
			} catch (Exception e) {
				shouldUpdateSearch = true;
			}
		});
	}

	public void sortItems() throws ConcurrentModificationException {
		if (!NotEnoughUpdates.INSTANCE.config.neuAuctionHouse.enableNeuAuctionHouse) {
			return;
		}

		try {
			List<String> sortedAuctionIdsNew = new ArrayList<>();

			sortedAuctionIdsNew.addAll(auctionIds);
			sortedAuctionIdsNew.sort((o1, o2) -> {
				APIManager.Auction auc1 = manager.auctionManager.getAuctionItems().get(o1);
				APIManager.Auction auc2 = manager.auctionManager.getAuctionItems().get(o2);

				if (auc1 == null) return 1;
				if (auc2 == null) return -1;

				if (sortMode == SORT_MODE_HIGH) {
					int price1 = Math.max(auc1.highest_bid_amount, auc1.starting_bid);
					int price2 = Math.max(auc2.highest_bid_amount, auc2.starting_bid);
					int diff = price2 - price1;
					if (diff != 0) {
						return diff;
					}
				} else if (sortMode == SORT_MODE_LOW) {
					int price1 = Math.max(auc1.highest_bid_amount, auc1.starting_bid);
					int price2 = Math.max(auc2.highest_bid_amount, auc2.starting_bid);
					int diff = price1 - price2;
					if (diff != 0) {
						return diff;
					}
				} else {
					long end1 = auc1.end;
					long end2 = auc2.end;

					if (end1 < System.currentTimeMillis()) return 999999;
					if (end2 < System.currentTimeMillis()) return -999999;

					int diff = (int) (end1 - end2);
					if (diff != 0) {
						return diff;
					}
				}
				return o1.compareTo(o2);
			});

			sortedAuctionIds = sortedAuctionIdsNew;
		} catch (Exception e) {
			shouldSortItems = true;
		}
	}

	public boolean keyboardInput() {
		if (!NotEnoughUpdates.INSTANCE.config.neuAuctionHouse.enableNeuAuctionHouse) {
			return false;
		}

		if (isEditingPrice() && Keyboard.getEventKey() == Keyboard.KEY_RETURN) {
			Minecraft.getMinecraft().displayGuiScreen(null);
		} else if (Keyboard.getEventKey() == Keyboard.KEY_ESCAPE) {
			if (Minecraft.getMinecraft().currentScreen instanceof GuiEditSign) {
				priceField.setText("");
				GuiEditSign editSign = (GuiEditSign) Minecraft.getMinecraft().currentScreen;
				TileEntitySign tes = (TileEntitySign) Utils.getField(GuiEditSign.class, editSign,
					"tileSign", "field_146848_f"
				);
				tes.lineBeingEdited = 0;
				tes.signText[0] = new ChatComponentText(priceField.getText());
			}
			return false;
		}
		if (Keyboard.getEventKeyState()) return keyTyped(Keyboard.getEventCharacter(), Keyboard.getEventKey());
		return true;
	}

	public boolean keyTyped(char typedChar, int keyCode) {
		if (searchField == null || priceField == null || priceFilterField == null || binPriceFilterField == null)
			init();

		if (!isEditingPrice()) {
			if (this.priceFilterField.textboxKeyTyped(typedChar, keyCode)) {
				this.updateSearch();
				return true;
			}
			if (this.binPriceFilterField.textboxKeyTyped(typedChar, keyCode)) {
				this.updateSearch();
				return true;
			}
			if (this.searchField.textboxKeyTyped(typedChar, keyCode)) {
				lastSearchFieldUpdate = System.currentTimeMillis();
				this.updateSearch();
				return true;
			}
		} else {
			if (!priceField.isFocused()) priceField.setFocused(searchField.isFocused() && priceFilterField.isFocused());
			return priceField.textboxKeyTyped(typedChar, keyCode);
		}
		return false;
	}

	private void increasePriceByFactor(float factor) {
		String price = priceField.getText().trim();
		StringBuilder priceNumbers = new StringBuilder();
		for (int i = 0; i < price.length(); i++) {
			char c = price.charAt(i);
			if ((int) c >= 48 && (int) c <= 57) {
				priceNumbers.append(c);
			} else {
				break;
			}
		}
		int priceI = 0;
		if (priceNumbers.length() > 0) {
			priceI = Integer.parseInt(priceNumbers.toString());
		}
		String end = price.substring(priceNumbers.length());
		priceField.setText((int) (priceI * factor) + end);
	}

	private int getPriceFilterAmount() {
		return getNumberFromTextBox(priceFilterField);
	}

	private int getNumberFromTextBox(GuiTextField textField) {
		if (textField.getText().equals("")) {
			return -2;
		}
		try {
			int parsed = Integer.parseInt(textField.getText());
			if (parsed < 0) {
				return -1;
			}
			return parsed;
		} catch (NumberFormatException e) {
			return -1;
		}
	}

	private int getBinPriceFilterAmount() {
		return getNumberFromTextBox(binPriceFilterField);
	}

	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		boolean wasFocused = searchField.isFocused();
		searchField.mouseClicked(mouseX, mouseY, mouseButton);
		if (mouseButton == 1 && !wasFocused && searchField.isFocused()) {
			searchField.setText("");
		}
		priceField.mouseClicked(mouseX, mouseY, mouseButton);
		priceFilterField.mouseClicked(mouseX, mouseY, mouseButton);
		binPriceFilterField.mouseClicked(mouseX, mouseY, mouseButton);

		int totalItems = auctionIds.size();
		int itemsScroll = (int) Math.floor((totalItems * scrollAmount) / 9f) * 9;

		int maxItemScroll = Math.max(0, totalItems - (5 + splits) * 9);
		itemsScroll = Math.min(itemsScroll, maxItemScroll);

		//Categories
		if (mouseY > guiTop - 28 && mouseY < guiTop + 4) {
			if (mouseX > guiLeft && mouseX < guiLeft + 168) {
				int offset = mouseX - guiLeft;
				int clickedCat = offset / 28;
				if (clickedMainCategory == clickedCat) {
					clickedMainCategory = -1;
				} else {
					clickedMainCategory = clickedCat;
				}
				clickedSubCategory = -1;
				updateSearch();
				Utils.playPressSound();
				return;
			}
		}

		if (clickedMainCategory >= 0 && clickedMainCategory < mainCategories.length) {
			Category mainCategory = mainCategories[clickedMainCategory];

			if (mouseX > guiLeft - 28 && mouseX < guiLeft) {
				int offset = mouseY - (guiTop + 17);
				if (offset > 0) {
					int clicked = offset / 28 - 1;
					if (clicked != clickedSubCategory && clicked < mainCategory.subcategories.length) {
						clickedSubCategory = clicked;
						updateSearch();
						Utils.playPressSound();
						return;
					}
				}
			}
		}

		if (mouseY > guiTop + 112 + 18 * splits && mouseY < guiTop + 112 + 18 * splits + 16) {
			if (mouseX > guiLeft + 9 && mouseX < guiLeft + 9 + controls.length * 18 - 2) {
				int offset = mouseX - (guiLeft + 9);
				int index = offset / 18;
				boolean rightClicked = Mouse.getEventButton() == 1;
				switch (index) {
					case 0:
						if (rightClicked) {
							dungeonFilter--;
							if (dungeonFilter < DUNGEON_FILTER_ALL) dungeonFilter = DUNGEON_FILTER_5;
						} else {
							dungeonFilter++;
							if (dungeonFilter > DUNGEON_FILTER_5) dungeonFilter = DUNGEON_FILTER_ALL;
						}
						break;
					case 1:
						if (rightClicked) {
							sortMode--;
							if (sortMode < SORT_MODE_HIGH) sortMode = SORT_MODE_SOON;
						} else {
							sortMode++;
							if (sortMode > SORT_MODE_SOON) sortMode = SORT_MODE_HIGH;
						}
						break;
					case 2:
						if (rightClicked) {
							rarityFilter--;
							if (rarityFilter < -1) rarityFilter = Utils.rarityArr.length - 1;
						} else {
							rarityFilter++;
							if (rarityFilter >= Utils.rarityArr.length) rarityFilter = -1;
						}
						break;
					case 3:
						break;
					case 4:
						filterMyAuctions = !filterMyAuctions;
						break;
					case 5:
						break;
					case 6:
						if (rightClicked) {
							binFilter--;
							if (binFilter < BIN_FILTER_ALL) binFilter = BIN_FILTER_AUC;
						} else {
							binFilter++;
							if (binFilter > BIN_FILTER_AUC) binFilter = BIN_FILTER_ALL;
						}
						break;
					case 7:
						if (rightClicked) {
							enchFilter--;
							if (enchFilter < ENCH_FILTER_ALL) enchFilter = ENCH_FILTER_ENCHHPB;
						} else {
							enchFilter++;
							if (enchFilter > ENCH_FILTER_ENCHHPB) enchFilter = ENCH_FILTER_ALL;
						}
						break;
					case 8:
						if (rightClicked) {
							if (currentAucId != null) {
								StringSelection selection = new StringSelection(niceAucId(currentAucId));
								Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
							}
						} else {
							manager.auctionManager.calculateStats();
						}
				}
				updateSearch();
				Utils.playPressSound();
			}
		}
		boolean hasPopup = false;
		if (mouseButton == 0 && Minecraft.getMinecraft().currentScreen instanceof GuiChest) {
			hasPopup = true;
			GuiChest auctionView = (GuiChest) Minecraft.getMinecraft().currentScreen;
			ContainerChest container = (ContainerChest) auctionView.inventorySlots;
			String containerName = container.getLowerChestInventory().getDisplayName().getUnformattedText();

			if (containerName.trim().equals("Auction View") || containerName.trim().equals("BIN Auction View")) {

				if (mouseX > guiLeft + getXSize() + 4 + 31 && mouseX < guiLeft + getXSize() + 4 + 31 + 16) {
					boolean leftFiller = isGuiFiller(auctionView.inventorySlots.getSlot(29).getStack());//isBin
					if (mouseY > guiTop + 35 && mouseY < guiTop + 35 + 16) {
						ItemStack topStack = auctionView.inventorySlots.getSlot(13).getStack();
						if (topStack != null) {
							String line = findStrStart(topStack, EnumChatFormatting.GRAY + "Seller: ");
							String[] split = line.split(" ");
							String seller = split[split.length - 1];
							StringSelection selection = new StringSelection(seller);
							Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
						}
					} else if (mouseY > guiTop + 100 && mouseY < guiTop + 100 + 16) {
						int slotClick = leftFiller ? 31 : 29;
						Minecraft.getMinecraft().playerController.windowClick(auctionView.inventorySlots.windowId,
							slotClick, 2, 3, Minecraft.getMinecraft().thePlayer
						);

						if (!leftFiller) {
							if (auctionView.inventorySlots.getSlot(29).getStack()
																						.getDisplayName().trim().equals(
									EnumChatFormatting.GOLD + "Collect Auction")) {
								manager.auctionManager.getPlayerBids().remove(currentAucId);
								auctionIds.remove(currentAucId);
							}
						}

						Utils.playPressSound();
					} else if (mouseY > guiTop + 126 && mouseY < guiTop + 126 + 16 && !leftFiller) {
						priceField.setFocused(true);
						Minecraft.getMinecraft().playerController.windowClick(auctionView.inventorySlots.windowId,
							31, 2, 3, Minecraft.getMinecraft().thePlayer
						);
						Utils.playPressSound();
					}
				}
			} else if (containerName.trim().equals("Confirm Bid") || containerName.trim().equals("Confirm Purchase")) {
				if (mouseX > guiLeft + getXSize() + 4 + 31 && mouseX < guiLeft + getXSize() + 4 + 31 + 16) {
					if (mouseY > guiTop + 31 && mouseY < guiTop + 31 + 16) {
						if (currentAucId != null) {
							manager.auctionManager.getPlayerBids().add(currentAucId);
							latestBid = currentAucId;
							latestBidMillis = System.currentTimeMillis();
							//reset timer to 2m if below
							if (manager.auctionManager.getAuctionItems().get(currentAucId) != null &&
								manager.auctionManager.getAuctionItems().get(currentAucId).end -
									System.currentTimeMillis() < 2 * 60 * 1000) {
								manager.auctionManager.getAuctionItems().get(currentAucId).end =
									System.currentTimeMillis() + 2 * 60 * 1000;
							}
						}
						Minecraft.getMinecraft().playerController.windowClick(auctionView.inventorySlots.windowId,
							11, 2, 3, Minecraft.getMinecraft().thePlayer
						);
					} else if (mouseY > guiTop + 125 && mouseY < guiTop + 125 + 16) {
						Minecraft.getMinecraft().playerController.windowClick(auctionView.inventorySlots.windowId,
							15, 2, 3, Minecraft.getMinecraft().thePlayer
						);
					}
					Utils.playPressSound();
				}
			}
		}
		int auctionViewLeft = guiLeft + getXSize() + 4;

		if (mouseButton == 0 && isEditingPrice()) {

			if (mouseX > auctionViewLeft + 16 && mouseX < auctionViewLeft + 16 + 64) {
				if (mouseY > guiTop + 32 && mouseY < guiTop + 32 + 16) {
					if (mouseX < auctionViewLeft + 16 + 32) {
						//top left
						increasePriceByFactor(2);
					} else {
						//top right
						increasePriceByFactor(1.5f);
					}
					Utils.playPressSound();
				} else if (mouseY > guiTop + 50 && mouseY < guiTop + 50 + 16) {
					if (mouseX < auctionViewLeft + 16 + 32) {
						//mid left
						increasePriceByFactor(1.25f);
					} else {
						//mid right
						increasePriceByFactor(1.1f);
					}
					Utils.playPressSound();
				} else if (mouseY > guiTop + 68 && mouseY < guiTop + 68 + 16) {
					//bottom
					Utils.playPressSound();
					Minecraft.getMinecraft().displayGuiScreen(null);
				}
			}

			this.drawTexturedModalRect(auctionViewLeft + 16, guiTop + 32, 0, 16, 30, 16);
			this.drawTexturedModalRect(auctionViewLeft + 16 + 34, guiTop + 32, 0, 16, 30, 16);
			this.drawTexturedModalRect(auctionViewLeft + 16, guiTop + 50, 0, 16, 30, 16);
			this.drawTexturedModalRect(auctionViewLeft + 16 + 34, guiTop + 50, 0, 16, 30, 16);
			this.drawTexturedModalRect(auctionViewLeft + 16, guiTop + 68, 0, 32, 64, 16);
		} else if (!hasPopup && NotEnoughUpdates.INSTANCE.config.neuAuctionHouse.priceFiltering) {

			if (mouseX > auctionViewLeft + 16 && mouseX < auctionViewLeft + 16 + 64) {
				if (mouseY > guiTop + 32 && mouseY < guiTop + 32 + 16) {
					if (mouseX < auctionViewLeft + 16 + 32) {
						//top left
						currentPriceFilterType = PriceFilter.Less;
					} else {
						//top right
						currentPriceFilterType = PriceFilter.Greater;
					}
					Utils.playPressSound();
					updateSearch();
				} else if (mouseY > guiTop + 50 && mouseY < guiTop + 50 + 16) {
					//middle
					currentPriceFilterType = PriceFilter.Equal;
					Utils.playPressSound();
					updateSearch();
				}
			}

			if (mouseX > auctionViewLeft + 16 && mouseX < auctionViewLeft + 16 + 64) {
				if (mouseY > guiTop + 32 + binPriceFilterYOffset && mouseY < guiTop + 32 + 16 + binPriceFilterYOffset) {
					if (mouseX < auctionViewLeft + 16 + 32) {
						//top left
						currentBinPriceFilterType = PriceFilter.Less;
					} else {
						//top right
						currentBinPriceFilterType = PriceFilter.Greater;
					}
					Utils.playPressSound();
					updateSearch();
				} else if (mouseY > guiTop + 50 + binPriceFilterYOffset && mouseY < guiTop + 50 + 16 + binPriceFilterYOffset) {
					//middle
					currentBinPriceFilterType = PriceFilter.Equal;
					Utils.playPressSound();
					updateSearch();
				}
			}
		}
		out:
		for (int i = 0; i < 5 + splits; i++) {
			int itemY = guiTop + i * 18 + 18;
			for (int j = 0; j < 9; j++) {
				int itemX = guiLeft + j * 18 + 9;
				int id = itemsScroll + i * 9 + j;
				if (auctionIds.size() <= id) break out;

				String aucid;
				try {
					aucid = sortedAuctionIds.get(id);
				} catch (IndexOutOfBoundsException e) {
					break out;
				}

				if (aucid == null) continue;
				APIManager.Auction auc = manager.auctionManager.getAuctionItems().get(aucid);
				if (auc != null) {
					if (mouseX > itemX && mouseX < itemX + 16) {
						if (mouseY > itemY && mouseY < itemY + 16) {
							if (Minecraft.getMinecraft().currentScreen instanceof GuiEditSign) {
								priceField.setText("");
								GuiEditSign editSign = (GuiEditSign) Minecraft.getMinecraft().currentScreen;
								TileEntitySign tes = (TileEntitySign) Utils.getField(GuiEditSign.class, editSign,
									"tileSign", "field_146848_f"
								);
								tes.lineBeingEdited = 0;
								tes.signText[0] = new ChatComponentText(priceField.getText());
							}
							startingBid = Math.max(auc.starting_bid, auc.highest_bid_amount);
							currAucIdSetTimer = System.currentTimeMillis();
							currentAucId = aucid;

							Minecraft.getMinecraft().thePlayer.sendChatMessage("/viewauction " + niceAucId(aucid));
							Utils.playPressSound();
						}
					}
				}
			}
		}

		int y = guiTop + 18 + (int) ((95 + ySplitSize * 2) * scrollAmount);
		if (mouseX > guiLeft + 175 && mouseX < guiLeft + 175 + 12) {
			if (mouseY > y && mouseY < y + 15) {
				scrollClicked = true;
				return;
			}
		}
		scrollClicked = false;
	}

	protected void mouseReleased(int mouseX, int mouseY, int state) {
		scrollClicked = false;
	}

	protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
		if (scrollClicked) {
			int yMin = guiTop + 18 + 8;
			int yMax = guiTop + 18 + (95 + ySplitSize * 2) + 8;

			scrollAmount = (mouseY - yMin) / (float) (yMax - yMin);
			scrollAmount = Math.max(0, Math.min(1, scrollAmount));
		}
	}
}
