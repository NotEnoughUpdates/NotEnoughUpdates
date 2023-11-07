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

package io.github.moulberry.notenoughupdates.miscgui.customtodos

import io.github.moulberry.notenoughupdates.NotEnoughUpdates
import io.github.moulberry.notenoughupdates.autosubscribe.NEUAutoSubscribe
import io.github.moulberry.notenoughupdates.core.util.StringUtils
import io.github.moulberry.notenoughupdates.events.SidebarChangeEvent
import io.github.moulberry.notenoughupdates.events.TabListChangeEvent
import io.github.moulberry.notenoughupdates.util.Utils
import net.minecraft.util.EnumChatFormatting
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@NEUAutoSubscribe
object CustomTodoHud {

    private fun matchString(todo: CustomTodo, text: String): Boolean {
        return when (todo.triggerMatcher) {
            CustomTodo.TriggerMatcher.REGEX -> text.matches(todo.trigger.toRegex())
            CustomTodo.TriggerMatcher.STARTS_WITH -> text.startsWith(todo.trigger)
            CustomTodo.TriggerMatcher.CONTAINS -> text.contains(todo.trigger)
            CustomTodo.TriggerMatcher.EQUALS -> text == todo.trigger
        }
    }

    @SubscribeEvent
    fun onTabList(event: TabListChangeEvent) {
        NotEnoughUpdates.INSTANCE.config.hidden.customTodos
            .forEach { todo ->
                if (todo.triggerTarget != CustomTodo.TriggerTarget.TAB_LIST) return@forEach
                event.newLines.forEach { text ->
                    val doesMatch = matchString(todo, text)
                    if (doesMatch) {
                        todo.setDoneNow()
                    }
                }
            }
    }

    @SubscribeEvent
    fun onSidebar(event: SidebarChangeEvent) {
        NotEnoughUpdates.INSTANCE.config.hidden.customTodos
            .forEach { todo ->
                if (todo.triggerTarget != CustomTodo.TriggerTarget.SIDEBAR) return@forEach
                event.lines.forEach { text ->
                    val doesMatch = matchString(todo, text)
                    if (doesMatch) {
                        todo.setDoneNow()
                    }
                }
            }
    }

    @SubscribeEvent
    fun onChat(event: ClientChatReceivedEvent) {
        val text = StringUtils.cleanColour(event.message.unformattedText)
        NotEnoughUpdates.INSTANCE.config.hidden.customTodos
            .forEach {
                val isCorrectTrigger = when (it.triggerTarget) {
                    CustomTodo.TriggerTarget.CHAT -> event.type != 2.toByte()
                    CustomTodo.TriggerTarget.ACTIONBAR -> event.type == 2.toByte()
                    CustomTodo.TriggerTarget.TAB_LIST -> false
                    CustomTodo.TriggerTarget.SIDEBAR -> false
                }
                val doesMatch = matchString(it, text)
                if (isCorrectTrigger && doesMatch)
                    it.setDoneNow()
            }
    }

    @JvmStatic
    fun processInto(strings: MutableList<String>) {
        NotEnoughUpdates.INSTANCE.config.hidden.customTodos
            .filter { it.isEnabledOnCurrentProfile }
            .forEach {
                val readyAt = it.readyAtOnCurrentProfile ?: (System.currentTimeMillis() - 1000L)
                val until = readyAt - System.currentTimeMillis()
                strings.add(
                    "CUSTOM" + it.icon + ":§3" + it.label + ": " +
                            if (until <= 0)
                                EnumChatFormatting.values()[NotEnoughUpdates.INSTANCE.config.miscOverlays.readyColour].toString() + "Ready"
                            else if (until < 60 * 30 * 1000L)
                                EnumChatFormatting.values()[NotEnoughUpdates.INSTANCE.config.miscOverlays.verySoonColour].toString()
                                        + Utils.prettyTime(until)
                            else if (until < 60 * 60 * 1000L)
                                EnumChatFormatting.values()[NotEnoughUpdates.INSTANCE.config.miscOverlays.soonColour].toString()
                                        + Utils.prettyTime(until)
                            else if (until < 3 * 60 * 60 * 1000L)
                                EnumChatFormatting.values()[NotEnoughUpdates.INSTANCE.config.miscOverlays.kindaSoonColour].toString()
                                        + Utils.prettyTime(until)
                            else
                                EnumChatFormatting.values()[NotEnoughUpdates.INSTANCE.config.miscOverlays.defaultColour].toString()
                                        + Utils.prettyTime(until)
                )
            }
    }

}
