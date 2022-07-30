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

import com.google.common.collect.ArrayListMultimap;
import com.google.gson.JsonObject;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.miscgui.minionhelper.loaders.MinionHelperApiLoader;
import io.github.moulberry.notenoughupdates.miscgui.minionhelper.loaders.MinionHelperRepoLoader;
import io.github.moulberry.notenoughupdates.miscgui.minionhelper.requirements.CollectionRequirement;
import io.github.moulberry.notenoughupdates.miscgui.minionhelper.requirements.CustomRequirement;
import io.github.moulberry.notenoughupdates.miscgui.minionhelper.requirements.MinionRequirement;
import io.github.moulberry.notenoughupdates.miscgui.minionhelper.requirements.ReputationRequirement;
import io.github.moulberry.notenoughupdates.miscgui.minionhelper.requirements.SlayerRequirement;
import io.github.moulberry.notenoughupdates.miscgui.minionhelper.sources.CraftingSource;
import io.github.moulberry.notenoughupdates.miscgui.minionhelper.sources.CustomSource;
import io.github.moulberry.notenoughupdates.miscgui.minionhelper.sources.MinionSource;
import io.github.moulberry.notenoughupdates.miscgui.minionhelper.sources.NpcSource;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MinionHelperManager {
	private static MinionHelperManager instance = null;
	private final Map<String, Minion> minions = new HashMap<>();
	private final Map<String, String> upgradeCostFormatCache = new HashMap<>();
	private final Map<String, String> fullCostFormatCache = new HashMap<>();
	private ApiData apiData = null;
	private boolean shouldNotifyNoCollectionApi = false;
	//TODO maybe change logic with 0 coins later or stuff
	private final List<String> cheapItems = Arrays.asList(
		"WOOD_SWORD",
		"WOOD_HOE",
		"WOOD_AXE",
		"WOOD_PICKAXE",
		"WOOD_SPADE",
		"FISHING_ROD",
		"SKYBLOCK_PELT"
	);

	public static MinionHelperManager getInstance() {
		if (instance == null) {
			instance = new MinionHelperManager();
		}
		return instance;
	}

	@SubscribeEvent
	public void onGuiOpen(GuiOpenEvent event) {
		upgradeCostFormatCache.clear();
		fullCostFormatCache.clear();
	}

	public boolean inCraftedMinionsInventory() {
		if (!NotEnoughUpdates.INSTANCE.isOnSkyblock()) return false;

		Minecraft minecraft = Minecraft.getMinecraft();
		if (minecraft == null || minecraft.thePlayer == null) return false;

		Container inventoryContainer = minecraft.thePlayer.openContainer;
		if (!(inventoryContainer instanceof ContainerChest)) return false;
		ContainerChest containerChest = (ContainerChest) inventoryContainer;
		return containerChest.getLowerChestInventory().getDisplayName()
												 .getUnformattedText().equalsIgnoreCase("Crafted Minions");
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

	public String calculateUpgradeCostsFormat(MinionSource source, boolean upgradeOnly) {
		if (source == null) return "§c?";
		String internalName = source.getMinion().getInternalName();
		if (upgradeOnly) {
			if (upgradeCostFormatCache.containsKey(internalName)) {
				upgradeCostFormatCache.get(internalName);
			}
		} else {
			if (fullCostFormatCache.containsKey(internalName)) {
				fullCostFormatCache.get(internalName);
			}
		}

		if (source instanceof CustomSource) {
			return "§f" + ((CustomSource) source).getCustomSource();
		}

		long costs = calculateUpgradeCosts(source, upgradeOnly);
		String result = formatCoins(costs, !upgradeOnly ? "§o" : "");

		if (source instanceof NpcSource) {
			ArrayListMultimap<String, Integer> items = ((NpcSource) source).getItems();
			if (items.containsKey("SKYBLOCK_PELT")) {
				int amount = items.get("SKYBLOCK_PELT").get(0);
				result += " §7+ §5" + amount + " Pelts";
			}
		}

		if (upgradeOnly) {
			upgradeCostFormatCache.put(internalName, result);
		} else {
			fullCostFormatCache.put(internalName, result);
		}

		return result;
	}

	public long calculateUpgradeCosts(MinionSource source, boolean upgradeOnly) {
		if (source instanceof CraftingSource) {
			CraftingSource craftingSource = (CraftingSource) source;
			return getCosts(source, upgradeOnly, craftingSource.getItems());

		} else if (source instanceof NpcSource) {
			NpcSource npcSource = (NpcSource) source;
			long upgradeCost = getCosts(source, upgradeOnly, npcSource.getItems());
			long coins = npcSource.getCoins();
			upgradeCost += coins;

			return upgradeCost;
		}

		return 0;
	}

	private long getCosts(MinionSource source, boolean upgradeOnly, ArrayListMultimap<String, Integer> items) {
		long upgradeCost = 0;
		for (Map.Entry<String, Integer> entry : items.entries()) {
			String internalName = entry.getKey();
			long price = getPrice(internalName);
			int amount = entry.getValue();
			upgradeCost += price * amount;
		}
		if (!upgradeOnly) {
			Minion parent = source.getMinion().getParent();
			if (parent != null) {
				upgradeCost += calculateUpgradeCosts(parent.getMinionSource(), false);
			}
		}
		return upgradeCost;
	}

	public long getPrice(String internalName) {
		JsonObject bazaarInfo = NotEnoughUpdates.INSTANCE.manager.auctionManager.getBazaarInfo(internalName);
		if (bazaarInfo == null) {
			if (internalName.contains("_GENERATOR_")) {
				return calculateUpgradeCosts(getMinionById(internalName).getMinionSource(), false);
			} else {
				if (!cheapItems.contains(internalName)) {
					return (long) NotEnoughUpdates.INSTANCE.manager.auctionManager.getItemAvgBin(internalName);
				}
			}
			return 0;
		}

		//TODO use average bazaar price?
		if (!bazaarInfo.has("curr_sell")) {
			System.err.println("curr_sell does not exist for '" + internalName + "'");
			return 0;
		}
		return (long) bazaarInfo.get("curr_sell").getAsDouble();
	}

	public void createMinion(String internalName, int tier) {
		minions.put(internalName, new Minion(internalName, tier));
	}

	public Map<String, Minion> getAllMinions() {
		return minions;
	}

	public List<MinionRequirement> getRequirements(Minion minion) {
		if (!minion.getRequirements().isEmpty()) {
			return minion.getRequirements();
		}

		Minion parent = minion.getParent();
		if (parent != null) {
			return getRequirements(parent);
		}

		return Collections.emptyList();
	}

	public boolean meetAllRequirements(Minion minion) {
		List<MinionRequirement> list = getRequirements(minion);
		for (MinionRequirement requirement : list) {
			if (!meetRequirement(minion, requirement)) {
				return false;
			}
		}

		return true;
	}

	public boolean meetRequirement(Minion minion, MinionRequirement requirement) {
		if (apiData == null) return false;

		if (requirement instanceof CollectionRequirement) {
			if (apiData.isCollectionApiDisabled()) return true;

			CollectionRequirement collectionRequirement = (CollectionRequirement) requirement;
			String collection = collectionRequirement.getCollection();
			String internalName = formatInternalName(collection);

			int need = collectionRequirement.getLevel();
			Map<String, Integer> highestCollectionTier = apiData.getHighestCollectionTier();
			if (highestCollectionTier.containsKey(internalName)) {
				int has = highestCollectionTier.get(internalName);

				return has >= need;
//			} else {
//				Utils.addChatMessage("§cInvalid hypixel collection name: '" + internalName + "'");
			}

		} else if (requirement instanceof SlayerRequirement) {
			SlayerRequirement slayerRequirement = (SlayerRequirement) requirement;
			String slayer = slayerRequirement.getSlayer();
			int need = slayerRequirement.getLevel();
			Map<String, Integer> slayerTiers = apiData.getSlayerTiers();
			if (slayerTiers.containsKey(slayer)) {
				return slayerTiers.get(slayer) >= need;
			}

		} else if (requirement instanceof ReputationRequirement) {
			ReputationRequirement reputationRequirement = (ReputationRequirement) requirement;
			int need = reputationRequirement.getReputation();
			String reputationType = reputationRequirement.getReputationType();
			if (reputationType.equals("BARBARIAN")) {
				return apiData.getBarbariansReputation() >= need;
			} else if (reputationType.equals("MAGE")) {
				return apiData.getMagesReputation() >= need;
			} else {
				Utils.addChatMessage("§cUnknown reputation type: '" + reputationType + "'");
				return false;
			}
		} else if (requirement instanceof CustomRequirement) {
			return minion.isCrafted();
		}

		return false;
	}

	public String formatInternalName(String text) {
		return text.toUpperCase().replace(" ", "_");
	}

	public void setApiData(ApiData apiData) {
		this.apiData = apiData;
	}

	public void reloadRequirements() {
		for (Minion minion : minions.values()) {
			minion.setMeetRequirements(meetAllRequirements(minion));
		}
	}

	public String formatCoins(long coins) {
		return formatCoins(coins, "");
	}

	public String formatCoins(long coins, String extraFormat) {
		String format = Utils.shortNumberFormat(coins, 0);
		return "§6" + extraFormat + format + " coins";
	}

	public boolean isCollectionApiDisabled() {
		return apiData != null && apiData.isCollectionApiDisabled();
	}

	public void setShouldNotifyNoCollectionApi(boolean shouldNotifyNoCollectionApi) {
		this.shouldNotifyNoCollectionApi = shouldNotifyNoCollectionApi;
	}

	public boolean isShouldNotifyNoCollectionApi() {
		return shouldNotifyNoCollectionApi;
	}

	public void handleCommand(String[] args) {
		if (args.length == 2) {
			String parameter = args[1];
			if (parameter.equals("clear")) {
				minions.clear();
				Utils.addChatMessage("minions cleared");
				return;
			}
			if (parameter.equals("resetrepo")) {
				MinionHelperRepoLoader.getInstance().setDirty();
				Utils.addChatMessage("repo reload requested");
				return;
			}
			if (parameter.equals("resetapi")) {
				MinionHelperApiLoader.getInstance().setDirty();
				Utils.addChatMessage("api reload requested");
				return;
			}
		}

		Utils.addChatMessage("");
		Utils.addChatMessage("§3NEU Minion Helper commands: §c((for testing only!)");
		Utils.addChatMessage("§6/neudevtest minion clear §7Clears the internal minion data");
		Utils.addChatMessage("§6/neudevtest minion resetrepo §7Manually loading the data from repo");
		Utils.addChatMessage("§6/neudevtest minion resetapi §7Manually loading the data from api");
		Utils.addChatMessage("");
	}
}
