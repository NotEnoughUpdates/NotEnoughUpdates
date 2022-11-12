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

package io.github.moulberry.notenoughupdates.miscgui.hex;

import com.google.gson.JsonObject;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.util.Constants;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.util.StringUtils;

import java.util.List;

public class HexItem {
	public int slotIndex;
	public String itemName;
	public String itemId;
	public List<String> displayLore;
	public int level;
	public int price = -1;
	public boolean overMaxLevel = false;
	public boolean conflicts = false;
	public ItemType itemType;
	public int gemstoneLevel = -1;

	public HexItem(
		int slotIndex, String itemName, String itemId, List<String> displayLore,
		boolean useMaxLevelForCost, boolean checkConflicts
	) {
		this.slotIndex = slotIndex;
		this.itemName = itemName;
		this.itemId = itemId.replace("'S", "");
		this.displayLore = displayLore;
		switch (itemId) {
			default:
				this.itemType = ItemType.UNKNOWN;
				break;
			case "HOT_POTATO_BOOK":
				this.itemType = ItemType.HOT_POTATO;
				break;
			case "FUMING_POTATO_BOOK":
				this.itemType = ItemType.FUMING_POTATO;
				break;
			case "BOOK_OF_STATS":
				this.itemType = ItemType.BOOK_OF_STATS;
				break;
			case "THE_ART_OF_WAR":
				this.itemType = ItemType.ART_OF_WAR;
				break;
			case "FARMING_FOR_DUMMIES":
				this.itemType = ItemType.FARMING_DUMMY;
				break;
			case "THE_ART_OF_PEACE":
				this.itemType = ItemType.ART_OF_PEACE;
				break;
			case "RECOMBOBULATOR_3000":
				this.itemType = ItemType.RECOMB;
				break;
			case "SILEX":
				this.itemId = "SIL_EX";
				this.itemType = ItemType.SILEX;
				break;
			case "RUBY_POWER_SCROLL":
				this.itemType = ItemType.RUBY_SCROLL;
				break;
			case "SAPPHIRE_POWER_SCROLL":
				this.itemType = ItemType.SAPPHIRE_SCROLL;
				break;
			case "JASPER_POWER_SCROLL":
				this.itemType = ItemType.JASPER_SCROLL;
				break;
			case "AMETHYST_POWER_SCROLL":
				this.itemType = ItemType.AMETHYST_SCROLL;
				break;
			case "AMBER_POWER_SCROLL":
				this.itemType = ItemType.AMBER_SCROLL;
				break;
			case "OPAL_POWER_SCROLL":
				this.itemType = ItemType.OPAL_SCROLL;
				break;
			case "FIRST_MASTER_STAR":
				this.itemType = ItemType.FIRST_MASTER_STAR;
				break;
			case "SECOND_MASTER_STAR":
				this.itemType = ItemType.SECOND_MASTER_STAR;
				break;
			case "THIRD_MASTER_STAR":
				this.itemType = ItemType.THIRD_MASTER_STAR;
				break;
			case "FOURTH_MASTER_STAR":
				this.itemType = ItemType.FOURTH_MASTER_STAR;
				break;
			case "FIFTH_MASTER_STAR":
				this.itemType = ItemType.FIFTH_MASTER_STAR;
				break;
			case "WOOD_SINGULARITY":
				this.itemType = ItemType.WOOD_SINGULARITY;
				break;
			case "IMPLOSION":
				this.itemType = ItemType.IMPLOSION_SCROLL;
				break;
			case "WITHER_SHIELD":
				this.itemType = ItemType.WITHER_SHIELD_SCROLL;
				break;
			case "SHADOW_WARP":
				this.itemType = ItemType.SHADOW_WARP_SCROLL;
				break;
			case "TRANSMISSION_TUNER":
				this.itemType = ItemType.TUNER;
				break;
			case "RANDOM_REFORGE":
				this.itemType = ItemType.RANDOM_REFORGE;
				break;
			case "MANA_DISINTEGRATOR":
				this.itemType = ItemType.MANA_DISINTEGRATOR;
				break;
			case "TOTAL_UPGRADES":
				this.itemType = ItemType.TOTAL_UPGRADES;
				break;
			case "CONVERT_TO_DUNGEON":
				this.itemType = ItemType.CONVERT_TO_DUNGEON;
				break;
			case "EXPERIENCE_BOTTLE":
				this.itemType = ItemType.EXPERIENCE_BOTTLE;
				break;
			case "GRAND_EXPERIENCE_BOTTLE":
				this.itemType = ItemType.GRAND_EXPERIENCE_BOTTLE;
				break;
			case "TITANIC_EXPERIENCE_BOTTLE":
				this.itemType = ItemType.TITANIC_EXPERIENCE_BOTTLE;
				break;
			case "COLOSSAL_EXPERIENCE_BOTTLE":
				this.itemType = ItemType.COLOSSAL_EXPERIENCE_BOTTLE;
				break;
			case "FEROCITY_ENRICHMENT":
				this.itemType = ItemType.ENRICHMENT_FEROCITY;
				break;
			case "SEA_CREATURE_CHANCE_ENRICHMENT":
				this.itemType = ItemType.ENRICHMENT_SCC;
				break;
			case "ATTACK_SPEED_ENRICHMENT":
				this.itemType = ItemType.ENRICHMENT_ATTACK_SPEED;
				break;
			case "SPEED_ENRICHMENT":
				this.itemType = ItemType.ENRICHMENT_SPEED;
				break;
			case "INTELLIGENCE_ENRICHMENT":
				this.itemType = ItemType.ENRICHMENT_INTELLIGENCE;
				break;
			case "CRITICAL_DAMAGE_ENRICHMENT":
				this.itemType = ItemType.ENRICHMENT_CRIT_DAMAGE;
				break;
			case "STRENGTH_ENRICHMENT":
				this.itemType = ItemType.ENRICHMENT_STRENGTH;
				break;
			case "DEFENSE_ENRICHMENT":
				this.itemType = ItemType.ENRICHMENT_DEFENSE;
				break;
			case "HEALTH_ENRICHMENT":
				this.itemType = ItemType.ENRICHMENT_HEALTH;
				break;
			case "MAGIC_FIND_ENRICHMENT":
				this.itemType = ItemType.ENRICHMENT_MAGIC_FIND;
				break;
			case "CRITICAL_CHANCE_ENRICHMENT":
				this.itemType = ItemType.ENRICHMENT_CRIT_CHANCE;
				break;
		}
		if (this.itemType == ItemType.UNKNOWN) {
			for (String string : displayLore) {
				if ((string.contains("Applies the") && string.contains("reforge")) ||
					string.contains("reforge when combined")) {
					this.itemType = ItemType.REFORGE;
					break;
				}
			}
		}
		if (!this.isMasterStar() && itemId.contains("✪")) {
			if (itemId.contains("✪✪✪✪✪")) this.itemType = ItemType.FIFTH_STAR;
			else if (itemId.contains("✪✪✪✪")) this.itemType = ItemType.FOURTH_STAR;
			else if (itemId.contains("✪✪✪")) this.itemType = ItemType.THIRD_STAR;
			else if (itemId.contains("✪✪")) this.itemType = ItemType.SECOND_STAR;
			else if (itemId.contains("✪")) this.itemType = ItemType.FIRST_STAR;
		}
		if (this.itemId.contains("EXPERIENCE_BOTTLE")) {
			this.itemId = this.itemId.replace("EXPERIENCE_BOTTLE", "EXP_BOTTLE");
		}
		if (this.itemId.contains("END_STONE_GEODE")) {
			this.itemId = this.itemId.replace("END_STONE_GEODE", "ENDSTONE_GEODE");
		}
		if (itemId.contains("HEX_ITEM")) this.itemType = ItemType.HEX_ITEM;
		JsonObject bazaarInfo = NotEnoughUpdates.INSTANCE.manager.auctionManager.getBazaarInfo(this.itemId);
		if (bazaarInfo != null && bazaarInfo.get("curr_buy") != null) {
			this.price = bazaarInfo.get("curr_buy").getAsInt();
		}
		if ("SIL_EX".equals(this.itemId)) this.itemId = "SILEX";
		if (itemName.contains("Amethyst Gemstone")) this.itemType = ItemType.AMETHYST_GEMSTONE;
		if (itemName.contains("Ruby Gemstone")) this.itemType = ItemType.RUBY_GEMSTONE;
		if (itemName.contains("Sapphire Gemstone")) this.itemType = ItemType.SAPPHIRE_GEMSTONE;
		if (itemName.contains("Jasper Gemstone")) this.itemType = ItemType.JASPER_GEMSTONE;
		if (itemName.contains("Jade Gemstone")) this.itemType = ItemType.JADE_GEMSTONE;
		if (itemName.contains("Amber Gemstone")) this.itemType = ItemType.AMBER_GEMSTONE;
		if (itemName.contains("Opal Gemstone")) this.itemType = ItemType.OPAL_GEMSTONE;
		if (itemName.contains("Topaz Gemstone")) this.itemType = ItemType.TOPAZ_GEMSTONE;
		if (itemName.contains("Gemstone Slot")) this.itemType = ItemType.GEMSTONE_SLOT;
		if (this.itemName.contains(" Gemstone")) {
			this.itemName = this.itemName.replace(" Gemstone", "").substring(2);
		} else if (this.itemName.contains(" Experience Bottle")) {
			this.itemName = this.itemName.replace("Experience Bottle", "");
		} else if (this.itemName.equals("Experience Bottle")) {
			this.itemName = "Exp Bottle";
		}
		if (isGemstone()) {
			if (this.itemName.contains("Rough")) this.gemstoneLevel = 0;
			if (this.itemName.contains("Flawed")) this.gemstoneLevel = 1;
			if (this.itemName.contains("Fine")) this.gemstoneLevel = 2;
			if (this.itemName.contains("Flawless")) this.gemstoneLevel = 3;
			if (this.itemName.contains("Perfect")) this.gemstoneLevel = 4;
		}
	}

