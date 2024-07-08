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

import com.google.common.collect.Lists;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.miscfeatures.tablisttutorial.TablistAPI;
import io.github.moulberry.notenoughupdates.util.NotificationHandler;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.util.EnumChatFormatting;

import java.util.List;
import java.util.Locale;

public class CookieWarning {

	private static boolean hasNotified;
	private static boolean hasErrorMessage;
	private static long cookieEndTime = 0;
	private static boolean hasCookie = true;
	private static long lastChecked = 0;

	public static void resetNotification() {
		hasNotified = false;
		hasCookie = true;
		NotificationHandler.cancelNotification();
	}

	/**
	 * Checks the tab list for a cookie timer, and sends a notification if the timer is within the tolerance
	 */
	public static void checkCookie() {
		if (!NotEnoughUpdates.INSTANCE.config.notifications.doBoosterNotif ||
			!NotEnoughUpdates.INSTANCE.hasSkyblockScoreboard()) {
			return;
		}
		String timeLine = getTimeLine();
		if (!hasCookie) {
			if (!hasNotified) {
				NotificationHandler.displayNotification(Lists.newArrayList(
					"§cBooster Cookie Ran Out!",
					"§7Your Booster Cookie expired!",
					"§7",
					"§7Press X on your keyboard to close this notification"
				), true, true);
				hasNotified = true;
			}
			return;
		}
		if (timeLine == null) return;

		int minutes = (int) (getMillisecondsRemaining(timeLine) / 60 / 1000);
		if (minutes < NotEnoughUpdates.INSTANCE.config.notifications.boosterCookieWarningMins && !hasNotified) {
			NotificationHandler.displayNotification(Lists.newArrayList(
				"§cBooster Cookie Running Low!",
				"§7Your Booster Cookie will expire in " + timeLine,
				"§7",
				"§7Press X on your keyboard to close this notification"
			), true, true);
			hasNotified = true;
		}
	}

	private static long getMillisecondsRemaining(String timeLine) {
		String clean = timeLine.replaceAll("(§.)", "");
		clean = clean.replaceAll("(\\d)([smhdy])", "$1 $2");
		String[] digits = clean.split(" ");
		long ms = 0;
		try {
			for (int i = 0; i < digits.length; i++) {
				if (i % 2 == 1) continue;

				String number = digits[i];
				String unit = digits[i + 1];
				long val = Integer.parseInt(number);
				ms += (getEffectRemainingInMilliseconds(unit, val));
			}
		} catch (NumberFormatException e) {
			if (!hasErrorMessage) {
				e.printStackTrace();
				Utils.addChatMessage(EnumChatFormatting.RED +
					"NEU ran into an issue when retrieving the Booster Cookie Timer. Check the logs for details.");
				hasErrorMessage = true;
			}
			hasNotified = true;
		}
		return ms;
	}

	private static String getTimeLine() {
		List<String> lines = TablistAPI.getOptionalWidgetLines(TablistAPI.WidgetNames.ACTIVE_EFFECTS);
		List<String> lines2 = TablistAPI.getOptionalWidgetLines(TablistAPI.WidgetNames.COOKIE_BUFF);
		lines.addAll(lines2);
		String timeLine = null; // the line that contains the cookie timer

		for (int i = 0; i < lines.size(); i++) {
			String line = lines.get(i);
			line = Utils.cleanColour(line).trim();

			if (line.startsWith("Cookie Buff:")) {
				timeLine = line.replace("Cookie Buff: ", "");
				if (timeLine.contains("INACTIVE")) {
					hasCookie = false;
					return null;
				}
			} else if (line.startsWith("Cookie Buff")) {
				timeLine = lines.get(i + 1); // the line after the "Cookie Buff" line
				timeLine = Utils.cleanColour(timeLine).trim();
			}


			if (line.startsWith("Not active! Obtain booster cookies from the")) {
				hasCookie = false;
				return null;
			}
		}
		return timeLine;
	}

	public static boolean hasActiveBoosterCookie() {
		long cookieEndTime = getCookieEndTime();
		return cookieEndTime > System.currentTimeMillis();
	}

	private static long getCookieEndTime() {
		// Only updating every 10 seconds
//		if (System.currentTimeMillis() > lastChecked + 10_000) return cookieEndTime;
		if (lastChecked + 3_000 > System.currentTimeMillis()) return cookieEndTime;

		String timeLine = getTimeLine();
		if (hasCookie && timeLine != null) {
			long ms = getMillisecondsRemaining(timeLine);
			cookieEndTime = System.currentTimeMillis() + ms;
		} else {
			cookieEndTime = 0;
		}

		lastChecked = System.currentTimeMillis();
		return cookieEndTime;
	}

	public static void onProfileSwitch() {
		resetNotification();
		hasErrorMessage = false;
		cookieEndTime = 0;
		hasCookie = true;
		lastChecked = 0;
	}

	public static long getEffectRemainingInMilliseconds(String remainingTimeType, long remainingTime) {
		switch (remainingTimeType.toLowerCase(Locale.ROOT).replace(",", "")) {
			case "years":
			case "year":
			case "y":
				return remainingTime * 24 * 60 * 60 * 1000 * 30 * 12;
			case "months":
			case "month":
			case "mo":
				return remainingTime * 24 * 60 * 60 * 1000 * 30;
			case "days":
			case "day":
			case "d":
				return remainingTime * 24 * 60 * 60 * 1000;
			case "hours":
			case "hour":
			case "h":
				return remainingTime * 60 * 60 * 1000;
			case "minutes":
			case "minute":
			case "m":
				return remainingTime * 60 * 1000;
			case "seconds":
			case "second":
			case "s":
				return remainingTime * 1000;
		}
		return remainingTime;
	}
}
