/*
 * Copyright (C) 2024 NotEnoughUpdates contributors
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

import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;

public class CrimsonIsleTextures implements IslandZoneSubdivider {

	//-200 123 -930
	//-390 123 -930
	//-200 123 -1100
	//-200 123 -1100
	//-200 40 -930
	//-390 40 -930
	//-200 40 -1100
	//-200 40 -1100
	//smoldering tomb

	@Override
	public SpecialBlockZone getSpecialZoneForBlock(String location, BlockPos position) {
		AxisAlignedBB axisAlignedBB = new AxisAlignedBB(-390, 40, -1100, -200, 123, -930);
		if (!axisAlignedBB.isVecInside(new Vec3(position))) return null;
		return SpecialBlockZone.SMOLDERING_TOMB;
	}
}
