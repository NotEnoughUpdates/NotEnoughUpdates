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
import io.github.moulberry.notenoughupdates.commands.ClientCommandBase;
import io.github.moulberry.notenoughupdates.util.NotificationHandler;
import net.minecraft.command.ICommandSender;

public class StorageViewerWhyCommand extends ClientCommandBase {

	public StorageViewerWhyCommand() {
		super("neustwhy");
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) {
		NotificationHandler.displayNotification(Lists.newArrayList(
			"\u00a7eStorage Viewer",
			"\u00a77Currently, the storage viewer requires you to click twice",
			"\u00a77in order to switch between pages. This is because Hypixel",
			"\u00a77has not yet added a shortcut command to go to any enderchest/",
			"\u00a77storage page.",
			"\u00a77While it is possible to send the second click",
			"\u00a77automatically, doing so violates Hypixel's new mod rules."
		), true);
	}
}
