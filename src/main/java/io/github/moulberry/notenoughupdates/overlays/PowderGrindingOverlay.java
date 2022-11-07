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
import io.github.moulberry.notenoughupdates.options.NEUConfig;
import io.github.moulberry.notenoughupdates.util.SBInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class PowderGrindingOverlay extends TextTabOverlay {

	NumberFormat FORMAT = NumberFormat.getIntegerInstance();

	private boolean isMining = false;
	public int chestCount = 0;
	public int openedChestCount = 0;
	public int blocksMined = 0;
	public int lastBlocksMined = 0;
	public int lastCompact = -1;
	public float lastBlocksMinedAverage = 0;
	public int mithrilPowderFound = 0;
	public float lastMithrilPowderFound = 0;
	public float lastMithrilPowderAverage = 0;
	public int gemstonePowderFound = 0;
	public float lastGemstonePowderFound = 0;
	public float lastGemstonePowderAverage = 0;
	public int elapsed = 0;
	public String elapsedString = "";
	public int inactiveFor = 0;
	public float lastMithrilPowderRate = 0;
	public float lastGemstonePowderRate = 0;
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
			if (Minecraft.getMinecraft().thePlayer == null) return;
			lastUpdate = System.currentTimeMillis();

			ItemStack stack = Minecraft.getMinecraft().thePlayer.getHeldItem();
			if (stack != null && stack.hasTagCompound()) {
				NBTTagCompound tag = stack.getTagCompound();
				if (tag.hasKey("ExtraAttributes", 10)) {
					NBTTagCompound ea = tag.getCompoundTag("ExtraAttributes");
					if (ea.hasKey("compact_blocks", 99)) {
						int compact = ea.getInteger("compact_blocks");
						if (compact != lastCompact && lastCompact != -1) {
							blocksMined += compact - lastCompact;
							inactiveFor = 0;
							isMining = true;
							elapsed++;
						} else if (isMining) {
							inactiveFor++;
							elapsed++;
							if (inactiveFor >= NotEnoughUpdates.INSTANCE.config.mining.powderGrindingPauseTimer) isMining = false;
						}
						int displaySeconds = elapsed % 60;
						int displayMinutes = (int) (Math.floor(elapsed / 60f) % 60);
						int displayHours = (int) Math.floor(elapsed / 3600f);
						elapsedString = (displayHours < 10 ? "0" + displayHours : displayHours) + ":" +
							(displayMinutes < 10 ? "0" + displayMinutes : displayMinutes) + ":" +
							(displaySeconds < 10 ? "0" + displaySeconds : displaySeconds);
						if (!isMining) elapsedString += " \u00a7c(PAUSED)";
						lastCompact = compact;
					}
				}
			}

			lastBlocksMined = blocksMined;
			lastBlocksMinedAverage = this.chestCount > 0 ?
				1f * this.blocksMined / this.chestCount :
				this.blocksMined;

			lastMithrilPowderFound = this.mithrilPowderFound;
			lastMithrilPowderAverage = this.openedChestCount > 0 ?
				1f * this.mithrilPowderFound / this.openedChestCount :
				0;
			lastGemstonePowderFound = this.gemstonePowderFound;
			lastGemstonePowderAverage = this.openedChestCount > 0 ?
				1f * this.gemstonePowderFound / this.openedChestCount :
				0;

			lastMithrilPowderRate = this.mithrilPowderFound / (Math.max(1, isMining ? elapsed - 1 : elapsed) / 3600f);
			lastGemstonePowderRate = this.gemstonePowderFound / (Math.max(1, isMining ? elapsed - 1 : elapsed) / 3600f);
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
				switch (index) {
					case 0:
						overlayStrings.add("\u00a73Chests Found: \u00a7a" + FORMAT.format(this.chestCount));
						break;
					case 1:
						overlayStrings.add("\u00a73Opened Chests: \u00a7a" + FORMAT.format(this.openedChestCount));
						break;
					case 2:
						overlayStrings.add("\u00a73Unopened Chests: \u00a7c" + FORMAT.format(this.chestCount - this.openedChestCount));
						break;
					case 3:
						overlayStrings.add("\u00a73Mithril Powder Found: \u00a72" +
							FORMAT.format(interp(this.mithrilPowderFound, lastMithrilPowderFound)));
						break;
					case 4:
						overlayStrings.add("\u00a73Average Mithril Powder/Chest: \u00a72" + FORMAT.format(interp(
							(this.openedChestCount > 0 ?
								1f * this.mithrilPowderFound / this.openedChestCount :
								0), lastMithrilPowderAverage)));
						break;
					case 5:
						overlayStrings.add("\u00a73Gemstone Powder Found: \u00a7d" +
							FORMAT.format(interp(this.gemstonePowderFound, lastGemstonePowderFound)));
						break;
					case 6:
						overlayStrings.add("\u00a73Average Gemstone Powder/Chest: \u00a7d" + FORMAT.format(interp(
							(this.openedChestCount > 0 ?
								1f * this.gemstonePowderFound / this.openedChestCount :
								0), lastGemstonePowderAverage)));
						break;
					case 7:
						overlayStrings.add("\u00a73Time Elapsed: \u00a7a" + elapsedString);
						break;
					case 8:
						overlayStrings.add("\u00a73Blocks Mined: \u00a77" + FORMAT.format(interp(lastBlocksMined, blocksMined)));
						break;
					case 9:
						overlayStrings.add("\u00a73Average Blocks Mined/Chest: \u00a77" + FORMAT.format(interp(
							(this.chestCount > 0 ?
								1f * this.blocksMined / this.chestCount :
								this.blocksMined), lastBlocksMinedAverage)));
						break;
					case 10:
						overlayStrings.add("\u00a73Mithril Powder / Hour: \u00a72" + FORMAT.format(interp(
							this.mithrilPowderFound / (Math.max(1, elapsed) / 3600f),
							lastMithrilPowderRate
						)) + (!isMining ? " \u00a7c(PAUSED)" : ""));
						break;
					case 11:
						overlayStrings.add("\u00a73Gemstone Powder / Hour: \u00a7d" + FORMAT.format(interp(
							this.gemstonePowderFound / (Math.max(1, elapsed) / 3600f),
							lastGemstonePowderRate
						)) + (!isMining ? " \u00a7c(PAUSED)" : ""));
						break;
				}
			}
		}

		if (overlayStrings != null && overlayStrings.isEmpty()) overlayStrings = null;
	}

	public void message(String message) {
		if (message.equals("You uncovered a treasure chest!")) {
			this.chestCount++;
		} else if (message.equals("You have successfully picked the lock on this chest!")) {
			this.openedChestCount++;
		} else {
			boolean mithril = message.endsWith(" Mithril Powder");
			boolean gemstone = message.endsWith(" Gemstone Powder");
			if (!(mithril || gemstone)) return;
			try {
				int amount = Integer.parseInt(message.split(" ")[2].replaceAll("\\+", ""));
				if (mithril) this.mithrilPowderFound += amount;
				else this.gemstonePowderFound += amount;
			} catch (NumberFormatException | IndexOutOfBoundsException e) {
				e.printStackTrace();
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
