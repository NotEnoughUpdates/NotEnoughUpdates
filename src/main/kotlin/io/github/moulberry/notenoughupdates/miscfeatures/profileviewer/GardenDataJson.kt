/*
 * Copyright (C) 2024 NotEnoughUpdates contributors
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

package io.github.moulberry.notenoughupdates.miscfeatures.profileviewer

import com.google.gson.annotations.SerializedName

data class GardenDataJson(
    val success: Boolean,
    val garden: GardenData,
)

data class GardenData(
    @SerializedName("unlocked_plots_ids") val unlockedPlotIds: List<String>,
    @SerializedName("commission_data") val commissionData: VisitorCommissions,
    @SerializedName("resources_collected") val resourcesCollected: Map<CropType, Long>,
    @SerializedName("garden_experience") val gardenExperience: Int,
    @SerializedName("composter_data") val composterData: ComposterData,
    @SerializedName("selected_barn_skin") val selectedBarnSkin: String,
    @SerializedName("crop_upgrade_levels") val cropUpgradeLevels: Map<CropType, Int>,
)

data class VisitorCommissions(
    val visits: Map<String, Int>,
    val completed: Map<String, Int>,
    @SerializedName("total_completed") val totalCompleted: Int,
    @SerializedName("unique_npcs_served") val uniqueNpcsServed: Int,
)

data class ComposterData(
    val upgrades: ComposterUpgrades,
)

data class ComposterUpgrades(
    val speed: Int,
    @SerializedName("multi_drop") val multiDrop: Int,
    @SerializedName("fuel_cap") val fuelCap: Int,
    @SerializedName("organic_matter_cap") val organicMatterCap: Int,
    @SerializedName("cost_reduction") val costReduction: Int,
)

data class GardenRepoJson(
    @SerializedName("garden_exp") val gardenExperience: List<Int>,
    @SerializedName("crop_milestones") val cropMilestones: Map<CropType, List<Int>>,
    @SerializedName("visitors") val visitors: Map<String, VisitorRarity>,
    val plots: Map<String, PlotData>,
    @SerializedName("plot_costs") val plotCosts: Map<String, List<PlotCost>>,
    @SerializedName("barn") val barn: Map<String, BarnSkin>,
)

data class PlotData(
    val name: String,
    val x: Int,
    val y: Int,
)

data class PlotCost(
    val item: String,
    val amount: Int,
)

data class BarnSkin(
    val name: String,
    val item: String,
)

enum class CropType(val itemId: String, val apiName: String, val displayName: String) {
    WHEAT("ENCHANTED_HAY_BLOCK", "WHEAT", "Wheat"),
    NETHER_WART("ENCHANTED_NETHER_STALK", "NETHER_STALK", "Nether Wart"),
    SUGAR_CANE("ENCHANTED_SUGAR", "SUGAR_CANE", "Sugar Cane"),
    CARROT("ENCHANTED_CARROT", "CARROT_ITEM", "Carrot"),
    POTATO("ENCHANTED_POTATO", "POTATO_ITEM", "Potato"),
    COCOA_BEANS("ENCHANTED_COCOA", "INK_SACK:3", "Cocoa Beans"),
    PUMPKIN("ENCHANTED_PUMPKIN", "PUMPKIN", "Pumpkin"),
    MELON("ENCHANTED_MELON", "MELON", "Melon"),
    CACTUS("ENCHANTED_CACTUS_GREEN", "CACTUS", "Cactus"),
    MUSHROOM("ENCHANTED_BROWN_MUSHROOM", "MUSHROOM_COLLECTION", "Mushroom"),
    ;

    companion object {
        fun fromApiName(apiName: String): CropType? {
            val fromApiName = values().firstOrNull { it.apiName == apiName }
            if (fromApiName != null) return fromApiName
            return values().firstOrNull { it.name == apiName }
        }
    }
}

enum class VisitorRarity(val displayName: String, var visits: Int = 0, var completed: Int = 0) {
    UNCOMMON("§aUncommon"),
    RARE("§9Rare"),
    LEGENDARY("§6Legendary"),
    MYTHIC("§dMythic"),
    SPECIAL("§cSpecial"),
    TOTAL("§7Total"),
    ;

    fun addVisits(visits: Int) {
        this.visits += visits
        if (this != TOTAL) {
            TOTAL.visits += visits
        }
    }

    fun addCompleted(completed: Int) {
        this.completed += completed
        if (this != TOTAL) {
            TOTAL.completed += completed
        }
    }
}
