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
import io.github.moulberry.notenoughupdates.profileviewer.weight.weight.SlayerWeight;
import io.github.moulberry.notenoughupdates.profileviewer.weight.weight.WeightStruct;
import io.github.moulberry.notenoughupdates.util.Constants;
import io.github.moulberry.notenoughupdates.util.Utils;

import java.util.Map;

public class SenitherSlayerWeight extends SlayerWeight {

	public SenitherSlayerWeight(Map<String, ProfileViewer.Level> player) {
		super(player);
	}

	public void getSlayerWeight(String slayerName) {
		if (slayerName.equals("blaze") || slayerName.equals("vampire")) {
			return;
		}

		JsonArray curWeights = Utils.getElement(Constants.WEIGHT, "senither.slayer." + slayerName).getAsJsonArray();
		double divider = curWeights.get(0).getAsDouble();
		double modifier = curWeights.get(1).getAsDouble();

		int currentSlayerXp = (int) player.get(slayerName).totalXp;

		if (currentSlayerXp <= 1000000) {
			weightStruct.add(new WeightStruct(currentSlayerXp == 0 ? 0 : currentSlayerXp / divider));
			return;
		}

		double base = 1000000 / divider;
		double remaining = currentSlayerXp - 1000000;
		double overflow = 0;
		double initialModifier = modifier;

		while (remaining > 0) {
			double left = Math.min(remaining, 1000000);

			overflow += Math.pow(left / (divider * (1.5 + modifier)), 0.942);
			modifier += initialModifier;
			remaining -= left;
		}

		weightStruct.add(new WeightStruct(base, overflow));
	}
}
