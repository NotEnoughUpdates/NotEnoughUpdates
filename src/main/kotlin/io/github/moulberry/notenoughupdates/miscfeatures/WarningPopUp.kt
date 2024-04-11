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

package io.github.moulberry.notenoughupdates.miscfeatures

import io.github.moulberry.notenoughupdates.core.util.render.RenderUtils
import io.github.moulberry.notenoughupdates.core.util.render.TextRenderUtils
import io.github.moulberry.notenoughupdates.events.IsSlotBeingHoveredEvent
import io.github.moulberry.notenoughupdates.util.ScreenReplacer
import io.github.moulberry.notenoughupdates.util.Utils
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.EnumChatFormatting
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse

abstract class WarningPopUp : ScreenReplacer() {

    var isShowing = false
        private set
    fun show() {
        isShowing = true
    }

    override fun shouldShow(): Boolean {
        return isShowing
    }

    open fun getWarningPopup(): List<String>? = null

    abstract fun getItemName(): String

    abstract fun getWarningLines(): List<String>

    override fun render() {
        val scaledResolution = ScaledResolution(Minecraft.getMinecraft())
        val width = scaledResolution.scaledWidth
        val height = scaledResolution.scaledHeight

        GlStateManager.disableLighting()

        GlStateManager.pushMatrix()
        GlStateManager.translate(0f, 0f, 500f)

        drawRect(0, 0, width, height, -0x80000000)

        RenderUtils.drawFloatingRectDark(width / 2 - 90, height / 2 - 45, 180, 90)

        val neuLength = Minecraft.getMinecraft().fontRendererObj.getStringWidth("\u00a7lNEU")
        Minecraft.getMinecraft().fontRendererObj.drawString(
            "\u00a7lNEU",
            width / 2 + 90 - neuLength - 3,
            height / 2 - 45 + 4,
            -0x1000000
        )

        TextRenderUtils.drawStringCenteredScaledMaxWidth(
            "Are you SURE?",
            (width / 2).toFloat(), (height / 2 - 45 + 10).toFloat(), false, 170, -0xbfc0
        )


        val itemNameLine = "\u00a77[ §r${getItemName()}\u00a77 ]"
        TextRenderUtils.drawStringCenteredScaledMaxWidth(
            itemNameLine,
            (width / 2).toFloat(), (height / 2 - 45 + 25).toFloat(), false, 170, -0x1
        )
        for ((index, line) in getWarningLines().withIndex()) {
            TextRenderUtils.drawStringCenteredScaledMaxWidth(
                line,
                (width / 2).toFloat(), (height / 2 - 45 + 34 + 10 * index).toFloat(),
                false, 170, -0x5f5f60
            )
        }


        RenderUtils.drawFloatingRectDark(width / 2 - 43, height / 2 + 23, 40, 16, false)
        RenderUtils.drawFloatingRectDark(width / 2 + 3, height / 2 + 23, 40, 16, false)

        TextRenderUtils.drawStringCenteredScaledMaxWidth(
            EnumChatFormatting.GREEN.toString() + "[Y]es",
            (width / 2 - 23).toFloat(), (height / 2 + 31).toFloat(), true, 36, -0xff0100
        )
        TextRenderUtils.drawStringCenteredScaledMaxWidth(
            EnumChatFormatting.RED.toString() + "[N]o",
            (width / 2 + 23).toFloat(), (height / 2 + 31).toFloat(), true, 36, -0x10000
        )

        getWarningPopup()?.let { tooltip ->
            val mouseX = Mouse.getX() * width / Minecraft.getMinecraft().displayWidth
            val mouseY = height - Mouse.getY() * height / Minecraft.getMinecraft().displayHeight - 1

            val itemNameLength = Minecraft.getMinecraft().fontRendererObj.getStringWidth(itemNameLine)

            if (mouseX >= width / 2 - itemNameLength / 2 && mouseX <= width / 2 + itemNameLength / 2 && mouseY >= height / 2 - 45 + 20 && mouseY <= height / 2 - 45 + 30) {
                Utils.drawHoveringText(tooltip, mouseX, mouseY, width, height, -1)
            }
        }

        GlStateManager.popMatrix()
    }

    abstract fun confirmClick()

    override fun mouseInput(mouseX: Int, mouseY: Int): Boolean {
        val scaledResolution = ScaledResolution(Minecraft.getMinecraft())
        val width = scaledResolution.scaledWidth
        val height = scaledResolution.scaledHeight

        if (Mouse.getEventButtonState()) {
            // Yes and No button
            if ((mouseX >= width / 2 - 43 && mouseX <= width / 2 - 43 + 40) && (mouseY >= height / 2 + 23 && mouseY <= height / 2 + 23 + 16)) {
                confirmClick()
                isShowing = false
            } else if ((mouseX >= width / 2 + 3 && mouseX <= width / 2 + 3 + 40) && (mouseY >= height / 2 + 23 && mouseY <= height / 2 + 23 + 16)) {
                isShowing = false
            }

            // click outside the popup
            if (mouseX < width / 2 - 90 || mouseX > width / 2 + 90 || mouseY < height / 2 - 45 || mouseY > height / 2 + 45) {
                isShowing = false
            }
        }
        return false
    }

    @SubscribeEvent
    fun onHover(event: IsSlotBeingHoveredEvent) {
        if (shouldShow()) {
            event.prevent()
        }
    }

    override fun keyboardInput(): Boolean {
        if (!Keyboard.getEventKeyState()) {
            if (Keyboard.getEventKey() == Keyboard.KEY_Y || Keyboard.getEventKey() == Keyboard.KEY_RETURN) {
                confirmClick()
            }
            isShowing = false
            return true
        }

        return false
    }
}
