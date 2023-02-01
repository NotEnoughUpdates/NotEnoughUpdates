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

import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.autosubscribe.NEUAutoSubscribe;
import io.github.moulberry.notenoughupdates.events.SlotClickEvent;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C01PacketChatMessage;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@NEUAutoSubscribe
public class AntiCoopAdd {

	private static final AntiCoopAdd INSTANCE = new AntiCoopAdd();

	public static AntiCoopAdd getInstance() {
		return INSTANCE;
	}

	@SubscribeEvent
	public void onMouseClick(SlotClickEvent event) {
		if (!NotEnoughUpdates.INSTANCE.config.misc.coopWarning) return;
		if (event.slotId == -999) return;
		if (!Utils.getOpenChestName().contains("Profile")) return;

		ItemStack stack = event.slot.getStack();
		if (stack == null) return;
		if (stack.getItem() == Items.diamond && stack.getDisplayName() != null && stack.getDisplayName().contains(
			"Co-op Request")) {
			String ign = Utils.getOpenChestName().split("'s Profile")[0];
			ChatComponentText storageMessage = new ChatComponentText(
				EnumChatFormatting.YELLOW + "[NEU] " + EnumChatFormatting.YELLOW +
					"You just clicked on the Co-op add button. If you want to coop add this person, click this chat message");
			storageMessage.setChatStyle(Utils.createClickStyle(ClickEvent.Action.RUN_COMMAND, "/coopadd " + ign));
			storageMessage.setChatStyle(storageMessage.getChatStyle().setChatHoverEvent(
				new HoverEvent(
					HoverEvent.Action.SHOW_TEXT,
					new ChatComponentText(EnumChatFormatting.YELLOW + "Click to add " + ign + " to your coop")
				)));
			ChatComponentText storageChatMessage = new ChatComponentText("");
			storageChatMessage.appendSibling(storageMessage);
			Minecraft.getMinecraft().thePlayer.addChatMessage(storageChatMessage);
			event.setCanceled(true);
		}
	}

	public Boolean onPacketChatMessage(C01PacketChatMessage packet) {
		if (!NotEnoughUpdates.INSTANCE.config.misc.coopWarning) return false;

		String message = packet.getMessage().toLowerCase();
		if (message.startsWith("/hypixelcommand:coopadd")) {
			Utils.addChatMessage("§e[NEU] You just entered a malicious looking Co-op add command! If you truly want to add someone to your coop, type §e/coopadd <name>");
			return true;
		}
		return false;
	}
}
