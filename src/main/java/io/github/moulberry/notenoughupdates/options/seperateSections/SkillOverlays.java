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
import io.github.moulberry.moulconfig.annotations.ConfigEditorDraggableList;
import io.github.moulberry.moulconfig.annotations.ConfigEditorDropdown;
import io.github.moulberry.moulconfig.annotations.ConfigEditorInfoText;
import io.github.moulberry.moulconfig.annotations.ConfigEditorKeybind;
import io.github.moulberry.moulconfig.annotations.ConfigEditorSlider;
import io.github.moulberry.moulconfig.annotations.ConfigOption;
import io.github.moulberry.notenoughupdates.core.config.Position;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static io.github.moulberry.notenoughupdates.overlays.FarmingSkillOverlay.CPS_WINDOW_SIZE;

public class SkillOverlays {
	@ConfigOption(
		name = "Skill Overlay info",
		desc =
			"For the overlays to show you need a \u00A7bmathematical hoe\u00A77 or an axe with \u00A7bcultivating\u00A77 " +
				"enchant for farming, a pickaxe with \u00A7bcompact\u00A77 for mining or a rod with \u00A7bexpertise\u00A77"
	)
	@ConfigEditorInfoText()
	public boolean skillInfo = false;

	@ConfigOption(
		name = "Farming",
		desc = ""
	)
	@ConfigEditorAccordion(id = 0)
	public boolean farmingAccordion = false;

	@Expose
	@ConfigOption(
		name = "Enable Farming Overlay",
		desc = "Show an overlay while farming with useful information"
	)
	@ConfigEditorBoolean
	@ConfigAccordionId(id = 0)
	public boolean farmingOverlay = true;

	@Expose
	@ConfigOption(
		name = "Farming Text",
		desc = "\u00a7eDrag text to change the appearance of the overlay\n" +
			"\u00a7rHold a mathematical hoe or use an axe with cultivating enchantment while gaining farming xp to show the overlay"
	)
	@ConfigEditorDraggableList(
		exampleText = {
			"\u00a7bCounter: \u00a7e37,547,860",
			"\u00a7bCrops/s: \u00a7e732",
			"\u00a7bFarming: \u00a7e12\u00a77 [\u00a7e|||||||||||||||||\u00a78||||||||\u00a77] \u00a7e67%",
			"\u00a7bCurrent XP: \u00a7e6,734",
			"\u00a7bRemaining XP: \u00a7e3,265",
			"\u00a7bXP/h: \u00a7e238,129",
			"\u00a7bYaw: \u00a7e68.25\u00a7l\u1D52",
			"\u00a7bETA: \u00a7e13h12m",
			"\u00a7bPitch: \u00a7e69.42\u00a7l\u1D52",
			"\u00a7bCultivating: \u00a7e10,137,945/20,000,000",
			"\u00a7bCoins/m \u00a7e57,432",
			"\u00a7bContest Estimate \u00a7e342,784",
		}
	)
	@ConfigAccordionId(id = 0)
	public List<Integer> farmingText = new ArrayList<>(Arrays.asList(0, 9, 10, 1, 2, 3, 4, 5, 7, 6, 11));

	@Expose
	@ConfigOption(
		name = "Use BZ Price For Coins/m",
		desc = "Uses the bazaar price instead of NPC price for coins/m"
	)
	@ConfigEditorBoolean
	@ConfigAccordionId(id = 0)
	public boolean useBZPrice = true;

	@Expose
	@ConfigOption(
		name = "Pause Timer",
		desc = "How many seconds does it wait before pausing the XP/h timer"
	)
	@ConfigAccordionId(id = 0)
	@ConfigEditorSlider(
		minValue = 1,
		maxValue = 20,
		minStep = 1
	)
	public int farmingPauseTimer = 3;

	@Expose
	@ConfigOption(
		name = "Crop rate time frame",
		desc = "Defines the duration in seconds over which the average crop yield is calculated"
	)
	@ConfigAccordionId(id = 0)
	@ConfigEditorSlider(
		minValue = 1,
		maxValue = CPS_WINDOW_SIZE - 2,
		minStep = 1
	)
	public int farmingCropsPerSecondTimeFrame = 5;

	@Expose
	@ConfigOption(
		name = "Crop rate unit",
		desc = "Choose the unit for displaying the crop rate"
	)
	@ConfigAccordionId(id = 0)
	@ConfigEditorDropdown(
		values = {"/s", "/m", "/h"}
	)
	public int farmingCropRateUnit = 0;

