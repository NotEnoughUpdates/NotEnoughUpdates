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

import com.google.gson.JsonObject;
import io.github.moulberry.notenoughupdates.NEUManager;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.core.config.Position;
import io.github.moulberry.notenoughupdates.core.util.lerp.LerpUtils;
import io.github.moulberry.notenoughupdates.util.ItemResolutionQuery;
import io.github.moulberry.notenoughupdates.util.SidebarUtil;
import io.github.moulberry.notenoughupdates.util.Utils;
import io.github.moulberry.notenoughupdates.util.XPInformation;
import io.github.moulberry.notenoughupdates.util.hypixelapi.HypixelItemAPI;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

public class FarmingSkillOverlay extends TextOverlay {
	private static final NumberFormat format = NumberFormat.getIntegerInstance();
	private final HashMap<Integer, String> lineMap = new HashMap<>();
	private long lastUpdate = -1;
	private int counterLast = -1;
	private int counter = -1;
	private int cultivatingLast = -1;
	private int cultivating = -1;
	private int cultivatingTier = -1;
	private String cultivatingTierAmount = "1";
	private int foraging = 0;
	private double coins = -1;
	private float cropsPerSecondLast = 0;
	private float cropsPerSecond = 0;
	private final LinkedList<Integer> counterQueue = new LinkedList<>();
	private String lastItemHeld = "null";
	private int jacobPredictionLast = -1;
	private int jacobPrediction = -1;
	private boolean inJacobContest = false;

	private XPInformation.SkillInfo skillInfo = null;
	private XPInformation.SkillInfo skillInfoLast = null;

	private float lastTotalXp = -1;
	private boolean isFarming = false;
	private final LinkedList<Float> xpGainQueue = new LinkedList<>();
	private float xpGainHourLast = -1;
	private float xpGainHour = -1;

	private int xpGainTimer = 0;

