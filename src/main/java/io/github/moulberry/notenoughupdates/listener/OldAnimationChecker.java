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

import com.google.common.collect.Lists;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.util.NotificationHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class OldAnimationChecker {

	private void unregister() {
		MinecraftForge.EVENT_BUS.unregister(this);
	}

	@SubscribeEvent
	public void onWorldLoad(WorldEvent.Load event) {
		if (!NotEnoughUpdates.INSTANCE.config.notifications.doOamNotif) {
			unregister();
			return;
		}
		boolean oldAnimations = false;
		if (Loader.isModLoaded("animations")) {
			oldAnimations = true;
		} else {
			try {
				Class.forName("com.spiderfrog.oldanimations.OldAnimationsMod");
				//previous statement would throw if not present
				oldAnimations = true;
			} catch (ClassNotFoundException ignored) {
			}
		}

		if (oldAnimations) {
			NotificationHandler.displayNotification(Lists.newArrayList(
				"§4Old animations warning",
				"§7You use a old animations mod from Orange or spiderfrog",
				"§7These mods break features in NEU and other mods",
				"§7Please remove them and optionally replace them with the OldAnimations mod from Sk1er",
				"§7It can be found at the following website: §9sk1er.club/beta",
				"§7For more information join the discord at §9discord.gg/moulberry§7 and message in §9#neu-support",
				"§7",
				"§7Press X on your keyboard to close this notification or turn it off in the config"
			), true, true);
			unregister();
		}
	}
}
