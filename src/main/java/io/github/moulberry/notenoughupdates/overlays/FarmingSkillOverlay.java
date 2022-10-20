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

import com.google.gson.JsonObject;
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

public class FarmingSkillOverlay extends TextOverlay {
	private long lastUpdate = -1;
	private int counterLast = -1;
	private int counter = -1;
	private int cultivatingLast = -1;
	private int cultivating = -1;
	private int cultivatingTier = -1;
	private String cultivatingTierAmount = "1";
	private int Farming = -1;
	private int Alch = -1;
	private int Foraging = -1;
	private double Coins = -1;
	private float cropsPerSecondLast = 0;
	private float cropsPerSecond = 0;
	private final LinkedList<Integer> counterQueue = new LinkedList<>();

	private XPInformation.SkillInfo skillInfo = null;
	private XPInformation.SkillInfo skillInfoLast = null;

	private float lastTotalXp = -1;
	private boolean isFarming = false;
	private final LinkedList<Float> xpGainQueue = new LinkedList<>();
	private float xpGainHourLast = -1;
	private float xpGainHour = -1;

	private int xpGainTimer = 0;

	private String skillType = "Farming";

	public FarmingSkillOverlay(
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
		if (!NotEnoughUpdates.INSTANCE.config.skillOverlays.farmingOverlay) {
			counter = -1;
			overlayStrings = null;
			return;
		}

		lastUpdate = System.currentTimeMillis();
		counterLast = counter;
		cultivatingLast = cultivating;
		xpGainHourLast = xpGainHour;
		counter = -1;

		if (Minecraft.getMinecraft().thePlayer == null) return;

		ItemStack stack = Minecraft.getMinecraft().thePlayer.getHeldItem();
		if (stack != null && stack.hasTagCompound()) {
			NBTTagCompound tag = stack.getTagCompound();

			if (tag.hasKey("ExtraAttributes", 10)) {
				NBTTagCompound ea = tag.getCompoundTag("ExtraAttributes");

				if (ea.hasKey("mined_crops", 99)) {
					counter = ea.getInteger("mined_crops");
					cultivating = ea.getInteger("farmed_cultivating");
					counterQueue.add(0, counter);
				} else if (ea.hasKey("farmed_cultivating", 99)) {
					counter = ea.getInteger("farmed_cultivating");
					cultivating = ea.getInteger("farmed_cultivating");
					counterQueue.add(0, counter);
				}
			}
		}

		if (cultivating < 1000) {
			cultivatingTier = 1;
		} else if (cultivating < 5000) {
			cultivatingTier = 2;
		} else if (cultivating < 25000) {
			cultivatingTier = 3;
		} else if (cultivating < 100000) {
			cultivatingTier = 4;
		} else if (cultivating < 300000) {
			cultivatingTier = 5;
		} else if (cultivating < 1500000) {
			cultivatingTier = 6;
		} else if (cultivating < 5000000) {
			cultivatingTier = 7;
		} else if (cultivating < 20000000) {
			cultivatingTier = 8;
		} else if (cultivating < 100000000) {
			cultivatingTier = 9;
		} else if (cultivating > 100000000) {
			cultivatingTier = 10;
		}

		switch (cultivatingTier) {
			case 1:
				cultivatingTierAmount = "1,000";
				break;
			case 2:
				cultivatingTierAmount = "5,000";
				break;
			case 3:
				cultivatingTierAmount = "25,000";
				break;
			case 4:
				cultivatingTierAmount = "100,000";
				break;
			case 5:
				cultivatingTierAmount = "300,000";
				break;
			case 6:
				cultivatingTierAmount = "1,500,000";
				break;
			case 7:
				cultivatingTierAmount = "5,000,000";
				break;
			case 8:
				cultivatingTierAmount = "20,000,000";
				break;
			case 9:
				cultivatingTierAmount = "100,000,000";
				break;
			case 10:
				cultivatingTierAmount = "Maxed";
				break;
		}

		String internalname = NotEnoughUpdates.INSTANCE.manager.getInternalNameForItem(stack);
		if (internalname != null && internalname.startsWith("THEORETICAL_HOE_WARTS")) {
			skillType = "Alchemy";
			Farming = 0;
			Alch = 1;
			Foraging = 0;
		} else if (internalname != null && internalname.startsWith("TREECAPITATOR_AXE") ||
			(internalname != null && internalname.startsWith("JUNGLE_AXE"))) {
			skillType = "Foraging";
			Farming = 0;
			Alch = 0;
			Foraging = 1;
		} else {
			skillType = "Farming";
			Farming = 1;
			Alch = 0;
			Foraging = 0;
		}

		if (!NotEnoughUpdates.INSTANCE.config.skillOverlays.useBZPrice || internalname != null && (internalname.equals(
			"TREECAPITATOR_AXE")) || internalname != null && (internalname.equals("JUNGLE_AXE"))) {
			if ((internalname != null && internalname.equals("COCO_CHOPPER"))) {
				Coins = 3;
			} else if (internalname != null && internalname.startsWith("THEORETICAL_HOE_POTATO") ||
				(internalname != null && internalname.startsWith("THEORETICAL_HOE_CARROT")) ||
				(internalname != null && internalname.equals("CACTUS_KNIFE")) ||
				(internalname != null && internalname.startsWith("THEORETICAL_HOE_WHEAT"))) {
				Coins = 1;
			} else if (internalname != null && internalname.startsWith("THEORETICAL_HOE_CANE")
				|| (internalname != null && internalname.equals("TREECAPITATOR_AXE"))
				|| (internalname != null && internalname.startsWith("THEORETICAL_HOE_WARTS"))
				|| (internalname != null && internalname.equals("JUNGLE_AXE"))) {
				Coins = 2;
			} else if ((internalname != null && internalname.equals("PUMPKIN_DICER")) ||
				(internalname != null && internalname.equals("FUNGI_CUTTER"))) {
				Coins = 4;
			} else if ((internalname != null && internalname.equals("MELON_DICER"))) {
				Coins = 0.5;
			} else {
				Coins = 0;
			}
		}
		if (NotEnoughUpdates.INSTANCE.config.skillOverlays.useBZPrice) {
			if (internalname != null && internalname.startsWith("THEORETICAL_HOE_WARTS")) {
				JsonObject crop = NotEnoughUpdates.INSTANCE.manager.auctionManager.getBazaarInfo("ENCHANTED_NETHER_STALK");
				if (crop != null) {
					if (crop.has("curr_sell")) {
						Coins = crop.get("curr_sell").getAsFloat() / 160;
					}
				}
			} else if (internalname != null && internalname.startsWith("THEORETICAL_HOE_POTATO")) {
				JsonObject crop = NotEnoughUpdates.INSTANCE.manager.auctionManager.getBazaarInfo("ENCHANTED_POTATO");
				if (crop != null) {
					if (crop.has("curr_sell")) {
						Coins = crop.get("curr_sell").getAsFloat() / 160;
					}
				}
			} else if (internalname != null && internalname.startsWith("THEORETICAL_HOE_CARROT")) {
				JsonObject crop = NotEnoughUpdates.INSTANCE.manager.auctionManager.getBazaarInfo("ENCHANTED_CARROT");
				if (crop != null) {
					if (crop.has("curr_sell")) {
						Coins = crop.get("curr_sell").getAsFloat() / 160;
					}
				}
			} else if (internalname != null && internalname.startsWith("THEORETICAL_HOE_CANE")) {
				JsonObject crop = NotEnoughUpdates.INSTANCE.manager.auctionManager.getBazaarInfo("ENCHANTED_SUGAR");
				if (crop != null) {
					if (crop.has("curr_sell")) {
						Coins = crop.get("curr_sell").getAsFloat() / 160;
					}
				}
			} else if (internalname != null && internalname.startsWith("PUMPKIN_DICER")) {
				JsonObject crop = NotEnoughUpdates.INSTANCE.manager.auctionManager.getBazaarInfo("ENCHANTED_PUMPKIN");
				if (crop != null) {
					if (crop.has("curr_sell")) {
						Coins = crop.get("curr_sell").getAsFloat() / 160;
					}
				}
			} else if (internalname != null && internalname.startsWith("FUNGI_CUTTER")) {
				JsonObject red = NotEnoughUpdates.INSTANCE.manager.auctionManager.getBazaarInfo("ENCHANTED_RED_MUSHROOM");
				JsonObject brown = NotEnoughUpdates.INSTANCE.manager.auctionManager.getBazaarInfo("ENCHANTED_BROWN_MUSHROOM");
				if (red != null && brown != null) {
					if (red.has("curr_sell") && (brown.has("curr_sell"))) {
						double crop = (red.get("curr_sell").getAsFloat() + red.get("curr_sell").getAsFloat()) / 2;
						Coins = crop / 160;
					}
				}
			} else if (internalname != null && internalname.startsWith("MELON_DICER")) {
				JsonObject crop = NotEnoughUpdates.INSTANCE.manager.auctionManager.getBazaarInfo("ENCHANTED_MELON");
				if (crop != null) {
					if (crop.has("curr_sell")) {
						Coins = crop.get("curr_sell").getAsFloat() / 160;
					}
				}
			} else if (internalname != null && internalname.startsWith("THEORETICAL_HOE_WHEAT")) {
				JsonObject crop = NotEnoughUpdates.INSTANCE.manager.auctionManager.getBazaarInfo("ENCHANTED_HAY_BLOCK");
				if (crop != null) {
					if (crop.has("curr_sell")) {
						Coins = crop.get("curr_sell").getAsFloat() / 1296;
					}
				}
			} else if (internalname != null && internalname.startsWith("CACTUS_KNIFE")) {
				JsonObject crop = NotEnoughUpdates.INSTANCE.manager.auctionManager.getBazaarInfo("ENCHANTED_CACTUS_GREEN");
				if (crop != null) {
					if (crop.has("curr_sell")) {
						Coins = crop.get("curr_sell").getAsFloat() / 160;
					}
				}
			}
		}
		skillInfoLast = skillInfo;
		skillInfo = XPInformation.getInstance().getSkillInfo(skillType);
		if (skillInfo != null) {
			float totalXp = skillInfo.totalXp;

			if (lastTotalXp > 0) {
				float delta = totalXp - lastTotalXp;

				if (delta > 0 && delta < 1000) {

					xpGainTimer = NotEnoughUpdates.INSTANCE.config.skillOverlays.farmingPauseTimer;

					xpGainQueue.add(0, delta);
					while (xpGainQueue.size() > 30) {
						xpGainQueue.removeLast();
					}

					float totalGain = 0;
					for (float f : xpGainQueue) totalGain += f;

					xpGainHour = totalGain * (60 * 60) / xpGainQueue.size();

					isFarming = true;
				} else if (xpGainTimer > 0) {
					xpGainTimer--;

					xpGainQueue.add(0, 0f);
					while (xpGainQueue.size() > 30) {
						xpGainQueue.removeLast();
					}

					float totalGain = 0;
					for (float f : xpGainQueue) totalGain += f;

					xpGainHour = totalGain * (60 * 60) / xpGainQueue.size();

					isFarming = true;
				} else if (delta <= 0) {
					isFarming = false;
				}
			}

			lastTotalXp = totalXp;
		}

		while (counterQueue.size() >= 4) {
			counterQueue.removeLast();
		}

		if (counterQueue.isEmpty()) {
			cropsPerSecond = -1;
			cropsPerSecondLast = 0;
		} else {
			cropsPerSecondLast = cropsPerSecond;
			int last = counterQueue.getLast();
			int first = counterQueue.getFirst();

			cropsPerSecond = (first - last) / 3f;
		}

		if (counter != -1) {
			overlayStrings = new ArrayList<>();
		} else {
			overlayStrings = null;
		}

	}

