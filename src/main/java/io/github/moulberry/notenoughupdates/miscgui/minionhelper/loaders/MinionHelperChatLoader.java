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

package io.github.moulberry.notenoughupdates.miscgui.minionhelper.loaders;

import io.github.moulberry.notenoughupdates.core.util.StringUtils;
import io.github.moulberry.notenoughupdates.miscgui.minionhelper.Minion;
import io.github.moulberry.notenoughupdates.miscgui.minionhelper.MinionHelperManager;
import io.github.moulberry.notenoughupdates.miscgui.minionhelper.MinionHelperOverlay;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class MinionHelperChatLoader {
	private static MinionHelperChatLoader instance = null;
	private final MinionHelperManager manager = MinionHelperManager.getInstance();

	public static MinionHelperChatLoader getInstance() {
		if (instance == null) {
			instance = new MinionHelperChatLoader();
		}
		return instance;
	}

	@SubscribeEvent(priority = EventPriority.LOW, receiveCanceled = true)
	public void onChat(ClientChatReceivedEvent event) {
		if (event.type != 0) return;
		String message = event.message.getFormattedText();

		try {
			if (message.startsWith("§r§aYou crafted a §eTier ") && message.contains("§a! That's a new one!")) {
				String text = StringUtils.substringBetween(message, "§eTier ", "§a! That's");
				String rawTier = text.split(" ")[0];
				int tier = Utils.parseRomanNumeral(rawTier);
				String name = text.substring(rawTier.length() + 1);

				setCrafted(manager.getMinionByName(name, tier));
			}

			if (message.contains("§f §acrafted a §eTier ") && message.contains(" Minion§a!")) {
				String text = StringUtils.substringBetween(message, "§eTier ", "§a!");
				String rawTier = text.split(" ")[0];
				int tier = Utils.parseRomanNumeral(rawTier);
				String name = text.substring(rawTier.length() + 1);

				setCrafted(manager.getMinionByName(name, tier));
				MinionHelperOverlay.getInstance().resetCache();
			}

			if (message.startsWith("§r§7Switching to profile ")) {
				manager.getApi().prepareProfileSwitch();
			}

		} catch (Exception e) {
			Utils.addChatMessage(
				"[NEU] §cMinion Helper failed reading the minion upgrade message. See the logs for more info!");
			e.printStackTrace();
		}
	}

	private void setCrafted(Minion minion) {
		minion.setCrafted(true);

		if (!minion.doesMeetRequirements()) {
			minion.setMeetRequirements(true);

			for (Minion child : manager.getChildren(minion)) {
				child.setMeetRequirements(true);
			}
		}
	}
}
