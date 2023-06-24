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

package io.github.moulberry.notenoughupdates.commands.misc

import com.mojang.brigadier.context.CommandContext
import io.github.moulberry.notenoughupdates.NotEnoughUpdates
import io.github.moulberry.notenoughupdates.autosubscribe.NEUAutoSubscribe
import io.github.moulberry.notenoughupdates.events.RegisterBrigadierCommandEvent
import io.github.moulberry.notenoughupdates.profileviewer.GuiProfileViewer
import io.github.moulberry.notenoughupdates.util.brigadier.*
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.OpenGlHelper
import net.minecraft.command.ICommandSender
import net.minecraft.util.EnumChatFormatting.RED
import net.minecraftforge.fml.common.Loader
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@NEUAutoSubscribe
class ProfileViewerCommands {
    companion object {
        fun CommandContext<ICommandSender>.openPv(name: String?) {
            if (!NotEnoughUpdates.INSTANCE.isOnSkyblock) {
                Minecraft.getMinecraft().thePlayer.sendChatMessage("/pv ${name ?: ""}")
                return
            }
            if (!OpenGlHelper.isFramebufferEnabled()) {
                reply("${RED}Some parts of the profile viewer do not work with OptiFine Fast Render. Go to ESC > Options > Video Settings > Performance > Fast Render to disable it.")
            }

            if (NotEnoughUpdates.INSTANCE.config.apiData.apiKey.isNullOrBlank()) {
                reply("${RED}Can't view profile, an API key is not set. Run /api new and put the result in settings.")
                return
            }

            NotEnoughUpdates.profileViewer.getProfileByName(
                name ?: Minecraft.getMinecraft().thePlayer.name
            ) { profile ->
            NotEnoughUpdates.profileViewer.loadPlayerByName(name) { profile ->
                if (profile == null) {
                    reply("${RED}Invalid player name/API key. Maybe the API is down? Try /api new.")
                } else {
                    profile.resetCache()
                    NotEnoughUpdates.INSTANCE.openGui = GuiProfileViewer(profile)
                }
            }
        }
    }


    @SubscribeEvent
    fun onCommand(event: RegisterBrigadierCommandEvent) {
        fun pvCommand(name: String, before: () -> Unit) {
            event.command(name) {
                thenExecute {
                    before()
                    openPv(null)
                }
                thenArgument("player", RestArgumentType) { player ->
                    suggestsList { Minecraft.getMinecraft().theWorld.playerEntities.map { it.name } }
                    thenExecute {
                        before()
                        openPv(this[player])
                    }
                }.withHelp("Open the profile viewer for a player")
            }.withHelp("Open the profile viewer for yourself")
        }
        pvCommand("pv") {}
        pvCommand("neuprofile") {}
        if (!Loader.isModLoaded("skyblockextras"))
            pvCommand("cata") {
                GuiProfileViewer.currentPage = GuiProfileViewer.ProfileViewerPage.DUNGEON
            }


    }
}
