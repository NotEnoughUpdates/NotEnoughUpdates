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

package io.github.moulberry.notenoughupdates.commands.misc

import com.mojang.brigadier.context.CommandContext
import io.github.moulberry.notenoughupdates.NotEnoughUpdates
import io.github.moulberry.notenoughupdates.autosubscribe.NEUAutoSubscribe
import io.github.moulberry.notenoughupdates.events.RegisterBrigadierCommandEvent
import io.github.moulberry.notenoughupdates.util.brigadier.*
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@NEUAutoSubscribe
class JoinCommand {

    fun removePartialPrefix(text: String, prefix: String): String? {
        var lf: String? = null
        for (i in 1..prefix.length) {
            if (text.startsWith(prefix.substring(0, i))) {
                lf = text.substring(i)
            }
        }
        return lf
    }

    val kuudraLevelNames = listOf("NORMAL", "HOT", "BURNING", "FIERY", "INFERNAL")
    val dungeonLevelNames = listOf("ONE", "TWO", "THREE", "FOUR", "FIVE", "SIX", "SEVEN")

    @SubscribeEvent
    fun onCommands(event: RegisterBrigadierCommandEvent) {
        event.command("join") {
            thenArgumentExecute("what", RestArgumentType) { what ->
                if (!NotEnoughUpdates.INSTANCE.isOnSkyblock) {
                    NotEnoughUpdates.INSTANCE.sendChatMessage("/join ${this[what]}")
                    return@thenArgumentExecute
                }
                val w = this[what].replace(" ", "").lowercase()
                val joinName = getNameFor(w)
                if (joinName == null) {
                    reply("Could not find instance kind for $w")
                } else {
                    reply("Running /joininstance $joinName")
                    NotEnoughUpdates.INSTANCE.sendChatMessage("/joininstance $joinName")
                }
            }
        }
    }

    fun CommandContext<DefaultSource>.getNameFor(w: String): String? {
        val kuudraLevel = removePartialPrefix(w, "kuudratier") ?: removePartialPrefix(w, "tier")
        if (kuudraLevel != null) {
            val l = kuudraLevel.toIntOrNull()?.let { it - 1 } ?: kuudraLevelNames.indexOfFirst {
                it.startsWith(
                    kuudraLevel,
                    true
                )
            }
            if (l !in kuudraLevelNames.indices) {
                reply("Could not find kuudra kind for name $kuudraLevel")
                return null
            }
            return "KUUDRA_${kuudraLevelNames[l]}"
        }
        val masterLevel = removePartialPrefix(w, "master")
        val normalLevel =
            removePartialPrefix(w, "floor") ?: removePartialPrefix(w, "catacombs") ?: removePartialPrefix(w, "dungeons")
        val dungeonLevel = masterLevel ?: normalLevel
        if (dungeonLevel != null) {
            val l = dungeonLevel.toIntOrNull()?.let { it - 1 } ?: dungeonLevelNames.indexOfFirst {
                it.startsWith(
                    dungeonLevel,
                    true
                )
            }
            if (masterLevel == null && (l == -1 || null != removePartialPrefix(w, "entrance"))) {
                return "CATACOMBS_ENTRANCE"
            }
            if (l !in dungeonLevelNames.indices) {
                reply("Could not find dungeon kind for name $dungeonLevel")
                return null
            }
            return "${if (masterLevel != null) "MASTER_" else ""}CATACOMBS_FLOOR_${dungeonLevelNames[l]}"
        }
        return null
    }
}

