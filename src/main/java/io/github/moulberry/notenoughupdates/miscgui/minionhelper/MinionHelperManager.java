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

package io.github.moulberry.notenoughupdates.miscgui.minionhelper;

import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.autosubscribe.NEUAutoSubscribe;
import io.github.moulberry.notenoughupdates.miscgui.minionhelper.loaders.MinionHelperApiLoader;
import io.github.moulberry.notenoughupdates.miscgui.minionhelper.loaders.MinionHelperChatLoader;
import io.github.moulberry.notenoughupdates.miscgui.minionhelper.loaders.MinionHelperInventoryLoader;
import io.github.moulberry.notenoughupdates.miscgui.minionhelper.loaders.repo.MinionHelperRepoLoader;
import io.github.moulberry.notenoughupdates.miscgui.minionhelper.render.MinionHelperOverlay;
import io.github.moulberry.notenoughupdates.miscgui.minionhelper.render.MinionHelperTooltips;
import io.github.moulberry.notenoughupdates.miscgui.minionhelper.sources.CustomSource;
import io.github.moulberry.notenoughupdates.miscgui.minionhelper.sources.MinionSource;
import io.github.moulberry.notenoughupdates.miscgui.minionhelper.util.MinionHelperPriceCalculation;
import io.github.moulberry.notenoughupdates.miscgui.minionhelper.util.MinionHelperRequirementsManager;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraftforge.common.MinecraftForge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@NEUAutoSubscribe
public class MinionHelperManager {
	private static MinionHelperManager instance = null;
	private final Map<String, Minion> minions = new HashMap<>();
	private int needForNextSlot = -1;
	private int localPelts = -1;

	private final MinionHelperPriceCalculation priceCalculation = new MinionHelperPriceCalculation(this);
	private final MinionHelperRequirementsManager requirementsManager = new MinionHelperRequirementsManager(this);
	private final MinionHelperApiLoader api = new MinionHelperApiLoader(this);
	private final MinionHelperRepoLoader repo = new MinionHelperRepoLoader(this);
	private final MinionHelperOverlay overlay = new MinionHelperOverlay(this);
	private final MinionHelperInventoryLoader inventoryLoader = new MinionHelperInventoryLoader(this);
	private String debugPlayerUuid;
	private String debugProfileName;
	private int debugNeedForNextSlot = -1;

	public static MinionHelperManager getInstance() {
		if (instance == null) {
			instance = new MinionHelperManager();
		}
		return instance;
	}

	private MinionHelperManager() {
		MinecraftForge.EVENT_BUS.register(priceCalculation);
		MinecraftForge.EVENT_BUS.register(api);
		MinecraftForge.EVENT_BUS.register(repo);
		MinecraftForge.EVENT_BUS.register(overlay);
		MinecraftForge.EVENT_BUS.register(new MinionHelperTooltips(this));
		MinecraftForge.EVENT_BUS.register(new MinionHelperChatLoader(this));
		MinecraftForge.EVENT_BUS.register(inventoryLoader);
	}

	public boolean inCraftedMinionsInventory() {
		if (!NotEnoughUpdates.INSTANCE.isOnSkyblock()) return false;

		Minecraft minecraft = Minecraft.getMinecraft();
		if (minecraft == null || minecraft.thePlayer == null) return false;

		Container inventoryContainer = minecraft.thePlayer.openContainer;
		if (!(inventoryContainer instanceof ContainerChest)) return false;
		ContainerChest containerChest = (ContainerChest) inventoryContainer;
		String name = containerChest.getLowerChestInventory().getDisplayName().getUnformattedText();
		return name.equalsIgnoreCase("Crafted Minions");
	}

	public boolean notReady() {
		return !repo.isReadyToUse() || !api.isReadyToUse();
	}

	public boolean isInvalidApiKey() {
		return api.isInvalidApiKey();
	}

	public Minion getMinionById(String internalName) {
		if (minions.containsKey(internalName)) {
			return minions.get(internalName);
		} else {
			System.err.println("Cannot get minion for id '" + internalName + "'!");
			return null;
		}
	}

	public Minion getMinionByName(String displayName, int tier) {
		for (Minion minion : minions.values()) {
			if (displayName.equals(minion.getDisplayName())) {
				if (minion.getTier() == tier) {
					return minion;
				}
			}
		}
		System.err.println("Cannot get minion for display name '" + displayName + "'!");
		return null;
	}

	public void createMinion(String internalName, int tier, int xpGain) {
		minions.put(internalName, new Minion(internalName, tier, xpGain));
	}

	public String formatInternalName(String minionName) {
		return minionName.toUpperCase().replace(" ", "_");
	}

	private List<Minion> getChildren(Minion minion) {
		List<Minion> list = new ArrayList<>();
		for (Minion other : minions.values()) {
			if (minion == other.getParent()) {
				list.add(other);
				list.addAll(getChildren(other));
				break;
			}
		}
		return list;
	}

	public void onProfileSwitch() {
		for (Minion minion : minions.values()) {
			minion.setCrafted(false);
			minion.setMeetRequirements(false);
		}

		needForNextSlot = -1;
		api.onProfileSwitch();
		overlay.onProfileSwitch();
		inventoryLoader.onProfileSwitch();
	}

	public void reloadData() {
		requirementsManager.reloadRequirements();

		ApiData apiData = api.getApiData();
		if (apiData != null) {
			for (String minion : apiData.getCraftedMinions()) {
				setCrafted(getMinionById(minion));
			}
		}
	}