	public boolean isPowerScroll() {
		return itemType == ItemType.RUBY_SCROLL || itemType == ItemType.SAPPHIRE_SCROLL ||
			itemType == ItemType.JASPER_SCROLL || itemType == ItemType.AMETHYST_SCROLL ||
			itemType == ItemType.AMBER_SCROLL || itemType == ItemType.OPAL_SCROLL;
	}

	public boolean isDungeonStar() {
		return itemType == ItemType.FIRST_STAR || itemType == ItemType.SECOND_STAR ||
			itemType == ItemType.THIRD_STAR || itemType == ItemType.FOURTH_STAR ||
			itemType == ItemType.FIFTH_STAR;
	}

	public boolean isMasterStar() {
		return itemType == ItemType.FIRST_MASTER_STAR || itemType == ItemType.SECOND_MASTER_STAR ||
			itemType == ItemType.THIRD_MASTER_STAR || itemType == ItemType.FOURTH_MASTER_STAR ||
			itemType == ItemType.FIFTH_MASTER_STAR;
	}

	public String getReforge() {
		JsonObject reforgeStones = Constants.REFORGESTONES;
		if (reforgeStones != null && reforgeStones.has(this.itemId.toUpperCase())) {
			JsonObject reforgeInfo = reforgeStones.get(this.itemId.toUpperCase()).getAsJsonObject();
			if (reforgeInfo != null) {
				return Utils.getElementAsString(reforgeInfo.get("reforgeName"), "");
			}

		}
		return "";
	}

