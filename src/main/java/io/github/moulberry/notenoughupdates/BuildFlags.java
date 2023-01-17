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

package io.github.moulberry.notenoughupdates;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * For storage of compile-time configuration flags
 */
public class BuildFlags {

	private static final Properties properties = new Properties();

	static {
		try {
			properties.load(BuildFlags.class.getResourceAsStream("/buildflags.properties"));
		} catch (IOException | NullPointerException e) {
			System.out.println("Failed to load build properties: " + e);
		}
	}

	private static boolean getBuildFlag(String flag) {
		return Boolean.parseBoolean(properties.getProperty("neu.buildflags." + flag));
	}

	public static Map<String, Boolean> getAllFlags() {
		return Holder.ALL_FLAGS;
	}

	public static final boolean ENABLE_PRONOUNS_IN_PV_BY_DEFAULT = getBuildFlag("pronouns");
	public static final boolean ENABLE_ONECONFIG_COMPAT_LAYER = getBuildFlag("oneconfig");

	private static class Holder {
		static Map<String, Boolean> ALL_FLAGS = new HashMap<>();

		static {
			for (Field declaredField : BuildFlags.class.getDeclaredFields()) {
				if (Boolean.TYPE.equals(declaredField.getType())
					&& (declaredField.getModifiers() & Modifier.STATIC) != 0) {
					try {
						declaredField.setAccessible(true);
						ALL_FLAGS.put(declaredField.getName(), (Boolean) declaredField.get(null));
					} catch (IllegalAccessException | ClassCastException | SecurityException e) {
						System.err.println("Failed to access BuildFlag: " + e);
					}
				}
			}
		}
	}

}
