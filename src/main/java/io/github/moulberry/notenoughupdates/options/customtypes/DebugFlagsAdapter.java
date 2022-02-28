package io.github.moulberry.notenoughupdates.options.customtypes;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

public class DebugFlagsAdapter extends TypeAdapter<DebugFlags> {
	public DebugFlags read(JsonReader reader) throws IOException {
		if (reader.peek() == JsonToken.NULL) {
			reader.nextNull();
			return null;
		}
		String value = reader.nextString();
		return new DebugFlags(Integer.decode(value));
	}

	public void write(JsonWriter writer, DebugFlags value) throws IOException {
		if (value == null) {
			writer.nullValue();
			return;
		}
		writer.value(String.format("0x%1$08X", value.getFlags()));
	}
}