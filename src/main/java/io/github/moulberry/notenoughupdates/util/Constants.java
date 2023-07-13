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
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.autosubscribe.NEUAutoSubscribe;
import io.github.moulberry.notenoughupdates.events.RepositoryReloadEvent;
import io.github.moulberry.notenoughupdates.recipes.EssenceUpgrades;
import io.github.moulberry.notenoughupdates.recipes.NeuRecipe;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;

@NEUAutoSubscribe
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
	public static JsonObject TROPHYFISH;
	public static JsonObject WEIGHT;
	public static JsonObject RNGSCORE;
	public static JsonObject ABIPHONE;
	public static JsonObject ESSENCESHOPS;
	public static JsonObject SBLEVELS;
	public static JsonObject MUSEUM;

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
			TROPHYFISH = Utils.getConstant("trophyfish", gson);
			WEIGHT = Utils.getConstant("weight", gson);
			RNGSCORE = Utils.getConstant("rngscore", gson);
			ABIPHONE = Utils.getConstant("abiphone", gson);
			ESSENCESHOPS = Utils.getConstant("essenceshops", gson);
			SBLEVELS = Utils.getConstant("sblevels", gson);
			MUSEUM = Utils.getConstant("museum", gson);

			parseEssenceCosts();
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			lock.unlock();
		}
	}

	public void parseEssenceCosts() {
		for (Map.Entry<String, JsonElement> entry : ESSENCECOSTS.entrySet()) {
			NeuRecipe parsed = EssenceUpgrades.parseFromEssenceCostEntry(entry);
			if (parsed != null) {
				NotEnoughUpdates.INSTANCE.manager.registerNeuRecipe(parsed);
			} else {
				System.out.println("NULL for: " + entry);
			}
		}
	}
}
