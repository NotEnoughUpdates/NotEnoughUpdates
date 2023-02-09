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

package io.github.moulberry.notenoughupdates.miscfeatures.updater;

import com.google.common.collect.Iterables;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.util.MoulSigner;
import io.github.moulberry.notenoughupdates.util.NotificationHandler;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.fml.common.Loader;
import org.apache.commons.lang3.SystemUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class AutoUpdater {

	NotEnoughUpdates neu;

	public AutoUpdater(NotEnoughUpdates neu) {
		this.neu = neu;
	}

	public void logProgress(String str) {
		logProgress(new ChatComponentText(str));
	}

	public void logProgress(IChatComponent chatComponent) {
		Minecraft.getMinecraft().addScheduledTask(() -> {
			IChatComponent chatComponent1 = new ChatComponentText("");
			chatComponent1.setChatStyle(new ChatStyle().setColor(EnumChatFormatting.AQUA));
			chatComponent1.appendSibling(chatComponent);
			Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("§e[NEU-AutoUpdater] ").appendSibling(
				chatComponent1));
		});
	}

	public UpdateLoader getUpdateLoader(URL url) {
		if (SystemUtils.IS_OS_UNIX) {
			return new LinuxBasedUpdater(this, url);
		}
		if (Loader.isModLoaded("skyblockclientupdater") && SCUCompatUpdater.IS_ENABLED) {
			return SCUCompatUpdater.tryCreate(this, url);
		}
		return null;
	}

	UpdateLoader updateLoader;

	public void updateFromURL(URL url) {
		if (updateLoader != null) {
			logProgress(
				"There is already an update in progress, so the auto updater cannot process this update (as it might already be installed or is currently being downloaded). Please restart your client to install another update");
			return;
		}
		if (!"https".equals(url.getProtocol())) {
			logProgress("§cInvalid protocol in url: " + url + ". Only https is a valid protocol.");
			return;
		}
		updateLoader = getUpdateLoader(url);
		if (updateLoader == null) {
			return;
		}
		updateLoader.greet();
		logProgress(new ChatComponentText("[Start download now]")
			.setChatStyle(new ChatStyle()
				.setColor(EnumChatFormatting.GREEN)
				.setChatHoverEvent(new HoverEvent(
					HoverEvent.Action.SHOW_TEXT,
					new ChatComponentText("Click to start download.")
				))
				.setChatClickEvent(new ClickEvent(
					ClickEvent.Action.RUN_COMMAND,
					"/neuupdate scheduleDownload"
				))));
	}

	public void scheduleDownload() {
		if (updateLoader == null) {
			logProgress("§cNo update found. Try running /neuupdate check first");
			return;
		}
		if (updateLoader.getState() != UpdateLoader.State.NOTHING) {
			logProgress("§cUpdate download already started. No need to start the download again.");
			return;
		}
		logProgress("Download started.");
		updateLoader.scheduleDownload();
	}

	private void displayUpdateMessage(
		JsonObject updateJson,
		String updateMessage,
		String downloadLink,
		String directDownload
	) {
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
				StringBuilder sb = new StringBuilder(missingLen / 4 / 2 + line.length());
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
			"",
			"§eThere is a new version of NotEnoughUpdates available.",
			"§eCheck the chat for more information"
		), true);
		try {
			if (directDownload != null)
				updateFromURL(new URL(directDownload));
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}

	public void displayUpdateMessageIfOutOfDate() {
		File repo = neu.manager.repoLocation;
		File updateJson = new File(repo, "update.json");
		if (updateJson.exists()) {
			if (!MoulSigner.verifySignature(updateJson)) {
				NotEnoughUpdates.LOGGER.error("update.json found without signature");
				Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(
					"§e[NEU] §cThere has been an error checking for updates. Check the log or join the discord for more information.").setChatStyle(
					Utils.createClickStyle(
						ClickEvent.Action.OPEN_URL, "https://discord.gg/moulberry")));
				return;
			}
			try {
				JsonObject o = neu.manager.getJsonFromFile(updateJson);

				int fullReleaseVersion =
					o.has("version_id") && o.get("version_id").isJsonPrimitive() ? o.get("version_id").getAsInt() : -1;
				int preReleaseVersion =
					o.has("pre_version_id") && o.get("pre_version_id").isJsonPrimitive() ? o.get("pre_version_id").getAsInt() : -1;
				int hotfixVersion =
					o.has("hotfix_id") && o.get("hotfix_id").isJsonPrimitive() ? o.get("hotfix_id").getAsInt() : -1;

				boolean hasFullReleaseAvailableForUpgrade = fullReleaseVersion > NotEnoughUpdates.VERSION_ID;
				boolean hasHotfixAvailableForUpgrade =
					fullReleaseVersion == NotEnoughUpdates.VERSION_ID && hotfixVersion > NotEnoughUpdates.HOTFIX_VERSION_ID;
				boolean hasPreReleaseAvailableForUpdate =
					fullReleaseVersion == NotEnoughUpdates.VERSION_ID && preReleaseVersion > NotEnoughUpdates.PRE_VERSION_ID;

				int updateChannel = NotEnoughUpdates.INSTANCE.config.notifications.updateChannel; /* 1 = Full, 2 = Pre */
				if (hasFullReleaseAvailableForUpgrade || (hasHotfixAvailableForUpgrade && updateChannel == 1)) {
					displayUpdateMessage(
						o,
						o.get("update_msg").getAsString(),
						o.get("update_link").getAsString(),
						o.has("update_direct") ? o.get("update_direct").getAsString() : null
					);
				} else if (hasPreReleaseAvailableForUpdate && updateChannel == 2) {
					displayUpdateMessage(
						o,
						o.get("pre_update_msg").getAsString(),
						o.get("pre_update_link").getAsString(),
						o.has("pre_update_direct") ? o.get("pre_update_direct").getAsString() : null
					);
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

	private boolean validateMcModInfo(JsonArray array) {
		if (array.size() != 1) return false;
		JsonElement jsonElement = array.get(0);
		if (!jsonElement.isJsonObject()) return false;
		JsonObject jsonObject = jsonElement.getAsJsonObject();
		if (!jsonObject.has("modid")) return false;
		JsonElement modid = jsonObject.get("modid");
		if (!modid.isJsonPrimitive()) return false;
		JsonPrimitive primitive = modid.getAsJsonPrimitive();
		if (!primitive.isString()) return false;
		return "notenoughupdates".equals(primitive.getAsString());
	}

	public boolean isNeuJar(File sus) {
		try (ZipFile zipFile = new ZipFile(sus)) {
			ZipEntry entry = zipFile.getEntry("mcmod.info");
			if (entry == null) {
				return false;
			}
			try (InputStream inputStream = zipFile.getInputStream(entry)) {
				JsonArray jsonArray = neu.manager.gson.fromJson(
					new InputStreamReader(inputStream),
					JsonArray.class
				);
				return validateMcModInfo(jsonArray);
			}
		} catch (IOException | JsonSyntaxException e) {
			e.printStackTrace();
		}
		return false;
	}
}
