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
    // todo repo to map these locations + plot id?
    @SerializedName("unlocked_plots_ids") val unlockedPlotIds: List<String>,
    @SerializedName("commission_data") val commissionData: VisitorCommissions,
    @SerializedName("resources_collected") val resourcesCollected: Map<CropType, Int>,
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

enum class CropType(val itemId: String, val apiName: String) {
    WHEAT("ENCHANTED_HAY_BLOCK", "WHEAT"),
    NETHER_WART("ENCHANTED_NETHER_STALK", "NETHER_STALK"),
    SUGAR_CANE("ENCHANTED_SUGAR", "SUGAR_CANE"),
    CARROT("ENCHANTED_CARROT", "CARROT_ITEM"),
    POTATO("ENCHANTED_POTATO", "POTATO_ITEM"),
    COCOA_BEANS("ENCHANTED_COCOA", "INK_SACK:3"),
    PUMPKIN("ENCHANTED_PUMPKIN", "PUMPKIN"),
    MELON("ENCHANTED_MELON", "MELON"),
    CACTUS("ENCHANTED_CACTUS_GREEN", "CACTUS"),
    MUSHROOM("ENCHANTED_BROWN_MUSHROOM", "MUSHROOM_COLLECTION"),
    ;

    companion object {
        fun fromApiName(apiName: String): CropType? {
            val fromApiName = values().firstOrNull { it.apiName == apiName }
            if (fromApiName != null) return fromApiName
            return values().firstOrNull { it.name == apiName }
        }
    }
}

enum class VisitorRarity {
    UNCOMMON,
    RARE,
    LEGENDARY,
    MYTHIC,
    SPECIAL,
}
