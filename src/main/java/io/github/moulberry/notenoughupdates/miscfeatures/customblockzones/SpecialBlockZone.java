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

package io.github.moulberry.notenoughupdates.miscfeatures.customblockzones;

import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import net.minecraft.world.biome.BiomeGenBase;

public enum SpecialBlockZone {
	DWARVEN_MINES_NON_MITHRIL(BiomeGenBase.extremeHillsPlus, false, false, false, true, false),
	DWARVEN_MINES_MITHRIL(BiomeGenBase.extremeHillsEdge, true, true, false, true, false),
	CRYSTAL_HOLLOWS_MAGMA_FIELDS(NotEnoughUpdates.crystalHollowsMagmaFields, true, false, true, false, true),
	CRYSTAL_HOLLOWS_NUCLEUS(NotEnoughUpdates.crystalHollowsCrystalNucleus, true, false, true, false, true),
	CRYSTAL_HOLLOWS_JUNGLE(NotEnoughUpdates.crystalHollowsJungle, true, false, true, false, true),
	CRYSTAL_HOLLOWS_GOBLIN_HIDEOUT(NotEnoughUpdates.crystalHollowsGoblinHoldout, true, false, true, false, true),
	CRYSTAL_HOLLOWS_MITHRIL_DEPOSIT(NotEnoughUpdates.crystalHollowsMithrilDeposit, true, false, true, false, true),
	CRYSTAL_HOLLOWS_PRECURSOR_REMNANTS(NotEnoughUpdates.crystalHollowsPrecursorRemnants, true, false, true, false, true),
	NON_SPECIAL_ZONE(null, false, false, false, false, false);

	private final BiomeGenBase customBiome;
	private final boolean hasMithril;
	private final boolean hasTitanium;
	private final boolean hasGemstones;
	private final boolean isDwarvenMines;
	private final boolean isCrystalHollows;

	SpecialBlockZone(
		BiomeGenBase customBiome,
		boolean hasMithril,
		boolean hasTitanium,
		boolean hasGemstones,
		boolean isDwarvenMines,
		boolean isCrystalHollows
	) {
		this.customBiome = customBiome;
		this.hasMithril = hasMithril;
		this.hasTitanium = hasTitanium;
		this.hasGemstones = hasGemstones;
		this.isDwarvenMines = isDwarvenMines;
		this.isCrystalHollows = isCrystalHollows;
	}

	public BiomeGenBase getCustomBiome() {
		return customBiome;
	}

	public boolean hasMithril() {
		return hasMithril;
	}

	public boolean hasTitanium() {
		return hasTitanium;
	}

	public boolean hasGemstones() {
		return hasGemstones;
	}

	public boolean isDwarvenMines() {
		return isDwarvenMines;
	}

	public boolean isCrystalHollows() {
		return isCrystalHollows;
	}

	public boolean isMiningZone() {
		return isCrystalHollows || isDwarvenMines;
	}
}
