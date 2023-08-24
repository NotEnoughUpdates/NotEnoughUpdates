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
import io.github.moulberry.moulconfig.annotations.ConfigEditorColour;
import io.github.moulberry.moulconfig.annotations.ConfigEditorDropdown;
import io.github.moulberry.moulconfig.annotations.ConfigEditorSlider;
import io.github.moulberry.moulconfig.annotations.ConfigOption;

public class Fishing {
	@Expose
	@ConfigOption(
		name = "Hide Other Players Fishing",
		desc = "Convenience option to easily hide \u00a7lother players'\u00a7r bobbers, rod lines and fishing particles\n" +
			"The advanced options below allow you to set the precise colour, particles, etc."
	)
	@ConfigEditorBoolean
	public boolean hideOtherPlayerAll = false;

	@ConfigOption(
		name = "Incoming Fish Warning",
		desc = ""
	)
	@ConfigEditorAccordion(id = 3)
	public boolean incomingFishAccordion = false;

	@Expose
	@ConfigOption(
		name = "Enable Fish Warning (R)",
		desc = "Display a red '!' when you need to pull the fish up. The warning takes your ping into account"
	)
	@ConfigEditorBoolean
	@ConfigAccordionId(id = 3)
	public boolean incomingFishWarningR = true;

	@Expose
	@ConfigOption(
		name = "Enable Fish Warning (Y)",
		desc = "Display a yellow '!' when a fish is approaching your bobber"
	)
	@ConfigEditorBoolean
	@ConfigAccordionId(id = 3)
	public boolean incomingFishWarning = true;

	@Expose
	@ConfigOption(
		name = "Enable Hooked Sound",
		desc = "Play a high-pitched ding sound when the '!' turns red"
	)
	@ConfigEditorBoolean
	@ConfigAccordionId(id = 3)
	public boolean incomingFishHookedSounds = true;

	@Expose
	@ConfigOption(
		name = "Enable Approach Sound",
		desc = "Play low-pitched ding sounds while the yellow '!' is visible"
	)
	@ConfigEditorBoolean
	@ConfigAccordionId(id = 3)
	public boolean incomingFishIncSounds = false;

	@ConfigOption(
		name = "Volumes",
		desc = ""
	)
	@ConfigAccordionId(id = 3)
	@ConfigEditorAccordion(id = 5)
	public boolean incomingFishVolumeAccordion = false;

	@Expose
	@ConfigOption(
		name = "Hooked Sound Vol.",
		desc = "Set the volume of the hooked sound"
	)
	@ConfigEditorSlider(
		minValue = 0,
		maxValue = 100,
		minStep = 1
	)
	@ConfigAccordionId(id = 5)
	public float incomingFishHookedSoundsVol = 25;

	@Expose
	@ConfigOption(
		name = "Approach Sound Vol.",
		desc = "Set the volume of the approaching sound"
	)
	@ConfigEditorSlider(
		minValue = 0,
		maxValue = 100,
		minStep = 1
	)
	@ConfigAccordionId(id = 5)
	public float incomingFishIncSoundsVol = 10;

	@ConfigOption(
		name = "Fishing Particles",
		desc = ""
	)
	@ConfigEditorAccordion(id = 0)
	public boolean particleAccordion = false;

	@Expose
	@ConfigOption(
		name = "Enable Custom Particles",
		desc = "Allow you to modify the particles that appear when a fish is incoming for you and other players"
	)
	@ConfigEditorBoolean
	@ConfigAccordionId(id = 0)
	public boolean enableCustomParticles = false;

	@ConfigOption(
		name = "Your Particles",
		desc = ""
	)
	@ConfigEditorAccordion(id = 1)
	@ConfigAccordionId(id = 0)
	public boolean yourParticlesAccordion = false;

	@Expose
	@ConfigOption(
		name = "Particle Type",
		desc = "Change the type of the particle that is spawned\n" +
			"Particle types with (RGB) support custom colours\n" +
			"Set to 'NONE' to disable particles"
	)
	@ConfigEditorDropdown(
		values = {"Default", "None", "Spark (RGB)", "Swirl (RGB)", "Dust (RGB)", "Flame", "Crit", "Magic Crit"}
	)
	@ConfigAccordionId(id = 1)
	public int yourParticleType = 0;

