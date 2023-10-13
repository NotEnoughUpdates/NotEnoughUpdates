/*
 * Copyright (C) 2023 NotEnoughUpdates contributors
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

package io.github.moulberry.notenoughupdates.core.config;

import com.google.gson.Gson;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class ConfigUtil {
	public static <T> @Nullable T loadConfig(Class<T> configClass, File file, Gson gson) {
		return loadConfig(configClass, file, gson, false);
	}

	public static <T> @Nullable T loadConfig(Class<T> configClass, File file, Gson gson, boolean useGzip) {
		if (!file.exists()) return null;
		try (
			BufferedReader reader = useGzip ?
				new BufferedReader(new InputStreamReader(
					new GZIPInputStream(Files.newInputStream(file.toPath())),
					StandardCharsets.UTF_8
				)) :
				new BufferedReader(new InputStreamReader(Files.newInputStream(file.toPath()), StandardCharsets.UTF_8))
		) {
			return gson.fromJson(reader, configClass);
		} catch (Exception e) {
			new RuntimeException(
				"Invalid config file '" + file + "'. This will reset the config to default",
				e
			).printStackTrace();
			try {
				// Try to save a version of the corrupted config for debugging purposes
				Files.copy(
					file.toPath(),
					new File(file.getParent(), file.getName() + ".corrupted").toPath(),
					StandardCopyOption.REPLACE_EXISTING
				);
			} catch (Exception ignored) {
			}
		}
		return null;
	}

	public static void saveConfig(Object config, File file, Gson gson) {
		saveConfig(config, file, gson, false);
	}

	public static void saveConfig(Object config, File file, Gson gson, boolean useGzip) {
		File tempFile = new File(file.getParent(), file.getName() + ".temp");
		try {
			tempFile.createNewFile();
			try (
				BufferedWriter writer = useGzip ?
					new BufferedWriter(new OutputStreamWriter(
						new GZIPOutputStream(Files.newOutputStream(tempFile.toPath())),
						StandardCharsets.UTF_8
					)) :
					new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(tempFile.toPath()), StandardCharsets.UTF_8))
			) {
				writer.write(gson.toJson(config));
			}

			try {
				Files.move(tempFile.toPath(), file.toPath(), StandardCopyOption.ATOMIC_MOVE);
			} catch (IOException e) {
				// If atomic move fails it could be because it isn't supported or because the implementation of it
				// doesn't overwrite the old file, in this case we will try a normal move.
				Files.move(tempFile.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
			}
		} catch (Exception e) {
			e.printStackTrace();
			tempFile.delete();
		}
	}
}
