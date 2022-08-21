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

package io.github.moulberry.notenoughupdates.listener;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.core.util.StringUtils;
import io.github.moulberry.notenoughupdates.events.RepositoryReloadEvent;
import io.github.moulberry.notenoughupdates.profileviewer.GuiProfileViewer;
import io.github.moulberry.notenoughupdates.util.Calculator;
import io.github.moulberry.notenoughupdates.util.Constants;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class ItemTooltipRngListener {
	private final NotEnoughUpdates neu;
	private boolean showSlayerRngFractures = false;
	private boolean pressedShiftLast = false;
	private int currentSelected = 0;
	private boolean pressedArrowLast = false;
	private boolean repoReloadNeeded = true;

	private final Map<String, Integer> dungeonData = new LinkedHashMap<>();
	private final Map<String, LinkedHashMap<String, Integer>> slayerData = new LinkedHashMap<>();

	public ItemTooltipRngListener(NotEnoughUpdates neu) {
		this.neu = neu;
	}

	@SubscribeEvent
	public void slayerRngChance(ItemTooltipEvent event) {
		if (!neu.isOnSkyblock()) return;
		if (event.toolTip == null) return;
		if (!Utils.getOpenChestName().endsWith(" RNG Meter") && !slayerData.containsKey(Utils.getOpenChestName())) return;

		List<String> newToolTip = new ArrayList<>();

		boolean nextLineProgress = false;
		for (String line : event.toolTip) {

			if (line.contains("Odds:")) {
				if (NotEnoughUpdates.INSTANCE.config.tooltipTweaks.rngMeterFractionDisplay) {
					fractionDisplay(newToolTip, line);
					continue;
				}
			}

			if (nextLineProgress || line.contains("Dungeon Score:") || line.contains("Slayer XP:")) {
				if (line.contains("/§d")) {
					String[] split = line.split(Pattern.quote("/§d"));
					String textNeeded = split[1].replace(",", "");
					int needed;
					try {
						needed = Calculator.calculate(textNeeded).intValue();
					} catch (Calculator.CalculatorException e) {
						needed = -1;
					}

					int having;
					split = split[0].split(" ");
					String nextHaving = StringUtils.cleanColour(split[split.length - 1].replace(",", ""));
					try {
						having = Calculator.calculate(nextHaving).intValue();
					} catch (Calculator.CalculatorException e) {
						having = -1;
					}

					if (NotEnoughUpdates.INSTANCE.config.tooltipTweaks.rngMeterRunsNeeded) {
						runsRequired(newToolTip, having, needed, nextLineProgress, event.itemStack);
					}

					if (NotEnoughUpdates.INSTANCE.config.tooltipTweaks.rngMeterProfitPerUnit) {
						if (!NotEnoughUpdates.INSTANCE.config.tooltipTweaks.rngMeterRunsNeeded) {
							String name = Utils.getOpenChestName().contains("Catacombs") ? "Score" : "XP";
							String formatCoinsPer = getFormatCoinsPer(event.itemStack, needed, 1, name);
							if (formatCoinsPer != null) {
								newToolTip.add(line);
								newToolTip.add(formatCoinsPer);
								continue;
							}
						}
					}
				}
				nextLineProgress = false;
			}

			if (line.contains("Progress:")) {
				nextLineProgress = true;
			}
			newToolTip.add(line);
		}

		event.toolTip.clear();
		event.toolTip.addAll(newToolTip);
	}

	private String getFormatCoinsPer(ItemStack stack, int needed, int multiplier, String name) {
		String internalName = neu.manager.getInternalNameForItem(stack);
		double bin = neu.manager.auctionManager.getBazaarOrBin(internalName);
		if (bin < 0) return null;

		double coinsPer = (bin / needed) * multiplier;
		String format = StringUtils.shortNumberFormat(coinsPer);
		return "§7Coins per " + name + ": §6" + format + " coins";
	}

	private void fractionDisplay(List<String> newToolTip, String line) {
		boolean shift = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
		if (!pressedShiftLast && shift) {
			showSlayerRngFractures = !showSlayerRngFractures;
		}
		pressedShiftLast = shift;

		String[] split = line.split(Pattern.quote(" §7("));
		String start = split[0];
		String string = StringUtils.cleanColour(split[1]);
		string = string.replace("%", "").replace(")", "");

		String addText;
		int baseChance;
		if (!string.contains(" ")) {
			baseChance = calculateChance(string);
			String format = GuiProfileViewer.numberFormat.format(baseChance);
			addText = " §7(1/" + format + ")";
		} else {
			split = string.split(" ");
			baseChance = calculateChance(split[0]);
			String base = GuiProfileViewer.numberFormat.format(baseChance);
			String increased = GuiProfileViewer.numberFormat.format(calculateChance(split[1]));
			addText = " §7(§8§m1/" + base + "§r §71/" + increased + ")";
		}

		if (showSlayerRngFractures) {
			newToolTip.add(start + addText);
			newToolTip.add("§8[Press SHIFT to show odds as percentages]");
		} else {
			newToolTip.add(line);
			newToolTip.add("§8[Press SHIFT to show odds as fractures]");
		}
	}

	/**
	 * This adds support for the /neureloadrepo command
	 */
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onRepoReload(RepositoryReloadEvent event) {
		repoReloadNeeded = true;
	}

	public void checkUpdateData() {
		if (repoReloadNeeded) {
			updateRepoData();
		}
	}

	private void updateRepoData() {
		slayerData.clear();
		dungeonData.clear();

		JsonObject leveling = Constants.LEVELING;
		if (!leveling.has("slayer_boss_xp") ||
			!leveling.has("slayer_highest_tier") ||
			!leveling.has("slayer_tier_colors") ||
			!leveling.has("rng_meter_dungeon_score")) {
			return;
		}

		List<Integer> slayerExp = new ArrayList<>();
		for (JsonElement element : leveling.get("slayer_boss_xp").getAsJsonArray()) {
			slayerExp.add(element.getAsInt());
		}

		List<String> slayerColors = new ArrayList<>();
		for (JsonElement element : leveling.get("slayer_tier_colors").getAsJsonArray()) {
			slayerColors.add(element.getAsString());
		}

		for (Map.Entry<String, JsonElement> entry : leveling.get("slayer_highest_tier").getAsJsonObject().entrySet()) {
			String slayerName = entry.getKey();
			int maxTier = entry.getValue().getAsInt();
			LinkedHashMap<String, Integer> singleSlayerData = new LinkedHashMap<>();
			for (int i = 0; i < maxTier; i++) {
				String name = slayerColors.get(i) + "Tier " + (i + 1);
				singleSlayerData.put(name, slayerExp.get(i));
			}
			slayerData.put(slayerName, singleSlayerData);
		}

		for (Map.Entry<String, JsonElement> entry : leveling.get("rng_meter_dungeon_score").getAsJsonObject().entrySet()) {
			String dungeonScore = entry.getKey();
			int score = entry.getValue().getAsInt();
			dungeonData.put(dungeonScore, score);
		}

		repoReloadNeeded = false;
	}

	private void runsRequired(
		List<String> toolTip,
		int having,
		int needed,
		boolean nextLineProgress,
		ItemStack stack
	) {
		checkUpdateData();
		if (repoReloadNeeded) return;

		String openChestName = Utils.getOpenChestName();
		Map<String, Integer> runsData;
		String labelPlural;
		String labelSingular;
		if (openChestName.contains("Catacombs")) {
			runsData = dungeonData;
			labelPlural = "Runs";
			labelSingular = "Run";
		} else { // Slayer
			String[] split = openChestName.split(" ");
			String slayerName = split[0] + " " + split[1];
			runsData = slayerData.get(slayerName);
			labelPlural = "Bosses";
			labelSingular = "Boss";
		}

		handleArrowKeys(runsData);

		if (currentSelected >= runsData.keySet().size()) {
			currentSelected = 0;
		}

		String name = (String) runsData.keySet().toArray()[currentSelected];
		int gainPerRun = runsData.get(name);

		int runsNeeded = needed / gainPerRun;
		int runsHaving = having / gainPerRun;
		String runsNeededFormat = GuiProfileViewer.numberFormat.format(runsNeeded);
		String runsHavingFormat = GuiProfileViewer.numberFormat.format(runsHaving);

		String progressString = null;
		if (nextLineProgress) {
			progressString = toolTip.remove(toolTip.size() - 1);
		}

		toolTip.add("§9Stats for " + name + "\u00a79: [\u00a7l\u00a7m< \u00a79Switch\u00a7l\u27a1\u00a79]");
		toolTip.add(
			"   §7" + labelPlural + " having: §e" + runsHavingFormat + " §7(of §e" + runsNeededFormat + " §7needed)");

		if (NotEnoughUpdates.INSTANCE.config.tooltipTweaks.rngMeterProfitPerUnit) {
			String formatCoinsPer = getFormatCoinsPer(stack, needed, gainPerRun, labelSingular);
			if (formatCoinsPer != null) {
				toolTip.add("   " + formatCoinsPer);
			}
		}

		toolTip.add(" ");
		if (progressString != null) {
			toolTip.add(progressString);
		}
	}

	private void handleArrowKeys(Map<String, Integer> runsData) {
		boolean left = Keyboard.isKeyDown(Keyboard.KEY_LEFT);
		boolean right = Keyboard.isKeyDown(Keyboard.KEY_RIGHT);
		if (!pressedArrowLast && (left || right)) {
			if (left) {
				currentSelected--;
			} else {
				currentSelected++;
			}
			if (currentSelected < 0) currentSelected = 0;
			if (currentSelected >= runsData.size()) currentSelected = runsData.size() - 1;
		}
		pressedArrowLast = left || right;
	}

	private int calculateChance(String string) {
		return (int) (100.0 / Double.parseDouble(string));
	}
}
