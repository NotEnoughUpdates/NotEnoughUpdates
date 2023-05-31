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

import net.minecraft.client.Minecraft
import net.minecraft.scoreboard.Score
import net.minecraft.scoreboard.ScorePlayerTeam

object SidebarUtil {
    @JvmStatic
    @JvmOverloads
    fun readSidebarLines(cleanColor: Boolean = true, cleanSpecialCharacters: Boolean = true): List<String> {
        var result = readRawSidebarLines()
        if (cleanColor) result = result.map { Utils.cleanColour(it) }
        if (cleanSpecialCharacters) result.map { cleanTeamName(it) }
        return result
    }

    @JvmStatic
    fun readRawSidebarLines() = fetchScoreboardLines().reversed()

    @JvmStatic
    fun cleanTeamName(scoreboard: String) =
        scoreboard.toCharArray().filter { it.code in 21..126 || it.code == 167 }.joinToString(separator = "")

    private fun fetchScoreboardLines(): List<String> {
        val scoreboard = Minecraft.getMinecraft().theWorld?.scoreboard ?: return emptyList()
        val objective = scoreboard.getObjectiveInDisplaySlot(1) ?: return emptyList()
        var scores = scoreboard.getSortedScores(objective)
        val list = scores.filter { input: Score? ->
            input != null && input.playerName != null && !input.playerName.startsWith("#")
        }
        scores = if (list.size > 15) {
            list.drop(15)
        } else list
        return scores.map {
            ScorePlayerTeam.formatPlayerName(scoreboard.getPlayersTeam(it.playerName), it.playerName)
        }
    }
}
