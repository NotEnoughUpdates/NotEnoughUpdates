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

package io.github.moulberry.notenoughupdates.commands.dev;

import io.github.moulberry.notenoughupdates.commands.ClientCommandBase;
import io.github.moulberry.notenoughupdates.miscfeatures.NullzeeSphere;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

public class NullzeeSphereCommand extends ClientCommandBase {

	public NullzeeSphereCommand() {
		super("neuzeesphere");
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) throws CommandException {
		if (args.length != 1) {
			sender.addChatMessage(new ChatComponentText(
				EnumChatFormatting.RED + "Usage: /neuzeesphere [on/off] or /neuzeesphere (radius) or /neuzeesphere setCenter"));
			return;
		}
		if (args[0].equalsIgnoreCase("on")) {
			NullzeeSphere.enabled = true;
		} else if (args[0].equalsIgnoreCase("off")) {
			NullzeeSphere.enabled = false;
		} else if (args[0].equalsIgnoreCase("setCenter")) {
			EntityPlayerSP p = ((EntityPlayerSP) sender);
			NullzeeSphere.centerPos = new BlockPos(p.posX, p.posY, p.posZ);
			NullzeeSphere.overlayVBO = null;
		} else {
			try {
				NullzeeSphere.size = Float.parseFloat(args[0]);
				NullzeeSphere.overlayVBO = null;
			} catch (Exception e) {
				sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Can't parse radius: " + args[0]));
			}
		}
	}
}