	@Expose
	@ConfigOption(
		name = "Coin rate unit",
		desc = "Choose the unit for displaying the coin rate"
	)
	@ConfigAccordionId(id = 0)
	@ConfigEditorDropdown(
		values = {"/s", "/m", "/h"}
	)
	public int farmingCoinRateUnit = 0;

	@Expose
	@ConfigOption(
		name = "Reset crop rate",
		desc = "How many seconds does it wait before resetting the crop rate values when inactive"
	)
	@ConfigAccordionId(id = 0)
	@ConfigEditorSlider(
		minValue = 1,
		maxValue = CPS_WINDOW_SIZE - 2,
		minStep = 1
	)
	public int farmingResetCPS = 5;

	@Expose
	public Position farmingPosition = new Position(10, 200);

	@Expose
	@ConfigOption(
		name = "Farming Style",
		desc = "Change the style of the Farming overlay"
	)
	@ConfigEditorDropdown(
		values = {"Background", "No Shadow", "Shadow", "Full Shadow"}
	)
	@ConfigAccordionId(id = 0)
	public int farmingStyle = 0;

	@ConfigOption(
		name = "Mining",
		desc = ""
	)
	@ConfigEditorAccordion(id = 1)
	public boolean miningAccordion = false;

	@Expose
	@ConfigOption(
		name = "Enable Mining Overlay",
		desc = "Show an overlay while Mining with useful information"
	)
	@ConfigEditorBoolean
	@ConfigAccordionId(id = 1)
	public boolean miningSkillOverlay = true;

	@Expose
	@ConfigOption(
		name = "Mining Text",
		desc = "\u00a7eDrag text to change the appearance of the overlay\n" +
			"\u00a7rHold a pickaxe with compact while gaining mining xp to show the overlay"
	)
	@ConfigEditorDraggableList(
		exampleText = {
			"\u00a7bCompact: \u00a7e547,860",
			"\u00a7bBlocks/m: \u00a7e38.29",
			"\u00a7bMining: \u00a7e12\u00a77 [\u00a7e|||||||||||||||||\u00a78||||||||\u00a77] \u00a7e67%",
			"\u00a7bCurrent XP: \u00a7e6,734",
			"\u00a7bRemaining XP: \u00a7e3,265",
			"\u00a7bXP/h: \u00a7e238,129",
			"\u00a7bYaw: \u00a7e68.25\u00a7l\u1D52",
			"\u00a7bETA: \u00a7e13h12m",
			"\u00a7bCompact Progress: \u00a7e137,945/150,000"
		}
	)
	@ConfigAccordionId(id = 1)
	public List<Integer> miningText = new ArrayList<>(Arrays.asList(0, 8, 1, 2, 3, 4, 5, 7));

	@Expose
	@ConfigOption(
		name = "Pause Timer",
		desc = "How many seconds does it wait before pausing"
	)
	@ConfigAccordionId(id = 1)
	@ConfigEditorSlider(
		minValue = 1,
		maxValue = 20,
		minStep = 1
	)
	public int miningPauseTimer = 3;

	@Expose
	public Position miningPosition = new Position(10, 200);

	@Expose
	@ConfigOption(
		name = "Mining Style",
		desc = "Change the style of the Mining overlay"
	)
	@ConfigEditorDropdown(
		values = {"Background", "No Shadow", "Shadow", "Full Shadow"}
	)
	@ConfigAccordionId(id = 1)
	public int miningStyle = 0;

	@ConfigOption(
		name = "Fishing",
		desc = ""
	)
	@ConfigEditorAccordion(id = 3)
	public boolean fishingAccordion = false;
	@Expose
	@ConfigOption(
		name = "Enable Fishing Overlay",
		desc = "Show an overlay while Fishing with useful information"
	)
	@ConfigEditorBoolean
	@ConfigAccordionId(id = 3)
	public boolean FishingSkillOverlay = true;

	@Expose
	@ConfigOption(
		name = "Fishing Text",
		desc = "\u00a7eDrag text to change the appearance of the overlay\n" +
			"\u00a7rHold a fishing rod with expertise enchantment while gaining fishing xp to show the overlay"
	)
	@ConfigEditorDraggableList(
		exampleText = {
			"\u00a7bExpertise: \u00a7e7,945/10,000",
			//"\u00a7bCatches/m: \u00a7e38.29",
			"\u00a7bFishing: \u00a7e12\u00a77 [\u00a7e|||||||||||||||||\u00a78||||||||\u00a77] \u00a7e67%",
			"\u00a7bCurrent XP: \u00a7e6,734",
			"\u00a7bRemaining XP: \u00a7e3,265",
			"\u00a7bXP/h: \u00a7e238,129",
			//"\u00a7bYaw: \u00a7e68.25\u00a7l\u1D52",
			"\u00a7bETA: \u00a7e13h12m",
			//"\u00a7bExpertise Progress: \u00a7e7,945/10,000",
			"\u00a7bTimer: \u00a7e1m15s"
		}
	)
	@ConfigAccordionId(id = 3)
	public List<Integer> fishingText = new ArrayList<>(Arrays.asList(0, 1, 2, 3, 4, 5, 6));

