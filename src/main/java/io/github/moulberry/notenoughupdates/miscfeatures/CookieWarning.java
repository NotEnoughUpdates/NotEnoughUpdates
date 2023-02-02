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

public class CookieWarning {

	private static boolean hasNotified;
	private static boolean hasErrorMessage;
	private static long cookieEndTime = 0;

	public static void resetNotification() {
		hasNotified = false;
		NotificationHandler.cancelNotification();
	}

	/**
	 * Checks the tab list for a cookie timer, and sends a notification if the timer is within the tolerance
	 */
	public static void checkCookie() {
		if (NotEnoughUpdates.INSTANCE.config.notifications.doBoosterNotif &&
			NotEnoughUpdates.INSTANCE.hasSkyblockScoreboard()) {
			String[] lines;
			try {
				lines = ((AccessorGuiPlayerTabOverlay) Minecraft.getMinecraft().ingameGUI.getTabList())
					.getFooter()
					.getUnformattedText()
					.split("\n");
			} catch (NullPointerException e) {
				return; // if the footer is null or somehow doesn't exist, stop
			}
			boolean hasCookie = true;
			String timeLine = null; // the line that contains the cookie timer
			for (int i = 0; i < lines.length; i++) {
				if (lines[i].startsWith("Cookie Buff")) {
					timeLine = lines[i + 1]; // the line after the "Cookie Buff" line
				}
				if (lines[i].startsWith("Not active! Obtain booster cookies from the")) {
					hasCookie = false;
				}
			}
			if (!hasCookie) {
				if (!hasNotified) {
					NotificationHandler.displayNotification(Lists.newArrayList(
						"\u00a7cBooster Cookie Ran Out!",
						"\u00a77Your Booster Cookie expired!",
						"\u00a77",
						"\u00a77Press X on your keyboard to close this notification"
					), true, true);
					hasNotified = true;
				}
				return;
			}
			if (timeLine != null) {
				String clean = timeLine.replaceAll("(\u00a7.)", "");
				clean = clean.replaceAll("(\\d)([smhdy])", "$1 $2");
				String[] digits = clean.split(" ");
				int minutes = 0;
				try {
					for (int i = 0; i < digits.length; i++) {
						if (i % 2 == 1) continue;

						String number = digits[i];
						String unit = digits[i + 1];
						long val = Integer.parseInt(number);
						switch (unit) {
							case "Years":
							case "Year":
								minutes += val * 525600;
								break;
							case "Months":
							case "Month":
								minutes += val * 43200;
								break;
							case "Days":
							case "Day":
								minutes += val * 1440;
								break;
							case "Hours":
							case "Hour":
							case "h":
								minutes += val * 60;
								break;
							case "Minutes":
							case "Minute":
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
				cookieEndTime = System.currentTimeMillis() + minutes * 60 * 1000;
				if (minutes < NotEnoughUpdates.INSTANCE.config.notifications.boosterCookieWarningMins && !hasNotified) {
					NotificationHandler.displayNotification(Lists.newArrayList(
						"\u00a7cBooster Cookie Running Low!",
						"\u00a77Your Booster Cookie will expire in " + timeLine,
						"\u00a77",
						"\u00a77Press X on your keyboard to close this notification"
					), true, true);
					hasNotified = true;
				}
			}
		}
	}

	public static boolean hasActiveBoosterCookie() {
		return cookieEndTime > System.currentTimeMillis();
	}
}
