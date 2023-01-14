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

package io.github.moulberry.notenoughupdates.util

import com.google.gson.Gson
import com.google.gson.JsonObject
import io.github.moulberry.notenoughupdates.miscfeatures.PetInfoOverlay
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class PetLevelingTest {

    val testJsonString = """
        {
          "pet_rarity_offset": {
            "COMMON": 0,
            "UNCOMMON": 6,
            "RARE": 11,
            "EPIC": 16,
            "LEGENDARY": 20,
            "MYTHIC": 20
          },
          "pet_levels": [
            100,
            110,
            120,
            130,
            145,
            160,
            175,
            190,
            210,
            230,
            250,
            275,
            300,
            330,
            360,
            400,
            440,
            490,
            540,
            600,
            660,
            730,
            800,
            880,
            960,
            1050,
            1150,
            1260,
            1380,
            1510,
            1650,
            1800,
            1960,
            2130,
            2310,
            2500,
            2700,
            2920,
            3160,
            3420,
            3700,
            4000,
            4350,
            4750,
            5200,
            5700,
            6300,
            7000,
            7800,
            8700,
            9700,
            10800,
            12000,
            13300,
            14700,
            16200,
            17800,
            19500,
            21300,
            23200,
            25200,
            27400,
            29800,
            32400,
            35200,
            38200,
            41400,
            44800,
            48400,
            52200,
            56200,
            60400,
            64800,
            69400,
            74200,
            79200,
            84700,
            90700,
            97200,
            104200,
            111700,
            119700,
            128200,
            137200,
            146700,
            156700,
            167700,
            179700,
            192700,
            206700,
            221700,
            237700,
            254700,
            272700,
            291700,
            311700,
            333700,
            357700,
            383700,
            411700,
            441700,
            476700,
            516700,
            561700,
            611700,
            666700,
            726700,
            791700,
            861700,
            936700,
            1016700,
            1101700,
            1191700,
            1286700,
            1386700,
            1496700,
            1616700,
            1746700,
            1886700
          ],
          "custom_pet_leveling": {
            "GOLDEN_DRAGON": {
              "type": 1,
              "pet_levels": [
                0,
                5555,
                1886700,
                1886700,
                1886700,
                1886700,
                1886700,
                1886700,
                1886700,
                1886700,
                1886700,
                1886700,
                1886700,
                1886700,
                1886700,
                1886700,
                1886700,
                1886700,
                1886700,
                1886700,
                1886700,
                1886700,
                1886700,
                1886700,
                1886700,
                1886700,
                1886700,
                1886700,
                1886700,
                1886700,
                1886700,
                1886700,
                1886700,
                1886700,
                1886700,
                1886700,
                1886700,
                1886700,
                1886700,
                1886700,
                1886700,
                1886700,
                1886700,
                1886700,
                1886700,
                1886700,
                1886700,
                1886700,
                1886700,
                1886700,
                1886700,
                1886700,
                1886700,
                1886700,
                1886700,
                1886700,
                1886700,
                1886700,
                1886700,
                1886700,
                1886700,
                1886700,
                1886700,
                1886700,
                1886700,
                1886700,
                1886700,
                1886700,
                1886700,
                1886700,
                1886700,
                1886700,
                1886700,
                1886700,
                1886700,
                1886700,
                1886700,
                1886700,
                1886700,
                1886700,
                1886700,
                1886700,
                1886700,
                1886700,
                1886700,
                1886700,
                1886700,
                1886700,
                1886700,
                1886700,
                1886700,
                1886700,
                1886700,
                1886700,
                1886700,
                1886700,
                1886700,
                1886700,
                1886700,
                1886700
              ],
              "max_level": 200
            },
            "BINGO": {
                "rarity_offset": {
                    "COMMON": 0,
                    "UNCOMMON": 0,
                    "RARE": 0,
                    "EPIC": 0,
                    "LEGENDARY": 0,
                    "MYTHIC": 0
                }
            }
          },
          "pet_types": {
            "ROCK": "MINING",
            "BAT": "MINING",
            "MITHRIL_GOLEM": "MINING",
            "WITHER_SKELETON": "MINING",
            "SILVERFISH": "MINING",
            "ENDERMITE": "MINING",
            "BEE": "FARMING",
            "CHICKEN": "FARMING",
            "PIG": "FARMING",
            "RABBIT": "FARMING",
            "ELEPHANT": "FARMING",
            "BLUE_WHALE": "FISHING",
            "DOLPHIN": "FISHING",
            "FLYING_FISH": "FISHING",
            "BABY_YETI": "FISHING",
            "MEGALODON": "FISHING",
            "SQUID": "FISHING",
            "JELLYFISH": "ALCHEMY",
            "SHEEP": "ALCHEMY",
            "PARROT": "ALCHEMY",
            "MONKEY": "FORAGING",
            "GIRAFFE": "FORAGING",
            "LION": "FORAGING",
            "OCELOT": "FORAGING",
            "BLACK_CAT": "COMBAT",
            "BLAZE": "COMBAT",
            "ENDER_DRAGON": "COMBAT",
            "ENDERMAN": "COMBAT",
            "GHOUL": "COMBAT",
            "GOLEM": "COMBAT",
            "GRIFFIN": "COMBAT",
            "HORSE": "COMBAT",
            "HOUND": "COMBAT",
            "JERRY": "COMBAT",
            "MAGMA_CUBE": "COMBAT",
            "PHOENIX": "COMBAT",
            "PIGMAN": "COMBAT",
            "SKELETON": "COMBAT",
            "SKELETON_HORSE": "COMBAT",
            "SNOWMAN": "COMBAT",
            "SPIDER": "COMBAT",
            "SPIRIT": "COMBAT",
            "TARANTULA": "COMBAT",
            "TURTLE": "COMBAT",
            "TIGER": "COMBAT",
            "ZOMBIE": "COMBAT",
            "WOLF": "COMBAT",
            "GUARDIAN": "ENCHANTING",
            "GRANDMA_WOLF": "COMBAT",
            "ARMADILLO": "MINING",
            "BAL": "COMBAT",
            "GOLDEN_DRAGON": "COMBAT",
            "RAT": "COMBAT",
            "SCATHA": "MINING",
            "AMMONITE": "FISHING",
            "SNAIL": "MINING",
            "MOOSHROOM_COW": "FARMING",
            "KUUDRA": "COMBAT"
          }
        }
    """

    val testJson = Gson().fromJson(testJsonString, JsonObject::class.java)

    @BeforeEach
    fun setup() {
        PetLeveling.petConstants = testJson
    }


    @Test
    fun testMaxedLevel200Pet() {
        val leveling = PetLeveling.getPetLevelingForPet0("GOLDEN_DRAGON", PetInfoOverlay.Rarity.LEGENDARY)
        Assertions.assertEquals(200, leveling.cumulativeLevelCost.size)
        val level = leveling.getPetLevel(219451664.0)
        Assertions.assertEquals(200, level.maxLevel)
        Assertions.assertEquals(200, level.currentLevel)
    }

    @Test
    fun testNonLegendaryMaxLevelPet() {
        val leveling = PetLeveling.getPetLevelingForPet0("GUARDIAN", PetInfoOverlay.Rarity.EPIC)
        Assertions.assertEquals(100, leveling.cumulativeLevelCost.size)
        val level = leveling.getPetLevel(67790664.0)
        Assertions.assertEquals(100, level.currentLevel)
        Assertions.assertEquals(100, level.maxLevel)
    }

    @Test
    fun testBingoPetsLevelLikeCommon() {
        val levelingC = PetLeveling.getPetLevelingForPet0("BINGO", PetInfoOverlay.Rarity.COMMON)
        val levelingE = PetLeveling.getPetLevelingForPet0("BINGO", PetInfoOverlay.Rarity.EPIC)
        Assertions.assertEquals(levelingC.getPetLevel(67790664.0), levelingE.getPetLevel(67790664.0))
    }


    @Test
    fun testPetLevelGrandmaWolf() {
        val leveling = PetLeveling.getPetLevelingForPet0("GRANDMA_WOLF", PetInfoOverlay.Rarity.LEGENDARY)
        val level = leveling.getPetLevel(22_500_000.0)
        Assertions.assertEquals(98, level.currentLevel)
        Assertions.assertEquals(1746700, level.expRequiredForNextLevel)
        Assertions.assertEquals(780170.0F, level.expInCurrentLevel, 0.5F)
    }

    @Test
    fun testPetLevelCommon() {
        val leveling = PetLeveling.getPetLevelingForPet0("BEE", PetInfoOverlay.Rarity.COMMON)
        val level = leveling.getPetLevel(197806.0385900295)
        Assertions.assertEquals(58, level.currentLevel)
        Assertions.assertEquals(19500, level.expRequiredForNextLevel)
        Assertions.assertEquals(5321.0F, level.expInCurrentLevel, 0.5F)
    }

    @Test
    fun testGoldenDragon() {
        val leveling = PetLeveling.getPetLevelingForPet0("GOLDEN_DRAGON", PetInfoOverlay.Rarity.LEGENDARY)
        val level = leveling.getPetLevel(95179565.04472463)
        Assertions.assertEquals(139, level.currentLevel)
        Assertions.assertEquals(1886700, level.expRequiredForNextLevel)
        Assertions.assertEquals(12828.0F, level.expInCurrentLevel, 0.5F)
    }

    @Test
    fun testEpicRabbit() {
        val leveling = PetLeveling.getPetLevelingForPet0("RABBIT", PetInfoOverlay.Rarity.EPIC)
        val level = leveling.getPetLevel(2864013.5452096704)
        Assertions.assertEquals(74, level.currentLevel)
        Assertions.assertEquals(206700, level.expRequiredForNextLevel)
        Assertions.assertEquals(114713.0F, level.expInCurrentLevel, 0.5F)
    }

    @Test
    fun testPetLevelEndermite() {
        val leveling = PetLeveling.getPetLevelingForPet0("ENDERMITE", PetInfoOverlay.Rarity.LEGENDARY)
        val level = leveling.getPetLevel(1_206_848.0)
        Assertions.assertEquals(59, level.currentLevel)
        Assertions.assertEquals(97200, level.expRequiredForNextLevel)
        Assertions.assertEquals(1318.0F, level.expInCurrentLevel, 0.5F)
    }
    @Test
    fun testLevel109GoldenDragon() {
        val leveling = PetLeveling.getPetLevelingForPet0("GOLDEN_DRAGON", PetInfoOverlay.Rarity.LEGENDARY)
        val level = leveling.getPetLevel(3.947913361545298E7)
        Assertions.assertEquals(109, level.currentLevel)
        Assertions.assertEquals(913448.0F, level.expInCurrentLevel, 0.5F)
    }

    @Test
    fun testExpRequiredForLevel140() {
        val leveling = PetLeveling.getPetLevelingForPet0("GOLDEN_DRAGON", PetInfoOverlay.Rarity.LEGENDARY)
        val petExpForLevel = leveling.getPetExpForLevel(140)
        val level = leveling.getPetLevel(petExpForLevel.toDouble())
        Assertions.assertEquals(97053440, petExpForLevel)
        Assertions.assertEquals(140, level.currentLevel)
        Assertions.assertEquals(0.0F, level.expInCurrentLevel, 1.0F)
        Assertions.assertEquals(0.0F, level.percentageToNextLevel, 1.0F)
    }

}
