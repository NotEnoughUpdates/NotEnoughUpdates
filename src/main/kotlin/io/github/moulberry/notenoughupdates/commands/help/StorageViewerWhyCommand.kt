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

import io.github.moulberry.notenoughupdates.autosubscribe.NEUAutoSubscribe
import io.github.moulberry.notenoughupdates.events.RegisterBrigadierCommandEvent
import io.github.moulberry.notenoughupdates.util.NotificationHandler
import io.github.moulberry.notenoughupdates.util.brigadier.thenExecute
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@NEUAutoSubscribe
class StorageViewerWhyCommand {
    @SubscribeEvent
    fun onCommands(event: RegisterBrigadierCommandEvent) {
        event.command("neustwhy") {
            thenExecute {
                NotificationHandler.displayNotification(
                    listOf(
                        "§eStorage Viewer",
                        "§7Currently, the storage viewer requires you to click twice",
                        "§7in order to switch between pages. This is because Hypixel",
                        "§7has not yet added a shortcut command to go to any enderchest/",
                        "§7storage page.",
                        "§7While it is possible to send the second click",
                        "§7automatically, doing so violates Hypixel's new mod rules."
                    ), true
                )
            }
        }
    }
}
