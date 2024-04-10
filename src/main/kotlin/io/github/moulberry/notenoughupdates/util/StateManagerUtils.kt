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

package io.github.moulberry.notenoughupdates.util

import io.github.moulberry.notenoughupdates.mixins.AccessorBooleanState
import net.minecraft.client.renderer.GlStateManager

object StateManagerUtils {
    fun readState(state: GlStateManager.BooleanState): Boolean {
        state as AccessorBooleanState
        return state.currentState
    }

    inline fun withSavedState(state: GlStateManager.BooleanState, block: () -> Unit) {
        val savedState = readState(state)
        block()
        state.setState(savedState)
    }
}
