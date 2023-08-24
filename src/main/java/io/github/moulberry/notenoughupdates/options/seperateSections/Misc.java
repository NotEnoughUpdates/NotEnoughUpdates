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
import io.github.moulberry.moulconfig.annotations.ConfigEditorDropdown;
import io.github.moulberry.moulconfig.annotations.ConfigEditorKeybind;
import io.github.moulberry.moulconfig.annotations.ConfigEditorSlider;
import io.github.moulberry.moulconfig.annotations.ConfigOption;
import org.lwjgl.input.Keyboard;

public class Misc {
	@Expose
	@ConfigOption(
		name = "Only Show on SkyBlock",
		desc = "The item list and some other GUI elements will only show on SkyBlock"
	)
	@ConfigEditorBoolean
	public boolean onlyShowOnSkyblock = true;

	@Expose
	@ConfigOption(
		name = "Hide Potion Effects",
		desc = "Hide the potion effects inside your inventory while on SkyBlock"
	)
	@ConfigEditorBoolean
	public boolean hidePotionEffect = true;

	@Expose
	@ConfigOption(
		name = "Streamer Mode",
		desc = "Randomize lobby names in the scoreboard and chat messages to help prevent stream sniping"
	)
	@ConfigEditorBoolean
	public boolean streamerMode = false;

	@Expose
	@ConfigOption(
		name = "Fix Steve skulls",
		desc = "Fix some skulls and skins not downloading on old java versions. May require restart."
	)
	@ConfigEditorBoolean
	public boolean fixSteveSkulls = true;

	@ConfigOption(
		name = "Fairy Soul Waypoints",
		desc = ""
	)
	@ConfigEditorAccordion(id = 0)
	public boolean fariySoulAccordion = false;
	@Expose
	@ConfigOption(
		name = "Track Fairy Souls",
		desc = "Track Found Fairy Souls"
	)
	@ConfigEditorBoolean(runnableId = 20)
	@ConfigAccordionId(id = 0)
	public boolean trackFairySouls = true;

	@Expose
	@ConfigOption(
		name = "Show Waypoints",
		desc = "Show Fairy Soul Waypoints (Requires fairy soul tracking)"
	)
	@ConfigEditorBoolean(
		runnableId = 15
	)
	@ConfigAccordionId(id = 0)
	public boolean fariySoul = false;

	@Expose
	@ConfigOption(
		name = "Show Waypoint Distance",
		desc = "Show the distance to each fairy soul waypoint"
	)
	@ConfigEditorBoolean
	@ConfigAccordionId(id = 0)
	public boolean fairySoulWaypointDistance = false;

	@Expose
	@ConfigOption(
		name = "Mark All As Found",
		desc = "Mark all fairy souls in current location as found"
	)
	@ConfigEditorButton(
		runnableId = 16,
		buttonText = "Clear"
	)
	@ConfigAccordionId(id = 0)
	public boolean fariySoulClear = false;

	@Expose
	@ConfigOption(
		name = "Mark All As Missing",
		desc = "Mark all fairy souls in current location as missing"
	)
	@ConfigEditorButton(
		runnableId = 17,
		buttonText = "Unclear"
	)
	@ConfigAccordionId(id = 0)
	public boolean fariySoulUnclear = false;

	@Expose
	@ConfigOption(
		name = "GUI Click Sounds",
		desc = "Play click sounds in various NEU-related GUIs when pressing buttons"
	)
	@ConfigEditorBoolean
	public boolean guiButtonClicks = true;

	@Expose
	@ConfigOption(
		name = "Replace Chat Social Options",
		desc = "Replace Hypixel's chat social options with NEU's profile viewer or with /ah"
	)
	@ConfigEditorDropdown(
		values = {"Off", "/pv", "/ah"}
	)
	public int replaceSocialOptions1 = 1;

	@Expose
	@ConfigOption(
		name = "Damage Indicator Style",
		desc = "Change SkyBlock damage indicators to use shortened numbers\n" +
			"\u00A7cSome old animations mods break this feature"
	)
	@ConfigEditorBoolean
	public boolean damageIndicatorStyle2 = false;

	@Expose
	@ConfigOption(
		name = "Profile Viewer",
		desc = "Brings up the profile viewer (/pv)\n" +
			"Shows stats and networth of players"
	)
	@ConfigEditorButton(runnableId = 13, buttonText = "Open")
	public boolean openPV = true;

	@ConfigOption(
		name = "Custom Enchant Colours",
		desc = ""
	)
	@ConfigEditorAccordion(
		id = 1
	)
	public boolean neuEnchantsAccordion = true;

