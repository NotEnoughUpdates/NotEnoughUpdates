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
import io.github.moulberry.moulconfig.annotations.ConfigAccordionId;
import io.github.moulberry.moulconfig.annotations.ConfigEditorAccordion;
import io.github.moulberry.moulconfig.annotations.ConfigEditorBoolean;
import io.github.moulberry.moulconfig.annotations.ConfigEditorButton;
import io.github.moulberry.moulconfig.annotations.ConfigEditorDropdown;
import io.github.moulberry.moulconfig.annotations.ConfigEditorSlider;
import io.github.moulberry.moulconfig.annotations.ConfigOption;

public class Toolbar {
	@Expose
	@ConfigOption(
		name = "Edit Toolbar Positions",
		desc = "Change the position of the QuickCommands / Search Bar"
	)
	@ConfigEditorButton(runnableId = 6, buttonText = "Edit")
	public boolean positionButton = true;

	@ConfigOption(
		name = "Search Bar",
		desc = ""
	)
	@ConfigEditorAccordion(id = 0)
	public boolean searchBarAccordion = false;

	@Expose
	@ConfigOption(
		name = "Show Search Bar",
		desc = "Show Itemlist search bar in the NEU toolbar"
	)
	@ConfigEditorBoolean
	@ConfigAccordionId(id = 0)
	public boolean searchBar = true;

	@Expose
	@ConfigOption(
		name = "Show a quick settings button",
		desc = "Show quick settings button in the NEU toolbar"
	)
	@ConfigEditorBoolean
	@ConfigAccordionId(id = 0)
	public boolean enableSettingsButton = true;

	@Expose
	@ConfigOption(
		name = "Show an inventory search button",
		desc = "Show button to enable inventory searching in the NEU toolbar"
	)
	@ConfigEditorBoolean
	@ConfigAccordionId(id = 0)
	public boolean enableSearchModeButton = true;

	@Expose
	@ConfigOption(
		name = "CTRL+F keybind",
		desc = "Change the focus of the search bar when pressing CTRL + F"
	)
	@ConfigEditorBoolean
	@ConfigAccordionId(id = 0)
	public boolean ctrlF = true;

	@Expose
	@ConfigOption(
		name = "Search Bar Width",
		desc = "Change the width of the search bar"
	)
	@ConfigEditorSlider(
		minValue = 50f,
		maxValue = 300f,
		minStep = 10f
	)
	@ConfigAccordionId(id = 0)
	public int searchBarWidth = 200;

	@Expose
	@ConfigOption(
		name = "Search Bar Height",
		desc = "Change the height of the search bar"
	)
	@ConfigEditorSlider(
		minValue = 15f,
		maxValue = 50f,
		minStep = 1f
	)
	@ConfigAccordionId(id = 0)
	public int searchBarHeight = 40;

	@Expose
	@ConfigOption(
		name = "Auto turnoff search mode",
		desc = "Turns off the inventory search mode after 2 minutes"
	)
	@ConfigEditorBoolean
	@ConfigAccordionId(id = 0)
	public boolean autoTurnOffSearchMode = true;

	@Expose
	@ConfigOption(
		name = "Show Quick Commands",
		desc = "Show QuickCommands\u2122 in the NEU toolbar"
	)
	@ConfigEditorBoolean
	public boolean quickCommands = false;

	@Expose
	@ConfigOption(
		name = "Quick Commands Click Type",
		desc = "Change the click type needed to trigger quick commands"
	)
	@ConfigEditorDropdown(
		values = {"Mouse Up", "Mouse Down"}
	)
	public int quickCommandsClickType = 0;
}
