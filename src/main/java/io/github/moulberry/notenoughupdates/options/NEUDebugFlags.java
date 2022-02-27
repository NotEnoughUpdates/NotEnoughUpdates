package io.github.moulberry.notenoughupdates.options;

import io.github.moulberry.notenoughupdates.NotEnoughUpdates;

public class NEUDebugFlags {
	public static final int METAL_DETECTOR = 1;

	public static boolean IsSet(int flag) {
		return (NotEnoughUpdates.INSTANCE.config.hidden.debugFlags & flag) == flag;
	}
}
