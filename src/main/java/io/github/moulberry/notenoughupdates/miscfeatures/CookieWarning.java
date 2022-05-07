package io.github.moulberry.notenoughupdates.miscfeatures;

import com.google.common.collect.Lists;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.mixins.AccessorGuiPlayerTabOverlay;
import io.github.moulberry.notenoughupdates.util.NotificationHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class CookieWarning {

	private static boolean hasNotified;

	public CookieWarning() {
		hasNotified = false;
	}

	@SubscribeEvent
	public void onJoinWorld(EntityJoinWorldEvent e) {
		if(e.entity == Minecraft.getMinecraft().thePlayer) {
			this.checkCookie();
		}
	}

	public static void resetNotification() {
		hasNotified = false;
	}

	/**
	 * Checks the tab list for a cookie timer, and sends a chat message if the timer is within the tolerance
	 */
	private void checkCookie() {
		if(!hasNotified && NotEnoughUpdates.INSTANCE.config.notifications.doBoosterNotif) {
			String[] lines = ((AccessorGuiPlayerTabOverlay) Minecraft.getMinecraft().ingameGUI.getTabList()).getFooter().getUnformattedText().split("\n");
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
				NotificationHandler.displayNotification(Lists.newArrayList(
					"\u00a7cBooster Cookie Ran Out!",
					"\u00a77Your Booster Cookie expired!",
					"\u00a77",
					"\u00a77Press X on your keyboard to close this notification"
				), true, true);
				hasNotified = true;
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
}
