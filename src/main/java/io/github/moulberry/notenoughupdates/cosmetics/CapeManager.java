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

package io.github.moulberry.notenoughupdates.cosmetics;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.autosubscribe.NEUAutoSubscribe;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@NEUAutoSubscribe
public class CapeManager {
	public static final CapeManager INSTANCE = new CapeManager();
	public long lastCapeUpdate = 0;
	public long lastCapeSynced = 0;

	public Pair<NEUCape, String> localCape = null;
	private final HashMap<String, Pair<NEUCape, String>> capeMap = new HashMap<>();

	private int permSyncTries = 5;
	private boolean allAvailable = false;
	private final HashSet<String> availableCapes = new HashSet<>();

	public JsonObject lastJsonSync = null;

	public static class CapeData {
		public String capeName;
		public boolean special;
		public boolean hidden;

		public boolean canShow() {
			return !special && !hidden;
		}

		public CapeData(String capeName, boolean special, boolean hidden) {
			this.capeName = capeName;
			this.special = special;
			this.hidden = hidden;
		}
	}

	public CapeData[] capes = new CapeData[]{
		//Patreon
		new CapeData("patreon1", false, false),
		new CapeData("patreon2", false, false),
		new CapeData("fade", false, false),
		new CapeData("space", false, false),
		new CapeData("mcworld", false, false),
		new CapeData("negative", false, false),
		new CapeData("void", false, false),
		new CapeData("lava", false, false),
		new CapeData("tunnel", false, false),
		new CapeData("planets", false, false),
		new CapeData("screensaver", false, false),

		//Admins
		new CapeData("nullzee", true, false),
		new CapeData("ironmoon", true, false),
		new CapeData("gravy", true, false),

		//Special Other
		new CapeData("contrib", true, false),
		new CapeData("mbstaff", true, false),

		//Partner
		new CapeData("thebakery", true, false),
		new CapeData("furf", true, false),
		new CapeData("dsm", true, false),
		new CapeData("skyclient", true, false),
		new CapeData("subreddit_dark", true, false),
		new CapeData("subreddit_light", true, false),
		new CapeData("packshq", true, false),
		new CapeData("skytils", true, false),
		new CapeData("sbp", true, false),
		new CapeData("sharex", true, false),
		new CapeData("sharex_white", true, false),
		new CapeData("dg", true, false),

		//Content Creator
		new CapeData("jakethybro", false, true),
		new CapeData("krusty", false, true),
		new CapeData("krusty_day", false, true),
		new CapeData("krusty_sunset", false, true),
		new CapeData("krusty_night", false, true),
		new CapeData("zera", false, true),
		new CapeData("soldier", false, true),
		new CapeData("alexxoffi", false, true),
		new CapeData("secondpfirsisch", false, true),
		new CapeData("stormy_lh", false, true),
	};

	public static CapeManager getInstance() {
		return INSTANCE;
	}

	public void tryUnlockCape(String unlock) {
		for (CapeData data : capes) {
			if (data.capeName.equalsIgnoreCase(unlock)) {
				data.hidden = false;
			}
		}
	}

	public void tick() {
		long currentTime = System.currentTimeMillis();
		if (currentTime - lastCapeUpdate > 60 * 1000) {
			lastCapeUpdate = currentTime;
			updateCapes();
		}
	}

