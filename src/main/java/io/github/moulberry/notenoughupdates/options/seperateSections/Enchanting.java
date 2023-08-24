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
import io.github.moulberry.moulconfig.annotations.ConfigEditorDropdown;
import io.github.moulberry.moulconfig.annotations.ConfigOption;

public class Enchanting {
	@ConfigOption(
		name = "Enchant Table / Hex GUI",
		desc = ""
	)
	@ConfigEditorAccordion(id = 1)
	public boolean tableGUIAccordion = false;

	@Expose
	@ConfigOption(
		name = "Enable Enchant Table GUI",
		desc = "Show a custom GUI when using the Enchant Table"
	)
	@ConfigEditorBoolean
	@ConfigAccordionId(id = 1)
	public boolean enableTableGUI = true;

	@Expose
	@ConfigOption(
		name = "Enable Hex GUI",
		desc = "Show a custom GUI when using the Hex"
	)
	@ConfigEditorBoolean
	@ConfigAccordionId(id = 1)
	public boolean enableHexGUI = true;

	/*@Expose
	@ConfigOption(
		name = "Incompatible Enchants",
		desc = "How to display enchants that are incompatible with your current item, eg. Smite on a sword with Sharpness"
	)
	@ConfigEditorDropdown(
		values = {"Highlight", "Hide"}
	)
	@ConfigAccordionId(id = 1)
	public int incompatibleEnchants = 0;*/

	@Expose
	@ConfigOption(
		name = "Enchant Sorting",
		desc = "Change the method of sorting enchants in the GUI"
	)
	@ConfigEditorDropdown(
		values = {"By Cost", "Alphabetical"}
	)
	@ConfigAccordionId(id = 1)
	public int enchantSorting = 0;

	@Expose
	@ConfigOption(
		name = "Enchant Ordering",
		desc = "Change the method of ordering used by the sort"
	)
	@ConfigEditorDropdown(
		values = {"Ascending", "Descending"}
	)
	@ConfigAccordionId(id = 1)
	public int enchantOrdering = 0;

	@Expose
	@ConfigOption(
		name = "Use highest level from /et in /hex",
		desc = "Show max level from /et in hex instead of highest possible"
	)
	@ConfigEditorBoolean()
	@ConfigAccordionId(id = 1)
	public boolean maxEnchLevel = false;

	@ConfigOption(
		name = "Enchanting Solvers",
		desc = ""
	)
	@ConfigEditorAccordion(id = 0)
	public boolean enchantingSolversAccordion = false;

	@Expose
	@ConfigOption(
		name = "Enable Solvers",
		desc = "Turn on solvers for the experimentation table"
	)
	@ConfigEditorBoolean
	@ConfigAccordionId(id = 0)
	public boolean enableEnchantingSolvers = true;
	//In an email from Donpireso (admin) he says not sending a packet at all isn't bannable
	//https://cdn.discordapp.com/attachments/823769568933576764/906101631861526559/unknown.png
	@Expose
	@ConfigOption(
		name = "Prevent Misclicks",
		desc = "Prevent accidentally failing the Chronomatron and Ultrasequencer experiments"
	)
	@ConfigEditorBoolean
	@ConfigAccordionId(id = 0)
	public boolean preventMisclicks1 = false;

	@Expose
	@ConfigOption(
		name = "Hide Tooltips",
		desc = "Hide the tooltip of items in the Chronomatron and Ultrasequencer experiments"
	)
	@ConfigEditorBoolean
	@ConfigAccordionId(id = 0)
	public boolean hideTooltips = true;

	@Expose
	@ConfigOption(
		name = "Ultrasequencer Numbers",
		desc = "Replace the items in the Ultrasequencer with only numbers"
	)
	@ConfigEditorBoolean
	@ConfigAccordionId(id = 0)
	public boolean seqNumbers = false;

