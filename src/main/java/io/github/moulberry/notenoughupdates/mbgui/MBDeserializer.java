package io.github.moulberry.notenoughupdates.mbgui;

import com.google.gson.JsonObject;

import java.io.IOException;

public class MBDeserializer {
	public static MBGuiElement deserialize(JsonObject json) {
		return null;
	}

	public static void serializeAndSave(MBGuiElement element, String filename) throws IOException {
        /*JsonObject json = element.serialize();

        File file = new File(NotEnoughUpdates.INSTANCE.manager.configLocation, filename+".json");
        NotEnoughUpdates.INSTANCE.manager.writeJson(json, file);*/
	}
}
