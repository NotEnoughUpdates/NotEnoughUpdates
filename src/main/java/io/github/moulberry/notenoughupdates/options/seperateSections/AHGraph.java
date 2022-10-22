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
import io.github.moulberry.notenoughupdates.core.config.annotations.ConfigEditorBoolean;
import io.github.moulberry.notenoughupdates.core.config.annotations.ConfigEditorColour;
import io.github.moulberry.notenoughupdates.core.config.annotations.ConfigEditorDropdown;
import io.github.moulberry.notenoughupdates.core.config.annotations.ConfigEditorKeybind;
import io.github.moulberry.notenoughupdates.core.config.annotations.ConfigEditorSlider;
import io.github.moulberry.notenoughupdates.core.config.annotations.ConfigOption;
import org.lwjgl.input.Keyboard;

public class AHGraph {
	@Expose
	@ConfigOption(
		name = "Enable AH/BZ Price Graph",
		desc = "Enable or disable the graph. Disabling this will also make it so that no price data is stored.",
		searchTags = {"auction", "bazaar"}
	)
	@ConfigEditorBoolean
	public boolean graphEnabled = true;

	@Expose
	@ConfigOption(
		name = "Keybind",
		desc = "Key to press to open the graph."
	)
	@ConfigEditorKeybind(defaultKey = Keyboard.KEY_P)
	public int graphKey = Keyboard.KEY_P;

	@Expose
	@ConfigOption(
		name = "GUI Style",
		desc = "Change the style of the graph GUI"
	)
	@ConfigEditorDropdown(
		values = {"Minecraft", "Grey", "PacksHQ Dark", "FSR"}
	)
	public int graphStyle = 0;

	@Expose
	@ConfigOption(
		name = "Graph Colour",
		desc = "Set a custom colour for the graph.",
		searchTags = "color"
	)
	@ConfigEditorColour
	public String graphColor = "0:255:0:255:0";

	@Expose
	@ConfigOption(
		name = "Secondary Graph Colour",
		desc = "Set a custom colour for the second graph line.",
		searchTags = "color"
	)
	@ConfigEditorColour
	public String graphColor2 = "0:255:255:255:0";

	@Expose
	@ConfigOption(
		name = "Default Time",
		desc = "Change the default time period for the graph."
	)
	@ConfigEditorDropdown(
		values = {"1 Hour", "1 Day", "1 Week", "All Time"}
	)
	public int defaultMode = 1;

	@Expose
	@ConfigOption(
		name = "Data Retention",
		desc = "Change the time (in days) that data is kept for.\nLonger retention require more storage."
	)
	@ConfigEditorSlider(
		minValue = 1,
		maxValue = 30,
		minStep = 1
	)
	public int dataRetention = 7;

	@Expose
	@ConfigOption(
		name = "Number of Graph Zones",
		desc = "Change the number of graph zones.\nHigher numbers will have more detail, but will look way more cramped."
	)
	@ConfigEditorSlider(
		minValue = 50,
		maxValue = 300,
		minStep = 1
	)
	public int graphZones = 175;
}
