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
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.profileviewer.ProfileViewer;
import io.github.moulberry.notenoughupdates.profileviewer.level.LevelPage;
import io.github.moulberry.notenoughupdates.util.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class DungeonTaskLevel {

	private final LevelPage levelPage;

	public DungeonTaskLevel(LevelPage levelPage) {this.levelPage = levelPage;}

	public void drawTask(JsonObject object, int mouseX, int mouseY, int guiLeft, int guiTop) {
		JsonObject dungeonTask = levelPage.getConstant().get("dungeon_task").getAsJsonObject();

		Map<String, ProfileViewer.Level> skyblockInfo =
			levelPage.getProfile().getSkyblockInfo(levelPage.getProfileId());

		double sbLevelGainedFloor = 0;
		double sbXpGainedClass = 0;
		double sbXpGainedLvl = 0;
		int catacombsLvl = 0;
		if (skyblockInfo != null && skyblockInfo.containsKey("catacombs")) {
			ProfileViewer.Level catacombs = skyblockInfo.get("catacombs");

			catacombsLvl = (int) catacombs.level;
			for (int i = 1; i <= catacombs.level; i++) {
				if (40 > i) {
					sbXpGainedLvl += 20;
				} else {
					sbXpGainedLvl += 40;
				}
			}

			List<String> dungeonClasses = Arrays.asList("healer", "tank", "mage", "archer", "berserk");
			for (String dungeonClass : dungeonClasses) {
				ProfileViewer.Level level = skyblockInfo.get(dungeonClass);
				for (int i = 1; i <= level.level; i++) {
					if (i <= 50) sbXpGainedClass += dungeonTask.get("class_xp").getAsInt();
				}
			}

			JsonArray completeCatacombs = dungeonTask.get("complete_catacombs").getAsJsonArray();
			int index = 0;
			for (JsonElement completeCatacomb : completeCatacombs) {
				int value = completeCatacomb.getAsInt();
				JsonObject normalCompletions = Utils
					.getElement(object, "dungeons.dungeon_types.catacombs.tier_completions")
					.getAsJsonObject();
				if (normalCompletions.has(index + "")) {
					sbLevelGainedFloor = sbLevelGainedFloor + value;
				}
				index++;
			}

			int masterCatacombs = dungeonTask.get("complete_master_catacombs").getAsInt();
			for (int i = 0; i <= 7; i++) {
				JsonElement masterCompletions = Utils
					.getElementOrDefault(object, "dungeons.dungeon_types.master_catacombs.tier_completions", null);
				if (masterCompletions != null) {
					if (masterCompletions.getAsJsonObject().has(i + "")) {
						sbLevelGainedFloor = sbLevelGainedFloor + masterCatacombs;
					}
				}
			}
		}

		int catacombsLevelUp = dungeonTask.get("catacombs_level_up").getAsInt();
		int classLevelUp = dungeonTask.get("class_level_up").getAsInt();
		int completeDungeon = dungeonTask.get("complete_dungeon").getAsInt();
		int totalGainful = catacombsLevelUp + classLevelUp + completeDungeon;
		double totalXp = sbXpGainedLvl + sbXpGainedClass + sbLevelGainedFloor;

		List<String> lore = new ArrayList<>();

		lore.add(levelPage.buildLore("Catacombs Level Up", sbXpGainedLvl, catacombsLevelUp, false));
		lore.add(levelPage.buildLore("Class Level Up", sbXpGainedClass, classLevelUp, false));
		lore.add(levelPage.buildLore("Complete Dungeons", sbLevelGainedFloor, completeDungeon, false));

		levelPage.getInstance().renderLevelBar(
			"Dungeon",
			NotEnoughUpdates.INSTANCE.manager
				.createItemResolutionQuery()
				.withKnownInternalName("WITHER_RELIC")
				.resolveToItemStack(),
			guiLeft + 23,
			guiTop + 55,
			110,
			catacombsLvl,
			totalXp,
			totalGainful,
			mouseX,
			mouseY,
			true,
			lore
		);
	}
}
