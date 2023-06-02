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
import io.github.moulberry.notenoughupdates.profileviewer.GuiProfileViewer;
import io.github.moulberry.notenoughupdates.profileviewer.ProfileViewer;
import io.github.moulberry.notenoughupdates.profileviewer.SkyblockProfiles;
import io.github.moulberry.notenoughupdates.profileviewer.level.LevelPage;
import io.github.moulberry.notenoughupdates.profileviewer.weight.weight.Weight;
import io.github.moulberry.notenoughupdates.util.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DungeonTaskLevel extends GuiTaskLevel {

	public DungeonTaskLevel(LevelPage levelPage) {super(levelPage);}

	@Override
	public void drawTask(JsonObject object, int mouseX, int mouseY, int guiLeft, int guiTop) {
		JsonObject dungeonTask = levelPage.getConstant().get("dungeon_task").getAsJsonObject();

		SkyblockProfiles.SkyblockProfile selectedProfile = GuiProfileViewer.getSelectedProfile();
		if (selectedProfile == null) {
			return;
		}

		Map<String, ProfileViewer.Level> skyblockInfo = selectedProfile.getLevelingInfo();

		int sbLevelGainedFloor = 0;
		int sbXpGainedClass = 0;
		int sbXpGainedLvl = 0;
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

			for (String dungeonClass : Weight.DUNGEON_CLASS_NAMES) {
				ProfileViewer.Level level = skyblockInfo.get(dungeonClass);
				for (int i = 1; i <= level.level; i++) {
					if (i <= 50) sbXpGainedClass += dungeonTask.get("class_xp").getAsInt();
				}
			}

			JsonArray completeCatacombs = dungeonTask.get("complete_catacombs").getAsJsonArray();
			int index = 0;
			for (JsonElement completeCatacomb : completeCatacombs) {
				int value = completeCatacomb.getAsInt();
				JsonElement normalCompletions = Utils
					.getElementOrDefault(object, "dungeons.dungeon_types.catacombs.tier_completions", null);
				if (normalCompletions != null && normalCompletions.getAsJsonObject().has("" + index)) {
					sbLevelGainedFloor += value;
				}
				index++;
			}

			int masterCatacombs = dungeonTask.get("complete_master_catacombs").getAsInt();
			for (int i = 0; i <= 7; i++) {
				JsonElement masterCompletions = Utils
					.getElementOrDefault(object, "dungeons.dungeon_types.master_catacombs.tier_completions", null);
				if (masterCompletions != null) {
					if (masterCompletions.getAsJsonObject().has("" + i)) {
						sbLevelGainedFloor += masterCatacombs;
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

		levelPage.renderLevelBar(
			"Dungeon Task",
			NotEnoughUpdates.INSTANCE.manager
				.createItemResolutionQuery()
				.withKnownInternalName("WITHER_RELIC")
				.resolveToItemStack(),
			guiLeft + 23, guiTop + 55,
			110,
			catacombsLvl,
			totalXp,
			totalGainful,
			mouseX, mouseY,
			true,
			lore
		);
	}
}
