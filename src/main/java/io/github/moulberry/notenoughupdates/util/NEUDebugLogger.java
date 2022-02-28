package io.github.moulberry.notenoughupdates.util;

import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import org.apache.commons.lang3.BitField;

import java.util.function.Consumer;

public class NEUDebugLogger {
	private static final Minecraft mc = Minecraft.getMinecraft();
	public static final BitField METAL_DETECTOR_FLAG = new BitField(0x1);

	public static Consumer<String> logMethod = NEUDebugLogger::chatLogger;

	private static void chatLogger(String message) {
		mc.thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.AQUA + "[NEU DEBUG] " + message));
	}

	public static boolean checkFlags(BitField flags) {
		return NotEnoughUpdates.INSTANCE.config.hidden.debugFlags.isSet(flags);
	}

	public static void log(BitField flags, String message) {
		if (logMethod != null && checkFlags(flags)) {
			logMethod.accept(message);
		}
	}
}
