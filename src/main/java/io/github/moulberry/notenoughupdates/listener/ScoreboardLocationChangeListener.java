package io.github.moulberry.notenoughupdates.listener;

import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.overlays.TimersOverlay;

public class ScoreboardLocationChangeListener {

	public ScoreboardLocationChangeListener(String oldLocation, String newLocation) {
		if (oldLocation.equals("Belly of the Beast") && newLocation.equals("Matriarchs Lair")) {
			//Check inventory pearl count for AFTER they complete to see if it is the same as before + (amount available on actionbar)
			Thread thread = new Thread(() -> {
				try {
					Thread.sleep(3000);
					TimersOverlay.afterPearls = TimersOverlay.heavyPearlCount();
					//Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.YELLOW+"You exited the beast with ["+EnumChatFormatting.AQUA+(TimersOverlay.afterPearls-TimersOverlay.beforePearls)+EnumChatFormatting.YELLOW+"/"+EnumChatFormatting.AQUA+TimersOverlay.availablePearls+EnumChatFormatting.YELLOW+"] Heavy Pearls!"));
					if (TimersOverlay.afterPearls - TimersOverlay.beforePearls == TimersOverlay.availablePearls) {
						NotEnoughUpdates.INSTANCE.config.getProfileSpecific().dailyHeavyPearlCompleted = System.currentTimeMillis();
						//Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN+"Daily "+EnumChatFormatting.DARK_AQUA+"Heavy Pearls"+EnumChatFormatting.GREEN+" Complete!"));
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			});
			thread.start();
		} else if (oldLocation.equals("Matriarchs Lair") && newLocation.equals("Belly of the Beast")) {
			//Check inventory pearl count BEFORE they complete so we can later check if it is complete.
			TimersOverlay.beforePearls = TimersOverlay.heavyPearlCount();
		}
	}
}
