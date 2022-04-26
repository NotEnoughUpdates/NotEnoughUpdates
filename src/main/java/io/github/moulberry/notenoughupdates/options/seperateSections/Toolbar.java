package io.github.moulberry.notenoughupdates.options.seperateSections;

import com.google.gson.annotations.Expose;
import io.github.moulberry.notenoughupdates.core.config.annotations.*;

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
		name = "Show a help settings button",
		desc = "Show quick settings button in the NEU toolbar"
	)
	@ConfigEditorBoolean
	@ConfigAccordionId(id = 0)
	public boolean enableHelpButton = true;

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
