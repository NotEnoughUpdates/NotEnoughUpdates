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

package io.github.moulberry.notenoughupdates.commands.dev

import io.github.moulberry.notenoughupdates.autosubscribe.NEUAutoSubscribe
import io.github.moulberry.notenoughupdates.events.RegisterBrigadierCommandEvent
import io.github.moulberry.notenoughupdates.util.brigadier.reply
import io.github.moulberry.notenoughupdates.util.brigadier.thenExecute
import io.github.moulberry.notenoughupdates.util.brigadier.thenLiteralExecute
import io.github.moulberry.notenoughupdates.util.brigadier.withHelp
import net.minecraft.client.Minecraft
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Desktop
import java.io.File

@NEUAutoSubscribe
class FolderCommand {

    @SubscribeEvent
    fun onCommands(event: RegisterBrigadierCommandEvent) {
        event.command("neufolder") {
            thenLiteralExecute("config") {
                Desktop.getDesktop().open(File(Minecraft.getMinecraft().mcDataDir, "config"));
                reply("Opened .minecraft/config")

            }.withHelp("Opens the .minecraft/config folder")

            thenLiteralExecute("mods") {
                Desktop.getDesktop().open(File(Minecraft.getMinecraft().mcDataDir, "mods"));
                reply("Opened .minecraft/mods")

            }.withHelp("Opens the .minecraft/mods folder")

            thenLiteralExecute("logs") {
                Desktop.getDesktop().open(File(Minecraft.getMinecraft().mcDataDir, "logs"));
                reply("Opened .minecraft/logs")

            }.withHelp("Opens the .minecraft/logs folder")

            thenLiteralExecute("crash") {
                Desktop.getDesktop().open(File(Minecraft.getMinecraft().mcDataDir, "crash-reports"));
                reply("Opened .minecraft/crash-reports")

            }.withHelp("Opens the .minecraft/crash-reports folder")

            thenLiteralExecute("minecraft") {
                Desktop.getDesktop().open(Minecraft.getMinecraft().mcDataDir);
                reply("Opened .minecraft")

            }.withHelp("Opens the .minecraft folder")


            thenExecute {
                Desktop.getDesktop().open(Minecraft.getMinecraft().mcDataDir);
                reply("Opened .minecraft")
            }
        }.withHelp("Opens the .minecraft folder")
    }
}
