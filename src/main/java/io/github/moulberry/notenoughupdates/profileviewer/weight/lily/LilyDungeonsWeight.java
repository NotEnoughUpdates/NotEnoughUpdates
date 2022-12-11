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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.moulberry.notenoughupdates.profileviewer.ProfileViewer;
import io.github.moulberry.notenoughupdates.profileviewer.weight.weight.DungeonsWeight;
import io.github.moulberry.notenoughupdates.profileviewer.weight.weight.WeightStruct;
import io.github.moulberry.notenoughupdates.util.Constants;
import io.github.moulberry.notenoughupdates.util.Utils;

import java.util.Map;

public class LilyDungeonsWeight extends DungeonsWeight {

	private final JsonObject profileJson;

	public LilyDungeonsWeight(Map<String, ProfileViewer.Level> player, JsonObject profileJson) {
		super(player);
		this.profileJson = profileJson;
	}

	@Override
	public void getDungeonWeight() {
		ProfileViewer.Level catacombs = player.get("catacombs");

		double extra = 0;
		double n = 0;
		if (catacombs.totalXp < CATACOMBS_LEVEL_50_XP) {
			n = 0.2 * Math.pow(catacombs.level / 50, 1.538679118869934);
		} else {
			extra = 500.0 * Math.pow((catacombs.totalXp - CATACOMBS_LEVEL_50_XP) / 142452410.0, 1.0 / 1.781925776625157);
		}

		if (catacombs.level != 0) {
			if (catacombs.totalXp < CATACOMBS_LEVEL_50_XP) {
				weightStruct.add(
					new WeightStruct(
						Utils.getElement(Constants.WEIGHT, "lily.dungeons.overall").getAsDouble() *
						((Math.pow(1.18340401286164044, (catacombs.level + 1)) - 1.05994990217254) * (1 + n))
					)
				);
			} else {
				weightStruct.add(new WeightStruct((4100 + extra) * 2));
			}
		}
	}

	public void getDungeonCompletionWeight(String cataMode) {
		double max1000 = 0;
		double mMax1000 = 0;
		JsonObject dungeonsCompletionWorth = Utils.getElement(Constants.WEIGHT, "lily.dungeons.completion_worth").getAsJsonObject();
		for (Map.Entry<String, JsonElement> dcwEntry : dungeonsCompletionWorth.entrySet()) {
			if (dcwEntry.getKey().startsWith("catacombs_")) {
				max1000 += dcwEntry.getValue().getAsDouble();
			} else {
				mMax1000 += dcwEntry.getValue().getAsDouble();
			}
		}
		max1000 *= 1000;
		mMax1000 *= 1000;
		double upperBound = 1500;
		if (cataMode.equals("normal")) {
			if (Utils.getElement(profileJson, "dungeons.dungeon_types.catacombs.tier_completions") == null) {
				return;
			}

			double score = 0;
			for (Map.Entry<String, JsonElement> normalFloor : Utils
				.getElement(profileJson, "dungeons.dungeon_types.catacombs.tier_completions")
				.getAsJsonObject()
				.entrySet()) {
				int amount = normalFloor.getValue().getAsInt();
				double excess = 0;
				if (amount > 1000) {
					excess = amount - 1000;
					amount = 1000;
				}

				double floorScore = amount * dungeonsCompletionWorth.get("catacombs_" + normalFloor.getKey()).getAsDouble();
				if (excess > 0) floorScore *= Math.log(excess / 1000 + 1) / Math.log(7.5) + 1;
				score += floorScore;
			}

			weightStruct.add(new WeightStruct(score / max1000 * upperBound * 2));
		} else {
			JsonObject dungeonsCompletionBuffs = Utils.getElement(Constants.WEIGHT, "lily.dungeons.completion_buffs").getAsJsonObject();

			if (Utils.getElement(profileJson, "dungeons.dungeon_types.master_catacombs.tier_completions") == null) {
				return;
			}

			for (Map.Entry<String, JsonElement> masterFloor : Utils
				.getElement(profileJson, "dungeons.dungeon_types.master_catacombs.tier_completions")
				.getAsJsonObject()
				.entrySet()) {
				if (dungeonsCompletionBuffs.get(masterFloor.getKey()) != null) {
					int amount = masterFloor.getValue().getAsInt();
					double threshold = 20;
					if (amount >= threshold) {
						upperBound += dungeonsCompletionBuffs.get(masterFloor.getKey()).getAsInt();
					} else {
						upperBound +=
							dungeonsCompletionBuffs.get(masterFloor.getKey()).getAsInt() * Math.pow((amount / threshold), 1.840896416);
					}
				}
			}

			double masterScore = 0;
			for (Map.Entry<String, JsonElement> masterFloor : Utils
				.getElement(profileJson, "dungeons.dungeon_types.master_catacombs.tier_completions")
				.getAsJsonObject()
				.entrySet()) {
				int amount = masterFloor.getValue().getAsInt();
				double excess = 0;
				if (amount > 1000) {
					excess = amount - 1000;
					amount = 1000;
				}

				double floorScore = amount * dungeonsCompletionWorth.get("master_catacombs_" + masterFloor.getKey()).getAsDouble();
				if (excess > 0) {
					floorScore *= (Math.log((excess / 1000) + 1) / Math.log(6)) + 1;
				}
				masterScore += floorScore;
			}

			weightStruct.add(new WeightStruct((masterScore / mMax1000) * upperBound * 2));
		}
	}
}
