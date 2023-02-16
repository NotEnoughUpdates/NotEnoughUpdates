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
import io.github.moulberry.notenoughupdates.util.MinecraftExecutor;
import io.github.moulberry.notenoughupdates.util.PronounDB;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;

import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class PronounsCommand extends ClientCommandBase {
	public PronounsCommand() {
		super("neupronouns");
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "/neupronouns <username> [platform]";
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) throws CommandException {
		switch (args.length) {
			case 1:
				fetchPronouns("minecraft", args[0]);
				break;
			case 2:
				fetchPronouns(args[1], args[0]);
				break;
			default:
				sender.addChatMessage(new ChatComponentText("§4" + getCommandUsage(sender)));
		}
	}

	private void fetchPronouns(String platform, String user) {
		GuiNewChat nc = Minecraft.getMinecraft().ingameGUI.getChatGUI();
		int id = new Random().nextInt();
		nc.printChatMessageWithOptionalDeletion(new ChatComponentText("§e[NEU] Fetching Pronouns..."), id);

		CompletableFuture<Optional<PronounDB.PronounChoice>> pronouns;
		if ("minecraft".equals(platform)) {
			CompletableFuture<UUID> c = new CompletableFuture<>();
			NotEnoughUpdates.profileViewer.getPlayerUUID(user, uuidString -> {
				if (uuidString == null) {
					c.completeExceptionally(new NullPointerException());
				} else {
					c.complete(Utils.parseDashlessUUID(uuidString));
				}
			});
			pronouns = c.thenCompose(PronounDB::getPronounsFor);
		} else {
			pronouns = PronounDB.getPronounsFor(platform, user);
		}
		pronouns.handleAsync((pronounChoice, throwable) -> {
			if (throwable != null || !pronounChoice.isPresent()) {
				nc.printChatMessageWithOptionalDeletion(new ChatComponentText("§e[NEU] §4Failed to fetch pronouns."), id);
				return null;
			}
			PronounDB.PronounChoice betterPronounChoice = pronounChoice.get();
			nc.printChatMessageWithOptionalDeletion(new ChatComponentText(
				"§e[NEU] Pronouns for §b" + user + " §eon §b" + platform + "§e:"), id);
			betterPronounChoice.render().forEach(it -> nc.printChatMessage(new ChatComponentText("§e[NEU] §a" + it)));
			return null;
		}, MinecraftExecutor.OnThread);

	}
}
