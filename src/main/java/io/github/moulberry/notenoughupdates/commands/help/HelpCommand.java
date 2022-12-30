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

package io.github.moulberry.notenoughupdates.commands.help;

import com.google.common.collect.Lists;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.commands.ClientCommandBase;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.command.ICommandSender;

import java.util.ArrayList;

public class HelpCommand extends ClientCommandBase {

	public HelpCommand() {
		super("neuhelp");
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) {
		ArrayList<String> neuHelpMessages = Lists.newArrayList(
			"§5§lNotEnoughUpdates commands",
			"§6/neu §7- Opens the main NEU GUI.",
			"§6/pv §b?{name} §2ⴵ §r§7- Opens the profile viewer",
			"§6/neusouls {on/off/clear/unclear} §r§7- Shows waypoints to fairy souls.",
			"§6/neubuttons §r§7- Opens a GUI which allows you to customize inventory buttons.",
			"§6/neuec §r§7- Opens the enchant colour GUI.",
			"§6/join {floor} §r§7- Short Command to join a Dungeon. §lNeed a Party of 5 People§r§7 {4/f7/m5}.",
			"§6/neucosmetics §r§7- Opens the cosmetic GUI.",
			"§6/neurename §r§7- Opens the NEU Item Customizer.",
			"§6/cata §b?{name} §2ⴵ §r§7- Opens the profile viewer's Catacombs page.",
			"§6/neulinks §r§7- Shows links to NEU/Moulberry.",
			"§6/neuoverlay §r§7- Opens GUI Editor for quickcommands and searchbar.",
			"§6/neuah §r§7- Opens NEU's custom auction house GUI.",
			"§6/neucalendar §r§7- Opens NEU's custom calendar GUI.",
			"§6/neucalc §r§7- Run calculations.",
			"",
			"§6§lOld commands:",
			"§6/peek §b?{user} §2ⴵ §r§7- Shows quick stats for a user.",
			"",
			"§6§lDebug commands:",
			"§6/neustats §r§7- Copies helpful info to the clipboard.",
			"§6/neustats modlist §r§7- Copies mod list info to clipboard.",
			"§6/neuresetrepo §r§7- Deletes all repo files.",
			"§6/neureloadrepo §r§7- Debug command with repo.",
			"",
			"§6§lDev commands:",
			"§6/neupackdev §r§7- pack creator command - getnpc, getmob(s), getarmorstand(s), getall. Optional radius argument for all."
		);
		for (String neuHelpMessage : neuHelpMessages) {
			Utils.addChatMessage(neuHelpMessage);
		}
		if (NotEnoughUpdates.INSTANCE.config.hidden.dev) {
			ArrayList<String> neuDevHelpMessages = Lists.newArrayList(
				"§6/neudevtest §r§7- dev test command",
				"§6/neuzeephere §r§7- sphere",
				"§6/neudungeonwintest §r§7- displays the dungeon win screen"
			);

			for (String neuDevHelpMessage : neuDevHelpMessages) {
				Utils.addChatMessage(neuDevHelpMessage);
			}
		}
		String[] helpInfo = {
			"",
			"§7Commands marked with a §2\"ⴵ\"§7 require an api key. You can set your api key via \"/api new\" or by manually putting it in the api field in \"/neu\"",
			"",
			"§7Arguments marked with a §b\"?\"§7 are optional.",
			"",
			"§6§lScroll up to see everything"
		};

		for (String message : helpInfo) {
			Utils.addChatMessage(message);
		}
	}
}
