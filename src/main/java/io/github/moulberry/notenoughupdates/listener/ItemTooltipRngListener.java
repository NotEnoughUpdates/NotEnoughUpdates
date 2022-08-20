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
import io.github.moulberry.notenoughupdates.profileviewer.GuiProfileViewer;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.util.StringUtils;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;
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
		if (!NotEnoughUpdates.INSTANCE.config.tooltipTweaks.rngMeterFractionDisplay) return;

		List<String> newToolTip = new ArrayList<>();
		int id = 0;
		for (String line : event.toolTip) {
			if (line.contains("Odds:")) {

				boolean shift = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
				if (!pressedShiftLast && shift) {
					showSlayerRngFractures = !showSlayerRngFractures;
				}
				pressedShiftLast = shift;

				if (!showSlayerRngFractures) {
					newToolTip.add(line);
					newToolTip.add("§8[Press SHIFT to show odds as fractures]");
				} else {
					String[] split = line.split(Pattern.quote(" §7("));
					String start = split[0];
					String string = StringUtils.stripControlCodes(split[1]);
					string = string.replace("%", "").replace(")", "");

					if (!string.contains(" ")) {
						String format = calculateChance(string);
						newToolTip.add(id, start + " §7(1/" + format + ")");
					} else {
						split = string.split(" ");
						String base = calculateChance(split[0]);
						String increased = calculateChance(split[1]);

						newToolTip.add(start + " §7(§8§m1/" + base + "§r §71/" + increased + ")");
					}
					newToolTip.add("§8[Press SHIFT to show odds as percentages]");
				}
			} else {
				newToolTip.add(line);
			}
			id++;
		}

		event.toolTip.clear();
		event.toolTip.addAll(newToolTip);
	}

	@NotNull
	private String calculateChance(String string) {
		int chance = (int) (100.0 / Double.parseDouble(string));
		return GuiProfileViewer.numberFormat.format(chance);
	}
}
