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

import io.github.moulberry.notenoughupdates.profileviewer.ProfileViewer;
import io.github.moulberry.notenoughupdates.profileviewer.weight.weight.SlayerWeight;
import io.github.moulberry.notenoughupdates.profileviewer.weight.weight.WeightStruct;
import io.github.moulberry.notenoughupdates.util.Constants;
import io.github.moulberry.notenoughupdates.util.Utils;

import java.util.Map;

public class LilySlayerWeight extends SlayerWeight {

	public LilySlayerWeight(Map<String, ProfileViewer.Level> player) {
		super(player);
	}

	public void getSlayerWeight(String slayerName) {
		int currentSlayerXp = (int) player.get(slayerName).totalXp;

		double score;
		double d = currentSlayerXp / 100000.0;
		if (currentSlayerXp >= 6416) {
			double D = (d - Math.pow(3, (-5.0 / 2))) * (d + Math.pow(3, -5.0 / 2));
			double u = Math.cbrt(3 * (d + Math.sqrt(D)));
			double v = Math.cbrt(3 * (d - Math.sqrt(D)));
			score = u + v - 1;
		} else {
			score = Math.sqrt(4.0 / 3) * Math.cos(Math.acos(d * Math.pow(3, 5.0 / 2)) / 3) - 1;
		}

		double scaleFactor = Utils.getElementAsFloat(
			Utils.getElement(Constants.WEIGHT, "lily.slayer.deprecation_scaling." + slayerName),
			0
		);
		int intScore = (int) score;
		double distance = currentSlayerXp - actualInt(intScore);
		double effectiveDistance = distance * Math.pow(scaleFactor, intScore);
		double effectiveScore = effectiveInt(intScore, scaleFactor) + effectiveDistance;
		double weight;
		switch (slayerName) {
			case "zombie":
				weight = (effectiveScore / 9250) + (currentSlayerXp / 1000000.0);
				break;
			case "spider":
				weight = (effectiveScore / 7019.57) + ((currentSlayerXp * 1.6) / 1000000);
				break;
			case "wolf":
				weight = (effectiveScore / 2982.06) + ((currentSlayerXp * 3.6) / 1000000);
				break;
			case "enderman":
				weight = (effectiveScore / 996.3003) + ((currentSlayerXp * 10.0) / 1000000);
				break;
			case "blaze":
				weight = (effectiveScore / 935.0455) + ((currentSlayerXp * 10.0) / 1000000);
				break;
			default:
				return;
		}

		weightStruct.add(new WeightStruct(2 * weight));
	}

	private double actualInt(int intScore) {
		return (((Math.pow(intScore, 3) / 6) + (Math.pow(intScore, 2) / 2) + (intScore / 3.0)) * 100000);
	}

	private double effectiveInt(int intScore, double scaleFactor) {
		double total = 0;
		for (int k = 0; k < intScore; k++) {
			total += (Math.pow((k + 1), 2) + (k + 1)) * Math.pow(scaleFactor, (k + 1));
		}
		return 1000000 * total * (0.05 / scaleFactor);
	}
}
