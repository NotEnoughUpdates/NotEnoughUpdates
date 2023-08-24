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
import io.github.moulberry.moulconfig.annotations.ConfigEditorButton;
import io.github.moulberry.moulconfig.annotations.ConfigEditorDropdown;
import io.github.moulberry.moulconfig.annotations.ConfigOption;
import io.github.moulberry.notenoughupdates.core.config.Position;

public class LocationEdit {

	@ConfigOption(
		name = "Edit GUI locations",
		desc = "Change the position of NEU's overlays"
	)
	@ConfigEditorButton(
		runnableId = 4,
		buttonText = "Edit"
	)
	public Position positions = new Position(-1, -1);

	@Expose
	@ConfigOption(
		name = "Edit Gui Scale",
		desc = "Change the size of NEU's overlays"
	)
	@ConfigEditorDropdown(
		values = {"Default", "Small", "Normal", "Large", "Auto"}
	)
	public int guiScale = 0;

	@ConfigOption(
		name = "Edit Dungeon Map",
		desc = "The NEU dungeon map has it's own editor (/neumap).\n" +
			"Click the button on the left to open it"
	)
	@ConfigEditorButton(
		runnableId = 0,
		buttonText = "Edit"
	)
	public int editDungeonMap = 0;

	@ConfigOption(
		name = "Inventory",
		desc = ""
	)
	@ConfigEditorAccordion(id = 1)
	public boolean inventoryAccordion = false;

	@ConfigOption(
		name = "Edit Toolbar Positions",
		desc = "Change the position of the QuickCommands / Search Bar"
	)
	@ConfigAccordionId(id = 1)
	@ConfigEditorButton(runnableId = 6, buttonText = "Edit")
	public boolean positionButton = true;

	@ConfigOption(
		name = "Open Button Editor",
		desc = "Open button editor GUI (/neubuttons)"
	)
	@ConfigAccordionId(id = 1)
	@ConfigEditorButton(runnableId = 7, buttonText = "Open")
	public boolean openEditorButton = true;
}
