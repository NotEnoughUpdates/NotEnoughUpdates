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

package io.github.moulberry.notenoughupdates.options.separatesections;

import com.google.gson.annotations.Expose;
import io.github.moulberry.moulconfig.annotations.ConfigAccordionId;
import io.github.moulberry.moulconfig.annotations.ConfigEditorAccordion;
import io.github.moulberry.moulconfig.annotations.ConfigEditorBoolean;
import io.github.moulberry.moulconfig.annotations.ConfigEditorSlider;
import io.github.moulberry.moulconfig.annotations.ConfigOption;

public class RecipeTweaks {

	@ConfigOption(
		name = "Search GUI",
		desc = ""
	)
	@ConfigEditorAccordion(id = 0)
	public boolean searchAccordion = false;

	@Expose
	@ConfigOption(
		name = "Add Button in /recipes",
		desc = "Replaces the sign gui with an advanced search GUI for recipes\n" +
		"You can also use /recipe to open the GUI"
	)
	@ConfigEditorBoolean
	@ConfigAccordionId(id = 0)
	public boolean enableSearchOverlay = true;

	@Expose
	@ConfigOption(
		name = "Add Button In Crafting Table",
		desc = "Adds a button in /craft to open the recipe search overlay"
	)
	@ConfigEditorBoolean
	@ConfigAccordionId(id = 0)
	public boolean addPickaxeStack = true;

	@Expose
	@ConfigOption(
		name = "Keep Previous Search",
		desc = "Don't clear the search bar after closing the GUI"
	)
	@ConfigEditorBoolean
	@ConfigAccordionId(id = 0)
	public boolean keepPreviousSearch = false;

	@Expose
	@ConfigOption(
		name = "Past Searches",
		desc = "Show past searches below the autocomplete box"
	)
	@ConfigEditorBoolean
	@ConfigAccordionId(id = 0)
	public boolean showPastSearches = true;

	@Expose
	@ConfigOption(
		name = "ESC to Full Close",
		desc = "Make pressing ESCAPE close the search GUI without opening up the Craft menu again\n" +
			"ENTER can still be used to search"
	)
	@ConfigEditorBoolean
	@ConfigAccordionId(id = 0)
	public boolean escFullClose = true;

	@Expose
	@ConfigOption(
		name = "Search History Size",
		desc = "Changes how many search items get stored"
	)
	@ConfigEditorSlider(
		minValue = 1,
		maxValue = 15,
		minStep = 1
	)
	@ConfigAccordionId(id = 0)
	public int recipeSearchHistorySize = 5;

}
