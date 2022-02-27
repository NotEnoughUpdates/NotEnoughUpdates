package io.github.moulberry.notenoughupdates.options.seperateSections;

import com.google.gson.annotations.Expose;
import io.github.moulberry.notenoughupdates.core.config.annotations.ConfigEditorBoolean;
import io.github.moulberry.notenoughupdates.core.config.annotations.ConfigEditorButton;
import io.github.moulberry.notenoughupdates.core.config.annotations.ConfigEditorDropdown;
import io.github.moulberry.notenoughupdates.core.config.annotations.ConfigOption;

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
}
