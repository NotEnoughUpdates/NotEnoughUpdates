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

import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.options.customtypes.NEUDebugFlag;
import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumChatFormatting;

import java.util.function.Consumer;

public class NEUDebugLogger {
	private static final Minecraft mc = Minecraft.getMinecraft();
	public static Consumer<String> logMethod = NEUDebugLogger::chatLogger;
	// Used to prevent accessing NEUConfig in unit tests
	public static boolean allFlagsEnabled = false;

	private static void chatLogger(String message) {
		mc.addScheduledTask(() -> Utils.addChatMessage(EnumChatFormatting.YELLOW + "[NEU DEBUG] " + message));
	}

	public static boolean isFlagEnabled(NEUDebugFlag flag) {
		return NotEnoughUpdates.INSTANCE.config.hidden.debugFlags.contains(flag);
	}

	public static void log(NEUDebugFlag flag, String message) {
		if (logMethod != null && (allFlagsEnabled || isFlagEnabled(flag))) {
			logAlways(message);
		}
	}

	public static void logAlways(String message) {
		if (logMethod != null) {
			logMethod.accept(message);
		}
	}
}
