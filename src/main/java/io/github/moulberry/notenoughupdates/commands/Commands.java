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

import io.github.moulberry.notenoughupdates.miscgui.GuiEnchantColour;
import io.github.moulberry.notenoughupdates.miscgui.GuiInvButtonEditor;
import io.github.moulberry.notenoughupdates.miscgui.NEUOverlayPlacements;
import net.minecraftforge.client.ClientCommandHandler;

public class Commands {
	public Commands() {

		// Profile Commands

		// Misc Commands
		ClientCommandHandler.instance.registerCommand(new ScreenCommand("neubuttons", GuiInvButtonEditor::new));
		ClientCommandHandler.instance.registerCommand(new ScreenCommand("neuec", GuiEnchantColour::new));
		ClientCommandHandler.instance.registerCommand(new ScreenCommand("neuoverlay", NEUOverlayPlacements::new));
	}
}
