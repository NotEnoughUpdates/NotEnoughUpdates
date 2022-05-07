package io.github.moulberry.notenoughupdates.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class JsonUtils {
	public static Stream<JsonElement> getJsonArrayAsStream(JsonArray array) {
		return StreamSupport.stream(array.spliterator(), false);
	}

	public static <T> List<T> transformJsonArrayToList(
		JsonArray array,
		Function<? super JsonElement, ? extends T> mapper
	) {
		return getJsonArrayAsStream(array).map(mapper).collect(Collectors.toList());
	}

	public static <T> List<T> getJsonArrayOrEmpty(
		JsonObject rootObject,
		String name,
		Function<? super JsonElement, ? extends T> mapper
	) {
		if (!rootObject.has(name)) {
			return Collections.emptyList();
		}
		JsonElement jsonElement = rootObject.get(name);
		if (jsonElement.isJsonArray()) {
			return transformJsonArrayToList(jsonElement.getAsJsonArray(), mapper);
		}
		return Collections.emptyList();
	}

	public static <T> JsonArray transformListToJsonArray(
		List<T> things,
		Function<? super T, ? extends JsonElement> mapper
	) {
		JsonArray array = new JsonArray();
		for (T t : things) {
			array.add(mapper.apply(t));
		}
		return array;
	}

	public static <T> Map<String, T> transformJsonObjectToMap(
		JsonObject object,
		Function<? super JsonElement, ? extends T> mapper
	) {
		Map<String, T> map = new HashMap<>();
		for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
			map.put(entry.getKey(), mapper.apply(entry.getValue()));
		}
		return map;
	}

}
