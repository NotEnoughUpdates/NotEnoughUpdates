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

package io.github.moulberry.notenoughupdates.recipes.generators

import io.github.moulberry.notenoughupdates.NotEnoughUpdates
import io.github.moulberry.notenoughupdates.autosubscribe.NEUAutoSubscribe
import io.github.moulberry.notenoughupdates.events.GuiContainerBackgroundDrawnEvent
import io.github.moulberry.notenoughupdates.mixins.AccessorGuiContainer
import io.github.moulberry.notenoughupdates.util.Utils
import io.github.moulberry.notenoughupdates.util.kotlin.Coroutines
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraftforge.client.event.GuiScreenEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.input.Mouse
import java.util.*

@NEUAutoSubscribe
object RepoExporters {
    private val allRepoExporters = ServiceLoader.load(RepoExporter::class.java).let {
        it.reload()
        it.toList()
    }
    private var lastRenderedButtons = listOf<Pair<GuiButton, RepoExporter>>()
    private var lastGui: GuiContainer? = null

    @SubscribeEvent
    fun onGuiRender(event: GuiContainerBackgroundDrawnEvent) {
        if (!NotEnoughUpdates.INSTANCE.config.apiData.repositoryEditing) return
        val mouseX = Utils.getMouseX()
        val mouseY = Utils.getMouseY()
        val gui = event.container
        if (gui !is AccessorGuiContainer) return
        val exporters = allRepoExporters.filter { it.canExport(gui) }
        synchronized(this) {
            lastGui = gui
            lastRenderedButtons = exporters.withIndex().map { (index, exporter) ->
                GuiButton(
                    0,
                    0, -30 - 20 * index,
                    gui.xSize, 20,
                    "Run Exporter: ${exporter.name}"
                ).also {
                    it.drawButton(
                        Minecraft.getMinecraft(),
                        mouseX - gui.guiLeft,
                        mouseY - gui.guiTop
                    )
                } to exporter
            }
        }
    }

    @SubscribeEvent
    fun onGuiClick(event: GuiScreenEvent.MouseInputEvent.Pre) {
        if (!Mouse.getEventButtonState()) return
        val accessor = event.gui as? AccessorGuiContainer ?: return

        val mouseX = Utils.getMouseX() - accessor.guiLeft
        val mouseY = Utils.getMouseY() - accessor.guiTop

        val exporter = synchronized(this) {
            if (lastGui !== event.gui) return
            lastRenderedButtons.forEach { (button, exporter) ->
                if (button.mousePressed(Minecraft.getMinecraft(), mouseX, mouseY)) {
                    return@synchronized exporter
                }
            }
            return
        }
        event.isCanceled = true
        Coroutines.launchCoroutineOnCurrentThread {
            try {
                exporter.export(RepoExportingContext(NotEnoughUpdates.INSTANCE.manager, event.gui))
                Utils.addChatMessage("Repo export completed")
            } catch (e: RepoExportingInterruptedException) {
                Utils.addChatMessage("Repo exporting interrupted")
            }
        }
    }
}
