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

import com.google.gson.JsonObject
import io.github.moulberry.notenoughupdates.NotEnoughUpdates
import io.github.moulberry.notenoughupdates.core.util.StringUtils
import io.github.moulberry.notenoughupdates.util.Utils
import io.github.moulberry.notenoughupdates.util.getIntOrValue
import io.github.moulberry.notenoughupdates.util.kotlin.Coroutines

object HoppityLeaderboardRank {

    private val manager get() = NotEnoughUpdates.INSTANCE.manager

    private var leaderboardRank = -1
    private var currentRankStatus = HoppityLeaderboardRankStatus.LOADING
    private var currentlyLoading = false

    fun getRank(): String = StringUtils.formatNumber(leaderboardRank)

    fun getRankInfo() = currentRankStatus.getDisplayString()
    fun getAdditionalInfo() = currentRankStatus.getAdditionalInfoString()

    fun resetData() {
        leaderboardRank = -1
        currentRankStatus = HoppityLeaderboardRankStatus.LOADING
        currentlyLoading = false
    }

    fun openWebsite() {
        if (currentRankStatus == HoppityLeaderboardRankStatus.LOADING) return
        Utils.openUrl("https://elitebot.dev/leaderboard/chocolate")
        Utils.playPressSound()
    }

    fun loadData(uuid: String?, profileId: String?) {
        if (uuid == null || profileId == null) {
            processResult(-1, true)
            return
        }
        if (currentlyLoading) return
        currentlyLoading = true
        Coroutines.launchCoroutine {

            manager.apiUtils.request()
                .url("https://api.elitebot.dev/leaderboard/rank/chocolate/$uuid/$profileId")
                .requestJson()
                .whenComplete { json: JsonObject?, error: Throwable? ->
                    if (error != null || json == null) {
                        processResult(-1, true, uuid)
                    } else {
                        val rank = json.getIntOrValue("rank", -1)
                        processResult(rank)
                    }
                }
        }
    }

    private fun processResult(rank: Int, errored: Boolean = false, uuid: String? = null) {
        if (!currentlyLoading) return
        if (errored) {
            currentRankStatus = HoppityLeaderboardRankStatus.ERROR
            if (uuid != null) {
                addToElite(uuid)
            }
        } else if (rank == -1) {
            currentRankStatus = HoppityLeaderboardRankStatus.TOO_LOW
        } else {
            leaderboardRank = rank
            currentRankStatus = HoppityLeaderboardRankStatus.FOUND
        }
        currentlyLoading = false
    }

    private fun addToElite(uuid: String) {
        // errors when player has never been loaded on elitebot before, load their whole profile to add them to it
        manager.apiUtils.request()
            .url("https://api.elitebot.dev/account/$uuid")
            .requestJson()
    }
}

enum class HoppityLeaderboardRankStatus(private val display: () -> String, private val additionalInfo: () -> String) {
    LOADING({ "Loading..." }, { "§eStill loading data." }),
    TOO_LOW({ "Too Low" }, { "§eLeaderboard only has top 5,000 players." }),
    FOUND(
        { HoppityLeaderboardRank.getRank() },
        { "§7#§b${HoppityLeaderboardRank.getRank()} §7on the Elitebot chocolate leaderboard." }
    ),

    ERROR({ "Error" }, { "§cError while fetching leaderboard rank, try again later." }),
    ;

    fun getDisplayString() = display()
    fun getAdditionalInfoString() = additionalInfo()
}
