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

package io.github.moulberry.notenoughupdates.miscfeatures;

import io.github.moulberry.notenoughupdates.util.Utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StreamerMode {
	private static final Pattern lobbyPattern = Pattern.compile("(mini|mega|m|M)([0-9]{1,3}[A-Z])");

	public static String filterLobbyNames(String line) {
		Matcher matcher = lobbyPattern.matcher(line);
		if (matcher.find() && matcher.groupCount() == 2) {
			String lobbyType = matcher.group(1);
			String lobbyId = matcher.group(2);
			long lobbyNum = Long.parseLong(lobbyId.substring(0, lobbyId.length() - 1));

			long obfLobbyNum = (lobbyNum * 9182739 + 11) % 500;
			char obfLobbyLetter = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".charAt((int) (obfLobbyNum % 26));

			line = line.replaceAll("(mini|mega|m|M)([0-9]{1,3}[A-Z])", lobbyType + obfLobbyNum + obfLobbyLetter);
		}
		return line;
	}

	public static String filterScoreboard(String line) {
		line = filterLobbyNames(Utils.cleanDuplicateColourCodes(line));
		return line;
	}

	public static String filterChat(String line) {
		line = filterLobbyNames(line);
		return line;
	}
}
