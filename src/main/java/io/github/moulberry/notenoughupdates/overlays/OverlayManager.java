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
			"§3Cakes: §eInactive!",
			"§3Cookie Buff: §eInactive!",
			"§3Godpot: §eInactive!",
			"§3Puzzler: §eReady!",
			"§3Fetchur: §eReady!",
			"§3Commissions: §eReady!",
			"§3Experiments: §eReady!",
			"§3Mithril Powder: §eReady",
			"§3Gemstone Powder: §eReady",
			"§3Cakes: §e1d21h",
			"§3Cookie Buff: §e2d23h",
			"§3Godpot: §e19h",
			"§3Puzzler: §e13h",
			"§3Fetchur: §e3h38m",
			"§3Commissions: §e3h38m",
			"§3Experiments: §e3h38m",
			"§3Mithril Powder: §e3h38m",
			"§3Gemstone Powder: §e3h38m",
			"§3Crimson Isle Quests: §e3h38m"
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
			"§3Goblin Slayer: §626.5%\n§3Lucky Raffle: §c0.0%",
			"§3Mithril Powder: §26,243",
			"§3Forge 1) §9Diamonite§7: §aReady!",
			"§3Forge 2) §7EMPTY\n§3Forge 3) §7EMPTY\n§3Forge 4) §7EMPTY"
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
			"§3Chests Found: §a13",
			"§3Opened Chests: §a11",
			"§3Unopened Chests: §c2",
			"§3Mithril Powder Found: §26,243",
			"§3Average Mithril Powder/Chest: §2568",
			"§3Gemstone Powder Found: §d6,243",
			"§3Average Gemstone Powder/Chest: §d568"
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
			"§bCounter: §e37,547,860",
			"§bCrops/m: §e38.29",
			"§bFarming: §e12§7 [§e|||||||||||||||||§8||||||||§7] §e67%",
			"§bCurrent XP: §e6,734",
			"§bRemaining XP: §e3,265",
			"§bXP/h: §e238,129",
			"§bYaw: §e68.25§lᵒ"
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
			"§bCompact: §e547,860",
			"§bBlocks/m: §e38.29",
			"§bMining: §e12§7 [§e|||||||||||||||||§8||||||||§7] §e67%",
			"§bCurrent XP: §e6,734",
			"§bRemaining XP: §e3,265",
			"§bXP/h: §e238,129",
			"§bYaw: §e68.25§lᵒ"
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
			"§bCatches: §e37,547,860",
			//"§bCatches/m: §e38.29",
			"§bFish: §e12§7 [§e|||||||||||||||||§8||||||||§7] §e67%",
			"§bCurrent XP: §e6,734",
			"§bRemaining XP: §e3,265",
			"§bXP/h: §e238,129"
			//"§bYaw: §e68.25§lᵒ"
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
			"§bKills: §e547,860",
			"§bCombat: §e12§7 [§e|||||||||||||||||§8||||||||§7] §e67%",
			"§bCurrent XP: §e6,734",
			"§bRemaining XP: §e3,265",
			"§bXP/h: §e238,129",
			"§bETA: §e13h12m"
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
			"§a[Lvl 37] §fRock",
			"§b2,312.9/2,700§e (85.7%)",
			"§b2.3k/2.7k§e (85.7%)",
			"§bXP/h: §e27,209",
			"§bTotal XP: §e30,597.9",
			"§bHeld Item: §fMining Exp Boost",
			"§bUntil L38: §e5m13s",
			"§bUntil L100: §e2d13h"
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
			"§cBonemerang will break!",
			"§7Targets: §6§l10"
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
			"§3Amber Crystal: §aPlaced\n" +
				"§3Sapphire Crystal: §eCollected\n" +
				"§3Jade Crystal: §eMissing\n" +
				"§3Amethyst Crystal: §cMissing\n" +
				"§3Topaz Crystal: §cMissing\n",
			"§3Crystals: §a4/5",
			"§3Crystals: §a80%",
			"§3Electron Transmitter: §aDone\n" +
				"§3Robotron Reflector: §eIn Storage\n" +
				"§3Superlite Motor: §eIn Inventory\n" +
				"§3Synthetic Heart: §cMissing\n" +
				"§3Control Switch: §cMissing\n" +
				"§3FTX 3070: §cMissing",
			"§3Electron Transmitter: §a3\n" +
				"§3Robotron Reflector: §e2\n" +
				"§3Superlite Motor: §e1\n" +
				"§3Synthetic Heart: §c0\n" +
				"§3Control Switch: §c0\n" +
				"§3FTX 3070: §c0",
			"§3Automaton parts: §a5/6",
			"§3Automaton parts: §a83%",
			"§3Scavenged Lapis Sword: §aDone\n" +
				"§3Scavenged Golden Hammer: §eIn Storage\n" +
				"§3Scavenged Diamond Axe: §eIn Inventory\n" +
				"§3Scavenged Emerald Hammer: §cMissing\n",
			"§3Scavenged Lapis Sword: §a3\n" +
				"§3Scavenged Golden Hammer: §e2\n" +
				"§3Scavenged Diamond Axe: §e1\n" +
				"§3Scavenged Emerald Hammer: §c0\n",
			"§3Mines of Divan parts: §a3/4",
			"§3Mines of Divan parts: §a75%"
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
			"§eSlayer: §4Sven",
			"§eRNG Meter: §5100%",
			"§eLvl: §d7",
			"§eKill time: §c1:30",
			"§eXP: §d75,450/100,000",
			"§eBosses till next Lvl: §d17",
			"§eAverage kill time: §c3:20"
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
			"§3This is a fuel bar"
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
