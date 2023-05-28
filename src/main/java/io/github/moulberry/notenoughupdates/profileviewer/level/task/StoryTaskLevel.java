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

public class StoryTaskLevel extends GuiTaskLevel {

	public StoryTaskLevel(LevelPage levelPage) {
		super(levelPage);
	}

	@Override
	public void drawTask(JsonObject object, int mouseX, int mouseY, int guiLeft, int guiTop) {
		JsonObject storyTask = levelPage.getConstant().getAsJsonObject("story_task");
		JsonArray storyTaskNames = storyTask.getAsJsonArray("complete_objectives_names");

		JsonObject objectives = object.getAsJsonObject("objectives");

		int storyTaskXp = storyTask.get("complete_objectives_xp").getAsInt();
		int sbXpStory = 0;
		for (JsonElement storyTaskName : storyTaskNames) {
			String value = storyTaskName.getAsString();
			if (objectives.has(value)) {
				JsonObject jsonObject = objectives.get(value).getAsJsonObject();
				if (jsonObject.has("status") && jsonObject.get("status").getAsString().equals("COMPLETE")) {
					sbXpStory += storyTaskXp;
				}
			}
		}

		List<String> lore = new ArrayList<>();
		lore.add(levelPage.buildLore("Complete Objectives",
			sbXpStory, storyTask.get("complete_objectives").getAsInt(), false
		));

		levelPage.renderLevelBar(
			"Story Task",
			new ItemStack(Items.map),
			guiLeft + 299, guiTop + 85,
			110,
			0,
			sbXpStory,
			levelPage.getConstant().getAsJsonObject("category_xp").get("story_task").getAsInt(),
			mouseX, mouseY,
			true,
			lore
		);
	}
}
