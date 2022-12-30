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

import java.time.Instant;

// cryptic helped me a "little" with the calculation
public class StarCultCalculator {

	public static final String[] SEASONS = new String[]{
		"Early Spring",
		"Spring",
		"Late Spring",
		"Early Summer",
		"Summer",
		"Late Summer",
		"Early Autumn",
		"Autumn",
		"Late Autumn",
		"Early Winter",
		"Winter",
		"Late Winter",
	};

	public static final long HOUR_MS = 50000;
	public static final long DAY_MS = 24 * HOUR_MS;
	public static final long MONTH_MS = 31 * DAY_MS;
	public static final long YEAR_MS = SEASONS.length * MONTH_MS;
	public static final long YEAR_0 = 1560275700000L;

	public static int getSkyblockYear() {
		long now = Instant.now().toEpochMilli();
		long currentYear = Math.floorDiv((now - YEAR_0), YEAR_MS);
		return (int) (currentYear + 1);
	}

	private static boolean active = false;
	private static long activeTill = 0;

	public static String getNextStarCult() {
		Instant instantNow = Instant.now();
		long nowEpoch = instantNow.toEpochMilli();
		long currentOffset = (nowEpoch - YEAR_0) % YEAR_MS;

		int currentMonth = (int) Math.floorDiv(currentOffset, MONTH_MS);
		int currentDay = (int) Math.floorDiv((currentOffset - (long) currentMonth * MONTH_MS) % MONTH_MS, DAY_MS) + 1;

		int out = 7;
		if (currentDay > 21) {
			out = 28;
		} else if (currentDay > 14) {
			out = 21;
		} else if (currentDay > 7) {
			out = 14;
		}
		Instant cultStart = Instant.ofEpochMilli(
			YEAR_0 + (getSkyblockYear() - 1) * YEAR_MS + currentMonth * MONTH_MS + (out - 1) * DAY_MS);
		if (cultStart.isBefore(instantNow)) {
			int curYearCult = getSkyblockYear() - 1;
			if (out == 28) {
				out = 7;
				if (currentMonth == 12) {
					currentMonth = 1;
					curYearCult++;
				} else {
					currentMonth++;
				}
			} else {
				out += 7;
			}
			out--;
			cultStart = Instant.ofEpochMilli(YEAR_0 + (curYearCult) * YEAR_MS + currentMonth * MONTH_MS + out * DAY_MS);
		}

		long l = System.currentTimeMillis();
		if (cultStart.toEpochMilli() - l <= 1000) {
			active = true;
			activeTill = l + 300000;
		}

		if (l > activeTill) {
			active = false;
			activeTill = 0;
		}

		if (active && activeTill != 0) {
			return "Active! (" + Utils.prettyTime(activeTill - System.currentTimeMillis()) + ")";
		}

		return Utils.prettyTime(cultStart.toEpochMilli() - l);
	}
}

