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
import com.google.gson.JsonParseException;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.core.util.StringUtils;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class PronounDB {

	static SSLContext ctx;

	static {
		try {
			KeyStore ks = KeyStore.getInstance("JKS");
			ks.load(PronounDB.class.getResourceAsStream("/pronoundb.jks"), "pronoundb".toCharArray());
			ctx = SSLContext.getInstance("TLS");
			KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			kmf.init(ks, null);
			tmf.init(ks);
			ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
		} catch (KeyStoreException | NoSuchAlgorithmException | KeyManagementException | UnrecoverableKeyException |
						 IOException | CertificateException e) {
			System.out.println("Failed to load keystore. PronounDB requests will probably not work");
			e.printStackTrace();
		}
	}

	/**
	 * Returns an Optional, since JVMs can be *very* funky with KeyStore loading
	 */
	public static Optional<JsonObject> performPronouning(String platform, String id) {
		try {
			URL url = new URL("https://pronoundb.org/api/v1/lookup" +
				"?platform=" + StringUtils.urlEncode(platform) +
				"&id=" + StringUtils.urlEncode(id));
			HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
			urlConnection.setSSLSocketFactory(ctx.getSocketFactory());
			return Optional.of(NotEnoughUpdates.INSTANCE.manager.gson.fromJson(
				new InputStreamReader(urlConnection.getInputStream()),
				JsonObject.class
			));
		} catch (ClassCastException | IOException | JsonParseException e) {
			System.out.println("Failed to contact PronounDB: " + e);
			return Optional.empty();
		}
	}

	public enum Pronoun {
		HE("he", "him", "his"),
		IT("it", "it", "its"),
		SHE("she", "her", "hers"),
		THEY("they", "they", "theirs");

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

	public static Optional<PronounChoice> getPronounsFor(UUID minecraftPlayer) {
		return performPronouning("minecraft", minecraftPlayer.toString() /* dashed UUID */)
			.flatMap(PronounDB::parsePronouns);
	}

	public static void test() {
		try {
			System.out.println("Pronouning...");
			PronounChoice pronounsFor = getPronounsFor(UUID.fromString("842204e6-6880-487b-ae5a-0595394f9948")).get();
			pronounsFor.render().forEach(System.out::println);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
