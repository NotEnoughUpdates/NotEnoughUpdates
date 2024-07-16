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

package io.github.moulberry.notenoughupdates.miscfeatures.updater

import io.github.moulberry.moulconfig.gui.GuiOptionEditor
import io.github.moulberry.moulconfig.processor.ProcessedOption
import io.github.moulberry.notenoughupdates.core.util.render.TextRenderUtils
import io.github.moulberry.notenoughupdates.itemeditor.GuiElementButton
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.EnumChatFormatting.*

import org.lwjgl.input.Mouse

class ConfigVersionGuiOption(option: ProcessedOption) : GuiOptionEditor(option) {
    val button = GuiElementButton("", -1) { }
    override fun render(x: Int, y: Int, width: Int) {
        val fr = Minecraft.getMinecraft().fontRendererObj
        GlStateManager.pushMatrix()
        GlStateManager.translate(x.toFloat() + 10, y.toFloat(), 1F)
        val width = width - 20
        val nextVersion = AutoUpdater.getNextVersion()

        button.text = when (AutoUpdater.updateState) {
            AutoUpdater.UpdateState.AVAILABLE -> "Download update"
            AutoUpdater.UpdateState.QUEUED -> "Downloading..."
            AutoUpdater.UpdateState.DOWNLOADED -> "Downloaded"
            AutoUpdater.UpdateState.NONE -> if (nextVersion == null) "Check for Updates" else "Up to date"
        }
        button.render(getButtonPosition(width), 10)

        if (AutoUpdater.updateState == AutoUpdater.UpdateState.DOWNLOADED) {
            TextRenderUtils.drawStringCentered(
                "${GREEN}The update will be installed after your next restart.",
                fr,
                width / 2F,
                40F,
                true,
                -1
            )
        }

        val widthRemaining = width - button.width - 10

        GlStateManager.scale(2F, 2F, 1F)
        TextRenderUtils.drawStringCenteredScaledMaxWidth(
            "${if (AutoUpdater.updateState == AutoUpdater.UpdateState.NONE) GREEN else RED}${AutoUpdater.getCurrentVersion()}" +
                    if (nextVersion != null && AutoUpdater.updateState != AutoUpdater.UpdateState.NONE) "➜ ${GREEN}${nextVersion}" else "",
            widthRemaining / 4F,
            10F,
            true,
            widthRemaining / 2,
            -1
        )

        GlStateManager.popMatrix()
    }

    fun getButtonPosition(width: Int) = width - button.width
    override fun getHeight(): Int {
        return 55
    }

    override fun mouseInput(x: Int, y: Int, width: Int, mouseX: Int, mouseY: Int): Boolean {
        val width = width - 20
        if (Mouse.getEventButtonState()) {
            if ((mouseX - getButtonPosition(width) - x) in (0..button.width) && (mouseY - 10 - y) in (0..button.height)) {
                when (AutoUpdater.updateState) {
                    AutoUpdater.UpdateState.AVAILABLE -> AutoUpdater.queueUpdate()
                    AutoUpdater.UpdateState.QUEUED -> {}
                    AutoUpdater.UpdateState.DOWNLOADED -> {}
                    AutoUpdater.UpdateState.NONE -> AutoUpdater.checkUpdate()
                }
                return true
            }
        }
        return false
    }

    override fun keyboardInput(): Boolean {
        return false
    }

}
