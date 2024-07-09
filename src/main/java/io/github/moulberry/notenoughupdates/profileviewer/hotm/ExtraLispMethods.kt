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

package io.github.moulberry.notenoughupdates.profileviewer.hotm

import io.github.moulberry.notenoughupdates.core.util.StringUtils
import moe.nea.lisp.LispData
import moe.nea.lisp.bind.LispBinding
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.pow
import kotlin.math.round

class ExtraLispMethods {
    @LispBinding("pow")
    fun powFunc(base: Double, exponent: Double): LispData.LispNumber {
        return LispData.LispNumber(base.pow(exponent))
    }

    @LispBinding("round")
    fun roundFunc(value: Double): LispData.LispNumber {
        return LispData.LispNumber(round(value))
    }

    @LispBinding("ceil")
    fun ceilFunc(value: Double): LispData.LispNumber {
        return LispData.LispNumber(ceil(value))
    }

    @LispBinding("floor")
    fun floorFunc(value: Double): LispData.LispNumber {
        return LispData.LispNumber(floor(value))
    }

    @LispBinding("format-int")
    fun intToString(base: Double): LispData.LispString {
        return LispData.LispString(StringUtils.formatNumber(base.toInt()))
    }
}
