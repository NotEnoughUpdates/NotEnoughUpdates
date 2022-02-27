package io.github.moulberry.notenoughupdates.options.seperateSections;

import com.google.gson.annotations.Expose;
import io.github.moulberry.notenoughupdates.core.config.annotations.ConfigEditorBoolean;
import io.github.moulberry.notenoughupdates.core.config.annotations.ConfigEditorDropdown;
import io.github.moulberry.notenoughupdates.core.config.annotations.ConfigOption;

public class CustomArmour {

	@Expose
	@ConfigOption(
		name = "Enable Custom Armour Hud",
		desc = "Shows an overlay in your inventory showing your 4 extra armour slots"
	)
	@ConfigEditorBoolean
	public boolean enableArmourHud = true;

	@Expose
	@ConfigOption(
		name = "Click To Open Wardrobe",
		desc = "Click on the hud to open /wardrobe"
	)
	@ConfigEditorBoolean
	public boolean sendWardrobeCommand = true;

	@Expose
	@ConfigOption(
		name = "GUI Colour",
		desc = "Change the colour of the GUI"
	)
	@ConfigEditorDropdown(
		values = {"Vanilla", "Grey", "Dark", "Transparent", "FSR"}
	)
	public int colourStyle = 0;

}
