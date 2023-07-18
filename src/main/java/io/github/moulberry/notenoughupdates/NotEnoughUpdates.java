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

package io.github.moulberry.notenoughupdates;

import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import io.github.moulberry.notenoughupdates.autosubscribe.AutoLoad;
import io.github.moulberry.notenoughupdates.autosubscribe.NEUAutoSubscribe;
import io.github.moulberry.notenoughupdates.core.BackgroundBlur;
import io.github.moulberry.notenoughupdates.cosmetics.ShaderManager;
import io.github.moulberry.notenoughupdates.listener.ChatListener;
import io.github.moulberry.notenoughupdates.listener.ItemTooltipEssenceShopListener;
import io.github.moulberry.notenoughupdates.listener.ItemTooltipListener;
import io.github.moulberry.notenoughupdates.listener.ItemTooltipRngListener;
import io.github.moulberry.notenoughupdates.listener.NEUEventListener;
import io.github.moulberry.notenoughupdates.listener.RenderListener;
import io.github.moulberry.notenoughupdates.listener.WorldListener;
import io.github.moulberry.notenoughupdates.miscfeatures.CustomSkulls;
import io.github.moulberry.notenoughupdates.miscfeatures.FairySouls;
import io.github.moulberry.notenoughupdates.miscfeatures.ItemCustomizeManager;
import io.github.moulberry.notenoughupdates.miscfeatures.NPCRetexturing;
import io.github.moulberry.notenoughupdates.miscfeatures.Navigation;
import io.github.moulberry.notenoughupdates.miscfeatures.PetInfoOverlay;
import io.github.moulberry.notenoughupdates.miscfeatures.SlotLocking;
import io.github.moulberry.notenoughupdates.miscfeatures.StorageManager;
import io.github.moulberry.notenoughupdates.miscfeatures.customblockzones.CustomBlockSounds;
import io.github.moulberry.notenoughupdates.miscfeatures.inventory.MuseumCheapestItemOverlay;
import io.github.moulberry.notenoughupdates.miscfeatures.inventory.MuseumItemHighlighter;
import io.github.moulberry.notenoughupdates.miscfeatures.updater.AutoUpdater;
import io.github.moulberry.notenoughupdates.mixins.AccessorMinecraft;
import io.github.moulberry.notenoughupdates.oneconfig.IOneConfigCompat;
import io.github.moulberry.notenoughupdates.options.NEUConfig;
import io.github.moulberry.notenoughupdates.overlays.OverlayManager;
import io.github.moulberry.notenoughupdates.profileviewer.ProfileViewer;
import io.github.moulberry.notenoughupdates.recipes.RecipeGenerator;
import io.github.moulberry.notenoughupdates.util.Utils;
import io.github.moulberry.notenoughupdates.util.brigadier.BrigadierRoot;
import io.github.moulberry.notenoughupdates.util.hypixelapi.HypixelItemAPI;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.event.ClickEvent;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.BiomeGenHell;
import net.minecraft.world.biome.BiomeGenJungle;
import net.minecraft.world.biome.BiomeGenMesa;
import net.minecraft.world.biome.BiomeGenSnow;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Set;

@NEUAutoSubscribe
@Mod(
	modid = NotEnoughUpdates.MODID, version = NotEnoughUpdates.VERSION, clientSideOnly = true, useMetadata = true,
	guiFactory = "io.github.moulberry.notenoughupdates.core.config.MoulConfigGuiForgeInterop")
public class NotEnoughUpdates {
	public static final String MODID = "notenoughupdates";
	public static final String VERSION = "2.1.1-PRE";
	public static final int VERSION_ID = 20101; //2.1.1 only so update notif works
	public static final int PRE_VERSION_ID = 0;
	public static final int HOTFIX_VERSION_ID = 0;

