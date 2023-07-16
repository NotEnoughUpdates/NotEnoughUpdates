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

package io.github.moulberry.notenoughupdates.options.customtypes;

import io.github.moulberry.notenoughupdates.util.MinecraftExecutor;
import io.github.moulberry.notenoughupdates.util.NEUDebugLogger;
import net.minecraft.client.Minecraft;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum NEUDebugFlag {
	// NOTE: Removing enum values causes gson to remove all debugFlags on load if any removed value is present
	METAL("Metal Detector Solver"),
	WISHING("Wishing Compass Solver"),
	MAP("Dungeon Map Player Information"),
	SEARCH("SearchString Matches"),
	API_CACHE("Api Cache"),
	ALWAYS_EXPORT_SHOPS("Always export shops even without buy back slot"),
	;

	private final String description;

	NEUDebugFlag(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	public void log(String message) {
			NEUDebugLogger.log(this, message);
	}

	public boolean isSet() {
		return NEUDebugLogger.isFlagEnabled(this);
	}

	public static String getFlagList() {
		return renderFlagInformation(Arrays.asList(values()));
	}

	public static String getEnabledFlags() {
		return renderFlagInformation(Arrays.stream(values())
																			 .filter(NEUDebugFlag::isSet)
																			 .collect(Collectors.toList()));
	}

	public static String renderFlagInformation(List<NEUDebugFlag> flags) {
		int maxNameLength = flags.stream()
														 .mapToInt(it -> it.name().length())
														 .max()
														 .orElse(0);
		return flags.stream()
								.map(it -> (CharSequence) String.format(
									"%-" + maxNameLength + "s" + " - %s",
									it.name(),
									it.getDescription()
								))
								.collect(Collectors.joining("\n"));
	}
}
