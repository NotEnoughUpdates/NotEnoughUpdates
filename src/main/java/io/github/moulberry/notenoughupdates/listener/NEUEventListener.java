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
import com.google.gson.JsonObject;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.core.BackgroundBlur;
import io.github.moulberry.notenoughupdates.cosmetics.CapeManager;
import io.github.moulberry.notenoughupdates.dungeons.DungeonBlocks;
import io.github.moulberry.notenoughupdates.dungeons.DungeonWin;
import io.github.moulberry.notenoughupdates.miscfeatures.CookieWarning;
import io.github.moulberry.notenoughupdates.miscfeatures.CrystalMetalDetectorSolver;
import io.github.moulberry.notenoughupdates.miscfeatures.CrystalOverlay;
import io.github.moulberry.notenoughupdates.miscfeatures.FairySouls;
import io.github.moulberry.notenoughupdates.miscfeatures.ItemCustomizeManager;
import io.github.moulberry.notenoughupdates.miscfeatures.NPCRetexturing;
import io.github.moulberry.notenoughupdates.miscgui.AccessoryBagOverlay;
import io.github.moulberry.notenoughupdates.miscgui.GuiCustomEnchant;
import io.github.moulberry.notenoughupdates.miscgui.GuiItemRecipe;
import io.github.moulberry.notenoughupdates.miscgui.StorageOverlay;
import io.github.moulberry.notenoughupdates.miscgui.hex.GuiCustomHex;
import io.github.moulberry.notenoughupdates.overlays.OverlayManager;
import io.github.moulberry.notenoughupdates.overlays.TextOverlay;
import io.github.moulberry.notenoughupdates.overlays.TextTabOverlay;
import io.github.moulberry.notenoughupdates.recipes.RecipeHistory;
import io.github.moulberry.notenoughupdates.util.Constants;
import io.github.moulberry.notenoughupdates.util.NotificationHandler;
import io.github.moulberry.notenoughupdates.util.ProfileApiSyncer;
import io.github.moulberry.notenoughupdates.util.SBInfo;
import io.github.moulberry.notenoughupdates.util.Utils;
import io.github.moulberry.notenoughupdates.util.XPInformation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.event.ClickEvent;
import net.minecraft.init.Items;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NEUEventListener {

	private final NotEnoughUpdates neu;
	private final ExecutorService itemPreloader = Executors.newFixedThreadPool(10);
	private final List<ItemStack> toPreload = new ArrayList<>();
	private boolean joinedSB = false;

	private boolean preloadedItems = false;
	private long lastLongUpdate = 0;
	private long lastSkyblockScoreboard = 0;

	public NEUEventListener(NotEnoughUpdates neu) {
		this.neu = neu;
	}

	@SubscribeEvent
	public void onWorldLoad(WorldEvent.Unload event) {
		NotEnoughUpdates.INSTANCE.saveConfig();
		CrystalMetalDetectorSolver.initWorld();
	}

	@SubscribeEvent
	public void onTick(TickEvent.ClientTickEvent event) {
		if (event.phase != TickEvent.Phase.START) return;
		if (Minecraft.getMinecraft().theWorld == null) return;
		if (Minecraft.getMinecraft().thePlayer == null) return;

		if ((Keyboard.isKeyDown(Keyboard.KEY_NUMPAD1) && Keyboard.isKeyDown(Keyboard.KEY_NUMPAD4) && Keyboard.isKeyDown(
			Keyboard.KEY_NUMPAD9))) {
			ChatComponentText component = new ChatComponentText("\u00a7cYou are permanently banned from this server!");
			component.appendText("\n");
			component.appendText("\n\u00a77Reason: \u00a7rSuspicious account activity/Other");
			component.appendText("\n\u00a77Find out more: \u00a7b\u00a7nhttps://www.hypixel.net/appeal");
			component.appendText("\n");
			component.appendText("\n\u00a77Ban ID: \u00a7r#49871982");
			component.appendText("\n\u00a77Sharing your Ban ID may affect the processing of your appeal!");
			Minecraft.getMinecraft().getNetHandler().getNetworkManager().closeChannel(component);
			return;
		}

		if (neu.hasSkyblockScoreboard()) {
			if (!preloadedItems) {
				preloadedItems = true;
				List<JsonObject> list = new ArrayList<>(neu.manager.getItemInformation().values());
				for (JsonObject json : list) {
					itemPreloader.submit(() -> {
						ItemStack stack = neu.manager.jsonToStack(json, true, true);
						if (stack.getItem() == Items.skull) toPreload.add(stack);
					});
				}
			} else if (!toPreload.isEmpty()) {
				Utils.drawItemStack(toPreload.get(0), -100, -100);
				toPreload.remove(0);
			} else {
				itemPreloader.shutdown();
			}

			for (TextOverlay overlay : OverlayManager.textOverlays) {
				overlay.shouldUpdateFrequent = true;
			}
		}

		boolean longUpdate = false;
		long currentTime = System.currentTimeMillis();
		if (currentTime - lastLongUpdate > 1000) {
			longUpdate = true;
			lastLongUpdate = currentTime;
		}
		if (!NotEnoughUpdates.INSTANCE.config.dungeons.slowDungeonBlocks) {
			DungeonBlocks.tick();
		}
		DungeonWin.tick();

		String containerName = null;
		if (Minecraft.getMinecraft().currentScreen instanceof GuiChest) {
			GuiChest eventGui = (GuiChest) Minecraft.getMinecraft().currentScreen;
			ContainerChest cc = (ContainerChest) eventGui.inventorySlots;
			containerName = cc.getLowerChestInventory().getDisplayName().getUnformattedText();

			if (GuiCustomEnchant.getInstance().shouldOverride(containerName)) {
				GuiCustomEnchant.getInstance().tick();
			}
			if (GuiCustomHex.getInstance().shouldOverride(containerName)) {
				GuiCustomHex.getInstance().tick(containerName);
			}
		}

		//MiningOverlay and TimersOverlay need real tick speed
		if (neu.hasSkyblockScoreboard()) {
			for (TextOverlay overlay : OverlayManager.textOverlays) {
				if (overlay instanceof TextTabOverlay) {
					TextTabOverlay skillOverlay = (TextTabOverlay) overlay;
					skillOverlay.realTick();
				}
			}
		}


		if (longUpdate) {

			if (!(Minecraft.getMinecraft().currentScreen instanceof GuiItemRecipe)) {
				RecipeHistory.clear();
			}

			CrystalOverlay.tick();
			FairySouls.getInstance().tick();
			XPInformation.getInstance().tick();
			ProfileApiSyncer.getInstance().tick();
			ItemCustomizeManager.tick();
			BackgroundBlur.markDirty();
			NPCRetexturing.getInstance().tick();
			StorageOverlay.getInstance().markDirty();
			CookieWarning.checkCookie();

			if (neu.hasSkyblockScoreboard()) {
				for (TextOverlay overlay : OverlayManager.textOverlays) {
					overlay.tick();
				}
			}

			NotEnoughUpdates.INSTANCE.overlay.redrawItems();
			CapeManager.onTickSlow();

			NotEnoughUpdates.profileViewer.putNameUuid(
				Minecraft.getMinecraft().thePlayer.getName(),
				Minecraft.getMinecraft().thePlayer.getUniqueID().toString().replace("-", "")
			);

			if (NotEnoughUpdates.INSTANCE.config.dungeons.slowDungeonBlocks) {
				DungeonBlocks.tick();
			}

			if (System.currentTimeMillis() - SBInfo.getInstance().joinedWorld > 500 &&
				System.currentTimeMillis() - SBInfo.getInstance().unloadedWorld > 500) {
				neu.updateSkyblockScoreboard();
			}
			CapeManager.getInstance().tick();

			if (containerName != null) {
				if (!containerName.trim().startsWith("Accessory Bag")) {
					AccessoryBagOverlay.resetCache();
				}
			} else {
				AccessoryBagOverlay.resetCache();
			}

			if (neu.hasSkyblockScoreboard()) {
				SBInfo.getInstance().tick();
				lastSkyblockScoreboard = currentTime;
				if (!joinedSB) {
					joinedSB = true;

					if (NotEnoughUpdates.INSTANCE.config.notifications.updateChannel != 0) {
						NotEnoughUpdates.INSTANCE.autoUpdater.displayUpdateMessageIfOutOfDate();
					}

					if (NotEnoughUpdates.INSTANCE.config.notifications.doRamNotif) {
						long maxMemoryMB = Runtime.getRuntime().maxMemory() / 1024L / 1024L;
						if (maxMemoryMB > 4100) {
							NotificationHandler.displayNotification(Lists.newArrayList(
								EnumChatFormatting.GRAY + "Too much memory allocated!",
								String.format(
									EnumChatFormatting.DARK_GRAY + "NEU has detected %03dMB of memory allocated to Minecraft!",
									maxMemoryMB
								),
								EnumChatFormatting.GRAY + "It is recommended to allocated between 2-4GB of memory",
								EnumChatFormatting.GRAY + "More than 4GB MAY cause FPS issues, EVEN if you have 16GB+ available",
								EnumChatFormatting.GRAY + "For more information, visit #ram-info in discord.gg/moulberry",
								"",
								EnumChatFormatting.GRAY + "Press X on your keyboard to close this notification"
							), false);
						}
					}

					if (!NotEnoughUpdates.INSTANCE.config.hidden.loadedModBefore) {
						NotEnoughUpdates.INSTANCE.config.hidden.loadedModBefore = true;
						if (Constants.MISC == null || !Constants.MISC.has("featureslist")) {
							Utils.showOutdatedRepoNotification();
							Utils.addChatMessage(
								"" + EnumChatFormatting.GOLD + "To view the feature list after restarting type /neufeatures");
						} else {
							String url = Constants.MISC.get("featureslist").getAsString();
							Utils.addChatMessage("");
							Utils.addChatMessage(EnumChatFormatting.BLUE + "It seems this is your first time using NotEnoughUpdates.");
							ChatComponentText clickTextFeatures = new ChatComponentText(EnumChatFormatting.YELLOW +
								"Click this message if you would like to view a list of NotEnoughUpdate's Features.");
							clickTextFeatures.setChatStyle(Utils.createClickStyle(ClickEvent.Action.OPEN_URL, url));
							Minecraft.getMinecraft().thePlayer.addChatMessage(clickTextFeatures);
						}
						Utils.addChatMessage("");
						ChatComponentText clickTextHelp = new ChatComponentText(EnumChatFormatting.YELLOW +
							"Click this message if you would like to view a list of NotEnoughUpdate's commands.");
						clickTextHelp.setChatStyle(Utils.createClickStyle(ClickEvent.Action.RUN_COMMAND, "/neuhelp"));
						Minecraft.getMinecraft().thePlayer.addChatMessage(clickTextHelp);
						Utils.addChatMessage("");
					}
				}
			}
			if (currentTime - lastSkyblockScoreboard < 5 * 60 * 1000) { //5 minutes
				neu.manager.auctionManager.tick();
			}
		}
	}
}
