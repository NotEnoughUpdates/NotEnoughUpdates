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

package io.github.moulberry.notenoughupdates.miscfeatures.killswitch;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.util.MinecraftExecutor;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;

public class Killswitch {
	private static Map<String, Function<List<String>, KillswitchDirective>> directives =
		new HashMap<String, Function<List<String>, KillswitchDirective>>() {{
			put("if_mod_at_least", args -> new ModAtLeastDirective(getJson(args, 0, Integer.class)));
			put("if_mod_at_most", args -> new ModAtMostDirective(getJson(args, 0, Integer.class)));
			put("crash", args -> new CrashDirective(getString(args, 0)));
			put(
				"if_config_equals",
				args -> new IfConfigEqualsDirective(getString(args, 0), getJson(args, 1, JsonElement.class))
			);
			put(
				"set_config",
				args -> new SetConfigDirective(getString(args, 0), getJson(args, 1, JsonElement.class))
			);
			put("comment", args -> new NOPDirective());
			put("feedback", args -> new FeedbackDirective(getString(args, 0)));
		}};

	private static final String KILLSWITCH_URL = System.getProperty(
		"notenoughupdates.killswitchurl",
		"https://raw.githubusercontent.com/NotEnoughUpdates/NotEnoughUpdates-REPO/master/killswitches.txt"
	);

	static final Gson GSON = new GsonBuilder()
		.create();
	private static Timer timer = new Timer(true);

	private static String getKillswitchSource() {
		try {
			URL url = new URL(KILLSWITCH_URL);
			return IOUtils.toString(url, StandardCharsets.UTF_8);
		} catch (Exception e) {
			NotEnoughUpdates.LOGGER.error("Failed to refresh killswitch data", e);
			return "";
		}
	}

	public static volatile List<KillswitchChain> killswitches = new ArrayList<>();

	public static void loadKillswitchData() {
		CompletableFuture
			.supplyAsync(Killswitch::getKillswitchData)
			.thenAcceptAsync(chain -> killswitches = chain, MinecraftExecutor.INSTANCE)
			.thenAccept(x -> runKillswitchChains());
	}

	public static void runKillswitchChains() {
		MinecraftExecutor.INSTANCE.execute(() -> {
			for (KillswitchChain killswitch : killswitches) {
				killswitch.run();
			}
		});
	}

	private static List<KillswitchChain> getKillswitchData() {
		StringReader reader = new StringReader(getKillswitchSource());
		try {
			List<KillswitchChain> list = new ArrayList<>();
			while (true) {
				skipWhitespaces(reader);
				reader.mark(1);
				if (reader.read() == -1) break;
				reader.reset();
				try {
					KillswitchChain killswitchChain = readChain(reader);
					if (killswitchChain != null) {
						list.add(killswitchChain);
					}
				} catch (Exception e) {
					NotEnoughUpdates.LOGGER.error("Ran into exception while parsing killswitch chain", e);
				}
			}
			return list;
		} catch (Exception e) {
			NotEnoughUpdates.LOGGER.error("Ran into exception while parsing killswitch file", e);
			return Collections.emptyList();
		}
	}

	private static KillswitchChain readChain(StringReader reader) throws IOException {
		KillswitchChain killswitchChain = new KillswitchChain();
		boolean bogus = false;
		while (true) {
			skipWhitespaces(reader);
			if (tryConsume(reader, ';')) break;
			KillswitchDirective directive = readDirective(reader);
			if (directive == null) {
				bogus = true;
			}
			killswitchChain.directives.add(directive);
		}
		if (bogus) return null;
		return killswitchChain;
	}

	private static boolean tryConsume(StringReader reader, int c) throws IOException {
		reader.mark(1);
		boolean charFound = reader.read() == c;
		if (!charFound)
			reader.reset();
		return charFound;
	}

	private static <T> T getJson(List<String> s, int i, Class<T> t) {
		return GSON.fromJson(s.get(i), t);
	}

	private static String getString(List<String> s, int i) {
		String j = s.get(i);
		try {
			return GSON.fromJson(j, String.class);
		} catch (JsonParseException e) {
			return j;
		}
	}

	private static KillswitchDirective readDirective(StringReader reader) throws IOException {
		skipWhitespaces(reader);
		String name = readUntil(reader, c -> c == '(').trim();
		if (name.length() == 0 && tryConsume(reader, -1)) return new NOPDirective();
		List<String> arguments = new ArrayList<>();
		if (tryConsume(reader, '(')) {
			skipWhitespaces(reader);
			while (!tryConsume(reader, ')')) {
				skipWhitespaces(reader);
				arguments.add(readArgument(reader).trim());
				skipWhitespaces(reader);
			}
		} else {
			throw new IOException("Expected (");
		}
		skipWhitespaces(reader);
		Function<List<String>, KillswitchDirective> genDir = directives.get(name);
		if (genDir == null) return null;
		return genDir.apply(arguments);
	}

	private static String readArgument(StringReader reader) throws IOException {
		skipWhitespaces(reader);
		String s = readUntil(reader, c -> c == ';');
		tryConsume(reader, ';');
		return s;
	}

	private static void skipWhitespaces(StringReader reader) throws IOException {
		while (true) {
			reader.mark(1);
			int read = reader.read();
			if (read != ' ' && read != '\n') {
				reader.reset();
				break;
			}
		}
	}

	private static String readUntil(StringReader reader, Predicate<Character> c) throws IOException {
		reader.mark(1);
		char[] buf = new char[128];
		StringBuilder sb = new StringBuilder();
		loop:
		while (true) {
			int read = reader.read(buf);
			if (read == -1) break;
			for (int i = 0; i < read; i++) {
				if (c.test(buf[i])) {
					sb.append(buf, 0, i);
					break loop;
				}
			}
			sb.append(buf, 0, read);
		}
		reader.reset();
		reader.skip(sb.length());
		return sb.toString().intern();
	}

	public static void initializeKillswitches() {
		loadKillswitchData();
		long THIRTY_MINUTES = TimeUnit.MINUTES.toMillis(30); // Oh my god thirty virus reference
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				loadKillswitchData();
			}
		}, THIRTY_MINUTES, THIRTY_MINUTES);
	}
}
