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
import io.github.moulberry.moulconfig.annotations.ConfigAccordionId;
import io.github.moulberry.moulconfig.annotations.ConfigEditorAccordion;
import io.github.moulberry.moulconfig.annotations.ConfigEditorBoolean;
import io.github.moulberry.moulconfig.annotations.ConfigEditorKeybind;
import io.github.moulberry.moulconfig.annotations.ConfigOption;
import org.lwjgl.input.Keyboard;

public class WardrobeKeybinds {

	@Expose
	@ConfigOption(
		name = "Enable Wardrobe Keybinds",
		desc = "Lets you use your number keys to quickly change your wardrobe"
	)
	@ConfigEditorBoolean
	public boolean enableWardrobeKeybinds = false;

	@ConfigOption(
		name = "Wardrobe Keybinds",
		desc = ""
	)
	@ConfigEditorAccordion(id = 2)
	public boolean wardrobeKeybindAccordion = false;
	@Expose
	@ConfigOption(
		name = "Slot 1",
		desc = "Keybind to toggle the first set in your wardrobe"
	)
	@ConfigEditorKeybind(defaultKey = Keyboard.KEY_1)
	@ConfigAccordionId(id = 2)
	public int wardrobeSlot1 = Keyboard.KEY_1;

	@Expose
	@ConfigOption(
		name = "Slot 2",
		desc = "Keybind to toggle the second set in your wardrobe"
	)
	@ConfigEditorKeybind(defaultKey = Keyboard.KEY_2)
	@ConfigAccordionId(id = 2)
	public int wardrobeSlot2 = Keyboard.KEY_2;

	@Expose
	@ConfigOption(
		name = "Slot 3",
		desc = "Keybind to toggle the third set in your wardrobe"
	)
	@ConfigEditorKeybind(defaultKey = Keyboard.KEY_3)
	@ConfigAccordionId(id = 2)
	public int wardrobeSlot3 = Keyboard.KEY_3;

	@Expose
	@ConfigOption(
		name = "Slot 4",
		desc = "Keybind to toggle the fourth set in your wardrobe"
	)
	@ConfigEditorKeybind(defaultKey = Keyboard.KEY_4)
	@ConfigAccordionId(id = 2)
	public int wardrobeSlot4 = Keyboard.KEY_4;

	@Expose
	@ConfigOption(
		name = "Slot 5",
		desc = "Keybind to toggle the fifth set in your wardrobe"
	)
	@ConfigEditorKeybind(defaultKey = Keyboard.KEY_5)
	@ConfigAccordionId(id = 2)
	public int wardrobeSlot5 = Keyboard.KEY_5;

	@Expose
	@ConfigOption(
		name = "Slot 6",
		desc = "Keybind to toggle the sixth set in your wardrobe"
	)
	@ConfigEditorKeybind(defaultKey = Keyboard.KEY_6)
	@ConfigAccordionId(id = 2)
	public int wardrobeSlot6 = Keyboard.KEY_6;

	@Expose
	@ConfigOption(
		name = "Slot 7",
		desc = "Keybind to toggle the seventh set in your wardrobe"
	)
	@ConfigEditorKeybind(defaultKey = Keyboard.KEY_7)
	@ConfigAccordionId(id = 2)
	public int wardrobeSlot7 = Keyboard.KEY_7;

	@Expose
	@ConfigOption(
		name = "Slot 8",
		desc = "Keybind to toggle the eighth set in your wardrobe"
	)
	@ConfigEditorKeybind(defaultKey = Keyboard.KEY_8)
	@ConfigAccordionId(id = 2)
	public int wardrobeSlot8 = Keyboard.KEY_8;

	@Expose
	@ConfigOption(
		name = "Slot 9",
		desc = "Keybind to toggle the ninth set in your wardrobe"
	)
	@ConfigEditorKeybind(defaultKey = Keyboard.KEY_9)
	@ConfigAccordionId(id = 2)
	public int wardrobeSlot9 = Keyboard.KEY_9;
}
