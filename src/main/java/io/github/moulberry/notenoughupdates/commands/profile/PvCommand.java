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

package io.github.moulberry.notenoughupdates.commands.profile;

import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import net.minecraft.client.Minecraft;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PvCommand extends ViewProfileCommand {

	public PvCommand() {
		super("pv");
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) {
		if (!NotEnoughUpdates.INSTANCE.isOnSkyblock()) {
			Minecraft.getMinecraft().thePlayer.sendChatMessage("/pv " + StringUtils.join(args, " "));
		} else {
			super.processCommand(sender, args);
		}
	}

	@Override
	public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
		if (args.length != 1) return null;

		String lastArg = args[args.length - 1];
		List<String> playerMatches = new ArrayList<>();
		for (EntityPlayer player : Minecraft.getMinecraft().theWorld.playerEntities) {
			String playerName = player.getName();
			if (playerName.toLowerCase().startsWith(lastArg.toLowerCase())) {
				playerMatches.add(playerName);
			}
		}
		return playerMatches;
	}
}
