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

package io.github.moulberry.notenoughupdates.miscgui;

import com.google.gson.JsonObject;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.auction.APIManager;
import io.github.moulberry.notenoughupdates.core.config.KeybindHelper;
import io.github.moulberry.notenoughupdates.core.util.StringUtils;
import io.github.moulberry.notenoughupdates.miscfeatures.SlotLocking;
import io.github.moulberry.notenoughupdates.mixins.AccessorGuiContainer;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TradeWindow {
	private static final ResourceLocation location = new ResourceLocation("notenoughupdates", "custom_trade.png");

	private static final int xSize = 176;
	private static final int ySize = 204;
	private static int guiLeft;
	private static int guiTop;

	private static long lastTradeMillis = -1;

	private static final long CHANGE_EXCLAM_MILLIS = 5000;

	private static Integer[] ourTradeIndexes = new Integer[16];
	private static Integer[] theirTradeIndexes = new Integer[16];
	private static String[] theirTradeOld = new String[16];
	private static Long[] theirTradeChangesMillis = new Long[16];

	public static boolean hypixelTradeWindowActive(String containerName) {
		return containerName != null && containerName.trim().startsWith("You     ");
	}

	public static boolean tradeWindowActive(String containerName) {
		if (!NotEnoughUpdates.INSTANCE.hasSkyblockScoreboard()) return false;
		if (!NotEnoughUpdates.INSTANCE.config.tradeMenu.enableCustomTrade) return false;

		if (hypixelTradeWindowActive(containerName)) {
			return true;
		}

		if (lastTradeMillis != -99) {
			lastTradeMillis = -99;
			ourTradeIndexes = new Integer[16];
			theirTradeIndexes = new Integer[16];
			theirTradeOld = new String[16];
			theirTradeChangesMillis = new Long[16];
		}

		return false;
	}

	private static void drawStringShadow(String str, float x, float y, int len) {
		for (int xOff = -2; xOff <= 2; xOff++) {
			for (int yOff = -2; yOff <= 2; yOff++) {
				if (Math.abs(xOff) != Math.abs(yOff)) {
					Utils.drawStringCenteredScaledMaxWidth(Utils.cleanColourNotModifiers(str),
						x + xOff / 2f, y + yOff / 2f, false, len,
						new Color(20, 20, 20, 100 / Math.max(Math.abs(xOff), Math.abs(yOff))).getRGB()
					);
				}
			}
		}

		Utils.drawStringCenteredScaledMaxWidth(str, x, y, false, len, new Color(64, 64, 64, 255).getRGB());
	}

	private static long getPrice(String internalName) {
		long pricePer = NotEnoughUpdates.INSTANCE.manager.auctionManager.getLowestBin(internalName);
		if (pricePer == -1) {
			JsonObject bazaarInfo = NotEnoughUpdates.INSTANCE.manager.auctionManager.getBazaarInfo(internalName);
			if (bazaarInfo != null && bazaarInfo.has("avg_buy")) {
				pricePer = (long) bazaarInfo.get("avg_buy").getAsDouble();
			}
		}
		if (pricePer == -1) {
			JsonObject info = NotEnoughUpdates.INSTANCE.manager.auctionManager.getItemAuctionInfo(internalName);
			if (info != null && !NotEnoughUpdates.INSTANCE.manager.auctionManager.isVanillaItem(internalName) &&
				info.has("price") && info.has("count")) {

				pricePer = (long) (info.get("price").getAsDouble() / info.get("count").getAsDouble());
			}
		}
		if (pricePer == -1) {
			APIManager.CraftInfo craftCost = NotEnoughUpdates.INSTANCE.manager.auctionManager.getCraftCost(internalName);
			if (craftCost != null) {
				pricePer = (int) craftCost.craftCost;
			}
		}
		return pricePer;
	}

	private static long processTopItems(
		ItemStack stack, Map<Long, Set<String>> topItems,
		Map<String, ItemStack> topItemsStack, Map<String, Integer> topItemsCount
	) {
		String internalname = NotEnoughUpdates.INSTANCE.manager.getInternalNameForItem(stack);
		if (internalname == null) {
			if (stack.getDisplayName().endsWith(" coins")) {
				String clean = Utils.cleanColour(stack.getDisplayName());

				int mult = 1;
				StringBuilder sb = new StringBuilder();
				for (int index = 0; index < clean.length(); index++) {
					char c = clean.charAt(index);
					if ("0123456789.".indexOf(c) >= 0) {
						sb.append(c);
					} else {
						switch (c) {
							case 'K':
							case 'k':
								mult = 1000;
								break;
							case 'M':
							case 'm':
								mult = 1000000;
								break;
							case 'B':
							case 'b':
								mult = 1000000000;
								break;
							default:
								break;
						}
						break;
					}
				}
				try {
					int coins = (int) (Float.parseFloat(sb.toString()) * mult);

					topItemsStack.putIfAbsent("TRADE_COINS", stack);

					long existingPrice = coins;
					Set<Long> toRemove = new HashSet<>();
					for (Map.Entry<Long, Set<String>> entry : topItems.entrySet()) {
						if (entry.getValue().contains("TRADE_COINS")) {
							entry.getValue().remove("TRADE_COINS");
							existingPrice += entry.getKey();
						}
						if (entry.getValue().isEmpty()) toRemove.add(entry.getKey());
					}
					topItems.keySet().removeAll(toRemove);

					Set<String> items = topItems.computeIfAbsent(existingPrice, k -> new HashSet<>());
					items.add("TRADE_COINS");

					return coins;

				} catch (Exception ignored) {
				}
			}
		} else {
			long pricePer = getPrice(internalname);
			if (pricePer > 0) {
				topItemsStack.putIfAbsent(internalname, stack);

				long price = pricePer * stack.stackSize;
				long priceInclBackpack = price;

				NBTTagCompound tag = stack.getTagCompound();
				if (tag != null && tag.hasKey("ExtraAttributes", 10)) {
					NBTTagCompound ea = tag.getCompoundTag("ExtraAttributes");

					byte[] bytes = null;
					for (String key : ea.getKeySet()) {
						if (key.endsWith("backpack_data") || key.equals("new_year_cake_bag_data")) {
							bytes = ea.getByteArray(key);
							break;
						}
					}
					if (bytes != null) {
						try {
							NBTTagCompound contents_nbt = CompressedStreamTools.readCompressed(new ByteArrayInputStream(bytes));
							NBTTagList items = contents_nbt.getTagList("i", 10);
							for (int k = 0; k < items.tagCount(); k++) {
								if (items.getCompoundTagAt(k).getKeySet().size() > 0) {
									NBTTagCompound nbt = items.getCompoundTagAt(k).getCompoundTag("tag");

									int id2 = items.getCompoundTagAt(k).getShort("id");
									int count2 = items.getCompoundTagAt(k).getByte("Count");
									int damage2 = items.getCompoundTagAt(k).getShort("Damage");

									if (id2 == 141) id2 = 391; //for some reason hypixel thinks carrots have id 141

									Item mcItem = Item.getItemById(id2);
									if (mcItem == null) continue;

									ItemStack stack2 = new ItemStack(mcItem, count2, damage2);
									stack2.setTagCompound(nbt);

									priceInclBackpack += processTopItems(stack2, topItems, topItemsStack, topItemsCount);
								}
							}
						} catch (Exception ignored) {
						}
					}
				}

				long existingPrice = price;
				Set<Long> toRemove = new HashSet<>();
				for (Map.Entry<Long, Set<String>> entry : topItems.entrySet()) {
					if (entry.getValue().contains(internalname)) {
						entry.getValue().remove(internalname);
						existingPrice += entry.getKey();
					}
					if (entry.getValue().isEmpty()) toRemove.add(entry.getKey());
				}
				topItems.keySet().removeAll(toRemove);

				Set<String> items = topItems.computeIfAbsent(existingPrice, k -> new HashSet<>());
				items.add(internalname);

				int count = topItemsCount.computeIfAbsent(internalname, l -> 0);
				topItemsCount.put(internalname, count + stack.stackSize);

				return priceInclBackpack;
			}
		}
		return 0;
	}

	private static int getBackpackValue(ItemStack stack) {
		int price = 0;

		NBTTagCompound tag = stack.getTagCompound();
		if (tag != null && tag.hasKey("ExtraAttributes", 10)) {
			NBTTagCompound ea = tag.getCompoundTag("ExtraAttributes");

			byte[] bytes = null;
			for (String key : ea.getKeySet()) {
				if (key.endsWith("backpack_data") || key.equals("new_year_cake_bag_data")) {
					bytes = ea.getByteArray(key);
					break;
				}
			}
			if (bytes != null) {
				try {
					NBTTagCompound contents_nbt = CompressedStreamTools.readCompressed(new ByteArrayInputStream(bytes));
					NBTTagList items = contents_nbt.getTagList("i", 10);
					for (int k = 0; k < items.tagCount(); k++) {
						if (items.getCompoundTagAt(k).getKeySet().size() > 0) {
							NBTTagCompound nbt = items.getCompoundTagAt(k).getCompoundTag("tag");
							String internalname2 = NotEnoughUpdates.INSTANCE.manager.createItemResolutionQuery()
																																			.withItemNBT(nbt)
																																			.resolveInternalName();
							if (internalname2 != null) {
								long pricePer2 = getPrice(internalname2);
								if (pricePer2 > 0) {
									int count2 = items.getCompoundTagAt(k).getByte("Count");
									price += pricePer2 * count2;
								}
							}
						}
					}
				} catch (Exception ignored) {
				}
			}
		}
		return price;
	}

	public static void render(int mouseX, int mouseY) {
		if (!(Minecraft.getMinecraft().currentScreen instanceof GuiContainer)) return;

		GuiContainer chest = ((GuiContainer) Minecraft.getMinecraft().currentScreen);
		ContainerChest cc = (ContainerChest) chest.inventorySlots;
		String containerName = cc.getLowerChestInventory().getDisplayName().getUnformattedText();

		ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());

		guiLeft = (scaledResolution.getScaledWidth() - xSize) / 2;
		guiTop = (scaledResolution.getScaledHeight() - ySize) / 2;

		List<String> tooltipToDisplay = null;
		ItemStack stackToRender = null;
		int tooltipLen = -1;

		//Set index mappings
		//Our slots
		TreeMap<Long, List<Integer>> ourTradeMap = new TreeMap<>();
		for (int i = 0; i < 16; i++) {
			ourTradeIndexes[i] = -1;

			int x = i % 4;
			int y = i / 4;
			int containerIndex = y * 9 + x;

			ItemStack stack = chest.inventorySlots.getInventory().get(containerIndex);
			if (stack == null) continue;

			String internalname = NotEnoughUpdates.INSTANCE.manager.getInternalNameForItem(stack);
			if (internalname == null) {
				if (stack.getDisplayName().endsWith(" coins")) {
					String clean = Utils.cleanColour(stack.getDisplayName());

					int mult = 1;
					StringBuilder sb = new StringBuilder();
					for (int index = 0; index < clean.length(); index++) {
						char c = clean.charAt(index);
						if ("0123456789.".indexOf(c) >= 0) {
							sb.append(c);
						} else {
							switch (c) {
								case 'K':
								case 'k':
									mult = 1000;
									break;
								case 'M':
								case 'm':
									mult = 1000000;
									break;
								case 'B':
								case 'b':
									mult = 1000000000;
									break;
								default:
									break;
							}
							break;
						}
					}
					try {
						int coins = (int) (Float.parseFloat(sb.toString()) * mult);

						List<Integer> list = ourTradeMap.computeIfAbsent((long) coins, k -> new ArrayList<>());
						list.add(containerIndex);

					} catch (Exception ignored) {
						List<Integer> list = ourTradeMap.computeIfAbsent(-1L, k -> new ArrayList<>());
						list.add(containerIndex);
					}
				} else {
					List<Integer> list = ourTradeMap.computeIfAbsent(-1L, k -> new ArrayList<>());
					list.add(containerIndex);
				}
			} else {
				long price = getPrice(internalname);
				if (price == -1) price = 0;

				price += getBackpackValue(stack);

				List<Integer> list = ourTradeMap.computeIfAbsent(price, k -> new ArrayList<>());
				list.add(containerIndex);
			}
		}
		long currentTime = System.currentTimeMillis();
		List<String> theirTradeCurrent = new ArrayList<>();
		TreeMap<Integer, List<Integer>> theirTradeMap = new TreeMap<>();
		HashMap<String, Integer> displayCountMap = new HashMap<>();
		for (int i = 0; i < 16; i++) {
			theirTradeIndexes[i] = -1;
			if (theirTradeChangesMillis[i] == null || currentTime - theirTradeChangesMillis[i] > CHANGE_EXCLAM_MILLIS) {
				theirTradeChangesMillis[i] = -1L;
			}

			int x = i % 4;
			int y = i / 4;
			int containerIndex = y * 9 + x + 5;

			ItemStack stack = chest.inventorySlots.getInventory().get(containerIndex);
			if (stack == null) continue;

			NBTTagCompound tag = stack.getTagCompound();
			String uuid;
			if (tag != null && tag.hasKey("ExtraAttributes", 10)) {
				NBTTagCompound ea = tag.getCompoundTag("ExtraAttributes");

				if (ea.hasKey("uuid", 8)) {
					uuid = ea.getString("uuid");
				} else {
					int displayCount = displayCountMap.computeIfAbsent(stack.getDisplayName(), k -> 0);
					uuid = stack.getDisplayName() + ":" + displayCount;
					displayCountMap.put(stack.getDisplayName(), displayCount + 1);
				}
			} else {
				int displayCount = displayCountMap.computeIfAbsent(stack.getDisplayName(), k -> 0);
				uuid = stack.getDisplayName() + ":" + displayCount;
				displayCountMap.put(stack.getDisplayName(), displayCount + 1);
			}
			if (uuid != null) theirTradeCurrent.add(uuid);

			String internalname = NotEnoughUpdates.INSTANCE.manager.getInternalNameForItem(stack);
			if (internalname == null) {
				if (stack.getDisplayName().endsWith(" coins")) {
					String clean = Utils.cleanColour(stack.getDisplayName());

					int mult = 1;
					StringBuilder sb = new StringBuilder();
					for (int index = 0; index < clean.length(); index++) {
						char c = clean.charAt(index);
						if ("0123456789.".indexOf(c) >= 0) {
							sb.append(c);
						} else {
							switch (c) {
								case 'K':
								case 'k':
									mult = 1000;
									break;
								case 'M':
								case 'm':
									mult = 1000000;
									break;
								case 'B':
								case 'b':
									mult = 1000000000;
									break;
								default:
									break;
							}
							break;
						}
					}
					try {
						int coins = (int) (Float.parseFloat(sb.toString()) * mult);

						List<Integer> list = theirTradeMap.computeIfAbsent(coins, k -> new ArrayList<>());
						list.add(containerIndex);

					} catch (Exception ignored) {
						List<Integer> list = theirTradeMap.computeIfAbsent(-1, k -> new ArrayList<>());
						list.add(containerIndex);
					}
				} else {
					List<Integer> list = theirTradeMap.computeIfAbsent(-1, k -> new ArrayList<>());
					list.add(containerIndex);
				}
			} else {
				JsonObject info = NotEnoughUpdates.INSTANCE.manager.auctionManager.getItemAuctionInfo(internalname);
				int price = -1;
				if (info != null && info.has("price") && info.has("count")) {
					int auctionPricePer = (int) (info.get("price").getAsFloat() / info.get("count").getAsFloat());

					price = auctionPricePer * stack.stackSize;
				} else {
					JsonObject bazaarInfo = NotEnoughUpdates.INSTANCE.manager.auctionManager.getBazaarInfo(internalname);
					if (bazaarInfo != null && bazaarInfo.has("avg_buy")) {
						price = (int) bazaarInfo.get("avg_buy").getAsFloat() * stack.stackSize;
					}
				}

				price += getBackpackValue(stack);

				List<Integer> list = theirTradeMap.computeIfAbsent(price, k -> new ArrayList<>());
				list.add(containerIndex);
			}
		}
		int ourTradeIndex = 0;
		for (Map.Entry<Long, List<Integer>> entry : ourTradeMap.descendingMap().entrySet()) {
			for (Integer index : entry.getValue()) {
				ourTradeIndexes[ourTradeIndex++] = index;
			}
		}

		//Their slots
		int maxMissing = 16 - theirTradeCurrent.size();
		int j = 0;
		for (int i = 0; i < 16; i++) {
			while (j <= 15 && (j - i < maxMissing) && theirTradeChangesMillis[j] >= 0) j++;
			j = Math.min(15, j);

			String oldUUID = theirTradeOld[i];
			if (oldUUID != null && !theirTradeCurrent.contains(oldUUID)) {
				theirTradeChangesMillis[j] = System.currentTimeMillis();
			}
			j++;
		}

		for (int i = 0; i < 16; i++) {
			theirTradeOld[i] = null;
		}
		int theirTradeIndex = 0;
		displayCountMap.clear();
		j = 0;
		for (Map.Entry<Integer, List<Integer>> entry : theirTradeMap.descendingMap().entrySet()) {
			for (Integer index : entry.getValue()) {
				while (j <= 15 && (j - theirTradeIndex < maxMissing) && theirTradeChangesMillis[j] >= 0) j++;
				j = Math.min(15, j);

				theirTradeIndexes[j] = index;

				ItemStack stack = chest.inventorySlots.getInventory().get(index);
				if (stack == null) continue;

				NBTTagCompound tag = stack.getTagCompound();
				String uuid;
				if (tag != null && tag.hasKey("ExtraAttributes", 10)) {
					NBTTagCompound ea = tag.getCompoundTag("ExtraAttributes");

					if (ea.hasKey("uuid", 8)) {
						uuid = ea.getString("uuid");
					} else {
						int displayCount = displayCountMap.computeIfAbsent(stack.getDisplayName(), k -> 0);
						uuid = stack.getDisplayName() + ":" + displayCount;
						displayCountMap.put(stack.getDisplayName(), displayCount + 1);
					}
				} else {
					int displayCount = displayCountMap.computeIfAbsent(stack.getDisplayName(), k -> 0);
					uuid = stack.getDisplayName() + ":" + displayCount;
					displayCountMap.put(stack.getDisplayName(), displayCount + 1);
				}
				//System.out.println(uuid);
				theirTradeOld[theirTradeIndex] = uuid;

				j++;
				theirTradeIndex++;
			}
		}

		GlStateManager.color(1, 1, 1, 1);
		Minecraft.getMinecraft().getTextureManager().bindTexture(location);
		Utils.drawTexturedRect(guiLeft, guiTop, xSize, ySize, 0, 176 / 256f, 0, 204 / 256f, GL11.GL_NEAREST);

		Utils.drawStringF(new ChatComponentTranslation("container.inventory").getUnformattedText(),
			guiLeft + 8, guiTop + 111, false, 4210752
		);
		Utils.drawStringF("You", guiLeft + 8, guiTop + 5, false, 421752);
		String[] split = containerName.split(" ");
		if (split.length >= 1) {
			Utils.drawStringF(split[split.length - 1],
				guiLeft + 167 - Minecraft.getMinecraft().fontRendererObj.getStringWidth(split[split.length - 1]),
				guiTop + 5, false, 4210752
			);
		}

		int index = 0;
		for (ItemStack stack : Minecraft.getMinecraft().thePlayer.inventory.mainInventory) {
			int x = 8 + 18 * (index % 9);
			int y = 104 + 18 * (index / 9);
			if (index < 9) y = 180;

			((AccessorGuiContainer) chest).doDrawSlot(new Slot(
				Minecraft.getMinecraft().thePlayer.inventory,
				index,
				guiLeft + x,
				guiTop + y
			));

			int col = 0x80ffffff;
			if (SlotLocking.getInstance().isSlotIndexLocked(index)) {
				col = 0x80ff8080;
			}

			if (mouseX > guiLeft + x - 1 && mouseX < guiLeft + x + 18) {
				if (mouseY > guiTop + y - 1 && mouseY < guiTop + y + 18) {
					if (stack != null) stackToRender = stack;

					GlStateManager.disableLighting();
					GlStateManager.disableDepth();
					GlStateManager.colorMask(true, true, true, false);
					Utils.drawGradientRect(guiLeft + x, guiTop + y,
						guiLeft + x + 16, guiTop + y + 16, col, col
					);
					GlStateManager.colorMask(true, true, true, true);
					GlStateManager.enableLighting();
					GlStateManager.enableDepth();
				}
			}

			index++;
		}

		for (int i = 0; i < 16; i++) {
			int x = i % 4;
			int y = i / 4;

			int containerIndex = ourTradeIndexes[i];

			ItemStack stack = null;
			if (containerIndex >= 0) {
				stack = chest.inventorySlots.getInventory().get(containerIndex);
				Utils.drawItemStack(stack, guiLeft + 10 + x * 18, guiTop + 15 + y * 18);
			}

			if (mouseX > guiLeft + 10 + x * 18 - 1 && mouseX < guiLeft + 10 + x * 18 + 18) {
				if (mouseY > guiTop + 15 + y * 18 - 1 && mouseY < guiTop + 15 + y * 18 + 18) {
					if (stack != null) stackToRender = stack;

					GlStateManager.disableLighting();
					GlStateManager.disableDepth();
					GlStateManager.colorMask(true, true, true, false);
					Utils.drawGradientRect(guiLeft + 10 + x * 18, guiTop + 15 + y * 18,
						guiLeft + 10 + x * 18 + 16, guiTop + 15 + y * 18 + 16, -2130706433, -2130706433
					);
					GlStateManager.colorMask(true, true, true, true);
					GlStateManager.enableLighting();
					GlStateManager.enableDepth();
				}
			}
		}

		ItemStack bidStack = chest.inventorySlots.getInventory().get(36);
		if (bidStack != null) {
			Utils.drawItemStack(bidStack, guiLeft + 10, guiTop + 90);
			if (mouseX > guiLeft + 10 - 1 && mouseX < guiLeft + 10 + 18) {
				if (mouseY > guiTop + 90 - 1 && mouseY < guiTop + 90 + 18) {
					tooltipToDisplay = bidStack.getTooltip(
						Minecraft.getMinecraft().thePlayer,
						Minecraft.getMinecraft().gameSettings.advancedItemTooltips
					);
				}
			}
		}

		ItemStack confirmStack = chest.inventorySlots.getInventory().get(39);
		if (confirmStack != null) {
			String confirmDisplay = confirmStack.getDisplayName();
			if (!confirmDisplay.equals(EnumChatFormatting.GREEN + "Trading!")) {
				if (mouseX > guiLeft + 81 - 51 && mouseX < guiLeft + 81) {
					if (mouseY > guiTop + 91 && mouseY < guiTop + 91 + 14) {
						tooltipToDisplay = confirmStack.getTooltip(
							Minecraft.getMinecraft().thePlayer,
							Minecraft.getMinecraft().gameSettings.advancedItemTooltips
						);
					}
				}

				Minecraft.getMinecraft().getTextureManager().bindTexture(location);
				Utils.drawTexturedRect(guiLeft + 81 - 51, guiTop + 91, 51, 14,
					0, 51 / 256f, ySize / 256f, (ySize + 14) / 256f, GL11.GL_NEAREST
				);

				Pattern pattern = Pattern.compile(
					EnumChatFormatting.GRAY + "\\(" + EnumChatFormatting.YELLOW + "([0-9]+)" + EnumChatFormatting.GRAY + "\\)");
				Matcher matcher = pattern.matcher(confirmDisplay);

				if (!confirmDisplay.equals(EnumChatFormatting.YELLOW + "Warning!") &&
					!confirmDisplay.equals(EnumChatFormatting.YELLOW + "Deal!")) {
					lastTradeMillis = -1;
				}

				if (matcher.find()) {
					String numS = matcher.group(1);
					int num = Integer.parseInt(numS);

					Utils.drawStringCentered(
						EnumChatFormatting.DARK_RED + "Check " + EnumChatFormatting.BOLD + (char) (9311 + num),
						guiLeft + 56, guiTop + 99,
						false,
						4210752
					);
				} else if (confirmDisplay.equals(EnumChatFormatting.AQUA + "Gift!")) {
					Utils.drawStringCentered(EnumChatFormatting.GREEN + "Accept", guiLeft + 56, guiTop + 99, true, 4210752);
				} else if (confirmDisplay.equals(EnumChatFormatting.GREEN + "Deal accepted!")) {
					Utils.drawStringCentered(EnumChatFormatting.GREEN + "Accepted", guiLeft + 56, guiTop + 99, true, 4210752);
				} else if (lastTradeMillis > 0) {
					long delta = System.currentTimeMillis() - lastTradeMillis;
					if (delta > 2000) {
						Utils.drawStringCentered(
							EnumChatFormatting.GREEN + "Accept",
							guiLeft + 56,
							guiTop + 99,
							true,
							4210752
						);
					} else {
						Utils.drawStringCentered(
							EnumChatFormatting.YELLOW + "Trade " + EnumChatFormatting.BOLD + (char) (9312 + (2000 - delta) / 1000),
							guiLeft + 56,
							guiTop + 99,
							true,
							4210752
						);
					}
				} else {
					Utils.drawStringCentered(
						EnumChatFormatting.YELLOW + "Trade " + EnumChatFormatting.BOLD + (char) (9314),
						guiLeft + 56,
						guiTop + 99,
						true,
						4210752
					);
				}
			}
		}

		ItemStack theirConfirmStack = chest.inventorySlots.getInventory().get(41);
		if (theirConfirmStack != null) {
			String confirmDisplay = theirConfirmStack.getDisplayName();
			if (mouseX > guiLeft + 95 && mouseX < guiLeft + 95 + 51) {
				if (mouseY > guiTop + 91 && mouseY < guiTop + 91 + 14) {
					tooltipToDisplay = theirConfirmStack.getTooltip(
						Minecraft.getMinecraft().thePlayer,
						Minecraft.getMinecraft().gameSettings.advancedItemTooltips
					);
				}
			}

			GlStateManager.color(1, 1, 1, 1);
			Minecraft.getMinecraft().getTextureManager().bindTexture(location);
			Utils.drawTexturedRect(guiLeft + 95, guiTop + 91, 51, 14,
				0, 51 / 256f, ySize / 256f, (ySize + 14) / 256f, GL11.GL_NEAREST
			);

			if (confirmDisplay.equals(EnumChatFormatting.YELLOW + "Pending their confirm")) {
				Utils.drawStringCentered(EnumChatFormatting.YELLOW + "Pending", guiLeft + 120, guiTop + 99, true, 4210752);
			} else if (confirmDisplay.equals(EnumChatFormatting.YELLOW + "Deal timer...")) {
				Utils.drawStringCentered(EnumChatFormatting.YELLOW + "Pending", guiLeft + 120, guiTop + 99, true, 4210752);
			} else if (confirmDisplay.equals(EnumChatFormatting.GREEN + "Other player confirmed!")) {
				Utils.drawStringCentered(EnumChatFormatting.GREEN + "Accepted", guiLeft + 120, guiTop + 99, true, 4210752);
			}
		}

		for (int i = 0; i < 16; i++) {
			int x = i % 4;
			int y = i / 4;

			int containerIndex = theirTradeIndexes[i];

			ItemStack stack = null;
			if (containerIndex >= 0) {
				stack = chest.inventorySlots.getInventory().get(containerIndex);
				Utils.drawItemStack(stack, guiLeft + 96 + x * 18, guiTop + 15 + y * 18);
			}

			if (currentTime % 400 > 200 && theirTradeChangesMillis[i] != null && theirTradeChangesMillis[i] > 0) {
				GlStateManager.translate(0, 0, 200);
				GlStateManager.color(1, 1, 1, 1);
				Minecraft.getMinecraft().getTextureManager().bindTexture(location);
				Utils.drawTexturedRect(guiLeft + 96 + x * 18, guiTop + 15 + y * 18, 16, 16,
					51 / 256f, 67 / 256f, 204 / 256f, 220 / 256f, GL11.GL_NEAREST
				);
				GlStateManager.translate(0, 0, -200);
			}

			if (mouseX > guiLeft + 96 + x * 18 - 1 && mouseX < guiLeft + 96 + x * 18 + 18) {
				if (mouseY > guiTop + 15 + y * 18 - 1 && mouseY < guiTop + 15 + y * 18 + 18) {
					if (stack != null) stackToRender = stack;

					GlStateManager.disableLighting();
					GlStateManager.disableDepth();
					GlStateManager.colorMask(true, true, true, false);
					Utils.drawGradientRect(guiLeft + 96 + x * 18, guiTop + 15 + y * 18,
						guiLeft + 96 + x * 18 + 16, guiTop + 15 + y * 18 + 16, -2130706433, -2130706433
					);
					GlStateManager.colorMask(true, true, true, true);
					GlStateManager.enableLighting();
					GlStateManager.enableDepth();
				}
			}
		}

		if (NotEnoughUpdates.INSTANCE.config.tradeMenu.customTradePrices) {
			TreeMap<Long, Set<String>> ourTopItems = new TreeMap<>();
			TreeMap<String, ItemStack> ourTopItemsStack = new TreeMap<>();
			TreeMap<String, Integer> ourTopItemsCount = new TreeMap<>();
			double ourPrice = 0;
			for (int i = 0; i < 16; i++) {
				int x = i % 4;
				int y = i / 4;
				int containerIndex = y * 9 + x;

				ItemStack stack = chest.inventorySlots.getInventory().get(containerIndex);
				if (stack == null) continue;

				ourPrice += processTopItems(stack, ourTopItems, ourTopItemsStack, ourTopItemsCount);
			}
			TreeMap<Long, Set<String>> theirTopItems = new TreeMap<>();
			TreeMap<String, ItemStack> theirTopItemsStack = new TreeMap<>();
			TreeMap<String, Integer> theirTopItemsCount = new TreeMap<>();
			double theirPrice = 0;
			for (int i = 0; i < 16; i++) {
				int x = i % 4;
				int y = i / 4;
				int containerIndex = y * 9 + x + 5;

				ItemStack stack = chest.inventorySlots.getInventory().get(containerIndex);
				if (stack == null) continue;

				theirPrice += processTopItems(stack, theirTopItems, theirTopItemsStack, theirTopItemsCount);
			}

			GlStateManager.disableLighting();
			GlStateManager.color(1, 1, 1, 1);
			Minecraft.getMinecraft().getTextureManager().bindTexture(location);
			Utils.drawTexturedRect(guiLeft - 80 - 3, guiTop, 80, 106,
				176 / 256f, 1, 0, 106 / 256f, GL11.GL_NEAREST
			);
			drawStringShadow(EnumChatFormatting.GOLD.toString() + EnumChatFormatting.BOLD + "Total Value",
				guiLeft - 40 - 3, guiTop + 11, 72
			);
			drawStringShadow(EnumChatFormatting.GOLD.toString() + EnumChatFormatting.BOLD + StringUtils.formatNumber(ourPrice),
				guiLeft - 40 - 3, guiTop + 21, 72
			);

			int ourTopIndex = Math.max(0, 3 - ourTopItemsStack.size());
			out:
			for (Map.Entry<Long, Set<String>> entry : ourTopItems.descendingMap().entrySet()) {
				for (String ourTopItemInternal : entry.getValue()) {
					ItemStack stack = ourTopItemsStack.get(ourTopItemInternal);
					if (stack == null) continue;

					if (NotEnoughUpdates.INSTANCE.config.tradeMenu.customTradePriceStyle) {
						String countS = "";
						if (ourTopItemsCount.containsKey(ourTopItemInternal)) {
							int count = ourTopItemsCount.get(ourTopItemInternal);
							if (count > 999999) {
								countS = Math.floor(count / 10000f) / 100f + "m";
							} else if (count > 999) {
								countS = Math.floor(count / 10f) / 100f + "k";
							} else {
								countS = "" + count;
							}
						}

						Utils.drawItemStackWithText(stack, guiLeft - 75 - 3, guiTop + 49 + 18 * ourTopIndex, countS);

						GlStateManager.disableLighting();
						GlStateManager.disableBlend();
						GlStateManager.color(1, 1, 1, 1);
						drawStringShadow(
							EnumChatFormatting.GOLD.toString() + EnumChatFormatting.BOLD + StringUtils.formatNumber(entry.getKey()),
							guiLeft - 29 - 3,
							guiTop + 57 + 18 * ourTopIndex,
							52
						);
						GlStateManager.enableBlend();
					} else {
						drawStringShadow(
							stack.getDisplayName() + EnumChatFormatting.GRAY + "x" + ourTopItemsCount.get(ourTopItemInternal),
							guiLeft - 40 - 3,
							guiTop + 46 + 20 * ourTopIndex,
							72
						);
						drawStringShadow(
							EnumChatFormatting.GOLD.toString() + EnumChatFormatting.BOLD + StringUtils.formatNumber(entry.getKey()),
							guiLeft - 40 - 3,
							guiTop + 56 + 20 * ourTopIndex,
							72
						);
					}

					if (++ourTopIndex >= 3) break out;
				}
			}

			GlStateManager.color(1, 1, 1, 1);
			Minecraft.getMinecraft().getTextureManager().bindTexture(location);
			Utils.drawTexturedRect(guiLeft + xSize + 3, guiTop, 80, 106,
				176 / 256f, 1, 0, 106 / 256f, GL11.GL_NEAREST
			);
			drawStringShadow(EnumChatFormatting.GOLD.toString() + EnumChatFormatting.BOLD + "Total Value",
				guiLeft + xSize + 3 + 40, guiTop + 11, 72
			);
			drawStringShadow(EnumChatFormatting.GOLD.toString() + EnumChatFormatting.BOLD + StringUtils.formatNumber(theirPrice),
				guiLeft + xSize + 3 + 40, guiTop + 21, 72
			);

			int theirTopIndex = Math.max(0, 3 - theirTopItemsStack.size());
			out:
			for (Map.Entry<Long, Set<String>> entry : theirTopItems.descendingMap().entrySet()) {
				for (String theirTopItemInternal : entry.getValue()) {
					ItemStack stack = theirTopItemsStack.get(theirTopItemInternal);
					if (stack == null) continue;

					if (NotEnoughUpdates.INSTANCE.config.tradeMenu.customTradePriceStyle) {
						String countS = "";
						if (theirTopItemsCount.containsKey(theirTopItemInternal)) {
							int count = theirTopItemsCount.get(theirTopItemInternal);
							if (count > 999999) {
								countS = Math.floor(count / 10000f) / 100f + "m";
							} else if (count > 999) {
								countS = Math.floor(count / 10f) / 100f + "k";
							} else {
								countS = "" + count;
							}
						}

						Utils.drawItemStackWithText(stack, guiLeft + xSize + 25 + 3 - 16, guiTop + 49 + 18 * theirTopIndex, countS);

						GlStateManager.disableLighting();
						GlStateManager.disableBlend();
						GlStateManager.color(1, 1, 1, 1);
						drawStringShadow(
							EnumChatFormatting.GOLD.toString() + EnumChatFormatting.BOLD + StringUtils.formatNumber(entry.getKey()),
							guiLeft + xSize + 3 + 51,
							guiTop + 57 + 18 * theirTopIndex,
							52
						);
						GlStateManager.enableBlend();
					} else {
						drawStringShadow(stack.getDisplayName(),
							guiLeft + xSize + 3 + 40, guiTop + 46 + 20 * theirTopIndex, 72
						);
						drawStringShadow(
							EnumChatFormatting.GOLD.toString() + EnumChatFormatting.BOLD + StringUtils.formatNumber(entry.getKey()),
							guiLeft + xSize + 3 + 40,
							guiTop + 56 + 20 * theirTopIndex,
							72
						);
					}

					if (++theirTopIndex >= 3) break out;
				}
			}
		}

		boolean button1 = NotEnoughUpdates.INSTANCE.config.tradeMenu.customTradePriceStyle;
		boolean button2 = NotEnoughUpdates.INSTANCE.config.tradeMenu.customTradePrices;
		boolean button3 = NotEnoughUpdates.INSTANCE.config.tradeMenu.enableCustomTrade;

		GlStateManager.color(1, 1, 1, 1);
		Minecraft.getMinecraft().getTextureManager().bindTexture(location);
		Utils.drawTexturedRect(guiLeft + xSize + 3, guiTop + ySize - 19, 17, 17,
			(button3 ? 17 : 0) / 256f, (button3 ? 34 : 17) / 256f, 218 / 256f, 235 / 256f, GL11.GL_NEAREST
		);
		Utils.drawTexturedRect(guiLeft + xSize + 3, guiTop + ySize - 38, 17, 17,
			(button2 ? 17 : 0) / 256f, (button2 ? 34 : 17) / 256f, 218 / 256f, 235 / 256f, GL11.GL_NEAREST
		);
		Utils.drawTexturedRect(guiLeft + xSize + 3, guiTop + ySize - 57, 17, 17,
			(button1 ? 17 : 0) / 256f, (button1 ? 34 : 17) / 256f, 218 / 256f, 235 / 256f, GL11.GL_NEAREST
		);

		if (mouseX >= guiLeft + xSize + 3 && mouseX <= guiLeft + xSize + 3 + 17) {
			if (mouseY >= guiTop + ySize - 19 && mouseY <= guiTop + ySize - 19 + 17) {
				tooltipToDisplay = new ArrayList<String>() {{
					add(EnumChatFormatting.GOLD + "Enable Custom Trade Menu");
					add(EnumChatFormatting.GRAY + "Use this menu instead of the default trade window");
				}};
				tooltipLen = 200;
			} else if (mouseY >= guiTop + ySize - 38 && mouseY <= guiTop + ySize - 38 + 17) {
				tooltipToDisplay = new ArrayList<String>() {{
					add(EnumChatFormatting.GOLD + "Price Information");
					add(EnumChatFormatting.GRAY + "Show the price of items on both sides");
				}};
				tooltipLen = 200;
			} else if (mouseY >= guiTop + ySize - 57 && mouseY <= guiTop + ySize - 57 + 17) {
				tooltipToDisplay = new ArrayList<String>() {{
					add(EnumChatFormatting.GOLD + "Trade Prices Style");
					add(EnumChatFormatting.GRAY + "Changes the style of the top item prices");
				}};
				tooltipLen = 200;
			}
		}

		if (stackToRender != null) {
			tooltipToDisplay = stackToRender.getTooltip(
				Minecraft.getMinecraft().thePlayer,
				Minecraft.getMinecraft().gameSettings.advancedItemTooltips
			);
		}

		if (tooltipToDisplay != null) {
			Utils.drawHoveringText(
				tooltipToDisplay,
				mouseX,
				mouseY,
				scaledResolution.getScaledWidth(),
				scaledResolution.getScaledHeight(),
				tooltipLen
			);
		}
	}

	public static void handleMouseInput() {
		if (!(Minecraft.getMinecraft().currentScreen instanceof GuiContainer)) return;

		ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
		int width = scaledResolution.getScaledWidth();
		int height = scaledResolution.getScaledHeight();

		int mouseX = Mouse.getEventX() * width / Minecraft.getMinecraft().displayWidth;
		int mouseY = height - Mouse.getEventY() * height / Minecraft.getMinecraft().displayHeight - 1;

		GuiContainer chest = ((GuiContainer) Minecraft.getMinecraft().currentScreen);

		if (Mouse.getEventButtonState() && Mouse.isButtonDown(0)) {
			int index = 0;
			for (ItemStack stack : Minecraft.getMinecraft().thePlayer.inventory.mainInventory) {
				if (stack == null) {
					index++;
					continue;
				}

				int x = 8 + 18 * (index % 9);
				int y = 104 + 18 * (index / 9);
				if (index < 9) y = 180;

				if (mouseX > guiLeft + x && mouseX < guiLeft + x + 16) {
					if (mouseY > guiTop + y && mouseY < guiTop + y + 16) {
						Slot slot = chest.inventorySlots.getSlotFromInventory(Minecraft.getMinecraft().thePlayer.inventory, index);
						if (!NotEnoughUpdates.INSTANCE.config.slotLocking.lockSlotsInTrade ||
							!SlotLocking.getInstance().isSlotLocked(slot)) {
							Minecraft.getMinecraft().playerController.windowClick(
								chest.inventorySlots.windowId,
								slot.slotNumber, 0, 0, Minecraft.getMinecraft().thePlayer
							);
						}
						return;
					}
				}

				index++;
			}

			for (int i = 0; i < 16; i++) {
				int x = i % 4;
				int y = i / 4;

				Integer containerIndex = ourTradeIndexes[i];
				if (containerIndex == null || containerIndex < 0) continue;

				if (mouseX > guiLeft + 10 + x * 18 - 1 && mouseX < guiLeft + 10 + x * 18 + 18) {
					if (mouseY > guiTop + 15 + y * 18 - 1 && mouseY < guiTop + 15 + y * 18 + 18) {
						Minecraft.getMinecraft().playerController.windowClick(
							chest.inventorySlots.windowId,
							containerIndex, 2, 3, Minecraft.getMinecraft().thePlayer
						);
						return;
					}
				}
			}

			if (mouseX > guiLeft + 10 - 1 && mouseX < guiLeft + 10 + 18) {
				if (mouseY > guiTop + 90 - 1 && mouseY < guiTop + 90 + 18) {
					Minecraft.getMinecraft().playerController.windowClick(
						chest.inventorySlots.windowId,
						36, 2, 3, Minecraft.getMinecraft().thePlayer
					);
					return;
				}
			}

			ItemStack confirmStack = chest.inventorySlots.getInventory().get(39);
			if (confirmStack != null) {
				String confirmDisplay = confirmStack.getDisplayName();
				if (!confirmDisplay.equals(EnumChatFormatting.GREEN + "Trading!")) {
					if (mouseX > guiLeft + 42 && mouseX < guiLeft + 42 + 40) {
						if (mouseY > guiTop + 92 && mouseY < guiTop + 92 + 14) {
							if ((confirmDisplay.equals(EnumChatFormatting.YELLOW + "Warning!") ||
								confirmDisplay.equals(EnumChatFormatting.YELLOW + "Deal!")) && lastTradeMillis < 0) {
								lastTradeMillis = System.currentTimeMillis();
							} else if (lastTradeMillis < 0 || System.currentTimeMillis() - lastTradeMillis > 2000) {
								Minecraft.getMinecraft().playerController.windowClick(
									chest.inventorySlots.windowId,
									39, 2, 3, Minecraft.getMinecraft().thePlayer
								);
								return;
							}
						}
					}

				}
			}

			if (mouseX >= guiLeft + xSize + 3 && mouseX <= guiLeft + xSize + 3 + 17) {
				if (mouseY >= guiTop + ySize - 19 && mouseY <= guiTop + ySize - 19 + 17) {
					NotEnoughUpdates.INSTANCE.config.tradeMenu.enableCustomTrade =
						!NotEnoughUpdates.INSTANCE.config.tradeMenu.enableCustomTrade;
				} else if (mouseY >= guiTop + ySize - 38 && mouseY <= guiTop + ySize - 38 + 17) {
					NotEnoughUpdates.INSTANCE.config.tradeMenu.customTradePrices =
						!NotEnoughUpdates.INSTANCE.config.tradeMenu.customTradePrices;
				} else if (mouseY >= guiTop + ySize - 57 && mouseY <= guiTop + ySize - 57 + 17) {
					NotEnoughUpdates.INSTANCE.config.tradeMenu.customTradePriceStyle =
						!NotEnoughUpdates.INSTANCE.config.tradeMenu.customTradePriceStyle;
				}
			}
		}
	}

	public static boolean keyboardInput() {
		if (NotEnoughUpdates.INSTANCE.config.slotLocking.enableSlotLocking &&
			NotEnoughUpdates.INSTANCE.config.slotLocking.lockSlotsInTrade &&
			!Keyboard.isRepeatEvent() &&
			KeybindHelper.isKeyPressed(NotEnoughUpdates.INSTANCE.config.slotLocking.slotLockKey)) {
			ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
			int width = scaledResolution.getScaledWidth();
			int height = scaledResolution.getScaledHeight();

			int mouseX = Mouse.getEventX() * width / Minecraft.getMinecraft().displayWidth;
			int mouseY = height - Mouse.getEventY() * height / Minecraft.getMinecraft().displayHeight - 1;

			int index = 0;
			for (ItemStack stack : Minecraft.getMinecraft().thePlayer.inventory.mainInventory) {
				if (stack == null) {
					index++;
					continue;
				}

				int x = 8 + 18 * (index % 9);
				int y = 104 + 18 * (index / 9);
				if (index < 9) y = 180;

				if (mouseX > guiLeft + x && mouseX < guiLeft + x + 16) {
					if (mouseY > guiTop + y && mouseY < guiTop + y + 16) {
						SlotLocking.getInstance().toggleLock(index);
						return true;
					}
				}

				index++;
			}
		}

		return Keyboard.getEventKey() != Keyboard.KEY_ESCAPE;
	}
}