	private void updateCapes() {
		NotEnoughUpdates.INSTANCE.manager.apiUtils
			.newMoulberryRequest("activecapes.json")
			.requestJson()
			.thenAcceptAsync(jsonObject -> {
				if (jsonObject.get("success").getAsBoolean()) {
					lastJsonSync = jsonObject;

					lastCapeSynced = System.currentTimeMillis();
					capeMap.clear();
					for (JsonElement active : jsonObject.get("active").getAsJsonArray()) {
						if (active.isJsonObject()) {
							JsonObject activeObj = (JsonObject) active;
							setCape(activeObj.get("_id").getAsString(), activeObj.get("capeType").getAsString(), false);
						}
					}
				}
			});

		if (Minecraft.getMinecraft().thePlayer != null && permSyncTries > 0) {
			String uuid = Minecraft.getMinecraft().thePlayer.getUniqueID().toString().replace("-", "");
			permSyncTries--;
			NotEnoughUpdates.INSTANCE.manager.apiUtils
				.newMoulberryRequest("permscapes.json")
				.requestJson()
				.thenAcceptAsync(jsonObject -> {
					if (!jsonObject.get("success").getAsBoolean()) return;

					permSyncTries = 0;
					availableCapes.clear();
					for (JsonElement permPlayer : jsonObject.get("perms").getAsJsonArray()) {
						if (!permPlayer.isJsonObject()) continue;
						String playerUuid = permPlayer.getAsJsonObject().get("_id").getAsString();
						if (!(playerUuid != null && playerUuid.equals(uuid))) continue;
						for (JsonElement perm : permPlayer.getAsJsonObject().get("perms").getAsJsonArray()) {
							if (!perm.isJsonPrimitive()) continue;
							String cape = perm.getAsString();
							if (cape.equals("*")) {
								allAvailable = true;
							} else {
								availableCapes.add(cape);
							}

						}
						return;
					}

				});
		}
	}

	public HashSet<String> getAvailableCapes() {
		return allAvailable ? null : availableCapes;
	}

	public void setCape(String playerUUID, String capename, boolean updateConfig) {
		boolean none = capename == null || capename.equals("null");

		updateConfig = updateConfig && playerUUID.equals(Minecraft.getMinecraft().thePlayer
			.getUniqueID()
			.toString()
			.replace("-", ""));
		if (updateConfig) {
			NotEnoughUpdates.INSTANCE.config.hidden.selectedCape = String.valueOf(capename);
		}

		if (updateConfig) {
			localCape = none ? null : new MutablePair<>(new NEUCape(capename), capename);
		} else if (capeMap.containsKey(playerUUID)) {
			if (none) {
				capeMap.remove(playerUUID);
			} else {
				Pair<NEUCape, String> capePair = capeMap.get(playerUUID);
				capePair.setValue(capename);
			}
		} else if (!none) {
			capeMap.put(playerUUID, new MutablePair<>(new NEUCape(capename), capename));
		}
	}

	public String getCape(String player) {
		if (capeMap.containsKey(player)) {
			return capeMap.get(player).getRight();
		}
		return null;
	}

	private static BiMap<String, EntityPlayer> playerMap = null;

	public EntityPlayer getPlayerForUUID(String uuid) {
		if (playerMap == null) {
			return null;
		}
		if (playerMap.containsKey(uuid)) {
			return playerMap.get(uuid);
		}
		return null;
	}

	private static Framebuffer checkFramebufferSizes(Framebuffer framebuffer, int width, int height) {
		if (framebuffer == null || framebuffer.framebufferWidth != width || framebuffer.framebufferHeight != height) {
			if (framebuffer == null) {
				framebuffer = new Framebuffer(width, height, true);
			} else {
				framebuffer.createBindFramebuffer(width, height);
			}
			framebuffer.setFramebufferFilter(GL11.GL_NEAREST);
		}
		return framebuffer;
	}

	public boolean updateWorldFramebuffer = false;
	public Framebuffer backgroundFramebuffer = null;

	public void postRenderBlocks() {
		int width = Minecraft.getMinecraft().displayWidth;
		int height = Minecraft.getMinecraft().displayHeight;
		backgroundFramebuffer = checkFramebufferSizes(backgroundFramebuffer,
			width, height
		);

		if (OpenGlHelper.isFramebufferEnabled() && updateWorldFramebuffer) {
			GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, Minecraft.getMinecraft().getFramebuffer().framebufferObject);
			GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, backgroundFramebuffer.framebufferObject);
			GL30.glBlitFramebuffer(0, 0, width, height,
				0, 0, width, height,
				GL11.GL_COLOR_BUFFER_BIT, GL11.GL_NEAREST
			);

