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
import io.github.moulberry.moulconfig.annotations.ConfigEditorButton;
import io.github.moulberry.moulconfig.annotations.ConfigEditorColour;
import io.github.moulberry.moulconfig.annotations.ConfigEditorDropdown;
import io.github.moulberry.moulconfig.annotations.ConfigEditorInfoText;
import io.github.moulberry.moulconfig.annotations.ConfigEditorSlider;
import io.github.moulberry.moulconfig.annotations.ConfigOption;

public class Dungeons {

	@ConfigOption(
		name = "Dungeon Map",
		desc = ""
	)
	@ConfigEditorAccordion(id = 0)
	public boolean dungeonMapAccordion = false;

	@Expose
	@ConfigOption(
		name = "Edit Dungeon Map",
		desc = "The NEU dungeon map has its own editor (/neumap).\n" +
			"Click the button on the left to open it"
	)
	@ConfigEditorButton(
		runnableId = 0,
		buttonText = "Edit"
	)
	@ConfigAccordionId(id = 0)
	public int editDungeonMap = 0;

	@Expose
	@ConfigOption(
		name = "Show Own Head As Marker",
		desc = "If you have the \"Head\" icon style selected, don't replace your green marker with a head"
	)
	@ConfigEditorBoolean
	@ConfigAccordionId(id = 0)
	public boolean showOwnHeadAsMarker = false;

	@ConfigOption(
		name = "Dungeon Profit",
		desc = ""
	)
	@ConfigEditorAccordion(id = 1)
	public boolean dungeonProfitAccordion = false;

	@Expose
	@ConfigOption(
		name = "Profit Type",
		desc = "Set the price dataset used for calculating profit"
	)
	@ConfigEditorDropdown(
		values = {"Lowest BIN", "24 AVG Lowest Bin", "Auction AVG"}
	)
	@ConfigAccordionId(id = 1)
	public int profitType = 0;

	@Expose
	@ConfigOption(
		name = "Profit Display Location",
		desc = "Set where the profit information is displayed\n" +
			"Overlay = Overlay on right side of inventory\n" +
			"GUI Title = Text displayed next to the inventory title\n" +
			"Lore = Inside the \"Open Reward Chest\" item"
	)
	@ConfigEditorDropdown(
		values = {"Overlay", "GUI Title", "Lore", "Off"}
	)
	@ConfigAccordionId(id = 1)
	public int profitDisplayLoc = 0;

	@Expose
	@ConfigOption(
		name = "Include Kismet Feather",
		desc = "Include Kismet Feathers in the Profit Calculation after rerolling"
	)
	@ConfigEditorBoolean
	@ConfigAccordionId(id = 1)
	public boolean useKismetOnDungeonProfit = true;

	@Expose
	@ConfigOption(
		name = "Include Essence Cost",
		desc = "Include Bazaar Essence Sell Cost in the Profit Calculation for Dungeon Chests"
	)
	@ConfigEditorBoolean
	@ConfigAccordionId(id = 1)
	public boolean useEssenceCostFromBazaar = true;

	@Expose
	@ConfigOption(
		name = "Warning if Derpy active",
		desc = "Shows a warning if the mayor Derpy is active"
	)
	@ConfigEditorBoolean
	@ConfigAccordionId(id = 1)
	public boolean shouldWarningDerpy = true;

	@ConfigOption(
		name = "Dungeon Win Overlay",
		desc = ""
	)
	@ConfigEditorAccordion(id = 3)
	public boolean dungeonWinAccordion = false;

	@Expose
	@ConfigOption(
		name = "Enable Dungeon Win",
		desc = "Show a fancy win screen and stats when completing a dungeon"
	)
	@ConfigEditorBoolean
	@ConfigAccordionId(id = 3)
	public boolean enableDungeonWin = true;

	@Expose
	@ConfigOption(
		name = "Dungeon Win Time",
		desc = "Change the amount of time (milliseconds) that the win screen shows for"
	)
	@ConfigEditorSlider(
		minValue = 0,
		maxValue = 20000,
		minStep = 500
	)
	@ConfigAccordionId(id = 3)
	public int dungeonWinMillis = 8000;

	@ConfigOption(
		name = "Dungeon Block Overlay",
		desc = ""
	)

