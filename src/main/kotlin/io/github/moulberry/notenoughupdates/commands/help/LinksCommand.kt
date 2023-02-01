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

package io.github.moulberry.notenoughupdates.commands.help

import io.github.moulberry.notenoughupdates.NotEnoughUpdates
import io.github.moulberry.notenoughupdates.autosubscribe.NEUAutoSubscribe
import io.github.moulberry.notenoughupdates.events.RegisterBrigadierCommandEvent
import io.github.moulberry.notenoughupdates.util.Utils
import io.github.moulberry.notenoughupdates.util.brigadier.reply
import io.github.moulberry.notenoughupdates.util.brigadier.thenExecute
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@NEUAutoSubscribe
class LinksCommand {
    @SubscribeEvent
    fun onCommands(event: RegisterBrigadierCommandEvent) {
        event.command("neulinks") {
            thenExecute {
                val manager = NotEnoughUpdates.INSTANCE.manager
                val updateJsonFile = manager.repoLocation.resolve("update.json")
                if (!updateJsonFile.exists()) {
                    Utils.showOutdatedRepoNotification()
                    return@thenExecute
                }
                try {
                    val updateJson = manager.getJsonFromFile(updateJsonFile)
                    reply("")
                    NotEnoughUpdates.INSTANCE.displayLinks(updateJson, 0)
                    reply("")
                } catch (_: Exception) {
                    Utils.showOutdatedRepoNotification()
                }
            }
        }
    }
}
