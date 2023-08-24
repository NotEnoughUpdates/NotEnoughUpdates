/*
 * Copyright (C) 2022-2023 NotEnoughUpdates contributors
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
import io.github.moulberry.moulconfig.annotations.ConfigEditorKeybind;
import io.github.moulberry.moulconfig.annotations.ConfigEditorSlider;
import io.github.moulberry.moulconfig.annotations.ConfigOption;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TooltipTweaks {
	@ConfigOption(
		name = "Tooltip Price Information",
		desc = ""
	)
	@ConfigEditorAccordion(id = 0)
	public boolean priceInfoAccordion = false;

	@Expose
	@ConfigOption(
		name = "Price Info (Auc)",
		desc = "\u00a7rSelect what price information you would like to see on auctionable item tooltips\n" +
			"\u00a7eDrag text to rearrange"
	)
	@ConfigEditorDraggableList(
		exampleText = {
			"\u00a7eLowest BIN",
			"\u00a7eAH Price",
			"\u00a7eAH Sales",
			"\u00a7eRaw Craft Cost",
			"\u00a7eAVG Lowest BIN",
			"\u00a7eDungeon Costs"
		}
	)
	@ConfigAccordionId(id = 0)
	public List<Integer> priceInfoAuc = new ArrayList<>(Arrays.asList(0, 1, 2, 3, 5));

	@Expose
	@ConfigOption(
		name = "Price Info (Baz)",
		desc = "\u00a7rSelect what price information you would like to see on bazaar item tooltips\n" +
			"\u00a7eDrag text to rearrange"
	)
	@ConfigEditorDraggableList(
		exampleText = {
			"\u00a7eBuy",
			"\u00a7eSell",
			"\u00a7eBuy (Insta)",
			"\u00a7eSell (Insta)",
			"\u00a7eRaw Craft Cost",
			"\u00a7eInsta-Buys (Hourly)",
			"\u00a7eInsta-Sells (Hourly)",
			"\u00a7eInsta-Buys (Daily)",
			"\u00a7eInsta-Sells (Daily)",
			"\u00a7eInsta-Buys (Weekly)",
			"\u00a7eInsta-Sells (Weekly)"}
	)
	@ConfigAccordionId(id = 0)
	public List<Integer> priceInfoBaz = new ArrayList<>(Arrays.asList(0, 1, 2, 3, 4));

	@Expose
	@ConfigOption(
		name = "Show raw craft on items that can't be sold",
		desc = "Raw craft cost will be shown on items that can't be sold on the ah or bz"
	)
	@ConfigEditorBoolean
	@ConfigAccordionId(id = 0)
	public boolean rawCraft = true;

	@Expose
	@ConfigOption(
		name = "Show NPC Sell price on Items",
		desc = "Display for how much items can be sold to NPC Shops"
	)
	@ConfigEditorBoolean
	@ConfigAccordionId(id = 0)
	public boolean npcSellPrice = false;

	@Expose
	@ConfigOption(
		name = "Display donation status",
		desc = "Add an extra line on items you have already donated to the museum. Visit all pages of the museum to populate this list."
	)
	@ConfigEditorBoolean
	@ConfigAccordionId(id = 0)
	public boolean museumDonationStatus = false;

	@Expose
	@ConfigOption(
		name = "Use Short Number Format",
		desc = "Use Short Numbers (5.1m) instead of 5,130,302"
	)
	@ConfigEditorBoolean
	@ConfigAccordionId(id = 0)
	public boolean shortNumberFormatPrices = false;

	@Expose
	@ConfigOption(
		name = "Price Info (Inv)",
		desc = "Show price information for items in your inventory"
	)
	@ConfigEditorBoolean
	public boolean showPriceInfoInvItem = true;

	@Expose
	@ConfigOption(
		name = "Price Info (AH)",
		desc = "Show price information for auctioned items"
	)
	@ConfigEditorBoolean
	public boolean showPriceInfoAucItem = true;

	@Expose
	@ConfigOption(
		name = "Price info keybind",
		desc = "Only show price info if holding a key."
	)
	@ConfigEditorBoolean
	public boolean disablePriceKey = false;

	@Expose
	@ConfigOption(
		name = "Show Price info Keybind",
		desc = "Hold this key to show a price info tooltip"
	)
	@ConfigEditorKeybind(defaultKey = Keyboard.KEY_NONE)
	public int disablePriceKeyKeybind = Keyboard.KEY_NONE;

	@Expose
	@ConfigOption(
		name = "Always show required dungeon items",
		desc = "Always show the required items to upgrade to the next star if more than just Essence is needed"
	)
	@ConfigEditorBoolean
	public boolean alwaysShowRequiredItems = false;

	@Expose
	@ConfigOption(
		name = "Show reforge stats",
		desc = "Show statistics a reforge stone will apply."
	)
	@ConfigEditorBoolean
	public boolean showReforgeStats = true;

	@Expose
	@ConfigOption(
		name = "Hide default reforge stats",
		desc = "Hides the reforge stats only for Legendary items that Hypixel adds to the Reforge stones"
	)
	@ConfigEditorBoolean
	public boolean hideDefaultReforgeStats = true;

	@Expose
	@ConfigOption(
		name = "Missing Enchant List",
		desc = "Show which enchants are missing on an item when pressing LSHIFT"
	)
	@ConfigEditorBoolean
	public boolean missingEnchantList = true;

	@Expose
	@ConfigOption(
		name = "Resize tooltips",
		desc = "Resizes tooltips to make them readable"
	)
	@ConfigEditorDropdown(
		values = {"Default", "Small", "Normal", "Large", "Auto"}
	)
	public int guiScale = 0;

	@Expose
	@ConfigOption(
		name = "Custom tooltips",
		desc = "Replace tooltips with neu's custom tooltips"
	)
	@ConfigEditorBoolean
	public boolean customTooltips = false;

	@Expose
	@ConfigOption(
		name = "Scrollable Tooltips",
		desc = "Make tooltips text scrollable, by making some text lines disappear when using the mouse while hovering over an item."
	)
	@ConfigEditorBoolean
	public boolean scrollableTooltips = false;


	@Expose
	@ConfigOption(
		name = "Expand Pet Exp Requirement",
		desc = "Show which the full amount of pet xp required"
	)
	@ConfigEditorBoolean
	public boolean petExtendExp = false;

	@Expose
	@ConfigOption(
		name = "Tooltip Border Colours",
		desc = "Make the borders of tooltips match the rarity of the item (NEU Tooltips Only)"
	)
	@ConfigEditorBoolean
	public boolean tooltipBorderColours = true;

	@Expose
	@ConfigOption(
		name = "Tooltip Border Opacity",
		desc = "Change the opacity of the rarity highlight (NEU Tooltips Only)"
	)
	@ConfigEditorSlider(
		minValue = 0f,
		maxValue = 255f,
		minStep = 1f
	)
	public int tooltipBorderOpacity = 200;

	@Expose
	@ConfigOption(
		name = "Power Stone Stats",
		desc = "Show your real magical power and real stat increase on power stones"
	)
	@ConfigEditorBoolean
	public boolean powerStoneStats = true;

	@ConfigOption(
		name = "RNG Meter",
		desc = ""
	)
	@ConfigEditorAccordion(id = 1)
	public boolean rngMeter = false;

	@Expose
	@ConfigOption(
		name = "Fraction Display",
		desc = "Show the fraction instead of the percentage in the slayer and dungeon rng meter inventory"
	)
	@ConfigEditorBoolean
	@ConfigAccordionId(id = 1)
	public boolean rngMeterFractionDisplay = true;

	@Expose
	@ConfigOption(
		name = " Profit Per Score/XP",
		desc = "Show the amount of coins per Score/XP in the rng meter inventory"
	)
	@ConfigEditorBoolean
	@ConfigAccordionId(id = 1)
	public boolean rngMeterProfitPerUnit = true;

	@Expose
	@ConfigOption(
		name = "Dungeon/Slayer Needed Counter",
		desc = "Show the amount of dungeon runs or slayer bosses needed for the rng meter to fill up"
	)
	@ConfigEditorBoolean
	@ConfigAccordionId(id = 1)
	public boolean rngMeterRunsNeeded = true;

	@Expose
	@ConfigOption(
		name = "Abiphone NPC Requirements",
		desc = "Show what the NPC needs in order to add him as contact in the abiphone"
	)
	@ConfigEditorBoolean
	public boolean abiphoneContactRequirements = true;

	@Expose
	@ConfigOption(
		name = "Abiphone NPC Location",
		desc = "Click on an NPC to set a marker at the location"
	)
	@ConfigEditorBoolean
	public boolean abiphoneContactMarker = true;

	@Expose
	@ConfigOption(
		name = "Essence Price In Shop",
		desc = "Show the essence price in the essence shop in the dungeon hub"
	)
	@ConfigEditorBoolean
	public boolean essencePriceInEssenceShop = true;

	@ConfigOption(
		name = "Garden Visitors",
		desc = ""
	)
	@ConfigEditorAccordion(id = 2)
	public boolean gardenVisitors = false;

	@Expose
	@ConfigOption(
		name = "Item Cost in Garden NPC",
		desc = "Show the item cost in garden NPC shop"
	)
	@ConfigEditorBoolean
	@ConfigAccordionId(id = 2)
	public boolean gardenNpcPrice = true;

	@Expose
	@ConfigOption(
		name = "Copper value",
		desc = "Calculate how much coins is a copper worth"
	)
	@ConfigEditorBoolean
	@ConfigAccordionId(id = 2)
	public boolean copperCoins = true;


	@Expose
	@ConfigOption(
		name = "AH items cost threshold",
		desc = "Ignore AH items that cost less or equal copper than"
	)
	@ConfigEditorSlider(
		minValue = 0f,
		maxValue = 500f,
		minStep = 5f
	)
	@ConfigAccordionId(id = 2)
	public double AHPriceIgnoreThreshold = 0;
	@Expose
	@ConfigOption(
		name = "Ignore AH items",
		desc = "Ignore ALL items that can be sold to AH"
	)
	@ConfigEditorBoolean
	@ConfigAccordionId(id = 2)
	public boolean ignoreAllAHItems = true;
}