	@Expose
	@ConfigOption(
		name = "Show Next Click In Chronomatron",
		desc = "Shows what block you need to click next in Chronomatron"
	)
	@ConfigEditorBoolean
	@ConfigAccordionId(id = 0)
	public boolean showNextClick = true;

	@Expose
	@ConfigOption(
		name = "Hide Buttons",
		desc = "Hide Inventory Buttons and Quick Commands while in the experimentation table"
	)
	@ConfigEditorBoolean
	@ConfigAccordionId(id = 0)
	public boolean hideButtons = false;

	@Expose
	@ConfigOption(
		name = "Ultrasequencer Next",
		desc = "Set the colour of the glass pane shown behind the element in the ultrasequencer which is next"
	)
	@ConfigEditorDropdown(
		values = {
			"None", "White", "Orange", "Light Purple", "Light Blue", "Yellow", "Light Green", "Pink",
			"Gray", "Light Gray", "Cyan", "Dark Purple", "Dark Blue", "Brown", "Dark Green", "Red", "Black"
		}
	)
	@ConfigAccordionId(id = 0)
	public int seqNext = 6;

	@Expose
	@ConfigOption(
		name = "Ultrasequencer Upcoming",
		desc = "Set the colour of the glass pane shown behind the element in the ultrasequencer which is coming after \"next\""
	)
	@ConfigEditorDropdown(
		values = {
			"None", "White", "Orange", "Light Purple", "Light Blue", "Yellow", "Light Green", "Pink",
			"Gray", "Light Gray", "Cyan", "Dark Purple", "Dark Blue", "Brown", "Dark Green", "Red", "Black"
		}
	)
	@ConfigAccordionId(id = 0)
	public int seqUpcoming = 5;

	@Expose
	@ConfigOption(
		name = "Superpairs Matched",
		desc = "Set the colour of the glass pane shown behind successfully matched pairs"
	)
	@ConfigEditorDropdown(
		values = {
			"None", "White", "Orange", "Light Purple", "Light Blue", "Yellow", "Light Green", "Pink",
			"Gray", "Light Gray", "Cyan", "Dark Purple", "Dark Blue", "Brown", "Dark Green", "Red", "Black"
		}
	)
	@ConfigAccordionId(id = 0)
	public int supMatched = 6;

	@Expose
	@ConfigOption(
		name = "Superpairs Possible",
		desc = "Set the colour of the glass pane shown behind pairs which can be matched, but have not yet"
	)
	@ConfigEditorDropdown(
		values = {
			"None", "White", "Orange", "Light Purple", "Light Blue", "Yellow", "Light Green", "Pink",
			"Gray", "Light Gray", "Cyan", "Dark Purple", "Dark Blue", "Brown", "Dark Green", "Red", "Black"
		}
	)
	@ConfigAccordionId(id = 0)
	public int supPossible = 2;

	@Expose
	@ConfigOption(
		name = "Superpairs Unmatched",
		desc = "Set the colour of the glass pane shown behind pairs which have been previously uncovered"
	)
	@ConfigEditorDropdown(
		values = {
			"None", "White", "Orange", "Light Purple", "Light Blue", "Yellow", "Light Green", "Pink",
			"Gray", "Light Gray", "Cyan", "Dark Purple", "Dark Blue", "Brown", "Dark Green", "Red", "Black"
		}
	)
	@ConfigAccordionId(id = 0)
	public int supUnmatched = 5;

	@Expose
	@ConfigOption(
		name = "Superpairs Powerups",
		desc = "Set the colour of the glass pane shown behind powerups"
	)
	@ConfigEditorDropdown(
		values = {
			"None", "White", "Orange", "Light Purple", "Light Blue", "Yellow", "Light Green", "Pink",
			"Gray", "Light Gray", "Cyan", "Dark Purple", "Dark Blue", "Brown", "Dark Green", "Red", "Black"
		}
	)
	@ConfigAccordionId(id = 0)
	public int supPower = 11;
}
