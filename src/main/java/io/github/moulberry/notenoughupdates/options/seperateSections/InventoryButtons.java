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
import io.github.moulberry.moulconfig.annotations.ConfigEditorButton;
import io.github.moulberry.moulconfig.annotations.ConfigEditorDropdown;
import io.github.moulberry.moulconfig.annotations.ConfigEditorSlider;
import io.github.moulberry.moulconfig.annotations.ConfigOption;

public class InventoryButtons {
	@Expose
	@ConfigOption(
		name = "Open Button Editor",
		desc = "Open button editor GUI (/neubuttons)"
	)
	@ConfigEditorButton(runnableId = 7, buttonText = "Open")
	public boolean openEditorButton = true;

	@Expose
	@ConfigOption(
		name = "Always Hide \"Crafting\" Text",
		desc = "Hide crafting text in inventory, even when no button is there"
	)
	@ConfigEditorBoolean
	public boolean hideCrafting = false;

	@Expose
	@ConfigOption(
		name = "Button Click Type",
		desc = "Change the click type needed to trigger commands"
	)
	@ConfigEditorDropdown(
		values = {"Mouse Down", "Mouse Up"}
	)
	public int clickType = 0;

	@Expose
	@ConfigOption(
		name = "Tooltip Delay",
		desc = "Change the Tooltip Delay in milliseconds"
	)
	@ConfigEditorSlider(
		minValue = 0,
		maxValue = 1500,
		minStep = 50
	)
	public int tooltipDelay = 600;
}
