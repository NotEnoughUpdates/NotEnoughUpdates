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

import io.github.moulberry.notenoughupdates.commands.ClientCommandBase;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

public class BlahajCommand extends ClientCommandBase {

	public BlahajCommand() {
		super("blahaj");
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) throws CommandException {
		Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.BLUE + "⬛⬛⬛⬛⬛  ⬛⬛\n"+
			EnumChatFormatting.WHITE + "⬛" + EnumChatFormatting.BLACK + "⬛" + EnumChatFormatting.WHITE + "⬛" + EnumChatFormatting.BLUE + "⬛⬛⬛⬛⬛\n"+
			"  " +EnumChatFormatting.WHITE + "⬛⬛⬛⬛" + EnumChatFormatting.BLUE + "⬛⬛\n" +
			"    "+EnumChatFormatting.WHITE + "⬛⬛⬛⬛" + EnumChatFormatting.BLUE + "⬛⬛\n" +
			"  " + EnumChatFormatting.BLUE + "⬛⬛" + "  " + EnumChatFormatting.WHITE + "⬛⬛" + EnumChatFormatting.BLUE + "⬛⬛\n" +
			"          "+EnumChatFormatting.WHITE + "⬛" + EnumChatFormatting.BLUE + "⬛⬛\n" +
			"        "+EnumChatFormatting.WHITE + "⬛" + EnumChatFormatting.BLUE + "⬛⬛\n" +
			"      "+ EnumChatFormatting.BLUE + "⬛" + EnumChatFormatting.WHITE + "⬛"+ EnumChatFormatting.BLUE + "⬛\n" +
			"      " +EnumChatFormatting.BLUE + "⬛⬛\n"));
	}
}
