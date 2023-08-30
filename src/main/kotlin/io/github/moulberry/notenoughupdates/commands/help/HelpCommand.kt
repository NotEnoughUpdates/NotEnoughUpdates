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

import com.mojang.brigadier.arguments.StringArgumentType.string
import io.github.moulberry.notenoughupdates.NotEnoughUpdates
import io.github.moulberry.notenoughupdates.autosubscribe.NEUAutoSubscribe
import io.github.moulberry.notenoughupdates.events.RegisterBrigadierCommandEvent
import io.github.moulberry.notenoughupdates.util.brigadier.*
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@NEUAutoSubscribe
class HelpCommand {
    val neuHelpMessages = listOf(
        "§5§lNotEnoughUpdates commands",
        "§6/neu §7- Opens the main NEU GUI.",
        "§6/pv §b?{name} §r§7- Opens the profile viewer",
        "§6/neusouls {on/off/clear/unclear} §r§7- Shows waypoints to fairy souls.",
        "§6/neubuttons §r§7- Opens a GUI which allows you to customize inventory buttons.",
        "§6/neuec §r§7- Opens the enchant colour GUI.",
        "§6/neucosmetics §r§7- Opens the cosmetic GUI.",
        "§6/neurename §r§7- Opens the NEU Item Customizer.",
        "§6/cata §b?{name} §r§7- Opens the profile viewer's Catacombs page.",
        "§6/neulinks §r§7- Shows links to NEU/Moulberry.",
        "§6/neuoverlay §r§7- Opens GUI Editor for quickcommands and searchbar.",
        "§6/neucalendar §r§7- Opens NEU's custom calendar GUI.",
        "§6/neucalc §r§7- Run calculations.",
        "",
        "§6§lOld commands:",
        "§6/peek §b?{user} §r§7- Shows quick stats for a user.",
        "",
        "§6§lDebug commands:",
        "§6/neustats §r§7- Copies helpful info to the clipboard.",
        "§6/neustats modlist §r§7- Copies mod list info to clipboard.",
        "§6/neuresetrepo §r§7- Deletes all repo files.",
        "§6/neureloadrepo §r§7- Debug command with repo.",
        "",
        "§6§lDev commands:",
        "§6/neupackdev §r§7- pack creator command - getnpc, getmob(s), getarmorstand(s), getall. Optional radius argument for all."
    )
    val neuDevHelpMessages = listOf(
        "§6/neudevtest §r§7- dev test command",
        "§6/neuzeephere §r§7- sphere",
        "§6/neudungeonwintest §r§7- displays the dungeon win screen"
    )
    val helpInfo = listOf(
        "",
        "§7Arguments marked with a §b\"?\"§7 are optional.",
        "",
        "§6§lScroll up to see everything"
    )

    @SubscribeEvent
    fun onCommands(event: RegisterBrigadierCommandEvent) {
        event.command("neuhelp") {
            thenArgumentExecute("command", string()) { commandName ->
                val commandNode = event.dispatcher.root.getChild(this[commandName])
                if (commandNode == null) {
                    reply("Could not find NEU command with name ${this[commandName]}")
                    return@thenArgumentExecute
                }
                reply(event.brigadierRoot.getAllUsages("/${this[commandName]}", commandNode).joinToString("\n"){
                    "${it.path} - ${it.help}"
                })
            }.withHelp("Display help for a specific NEU command")
            thenExecute {
                neuHelpMessages.forEach(::reply)
                if (NotEnoughUpdates.INSTANCE.config.hidden.dev)
                    neuDevHelpMessages.forEach(::reply)
                helpInfo.forEach(::reply)
            }
        }.withHelp("Display a list of all NEU commands")
    }
}
