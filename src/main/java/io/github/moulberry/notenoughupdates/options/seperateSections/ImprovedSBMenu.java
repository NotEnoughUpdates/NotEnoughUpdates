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
import io.github.moulberry.moulconfig.annotations.ConfigEditorDropdown;
import io.github.moulberry.moulconfig.annotations.ConfigOption;

public class ImprovedSBMenu {
	@Expose
	@ConfigOption(
		name = "Enable Improved SB Menus",
		desc = "Change the way that SkyBlock menus (eg. /sbmenu) look"
	)
	@ConfigEditorBoolean
	public boolean enableSbMenus = true;

	@Expose
	@ConfigOption(
		name = "Menu Background Style",
		desc = "Change the style of the background of SkyBlock menus"
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
		desc = "Change the style of the foreground elements in SkyBlock menus"
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
