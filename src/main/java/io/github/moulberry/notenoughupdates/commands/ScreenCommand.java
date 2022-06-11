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

package io.github.moulberry.notenoughupdates.commands;

import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

public class ScreenCommand extends ClientCommandBase {

	private final ScreenOpener opener;

	protected ScreenCommand(String name, ScreenOpener opener) {
		super(name);
		this.opener = opener;
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) throws CommandException {
		NotEnoughUpdates.INSTANCE.openGui = opener.open();
	}

	@FunctionalInterface
	public interface ScreenOpener {
		GuiScreen open();
	}
}
