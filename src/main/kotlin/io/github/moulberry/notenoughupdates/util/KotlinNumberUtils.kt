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

package io.github.moulberry.notenoughupdates.util

import java.util.*
import kotlin.math.pow
import kotlin.math.round

fun Double.roundToDecimals(decimals: Int): Double {
    val multiplier = 10.0.pow(decimals)
    return round(this * multiplier) / multiplier
}

private val romanSymbols = TreeMap(
    mapOf(
        1000 to "M",
        900 to "CM",
        500 to "D",
        400 to "CD",
        100 to "C",
        90 to "XC",
        50 to "L",
        40 to "XL",
        10 to "X",
        9 to "IX",
        5 to "V",
        4 to "IV",
        1 to "I",
    )
)

fun Int.toRoman(): String {
    if (this <= 0) error("$this must be positive!")
    val l = romanSymbols.floorKey(this)
    return if (this == l) {
        romanSymbols[this]!!
    } else romanSymbols[l] + (this - l).toRoman()
}
