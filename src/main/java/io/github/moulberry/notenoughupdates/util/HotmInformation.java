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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.ContainerChest;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class HotmInformation {
	private final NotEnoughUpdates neu;
	public static final int[] EXPERIENCE_FOR_HOTM_LEVEL = {
		// Taken from the wiki: https://hypixel-skyblock.fandom.com/wiki/Heart_of_the_Mountain#Experience_for_Each_Tier
		0,
		3000,
		12000,
		37000,
		97000,
		197000,
		347000
	};
	public static final int[] QUICK_FORGE_MULTIPLIERS = {
		985,
		970,
		955,
		940,
		925,
		910,
		895,
		880,
		865,
		850,
		845,
		840,
		835,
		830,
		825,
		820,
		815,
		810,
		805,
		700
	};
	private final Map<String, Tree> profiles = new ConcurrentHashMap<>();

	public static class Tree {
		private final Map<String, Integer> levels = new HashMap<>();
		private int totalMithrilPowder;
		private int totalGemstonePowder;
		private int hotmExp;

		public int getHotmExp() {
			return hotmExp;
		}

		public int getTotalGemstonePowder() {
			return totalGemstonePowder;
		}

		public int getTotalMithrilPowder() {
			return totalMithrilPowder;
		}

		public Set<String> getAllUnlockedNodes() {
			return levels.keySet();
		}

		public int getHotmLevel() {
			for (int i = EXPERIENCE_FOR_HOTM_LEVEL.length - 1; i >= 0; i--) {
				if (EXPERIENCE_FOR_HOTM_LEVEL[i] >= this.hotmExp)
					return i;
			}
			return 0;
		}

		public int getLevel(String node) {
			return levels.getOrDefault(node, 0);
		}

	}

	private CompletableFuture<Void> updateTask = CompletableFuture.completedFuture(null);

	private boolean shouldReloadSoon = false;

	public HotmInformation(NotEnoughUpdates neu) {
		this.neu = neu;
		MinecraftForge.EVENT_BUS.register(this);
	}

	public Optional<Tree> getInformationOn(String profile) {
		if (profile == null) {
			return Optional.empty();
		}
		return Optional.ofNullable(this.profiles.get(profile));
	}

	public Optional<Tree> getInformationOnCurrentProfile() {
		return getInformationOn(neu.manager.getCurrentProfile());
	}

	@SubscribeEvent
	public synchronized void onLobbyJoin(WorldEvent.Load event) {
		if (shouldReloadSoon) {
			shouldReloadSoon = false;
			requestUpdate(false);
		}
	}

	@SubscribeEvent
	public synchronized void onGuiOpen(GuiOpenEvent event) {
		if (event.gui instanceof GuiChest) {
			String containerName = ((ContainerChest) ((GuiChest) event.gui).inventorySlots)
				.getLowerChestInventory()
				.getDisplayName()
				.getUnformattedText();
			if (containerName.equals("Heart of the Mountain"))
				shouldReloadSoon = true;
		}
	}

	@SubscribeEvent
	public synchronized void onChat(ClientChatReceivedEvent event) {
		if (event.message.getUnformattedText().equals("Welcome to Hypixel SkyBlock!"))
			requestUpdate(false);
	}

	public synchronized void requestUpdate(boolean force) {
		if (updateTask.isDone() || force) {
			updateTask = neu.manager.hypixelApi.getHypixelApiAsync(
				neu.config.apiData.apiKey,
				"skyblock/profiles",
				new HashMap<String, String>() {{
					put("uuid", Minecraft.getMinecraft().thePlayer.getUniqueID().toString().replace("-", ""));
				}}
			).thenAccept(this::updateInformation);
		}
	}

	/*
	 * 1000 = 100% of the time left
	 *  700 = 70% of the time left
	 * */
	public static int getQuickForgeMultiplier(int level) {
		if (level <= 0) return 1000;
		if (level > 20) return -1;
		return QUICK_FORGE_MULTIPLIERS[level - 1];
	}

	public void updateInformation(JsonObject entireApiResponse) {
		if (!entireApiResponse.has("success") || !entireApiResponse.get("success").getAsBoolean()) return;
		JsonArray profiles = entireApiResponse.getAsJsonArray("profiles");
		for (JsonElement element : profiles) {
			JsonObject profile = element.getAsJsonObject();
			String profileName = profile.get("cute_name").getAsString();
			JsonObject player = profile.getAsJsonObject("members").getAsJsonObject(Minecraft.getMinecraft().thePlayer
				.getUniqueID()
				.toString()
				.replace("-", ""));
			if (!player.has("mining_core"))
				continue;
			JsonObject miningCore = player.getAsJsonObject("mining_core");
			Tree tree = new Tree();
			JsonObject nodes = miningCore.getAsJsonObject("nodes");
			for (Map.Entry<String, JsonElement> node : nodes.entrySet()) {
				String key = node.getKey();
				if (!key.startsWith("toggle_")) {
					tree.levels.put(key, node.getValue().getAsInt());
				}
			}
			if (miningCore.has("powder_mithril_total")) {
				tree.totalMithrilPowder = miningCore.get("powder_mithril_total").getAsInt();
			}
			if (miningCore.has("powder_gemstone_total")) {
				tree.totalGemstonePowder = miningCore.get("powder_gemstone_total").getAsInt();
			}
			if (miningCore.has("experience")) {
				tree.hotmExp = miningCore.get("experience").getAsInt();
			}
			this.profiles.put(profileName, tree);
		}
	}

}
