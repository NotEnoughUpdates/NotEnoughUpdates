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

import com.google.gson.JsonArray;
import io.github.moulberry.notenoughupdates.profileviewer.ProfileViewer;
import io.github.moulberry.notenoughupdates.profileviewer.ProfileViewerUtils;
import io.github.moulberry.notenoughupdates.profileviewer.weight.weight.SkillsWeight;
import io.github.moulberry.notenoughupdates.profileviewer.weight.weight.WeightStruct;
import io.github.moulberry.notenoughupdates.util.Constants;
import io.github.moulberry.notenoughupdates.util.Utils;

import java.util.Map;

public class SenitherSkillsWeight extends SkillsWeight {

	public SenitherSkillsWeight(Map<String, ProfileViewer.Level> player) {
		super(player);
	}

	@Override
	public void getSkillsWeight(String skillName) {
		JsonArray curWeights = Utils.getElement(Constants.WEIGHT, "senither.skills." + skillName).getAsJsonArray();
		double exponent = curWeights.get(0).getAsDouble();
		double divider = curWeights.get(1).getAsDouble();

		float currentSkillXp = player.get(skillName).totalXp;

		if (currentSkillXp > 0) {
			int maxLevel = skillName.equals("farming")
				? 60
				: Utils.getElementAsInt(Utils.getElement(Constants.LEVELING, "leveling_caps." + skillName), 50);
			double level = ProfileViewerUtils.getLevel(
				Utils.getElement(Constants.LEVELING, "leveling_xp").getAsJsonArray(),
				currentSkillXp,
				maxLevel,
				false
			)
				.level;

			double maxLevelExp = maxLevel == 50 ? SKILLS_LEVEL_50 : SKILLS_LEVEL_60;
			double base = Math.pow(level * 10, 0.5 + exponent + (level / 100)) / 1250;
			if (currentSkillXp <= maxLevelExp) {
				weightStruct.add(new WeightStruct(base));
				return;
			}

			weightStruct.add(new WeightStruct(Math.round(base), Math.pow((currentSkillXp - maxLevelExp) / divider, 0.968)));
		}
	}
}
