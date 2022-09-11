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
import io.github.moulberry.notenoughupdates.core.util.lerp.LerpUtils;
import io.github.moulberry.notenoughupdates.util.SBInfo;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class PowderGrindingOverlay extends TextTabOverlay {

	public int chestCount = 0;
	public int openedChestCount = 0;
	public int mithrilPowderFound = 0;
	public float lastMithrilPowderFound = 0;
	public float lastMithrilPowderAverage = 0;
	public int gemstonePowderFound = 0;
	public float lastGemstonePowderFound = 0;
	public float lastGemstonePowderAverage = 0;
	private long lastUpdate = -1;

	public PowderGrindingOverlay(
		Position position,
		Supplier<List<String>> dummyStrings,
		Supplier<TextOverlayStyle> styleSupplier
	) {
		super(position, dummyStrings, styleSupplier);
	}

	private float interp(float now, float last) {
		float interp = now;
		if (last >= 0 && last != now) {
			float factor = (System.currentTimeMillis() - lastUpdate) / 1000f;
			factor = LerpUtils.clampZeroOne(factor);
			interp = last + (now - last) * factor;
		}
		return interp;
	}

	@Override
	public void update() {
		if (NotEnoughUpdates.INSTANCE.config.mining.powderGrindingTrackerEnabled) {
			lastUpdate = System.currentTimeMillis();
			lastMithrilPowderFound = mithrilPowderFound;
			lastMithrilPowderAverage = openedChestCount > 0 ?
				1f * mithrilPowderFound / openedChestCount :
				0;
			lastGemstonePowderFound = gemstonePowderFound;
			lastGemstonePowderAverage = openedChestCount > 0 ?
				1f * gemstonePowderFound / openedChestCount :
				0;
		} else overlayStrings = null;
	}

	@Override
	public void updateFrequent() {
		overlayStrings = null;

		if (!NotEnoughUpdates.INSTANCE.config.mining.powderGrindingTrackerEnabled) {
			return;
		}

		String location = SBInfo.getInstance().getLocation();
		if (location == null) return;
		if (location.equals("crystal_hollows")) {

			overlayStrings = new ArrayList<>();
			for (int index : NotEnoughUpdates.INSTANCE.config.mining.powderGrindingTrackerText) {
				NumberFormat format = NumberFormat.getIntegerInstance();
				switch (index) {
					case 0:
						overlayStrings.add("\u00a73Chests Found: \u00a7a" + format.format(chestCount));
						break;
					case 1:
						overlayStrings.add("\u00a73Opened Chests: \u00a7a" + format.format(openedChestCount));
						break;
					case 2:
						overlayStrings.add("\u00a73Unopened Chests: \u00a7c" + format.format(chestCount - openedChestCount));
						break;
					case 3:
						overlayStrings.add("\u00a73Mithril Powder Found: \u00a72" +
							format.format(interp(mithrilPowderFound, lastMithrilPowderFound)));
						break;
					case 4:
						overlayStrings.add("\u00a73Average Mithril Powder/Chest: \u00a72" + format.format(interp(
							(openedChestCount > 0 ?
								1f * mithrilPowderFound / openedChestCount :
								0), lastMithrilPowderAverage)));
						break;
					case 5:
						overlayStrings.add("\u00a73Gemstone Powder Found: \u00a7d" +
							format.format(interp(gemstonePowderFound, lastGemstonePowderFound)));
						break;
					case 6:
						overlayStrings.add("\u00a73Average Gemstone Powder/Chest: \u00a7d" + format.format(interp(
							(openedChestCount > 0 ?
								1f * gemstonePowderFound / openedChestCount :
								0), lastGemstonePowderAverage)));
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
			try {
				int amount = Integer.parseInt(message.split(" ")[2].replaceAll("\\+", ""));
				if (mithril) mithrilPowderFound += amount;
				else gemstonePowderFound += amount;
			} catch (NumberFormatException | IndexOutOfBoundsException e) {
				e.printStackTrace();
			}
		}
	}

}
