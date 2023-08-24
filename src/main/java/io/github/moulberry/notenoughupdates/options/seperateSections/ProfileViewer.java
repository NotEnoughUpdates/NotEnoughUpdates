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
import io.github.moulberry.notenoughupdates.BuildFlags;
import io.github.moulberry.moulconfig.annotations.ConfigEditorBoolean;
import io.github.moulberry.moulconfig.annotations.ConfigEditorButton;
import io.github.moulberry.moulconfig.annotations.ConfigEditorDraggableList;
import io.github.moulberry.moulconfig.annotations.ConfigOption;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ProfileViewer {

	@Expose
	@ConfigOption(
		name = "Open Profile Viewer",
		desc = "Brings up the profile viewer (/pv)\n" +
			"Shows stats and networth of players"
	)
	@ConfigEditorButton(
		runnableId = 13,
		buttonText = "Open"
	)
	public boolean openPV = true;

	@Expose
	@ConfigOption(
		name = "Page layout",
		desc = "\u00a7rSelect the order of the pages at the top of the Profile Viewer\n" +
			"\u00a7eDrag text to rearrange"
	)
	@ConfigEditorDraggableList(
		exampleText = {
			"\u00a7eBasic Info",
			"\u00a7eDungeons",
			"\u00a7eExtra Info",
			"\u00a7eInventories",
			"\u00a7eCollections",
			"\u00a7ePets",
			"\u00a7eMining",
			"\u00a7eBingo",
			"\u00a7eTrophy Fish",
			"\u00a7eBestiary",
			"\u00a7eCrimson Isle",
			"\u00a7eMuseum",
			"\u00a7eRift"
		},
		allowDeleting = false
	)
	public List<Integer> pageLayout = new ArrayList<>(Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12));

	@Expose
	@ConfigOption(
		name = "Always show bingo tab",
		desc = "Always show bingo tab or only show it when the bingo profile is selected"
	)
	@ConfigEditorBoolean
	public boolean alwaysShowBingoTab = false;

	@Expose
	@ConfigOption(
		name = "Show Pronouns in /pv",
		desc = "Shows the pronouns of a player in /pv. Data sourced from pronoundb.org"
	)
	@ConfigEditorBoolean
	public boolean showPronounsInPv = BuildFlags.ENABLE_PRONOUNS_IN_PV_BY_DEFAULT;

	@Expose
	@ConfigOption(
		name = "Use Soopy Networth",
		desc = "Replaces NEU networth with Soopy networth in /pv and /peek"
	)
	@ConfigEditorBoolean
	public boolean useSoopyNetworth = true;

	@Expose
	@ConfigOption(
		name = "Display Weight",
		desc = "Display Lily and Senither Weight in the Basic PV page"
	)
	@ConfigEditorBoolean
	public boolean displayWeight = true;
}
