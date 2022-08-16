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

package io.github.moulberry.notenoughupdates.util;

import com.google.common.reflect.TypeToken;
import com.google.gson.JsonObject;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.listener.ScoreboardLocationChangeListener;
import io.github.moulberry.notenoughupdates.miscfeatures.customblockzones.LocationChangeEvent;
import io.github.moulberry.notenoughupdates.overlays.SlayerOverlay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SBInfo {
	private static final SBInfo INSTANCE = new SBInfo();

	public static SBInfo getInstance() {
		return INSTANCE;
	}

	private static final Pattern timePattern = Pattern.compile(".+(am|pm)");

	public IChatComponent footer;
	public IChatComponent header;

	public String location = "";
	public String date = "";
	public String time = "";
	public String objective = "";
	public String slayer = "";
	public boolean stranded = false;

	public String mode = null;

	public Date currentTimeDate = null;

	/**
	 * Use Utils.getOpenChestName() instead
	 */
	@Deprecated
	public String currentlyOpenChestName = "";

	private long lastManualLocRaw = -1;
	private long lastLocRaw = -1;
	public long joinedWorld = -1;
	public long unloadedWorld = -1;
	private JsonObject locraw = null;
	public boolean isInDungeon = false;
	public boolean hasNewTab = false;

	public enum Gamemode {
		NORMAL("", ""), IRONMAN("Ironman", "♲"), STRANDED("Stranded", "☀");

		private final String name;
		private final String emoji;

		Gamemode(String name, String emoji) {
			this.name = name;
			this.emoji = emoji;
		}

		public static Gamemode find(String type) {
			for (Gamemode gamemode : values()) {
				if (type.contains(gamemode.name))
					return gamemode;
			}
			return null;
		}
	}

	private Map<String, Gamemode> gamemodes = new HashMap<>();
	private boolean areGamemodesLoaded = false;
	private int tickCount = 0;
	public String currentProfile = null;

	@SubscribeEvent
	public void onGuiOpen(GuiOpenEvent event) {
		if (!NotEnoughUpdates.INSTANCE.hasSkyblockScoreboard()) return;

		if (event.gui instanceof GuiChest) {
			GuiChest chest = (GuiChest) event.gui;
			ContainerChest container = (ContainerChest) chest.inventorySlots;

			currentlyOpenChestName = container.getLowerChestInventory().getDisplayName().getUnformattedText();
		} else {
			currentlyOpenChestName = "";
		}
	}

	@SubscribeEvent
	public void onGuiTick(TickEvent event) {
		if (tickCount++ % 10 != 0) return;
		if (Utils.getOpenChestName().equals("Profile Management")) {
			ContainerChest container = (ContainerChest) ((GuiChest) Minecraft.getMinecraft().currentScreen).inventorySlots;
			updateProfileInformation(container);
		}
	}

	public boolean checkForSkyblockLocation() {
		if (!NotEnoughUpdates.INSTANCE.hasSkyblockScoreboard() || getLocation() == null) {
			Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.RED +
				"[NEU] This command is not available outside SkyBlock"));
			return false;
		}

		return true;
	}

	private static final Pattern PROFILE_PATTERN =
		Pattern.compile("(?<type>(♲ Ironman)|(☀ Stranded)|()) *Profile: (?<name>[^ ]+)");

	private void updateProfileInformation(ContainerChest container) {
		for (int i = 11; i < 16; i = -~i) {
			Slot slot = container.getSlot(i);
			if (slot == null || !slot.getHasStack()) continue;
			ItemStack item = slot.getStack();
			if (item == null || item.getItem() == Item.getItemFromBlock(Blocks.bedrock)) continue;
			String displayName = Utils.cleanColour(item.getDisplayName());
			Matcher matcher = PROFILE_PATTERN.matcher(displayName);
			if (!matcher.matches()) continue;
			String type = matcher.group("type");
			String name = matcher.group("name");
			Gamemode gamemode = Gamemode.find(type);
			gamemodes.put(name, gamemode);
		}
		areGamemodesLoaded = true;
		saveGameModes();
	}

	private Path getProfilesFile() {
		return new File(NotEnoughUpdates.INSTANCE.manager.configLocation, "profiles.json").toPath();
	}

	public Map<String, Gamemode> getAllGamemodes() {
		if (!areGamemodesLoaded)
			loadGameModes();
		return gamemodes;
	}

	public void saveGameModes() {
		try {
			Files.write(
				getProfilesFile(),
				NotEnoughUpdates.INSTANCE.manager.gson.toJson(gamemodes).getBytes(StandardCharsets.UTF_8)
			);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Gamemode getGamemodeForProfile(String profiles) {
		return getAllGamemodes().get(profiles);
	}

	public Gamemode getCurrentMode() {
		return getGamemodeForProfile(currentProfile);
	}

	public void loadGameModes() {
		try {
			gamemodes = NotEnoughUpdates.INSTANCE.manager.gson.fromJson(
				Files.newBufferedReader(getProfilesFile()),
				new TypeToken<Map<String, Gamemode>>() {
				}.getType()
			);
			areGamemodesLoaded = true;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@SubscribeEvent
	public void onWorldLoad(WorldEvent.Load event) {
		lastLocRaw = -1;
		locraw = null;
		this.setLocation(null);
		joinedWorld = System.currentTimeMillis();
		currentlyOpenChestName = "";
		hasNewTab = false;
	}

	@SubscribeEvent
	public void onWorldUnload(WorldEvent.Unload event) {
		unloadedWorld = System.currentTimeMillis();
	}

	private static final Pattern JSON_BRACKET_PATTERN = Pattern.compile("^\\{.+}");

	public void onSendChatMessage(String msg) {
		if (msg.trim().startsWith("/locraw") || msg.trim().startsWith("/locraw ")) {
			lastManualLocRaw = System.currentTimeMillis();
		}
	}

	@SubscribeEvent(priority = EventPriority.LOW, receiveCanceled = true)
	public void onChatMessage(ClientChatReceivedEvent event) {
		Matcher matcher = JSON_BRACKET_PATTERN.matcher(event.message.getUnformattedText());
		if (matcher.find()) {
			try {
				JsonObject obj = NotEnoughUpdates.INSTANCE.manager.gson.fromJson(matcher.group(), JsonObject.class);
				if (obj.has("server")) {
					if (System.currentTimeMillis() - lastManualLocRaw > 5000) event.setCanceled(true);
					if (obj.has("gametype") && obj.has("mode") && obj.has("map")) {
						locraw = obj;
						setLocation(locraw.get("mode").getAsString());
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public String getLocation() {
		return mode;
	}

	public void setLocation(String location) {
		if (!Objects.equals(this.mode, location)) {
			MinecraftForge.EVENT_BUS.post(new LocationChangeEvent(location, this.mode));
		}
		this.mode = location;
	}

	private static final String profilePrefix = "\u00a7r\u00a7e\u00a7lProfile: \u00a7r\u00a7a";
	private static final String skillsPrefix = "\u00a7r\u00a7e\u00a7lSkills: \u00a7r\u00a7a";

	private static final Pattern SKILL_LEVEL_PATTERN = Pattern.compile("([^0-9:]+) (\\d{1,2})");

	public void tick() {
		boolean tempIsInDungeon = false;

		long currentTime = System.currentTimeMillis();

		if (Minecraft.getMinecraft().thePlayer != null &&
			Minecraft.getMinecraft().theWorld != null &&
			locraw == null &&
			(currentTime - joinedWorld) > 1000 &&
			(currentTime - lastLocRaw) > 15000) {
			lastLocRaw = System.currentTimeMillis();
			NotEnoughUpdates.INSTANCE.sendChatMessage("/locraw");
		}

		try {
			for (NetworkPlayerInfo info : Minecraft.getMinecraft().thePlayer.sendQueue.getPlayerInfoMap()) {
				String name = Minecraft.getMinecraft().ingameGUI.getTabList().getPlayerName(info);
				if (name.startsWith(profilePrefix)) {
					currentProfile = Utils.cleanColour(name.substring(profilePrefix.length()));
					hasNewTab = true;
				} else if (name.startsWith(skillsPrefix)) {
					String levelInfo = name.substring(skillsPrefix.length()).trim();
					Matcher matcher = SKILL_LEVEL_PATTERN.matcher(Utils.cleanColour(levelInfo).split(":")[0]);
					if (matcher.find()) {
						try {
							int level = Integer.parseInt(matcher.group(2).trim());
							XPInformation.getInstance().updateLevel(matcher.group(1).toLowerCase().trim(), level);
						} catch (Exception ignored) {
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			Scoreboard scoreboard = Minecraft.getMinecraft().thePlayer.getWorldScoreboard();

			ScoreObjective sidebarObjective = scoreboard.getObjectiveInDisplaySlot(1);

			List<Score> scores = new ArrayList<>(scoreboard.getSortedScores(sidebarObjective));

			List<String> lines = new ArrayList<>();
			for (int i = scores.size() - 1; i >= 0; i--) {
				Score score = scores.get(i);
				ScorePlayerTeam scoreplayerteam1 = scoreboard.getPlayersTeam(score.getPlayerName());
				String line = ScorePlayerTeam.formatPlayerName(scoreplayerteam1, score.getPlayerName());
				line = Utils.cleanDuplicateColourCodes(line);

				String cleanLine = Utils.cleanColour(line);

				if (cleanLine.contains("Cleared:") && cleanLine.contains("%")) {
					tempIsInDungeon = true;
				}

				lines.add(line);
			}
			isInDungeon = tempIsInDungeon;

			boolean containsStranded = false;
			for (String line : lines) { //Slayer stuff
				if (line.contains("Tarantula Broodfather")) {
					slayer = "Tarantula";
				} else if (line.contains("Revenant Horror")) {
					slayer = "Revenant";
				} else if (line.contains("Sven Packmaster")) {
					slayer = "Sven";
				} else if (line.contains("Voidgloom Seraph")) {
					slayer = "Enderman";
				} else if (line.contains("Inferno Demonlord")) {
					slayer = "Blaze";
				}
				if (lines.contains("Slayer Quest") && SlayerOverlay.unloadOverlayTimer == -1 ||
					lines.contains("Slayer Quest") && System.currentTimeMillis() - SlayerOverlay.unloadOverlayTimer > 500) {
					SlayerOverlay.slayerQuest = true;
				}
				if (SlayerOverlay.slayerQuest) {
					if (line.contains(" I")) {
						SlayerOverlay.slayerTier = 1;
					}
					if (line.contains(" II")) {
						SlayerOverlay.slayerTier = 2;
					}
					if (line.contains(" III")) {
						SlayerOverlay.slayerTier = 3;
					}
					if (line.contains(" IV")) {
						SlayerOverlay.slayerTier = 4;
					}
					if (line.contains(" V")) {
						SlayerOverlay.slayerTier = 5;
					}
				}
				if (line.contains("☀ Stranded")) containsStranded = true;
			}
			stranded = containsStranded;

			if (lines.size() >= 5) {
				date = Utils.cleanColour(lines.get(1)).trim();
				//§74:40am
				Matcher matcher = timePattern.matcher(lines.get(2));
				if (matcher.find()) {
					time = Utils.cleanColour(matcher.group()).trim();
					try {
						String timeSpace = time.replace("am", " am").replace("pm", " pm");
						SimpleDateFormat parseFormat = new SimpleDateFormat("hh:mm a");
						currentTimeDate = parseFormat.parse(timeSpace);
					} catch (ParseException ignored) {
					}
				}
				//Replaced with for loop because in crystal hollows with events the line it's on can shift.
				for (String line : lines) {
					if (line.contains("⏣")) {
						String l = Utils.cleanColour(line).replaceAll("[^A-Za-z0-9() ]", "").trim();
						if (!l.equals(location)) {
							new ScoreboardLocationChangeListener(location, l);
						}
						location = l;
						break;
					}
				}
			}
			objective = null;

			boolean objTextLast = false;
			for (String line : lines) {
				if (objTextLast) {
					objective = line;
				}

				objTextLast = line.equals("Objective");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
