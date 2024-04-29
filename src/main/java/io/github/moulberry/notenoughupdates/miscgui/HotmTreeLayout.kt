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

package io.github.moulberry.notenoughupdates.miscgui

import com.google.gson.JsonElement
import io.github.moulberry.notenoughupdates.util.kotlin.ExtraData
import io.github.moulberry.notenoughupdates.util.kotlin.KSerializable

@KSerializable
data class HotmTreeLayoutFile(val hotm: HotmTreeLayout)

@KSerializable
data class HotmTreeLayout(val perks: Map<String, LayoutedHotmPerk>)

@KSerializable
data class LayoutedHotmPerk(
    val name: String,
    val x: Int,
    val y: Int,
    val maxLevel: Int,
    val lore: List<String>,
    @ExtraData
    val extras: Map<String, JsonElement>
) {
//TODO:     val compiledFunctions = mutableMapOf<String, ()
}
