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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class PronounDB {

	private static boolean isDisabled() {
		JsonObject disabled = Constants.DISABLE;
		return disabled != null && disabled.has("pronoundb");
	}

	/**
	 * Returns an Optional, since JVMs can be *very* funky with KeyStore loading
	 */
	public static CompletableFuture<Optional<JsonObject>> performPronouning(String platform, String id) {
		if (isDisabled()) return CompletableFuture.completedFuture(Optional.empty());
		return NotEnoughUpdates.INSTANCE.manager.apiUtils
			.request()
			.url("https://pronoundb.org/api/v1/lookup")
			.queryArgument("platform", platform)
			.queryArgument("id", id)
			.requestJson()
			.handle((result, ex) -> Optional.ofNullable(result));
	}

	public enum Pronoun {
		HE("he", "him", "his"),
		IT("it", "it", "its"),
		SHE("she", "her", "hers"),
		THEY("they", "them", "theirs");

		private final String subject;
		private final String object;
		private final String possessive;

		Pronoun(String subject, String object, String possessive) {
			this.subject = subject;
			this.object = object;
			this.possessive = possessive;
		}

		public String getSubject() {
			return subject;
		}

		public String getObject() {
			return object;
		}

		public String getPossessive() {
			return possessive;
		}
	}

	public enum PronounChoice {
		UNSPECIFIED("unspecified", "Unspecified"),
		HE("hh", Pronoun.HE),
		HEIT("hi", Pronoun.HE, Pronoun.IT),
		HESHE("hs", Pronoun.HE, Pronoun.SHE),
		HETHEY("ht", Pronoun.HE, Pronoun.THEY),
		ITHE("ih", Pronoun.IT, Pronoun.HE),
		IT("ii", Pronoun.IT),
		ITSHE("is", Pronoun.IT, Pronoun.SHE),
		ITTHEY("it", Pronoun.IT, Pronoun.THEY),
		SHEHE("shh", Pronoun.SHE, Pronoun.HE),
		SHE("sh", Pronoun.SHE),
		SHEIT("si", Pronoun.SHE, Pronoun.IT),
		SHETHEY("st", Pronoun.SHE, Pronoun.THEY),
		THEYHE("th", Pronoun.THEY, Pronoun.HE),
		THEYIT("ti", Pronoun.THEY, Pronoun.IT),
		THEYSHE("ts", Pronoun.THEY, Pronoun.SHE),
		THEY("tt", Pronoun.THEY),
		ANY("any", "Any pronouns"),
		OTHER("other", "Other pronouns"),
		ASK("ask", "Ask me my pronouns"),
		AVOID("avoid", "Avoid pronouns, use my name");
		private final String id;
		private List<Pronoun> pronouns = null;
		private String override = null;

		PronounChoice(String id, String override) {
			this.override = override;
			this.id = id;
		}

		PronounChoice(String id, Pronoun... pronouns) {
			this.id = id;
			this.pronouns = Arrays.asList(pronouns);
		}

		public static Optional<PronounChoice> findPronounsForId(String id) {
			for (PronounChoice value : values()) {
				if (value.id.equals(id)) return Optional.of(value);
			}
			return Optional.empty();
		}

		public String getOverride() {
			return override;
		}

		public List<Pronoun> getPronounsInPreferredOrder() {
			return pronouns;
		}

		public String getId() {
			return id;
		}

		public List<String> render() {
			if (override != null)
				return Arrays.asList(override);
			return pronouns
				.stream()
				.map(pronoun -> pronoun.getSubject() + "/" + pronoun.getObject())
				.collect(Collectors.toList());
		}

		public boolean isConsciousChoice() {
			return this != UNSPECIFIED;
		}

	}

	public static Optional<PronounChoice> parsePronouns(JsonObject pronounObject) {
		if (pronounObject.has("pronouns")) {
			JsonElement pronouns = pronounObject.get("pronouns");
			if (pronouns.isJsonPrimitive() && pronouns.getAsJsonPrimitive().isString())
				return PronounChoice.findPronounsForId(pronouns.getAsString());
		}
		return Optional.empty();
	}

	public static CompletableFuture<Optional<PronounChoice>> getPronounsFor(String platform, String name) {
		return performPronouning(platform, name).thenApply(it -> it.flatMap(PronounDB::parsePronouns));
	}

	public static CompletableFuture<Optional<PronounChoice>> getPronounsFor(UUID minecraftPlayer) {
		return getPronounsFor("minecraft", minecraftPlayer.toString() /* dashed UUID */);
	}

	public static void test() {
		System.out.println("Pronouning...");
		getPronounsFor(UUID.fromString("842204e6-6880-487b-ae5a-0595394f9948")).thenAccept(it -> {
			PronounChoice pronounsFor = it.get();
			pronounsFor.render().forEach(System.out::println);
		});
	}

}
