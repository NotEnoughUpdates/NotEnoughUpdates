/*
 * Copyright (C) 2022-2023 NotEnoughUpdates contributors
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
import io.github.moulberry.notenoughupdates.options.NEUConfig;
import io.github.moulberry.notenoughupdates.util.SBInfo;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.entity.boss.BossStatus;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PowderGrindingOverlay extends TextTabOverlay {

	private final static Pattern POWDER_PATTERN =
		Pattern.compile("You received \\+([0-9]+(?:,\\d+)*) (Mithril|Gemstone) Powder\\.");
	private final static Pattern EVENT_PATTERN = Pattern.compile("PASSIVE EVENT (.+) RUNNING FOR \\d{2}:\\d{2}");

	public int chestCount = 0;
	public int openedChestCount = 0;
	public int mithrilPowderFound = 0;
	public float lastMithrilPowderFound = 0;
	public float lastMithrilPowderAverage = 0;
	public int gemstonePowderFound = 0;
	public float lastGemstonePowderFound = 0;
	public float lastGemstonePowderAverage = 0;
	public MiningEvent miningEvent = MiningEvent.UNKNOWN;
	private long lastUpdate = -1;

	public PowderGrindingOverlay(
		Position position,
		Supplier<List<String>> dummyStrings,
		Supplier<TextOverlayStyle> styleSupplier
	) {
		super(position, dummyStrings, styleSupplier);
	}

	public enum MiningEvent {
		UNKNOWN,
		DOUBLE_POWDER,
		BETTER_TOGETHER,
		GONE_WITH_THE_WIND;
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
	public boolean isEnabled() {
		return NotEnoughUpdates.INSTANCE.config.mining.powderGrindingTrackerEnabled;
	}

	@Override
	public void update() {
		if (isEnabled()) {
			lastUpdate = System.currentTimeMillis();
			lastMithrilPowderFound = this.mithrilPowderFound;
			lastMithrilPowderAverage = this.openedChestCount > 0 ?
				1f * this.mithrilPowderFound / this.openedChestCount :
				0;
			lastGemstonePowderFound = this.gemstonePowderFound;
			lastGemstonePowderAverage = this.openedChestCount > 0 ?
				1f * this.gemstonePowderFound / this.openedChestCount :
				0;

			Matcher matcher = EVENT_PATTERN.matcher(BossStatus.bossName == null ? "" : Utils.cleanColour(BossStatus.bossName));
			if (matcher.matches()) {
				switch (matcher.group(1)) {
					case "2X POWDER":
						miningEvent = MiningEvent.DOUBLE_POWDER;
						break;
					case "BETTER TOGETHER":
						miningEvent = MiningEvent.BETTER_TOGETHER;
						break;
					case "GONE WITH THE WIND":
						miningEvent = MiningEvent.GONE_WITH_THE_WIND;
						break;
					default:
						miningEvent = MiningEvent.UNKNOWN;
				}
			}
		} else overlayStrings = null;
	}

	@Override
	public void updateFrequent() {
		overlayStrings = null;
		if (!NotEnoughUpdates.INSTANCE.config.mining.powderGrindingTrackerEnabled) return;

		String location = SBInfo.getInstance().getLocation();
		if (location == null) return;
		if (location.equals("crystal_hollows")) {

			overlayStrings = new ArrayList<>();
			for (int index : NotEnoughUpdates.INSTANCE.config.mining.powderGrindingTrackerText) {
				NumberFormat format = NumberFormat.getIntegerInstance();
				switch (index) {
					case 0:
						overlayStrings.add("\u00a73Chests Found: \u00a7a" + format.format(this.chestCount));
						break;
					case 1:
						overlayStrings.add("\u00a73Opened Chests: \u00a7a" + format.format(this.openedChestCount));
						break;
					case 2:
						overlayStrings.add(
							"\u00a73Unopened Chests: \u00a7c" + format.format(this.chestCount - this.openedChestCount));
						break;
					case 3:
						overlayStrings.add("\u00a73Mithril Powder Found: \u00a72" +
							format.format(interp(this.mithrilPowderFound, lastMithrilPowderFound)));
						break;
					case 4:
						overlayStrings.add("\u00a73Average Mithril Powder/Chest: \u00a72" + format.format(interp(
							(this.openedChestCount > 0 ?
								1f * this.mithrilPowderFound / this.openedChestCount :
								0), lastMithrilPowderAverage)));
						break;
					case 5:
						overlayStrings.add("\u00a73Gemstone Powder Found: \u00a7d" +
							format.format(interp(this.gemstonePowderFound, lastGemstonePowderFound)));
						break;
					case 6:
						overlayStrings.add("\u00a73Average Gemstone Powder/Chest: \u00a7d" + format.format(interp(
							(this.openedChestCount > 0 ?
								1f * this.gemstonePowderFound / this.openedChestCount :
								0), lastGemstonePowderAverage)));
						break;
				}
			}
		}

		if (overlayStrings != null && overlayStrings.isEmpty()) overlayStrings = null;
	}

	public void onMessage(String message) {
		if (message.equals("You uncovered a treasure chest!")) {
			this.chestCount++;
		} else if (message.equals("You have successfully picked the lock on this chest!")) {
			this.openedChestCount++;
		} else {
			Matcher matcher = POWDER_PATTERN.matcher(message);
			if (matcher.matches()) {
				String rawNumber = matcher.group(1).replace(",", "");
				try {
					int amount = Integer.parseInt(rawNumber);
					String type = matcher.group(2);
					if (type.equals("Mithril")) {
						this.mithrilPowderFound += miningEvent == MiningEvent.DOUBLE_POWDER ? amount * 2 : amount;
					} else if (type.equals("Gemstone")) {
						this.gemstonePowderFound += miningEvent == MiningEvent.DOUBLE_POWDER ? amount * 2 : amount;
					}
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void load() {
		NEUConfig.HiddenProfileSpecific profileSpecific = NotEnoughUpdates.INSTANCE.config.getProfileSpecific();
		if (profileSpecific == null) return;
		this.chestCount = profileSpecific.chestCount;
		this.openedChestCount = profileSpecific.openedChestCount;
		this.mithrilPowderFound = profileSpecific.mithrilPowderFound;
		this.gemstonePowderFound = profileSpecific.gemstonePowderFound;
	}

	public void save() {
		NEUConfig.HiddenProfileSpecific profileSpecific = NotEnoughUpdates.INSTANCE.config.getProfileSpecific();
		if (profileSpecific == null) return;
		profileSpecific.chestCount = this.chestCount;
		profileSpecific.openedChestCount = this.openedChestCount;
		profileSpecific.mithrilPowderFound = this.mithrilPowderFound;
		profileSpecific.gemstonePowderFound = this.gemstonePowderFound;
	}

	public void reset() {
		this.chestCount = 0;
		this.openedChestCount = 0;
		this.mithrilPowderFound = 0;
		this.gemstonePowderFound = 0;
	}

}
