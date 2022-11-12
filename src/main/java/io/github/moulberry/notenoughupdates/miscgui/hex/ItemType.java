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

package io.github.moulberry.notenoughupdates.miscgui.hex;

enum ItemType {
	HOT_POTATO,
	FUMING_POTATO,
	BOOK_OF_STATS,
	ART_OF_WAR,
	ART_OF_PEACE,
	FARMING_DUMMY,
	RECOMB,
	SILEX,
	RUBY_SCROLL,
	SAPPHIRE_SCROLL,
	JASPER_SCROLL,
	AMETHYST_SCROLL,
	AMBER_SCROLL,
	OPAL_SCROLL,
	FIRST_STAR(1),
	SECOND_STAR(2),
	THIRD_STAR(3),
	FOURTH_STAR(4),
	FIFTH_STAR(5),
	FIRST_MASTER_STAR(6),
	SECOND_MASTER_STAR(7),
	THIRD_MASTER_STAR(8),
	FOURTH_MASTER_STAR(9),
	FIFTH_MASTER_STAR(10),
	WOOD_SINGULARITY,
	IMPLOSION_SCROLL,
	SHADOW_WARP_SCROLL,
	WITHER_SHIELD_SCROLL,
	TUNER,
	REFORGE,
	RANDOM_REFORGE,
	MANA_DISINTEGRATOR,
	HEX_ITEM,
	TOTAL_UPGRADES,
	RUBY_GEMSTONE,
	AMETHYST_GEMSTONE,
	SAPPHIRE_GEMSTONE,
	JASPER_GEMSTONE,
	JADE_GEMSTONE,
	AMBER_GEMSTONE,
	OPAL_GEMSTONE,
	TOPAZ_GEMSTONE,
	CONVERT_TO_DUNGEON,
	GEMSTONE_SLOT,
	EXPERIENCE_BOTTLE,
	GRAND_EXPERIENCE_BOTTLE,
	TITANIC_EXPERIENCE_BOTTLE,
	COLOSSAL_EXPERIENCE_BOTTLE,
	ENRICHMENT_SPEED,
	ENRICHMENT_INTELLIGENCE,
	ENRICHMENT_CRIT_DAMAGE,
	ENRICHMENT_STRENGTH,
	ENRICHMENT_DEFENSE,
	ENRICHMENT_HEALTH,
	ENRICHMENT_MAGIC_FIND,
	ENRICHMENT_FEROCITY,
	ENRICHMENT_SCC,
	ENRICHMENT_ATTACK_SPEED,
	ENRICHMENT_CRIT_CHANCE,
	UNKNOWN;

	private int starLevel = -1;

	ItemType() {}

	ItemType(int starLevel) {
		this.starLevel = starLevel;
	}

	public int getStarLevel() {
		return starLevel;
	}
}
