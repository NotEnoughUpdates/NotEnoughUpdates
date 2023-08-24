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

package io.github.moulberry.notenoughupdates.oneconfig;

import io.github.moulberry.moulconfig.Config;
import net.minecraftforge.fml.common.Loader;

import java.util.Optional;

public abstract class IOneConfigCompat {
	private static final Object sentinelFailure = new Object();
	private static final Object lock = new Object();
	private static Object INSTANCE = null;

	public static Optional<IOneConfigCompat> getInstance() {
		if (INSTANCE == null && Loader.isModLoaded("oneconfig")) {
			synchronized (lock) {
				if (INSTANCE == null)
					try {
						Class<?> aClass = Class.forName("io.github.moulberry.notenoughupdates.compat.oneconfig.OneConfigCompat");
						INSTANCE = aClass.newInstance();
					} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
						System.err.println("Critical failure in OneConfigCompat initialization");
						e.printStackTrace();
						INSTANCE = sentinelFailure;
					}
			}
		}
		if (INSTANCE == sentinelFailure) return Optional.empty();
		return Optional.ofNullable((IOneConfigCompat) INSTANCE);
	}

	public abstract void initConfig(Config moulConfig, Runnable saveCallback);

}
