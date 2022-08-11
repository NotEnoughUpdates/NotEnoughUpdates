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

package io.github.moulberry.notenoughupdates.profileviewer.trophy;

import java.util.Map;

public class TrophyFish {

	public final Map<TrophyFishRarity, Integer> trophyFishRarityIntegerMap;
	private final String name;
	private int total = 0;

	public TrophyFish(String name, Map<TrophyFishRarity, Integer> trophyFishRarityIntegerMap) {
		this.name = name;
		this.trophyFishRarityIntegerMap = trophyFishRarityIntegerMap;
	}

	public void addTotal(int n) {
		total += n;
	}

	public int getTotal() {
		return total;
	}

	public void removeTotal(int n) {
		total -= n;
	}

	public String getName() {
		return name;
	}

	public Map<TrophyFishRarity, Integer> getTrophyFishRarityIntegerMap() {
		return trophyFishRarityIntegerMap;
	}

	public void add(TrophyFishRarity rarity, int value) {
		if (!trophyFishRarityIntegerMap.containsKey(rarity)) {
			trophyFishRarityIntegerMap.put(rarity, value);
		}
	}

	public String getInternalName() {
		return name.toLowerCase().replace(" ", "_");
	}

	public enum TrophyFishRarity {
		BRONZE,
		SILVER,
		GOLD,
		DIAMOND,
	}
}
