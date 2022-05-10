package io.github.moulberry.notenoughupdates.options.seperateSections;

import com.google.gson.annotations.Expose;
import io.github.moulberry.notenoughupdates.core.config.annotations.ConfigAccordionId;
import io.github.moulberry.notenoughupdates.core.config.annotations.ConfigEditorAccordion;
import io.github.moulberry.notenoughupdates.core.config.annotations.ConfigEditorBoolean;
import io.github.moulberry.notenoughupdates.core.config.annotations.ConfigEditorButton;
import io.github.moulberry.notenoughupdates.core.config.annotations.ConfigEditorDropdown;
import io.github.moulberry.notenoughupdates.core.config.annotations.ConfigEditorKeybind;
import io.github.moulberry.notenoughupdates.core.config.annotations.ConfigEditorSlider;
import io.github.moulberry.notenoughupdates.core.config.annotations.ConfigOption;
import org.lwjgl.input.Keyboard;

public class Misc {
	@Expose
	@ConfigOption(
		name = "Only Show on Skyblock",
		desc = "The item list and some other GUI elements will only show on skyblock"
	)
	@ConfigEditorBoolean
	public boolean onlyShowOnSkyblock = true;

	@Expose
	@ConfigOption(
		name = "Hide Potion Effects",
		desc = "Hide the potion effects inside your inventory while on skyblock"
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
		desc = "Change the style of Skyblock damage indicators to be easier to read\n" +
			"\u00A7cSome old animations mods break this feature"
	)
	@ConfigEditorDropdown(
		values = {"Off", "Commas", "Shortened"}
	)
	public int damageIndicatorStyle = 1;

	@Expose
	@ConfigOption(
		name = "Profile Viewer",
		desc = "Brings up the profile viewer (/pv)\n" +
			"Shows stats and networth of players"
	)
	@ConfigEditorButton(runnableId = 13, buttonText = "Open")
	public boolean openPV = true;

	@Expose
	@ConfigOption(

		name = "Edit Enchant Colours",
		desc = "Change the colours of certain skyblock enchants (/neuec)"
	)
	@ConfigEditorButton(runnableId = 8, buttonText = "Open")
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
	public int chromaSpeed = 100;

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
	@ConfigEditorKeybind(defaultKey = Keyboard.KEY_N)
	public int keybindWaypoint = Keyboard.KEY_N;

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
		desc = "Warp twice when using SHIFT+N to /warp to a waypoint."
	)
	@ConfigEditorBoolean
	public boolean warpTwice = true;

	@Expose
	@ConfigOption(
		name = "Rat Protection",
		desc = "Attempts to prevent your Minecraft Session ID from being stolen by malicious mods. \u00A7cAlthough your safety is not guaranteed, it is STRONGLY recommended that you leave this feature enabled."
	)
	@ConfigEditorBoolean
	public boolean ratProtection = true;

}
