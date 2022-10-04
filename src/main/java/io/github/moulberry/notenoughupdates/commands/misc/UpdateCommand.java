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
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class UpdateCommand extends ClientCommandBase {
	NotEnoughUpdates neu;

	public UpdateCommand(NotEnoughUpdates neu) {
		super("neuupdate");
		this.neu = neu;
	}

	@Override
	public List<String> getCommandAliases() {
		return Arrays.asList("neuupdates", "enoughupdates");
	}

	public void displayHelp(ICommandSender sender) {
		sender.addChatMessage(new ChatComponentText(
			"" +
				"§e[NEU] §b/neuupdate help - View help.\n" +
				"§e[NEU] §b/neuupdate check - Check for updates.\n" +
				"§e[NEU] §b/neuupdate url <url> - Load an update from an direct download URL.\n" +
				"       §cONLY DO THIS WITH TRUSTED URLS OR IT MIGHT RESULT IN A RAT!\n" +
				"§e[NEU] §b/neuupdate fromartifact <url> - Load an update from an artifact.\n" +
				"       §cIf you don't know what this is, don't use it.\n" +
				""
		));

	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) throws CommandException {
		if ((args.length == 1 && Objects.equals(args[0], "help")) || args.length == 0) {
			displayHelp(sender);
			return;
		}
		switch (args[0].toLowerCase().intern()) {
			case "check":
				neu.autoUpdater.displayUpdateMessageIfOutOfDate();
				break;
			case "url":
				if (args.length != 2) {
					sender.addChatMessage(new ChatComponentText("§e[NEU] §cPlease provide a URL"));
				}
				URL url;
				try {
					url = new URL(args[1]);
				} catch (MalformedURLException e) {
					e.printStackTrace();
					sender.addChatMessage(new ChatComponentText("§e[NEU] §cInvalid URL"));
					return;
				}
				neu.autoUpdater.updateFromURL(url);
				break;
			case "scheduledownload":
				neu.autoUpdater.scheduleDownload();
				break;
			case "updatemodes":
				sender.addChatMessage(new ChatComponentText("§e[NEU] §bTo ensure we do not accidentally corrupt your mod folder, we can only offer support for auto-updates on system with certain capabilities for file deletions (specifically unix systems). You can still manually update your files"));
				break;
			default:
				displayHelp(sender);

		}
	}
}
