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
import io.github.moulberry.notenoughupdates.miscfeatures.dev.AnimatedSkullExporter
import io.github.moulberry.notenoughupdates.util.brigadier.reply
import io.github.moulberry.notenoughupdates.util.brigadier.thenLiteralExecute
import io.github.moulberry.notenoughupdates.util.brigadier.withHelp
import net.minecraft.util.ChatComponentText
import net.minecraft.util.EnumChatFormatting.YELLOW
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@NEUAutoSubscribe
class SkullCommand {

    @SubscribeEvent
    fun onCommands(event: RegisterBrigadierCommandEvent) {
        event.command("neuskull") {
            thenLiteralExecute("start") {
                if (!AnimatedSkullExporter.enabled) {
                    AnimatedSkullExporter.enabled = true
                    reply(ChatComponentText("${YELLOW}Started recording skull frames"))
                    reply(ChatComponentText("${YELLOW}Wait for the animation to play out"))
                    reply(ChatComponentText("${YELLOW}Use /neuskull stop to stop recording"))
                } else {
                    AnimatedSkullExporter.finishRecording(false)
                    AnimatedSkullExporter.enabled = true
                    reply(ChatComponentText("${YELLOW}Restarted recording skull frames"))
                    reply(ChatComponentText("${YELLOW}Wait for the animation to play out"))
                    reply(ChatComponentText("${YELLOW}Use /neuskull stop to stop recording"))
                }
            }.withHelp("Starts recording skull frames")
            thenLiteralExecute("stop") {
                if (AnimatedSkullExporter.enabled) {
                    AnimatedSkullExporter.finishRecording(true)
                    reply(ChatComponentText("${YELLOW}Stopped recording skull frames"))
                } else {
                    reply(ChatComponentText("${YELLOW}Not recording skull frames"))
                    reply(ChatComponentText("${YELLOW}Use /neuskull start to start recording"))
                }
            }.withHelp("Stops recording skull frames")
        }
    }
}
