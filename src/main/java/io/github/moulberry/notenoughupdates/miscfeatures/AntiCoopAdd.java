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
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.init.Items;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

public class AntiCoopAdd {

	public static boolean onMouseClick(Slot slotIn, int slotId, int clickedButton, int clickType) {
		if (!NotEnoughUpdates.INSTANCE.config.misc.coopWarning) return false;
		if (slotId == -999) return false;
		if (!Utils.getOpenChestName().contains("Profile")) return false;

		GuiChest chest = (GuiChest) Minecraft.getMinecraft().currentScreen;

		ItemStack stack = chest.inventorySlots.getSlot(slotId).getStack();
		if (stack == null) return false;
		if (stack.getItem() == Items.diamond && stack.getDisplayName() != null && stack.getDisplayName().contains("Co-op Request")) {
			String ign = Utils.getOpenChestName().split("'s Profile")[0];
			ChatComponentText storageMessage = new ChatComponentText(
				EnumChatFormatting.YELLOW + "[NEU] " + EnumChatFormatting.YELLOW +
					"You just clicked on the Co-op add button. If you want to coop add this person, click this chat message");
			storageMessage.setChatStyle(Utils.createClickStyle(ClickEvent.Action.RUN_COMMAND, "/coopadd " + ign));
			storageMessage.setChatStyle(storageMessage.getChatStyle().setChatHoverEvent(
				new HoverEvent(HoverEvent.Action.SHOW_TEXT,
					new ChatComponentText(EnumChatFormatting.YELLOW + "Click to add " + ign + " to your coop"))));
			ChatComponentText storageChatMessage = new ChatComponentText("");
			storageChatMessage.appendSibling(storageMessage);
			Minecraft.getMinecraft().thePlayer.addChatMessage(storageChatMessage);
			return true;
		}

		return false;
	}
}
