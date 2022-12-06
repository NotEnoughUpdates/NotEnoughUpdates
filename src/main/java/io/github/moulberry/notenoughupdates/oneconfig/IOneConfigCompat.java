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

import io.github.moulberry.notenoughupdates.BuildFlags;
import io.github.moulberry.notenoughupdates.core.config.Config;

import java.util.Optional;

public abstract class IOneConfigCompat {
	private static IOneConfigCompat INSTANCE = null;
	private static final Object lock = new Object();

	public static Optional<IOneConfigCompat> getInstance() {
		if (BuildFlags.ENABLE_ONECONFIG_COMPAT_LAYER && INSTANCE == null) {
			synchronized (lock) {
				if (INSTANCE == null)
					try {
						Class<?> aClass = Class.forName("io.github.moulberry.notenoughupdates.compat.oneconfig.OneConfigCompat");
						INSTANCE = (IOneConfigCompat) aClass.newInstance();
					} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
						System.err.println("Critical failure in OneConfigCompat initialization");
						e.printStackTrace();
					}
			}
		}

		return Optional.ofNullable(INSTANCE);
	}

	public abstract void initConfig(Config moulConfig, Runnable saveCallback);

}
