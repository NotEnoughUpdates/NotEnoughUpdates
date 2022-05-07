package io.github.moulberry.notenoughupdates.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import io.github.moulberry.notenoughupdates.events.RepositoryReloadEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.lang.reflect.Type;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;

public class Constants {
	private static class PatternSerializer implements JsonDeserializer<Pattern>, JsonSerializer<Pattern> {
		@Override
		public Pattern deserialize(
			JsonElement json,
			Type typeOfT,
			JsonDeserializationContext context
		) throws JsonParseException {
			return Pattern.compile(json.getAsString());
		}

		@Override
		public JsonElement serialize(Pattern src, Type typeOfSrc, JsonSerializationContext context) {
			return new JsonPrimitive(src.pattern());
		}
	}

	private static final Gson gson = new GsonBuilder()
		.setPrettyPrinting()
		.registerTypeAdapter(Pattern.class, new PatternSerializer())
		.create();

	public static JsonObject BONUSES;
	public static JsonObject DISABLE;
	public static JsonObject ENCHANTS;
	public static JsonObject LEVELING;
	public static JsonObject MISC;
	public static JsonObject PETNUMS;
	public static JsonObject PETS;
	public static JsonObject PARENTS;
	public static JsonObject ESSENCECOSTS;
	public static JsonObject FAIRYSOULS;
	public static JsonObject REFORGESTONES;

	private static final ReentrantLock lock = new ReentrantLock();

	@SubscribeEvent
	public void reload(RepositoryReloadEvent event) {
		try {
			lock.lock();

			BONUSES = Utils.getConstant("bonuses", gson);
			DISABLE = Utils.getConstant("disable", gson);
			ENCHANTS = Utils.getConstant("enchants", gson);
			LEVELING = Utils.getConstant("leveling", gson);
			MISC = Utils.getConstant("misc", gson);
			PETNUMS = Utils.getConstant("petnums", gson);
			PETS = Utils.getConstant("pets", gson);
			PARENTS = Utils.getConstant("parents", gson);
			ESSENCECOSTS = Utils.getConstant("essencecosts", gson);
			FAIRYSOULS = Utils.getConstant("fairy_souls", gson);
			REFORGESTONES = Utils.getConstant("reforgestones", gson);
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			lock.unlock();
		}
	}
}