	@Expose
	@ConfigOption(
		name = "Edit Enchant Colours",
		desc = "Change the colours of certain SkyBlock enchants (/neuec)"
	)
	@ConfigEditorButton(runnableId = 8, buttonText = "Open")
	@ConfigAccordionId(id = 1)
	public boolean editEnchantColoursButton = true;

	@Expose
	@ConfigOption(
		name = "Chroma Text Speed",
		desc = "Change the speed of chroma text for items names (/neucustomize) and enchant colours (/neuec) with the chroma colour code (&z)"
	)
	@ConfigEditorSlider(
		minValue = 10,
		maxValue = 500,
		minStep = 10
	)
	@ConfigAccordionId(id = 1)
	public int chromaSpeed = 100;

	@Expose
	@ConfigOption(
		name = "Cache Tooltip Enchants",
		desc = "Caches item enchants in tooltip to only use the neuec config once per item lookup."
	)
	@ConfigEditorBoolean
	@ConfigAccordionId(id = 1)
	public boolean cacheItemEnchant = true;

	@Expose
	@ConfigOption(
		name = "Disable Skull retexturing",
		desc = "Disables the skull retexturing."
	)
	@ConfigEditorBoolean
	public boolean disableSkullRetexturing = false;

	@Expose
	@ConfigOption(
		name = "Disable NPC retexturing",
		desc = "Disables the NPC retexturing."
	)
	@ConfigEditorBoolean
	public boolean disableNPCRetexturing = false;

	@Expose
	@ConfigOption(
		name = "Wiki",
		desc = "The wiki to use in the wiki renderer."
	)
	@ConfigEditorDropdown(values = {
		"Hypixel",
		"Fandom"
	})
	public int wiki = 0;

	@Expose
	@ConfigOption(
		name = "Waypoint Keybind",
		desc = "Press this keybind to show waypoints to various NPCs"
	)
	@ConfigEditorKeybind(defaultKey = Keyboard.KEY_NONE)
	public int keybindWaypoint = Keyboard.KEY_NONE;

	@Expose
	@ConfigOption(
		name = "Untrack close Waypoints",
		desc = "Automatically untrack waypoints once you get close to them."
	)
	@ConfigEditorBoolean
	public boolean untrackCloseWaypoints = true;

	@Expose
	@ConfigOption(
		name = "Warp twice",
		desc = "Warp twice when using SHIFT+<waypoint keybind> to /warp to a waypoint."
	)
	@ConfigEditorBoolean
	public boolean warpTwice = true;

	@Expose
	@ConfigOption(
		name = "Calculator",
		desc = "Replace calculations like §9\"1+2\"§7 with the calculation result in sign popups (AH/BZ) and in the neu search bar"
	)
	@ConfigEditorDropdown(values = {"Off", "Enabled with ! Prefix", "Always enabled"})
	public int calculationMode = 2;

	@Expose
	@ConfigOption(
		name = "Enable Abiphone Warning",
		desc = "Asks for confirmation when removing a contact in the abiphone"
	)
	@ConfigEditorBoolean
	public boolean abiphoneWarning = true;

	@Expose
	@ConfigOption(
		name = "Enable Coop Warning",
		desc = "Asks for confirmation when clicking the coop diamond in profile menu and prevents 'wrong' /coopadd commands"
	)
	@ConfigEditorBoolean
	public boolean coopWarning = true;

	@Expose
	@ConfigOption(
		name = "Filter Skyblock Levels in Chat",
		desc = "Requires the \"SkyBlock Levels in Chat\" skyblock setting to be on"
	)
	@ConfigEditorSlider(
		minValue = 0,
		maxValue = 300,
		minStep = 10
	)
	public int filterChatLevel = 0;

	@Expose
	@ConfigOption(
		name = "Enable text field tweaks",
		desc = "Allows the use of ctrl + z, ctrl + y and ctrl + Lshift + z in text fields"
	)
	@ConfigEditorBoolean
	public boolean textFieldTweaksEnabled = true;

	@Expose
	@ConfigOption(
		name = "Abiphone Favourites",
		desc = "Allows to set abiphone contacts as favourites, toggle between displaying all contacts or favourites only and deactivates the option to remove contacts at all."
	)
	@ConfigEditorBoolean
	public boolean abiphoneFavourites = true;

	@Expose
	@ConfigOption(
		name = "Group Join PV",
		desc = "View another player's profile by clicking on the chat message when they join in a dungeon or kuudra group."
	)
	@ConfigEditorBoolean
	public boolean dungeonGroupsPV = true;

	@Expose
	@ConfigOption(
		name = "Old SkyBlock Menu",
		desc = "Show old buttons in the SkyBlock Menu: Trade, Accessories, Potions, Quiver, Fishing and Sacks. " +
			"§cOnly works with the booster cookie effect active."
	)
	@ConfigEditorBoolean
	public boolean oldSkyBlockMenu = false;
}
