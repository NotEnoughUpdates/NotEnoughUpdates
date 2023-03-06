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

import com.google.auto.service.AutoService
import io.github.moulberry.notenoughupdates.util.Utils
import net.minecraft.client.gui.GuiScreen

@AutoService(RepoExporter::class)
class TestExporter : RepoExporter {
    override suspend fun export(context: RepoExportingContext) {
        if (context.askYesNo("Do Vaccines cause Autism?", "(Specifically the CoViD-19 Vaccine)")) {
            Utils.addChatMessage("Stupid")
        } else {
            Utils.addChatMessage("Also Stupid")
        }
    }

    override fun canExport(gui: GuiScreen): Boolean {
        return true
    }

    override val name: String
        get() = "Test Exporter"
}
