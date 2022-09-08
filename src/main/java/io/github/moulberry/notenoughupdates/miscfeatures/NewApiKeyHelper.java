/*
 * Copyright (C) 2022 NotEnoughUpdates contributors
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

package io.github.moulberry.notenoughupdates.miscfeatures;

import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.C01PacketChatMessage;
import net.minecraft.util.ChatComponentText;

public class NewApiKeyHelper {
	private static final NewApiKeyHelper INSTANCE = new NewApiKeyHelper();

	public static NewApiKeyHelper getInstance() {
		return INSTANCE;
	}

	public void hookPacketChatMessage(C01PacketChatMessage packet) {
		String message = packet.getMessage().toLowerCase();
		if (message.equals("/api new")) return;

		if (message.replace(" ", "").startsWith("/apinew")) {
			Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(
				"§e[NotEnoughUpdates] §7You just executed §c" + packet.getMessage() +
					"§7. Did you mean to execute §e/api new§7?"));
		}
	}
}
