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

package io.github.moulberry.notenoughupdates.overlays;

import com.google.gson.annotations.Expose;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.core.config.Position;
import io.github.moulberry.notenoughupdates.core.util.StringUtils;
import io.github.moulberry.notenoughupdates.core.util.lerp.LerpUtils;
import io.github.moulberry.notenoughupdates.guifeatures.SkyMallDisplay;
import io.github.moulberry.notenoughupdates.miscfeatures.GlaciteTunnelWaypoints;
import io.github.moulberry.notenoughupdates.miscfeatures.ItemCooldowns;
import io.github.moulberry.notenoughupdates.miscfeatures.tablisttutorial.TablistAPI;
import io.github.moulberry.notenoughupdates.options.NEUConfig;
import io.github.moulberry.notenoughupdates.options.separatesections.Mining;
import io.github.moulberry.notenoughupdates.util.ItemResolutionQuery;
import io.github.moulberry.notenoughupdates.util.SBInfo;
import io.github.moulberry.notenoughupdates.util.StarCultCalculator;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.util.vector.Vector2f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.github.moulberry.notenoughupdates.util.Utils.showOutdatedRepoNotification;
import static net.minecraft.util.EnumChatFormatting.DARK_AQUA;
import static net.minecraft.util.EnumChatFormatting.GOLD;
import static net.minecraft.util.EnumChatFormatting.GREEN;
import static net.minecraft.util.EnumChatFormatting.RED;
import static net.minecraft.util.EnumChatFormatting.YELLOW;

public class MiningOverlay extends TextTabOverlay {
	public MiningOverlay(
		Position position,
		Supplier<List<String>> dummyStrings,
		Supplier<TextOverlayStyle> styleSupplier
	) {
		super(position, dummyStrings, styleSupplier);
	}

	private static final Pattern NUMBER_PATTERN = Pattern.compile("(?<number>\\d*,?\\d+)(?: |$)");
	public static Map<String, Float> commissionProgress = new LinkedHashMap<>();

	@Override
	public void updateFrequent() {
		if (Minecraft.getMinecraft().currentScreen instanceof GuiChest) {
			GuiChest chest = (GuiChest) Minecraft.getMinecraft().currentScreen;
			ContainerChest container = (ContainerChest) chest.inventorySlots;
			IInventory lower = container.getLowerChestInventory();
			String containerName = lower.getDisplayName().getUnformattedText();

			if (containerName.equals("Commissions") && lower.getSizeInventory() >= 27) {
				updateCommissions(lower);
			} else if (containerName.equals("Forge") && lower.getSizeInventory() >= 36) {
				updateForge(lower);
			}
		}
	}

	private void updateForge(IInventory lower) {
		NEUConfig.HiddenProfileSpecific hidden = NotEnoughUpdates.INSTANCE.config.getProfileSpecific();
		if (hidden == null) {
			return;
		}

		itemLoop:
		for (int i = 0; i < 5; i++) {
			ItemStack stack = lower.getStackInSlot(i + 11);
			if (stack != null) {
				String[] lore = NotEnoughUpdates.INSTANCE.manager.getLoreFromNBT(stack.getTagCompound());

				for (String line : lore) {
					Matcher matcher = timeRemainingForge.matcher(line);
					if (stack.getDisplayName().matches("\\xA7cSlot #([1-5])")) {
						ForgeItem newForgeItem = new ForgeItem(i, 1, false);
						replaceForgeOrAdd(newForgeItem, hidden.forgeItems, true);
						//empty Slot
					} else if (stack.getDisplayName().matches("\\xA7aSlot #([1-5])")) {
						ForgeItem newForgeItem = new ForgeItem(i, 0, false);
						replaceForgeOrAdd(newForgeItem, hidden.forgeItems, true);
					} else if (matcher.matches()) {
						String timeremainingString = matcher.group(1);

						long duration = 0;

						if (matcher.group("Completed") != null && !matcher.group("Completed").equals("")) {
							ForgeItem newForgeItem = new ForgeItem(Utils.cleanColour(stack.getDisplayName()), 0, i, false);
							replaceForgeOrAdd(newForgeItem, hidden.forgeItems, true);
						} else {

							try {
								if (matcher.group("days") != null && !matcher.group("days").equals("")) {
									duration = duration + (long) Integer.parseInt(matcher.group("days")) * 24 * 60 * 60 * 1000;
								}
								if (matcher.group("hours") != null && !matcher.group("hours").equals("")) {
									duration = duration + (long) Integer.parseInt(matcher.group("hours")) * 60 * 60 * 1000;
								}
								if (matcher.group("minutes") != null && !matcher.group("minutes").equals("")) {
									duration = duration + (long) Integer.parseInt(matcher.group("minutes")) * 60 * 1000;
								}
								if (matcher.group("seconds") != null && !matcher.group("seconds").equals("")) {
									duration = duration + (long) Integer.parseInt(matcher.group("seconds")) * 1000;
								}
							} catch (Exception ignored) {
							}
							if (duration > 0) {
								ForgeItem newForgeItem = new ForgeItem(
									Utils.cleanColour(stack.getDisplayName()),
									System.currentTimeMillis() + duration,
									i,
									false
								);
								replaceForgeOrAdd(newForgeItem, hidden.forgeItems, true);
							}
						}

						continue itemLoop;
					}
				}

				//Locked Slot
			}
		}
	}