	@Expose
	@ConfigOption(
		name = "Custom Colour",
		desc = "Set a custom colour for the particle\n" +
			"Only works for particle types with (RGB)"
	)
	@ConfigEditorColour
	@ConfigAccordionId(id = 1)
	public String yourParticleColour = "0:255:255:255:255";

	@ConfigOption(
		name = "Other Players' Particles",
		desc = ""
	)
	@ConfigEditorAccordion(id = 2)
	@ConfigAccordionId(id = 0)
	public boolean otherParticlesAccordion = false;

	@Expose
	@ConfigOption(
		name = "Particle Type",
		desc = "Change the type of the particle that is spawned\n" +
			"Particle types with (RGB) support custom colours\n" +
			"Set to 'NONE' to disable particles"
	)
	@ConfigEditorDropdown(
		values = {"Default", "None", "Spark (RGB)", "Swirl (RGB)", "Dust (RGB)", "Flame", "Crit", "Magic Crit"}
	)
	@ConfigAccordionId(id = 2)
	public int otherParticleType = 0;

	@Expose
	@ConfigOption(
		name = "Custom Colour",
		desc = "Set a custom colour for the particle\n" +
			"Only works for particle types with (RGB)"
	)
	@ConfigEditorColour
	@ConfigAccordionId(id = 2)
	public String otherParticleColour = "0:255:255:255:255";

	@ConfigOption(
		name = "Rod Line Colours",
		desc = ""
	)
	@ConfigEditorAccordion(id = 4)
	public boolean rodAccordion = false;

	@Expose
	@ConfigOption(
		name = "Enable Rod Line Colours",
		desc = "Change the colour of your and other players' rod lines\n" +
			"Also fixes the position of the rod line"
	)
	@ConfigEditorBoolean
	@ConfigAccordionId(id = 4)
	public boolean enableRodColours = true;

	@Expose
	@ConfigOption(
		name = "Own Rod Colour",
		desc = "Change the colour of your own rod lines\n" +
			"You can set the opacity to '0' to HIDE"
	)
	@ConfigEditorColour
	@ConfigAccordionId(id = 4)
	public String ownRodColour = "0:255:0:0:0";

	@Expose
	@ConfigOption(
		name = "Other Rod Colour",
		desc = "Change the colour of other players' rod lines\n" +
			"You can set the opacity to '0' to HIDE"
	)
	@ConfigEditorColour
	@ConfigAccordionId(id = 4)
	public String otherRodColour = "0:255:0:0:0";

	@ConfigOption(
		name = "Fishing Timer",
		desc = ""
	)
	@ConfigEditorAccordion(id = 6)
	public boolean fishingAccordion = false;

	@Expose
	@ConfigOption(
		name = "Display a Fishing Timer",
		desc = "Display a timer above your bobber showing your current time"
	)
	@ConfigEditorBoolean
	@ConfigAccordionId(id = 6)
	public boolean fishingTimer = false;

	@Expose
	@ConfigOption(
		name = "Fishing timer colour",
		desc = "Colour of the fishing timer"
	)
	@ConfigEditorColour
	@ConfigAccordionId(id = 6)
	public String fishingTimerColor = "0:255:0:0:0";

	@Expose
	@ConfigOption(
		name = "Fishing timer colour (20s)",
		desc = "Colour of the fishing timer after 20 seconds or more have passed"
	)
	@ConfigEditorColour
	@ConfigAccordionId(id = 6)
	public String fishingTimerColor30SecPlus = "0:255:0:0:0";

	@Expose
	@ConfigOption(
		name = "Fishing timer ping (20s)",
		desc = "Play a sound after 20 seconds passed"
	)
	@ConfigEditorBoolean
	@ConfigAccordionId(id = 6)
	public boolean fishingSound30Sec = true;

	@ConfigOption(
		name = "Trophy Reward",
		desc = ""
	)
	@ConfigEditorAccordion(id = 7)
	public boolean trophyReward = false;

	@Expose
	@ConfigOption(
		name = "Trophy Reward Overlay",
		desc = "Displays an overlay at Odger that shows information about your trophies"
	)
	@ConfigEditorBoolean
	@ConfigAccordionId(id = 7)
	public boolean trophyRewardOverlay = true;

	@Expose
	@ConfigOption(
		name = "Trophy Reward Tooltips",
		desc = "Displays the exchange of your trophies as a tooltip in the Odger Inventory"
	)
	@ConfigEditorBoolean
	@ConfigAccordionId(id = 7)
	public boolean trophyRewardTooltips = true;

}
