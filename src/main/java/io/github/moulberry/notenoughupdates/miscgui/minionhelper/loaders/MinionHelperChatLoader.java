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

import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.miscgui.minionhelper.Minion;
import io.github.moulberry.notenoughupdates.miscgui.minionhelper.MinionHelperManager;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MinionHelperChatLoader {

	private final MinionHelperManager manager;

	//§aYou crafted a §eTier I Redstone Minion§a! That's a new one!
	// §aCraft §e7 §amore unique Minions to unlock your §e9th Minion slot§a!
	private final Pattern PATTERN_OWN_MINION = Pattern.compile(
		"§r§aYou crafted a §eTier (\\S+) (.+) Minion§a! That's a new one!(\\r\\n|\\r|\\n)(.*)");
	//§aYou crafted a §eTier VI Enderman Minion§a! That's a new one!

	//§b[MVP§3+§b] Eisengolem§f §acrafted a §eTier I Birch Minion§a!
	private final Pattern PATTERN_COOP_MINION = Pattern.compile(
		"(.+)§f §acrafted a §eTier (\\S+) (.+) Minion§a!(§r)?(\\r\\n|\\r|\\n)?(.*)?");

	public MinionHelperChatLoader(MinionHelperManager manager) {
		this.manager = manager;
	}

	@SubscribeEvent(priority = EventPriority.LOW, receiveCanceled = true)
	public void onChat(ClientChatReceivedEvent event) {
		if (event.type != 0) return;
		String message = event.message.getFormattedText();
		if (!NotEnoughUpdates.INSTANCE.config.minionHelper.gui) return;

		try {
			Matcher ownMatcher = PATTERN_OWN_MINION.matcher(message);
			if (ownMatcher.matches()) {
				String rawTier = ownMatcher.group(1);
				int tier = Utils.parseRomanNumeral(rawTier);
				String name = ownMatcher.group(2) + " Minion";
				name = Utils.cleanColour(name);

				setCrafted(manager.getMinionByName(name, tier));
			}

			Matcher coopMatcher = PATTERN_COOP_MINION.matcher(message);
			if (coopMatcher.matches()) {
				String rawTier = coopMatcher.group(2);
				int tier = Utils.parseRomanNumeral(rawTier);
				String name = coopMatcher.group(3) + " Minion";

				setCrafted(manager.getMinionByName(name, tier));
				manager.getOverlay().resetCache();
			}

			if (message.startsWith("§r§7Switching to profile ")) {
				manager.getApi().prepareProfileSwitch();
			}

		} catch (Exception e) {
			Utils.addChatMessage(
				"§c[NEU] Minion Helper failed reading the minion upgrade message. See the logs for more info!");
			e.printStackTrace();
		}
	}

	private void setCrafted(Minion minion) {
		manager.setCrafted(minion);
	}
}
