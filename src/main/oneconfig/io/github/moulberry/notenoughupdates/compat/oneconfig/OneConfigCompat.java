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

import cc.polyfrost.oneconfig.config.data.Mod;
import cc.polyfrost.oneconfig.config.data.ModType;
import io.github.moulberry.notenoughupdates.core.config.Config;
import io.github.moulberry.notenoughupdates.oneconfig.IOneConfigCompat;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.ArrayList;

public class OneConfigCompat extends IOneConfigCompat {

	public static ArrayList<Mod> getModList() {
		try {
			Class<?> oneClass = Class.forName("cc.polyfrost.oneconfig.internal.config.core.ConfigCore");
			Field mods = oneClass.getDeclaredField("mods");
			//noinspection unchecked
			return (ArrayList<Mod>) mods.get(null);
		} catch (ClassNotFoundException | IllegalAccessException | ClassCastException | SecurityException |
						 NoSuchFieldException e) {
			System.err.println("Failed to load ConfigCore.mods. Bug ");
			return new ArrayList<>();
		}
	}

	Mod mod = new Mod("NotEnoughUpdates", ModType.SKYBLOCK /*, TODO: icon loading*/);

	OneMoulConfig omc;

	@Override
	public void initConfig(Config moulConfig, Runnable saveCallback) {
		omc = new OneMoulConfig(mod, moulConfig, saveCallback);
	}
}
