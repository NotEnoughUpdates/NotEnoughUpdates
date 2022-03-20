package io.github.moulberry.notenoughupdates.options.seperateSections;

import com.google.gson.annotations.Expose;
import io.github.moulberry.notenoughupdates.core.config.annotations.ConfigEditorBoolean;
import io.github.moulberry.notenoughupdates.core.config.annotations.ConfigEditorDropdown;
import io.github.moulberry.notenoughupdates.core.config.annotations.ConfigOption;

public class ImprovedSBMenu {
	@Expose
	@ConfigOption(
		name = "Enable Improved SB Menus",
		desc = "Change the way that skyblock menus (eg. /sbmenu) look"
	)
	@ConfigEditorBoolean
	public boolean enableSbMenus = true;

	@Expose
	@ConfigOption(
		name = "Menu Background Style",
		desc = "Change the style of the background of skyblock menus"
	)
	@ConfigEditorDropdown(
		values = {
			"Dark 1", "Dark 2", "Transparent", "Light 1", "Light 2", "Light 3",
			"Unused 1", "Unused 2", "Unused 3", "Unused 4"
		}
	)
	public int backgroundStyle = 0;

	@Expose
	@ConfigOption(
		name = "Button Background Style",
		desc = "Change the style of the foreground elements in skyblock menus"
	)
	@ConfigEditorDropdown(
		values = {
			"Dark 1", "Dark 2", "Transparent", "Light 1", "Light 2", "Light 3",
			"Unused 1", "Unused 2", "Unused 3", "Unused 4"
		}
	)
	public int buttonStyle = 0;

	@Expose
	@ConfigOption(
		name = "Hide Empty Tooltips",
		desc = "Hide the tooltips of glass panes with no text"
	)
	@ConfigEditorBoolean
	public boolean hideEmptyPanes = true;
}
