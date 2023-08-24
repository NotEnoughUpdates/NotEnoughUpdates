/*
 * Copyright (C) 2023 NotEnoughUpdates contributors
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

package io.github.moulberry.notenoughupdates.options.seperateSections;

import com.google.gson.annotations.Expose;
import io.github.moulberry.moulconfig.annotations.ConfigEditorBoolean;
import io.github.moulberry.moulconfig.annotations.ConfigEditorColour;
import io.github.moulberry.moulconfig.annotations.ConfigEditorDropdown;
import io.github.moulberry.moulconfig.annotations.ConfigOption;

public class Museum {

	@Expose
	@ConfigOption(
		name = "Show Museum Items",
		desc = "Show real items instead of green dye in the museum"
	)
	@ConfigEditorBoolean
	public boolean museumItemShow = false;

	@Expose
	@ConfigOption(
		name = "Highlight virtual museum items",
		desc = "Highlight virtual museum items with a background color"
	)
	@ConfigEditorColour
	public String museumItemColor = "0:255:0:255:0";

	@Expose
	@ConfigOption(
		name = "Show Items to donate",
		desc = "Show the cheapest items you have not yet donated to the Museum"
	)
	@ConfigEditorBoolean
	public boolean museumCheapestItemOverlay = true;

	@Expose
	@ConfigOption(
		name = "Value calculation",
		desc = "Choose the source for the value calculation"
	)
	@ConfigEditorDropdown(
		values = {"Lowest BIN", "Craft cost"}
	)
	public int museumCheapestItemOverlayValueSource = 0;

}
