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

public class CustomArmour {

	@Expose
	@ConfigOption(
		name = "Enable Equipment Hud",
		desc = "Shows an overlay in your inventory showing your 4 extra armour slots\n" +
			"\u00A7cRequires Hide Potion Effects to be enabled"
	)
	@ConfigEditorBoolean
	public boolean enableArmourHud = true;

	@Expose
	@ConfigOption(
		name = "Click To Open Equipment Menu",
		desc = "Click on the hud to open /equipment"
	)
	@ConfigEditorBoolean
	public boolean sendWardrobeCommand = true;

	@Expose
	@ConfigOption(
		name = "GUI Style",
		desc = "Change the colour of the GUI"
	)
	@ConfigEditorDropdown(
		values = {"Minecraft", "Grey", "PacksHQ Dark", "Transparent", "FSR"}
	)
	public int colourStyle = 0;

}
