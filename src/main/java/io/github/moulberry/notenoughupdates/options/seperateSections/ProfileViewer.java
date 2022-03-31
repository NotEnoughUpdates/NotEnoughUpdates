package io.github.moulberry.notenoughupdates.options.seperateSections;

import com.google.gson.annotations.Expose;
import io.github.moulberry.notenoughupdates.core.config.annotations.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ProfileViewer {

	@ConfigOption(
		name = "Profile Viewer info",
		desc =
			"The Profile Viewer requires you to have an \u00A72api key\u00A77 set (if you don't have one set do \u00A72/api new\u00A77)\n"
	)
	@ConfigEditorFSR(
		runnableId = 12,
		buttonText = ""
	)
	public boolean pvInfo = false;

	@Expose
	@ConfigOption(
		name = "Open Profile Viewer",
		desc = "Brings up the profile viewer (/pv)\n" +
			"Shows stats and networth of players"
	)
	@ConfigEditorButton(
		runnableId = 13,
		buttonText = "Open"
	)
	public boolean openPV = true;

	@Expose
	@ConfigOption(
		name = "Page layout",
		desc = "\u00a7rSelect the order of the pages at the top of the Profile Viewer\n" +
			"\u00a7eDrag text to rearrange"
	)
	@ConfigEditorDraggableList(
		exampleText = {
			"\u00a7eBasic Info",
			"\u00a7eDungeons",
			"\u00a7eExtra Info",
			"\u00a7eInventories",
			"\u00a7eCollections",
			"\u00a7ePets",
			"\u00a7eMining",
			"\u00a7eBingo",
		},
		allowDeleting = false
	)
	public List<Integer> pageLayout = new ArrayList<>(Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7));

	@Expose
	@ConfigOption(
		name = "Always show bingo tab",
		desc = "Always show bingo tab or only show it when the bingo profile is selected"
	)
	@ConfigEditorBoolean
	public boolean alwaysShowBingoTab = false;
}
