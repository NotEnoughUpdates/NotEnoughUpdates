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

import com.google.common.collect.Lists;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.miscfeatures.PetInfoOverlay;

import java.util.ArrayList;
import java.util.List;

public class OverlayManager {
	public static ArrayList<Class<? extends TextOverlay>> dontRenderOverlay = new ArrayList<>();

	public static MiningOverlay miningOverlay;
	public static PowderGrindingOverlay powderGrindingOverlay;
	public static FarmingSkillOverlay farmingOverlay;
	public static FishingSkillOverlay fishingSkillOverlay;
	public static MiningSkillOverlay miningSkillOverlay;
	public static CombatSkillOverlay combatSkillOverlay;
	public static PetInfoOverlay petInfoOverlay;
	public static TimersOverlay timersOverlay;
	public static BonemerangOverlay bonemerangOverlay;
	public static CrystalHollowOverlay crystalHollowOverlay;
	public static SlayerOverlay slayerOverlay;
	public static FuelBarDummy fuelBar;
	public static final List<TextOverlay> textOverlays = new ArrayList<>();

	static {
		List<String> todoDummy = Lists.newArrayList(
			"\u00a73Cakes: \u00a7eInactive!",
			"\u00a73Cookie Buff: \u00a7eInactive!",
			"\u00a73Godpot: \u00a7eInactive!",
			"\u00a73Puzzler: \u00a7eReady!",
			"\u00a73Fetchur: \u00a7eReady!",
			"\u00a73Commissions: \u00a7eReady!",
			"\u00a73Experiments: \u00a7eReady!",
			"\u00a73Mithril Powder: \u00a7eReady",
			"\u00a73Gemstone Powder: \u00a7eReady",
			"\u00a73Cakes: \u00a7e1d21h",
			"\u00a73Cookie Buff: \u00a7e2d23h",
			"\u00a73Godpot: \u00a7e19h",
			"\u00a73Puzzler: \u00a7e13h",
			"\u00a73Fetchur: \u00a7e3h38m",
			"\u00a73Commissions: \u00a7e3h38m",
			"\u00a73Experiments: \u00a7e3h38m",
			"\u00a73Mithril Powder: \u00a7e3h38m",
			"\u00a73Gemstone Powder: \u00a7e3h38m",
			"\u00a73Crimson Isle Quests: \u00a7e3h38m",
			"\u00a73NPC Buy Daily Limit: \u00a7e3h38m"
		);
		textOverlays.add(
			timersOverlay = new TimersOverlay(NotEnoughUpdates.INSTANCE.config.miscOverlays.todoPosition, () -> {
				List<String> strings = new ArrayList<>();
				for (int i : NotEnoughUpdates.INSTANCE.config.miscOverlays.todoText2) {
					if (i >= 0 && i < todoDummy.size()) strings.add(todoDummy.get(i));
				}
				return strings;
			}, () -> {
				int style = NotEnoughUpdates.INSTANCE.config.miscOverlays.todoStyle;
				if (style >= 0 && style < TextOverlayStyle.values().length) {
					return TextOverlayStyle.values()[style];
				}
				return TextOverlayStyle.BACKGROUND;
			}));

		List<String> miningDummy = Lists.newArrayList(
			"\u00a73Goblin Slayer: \u00a7626.5%\n\u00a73Lucky Raffle: \u00a7c0.0%",
			"\u00a73Mithril Powder: \u00a726,243",
			"\u00a73Forge 1) \u00a79Diamonite\u00a77: \u00a7aReady!",
			"\u00a73Forge 2) \u00a77EMPTY\n\u00a73Forge 3) \u00a77EMPTY\n\u00a73Forge 4) \u00a77EMPTY"
		);
		miningOverlay = new MiningOverlay(NotEnoughUpdates.INSTANCE.config.mining.overlayPosition, () -> {
			List<String> strings = new ArrayList<>();
			for (int i : NotEnoughUpdates.INSTANCE.config.mining.dwarvenText2) {
				if (i >= 0 && i < miningDummy.size()) strings.add(miningDummy.get(i));
			}
			return strings;
		}, () -> {
			int style = NotEnoughUpdates.INSTANCE.config.mining.overlayStyle;
			if (style >= 0 && style < TextOverlayStyle.values().length) {
				return TextOverlayStyle.values()[style];
			}
			return TextOverlayStyle.BACKGROUND;
		});

		List<String> powderGrindingDummy = Lists.newArrayList(
			"\u00a73Chests Found: \u00a7a13",
			"\u00a73Opened Chests: \u00a7a11",
			"\u00a73Unopened Chests: \u00a7c2",
			"\u00a73Mithril Powder Found: \u00a726,243",
			"\u00a73Average Mithril Powder/Chest: \u00a72568",
			"\u00a73Gemstone Powder Found: \u00a7d6,243",
			"\u00a73Average Gemstone Powder/Chest: \u00a7d568"
		);
		powderGrindingOverlay =
			new PowderGrindingOverlay(NotEnoughUpdates.INSTANCE.config.mining.powderGrindingTrackerPosition, () -> {
				List<String> strings = new ArrayList<>();
				for (int i : NotEnoughUpdates.INSTANCE.config.mining.powderGrindingTrackerText) {
					if (i >= 0 && i < powderGrindingDummy.size()) strings.add(powderGrindingDummy.get(i));
				}
				return strings;
			}, () -> {
				int style = NotEnoughUpdates.INSTANCE.config.mining.powderGrindingTrackerOverlayStyle;
				if (style >= 0 && style < TextOverlayStyle.values().length) {
					return TextOverlayStyle.values()[style];
				}
				return TextOverlayStyle.BACKGROUND;
			});

		List<String> farmingDummy = Lists.newArrayList(
			"\u00a7bCounter: \u00a7e37,547,860",
			"\u00a7bCrops/m: \u00a7e38.29",
			"\u00a7bFarming: \u00a7e12\u00a77 [\u00a7e|||||||||||||||||\u00a78||||||||\u00a77] \u00a7e67%",
			"\u00a7bCurrent XP: \u00a7e6,734",
			"\u00a7bRemaining XP: \u00a7e3,265",
			"\u00a7bXP/h: \u00a7e238,129",
			"\u00a7bYaw: \u00a7e68.25\u00a7l\u1D52"
		);
		farmingOverlay = new FarmingSkillOverlay(NotEnoughUpdates.INSTANCE.config.skillOverlays.farmingPosition, () -> {
			List<String> strings = new ArrayList<>();
			for (int i : NotEnoughUpdates.INSTANCE.config.skillOverlays.farmingText) {
				if (i >= 0 && i < farmingDummy.size()) strings.add(farmingDummy.get(i));
			}
			return strings;
		}, () -> {
			int style = NotEnoughUpdates.INSTANCE.config.skillOverlays.farmingStyle;
			if (style >= 0 && style < TextOverlayStyle.values().length) {
				return TextOverlayStyle.values()[style];
			}
			return TextOverlayStyle.BACKGROUND;
		});
		List<String> miningSkillDummy = Lists.newArrayList(
			"\u00a7bCompact: \u00a7e547,860",
			"\u00a7bBlocks/m: \u00a7e38.29",
			"\u00a7bMining: \u00a7e12\u00a77 [\u00a7e|||||||||||||||||\u00a78||||||||\u00a77] \u00a7e67%",
			"\u00a7bCurrent XP: \u00a7e6,734",
			"\u00a7bRemaining XP: \u00a7e3,265",
			"\u00a7bXP/h: \u00a7e238,129",
			"\u00a7bYaw: \u00a7e68.25\u00a7l\u1D52"
		);
		miningSkillOverlay = new MiningSkillOverlay(NotEnoughUpdates.INSTANCE.config.skillOverlays.miningPosition, () -> {
			List<String> strings = new ArrayList<>();
			for (int i : NotEnoughUpdates.INSTANCE.config.skillOverlays.miningText) {
				if (i >= 0 && i < miningSkillDummy.size()) strings.add(miningSkillDummy.get(i));
			}
			return strings;
		}, () -> {
			int style = NotEnoughUpdates.INSTANCE.config.skillOverlays.miningStyle;
			if (style >= 0 && style < TextOverlayStyle.values().length) {
				return TextOverlayStyle.values()[style];
			}
			return TextOverlayStyle.BACKGROUND;
		});
		List<String> fishingDummy = Lists.newArrayList(
			"\u00a7bCatches: \u00a7e37,547,860",
			//"\u00a7bCatches/m: \u00a7e38.29",
			"\u00a7bFish: \u00a7e12\u00a77 [\u00a7e|||||||||||||||||\u00a78||||||||\u00a77] \u00a7e67%",
			"\u00a7bCurrent XP: \u00a7e6,734",
			"\u00a7bRemaining XP: \u00a7e3,265",
			"\u00a7bXP/h: \u00a7e238,129"
			//"\u00a7bYaw: \u00a7e68.25\u00a7l\u1D52"
		);
		fishingSkillOverlay =
			new FishingSkillOverlay(NotEnoughUpdates.INSTANCE.config.skillOverlays.fishingPosition, () -> {
				List<String> strings = new ArrayList<>();
				for (int i : NotEnoughUpdates.INSTANCE.config.skillOverlays.fishingText) {
					if (i >= 0 && i < fishingDummy.size()) strings.add(fishingDummy.get(i));
				}
				return strings;
			}, () -> {
				int style = NotEnoughUpdates.INSTANCE.config.skillOverlays.fishingStyle;
				if (style >= 0 && style < TextOverlayStyle.values().length) {
					return TextOverlayStyle.values()[style];
				}
				return TextOverlayStyle.BACKGROUND;
			});
		List<String> combatSkillDummy = Lists.newArrayList(
			"\u00a7bKills: \u00a7e547,860",
			"\u00a7bCombat: \u00a7e12\u00a77 [\u00a7e|||||||||||||||||\u00a78||||||||\u00a77] \u00a7e67%",
			"\u00a7bCurrent XP: \u00a7e6,734",
			"\u00a7bRemaining XP: \u00a7e3,265",
			"\u00a7bXP/h: \u00a7e238,129",
			"\u00a7bETA: \u00a7e13h12m"
		);
		combatSkillOverlay = new CombatSkillOverlay(NotEnoughUpdates.INSTANCE.config.skillOverlays.combatPosition, () -> {
			List<String> strings = new ArrayList<>();
			for (int i : NotEnoughUpdates.INSTANCE.config.skillOverlays.combatText) {
				if (i >= 0 && i < combatSkillDummy.size()) strings.add(combatSkillDummy.get(i));
			}
			return strings;
		}, () -> {
			int style = NotEnoughUpdates.INSTANCE.config.skillOverlays.combatStyle;
			if (style >= 0 && style < TextOverlayStyle.values().length) {
				return TextOverlayStyle.values()[style];
			}
			return TextOverlayStyle.BACKGROUND;
		});
		List<String> petInfoDummy = Lists.newArrayList(
			"\u00a7a[Lvl 37] \u00a7fRock",
			"\u00a7b2,312.9/2,700\u00a7e (85.7%)",
			"\u00a7b2.3k/2.7k\u00a7e (85.7%)",
			"\u00a7bXP/h: \u00a7e27,209",
			"\u00a7bTotal XP: \u00a7e30,597.9",
			"\u00a7bHeld Item: \u00a7fMining Exp Boost",
			"\u00a7bUntil L38: \u00a7e5m13s",
			"\u00a7bUntil L100: \u00a7e2d13h"
		);
		petInfoOverlay = new PetInfoOverlay(NotEnoughUpdates.INSTANCE.config.petOverlay.petInfoPosition, () -> {
			List<String> strings = new ArrayList<>();
			for (int i : NotEnoughUpdates.INSTANCE.config.petOverlay.petOverlayText) {
				if (i >= 0 && i < petInfoDummy.size()) strings.add(petInfoDummy.get(i));
			}
			return strings;
		}, () -> {
			int style = NotEnoughUpdates.INSTANCE.config.petOverlay.petInfoOverlayStyle;
			if (style >= 0 && style < TextOverlayStyle.values().length) {
				return TextOverlayStyle.values()[style];
			}
			return TextOverlayStyle.BACKGROUND;
		});

		List<String> bonemerangDummy = Lists.newArrayList(
			"\u00a7cBonemerang will break!",
			"\u00a77Targets: \u00a76\u00a7l10"
		);
		bonemerangOverlay = new BonemerangOverlay(
			NotEnoughUpdates.INSTANCE.config.itemOverlays.bonemerangPosition,
			() -> bonemerangDummy,
			() -> {
				int style = NotEnoughUpdates.INSTANCE.config.itemOverlays.bonemerangOverlayStyle;
				if (style >= 0 && style < TextOverlayStyle.values().length) {
					return TextOverlayStyle.values()[style];
				}
				return TextOverlayStyle.BACKGROUND;
			}
		);
		List<String> crystalHollowOverlayDummy = Lists.newArrayList(
			"\u00a73Amber Crystal: \u00a7aPlaced\n" +
				"\u00a73Sapphire Crystal: \u00a7eCollected\n" +
				"\u00a73Jade Crystal: \u00a7eMissing\n" +
				"\u00a73Amethyst Crystal: \u00a7cMissing\n" +
				"\u00a73Topaz Crystal: \u00a7cMissing\n",
			"\u00a73Crystals: \u00a7a4/5",
			"\u00a73Crystals: \u00a7a80%",
			"\u00a73Electron Transmitter: \u00a7aDone\n" +
				"\u00a73Robotron Reflector: \u00a7eIn Storage\n" +
				"\u00a73Superlite Motor: \u00a7eIn Inventory\n" +
				"\u00a73Synthetic Heart: \u00a7cMissing\n" +
				"\u00a73Control Switch: \u00a7cMissing\n" +
				"\u00a73FTX 3070: \u00a7cMissing",
			"\u00a73Electron Transmitter: \u00a7a3\n" +
				"\u00a73Robotron Reflector: \u00a7e2\n" +
				"\u00a73Superlite Motor: \u00a7e1\n" +
				"\u00a73Synthetic Heart: \u00a7c0\n" +
				"\u00a73Control Switch: \u00a7c0\n" +
				"\u00a73FTX 3070: \u00a7c0",
			"\u00a73Automaton parts: \u00a7a5/6",
			"\u00a73Automaton parts: \u00a7a83%",
			"\u00a73Scavenged Lapis Sword: \u00a7aDone\n" +
				"\u00a73Scavenged Golden Hammer: \u00a7eIn Storage\n" +
				"\u00a73Scavenged Diamond Axe: \u00a7eIn Inventory\n" +
				"\u00a73Scavenged Emerald Hammer: \u00a7cMissing\n",
			"\u00a73Scavenged Lapis Sword: \u00a7a3\n" +
				"\u00a73Scavenged Golden Hammer: \u00a7e2\n" +
				"\u00a73Scavenged Diamond Axe: \u00a7e1\n" +
				"\u00a73Scavenged Emerald Hammer: \u00a7c0\n",
			"\u00a73Mines of Divan parts: \u00a7a3/4",
			"\u00a73Mines of Divan parts: \u00a7a75%"
		);
		crystalHollowOverlay =
			new CrystalHollowOverlay(NotEnoughUpdates.INSTANCE.config.mining.crystalHollowOverlayPosition, () -> {
				List<String> strings = new ArrayList<>();
				for (int i : NotEnoughUpdates.INSTANCE.config.mining.crystalHollowText) {
					if (i >= 0 && i < crystalHollowOverlayDummy.size()) strings.add(crystalHollowOverlayDummy.get(i));
				}
				return strings;
			}, () -> {
				int style = NotEnoughUpdates.INSTANCE.config.mining.crystalHollowOverlayStyle;
				if (style >= 0 && style < TextOverlayStyle.values().length) {
					return TextOverlayStyle.values()[style];
				}
				return TextOverlayStyle.BACKGROUND;
			});
		List<String> slayerDummy = Lists.newArrayList(
			"\u00a7eSlayer: \u00a74Sven",
			"\u00a7eRNG Meter: \u00a75100%",
			"\u00a7eLvl: \u00a7d7",
			"\u00a7eKill time: \u00a7c1:30",
			"\u00a7eXP: \u00a7d75,450/100,000",
			"\u00a7eBosses till next Lvl: \u00a7d17",
			"\u00a7eAverage kill time: \u00a7c3:20"
		);
		slayerOverlay = new SlayerOverlay(NotEnoughUpdates.INSTANCE.config.slayerOverlay.slayerPosition, () -> {
			List<String> strings = new ArrayList<>();
			for (int i : NotEnoughUpdates.INSTANCE.config.slayerOverlay.slayerText) {
				if (i >= 0 && i < slayerDummy.size()) strings.add(slayerDummy.get(i));
			}
			return strings;
		}, () -> {
			int style = NotEnoughUpdates.INSTANCE.config.slayerOverlay.slayerStyle;
			if (style >= 0 && style < TextOverlayStyle.values().length) {
				return TextOverlayStyle.values()[style];
			}
			return TextOverlayStyle.BACKGROUND;
		});

		List<String> fuelDummy = Lists.newArrayList(
			"\u00a73This is a fuel bar"
		);
		fuelBar = new FuelBarDummy(NotEnoughUpdates.INSTANCE.config.mining.drillFuelBarPosition, () -> {
			List<String> strings = new ArrayList<>();
			strings.add(fuelDummy.get(0));
			return strings;
		}, () -> TextOverlayStyle.BACKGROUND);

		textOverlays.add(miningOverlay);
		textOverlays.add(powderGrindingOverlay);
		textOverlays.add(farmingOverlay);
		textOverlays.add(miningSkillOverlay);
		textOverlays.add(combatSkillOverlay);
		textOverlays.add(fishingSkillOverlay);
		textOverlays.add(petInfoOverlay);
		textOverlays.add(bonemerangOverlay);
		textOverlays.add(crystalHollowOverlay);
		textOverlays.add(slayerOverlay);
		textOverlays.add(fuelBar);
	}

}
