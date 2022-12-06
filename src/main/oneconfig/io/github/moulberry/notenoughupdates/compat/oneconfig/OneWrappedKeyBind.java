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

package io.github.moulberry.notenoughupdates.compat.oneconfig;

import cc.polyfrost.oneconfig.config.core.OneKeyBind;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.Arrays;

public class OneWrappedKeyBind extends OneKeyBind {

	public int value = Keyboard.KEY_NONE;

	@Override
	public String getDisplay() {
		keyBinds.clear();
		keyBinds.addAll(getKeyBinds());
		return super.getDisplay();
	}

	@Override
	public void addKey(int key) {
		value = key;
	}

	@Override
	public void clearKeys() {
		value = Keyboard.KEY_NONE;
	}

	@Override
	public int getSize() {
		return getKeyBinds().size();
	}

	@Override
	public ArrayList<Integer> getKeyBinds() {
		return value == Keyboard.KEY_NONE ? new ArrayList<>() : new ArrayList<>(Arrays.asList(value));
	}
}
