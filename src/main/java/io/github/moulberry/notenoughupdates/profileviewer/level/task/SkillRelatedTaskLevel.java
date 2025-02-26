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
import com.google.gson.JsonPrimitive;
import io.github.moulberry.notenoughupdates.profileviewer.GuiProfileViewer;
import io.github.moulberry.notenoughupdates.profileviewer.ProfileViewer;
import io.github.moulberry.notenoughupdates.profileviewer.SkyblockProfiles;
import io.github.moulberry.notenoughupdates.profileviewer.data.APIDataJson;
import io.github.moulberry.notenoughupdates.profileviewer.level.LevelPage;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SkillRelatedTaskLevel extends GuiTaskLevel {

	public SkillRelatedTaskLevel(LevelPage levelPage) {
		super(levelPage);
	}

	@Override
	public void drawTask(JsonObject object, int mouseX, int mouseY, int guiLeft, int guiTop) {
		SkyblockProfiles.SkyblockProfile selectedProfile = GuiProfileViewer.getSelectedProfile();
		if (selectedProfile == null) {
			return;
		}
		APIDataJson data = selectedProfile.getAPIDataJson();
		if (data == null) {
			return;
		}

		Map<String, ProfileViewer.Level> levelingInfo = selectedProfile.getLevelingInfo();
		if (levelingInfo == null) {
			return;
		}

		JsonObject skillRelatedTask = levelPage.getConstant().getAsJsonObject("skill_related_task");
		JsonObject miningObj = skillRelatedTask.getAsJsonObject("mining");

		int hotmLevel = (int) levelingInfo.get("hotm").level;

		int hotmXP = 0;
		JsonArray hotmXpArray = miningObj.get("hotm_xp").getAsJsonArray();
		for (int i = 1; i <= hotmLevel; i++) {
			hotmXP += hotmXpArray.get(i - 1).getAsInt();
		}

		float mithrilPowder = data.mining_core.powder_mithril;
		float gemstonePowder = data.mining_core.powder_gemstone;
		float glacitePowder = data.mining_core.powder_glacite;
		float mithril = data.mining_core.powder_spent_mithril + mithrilPowder;
		float gemstone = data.mining_core.powder_spent_gemstone + gemstonePowder;
		float glacite = data.mining_core.powder_spent_glacite + glacitePowder;

		// PUNKT NULL

		double totalMithril = mithril + mithrilPowder;
		double totalGemstone = gemstone + gemstonePowder;
		double totalGlacite = glacite + glacitePowder;
		double mithrilUnder = Math.min(350000.0, totalMithril);
		double mithrilOver = Math.max(0, Math.min(totalMithril, 12_500_000.0) - 350000.0);
		double gemstoneUnder = Math.min(350000.0, totalGemstone);
		double gemstoneOver = Math.max(0, Math.min(totalGemstone, 20_000_000.0) - 350000.0);
		double glaciteUnder = Math.min(350000.0, totalGlacite);
		double glaciteOver = Math.max(0, Math.min(totalGlacite, 20_000_000.0) - 350000.0);

		double mithrilXP = Math.floor(mithrilUnder / 2400.0);
		double gemstoneXP = Math.floor(gemstoneUnder / 2500.0);
		double glaciteXP = Math.floor(glaciteUnder / 2500.0);
		double mithrilExcess = Math.floor(
			3.75 * (Math.sqrt(1 + 8 * Math.sqrt((1758267.0 / 12_500_000.0) * mithrilOver + 9)) - 3));
		double gemstoneExcess = Math.floor(
			4.25 * (Math.sqrt(1 + 8 * Math.sqrt((1758267.0 / 20_000_000.0) * gemstoneOver + 9)) - 3));
		double glaciteExcess = Math.floor(
			4.25 * (Math.sqrt(1 + 8 * Math.sqrt((1758267.0 / 20_000_000.0) * glaciteOver + 9)) - 3));

		double sbXpHotmTier =
			(mithrilXP + mithrilExcess) + (gemstoneXP + gemstoneExcess) + (glaciteXP + glaciteExcess)
				+ hotmXP;

		int sbXpPotmTier = 0;
		JsonArray potmXpArray = miningObj.get("potm_xp").getAsJsonArray();

		int potm = (data.mining_core.nodes.getOrDefault("special_0", new JsonPrimitive(0)).getAsInt());
		for (int i = 1; i <= potm; i++) {
			sbXpPotmTier += potmXpArray.get(i - 1).getAsInt();
		}

		int sbXpCommissionMilestone = 0;
		JsonArray tutorialArray = Utils.getElementOrDefault(
			selectedProfile.getProfileJson(),
			"objectives.tutorial",
			new JsonArray()
		).getAsJsonArray();
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

		int sbXpFossilResearch =
			data.glacite_player_data.fossils_donated.size() * miningObj.get("fossil_research_xp").getAsInt();

		// rock mines
		float pet_milestone_ores_mined = data.player_stats.pets.milestone.ores_mined;

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
		int doubleDrops = data.jacobs_contest.perks.double_drops;
		int farmingLevelCap = data.jacobs_contest.perks.farming_level_cap;

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
		float petMilestoneKilled = data.player_stats.pets.milestone.sea_creatures_killed;

		int sbXpDolphinPet = 0;
		int dolphinMilestoneXp = fishingObj.get("dolphin_milestone_xp").getAsInt();
		JsonArray dolphinMilestoneRequired = fishingObj.get("dolphin_milestone_required").getAsJsonArray();
		for (JsonElement jsonElement : dolphinMilestoneRequired) {
			int value = jsonElement.getAsInt();
			if (petMilestoneKilled >= value) {
				sbXpDolphinPet += dolphinMilestoneXp;
			}
		}

		int sbXpNucleus = 0;
		int nucleusRuns = data.leveling.completions.NUCLEUS_RUNS;
		JsonElement nucleusXp = miningObj.get("crystal_nucleus_xp");
		if (nucleusXp == null) {
			Utils.showOutdatedRepoNotification("crystal_nucleus_xp from sblevels.json");
		} else {
			sbXpNucleus += nucleusRuns * nucleusXp.getAsInt();
		}

		List<String> lore = new ArrayList<>();
		lore.add(levelPage.buildLore("Heart of the Mountain", sbXpHotmTier, miningObj.get("hotm").getAsInt(), false));
		lore.add(levelPage.buildLore(
			"Commission Milestones",
			sbXpCommissionMilestone,
			miningObj.get("commission_milestone").getAsInt(),
			false
		));
		lore.add(levelPage.buildLore("Crystal Nucleus", sbXpNucleus, miningObj.get("crystal_nucleus").getAsInt(), false));
		lore.add(levelPage.buildLore(
			"Anita's Shop Upgrade",
			sbXpGainedByAnita,
			farmingObj.get("anita_shop_upgrades").getAsInt(),
			false
		));
		lore.add(levelPage.buildLore("Peak of the Mountain", sbXpPotmTier, miningObj.get("potm").getAsInt(), false));
		lore.add(levelPage.buildLore(
			"Fossil Research",
			sbXpFossilResearch,
			miningObj.get("fossil_research").getAsInt(),
			false
		));
		lore.add(levelPage.buildLore("Trophy Fish", sbXpTrophyFish, fishingObj.get("trophy_fish").getAsInt(), false));
		lore.add(levelPage.buildLore("Rock Milestone", sbXpRockPet, miningObj.get("rock_milestone").getAsInt(), false));
		lore.add(levelPage.buildLore(
			"Dolphin Milestone",
			sbXpDolphinPet,
			fishingObj.get("dolphin_milestone").getAsInt(),
			false
		));

		int totalXp =
			(int) (sbXpHotmTier + sbXpCommissionMilestone + sbXpGainedByAnita + sbXpPotmTier + sbXpTrophyFish + sbXpRockPet +
				sbXpDolphinPet + sbXpNucleus + sbXpFossilResearch);
		levelPage.renderLevelBar(
			"Skill Related Task",
			new ItemStack(Items.diamond_sword),
			guiLeft + 23, guiTop + 115,
			110,
			0,
			totalXp,
			levelPage.getConstant().getAsJsonObject("category_xp").get("skill_related_task").getAsInt(),
			mouseX, mouseY,
			true,
			lore
		);
	}
}