	public int getPrice() {
		if (this.itemType == ItemType.RANDOM_REFORGE) {
			for (String string : displayLore) {
				if (string.contains("Coins")) {
					try {
						price = Integer.parseInt(StringUtils
							.stripControlCodes(string)
							.replace(" Coins", "")
							.replace(",", "")
							.trim());
					} catch (NumberFormatException ignored) {
					}
				}
			}
		}
		return price;
	}

	public boolean isHypeScroll() {
		return itemType == ItemType.IMPLOSION_SCROLL || itemType == ItemType.WITHER_SHIELD_SCROLL ||
			itemType == ItemType.SHADOW_WARP_SCROLL;
	}

	public boolean isGemstone() {
		return itemType == ItemType.RUBY_GEMSTONE || itemType == ItemType.AMETHYST_GEMSTONE ||
			itemType == ItemType.SAPPHIRE_GEMSTONE || itemType == ItemType.JASPER_GEMSTONE ||
			itemType == ItemType.JADE_GEMSTONE || itemType == ItemType.AMBER_GEMSTONE ||
			itemType == ItemType.OPAL_GEMSTONE || itemType == ItemType.TOPAZ_GEMSTONE;
	}

	public boolean isEnrichment() {
		return itemType == ItemType.ENRICHMENT_DEFENSE || itemType == ItemType.ENRICHMENT_SCC ||
			itemType == ItemType.ENRICHMENT_HEALTH || itemType == ItemType.ENRICHMENT_STRENGTH ||
			itemType == ItemType.ENRICHMENT_SPEED || itemType == ItemType.ENRICHMENT_CRIT_DAMAGE ||
			itemType == ItemType.ENRICHMENT_CRIT_CHANCE || itemType == ItemType.ENRICHMENT_ATTACK_SPEED ||
			itemType == ItemType.ENRICHMENT_INTELLIGENCE || itemType == ItemType.ENRICHMENT_MAGIC_FIND ||
			itemType == ItemType.ENRICHMENT_FEROCITY;
	}
}
