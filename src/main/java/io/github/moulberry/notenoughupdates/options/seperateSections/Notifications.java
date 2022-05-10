package io.github.moulberry.notenoughupdates.options.seperateSections;

import com.google.gson.annotations.Expose;
import io.github.moulberry.notenoughupdates.core.config.annotations.ConfigEditorBoolean;
import io.github.moulberry.notenoughupdates.core.config.annotations.ConfigEditorSlider;
import io.github.moulberry.notenoughupdates.core.config.annotations.ConfigOption;

public class Notifications {
	@Expose
	@ConfigOption(
		name = "Update Messages",
		desc = "Give a notification in chat whenever a new version of NEU is released"
	)
	@ConfigEditorBoolean
	public boolean showUpdateMsg = true;

	@Expose
	@ConfigOption(
		name = "RAM Warning",
		desc = "Warning when game starts with lots of RAM allocated\n" +
			"\u00a7cBefore disabling this, please seriously read the message. If you complain about FPS issues without listening to the warning, that's your fault."
	)
	@ConfigEditorBoolean
	public boolean doRamNotif = true;

	@Expose
	@ConfigOption(
		name = "OldAnimations warning",
		desc = "Warning when an unsupported OldAnimations mod is used"
	)
	@ConfigEditorBoolean
	public boolean doOamNotif = true;

	@Expose
	@ConfigOption(
		name = "Booster Cookie Warning",
		desc = "Warning when a booster cookie is about to expire"
	)
	@ConfigEditorBoolean
	public boolean doBoosterNotif = false;

	@Expose
	@ConfigOption(
		name = "Booster Cookie Warning Minutes",
		desc = "Change the minimum time required for the Booster Cookie warning to activate"
	)
	@ConfigEditorSlider(
		minValue = 10,
		maxValue = 5760,
		minStep = 25
	)
	public int boosterCookieWarningMins = 1440;
}
