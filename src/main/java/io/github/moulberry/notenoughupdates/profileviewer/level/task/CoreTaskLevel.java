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

import com.google.gson.JsonObject;
import io.github.moulberry.notenoughupdates.profileviewer.PlayerStats;
import io.github.moulberry.notenoughupdates.profileviewer.ProfileViewer;
import io.github.moulberry.notenoughupdates.profileviewer.level.LevelPage;
import io.github.moulberry.notenoughupdates.util.Constants;
import io.github.moulberry.notenoughupdates.util.hypixelapi.ProfileCollectionInfo;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class CoreTaskLevel {

	private final LevelPage levelPage;

	public CoreTaskLevel(LevelPage levelPage) {this.levelPage = levelPage;}

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

	public void drawTask(JsonObject object, int mouseX, int mouseY, int guiLeft, int guiTop) {
		JsonObject coreTask = levelPage.getConstant().get("core_task").getAsJsonObject();
		// skills
		Map<String, ProfileViewer.Level> skyblockInfo = levelPage.getProfile().getSkyblockInfo(levelPage.getProfileId());

		int sbXpGainedSkillLVL = 0;
		if (skyblockInfo != null) {
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
		int sbXpGainedMp = levelPage.getProfile().getMagicalPower(levelPage.getProfileId());

		// pets

		int petScore = PlayerStats.getPetScore(object);
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
		collection = levelPage.getProfile().getCollectionInfo(
			levelPage.getProfileId()
		);
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
					if (minionXp.has(i + "")) sbXpMinionTier += minionXp.get(i + "").getAsInt();
				}
			}
		}
		List<String> lore = new ArrayList<>();

		lore.add(levelPage.buildLore("Skill Level Up",
			sbXpGainedSkillLVL, coreTask.get("skill_level_up").getAsInt(), false
		));
		lore.add(levelPage.buildLore("Museum Progression",
			0, 0, false
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
			0, 0, false
		));

		levelPage.renderLevelBar(
			"Core Task",
			new ItemStack(Items.nether_star),
			guiLeft + 23,
			guiTop + 25,
			110,
			0,
			sbXpGainedSkillLVL + sbXpGainedFairy +
				sbXpCollection + sbXpMinionTier,
			levelPage.getConstant().getAsJsonObject("category_xp").get("core_task").getAsInt(),
			mouseX,
			mouseY,
			true,
			lore
		);
	}
}
