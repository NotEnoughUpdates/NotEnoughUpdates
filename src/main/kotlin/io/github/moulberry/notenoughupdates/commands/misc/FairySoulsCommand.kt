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

import io.github.moulberry.notenoughupdates.autosubscribe.NEUAutoSubscribe
import io.github.moulberry.notenoughupdates.events.RegisterBrigadierCommandEvent
import io.github.moulberry.notenoughupdates.miscfeatures.FairySouls
import io.github.moulberry.notenoughupdates.util.brigadier.*
import net.minecraft.util.EnumChatFormatting.DARK_PURPLE
import net.minecraft.util.EnumChatFormatting.RED
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@NEUAutoSubscribe
class FairySoulsCommand {

    @SubscribeEvent
    fun onCommands(event: RegisterBrigadierCommandEvent) {
        event.command("neusouls", "fairysouls") {
            val enable = thenLiteralExecute("enable") {
                if (!FairySouls.getInstance().isTrackSouls) {
                    reply("${RED}Fairy soul tracking is off, enable it using /neu before using this command")
                    return@thenLiteralExecute
                }
                reply("${DARK_PURPLE}Enabled fairy soul waypoints")
                FairySouls.getInstance().setShowFairySouls(true)
            }.withHelp("Show fairy soul waypoints")
            thenLiteral("on") { thenRedirect(enable) }
            val disable = thenLiteralExecute("disable") {
                FairySouls.getInstance().setShowFairySouls(false)
                reply("${DARK_PURPLE}Disabled fairy soul waypoints")
            }.withHelp("Hide fairy soul waypoints")
            thenLiteral("off") { thenRedirect(disable) }
            val clear = thenLiteralExecute("clear") {
                FairySouls.getInstance().markAllAsFound()
                // Reply handled by mark all as found
            }.withHelp("Mark all fairy souls in your current world as found")
            thenLiteral("markfound") { thenRedirect(clear) }
            val unclear = thenLiteralExecute("unclear") {
                FairySouls.getInstance().markAllAsMissing()
                // Reply handled by mark all as missing
            }.withHelp("Mark all fairy souls in your current world as not found")
            thenLiteral("marknotfound") { thenRedirect(unclear) }
        }
    }
}