	public static final Logger LOGGER = LogManager.getLogger("NotEnoughUpdates");
	/**
	 * Registers the biomes for the crystal hollows here so optifine knows they exists
	 */
	public static final BiomeGenBase crystalHollowsJungle =
		(new BiomeGenJungle(101, true))
			.setColor(5470985)
			.setBiomeName("NeuCrystalHollowsJungle")
			.setFillerBlockMetadata(5470985)
			.setTemperatureRainfall(0.95F, 0.9F);
	public static final BiomeGenBase crystalHollowsMagmaFields =
		(new BiomeGenHell(102))
			.setColor(16711680)
			.setBiomeName("NeuCrystalHollowsMagmaFields")
			.setDisableRain()
			.setTemperatureRainfall(2.0F, 0.0F);
	public static final BiomeGenBase crystalHollowsGoblinHoldout =
		(new BiomeGenMesa(103, false, false))
			.setColor(13274213)
			.setBiomeName("NeuCrystalHollowsGoblinHoldout");
	public static final BiomeGenBase crystalHollowsPrecursorRemnants =
		(new BiomeGenMesa(104, false, true))
			.setColor(11573093)
			.setBiomeName("NeuCrystalHollowsPrecursorRemnants");
	public static final BiomeGenBase crystalHollowsMithrilDeposit =
		(new BiomeGenSnow(105, false))
			.setColor(16777215)
			.setBiomeName("NeuCrystalHollowsMithrilDeposits");
	public static final BiomeGenBase crystalHollowsCrystalNucleus =
		(new BiomeGenJungle(106, true))
			.setColor(5470985)
			.setBiomeName("NeuCrystalHollowsCrystalNucleus")
			.setFillerBlockMetadata(5470985)
			.setTemperatureRainfall(0.95F, 0.9F);
	private static final long CHAT_MSG_COOLDOWN = 200;
	//Stolen from Biscut and used for detecting whether in skyblock
	private static final Set<String> SKYBLOCK_IN_ALL_LANGUAGES =
		Sets.newHashSet("SKYBLOCK", "\u7A7A\u5C9B\u751F\u5B58", "\u7A7A\u5CF6\u751F\u5B58",
			"SKIBLOCK"
		); // april fools language
	public static NotEnoughUpdates INSTANCE = null;
	public static HashMap<String, String> petRarityToColourMap = new HashMap<String, String>() {{
		put("UNKNOWN", EnumChatFormatting.RED.toString());
		put("COMMON", EnumChatFormatting.WHITE.toString());
		put("UNCOMMON", EnumChatFormatting.GREEN.toString());
		put("RARE", EnumChatFormatting.BLUE.toString());
		put("EPIC", EnumChatFormatting.DARK_PURPLE.toString());
		put("LEGENDARY", EnumChatFormatting.GOLD.toString());
		put("MYTHIC", EnumChatFormatting.LIGHT_PURPLE.toString());
	}};
	public static ProfileViewer profileViewer;
	private final Gson gson = new GsonBuilder().setPrettyPrinting().excludeFieldsWithoutExposeAnnotation().create();
	public NEUManager manager;
	public NEUOverlay overlay;
	public NEUConfig config;
	public Navigation navigation = new Navigation(this);
	public GuiScreen openGui = null;
	public long lastOpenedGui = 0;
	public boolean packDevEnabled = false;
	public Color[][] colourMap = null;
	public AutoUpdater autoUpdater = new AutoUpdater(this);
	private File configFile;
	private long lastChatMessage = 0;
	private long secondLastChatMessage = 0;
	private String currChatMessage = null;
	private File neuDir;
	private boolean hasSkyblockScoreboard;

	public NotEnoughUpdates() {
		// Budget Construction Event
		((AccessorMinecraft) FMLClientHandler.instance().getClient())
			.onGetDefaultResourcePacks()
			.add(new NEURepoResourcePack(null, "neurepo"));
	}

	public File getConfigFile() {
		return this.configFile;
	}

	public void newConfigFile() {
		this.configFile = new File(NotEnoughUpdates.INSTANCE.getNeuDir(), "configNew.json");
	}

	public File getNeuDir() {
		return this.neuDir;
	}

