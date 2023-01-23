/*
 * Copyright (C) 2023 NotEnoughUpdates contributors
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

package io.github.moulberry.notenoughupdates.profileviewer.level.task;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.moulberry.notenoughupdates.profileviewer.ProfileViewer;
import io.github.moulberry.notenoughupdates.profileviewer.level.LevelPage;
import io.github.moulberry.notenoughupdates.util.Constants;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SkillRelatedTaskLevel {

	private final LevelPage levelPage;

	public SkillRelatedTaskLevel(LevelPage levelPage) {this.levelPage = levelPage;}

	public void drawTask(JsonObject object, int mouseX, int mouseY, int guiLeft, int guiTop) {
		JsonObject skillRelatedTask = levelPage.getConstant().get("skill_related_task").getAsJsonObject();
		JsonObject miningObj = skillRelatedTask.get("mining").getAsJsonObject();

		float mithrilPowder = Utils.getElementAsFloat(Utils.getElement(object, "mining_core.powder_mithril"), 0);
		float gemstonePowder = Utils.getElementAsFloat(Utils.getElement(object, "mining_core.powder_gemstone"), 0);
		float mithril = Utils.getElementAsFloat(Utils.getElement(object, "mining_core.powder_spent_mithril"), 0) +
			mithrilPowder;
		float gemstone = (Utils.getElementAsFloat(Utils.getElement(object, "mining_core.powder_spent_gemstone"), 0)) +
			gemstonePowder;

		float hotmXp = Utils.getElementAsFloat(Utils.getElement(object, "mining_core.experience"), 0);
		ProfileViewer.Level levelObjHotm =
			ProfileViewer.getLevel(
				Utils.getElementOrDefault(Constants.LEVELING, "HOTM", new JsonArray()).getAsJsonArray(),
				hotmXp,
				7,
				false
			);

		int hotmXP = 0;
		float level = levelObjHotm.level;
		JsonArray hotmXpArray = miningObj.get("hotm_xp").getAsJsonArray();
		for (int i = 1; i <= level; i++) {
			hotmXP += hotmXpArray.get(i - 1).getAsInt();
		}

		int gainByFirstMithrilThing;
		if (mithril >= 350_000) {
			gainByFirstMithrilThing = (int) (350000 / 2400d);
			mithril -= 350_000;
		} else {
			gainByFirstMithrilThing = (int) (mithril / 2400d);
			mithril = 0;
		}

		int gainByFirstGemstoneThing;
		if (gemstone >= 350_000) {
			gainByFirstGemstoneThing = (int) (350_000 / 2500d);
			gemstone -= 350_000;
		} else {
			gainByFirstGemstoneThing = (int) (gemstone / 2500d);
			gemstone = 0;
		}

		int sbXpMithrilPowder = (int) powder(3.75, mithril, 12_500_000);
		int sbXpGemstonePowder = (int) powder(4.25, gemstone, 20_000_000);

		double sbXpHotmTier =
			(sbXpMithrilPowder + gainByFirstMithrilThing) + (sbXpGemstonePowder + gainByFirstGemstoneThing)
				+ hotmXP;

		int sbXpPotmTier = 0;
		JsonArray potmXpArray = miningObj.get("potm_xp").getAsJsonArray();

		int potm = ((Utils.getElementAsInt(Utils.getElement(object, "mining_core.nodes.special_0"), 0)));
		for (int i = 1; i <= potm; i++) {
			sbXpPotmTier += potmXpArray.get(i - 1).getAsInt();
		}

		int sbXpCommissionMilestone = 0;
		JsonArray tutorialArray = object.get("tutorial").getAsJsonArray();
		JsonArray commissionMilestoneXpArray = miningObj.get("commission_milestone_xp").getAsJsonArray();
		for (JsonElement jsonElement : tutorialArray) {
			if (jsonElement.getAsJsonPrimitive().isString() && jsonElement.getAsString().startsWith(
				"commission_milestone_reward_skyblock_xp_tier"))
				for (int i = 1; i <= commissionMilestoneXpArray.size(); i++) {
					int value = commissionMilestoneXpArray.get(i - 1).getAsInt();
					if (jsonElement.getAsString().equals("commission_milestone_reward_skyblock_xp_tier_" + i)) {
						sbXpCommissionMilestone += value;
					}
				}
		}

		// rock mines
		float pet_milestone_ores_mined = Utils.getElementAsFloat(Utils.getElement(
			object,
			"stats.pet_milestone_ores_mined"
		), 0);

		int sbXpRockPet = 0;
		int rockMilestoneXp = miningObj.get("rock_milestone_xp").getAsInt();
		JsonArray rockMilestoneRequired = miningObj.get("rock_milestone_required").getAsJsonArray();
		for (JsonElement jsonElement : rockMilestoneRequired) {
			int value = jsonElement.getAsInt();
			if (pet_milestone_ores_mined >= value) {
				sbXpRockPet += rockMilestoneXp;
			}
		}

		// farming
		JsonObject farmingObj = skillRelatedTask.get("farming").getAsJsonObject();
		int anitaShopUpgradesXp = farmingObj.get("anita_shop_upgrades_xp").getAsInt();
		int doubleDrops = Utils.getElementAsInt(Utils.getElement(object, "jacob2.perks.double_drops"), 0);
		int farmingLevelCap = Utils.getElementAsInt(Utils.getElement(object, "jacob2.perks.farming_level_cap"), 0);

		int sbXpGainedByAnita = (doubleDrops + farmingLevelCap) * anitaShopUpgradesXp;

		// fishing
		int sbXpTrophyFish = 0;
		JsonObject fishingObj = skillRelatedTask.get("fishing").getAsJsonObject();

		JsonArray trophyFishXpArr = fishingObj.get("trophy_fish_xp").getAsJsonArray();
		if (object.has("trophy_fish")) {

			JsonObject trophyFish = object.get("trophy_fish").getAsJsonObject();
			for (Map.Entry<String, JsonElement> stringJsonElementEntry : trophyFish.entrySet()) {
				String key = stringJsonElementEntry.getKey();
				if (stringJsonElementEntry.getValue().isJsonPrimitive()) {
					if (key.endsWith("_bronze")) sbXpTrophyFish += trophyFishXpArr.get(0).getAsInt();
					if (key.endsWith("_silver")) sbXpTrophyFish += trophyFishXpArr.get(1).getAsInt();
					if (key.endsWith("_gold")) sbXpTrophyFish += trophyFishXpArr.get(2).getAsInt();
					if (key.endsWith("_diamond")) sbXpTrophyFish += trophyFishXpArr.get(3).getAsInt();
				}
			}

		}
		float pet_milestone_sea_creatures_killed = Utils.getElementAsFloat(
			Utils.getElement(object, "stats.pet_milestone_sea_creatures_killed"),
			0
		);

		int sbXpDolphinPet = 0;
		int dolphinMilestoneXp = fishingObj.get("dolphin_milestone_xp").getAsInt();
		JsonArray dolphinMilestoneRequired = fishingObj.get("dolphin_milestone_required").getAsJsonArray();
		for (JsonElement jsonElement : dolphinMilestoneRequired) {
			int value = jsonElement.getAsInt();
			if (pet_milestone_sea_creatures_killed >= value) {
				sbXpDolphinPet += dolphinMilestoneXp;
			}
		}

		List<String> lore = new ArrayList<>();
		lore.add(levelPage.buildLore("Heart of the Mountain", sbXpHotmTier, miningObj.get("hotm").getAsInt(), false));
		lore.add(levelPage.buildLore(
			"Commission Milestones",
			sbXpCommissionMilestone,
			miningObj.get("commission_milestone").getAsInt(),
			false
		));
		lore.add(levelPage.buildLore("Crystal Nucleus", 0, 0, false));
		lore.add(levelPage.buildLore(
			"Anita's Shop Upgrade",
			sbXpGainedByAnita,
			farmingObj.get("anita_shop_upgrades").getAsInt(),
			false
		));
		lore.add(levelPage.buildLore("Peak of the Mountain", sbXpPotmTier, miningObj.get("potm").getAsInt(), false));
		lore.add(levelPage.buildLore("Trophy Fish", sbXpTrophyFish, fishingObj.get("trophy_fish").getAsInt(), false));
		lore.add(levelPage.buildLore("Rock Milestone", sbXpRockPet, miningObj.get("rock_milestone").getAsInt(), false));
		lore.add(levelPage.buildLore(
			"Dolphin Milestone",
			sbXpDolphinPet,
			fishingObj.get("dolphin_milestone").getAsInt(),
			false
		));

		levelPage.getInstance().renderLevelBar(
			"Skill Related Task",
			new ItemStack(Items.diamond_sword),
			guiLeft + 23,
			guiTop + 115,
			110,
			0,
			sbXpHotmTier + sbXpCommissionMilestone + sbXpGainedByAnita + sbXpPotmTier + sbXpTrophyFish + sbXpRockPet +
				sbXpDolphinPet,
			levelPage.getConstant().getAsJsonObject("category_xp").get("skill_related_task").getAsInt(),
			mouseX,
			mouseY,
			true,
			lore
		);
	}

	private static double powder(double multiplier, float left, int CAP) {
		double cons = 1758267;
		if (left <= 0) return 0;

		left = Math.min(CAP, left);

		return multiplier * (Math.sqrt(1 + 8 * (Math.sqrt((cons / CAP) * left + 9))) - 3);
	}

}
