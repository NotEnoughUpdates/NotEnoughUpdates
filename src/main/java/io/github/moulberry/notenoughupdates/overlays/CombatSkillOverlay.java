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

public class CombatSkillOverlay
	extends TextOverlay { //Im sure there is a much better way to do this besides making another class ¯\_(ツ)_/¯
	private long lastUpdate = -1;
	private int killLast = -1;
	private int kill = -1;
	private int championTier = -1;
	private String championTierAmount = "1";
	private int championXp = -1;
	private int championXpLast = -1;
	private final LinkedList<Integer> killQueue = new LinkedList<>();

	private XPInformation.SkillInfo skillInfo = null;
	private XPInformation.SkillInfo skillInfoLast = null;

	private float lastTotalXp = -1;
	private boolean isKilling = false;
	private final LinkedList<Float> xpGainQueue = new LinkedList<>();
	private float xpGainHourLast = -1;
	private float xpGainHour = -1;

	private int xpGainTimer = 0;

	private final String skillType = "Combat";

	public CombatSkillOverlay(
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
		return NotEnoughUpdates.INSTANCE.config.skillOverlays.combatSkillOverlay;
	}

	@Override
	public void update() {
		if (!isEnabled()) {
			kill = -1;
			championXp = -1;
			overlayStrings = null;
			return;
		}

		lastUpdate = System.currentTimeMillis();
		killLast = kill;
		championXpLast = championXp;
		xpGainHourLast = xpGainHour;
		kill = -1;
		championXp = -1;

		if (Minecraft.getMinecraft().thePlayer == null) return;

		ItemStack stack = Minecraft.getMinecraft().thePlayer.getHeldItem();
		if (stack != null && stack.hasTagCompound()) {
			NBTTagCompound tag = stack.getTagCompound();

			if (tag.hasKey("ExtraAttributes", 10)) {
				NBTTagCompound ea = tag.getCompoundTag("ExtraAttributes");

				if (ea.hasKey("stats_book", 99)) {
					kill = ea.getInteger("stats_book");
					killQueue.add(0, kill);
				}
				if (ea.hasKey("champion_combat_xp", 99)) {
					championXp = (int) ea.getDouble("champion_combat_xp");
				}
			}
		}

		if (championXp < 50000) {
			championTier = 1;
		} else if (championXp < 100000) {
			championTier = 2;
		} else if (championXp < 250000) {
			championTier = 3;
		} else if (championXp < 500000) {
			championTier = 4;
		} else if (championXp < 1000000) {
			championTier = 5;
		} else if (championXp < 1500000) {
			championTier = 6;
		} else if (championXp < 2000000) {
			championTier = 7;
		} else if (championXp < 2500000) {
			championTier = 8;
		} else if (championXp < 3000000) {
			championTier = 9;
		} else if (championXp > 3000000) {
			championTier = 10;
		}

		switch (championTier) {
			case 1:
				championTierAmount = "50,000";
				break;
			case 2:
				championTierAmount = "100,000";
				break;
			case 3:
				championTierAmount = "250,000";
				break;
			case 4:
				championTierAmount = "500,000";
				break;
			case 5:
				championTierAmount = "1,000,000";
				break;
			case 6:
				championTierAmount = "1,500,000";
				break;
			case 7:
				championTierAmount = "2,000,000";
				break;
			case 8:
				championTierAmount = "2,500,000";
				break;
			case 9:
				championTierAmount = "3,000,000";
				break;
			case 10:
				championTierAmount = "Maxed";
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
					xpGainTimer = NotEnoughUpdates.INSTANCE.config.skillOverlays.combatPauseTimer;

					xpGainQueue.add(0, delta);
					while (xpGainQueue.size() > 30) {
						xpGainQueue.removeLast();
					}

					float totalGain = 0;
					for (float f : xpGainQueue) totalGain += f;

					xpGainHour = totalGain * (60 * 60) / xpGainQueue.size();

					isKilling = true;
				} else if (xpGainTimer > 0) {
					xpGainTimer--;

					xpGainQueue.add(0, 0f);
					while (xpGainQueue.size() > 30) {
						xpGainQueue.removeLast();
					}

					float totalGain = 0;
					for (float f : xpGainQueue) totalGain += f;

					xpGainHour = totalGain * (60 * 60) / xpGainQueue.size();

					isKilling = true;
				} else if (delta <= 0) {
					isKilling = false;
				}
			}

			lastTotalXp = totalXp;
		}

		while (killQueue.size() >= 4) {
			killQueue.removeLast();
		}

		if (kill != -1 || championXp != -1) {
			overlayStrings = new ArrayList<>();
		} else {
			overlayStrings = null;
		}

	}

	@Override
	public void updateFrequent() {
		super.updateFrequent();

		if ((kill < 0 && championXp < 0) && !NotEnoughUpdates.INSTANCE.config.skillOverlays.alwaysShowCombatOverlay) {
			overlayStrings = null;
		} else {
			HashMap<Integer, String> lineMap = new HashMap<>();

			overlayStrings = new ArrayList<>();

			NumberFormat format = NumberFormat.getIntegerInstance();

			if (kill >= 0) {
				int counterInterp = (int) interp(kill, killLast);

				lineMap.put(0, EnumChatFormatting.AQUA + "Kills: " + EnumChatFormatting.YELLOW + format.format(counterInterp));
			}

			if (championTier <= 9 && championXp >= 0) {
				int counterInterp = (int) interp(championXp, championXpLast);
				lineMap.put(
					6,
					EnumChatFormatting.AQUA + "Champion: " + EnumChatFormatting.YELLOW + format.format(counterInterp) + "/" +
						championTierAmount
				);
			}
			if (championTier == 10) {
				int counterInterp = (int) interp(championXp, championXpLast);
				lineMap.put(
					6,
					EnumChatFormatting.AQUA + "Champion: " + EnumChatFormatting.YELLOW + format.format(counterInterp) + " " +
						EnumChatFormatting.RED + championTierAmount
				);
			}

			float xpInterp = xpGainHour;
			if (xpGainHourLast == xpGainHour && xpGainHour <= 0) {
				lineMap.put(4, EnumChatFormatting.AQUA + "XP/h: " + EnumChatFormatting.YELLOW + "N/A");
			} else {
				xpInterp = interp(xpGainHour, xpGainHourLast);

				lineMap.put(4, EnumChatFormatting.AQUA + "XP/h: " + EnumChatFormatting.YELLOW +
					format.format(xpInterp) + (isKilling ? "" : EnumChatFormatting.RED + " (PAUSED)"));
			}

			if (skillInfo != null && skillInfo.level < 60) {
				StringBuilder levelStr = new StringBuilder(EnumChatFormatting.AQUA + "Combat" + ": ");

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

				lineMap.put(1, levelStr.toString());
				lineMap.put(2, EnumChatFormatting.AQUA + "Current XP: " + EnumChatFormatting.YELLOW + format.format(current));
				if (remaining < 0) {
					lineMap.put(3, EnumChatFormatting.AQUA + "Remaining XP: " + EnumChatFormatting.YELLOW + "MAXED!");
					lineMap.put(5, EnumChatFormatting.AQUA + "ETA: " + EnumChatFormatting.YELLOW + "MAXED!");
				} else {
					lineMap.put(
						3,
						EnumChatFormatting.AQUA + "Remaining XP: " + EnumChatFormatting.YELLOW + format.format(remaining)
					);
					if (xpGainHour < 1000) {
						lineMap.put(5, EnumChatFormatting.AQUA + "ETA: " + EnumChatFormatting.YELLOW + "N/A");
					} else {
						lineMap.put(
							5,
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
					1,
					EnumChatFormatting.AQUA + "Combat: " + EnumChatFormatting.YELLOW + "60 " + EnumChatFormatting.RED + "(Maxed)"
				);
				lineMap.put(2, EnumChatFormatting.AQUA + "Current XP: " + EnumChatFormatting.YELLOW + format.format(current));

			}

			for (int strIndex : NotEnoughUpdates.INSTANCE.config.skillOverlays.combatText) {
				if (lineMap.get(strIndex) != null) {
					overlayStrings.add(lineMap.get(strIndex));
				}
			}
			if (overlayStrings != null && overlayStrings.isEmpty()) overlayStrings = null;
		}
	}
}
