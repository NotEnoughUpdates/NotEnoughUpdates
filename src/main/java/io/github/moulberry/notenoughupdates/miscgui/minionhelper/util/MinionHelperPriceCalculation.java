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

package io.github.moulberry.notenoughupdates.miscgui.minionhelper.util;

import com.google.common.collect.ArrayListMultimap;
import com.google.gson.JsonObject;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.miscgui.minionhelper.Minion;
import io.github.moulberry.notenoughupdates.miscgui.minionhelper.MinionHelperManager;
import io.github.moulberry.notenoughupdates.miscgui.minionhelper.sources.CraftingSource;
import io.github.moulberry.notenoughupdates.miscgui.minionhelper.sources.CustomSource;
import io.github.moulberry.notenoughupdates.miscgui.minionhelper.sources.MinionSource;
import io.github.moulberry.notenoughupdates.miscgui.minionhelper.sources.NpcSource;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MinionHelperPriceCalculation {

	private final MinionHelperManager manager;
	private final Map<String, String> upgradeCostFormatCache = new HashMap<>();
	private final Map<String, String> fullCostFormatCache = new HashMap<>();
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

	public MinionHelperPriceCalculation(MinionHelperManager manager) {
		this.manager = manager;
	}

	@SubscribeEvent
	public void onGuiOpen(GuiOpenEvent event) {
		upgradeCostFormatCache.clear();
		fullCostFormatCache.clear();
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
				return calculateUpgradeCosts(manager.getMinionById(internalName).getMinionSource(), false);
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

	public String formatCoins(long coins) {
		return formatCoins(coins, "");
	}

	public String formatCoins(long coins, String extraFormat) {
		String format = Utils.shortNumberFormat(coins, 0);
		return "§6" + extraFormat + format + " coins";
	}
}
