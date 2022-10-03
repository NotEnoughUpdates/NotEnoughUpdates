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

package io.github.moulberry.notenoughupdates.miscfeatures.killswitch;

import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import net.minecraftforge.fml.common.FMLCommonHandler;

import javax.swing.*;

public class CrashDirective
	implements KillswitchDirective {
	public final String message;

	public CrashDirective(String message) {this.message = message;}

	@Override
	public boolean matches() {
		JOptionPane.showMessageDialog(null, message, "NEU Shutdown", JOptionPane.ERROR_MESSAGE);
		NotEnoughUpdates.LOGGER.fatal("NEU has to crash due to a killswitch: " + message);
		FMLCommonHandler.instance().exitJava(1, false);
		System.exit(1);
		return false;
	}
}
