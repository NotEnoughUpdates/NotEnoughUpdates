/*
 * Copyright (C) 2022 Linnea Gräf
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

package io.github.moulberry.notenoughupdates.util

import com.google.gson.JsonObject
import io.github.moulberry.notenoughupdates.events.RepositoryReloadEvent
import io.github.moulberry.notenoughupdates.miscfeatures.PetInfoOverlay.Rarity
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import io.github.moulberry.notenoughupdates.autosubscribe.NEUAutoSubscribe

@NEUAutoSubscribe
object PetLeveling {

    data class ExpLadder(
        val individualLevelCost: List<Long>,
    ) {
        val cumulativeLevelCost = individualLevelCost.runningFold(0F) { a, b -> a + b }.map { it.toLong() }
        fun getPetLevel(currentExp: Double): PetLevel {
            val currentOneIndexedLevel = cumulativeLevelCost.indexOfLast { it <= currentExp } + 1
            val expForNextLevel = if (currentOneIndexedLevel > individualLevelCost.size) { // Max leveled pet
                individualLevelCost.last()
            } else {
                individualLevelCost[currentOneIndexedLevel - 1]
            }
            val expInCurrentLevel =
                if (currentOneIndexedLevel >= cumulativeLevelCost.size)
                    currentExp.toFloat() - cumulativeLevelCost.last()
                else
                    (expForNextLevel - (cumulativeLevelCost[currentOneIndexedLevel] - currentExp.toFloat())).coerceAtLeast(0F)
            return PetLevel(
                currentLevel = currentOneIndexedLevel,
                maxLevel = cumulativeLevelCost.size,
                expRequiredForNextLevel = expForNextLevel,
                expRequiredForMaxLevel = cumulativeLevelCost.last(),
                expInCurrentLevel = expInCurrentLevel,
                expTotal = currentExp.toFloat()
            )
        }

        fun getPetExpForLevel(level: Int): Long {
            if (level < 2) return 0L
            if (level >= cumulativeLevelCost.size) return cumulativeLevelCost.last()
            return cumulativeLevelCost[level - 1]
        }
    }

    data class PetLevel(
        val currentLevel: Int,
        val maxLevel: Int,
        val expRequiredForNextLevel: Long,
        val expRequiredForMaxLevel: Long,
        val expInCurrentLevel: Float,
        var expTotal: Float,
    ) {
        val percentageToNextLevel: Float = expInCurrentLevel / expRequiredForNextLevel
    }

    private data class Key(val petIdWithoutRarity: String, val rarity: Rarity)

    private val cache = mutableMapOf<Key, ExpLadder>()

    @SubscribeEvent
    fun onRepoReload(event: RepositoryReloadEvent) {
        cache.clear()
    }

    var petConstants: JsonObject? = null

    @JvmStatic
    fun getPetLevelingForPet(petIdWithoutRarity: String, rarity: Rarity): ExpLadder {
        return cache.computeIfAbsent(Key(petIdWithoutRarity, rarity)) {
            getPetLevelingForPet0(
                petIdWithoutRarity,
                rarity
            )
        }
    }

    val stubExpLadder by lazy {
        Utils.showOutdatedRepoNotification()
        ExpLadder(listOf(1, 1))
    }

    internal fun getPetLevelingForPet0(petIdWithoutRarity: String, rarity: Rarity): ExpLadder {
        val petConstants = this.petConstants ?: Constants.PETS ?: return stubExpLadder
        var levels = petConstants["pet_levels"]?.asJsonArray?.map { it.asLong }?.toMutableList() ?: return stubExpLadder
        val customLeveling = petConstants["custom_pet_leveling"]?.asJsonObject?.get(petIdWithoutRarity)
        var rarityOffsets = petConstants["pet_rarity_offset"]?.asJsonObject
        var maxLevel = 100
        if (customLeveling is JsonObject) {
            val customLevels by lazy { customLeveling["pet_levels"]?.asJsonArray?.map { it.asLong } }
            when (customLeveling["type"]?.asInt ?: 0) {
                1 -> levels.addAll(customLevels ?: return stubExpLadder)
                2 -> levels = customLevels?.toMutableList() ?: return stubExpLadder
            }
            maxLevel = customLeveling["max_level"]?.asInt ?: maxLevel
            rarityOffsets = customLeveling["rarity_offset"]?.asJsonObject ?: rarityOffsets
        }
        val offset = rarityOffsets?.get(rarity.name)?.asInt ?: return stubExpLadder

        return ExpLadder(levels.drop(offset).take(maxLevel - 1))
    }

}