	@ConfigEditorAccordion(id = 2)
	public boolean dungeonBlocksAccordion = false;

	@ConfigOption(
		name = "\u00A7cWarning",
		desc = "You need Fast Render and Antialiasing off for these settings to work\n" +
			"You can find these in your video settings"
	)
	@ConfigEditorInfoText()
	@ConfigAccordionId(id = 2)
	public boolean dungeonBlockWarning = false;

	@Expose
	@ConfigOption(
		name = "Enable Block Overlay",
		desc = "Change the colour of certain blocks / entities while inside dungeons, but keeps the normal texture outside of dungeons"
	)
	@ConfigEditorBoolean
	@ConfigAccordionId(id = 2)
	public boolean enableDungBlockOverlay = true;

	@Expose
	@ConfigOption(
		name = "Show Overlay Everywhere",
		desc = "Show the dungeon block overlay even when not inside dungeons. Should only be used for testing."
	)
	@ConfigEditorBoolean
	@ConfigAccordionId(id = 2)
	public boolean dungeonBlocksEverywhere = false;

	@Expose
	@ConfigOption(
		name = "Slow Update",
		desc = "Updates the colour every second instead of every tick.\n" +
			"\u00A7cWARNING: This will cause all texture animations (eg. flowing water) to update slowly.\n" +
			"This should only be used on low-end machines"
	)
	@ConfigEditorBoolean
	@ConfigAccordionId(id = 2)
	public boolean slowDungeonBlocks = false;

	@Expose
	@ConfigOption(
		name = "Cracked Bricks",
		desc = "Change the colour of: Cracked Bricks"
	)
	@ConfigEditorColour
	@ConfigAccordionId(id = 2)
	public String dungCrackedColour = "0:255:7:255:217";

	@Expose
	@ConfigOption(
		name = "Dispensers",
		desc = "Change the colour of: Dispensers"
	)
	@ConfigEditorColour
	@ConfigAccordionId(id = 2)
	public String dungDispenserColour = "0:255:255:76:0";

	@Expose
	@ConfigOption(
		name = "Levers",
		desc = "Change the colour of: Levers"
	)
	@ConfigEditorColour
	@ConfigAccordionId(id = 2)
	public String dungLeverColour = "0:252:24:249:255";

	@Expose
	@ConfigOption(
		name = "Tripwire String",
		desc = "Change the colour of: Tripwire String"
	)
	@ConfigEditorColour
	@ConfigAccordionId(id = 2)
	public String dungTripWireColour = "0:255:255:0:0";

	@Expose
	@ConfigOption(
		name = "Normal Chests",
		desc = "Change the colour of: Normal Chests"
	)
	@ConfigEditorColour
	@ConfigAccordionId(id = 2)
	public String dungChestColour = "0:255:0:163:36";

	@Expose
	@ConfigOption(
		name = "Trapped Chests",
		desc = "Change the colour of: Trapped Chests"
	)
	@ConfigEditorColour
	@ConfigAccordionId(id = 2)
	public String dungTrappedChestColour = "0:255:0:163:36";

	@Expose
	@ConfigOption(
		name = "Bats",
		desc = "Change the colour of: Bats"
	)
	@ConfigEditorColour
	@ConfigAccordionId(id = 2)
	public String dungBatColour = "0:255:12:255:0";

	@ConfigOption(
		name = "Croesus Overlay",
		desc = ""
	)
	@ConfigEditorAccordion(id = 4)
	public boolean croesusAccordion = false;

	@Expose
	@ConfigOption(
		name = "Enable Croesus Overlay",
		desc = "Shows a profit overlay next to your inventory when viewing chest previews at the Croesus NPC"
	)
	@ConfigEditorBoolean
	@ConfigAccordionId(id = 4)
	public boolean croesusProfitOverlay = true;

	@Expose
	@ConfigOption(
		name = "Sort by profit",
		desc = "Lists the chest by profit (descending)"
	)
	@ConfigEditorBoolean
	@ConfigAccordionId(id = 4)
	public boolean croesusSortByProfit = true;

	@Expose
	@ConfigOption(
		name = "Highlight highest profit",
		desc = "Highlight the chest which has the most profit"
	)
	@ConfigEditorBoolean
	@ConfigAccordionId(id = 4)
	public boolean croesusHighlightHighestProfit = true;

}
