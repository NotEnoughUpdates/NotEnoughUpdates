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

package io.github.moulberry.notenoughupdates.events

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import io.github.moulberry.notenoughupdates.util.brigadier.BrigadierRoot
import io.github.moulberry.notenoughupdates.util.brigadier.NEUBrigadierHook
import io.github.moulberry.notenoughupdates.util.brigadier.literal
import net.minecraft.command.ICommandSender
import java.util.function.Consumer

data class RegisterBrigadierCommandEvent(val brigadierRoot: BrigadierRoot) : NEUEvent() {
    val dispatcher = brigadierRoot.dispatcher
    val hooks = mutableListOf<NEUBrigadierHook>()
    fun command(name: String, block: Consumer<LiteralArgumentBuilder<ICommandSender>>): NEUBrigadierHook {
        return command(name) {
            block.accept(this)
        }
    }

    fun command(name: String, block: LiteralArgumentBuilder<ICommandSender>.() -> Unit): NEUBrigadierHook {
        val node = dispatcher.register(literal(name, block))
        val hook = NEUBrigadierHook(brigadierRoot, node)
        hooks.add(hook)
        return hook
    }

}
