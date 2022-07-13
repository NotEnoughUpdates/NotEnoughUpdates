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

import com.google.common.collect.Iterables;
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
import io.github.moulberry.notenoughupdates.miscgui.StorageOverlay;
import io.github.moulberry.notenoughupdates.overlays.OverlayManager;
import io.github.moulberry.notenoughupdates.overlays.TextOverlay;
import io.github.moulberry.notenoughupdates.util.Constants;
import io.github.moulberry.notenoughupdates.util.NotificationHandler;
import io.github.moulberry.notenoughupdates.util.ProfileApiSyncer;
import io.github.moulberry.notenoughupdates.util.SBInfo;
import io.github.moulberry.notenoughupdates.util.Utils;
import io.github.moulberry.notenoughupdates.util.XPInformation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
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

	private void displayUpdateMessage(JsonObject updateJson, String updateMessage, String downloadLink) {
		int firstWidth = -1;

		for (String line : Iterables.concat(Arrays.asList(updateMessage.split("\n")), Arrays.asList("Download here"))) {
			FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
			boolean isDownloadLink = line.equals("Download here");
			int width = fr.getStringWidth(line);
			if (firstWidth == -1) {
				firstWidth = width;
			}
			int missingLen = firstWidth - width;
			if (missingLen > 0) {
				StringBuilder sb = new StringBuilder(missingLen / 4 / 2 +line.length());
				for (int i = 0; i < missingLen / 4 / 2; i++) { /* fr.getCharWidth(' ') == 4 */
					sb.append(" ");
				}
				sb.append(line);
				line = sb.toString();
			}
			ChatComponentText cp = new ChatComponentText(line);
			if (isDownloadLink)
				cp.setChatStyle(Utils.createClickStyle(ClickEvent.Action.OPEN_URL, downloadLink));
			Minecraft.getMinecraft().thePlayer.addChatMessage(cp);
		}
		neu.displayLinks(updateJson, firstWidth);
		NotificationHandler.displayNotification(Arrays.asList(
			"§eThere is a new version of NotEnoughUpdates available.",
			"§eCheck the chat for more information"
		), true);
	}

	private void displayUpdateMessageIfOutOfDate() {
		File repo = neu.manager.repoLocation;
		if (repo.exists()) {
			File updateJson = new File(repo, "update.json");
			try {
				JsonObject o = neu.manager.getJsonFromFile(updateJson);

				int fullReleaseVersion =
					o.has("version_id") && o.get("version_id").isJsonPrimitive() ? o.get("version_id").getAsInt() : -1;
				int preReleaseVersion =
					o.has("version_id") && o.get("version_id").isJsonPrimitive() ? o.get("version_id").getAsInt() : -1;
				int hotfixVersion =
					o.has("hotfix_id") && o.get("hotfix_id").isJsonPrimitive() ? o.get("hotfix_id").getAsInt() : -1;

				boolean hasFullReleaseAvailableForUpgrade = fullReleaseVersion > NotEnoughUpdates.VERSION_ID;
				boolean hasHotfixAvailableForUpgrade =
					fullReleaseVersion == NotEnoughUpdates.VERSION_ID && hotfixVersion > NotEnoughUpdates.HOTFIX_VERSION_ID;
				boolean hasPreReleaseAvailableForUpdate =
					fullReleaseVersion == NotEnoughUpdates.VERSION_ID && preReleaseVersion > NotEnoughUpdates.PRE_VERSION_ID;

				int updateChannel = NotEnoughUpdates.INSTANCE.config.notifications.updateChannel; /* 1 = Full, 2 = Pre */
				if (hasFullReleaseAvailableForUpgrade || (hasHotfixAvailableForUpgrade && updateChannel == 1)) {
					displayUpdateMessage(o, o.get("update_msg").getAsString(), o.get("update_link").getAsString());
				} else if (hasPreReleaseAvailableForUpdate && updateChannel == 2) {
					displayUpdateMessage(o, o.get("pre_update_msg").getAsString(), o.get("pre_update_link").getAsString());
				}
			} catch (Exception e) {
				e.printStackTrace();
				Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(
					"§e[NEU] §cThere has been an error checking for updates. Check the log or join the discord for more information.").setChatStyle(
					Utils.createClickStyle(
						ClickEvent.Action.OPEN_URL, "https://discord.gg/moulberry")));
			}
		}
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
		}

		if (longUpdate) {
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
						displayUpdateMessageIfOutOfDate();
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
							Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(
								"" + EnumChatFormatting.DARK_RED + EnumChatFormatting.BOLD + "WARNING: " + EnumChatFormatting.RESET +
									EnumChatFormatting.RED + "Could not load Feature List URL from repo."));
							Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(
								"" + EnumChatFormatting.RED + "Please run " + EnumChatFormatting.BOLD + "/neuresetrepo" +
									EnumChatFormatting.RESET + EnumChatFormatting.RED + " and " + EnumChatFormatting.BOLD +
									"restart your game" + EnumChatFormatting.RESET + EnumChatFormatting.RED + " in order to fix. " +
									EnumChatFormatting.DARK_RED + EnumChatFormatting.BOLD + "If that doesn't fix it" +
									EnumChatFormatting.RESET + EnumChatFormatting.RED +
									", please join discord.gg/moulberry and post in #neu-support"));
							Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(""));
							Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(
								"" + EnumChatFormatting.GOLD + "To view the feature list after restarting type /neufeatures"));
						} else {
							String url = Constants.MISC.get("featureslist").getAsString();
							Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(""));
							Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(
								EnumChatFormatting.BLUE + "It seems this is your first time using NotEnoughUpdates."));
							ChatComponentText clickTextFeatures = new ChatComponentText(EnumChatFormatting.YELLOW +
								"Click this message if you would like to view a list of NotEnoughUpdate's Features.");
							clickTextFeatures.setChatStyle(Utils.createClickStyle(ClickEvent.Action.OPEN_URL, url));
							Minecraft.getMinecraft().thePlayer.addChatMessage(clickTextFeatures);
						}
						Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(""));
						ChatComponentText clickTextHelp = new ChatComponentText(EnumChatFormatting.YELLOW +
							"Click this message if you would like to view a list of NotEnoughUpdate's commands.");
						clickTextHelp.setChatStyle(Utils.createClickStyle(ClickEvent.Action.RUN_COMMAND, "/neuhelp"));
						Minecraft.getMinecraft().thePlayer.addChatMessage(clickTextHelp);
						Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(""));
					}
				}
			}
			if (currentTime - lastSkyblockScoreboard < 5 * 60 * 1000) { //5 minutes
				neu.manager.auctionManager.tick();
			} else {
				neu.manager.auctionManager.markNeedsUpdate();
			}
		}
	}
}
