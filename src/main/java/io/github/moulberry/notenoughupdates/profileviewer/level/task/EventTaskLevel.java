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
import io.github.moulberry.notenoughupdates.profileviewer.level.LevelPage;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EventTaskLevel extends GuiTaskLevel {

	public EventTaskLevel(LevelPage levelPage) {
		super(levelPage);
	}

	@Override
	public void drawTask(JsonObject object, int mouseX, int mouseY, int guiLeft, int guiTop) {
		List<String> lore = new ArrayList<>();

		int sbXpMiningFiesta = 0;
		int sbXpFishingFestival = 0;
		int sbXpSpookyFestival = 0;
		JsonObject constant = levelPage.getConstant();
		JsonObject eventTask = constant.getAsJsonObject("event_task");

		if (object.has("leveling")) {
			JsonObject leveling = object.getAsJsonObject("leveling");
			int miningFiestaOresMined = 0;
			int fishingFestivalSharksKilled = 0;
			if (leveling.has("mining_fiesta_ores_mined"))
				miningFiestaOresMined = leveling.get("mining_fiesta_ores_mined").getAsInt();
			if (leveling.has("fishing_festival_sharks_killed")) fishingFestivalSharksKilled = leveling.get(
				"fishing_festival_sharks_killed").getAsInt();

			sbXpMiningFiesta = getCapOrAmount(miningFiestaOresMined, 1_000_000, 5_000);
			sbXpFishingFestival = getCapOrAmount(fishingFestivalSharksKilled, 5_000, 50);

			if (leveling.has("completed_tasks")) {
				JsonArray completedTasks = leveling.get("completed_tasks").getAsJsonArray();
				JsonObject spookyFestivalXp = eventTask.getAsJsonObject("spooky_festival_xp");
				for (JsonElement completedTask : completedTasks) {
					String name = completedTask.getAsString();
					if (spookyFestivalXp.has(name)) {
						sbXpSpookyFestival += spookyFestivalXp.get(name).getAsInt();
					}
				}
			}
		}

		int sbXpUniqueMedals = 0;
		if(object.has("jacob2") && object.getAsJsonObject("jacob2").has("unique_golds2")) {
			int size = object.getAsJsonObject("jacob2").getAsJsonArray("unique_golds2").size();
			sbXpUniqueMedals = size * eventTask.get("jacob_farming_contest_xp").getAsInt();
		}

		lore.add(levelPage.buildLore("Mining Fiesta", sbXpMiningFiesta, eventTask.get("mining_fiesta").getAsInt(), false));
		lore.add(levelPage.buildLore(
			"Fishing Festival",
			sbXpFishingFestival,
			eventTask.get("fishing_festival").getAsInt(),
			false
		));
		lore.add(levelPage.buildLore(
			"Jacob's Farming Contest",
			sbXpUniqueMedals,
			eventTask.get("jacob_farming_contest").getAsInt(),
			false
		));
		lore.add(levelPage.buildLore(
			"Spooky Festival",
			sbXpSpookyFestival,
			eventTask.get("spooky_festival").getAsInt(),
			false
		));

		int totalXp = sbXpMiningFiesta + sbXpSpookyFestival +
			sbXpFishingFestival + sbXpUniqueMedals;
		levelPage.renderLevelBar(
			"Event Task",
			new ItemStack(Items.clock),
			guiLeft + 299, guiTop + 115,
			110,
			0,
			totalXp,
			levelPage.getConstant().getAsJsonObject("category_xp").get("event_task").getAsInt(),
			mouseX, mouseY,
			true,
			lore
		);
	}

	private int getCapOrAmount(int miningFiestaOresMined, int cap, int per) {
		if (miningFiestaOresMined == 0) return 0;
		if (miningFiestaOresMined > cap) {
			return cap / per;
		}
		return miningFiestaOresMined / per;
	}
}