	private void updateCommissions(IInventory lower) {
		// Get the location (type) of the currently shown commissions
		ItemStack commTypeStack = lower.getStackInSlot(32);
		if (commTypeStack == null || !commTypeStack.hasTagCompound()) {
			return;
		}

		String name = Utils.cleanColour(commTypeStack.getDisplayName()).trim();
		if (!name.equals("Filter")) {
			return;
		}

		String commLocation = null;
		String[] lore = NotEnoughUpdates.INSTANCE.manager.getLoreFromNBT(commTypeStack.getTagCompound());
		for (String line : lore) {
			if (line == null) {
				continue;
			}
			if (!line.contains("▶")) continue;
			String cleanLine = Utils.cleanColour(line).replace("▶", "").trim();
			if (cleanLine.equals("Dwarven Mines")) {
				commLocation = "mining_3";
			} else if (cleanLine.equals("Crystal Hollows")) {
				commLocation = "crystal_hollows";
			} else if (cleanLine.equals("Glacite Tunnels")) {
				commLocation = "mineshaft";
			} else {
				continue;
			}
			break;
		}
		if (commLocation == null) {
			return;
		}

		// Now get the commission info
		for (int i = 9; i < 18; i++) {
			ItemStack stack = lower.getStackInSlot(i);
			if (stack != null && stack.hasTagCompound()) {
				lore = NotEnoughUpdates.INSTANCE.manager.getLoreFromNBT(stack.getTagCompound());
				String commName = null;
				int numberValue = -1;
				for (String line : lore) {
					if (commName != null) {
						String clean = Utils.cleanColour(line).trim();
						if (clean.isEmpty()) {
							break;
						} else {
							Matcher matcher = NUMBER_PATTERN.matcher(clean);
							if (matcher.find()) {
								try {
									numberValue = Integer.parseInt(matcher.group("number").replace(",", ""));
								} catch (NumberFormatException ignored) {
								}
							}
						}
					}
					if (line.startsWith("\u00a79")) {
						String textAfter = line.substring(2);
						if (!textAfter.contains("\u00a7") && !textAfter.equals("Rewards") && !textAfter.equals("Progress")) {
							commName = textAfter;
						}
					}
				}

				NEUConfig.HiddenLocationSpecific locationSpecific = NotEnoughUpdates.INSTANCE.config.getLocationSpecific(
					commLocation);
				if (commName != null && numberValue > 0) {
					locationSpecific.commissionMaxes.put(commName, numberValue);
				}
			}
		}
	}

	private static final Pattern timeRemainingForge = Pattern.compile(
		"\\xA77Time Remaining: \\xA7a((?<Completed>Completed!)|(((?<days>[0-9]+)d)? ?((?<hours>[0-9]+)h)? ?((?<minutes>[0-9]+)m)? ?((?<seconds>[0-9]+)s)?))");
	private static final Pattern timeRemainingTab = Pattern.compile(
		".*[1-5]\\) (?<ItemName>.*): ((?<Ready>Ready!)|(((?<days>[0-9]+)d)? ?((?<hours>[0-9]+)h)? ?((?<minutes>[0-9]+)m)? ?((?<seconds>[0-9]+)s)?))");
	private static final Pattern forgeIntPattern = Pattern.compile(
		"[^)]*([1-5])\\).*");

	@Override
	public boolean isEnabled() {
		return NotEnoughUpdates.INSTANCE.config.mining.dwarvenOverlay;
	}

	public @Nullable NEUConfig.HiddenLocationSpecific getMiningLocationSpecific() {
		String location = SBInfo.getInstance().getLocation();
		if (location == null || location.isEmpty()) {
			return null;
		}

		String sideBarLoc = SBInfo.getInstance().getScoreboardLocation();
		if (location.equals("mining_3")
			&& (GlaciteTunnelWaypoints.INSTANCE.getGlaciteTunnelLocations().contains(sideBarLoc))) {
			location = "mineshaft";
		}
		return NotEnoughUpdates.INSTANCE.config.getLocationSpecific(location);

	}

