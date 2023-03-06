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
import io.github.moulberry.notenoughupdates.core.GuiScreenElementWrapper
import io.github.moulberry.notenoughupdates.core.config.struct.ConfigProcessor
import io.github.moulberry.notenoughupdates.events.RegisterBrigadierCommandEvent
import io.github.moulberry.notenoughupdates.options.NEUConfigEditor
import io.github.moulberry.notenoughupdates.util.brigadier.*
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@NEUAutoSubscribe
class SettingsCommand {
    @SubscribeEvent
    fun onCommands(event: RegisterBrigadierCommandEvent) {
        event.command("neu", "neusettings") {
            thenArgument("search", RestArgumentType) { search ->
                suggestsList(ConfigProcessor.create(NotEnoughUpdates.INSTANCE.config).keys.toList())
                thenExecute {
                    NotEnoughUpdates.INSTANCE.openGui = GuiScreenElementWrapper(
                        NEUConfigEditor(
                            NotEnoughUpdates.INSTANCE.config,
                            this[search]
                        )
                    )
                }
            }.withHelp("Search the NEU settings")
            thenExecute {
                NotEnoughUpdates.INSTANCE.openGui =
                    GuiScreenElementWrapper(NEUConfigEditor(NotEnoughUpdates.INSTANCE.config))
            }
        }.withHelp("Open the NEU settings")
    }
}
