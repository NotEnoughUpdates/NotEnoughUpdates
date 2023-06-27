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
import io.github.moulberry.notenoughupdates.mixins.AccessorGuiPlayerTabOverlay;
import io.github.moulberry.notenoughupdates.util.NotificationHandler;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumChatFormatting;

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

		int minutes = getMinutesRemaining(timeLine);
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

	private static int getMinutesRemaining(String timeLine) {
		String clean = timeLine.replaceAll("(§.)", "");
		clean = clean.replaceAll("(\\d)([smhdy])", "$1 $2");
		String[] digits = clean.split(" ");
		int minutes = 0;
		try {
			for (int i = 0; i < digits.length; i++) {
				if (i % 2 == 1) continue;

				String number = digits[i];
				String unit = digits[i + 1];
				long val = Integer.parseInt(number);
				switch (unit.toLowerCase(Locale.ROOT)) {
					case "years":
					case "year":
						minutes += val * 525600;
						break;
					case "months":
					case "month":
						minutes += val * 43200;
						break;
					case "days":
					case "day":
						minutes += val * 1440;
						break;
					case "hours":
					case "hour":
					case "h":
						minutes += val * 60;
						break;
					case "minutes":
					case "minute":
					case "m":
						minutes += val;
						break;
				} // ignore seconds
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
		return minutes;
	}

	private static String getTimeLine() {
		String[] lines;
		try {
			lines = ((AccessorGuiPlayerTabOverlay) Minecraft.getMinecraft().ingameGUI.getTabList())
				.getFooter()
				.getUnformattedText()
				.split("\n");
		} catch (NullPointerException ignored) {
			return null;
		}
		String timeLine = null; // the line that contains the cookie timer
		for (int i = 0; i < lines.length; i++) {
			if (lines[i].startsWith("Cookie Buff")) {
				timeLine = lines[i + 1]; // the line after the "Cookie Buff" line
			}
			if (lines[i].startsWith("Not active! Obtain booster cookies from the")) {
				hasCookie = false;
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
			int minutes = getMinutesRemaining(timeLine);
			cookieEndTime = System.currentTimeMillis() + (long) minutes * 60 * 1000;
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
}
