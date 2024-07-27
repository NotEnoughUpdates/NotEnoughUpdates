/*
 * Copyright (C) 2022-2024 NotEnoughUpdates contributors
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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.moulberry.notenoughupdates.NEUOverlay;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.auction.APIManager;
import io.github.moulberry.notenoughupdates.autosubscribe.NEUAutoSubscribe;
import io.github.moulberry.notenoughupdates.core.util.ArrowPagesUtils;
import io.github.moulberry.notenoughupdates.events.ButtonExclusionZoneEvent;
import io.github.moulberry.notenoughupdates.listener.RenderListener;
import io.github.moulberry.notenoughupdates.profileviewer.GuiProfileViewer;
import io.github.moulberry.notenoughupdates.profileviewer.PlayerStats;
import io.github.moulberry.notenoughupdates.util.Constants;
import io.github.moulberry.notenoughupdates.util.ItemUtils;
import io.github.moulberry.notenoughupdates.util.Rectangle;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static io.github.moulberry.notenoughupdates.util.GuiTextures.accessory_bag_overlay;

@NEUAutoSubscribe
public class AccessoryBagOverlay {
	private static final int TAB_BASIC = 0;
	private static final int TAB_TOTAL = 1;
	private static final int TAB_DUP = 2;
	private static final int TAB_MISSING = 3;

	public static final AccessoryBagOverlay INSTANCE = new AccessoryBagOverlay();

	// Arrow pages variables
	private static int dupePageActive = 0;
	private static int dupePagesTotal = 0;
	private static int missingPageActive = 0;
	private static int missingPagesTotal = 0;

	// Page-specific button variables
	private static boolean dupe_highlight = false;
	private static boolean dupe_showPersonal = false;
	private static boolean missing_showAllTiers = true;
	private static boolean missing_useMP = true;

	private static List<String> tooltipToDisplay = null;
	private static boolean offsetButtons = false;

	@SubscribeEvent
	public void onButtonExclusionZones(ButtonExclusionZoneEvent event) {
		if (isInAccessoryBag()) {
			event.blockArea(
				new Rectangle(
					event.getGuiBaseRect().getRight(),
					event.getGuiBaseRect().getTop(),
					168 /*pane*/ + (offsetButtons ? 24 : 0) /*tabs*/ + 5 /*space*/, 128
				),
				ButtonExclusionZoneEvent.PushDirection.TOWARDS_RIGHT
			);
		}
	}

	private static final ItemStack[] TAB_STACKS = new ItemStack[]{
		Utils.createItemStack(Items.dye, EnumChatFormatting.DARK_AQUA + "Basic Information",
			10, EnumChatFormatting.GREEN + "- Talis count by rarity"
		),
		Utils.createItemStack(Items.diamond_sword, EnumChatFormatting.DARK_AQUA + "Total Stat Bonuses",
			0
		),
		Utils.createItemStack(Items.dye, EnumChatFormatting.DARK_AQUA + "Duplicates",
			8
		),
		Utils.createItemStack(Item.getItemFromBlock(Blocks.barrier), EnumChatFormatting.DARK_AQUA + "Missing",
			0
		)
	};

	private static int currentTab = TAB_BASIC;

	public static boolean mouseClick() {
		if (Minecraft.getMinecraft().currentScreen instanceof GuiChest) {
			GuiChest eventGui = (GuiChest) Minecraft.getMinecraft().currentScreen;
			ContainerChest cc = (ContainerChest) eventGui.inventorySlots;
			String containerName = cc.getLowerChestInventory().getDisplayName().getUnformattedText();
			if (!containerName.trim().startsWith("Accessory Bag")) {
				return false;
			}
		} else {
			return false;
		}

		if (!Mouse.getEventButtonState()) return false;
		try {
			int xSize = (int) Utils.getField(
				GuiContainer.class,
				Minecraft.getMinecraft().currentScreen,
				"xSize",
				"field_146999_f"
			);
			int ySize = (int) Utils.getField(
				GuiContainer.class,
				Minecraft.getMinecraft().currentScreen,
				"ySize",
				"field_147000_g"
			);
			int guiLeft = (int) Utils.getField(
				GuiContainer.class,
				Minecraft.getMinecraft().currentScreen,
				"guiLeft",
				"field_147003_i"
			);
			int guiTop = (int) Utils.getField(
				GuiContainer.class,
				Minecraft.getMinecraft().currentScreen,
				"guiTop",
				"field_147009_r"
			);

			if (mouseX() < guiLeft + xSize + 3 || mouseX() > guiLeft + xSize + 168 + 28) return false;
			if (mouseY() < guiTop || mouseY() > guiTop + 128) return false;

			if (mouseX() > guiLeft + xSize + 168 + 3 && mouseY() < guiTop + 20 * TAB_MISSING + 22) {
				currentTab = (mouseY() - guiTop) / 20;
				if (currentTab < 0) currentTab = 0;
				if (currentTab > TAB_MISSING) currentTab = TAB_MISSING;
			}

			if (currentTab == TAB_DUP) {
				ArrowPagesUtils.onPageSwitchMouse(
					guiLeft + xSize + 3,
					guiTop,
					new int[]{60, 110},
					dupePageActive,
					dupePagesTotal,
					integer -> dupePageActive = integer
				);

				if (new Rectangle(guiLeft + xSize + 3 + 120, guiTop + 108, 16, 16).contains(mouseX(), mouseY())) {
					dupe_highlight = !dupe_highlight;
				}

				if (new Rectangle(guiLeft + xSize + 3 + 141, guiTop + 108, 16, 16).contains(mouseX(), mouseY())) {
					dupe_showPersonal = !dupe_showPersonal;
				}

			}
			if (currentTab == TAB_MISSING) {
				ArrowPagesUtils.onPageSwitchMouse(
					guiLeft + xSize + 3,
					guiTop,
					new int[]{60, 110},
					missingPageActive,
					missingPagesTotal,
					integer -> missingPageActive = integer
				);

				if (new Rectangle(guiLeft + xSize + 3 + 120, guiTop + 108, 16, 16).contains(mouseX(), mouseY())) {
					missing_useMP = !missing_useMP;
					missing = null;
				}
				if (new Rectangle(guiLeft + xSize + 3 + 141, guiTop + 108, 16, 16).contains(mouseX(), mouseY())) {
					missing_showAllTiers = !missing_showAllTiers;
					missing = null;
				}
			}

			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static void resetCache() {
		accessoryStacks = new HashSet<>();
		pagesVisited = new HashSet<>();
		talismanCountRarity = null;
		totalStats = null;
		duplicates = null;
		missing = null;
	}

	private static Set<ItemStack> accessoryStacks = new HashSet<>();
	private static Set<Integer> pagesVisited = new HashSet<>();

	public static void renderVisitOverlay(int x, int y) {
		Utils.drawStringCenteredScaledMaxWidth("Please visit all", x + 80, y + 60, true, 140, -1);
		Utils.drawStringCenteredScaledMaxWidth("pages of the bag", x + 80, y + 72, true, 140, -1);
	}

	private static TreeMap<Integer, Integer> talismanCountRarity = null;

	public static void renderBasicOverlay(int x, int y) {
		if (talismanCountRarity == null) {
			talismanCountRarity = new TreeMap<>();
			for (ItemStack stack : accessoryStacks) {
				int rarity = getRarity(stack);
				if (rarity >= 0) {
					talismanCountRarity.put(rarity, talismanCountRarity.getOrDefault(rarity, 0) + 1);
				}
			}
		}

		drawTitle(x, y, "Total Counts By Rarity");

		int yIndex = 0;
		for (Map.Entry<Integer, Integer> entry : talismanCountRarity.descendingMap().entrySet()) {
			String rarityName = Utils.rarityArrC[entry.getKey()];
			Utils.renderAlignedString(
				rarityName,
				EnumChatFormatting.WHITE.toString() + entry.getValue(),
				x + 34,
				y + 25 + 11 * yIndex,
				100
			);
			yIndex++;
		}
	}

	private static PlayerStats.Stats totalStats = null;

	public static void renderTotalStatsOverlay(int x, int y) {
		if (totalStats == null) {
			totalStats = new PlayerStats.Stats();
			for (ItemStack stack : accessoryStacks) {
				if (stack != null) totalStats.add(getStatForItem(stack, STAT_PATTERN_MAP, true));
			}
		}

		drawTitle(x, y, "Total Stats");
		int yIndex = 0;
		for (int i = 0; i < PlayerStats.defaultStatNames.length; i++) {
			String statName = PlayerStats.defaultStatNames[i];
			String statNamePretty = PlayerStats.defaultStatNamesPretty[i];

			int val = Math.round(totalStats.get(statName));

			if (Math.abs(val) < 1E-5) continue;

			GlStateManager.color(1, 1, 1, 1);
			GlStateManager.enableBlend();
			GL14.glBlendFuncSeparate(
				GL11.GL_SRC_ALPHA,
				GL11.GL_ONE_MINUS_SRC_ALPHA,
				GL11.GL_ONE,
				GL11.GL_ONE_MINUS_SRC_ALPHA
			);
			if (totalStats.size() < 10) {
				Utils.renderAlignedString(
					statNamePretty,
					EnumChatFormatting.WHITE.toString() + val,
					x + 34,
					y + 20 + 11 * yIndex, 100
				);
			} else {
				Utils.renderAlignedString(
					statNamePretty,
					EnumChatFormatting.WHITE.toString() + val,
					x + (yIndex < 9 ? 10 : 87),
					y + 20 + 11 * (yIndex < 9 ? yIndex : yIndex - 9), 75
				);
			}

			yIndex++;
		}
	}

	private static Set<ItemStack> duplicates = null;

	public static void renderDuplicatesOverlay(int x, int y) {
		if (duplicates == null) {
			JsonObject misc = Constants.MISC;
			if (misc == null) {
				drawTitle(x, y, "Duplicates: REPO ERROR");
				return;
			}
			JsonElement talisman_upgrades_element = misc.get("talisman_upgrades");
			if (talisman_upgrades_element == null) {
				drawTitle(x, y, "Duplicates: REPO ERROR");
				return;
			}
			JsonObject talisman_upgrades = talisman_upgrades_element.getAsJsonObject();

			duplicates = new HashSet<>();

			Set<String> prevInternalnames = new HashSet<>();
			for (ItemStack stack : accessoryStacks) {
				String internalname =
					NotEnoughUpdates.INSTANCE.manager.createItemResolutionQuery().withItemStack(stack).resolveInternalName();

				if (prevInternalnames.contains(internalname)) {
					duplicates.add(stack);
					continue;
				}
				prevInternalnames.add(internalname);

				if (talisman_upgrades.has(internalname)) {
					JsonArray upgrades = talisman_upgrades.get(internalname).getAsJsonArray();
					for (ItemStack stack2 : accessoryStacks) {
						if (stack != stack2) {
							String internalname2 = NotEnoughUpdates.INSTANCE.manager
								.createItemResolutionQuery()
								.withItemStack(stack2)
								.resolveInternalName();
							for (int j = 0; j < upgrades.size(); j++) {
								String upgrade = upgrades.get(j).getAsString();
								if (internalname2.equals(upgrade)) {
									duplicates.add(stack);
									break;
								}
							}
						}
					}
				}
			}
		}
		if (duplicates.isEmpty()) {
			drawTitle(x, y, "No Duplicates");
		} else {
			drawTitle(x, y, "Duplicates: " + duplicates.size());

			int yIndex = 0;
			List<ItemStack> sortedDupes =
				duplicates.stream().sorted((Comparator.comparing(ItemStack::getDisplayName))).collect(Collectors.toList());

			for (ItemStack duplicate : sortedDupes.subList(dupePageActive * 8, sortedDupes.size())) {
				String s = duplicate.getDisplayName();
				Utils.renderShadowedString(s.substring(0, Math.min(s.length(), 35)), x + 84, y + 20 + 11 * yIndex, 158);
				if (++yIndex >= 8 && sortedDupes.size() > 9) break;
			}

			if (sortedDupes.size() > 9) {
				dupePagesTotal = (int) Math.ceil(sortedDupes.size() / 8.0);
				GlStateManager.color(1f, 1f, 1f, 1f);
				ArrowPagesUtils.onDraw(x, y, new int[]{60, 110}, dupePageActive, dupePagesTotal);
			}
		}

		List<String> highlightTooltip = new ArrayList<>();
		if (dupe_highlight) {
			highlightTooltip.add("§aHighlight dupes");
			highlightTooltip.add("§7Will highlight accessories");
			highlightTooltip.add("§7you have duplicates of.");
		} else {
			highlightTooltip.add("§cDon't highlight dupes");
			highlightTooltip.add("§7Will not highlight accessories");
			highlightTooltip.add("§7you have duplicates of.");
		}
		renderButton(
			new ItemStack(dupe_highlight ? Items.ender_eye : Items.ender_pearl),
			x + 120,
			y + 107,
			highlightTooltip
		);

		List<String> compDeletorTooltip = new ArrayList<>();
		if (dupe_showPersonal) {
			compDeletorTooltip.add("§aHighlight Compactors & Deletors");
			compDeletorTooltip.add("§7Will highlight all duplicates.");
		} else {
			compDeletorTooltip.add("§cDon't highlight Compactors & Deletors");
			compDeletorTooltip.add("§7Duplicates allow you to specify");
			compDeletorTooltip.add("§7more things to compact and delete,");
			compDeletorTooltip.add("§7but they do not give more MP!");
		}
		renderButton(
			NotEnoughUpdates.INSTANCE.manager.createItem(dupe_showPersonal ? "PERSONAL_DELETOR_4000" : "DISPENSER"),
			x + 141,
			y + 107,
			compDeletorTooltip
		);

	}

	private static List<ItemStack> missing = null;

	public static void renderMissingOverlay(int x, int y) {
		if (missing == null) {
			JsonObject misc = Constants.MISC;
			if (misc == null) {
				drawTitle(x, y, "Missing: REPO ERROR");
				return;
			}
			JsonElement talisman_upgrades_element = misc.get("talisman_upgrades");
			if (talisman_upgrades_element == null) {
				drawTitle(x, y, "Missing: REPO ERROR");
				return;
			}
			JsonObject talisman_upgrades = talisman_upgrades_element.getAsJsonObject();

			missing = new ArrayList<>();

			List<String> missingInternal = new ArrayList<>();

			List<String> ignoredTalisman = new ArrayList<>();
			if (misc.has("ignored_talisman")) {
				for (JsonElement jsonElement : misc.getAsJsonArray("ignored_talisman")) {
					ignoredTalisman.add(jsonElement.getAsString());
				}
			}

			for (Map.Entry<String, JsonObject> entry : NotEnoughUpdates.INSTANCE.manager.getItemInformation().entrySet()) {
				if (ignoredTalisman.contains(entry.getValue().get("internalname").getAsString())) continue;
				if (entry.getValue().has("lore")) {
					if (checkItemType(
						entry.getValue().get("lore").getAsJsonArray(),
						"ACCESSORY",
						"HATCESSORY",
						"DUNGEON ACCESSORY"
					) >= 0) {
						missingInternal.add(entry.getKey());
					}
				}
			}

			for (ItemStack stack : accessoryStacks) {
				String internalname =
					NotEnoughUpdates.INSTANCE.manager.createItemResolutionQuery().withItemStack(stack).resolveInternalName();
				missingInternal.remove(internalname);

				for (Map.Entry<String, JsonElement> talisman_upgrade_element : talisman_upgrades.entrySet()) {
					JsonArray upgrades = talisman_upgrade_element.getValue().getAsJsonArray();
					for (int j = 0; j < upgrades.size(); j++) {
						String upgrade = upgrades.get(j).getAsString();
						if (internalname.equals(upgrade)) {
							missingInternal.remove(talisman_upgrade_element.getKey());
							break;
						}
					}
				}
			}
			missingInternal.sort(getItemComparator(missing_useMP));

			Set<String> missingDisplayNames = new HashSet<>();
			for (String internal : missingInternal) {
				boolean hasDup = false;

				if (talisman_upgrades.has(internal)) {
					JsonArray upgrades = talisman_upgrades.get(internal).getAsJsonArray();
					for (int j = 0; j < upgrades.size(); j++) {
						String upgrade = upgrades.get(j).getAsString();
						if (missingInternal.contains(upgrade)) {
							hasDup = true;
							break;
						}
					}
				}

				ItemStack stack = NotEnoughUpdates.INSTANCE.manager.jsonToStack(NotEnoughUpdates.INSTANCE.manager
					.getItemInformation()
					.get(internal), false);

				if (missingDisplayNames.contains(stack.getDisplayName())) continue;
				missingDisplayNames.add(stack.getDisplayName());

				if (hasDup) {
					if (!missing_showAllTiers) continue;
					stack.setStackDisplayName(stack.getDisplayName() + "*");
				}
				missing.add(stack);
			}
		}
		if (missing.isEmpty()) {
			drawTitle(x, y, "No Missing");
		} else {
			drawTitle(x, y, "Missing: " + missing.size());

			int yIndex = 0;
			for (ItemStack missingStack : missing.subList(missingPageActive * 8, missing.size())) {
				String s = missingStack.getDisplayName();
				String internal = NotEnoughUpdates.INSTANCE.manager
					.createItemResolutionQuery()
					.withItemStack(missingStack)
					.resolveInternalName();
				if (internal.equals("RIFT_PRISM") && hasConsuedRiftPrism()) continue;
				double price = getItemPrice(internal);
				Utils.renderAlignedString(
					s,
					price != -1
						? "§6" + Utils.shortNumberFormat(price, 0)
						: "§c" + "NO DATA",
					x + 5,
					y + 20 + 11 * yIndex,
					158
				);
				Rectangle rect = new Rectangle(x, y + 20 + 11 * yIndex, 168, 11);
				renderAccessoryHover(rect, missingStack);
				if (++yIndex >= 8 && missing.size() > 9) break;
			}

			if (missing.size() > 9) {
				missingPagesTotal = (int) Math.ceil(missing.size() / 8.0);
				GlStateManager.color(1f, 1f, 1f, 1f);
				ArrowPagesUtils.onDraw(x, y, new int[]{60, 110}, missingPageActive, missingPagesTotal);
			}

			List<String> mpTooltip = new ArrayList<>();
			if (missing_useMP) {
				mpTooltip.add("§bSort by Magical Power");
				mpTooltip.add("§7Will sort the accessories");
				mpTooltip.add("§7by the best MP gain.");
			} else {
				mpTooltip.add("§6Sort by Coins");
				mpTooltip.add("§7Will sort the accessories");
				mpTooltip.add("§7by the cheapest options.");
			}
			renderButton(ItemUtils.getCoinItemStack(missing_useMP ? 10_000_000 : 100_000), x + 120, y + 107, mpTooltip);

			List<String> tieredTooltip = new ArrayList<>();
			if (missing_showAllTiers) {
				tieredTooltip.add("§aShow all tiers");
				tieredTooltip.add("§7Will show all the tiers");
				tieredTooltip.add("§7to get the cheapest options.");
			} else {
				tieredTooltip.add("§6Show highest tier");
				tieredTooltip.add("§7Will show only the highest tier");
				tieredTooltip.add("§7to avoid wasting money on lower ones.");
			}
			renderButton(new ItemStack(missing_showAllTiers ? Items.coal : Items.diamond), x + 141, y + 107, tieredTooltip);
		}
	}

	private static void drawTitle(int x, int y, String abc) {
		Utils.drawStringCenteredScaledMaxWidth(abc, x + 84, y + 12, false, 158, gray());
	}

	private static int gray() {
		return new Color(80, 80, 80).getRGB();
	}

	private static Comparator<String> getItemComparator(boolean accountMP) {
		return (o1, o2) -> {
			double cost1 = getItemPrice(o1);
			double cost2 = getItemPrice(o2);
			if (accountMP) {
				cost1 /= cost1 != -1 ? getMagicalPowerForItem(o1) : -1E-99; // Artificially push items with -1 price to the end
				cost2 /= cost2 != -1 ? getMagicalPowerForItem(o2) : -1E-99; // since they would be put at the start otherwise
			}

			if (cost1 == -1 && cost2 == -1) return o1.compareTo(o2);
			if (cost1 == -1) return 1;
			if (cost2 == -1) return -1;

			if (cost1 < cost2) return -1;
			if (cost1 > cost2) return 1;

			return o1.compareTo(o2);
		};
	}

	private static boolean inAccessoryBag = false;

	public static boolean isInAccessoryBag() {
		return inAccessoryBag && NotEnoughUpdates.INSTANCE.config.accessoryBag.enableOverlay;
	}

	public static void renderOverlay() {
		inAccessoryBag = false;
		offsetButtons = false;
		if (Minecraft.getMinecraft().currentScreen instanceof GuiChest && RenderListener.inventoryLoaded) {
			GuiChest eventGui = (GuiChest) Minecraft.getMinecraft().currentScreen;
			ContainerChest cc = (ContainerChest) eventGui.inventorySlots;
			String containerName = cc.getLowerChestInventory().getDisplayName().getUnformattedText();
			if (containerName.trim().startsWith("Accessory Bag") && !containerName.contains("Thaumaturgy") &&
				!containerName.contains("Upgrades")) {
				inAccessoryBag = true;
				try {
					int xSize = (int) Utils.getField(GuiContainer.class, eventGui, "xSize", "field_146999_f");
					int ySize = (int) Utils.getField(GuiContainer.class, eventGui, "ySize", "field_147000_g");
					int guiLeft = (int) Utils.getField(GuiContainer.class, eventGui, "guiLeft", "field_147003_i");
					int guiTop = (int) Utils.getField(GuiContainer.class, eventGui, "guiTop", "field_147009_r");

					if (accessoryStacks.isEmpty()) {
						for (ItemStack stack : Minecraft.getMinecraft().thePlayer.inventory.mainInventory) {
							if (stack != null && isAccessory(stack)) {
								accessoryStacks.add(stack);
							}
						}
					}

					if (containerName.trim().contains("(")) {
						String first = containerName.trim().split("\\(")[1].split("/")[0];
						Integer currentPageNumber = Integer.parseInt(first);
						//System.out.println("current:"+currentPageNumber);
						if (!pagesVisited.contains(currentPageNumber)) {
							boolean hasStack = false;
							if (Minecraft.getMinecraft().thePlayer.openContainer instanceof ContainerChest) {
								IInventory inv =
									((ContainerChest) Minecraft.getMinecraft().thePlayer.openContainer).getLowerChestInventory();
								for (int i = 0; i < inv.getSizeInventory(); i++) {
									ItemStack stack = inv.getStackInSlot(i);
									if (stack != null) {
										hasStack = true;
										if (isAccessory(stack)) {
											accessoryStacks.add(stack);
										}
									}
								}
							}

							if (hasStack) pagesVisited.add(currentPageNumber);
						}

						String second = containerName.trim().split("/")[1].split("\\)")[0];
						//System.out.println(second + ":" + pagesVisited.size());
						if (Integer.parseInt(second) > pagesVisited.size()) {
							GlStateManager.color(1, 1, 1, 1);
							Minecraft.getMinecraft().getTextureManager().bindTexture(accessory_bag_overlay);
							GlStateManager.disableLighting();
							Utils.drawTexturedRect(guiLeft + xSize + 4, guiTop, 168, 128, 0, 168 / 196f, 0, 1f, GL11.GL_NEAREST);

							renderVisitOverlay(guiLeft + xSize + 3, guiTop);
							return;
						}
					} else if (pagesVisited.isEmpty()) {
						boolean hasStack = false;
						if (Minecraft.getMinecraft().thePlayer.openContainer instanceof ContainerChest) {
							IInventory inv =
								((ContainerChest) Minecraft.getMinecraft().thePlayer.openContainer).getLowerChestInventory();
							for (int i = 0; i < inv.getSizeInventory(); i++) {
								ItemStack stack = inv.getStackInSlot(i);
								if (stack != null) {
									hasStack = true;
									if (isAccessory(stack)) {
										accessoryStacks.add(stack);
									}
								}
							}
						}

						if (hasStack) pagesVisited.add(1);
					}

					GlStateManager.disableLighting();
					offsetButtons = true;

					for (int i = 0; i <= TAB_MISSING; i++) {
						if (i != currentTab) {
							GlStateManager.color(1, 1, 1, 1);
							Minecraft.getMinecraft().getTextureManager().bindTexture(accessory_bag_overlay);
							Utils.drawTexturedRect(guiLeft + xSize + 168, guiTop + 20 * i, 25, 22,
								168 / 196f, 193f / 196f, 0f, 22 / 128f, GL11.GL_NEAREST
							);
							RenderHelper.enableGUIStandardItemLighting();
							Utils.drawItemStack(TAB_STACKS[i], guiLeft + xSize + 168 + 5, guiTop + 20 * i + 3);
						}
					}

					GlStateManager.color(1, 1, 1, 1);
					Minecraft.getMinecraft().getTextureManager().bindTexture(accessory_bag_overlay);
					Utils.drawTexturedRect(guiLeft + xSize + 4, guiTop, 168, 128, 0, 168 / 196f, 0, 1f, GL11.GL_NEAREST);

					if (pagesVisited.isEmpty()) {
						renderVisitOverlay(guiLeft + xSize + 3, guiTop);
						return;
					}

					Minecraft.getMinecraft().getTextureManager().bindTexture(accessory_bag_overlay);
					Utils.drawTexturedRect(guiLeft + xSize + 168, guiTop + 20 * currentTab, 28, 22,
						168 / 196f, 1f, 22 / 128f, 44 / 128f, GL11.GL_NEAREST
					);
					RenderHelper.enableGUIStandardItemLighting();
					Utils.drawItemStack(TAB_STACKS[currentTab], guiLeft + xSize + 168 + 8, guiTop + 20 * currentTab + 3);

					if (dupe_highlight) highlightDuplicates(guiLeft + xSize + 3, guiTop);

					switch (currentTab) {
						case TAB_BASIC:
							renderBasicOverlay(guiLeft + xSize + 3, guiTop);
							return;
						case TAB_TOTAL:
							renderTotalStatsOverlay(guiLeft + xSize + 3, guiTop);
							return;
						case TAB_DUP:
							renderDuplicatesOverlay(guiLeft + xSize + 3, guiTop);
							return;
						case TAB_MISSING:
							renderMissingOverlay(guiLeft + xSize + 3, guiTop);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (tooltipToDisplay != null) {
					Utils.drawHoveringText(tooltipToDisplay, mouseX(), mouseY(), eventGui.width, eventGui.height, -1);
					tooltipToDisplay = null;
				}
			}
		}
	}

	private static final HashMap<String, Pattern> STAT_PATTERN_MAP = new HashMap<String, Pattern>() {{
		String STAT_PATTERN_END = ": ((?:\\+|-)([0-9]+(\\.[0-9]+)?))%?";
		put("health", Pattern.compile("^Health" + STAT_PATTERN_END));
		put("defence", Pattern.compile("^Defense" + STAT_PATTERN_END));
		put("strength", Pattern.compile("^Strength" + STAT_PATTERN_END));
		put("speed", Pattern.compile("^Speed" + STAT_PATTERN_END));
		put("crit_chance", Pattern.compile("^Crit Chance" + STAT_PATTERN_END));
		put("crit_damage", Pattern.compile("^Crit Damage" + STAT_PATTERN_END));
		put("bonus_attack_speed", Pattern.compile("^Bonus Attack Speed" + STAT_PATTERN_END));
		put("intelligence", Pattern.compile("^Intelligence" + STAT_PATTERN_END));
		put("sea_creature_chance", Pattern.compile("^Sea Creature Chance" + STAT_PATTERN_END));
		put("magic_find", Pattern.compile("^Magic Find" + STAT_PATTERN_END));
		put("pet_luck", Pattern.compile("^Pet Luck" + STAT_PATTERN_END));
		put("ferocity", Pattern.compile("^Ferocity" + STAT_PATTERN_END));
		put("ability_damage", Pattern.compile("^Ability Damage" + STAT_PATTERN_END));
		put("mining_fortune", Pattern.compile("^Mining Fortune" + STAT_PATTERN_END));
		put("mining_speed", Pattern.compile("^Mining Speed" + STAT_PATTERN_END));
	}};

	private static PlayerStats.Stats getStatForItem(
		ItemStack stack,
		HashMap<String, Pattern> patternMap,
		boolean addExtras
	) {
		String internalname =
			NotEnoughUpdates.INSTANCE.manager.createItemResolutionQuery().withItemStack(stack).resolveInternalName();
		NBTTagCompound tag = stack.getTagCompound();
		PlayerStats.Stats stats = new PlayerStats.Stats();

		if (internalname == null) {
			return stats;
		}

		if (tag != null) {
			NBTTagCompound display = tag.getCompoundTag("display");
			if (display.hasKey("Lore", 9)) {
				NBTTagList list = display.getTagList("Lore", 8);
				for (int i = 0; i < list.tagCount(); i++) {
					String line = list.getStringTagAt(i);
					for (Map.Entry<String, Pattern> entry : patternMap.entrySet()) {
						Matcher matcher = entry.getValue().matcher(Utils.cleanColour(line));
						if (matcher.find()) {
							float bonus = Float.parseFloat(matcher.group(1));
							stats.addStat(entry.getKey(), bonus);
						}
					}
				}
			}
		}

		if (!addExtras) return stats;

		if (internalname.equals("DAY_CRYSTAL") || internalname.equals("NIGHT_CRYSTAL")) {
			stats.addStat(PlayerStats.STRENGTH, 2.5f);
			stats.addStat(PlayerStats.DEFENCE, 2.5f);
		}

		if (internalname.equals("NEW_YEAR_CAKE_BAG") && tag != null && tag.hasKey("ExtraAttributes", 10)) {
			NBTTagCompound ea = tag.getCompoundTag("ExtraAttributes");

			byte[] bytes = null;
			for (String key : ea.getKeySet()) {
				if (key.endsWith("backpack_data") || key.equals("new_year_cake_bag_data")) {
					bytes = ea.getByteArray(key);
					try {
						NBTTagCompound contents_nbt = CompressedStreamTools.readCompressed(new ByteArrayInputStream(bytes));
						NBTTagList items = contents_nbt.getTagList("i", 10);
						HashSet<Integer> cakes = new HashSet<>();
						for (int j = 0; j < items.tagCount(); j++) {
							if (!items.getCompoundTagAt(j).getKeySet().isEmpty()) {
								NBTTagCompound nbt = items.getCompoundTagAt(j).getCompoundTag("tag");
								if (nbt != null && nbt.hasKey("ExtraAttributes", 10)) {
									NBTTagCompound ea2 = nbt.getCompoundTag("ExtraAttributes");
									if (ea2.hasKey("new_years_cake")) {
										cakes.add(ea2.getInteger("new_years_cake"));
									}
								}
							}
						}
						stats.addStat(PlayerStats.HEALTH, cakes.size());
					} catch (IOException e) {
						e.printStackTrace();
						return stats;
					}
					break;
				}
			}
		}
		return stats;
	}

	public static int checkItemType(ItemStack stack, boolean contains, String... typeMatches) {
		NBTTagCompound tag = stack.getTagCompound();
		if (tag != null) {
			NBTTagCompound display = tag.getCompoundTag("display");
			if (display.hasKey("Lore", 9)) {
				NBTTagList list = display.getTagList("Lore", 8);
				for (int i = list.tagCount() - 1; i >= 0; i--) {
					String line = list.getStringTagAt(i);
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
			}
		}
		return -1;
	}

	private static int checkItemType(JsonArray lore, String... typeMatches) {
		for (int i = lore.size() - 1; i >= 0; i--) {
			String line = lore.get(i).getAsString();

			for (String rarity : Utils.rarityArr) {
				for (int j = 0; j < typeMatches.length; j++) {
					if (line.trim().endsWith(rarity + " " + typeMatches[j])) {
						return j;
					}
				}
			}
		}
		return -1;
	}

	public static boolean isAccessory(ItemStack stack) {
		return checkItemType(stack, true, "ACCESSORY", "HATCESSORY") >= 0;
	}

	public static int getRarity(ItemStack stack) {
		NBTTagCompound tag = stack.getTagCompound();
		if (tag != null) {
			NBTTagCompound display = tag.getCompoundTag("display");
			if (display.hasKey("Lore", 9)) {
				NBTTagList list = display.getTagList("Lore", 8);
				for (int i = list.tagCount(); i >= 0; i--) {
					String line = list.getStringTagAt(i);
					for (int j = 0; j < Utils.rarityArrC.length; j++) {
						if (line.contains(Utils.rarityArrC[j])) {
							return j;
						}
					}
				}
			}
		}
		return -1;
	}

	public static double getItemPrice(String internal) {
		APIManager.CraftInfo info = NotEnoughUpdates.INSTANCE.manager.auctionManager.getCraftCost(internal);
		double bin = NotEnoughUpdates.INSTANCE.manager.auctionManager.getLowestBin(internal);
		if (info == null) return bin;
		if (bin == -1) return info.craftCost;
		return Math.min(info.craftCost, bin);
	}

	public static ScaledResolution getScaledResolution() {
		return new ScaledResolution(Minecraft.getMinecraft());
	}

	public static int mouseX() {
		return Mouse.getX() / getScaledResolution().getScaleFactor();
	}

	public static int mouseY() {
		return getScaledResolution().getScaledHeight() - Mouse.getY() / getScaledResolution().getScaleFactor();
	}

	public static int getMagicalPowerForItem(String internal) {
		int abi = 0;
		JsonObject jsonStack = NotEnoughUpdates.INSTANCE.manager.getJsonForItem(
			NotEnoughUpdates.INSTANCE.manager.
				createItemResolutionQuery().
				withKnownInternalName(internal).
				resolveToItemStack());
		int rarity = Utils.getRarityFromLore(jsonStack.get("lore").getAsJsonArray());

		if (internal.equals("HEGEMONY_ARTIFACT")) {
			switch (rarity) {
				case 4:
					return 16;
				case 5:
					return 22;
			}
		}

		if (internal.contains("ABICASE")) {
			try {
				JsonObject profileInfo = GuiProfileViewer.getSelectedProfile().getProfileJson();
				if (profileInfo.has("nether_island_player_data")) {
					JsonObject data = profileInfo.get("nether_island_player_data").getAsJsonObject();
					if (data.has("abiphone") && data.get("abiphone").getAsJsonObject().has("active_contacts")) { // BatChest
						int contact = data.get("abiphone").getAsJsonObject().get("active_contacts").getAsJsonArray().size();
						abi = contact / 2;
					}
				}
			} catch (NullPointerException e) {
			}
		}

		switch (rarity) {
			case 0:
			case 6:
				return abi + 3;
			case 1:
			case 7:
				return abi + 5;
			case 2:
				return abi + 8;
			case 3:
				return abi + 12;
			case 4:
				return abi + 16;
			case 5:
				return abi + 22;
		}

		return 0;
	}

	public static void renderButton(ItemStack stack, int x, int y, List<String> tooltip) {
		GlStateManager.color(1, 1, 1, 1);
		Minecraft.getMinecraft().getTextureManager().bindTexture(accessory_bag_overlay);
		GlStateManager.disableLighting();
		Utils.drawTexturedRect(x, y, 17, 17, 168f / 196f, 184f / 196f, 112f / 128f, 1f, GL11.GL_NEAREST); // slot
		RenderHelper.enableGUIStandardItemLighting();
		Minecraft.getMinecraft().getRenderItem().renderItemIntoGUI(stack, x, y); // item
		if (new Rectangle(x, y, 16, 16).contains(mouseX(), mouseY())) {
			Utils.drawHoveringText(
				tooltip,
				mouseX(), mouseY(),
				getScaledResolution().getScaledWidth(),
				getScaledResolution().getScaledHeight(),
				-1
			);
		}
	}

	public static void highlightDuplicates(int x, int y) {
		int guiLeft = (int) Utils.getField(
			GuiContainer.class,
			Minecraft.getMinecraft().currentScreen,
			"guiLeft",
			"field_147003_i"
		);
		int guiTop = (int) Utils.getField(
			GuiContainer.class,
			Minecraft.getMinecraft().currentScreen,
			"guiTop",
			"field_147009_r"
		);

		for (Slot slot : Minecraft.getMinecraft().thePlayer.openContainer.inventorySlots) {
			ItemStack stack = slot.getStack();
			if (stack != null && isAccessory(stack)) {
				if (NotEnoughUpdates.INSTANCE.manager
					.createItemResolutionQuery()
					.withItemStack(stack)
					.resolveInternalName()
					.matches("PERSONAL_(DELETOR|COMPACTOR)_[0-9]+") && !dupe_showPersonal) continue;
				if (duplicates != null && duplicates
					.stream()
					.map(ItemStack::getDisplayName)
					.collect(Collectors.toList())
					.contains(stack.getDisplayName())) {
					GlStateManager.translate(0, 0, 50);
					GuiScreen.drawRect(
						guiLeft + slot.xDisplayPosition,
						guiTop + slot.yDisplayPosition,
						guiLeft + slot.xDisplayPosition + 16,
						guiTop + slot.yDisplayPosition + 16,
						0xBBFF0000
					);
					GlStateManager.translate(0, 0, -50);
				}
			}
		}
	}

	public static void renderAccessoryHover(Rectangle rect, ItemStack stack) {
		if (rect.contains(mouseX(), mouseY())) {
			String internal = NotEnoughUpdates.INSTANCE.manager
				.createItemResolutionQuery()
				.withItemStack(stack)
				.resolveInternalName();
			tooltipToDisplay = Arrays.asList(
				stack.getDisplayName().replace("*", ""),
				"",
				"§eClick to learn more!",
				"§eCtrl+Click to search on ah!"
			);
			handleAccessoryClick(stack, internal);
		}
	}

	public static void handleAccessoryClick(ItemStack stack, String internal) {
		if (Mouse.isButtonDown(0) && internal != null) {
			if (!Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
				if (!NotEnoughUpdates.INSTANCE.manager.displayGuiItemRecipe(internal)) {
					NEUOverlay.getTextField().setText("id:" + internal);
					NotEnoughUpdates.INSTANCE.overlay.updateSearch();
					NotEnoughUpdates.INSTANCE.overlay.setSearchBarFocus(true);
				}
			} else {
				String displayname = Utils.cleanColour(stack.getDisplayName());
				NotEnoughUpdates.INSTANCE.trySendCommand("/ahs " + displayname.replace("*", ""));
			}
		}
	}

	public static boolean hasConsuedRiftPrism() {
		try {
			JsonObject profileInfo = GuiProfileViewer.getSelectedProfile().getProfileJson();
			if (profileInfo.has("rift") && profileInfo.getAsJsonObject("rift").has("access") && profileInfo.getAsJsonObject(
				"rift").getAsJsonObject("access").has("consumed_prism")) {
				return true;
			}
			return false;
		} catch (NullPointerException e) {
			return false;
		}
	}
}