	@Override
	public void update() {
		overlayStrings = null;
		NEUConfig.HiddenProfileSpecific profileConfig = NotEnoughUpdates.INSTANCE.config.getProfileSpecific();

		if (!NotEnoughUpdates.INSTANCE.config.mining.dwarvenOverlay &&
			NotEnoughUpdates.INSTANCE.config.mining.emissaryWaypoints == 0 &&
			!NotEnoughUpdates.INSTANCE.config.mining.titaniumAlert &&
			NotEnoughUpdates.INSTANCE.config.mining.locWaypoints == 0
			&& NotEnoughUpdates.INSTANCE.config.mining.tunnelWaypoints != Mining.GlaciteTunnelWaypointBehaviour.NONE) {
			return;
		}

		// Get commission and forge info even if the overlay isn't going to be rendered since it is used elsewhere
		//thanks to "Pure Genie#7250" for helping with this (makes tita alert and waypoints work without mine overlay)
		if (SBInfo.getInstance().getLocation() == null) return;
		if (SBInfo.getInstance().getLocation().equals("mining_3") ||
			SBInfo.getInstance().getLocation().equals("crystal_hollows")
			|| SBInfo.getInstance().getLocation().equals("mineshaft")) {
			commissionProgress.clear();

			// These strings will be displayed one after the other when the player list is disabled
			String mithrilPowder = "";
			String gemstonePowder = "";
			String glacitePowder = "";

			List<String> powderLines = getTabLinesOrAddWarning(1, TablistAPI.WidgetNames.POWDER);
			getTabLinesOrAddWarning(2, TablistAPI.WidgetNames.POWDER);
			getTabLinesOrAddWarning(6, TablistAPI.WidgetNames.POWDER);

			for (String line : powderLines) {
				if (line.contains("Mithril:")) {
					mithrilPowder = DARK_AQUA + Utils.trimWhitespaceAndFormatCodes(line).replaceAll("\u00a7[f|F|r]", "");
				}
				if (line.contains("Gemstone:")) {
					gemstonePowder = DARK_AQUA + Utils.trimWhitespaceAndFormatCodes(line).replaceAll("\u00a7[f|F|r]", "");
				}
				if (line.contains("Glacite: ")) {
					glacitePowder = DARK_AQUA + Utils.trimWhitespaceAndFormatCodes(line).replaceAll("\u00a7[f|F|r]", "");
				}
			}

			List<String> tabForgeLines = getTabLinesOrAddWarning(3, TablistAPI.WidgetNames.FORGE);

			Set<Integer> foundForges = new HashSet<>();

			for (String name : tabForgeLines) {
				String cleanName = StringUtils.cleanColour(name);
				if (cleanName.startsWith(" ") && profileConfig != null) {
					char firstChar = cleanName.trim().charAt(0);
					if (firstChar < '0' || firstChar > '9') {
						break;
					} else {
						int forgeInt;
						Matcher forgeIntMatcher = forgeIntPattern.matcher(cleanName);
						if (forgeIntMatcher.matches()) {
							forgeInt = Integer.parseInt(forgeIntMatcher.group(1)) - 1;
							foundForges.add(forgeInt);
						} else {
							continue;
						}
						if (name.contains("LOCKED")) {
							ForgeItem item = new ForgeItem(forgeInt, 1, true);
							replaceForgeOrAdd(item, profileConfig.forgeItems, true);
						} else if (name.contains("EMPTY")) {
							ForgeItem item = new ForgeItem(forgeInt, 0, true);
							replaceForgeOrAdd(item, profileConfig.forgeItems, true);
						} else {
							Matcher matcher = timeRemainingTab.matcher(cleanName);

							if (matcher.matches()) {

								String itemName = matcher.group(1);

								if (matcher.group("Ready") != null && !matcher.group("Ready").equals("")) {
									ForgeItem item = new ForgeItem(Utils.cleanColour(itemName), 0, forgeInt, true);
									replaceForgeOrAdd(item, profileConfig.forgeItems, true);
								} else {
									long duration = 0;
									try {
										if (matcher.group("days") != null && !matcher.group("days").equals("")) {
											duration = duration + (long) Integer.parseInt(matcher.group("days")) * 24 * 60 * 60 * 1000;
										}
										if (matcher.group("hours") != null && !matcher.group("hours").equals("")) {
											duration = duration + (long) Integer.parseInt(matcher.group("hours")) * 60 * 60 * 1000;
										}
										if (matcher.group("minutes") != null && !matcher.group("minutes").equals("")) {
											duration = duration + (long) Integer.parseInt(matcher.group("minutes")) * 60 * 1000;
										}
										if (matcher.group("seconds") != null && !matcher.group("seconds").equals("")) {
											duration = duration + (long) Integer.parseInt(matcher.group("seconds")) * 1000;
										}
									} catch (Exception ignored) {
									}
									if (duration > 0) {
										duration = duration + 4000;
										ForgeItem item = new ForgeItem(
											Utils.cleanColour(itemName),
											System.currentTimeMillis() + duration,
											forgeInt,
											true
										);
										replaceForgeOrAdd(item, profileConfig.forgeItems, false);
									}
								}
							}
						}
					}
				}
			}

			if (profileConfig != null)
				for (int i = 0; i < 5; i++) {
					if (foundForges.contains(i)) continue;
					ForgeItem item = new ForgeItem(i, 0, true);
					replaceForgeOrAdd(item, profileConfig.forgeItems, true);
				}

			List<String> tabCommissionLines = getTabLinesOrAddWarning(0, TablistAPI.WidgetNames.COMMISSIONS);

			for (String name : tabCommissionLines) {
				String cleanName = StringUtils.cleanColour(name);
				if (cleanName.startsWith(" ") && profileConfig != null) {
					String[] split = cleanName.trim().split(": ");
					if (split.length == 2) {
						if (split[1].endsWith("%")) {
							try {
								float progress = Float.parseFloat(split[1].replace("%", "")) / 100;
								progress = LerpUtils.clampZeroOne(progress);
								commissionProgress.put(split[0], progress);
							} catch (Exception ignored) {
							}
						} else {
							commissionProgress.put(split[0], 1.0f);
						}
					}
				}
			}

			if (ItemCooldowns.firstLoadMillis > 0) {
				//set cooldown on first skyblock load.
				ItemCooldowns.pickaxeUseCooldownMillisRemaining =
					60 * 1000 - (System.currentTimeMillis() - ItemCooldowns.firstLoadMillis);
				ItemCooldowns.firstLoadMillis = 0;
			}

			if (!NotEnoughUpdates.INSTANCE.config.mining.dwarvenOverlay) {
				overlayStrings = null;
				return;
			}

			List<String> commissionsStrings = new ArrayList<>();
			for (Map.Entry<String, Float> entry : commissionProgress.entrySet()) {
				if (entry.getValue() >= 1) {
					commissionsStrings.add(DARK_AQUA + entry.getKey() + ": " + GREEN + "DONE");
				} else {
					EnumChatFormatting col = RED;
					if (entry.getValue() >= 0.75) {
						col = GREEN;
					} else if (entry.getValue() >= 0.5) {
						col = YELLOW;
					} else if (entry.getValue() >= 0.25) {
						col = GOLD;
					}
					String tips = getTipPart(entry.getKey());
					boolean newLine = NotEnoughUpdates.INSTANCE.config.mining.commissionTaskTipNewLine;
					String newLineTip = null;
					if (newLine) {
						if (!tips.isEmpty()) {
							newLineTip = "  " + tips;
							tips = "";
						}
					}
					NEUConfig.HiddenLocationSpecific locationSpecific = getMiningLocationSpecific();
					int max;
					if (-1 != (max = locationSpecific.commissionMaxes.getOrDefault(entry.getKey(), -1))) {
						commissionsStrings.add(
							DARK_AQUA + entry.getKey() + ": " + col + Math.round(entry.getValue() * max) + "/" + max + tips);
					} else {
						String valS = Utils.floatToString(entry.getValue() * 100, 1);

						commissionsStrings.add(DARK_AQUA + entry.getKey() + ": " + col + valS + "%" + tips);
					}
					if (newLineTip != null) {
						commissionsStrings.add(newLineTip);
					}
				}
			}

			String pickaxeCooldown;
			if (ItemCooldowns.pickaxeUseCooldownMillisRemaining <= 0) {
				pickaxeCooldown = DARK_AQUA + "Pickaxe CD: \u00a7aReady";
			} else {
				pickaxeCooldown =
					DARK_AQUA + "Pickaxe CD: \u00a7a" + (ItemCooldowns.pickaxeUseCooldownMillisRemaining / 1000) + "s";
			}
			overlayStrings = new ArrayList<>();
			for (int index : NotEnoughUpdates.INSTANCE.config.mining.dwarvenText2) {
				switch (index) {
					case 0:
						overlayStrings.addAll(commissionsStrings);
						break;
					case 1:
						overlayStrings.add(mithrilPowder);
						break;
					case 2:
						overlayStrings.add(gemstonePowder);
						break;
					case 3:
						if (profileConfig != null) {
							overlayStrings.addAll(getForgeStrings(profileConfig.forgeItems));
						}
						break;
					case 4:
						overlayStrings.add(pickaxeCooldown);
						break;
					case 5:
						overlayStrings.add(
							DARK_AQUA + "Star Cult: " + GREEN + StarCultCalculator.getNextStarCult());
						break;
					case 6:
						overlayStrings.add("§3Sky Mall: §a" + SkyMallDisplay.Companion.getDisplayText());
						break;
					case 7:
						overlayStrings.add(glacitePowder);
						break;
				}
			}
		} else {
			if (profileConfig == null) {
				return;
			}
			boolean forgeDisplay = false;
			boolean starCultDisplay = false;
			boolean skyMallDisplay = false;
			for (int i = 0; i < NotEnoughUpdates.INSTANCE.config.mining.dwarvenText2.size(); i++) {
				if (NotEnoughUpdates.INSTANCE.config.mining.dwarvenText2.get(i) == 3) {
					forgeDisplay = true;
				}
				if (NotEnoughUpdates.INSTANCE.config.mining.dwarvenText2.get(i) == 5) {
					starCultDisplay = true;
				}
				if (NotEnoughUpdates.INSTANCE.config.mining.dwarvenText2.get(i) == 6) {
					skyMallDisplay = true;
				}
			}

			if (starCultDisplay) {
				if (overlayStrings == null) overlayStrings = new ArrayList<>();

				if (!NotEnoughUpdates.INSTANCE.config.mining.starCultDisplayOnlyShowTab ||
					lastTabState) {
					if (NotEnoughUpdates.INSTANCE.config.mining.starCultDisplayEnabledLocations == 1 &&
						!SBInfo.getInstance().isInDungeon) {
						overlayStrings.add(
							DARK_AQUA + "Star Cult: " + GREEN +
								StarCultCalculator.getNextStarCult());
					} else if (NotEnoughUpdates.INSTANCE.config.mining.starCultDisplayEnabledLocations == 2) {
						overlayStrings.add(
							DARK_AQUA + "Star Cult: " + GREEN +
								StarCultCalculator.getNextStarCult());
					}
				}
			}

			if (forgeDisplay) {
				if (overlayStrings == null) overlayStrings = new ArrayList<>();

				if (!NotEnoughUpdates.INSTANCE.config.mining.forgeDisplayOnlyShowTab ||
					lastTabState) {
					if (NotEnoughUpdates.INSTANCE.config.mining.forgeDisplayEnabledLocations == 1 &&
						!SBInfo.getInstance().isInDungeon) {
						overlayStrings.addAll(getForgeStrings(profileConfig.forgeItems));
					} else if (NotEnoughUpdates.INSTANCE.config.mining.forgeDisplayEnabledLocations == 2) {
						overlayStrings.addAll(getForgeStrings(profileConfig.forgeItems));
					}
				}
			}

			if (skyMallDisplay) {
				if (overlayStrings == null) overlayStrings = new ArrayList<>();

				if (!NotEnoughUpdates.INSTANCE.config.mining.skyMallDisplayOnlyShowTab ||
					lastTabState) {
					if (NotEnoughUpdates.INSTANCE.config.mining.skyMallDisplayEnabledLocations == 1 &&
						!SBInfo.getInstance().isInDungeon) {
						overlayStrings.add(
							DARK_AQUA + "Sky Mall: " + GREEN +
								SkyMallDisplay.Companion.getDisplayText());
					} else if (NotEnoughUpdates.INSTANCE.config.mining.skyMallDisplayEnabledLocations == 2) {
						overlayStrings.add(
							DARK_AQUA + "Sky Mall: " + GREEN +
								SkyMallDisplay.Companion.getDisplayText());
					}
				}
			}
		}

		if (overlayStrings != null && overlayStrings.isEmpty()) overlayStrings = null;
	}

