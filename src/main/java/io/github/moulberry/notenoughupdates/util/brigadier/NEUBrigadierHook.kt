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

import com.mojang.brigadier.ParseResults
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.tree.CommandNode
import net.minecraft.command.CommandBase
import net.minecraft.command.ICommandSender
import net.minecraft.util.BlockPos
import net.minecraft.util.ChatComponentText
import net.minecraft.util.EnumChatFormatting.RED
import net.minecraft.util.EnumChatFormatting.YELLOW
import java.util.concurrent.CompletableFuture
import java.util.function.Predicate

/**
 * Hook for converting brigadier commands to normal legacy Minecraft commands (string array style).
 */
class NEUBrigadierHook(
    val brigadierRoot: BrigadierRoot,
    val commandNode: CommandNode<DefaultSource>,
    val aliases: List<String>
) : CommandBase() {
    /**
     * Runs before the command gets executed. Return false to prevent execution.
     */
    var beforeCommand: Predicate<ParseResults<DefaultSource>>? = null

    override fun getCommandName(): String {
        return commandNode.name
    }

    override fun getCommandAliases(): List<String> {
        return aliases
    }

    data class Usage(
        val path: String,
        val help: String?,
    )


    override fun getCommandUsage(sender: ICommandSender): String {
        return brigadierRoot.getAllUsages("/$commandName", commandNode, mutableSetOf()).joinToString("\n") { "${it.path} - ${it.help ?: "Missing help"}"}
    }

    private fun getText(args: Array<out String>) = "${commandNode.name} ${args.joinToString(" ")}"

    override fun processCommand(sender: ICommandSender, args: Array<out String>) {
        val results = brigadierRoot.parseText.apply(sender to getText(args).trim())
        if (beforeCommand?.test(results) == false)
            return
        try {
            brigadierRoot.dispatcher.execute(results)
        } catch (syntax: CommandSyntaxException) {
            brigadierRoot.getAllUsages("/$commandName", commandNode, mutableSetOf()).forEach {
                sender.addChatMessage(ChatComponentText("${YELLOW}[NEU] ${it.path} - ${it.help}"))
            }
        }
    }

    // We love async tab completion (may end up requiring pressing tab multiple times, but uhhhhh .get() bad)
    private var lastCompletionText: String? = null
    private var lastCompletion: CompletableFuture<Suggestions>? = null
    override fun addTabCompletionOptions(
        sender: ICommandSender,
        args: Array<out String>,
        pos: BlockPos
    ): List<String> {
        val originalText = getText(args)
        var lc: CompletableFuture<Suggestions>? = null
        if (lastCompletionText == originalText) {
            lc = lastCompletion
        }
        if (lc == null) {
            lastCompletion?.cancel(true)
            val results = brigadierRoot.parseText.apply(sender to originalText)
            lc = brigadierRoot.dispatcher.getCompletionSuggestions(results)
        }
        lastCompletion = lc
        lastCompletionText = originalText
        val suggestions = lastCompletion?.getNow(null) ?: return emptyList()
        return suggestions.list.map { it.text }
    }

    override fun canCommandSenderUseCommand(sender: ICommandSender): Boolean {
        return true // Permissions are checked by brigadier instead (or by the beforeCommand hook)
    }

}
