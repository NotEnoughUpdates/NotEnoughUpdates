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
import io.github.moulberry.notenoughupdates.profileviewer.GuiProfileViewer;
import io.github.moulberry.notenoughupdates.profileviewer.ProfileViewer;
import io.github.moulberry.notenoughupdates.profileviewer.SkyblockProfiles;
import io.github.moulberry.notenoughupdates.profileviewer.level.LevelPage;
import io.github.moulberry.notenoughupdates.util.Constants;
import io.github.moulberry.notenoughupdates.util.hypixelapi.ProfileCollectionInfo;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class CoreTaskLevel extends GuiTaskLevel {

	public CoreTaskLevel(LevelPage levelPage) {
		super(levelPage);
	}

	private final List<String> skills = Arrays.asList(
		"taming",
		"mining",
		"foraging",
		"enchanting",
		"carpentry",
		"farming",
		"combat",
		"fishing",
		"alchemy"
	);

	@Override
	public void drawTask(JsonObject object, int mouseX, int mouseY, int guiLeft, int guiTop) {
		JsonObject coreTask = levelPage.getConstant().get("core_task").getAsJsonObject();

		SkyblockProfiles.SkyblockProfile selectedProfile = GuiProfileViewer.getSelectedProfile();
		if (selectedProfile == null) {
			return;
		}

		Map<String, ProfileViewer.Level> skyblockInfo = selectedProfile.getLevelingInfo();
		int sbXpGainedSkillLVL = 0;
		if (skyblockInfo != null && selectedProfile.skillsApiEnabled()) {
			for (String skill : skills) {
				ProfileViewer.Level level = skyblockInfo.get(skill);
				for (int i = 1; i <= level.level; i++) {
					if (i <= 10) {
						sbXpGainedSkillLVL += 5;
					}
					if (i <= 25 && i > 10) {
						sbXpGainedSkillLVL += 10;
					}
					if (i <= 50 && i > 25) {
						sbXpGainedSkillLVL += 20;
					}
					if (i <= 60 && i > 50) {
						sbXpGainedSkillLVL += 30;
					}
				}
			}
		} else {
			sbXpGainedSkillLVL = -1;
		}

		// mp acc
		int sbXpGainedMp = 0;
		if (object.has("accessory_bag_storage") &&
			object.getAsJsonObject("accessory_bag_storage").has("highest_magical_power")) {
			sbXpGainedMp = object.getAsJsonObject("accessory_bag_storage").get("highest_magical_power").getAsInt();
		}

		// pets

		int petScore = 0;
		if (object.has("leveling") &&
			object.getAsJsonObject("leveling").has("highest_pet_score")) {
			petScore = object.getAsJsonObject("leveling").get("highest_pet_score").getAsInt();

		}
		int sbXpPetScore = petScore * coreTask.get("pet_score_xp").getAsInt();

		// museum is not possible

		// fairy soul
		int fairySoulsCollected = object.get("fairy_souls_collected").getAsInt();
		int sbXpGainedFairy = ((fairySoulsCollected / 5)) * coreTask.get("fairy_souls_xp").getAsInt();

		int sbXpCollection = -1;
		int sbXpMinionTier = -1; // keeping at -1 here because cobblestone 1 minion XP isn't included for some reason?
		JsonObject minionXp = Constants.MISC.get("minionXp").getAsJsonObject();
		int collectionsXp = coreTask.get("collections_xp").getAsInt();
		ProfileCollectionInfo collection;
		collection = GuiProfileViewer.getSelectedProfile().getCollectionInfo();
		if (collection != null) {
			sbXpCollection = 0;
			for (Map.Entry<String, ProfileCollectionInfo.CollectionInfo> stringCollectionInfoEntry : collection
				.getCollections()
				.entrySet()) {
				ProfileCollectionInfo.CollectionInfo value = stringCollectionInfoEntry.getValue();
				sbXpCollection += value.getUnlockedTiers().size() * collectionsXp;
			}

			for (int tier : collection.getCraftedGenerators().values()) {
				for (int i = 1; i <= tier; i++) {
					if (minionXp.has(String.valueOf(i))) sbXpMinionTier += minionXp.get(String.valueOf(i)).getAsInt();
				}
			}
		}

		int sbXpBankUpgrades = 0;
		int sbXpTravelScroll = 0;
		if (object.has("leveling") && object.getAsJsonObject("leveling").has("completed_tasks")) {
			JsonArray completedTasks = object.getAsJsonObject("leveling").get("completed_tasks").getAsJsonArray();
			JsonObject bankUpgradesXp = coreTask.getAsJsonObject("bank_upgrades_xp");
			for (JsonElement completedTask : completedTasks) {
				String name = completedTask.getAsString();
				if (name.startsWith("FAST_TRAVEL_") && coreTask.has("fast_travel_unlocked_xp")) {
					sbXpTravelScroll += coreTask.get("fast_travel_unlocked_xp").getAsInt();
				}
				if (bankUpgradesXp.has(name)) {
					sbXpBankUpgrades += bankUpgradesXp.get(name).getAsInt();
				}
			}
		}


		List<String> lore = new ArrayList<>();

		int totalXp = sbXpGainedSkillLVL + sbXpGainedFairy +
			sbXpCollection + sbXpMinionTier + sbXpBankUpgrades + sbXpTravelScroll;

		lore.add(levelPage.buildLore("Skill Level Up",
			sbXpGainedSkillLVL, coreTask.get("skill_level_up").getAsInt(), false
		));

		lore.add(levelPage.buildLore(
			"Museum Progression",
			0,
			0,
			false
		));
		lore.add(levelPage.buildLore(
			"Fairy Soul",
			sbXpGainedFairy, coreTask.get("fairy_souls").getAsInt(), false
		));
		lore.add(levelPage.buildLore("Accessory Bag",
			sbXpGainedMp, 0, true
		));
		lore.add(levelPage.buildLore("Pet Score",
			sbXpPetScore, 0, true
		));
		lore.add(levelPage.buildLore("Collections",
			sbXpCollection, coreTask.get("collections").getAsInt(), false
		));
		lore.add(levelPage.buildLore("Craft Minions",
			sbXpMinionTier, coreTask.get("craft_minions").getAsInt(), false
		));
		lore.add(levelPage.buildLore("Bank Upgrade",
			sbXpBankUpgrades, coreTask.get("bank_upgrades").getAsInt(), false
		));
		lore.add(levelPage.buildLore("Fast Travel Scroll",
			sbXpTravelScroll, coreTask.get("fast_travel_unlocked").getAsInt(), false
		));

		levelPage.renderLevelBar(
			"Core Task",
			new ItemStack(Items.nether_star),
			guiLeft + 23, guiTop + 25,
			110,
			0,
			totalXp,
			levelPage.getConstant().getAsJsonObject("category_xp").get("core_task").getAsInt(),
			mouseX, mouseY,
			true,
			lore
		);
	}
}
