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
import io.github.moulberry.moulconfig.annotations.ConfigAccordionId;
import io.github.moulberry.moulconfig.annotations.ConfigEditorAccordion;
import io.github.moulberry.moulconfig.annotations.ConfigEditorBoolean;
import io.github.moulberry.moulconfig.annotations.ConfigEditorButton;
import io.github.moulberry.moulconfig.annotations.ConfigEditorDraggableList;
import io.github.moulberry.moulconfig.annotations.ConfigEditorDropdown;
import io.github.moulberry.moulconfig.annotations.ConfigEditorSlider;
import io.github.moulberry.moulconfig.annotations.ConfigOption;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Mining {
	@ConfigOption(
		name = "Waypoints",
		desc = ""
	)
	@ConfigEditorAccordion(id = 0)
	public boolean waypointsAccordion = false;

	@Expose
	@ConfigOption(
		name = "Mines Waypoints",
		desc = "Show waypoints in the Dwarven mines to the various locations\n" +
			"Use \"Commissions Only\" to only show active commission locations"
	)
	@ConfigEditorDropdown(
		values = {"Hide", "Commissions Only", "Always"}
	)
	@ConfigAccordionId(id = 0)
	public int locWaypoints = 1;

	@Expose
	@ConfigOption(
		name = "Hide waypoints when at Location",
		desc = "Hides the Commission Waypoints if you are already at the location of the waypoint.\n" +
			"Only active if Waypoints are set to \"Commissions Only\""
	)
	@ConfigEditorBoolean
	@ConfigAccordionId(id = 0)
	public boolean hideWaypointIfAtLocation = true;

	@Expose
	@ConfigOption(
		name = "Emissary Waypoints",
		desc = "Show waypoints in the Dwarven mines to emissaries\n" +
			"Use \"Commission End\" to only show after finishing commissions"
	)
	@ConfigEditorDropdown(
		values = {"Hide", "Commission End", "Always"}
	)
	@ConfigAccordionId(id = 0)
	public int emissaryWaypoints = 1;

	@Expose
	@ConfigOption(
		name = "Enable Powder Ghast Waypoints",
		desc = "Shows a waypoint at the location of the Powder Ghast"
	)
	@ConfigEditorBoolean
	@ConfigAccordionId(id = 0)
	public boolean powderGhastWaypoint = true;

	@Expose
	@ConfigOption(
		name = "Enable Fallen Star Waypoints",
		desc = "Shows a waypoint at the location of the Fallen Star"
	)
	@ConfigEditorBoolean
	@ConfigAccordionId(id = 0)
	public boolean fallenStarWaypoint = true;

	@ConfigOption(
		name = "Drill Fuel Bar",
		desc = ""
	)
	@ConfigEditorAccordion(id = 1)
	public boolean drillAccordion = false;

	@Expose
	@ConfigOption(
		name = "Drill Fuel Bar",
		desc = "Show a fancy drill fuel bar when holding a drill in mining areas"
	)
	@ConfigEditorBoolean
	@ConfigAccordionId(id = 1)
	public boolean drillFuelBar = true;

	@Expose
	@ConfigOption(
		name = "Fuel Bar Width",
		desc = "Change the width of the drill fuel bar"
	)
	@ConfigEditorSlider(
		minValue = 50,
		maxValue = 400,
		minStep = 10
	)
	@ConfigAccordionId(id = 1)
	public int drillFuelBarWidth = 200;

	@Expose
	public Position drillFuelBarPosition = new Position(0, -100, true, false);

	@ConfigOption(
		name = "Dwarven Overlay",
		desc = ""
	)
	@ConfigEditorAccordion(id = 2)
	public boolean overlayAccordion = false;

	@Expose
	@ConfigOption(
		name = "Enable Dwarven Overlay",
		desc = "Show an Overlay with useful information on the screen while in Dwarven Mines"
	)
	@ConfigEditorBoolean
	@ConfigAccordionId(id = 2)
	public boolean dwarvenOverlay = true;

	@Expose
	@ConfigOption(
		name = "Dwarven Text",
		desc = "\u00a7eDrag text to change the appearance of the Overlay\n" +
			"\u00a7rGo to the Dwarven Mines to show this Overlay with useful information"
	)
	@ConfigEditorDraggableList(
		exampleText = {
			"§3Goblin Slayer: §626.5%\n§3Lucky Raffle: §c0.0%",
			"§3Mithril Powder: §26,243",
			"§3Gemstone Powder: §d6,243",
			"§3Forge 1) §9Diamonite§7: §aReady!",
			"§3Pickaxe CD: §a78s",
			"§3Star Cult: §a78s",
			"§3Sky Mall: §a5x Titanium (78s)"
		}
	)
	@ConfigAccordionId(id = 2)
	public List<Integer> dwarvenText2 = new ArrayList<>(Arrays.asList(0, 1, 2, 3, 4, 5, 6));

	@Expose
	public Position overlayPosition = new Position(10, 100);

	@Expose
	@ConfigOption(
		name = "Overlay Style",
		desc = "Change the style of the Dwarven Mines information Overlay"
	)
	@ConfigEditorDropdown(
		values = {"Background", "No Shadow", "Shadow", "Full Shadow"}
	)
	@ConfigAccordionId(id = 2)
	public int overlayStyle = 0;

	@Expose
	@ConfigOption(
		name = "Show Icons",
		desc = "Show Icons representing the part of the overlay."
	)
	@ConfigEditorBoolean
	@ConfigAccordionId(id = 2)
	public boolean dwarvenOverlayIcons = true;

	@Expose
	@ConfigOption(
		name = "Forge Display",
		desc = "Change what gets shown in the Forge Display"
	)
	@ConfigEditorDropdown(
		values = {"Only Done", "Only Working", "Everything Except Locked", "Everything"}
	)
	@ConfigAccordionId(id = 2)
	public int forgeDisplay = 1;

	@Expose
	@ConfigOption(
		name = "Forge Location",
		desc = "Change when the forge display gets shown"
	)
	@ConfigEditorDropdown(
		values = {"Dwarven Mines+Crystal Hollows", "Everywhere except dungeons", "Everywhere"}
	)
	@ConfigAccordionId(id = 2)
	public int forgeDisplayEnabledLocations = 0;

	@Expose
	@ConfigOption(
		name = "Forge Tab",
		desc = "Only show the forge display when tab list is open\n" +
			"\u00A7cThis only works outside of Dwarven Caves!"
	)
	@ConfigEditorBoolean
	@ConfigAccordionId(id = 2)
	public boolean forgeDisplayOnlyShowTab = false;

	@Expose
	@ConfigOption(
		name = "Star Cult Location",
		desc = "Change when the Star Cult timer gets shown"
	)
	@ConfigEditorDropdown(
		values = {"Dwarven Mines+Crystal Hollows", "Everywhere except dungeons", "Everywhere"}
	)
	@ConfigAccordionId(id = 2)
	public int starCultDisplayEnabledLocations = 0;

	@Expose
	@ConfigOption(
		name = "Star Cult Tab",
		desc = "Only show the star cult timer when tab list is open\n" +
			"§cThis only works outside of Dwarven Caves!"
	)
	@ConfigEditorBoolean
	@ConfigAccordionId(id = 2)
	public boolean starCultDisplayOnlyShowTab = false;

	@Expose
	@ConfigOption(
		name = "Commission Task Tips",
		desc = "Show tips to help complete commission tasks"
	)
	@ConfigEditorDropdown(
		values = {"Off", "Only while sneaking", "Always"}
	)
	@ConfigAccordionId(id = 2)
	public int commissionTaskTips = 0;

	@Expose
	@ConfigOption(
		name = "Tips in New Line",
		desc = "Show the Commission Task Tips in the next line.")
	@ConfigEditorBoolean
	@ConfigAccordionId(id = 2)
	public boolean commissionTaskTipNewLine = true;

	@ConfigOption(
		name = "Metal Detector Solver",
		desc = ""
	)
	@ConfigEditorAccordion(id = 3)
	public boolean metalDetectorSolverAccordion = false;

	@Expose
	@ConfigOption(
		name = "Enable Waypoints",
		desc =
			"Enabled the metal detector solver for Mines of Divan, to use this stand still to calculate possible blocks and then if required stand" +
				" still on another block."
	)
	@ConfigEditorBoolean
	@ConfigAccordionId(id = 3)
	public boolean metalDetectorEnabled = true;

	@Expose
	@ConfigOption(
		name = "Show Possible Blocks",
		desc = "Show waypoints on possible locations when NEU isn't sure about what block the treasure is."
	)
	@ConfigEditorBoolean
	@ConfigAccordionId(id = 3)
	public boolean metalDetectorShowPossible = false;

	@ConfigOption(
		name = "Crystal Hollows Overlay",
		desc = ""
	)
	@ConfigEditorAccordion(id = 4)
	public boolean crystalHollowOverlayAccordion = false;

	@Expose
	@ConfigOption(
		name = "Enable Crystal Overlay",
		desc = "Enables the Crystal Hollows Overlay."
	)
	@ConfigEditorBoolean
	@ConfigAccordionId(id = 4)
	public boolean crystalHollowOverlay = true;

	@Expose
	public Position crystalHollowOverlayPosition = new Position(200, 0);

	@Expose
	@ConfigOption(
		name = "Options",
		desc = "Drag text to change the appearance of the overlay!\n" +
			"Click add to add extra things!"
	)
	@ConfigEditorDraggableList(
		exampleText = {
			"\u00a73Amber Crystal: \u00a7aPlaced\n" +
				"\u00a73Sapphire Crystal: \u00a7eCollected\n" +
				"\u00a73Jade Crystal: \u00a7eMissing\n" +
				"\u00a73Amethyst Crystal: \u00a7cMissing\n" +
				"\u00a73Topaz Crystal: \u00a7cMissing\n",
			"\u00a73Crystals: \u00a7a4/5",
			"\u00a73Crystals: \u00a7a80%",
			"\u00a73Electron Transmitter: \u00a7aDone\n" +
				"\u00a73Robotron Reflector: \u00a7eIn Storage\n" +
				"\u00a73Superlite Motor: \u00a7eIn Inventory\n" +
				"\u00a73Synthetic Heart: \u00a7cMissing\n" +
				"\u00a73Control Switch: \u00a7cMissing\n" +
				"\u00a73FTX 3070: \u00a7cMissing",
			"\u00a73Electron Transmitter: \u00a7a3\n" +
				"\u00a73Robotron Reflector: \u00a7e2\n" +
				"\u00a73Superlite Motor: \u00a7e1\n" +
				"\u00a73Synthetic Heart: \u00a7c0\n" +
				"\u00a73Control Switch: \u00a7c0\n" +
				"\u00a73FTX 3070: \u00a7c0",
			"\u00a73Automaton parts: \u00a7a5/6",
			"\u00a73Automaton parts: \u00a7a83%",
			"\u00a73Scavenged Lapis Sword: \u00a7aDone\n" +
				"\u00a73Scavenged Golden Hammer: \u00a7eIn Storage\n" +
				"\u00a73Scavenged Diamond Axe: \u00a7eIn Inventory\n" +
				"\u00a73Scavenged Emerald Hammer: \u00a7cMissing\n",
			"\u00a73Scavenged Lapis Sword: \u00a7a3\n" +
				"\u00a73Scavenged Golden Hammer: \u00a7e2\n" +
				"\u00a73Scavenged Diamond Axe: \u00a7e1\n" +
				"\u00a73Scavenged Emerald Hammer: \u00a7c0\n",
			"\u00a73Mines of Divan parts: \u00a7a3/4",
			"\u00a73Mines of Divan parts: \u00a7a75%"
		}
	)
	@ConfigAccordionId(id = 4)
	public List<Integer> crystalHollowText = new ArrayList<>(Arrays.asList(0, 3, 7));

	@Expose
	@ConfigOption(
		name = "Style",
		desc = "Change the style of the Crystal Hollows Overlay."
	)
	@ConfigEditorDropdown(
		values = {"Background", "No Shadow", "Shadow", "Full Shadow"}
	)
	@ConfigAccordionId(id = 4)
	public int crystalHollowOverlayStyle = 0;

	@Expose
	@ConfigOption(
		name = "Show Icons",
		desc = "Show icons in the Overlay that represent the part."
	)
	@ConfigEditorBoolean
	@ConfigAccordionId(id = 4)
	public boolean crystalHollowIcons = true;

	@Expose
	@ConfigOption(
		name = "Hide Done",
		desc = "Don't show parts you've given to the NPC."
	)
	@ConfigEditorBoolean
	@ConfigAccordionId(id = 4)
	public boolean crystalHollowHideDone = false;

	@ConfigOption(
		name = "Locations",
		desc = ""
	)
	@ConfigEditorAccordion(id = 5)
	@ConfigAccordionId(id = 4)
	public boolean crystalHollowLocationAccordion = false;

	@Expose
	@ConfigOption(
		name = "Show Automaton",
		desc = "Change where to show Automaton parts."
	)
	@ConfigEditorDropdown(
		values = {"Crystal Hollows", "Precursor Remnants", "Lost Precursor City"}
	)
	@ConfigAccordionId(id = 5)
	public int crystalHollowAutomatonLocation = 2;

	@Expose
	@ConfigOption(
		name = "Show Divan",
		desc = "Change where to show Mines of Divan parts."
	)
	@ConfigEditorDropdown(
		values = {"Crystal Hollows", "Mithril Deposits", "Mines of Divan"}
	)
	@ConfigAccordionId(id = 5)
	public int crystalHollowDivanLocation = 2;

	@Expose
	@ConfigOption(
		name = "Show Crystal",
		desc = "Change where to show Collected Crystals."
	)
	@ConfigEditorDropdown(
		values = {"Crystal Hollows", "When No Other Overlays"}
	)
	@ConfigAccordionId(id = 5)
	public int crystalHollowCrystalLocation = 1;

	@ConfigOption(
		name = "Colours",
		desc = ""
	)
	@ConfigEditorAccordion(id = 6)
	@ConfigAccordionId(id = 4)
	public boolean crystalHollowColourAccordion = false;

	@Expose
	@ConfigOption(
		name = "Main Color",
		desc = "Change the main color of the overlay."

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
	@ConfigAccordionId(id = 6)
	public int crystalHollowPartColor = 3;

	@Expose
	@ConfigOption(
		name = "Done Color",
		desc = "Change the colour when the part is given to the NPC."
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

	@ConfigAccordionId(id = 6)
	public int crystalHollowDoneColor = 10;

	@Expose
	@ConfigOption(
		name = "In Inventory Color",
		desc = "Change the colour when the part is in the inventory."
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
	@ConfigAccordionId(id = 6)
	public int crystalHollowInventoryColor = 14;

	@Expose
	@ConfigOption(
		name = "In Storage Color",
		desc = "Change the colour when the part is in the storage."
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
	@ConfigAccordionId(id = 6)
	public int crystalHollowStorageColor = 14;

	@Expose
	@ConfigOption(
		name = "Missing Color",
		desc = "Change the colour when the part is missing."
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
	@ConfigAccordionId(id = 6)
	public int crystalHollowMissingColor = 12;

	@Expose
	@ConfigOption(
		name = "Placed Color",
		desc = "Change the colour when the crystal is placed."
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
	@ConfigAccordionId(id = 6)
	public int crystalHollowPlacedColor = 10;

	@Expose
	@ConfigOption(
		name = "Collected Color",
		desc = "Change the colour when the crystal is collected"
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
	@ConfigAccordionId(id = 6)
	public int crystalHollowCollectedColor = 14;

	@Expose
	@ConfigOption(
		name = "All Color",
		desc = "Change the colour when you have 2/3-all of the parts collected in the count overlay."
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
	@ConfigAccordionId(id = 6)
	public int crystalHollowAllColor = 10;

	@Expose
	@ConfigOption(
		name = "1/3 Color",
		desc = "Change the colour when you have 1/3-2/3 of the parts collected in the count overlay."
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
	@ConfigAccordionId(id = 6)
	public int crystalHollowMiddleColor = 14;

	@Expose
	@ConfigOption(
		name = "0 Color",
		desc = "Change the colour when you have 0-1/3 of the parts collected in the count overlay."
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
	@ConfigAccordionId(id = 6)
	public int crystalHollowNoneColor = 12;

	@ConfigOption(
		name = "Wishing Compass Solver",
		desc = ""
	)
	@ConfigEditorAccordion(id = 7)
	public boolean wishingCompassSolverAccordion = false;

	@Expose
	@ConfigOption(
		name = "Enable Solver",
		desc = "Show wishing compass target coordinates based on two samples"
	)
	@ConfigAccordionId(id = 7)
	@ConfigEditorBoolean
	public boolean wishingCompassSolver = true;

	@Expose
	@ConfigOption(
		name = "Skytils Waypoints",
		desc = "Automatically create Skytils waypoints for well-known targets"
	)
	@ConfigAccordionId(id = 7)
	@ConfigEditorBoolean
	public boolean wishingCompassAutocreateKnownWaypoints = false;

	@Expose
	@ConfigOption(
		name = "Waypoint Names",
		desc = "NOTE: Skytils overwrites waypoint coordinates with less accurate values for Skytils names."
	)
	@ConfigAccordionId(id = 7)
	@ConfigEditorDropdown(
		values = {"NEU", "Skytils"}
	)
	public int wishingCompassWaypointNames = 0;

	@ConfigOption(
		name = "Powder Grinding Tracker",
		desc = ""
	)
	@ConfigEditorAccordion(id = 9)
	public boolean powderGrindingTrackerAccordion = false;

	@Expose
	@ConfigOption(
		name = "Enable Tracker",
		desc = "Show an Overlay with useful information related to Powder Grinding"
	)
	@ConfigAccordionId(id = 9)
	@ConfigEditorBoolean
	public boolean powderGrindingTrackerEnabled = false;

	@Expose
	@ConfigOption(
		name = "Tracker Text",
		desc = "\u00a7eDrag text to change the appearance of the Overlay\n" +
			"\u00a7rGo to the Crystal Hollows to show this Overlay with useful information"
	)
	@ConfigEditorDraggableList(
		exampleText = {
			"\u00a73Chests Found: \u00a7a13",
			"\u00a73Opened Chests: \u00a7a11",
			"\u00a73Unopened Chests: \u00a7c2",
			"\u00a73Mithril Powder Found: \u00a726,243",
			"\u00a73Average Mithril Powder/Chest: \u00a72568",
			"\u00a73Gemstone Powder Found: \u00a7d6,243",
			"\u00a73Average Gemstone Powder/Chest: \u00a7d568"
		}
	)
	@ConfigAccordionId(id = 9)
	public List<Integer> powderGrindingTrackerText = new ArrayList<>(Arrays.asList(0, 1, 2, 3, 4, 5, 6));

	@Expose
	public Position powderGrindingTrackerPosition = new Position(10, 265);

	@Expose
	@ConfigOption(
		name = "Overlay Style",
		desc = "Change the style of the Powder Grinding Tracker information Overlay"
	)
	@ConfigEditorDropdown(
		values = {"Background", "No Shadow", "Shadow", "Full Shadow"}
	)
	@ConfigAccordionId(id = 9)
	public int powderGrindingTrackerOverlayStyle = 0;

	@Expose
	@ConfigOption(
		name = "Tracker Reset Mode",
		desc = "When the Powder Grinding Tracker should be reset"
	)
	@ConfigEditorDropdown(
		values = {"On World Change", "On Restart", "Never"}
	)
	@ConfigAccordionId(id = 9)
	public int powderGrindingTrackerResetMode = 2;

	@Expose
	@ConfigOption(
		name = "Reset Tracker",
		desc = "Reset all stats for Powder Grinding Tracker"
	)
	@ConfigEditorButton(
		runnableId = 26,
		buttonText = "Reset"
	)
	@ConfigAccordionId(id = 9)
	public int resetPowderGrindingTracker = 0;

	@Expose
	@ConfigOption(
		name = "Puzzler Solver",
		desc = "Show the correct block to mine for the puzzler puzzle in Dwarven Mines"
	)
	@ConfigEditorBoolean
	public boolean puzzlerSolver = true;

	@Expose
	@ConfigOption(
		name = "Titanium Alert",
		desc = "Show an alert whenever titanium appears nearby"
	)
	@ConfigEditorBoolean
	public boolean titaniumAlert = true;

	@Expose
	@ConfigOption(
		name = "Titanium must touch air",
		desc = "Only show an alert if the Titanium touches air. (kinda sus)"
	)
	@ConfigEditorBoolean
	public boolean titaniumAlertMustBeVisible = false;

	@ConfigOption(
		name = "Custom Textures",
		desc = ""
	)
	@ConfigEditorAccordion(id = 8)
	public boolean texturesAccordion = false;

	@Expose
	@ConfigOption(
		name = "Dwarven Mines Textures",
		desc = "Allows texture packs to retexture blocks in the Dwarven Mines. If you don't have a texture pack that does this, you should leave this off"
	)
	@ConfigAccordionId(id = 8)
	@ConfigEditorBoolean
	public boolean dwarvenTextures = false;
	@Expose
	@ConfigOption(
		name = "Crystal Hollows Textures",
		desc = "Allows texture packs to retexture blocks in the Crystal Hollows. If you don't have a texture pack that does this, you should leave this off"
	)
	@ConfigAccordionId(id = 8)
	@ConfigEditorBoolean
	public boolean crystalHollowTextures = false;

	@Expose
	@ConfigOption(
		name = "Replace Gemstone sounds",
		desc = "Replace the break sounds of crystals in the Crystal Hollows. Requires a texture pack with this feature"
	)
	@ConfigAccordionId(id = 8)
	@ConfigEditorBoolean
	public boolean gemstoneSounds = false;

	@Expose
	@ConfigOption(
		name = "Replace Mithril sounds",
		desc = "Replace the break sounds of mithril and titanium in the Dwarven mines. Requires a texture pack with this feature"
	)
	@ConfigAccordionId(id = 8)
	@ConfigEditorBoolean
	public boolean mithrilSounds = false;

}
