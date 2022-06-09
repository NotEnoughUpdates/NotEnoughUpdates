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
import com.google.gson.JsonObject;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;

import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * @author ThatGravyBoat
 */
public class HastebinUploader {
	private static final String UPLOAD_URL = "https://hst.sh/documents";
	private static final String RETURN_URL = "https://hst.sh/";
	private static final String RAW_RETURN_URL = "https://hst.sh/raw/";

	private static final Gson GSON = new Gson();

	/**
	 * @param data the data you want to upload
	 * @param mode the mode in which the thing should return NORMAL = returns the url, RAW = returns the raw url, NO_URL = returns the slug.
	 * @return The url if mode is NORMAL OR RAW, the slug if mode is NO_URL, and null if an error occurred.
	 */
	public static String upload(String data, Mode mode) {
		try {
			byte[] bytes = data.getBytes(StandardCharsets.UTF_8);
			HttpURLConnection connection = (HttpURLConnection) new URL(UPLOAD_URL).openConnection();
			connection.setRequestMethod("POST");
			connection.addRequestProperty(
				"User-Agent",
				"Minecraft Mod (" + NotEnoughUpdates.MODID + "/" + NotEnoughUpdates.VERSION + ")"
			);
			connection.addRequestProperty("Content-Length", String.valueOf(bytes.length));
			connection.setReadTimeout(15000);
			connection.setConnectTimeout(15000);
			connection.setDoOutput(true);
			connection.getOutputStream().write(bytes);
			final JsonObject json =
				GSON.fromJson(IOUtils.toString(connection.getInputStream(), Charsets.UTF_8), JsonObject.class);
			if (!json.has("key")) return null;
			final String key = json.get("key").getAsString();
			switch (mode) {
				case RAW:
					return RAW_RETURN_URL + key;
				case NORMAL:
					return RETURN_URL + key;
				case NO_URL:
					return key;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public enum Mode {
		NORMAL,
		RAW,
		NO_URL
	}
}
