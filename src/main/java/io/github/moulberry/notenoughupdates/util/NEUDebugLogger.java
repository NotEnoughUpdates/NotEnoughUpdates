package io.github.moulberry.notenoughupdates.util;

import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.options.customtypes.NEUDebugFlag;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import java.util.Collections;
import java.util.EnumSet;
import java.util.function.Consumer;

public class NEUDebugLogger {
	private static final Minecraft mc = Minecraft.getMinecraft();
	public static Consumer<String> logMethod = NEUDebugLogger::chatLogger;

	private static void chatLogger(String message) {
		mc.thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.YELLOW + "[NEU DEBUG] " + message));
	}

	public static boolean isFlagEnabled(NEUDebugFlag flag) {
		return NotEnoughUpdates.INSTANCE.config.hidden.debugFlags.contains(flag);
	}

	public static void log(NEUDebugFlag flag, String message) {
		if (logMethod != null && isFlagEnabled(flag)) {
			logMethod.accept(message);
		}
	}
}
