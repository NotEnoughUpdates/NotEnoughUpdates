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

package io.github.moulberry.notenoughupdates.util

import com.google.gson.JsonArray
import net.minecraft.util.EnumChatFormatting
import java.util.regex.Pattern
import kotlin.math.abs

object TabSkillInfoParser {
    private val skillTabPattern: Pattern =
        Pattern.compile("^§r§e§lSkills: §r§a(?<type>\\w+) (?<level>\\d+): §r§3(?<progress>.+)%§r\$")

    private fun calculateLevelXp(levelingArray: JsonArray, level: Int): Double {
        var totalXp = 0.0
        for (i in 0 until level + 1) {
            val xp = levelingArray[i].asDouble
            totalXp += xp
        }
        return totalXp
    }

    private fun isWithinPercentageRange(xp: Double, existingXp: Double, percentage: Double): Boolean {
        val diff = (abs(xp - existingXp) / existingXp) * 100
        return diff <= percentage
    }

    @JvmStatic
    fun parseSkillInfo() {
        for (s in TabListUtils.getTabList()) {
            val matcher = skillTabPattern.matcher(s)
            if (matcher.matches()) {
                // All the groups are guaranteed to match
                val name = matcher.group("type")!!.lowercase()
                val level = matcher.group("level")!!.toInt()
                val progress = matcher.group("progress")!!.toFloatOrNull()
                if (progress == null) {
                    Utils.addChatMessage("${EnumChatFormatting.RED}[NEU] Error while parsing skill level from tab list")
                    return
                }
                val runecrafting = name == "runecrafting"
                val levelingArray =
                    if (runecrafting) Utils.getElement(Constants.LEVELING, "runecrafting_xp").asJsonArray
                    else Utils.getElement(Constants.LEVELING, "leveling_xp").asJsonArray

                val levelXp = calculateLevelXp(levelingArray, level - 1)
                // This *should* not cause problems, since skills that are max Level won't be picked up
                val nextLevelDiff = levelingArray[level].asDouble
                val nextLevelProgress = nextLevelDiff * progress / 100

                val totalXp = levelXp + nextLevelProgress
                val existingLevel = XPInformation.getInstance().getSkillInfo(name)

                // Only update if the numbers are substantially different
                if (!isWithinPercentageRange(totalXp, existingLevel.totalXp.toDouble(), 1.0)) {
                    existingLevel.level = level
                    existingLevel.totalXp = totalXp.toFloat()
                    existingLevel.currentXp = nextLevelProgress.toFloat()
                    existingLevel.currentXpMax = nextLevelDiff.toFloat()
                    XPInformation.getInstance().skillInfoMap[name] = existingLevel
                }

                // There is only one skill at a time in the tab list
                break
            }
        }
    }
}