	public static final int CPS_MAX_FRAME_SIZE = 120;
	/**
	 * 6
	 * Stores the values of the crop counter. Values can be accessed using the {@link #cropsPerSecondCursor}
	 */
	//+2 is to prevent calculation issues
	private final int[] cropsPerSecondValues = new int[CPS_MAX_FRAME_SIZE + 2];
	/**
	 * The theoretical call interval of {@link #update()} is 1 second,
	 * but in reality it can deviate by one tick, or 50ms,
	 * which means we have to save time stamps of the values in order to prevent up to 5% (50ms) incorrectness.
	 */
	private final long[] cropsPerSecondTimeStamps = new long[CPS_MAX_FRAME_SIZE + 2];
	private int cropsPerSecondCursor = -1;

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
		inJacobContest = false;
		if (isJacobTime()) {
			int timeLeftInContest = (20 * 60) - ((int) ((System.currentTimeMillis() % 3600000 - 900000) / 1000));

			int cropsFarmed = -1;
			for (String line : SidebarUtil.readSidebarLines()) {
				if (line.contains("Collected") || line.contains("BRONZE") || line.contains("SILVER") ||
					line.contains("GOLD")) {
					inJacobContest = true;
					String l = line.replaceAll("[^A-Za-z0-9() ]", "");
					cropsFarmed = Integer.parseInt(l.substring(l.lastIndexOf(" ") + 1).replace(",", ""));
				}
				jacobPrediction = (int) (cropsFarmed + (cropsPerSecond * timeLeftInContest));
			}

		} else {
			jacobPrediction = -1;
			jacobPredictionLast = -1;
		}
	}

	// Check if there is an active Jacob's contest
	private static boolean isJacobTime() {
		long now = System.currentTimeMillis();
		return now % 3600000 >= 900000 && now % 3600000 <= 2100000;
	}

	//This is a list of the last X cropsPerSeconds to try and calm down the fluctuation for crops/min (they will be averaged)
	//Needed due to farming fortune causing inconsistent amounts of crops each block break
	private static ArrayList<Float> cropsOverLastXSeconds = new ArrayList<>();

	@Override
	public boolean isEnabled() {
		return NotEnoughUpdates.INSTANCE.config.skillOverlays.farmingOverlay;
	}

	private enum CropType {
		WHEAT("THEORETICAL_HOE_WHEAT", "ENCHANTED_HAY_BLOCK", 1296),
		NETHER_WART("THEORETICAL_HOE_WARTS", "ENCHANTED_NETHER_STALK", 160),
		SUGAR_CANE("THEORETICAL_HOE_CANE", "ENCHANTED_SUGAR", 160),
		CARROT("THEORETICAL_HOE_CARROT", "ENCHANTED_CARROT", 160),
		POTATO("THEORETICAL_HOE_POTATO", "ENCHANTED_POTATO", 160),
		COCOA_BEANS("COCO_CHOPPER", "ENCHANTED_COCOA", 160),
		PUMPKIN("PUMPKIN_DICER", "ENCHANTED_PUMPKIN", 160),
		MELON("MELON_DICER", "ENCHANTED_MELON", 160),
		CACTUS("CACTUS_KNIFE", "ENCHANTED_CACTUS_GREEN", 160),
		;

		private final String toolName;
		private final String item;
		private final int enchSize;

		CropType(String toolName, String item, int enchSize) {
			this.toolName = toolName;
			this.item = item;
			this.enchSize = enchSize;
		}

	}

	@Override
	public void update() {
		if (!isEnabled()) {
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

		updateCultivating(stack);

		String internalName =
			new ItemResolutionQuery(NotEnoughUpdates.INSTANCE.manager).withItemStack(stack).resolveInternalName();

		if (internalName != null) {
			updateSkillType(internalName);
		}

		updateSkillInfo();

		while (counterQueue.size() > 3) {
			counterQueue.removeLast();
		}

		if (counterQueue.isEmpty()) {
			cropsPerSecond = -1;
			cropsPerSecondLast = 0;
		} else {
			updateCropsPerSecond();
		}

		if (counter != -1) {
			overlayStrings = new ArrayList<>();
		} else {
			overlayStrings = null;
		}
		gatherJacobData();
	}

	private void updateCropsPerSecond() {
		cropsPerSecondLast = cropsPerSecond;
		int last = counterQueue.getLast();
		int first = counterQueue.getFirst();

		//This is a list of the last X cropsPerSeconds to try and calm down the fluctuation for crops/min (they will be averaged)
		//Needed due to farming fortune causing inconsistent amounts of crops each block break
		//Making this while in case somehow it goes over X+1
		while (cropsOverLastXSeconds.size() > 60) {
			cropsOverLastXSeconds.remove(0);
		}
		if ((first - last) / 2f != 0) {
			cropsOverLastXSeconds.add((first - last) / 2f);
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

		if (!lastItemHeld.equals(NEUManager.getUUIDForItem(Minecraft.getMinecraft().thePlayer.getHeldItem()) == null
			? "null"
			: NEUManager.getUUIDForItem(Minecraft.getMinecraft().thePlayer.getHeldItem()))) {
			lastItemHeld = NEUManager.getUUIDForItem(Minecraft.getMinecraft().thePlayer.getHeldItem()) == null
				? "null"
				: NEUManager.getUUIDForItem(Minecraft.getMinecraft().thePlayer.getHeldItem());
			cropsOverLastXSeconds.clear();
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

	private void updateSkillType(String internalName) {
		//Set default skillType to Farming and get BZ price config value
		skillType = "Farming";
		foraging = 0;

		//WOOD
		boolean useBZPrice = NotEnoughUpdates.INSTANCE.config.skillOverlays.useBZPrice;
		if (internalName.equals("TREECAPITATOR_AXE") || internalName.equalsIgnoreCase("JUNGLE_AXE")) {
			skillType = "Foraging";
			foraging = 1;
			coins = 2;

			//MUSHROOM
		} else if (internalName.equals("FUNGI_CUTTER")) {

			if (useBZPrice) {
				coins = (getCoinsBz("ENCHANTED_RED_MUSHROOM", 160) +
					getCoinsBz("ENCHANTED_BROWN_MUSHROOM", 160)) / 2;
			} else {
				Double red = HypixelItemAPI.getNPCSellPrice("ENCHANTED_RED_MUSHROOM");
				Double brown = HypixelItemAPI.getNPCSellPrice("ENCHANTED_BROWN_MUSHROOM");
				if (red == null || brown == null) {
					coins = 0;
				} else {
					coins = (red * 160 + brown * 160) / 2;
				}
			}

		} else {
			// EVERYTHING ELSE
			coins = 0;
			for (CropType crop : CropType.values()) {
				if (internalName.startsWith(crop.toolName)) {
					Double npcSellPrice = HypixelItemAPI.getNPCSellPrice(crop.item);
					if (npcSellPrice == null) {
						npcSellPrice = 0.0;
					}
					coins = useBZPrice ? getCoinsBz(crop.item, crop.enchSize) : npcSellPrice / crop.enchSize;
				}
			}
		}

	}

	private void updateSkillInfo() {
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
	}

	private void updateCultivating(ItemStack stack) {
		if (stack != null && stack.hasTagCompound()) {
			NBTTagCompound tag = stack.getTagCompound();

			if (tag.hasKey("ExtraAttributes", 10)) {
				NBTTagCompound ea = tag.getCompoundTag("ExtraAttributes");

				if (ea.hasKey("mined_crops", 99)) {
					counter = ea.getInteger("mined_crops");
					cultivating = ea.getInteger("farmed_cultivating");
					counterQueue.add(0, counter);
					cropsPerSecondTimeStamps[++cropsPerSecondCursor % CPS_MAX_FRAME_SIZE] =
						System.currentTimeMillis();
					cropsPerSecondValues[cropsPerSecondCursor % CPS_MAX_FRAME_SIZE] = counter;
				} else if (ea.hasKey("farmed_cultivating", 99)) {
					counter = ea.getInteger("farmed_cultivating");
					cultivating = ea.getInteger("farmed_cultivating");
					counterQueue.add(0, counter);
					cropsPerSecondTimeStamps[++cropsPerSecondCursor % CPS_MAX_FRAME_SIZE] =
						System.currentTimeMillis();
					cropsPerSecondValues[cropsPerSecondCursor % CPS_MAX_FRAME_SIZE] = counter;
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
	}

	@Override
	public void updateFrequent() {
		super.updateFrequent();

		if (counter < 0) {
			overlayStrings = null;
		} else {
			lineMap.clear();
			overlayStrings = new ArrayList<>();

			if (cultivating != counter) {
				renderCounter();
			}

			if (counter >= 0) {
				renderCrops();
				renderCropsPerSecond();
			}

			renderCoins();

			renderCultivating();

			renderJacob();

			renderLevelAndXP();

			renderYawPitch();

			for (int strIndex : NotEnoughUpdates.INSTANCE.config.skillOverlays.farmingText) {
				if (lineMap.get(strIndex) != null) {
					overlayStrings.add(lineMap.get(strIndex));
				}
			}
			if (overlayStrings != null && overlayStrings.isEmpty()) overlayStrings = null;
		}
	}

	private void renderCounter() {
		int counterInterp = (int) interp(counter, counterLast);
		lineMap.put(
			0,
			EnumChatFormatting.AQUA + "Counter: " + EnumChatFormatting.YELLOW + format.format(counterInterp)
		);
	}

	private void renderCrops() {
		String cropsTimeSuffix = NotEnoughUpdates.INSTANCE.config.skillOverlays.cropsPerHour ? "h" : "m";
		int cropMultiplier = NotEnoughUpdates.INSTANCE.config.skillOverlays.cropsPerHour ? 60 : 1;
		int cropDecimals = NotEnoughUpdates.INSTANCE.config.skillOverlays.cropsPerHour ? 0 : 2;
		if (cropsPerSecondLast == cropsPerSecond && cropsPerSecond <= 0) {
			lineMap.put(
				1,
				EnumChatFormatting.AQUA +
					(foraging == 1 ? "Logs/" + cropsTimeSuffix + ": " : "Crops/" + cropsTimeSuffix + ": ") +
					EnumChatFormatting.YELLOW + "N/A"
			);
		} else {
			float cpsInterp = interp(cropsPerSecond, cropsPerSecondLast);

			lineMap.put(
				1,
				EnumChatFormatting.AQUA +
					(foraging == 1 ? "Logs/" + cropsTimeSuffix + ": " : "Crops/" + cropsTimeSuffix + ": ") +
					EnumChatFormatting.YELLOW +
					String.format("%,." + cropDecimals + "f", cpsInterp * 60 * cropMultiplier)
			);
		}
	}

	private void renderCoins() {
		String coinsTimeSuffix = NotEnoughUpdates.INSTANCE.config.skillOverlays.coinsPerHour ? "h" : "m";
		int coinMultiplier = NotEnoughUpdates.INSTANCE.config.skillOverlays.coinsPerHour ? 60 : 1;
		int coinDecimals = NotEnoughUpdates.INSTANCE.config.skillOverlays.coinsPerHour ? 0 : 2;
		if (counter >= 0 && coins > 0) {
			if (cropsPerSecondLast == cropsPerSecond && cropsPerSecond <= 0) {
				lineMap.put(
					10,
					EnumChatFormatting.AQUA + "Coins/" + coinsTimeSuffix + ": " + EnumChatFormatting.YELLOW + "N/A"
				);
			} else {
				float cpsInterp = interp(cropsPerSecond, cropsPerSecondLast);
				lineMap.put(10, EnumChatFormatting.AQUA + "Coins/" + coinsTimeSuffix + ": " + EnumChatFormatting.YELLOW +
					String.format("%,." + coinDecimals + "f", (cpsInterp * 60) * coins * coinMultiplier));
			}
		}
	}

	private void renderCultivating() {
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
	}

	private void renderJacob() {
		if (isJacobTime() && inJacobContest) {
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
	}

	private void renderLevelAndXP() {
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

		if (skillInfo != null && skillInfo.level == 60) {
			int current = (int) skillInfo.currentXp;
			if (skillInfoLast != null && skillInfo.currentXpMax == skillInfoLast.currentXpMax) {
				current = (int) interp(current, skillInfoLast.currentXp);
			}

			if (foraging == 0) {
				lineMap.put(
					2,
					EnumChatFormatting.AQUA + "Farming: " + EnumChatFormatting.YELLOW + "60 " + EnumChatFormatting.RED +
						"(Maxed)"
				);
			} else {
				lineMap.put(
					2,
					EnumChatFormatting.AQUA + "Foraging: " + EnumChatFormatting.YELLOW + "50 " + EnumChatFormatting.RED +
						"(Maxed)"
				);
			}
			lineMap.put(3, EnumChatFormatting.AQUA + "Current XP: " + EnumChatFormatting.YELLOW + format.format(current));

		}
	}

	private void renderYawPitch() {
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
	}

	/**
	 * Finds the average crops farmed during the configured time frame or since the player has started farming.
	 * This value is interpolated with the average of one second ago and rendered to the overlay.
	 */
	private void renderCropsPerSecond() {
		int current = cropsPerSecondValues[cropsPerSecondCursor % CPS_MAX_FRAME_SIZE];
		int prev = cropsPerSecondValues[Math.max(cropsPerSecondCursor - 1, 0) % CPS_MAX_FRAME_SIZE];
		int timeFrame = NotEnoughUpdates.INSTANCE.config.skillOverlays.farmingCropsPerSecondTimeFrame;

		//The searchIndex serves to find the start of the player farming.
		//This makes it so that even if the timeframe is set high,
		// the initial average will be the average since the player starts farming instead of the full timeframe.
		int searchIndex = Math.max(cropsPerSecondCursor - timeFrame, 0) %
			CPS_MAX_FRAME_SIZE;
		while (cropsPerSecondValues[searchIndex] == cropsPerSecondValues[Math.max(searchIndex - 1, 0)] && searchIndex != cropsPerSecondCursor) {
			searchIndex++;
			searchIndex %= CPS_MAX_FRAME_SIZE;
		}

		float cropsPerSecond = current - cropsPerSecondValues[searchIndex];
		cropsPerSecond = interp(cropsPerSecond, prev - cropsPerSecondValues[Math.max(searchIndex - 1, 0)]);

		float timePassed =
			cropsPerSecondTimeStamps[cropsPerSecondCursor % CPS_MAX_FRAME_SIZE] - cropsPerSecondTimeStamps[searchIndex];
		timePassed /= 1000f;

		cropsPerSecond /= timePassed;

		if(Float.isNaN(cropsPerSecond)) cropsPerSecond = 0;

		lineMap.put(
			12,
			EnumChatFormatting.AQUA + "Crops/s: " + EnumChatFormatting.YELLOW +
				String.format("%,.0f", cropsPerSecond)
		);
	}
}
