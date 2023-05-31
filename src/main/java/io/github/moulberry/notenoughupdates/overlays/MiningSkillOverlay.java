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
import io.github.moulberry.notenoughupdates.util.Utils;
import io.github.moulberry.notenoughupdates.util.XPInformation;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

public class MiningSkillOverlay
	extends TextOverlay { //Im sure there is a much better way to do this besides making another class ¯\_(ツ)_/¯
	private long lastUpdate = -1;
	private int compactLast = -1;
	private int compact = -1;
	private int compactTier = -1;
	private String compactTierAmount = "1";
	private float minedPerSecondLast = 0;
	private float minedPerSecond = 0;
	private final LinkedList<Integer> compactQueue = new LinkedList<>();

	private XPInformation.SkillInfo skillInfo = null;
	private XPInformation.SkillInfo skillInfoLast = null;

	private float lastTotalXp = -1;
	private boolean isMining = false;
	private final LinkedList<Float> xpGainQueue = new LinkedList<>();
	private float xpGainHourLast = -1;
	private float xpGainHour = -1;

	private int xpGainTimer = 0;

	private final String skillType = "Mining";

	public MiningSkillOverlay(
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
	public boolean isEnabled() {
		return NotEnoughUpdates.INSTANCE.config.skillOverlays.miningSkillOverlay;
	}

	@Override
	public void update() {
		if (!isEnabled()) {
			compact = -1;
			overlayStrings = null;
			return;
		}

		lastUpdate = System.currentTimeMillis();
		compactLast = compact;
		xpGainHourLast = xpGainHour;
		compact = -1;

		if (Minecraft.getMinecraft().thePlayer == null) return;

		ItemStack stack = Minecraft.getMinecraft().thePlayer.getHeldItem();
		if (stack != null && stack.hasTagCompound()) {
			NBTTagCompound tag = stack.getTagCompound();

			if (tag.hasKey("ExtraAttributes", 10)) {
				NBTTagCompound ea = tag.getCompoundTag("ExtraAttributes");

				if (ea.hasKey("compact_blocks", 99)) {
					compact = ea.getInteger("compact_blocks");
					compactQueue.add(0, compact);
				}
			}
		}
		if (compact < 100) {
			compactTier = 1;
		} else if (compact < 500) {
			compactTier = 2;
		} else if (compact < 1500) {
			compactTier = 3;
		} else if (compact < 5000) {
			compactTier = 4;
		} else if (compact < 15000) {
			compactTier = 5;
		} else if (compact < 50000) {
			compactTier = 6;
		} else if (compact < 150000) {
			compactTier = 7;
		} else if (compact < 500000) {
			compactTier = 8;
		} else if (compact < 1000000) {
			compactTier = 9;
		} else if (compact > 1000000) {
			compactTier = 10;
		}

		switch (compactTier) {
			case 1:
				compactTierAmount = "100";
				break;
			case 2:
				compactTierAmount = "500";
				break;
			case 3:
				compactTierAmount = "1,500";
				break;
			case 4:
				compactTierAmount = "5,000";
				break;
			case 5:
				compactTierAmount = "15,000";
				break;
			case 6:
				compactTierAmount = "50,000";
				break;
			case 7:
				compactTierAmount = "150,000";
				break;
			case 8:
				compactTierAmount = "500,000";
				break;
			case 9:
				compactTierAmount = "1,000,000";
				break;
			case 10:
				compactTierAmount = "Maxed";
				break;
		}

		String internalname = NotEnoughUpdates.INSTANCE.manager.getInternalNameForItem(stack);

		skillInfoLast = skillInfo;
		skillInfo = XPInformation.getInstance().getSkillInfo(skillType);
		if (skillInfo != null) {
			float totalXp = skillInfo.totalXp;

			if (lastTotalXp > 0) {
				float delta = totalXp - lastTotalXp;

				if (delta > 0 && delta < 1000) {
					xpGainTimer = NotEnoughUpdates.INSTANCE.config.skillOverlays.miningPauseTimer;

					xpGainQueue.add(0, delta);
					while (xpGainQueue.size() > 30) {
						xpGainQueue.removeLast();
					}

					float totalGain = 0;
					for (float f : xpGainQueue) totalGain += f;

					xpGainHour = totalGain * (60 * 60) / xpGainQueue.size();

					isMining = true;
				} else if (xpGainTimer > 0) {
					xpGainTimer--;

					xpGainQueue.add(0, 0f);
					while (xpGainQueue.size() > 30) {
						xpGainQueue.removeLast();
					}

					float totalGain = 0;
					for (float f : xpGainQueue) totalGain += f;

					xpGainHour = totalGain * (60 * 60) / xpGainQueue.size();

					isMining = true;
				} else if (delta <= 0) {
					isMining = false;
				}
			}

			lastTotalXp = totalXp;
		}

		while (compactQueue.size() >= 4) {
			compactQueue.removeLast();
		}

		if (compactQueue.isEmpty()) {
			minedPerSecond = -1;
			minedPerSecondLast = 0;
		} else {
			minedPerSecondLast = minedPerSecond;
			int last = compactQueue.getLast();
			int first = compactQueue.getFirst();

			minedPerSecond = (first - last) / 3f;
		}

		if (compact != -1) {
			overlayStrings = new ArrayList<>();
		} else {
			overlayStrings = null;
		}

	}

	@Override
	public void updateFrequent() {
		super.updateFrequent();

		if (compact < 0) {
			overlayStrings = null;
		} else {
			HashMap<Integer, String> lineMap = new HashMap<>();

			overlayStrings = new ArrayList<>();

			NumberFormat format = NumberFormat.getIntegerInstance();

			if (compact >= 0) {
				int counterInterp = (int) interp(compact, compactLast);

				lineMap.put(
					0,
					EnumChatFormatting.AQUA + "Compact: " + EnumChatFormatting.YELLOW + format.format(counterInterp)
				);
			}

			if (compact >= 0) {
				if (minedPerSecondLast == minedPerSecond && minedPerSecond <= 0) {
					lineMap.put(1, EnumChatFormatting.AQUA + "Blocks/m: " + EnumChatFormatting.YELLOW + "N/A");
				} else {
					float cpsInterp = interp(minedPerSecond, minedPerSecondLast);

					lineMap.put(1, EnumChatFormatting.AQUA + "Blocks/m: " + EnumChatFormatting.YELLOW +
						String.format("%,.2f", cpsInterp * 60));
				}
			}

			if (compactTier <= 9) {
				int counterInterp = (int) interp(compact, compactLast);
				lineMap.put(
					8,
					EnumChatFormatting.AQUA + "Compact Progress: " + EnumChatFormatting.YELLOW + format.format(counterInterp) +
						"/" + compactTierAmount
				);
			}
			if (compactTier == 10) {
				lineMap.put(8, EnumChatFormatting.AQUA + "Compact Progress: " + EnumChatFormatting.RED + compactTierAmount);
			}

			float xpInterp = xpGainHour;
			if (xpGainHourLast == xpGainHour && xpGainHour <= 0) {
				lineMap.put(5, EnumChatFormatting.AQUA + "XP/h: " + EnumChatFormatting.YELLOW + "N/A");
			} else {
				xpInterp = interp(xpGainHour, xpGainHourLast);

				lineMap.put(5, EnumChatFormatting.AQUA + "XP/h: " + EnumChatFormatting.YELLOW +
					format.format(xpInterp) + (isMining ? "" : EnumChatFormatting.RED + " (PAUSED)"));
			}

			if (skillInfo != null && skillInfo.level < 60) {
				StringBuilder levelStr = new StringBuilder(EnumChatFormatting.AQUA + "Mining" + ": "); //yes ik its spelt wrong

				levelStr.append(EnumChatFormatting.YELLOW)
								.append(skillInfo.level)
								.append(EnumChatFormatting.GRAY)
								.append(" [");

				float progress = skillInfo.currentXp / skillInfo.currentXpMax;
				if (skillInfoLast != null && skillInfo.currentXpMax == skillInfoLast.currentXpMax) {
					progress = interp(progress, skillInfoLast.currentXp / skillInfoLast.currentXpMax);
				}

				float lines = 25;
				for (int i = 0; i < lines; i++) {
					if (i / lines < progress) {
						levelStr.append(EnumChatFormatting.YELLOW);
					} else {
						levelStr.append(EnumChatFormatting.DARK_GRAY);
					}
					levelStr.append('|');
				}

				levelStr.append(EnumChatFormatting.GRAY)
								.append("] ")
								.append(EnumChatFormatting.YELLOW)
								.append((int) (progress * 100))
								.append("%");

				int current = (int) skillInfo.currentXp;
				if (skillInfoLast != null && skillInfo.currentXpMax == skillInfoLast.currentXpMax) {
					current = (int) interp(current, skillInfoLast.currentXp);
				}

				int remaining = (int) (skillInfo.currentXpMax - skillInfo.currentXp);
				if (skillInfoLast != null && skillInfo.currentXpMax == skillInfoLast.currentXpMax) {
					remaining = (int) interp(remaining, (int) (skillInfoLast.currentXpMax - skillInfoLast.currentXp));
				}

				lineMap.put(2, levelStr.toString());
				lineMap.put(3, EnumChatFormatting.AQUA + "Current XP: " + EnumChatFormatting.YELLOW + format.format(current));
				if (remaining < 0) {
					lineMap.put(4, EnumChatFormatting.AQUA + "Remaining XP: " + EnumChatFormatting.YELLOW + "MAXED!");
					lineMap.put(7, EnumChatFormatting.AQUA + "ETA: " + EnumChatFormatting.YELLOW + "MAXED!");
				} else {
					lineMap.put(
						4,
						EnumChatFormatting.AQUA + "Remaining XP: " + EnumChatFormatting.YELLOW + format.format(remaining)
					);
					if (xpGainHour < 1000) {
						lineMap.put(7, EnumChatFormatting.AQUA + "ETA: " + EnumChatFormatting.YELLOW + "N/A");
					} else {
						lineMap.put(
							7,
							EnumChatFormatting.AQUA + "ETA: " + EnumChatFormatting.YELLOW +
								Utils.prettyTime((long) (remaining) * 1000 * 60 * 60 / (long) xpInterp)
						);
					}
				}

			}

			if (skillInfo != null && skillInfo.level == 60) {
				int current = (int) skillInfo.currentXp;
				if (skillInfoLast != null && skillInfo.currentXpMax == skillInfoLast.currentXpMax) {
					current = (int) interp(current, skillInfoLast.currentXp);
				}

				lineMap.put(
					2,
					EnumChatFormatting.AQUA + "Mining: " + EnumChatFormatting.YELLOW + "60 " + EnumChatFormatting.RED + "(Maxed)"
				);
				lineMap.put(3, EnumChatFormatting.AQUA + "Current XP: " + EnumChatFormatting.YELLOW + format.format(current));

			}

			float yaw = Minecraft.getMinecraft().thePlayer.rotationYawHead;
			yaw %= 360;
			if (yaw < 0) yaw += 360;
			if (yaw > 180) yaw -= 360;

			lineMap.put(6, EnumChatFormatting.AQUA + "Yaw: " + EnumChatFormatting.YELLOW +
				String.format("%.2f", yaw) + EnumChatFormatting.BOLD + "\u1D52");

			for (int strIndex : NotEnoughUpdates.INSTANCE.config.skillOverlays.miningText) {
				if (lineMap.get(strIndex) != null) {
					overlayStrings.add(lineMap.get(strIndex));
				}
			}
			if (overlayStrings != null && overlayStrings.isEmpty()) overlayStrings = null;
		}
	}
}