	private List<String> getTabLinesOrAddWarning(int configIndex, TablistAPI.WidgetNames widgetName) {
		List<String> lines;
		if (NotEnoughUpdates.INSTANCE.config.mining.dwarvenText2.contains(configIndex) &&
			NotEnoughUpdates.INSTANCE.config.mining.dwarvenOverlay) {
			lines = TablistAPI.getWidgetLinesWithoutNotification(widgetName);
			if (lines.isEmpty() && (overlayStrings == null || !overlayStrings.contains("§l§4One or more tab widgets missing!"))) {
				if (overlayStrings == null) overlayStrings = new ArrayList<>();
				overlayStrings.add("§l§4One or more tab widgets missing!");
				overlayStrings.add("§l§4Enable it in §b/tab§4!");
			}
		} else {
			lines = TablistAPI.getOptionalWidgetLines(widgetName);
		}

		return lines;
	}

	private String getTipPart(String name) {
		int settings = NotEnoughUpdates.INSTANCE.config.mining.commissionTaskTips;
		if (settings == 0) return "";

		if (!Minecraft.getMinecraft().thePlayer.isSneaking() && settings == 1) return "";

		String tip = getTip(name);
		if (tip == null) return "  §4???";

		return " §8§l>§7 " + tip;
	}

	private String getTip(String name) {
		if (SBInfo.getInstance().getLocation().equals("mining_3")) { // Dwarven Mines
			if (name.equals("First Event")) return "Participate in any §6Mining Event";

			// During Event
			if (name.equals("Lucky Raffle")) return "Collect 20 Raffle Tickets during §6Raffle Event";
			if (name.equals("Goblin Raid Slayer")) return "Kill 20 Goblins during §6Goblin Raid Event";
			if (name.equals("Raffle")) return "Participate in §6Raffle Event";
			if (name.equals("Goblin Raid")) return "Participate in §6Goblin Raid event";
			if (name.equals("2x Mithril Powder Collector")) return "Collect 500 Mithril Powder during §62x Powder event";

			// Slay
			if (name.equals("Glacite Walker Slayer")) return "Kill 50 Glacite Walkers §b(Great Ice Wall)";
			if (name.equals("Goblin Slayer")) return "Kill 100 Goblins §b(Goblin Burrows)";
			if (name.equals("Golden Goblin Slayer")) return "Kill 1 Golden Goblin (anywhere)";
			if (name.equals("Star Sentry Puncher")) return "Damage Star Sentries 10 times (anywhere)";
			if (name.equals("Treasure Hoarder Puncher")) return "Damage Treasure Hoarders 10 times §b(Upper Mines)";
			if (name.equals("Mines Slayer")) return "Kill 50 mobs (anywhere)";

			// Mining
			if (name.equals("Mithril Miner")) return "Break 350 Mithril (anywhere)";
			if (name.equals("Titanium Miner")) return "Break 15 Titanium (anywhere)";

			if (name.equals("Cliffside Veins Mithril")) return "Break 250 Mithril §b(Cliffside Veins)";
			if (name.equals("Royal Mines Mithril")) return "Break 250 Mithril §b(Royal Mines)";
			if (name.equals("Lava Springs Mithril")) return "Break 250 Mithril §b(Lava Springs)";
			if (name.equals("Rampart's Quarry Mithril")) return "Break 250 Mithril §b(Rampart's Quarry)";
			if (name.equals("Upper Mines Mithril")) return "Break 250 Mithril §b(Upper Mines)";

			if (name.equals("Cliffside Veins Titanium")) return "Break 10 Titanium §b(Cliffside Veins)";
			if (name.equals("Lava Springs Titanium")) return "Break 10 Titanium §b(Lava Springs)";
			if (name.equals("Royal Mines Titanium")) return "Break 10 Titanium §b(Royal Mines)";
			if (name.equals("Rampart's Quarry Titanium")) return "Break 10 Titanium §b(Rampart's Quarry)";
			if (name.equals("Upper Mines Titanium")) return "Break 10 Titanium §b(Upper Mines)";

			// Glacite Tunnels

			if (name.equals("Corpse Looter")) return "Find Corpses in a Glacite Mineshaft";
			if (name.equals("Mineshaft Explorer")) return "Discover a Glacite Mineshaft";
			if (name.equals("Scrap Collector")) return "Break non-vanilla Ores and not Hard Stone in a Glacite Mineshaft";
			if (name.equals("Umber Collector")) return "Break red sand/hardened clay";
			if (name.equals("Tungsten Collector")) return "Break cobblestone/clay";
			if (name.equals("Glacite Collector")) return "Break ice";
			if (name.equals("Onyx Gemstone Collector")) return "Break black glass";
			if (name.equals("Aquamarine Gemstone Collector")) return "Break aqua glass";
			if (name.equals("Peridot Gemstone Collector")) return "Break dark green glass";
			if (name.equals("Citrine Gemstone Collector")) return "Break brown glass";

		} else if (SBInfo.getInstance().getLocation().equals("crystal_hollows")) { // Crystal Hollows
			if (name.equals("Chest Looter")) return "Open 3 chests";
			if (name.equals("Hard Stone Miner")) return "Break 1,000 Hard Stone";

			String jungle = " §a(Jungle)";
			String goblin = " §6(Golbin Holdout)";
			String mithril = " §b(Mithril Deposits)";
			String precursor = " §8(Precursor Remenants)";
			String magma = " §c(Magma Fields)";

			if (name.equals("Goblin Slayer")) return "Kill 13 Goblins" + goblin;
			if (name.equals("Sludge Slayer")) return "Kill 25 Sludges" + jungle;
			if (name.equals("Thyst Slayer")) return "Kill 5 Thysts, when breaking Amethysts" + jungle;
			if (name.equals("Boss Corleone Slayer")) return "Find and kill Corleone" + mithril;
			if (name.equals("Yog Slayer")) return "Kill 13 Yogs" + magma;
			if (name.equals("Automaton Slayer")) return "Kill 13 Automatons" + precursor;
			if (name.equals("Team Treasurite Member Slayer")) return "Kill 13 Team Treasurite Members" + mithril;

			if (name.endsWith("Crystal Hunter")) {
				if (name.startsWith("Amethyst")) return "Temple Jump & Run" + jungle;
				if (name.startsWith("Jade")) return "4 weapons from Mines of Divan" + mithril;
				if (name.startsWith("Amber")) return "King and Queen" + goblin;
				if (name.startsWith("Sapphire")) return "6 Robot Parts in Precursor City" + precursor;
				if (name.startsWith("Topaz")) return "Kill Bal" + magma;
			}

			if (name.endsWith("Gemstone Collector")) {
				if (name.startsWith("Amber")) return "Break orange glass" + goblin;
				if (name.startsWith("Sapphire")) return "Break blue glass" + precursor;
				if (name.startsWith("Jade")) return "Break green glass" + mithril;
				if (name.startsWith("Amethyst")) return "Break purple glass" + jungle;
				if (name.startsWith("Ruby")) return "Break red glass (anywhere)";
				if (name.startsWith("Topaz")) return "Break yellow glass" + magma;
			}
		} else if (SBInfo.getInstance().getLocation().equals("mineshaft")) {
			if (name.equals("Corpse Looter")) return "Find Corpses and click them";
			if (name.equals("Mineshaft Explorer")) return "Discover a Glacite Mineshaft";
			if (name.equals("Scrap Collector")) return "Break non-vanilla Ores and not Hard Stone";
			if (name.equals("Umber Collector")) return "Break red sand/hardened clay";
			if (name.equals("Tungsten Collector")) return "Break cobblestone/clay";
			if (name.equals("Glacite Collector")) return "Break ice";
			if (name.equals("Onyx Gemstone Collector")) return "Break black glass";
			if (name.equals("Aquamarine Gemstone Collector")) return "Break aqua glass";
			if (name.equals("Peridot Gemstone Collector")) return "Break dark green glass";
			if (name.equals("Citrine Gemstone Collector")) return "Break brown glass";
		}

		return null;
	}

