/*
 * Copyright (C) 2023 NotEnoughUpdates contributors
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

package io.github.moulberry.notenoughupdates.profileviewer.bestiary

import com.google.gson.JsonObject
import io.github.moulberry.notenoughupdates.util.Constants
import io.github.moulberry.notenoughupdates.util.ItemUtils
import io.github.moulberry.notenoughupdates.util.Utils
import kotlin.math.min

object BestiaryData {
    private val categoriesToParse = listOf(
        "dynamic",
        "hub",
        "farming_1",
        "combat_1",
        "combat_3",
        "crimson_isle",
        "mining_2",
        "mining_3",
        "crystal_hollows",
        "foraging_1",
        "spooky_festival",
        "mythological_creatures",
        "jerry",
        "kuudra",
        "catacombs",
        "fishing"
    )

    /**
     * Calculates the sum of all individual tiers for this profile
     *
     * @param computedCategories List of parsed categories
     * @see BestiaryPage.parseBestiaryData
     */
    @JvmStatic
    fun calculateTotalBestiaryTiers(computedCategories: List<Category>): Int {
        var tiers = 0.0
        computedCategories.forEach {
            tiers += countTotalLevels(it)
        }
        return tiers.toInt()
    }

    /**
     * Calculate the skyblock xp awarded for the given bestiary progress
     */
    @JvmStatic
    fun calculateBestiarySkyblockXp(profileInfo: JsonObject): Int {
        val totalTiers = calculateTotalBestiaryTiers(parseBestiaryData(profileInfo))
        var skyblockXp = 0

        val slayingTask = Constants.SBLEVELS.getAsJsonObject("slaying_task") ?: return 0
        val xpPerTier = (slayingTask.get("bestiary_family_xp") ?: return 0).asInt
        val xpPerMilestone = slayingTask.get("bestiary_milestone_xp").asInt
        val maxXp = slayingTask.get("bestiary_progress").asInt

        skyblockXp += totalTiers * xpPerTier

        val milestones = (totalTiers / 100)
        skyblockXp += milestones * xpPerMilestone

        return min(skyblockXp, maxXp)
    }

    private fun countTotalLevels(category: Category): Int {
        var levels = 0
        for (mob in category.mobs) {
            levels += mob.mobLevelData.level
        }
        category.subCategories.forEach { levels += countTotalLevels(it) }
        return levels
    }

    /**
     * Checks if a user profile has migrated.
     *
     * @param profileInfo skyblock profile information
     */
    fun hasMigrated(profileInfo: JsonObject): Boolean {
        val bestiaryObject = profileInfo.getAsJsonObject("bestiary") ?: return false

        return (bestiaryObject.get("migration") ?: return false).asBoolean
    }

    /**
     * Parse the bestiary data for the profile. Categories are taken from the `constants/bestiary.json` repo file
     *
     * @param profileInfo the JsonObject containing the bestiary data
     */
    @JvmStatic
    fun parseBestiaryData(profileInfo: JsonObject): MutableList<Category> {
        if (!hasMigrated(profileInfo) || Constants.BESTIARY == null) {
            return mutableListOf()
        }

        val parsedCategories = mutableListOf<Category>()

        val apiKills = profileInfo.getAsJsonObject("bestiary")!!.getAsJsonObject("kills") ?: return mutableListOf()
        val apiDeaths = profileInfo.getAsJsonObject("bestiary").getAsJsonObject("deaths") ?: return mutableListOf()
        val killsMap: HashMap<String, Int> = HashMap()
        for (entry in apiKills.entrySet()) {
            killsMap[entry.key] = entry.value.asInt
        }
        val deathsMap: HashMap<String, Int> = HashMap()
        for (entry in apiDeaths.entrySet()) {
            deathsMap[entry.key] = entry.value.asInt
        }

        for (categoryId in categoriesToParse) {
            val categoryData = Constants.BESTIARY.getAsJsonObject(categoryId)!!
            parsedCategories.add(parseCategory(categoryData, categoryId, killsMap, deathsMap))
        }

        return parsedCategories
    }

    /**
     * Parse one individual category, including potential subcategories
     */
    private fun parseCategory(
        categoryData: JsonObject,
        categoryId: String,
        killsMap: HashMap<String, Int>,
        deathsMap: HashMap<String, Int>
    ): Category {
        val categoryName = categoryData["name"].asString
        val computedMobs: MutableList<Mob> = mutableListOf()
        val categoryIconData = categoryData["icon"].asJsonObject

        val categoryIcon = if (categoryIconData.has("skullOwner")) {
            Utils.createSkull(
                categoryName, categoryIconData["skullOwner"].asString, categoryIconData["texture"].asString
            )
        } else {
            ItemUtils.createItemStackFromId(categoryIconData["item"].asString, categoryName)
        }
        if (categoryData.has("hasSubcategories")) { // It must have some subcategories
            val subCategories: MutableList<Category> = mutableListOf()

            val reserved = listOf("name", "icon", "hasSubcategories")
            for (entry in categoryData.entrySet()) {
                if (!reserved.contains(entry.key)) {
                    subCategories.add(
                        parseCategory(
                            entry.value.asJsonObject,
                            "${categoryId}_${entry.key}",
                            killsMap,
                            deathsMap
                        )
                    )
                }
            }
            return Category(categoryId, categoryName, categoryIcon, emptyList(), subCategories)
        } else {
            val categoryMobs = categoryData["mobs"].asJsonArray.map { it.asJsonObject }

            for (mobData in categoryMobs) {
                val mobName = mobData["name"].asString
                val mobIcon = if (mobData.has("skullOwner")) {
                    Utils.createSkull(
                        mobName, mobData["skullOwner"].asString, mobData["texture"].asString
                    )
                } else {
                    ItemUtils.createItemStackFromId(mobData["item"].asString, mobName)
                }

                val cap = mobData["cap"].asDouble
                val bracket = mobData["bracket"].asInt

                var kills = 0.0
                var deaths = 0.0

                // The mobs array contains the individual names returned by the API
                val mobsArray = mobData["mobs"].asJsonArray.map { it.asString }
                for (s in mobsArray) {
                    kills += killsMap.getOrDefault(s, 0)
                    deaths += deathsMap.getOrDefault(s, 0)
                }

                val levelData = calculateLevel(bracket, kills, cap)
                computedMobs.add(Mob(mobName, mobIcon, kills, deaths, levelData))
            }
            return Category(categoryId, categoryName, categoryIcon, computedMobs, emptyList())
        }
    }

    /**
     * Calculates the level for a given mob
     *
     * @param bracket applicable bracket number
     * @param kills number of kills the player has on that mob type
     * @param cap maximum kill limit for the mob
     */
    private fun calculateLevel(bracket: Int, kills: Double, cap: Double): MobLevelData {
        val bracketData =
            Constants.BESTIARY["brackets"].asJsonObject[bracket.toString()].asJsonArray.map { it.asDouble }
        var maxLevel = false

        val effectiveKills = if (kills >= cap) {
            maxLevel = true
            cap
        } else {
            kills
        }

        var level = 0
        for (requiredKills in bracketData) {
            if (effectiveKills >= requiredKills) {
                level++
            } else {
                break
            }
        }
        return MobLevelData(level, maxLevel)
    }
}
