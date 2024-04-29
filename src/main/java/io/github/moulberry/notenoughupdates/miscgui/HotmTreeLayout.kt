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
import moe.nea.lisp.LispAst
import moe.nea.lisp.LispParser

@KSerializable
data class HotmTreeLayoutFile(
    val hotm: HotmTreeLayout,
    val prelude: List<String> = listOf()
)

@KSerializable
data class HotmTreeLayout(
    val perks: Map<String, LayoutedHotmPerk>,
    val powders: Map<String, PowderType>,
)

@KSerializable
data class PowderType(
    val costLine: String,
)

@KSerializable
data class LayoutedHotmPerk(
    val name: String,
    val x: Int,
    val y: Int,
    val maxLevel: Int,
    val powder: String,
    val cost: String,
    val lore: List<String>,
    @ExtraData
    val extras: Map<String, JsonElement>
) {

    val compiledFunctions: Map<String, LispAst.Program> = extras.mapValues {
        LispParser.parse("hotmlayout.json:perk:$name:${it.key}", it.value.asString)
    } + mapOf(
        "cost" to LispParser.parse("hotmlayout.json:perk:$name:cost", "(format-int $cost)")
    )
}