	private static List<String> getForgeStrings(List<ForgeItem> forgeItems) {
		List<String> forgeString = new ArrayList<>();
		long currentTimeMillis = System.currentTimeMillis();
		forgeIDLabel:
		for (int i = 0; i < 5; i++) {
			for (ForgeItem item : forgeItems) {
				if (item.forgeID == i) {
					if (NotEnoughUpdates.INSTANCE.config.mining.forgeDisplay == 0) {
						if (item.status == 2 && item.finishTime < currentTimeMillis) {

							forgeString.add(item.getFormattedString(currentTimeMillis));
							continue forgeIDLabel;
						}
					} else if (NotEnoughUpdates.INSTANCE.config.mining.forgeDisplay == 1) {
						if (item.status == 2) {

							forgeString.add(item.getFormattedString(currentTimeMillis));
							continue forgeIDLabel;
						}
					} else if (NotEnoughUpdates.INSTANCE.config.mining.forgeDisplay == 2) {
						if (item.status == 2 || item.status == 0) {

							forgeString.add(item.getFormattedString(currentTimeMillis));
							continue forgeIDLabel;
						}
					} else if (NotEnoughUpdates.INSTANCE.config.mining.forgeDisplay == 3) {

						forgeString.add(item.getFormattedString(currentTimeMillis));
						continue forgeIDLabel;
					}
				}
			}
		}
		return forgeString;
	}