	@Override
	public void updateFrequent() {
		super.updateFrequent();

		if (counter < 0) {
			overlayStrings = null;
		} else {
			HashMap<Integer, String> lineMap = new HashMap<>();

			overlayStrings = new ArrayList<>();

			NumberFormat format = NumberFormat.getIntegerInstance();

			if (counter >= 0 && cultivating != counter) {
				int counterInterp = (int) interp(counter, counterLast);

				lineMap.put(
					0,
					EnumChatFormatting.AQUA + "Counter: " + EnumChatFormatting.YELLOW + format.format(counterInterp)
				);
			}

			if (counter >= 0) {
				if (cropsPerSecondLast == cropsPerSecond && cropsPerSecond <= 0) {
					lineMap.put(
						1,
						EnumChatFormatting.AQUA + (Foraging == 1 ? "Logs/m: " : "Crops/m: ") + EnumChatFormatting.YELLOW + "N/A"
					);
				} else {
					float cpsInterp = interp(cropsPerSecond, cropsPerSecondLast);

					lineMap.put(
						1,
						EnumChatFormatting.AQUA + (Foraging == 1 ? "Logs/m: " : "Crops/m: ") + EnumChatFormatting.YELLOW +
							String.format("%,.2f", cpsInterp * 60)
					);
				}
			}

			if (counter >= 0 && Coins > 0) {
				if (cropsPerSecondLast == cropsPerSecond && cropsPerSecond <= 0) {
					lineMap.put(10, EnumChatFormatting.AQUA + "Coins/m: " + EnumChatFormatting.YELLOW + "N/A");
				} else {
					float cpsInterp = interp(cropsPerSecond, cropsPerSecondLast);

					lineMap.put(10, EnumChatFormatting.AQUA + "Coins/m: " + EnumChatFormatting.YELLOW +
						String.format("%,.2f", (cpsInterp * 60) * Coins));
				}
			}

			if (cultivatingTier <= 9 && cultivating > 0) {
				int counterInterp = (int) interp(cultivating, cultivatingLast);
				lineMap.put(
					9,
					EnumChatFormatting.AQUA + "Cultivating: " + EnumChatFormatting.YELLOW + format.format(counterInterp) + "/" +
						cultivatingTierAmount
				);
			}
			if (cultivatingTier == 10) {
				int counterInterp = (int) interp(cultivating, cultivatingLast);
				lineMap.put(
					9,
					EnumChatFormatting.AQUA + "Cultivating: " + EnumChatFormatting.YELLOW + format.format(counterInterp)
				);
			}

			float xpInterp = xpGainHour;
			if (xpGainHourLast == xpGainHour && xpGainHour <= 0) {
				lineMap.put(5, EnumChatFormatting.AQUA + "XP/h: " + EnumChatFormatting.YELLOW + "N/A");
			} else {
				xpInterp = interp(xpGainHour, xpGainHourLast);

				lineMap.put(5, EnumChatFormatting.AQUA + "XP/h: " + EnumChatFormatting.YELLOW +
					format.format(xpInterp) + (isFarming ? "" : EnumChatFormatting.RED + " (PAUSED)"));
			}

			if (skillInfo != null && skillInfo.level < 60) {
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

			if (skillInfo != null && skillInfo.level == 60 || Alch == 1 && skillInfo != null && skillInfo.level == 50) {
				int current = (int) skillInfo.currentXp;
				if (skillInfoLast != null && skillInfo.currentXpMax == skillInfoLast.currentXpMax) {
					current = (int) interp(current, skillInfoLast.currentXp);
				}

				if (Alch == 0) {
					lineMap.put(
						2,
						EnumChatFormatting.AQUA + "Farming: " + EnumChatFormatting.YELLOW + "60 " + EnumChatFormatting.RED +
							"(Maxed)"
					);
				} else if (Foraging == 1) {
					lineMap.put(
						2,
						EnumChatFormatting.AQUA + "Foraging: " + EnumChatFormatting.YELLOW + "50 " + EnumChatFormatting.RED +
							"(Maxed)"
					);
				} else {
					lineMap.put(
						2,
						EnumChatFormatting.AQUA + "Alch: " + EnumChatFormatting.YELLOW + "50 " + EnumChatFormatting.RED + "(Maxed)"
					);
				}
				lineMap.put(3, EnumChatFormatting.AQUA + "Current XP: " + EnumChatFormatting.YELLOW + format.format(current));

			}

			float yaw = Minecraft.getMinecraft().thePlayer.rotationYawHead;
			float pitch = Minecraft.getMinecraft().thePlayer.rotationPitch;
			yaw %= 360;
			if (yaw < 0) yaw += 360;
			if (yaw > 180) yaw -= 360;
			pitch %= 360;
			if (pitch < 0) pitch += 360;
			if (pitch > 180) pitch -= 360;

			lineMap.put(6, EnumChatFormatting.AQUA + "Yaw: " + EnumChatFormatting.YELLOW +
				String.format("%.2f", yaw) + EnumChatFormatting.BOLD + "\u1D52");

			lineMap.put(8, EnumChatFormatting.AQUA + "Pitch: " + EnumChatFormatting.YELLOW +
				String.format("%.2f", pitch) + EnumChatFormatting.BOLD + "\u1D52");

			for (int strIndex : NotEnoughUpdates.INSTANCE.config.skillOverlays.farmingText) {
				if (lineMap.get(strIndex) != null) {
					overlayStrings.add(lineMap.get(strIndex));
				}
			}
			if (overlayStrings != null && overlayStrings.isEmpty()) overlayStrings = null;
		}
	}
}
