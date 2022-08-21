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

import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.core.util.StringUtils;
import io.github.moulberry.notenoughupdates.profileviewer.GuiProfileViewer;
import io.github.moulberry.notenoughupdates.util.Calculator;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class ItemTooltipRngListener {
	private final NotEnoughUpdates neu;
	private boolean showSlayerRngFractures = false;
	private boolean pressedShiftLast = false;

	public ItemTooltipRngListener(NotEnoughUpdates neu) {
		this.neu = neu;
	}

	@SubscribeEvent
	public void slayerRngChance(ItemTooltipEvent event) {
		if (!neu.isOnSkyblock()) return;
		if (event.toolTip == null) return;
		if (!Utils.getOpenChestName().endsWith(" RNG Meter")) return;

		List<String> newToolTip = new ArrayList<>();

		int baseChance = -1;
		boolean nextNeeded = false;
		for (String line : event.toolTip) {
			if (line.contains("Odds:")) {

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

				if (NotEnoughUpdates.INSTANCE.config.tooltipTweaks.rngMeterFractionDisplay) {
					if (showSlayerRngFractures) {
						newToolTip.add(start + addText);
						newToolTip.add("§8[Press SHIFT to show odds as percentages]");
					} else {
						newToolTip.add(line);
						newToolTip.add("§8[Press SHIFT to show odds as fractures]");
					}
					continue;
				}
			}

			if (NotEnoughUpdates.INSTANCE.config.tooltipTweaks.rngMeterProfitPerUnit) {
				if (nextNeeded || line.contains("Dungeon Score:") || line.contains("Slayer XP:")) {
					nextNeeded = false;
					if (line.contains("/§d")) {
						String textNeeded = line.split(Pattern.quote("/§d"))[1].replace(",", "");
						int needed;
						try {
							needed = Calculator.calculate(textNeeded).intValue();
						} catch (Calculator.CalculatorException e) {
							needed = -1;
						}
						String internalName = neu.manager.getInternalNameForItem(event.itemStack);

						double bin = neu.manager.auctionManager.getBazaarOrBin(internalName);
						if (bin > 0) {
							double coinsPer = bin / needed;
							String name = Utils.getOpenChestName().contains("Catacombs") ? "Score" : "XP";
							String format = StringUtils.shortNumberFormat(coinsPer);
							String formatCoinsPer = "§7Coins per " + name + ": §6" + format + " coins";
							newToolTip.add(line);
							newToolTip.add(formatCoinsPer);
							continue;
						}
					}
				}
				if (line.contains("Progress:")) {
					nextNeeded = true;
				}
			}
			newToolTip.add(line);
		}

		event.toolTip.clear();
		event.toolTip.addAll(newToolTip);
	}

	private int calculateChance(String string) {
		int chance = (int) (100.0 / Double.parseDouble(string));
		return chance;
	}
}
