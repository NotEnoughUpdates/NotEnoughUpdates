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

package io.github.moulberry.notenoughupdates.overlays;

import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.core.config.Position;
import io.github.moulberry.notenoughupdates.options.NEUConfig;
import io.github.moulberry.notenoughupdates.util.SBInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class PowderGrindingOverlay extends TextTabOverlay {

	public int chestCount = 0;
	public int openedChestCount = 0;
	public int mithrilPowderFound = 0;
	public int gemstonePowderFound = 0;

	public PowderGrindingOverlay(
		Position position,
		Supplier<List<String>> dummyStrings,
		Supplier<TextOverlayStyle> styleSupplier
	) {
		super(position, dummyStrings, styleSupplier);
	}

	@Override
	public void update() {
		overlayStrings = null;
		NEUConfig.HiddenProfileSpecific profileConfig = NotEnoughUpdates.INSTANCE.config.getProfileSpecific();

		if (!NotEnoughUpdates.INSTANCE.config.mining.powderGrindingTrackerEnabled) {
			return;
		}

		// Get commission and forge info even if the overlay isn't going to be rendered since it is used elsewhere
		//thanks to "Pure Genie#7250" for helping with this (makes tita alert and waypoints work without mine overlay)
		String location = SBInfo.getInstance().getLocation();
		if (location == null) return;
		if (location.equals("crystal_hollows")) {

			overlayStrings = new ArrayList<>();
			for (int index : NotEnoughUpdates.INSTANCE.config.mining.powderGrindingTrackerText) {
				switch (index) {
					case 0:
						overlayStrings.add("\u00a73Chests Found: \u00a7a" + chestCount);
						break;
					case 1:
						overlayStrings.add("\u00a73Opened Chests: \u00a7a" + openedChestCount);
						break;
					case 2:
						overlayStrings.add("\u00a73Unopened Chests: \u00a7a" + (chestCount - openedChestCount));
						break;
					case 3:
						overlayStrings.add("\u00a73Mithril Powder Found: \u00a72" + mithrilPowderFound);
						break;
					case 4:
						overlayStrings.add("\u00a73Average Mithril Powder/Chest: \u00a72" + (
							openedChestCount > 0 ?
								mithrilPowderFound / openedChestCount :
								0));
						break;
					case 5:
						overlayStrings.add("\u00a73Gemstone Powder Found: \u00a7d" + gemstonePowderFound);
						break;
					case 6:
						overlayStrings.add("\u00a73Average Gemstone Powder/Chest: \u00a7d" + (
							openedChestCount > 0 ?
								gemstonePowderFound / openedChestCount :
								0));
						break;
				}
			}
		}

		if (overlayStrings != null && overlayStrings.isEmpty()) overlayStrings = null;
	}

	public void message(String message) {
		if (message.equals("You uncovered a treasure chest!")) {
			chestCount++;
		} else if (message.equals("You have successfully picked the lock on this chest!")) {
			openedChestCount++;
		} else {
			boolean mithril = message.endsWith(" Mithril Powder");
			boolean gemstone = message.endsWith(" Gemstone Powder");
			if (!(mithril || gemstone)) return;
			int amount = Integer.parseInt(message.split(" ")[2].replaceAll("\\+", ""));
			if (mithril) mithrilPowderFound += amount;
			else gemstonePowderFound += amount;
		}
	}

}
