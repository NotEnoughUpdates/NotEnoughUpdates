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

import io.github.moulberry.notenoughupdates.NotEnoughUpdates
import io.github.moulberry.notenoughupdates.autosubscribe.NEUAutoSubscribe
import io.github.moulberry.notenoughupdates.events.RegisterBrigadierCommandEvent
import io.github.moulberry.notenoughupdates.miscgui.GuiEnchantColour
import io.github.moulberry.notenoughupdates.miscgui.GuiInvButtonEditor
import io.github.moulberry.notenoughupdates.miscgui.NEUOverlayPlacements
import io.github.moulberry.notenoughupdates.util.brigadier.thenExecute
import io.github.moulberry.notenoughupdates.util.brigadier.withHelp
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@NEUAutoSubscribe
class ScreenOpenCommands {
    @SubscribeEvent
    fun onCommands(event: RegisterBrigadierCommandEvent) {
        event.command("neubuttons") {
            thenExecute {
                NotEnoughUpdates.INSTANCE.openGui = GuiInvButtonEditor()
            }
        }.withHelp("Open the NEU inventory button editor")
        event.command("neuec") {
            thenExecute {
                NotEnoughUpdates.INSTANCE.openGui = GuiEnchantColour()
            }
        }.withHelp("Open the NEU custom enchant colour editor")
        event.command("neuoverlay") {
            thenExecute {
                NotEnoughUpdates.INSTANCE.openGui = NEUOverlayPlacements()
            }
        }.withHelp("Open the NEU gui overlay editor")
    }
}
