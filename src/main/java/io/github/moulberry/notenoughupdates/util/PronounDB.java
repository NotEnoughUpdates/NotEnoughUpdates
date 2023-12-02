/*
 * Copyright (C) 2022-2023 NotEnoughUpdates contributors
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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import lombok.Getter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class PronounDB {
	/**
	 * Returns an Optional, since JVMs can be *very* funky with KeyStore loading
	 */
	public static CompletableFuture<Optional<JsonObject>> performPronouning(String platform, String id) {
		if (isDisabled()) return CompletableFuture.completedFuture(Optional.empty());
		return NotEnoughUpdates.INSTANCE.manager.apiUtils
			.request().url("https://pronoundb.org/api/v2/lookup")
			.queryArgument("platform", platform).queryArgument("ids", id)
			.requestJson()
			.handle((result, ex) -> Optional.ofNullable(result));
	}

	private static boolean isDisabled() {
		JsonObject disabled = Constants.DISABLE;
		return disabled != null && disabled.has("pronoundb");
	}

	public static CompletableFuture<Optional<PronounChoice>> getPronounsFor(String platform, String name) {
		return performPronouning(platform, name).thenApply(it -> it.flatMap(jsonObject -> parsePronouns(jsonObject, name)));
	}

	public static Optional<PronounChoice> parsePronouns(JsonObject pronounObject, String name) {
		if (pronounObject.has(name)) {

			List<String> set = JsonUtils
				.transformJsonArrayToList(Utils
					.getElementOrDefault(pronounObject, name + ".sets.en", new JsonArray())
					.getAsJsonArray(), JsonElement::getAsString);

			List<Pronoun> pronouns = set
				.stream()
				.map(pronounId -> Arrays
					.stream(Pronoun.values())
					.filter(pronoun -> pronoun.id.equals(pronounId))
					.findFirst()
					.orElse(null))
				.filter(Objects::nonNull)
				.collect(Collectors.toList());

			if (pronouns.isEmpty()) {
				return Optional.empty();
			}

			if (pronouns.size() >= 2) {
				return Optional.of(new PronounChoice(pronouns.get(0), pronouns.get(1)));
			} else {
				return Optional.of(new PronounChoice(pronouns.get(0), null));

			}
		}
		return Optional.empty();
	}

	@Getter
	public enum Pronoun {
		HE("he", "him", "his"),
		IT("it", "it", "its"),
		SHE("she", "her", "hers"),
		THEY("they", "them", "theirs"),
		ANY("any", "Any pronouns"),
		OTHER("other", "Other pronouns"),
		ASK("ask", "Ask me my pronouns"),
		AVOID("avoid", "Avoid pronouns, use my name");

		private final String id;
		private final String object;
		private final String possessive;
		private final String override;

		Pronoun(String id, String object, String possessive) {
			this.id = id;
			this.object = object;
			this.possessive = possessive;
			this.override = null;
		}

		Pronoun(String id, String override) {
			this.id = id;
			this.override = override;
			this.object = null;
			this.possessive = null;
		}
	}

	@Getter
	public static class PronounChoice {
		private final Pronoun firstPronoun;
		private final Pronoun secondPronoun;

		PronounChoice(Pronoun firstPronoun, Pronoun secondPronoun) {
			this.firstPronoun = firstPronoun;
			this.secondPronoun = secondPronoun;
		}

		public List<String> render() {
			if (firstPronoun.override != null)
				return Collections.singletonList(firstPronoun.override);

			if (secondPronoun == null) {
				return Collections.singletonList(firstPronoun.id + "/" + firstPronoun.object);
			} else {
				return Collections.singletonList(firstPronoun.id + "/" + secondPronoun.id);
			}
		}
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
