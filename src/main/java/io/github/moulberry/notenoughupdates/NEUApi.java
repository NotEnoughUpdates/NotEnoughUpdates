package io.github.moulberry.notenoughupdates;

import net.minecraftforge.fml.relauncher.ReflectionHelper;

public class NEUApi {
	public static boolean disableInventoryButtons = false;

	public static void setInventoryButtonsToDisabled() {
		disableInventoryButtons = true;
	}
}
