/*
 * Copyright (C) 2022 NotEnoughUpdates contributors
 *
 * This file is part of NotEnoughUpdates.
 *
 * NotEnoughUpdates is free software: you can redistribute it
 * and/or modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * NotEnoughUpdates is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with NotEnoughUpdates. If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.moulberry.notenoughupdates.options.seperateSections;

import com.google.gson.annotations.Expose;
import io.github.moulberry.moulconfig.annotations.ConfigEditorBoolean;
import io.github.moulberry.moulconfig.annotations.ConfigEditorSlider;
import io.github.moulberry.moulconfig.annotations.ConfigOption;

public class Calendar {
	@Expose
	@ConfigOption(
		name = "Event Notifications",
		desc = "Display notifications for SkyBlock calendar events"
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
