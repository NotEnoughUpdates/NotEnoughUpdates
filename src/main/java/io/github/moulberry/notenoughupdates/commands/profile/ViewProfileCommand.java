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
import io.github.moulberry.notenoughupdates.commands.ClientCommandBase;
import io.github.moulberry.notenoughupdates.profileviewer.GuiProfileViewer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ViewProfileCommand extends ClientCommandBase {

	public static final Consumer<String[]> RUNNABLE = (args) -> {
		if (!OpenGlHelper.isFramebufferEnabled()) {
			Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.RED +
				"Some parts of the profile viewer do not work with OF Fast Render. Go to ESC > Options > Video Settings > Performance > Fast Render to disable it."));

		}
		if (NotEnoughUpdates.INSTANCE.config.apiData.apiKey == null ||
			NotEnoughUpdates.INSTANCE.config.apiData.apiKey.trim().isEmpty()) {
			Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.RED +
				"Can't view profile, apikey is not set. Run /api new and put the result in settings."));
		} else if (args.length == 0) {
			NotEnoughUpdates.profileViewer.getProfileByName(Minecraft.getMinecraft().thePlayer.getName(), profile -> {
				if (profile == null) {
					Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.RED +
						"Invalid player name/api key. Maybe api is down? Try /api new."));
				} else {
					profile.resetCache();
					NotEnoughUpdates.INSTANCE.openGui = new GuiProfileViewer(profile);
				}
			});
		} else if (args.length > 1) {
			Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.RED +
				"Too many arguments. Usage: /neuprofile [name]"));
		} else {
			NotEnoughUpdates.profileViewer.getProfileByName(args[0], profile -> {
				if (profile == null) {
					Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.RED +
						"Invalid player name/api key. Maybe api is down? Try /api new."));
				} else {
					profile.resetCache();
					NotEnoughUpdates.INSTANCE.openGui = new GuiProfileViewer(profile);
				}
			});
		}
	};

	public ViewProfileCommand() {
		this("neuprofile");
	}

	public ViewProfileCommand(String name) {
		super(name);
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) {
		RUNNABLE.accept(args);
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
