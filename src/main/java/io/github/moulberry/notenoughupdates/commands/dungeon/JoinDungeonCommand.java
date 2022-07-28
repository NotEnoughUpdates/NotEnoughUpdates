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

package io.github.moulberry.notenoughupdates.commands.dungeon;

import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.commands.ClientCommandBase;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.EnumChatFormatting;
import org.apache.commons.lang3.StringUtils;

public class JoinDungeonCommand extends ClientCommandBase {

	public JoinDungeonCommand() {
		super("join");
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) throws CommandException {
		if (!NotEnoughUpdates.INSTANCE.hasSkyblockScoreboard()) {
			Minecraft.getMinecraft().thePlayer.sendChatMessage("/join " + StringUtils.join(args, " "));
		} else {
			if (args.length != 1) {
				Utils.addChatMessage(EnumChatFormatting.RED + "Example Usage: /join f7, /join m6 or /join 7");
			} else {
				String cataPrefix = "catacombs";
				if (args[0].startsWith("m")) {
					cataPrefix = "master_catacombs";
				}
				String cmd = "/joindungeon " + cataPrefix + " " + args[0].charAt(args[0].length() - 1);
				Utils.addChatMessage(EnumChatFormatting.YELLOW + "Running command: " + cmd);
				Utils.addChatMessage(EnumChatFormatting.YELLOW +
					"The dungeon should start soon. If it doesn't, make sure you have a party of 5 people");
				Minecraft.getMinecraft().thePlayer.sendChatMessage(cmd);
			}
		}
	}
}
