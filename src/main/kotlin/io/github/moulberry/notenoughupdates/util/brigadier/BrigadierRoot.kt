/*
 * Copyright (C) 2023 Linnea Gräf
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

package io.github.moulberry.notenoughupdates.util.brigadier

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.ParseResults
import com.mojang.brigadier.tree.ArgumentCommandNode
import com.mojang.brigadier.tree.CommandNode
import io.github.moulberry.notenoughupdates.autosubscribe.NEUAutoSubscribe
import io.github.moulberry.notenoughupdates.events.RegisterBrigadierCommandEvent
import io.github.moulberry.notenoughupdates.util.LRUCache
import net.minecraft.command.ICommandSender
import net.minecraftforge.client.ClientCommandHandler
import java.lang.RuntimeException
import java.util.*

@NEUAutoSubscribe
object BrigadierRoot {
    private val help: MutableMap<CommandNode<DefaultSource>, String> = IdentityHashMap()
    var dispatcher = CommandDispatcher<DefaultSource>()
        private set
    val parseText =
        LRUCache.memoize<Pair<ICommandSender, String>, ParseResults<DefaultSource>>({ (sender, text) ->
            dispatcher.parse(text, sender)
        }, 1)

    fun getHelpForNode(node: CommandNode<DefaultSource>): String? {
        return help[node]
    }

    fun setHelpForNode(node: CommandNode<DefaultSource>, helpText: String) {
        if (node.command == null) {
            RuntimeException("Warning: Setting help on node that cannot be executed. Will be ignored").printStackTrace()
        }
        help[node] = helpText
    }


    fun getAllUsages(
        path: String,
        node: CommandNode<ICommandSender>,
        visited: MutableSet<CommandNode<ICommandSender>> = mutableSetOf()
    ): Sequence<NEUBrigadierHook.Usage> = sequence {
        if (node in visited) return@sequence
        visited.add(node)
        val redirect = node.redirect
        if (redirect != null) {
            yieldAll(getAllUsages(path, node.redirect, visited))
            visited.remove(node)
            return@sequence
        }
        if (node.command != null)
            yield(NEUBrigadierHook.Usage(path, getHelpForNode(node)))
        node.children.forEach {
            val nodeName = when (it) {
                is ArgumentCommandNode<*, *> -> "<${it.name}>"
                else -> it.name
            }
            yieldAll(getAllUsages("$path $nodeName", it, visited))
        }
        visited.remove(node)
    }


    fun updateHooks() = registerHooks(ClientCommandHandler.instance)

    fun registerHooks(handler: ClientCommandHandler) {
        val iterator = handler.commands.entries.iterator()
        while (iterator.hasNext()) {
            if (iterator.next().value is NEUBrigadierHook)
                iterator.remove()
        }
        dispatcher = CommandDispatcher()
        help.clear()
        parseText.clearCache()
        val event = RegisterBrigadierCommandEvent(this)
        event.post()
        event.hooks.forEach {
            if (handler.commands.containsKey(it.commandName)) {
                println("Could not register command ${it.commandName}")
            } else {
                handler.registerCommand(it)
            }
        }
    }
}
