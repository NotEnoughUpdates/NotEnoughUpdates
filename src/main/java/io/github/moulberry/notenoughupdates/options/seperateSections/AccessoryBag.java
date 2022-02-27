package io.github.moulberry.notenoughupdates.options.seperateSections;

import com.google.gson.annotations.Expose;
import io.github.moulberry.notenoughupdates.core.config.annotations.ConfigEditorBoolean;
import io.github.moulberry.notenoughupdates.core.config.annotations.ConfigOption;

public class AccessoryBag {
	@Expose
	@ConfigOption(
		name = "Enable Accessory Bag Overlay",
		desc = "Show an overlay on the accessory bag screen which gives useful information about your accessories"
	)
	@ConfigEditorBoolean
	public boolean enableOverlay = true;
}
