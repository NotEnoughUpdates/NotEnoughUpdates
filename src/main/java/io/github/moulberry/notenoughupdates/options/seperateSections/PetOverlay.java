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
import io.github.moulberry.notenoughupdates.core.config.Position;
import io.github.moulberry.notenoughupdates.core.config.annotations.ConfigEditorBoolean;
import io.github.moulberry.notenoughupdates.core.config.annotations.ConfigEditorDraggableList;
import io.github.moulberry.notenoughupdates.core.config.annotations.ConfigEditorDropdown;
import io.github.moulberry.notenoughupdates.core.config.annotations.ConfigOption;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PetOverlay {
	@Expose
	@ConfigOption(
		name = "Enable Pet Info Overlay",
		desc = "Shows current active pet and pet exp on screen."
	)
	@ConfigEditorBoolean
	public boolean enablePetInfo = false;

	@Expose
	public Position petInfoPosition = new Position(-1, -1);

	@Expose
	@ConfigOption(
		name = "Pet Overlay Text",
		desc = "§eDrag text to change the appearance of the overlay\n" +
			"§rEquip a pet to show the overlay"
	)
	@ConfigEditorDraggableList(
		exampleText = {
			"§a[Lvl 37] §fRock",
			"§b2,312.9/2,700§e (85.7%)",
			"§b2.3k/2.7k§e (85.7%)",
			"§bXP/h: §e27,209",
			"§bTotal XP: §e30,597.9",
			"§bHeld Item: §fMining Exp Boost",
			"§bUntil L38: §e5m13s",
			"§bUntil L100: §e2d13h"
		}
	)
	public List<Integer> petOverlayText = new ArrayList<>(Arrays.asList(0, 2, 3, 6, 4));

	@Expose
	@ConfigOption(
		name = "Pet Overlay Icon",
		desc = "Show the icon of the pet you have equiped in the overlay"
	)
	@ConfigEditorBoolean
	public boolean petOverlayIcon = true;

	@Expose
	@ConfigOption(
		name = "Pet Info Overlay Style",
		desc = "Change the style of the Pet Info overlay"
	)
	@ConfigEditorDropdown(
		values = {"Background", "No Shadow", "Shadow", "Full Shadow"}
	)
	public int petInfoOverlayStyle = 0;

	@Expose
	@ConfigOption(
		name = "Show Last Pet",
		desc = "Show 2 pets on the overlay\nUseful if training two pets at once with autopet"
	)
	@ConfigEditorBoolean
	public boolean dualPets = false;

	@Expose
	@ConfigOption(
		name = "Pet Inventory Display",
		desc = "Shows an overlay in your inventory showing your current pet"
	)
	@ConfigEditorBoolean
	public boolean petInvDisplay = false;

	@Expose
	@ConfigOption(
		name = "GUI Style",
		desc = "Change the colour of the GUI",
		searchTags = "color"
	)
	@ConfigEditorDropdown(
		values = {"Minecraft", "Grey", "PacksHQ Dark", "Transparent", "FSR"}
	)
	public int colourStyle = 0;

	@Expose
	@ConfigOption(
		name = "Click To Open Pets",
		desc = "Click on the hud to open /pets"
	)
	@ConfigEditorBoolean
	public boolean sendPetsCommand = true;

	@Expose
	@ConfigOption(
		name = "Hide Pet Inventory Tooltip",
		desc = "Hides the tooltip of your active in your inventory"
	)
	@ConfigEditorBoolean
	public boolean hidePetTooltip = false;

	@Expose
	@ConfigOption(
		name = "Show upgraded Pet Level",
		desc = "Show the estimated pet level after an upgrade at Kats"
	)
	@ConfigEditorBoolean
	public boolean showKatSitting = true;
}