	public void setCrafted(Minion minion) {
		minion.setCrafted(true);

		if (minion.getCustomSource() != null) {
			minion.setMeetRequirements(true);

			for (Minion child : getChildren(minion)) {
				child.setMeetRequirements(true);
			}
		}
	}

	public void handleCommand(String[] args) {
		if (!NotEnoughUpdates.INSTANCE.config.minionHelper.gui) {
			Utils.addChatMessage("§e[NEU] Minion Helper gui is disabled!");
			return;
		}

		if (args.length > 1) {
			String parameter = args[1];

			if (parameter.equals("debugplayer")) {
				if (args.length == 3) {
					if (args[2].equals("reset")) {
						Utils.addChatMessage("§e[NEU] Minion debug player reset.");
						setDebugPlayer(null, null, -1);
						return;
					}
				}
				if (args.length < 4) {
					Utils.addChatMessage("§c[NEU] Usage: /neudevtest minion " +
						"setplayer <player-uuid> <player-profile-name> [need-for-next-slot]");
					return;
				}
				String playerUuid = args[2];
				String playerProfileName = args[3];
				int need = args.length == 5 ? Integer.parseInt(args[4]) : -1;
				setDebugPlayer(playerUuid, playerProfileName, need);
				Utils.addChatMessage("§e[NEU] Minion debug player set.");
				return;
			}

			if (args.length == 2) {
				if (parameter.equals("clearminion")) {
					minions.clear();
					Utils.addChatMessage("minion map cleared");
					return;
				}
				if (parameter.equals("reloadrepo")) {
					repo.setDirty();
					Utils.addChatMessage("repo reload requested");
					return;
				}
				if (parameter.equals("reloadapi")) {
					api.resetData();
					api.setDirty();
					Utils.addChatMessage("api reload requested");
					return;
				}
				if (parameter.equals("clearapi")) {
					api.resetData();
					Utils.addChatMessage("api data cleared");
					return;
				}
			}

			if (args.length == 3) {
				if (parameter.equals("maxperpage")) {
					api.resetData();
					int maxPerPage = Integer.parseInt(args[2]);
					Utils.addChatMessage("set max per page to " + maxPerPage);
					overlay.setMaxPerPage(maxPerPage);
					return;
				}
			}

			if (args.length == 4) {
				if (parameter.equals("arrowpos")) {
					int x = Integer.parseInt(args[2]);
					int y = Integer.parseInt(args[3]);
					Utils.addChatMessage("set page pos to " + x + ";" + y);
					overlay.setTopLeft(new int[]{x, y});
					return;
				}
			}
		}

		Utils.addChatMessage("");
		Utils.addChatMessage("§3NEU Minion Helper commands: §c(for testing only!)");
		Utils.addChatMessage("§6/neudevtest minion clearminion §7Clears the minion map");
		Utils.addChatMessage("§6/neudevtest minion reloadrepo §7Manually loading the data from repo");
		Utils.addChatMessage("§6/neudevtest minion reloadapi §7Manually loading the data from api");
		Utils.addChatMessage("§6/neudevtest minion clearapi §7Clears the api data");
		Utils.addChatMessage("§6/neudevtest minion maxperpage <number> §7Changes the max minions per page number");
		Utils.addChatMessage("§6/neudevtest minion arrowpos <x, y> §7Changes the position of the page numbers");
		Utils.addChatMessage("§6/neudevtest minion debugplayer <player-uuid> <player-profile-name> [need-for-next-slot] §7" +
			"See the Minions missing of other player");
		Utils.addChatMessage("");
	}

	private void setDebugPlayer(String playerUuid, String playerProfileName, int fakeNeedForNextSlot) {
		this.debugPlayerUuid = playerUuid;
		this.debugProfileName = playerProfileName;
		this.debugNeedForNextSlot = fakeNeedForNextSlot;

		onProfileSwitch();
	}

	public MinionHelperPriceCalculation getPriceCalculation() {
		return priceCalculation;
	}

	public MinionHelperRequirementsManager getRequirementsManager() {
		return requirementsManager;
	}

	public MinionHelperApiLoader getApi() {
		return api;
	}

	public MinionHelperOverlay getOverlay() {
		return overlay;
	}

	public Map<String, Minion> getAllMinions() {
		return minions;
	}

	public void setNeedForNextSlot(int needForNextSlot) {
		this.needForNextSlot = needForNextSlot;
		overlay.resetCache();
	}

	public int getNeedForNextSlot() {
		return needForNextSlot;
	}

	public void setCustomSource(Minion minion, CustomSource customSource) {
		MinionSource minionSource = minion.getMinionSource();
		if (minionSource == null) {
			minion.setMinionSource(customSource);
		}
		minion.setCustomSource(customSource);
	}

	public int getLocalPelts() {
		return localPelts;
	}

	public void setLocalPelts(int pelts) {
		localPelts = pelts;
		if (localPelts != -1) {
			ApiData apiData = api.getApiData();
			if (apiData != null) {
				apiData.setPeltCount(localPelts);
			}
		}
	}

	public String getDebugPlayerUuid() {
		return debugPlayerUuid;
	}

	public String getDebugProfileName() {
		return debugProfileName;
	}

	public int getDebugNeedForNextSlot() {
		return debugNeedForNextSlot;
	}
}
