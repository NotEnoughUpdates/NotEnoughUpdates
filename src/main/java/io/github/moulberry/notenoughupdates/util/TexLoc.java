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

package io.github.moulberry.notenoughupdates.util;

import org.lwjgl.input.Keyboard;

/**
 * Utility used for positioning GUI elements during development.
 */
public class TexLoc {
	public int x;
	public int y;
	private final int toggleKey;
	private boolean toggled;
	private boolean pressedLastTick;
	private boolean dirPressed;

	public TexLoc(int x, int y, int toggleKey) {
		this.x = x;
		this.y = y;
		this.toggleKey = toggleKey;
	}

	public boolean handleKeyboardInput() {
		int mult = 1;
		if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) mult = 10;
		if (Keyboard.isKeyDown(toggleKey)) {
			if (!pressedLastTick) {
				toggled = !toggled;
			}
			pressedLastTick = true;
		} else {
			pressedLastTick = false;
		}
		if (toggled || toggleKey == 0) {
			if (Keyboard.isKeyDown(Keyboard.KEY_LEFT)) {
				if (!dirPressed) x -= mult;
				dirPressed = true;
			} else if (Keyboard.isKeyDown(Keyboard.KEY_RIGHT)) {
				if (!dirPressed) x += mult;
				dirPressed = true;
			} else if (Keyboard.isKeyDown(Keyboard.KEY_UP)) {
				if (!dirPressed) y -= mult;
				dirPressed = true;
			} else if (Keyboard.isKeyDown(Keyboard.KEY_DOWN)) {
				if (!dirPressed) y += mult;
				dirPressed = true;
			} else {
				dirPressed = false;
				return false;
			}
			System.out.println("X: " + x + " ; Y: " + y);
			return true;
		}
		return false;
	}
}
