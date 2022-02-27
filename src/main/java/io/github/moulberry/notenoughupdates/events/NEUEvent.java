package io.github.moulberry.notenoughupdates.events;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event;

public class NEUEvent extends Event {
	public boolean post() {
		MinecraftForge.EVENT_BUS.post(this);
		return isCancelable() && isCanceled();
	}
}
