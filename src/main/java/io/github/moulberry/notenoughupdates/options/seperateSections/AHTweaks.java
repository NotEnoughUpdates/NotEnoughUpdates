package io.github.moulberry.notenoughupdates.options.seperateSections;

import com.google.gson.annotations.Expose;
import io.github.moulberry.notenoughupdates.core.config.annotations.*;

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
		name = "Enable BIN Warning",
		desc = "Ask for confirmation when BINing an item for below X% of lowestbin"
	)
	@ConfigEditorBoolean
	@ConfigAccordionId(id = 1)
	public boolean enableBINWarning = true;

	@Expose
	@ConfigOption(
		name = "Warning Threshold",
		desc = "Threshold for BIN warning\nExample: 10% means warn if sell price is 10% lower than lowestbin"
	)
	@ConfigEditorSlider(
		minValue = 0.0f,
		maxValue = 100.0f,
		minStep = 5f
	)
	@ConfigAccordionId(id = 1)
	public float warningThreshold = 10f;

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
}
