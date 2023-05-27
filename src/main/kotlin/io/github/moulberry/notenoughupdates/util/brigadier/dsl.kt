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
import com.mojang.brigadier.tree.ArgumentCommandNode
import com.mojang.brigadier.tree.CommandNode
import com.mojang.brigadier.tree.LiteralCommandNode
import io.github.moulberry.notenoughupdates.commands.dev.DevTestCommand
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
    source.addChatMessage(ChatComponentText("§e[NEU] ").appendSibling(component))
}

@JvmOverloads
fun <T : ICommandSender, C : CommandContext<T>> C.reply(text: String, block: ChatComponentText.() -> Unit = {}) {
    source.addChatMessage(ChatComponentText(text.split("\n").joinToString("\n") { "§e[NEU] $it" }).also(block))
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
): ArgumentCommandNode<DefaultSource, AT> = argument(name, argument, block).build().also(::then)

fun <T : ArgumentBuilder<DefaultSource, T>, AT : Any> T.thenArgumentExecute(
    name: String,
    argument: ArgumentType<AT>,
    block: CommandContext<DefaultSource>.(TypeSafeArg<AT>) -> Unit
): ArgumentCommandNode<DefaultSource, AT> = thenArgument(name, argument) {
    thenExecute {
        block(it)
    }
}

fun literal(
    name: String,
    block: LiteralArgumentBuilder<DefaultSource>.() -> Unit = {}
): LiteralArgumentBuilder<DefaultSource> =
    LiteralArgumentBuilder.literal<DefaultSource>(name).also(block)

fun <T : ArgumentBuilder<DefaultSource, T>> T.thenLiteral(
    name: String,
    block: LiteralArgumentBuilder<DefaultSource>.() -> Unit
): LiteralCommandNode<DefaultSource> =
    then(literal(name), block) as LiteralCommandNode<DefaultSource>


fun <T : ArgumentBuilder<DefaultSource, T>> T.thenLiteralExecute(
    name: String,
    block: CommandContext<DefaultSource>.() -> Unit
): LiteralCommandNode<DefaultSource> =
    thenLiteral(name) {
        thenExecute(block)
    }

fun <T : ArgumentBuilder<DefaultSource, T>> T.thenRedirect(node: CommandNode<DefaultSource>): T {
    node.children.forEach {
        this.then(it)
    }
    forward(node.redirect, node.redirectModifier, node.isFork)
    executes(node.command)
    return this
}

fun <T : ArgumentBuilder<DefaultSource, T>, U : ArgumentBuilder<DefaultSource, U>> T.then(
    node: U,
    block: U.() -> Unit
): CommandNode<DefaultSource> =
    node.also(block).build().also(::then)

fun <T : ArgumentBuilder<DefaultSource, T>> T.thenExecute(block: CommandContext<DefaultSource>.() -> Unit): T =
    executes {
        block(it)
        1
    }

fun <T : ArgumentBuilder<DefaultSource, T>> T.requiresDev(): T {
    requires { DevTestCommand.isDeveloper(it) }
    return this
}

fun NEUBrigadierHook.withHelp(helpText: String): NEUBrigadierHook {
    commandNode.withHelp(helpText)
    return this
}

fun <T : CommandNode<DefaultSource>> T.withHelp(helpText: String): T {
    BrigadierRoot.setHelpForNode(this, helpText)
    return this
}

fun <A : Any, T : RequiredArgumentBuilder<DefaultSource, A>> T.suggestsList(list: List<String>) {
    suggestsList { list }
}

fun <A : Any, T : RequiredArgumentBuilder<DefaultSource, A>> T.suggestsList(list: () -> List<String>) {
    suggests { context, builder ->
        list().filter { it.startsWith(builder.remaining, ignoreCase = true) }
            .forEach { builder.suggest(it) }
        builder.buildFuture()
    }
}



