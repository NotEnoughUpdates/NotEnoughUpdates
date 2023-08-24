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
import io.github.moulberry.moulconfig.annotations.ConfigEditorSlider;
import io.github.moulberry.moulconfig.annotations.ConfigOption;

public class AHTweaks {
	@ConfigOption(
		name = "Search GUI",
		desc = ""
	)
	@ConfigEditorAccordion(id = 0)
	public boolean searchAccordion = false;

	@Expose
	@ConfigOption(
		name = "Enable Search GUI",
		desc = "Use the advanced search GUI with autocomplete and history instead of the normal sign GUI\n\u00a7eStar Selection Texture: Johnny#4567"
	)
	@ConfigEditorBoolean
	@ConfigAccordionId(id = 0)
	public boolean enableSearchOverlay = true;

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
		desc = "Make pressing ESCAPE close the search GUI without opening up the AH again\n" +
			"ENTER can still be used to search"
	)
	@ConfigEditorBoolean
	@ConfigAccordionId(id = 0)
	public boolean escFullClose = true;

	@ConfigOption(
		name = "BIN Warning",
		desc = ""
	)
	@ConfigEditorAccordion(id = 1)
	public boolean binWarningAccordion = false;

	@Expose
	@ConfigOption(
		name = "Enable Undercut BIN Warning",
		desc = "Ask for confirmation when BINing an item for below X% of lowest bin"
	)
	@ConfigEditorBoolean
	@ConfigAccordionId(id = 1)
	public boolean underCutWarning = true;

	@Expose
	@ConfigOption(
		name = "Enable Overcut BIN Warning",
		desc = "Ask for confirmation when BINing an item for over X% of lowest bin"
	)
	@ConfigEditorBoolean
	@ConfigAccordionId(id = 1)
	public boolean overCutWarning = true;

	@Expose
	@ConfigOption(
		name = "Undercut Warning Threshold",
		desc = "Threshold for BIN warning\nExample: 10% means warn if sell price is 10% lower than lowest bin"
	)
	@ConfigEditorSlider(
		minValue = 0.0f,
		maxValue = 100.0f,
		minStep = 5f
	)
	@ConfigAccordionId(id = 1)
	public float warningThreshold = 10f;

	@Expose
	@ConfigOption(
		name = "Overcut Warning Threshold",
		desc = "Threshold for BIN warning\nExample: 50% means warn if sell price is 50% higher than lowest bin\n\u00A7c\u00a7lWARNING: \u00A7r\u00A7c100% will if above lbin always trigger, 0% instead will never trigger"
	)
	@ConfigEditorSlider(
		minValue = 0.0f,
		maxValue = 100.0f,
		minStep = 5f
	)
	@ConfigAccordionId(id = 1)
	public float overcutWarningThreshold = 50f;

	@ConfigOption(
		name = "Sort Warning",
		desc = ""
	)
	@ConfigEditorAccordion(id = 2)
	public boolean sortWarningAccordion = false;

	@Expose
	@ConfigOption(
		name = "Enable Sort Warning",
		desc = "Show the sort mode when the mode is not 'Lowest Price'"
	)
	@ConfigEditorBoolean
	@ConfigAccordionId(id = 2)
	public boolean enableSortWarning = true;

	@Expose
	@ConfigOption(
		name = "Enable AH Sell Value",
		desc = "Display profit information (coins to collect, value if all sold, expired and unclaimed auctions)"
	)
	@ConfigEditorBoolean
	public boolean enableAhSellValue = true;

}
