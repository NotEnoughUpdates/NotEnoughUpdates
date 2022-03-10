package io.github.moulberry.notenoughupdates.options.seperateSections;

import com.google.gson.annotations.Expose;
import io.github.moulberry.notenoughupdates.core.config.annotations.ConfigEditorBoolean;
import io.github.moulberry.notenoughupdates.core.config.annotations.ConfigEditorButton;
import io.github.moulberry.notenoughupdates.core.config.annotations.ConfigEditorFSR;
import io.github.moulberry.notenoughupdates.core.config.annotations.ConfigOption;

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
		name = "Always show bingo tab",
		desc = "Always show bingo tab or only show it when the bingo profile is selected"
	)
	@ConfigEditorBoolean
	public boolean alwaysShowBingoTab = false;
}
