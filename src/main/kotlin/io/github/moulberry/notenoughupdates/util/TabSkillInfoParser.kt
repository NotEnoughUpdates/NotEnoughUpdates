/*
 * Copyright (C) 2023-2024 NotEnoughUpdates contributors
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
import io.github.moulberry.notenoughupdates.NotEnoughUpdates
import io.github.moulberry.notenoughupdates.miscfeatures.tablisttutorial.TablistAPI
import net.minecraft.util.EnumChatFormatting
import java.util.regex.Pattern
import kotlin.math.abs

object TabSkillInfoParser {
    private val skillTabPattern: Pattern =
        Pattern.compile("^§r (?<type>\\w+) (?<level>\\d+): §r§a(?<progress>.+)%§r\$")
    private val maxSkillTabPattern: Pattern =
        Pattern.compile("^§r (?<type>\\w+) (?<level>\\d+): §r§c§lMAX§r\$")
    private var sentErrorOnce = false

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

    private fun sendError(message: String) {
        if (!sentErrorOnce) {
            Utils.addChatMessage(message)
            sentErrorOnce = true
        }
    }

    private fun levelArray(skillType: String) =
        if (skillType == "runecrafting") Utils.getElement(Constants.LEVELING, "runecrafting_xp").asJsonArray
        else Utils.getElement(Constants.LEVELING, "leveling_xp").asJsonArray

    @JvmStatic
    fun parseSkillInfo() {
        if (!NotEnoughUpdates.INSTANCE.hasSkyblockScoreboard()) {
            return
        }

        if (Constants.LEVELING == null) {
            sendError("${EnumChatFormatting.RED}[NEU] There is an error with your repo, please report this in the discord at ${EnumChatFormatting.AQUA}discord.gg/moulberry")
            return
        }

        for (s in TablistAPI.getOptionalWidgetLines(
            TablistAPI.WidgetNames.SKILLS
        )) {
            val matcher = skillTabPattern.matcher(s)
            val maxLevelMatcher = maxSkillTabPattern.matcher(s)
            if (matcher.matches()) {
                // All the groups are guaranteed to match
                val name = matcher.group("type")!!.lowercase()
                val level = matcher.group("level")!!.toInt()
                val progress = matcher.group("progress")!!.toFloatOrNull()
                if (progress == null) {
                    sendError("${EnumChatFormatting.RED}[NEU] Error while parsing skill level from tab list")
                    return
                }
                val levelingArray = levelArray(name)
                val levelXp = calculateLevelXp(levelingArray, level - 1)
                // This *should* not cause problems, since skills that are max Level won't be picked up
                val nextLevelDiff = levelingArray[level].asDouble
                val nextLevelProgress = nextLevelDiff * progress / 100

                val totalXp = levelXp + nextLevelProgress
                val existingLevel = XPInformation.getInstance().getSkillInfo(name) ?: XPInformation.SkillInfo()

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
            } else if (maxLevelMatcher.matches()) {
                val name = maxLevelMatcher.group("type")!!.lowercase()
                val level = maxLevelMatcher.group("level")!!.toInt()

                val existingLevel = XPInformation.getInstance().getSkillInfo(name) ?: XPInformation.SkillInfo()
                if (existingLevel.level != level) {
                    existingLevel.level = level
                    val levelingArray = levelArray(name)

                    val totalXp = calculateLevelXp(levelingArray, level - 1)
                    existingLevel.totalXp = totalXp.toFloat()
                    XPInformation.getInstance().skillInfoMap[name] = existingLevel
                }
            }
        }
    }
}
