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
import io.github.moulberry.moulconfig.annotations.ConfigEditorInfoText;
import io.github.moulberry.notenoughupdates.core.config.Position;
import io.github.moulberry.moulconfig.annotations.ConfigAccordionId;
import io.github.moulberry.moulconfig.annotations.ConfigEditorAccordion;
import io.github.moulberry.moulconfig.annotations.ConfigEditorBoolean;
import io.github.moulberry.moulconfig.annotations.ConfigEditorColour;
import io.github.moulberry.moulconfig.annotations.ConfigEditorDraggableList;
import io.github.moulberry.moulconfig.annotations.ConfigEditorDropdown;
import io.github.moulberry.moulconfig.annotations.ConfigEditorSlider;
import io.github.moulberry.moulconfig.annotations.ConfigOption;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ItemOverlays {
	@ConfigOption(
		name = "Treecapitator Overlay",
		desc = "Jungle Axe"
	)
	@ConfigEditorAccordion(id = 0)
	public boolean treecapAccordion = false;

	@Expose
	@ConfigOption(
		name = "Enable Treecap Overlay",
		desc = "Show which blocks will be broken when using a Jungle Axe or Treecapitator"
	)
	@ConfigEditorBoolean
	@ConfigAccordionId(id = 0)
	public boolean enableTreecapOverlay = true;

	@Expose
	@ConfigOption(
		name = "Show in Item durability",
		desc = "Show the cooldown of the Treecapitator in the item durability"
	)
	@ConfigEditorBoolean
	@ConfigAccordionId(id = 0)
	public boolean enableCooldownInItemDurability = true;

	@Expose
	@ConfigOption(
		name = "Overlay Colour",
		desc = "Change the colour of the overlay"
	)
	@ConfigEditorColour
	@ConfigAccordionId(id = 0)
	public String treecapOverlayColour = "00:50:64:224:208";

	@Expose
	@ConfigOption(
		name = "Enable Monkey Pet Check",
		desc = "Will check using the API to check what pet you're using\nto determine the cooldown based off of if you have a monkey pet."
	)
	@ConfigEditorBoolean
	@ConfigAccordionId(id = 0)
	public boolean enableMonkeyCheck = true;

	@ConfigOption(
		name = "Builder's Wand Overlay",
		desc = ""
	)
	@ConfigEditorAccordion(id = 1)
	public boolean wandAccordion = false;

	@Expose
	@ConfigOption(
		name = "Enable Wand Overlay",
		desc = "Show which blocks will be placed when using the Builder's Wand"
	)
	@ConfigEditorBoolean
	@ConfigAccordionId(id = 1)
	public boolean enableWandOverlay = true;

	@Expose
	@ConfigOption(
		name = "Wand Block Count",
		desc = "Shows the total count of a block in your inventory"
	)
	@ConfigEditorBoolean
	@ConfigAccordionId(id = 1)
	public boolean wandBlockCount = true;

	@Expose
	@ConfigOption(
		name = "Overlay Colour",
		desc = "Change the colour of the ghost block outline"
	)
	@ConfigEditorColour
	@ConfigAccordionId(id = 1)
	public String wandOverlayColour = "00:50:64:224:208";

	@ConfigOption(
		name = "Block Zapper Overlay",
		desc = ""
	)
	@ConfigEditorAccordion(id = 2)
	public boolean zapperAccordion = false;

	@Expose
	@ConfigOption(
		name = "Enable Zapper Overlay",
		desc = "Show which blocks will be destroyed when using the Block Zapper"
	)
	@ConfigEditorBoolean
	@ConfigAccordionId(id = 2)
	public boolean enableZapperOverlay = true;

	@Expose
	@ConfigOption(
		name = "Overlay Colour",
		desc = "Change the colour of the ghost block outline"
	)
	@ConfigEditorColour
	@ConfigAccordionId(id = 2)
	public String zapperOverlayColour = "0:102:171:5:0";

	@ConfigOption(
		name = "Smooth AOTE/AOTV/AOTL/Hyp",
		desc = ""
	)
	@ConfigEditorAccordion(id = 3)
	public boolean aoteAccordion = false;

	@Expose
	@ConfigOption(
		name = "Enable Smooth AOTE",
		desc = "Teleport smoothly to your destination when using AOTE, AOTV or AOTL"
	)
	@ConfigEditorBoolean
	@ConfigAccordionId(id = 3)
	public boolean enableSmoothAOTE = true;

	@Expose
	@ConfigOption(
		name = "Enable Smooth Hyperion",
		desc = "Teleport smoothly to your destination when using Hyperion"
	)
	@ConfigEditorBoolean
	@ConfigAccordionId(id = 3)
	public boolean enableSmoothHyperion = true;

	@Expose
	@ConfigOption(
		name = "Smooth TP Time",
		desc = "Change the amount of time (milliseconds) taken to teleport"
	)
	@ConfigEditorSlider(
		minValue = 0,
		maxValue = 500,
		minStep = 25
	)
	@ConfigAccordionId(id = 3)
	public int smoothTpMillis = 125;

	@Expose
	@ConfigOption(
		name = "Smooth TP Time (Etherwarp)",
		desc = "Teleport smoothly to your destination when using AOTV Etherwarp"
	)
	@ConfigEditorSlider(
		minValue = 0,
		maxValue = 500,
		minStep = 25
	)
	@ConfigAccordionId(id = 3)
	public int smoothTpMillisEtherwarp = 50;

	@Expose
	@ConfigOption(
		name = "Disable Hyperion Particles",
		desc = "Remove the explosion effect when using a hyperion"
	)
	@ConfigEditorBoolean
	@ConfigAccordionId(id = 3)
	public boolean disableHyperionParticles = true;

	@ConfigOption(
		name = "Etherwarp",
		desc = ""
	)
	@ConfigEditorAccordion(id = 4)
	public boolean etherwarpAccordion = false;

	@Expose
	@ConfigOption(
		name = "Etherwarp Zoom",
		desc = "Zoom in on targeted blocks with etherwarp, making it easier to adjust at a distance"
	)
	@ConfigEditorBoolean
	@ConfigAccordionId(id = 4)
	public boolean etherwarpZoom = true;

	@Expose
	@ConfigOption(
		name = "Enable etherwarp helper overlay",
		desc = "Display an overlay which tells you if the etherwarp will fail."
	)
	@ConfigEditorBoolean
	@ConfigAccordionId(id = 4)
	public boolean enableEtherwarpHelperOverlay = true;

	@Expose
	@ConfigOption(
		name = "Enable etherwarp block overlay",
		desc = "Display an overlay that tells you what block you will TP to."
	)
	@ConfigEditorBoolean
	@ConfigAccordionId(id = 4)
	public boolean enableEtherwarpBlockOverlay = true;

	@Expose
	@ConfigOption(
		name = "Disable overlay when fail",
		desc = "Don't display the etherwarp block overlay when you can't TP to the block"
	)
	@ConfigEditorBoolean
	@ConfigAccordionId(id = 4)
	public boolean disableOverlayWhenFailed = false;

	@Expose
	@ConfigOption(
		name = "Highlight Colour",
		desc = "Change the colour of the etherwarp target block outline"
	)
	@ConfigEditorColour
	@ConfigAccordionId(id = 4)
	public String etherwarpHighlightColour = "00:70:156:8:96";

	@ConfigOption(
		name = "Bonemerang Overlay",
		desc = ""
	)
	@ConfigEditorAccordion(id = 5)
	public boolean bonemerangAccordion = false;

	@Expose
	@ConfigOption(
		name = "Enable Bonemerang Overlay",
		desc = "Shows info about the bonemerang while holding it."
	)
	@ConfigEditorBoolean
	@ConfigAccordionId(id = 5)
	public boolean enableBonemerangOverlay = true;

	@Expose
	@ConfigOption(
		name = "Highlight Targeted Entities",
		desc = "Highlight entities that will be hit by your bonemerang"
	)
	@ConfigEditorBoolean
	@ConfigAccordionId(id = 5)
	public boolean highlightTargeted = true;

	@Expose
	public Position bonemerangPosition = new Position(-1, -1);

	@Expose
	@ConfigOption(
		name = "Bonemerang Overlay Text",
		desc = "\u00a7eDrag text to change the appearance of the overlay\n" +
			"\u00a7rHold a Bonemerang to display the overlay"
	)
	@ConfigEditorDraggableList(
		exampleText = {
			"\u00a7cBonemerang will break!",
			"\u00a77Targets: \u00a76\u00a7l10"
		}
	)
	@ConfigAccordionId(id = 5)
	public List<Integer> bonemerangOverlayText = new ArrayList<>(Arrays.asList(0, 1));

	@Expose
	@ConfigOption(
		name = "Bonemerang Overlay Style",
		desc = "Change the style of the Bonemerang overlay"
	)
	@ConfigEditorDropdown(
		values = {"Background", "No Shadow", "Shadow Only", "Full Shadow"}
	)
	@ConfigAccordionId(id = 5)
	public int bonemerangOverlayStyle = 0;
	@Expose
	@ConfigOption(
		name = "Fast update",
		desc = "Updates the bonemerang overlay faster.\n" +
			"Might cause some lag."
	)
	@ConfigEditorBoolean
	@ConfigAccordionId(id = 5)
	public boolean bonemerangFastUpdate = false;

	@ConfigOption(
		name = "Minion Crystal Radius Overlay",
		desc = ""
	)
	@ConfigEditorAccordion(id = 6)
	public boolean crystalAccordion = false;

	@Expose
	@ConfigOption(
		name = "Enable Crystal Overlay",
		desc = "Show a block overlay for the effective radius of minion crystals (farming, mining, etc)."
	)
	@ConfigEditorBoolean
	@ConfigAccordionId(id = 6)
	public boolean enableCrystalOverlay = true;

	@Expose
	@ConfigOption(
		name = "Always Show Crystal Overlay",
		desc = "Show the crystal overlay, even when a minion crystal is not being held."
	)
	@ConfigEditorBoolean
	@ConfigAccordionId(id = 6)
	public boolean alwaysShowCrystal = false;

	@ConfigOption(
		name = "Farming Overlays",
		desc = ""
	)
	@ConfigEditorAccordion(id = 7)
	public boolean farmingAccordion = false;

	@Expose
	@ConfigOption(
		name = "Enable Prismapump Overlay",
		desc = "Show a block overlay for the effected blocks of prismapump's ability."
	)
	@ConfigEditorBoolean
	@ConfigAccordionId(id = 7)
	public boolean enablePrismapumpOverlay = true;

	@Expose
	@ConfigOption(
		name = "Enable Hoe Of Tilling Overlay",
		desc = "Show a block overlay for the effected blocks of the hoe of tilling's ability."
	)
	@ConfigEditorBoolean
	@ConfigAccordionId(id = 7)
	public boolean enableHoeOverlay = true;

	@Expose
	@ConfigOption(
		name = "Enable Dirt Wand Overlay",
		desc = "Show a block overlay for the effected blocks of dirt wand's ability."
	)
	@ConfigEditorBoolean
	@ConfigAccordionId(id = 7)
	public boolean enableDirtWandOverlay = true;

	@Expose
	@ConfigOption(
		name = "Enable Scythe Overlay",
		desc = "Show a block overlay for the effected blocks of scythe's ability."
	)
	@ConfigEditorBoolean
	@ConfigAccordionId(id = 7)
	public boolean enableScytheOverlay = true;

	@ConfigOption(
		name="Custom Wither Cloak",
		desc = ""
	)
	@ConfigEditorAccordion(id = 8)
	public boolean customWitherCloakAccordion = false;

	@Expose
	@ConfigAccordionId(id = 8)
	@ConfigOption(
		name = "Enable Custom Wither Cloak",
		desc = "Replaces Hypixel Wither Cloak with custom shields due to the Hypixel Wither Cloak being difficult to see through"
	)
	@ConfigEditorBoolean
	public boolean customWitherCloakToggle = true;

	@Expose
	@ConfigAccordionId(id = 8)
	@ConfigOption(
		name = "Shield Count",
		desc = "The amount of shields circling the player\n0 = No shields"
	)
	@ConfigEditorSlider(
		minValue = 0,
		maxValue = 20,
		minStep = 1
	)
	public int customWitherCloakCount = 6;

	@Expose
	@ConfigAccordionId(id = 8)
	@ConfigOption(
		name = "Shield Speed",
		desc = "How fast they circle the player\n0 = Not moving\nNegative = Spinning opposite direction"
	)
	@ConfigEditorSlider(
		minValue = -20f,
		maxValue = 20f,
		minStep = 1.0f
	)
	public double customWitherCloakSpeed = 2d;

	/*@Expose
	@ConfigAccordionId(id = 7)
	@ConfigOption(
		name = "Shield Transparency",
		desc = "Changes how visible each shield is\n0 = Invisible"
	)
	@ConfigEditorSlider(
		minValue = 0f,
		maxValue = 1,
		minStep = 0.1f
	)
	public float customWitherCloakTransparency = 1.0f;

	Couldn't get this to work and couldn't for the life of me figure out why - Cobble8
	*/

	@Expose
	@ConfigAccordionId(id = 8)
	@ConfigOption(
		name = "Shield Distance From Player",
		desc = "How far (in blocks) each shield is from the player\n" +
			"0 = Inside the player"
	)
	@ConfigEditorSlider(
		minValue = 0f,
		maxValue = 3.0f,
		minStep = 0.1f
	)
	public float customWitherCloakDistance = 1.2f;

	@Expose
	@ConfigAccordionId(id = 8)
	@ConfigOption(
		name = "\u00A7aInspiration:",
		desc = "\u00A76u/Sori0612 \u00A77on \u00A7cReddit\n\n\u00A78https://tinyurl.com/creeperveil"
	)
	@ConfigEditorInfoText()
	public boolean customWitherCloakCredit = false;

	@Expose
	@ConfigOption(
		name = "Pickaxe Ability Cooldown",
		desc = "Show the cooldown duration of the pickaxe ability as the durability."
	)
	@ConfigEditorBoolean
	public boolean pickaxeAbility = true;

	@Expose
	@ConfigOption(
		name = "Bonzo Mask Ability Cooldown",
		desc = "Show the cooldown duration of a bonzo mask ability as the durability."
	)
	@ConfigEditorBoolean
	public boolean bonzoAbility = true;

	@Expose
	@ConfigOption(
		name = "Spirit Mask Ability Cooldown",
		desc = "Show the cooldown duration of the spirit mask ability as the durability."
	)
	@ConfigEditorBoolean
	public boolean spiritAbility = true;

}
