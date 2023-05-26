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
import io.github.moulberry.notenoughupdates.core.config.KeybindHelper;
import io.github.moulberry.notenoughupdates.core.config.Position;
import io.github.moulberry.notenoughupdates.core.util.lerp.LerpUtils;
import io.github.moulberry.notenoughupdates.util.Utils;
import io.github.moulberry.notenoughupdates.util.XPInformation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.PositionedSound;
import net.minecraft.client.audio.SoundCategory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

public class FishingSkillOverlay
	extends TextOverlay { //Im sure there is a much better way to do this besides making another class ¯\_(ツ)_/¯

	private long lastUpdate = -1;
	private long timer = -1;
	private int expertiseLast = -1;
	private int expertise = -1;
	private int expertiseTier = -1;
	private String expertiseTierAmount = "1";
	private float fishedPerSecondLast = 0;
	private float fishedPerSecond = 0;
	private final LinkedList<Integer> expertiseQueue = new LinkedList<>();

	private XPInformation.SkillInfo skillInfo = null;
	private XPInformation.SkillInfo skillInfoLast = null;

	private float lastTotalXp = -1;
	private boolean isFishing = false;
	private final LinkedList<Float> xpGainQueue = new LinkedList<>();
	private float xpGainHourLast = -1;
	private float xpGainHour = -1;

	private int xpGainTimer = 0;

	private final String skillType = "Fishing";

	public FishingSkillOverlay(
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
		// NopoTheGamer named this variable
		return NotEnoughUpdates.INSTANCE.config.skillOverlays.FishingSkillOverlay;
	}

	@Override
	public void update() {
		if (!isEnabled()) {
			expertise = -1;
			overlayStrings = null;
			return;
		}

		lastUpdate = System.currentTimeMillis();
		expertiseLast = expertise;
		xpGainHourLast = xpGainHour;
		expertise = -1;

		if (Minecraft.getMinecraft().thePlayer == null) return;

		ItemStack stack = Minecraft.getMinecraft().thePlayer.getHeldItem();
		if (stack != null && stack.hasTagCompound()) {
			NBTTagCompound tag = stack.getTagCompound();

			if (tag.hasKey("ExtraAttributes", 10)) {
				NBTTagCompound ea = tag.getCompoundTag("ExtraAttributes");

				if (ea.hasKey("expertise_kills", 99)) {
					expertise = ea.getInteger("expertise_kills");
					expertiseQueue.add(0, expertise);
				}
			}
		}

		if (expertise < 50) {
			expertiseTier = 1;
		} else if (expertise < 100) {
			expertiseTier = 2;
		} else if (expertise < 250) {
			expertiseTier = 3;
		} else if (expertise < 500) {
			expertiseTier = 4;
		} else if (expertise < 1000) {
			expertiseTier = 5;
		} else if (expertise < 2500) {
			expertiseTier = 6;
		} else if (expertise < 5500) {
			expertiseTier = 7;
		} else if (expertise < 10000) {
			expertiseTier = 8;
		} else if (expertise < 15000) {
			expertiseTier = 9;
		} else if (expertise > 15000) {
			expertiseTier = 10;
		}

		switch (expertiseTier) {
			case 1:
				expertiseTierAmount = "50";
				break;
			case 2:
				expertiseTierAmount = "100";
				break;
			case 3:
				expertiseTierAmount = "250";
				break;
			case 4:
				expertiseTierAmount = "500";
				break;
			case 5:
				expertiseTierAmount = "1,000";
				break;
			case 6:
				expertiseTierAmount = "2,500";
				break;
			case 7:
				expertiseTierAmount = "5,500";
				break;
			case 8:
				expertiseTierAmount = "10,000";
				break;
			case 9:
				expertiseTierAmount = "15,000";
				break;
			case 10:
				expertiseTierAmount = "Maxed";
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
					xpGainTimer = NotEnoughUpdates.INSTANCE.config.skillOverlays.fishingPauseTimer;

					xpGainQueue.add(0, delta);
					while (xpGainQueue.size() > 30) {
						xpGainQueue.removeLast();
					}

					float totalGain = 0;
					for (float f : xpGainQueue) totalGain += f;

					xpGainHour = totalGain * (60 * 60) / xpGainQueue.size();

					isFishing = true;
				} else if (xpGainTimer > 0) {
					xpGainTimer--;

					xpGainQueue.add(0, 0f);
					while (xpGainQueue.size() > 30) {
						xpGainQueue.removeLast();
					}

					float totalGain = 0;
					for (float f : xpGainQueue) totalGain += f;

					xpGainHour = totalGain * (60 * 60) / xpGainQueue.size();

					isFishing = true;
				} else if (delta <= 0) {
					isFishing = false;
				}
			}

			lastTotalXp = totalXp;
		}

		while (expertiseQueue.size() >= 4) {
			expertiseQueue.removeLast();
		}

		if (expertiseQueue.isEmpty()) {
			fishedPerSecond = -1;
			fishedPerSecondLast = 0;
		} else {
			fishedPerSecondLast = fishedPerSecond;
			int last = expertiseQueue.getLast();
			int first = expertiseQueue.getFirst();

			fishedPerSecond = (first - last) / 3f;
		}

		if (expertise != -1) {
			overlayStrings = new ArrayList<>();
		} else {
			overlayStrings = null;
		}

	}

	@Override
	public void updateFrequent() {
		super.updateFrequent();

		if (expertise < 0) {
			overlayStrings = null;
		} else {
			HashMap<Integer, String> lineMap = new HashMap<>();

			overlayStrings = new ArrayList<>();

			NumberFormat format = NumberFormat.getIntegerInstance();

            /*if(expertise >= 0) {
                int counterInterp = (int)interp(expertise, expertiseLast);

                lineMap.put(0, EnumChatFormatting.AQUA+"Expertise Kills: "+EnumChatFormatting.YELLOW+format.format(counterInterp));
            }*/


            /*if(expertise >= 0) {
                if(fishedPerSecondLast == fishedPerSecond && fishedPerSecond <= 0) {
                    lineMap.put(7, EnumChatFormatting.AQUA+"Catches/m: "+EnumChatFormatting.YELLOW + "N/A");
                } else {
                    //float cpsInterp = interp(fishedPerSecond, fishedPerSecondLast);

                    lineMap.put(7, EnumChatFormatting.AQUA+"Catches/m: "+EnumChatFormatting.YELLOW +
                            fishedPerSecond);
                            //String.format("%.2f", cpsInterp*60));
                }
            }*/

			if (expertiseTier <= 9) {
				int counterInterp = (int) interp(expertise, expertiseLast);
				lineMap.put(
					0,
					EnumChatFormatting.AQUA + "Expertise: " + EnumChatFormatting.YELLOW + format.format(counterInterp) + "/" +
						expertiseTierAmount
				);
			}
			if (expertiseTier == 10) {
				int counterInterp = (int) interp(expertise, expertiseLast);
				lineMap.put(
					0,
					EnumChatFormatting.AQUA + "Expertise: " + EnumChatFormatting.YELLOW + format.format(counterInterp) + " " +
						EnumChatFormatting.RED + expertiseTierAmount
				);
			}

			float xpInterp = xpGainHour;
			if (xpGainHourLast == xpGainHour && xpGainHour <= 0) {
				lineMap.put(4, EnumChatFormatting.AQUA + "XP/h: " + EnumChatFormatting.YELLOW + "N/A");
			} else {
				xpInterp = interp(xpGainHour, xpGainHourLast);

				lineMap.put(4, EnumChatFormatting.AQUA + "XP/h: " + EnumChatFormatting.YELLOW +
					format.format(xpInterp) + (isFishing ? "" : EnumChatFormatting.RED + " (PAUSED)"));
			}

			if (skillInfo != null && skillInfo.level < 50) {
				StringBuilder levelStr = new StringBuilder(EnumChatFormatting.AQUA + skillType + ": ");

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

			if (skillInfo != null && skillInfo.level == 50) {
				int current = (int) skillInfo.currentXp;
				if (skillInfoLast != null && skillInfo.currentXpMax == skillInfoLast.currentXpMax) {
					current = (int) interp(current, skillInfoLast.currentXp);
				}

				lineMap.put(
					1,
					EnumChatFormatting.AQUA + "Fishing: " + EnumChatFormatting.YELLOW + "50 " + EnumChatFormatting.RED + "(Maxed)"
				);
				lineMap.put(2, EnumChatFormatting.AQUA + "Current XP: " + EnumChatFormatting.YELLOW + format.format(current));

			}

            /*float yaw = Minecraft.getMinecraft().thePlayer.rotationYawHead;
            yaw %= 360;
            if(yaw < 0) yaw += 360;
            if(yaw > 180) yaw -= 360;

            lineMap.put(6, EnumChatFormatting.AQUA+"Yaw: "+EnumChatFormatting.YELLOW+
                    String.format("%.2f", yaw)+EnumChatFormatting.BOLD+"\u1D52");*/
			int key = NotEnoughUpdates.INSTANCE.config.skillOverlays.fishKey;

			ISound sound = new PositionedSound(new ResourceLocation("random.orb")) {{
				volume = 50;
				repeat = false;
				repeatDelay = 0;
				attenuationType = ISound.AttenuationType.NONE;
			}};

			int funnyCustomTimer = 1000 * NotEnoughUpdates.INSTANCE.config.skillOverlays.customFishTimer;
			if (KeybindHelper.isKeyPressed(key) && timer != 0 && System.currentTimeMillis() - timer > 1000) {
				timer = 0;
			} else if (KeybindHelper.isKeyPressed(key) && timer == 0) {
				timer = System.currentTimeMillis();
			}
			if (timer >= 1) {
				lineMap.put(
					6,
					EnumChatFormatting.AQUA + "Timer: " + EnumChatFormatting.YELLOW +
						Utils.prettyTime(System.currentTimeMillis() - (timer))
				);
			}
			if (timer <= 0) {
				lineMap.put(6, EnumChatFormatting.AQUA + "Timer: " + EnumChatFormatting.RED + "(Stopped)");
			}
			if (System.currentTimeMillis() - timer > funnyCustomTimer &&
				System.currentTimeMillis() - timer < (funnyCustomTimer + 100) && funnyCustomTimer != 0) {
				float oldLevel = Minecraft.getMinecraft().gameSettings.getSoundLevel(SoundCategory.PLAYERS);
				Minecraft.getMinecraft().gameSettings.setSoundLevel(SoundCategory.PLAYERS, 1);
				Minecraft.getMinecraft().getSoundHandler().playSound(sound);
				Minecraft.getMinecraft().gameSettings.setSoundLevel(SoundCategory.PLAYERS, oldLevel);
			}

			for (int strIndex : NotEnoughUpdates.INSTANCE.config.skillOverlays.fishingText) {
				if (lineMap.get(strIndex) != null) {
					overlayStrings.add(lineMap.get(strIndex));
				}
			}
			if (overlayStrings != null && overlayStrings.isEmpty()) overlayStrings = null;
		}
	}
}
