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

import io.github.moulberry.moulconfig.internal.ClipboardUtils
import io.github.moulberry.moulconfig.observer.ObservableList
import io.github.moulberry.moulconfig.xml.Bind
import io.github.moulberry.moulconfig.xml.XMLUniverse
import io.github.moulberry.notenoughupdates.NotEnoughUpdates
import io.github.moulberry.notenoughupdates.autosubscribe.NEUAutoSubscribe
import io.github.moulberry.notenoughupdates.events.RegisterBrigadierCommandEvent
import io.github.moulberry.notenoughupdates.util.brigadier.thenExecute
import io.github.moulberry.notenoughupdates.util.brigadier.withHelp
import io.github.moulberry.notenoughupdates.util.loadResourceLocation
import net.minecraft.client.gui.GuiScreen
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

class CustomTodoList(
    @field:Bind
    val todos: ObservableList<CustomTodoEditor>,
    val xmlUniverse: XMLUniverse,
) {
    @NEUAutoSubscribe
    companion object {
        @SubscribeEvent
        fun onCommand(event: RegisterBrigadierCommandEvent) {
            event.command("neutodos", "neucustomtodos") {
                thenExecute {
                    NotEnoughUpdates.INSTANCE.openGui = create().open()
                }
            }.withHelp("Edit NEUs custom TODOs")
        }

        fun create(): CustomTodoList {
            val universe = XMLUniverse.getDefaultUniverse()
            val list = ObservableList<CustomTodoEditor>(mutableListOf())
            NotEnoughUpdates.INSTANCE.config.hidden.customTodos.forEach {
                list.add(CustomTodoEditor(it, list, universe))
            }
            return CustomTodoList(
                list,
                universe
            )
        }
    }

    fun open(): GuiScreen {
        return xmlUniverse.loadResourceLocation(this, ResourceLocation("notenoughupdates:gui/customtodos/overview.xml"))
    }

    @Bind
    fun pasteTodo() {
        val customTodo = CustomTodo.fromTemplate(ClipboardUtils.getClipboardContent())
            ?: return
        todos.add(CustomTodoEditor(customTodo, todos, xmlUniverse))
    }

    @Bind
    fun save() {
        NotEnoughUpdates.INSTANCE.config.hidden.customTodos = todos.map { it.into() }.toMutableList()
        NotEnoughUpdates.INSTANCE.saveConfig()
    }

    @Bind
    fun addTodo() {
        todos.add(
            CustomTodoEditor(
                CustomTodo(
                    "Custom Todo # ${todos.size + 1}",
                    0,
                    "",
                    "",
                    false,
                ),
                todos,
                xmlUniverse,
            )
        )
    }
}
