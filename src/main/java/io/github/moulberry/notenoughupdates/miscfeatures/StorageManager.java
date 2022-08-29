/*
 * Copyright (C) 2022 NotEnoughUpdates contributors
 *
 * This file is part of NotEnoughUpdates.
 *
 * NotEnoughUpdates is free software: you can redistribute it
 * and/or modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * NotEnoughUpdates is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with NotEnoughUpdates. If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.moulberry.notenoughupdates.miscfeatures;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.miscgui.StorageOverlay;
import io.github.moulberry.notenoughupdates.util.SBInfo;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.nbt.NBTTagShort;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.play.client.C0EPacketClickWindow;
import net.minecraft.network.play.server.S2DPacketOpenWindow;
import net.minecraft.network.play.server.S2EPacketCloseWindow;
import net.minecraft.network.play.server.S2FPacketSetSlot;
import net.minecraft.network.play.server.S30PacketWindowItems;
import org.lwjgl.Sys;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class StorageManager {
	private static final StorageManager INSTANCE = new StorageManager();
	private static final Gson GSON = new GsonBuilder()
		.registerTypeAdapter(ItemStack.class, new ItemStackSerializer())
		.registerTypeAdapter(ItemStack.class, new ItemStackDeserializer()).create();

	public static class ItemStackSerializer implements JsonSerializer<ItemStack> {
		@Override
		public JsonElement serialize(ItemStack src, Type typeOfSrc, JsonSerializationContext context) {
			fixPetInfo(src);
			NBTTagCompound tag = src.serializeNBT();
			return nbtToJson(tag);
		}
	}

	private static final Pattern JSON_FIX_REGEX = Pattern.compile("\"([^,:]+)\":");

	public static class ItemStackDeserializer implements JsonDeserializer<ItemStack> {
		@Override
		public ItemStack deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {
			try {
				JsonObject object = json.getAsJsonObject();

				NBTTagCompound tag = JsonToNBT.getTagFromJson(JSON_FIX_REGEX.matcher(object.toString()).replaceAll("$1:"));

				Item item;
				if (tag.hasKey("id", 8)) {
					item = Item.getByNameOrId(tag.getString("id"));
				} else {
					item = Item.getItemById(tag.getShort("id"));
				}
				if (item == null) {
					return null;
				}
				int stackSize = tag.getInteger("Count");
				int damage = tag.getInteger("Damage");

				ItemStack stack = new ItemStack(item, stackSize, damage);

				if (tag.hasKey("tag")) {
					NBTTagCompound itemTag = tag.getCompoundTag("tag");
					stack.setTagCompound(itemTag);
				}

				return stack;
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
	}

	private static JsonObject nbtToJson(NBTTagCompound NBTTagCompound) {
		return (JsonObject) loadJson(NBTTagCompound);
	}

	private static class PetInfo {
		String type;
		Boolean active;
		Double exp;
		String tier;
		Boolean hideInfo;
		Integer candyUsed;
		String uuid;
		Boolean hideRightClick;
		String heldItem;
		String skin;

		private <T> void appendIfNotNull(StringBuilder builder, String key, T value) {
			if (value != null) {
				if (builder.indexOf("{") != builder.length()-1) {
					builder.append(",");
				}
				builder.append(key).append(":");
				if (value instanceof String) {
					builder.append("\"").append(value).append("\"");
				} else {
					builder.append(value);
				}
			}
		}

		@Override
		public String toString() {
			StringBuilder object = new StringBuilder();
			object.append("{");
			appendIfNotNull(object, "type", type);
			appendIfNotNull(object, "active", active);
			appendIfNotNull(object, "exp", exp);
			appendIfNotNull(object, "tier", tier);
			appendIfNotNull(object, "hideInfo", hideInfo);
			appendIfNotNull(object, "candyUsed", candyUsed);
			appendIfNotNull(object, "uuid", uuid);
			appendIfNotNull(object, "hideRightClick", hideRightClick);
			appendIfNotNull(object, "heldItem", heldItem);
			appendIfNotNull(object, "skin", skin);
			object.append("}");
			return object.toString();
		}
	}

	private static void fixPetInfo(ItemStack src) {
		if (!src.getTagCompound().hasKey("ExtraAttributes") || !src.getTagCompound().getCompoundTag("ExtraAttributes").hasKey("petInfo")) return;
		PetInfo oldPetInfo = GSON.fromJson(src.getTagCompound().getCompoundTag("ExtraAttributes").getString("petInfo"), PetInfo.class);
		src.getTagCompound().getCompoundTag("ExtraAttributes").removeTag("petInfo");
		try {
			src.getTagCompound().getCompoundTag("ExtraAttributes").setTag(
				"petInfo",
				JsonToNBT.getTagFromJson(oldPetInfo.toString())
			);
		} catch (NBTException | NullPointerException ignored) {}
	}

	private static JsonElement loadJson(NBTBase tag) {
		if (tag instanceof NBTTagCompound) {
			NBTTagCompound compoundTag = (NBTTagCompound) tag;
			JsonObject jsonObject = new JsonObject();
			for (String key : compoundTag.getKeySet()) {
				jsonObject.add(key, loadJson(compoundTag.getTag(key)));
			}
			return jsonObject;
		} else if (tag instanceof NBTTagList) {
			NBTTagList listTag = (NBTTagList) tag;
			JsonArray jsonArray = new JsonArray();
			for (int i = 0; i < listTag.tagCount(); i++) {
				jsonArray.add(loadJson(listTag.get(i)));
			}
			return jsonArray;
		} else if (tag instanceof NBTTagIntArray) {
			NBTTagIntArray listTag = (NBTTagIntArray) tag;
			int[] arr = listTag.getIntArray();
			JsonArray jsonArray = new JsonArray();
			for (int j : arr) {
				jsonArray.add(new JsonPrimitive(j));
			}
			return jsonArray;
		} else if (tag instanceof NBTTagByteArray) {
			NBTTagByteArray listTag = (NBTTagByteArray) tag;
			byte[] arr = listTag.getByteArray();
			JsonArray jsonArray = new JsonArray();
			for (byte b : arr) {
				jsonArray.add(new JsonPrimitive(b));
			}
			return jsonArray;
		} else if (tag instanceof NBTTagShort) {
			return new JsonPrimitive(((NBTTagShort) tag).getShort());
		} else if (tag instanceof NBTTagInt) {
			return new JsonPrimitive(((NBTTagInt) tag).getInt());
		} else if (tag instanceof NBTTagLong) {
			return new JsonPrimitive(((NBTTagLong) tag).getLong());
		} else if (tag instanceof NBTTagFloat) {
			return new JsonPrimitive(((NBTTagFloat) tag).getFloat());
		} else if (tag instanceof NBTTagDouble) {
			return new JsonPrimitive(((NBTTagDouble) tag).getDouble());
		} else if (tag instanceof NBTTagByte) {
			return new JsonPrimitive(((NBTTagByte) tag).getByte());
		} else if (tag instanceof NBTTagString) {
			return new JsonPrimitive(((NBTTagString) tag).getString());
		} else {
			return new JsonPrimitive("Broken_Json_Deserialize_Tag");
		}
	}

	public static StorageManager getInstance() {
		return INSTANCE;
	}

	private final AtomicInteger searchId = new AtomicInteger(0);

	public static class StoragePage {
		public ItemStack[] items = new ItemStack[45];
		public ItemStack backpackDisplayStack;
		public String customTitle;
		public int rows = -1;
		public boolean[] shouldDarkenIfNotSelected = new boolean[45];

		public transient boolean matchesSearch;
		public transient int searchedId;
	}

	public static int MAX_ENDER_CHEST_PAGES = 9;

	public static final ItemStack LOCKED_ENDERCHEST_STACK =
		Utils.createItemStack(Item.getItemFromBlock(Blocks.stained_glass_pane),
			"\u00a7cLocked Page", 14,
			"\u00a77Unlock more Ender Chest",
			"\u00a77pages in the community",
			"\u00a77shop!"
		);

	public static class StorageConfig {
		public HashMap<String, StoragePage[]> pages = new HashMap<>();
		public final HashMap<Integer, Integer> displayToStorageIdMap = new HashMap<>();
		public final HashMap<Integer, Integer> displayToStorageIdMapRender = new HashMap<>();
	}

	public StorageConfig storageConfig = new StorageConfig();

	private int currentStoragePage = -1;
	public boolean onStorageMenu = false;

	private String lastSearch = "";

	private boolean[] storagePresent = null;

	//TODO: Replace with /storage {id} when hypixel becomes not lazy
	public int desiredStoragePage = -1;
	public long storageOpenSwitchMillis = 0;

	private final ItemStack[] missingBackpackStacks = new ItemStack[18];

	private boolean shouldRenderStorageOverlayCached = false;

	private static final Pattern WINDOW_REGEX = Pattern.compile(".+ Backpack (?:\u2726 )?\\((\\d+)/(\\d+)\\)");
	private static final Pattern ECHEST_WINDOW_REGEX = Pattern.compile("Ender Chest \\((\\d+)/(\\d+)\\)");

	public void loadConfig(File file) {
		try (
			BufferedReader reader = new BufferedReader(new InputStreamReader(
				new GZIPInputStream(new FileInputStream(file)),
				StandardCharsets.UTF_8
			))
		) {
			storageConfig = GSON.fromJson(reader, StorageConfig.class);
		} catch (Exception ignored) {
		}
		if (storageConfig == null) {
			storageConfig = new StorageConfig();
		}
	}

	public void saveConfig(File file) {
		try {
			file.createNewFile();
			try (
				BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
					new GZIPOutputStream(new FileOutputStream(file)),
					StandardCharsets.UTF_8
				))
			) {
				writer.write(GSON.toJson(storageConfig));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public ItemStack getMissingBackpackStack(int storageId) {
		if (missingBackpackStacks[storageId] != null) {
			return missingBackpackStacks[storageId];
		}

		ItemStack stack = Utils.createItemStack(Item.getItemFromBlock(Blocks.stained_glass_pane),
			"\u00a7cEmpty Backpack Slot " + (storageId + 1), 12,
			"",
			"\u00a7eLeft-click a backpack",
			"\u00a7eitem on this slot to place",
			"\u00a7eit!"
		);

		missingBackpackStacks[storageId] = stack;
		return stack;
	}

	public boolean shouldRenderStorageOverlay(String containerName) {
		if (!NotEnoughUpdates.INSTANCE.config.storageGUI.enableStorageGUI3) {
			shouldRenderStorageOverlayCached = false;
			return false;
		}

		if (!NotEnoughUpdates.INSTANCE.hasSkyblockScoreboard()) {
			shouldRenderStorageOverlayCached = false;
			return false;
		}

		if (!(Minecraft.getMinecraft().currentScreen instanceof GuiChest)) {
			shouldRenderStorageOverlayCached = false;
			return false;
		}

		if (getCurrentWindowId() != -1 && getCurrentPageId() != -1) {
			shouldRenderStorageOverlayCached = true;
			return true;
		}

		shouldRenderStorageOverlayCached = containerName != null && containerName.trim().startsWith("Storage");
		return shouldRenderStorageOverlayCached;
	}

	public boolean shouldRenderStorageOverlayFast() {
		return shouldRenderStorageOverlayCached;
	}

	private StoragePage[] getPagesForProfile() {
		if (!NotEnoughUpdates.INSTANCE.hasSkyblockScoreboard()) return null;
		if (SBInfo.getInstance().currentProfile == null) return null;

		return storageConfig.pages.computeIfAbsent(SBInfo.getInstance().currentProfile, k -> new StoragePage[27]);
	}

	public StoragePage getPage(int pageIndex, boolean createPage) {
		if (pageIndex == -1) return null;

		StoragePage[] pages = getPagesForProfile();
		if (pages == null) return null;

		if (createPage && pages[pageIndex] == null) pages[pageIndex] = new StoragePage();

		return pages[pageIndex];
	}

	public void removePage(int pageIndex) {
		if (pageIndex == -1) return;

		StoragePage[] pages = getPagesForProfile();
		if (pages == null) return;

		pages[pageIndex] = null;
	}

	public StoragePage getCurrentPage() {
		return getPage(getCurrentPageId(), true);
	}

	private void setItemSlot(int index, ItemStack item) {
		StoragePage page = getCurrentPage();
		if (page != null) {
			page.items[index] = item;
		}
	}

	public int getCurrentPageId() {
		if (!(Minecraft.getMinecraft().currentScreen instanceof GuiChest)) {
			currentStoragePage = -1;
			return -1;
		}

		return currentStoragePage;
	}

	public int getCurrentWindowId() {
		if (!(Minecraft.getMinecraft().currentScreen instanceof GuiChest)) {
			currentStoragePage = -1;
			return -1;
		}

		GuiChest chest = (GuiChest) Minecraft.getMinecraft().currentScreen;

		return chest.inventorySlots.windowId;
	}

	public void sendToPage(int page) {
		if (desiredStoragePage != getCurrentPageId() &&
			System.currentTimeMillis() - storageOpenSwitchMillis < 100) return;
		if (getCurrentPageId() == page) return;

		if (page == 0) {
			NotEnoughUpdates.INSTANCE.sendChatMessage("/enderchest");
		} else if (getCurrentWindowId() != -1 && onStorageMenu) {
			if (page < 9) {
				sendMouseClick(getCurrentWindowId(), 9 + page);
			} else {
				sendMouseClick(getCurrentWindowId(), 27 + page - MAX_ENDER_CHEST_PAGES);
			}
		} else {
			boolean onEnderchest = page < MAX_ENDER_CHEST_PAGES && currentStoragePage < MAX_ENDER_CHEST_PAGES;
			boolean onStorage = page >= MAX_ENDER_CHEST_PAGES && currentStoragePage >= MAX_ENDER_CHEST_PAGES;
			if (currentStoragePage >= 0 && (onEnderchest || (onStorage))) {
				int currentPageDisplay = getDisplayIdForStorageId(currentStoragePage);
				int desiredPageDisplay = getDisplayIdForStorageId(page);

				if (onEnderchest && desiredPageDisplay > currentPageDisplay) {
					boolean isLastPage = true;
					for (int pageN = page + 1; pageN < MAX_ENDER_CHEST_PAGES; pageN++) {
						if (getDisplayIdForStorageId(pageN) >= 0) {
							isLastPage = false;
							break;
						}
					}
					if (isLastPage) {
						sendMouseClick(getCurrentWindowId(), 8);
						return;
					}
				}

				if (onStorage && page == MAX_ENDER_CHEST_PAGES) {
					sendMouseClick(getCurrentWindowId(), 5);
					return;
				} else if (onStorage && desiredPageDisplay == storageConfig.displayToStorageIdMap.size() - 1) {
					sendMouseClick(getCurrentWindowId(), 8);
					return;
				} else {
					int delta = desiredPageDisplay - currentPageDisplay;
					if (delta == -1) {
						sendMouseClick(getCurrentWindowId(), 6);
						return;
					} else if (delta == 1) {
						sendMouseClick(getCurrentWindowId(), 7);
						return;
					}
				}
			}

			storageOpenSwitchMillis = System.currentTimeMillis();
			desiredStoragePage = page;

			NotEnoughUpdates.INSTANCE.sendChatMessage("/storage " + (desiredStoragePage - 8));
		}
	}

	private void sendMouseClick(int windowId, int slotIndex) {
		EntityPlayerSP playerIn = Minecraft.getMinecraft().thePlayer;
		short short1 = playerIn.openContainer.getNextTransactionID(playerIn.inventory);
		ItemStack itemstack = playerIn.openContainer.getSlot(slotIndex).getStack();
		Minecraft.getMinecraft().getNetHandler().addToSendQueue(new C0EPacketClickWindow(
			windowId,
			slotIndex,
			0,
			0,
			itemstack,
			short1
		));
	}

	public int getDisplayIdForStorageId(int storageId) {
		if (storageId < 0) return -1;
		for (Map.Entry<Integer, Integer> entry : storageConfig.displayToStorageIdMap.entrySet()) {
			if (entry.getValue() == storageId) {
				return entry.getKey();
			}
		}
		return -1;
	}

	public int getDisplayIdForStorageIdRender(int storageId) {
		if (storageId < 0) return -1;
		for (Map.Entry<Integer, Integer> entry : storageConfig.displayToStorageIdMapRender.entrySet()) {
			if (entry.getValue() == storageId) {
				return entry.getKey();
			}
		}
		return -1;
	}

	public boolean onAnyClick() {
		if (onStorageMenu && desiredStoragePage >= 0) {
			if (desiredStoragePage < 9) {
				sendMouseClick(getCurrentWindowId(), 9 + desiredStoragePage);
			} else {
				sendMouseClick(getCurrentWindowId(), 27 + desiredStoragePage - MAX_ENDER_CHEST_PAGES);
			}
			desiredStoragePage = -1;
			return true;
		}
		return false;
	}

	public void openWindowPacket(S2DPacketOpenWindow packet) {
		shouldRenderStorageOverlayCached = false;
		if (!NotEnoughUpdates.INSTANCE.hasSkyblockScoreboard()) return;

		String windowTitle = Utils.cleanColour(packet.getWindowTitle().getUnformattedText());

		Matcher matcher = WINDOW_REGEX.matcher(windowTitle);
		Matcher matcherEchest = ECHEST_WINDOW_REGEX.matcher(windowTitle);

		currentStoragePage = -1;
		onStorageMenu = false;

		if (windowTitle.trim().equals("Storage")) {
			onStorageMenu = true;
		} else if (matcher.matches()) {
			int page = Integer.parseInt(matcher.group(1));

			if (page > 0 && page <= 18) {
				currentStoragePage = page - 1 + MAX_ENDER_CHEST_PAGES;

				int displayId = getDisplayIdForStorageId(currentStoragePage);
				if (displayId >= 0) StorageOverlay.getInstance().scrollToStorage(displayId, false);

				StoragePage spage = getCurrentPage();
				if (spage != null) {
					spage.rows = packet.getSlotCount() / 9 - 1;
				}
			}
		} else if (matcherEchest.matches()) {
			int page = Integer.parseInt(matcherEchest.group(1));

			if (page > 0 && page <= 9) {
				currentStoragePage = page - 1;

				int displayId = getDisplayIdForStorageId(currentStoragePage);
				if (displayId >= 0) StorageOverlay.getInstance().scrollToStorage(displayId, false);

				StoragePage spage = getCurrentPage();
				if (spage != null) {
					spage.rows = packet.getSlotCount() / 9 - 1;
				}
			}
		} else {
			StorageOverlay.getInstance().clearSearch();
			return;
		}
		StorageOverlay.getInstance().fastRenderCheck();

	}

	public void closeWindowPacket(S2EPacketCloseWindow packet) {
		shouldRenderStorageOverlayCached = false;
	}

	public void setSlotPacket(S2FPacketSetSlot packet) {
		if (!NotEnoughUpdates.INSTANCE.hasSkyblockScoreboard()) return;
		if (getCurrentWindowId() == -1 || getCurrentWindowId() != packet.func_149175_c()) return;

		if (getCurrentPageId() != -1) {
			StoragePage page = getCurrentPage();

			int slot = packet.func_149173_d();
			if (page != null && slot >= 9 && slot < 9 + page.rows * 9) {
				setItemSlot(packet.func_149173_d() - 9, packet.func_149174_e());
			}
		} else if (onStorageMenu) {
			if (storagePresent == null) {
				storagePresent = new boolean[27];
			}

			int slot = packet.func_149173_d();
			ItemStack stack = packet.func_149174_e();

			if (slot >= 9 && slot < 18) {
				int index = slot - 9;

				boolean changed = false;
				if (stack.getItem() == Item.getItemFromBlock(Blocks.stained_glass_pane) &&
					stack.getMetadata() == 14) {
					if (storagePresent[index]) changed = true;
					storagePresent[index] = false;
					removePage(index);
				} else {
					if (!storagePresent[index]) changed = true;
					storagePresent[index] = true;
					getPage(index, true).backpackDisplayStack = stack;
				}

				if (changed) {
					synchronized (storageConfig.displayToStorageIdMap) {
						storageConfig.displayToStorageIdMap.clear();
						storageConfig.displayToStorageIdMapRender.clear();
						int displayIndex = 0;
						for (int i = 0; i < storagePresent.length; i++) {
							if (storagePresent[i]) {
								storageConfig.displayToStorageIdMap.put(displayIndex, i);
								if (lastSearch != null && !lastSearch.isEmpty()) {
									StoragePage page = getPage(i, false);

									if (page != null) {
										updateSearchForPage(lastSearch, page);
										if (page.matchesSearch) {
											storageConfig.displayToStorageIdMapRender.put(displayIndex++, i);
										}
									}
								} else
									storageConfig.displayToStorageIdMapRender.put(displayIndex++, i);
							}
						}
					}
				}
			}

			if (slot >= 27 && slot < 45) {
				int index = (slot - 27) % 9 + (slot - 27) / 9 * 9 + MAX_ENDER_CHEST_PAGES;

				boolean changed = false;

				if (stack.getItem() == Item.getItemFromBlock(Blocks.stained_glass_pane)) {
					if (storagePresent[index]) changed = true;
					storagePresent[index] = false;
					removePage(index);
				} else {
					if (!storagePresent[index]) changed = true;
					storagePresent[index] = true;
					getPage(index, true).backpackDisplayStack = stack;
				}

				if (changed) {
					synchronized (storageConfig.displayToStorageIdMap) {
						storageConfig.displayToStorageIdMap.clear();
						storageConfig.displayToStorageIdMapRender.clear();
						int displayIndex = 0;
						for (int i = 0; i < storagePresent.length; i++) {
							if (storagePresent[i]) {
								storageConfig.displayToStorageIdMap.put(displayIndex, i);
								if (lastSearch != null && !lastSearch.isEmpty()) {
									StoragePage page = getPage(i, false);

									if (page != null) {
										updateSearchForPage(lastSearch, page);
										if (page.matchesSearch) {
											storageConfig.displayToStorageIdMapRender.put(displayIndex++, i);
										}
									}
								} else
									storageConfig.displayToStorageIdMapRender.put(displayIndex++, i);
							}
						}
					}
				}
			}
		}
	}

	public void updateSearchForPage(String searchStr, StoragePage page) {
		if (page == null) {
			return;
		}

		if (page.rows <= 0) {
			page.matchesSearch = true;
			return;
		}

		if (page.searchedId > searchId.get()) {
			page.searchedId = -1;
			return;
		}
		if (page.searchedId == searchId.get()) {
			return;
		}

		page.searchedId = searchId.get();

		if (searchStr == null || searchStr.trim().isEmpty()) {
			page.matchesSearch = true;
			return;
		}

		for (ItemStack stack : page.items) {
			if (stack != null && NotEnoughUpdates.INSTANCE.manager.doesStackMatchSearch(stack, searchStr)) {
				page.matchesSearch = true;
				return;
			}
		}
		page.matchesSearch = false;
	}

	public void searchDisplay(String searchStr) {
		if (storagePresent == null) return;

		synchronized (storageConfig.displayToStorageIdMapRender) {
			storageConfig.displayToStorageIdMapRender.clear();

			lastSearch = searchStr;
			int sid = searchId.incrementAndGet();
			int displayIndex = 0;
			for (int i = 0; i < storagePresent.length; i++) {
				if (storagePresent[i]) {
					StoragePage page = getPage(i, false);
					if (page != null) {
						if (page.rows > 0) {
							updateSearchForPage(searchStr, page);
							if (page.matchesSearch) {
								storageConfig.displayToStorageIdMapRender.put(displayIndex++, i);
							}
						} else {
							storageConfig.displayToStorageIdMapRender.put(displayIndex++, i);
							page.matchesSearch = true;
							page.searchedId = sid;
						}
					}
				}
			}
		}
	}

	public void setItemsPacket(S30PacketWindowItems packet) {
		if (!NotEnoughUpdates.INSTANCE.hasSkyblockScoreboard()) return;
		if (getCurrentWindowId() == -1 || getCurrentWindowId() != packet.func_148911_c()) return;

		if (getCurrentPageId() != -1) {
			StoragePage page = getPage(getCurrentPageId(), false);

			if (page != null) {
				int max = Math.min(page.rows * 9, packet.getItemStacks().length - 9);
				for (int i = 0; i < max; i++) {
					setItemSlot(i, packet.getItemStacks()[i + 9]);
				}
			}

		}
	}

	public void clientSendWindowClick(C0EPacketClickWindow packet) {
		if (!NotEnoughUpdates.INSTANCE.hasSkyblockScoreboard()) return;
		if (getCurrentWindowId() == -1 || getCurrentWindowId() != packet.getWindowId()) return;
		if (!(Minecraft.getMinecraft().currentScreen instanceof GuiChest)) return;
		ContainerChest containerChest = (ContainerChest) ((GuiChest) Minecraft.getMinecraft().currentScreen).inventorySlots;

		if (getCurrentPageId() != -1) {
			StoragePage page = getCurrentPage();
			if (page == null) return;

			IInventory inv = containerChest.getLowerChestInventory();
			int max = Math.min(9 + page.rows * 9, inv.getSizeInventory());
			for (int i = 9; i < max; i++) {
				setItemSlot(i - 9, inv.getStackInSlot(i));
			}
		}
	}
}
