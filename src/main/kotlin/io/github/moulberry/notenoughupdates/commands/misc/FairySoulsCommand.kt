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
import io.github.moulberry.notenoughupdates.util.brigadier.literal
import io.github.moulberry.notenoughupdates.util.brigadier.reply
import io.github.moulberry.notenoughupdates.util.brigadier.thenLiteral
import io.github.moulberry.notenoughupdates.util.brigadier.thenLiteralExecute
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
            }
            thenLiteral("on") { redirect(enable) }
            val disable = thenLiteralExecute("disable") {
                FairySouls.getInstance().setShowFairySouls(false)
                reply("${DARK_PURPLE}Disabled fairy soul waypoints")
            }
            thenLiteral("off") { redirect(disable) }
            val clear = thenLiteralExecute("clear") {
                FairySouls.getInstance().markAllAsFound()
                // Reply handled by mark all as found
            }
            thenLiteral("markfound") { redirect(clear) }
            val unclear = thenLiteralExecute("unclear") {
                FairySouls.getInstance().markAllAsMissing()
                // Reply handled by mark all as missing
            }
            thenLiteral("marknotfound") { redirect(unclear) }
        }
    }
}
