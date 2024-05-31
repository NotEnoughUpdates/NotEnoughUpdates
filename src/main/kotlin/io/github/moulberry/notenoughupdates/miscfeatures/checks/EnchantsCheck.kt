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

package io.github.moulberry.notenoughupdates.miscfeatures.checks

import net.minecraft.event.ClickEvent
import net.minecraft.event.HoverEvent
import net.minecraft.util.ChatComponentText
import net.minecraft.util.EnumChatFormatting.*
import net.minecraft.util.IChatComponent
import net.minecraftforge.fml.common.Loader

class EnchantsCheck {

    private val sbaMsg = "SkyblockAddons is installed and might break NEU enchants!"
    private val shMsg = "SkyHanni is installed and might break NEU enchants!"
    private val dsmMsg = "DSM is installed and might break NEU enchants!"
    private val sbeMsg = "SBE is installed and might break NEU enchants!"

    private val sbaHelp = "${LIGHT_PURPLE}/sba${YELLOW} -> ${LIGHT_PURPLE}Parse Enchant Tooltips${YELLOW} -> " +
            "${LIGHT_PURPLE}Second Page${YELLOW} -> ${LIGHT_PURPLE}Highlight Special Enchantments${YELLOW} -> ${RED}${BOLD}DISABLE"
    private val shHelp = "${LIGHT_PURPLE}/sh enchant parsing${YELLOW} -> ${LIGHT_PURPLE}Inventory${YELLOW} -> " +
            "${LIGHT_PURPLE}Enchant Parsing${YELLOW} -> ${RED}${BOLD}DISABLE"
    private val dsmHelp = "${LIGHT_PURPLE}/dsm${YELLOW} -> ${LIGHT_PURPLE}General${YELLOW} -> " +
            "${LIGHT_PURPLE}Golden ... Enchantments${YELLOW} -> ${RED}${BOLD}DISABLE"
    private val sbeHelp =
        "${LIGHT_PURPLE}/sbe${YELLOW} -> ${LIGHT_PURPLE}Color Enchants${YELLOW} -> ${RED}${BOLD}DISABLE"

    fun getMessages(): List<IChatComponent> {
        val messages = mutableListOf<IChatComponent>()
        if (Loader.isModLoaded("skyblockaddons")) messages.add(modMessage(sbaMsg, sbaHelp, "/sba"))
        if (Loader.isModLoaded("skyhanni")) messages.add(modMessage(shMsg, shHelp, "/sh enchant parsing"))
        if (Loader.isModLoaded("Danker's Skyblock Mod")) messages.add(modMessage(dsmMsg, dsmHelp, "/dsm"))
        if (Loader.isModLoaded("SkyblockExtras")) messages.add(modMessage(sbeMsg, sbeHelp, "/sbe"))
        if (messages.isNotEmpty()) {
            messages.addAll(
                listOf(
                    ChatComponentText(""),
                    ChatComponentText("${YELLOW}One or more mods conflicting with /neuec found!"),
                    ChatComponentText("${LIGHT_PURPLE}Hover${YELLOW} over the above messages to check the solutions."),
                    ChatComponentText("${LIGHT_PURPLE}Click${YELLOW} on the above messages to run the command mentioned.")
                )
            )
        } else {
            val discordHover = HoverEvent(HoverEvent.Action.SHOW_TEXT, ChatComponentText("${BLUE}discord.gg/moulberry"))
            val discordClick = ClickEvent(ClickEvent.Action.OPEN_URL, "https://discord.gg/moulberry")
            messages.addAll(
                listOf(
                    ChatComponentText("${GREEN}Your enchant colors should not be overriden by any mods!"),
                    ChatComponentText("${YELLOW}If any of your mods override them, please let us know on ${BLUE}NEU Discord")
                        .also { it.chatStyle.setChatHoverEvent(discordHover).setChatClickEvent(discordClick) })
            )
        }
        return messages
    }

    private fun modMessage(msg: String, help: String, cmd: String): IChatComponent {
        val mainComponent = ChatComponentText("$RED$msg")
        val hoverComponent = ChatComponentText(help)

        val hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverComponent)
        val clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, cmd)
        mainComponent.chatStyle.setChatHoverEvent(hoverEvent).setChatClickEvent(clickEvent)

        return mainComponent
    }
}
