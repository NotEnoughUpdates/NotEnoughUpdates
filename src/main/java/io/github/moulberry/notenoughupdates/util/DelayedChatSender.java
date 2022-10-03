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

package io.github.moulberry.notenoughupdates.util;

import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DelayedChatSender {

	private static final List<IChatComponent> delayedMessages = new ArrayList<>();

	public static void sendChatMessage(IChatComponent comp) {
		MinecraftExecutor.INSTANCE.execute(() -> {
			NotEnoughUpdates.LOGGER.info("Enqueuing chat message: " + comp.getUnformattedText());
			EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;
			if (thePlayer != null) {
				thePlayer.addChatMessage(comp);
			} else {
				delayedMessages.add(comp);
			}
		});
	}

	@SubscribeEvent
	public void onPlayerJoinWorld(EntityJoinWorldEvent event) {
		EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;
		if (thePlayer == null) return;
		for (Iterator<IChatComponent> iterator = delayedMessages.iterator(); iterator.hasNext(); ) {
			thePlayer.addChatMessage(iterator.next());
			iterator.remove();
		}
	}

}
