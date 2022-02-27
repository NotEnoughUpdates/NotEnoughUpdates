package io.github.moulberry.notenoughupdates.options.seperateSections;

import com.google.gson.annotations.Expose;
import io.github.moulberry.notenoughupdates.core.config.annotations.ConfigEditorBoolean;
import io.github.moulberry.notenoughupdates.core.config.annotations.ConfigEditorSlider;
import io.github.moulberry.notenoughupdates.core.config.annotations.ConfigOption;

public class Calendar {
	@Expose
	@ConfigOption(
		name = "Event Notifications",
		desc = "Display notifications for skyblock calendar events"
	)
	@ConfigEditorBoolean
	public boolean eventNotifications = true;

	@Expose
	@ConfigOption(
		name = "Starting Soon Time",
		desc = "Display a notification before events start, time in seconds.\n" +
			"0 = No prior notification"
	)
	@ConfigEditorSlider(
		minValue = 0f,
		maxValue = 600f,
		minStep = 30f
	)
	public int startingSoonTime = 300;

	@Expose
	@ConfigOption(
		name = "Timer In Inventory",
		desc = "Displays the time until the next event at the top of your screen when in inventories"
	)
	@ConfigEditorBoolean
	public boolean showEventTimerInInventory = true;

	@Expose
	@ConfigOption(
		name = "Notification Sounds",
		desc = "Play a sound whenever events start"
	)
	@ConfigEditorBoolean
	public boolean eventNotificationSounds = true;

	@Expose
	@ConfigOption(
		name = "Spooky Night Notification",
		desc = "Send a notification during spooky event when the time reaches 7pm"
	)
	@ConfigEditorBoolean
	public boolean spookyNightNotification = true;
}
