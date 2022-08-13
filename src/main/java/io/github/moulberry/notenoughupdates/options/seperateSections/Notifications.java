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
import io.github.moulberry.notenoughupdates.core.config.annotations.ConfigEditorBoolean;
import io.github.moulberry.notenoughupdates.core.config.annotations.ConfigEditorDropdown;
import io.github.moulberry.notenoughupdates.core.config.annotations.ConfigEditorSlider;
import io.github.moulberry.notenoughupdates.core.config.annotations.ConfigOption;

public class Notifications {
	@Expose
	@ConfigOption(
		name = "Update Messages",
		desc = "Give a notification in chat whenever a new version of NEU is released"
	)
	@ConfigEditorDropdown(values = {"Off", "Releases", "Pre-Releases"})
	public int updateChannel = 1;

	@Expose
	@ConfigOption(
		name = "Missing repo warning",
		desc = "Warning when repo data is missing or out of date"
	)
	@ConfigEditorBoolean
	public boolean outdatedRepo = true;

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
		name = "OldAnimations Warning",
		desc = "Warning when an unsupported OldAnimations mod is used"
	)
	@ConfigEditorBoolean
	public boolean doOamNotif = true;

	@Expose
	@ConfigOption(
		name = "Fast Render Warning",
		desc = "\u00a7cIf and ONLY if you have Fast Render disabled and are still seeing the warning, you can disable it here.\nDisabling it with Fast Render still on will lead to broken features."
	)
	@ConfigEditorBoolean
	public boolean doFastRenderNotif = true;

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
