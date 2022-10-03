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

package io.github.moulberry.notenoughupdates.commands.misc;

import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.commands.ClientCommandBase;
import io.github.moulberry.notenoughupdates.miscgui.GuiItemCustomize;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;

import java.util.Collections;
import java.util.List;

public class CustomizeCommand extends ClientCommandBase {

	public CustomizeCommand() {
		super("neucustomize");
	}

	@Override
	public List<String> getCommandAliases() {
		return Collections.singletonList("neurename");
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) throws CommandException {
		ItemStack held = Minecraft.getMinecraft().thePlayer.getHeldItem();

		if (held == null) {
			sender.addChatMessage(new ChatComponentText("\u00a7cYou can't customize your hand..."));
			return;
		}

		String heldUUID = NotEnoughUpdates.INSTANCE.manager.getUUIDForItem(held);

		if (heldUUID == null) {
			sender.addChatMessage(new ChatComponentText("\u00a7cHeld item does not have UUID, cannot be customized"));
			return;
		}

		NotEnoughUpdates.INSTANCE.openGui = new GuiItemCustomize(held, heldUUID);
	}
}