			Minecraft.getMinecraft().getFramebuffer().bindFramebuffer(true);
		}

		updateWorldFramebuffer = false;
	}

	@SubscribeEvent
	public void onWorldLoad(WorldEvent.Unload event) {
		if (playerMap != null) playerMap.clear();
	}

	@SubscribeEvent
	public void onRenderPlayer(RenderPlayerEvent.Post e) {
		if (e.partialRenderTick == 1.0F) return; //rendering in inventory

		try {
			String uuid = e.entityPlayer.getUniqueID().toString().replace("-", "");
			String clientUuid = Minecraft.getMinecraft().thePlayer.getUniqueID().toString().replace("-", "");

			if (Minecraft.getMinecraft().thePlayer != null && uuid.equals(clientUuid)) {
				String selCape = NotEnoughUpdates.INSTANCE.config.hidden.selectedCape;
				if (selCape != null && !selCape.isEmpty()) {
					if (localCape == null) {
						localCape = new MutablePair<>(new NEUCape(selCape), selCape);
					} else {
						localCape.setValue(selCape);
					}
				}
			}
			if (uuid.equals(clientUuid) && localCape != null && localCape.getRight() != null && !localCape.getRight().equals(
				"null")) {
				localCape.getLeft().onRenderPlayer(e);
			} else if (!Minecraft.getMinecraft().thePlayer.isPotionActive(Potion.blindness) && capeMap.containsKey(uuid)) {
				capeMap.get(uuid).getLeft().onRenderPlayer(e);
			}
		} catch (Exception ignored) {
		}
	}

	public static void onTickSlow() {
		if (Minecraft.getMinecraft().theWorld == null) return;

		if (playerMap == null) {
			playerMap = HashBiMap.create(Minecraft.getMinecraft().theWorld.playerEntities.size());
		}
		playerMap.clear();
		for (EntityPlayer player : Minecraft.getMinecraft().theWorld.playerEntities) {
			String uuid = player.getUniqueID().toString().replace("-", "");
			try {
				playerMap.put(uuid, player);
			} catch (IllegalArgumentException ignored) {
			}
		}
	}

	private static final ExecutorService capeTicker = Executors.newCachedThreadPool();

	@SubscribeEvent
	public void onTick(TickEvent.ClientTickEvent event) {
		if (event.phase != TickEvent.Phase.END) return;
		if (Minecraft.getMinecraft().theWorld == null) return;

		if (playerMap == null) {
			return;
		}

		String clientUuid = null;
		if (Minecraft.getMinecraft().thePlayer != null) {
			clientUuid = Minecraft.getMinecraft().thePlayer.getUniqueID().toString().replace("-", "");
		}

		boolean hasLocalCape = localCape != null && localCape.getRight() != null && !localCape.getRight().equals("null");

		Set<String> toRemove = new HashSet<>();
		try {
			for (String playerUUID : capeMap.keySet()) {
				EntityPlayer player;
				if (playerUUID.equals(clientUuid)) {
					player = Minecraft.getMinecraft().thePlayer;
				} else {
					player = getPlayerForUUID(playerUUID);
				}
				if (player != null) {
					String capeName = capeMap.get(playerUUID).getRight();
					if (capeName != null && !capeName.equals("null")) {
						if (player == Minecraft.getMinecraft().thePlayer && hasLocalCape) {
							continue;
						}
						capeMap.get(playerUUID).getLeft().setCapeTexture(capeName);
						capeTicker.submit(() -> capeMap.get(playerUUID).getLeft().onTick(event, player));
					} else {
						toRemove.add(playerUUID);
					}
				}
			}
		} catch (Exception ignored) {
		}

		if (hasLocalCape) {
			localCape.getLeft().setCapeTexture(localCape.getValue());
			capeTicker.submit(() -> localCape.getLeft().onTick(event, Minecraft.getMinecraft().thePlayer));
		}
		for (String playerName : toRemove) {
			capeMap.remove(playerName);
		}
	}

	public CapeData[] getCapes() {
		return capes;
	}
}
