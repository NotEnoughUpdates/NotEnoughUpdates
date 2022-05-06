package io.github.moulberry.notenoughupdates.miscfeatures;

import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

public class CookieWarning {

	/**
	 * Checks the tab list for a cookie timer, and sends a chat message if the timer is within the tolerance
	 * @param footer Footer text on the tablist
	 */
	public static void checkCookie(String footer) {
		String[] lines = footer.split("\n");
		boolean hasCookie = true;
		String timeLine = null; // the line that contains the cookie timer
		for(int i = 0; i < lines.length; i++) {
			if(lines[i].startsWith("Cookie Buff")) {
				timeLine = lines[i+1]; // the line after the "Cookie Buff" line
			}
			if(lines[i].startsWith("Not active! Obtain booster cookies from the")) {
				hasCookie = false;
			}
		}
		if(!hasCookie) {
			showTitle("", "\u00A7cYour Booster Cookie Ran Out!", 0, 60, 0);
			return;
		}
		if(timeLine != null) {
			String[] digits = timeLine.split(" ");
			int minutes = 0;
			try {
				for(String digit : digits) {
					if(digit.endsWith("y")) {
						digit = digit.substring(0, digit.length() - 1);
						minutes += Integer.parseInt(digit) * 525600;
					} else if(digit.endsWith("d")) {
						digit = digit.substring(0, digit.length() - 1);
						minutes += Integer.parseInt(digit) * 1440;
					} else if(digit.endsWith("h")) {
						digit = digit.substring(0, digit.length() - 1);
						minutes += Integer.parseInt(digit) * 60;
					} else if(digit.endsWith("m")) {
						digit = digit.substring(0, digit.length() - 1);
						minutes += Integer.parseInt(digit);
					} // ignore seconds
				}
			} catch (NumberFormatException e) {
				e.printStackTrace();
				Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(
					EnumChatFormatting.RED +
						"NEU ran into an issue when retrieving the Booster Cookie Timer. Check the logs for details."));
			}
			if(minutes < NotEnoughUpdates.INSTANCE.config.notifications.boosterCookieWarningMins) {
				showTitle("", "\u00A7cYour Booster Cookie is Running Low!", 0, 60, 0);
			}
		}
	}

	private static void showTitle(String title, String subtitle, int fadeIn, int time, int fadeOut) {
		GuiIngame gui = Minecraft.getMinecraft().ingameGUI;
		gui.displayTitle(title, null, fadeIn, time, fadeOut);
		gui.displayTitle(null, subtitle, fadeIn, time, fadeOut);
		gui.displayTitle(null, null, fadeIn, time, fadeOut);
	}
}
