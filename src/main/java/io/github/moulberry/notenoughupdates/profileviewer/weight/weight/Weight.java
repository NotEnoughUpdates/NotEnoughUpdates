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

package io.github.moulberry.notenoughupdates.profileviewer.weight.weight;

import java.util.Arrays;
import java.util.List;

public abstract class Weight {

	public static final List<String> SKILL_NAMES = Arrays.asList(
		"taming",
		"mining",
		"foraging",
		"enchanting",
		"farming",
		"combat",
		"fishing",
		"alchemy"
	);
	public static final List<String> SLAYER_NAMES = Arrays.asList("wolf", "zombie", "spider", "enderman", "blaze", "vampire");
	public static final List<String> DUNGEON_CLASS_NAMES = Arrays.asList("healer", "mage", "berserk", "archer", "tank");
	protected final SlayerWeight slayerWeight;
	protected final SkillsWeight skillsWeight;
	protected final DungeonsWeight dungeonsWeight;

	public Weight(SlayerWeight slayerWeight, SkillsWeight skillsWeight, DungeonsWeight dungeonsWeight) {
		this.slayerWeight = slayerWeight;
		this.skillsWeight = skillsWeight;
		this.dungeonsWeight = dungeonsWeight;
		this.calculateWeight();
	}

	public WeightStruct getTotalWeight() {
		return new WeightStruct()
			.add(slayerWeight.getWeightStruct())
			.add(skillsWeight.getWeightStruct())
			.add(dungeonsWeight.getWeightStruct());
	}

	public SlayerWeight getSlayerWeight() {
		return slayerWeight;
	}

	public SkillsWeight getSkillsWeight() {
		return skillsWeight;
	}

	public DungeonsWeight getDungeonsWeight() {
		return dungeonsWeight;
	}

	protected abstract void calculateWeight();
}