	private static void replaceForgeOrAdd(ForgeItem item, List<ForgeItem> forgeItems, boolean overwrite) {
		for (int i = 0; i < forgeItems.size(); i++) {
			if (forgeItems.get(i).forgeID == item.forgeID) {
				if (overwrite) {
					forgeItems.set(i, item);
					return;
				} else {
					ForgeItem currentItem = forgeItems.get(i);
					if (!(currentItem.status == 2 && item.status == 2)) {
						forgeItems.set(i, item);
						return;
					} else if (currentItem.fromScoreBoard) {
						forgeItems.set(i, item);
						return;
					}
				}
				return;
			}
		}
		forgeItems.add(item);
	}

	public static class ForgeItem {
		public ForgeItem(String itemName, long finishTime, int forgeID, boolean fromScoreBoard) {
			this.itemName = itemName;
			this.finishTime = finishTime;
			this.status = 2;
			this.forgeID = forgeID;
			this.fromScoreBoard = fromScoreBoard;
		}

		public ForgeItem(int forgeID, int status, boolean fromScoreBoard) {
			this.forgeID = forgeID;
			this.status = status;
			this.fromScoreBoard = fromScoreBoard;
		}

		@Expose
		public String itemName;
		@Expose
		public long finishTime;
		@Expose
		public final int status;
		@Expose
		public final int forgeID;
		@Expose
		public final boolean fromScoreBoard;

