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

package io.github.moulberry.notenoughupdates.options.separatesections;

import com.google.gson.annotations.Expose;
import io.github.moulberry.notenoughupdates.core.config.Position;
import io.github.moulberry.moulconfig.annotations.ConfigAccordionId;
import io.github.moulberry.moulconfig.annotations.ConfigEditorAccordion;
import io.github.moulberry.moulconfig.annotations.ConfigEditorBoolean;
import io.github.moulberry.moulconfig.annotations.ConfigEditorDraggableList;
import io.github.moulberry.moulconfig.annotations.ConfigEditorDropdown;
import io.github.moulberry.moulconfig.annotations.ConfigOption;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MiscOverlays {
	@ConfigOption(
		name = "Todo Overlay",
		desc = ""
	)
	@ConfigEditorAccordion(id = 0)
	public boolean todoAccordion = true;

	@Expose
	@ConfigOption(
		name = "Enable Todo Overlay",
		desc = "Show an overlay that reminds you to do important tasks"
	)
	@ConfigEditorBoolean
	@ConfigAccordionId(id = 0)
	public boolean todoOverlay2 = false;

	@Expose
	@ConfigOption(
		name = "Todo Overlay Tab",
		desc = "Only show the todo overlay when tab list is open"
	)
	@ConfigEditorBoolean
	@ConfigAccordionId(id = 0)
	public boolean todoOverlayOnlyShowTab = false;

	@Expose
	@ConfigOption(
		name = "Todo Overlay Hide Bingo",
		desc = "Hide some tasks from the todo overlay while on a bingo profile: Cookie Buff, Godpot, Heavy Pearls, Crimson Isle Quests"
	)
	@ConfigEditorBoolean
	@ConfigAccordionId(id = 0)
	public boolean todoOverlayHideAtBingo = true;

	@Expose
	@ConfigOption(
		name = "Todo Text",
		desc = "§eDrag text to change the appearance of the overlay\n" +
			"§rIf you want to see the time until something is available, click \"Add\" and then the respective timer"
	)
	@ConfigEditorDraggableList(
		exampleText = {
			"§3Cakes: §e1d21h",
			"§3Cookie Buff: §e2d23h",
			"§3Godpot: §e19h",
			"§3Puzzler: §e13h",
			"§3Fetchur: §e3h38m",
			"§3Commissions: §e3h38m",
			"§3Experiments: §e3h38m",
			"§3Mithril Powder: §e3h38m",
			"§3Gemstone Powder: §e3h38m",
			"§3Heavy Pearls: §e3h38m",
			"§3Crimson Isle Quests: §e3h38m",
			"§3NPC Buy Daily Limit: §e3h38m",
			"§3Free Rift Infusion: §e3h38m",
		}
	)
	@ConfigAccordionId(id = 0)
	public List<Integer> todoText2 = new ArrayList<>(Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12));

	@ConfigOption(
		name = "Show Only If Soon",
		desc = ""
	)
	@ConfigAccordionId(id = 0)
	@ConfigEditorAccordion(id = 1)
	public boolean TodoAccordion = false;

	@Expose
	@ConfigOption(
		name = "Experimentation Display",
		desc = "Change the way the experimentation timer displays\n" +
			"Only when ready, When very Soon, When soon, When kinda soon or always."
	)
	@ConfigAccordionId(id = 1)
	@ConfigEditorDropdown(
		values = {"Only when ready", "When very Soon", "When soon", "When Kinda Soon", "Always"}
	)
	public int experimentationDisplay = 0;

	@Expose
	@ConfigOption(
		name = "Puzzler Reset Display",
		desc = "Change the way the puzzler reset timer displays\n" +
			"Only when ready, When very Soon, When soon, When kinda soon or always."
	)
	@ConfigAccordionId(id = 1)
	@ConfigEditorDropdown(
		values = {"Only when ready", "When very Soon", "When soon", "When Kinda Soon", "Always"}
	)
	public int puzzlerDisplay = 0;

	@Expose
	@ConfigOption(
		name = "Fetchur Reset Display",
		desc = "Change the way the fetchur reset timer displays\n" +
			"Only when ready, When very Soon, When soon, When kinda soon or always."
	)
	@ConfigAccordionId(id = 1)
	@ConfigEditorDropdown(
		values = {"Only when ready", "When very Soon", "When soon", "When Kinda Soon", "Always"}
	)
	public int fetchurDisplay = 0;

	@Expose
	@ConfigOption(
		name = "Commission timer Display",
		desc = "Change the way the Commission timer displays\n" +
			"Only when ready, When very Soon, When soon, When kinda soon or always."
	)
	@ConfigAccordionId(id = 1)
	@ConfigEditorDropdown(
		values = {"Only when ready", "When very Soon", "When soon", "When Kinda Soon", "Always"}
	)
	public int commissionDisplay = 0;

	@Expose
	@ConfigOption(
		name = "Cake Buff Display",
		desc = "Change the way the cake buff timer displays\n" +
			"Only when ready, When very Soon, When soon, When kinda soon or always."
	)
	@ConfigAccordionId(id = 1)
	@ConfigEditorDropdown(
		values = {"Only when ready", "When very Soon", "When soon", "When Kinda Soon", "Always"}
	)
	public int cakesDisplay = 0;

	@Expose
	@ConfigOption(
		name = "Cookie Buff Display",
		desc = "Change the way the cookie buff displays\n" +
			"Only when ready, When very Soon, When soon, When kinda soon or always."
	)
	@ConfigAccordionId(id = 1)
	@ConfigEditorDropdown(
		values = {"Only when ready", "When very Soon", "When soon", "When Kinda Soon", "Always"}
	)
	public int cookieBuffDisplay = 0;

	@Expose
	@ConfigOption(
		name = "God Pot Display",
		desc = "Change the way the god pot displays\n" +
			"Only when ready, When very Soon, When soon, When kinda soon or always."
	)
	@ConfigAccordionId(id = 1)
	@ConfigEditorDropdown(
		values = {"Only when ready", "When very Soon", "When soon", "When Kinda Soon", "Always"}
	)
	public int godpotDisplay = 0;

	@Expose
	@ConfigOption(
		name = "Mithril Powder Display",
		desc = "Change the way the mithril powder displays\n" +
			"Only when ready, When very Soon, When soon, When kinda soon or always."
	)
	@ConfigAccordionId(id = 1)
	@ConfigEditorDropdown(
		values = {"Only when ready", "When very Soon", "When soon", "When Kinda Soon", "Always"}
	)
	public int dailyMithrilPowderDisplay = 0;

	@Expose
	@ConfigOption(
		name = "Gemstone Powder Display",
		desc = "Change the way the gemstone powder displays\n" +
			"Only when ready, When very Soon, When soon, When kinda soon or always."
	)
	@ConfigAccordionId(id = 1)
	@ConfigEditorDropdown(
		values = {"Only when ready", "When very Soon", "When soon", "When Kinda Soon", "Always"}
	)
	public int dailyGemstonePowderDisplay = 0;

	@Expose
	@ConfigOption(
		name = "Heavy Pearl Display",
		desc = "Change the way the heavy pearl displays\n" +
			"Only when ready, When very Soon, When soon, When kinda soon or always."
	)
	@ConfigAccordionId(id = 1)
	@ConfigEditorDropdown(
		values = {"Only when ready", "When very Soon", "When soon", "When Kinda Soon", "Always"}
	)

	public int dailyHeavyPearlDisplay = 0;

	@Expose
	@ConfigOption(
		name = "Crimson Isle Quests Display",
		desc = "Change the way the crimson isle quests display\n" +
			"Only when ready, When very Soon, When soon, When kinda soon or always."
	)
	@ConfigAccordionId(id = 1)
	@ConfigEditorDropdown(
		values = {"Only when ready", "When very Soon", "When soon", "When Kinda Soon", "Always"}
	)
	public int questBoardDisplay = 0;

	@Expose
	@ConfigOption(
		name = "NPC Buy Daily Limit Display",
		desc = "Change the way the NPC shop limit displays\n" +
			"Only when ready, When very Soon, When soon, When kinda soon or always."
	)
	@ConfigAccordionId(id = 1)
	@ConfigEditorDropdown(
		values = {"Only when ready", "When very Soon", "When soon", "When Kinda Soon", "Always"}
	)
	public int shopLimitDisplay = 0;

	@Expose
	@ConfigOption(
		name = "Free Rift Infusion Display",
		desc = "Change the way the Free Rift infusion displays\n" +
			"Only when ready, When very Soon,  or always."
	)
	@ConfigAccordionId(id = 1)
	@ConfigEditorDropdown(
		values = {"Only when ready", "When very Soon", "Always"}
	)
	public int freeRiftInfusionDisplay = 0;



	@ConfigOption(
		name = "Colours",
		desc = ""
	)

	@ConfigEditorAccordion(id = 2)
	@ConfigAccordionId(id = 0)
	public boolean TodoColourAccordion = false;

	@Expose
	@ConfigOption(
		name = "Ready colour",
		desc = "Change the colour of when the timer is ready"

	)
	@ConfigEditorDropdown(
		values = {
			"Black",
			"Dark Blue",
			"Dark Green",
			"Dark Aqua",
			"Dark Red",
			"Dark Purple",
			"Gold",
			"Gray",
			"Dark Gray",
			"Blue",
			"Green",
			"Aqua",
			"Red",
			"Light Purple",
			"Yellow",
			"White"
		}
	)
	@ConfigAccordionId(id = 2)
	public int readyColour = 10;

	@Expose
	@ConfigOption(
		name = "Gone colour",
		desc = "Change the colour of when the timer is gone"

	)
	@ConfigEditorDropdown(
		values = {
			"Black",
			"Dark Blue",
			"Dark Green",
			"Dark Aqua",
			"Dark Red",
			"Dark Purple",
			"Gold",
			"Gray",
			"Dark Gray",
			"Blue",
			"Green",
			"Aqua",
			"Red",
			"Light Purple",
			"Yellow",
			"White"
		}
	)
	@ConfigAccordionId(id = 2)
	public int goneColour = 12;

	@Expose
	@ConfigOption(
		name = "Very soon colour",
		desc = "Change the colour of when the timer is almost ready/gone"

	)
	@ConfigEditorDropdown(
		values = {
			"Black",
			"Dark Blue",
			"Dark Green",
			"Dark Aqua",
			"Dark Red",
			"Dark Purple",
			"Gold",
			"Gray",
			"Dark Gray",
			"Blue",
			"Green",
			"Aqua",
			"Red",
			"Light Purple",
			"Yellow",
			"White"
		}
	)
	@ConfigAccordionId(id = 2)
	public int verySoonColour = 11;

	@Expose
	@ConfigOption(
		name = "Soon Colour",
		desc = "Change the colour of when the timer is soon ready/gone"

	)
	@ConfigEditorDropdown(
		values = {
			"Black",
			"Dark Blue",
			"Dark Green",
			"Dark Aqua",
			"Dark Red",
			"Dark Purple",
			"Gold",
			"Gray",
			"Dark Gray",
			"Blue",
			"Green",
			"Aqua",
			"Red",
			"Light Purple",
			"Yellow",
			"White"
		}
	)
	@ConfigAccordionId(id = 2)
	public int soonColour = 9;

	@Expose
	@ConfigOption(
		name = "Kinda Soon Colour",
		desc = "Change the colour of when the timer is kinda soon ready/gone"

	)
	@ConfigEditorDropdown(
		values = {
			"Black",
			"Dark Blue",
			"Dark Green",
			"Dark Aqua",
			"Dark Red",
			"Dark Purple",
			"Gold",
			"Gray",
			"Dark Gray",
			"Blue",
			"Green",
			"Aqua",
			"Red",
			"Light Purple",
			"Yellow",
			"White"
		}
	)
	@ConfigAccordionId(id = 2)
	public int kindaSoonColour = 1;

	@Expose
	@ConfigOption(
		name = "Default Colour",
		desc = "Change the default colour of the timers"

	)

	@ConfigEditorDropdown(
		values = {
			"Black",
			"Dark Blue",
			"Dark Green",
			"Dark Aqua",
			"Dark Red",
			"Dark Purple",
			"Gold",
			"Gray",
			"Dark Gray",
			"Blue",
			"Green",
			"Aqua",
			"Red",
			"Light Purple",
			"Yellow",
			"White"
		}
	)
	@ConfigAccordionId(id = 2)
	public int defaultColour = 15;

	@Expose
	public Position todoPosition = new Position(100, 0);

	@Expose
	@ConfigOption(
		name = "Todo Style",
		desc = "Change the style of the todo overlay"
	)
	@ConfigEditorDropdown(
		values = {"Background", "No Shadow", "Shadow", "Full Shadow"}
	)
	@ConfigAccordionId(id = 0)
	public int todoStyle = 0;

	@Expose
	@ConfigOption(
		name = "Todo Icons",
		desc = "Add little item icons next to the lines in the todo overlay"
	)
	@ConfigEditorBoolean
	@ConfigAccordionId(id = 0)
	public boolean todoIcons = true;
}
