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

package io.github.moulberry.notenoughupdates.profileviewer.weight.senither;

import com.google.gson.JsonObject;
import io.github.moulberry.notenoughupdates.profileviewer.ProfileViewer;
import io.github.moulberry.notenoughupdates.profileviewer.weight.weight.DungeonsWeight;
import io.github.moulberry.notenoughupdates.profileviewer.weight.weight.WeightStruct;
import io.github.moulberry.notenoughupdates.util.Constants;
import io.github.moulberry.notenoughupdates.util.Utils;

public class SenitherDungeonsWeight extends DungeonsWeight {

	public SenitherDungeonsWeight(JsonObject player) {
		super(player);
	}

	public void getClassWeight(String className) {
		float currentClassXp = Utils.getElementAsFloat(Utils.getElement(player, "experience_skill_" + className), 0);
		double currentClassLevel = ProfileViewer.getLevel(Utils.getElement(Constants.LEVELING, "catacombs").getAsJsonArray(),
			currentClassXp, Utils.getElementAsInt(Utils.getElement(Constants.LEVELING, "leveling_caps.catacombs"), 50), false
		).level;
		double base = Math.pow(currentClassLevel, 4.5) * Utils.getElementAsFloat(Utils.getElement(Constants.WEIGHT, "senither.dungeons.classes." + className), 0);

		if (currentClassXp <= CATACOMBS_LEVEL_50_XP) {
			weightStruct.add(new WeightStruct(base));
			return;
		}

		double remaining = currentClassXp - CATACOMBS_LEVEL_50_XP;
		double splitter = (4 * CATACOMBS_LEVEL_50_XP) / base;
		weightStruct.add(new WeightStruct(Math.floor(base), Math.pow(remaining / splitter, 0.968)));
	}

	@Override
	public void getDungeonWeight() {
		float catacombsSkillXp = Utils.getElementAsFloat(Utils.getElement(player, "experience_skill_catacombs"), 0);

		double level = ProfileViewer.getLevel(Utils.getElement(Constants.LEVELING, "catacombs").getAsJsonArray(),
			catacombsSkillXp, Utils.getElementAsInt(Utils.getElement(Constants.LEVELING, "leveling_caps.catacombs"), 50), false
		).level;
		double base = Math.pow(level, 4.5) * Utils.getElementAsFloat(Utils.getElement(Constants.WEIGHT, "senither.dungeons.catacombs"), 0);

		if (catacombsSkillXp <= CATACOMBS_LEVEL_50_XP) {
			weightStruct.add(new WeightStruct(base));
			return;
		}

		double remaining = catacombsSkillXp - CATACOMBS_LEVEL_50_XP;
		double splitter = (4 * CATACOMBS_LEVEL_50_XP) / base;
		weightStruct.add(new WeightStruct(Math.floor(base), Math.pow(remaining / splitter, 0.968)));
	}
}
