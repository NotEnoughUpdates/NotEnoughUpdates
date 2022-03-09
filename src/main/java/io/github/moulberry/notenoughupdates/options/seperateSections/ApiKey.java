package io.github.moulberry.notenoughupdates.options.seperateSections;

import com.google.gson.annotations.Expose;
import io.github.moulberry.notenoughupdates.core.config.annotations.ConfigEditorText;
import io.github.moulberry.notenoughupdates.core.config.annotations.ConfigOption;

public class ApiKey {
	@Expose
	@ConfigOption(
		name = "Api Key",
		desc = "Hypixel api key"
	)
	@ConfigEditorText
	public String apiKey = "";
}
