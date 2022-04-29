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
				"\u00a74Old animations warning",
				"\u00a77You use a old animations mod from Orange or spiderfrog",
				"\u00a77These mods break features in NEU and other mods",
				"\u00a77Please remove them and optionally replace them with the OldAnimations mod from Sk1er",
				"\u00a77It can be found at the following website: \u00a79sk1er.club/beta",
				"\u00a77For more information join the discord at \u00a79discord.gg/moulberry\u00a77 and message in \u00a79#neu-support",
				"\u00a77",
				"\u00a77Press X on your keyboard to close this notification or turn it off in the config"
			), true, true);
			unregister();
		}
	}
}
