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

import io.github.moulberry.notenoughupdates.util.Calculator.CalculatorException
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.util.*
import kotlin.math.absoluteValue
import kotlin.math.pow

internal class CalculatorTest {
    @Suppress("NOTHING_TO_INLINE")
    inline fun calculationShouldBe(expr: String, d: Number) =
        assert(
            (Calculator.calculate(expr).toDouble() - d.toDouble()).absoluteValue < 0.001
        ) { "$expr should be equal to $d but is ${Calculator.calculate(expr)}" }

    @Test
    fun testReasonableCalculations() {
        calculationShouldBe("1+1", 2)
        calculationShouldBe("1k+1", 1001)
        calculationShouldBe("(1)+1", 2)
        calculationShouldBe("-0", 0)
        calculationShouldBe("-10+2", -8)
        calculationShouldBe("14k*23m/2.35+596123-9213", 137021863505.74)
        calculationShouldBe("1+-10+2", -7)
        calculationShouldBe("2**--10", 2.0.pow(10))
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val s = Scanner(System.`in`)
            while (true) {
                try {
                    val lex = Calculator.lex(s.nextLine())
                    val shunted = Calculator.shuntingYard(lex)
                    for (rawToken in shunted) {
                        System.out.printf(
                            "%s(%s)",
                            rawToken.type,
                            if (rawToken.operatorValue == null) rawToken.numericValue.toString() + " * 10 ^ " + rawToken.exponent else rawToken.operatorValue
                        )
                    }
                    println()
                    val evaluate = Calculator.evaluate(
                        { name: String? ->
                            Optional.of(
                                BigDecimal.valueOf(
                                    16
                                )
                            )
                        }, shunted
                    )
                    println("Eval: $evaluate")
                } catch (e: CalculatorException) {
                    e.printStackTrace()
                }
            }
        }
    }
}
