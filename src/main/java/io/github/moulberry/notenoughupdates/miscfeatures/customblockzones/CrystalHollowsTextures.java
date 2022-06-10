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

import net.minecraft.util.BlockPos;

public class CrystalHollowsTextures implements IslandZoneSubdivider {
	public SpecialBlockZone getSpecialZoneForBlock(String location, BlockPos pos) {
		if (pos.getY() < 65) {
			return SpecialBlockZone.CRYSTAL_HOLLOWS_MAGMA_FIELDS;
		} else if (pos.getX() < 565 && pos.getX() > 461 && pos.getZ() < 566 && pos.getZ() > 460 && pos.getY() > 64) {
			return SpecialBlockZone.CRYSTAL_HOLLOWS_NUCLEUS;
		} else if (pos.getX() < 513 && pos.getZ() < 513 && pos.getY() > 64) {
			return SpecialBlockZone.CRYSTAL_HOLLOWS_JUNGLE;
		} else if (pos.getX() < 513 && pos.getZ() > 512 && pos.getY() > 64) {
			return SpecialBlockZone.CRYSTAL_HOLLOWS_GOBLIN_HIDEOUT;
		} else if (pos.getX() > 512 && pos.getZ() < 513 && pos.getY() > 64) {
			return SpecialBlockZone.CRYSTAL_HOLLOWS_MITHRIL_DEPOSIT;
		} else if (pos.getX() > 512 && pos.getZ() > 512 && pos.getY() > 64) {
			return SpecialBlockZone.CRYSTAL_HOLLOWS_PRECURSOR_REMNANTS;
		}
		return null;
	}
}
