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

import com.mojang.realmsclient.gui.ChatFormatting
import io.github.moulberry.notenoughupdates.NotEnoughUpdates
import io.github.moulberry.notenoughupdates.autosubscribe.NEUAutoSubscribe
import io.github.moulberry.notenoughupdates.events.RegisterBrigadierCommandEvent
import io.github.moulberry.notenoughupdates.miscfeatures.FairySouls
import io.github.moulberry.notenoughupdates.util.Calculator
import io.github.moulberry.notenoughupdates.util.brigadier.RestArgumentType
import io.github.moulberry.notenoughupdates.util.brigadier.get
import io.github.moulberry.notenoughupdates.util.brigadier.thenArgumentExecute
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import io.github.moulberry.notenoughupdates.util.brigadier.*
import net.minecraft.util.EnumChatFormatting.AQUA
import net.minecraft.util.EnumChatFormatting.WHITE
import net.minecraft.util.EnumChatFormatting
import net.minecraft.util.EnumChatFormatting.DARK_PURPLE
import net.minecraft.util.EnumChatFormatting.RED
import java.text.DecimalFormat


@NEUAutoSubscribe
class QOLCommands {
    @SubscribeEvent
    fun onCommands(event: RegisterBrigadierCommandEvent) {
        event.command("afk", ) {
            val enable = thenLiteralExecute("on") {
                reply("${DARK_PURPLE}Enabled anti-afk")
                NotEnoughUpdates.INSTANCE.config.fishing.antiAFK=true
            }
            thenLiteral("on") { redirect(enable) }
            val disable = thenLiteralExecute("off") {
                NotEnoughUpdates.INSTANCE.config.fishing.antiAFK=false
                reply("${DARK_PURPLE}Disabled anti-afk")
            }
            thenLiteral("off") { redirect(disable) }
        }
        event.command("chums", "whitelist") {
            thenExecute() {

                reply("$AQUA Whitelist $WHITE: "+NotEnoughUpdates.INSTANCE.config.fishing.whitelist.replace(',',' ') )
            }
        }

        event.command("addchum", "addwhitelist") {
            thenArgumentExecute("name", RestArgumentType) { name ->
                val name = this[name]
                NotEnoughUpdates.INSTANCE.config.fishing.whitelist += "$name,"
                reply("Added $name to whitelist")
            }
        }.withHelp("/addchum <playername>")

        event.command("removechum", "removewhitelist") {
            thenArgumentExecute("name", RestArgumentType) { name ->
                val name = this[name]
                if(NotEnoughUpdates.INSTANCE.config.fishing.whitelist.contains("$name")) {
                    NotEnoughUpdates.INSTANCE.config.fishing.whitelist = NotEnoughUpdates.INSTANCE.config.fishing.whitelist.replace("$name,", "")
                    NotEnoughUpdates.INSTANCE.config.fishing.whitelist = NotEnoughUpdates.INSTANCE.config.fishing.whitelist.replace("$name", "")
                    reply("Removed $name from whitelist")
                }else{
                    reply("$name is not whitelisted")
                }
            }
        }.withHelp("/removechum <playername>")


    }
}
