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

package io.github.moulberry.notenoughupdates.commands.dev

import com.mojang.brigadier.arguments.FloatArgumentType.floatArg
import com.mojang.brigadier.arguments.StringArgumentType.string
import io.github.moulberry.notenoughupdates.NotEnoughUpdates
import io.github.moulberry.notenoughupdates.autosubscribe.NEUAutoSubscribe
import io.github.moulberry.notenoughupdates.dungeons.DungeonWin
import io.github.moulberry.notenoughupdates.events.RegisterBrigadierCommandEvent
import io.github.moulberry.notenoughupdates.miscfeatures.NullzeeSphere
import io.github.moulberry.notenoughupdates.util.brigadier.*
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.event.ClickEvent
import net.minecraft.util.BlockPos
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@NEUAutoSubscribe
class SimpleDevCommands {
    @SubscribeEvent
    fun onCommands(event: RegisterBrigadierCommandEvent) {
        event.command("neureloadrepo") {
            thenExecute {
                NotEnoughUpdates.INSTANCE.manager.reloadRepository()
                reply("§e[NEU] Reloaded repository.")
            }
        }
        event.command("neudungeonwintest") {
            thenExecute {
                DungeonWin.displayWin()
            }
            thenArgumentExecute("file", string()) { file ->
                DungeonWin.TEAM_SCORE = ResourceLocation("notenoughupdates:dungeon_win/${this[file].lowercase()}.png")
                reply("Changed the dungeon win display")
            }
        }
        event.command("neuenablestorage") {
            thenLiteralExecute("disable") {
                NotEnoughUpdates.INSTANCE.config.storageGUI.enableStorageGUI3 = true
                NotEnoughUpdates.INSTANCE.saveConfig()
                reply("Disabled the NEU storage overlay. Click here to enable again") {
                    chatStyle.chatClickEvent = ClickEvent(
                        ClickEvent.Action.SUGGEST_COMMAND,
                        "/neuenablestorage"
                    )
                }
            }
            thenExecute {
                NotEnoughUpdates.INSTANCE.config.storageGUI.enableStorageGUI3 = true
                NotEnoughUpdates.INSTANCE.saveConfig()
                reply("Enabled the NEU storage overlay. Click here to disable again") {
                    chatStyle.chatClickEvent = ClickEvent(
                        ClickEvent.Action.SUGGEST_COMMAND,
                        "/neuenablestorage disable"
                    )
                }
            }
        }
        event.command("neuzeesphere") {
            thenLiteralExecute("on") {
                NullzeeSphere.enabled = true
                reply("Enabled nullzee sphere")
            }
            thenLiteralExecute("off") {
                NullzeeSphere.enabled = false
                reply("Disabled nullzee sphere")
            }
            thenLiteralExecute("setcenter") {
                val p = source as EntityPlayerSP
                NullzeeSphere.centerPos = BlockPos(p.posX, p.posY, p.posZ)
                NullzeeSphere.overlayVBO = null
                reply("Set center to ${NullzeeSphere.centerPos}")
            }
            thenArgumentExecute("radius", floatArg(0F)) { size ->
                NullzeeSphere.size = this[size]
                NullzeeSphere.overlayVBO = null
                reply("Set size to ${this[size]}")
            }
        }
    }
}
