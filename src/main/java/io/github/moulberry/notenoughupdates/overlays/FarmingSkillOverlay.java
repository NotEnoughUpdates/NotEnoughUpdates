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
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.EnumChatFormatting;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
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
	private int Farming = 1;
	private int Alch = 0;
	private int Foraging = 0;
	private double Coins = -1;
	private float cropsPerSecondLast = 0;
	private float cropsPerSecond = 0;
	private final LinkedList<Integer> counterQueue = new LinkedList<>();
	private int jacobPredictionLast = -1;
	private int jacobPrediction = -1;

	private XPInformation.SkillInfo skillInfo = null;
	private XPInformation.SkillInfo skillInfoLast = null;

	private float lastTotalXp = -1;
	private boolean isFarming = false;
	private final LinkedList<Float> xpGainQueue = new LinkedList<>();
	private float xpGainHourLast = -1;
	private float xpGainHour = -1;

	private final int ENCH_SIZE = 160;
	private final int ENCH_BLOCK_SIZE = 1296;
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

	private double getCoinsBz(String enchCropName, int numItemsForEnch) {
		JsonObject crop = NotEnoughUpdates.INSTANCE.manager.auctionManager.getBazaarInfo(enchCropName);
		if (crop != null && crop.has("curr_sell")) {
			return crop.get("curr_sell").getAsFloat() / numItemsForEnch;
		}
		return 0;
	}

	private void gatherJacobData() {
		if (System.currentTimeMillis() % 3600000 >= 900000 &&
			System.currentTimeMillis() % 3600000 <= 2100000) { //Check to see if there is an active Jacob's contest
			int timeLeftInContest = (20 * 60) - ((int) ((System.currentTimeMillis() % 3600000 - 900000) / 1000));
			try {
				Scoreboard scoreboard = Minecraft.getMinecraft().thePlayer.getWorldScoreboard();
				ScoreObjective sidebarObjective = scoreboard.getObjectiveInDisplaySlot(1);
				List<Score> scores = new ArrayList<>(scoreboard.getSortedScores(sidebarObjective));
				String currentCrop = "N/A";
				int cropsFarmed = -1;
				for (int i = scores.size() - 1; i >= 0; i--) {
					Score score = scores.get(i);
					ScorePlayerTeam scoreplayerteam1 = scoreboard.getPlayersTeam(score.getPlayerName());
					String line = ScorePlayerTeam.formatPlayerName(scoreplayerteam1, score.getPlayerName());
					line = Utils.cleanColour(line);
					if (line.contains("Collected") || line.contains("BRONZE") || line.contains("SILVER") ||
						line.contains("GOLD")) {
						String l = line.replaceAll("[^A-Za-z0-9() ]", "");
						cropsFarmed = Integer.parseInt(l.substring(l.lastIndexOf(" ") + 1).replace(",", ""));
					}
				}
				jacobPrediction = (int) (cropsFarmed + (cropsPerSecond * timeLeftInContest));

			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			jacobPrediction = -1;
			jacobPredictionLast = -1;
		}
	}

	//This is a list of the last X cropsPerSeconds to try and calm down the fluctuation for crops/min (they will be averaged)
	//Needed due to farming fortune causing inconsistent amounts of crops each block break
	private static ArrayList<Float> cropsOverLastXSeconds = new ArrayList<>();

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
			cultivatingTierAmount = "1,000";
		} else if (cultivating < 5000) {
			cultivatingTier = 2;
			cultivatingTierAmount = "5,000";
		} else if (cultivating < 25000) {
			cultivatingTier = 3;
			cultivatingTierAmount = "25,000";
		} else if (cultivating < 100000) {
			cultivatingTier = 4;
			cultivatingTierAmount = "100,000";
		} else if (cultivating < 300000) {
			cultivatingTier = 5;
			cultivatingTierAmount = "300,000";
		} else if (cultivating < 1500000) {
			cultivatingTier = 6;
			cultivatingTierAmount = "1,500,000";
		} else if (cultivating < 5000000) {
			cultivatingTier = 7;
			cultivatingTierAmount = "5,000,000";
		} else if (cultivating < 20000000) {
			cultivatingTier = 8;
			cultivatingTierAmount = "20,000,000";
		} else if (cultivating < 100000000) {
			cultivatingTier = 9;
			cultivatingTierAmount = "100,000,000";
		} else if (cultivating > 100000000) {
			cultivatingTier = 10;
			cultivatingTierAmount = "Maxed";
		}

		String internalname = NotEnoughUpdates.INSTANCE.manager.getInternalNameForItem(stack);
		if (internalname != null) {

			//Set default skilltype to Farming and get BZprice config value
			boolean useBZPrice = NotEnoughUpdates.INSTANCE.config.skillOverlays.useBZPrice;
			skillType = "Farming";
			Farming = 1;
			Alch = 0;
			Foraging = 0;

			//WARTS
			if (internalname.startsWith("THEORETICAL_HOE_WARTS")) {
				skillType = "Alchemy";
				Farming = 0;
				Alch = 1;
				Foraging = 0;
				Coins = useBZPrice ? getCoinsBz("ENCHANTED_NETHER_STALK", ENCH_SIZE) : 2;

				//WOOD
			} else if (internalname.equals("TREECAPITATOR_AXE") || internalname.equalsIgnoreCase("JUNGLE_AXE")) {
				skillType = "Foraging";
				Farming = 0;
				Alch = 0;
				Foraging = 1;
				Coins = 2;

				//COCOA
			} else if (internalname.equals("COCO_CHOPPER")) {
				Coins = useBZPrice ? getCoinsBz("ENCHANTED_COCOA", ENCH_SIZE) : 3;

				//CACTUS
			} else if (internalname.equals("CACTUS_KNIFE")) {
				Coins = useBZPrice ? getCoinsBz("ENCHANTED_CACTUS_GREEN", ENCH_SIZE) : 2;

				//CANE
			} else if (internalname.startsWith("THEORETICAL_HOE_CANE")) {
				Coins = useBZPrice ? getCoinsBz("ENCHANTED_SUGAR", ENCH_SIZE) : 2;

				//CARROT
			} else if (internalname.startsWith("THEORETICAL_HOE_CARROT")) {
				Coins = useBZPrice ? getCoinsBz("ENCHANTED_CARROT", ENCH_SIZE) : 1;

				//POTATO
			} else if (internalname.startsWith("THEORETICAL_HOE_POTATO")) {
				Coins = useBZPrice ? getCoinsBz("ENCHANTED_POTATO", ENCH_SIZE) : 1;

				//MUSHROOM
			} else if (internalname.equals("FUNGI_CUTTER")) {
				Coins = useBZPrice ? ((getCoinsBz("ENCHANTED_RED_MUSHROOM", ENCH_SIZE) +
					getCoinsBz("ENCHANTED_BROWN_MUSHROOM", ENCH_SIZE)) / 2) : 4;

				//PUMPKIN
			} else if (internalname.startsWith("PUMPKIN_DICER")) {
				Coins = useBZPrice ? getCoinsBz("ENCHANTED_PUMPKIN", ENCH_SIZE) : 4;

				//MELON
			} else if (internalname.startsWith("MELON_DICER")) {
				Coins = useBZPrice ? getCoinsBz("ENCHANTED_MELON", ENCH_SIZE) : 0.5;

				//WHEAT
			} else if (internalname.startsWith("THEORETICAL_HOE_WHEAT")) {
				Coins = useBZPrice ? getCoinsBz("ENCHANTED_HAY_BLOCK", ENCH_BLOCK_SIZE) : 1;

			} else {
				Coins = 0;
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

			//This is a list of the last X cropsPerSeconds to try and calm down the fluctuation for crops/min (they will be averaged)
			//Needed due to farming fortune causing inconsistent amounts of crops each block break
			//Making this while in case somehow it goes over X+1
			while (cropsOverLastXSeconds.size() > 60) {
				cropsOverLastXSeconds.remove(0);
			}
			if ((first - last) / 3f != 0) {
				cropsOverLastXSeconds.add((first - last) / 3f);
			} else {
				if (cropsPerSecondLast == 0) {
					//This is to prevent bleeding from one crop to the next (or if you stop and then start again at a different pace)
					//It removes 12 per tick because otherwise it would take 60s to go to N/A (now it only takes 5s)
					int i = 12;
					while (i > 0) {
						i--;
						if (cropsOverLastXSeconds.size() > 0) {
							cropsOverLastXSeconds.remove(0);
						} else {
							break;
						}
					}
				}
			}

			ArrayList<Float> temp = new ArrayList<>(cropsOverLastXSeconds);
			if (cropsOverLastXSeconds.size() >= 3) {
				temp.remove(Collections.min(temp));
			}
			if (cropsOverLastXSeconds.size() >= 6) {
				temp.remove(Collections.min(temp));
				temp.remove(Collections.max(temp));
			}
			if (cropsOverLastXSeconds.size() >= 10) {
				temp.remove(Collections.max(temp));
			}

			float cropsOverLastXSecondsTotal = 0;
			for (Float crops : temp) {
				cropsOverLastXSecondsTotal += crops;
			}
			//To prevent 0/0
			cropsPerSecond =
				temp.size() != 0 && cropsOverLastXSecondsTotal != 0 ? cropsOverLastXSecondsTotal / temp.size() : 0;
		}

		if (counter != -1) {
			overlayStrings = new ArrayList<>();
		} else {
			overlayStrings = null;
		}
		gatherJacobData();
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

			//Stands for 'Crops Per Hour Or Per Minute'
			String crphopm = NotEnoughUpdates.INSTANCE.config.skillOverlays.cropsPerHour ? "h" : "m";
			int cropMultiplier = NotEnoughUpdates.INSTANCE.config.skillOverlays.cropsPerHour ? 60 : 1;
			int cropDecimals = NotEnoughUpdates.INSTANCE.config.skillOverlays.cropsPerHour ? 0 : 2;

			//Stands for 'Coins Per Hour Or Per Minute'
			String cophopm = NotEnoughUpdates.INSTANCE.config.skillOverlays.coinsPerHour ? "h" : "m";
			int coinMultiplier = NotEnoughUpdates.INSTANCE.config.skillOverlays.coinsPerHour ? 60 : 1;
			int coinDecimals = NotEnoughUpdates.INSTANCE.config.skillOverlays.coinsPerHour ? 0 : 2;

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
						EnumChatFormatting.AQUA + (Foraging == 1 ? "Logs/" + crphopm + ": " : "Crops/" + crphopm + ": ") +
							EnumChatFormatting.YELLOW + "N/A"
					);
				} else {
					float cpsInterp = interp(cropsPerSecond, cropsPerSecondLast);

					lineMap.put(
						1,
						EnumChatFormatting.AQUA + (Foraging == 1 ? "Logs/" + crphopm + ": " : "Crops/" + crphopm + ": ") +
							EnumChatFormatting.YELLOW +
							String.format("%,." + cropDecimals + "f", cpsInterp * 60 * cropMultiplier)
					);
				}
			}

			if (counter >= 0 && Coins > 0) {
				if (cropsPerSecondLast == cropsPerSecond && cropsPerSecond <= 0) {
					lineMap.put(10, EnumChatFormatting.AQUA + "Coins/" + cophopm + ": " + EnumChatFormatting.YELLOW + "N/A");
				} else {
					float cpsInterp = interp(cropsPerSecond, cropsPerSecondLast);
					lineMap.put(10, EnumChatFormatting.AQUA + "Coins/" + cophopm + ": " + EnumChatFormatting.YELLOW +
						String.format("%,." + coinDecimals + "f", (cpsInterp * 60) * Coins * coinMultiplier));
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

			if (System.currentTimeMillis() % 3600000 >= 900000 &&
				System.currentTimeMillis() % 3600000 <= 2100000) { //Check to see if there is an active Jacob's contest
				if (jacobPredictionLast == jacobPrediction && jacobPrediction <= 0) {
					lineMap.put(11, EnumChatFormatting.AQUA + "Contest Estimate: " + EnumChatFormatting.YELLOW + "N/A");
				} else {
					float predInterp = interp(jacobPrediction, jacobPredictionLast);
					lineMap.put(
						11,
						EnumChatFormatting.AQUA + "Contest Estimate: " + EnumChatFormatting.YELLOW +
							String.format("%,.0f", predInterp)
					);
				}
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
