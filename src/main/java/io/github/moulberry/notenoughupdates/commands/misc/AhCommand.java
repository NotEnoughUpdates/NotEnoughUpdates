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
import io.github.moulberry.notenoughupdates.auction.CustomAHGui;
import io.github.moulberry.notenoughupdates.commands.ClientCommandBase;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import org.apache.commons.lang3.StringUtils;

public class AhCommand extends ClientCommandBase {

	public AhCommand() {
		super("neuah");
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) throws CommandException {
		if (!NotEnoughUpdates.INSTANCE.hasSkyblockScoreboard()) {
			Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.RED +
				"You must be on Skyblock to use this feature."));
		} else if (NotEnoughUpdates.INSTANCE.config.apiData.apiKey == null ||
			NotEnoughUpdates.INSTANCE.config.apiData.apiKey.trim().isEmpty()) {
			Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.RED +
				"Can't open NeuAH, apikey is not set. Run /api new and put the result in settings."));
		} else {
			NotEnoughUpdates.INSTANCE.openGui = new CustomAHGui();
			NotEnoughUpdates.INSTANCE.manager.auctionManager.customAH.lastOpen = System.currentTimeMillis();
			NotEnoughUpdates.INSTANCE.manager.auctionManager.customAH.clearSearch();
			NotEnoughUpdates.INSTANCE.manager.auctionManager.customAH.updateSearch();

			if (args.length > 0)
				NotEnoughUpdates.INSTANCE.manager.auctionManager.customAH.setSearch(StringUtils.join(args, " "));
		}
	}
}
