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

import com.mojang.brigadier.arguments.BoolArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import io.github.moulberry.notenoughupdates.autosubscribe.NEUAutoSubscribe
import io.github.moulberry.notenoughupdates.events.RegisterBrigadierCommandEvent
import io.github.moulberry.notenoughupdates.miscfeatures.dev.AnimatedSkullExporter
import io.github.moulberry.notenoughupdates.util.brigadier.get
import io.github.moulberry.notenoughupdates.util.brigadier.reply
import io.github.moulberry.notenoughupdates.util.brigadier.thenArgumentExecute
import io.github.moulberry.notenoughupdates.util.brigadier.thenExecute
import io.github.moulberry.notenoughupdates.util.brigadier.thenLiteral
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
                AnimatedSkullExporter.startRecording(AnimatedSkullExporter.RecordingType.HEAD)
            }.withHelp("Starts recording skull frames")

            thenLiteral("player") {
                thenArgumentExecute("player", StringArgumentType.string()) { name ->
                    AnimatedSkullExporter.startRecordingPlayer(this[name])
                }.withHelp("Starts recording another player's head")
            }.withHelp("Starts recording another player's head")

            thenLiteralExecute("pet") {
                    AnimatedSkullExporter.startRecording(AnimatedSkullExporter.RecordingType.PET)
                }.withHelp("Records pet texture instead")

            thenLiteral("stop") {
                thenArgumentExecute("recordExisting", BoolArgumentType.bool()) { record ->
                    if (AnimatedSkullExporter.isRecording()) {
                        AnimatedSkullExporter.finishRecording(true, this[record])
                        reply(ChatComponentText("${YELLOW}Stopped recording skull frames"))
                    } else {
                        reply(ChatComponentText("${YELLOW}Not recording skull frames"))
                        reply(ChatComponentText("${YELLOW}Use /neuskull start to start recording"))
                    }
                }.withHelp("True to also record frames that are already in the mod")
                thenExecute {
                    if (AnimatedSkullExporter.isRecording()) {
                        AnimatedSkullExporter.finishRecording(true, false)
                        reply(ChatComponentText("${YELLOW}Stopped recording skull frames"))
                    } else {
                        reply(ChatComponentText("${YELLOW}Not recording skull frames"))
                        reply(ChatComponentText("${YELLOW}Use /neuskull start to start recording"))
                    }
                }
            }.withHelp("Stops recording skull frames")
        }
    }
}