		public String getFormattedString(long currentTimeMillis) {
			String returnText = EnumChatFormatting.DARK_AQUA + "Forge " + (this.forgeID + 1) + ": ";
			if (status == 0) {
				return returnText + EnumChatFormatting.GRAY + "Empty";
			} else if (status == 1) {
				return returnText + EnumChatFormatting.DARK_RED + "Locked";
			}

			long timeDuration = finishTime - currentTimeMillis;
			returnText = returnText + EnumChatFormatting.DARK_PURPLE + this.itemName + ": ";

			int days = (int) (timeDuration / (1000 * 60 * 60 * 24));
			timeDuration = timeDuration - (days * (1000 * 60 * 60 * 24));
			int hours = (int) ((timeDuration / (1000 * 60 * 60)) % 24);

			if (days > 0) {
				return returnText + EnumChatFormatting.AQUA + days + "d " + hours + "h";
			}
			timeDuration = timeDuration - (hours * (1000 * 60 * 60));
			int minutes = (int) ((timeDuration / (1000 * 60)) % 60);
			if (hours > 0) {
				return returnText + EnumChatFormatting.AQUA + hours + "h " + minutes + "m";
			}
			timeDuration = timeDuration - (minutes * (1000 * 60));
			int seconds = (int) (timeDuration / 1000) % 60;
			if (minutes > 0) {
				return returnText + EnumChatFormatting.AQUA + minutes + "m " + seconds + "s";
			} else if (seconds > 0) {
				return returnText + EnumChatFormatting.AQUA + seconds + "s";
			} else {
				return returnText + EnumChatFormatting.DARK_GREEN + "Done";
			}
		}
	}

	@Override
	protected Vector2f getSize(List<String> strings) {
		if (NotEnoughUpdates.INSTANCE.config.mining.dwarvenOverlayIcons)
			return super.getSize(strings).translate(12, 0);
		return super.getSize(strings);
	}