	@Expose
	@ConfigOption(
		name = "Pause Timer",
		desc = "How many seconds does it wait before pausing"
	)
	@ConfigAccordionId(id = 3)
	@ConfigEditorSlider(
		minValue = 1,
		maxValue = 20,
		minStep = 1
	)
	public int fishingPauseTimer = 3;

	@Expose
	public Position fishingPosition = new Position(10, 200);

	@Expose
	@ConfigOption(
		name = "Fishing Style",
		desc = "Change the style of the Fishing overlay"
	)
	@ConfigEditorDropdown(
		values = {"Background", "No Shadow", "Shadow", "Full Shadow"}
	)
	@ConfigAccordionId(id = 3)
	public int fishingStyle = 0;

	@Expose
	@ConfigOption(
		name = "Toggle Fishing timer",
		desc = "Start or stop the timer on the fishing overlay\n" +
			"Also can plays a ding customizable below"
	)
	@ConfigEditorKeybind(defaultKey = Keyboard.KEY_END)
	@ConfigAccordionId(id = 3)
	public int fishKey = Keyboard.KEY_END;

	@Expose
	@ConfigOption(
		name = "Fishing Timer Alert",
		desc = "Change the amount of time (seconds) until the timer dings"
	)
	@ConfigEditorSlider(
		minValue = 0,
		maxValue = 600,
		minStep = 20
	)
	@ConfigAccordionId(id = 3)
	public int customFishTimer = 300;

	@ConfigOption(
		name = "Combat",
		desc = ""
	)
	@ConfigEditorAccordion(id = 4)
	public boolean combatAccordion = false;

	@Expose
	@ConfigOption(
		name = "\u00A7cWarning",
		desc = "The combat display will only show if you have a Book of Stats or the Champion enchant"
	)
	@ConfigEditorInfoText()
	@ConfigAccordionId(id = 4)
	public boolean combatInfo = false;

	@Expose
	@ConfigOption(
		name = "Enable Combat Overlay",
		desc = "Show an overlay while Combat with useful information"
	)
	@ConfigEditorBoolean
	@ConfigAccordionId(id = 4)
	public boolean combatSkillOverlay = false;

	@Expose
	@ConfigOption(
		name = "Combat Text",
		desc = "\u00a7eDrag text to change the appearance of the overlay\n" +
			"\u00a7rHold an item with Book of Stats to show the display"
	)
	@ConfigEditorDraggableList(
		exampleText = {
			"\u00a7bKills: \u00a7e547,860",
			"\u00a7bCombat: \u00a7e12\u00a77 [\u00a7e|||||||||||||||||\u00a78||||||||\u00a77] \u00a7e67%",
			"\u00a7bCurrent XP: \u00a7e6,734",
			"\u00a7bRemaining XP: \u00a7e3,265",
			"\u00a7bXP/h: \u00a7e238,129",
			"\u00a7bETA: \u00a7e13h12m",
			"\u00a7bChampion XP: \u00a7e3,523"
		}
	)
	@ConfigAccordionId(id = 4)
	public List<Integer> combatText = new ArrayList<>(Arrays.asList(0, 6, 1, 2, 3, 4, 5));

	@Expose
	@ConfigOption(
		name = "Pause Timer",
		desc = "How many seconds does it wait before pausing"
	)
	@ConfigAccordionId(id = 4)
	@ConfigEditorSlider(
		minValue = 1,
		maxValue = 20,
		minStep = 1
	)
	public int combatPauseTimer = 3;

	@Expose
	public Position combatPosition = new Position(10, 200);

	@Expose
	@ConfigOption(
		name = "Combat Style",
		desc = "Change the style of the Combat overlay"
	)
	@ConfigEditorDropdown(
		values = {"Background", "No Shadow", "Shadow", "Full Shadow"}
	)
	@ConfigAccordionId(id = 4)
	public int combatStyle = 0;

	@Expose
	@ConfigOption(
		name = "Always show combat overlay",
		desc = "Shows combat overlay even if you dont have Book of Stats or the Champion enchant"
	)
	@ConfigEditorBoolean
	@ConfigAccordionId(id = 4)
	public boolean alwaysShowCombatOverlay = false;
}
