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

import com.google.gson.JsonObject
import io.github.moulberry.notenoughupdates.NEUManager
import io.github.moulberry.notenoughupdates.util.MinecraftExecutor
import io.github.moulberry.notenoughupdates.util.kotlin.Coroutines.continueOn
import io.github.moulberry.notenoughupdates.util.kotlin.Coroutines.waitTicks
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.GuiYesNo
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class RepoExportingContext(
    val manager: NEUManager,
    val gui: GuiScreen,
) {
    val mc = Minecraft.getMinecraft()
    val nameToItemCache = mutableMapOf<String, String>()

    suspend fun writeFile(file: File, json: JsonObject) {
        if (file.exists()) {
            if (!askYesNo("Overwrite file?", "The file $file already exists."))
                return
        }
        manager.writeJson(json, file)
    }

    suspend fun askYesNo(title: String, subtitle: String): Boolean {
        var wasResolved = false
        continueOn(MinecraftExecutor.OnThread)
        val result = suspendCoroutine {
            mc.displayGuiScreen(object : GuiYesNo({ result, id ->
                if (!wasResolved) {
                    wasResolved = true
                    it.resume(result)
                }
            }, title, subtitle, 0) {
                override fun onGuiClosed() {
                    if (!wasResolved) {
                        wasResolved = true
                        it.resume(null)
                    }
                }
            })
        }
        if (result == null) {
            waitTicks(1) // Wait one tick for gui closing animation
        }
        mc.displayGuiScreen(gui)
        return result ?: false
    }

    suspend fun findItemByName(name: String): String {
        val c = nameToItemCache[name]
        if (c != null) return c
        var wasResolved = false
        continueOn(MinecraftExecutor.OnThread)
        val result = suspendCoroutine { cont ->
            mc.displayGuiScreen(ItemSearchGui(name) {
                if (!wasResolved) {
                    wasResolved = true
                    cont.resume(it)
                }
            })
        }
        waitTicks(1)
        mc.displayGuiScreen(gui)
        result ?: throw RepoExportingInterruptedException()
        nameToItemCache[name] = result
        return result
    }
}
