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
import io.github.moulberry.moulconfig.annotations.ConfigEditorBoolean;
import io.github.moulberry.moulconfig.annotations.ConfigEditorDraggableList;
import io.github.moulberry.moulconfig.annotations.ConfigEditorDropdown;
import io.github.moulberry.moulconfig.annotations.ConfigOption;

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
		desc = "\u00a7eDrag text to change the appearance of the overlay\n" +
			"\u00a7rEquip a pet to show the overlay"
	)
	@ConfigEditorDraggableList(
		exampleText = {
			"\u00a7a[Lvl 37] \u00a7fRock",
			"\u00a7b2,312.9/2,700\u00a7e (85.7%)",
			"\u00a7b2.3k/2.7k\u00a7e (85.7%)",
			"\u00a7bXP/h: \u00a7e27,209",
			"\u00a7bTotal XP: \u00a7e30,597.9",
			"\u00a7bHeld Item: \u00a7fMining Exp Boost",
			"\u00a7bUntil L38: \u00a7e5m13s",
			"\u00a7bUntil L100: \u00a7e2d13h"
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
		desc = "Change the colour of the GUI"
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

	@Expose
	@ConfigOption(
		name = "Hide Pet Level Progress",
		desc = "Hide the pet level progress information for maxed out pets."
	)
	@ConfigEditorBoolean
	public boolean hidePetLevelProgress = false;
}
