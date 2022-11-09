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

package io.github.moulberry.notenoughupdates.events;

import net.minecraft.util.EnumParticleTypes;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

@Cancelable
public class SpawnParticleEvent extends NEUEvent {

	// I LOVE code duplication!
	// I LOVE repeating field names five times to declare a data class!
	// I LOVE that yummy high fructose corn syrup!
	// I LOVE the United Nations!

	EnumParticleTypes particleTypes;
	boolean isLongDistance;
	double xCoord;
	double yCoord;
	double zCoord;
	double xOffset;
	double yOffset;
	double zOffset;
	int[] params;

	public SpawnParticleEvent(
		EnumParticleTypes particleTypes,
		boolean isLongDistance,
		double xCoord, double yCoord, double zCoord,
		double xOffset, double yOffset, double zOffset,
		int[] params
	) {
		this.particleTypes = particleTypes;
		this.isLongDistance = isLongDistance;
		this.xCoord = xCoord;
		this.yCoord = yCoord;
		this.zCoord = zCoord;
		this.xOffset = xOffset;
		this.yOffset = yOffset;
		this.zOffset = zOffset;
		this.params = params;
	}

	public EnumParticleTypes getParticleTypes() {
		return particleTypes;
	}

	public boolean isLongDistance() {
		return isLongDistance;
	}

	public double getXCoord() {
		return xCoord;
	}

	public double getYCoord() {
		return yCoord;
	}

	public double getZCoord() {
		return zCoord;
	}

	public double getXOffset() {
		return xOffset;
	}

	public double getYOffset() {
		return yOffset;
	}

	public double getZOffset() {
		return zOffset;
	}

	public int[] getParams() {
		return params;
	}

}
