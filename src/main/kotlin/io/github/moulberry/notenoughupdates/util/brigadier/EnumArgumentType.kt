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

package io.github.moulberry.notenoughupdates.util.brigadier

import com.mojang.brigadier.LiteralMessage
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import java.util.concurrent.CompletableFuture

class EnumArgumentType<T : Enum<T>>(
    val values: List<T>
) : ArgumentType<T> {
    companion object {
        @JvmStatic
        fun <T : Enum<T>> enum(values: Array<T>) = EnumArgumentType(values.toList())

        inline fun <reified T : Enum<T>> enum() = enum(enumValues<T>())
    }

    override fun getExamples(): Collection<String> {
        return values.map { it.name }
    }

    override fun <S : Any?> listSuggestions(
        context: CommandContext<S>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {

        examples
            .filter {builder.remaining.isBlank() ||  it.startsWith(builder.remaining, ignoreCase = true) }
            .forEach { builder.suggest(it) }
        return builder.buildFuture()
    }

    private val invalidEnum =
        SimpleCommandExceptionType(LiteralMessage("Expected one of: ${values.joinToString(", ")}"))

    override fun parse(reader: StringReader): T {
        val enumName = reader.readString()
        return values.find { enumName == it.name }
            ?: throw invalidEnum.createWithContext(reader)
    }
}
