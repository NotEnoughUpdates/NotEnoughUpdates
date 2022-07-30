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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.core.util.MiscUtils;
import io.github.moulberry.notenoughupdates.core.util.StringUtils;
import io.github.moulberry.notenoughupdates.miscgui.minionhelper.ApiData;
import io.github.moulberry.notenoughupdates.miscgui.minionhelper.MinionHelperManager;
import io.github.moulberry.notenoughupdates.profileviewer.ProfileViewer;
import io.github.moulberry.notenoughupdates.util.Constants;
import io.github.moulberry.notenoughupdates.util.SBInfo;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MinionHelperApiLoader {
	private static MinionHelperApiLoader instance = null;
	private final MinionHelperManager manager = MinionHelperManager.getInstance();
	private boolean dirty = true;
	private int ticks = 0;
	private boolean collectionApiEnabled = true;
	private boolean ignoreWorldSwitches = false;
	private boolean apiReadyToUse = false;

	public static MinionHelperApiLoader getInstance() {
		if (instance == null) {
			instance = new MinionHelperApiLoader();
		}
		return instance;
	}

	@SubscribeEvent
	public void onWorldLoad(WorldEvent.Load event) {
		if (ignoreWorldSwitches) return;

		setDirty();
	}

	@SubscribeEvent
	public void onTick(TickEvent.ClientTickEvent event) {
		if (Minecraft.getMinecraft().thePlayer == null) return;
		if (!NotEnoughUpdates.INSTANCE.hasSkyblockScoreboard()) return;
		ticks++;

		if (ticks % 20 != 0) return;

		if (dirty) {
			load();
		}
	}

	private void load() {
		EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;
		if (thePlayer == null) return;

		dirty = false;
		String uuid = thePlayer.getUniqueID().toString().replace("-", "");
		HashMap<String, String> map = new HashMap<String, String>() {{
			put("uuid", uuid);
		}};

		NotEnoughUpdates neu = NotEnoughUpdates.INSTANCE;
		neu.manager.hypixelApi.getHypixelApiAsync(
			neu.config.apiData.apiKey,
			"skyblock/profiles",
			map
		).thenAccept(this::updateInformation);
	}

	private void updateInformation(JsonObject entireApiResponse) {
		if (!entireApiResponse.has("success") || !entireApiResponse.get("success").getAsBoolean()) return;
		JsonArray profiles = entireApiResponse.getAsJsonArray("profiles");
		for (JsonElement element : profiles) {
			JsonObject profile = element.getAsJsonObject();
			String profileName = profile.get("cute_name").getAsString();
			JsonObject members = profile.getAsJsonObject("members");
			JsonObject player = members.getAsJsonObject(Minecraft.getMinecraft().thePlayer
				.getUniqueID()
				.toString()
				.replace("-", ""));

			if (profileName.equals(SBInfo.getInstance().currentProfile)) {
				readData(player, members);
				return;
			}
		}
	}

	private List<String> loadMinionData(JsonObject members) {
		List<String> craftedMinions = new ArrayList<>();
		for (Map.Entry<String, JsonElement> entry : members.entrySet()) {
			JsonObject value = entry.getValue().getAsJsonObject();
			if (value.has("crafted_generators")) {
				for (JsonElement e : value.get("crafted_generators").getAsJsonArray()) {
					String rawGenerator = e.getAsString();
					String[] split = rawGenerator.split("_");
					String tier = split[split.length - 1];
					String name = rawGenerator.substring(0, rawGenerator.length() - tier.length() - 1);
					String internalName = name + "_GENERATOR_" + tier;
					craftedMinions.add(internalName);
				}
			}
		}

		return craftedMinions;
	}

	private void readData(JsonObject player, JsonObject members) {
		Map<String, Integer> highestCollectionTier = new HashMap<>();
		Map<String, Integer> slayerTier = new HashMap<>();
		int magesReputation = 0;
		int barbariansReputation = 0;

		if (player.has("unlocked_coll_tiers")) {
			for (JsonElement element : player.get("unlocked_coll_tiers").getAsJsonArray()) {
				String text = element.getAsString();
				String[] split = text.split("_");
				int level = Integer.parseInt(split[split.length - 1]);
				String name = StringUtils.removeLastWord(text, "_");

				//Because skyblock is good in naming things
				LinkedHashMap<String, ItemStack> collectionMap = ProfileViewer.getCollectionToCollectionDisplayMap();
				if (collectionMap.containsKey(name)) {
					ItemStack itemStack = collectionMap.get(name);
					String displayName = itemStack.getDisplayName();
					name = Utils.cleanColour(displayName);
					name = manager.formatInternalName(name);
				} else {
					//Doing this since there is no space in the profile viewer gui for more entries in collectionToCollectionDisplayMap
					if (name.equals("SAND:1")) name = "RED_SAND";
					if (name.equals("MYCEL")) name = "MYCELIUM";
				}

				level = Math.max(highestCollectionTier.getOrDefault(name, 0), level);
				highestCollectionTier.put(name, level);
			}
			if (!collectionApiEnabled) {
				Utils.addChatMessage("§e[NEU] Collection API detected!");
			}
			collectionApiEnabled = true;
		} else {
			if (collectionApiEnabled) {
				manager.setShouldNotifyNoCollectionApi(true);
			}
			collectionApiEnabled = false;
		}

		if (player.has("nether_island_player_data")) {
			JsonObject netherData = player.getAsJsonObject("nether_island_player_data");
			if (netherData.has("mages_reputation")) {
				magesReputation = netherData.get("mages_reputation").getAsInt();
			}
			if (netherData.has("barbarians_reputation")) {
				barbariansReputation = netherData.get("barbarians_reputation").getAsInt();
			}
		}

		JsonObject slayerLeveling = Constants.LEVELING.getAsJsonObject("slayer_xp");

		if (player.has("slayer_bosses")) {
			JsonObject slayerBosses = player.getAsJsonObject("slayer_bosses");
			for (Map.Entry<String, JsonElement> entry : slayerBosses.entrySet()) {
				String name = entry.getKey();
				JsonObject slayerEntry = entry.getValue().getAsJsonObject();
				if (slayerEntry.has("xp")) {
					long xp = slayerEntry.get("xp").getAsLong();

					int tier = 0;
					for (JsonElement element : slayerLeveling.getAsJsonArray(name)) {
						int needForLevel = element.getAsInt();
						if (xp >= needForLevel) {
							tier++;
						} else {
							break;
						}
					}
					slayerTier.put(name, tier);
				}
			}
		}

		List<String> craftedMinions = loadMinionData(members);

		manager.setApiData(new ApiData(
			highestCollectionTier,
			slayerTier,
			magesReputation,
			barbariansReputation,
			!collectionApiEnabled,
			craftedMinions
		));

		manager.reloadData();
		apiReadyToUse = true;
	}

	public void setDirty() {
		dirty = true;
		apiReadyToUse = false;
	}

	public void prepareProfileSwitch() {
		ignoreWorldSwitches = true;
		apiReadyToUse = false;
	}

	public void onProfileSwitch() {
		setDirty();
		ignoreWorldSwitches = false;
		collectionApiEnabled = true;
	}

	public boolean isApiReadyToUse() {
		return apiReadyToUse;
	}
}
