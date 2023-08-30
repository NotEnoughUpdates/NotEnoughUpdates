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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.auction.APIManager;
import io.github.moulberry.notenoughupdates.core.util.StringUtils;
import io.github.moulberry.notenoughupdates.events.ButtonExclusionZoneEvent;
import io.github.moulberry.notenoughupdates.listener.RenderListener;
import io.github.moulberry.notenoughupdates.profileviewer.PlayerStats;
import io.github.moulberry.notenoughupdates.util.Constants;
import io.github.moulberry.notenoughupdates.util.Rectangle;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.github.moulberry.notenoughupdates.util.GuiTextures.accessory_bag_overlay;

public class AccessoryBagOverlay {
	private static final int TAB_BASIC = 0;
	private static final int TAB_TOTAL = 1;
	private static final int TAB_BONUS = 2;
	private static final int TAB_DUP = 3;
	private static final int TAB_MISSING = 4;
	private static final int TAB_OPTIMIZER = 5;

	public static final AccessoryBagOverlay INSTANCE = new AccessoryBagOverlay();

	@SubscribeEvent
	public void onButtonExclusionZones(ButtonExclusionZoneEvent event) {
		if (isInAccessoryBag()) {
			event.blockArea(
				new Rectangle(
					event.getGuiBaseRect().getRight(),
					event.getGuiBaseRect().getTop(),
					80 /*pane*/ + 24 /*tabs*/ + 4 /*space*/, 150
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
		Utils.createItemStack(
			Item.getItemFromBlock(Blocks.anvil),
			EnumChatFormatting.DARK_AQUA + "Total Stat Bonuses (from reforges)",
			0
		),
		Utils.createItemStack(Items.dye, EnumChatFormatting.DARK_AQUA + "Duplicates",
			8
		),
		Utils.createItemStack(Item.getItemFromBlock(Blocks.barrier), EnumChatFormatting.DARK_AQUA + "Missing",
			0
		),
		Utils.createItemStack(Item.getItemFromBlock(Blocks.redstone_block), EnumChatFormatting.DARK_AQUA + "Optimizer",
			0
		),
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
			ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());

			int width = scaledResolution.getScaledWidth();
			int height = scaledResolution.getScaledHeight();

			int mouseX = Mouse.getX() / scaledResolution.getScaleFactor();
			int mouseY = height - Mouse.getY() / scaledResolution.getScaleFactor();

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

			if (mouseX < guiLeft + xSize + 3 || mouseX > guiLeft + xSize + 80 + 28) return false;
			if (mouseY < guiTop || mouseY > guiTop + 166) return false;

			if (mouseX > guiLeft + xSize + 83 && mouseY < guiTop + 20 * TAB_MISSING + 22) {
				currentTab = (mouseY - guiTop) / 20;
				if (currentTab < 0) currentTab = 0;
				if (currentTab > TAB_MISSING) currentTab = TAB_MISSING;
			}

			if (currentTab == TAB_OPTIMIZER) {
				int x = guiLeft + xSize + 3;
				int y = guiTop;

				if (mouseY > y + 92 && mouseY < y + 103) {
					if (mouseX > x + 5 && mouseX < x + 75) {
						mainWeapon = (int) Math.floor((mouseX - x - 5) / 70f * 9);
						if (mainWeapon < 1) {
							mainWeapon = 1;
						} else if (mainWeapon > 9) {
							mainWeapon = 9;
						}
					}
				}

				if (mouseX > x + 5 && mouseX < x + 35 || mouseX > x + 45 && mouseX < x + 75) {
					boolean set = mouseX > x + 5 && mouseX < x + 35;

					if (mouseY > y + 32 && mouseY < y + 43) {
						forceCC = set;
					} else if (mouseY > y + 52 && mouseY < y + 63) {
						forceAS = set;
					} else if (mouseY > y + 72 && mouseY < y + 83) {
						useGodPot = set;
					} else if (mouseY > y + 92 && mouseY < y + 103) {
						allowShaded = set;
					}
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
		reforgeStats = null;
		duplicates = null;
		missing = null;
	}

	private static Set<ItemStack> accessoryStacks = new HashSet<>();
	private static Set<Integer> pagesVisited = new HashSet<>();

	public static void renderVisitOverlay(int x, int y) {
		Utils.drawStringCenteredScaledMaxWidth("Please visit all", x + 40, y + 78, true, 70, -1);
		Utils.drawStringCenteredScaledMaxWidth("pages of the bag", x + 40, y + 86, true, 70, -1);
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

		drawString(x, y, "# By Rarity");

		int yIndex = 0;
		for (Map.Entry<Integer, Integer> entry : talismanCountRarity.descendingMap().entrySet()) {
			String rarityName = Utils.rarityArrC[entry.getKey()];
			Utils.renderAlignedString(
				rarityName,
				EnumChatFormatting.WHITE.toString() + entry.getValue(),
				x + 5,
				y + 20 + 11 * yIndex,
				70
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

		drawString(x, y, "Total Stats");
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
			Utils.renderAlignedString(
				statNamePretty,
				EnumChatFormatting.WHITE.toString() + val,
				x + 5,
				y + 20 + 11 * yIndex,
				70
			);

			yIndex++;
		}
	}

	private static PlayerStats.Stats reforgeStats = null;

	public static void renderReforgeStatsOverlay(int x, int y) {
		if (reforgeStats == null) {
			reforgeStats = new PlayerStats.Stats();
			for (ItemStack stack : accessoryStacks) {
				if (stack != null) reforgeStats.add(getStatForItem(stack, STAT_PATTERN_MAP_BONUS, false));
			}
		}

		drawString(x, y, "Reforge Stats");
		int yIndex = 0;
		for (int i = 0; i < PlayerStats.defaultStatNames.length; i++) {
			String statName = PlayerStats.defaultStatNames[i];
			String statNamePretty = PlayerStats.defaultStatNamesPretty[i];

			int val = Math.round(reforgeStats.get(statName));

			if (Math.abs(val) < 1E-5) continue;

			GlStateManager.color(1, 1, 1, 1);
			GlStateManager.enableBlend();
			GL14.glBlendFuncSeparate(
				GL11.GL_SRC_ALPHA,
				GL11.GL_ONE_MINUS_SRC_ALPHA,
				GL11.GL_ONE,
				GL11.GL_ONE_MINUS_SRC_ALPHA
			);
			Utils.renderAlignedString(
				statNamePretty,
				EnumChatFormatting.WHITE.toString() + val,
				x + 5,
				y + 20 + 11 * yIndex,
				70
			);

			yIndex++;
		}
	}

	private static Set<ItemStack> duplicates = null;

	public static void renderDuplicatesOverlay(int x, int y) {
		if (duplicates == null) {
			JsonObject misc = Constants.MISC;
			if (misc == null) {
				drawString(x, y, "Duplicates: ERROR");
				return;
			}
			JsonElement talisman_upgrades_element = misc.get("talisman_upgrades");
			if (talisman_upgrades_element == null) {
				drawString(x, y, "Duplicates: ERROR");
				return;
			}
			JsonObject talisman_upgrades = talisman_upgrades_element.getAsJsonObject();

			duplicates = new HashSet<>();

			Set<String> prevInternalnames = new HashSet<>();
			for (ItemStack stack : accessoryStacks) {
				String internalname = NotEnoughUpdates.INSTANCE.manager.getInternalNameForItem(stack);

				if (prevInternalnames.contains(internalname)) {
					duplicates.add(stack);
					continue;
				}
				prevInternalnames.add(internalname);

				if (talisman_upgrades.has(internalname)) {
					JsonArray upgrades = talisman_upgrades.get(internalname).getAsJsonArray();
					for (ItemStack stack2 : accessoryStacks) {
						if (stack != stack2) {
							String internalname2 = NotEnoughUpdates.INSTANCE.manager.getInternalNameForItem(stack2);
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
			drawString(x, y, "No Duplicates");
		} else {
			drawString(x, y, "Duplicates: " + duplicates.size());

			int yIndex = 0;
			for (ItemStack duplicate : duplicates) {
				String s = duplicate.getDisplayName();
				Utils.renderShadowedString(s, x + 40, y + 20 + 11 * yIndex, 70);
				if (duplicates.size() > 11) {
					if (++yIndex >= 10) break;
				} else {
					if (++yIndex >= 11) break;
				}
			}

			if (duplicates.size() > 11) {
				Utils.drawStringCenteredScaledMaxWidth(
					"+" + (duplicates.size() - 10) + " More",
					x + 40, y + 16 + 121,
					false,
					70,
					gray()
				);
			}
		}
	}

	private static List<ItemStack> missing = null;

	public static void renderMissingOverlay(int x, int y) {
		if (missing == null) {
			JsonObject misc = Constants.MISC;
			if (misc == null) {
				drawString(x, y, "Duplicates: ERROR");
				return;
			}
			JsonElement talisman_upgrades_element = misc.get("talisman_upgrades");
			if (talisman_upgrades_element == null) {
				drawString(x, y, "Duplicates: ERROR");
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
						"HATCCESSORY",
						"DUNGEON ACCESSORY"
					) >= 0) {
						missingInternal.add(entry.getKey());
					}
				}
			}

			for (ItemStack stack : accessoryStacks) {
				String internalname = NotEnoughUpdates.INSTANCE.manager.getInternalNameForItem(stack);
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

			missingInternal.sort(getItemComparator());

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
					stack.setStackDisplayName(stack.getDisplayName() + "*");
				}
				missing.add(stack);
			}
		}
		if (missing.isEmpty()) {
			drawString(x, y, "No Missing");
		} else {
			drawString(x, y, "Missing: " + missing.size());

			int yIndex = 0;
			long currentTime = System.currentTimeMillis();
			for (ItemStack missingStack : missing) {
				String s = missingStack.getDisplayName();

				s = Minecraft.getMinecraft().fontRendererObj.trimStringToWidth(s, 70);

				String clean = StringUtils.cleanColourNotModifiers(s);
				for (int xO = -1; xO <= 1; xO++) {
					for (int yO = -1; yO <= 1; yO++) {
						int col = 0xff202020;
						//if(xO != 0 && yO != 0) col = 0xff252525;
						Minecraft.getMinecraft().fontRendererObj.drawString(
							clean,
							x + 5 + xO,
							y + 20 + 11 * yIndex + yO,
							col,
							false
						);
					}
				}
				Minecraft.getMinecraft().fontRendererObj.drawString(s, x + 5, y + 20 + 11 * yIndex, 0xffffff, false);
				if (missing.size() > 11) {
					if (++yIndex >= 10) break;
				} else {
					if (++yIndex >= 11) break;
				}
			}

			if (missing.size() > 11) {
				Utils.drawStringCenteredScaledMaxWidth("Show All", x + 40, y + 16 + 121, false, 70, gray());

				final ScaledResolution scaledresolution = new ScaledResolution(Minecraft.getMinecraft());
				final int scaledWidth = scaledresolution.getScaledWidth();
				final int scaledHeight = scaledresolution.getScaledHeight();
				int mouseX = Mouse.getX() * scaledWidth / Minecraft.getMinecraft().displayWidth;
				int mouseY = scaledHeight - Mouse.getY() * scaledHeight / Minecraft.getMinecraft().displayHeight - 1;

				if (mouseX > x && mouseX < x + 80 &&
					mouseY > y + 11 + 121 && mouseY < y + 21 + 121) {
					List<String> text = new ArrayList<>();
					StringBuilder line = new StringBuilder();
					int leftMaxSize = 0;
					int middleMaxSize = 0;
					for (int i = 0; i < missing.size(); i += 3) {
						leftMaxSize = Math.max(leftMaxSize, Minecraft.getMinecraft().fontRendererObj.
							getStringWidth(missing.get(i).getDisplayName()));
					}
					for (int i = 1; i < missing.size(); i += 3) {
						middleMaxSize = Math.max(middleMaxSize, Minecraft.getMinecraft().fontRendererObj.
							getStringWidth(missing.get(i).getDisplayName()));
					}
					for (int i = 0; i < missing.size(); i++) {
						if (i % 3 == 0 && i > 0) {
							text.add(line.toString());
							line = new StringBuilder();
						}
						StringBuilder name = new StringBuilder(missing.get(i).getDisplayName());
						int nameLen = Minecraft.getMinecraft().fontRendererObj.getStringWidth(name.toString());

						int padSize = -1;
						if (i % 3 == 0) padSize = leftMaxSize;
						if (i % 3 == 1) padSize = middleMaxSize;
						if (padSize > 0) {
							float padNum = (padSize - nameLen) / 4.0f;
							int remainder = (int) ((padNum % 1) * 4);
							while (padNum >= 1) {
								if (remainder > 0) {
									name.append(EnumChatFormatting.BOLD).append(" ");
									remainder--;
								} else {
									name.append(EnumChatFormatting.RESET).append(" ");
								}
								padNum--;
							}
						}
						line.append('\u00A7').append(Utils.getPrimaryColourCode(missing.get(i).getDisplayName()));
						if (i < 9) {
							line.append((char) ('\u2776' + i)).append(' ');
						} else {
							line.append("\u2b24 ");
						}
						line.append(name);
						if (i % 3 < 2) line.append("  ");
					}

					GlStateManager.pushMatrix();
					GlStateManager.scale(2f / scaledresolution.getScaleFactor(), 2f / scaledresolution.getScaleFactor(), 1);
					Utils.drawHoveringText(text,
						mouseX * scaledresolution.getScaleFactor() / 2,
						mouseY * scaledresolution.getScaleFactor() / 2,
						scaledWidth * scaledresolution.getScaleFactor() / 2,
						scaledHeight * scaledresolution.getScaleFactor() / 2, -1
					);
					GlStateManager.popMatrix();
				}
			}
		}
	}

	private static void drawString(int x, int y, String abc) {
		Utils.drawStringCenteredScaledMaxWidth(abc, x + 40, y + 12, false, 70, gray());
	}

	private static boolean forceCC = false;
	private static boolean forceAS = false;
	private static boolean useGodPot = true;
	private static boolean allowShaded = true;
	private static int mainWeapon = 1;

	public static void renderOptimizerOverlay(int x, int y) {
		Utils.drawStringCenteredScaledMaxWidth("Optimizer", x + 40, y + 12, false, 70, gray());

		int light = new Color(220, 220, 220).getRGB();
		int dark = new Color(170, 170, 170).getRGB();

		Gui.drawRect(x + 5, y + 32, x + 35, y + 43, forceCC ? dark : light);
		Gui.drawRect(x + 45, y + 32, x + 75, y + 43, forceCC ? light : dark);

		Gui.drawRect(x + 5, y + 52, x + 35, y + 63, forceAS ? dark : light);
		Gui.drawRect(x + 45, y + 52, x + 75, y + 63, forceAS ? light : dark);

		Gui.drawRect(x + 5, y + 72, x + 35, y + 83, useGodPot ? dark : light);
		Gui.drawRect(x + 45, y + 72, x + 75, y + 83, useGodPot ? light : dark);

		Gui.drawRect(x + 5, y + 92, x + 35, y + 103, allowShaded ? dark : light);
		Gui.drawRect(x + 45, y + 92, x + 75, y + 103, allowShaded ? light : dark);

		Gui.drawRect(x + 5, y + 102, x + 75, y + 113, light);
		Gui.drawRect(
			x + 5 + (int) ((mainWeapon - 1) / 9f * 70),
			y + 102,
			x + 5 + (int) (mainWeapon / 9f * 70),
			y + 113,
			dark
		);

		Utils.drawStringCenteredScaledMaxWidth("Force 100% CC", x + 40, y + 27, false, 70, gray());
		Utils.drawStringCenteredScaledMaxWidth(
			(forceCC ? EnumChatFormatting.GREEN : EnumChatFormatting.GRAY) + "YES", x + 20, y + 37, true, 30, gray()
		);
		Utils.drawStringCenteredScaledMaxWidth(
			(forceCC ? EnumChatFormatting.GRAY : EnumChatFormatting.RED) + "NO",
			x + 60, y + 37, true, 30, gray()
		);

		Utils.drawStringCenteredScaledMaxWidth("Force 100% ATKSPEED", x + 40, y + 47, false, 70, gray());
		Utils.drawStringCenteredScaledMaxWidth(
			(forceAS ? EnumChatFormatting.GREEN : EnumChatFormatting.GRAY) + "YES",
			x + 20, y + 57, true, 30, gray()
		);
		Utils.drawStringCenteredScaledMaxWidth(
			(forceAS ? EnumChatFormatting.GRAY : EnumChatFormatting.RED) + "NO",
			x + 60, y + 57, true, 30, gray()
		);

		Utils.drawStringCenteredScaledMaxWidth("Use God Potion", x + 40, y + 67, false, 70, gray());
		Utils.drawStringCenteredScaledMaxWidth(
			(useGodPot ? EnumChatFormatting.GREEN : EnumChatFormatting.GRAY) + "YES",
			x + 20, y + 77, true, 30, gray()
		);
		Utils.drawStringCenteredScaledMaxWidth(
			(useGodPot ? EnumChatFormatting.GRAY : EnumChatFormatting.RED) + "NO",
			x + 60, y + 77, true, 30, gray()
		);

		Utils.drawStringCenteredScaledMaxWidth("Use God Potion", x + 40, y + 87, false, 70, gray());
		Utils.drawStringCenteredScaledMaxWidth((allowShaded ? EnumChatFormatting.GREEN : EnumChatFormatting.GRAY) + "YES",
			x + 20, y + 97, true, 30, gray()
		);
		Utils.drawStringCenteredScaledMaxWidth((allowShaded ? EnumChatFormatting.GRAY : EnumChatFormatting.RED) + "NO",
			x + 60, y + 97,
			true, 30, gray()
		);

		Utils.drawStringCenteredScaledMaxWidth("Main Weapon", x + 40, y + 107, false, 70, gray());
		Utils.drawStringCenteredScaled("1 2 3 4 5 6 7 8 9", x + 40, y + 117, true, 70, gray());
	}

	private static int gray() {
		return new Color(80, 80, 80).getRGB();
	}

	private static Comparator<String> getItemComparator() {
		return (o1, o2) -> {
			double cost1;
			JsonObject o1Auc = NotEnoughUpdates.INSTANCE.manager.auctionManager.getItemAuctionInfo(o1);
			if (o1Auc != null && o1Auc.has("price")) {
				cost1 = o1Auc.get("price").getAsFloat();
			} else {
				APIManager.CraftInfo info = NotEnoughUpdates.INSTANCE.manager.auctionManager.getCraftCost(o1);
				if (info != null)
					cost1 = info.craftCost;
				else
					cost1 = -1;
			}
			double cost2;
			JsonObject o2Auc = NotEnoughUpdates.INSTANCE.manager.auctionManager.getItemAuctionInfo(o2);
			if (o2Auc != null && o2Auc.has("price")) {
				cost2 = o2Auc.get("price").getAsFloat();
			} else {
				APIManager.CraftInfo info = NotEnoughUpdates.INSTANCE.manager.auctionManager.getCraftCost(o2);
				if (info != null)
					cost2 = info.craftCost;
				else
					cost2 = -1;
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
							Utils.drawTexturedRect(
								guiLeft + xSize + 3,
								guiTop,
								80,
								149,
								0,
								80 / 256f,
								0,
								149 / 256f,
								GL11.GL_NEAREST
							);

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

					for (int i = 0; i <= TAB_MISSING; i++) {
						if (i != currentTab) {
							GlStateManager.color(1, 1, 1, 1);
							Minecraft.getMinecraft().getTextureManager().bindTexture(accessory_bag_overlay);
							Utils.drawTexturedRect(guiLeft + xSize + 80, guiTop + 20 * i, 25, 22,
								80 / 256f, 105 / 256f, 0, 22 / 256f, GL11.GL_NEAREST
							);
							Utils.drawItemStack(TAB_STACKS[i], guiLeft + xSize + 80 + 5, guiTop + 20 * i + 3);
						}
					}

					GlStateManager.color(1, 1, 1, 1);
					Minecraft.getMinecraft().getTextureManager().bindTexture(accessory_bag_overlay);
					Utils.drawTexturedRect(guiLeft + xSize + 3, guiTop, 80, 149, 0, 80 / 256f, 0, 149 / 256f, GL11.GL_NEAREST);

					if (pagesVisited.size() < 1) {
						renderVisitOverlay(guiLeft + xSize + 3, guiTop);
						return;
					}

					Minecraft.getMinecraft().getTextureManager().bindTexture(accessory_bag_overlay);
					Utils.drawTexturedRect(guiLeft + xSize + 80, guiTop + 20 * currentTab, 28, 22,
						80 / 256f, 108 / 256f, 22 / 256f, 44 / 256f, GL11.GL_NEAREST
					);
					Utils.drawItemStack(TAB_STACKS[currentTab], guiLeft + xSize + 80 + 8, guiTop + 20 * currentTab + 3);

					switch (currentTab) {
						case TAB_BASIC:
							renderBasicOverlay(guiLeft + xSize + 3, guiTop);
							return;
						case TAB_TOTAL:
							renderTotalStatsOverlay(guiLeft + xSize + 3, guiTop);
							return;
						case TAB_BONUS:
							renderReforgeStatsOverlay(guiLeft + xSize + 3, guiTop);
							return;
						case TAB_DUP:
							renderDuplicatesOverlay(guiLeft + xSize + 3, guiTop);
							return;
						case TAB_MISSING:
							renderMissingOverlay(guiLeft + xSize + 3, guiTop);
							return;
						case TAB_OPTIMIZER:
							renderOptimizerOverlay(guiLeft + xSize + 3, guiTop);
							return;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}




	private static final HashMap<String, Pattern> STAT_PATTERN_MAP_BONUS = new HashMap<String, Pattern>() {{
		String STAT_PATTERN_BONUS_END = ": (?:\\+|-)[0-9]+(?:\\.[0-9]+)?\\%? \\(((?:\\+|-)[0-9]+)%?";
		put("health", Pattern.compile("^Health" + STAT_PATTERN_BONUS_END));
		put("defence", Pattern.compile("^Defense" + STAT_PATTERN_BONUS_END));
		put("strength", Pattern.compile("^Strength" + STAT_PATTERN_BONUS_END));
		put("speed", Pattern.compile("^Speed" + STAT_PATTERN_BONUS_END));
		put("crit_chance", Pattern.compile("^Crit Chance" + STAT_PATTERN_BONUS_END));
		put("crit_damage", Pattern.compile("^Crit Damage" + STAT_PATTERN_BONUS_END));
		put("bonus_attack_speed", Pattern.compile("^Bonus Attack Speed" + STAT_PATTERN_BONUS_END));
		put("intelligence", Pattern.compile("^Intelligence" + STAT_PATTERN_BONUS_END));
		put("sea_creature_chance", Pattern.compile("^Sea Creature Chance" + STAT_PATTERN_BONUS_END));
		put("ferocity", Pattern.compile("^Ferocity" + STAT_PATTERN_BONUS_END));
		put("mining_fortune", Pattern.compile("^Mining Fortune" + STAT_PATTERN_BONUS_END));
		put("mining_speed", Pattern.compile("^Mining Speed" + STAT_PATTERN_BONUS_END));
		put("magic_find", Pattern.compile("^Magic Find" + STAT_PATTERN_BONUS_END));
	}};

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
		put("ferocity", Pattern.compile("^Ferocity" + STAT_PATTERN_END));
		put("mining_fortune", Pattern.compile("^Mining Fortune" + STAT_PATTERN_END));
		put("mining_speed", Pattern.compile("^Mining Speed" + STAT_PATTERN_END));
		put("magic_find", Pattern.compile("^Magic Find" + STAT_PATTERN_END));
	}};

	private static PlayerStats.Stats getStatForItem(
		ItemStack stack,
		HashMap<String, Pattern> patternMap,
		boolean addExtras
	) {
		String internalname = NotEnoughUpdates.INSTANCE.manager.getInternalNameForItem(stack);
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
							if (items.getCompoundTagAt(j).getKeySet().size() > 0) {
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

	// private static String[] rarityArr = new String[] {
	//         "COMMON", "UNCOMMON", "RARE", "EPIC", "LEGENDARY", "MYTHIC", "SPECIAL", "VERY SPECIAL", "SUPREME"
	// };
	// private static String[] rarityArrC = new String[] {
	//         EnumChatFormatting.WHITE+EnumChatFormatting.BOLD.toString()+"COMMON",
	//         EnumChatFormatting.GREEN+EnumChatFormatting.BOLD.toString()+"UNCOMMON",
	//         EnumChatFormatting.BLUE+EnumChatFormatting.BOLD.toString()+"RARE",
	//         EnumChatFormatting.DARK_PURPLE+EnumChatFormatting.BOLD.toString()+"EPIC",
	//         EnumChatFormatting.GOLD+EnumChatFormatting.BOLD.toString()+"LEGENDARY",
	//         EnumChatFormatting.LIGHT_PURPLE+EnumChatFormatting.BOLD.toString()+"MYTHIC",
	//         EnumChatFormatting.RED+EnumChatFormatting.BOLD.toString()+"SPECIAL",
	//         EnumChatFormatting.RED+EnumChatFormatting.BOLD.toString()+"VERY SPECIAL",
	//         EnumChatFormatting.DARK_RED+EnumChatFormatting.BOLD.toString()+"SUPREME",
	// };

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
		return checkItemType(stack, true, "ACCESSORY", "HATCCESSORY") >= 0;
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
}
