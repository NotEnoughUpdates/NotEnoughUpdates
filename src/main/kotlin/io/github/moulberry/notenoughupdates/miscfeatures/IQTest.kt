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

package io.github.moulberry.notenoughupdates.miscfeatures

import io.github.moulberry.notenoughupdates.NotEnoughUpdates
import io.github.moulberry.notenoughupdates.core.config.gui.GuiOptionEditorButton
import io.github.moulberry.notenoughupdates.core.config.gui.GuiOptionEditorFSR
import io.github.moulberry.notenoughupdates.core.config.gui.GuiOptionEditorText
import io.github.moulberry.notenoughupdates.core.config.struct.ConfigProcessor.ProcessedOption
import io.github.moulberry.notenoughupdates.util.Calculator
import io.github.moulberry.notenoughupdates.util.Calculator.CalculatorException
import java.math.BigDecimal
import kotlin.random.Random

object IQTest {
    fun randOperator() = listOf("+", "*").random()
    fun randNum() = Random.nextInt(1, 20)
    val question = "${randNum()} ${randOperator()} ${randNum()} ${randOperator()} ${randNum()}"

    @JvmStatic
    fun testIQ() {
        val testedAnswer = try {
            Calculator.calculate(answer)
        } catch (e: CalculatorException) {
            wrongAnswer()
            return
        }
        val realAnswer = Calculator.calculate(question)
        if (testedAnswer.subtract(realAnswer).abs() < BigDecimal.valueOf(0.1)) {
            NotEnoughUpdates.INSTANCE.config.apiData.apiDataUnlocked = true
            lastAnsweredTimestamp = 0L
        } else {
            wrongAnswer()
        }
    }

    private fun wrongAnswer() {
        lastAnsweredTimestamp = System.currentTimeMillis()
    }

    var lastAnsweredTimestamp = 0L

    @JvmField
    @Suppress("UNUSED")
    var buttonPlaceHolder = false

    @JvmField
    var answer = ""

    val answerOption = ProcessedOption(
        "IQ Test Question",
        "Please type out the answer to §a$question\n§cIf you just want to set your api key do §6/neusetkey <key>",
        -1,
        javaClass.getField("answer"),
        this,
        arrayOf()
    ).also {
        it.editor = GuiOptionEditorText(it)
    }
    val warningOption = ProcessedOption(
        "§4§lWARNING",
        "§4§lThis page is dangerous. Please make sure you know what you are doing!",
        -1,
        javaClass.getField("buttonPlaceHolder"),
        this,
        arrayOf()
    ).also {
        it.editor = GuiOptionEditorButton(it, 27, "SUBMIT", NotEnoughUpdates.INSTANCE.config)
    }
    val wrongOption = ProcessedOption(
        "§4Wrong Answer",
        "§4Please think twice before accessing it.",
        -1,
        javaClass.getField("buttonPlaceHolder"),
        this,
        arrayOf()
    ).also {
        it.editor = GuiOptionEditorFSR(it, -1, "", NotEnoughUpdates.INSTANCE.config)
    }


    @get:JvmStatic
    val options
        get() = LinkedHashMap<String, ProcessedOption>().also {
            it["iqTestAnswer"] = answerOption
            it["iqTestSubmit"] = warningOption

            if (System.currentTimeMillis() - lastAnsweredTimestamp < 10_000L) {
                it["iqTestWrong"] = wrongOption
            }
        }
}
