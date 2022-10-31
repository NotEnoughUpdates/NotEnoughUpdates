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
					//Utils.sendMessageToPlayer(EnumChatFormatting.YELLOW+"You exited the beast with ["+EnumChatFormatting.AQUA+(TimersOverlay.afterPearls-TimersOverlay.beforePearls)+EnumChatFormatting.YELLOW+"/"+EnumChatFormatting.AQUA+TimersOverlay.availablePearls+EnumChatFormatting.YELLOW+"] Heavy Pearls!");
					if (TimersOverlay.afterPearls - TimersOverlay.beforePearls == TimersOverlay.availablePearls) {
						NotEnoughUpdates.INSTANCE.config.getProfileSpecific().dailyHeavyPearlCompleted = System.currentTimeMillis();
						//Utils.sendMessageToPlayer(EnumChatFormatting.GREEN+"Daily "+EnumChatFormatting.DARK_AQUA+"Heavy Pearls"+EnumChatFormatting.GREEN+" Complete!");
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
