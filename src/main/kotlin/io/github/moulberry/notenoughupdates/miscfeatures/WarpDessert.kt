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

package io.github.moulberry.notenoughupdates.miscfeatures

import io.github.moulberry.notenoughupdates.util.Utils
import net.minecraft.client.Minecraft
import net.minecraft.network.play.client.C01PacketChatMessage

object WarpDessert {
    @JvmStatic
    fun onPacketChatMessage(packet: C01PacketChatMessage): Boolean {
        val message = packet.message.lowercase()
        if (message == "/warp dessert") {
            Minecraft.getMinecraft().thePlayer.sendChatMessage("/warp desert")
            Utils.addChatMessage("§e[NEU] Did someone say §d§lDessert§e? Sand is yummy!")
            return true
        }
        return false
    }
}
