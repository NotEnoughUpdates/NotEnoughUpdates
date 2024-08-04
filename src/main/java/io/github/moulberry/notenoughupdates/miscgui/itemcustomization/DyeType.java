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

package io.github.moulberry.notenoughupdates.miscgui.itemcustomization;

import com.google.gson.JsonArray;

public class DyeType {

	String itemId;
	String colour = null;
	JsonArray colours = null;
	String[] coloursArray = null;
	int ticks = 2;
	DyeMode dyeMode = DyeMode.CYCLING;

	public DyeType(String displayName) {
		this.itemId = displayName;

	}

	public DyeType(String itemID, String colour) {
		this.itemId = itemID;
		this.colour = colour;
	}

	public DyeType(String itemID, JsonArray colours) {
		this.itemId = itemID;
		this.colours = colours;
	}

	public DyeType(String[] coloursArray, int ticks, DyeMode dyeMode) {
		this.ticks = ticks;
		this.coloursArray = coloursArray;
		this.dyeMode = dyeMode;
	}

	public boolean hasStaticColour() {
		return colour != null;
	}

	public boolean hasAnimatedColour() {
		return colours != null;
	}
}
