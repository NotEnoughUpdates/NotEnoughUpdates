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

import io.github.moulberry.notenoughupdates.core.util.render.RenderUtils
import io.github.moulberry.notenoughupdates.util.Utils
import moe.nea.lisp.LispData
import moe.nea.lisp.LispExecutionContext
import moe.nea.lisp.bind.AutoBinder
import net.minecraft.client.gui.GuiScreen
import org.lwjgl.input.Mouse

class HotmTreeScreen(val hotmLayout: HotmTreeLayout) : GuiScreen() {
    val levels = mutableMapOf<String, Int>()
    var lastMouse = false
    val lec = LispExecutionContext()

    init {
        lec.setupStandardBindings()
        AutoBinder().bindTo(ExtraLispMethods(), lec.rootStackFrame)
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        super.drawScreen(mouseX, mouseY, partialTicks)
        RenderUtils.drawFloatingRectDark(10, 10, width - 20, height - 20)
        val thisMouse = Mouse.isButtonDown(0)
        val isClicking = lastMouse < thisMouse
        lastMouse = thisMouse
        for ((key, perk) in hotmLayout.perks) {
            RenderUtils.drawFloatingRectDark(
                perk.x * 18 + 18,
                perk.y * 18 + 18,
                16, 16
            )
            if (mouseX in (perk.x * 18 + 18..perk.x * 18 + 18 + 16) &&
                mouseY in (perk.y * 18 + 18..perk.y * 18 + 18 + 16)) {
                val level = levels[key] ?: 0
                Utils.drawHoveringText(
                    processList(perk, level),
                    mouseX, mouseY, width, height, -1
                )
                if (isClicking) {
                    levels[key] = (level + 1) % (perk.maxLevel + 1)
                }
            }
        }
    }

    private fun processList(perk: LayoutedHotmPerk, level: Int): List<String> {
        val bindings = lec.genBindings()
        bindings.setValueLocal("level", LispData.LispNumber(level.toDouble()))
        bindings.setValueLocal("potm", LispData.LispNumber((levels["special_0"] ?: 0).toDouble()))
        val values = perk.compiledFunctions.mapValues {
            lec.executeProgram(bindings.fork(), it.value)
        }
        val begin = listOf(
            perk.name,
            "§7Level $level/${perk.maxLevel}",
            ""
        )
        val end: List<String> = if (level == 0 || level == perk.maxLevel) listOf() else listOf(
            "",
            hotmLayout.powders[perk.powder]!!.costLine
        )
        return (begin + perk.lore + end).map {
            it.replace("\\{([a-z\\-]+)\\}".toRegex()) {
                (when (val value = values[it.groupValues[1]]) {
                    is LispData.LispString -> value.string
                    is LispData.LispNumber -> value.value.toString()
                    else -> "<lisp-error>"
                })
            }
        }
    }
}