	@Override
	protected void renderLine(String line, Vector2f position, boolean dummy) {
		if (!NotEnoughUpdates.INSTANCE.config.mining.dwarvenOverlayIcons) return;
		GlStateManager.enableDepth();

		// No icon for the tip line
		if (line.contains(">")) return;

		ItemStack icon = null;
		String cleaned = Utils.cleanColour(line);
		String beforeColon = cleaned.split(":")[0];

		if (miningOverlayCommissionItems == null) {
			setupMiningOverlayCommissionItems();
		}

		if (miningOverlayCommissionItems.containsKey(beforeColon)) {
			icon = miningOverlayCommissionItems.get(beforeColon);
		} else {
			if (beforeColon.startsWith("Forge")) {
				icon = miningOverlayCommissionItems.get("Forge");
			} else if (beforeColon.contains("Mithril")) {
				icon = miningOverlayCommissionItems.get("Mithril");
			} else if (beforeColon.endsWith(" Gemstone Collector")) {
				String gemName = "ROUGH_"
					+ beforeColon.replace(" Gemstone Collector", "").toUpperCase() + "_GEM";
				if (miningOverlayRoughGems.containsKey(gemName)) {
					icon = miningOverlayRoughGems.get(gemName);
				} else {
					icon = NotEnoughUpdates.INSTANCE.manager.jsonToStack(NotEnoughUpdates.INSTANCE.manager
						.getItemInformation()
						.get(gemName));
					miningOverlayRoughGems.put(gemName, icon);
				}
			} else if (beforeColon.endsWith(" Crystal Hunter")) {
				String gemName = "PERFECT_"
					+ beforeColon.replace(" Crystal Hunter", "").toUpperCase() + "_GEM";
				if (miningOverlayPerfectGems.containsKey(gemName)) {
					icon = miningOverlayPerfectGems.get(gemName);
				} else {
					icon = NotEnoughUpdates.INSTANCE.manager.jsonToStack(NotEnoughUpdates.INSTANCE.manager
						.getItemInformation()
						.get(gemName));
					miningOverlayPerfectGems.put(gemName, icon);
				}
			} else if (beforeColon.contains("Titanium")) {
				icon = miningOverlayCommissionItems.get("Titanium");
			} else if (beforeColon.contains("Sky Mall")) {
				icon = SkyMallDisplay.Companion.getDisplayItem();
			}
		}

		if (icon != null) {
			GlStateManager.pushMatrix();
			GlStateManager.translate(position.x, position.y, 0);
			GlStateManager.scale(0.5f, 0.5f, 1f);
			Utils.drawItemStack(icon, 0, 0);
			GlStateManager.popMatrix();

			position.x += 12;
		}

		super.renderLine(line, position, dummy);
	}

	private static final HashMap<String, ItemStack> miningOverlayRoughGems = new HashMap<String, ItemStack>() {};
	private static final HashMap<String, ItemStack> miningOverlayPerfectGems = new HashMap<String, ItemStack>() {};

	private static HashMap<String, ItemStack> miningOverlayCommissionItems;

	private static void setupMiningOverlayCommissionItems() {
		miningOverlayCommissionItems = new HashMap<String, ItemStack>() {
			{
				addItem("Mithril Powder", "INK_SACK-10");
				addItem("Gemstone Powder", "INK_SACK-9");
				addItem("Lucky Raffle", "MINING_RAFFLE_TICKET");
				addItem("Raffle", "MINING_RAFFLE_TICKET");
				addItem("Pickaxe CD", "DIAMOND_PICKAXE");
				addItem("Star Cult", "FALLEN_STAR_HAT");
				addItem("Thyst Slayer", "THYST_MONSTER");
				addItem("Hard Stone Miner", "HARD_STONE");
				addItem("Glacite Walker Slayer", "ENCHANTED_ICE");
				addItem("Goblin Slayer", "GOBLIN_MONSTER");
				addItem("Star Sentry Puncher", "NETHER_STAR");
				addItem("Treasure Hoarder Puncher", "TREASURE_HOARDER_MONSTER");
				addItem("Mines Slayer", "IRON_SWORD");
				addItem("Goblin Raid", "ENCHANTED_GOLD");
				addItem("Goblin Raid Slayer", "ENCHANTED_GOLD");
				addItem("Golden Goblin Slayer", "GOLD_HELMET");
				addItem("2x Mithril Powder Collector", "ENCHANTED_GLOWSTONE_DUST");
				addItem("Automaton Slayer", "AUTOMATON_MONSTER");
				addItem("Sludge Slayer", "SLUDGE_MONSTER");
				addItem("Team Treasurite Member Slayer", "EXECUTIVE_WENDY_MONSTER");
				addItem("Yog Slayer", "YOG_MONSTER");
				addItem("Boss Corleone Slayer", "BOSS_CORLEONE_BOSS");
				addItem("Chest Looter", "CHEST");
				addItem("Titanium", "TITANIUM_ORE");
				addItem("Mithril", "MITHRIL_ORE");
				addItem("Gemstone", "ROCK_GEMSTONE");
				addItem("Glacite", "GLACITE");
				addItem("Forge", "ANVIL");
				addItem("First Event", "FIREWORK");
				addItem("Corpse Looter", "MINERAL_HELMET");
				addItem("Mineshaft Explorer", "STORAGE_MINECART");
				addItem("Scrap Collector", "SUSPICIOUS_SCRAP");
				addItem("Umber Collector", "UMBER");
				addItem("Tungsten Collector", "TUNGSTEN");
				addItem("Glacite Collector", "GLACITE");
			}

			private void addItem(String eventName, String internalName) {
				ItemStack itemStack = new ItemResolutionQuery(NotEnoughUpdates.INSTANCE.manager)
					.withKnownInternalName(internalName).resolveToItemStack();
				if (itemStack == null) {
					showOutdatedRepoNotification(internalName);
					return;
				}
				put(eventName, itemStack.copy());
			}
		};
	}
}
