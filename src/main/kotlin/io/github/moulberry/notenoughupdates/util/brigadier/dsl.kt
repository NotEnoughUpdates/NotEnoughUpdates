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

import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import io.github.moulberry.notenoughupdates.util.iterate
import net.minecraft.command.ICommandSender
import net.minecraft.util.ChatComponentText
import net.minecraft.util.IChatComponent
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.lang.reflect.TypeVariable


typealias DefaultSource = ICommandSender



private fun normalizeGeneric(argument: Type): Class<*> {
    return if (argument is Class<*>) {
        argument
    } else if (argument is TypeVariable<*>) {
        normalizeGeneric(argument.bounds[0])
    } else if (argument is ParameterizedType) {
        normalizeGeneric(argument.rawType)
    } else {
        Any::class.java
    }
}

data class TypeSafeArg<T : Any>(val name: String, val argument: ArgumentType<T>) {
    val argClass by lazy {
        argument.javaClass
            .iterate<Class<in ArgumentType<T>>> {
                it.superclass
            }
            .flatMap {
                it.genericInterfaces.toList()
            }
            .filterIsInstance<ParameterizedType>()
            .find { it.rawType == ArgumentType::class.java }!!
            .let {
                normalizeGeneric(it.actualTypeArguments[0])
            }
    }

    @JvmName("getWithThis")
    fun <S> CommandContext<S>.get(): T =
        get(this)


    fun <S> get(ctx: CommandContext<S>): T {
        return ctx.getArgument(name, argClass) as T
    }
}

fun <T : ICommandSender, C : CommandContext<T>> C.reply(component: IChatComponent) {
    source.addChatMessage(component)
}

fun <T : ICommandSender, C : CommandContext<T>> C.reply(text: String) {
    source.addChatMessage(ChatComponentText(text))
}

fun <T : ICommandSender, C : CommandContext<T>> C.reply(text: String, block: ChatComponentText.() -> Unit) {
    source.addChatMessage(ChatComponentText(text).also(block))
}

operator fun <T : Any, C : CommandContext<*>> C.get(arg: TypeSafeArg<T>): T {
    return arg.get(this)
}


fun <T : Any> argument(
    name: String,
    argument: ArgumentType<T>,
    block: RequiredArgumentBuilder<DefaultSource, T>.(TypeSafeArg<T>) -> Unit
): RequiredArgumentBuilder<DefaultSource, T> =
    RequiredArgumentBuilder.argument<DefaultSource, T>(name, argument).also { block(it, TypeSafeArg(name, argument)) }

fun <T : ArgumentBuilder<DefaultSource, T>, AT : Any> T.thenArgument(
    name: String,
    argument: ArgumentType<AT>,
    block: RequiredArgumentBuilder<DefaultSource, AT>.(TypeSafeArg<AT>) -> Unit
): T = then(argument(name, argument, block))

fun <T : ArgumentBuilder<DefaultSource, T>, AT : Any> T.thenArgumentExecute(
    name: String,
    argument: ArgumentType<AT>,
    block: CommandContext<DefaultSource>.(TypeSafeArg<AT>) -> Unit
): T = thenArgument(name, argument) {
    thenExecute {
        block(it)
    }
}

fun literal(
    name: String,
    block: LiteralArgumentBuilder<DefaultSource>.() -> Unit
): LiteralArgumentBuilder<DefaultSource> =
    LiteralArgumentBuilder.literal<DefaultSource>(name).also(block)

fun <T : ArgumentBuilder<DefaultSource, T>> T.thenLiteral(
    name: String,
    block: LiteralArgumentBuilder<DefaultSource>.() -> Unit
): T =
    then(literal(name, block))


fun <T : ArgumentBuilder<DefaultSource, T>> T.thenLiteralExecute(
    name: String,
    block: CommandContext<DefaultSource>.() -> Unit
): T =
    thenLiteral(name) {
        thenExecute(block)
    }

fun <T : ArgumentBuilder<DefaultSource, T>> T.then(node: ArgumentBuilder<DefaultSource, *>, block: T.() -> Unit): T =
    then(node).also(block)

fun <T : ArgumentBuilder<DefaultSource, T>> T.thenExecute(block: CommandContext<DefaultSource>.() -> Unit): T =
    executes {
        block(it)
        1
    }


