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

package io.github.moulberry.notenoughupdates.options.seperateSections;

import com.google.gson.annotations.Expose;
import io.github.moulberry.moulconfig.annotations.ConfigEditorBoolean;
import io.github.moulberry.moulconfig.annotations.ConfigEditorColour;
import io.github.moulberry.moulconfig.annotations.ConfigEditorDropdown;
import io.github.moulberry.moulconfig.annotations.ConfigEditorSlider;
import io.github.moulberry.moulconfig.annotations.ConfigOption;

public class Itemlist {
	@Expose
	@ConfigOption(
		name = "Show Vanilla Items",
		desc = "Vanilla items are included in the item list"
	)
	@ConfigEditorBoolean
	public boolean showVanillaItems = true;

	@Expose
	@ConfigOption(
		name = "Open Itemlist Arrow",
		desc = "Creates an arrow on the right-side to open the item list when hovered"
	)
	@ConfigEditorBoolean
	public boolean tabOpen = true;

	@Expose
	@ConfigOption(
		name = "Keep Open",
		desc = "Keeps the Itemlist open after the inventory is closed"
	)
	@ConfigEditorBoolean
	public boolean keepopen = false;

	@Expose
	@ConfigOption(
		name = "Open when searching",
		desc = "Open the Itemlist when in container search mode by double clicking the search bar"
	)
	@ConfigEditorBoolean
	public boolean openWhenSearching = true;


	@Expose
	@ConfigOption(
		name = "Item Style",
		desc = "Sets the style of the background behind items"
	)
	@ConfigEditorDropdown(
		values = {"Round", "Square"}
	)
	public int itemStyle = 0;

	@Expose
	@ConfigOption(
		name = "Pane Gui Scale",
		desc = "Change the gui scale of the Itemlist"
	)
	@ConfigEditorDropdown(
		values = {"Default", "Small", "Medium", "Large", "Auto"}
	)
	public int paneGuiScale = 0;

	@Expose
	@ConfigOption(
		name = "Background Blur",
		desc = "Change the blur amount behind the Itemlist. 0 = off"
	)
	@ConfigEditorSlider(
		minValue = 0,
		maxValue = 20,
		minStep = 1
	)
	public int bgBlurFactor = 5;

	@Expose
	@ConfigOption(
		name = "Pane Width Multiplier",
		desc = "Change the width of the Itemlist"
	)
	@ConfigEditorSlider(
		minValue = 0.5f,
		maxValue = 1.5f,
		minStep = 0.1f
	)
	public float paneWidthMult = 1.0f;

	@Expose
	@ConfigOption(
		name = "Pane Padding",
		desc = "Change the padding around the Itemlist"
	)
	@ConfigEditorSlider(
		minValue = 0f,
		maxValue = 20f,
		minStep = 1f
	)
	public int panePadding = 10;

	@Expose
	@ConfigOption(
		name = "Foreground Colour",
		desc = "Change the colour of foreground elements in the Itemlist"
	)
	@ConfigEditorColour
	public String foregroundColour = "00:255:100:100:100";

	@Expose
	@ConfigOption(
		name = "Favourite Colour",
		desc = "Change the colour of favourited elements in the Itemlist"
	)
	@ConfigEditorColour
	public String favouriteColour = "00:255:200:150:50";

	@Expose
	@ConfigOption(
		name = "Pane Background Colour",
		desc = "Change the colour of the Itemlist background"
	)
	@ConfigEditorColour
	public String backgroundColour = "15:6:0:0:255";

	@Expose
	@ConfigOption(
		name = "Always show Monsters",
		desc = "Always show Monster Items in the item list"
	)
	@ConfigEditorBoolean(
		runnableId = 21
	)
	public boolean alwaysShowMonsters = false;
}