	/**
	 * Instantiates NEUIo, NEUManager and NEUOverlay instances. Registers keybinds and adds a shutdown hook to clear tmp folder.
	 */
	@EventHandler
	public void preinit(FMLPreInitializationEvent event) {
		INSTANCE = this;

		neuDir = new File(event.getModConfigurationDirectory(), "notenoughupdates");
		neuDir.mkdirs();

		configFile = new File(neuDir, "configNew.json");

		if (configFile.exists()) {
			try (
				BufferedReader reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(configFile),
					StandardCharsets.UTF_8
				))
			) {
				config = gson.fromJson(reader, NEUConfig.class);
			} catch (Exception exc) {
				new RuntimeException("Invalid config file. This will reset the config to default", exc).printStackTrace();
			}
		}

		ItemCustomizeManager.loadCustomization(new File(neuDir, "itemCustomization.json"));
		StorageManager.getInstance().loadConfig(new File(neuDir, "storageItems.json"));
		FairySouls.getInstance().loadFoundSoulsForAllProfiles(new File(neuDir, "collected_fairy_souls.json"), gson);
		PetInfoOverlay.loadConfig(new File(neuDir, "petCache.json"));
		SlotLocking.getInstance().loadConfig(new File(neuDir, "slotLocking.json"));
		ItemPriceInformation.init(new File(neuDir, "auctionable_items.json"), gson);

		if (config == null) {
			config = new NEUConfig();
			saveConfig();
		} else {
			if (config.apiKey != null && config.apiKey.apiKey != null) {
				config.apiData.apiKey = config.apiKey.apiKey;
				config.apiKey = null;
			}

			//add the trophy fishing tab to the config
			if (config.profileViewer.pageLayout.size() == 8) {
				config.profileViewer.pageLayout.add(8);
			}
			if (config.profileViewer.pageLayout.size() == 9) {
				config.profileViewer.pageLayout.add(9);
			}
			if (config.profileViewer.pageLayout.size() == 10) {
				config.profileViewer.pageLayout.add(10);
			}
			if (config.profileViewer.pageLayout.size() == 11) {
				config.profileViewer.pageLayout.add(11);
			}
			if (config.profileViewer.pageLayout.size() == 12) {
				config.profileViewer.pageLayout.add(12);
			}

			// Remove after 2.1 ig
			if ("dangerous".equals(config.apiData.repoBranch)) {
				config.apiData.repoBranch = "prerelease";
			}

			// Remove before 2.1.1 release
			if ("master".equals(config.apiData.repoBranch)) {
				config.apiData.repoBranch = "prerelease";
			}

			if (config.apiData.moulberryCodesApi.isEmpty()) {
				config.apiData.moulberryCodesApi = "moulberry.codes";
			}
			if (config.ahGraph.serverUrl.trim().isEmpty()) {
				config.ahGraph.serverUrl = "pricehistory.notenoughupdates.org";
			}

			saveConfig();
		}

		if (config != null)
			if (config.mining.powderGrindingTrackerResetMode == 2)
				OverlayManager.powderGrindingOverlay.load();

		IOneConfigCompat.getInstance().ifPresent(it -> it.initConfig(config, this::saveConfig));

		MinecraftForge.EVENT_BUS.register(new NEUEventListener(this));
		MinecraftForge.EVENT_BUS.register(new RecipeGenerator(this));
		MinecraftForge.EVENT_BUS.register(OverlayManager.petInfoOverlay);
		MinecraftForge.EVENT_BUS.register(OverlayManager.timersOverlay);
		MinecraftForge.EVENT_BUS.register(new ChatListener(this));
		MinecraftForge.EVENT_BUS.register(new ItemTooltipListener(this));
		MinecraftForge.EVENT_BUS.register(new ItemTooltipRngListener(this));
		MinecraftForge.EVENT_BUS.register(new ItemTooltipEssenceShopListener(this));
		MinecraftForge.EVENT_BUS.register(new RenderListener(this));
		MinecraftForge.EVENT_BUS.register(navigation);
		MinecraftForge.EVENT_BUS.register(new WorldListener(this));
		AutoLoad.INSTANCE.provide(supplier -> MinecraftForge.EVENT_BUS.register(supplier.get()));
		MinecraftForge.EVENT_BUS.register(MuseumItemHighlighter.INSTANCE);
		MinecraftForge.EVENT_BUS.register(MuseumCheapestItemOverlay.INSTANCE);

		if (Minecraft.getMinecraft().getResourceManager() instanceof IReloadableResourceManager) {
			IReloadableResourceManager manager = (IReloadableResourceManager) Minecraft.getMinecraft().getResourceManager();
			manager.registerReloadListener(CustomSkulls.getInstance());
			manager.registerReloadListener(NPCRetexturing.getInstance());
			manager.registerReloadListener(ShaderManager.getInstance());
			manager.registerReloadListener(new ItemCustomizeManager.ReloadListener());
			manager.registerReloadListener(new CustomBlockSounds.ReloaderListener());
		}

		BrigadierRoot.INSTANCE.updateHooks();

		BackgroundBlur.registerListener();

		manager = new NEUManager(this, neuDir);
		manager.loadItemInformation();
		overlay = new NEUOverlay(manager);
		profileViewer = new ProfileViewer(manager);
		HypixelItemAPI.INSTANCE.loadItemData();

		for (KeyBinding kb : manager.keybinds) {
			ClientRegistry.registerKeyBinding(kb);
		}

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			File tmp = new File(neuDir, "tmp");
			if (tmp.exists()) {
				for (File tmpFile : tmp.listFiles()) {
					tmpFile.delete();
				}
				tmp.delete();
			}
		}));
	}

	public void saveConfig() {
		try {
			OverlayManager.powderGrindingOverlay.save();
		} catch (Exception ignored) {
		}

		try {
			configFile.createNewFile();

			try (
				BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(configFile),
					StandardCharsets.UTF_8
				))
			) {
				writer.write(gson.toJson(config));
			}
		} catch (Exception ignored) {
		}

		try {
			ItemCustomizeManager.saveCustomization(new File(neuDir, "itemCustomization.json"));
		} catch (Exception ignored) {
		}
		try {
			StorageManager.getInstance().saveConfig(new File(neuDir, "storageItems.json"));
		} catch (Exception ignored) {
		}
		try {
			FairySouls.getInstance().saveFoundSoulsForAllProfiles(new File(neuDir, "collected_fairy_souls.json"), gson);
		} catch (Exception ignored) {
		}
		try {
			PetInfoOverlay.saveConfig(new File(neuDir, "petCache.json"));
		} catch (Exception ignored) {
		}
		try {
			SlotLocking.getInstance().saveConfig(new File(neuDir, "slotLocking.json"));
		} catch (Exception ignored) {
		}
	}

	/**
	 * If the last chat messages was sent >200ms ago, sends the message.
	 * If the last chat message was sent <200 ago, will cache the message for #onTick to handle.
	 */
	public void sendChatMessage(String message) {
		if (System.currentTimeMillis() - lastChatMessage > CHAT_MSG_COOLDOWN) {
			secondLastChatMessage = lastChatMessage;
			lastChatMessage = System.currentTimeMillis();
			Minecraft.getMinecraft().thePlayer.sendChatMessage(message);
			currChatMessage = null;
		} else {
			currChatMessage = message;
		}
	}

	public void trySendCommand(String message) {
		if (ClientCommandHandler.instance.executeCommand(Minecraft.getMinecraft().thePlayer, message) == 0) {
			sendChatMessage(message);
		}
	}

	public void displayLinks(JsonObject update, int totalWidth) {
		String discord_link = update.get("discord_link").getAsString();
		String youtube_link = update.get("youtube_link").getAsString();
		String twitch_link = update.get("twitch_link").getAsString();
		String github_link = update.get("github_link").getAsString();
		String other_text = update.get("other_text").getAsString();
		String other_link = update.get("other_link").getAsString();

		ChatComponentText other = null;
		if (other_text.length() > 0) {
			other = new ChatComponentText(
				EnumChatFormatting.GRAY + "[" + EnumChatFormatting.BLUE + other_text + EnumChatFormatting.GRAY + "]");
			other.setChatStyle(Utils.createClickStyle(ClickEvent.Action.OPEN_URL, other_link));
		}
		ChatComponentText links = new ChatComponentText("");
		ChatComponentText separator = new ChatComponentText(
			EnumChatFormatting.GRAY + EnumChatFormatting.BOLD.toString() + EnumChatFormatting.STRIKETHROUGH + "-");
		ChatComponentText discord = new ChatComponentText(
			EnumChatFormatting.GRAY + "[" + EnumChatFormatting.BLUE + "Discord" + EnumChatFormatting.GRAY + "]");
		discord.setChatStyle(Utils.createClickStyle(ClickEvent.Action.OPEN_URL, discord_link));
		ChatComponentText youtube = new ChatComponentText(
			EnumChatFormatting.GRAY + "[" + EnumChatFormatting.RED + "YouTube" + EnumChatFormatting.GRAY + "]");
		youtube.setChatStyle(Utils.createClickStyle(ClickEvent.Action.OPEN_URL, youtube_link));
		ChatComponentText twitch = new ChatComponentText(
			EnumChatFormatting.GRAY + "[" + EnumChatFormatting.DARK_PURPLE + "Twitch" + EnumChatFormatting.GRAY + "]");
		twitch.setChatStyle(Utils.createClickStyle(ClickEvent.Action.OPEN_URL, twitch_link));
		ChatComponentText github = new ChatComponentText(
			EnumChatFormatting.GRAY + "[" + EnumChatFormatting.DARK_PURPLE + "GitHub" + EnumChatFormatting.GRAY + "]");
		github.setChatStyle(Utils.createClickStyle(ClickEvent.Action.OPEN_URL, github_link));

		links.appendSibling(separator);
		links.appendSibling(discord);
		links.appendSibling(separator);
		links.appendSibling(youtube);
		links.appendSibling(separator);
		links.appendSibling(twitch);
		links.appendSibling(separator);
		links.appendSibling(github);
		links.appendSibling(separator);
		if (other != null) {
			links.appendSibling(other);
			links.appendSibling(separator);
		}
		FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
		int missingWidth = Math.max(0, totalWidth - fr.getStringWidth(links.getFormattedText()));
		int missingCharsOnEitherSide = missingWidth / fr.getStringWidth(EnumChatFormatting.BOLD + "-") / 2;
		StringBuilder sb = new StringBuilder(missingCharsOnEitherSide + 6);
		sb.append(EnumChatFormatting.GRAY);
		sb.append(EnumChatFormatting.BOLD);
		sb.append(EnumChatFormatting.STRIKETHROUGH);
		for (int i = 0; i < missingCharsOnEitherSide; i++) {
			sb.append("-");
		}
		String padding = sb.toString();
		ChatComponentText cp = new ChatComponentText("");
		cp.appendText(padding);
		cp.appendSibling(links);
		cp.appendText(padding);
		Minecraft.getMinecraft().thePlayer.addChatMessage(cp);
	}

	@SubscribeEvent
	public void onTick(TickEvent.ClientTickEvent event) {
		if (event.phase != TickEvent.Phase.START) return;
		if (Minecraft.getMinecraft().thePlayer == null) {
			openGui = null;
			currChatMessage = null;
			return;
		}
		long currentTime = System.currentTimeMillis();

		if (openGui != null) {
			if (Minecraft.getMinecraft().thePlayer.openContainer != null) {
				Minecraft.getMinecraft().thePlayer.closeScreen();
			}
			Minecraft.getMinecraft().displayGuiScreen(openGui);
			openGui = null;
			lastOpenedGui = System.currentTimeMillis();
		}
		if (currChatMessage != null && currentTime - lastChatMessage > CHAT_MSG_COOLDOWN) {
			lastChatMessage = currentTime;
			Minecraft.getMinecraft().thePlayer.sendChatMessage(currChatMessage);
			currChatMessage = null;
		}
	}

	public boolean isOnSkyblock() {
		if (!config.misc.onlyShowOnSkyblock) return true;
		return hasSkyblockScoreboard();
	}

	public boolean hasSkyblockScoreboard() {
		return hasSkyblockScoreboard;
	}

	public void updateSkyblockScoreboard() {
		Minecraft mc = Minecraft.getMinecraft();

		if (mc != null && mc.theWorld != null && mc.thePlayer != null) {
			if (mc.isSingleplayer() || mc.thePlayer.getClientBrand() == null ||
				!mc.thePlayer.getClientBrand().toLowerCase().contains("hypixel")) {
				hasSkyblockScoreboard = false;
				return;
			}

			Scoreboard scoreboard = mc.theWorld.getScoreboard();
			ScoreObjective sidebarObjective = scoreboard.getObjectiveInDisplaySlot(1);
			if (sidebarObjective != null) {
				String objectiveName = sidebarObjective.getDisplayName().replaceAll("(?i)\\u00A7.", "");
				for (String skyblock : SKYBLOCK_IN_ALL_LANGUAGES) {
					if (objectiveName.startsWith(skyblock)) {
						hasSkyblockScoreboard = true;
						return;
					}
				}
			}

			hasSkyblockScoreboard = false;
		}
	}
}
