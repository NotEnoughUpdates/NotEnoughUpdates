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

package io.github.moulberry.notenoughupdates.profileviewer.weight.lily;

import static io.github.moulberry.notenoughupdates.profileviewer.weight.weight.Weight.SKILL_NAMES;

import com.google.gson.JsonArray;
import io.github.moulberry.notenoughupdates.profileviewer.ProfileViewer;
import io.github.moulberry.notenoughupdates.profileviewer.weight.weight.SkillsWeight;
import io.github.moulberry.notenoughupdates.profileviewer.weight.weight.WeightStruct;
import io.github.moulberry.notenoughupdates.util.Constants;
import io.github.moulberry.notenoughupdates.util.Utils;

import java.util.Map;

public class LilySkillsWeight extends SkillsWeight {

	public LilySkillsWeight(Map<String, ProfileViewer.Level> player) {
		super(player);
	}

	@Override
	public void getSkillsWeight(String skillName) {
		double skillAverage = 0;
		for (String skill : SKILL_NAMES) {
			skillAverage +=
				(int) ProfileViewer.getLevel(
					Utils.getElement(Constants.LEVELING, "leveling_xp").getAsJsonArray(),
					player.get(skill).totalXp,
					60,
					false
				)
					.level;
		}
		skillAverage /= SKILL_NAMES.size();

		float currentExp = player.get(skillName).totalXp;
		int currentLevel = (int) ProfileViewer.getLevel(
			Utils.getElement(Constants.LEVELING, "leveling_xp").getAsJsonArray(),
			currentExp,
			60,
			false
		)
			.level;

		JsonArray srwTable = Utils.getElement(Constants.WEIGHT, "lily.skills.ratio_weight." + skillName).getAsJsonArray();
		double base =
			(
				(12 * Math.pow((skillAverage / 60), 2.44780217148309)) *
				srwTable.get(currentLevel).getAsDouble() *
				srwTable.get(srwTable.size() - 1).getAsDouble()
			) +
			(srwTable.get(srwTable.size() - 1).getAsDouble() * Math.pow(currentLevel / 60.0, Math.pow(2, 0.5)));
		base *= 1.8162162162162162;
		double overflow = 0;
		if (currentExp > 111672425) {
			double factor = Utils.getElementAsFloat(Utils.getElement(Constants.WEIGHT, "lily.skills.factors." + skillName), 0);
			double effectiveOver = effectiveXP(currentExp - 111672425, factor);
			double t =
				(effectiveOver / 111672425) *
				Utils.getElementAsFloat(Utils.getElement(Constants.WEIGHT, "lily.skills.overflow_multipliers." + skillName), 0);
			if (t > 0) {
				overflow += 1.8162162162162162 * t;
			}
		}

		weightStruct.add(new WeightStruct(base, overflow));
	}

	private double effectiveXP(double xp, double factor) {
		if (xp < 111672425) {
			return xp;
		} else {
			double remainingXP = xp;
			double z = 0;
			for (int i = 0; i <= (int) (xp / 111672425); i++) {
				if (remainingXP >= 111672425) {
					remainingXP -= 111672425;
					z += Math.pow(factor, i);
				}
			}
			return z * 111672425;
		}
	}
}
