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
import io.github.moulberry.notenoughupdates.miscgui.minionhelper.sources.MinionSource;
import io.github.moulberry.notenoughupdates.miscgui.minionhelper.sources.NpcSource;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;

public class MinionHelperPriceCalculation {

	private final MinionHelperManager manager;
	private final Map<String, String> upgradeCostFormatCache = new HashMap<>();
	private final Map<String, String> fullCostFormatCache = new HashMap<>();

	public MinionHelperPriceCalculation(MinionHelperManager manager) {
		this.manager = manager;
	}

	@SubscribeEvent
	public void onGuiOpen(GuiOpenEvent event) {
		resetCache();
	}

	public void resetCache() {
		upgradeCostFormatCache.clear();
		fullCostFormatCache.clear();
	}

	public String calculateUpgradeCostsFormat(Minion minion, boolean upgradeOnly) {
		MinionSource source = minion.getMinionSource();

		if (source == null) return "§c?";
		String internalName = minion.getInternalName();
		if (upgradeOnly) {
			if (upgradeCostFormatCache.containsKey(internalName)) {
				return upgradeCostFormatCache.get(internalName);
			}
		} else {
			if (fullCostFormatCache.containsKey(internalName)) {
				return fullCostFormatCache.get(internalName);
			}
		}

		if (upgradeOnly) {
			if (minion.getCustomSource() != null) {
				return (minion.getCustomSource()).getSourceName();
			}
		}

		double costs = calculateUpgradeCosts(minion, upgradeOnly);
		String result = formatCoins(costs, !upgradeOnly ? "§o" : "");

		if (source instanceof NpcSource) {
			ArrayListMultimap<String, Integer> items = ((NpcSource) source).getItems();
			// Please hypixel never ever add a minion recipe with pelts and north stars at the same time! Thank you :)
			if (items.containsKey("SKYBLOCK_PELT")) {
				int amount = items.get("SKYBLOCK_PELT").get(0);
				result += " §8+ §5" + amount + " Pelts";
			}
			if (items.containsKey("SKYBLOCK_NORTH_STAR")) {
				int amount = items.get("SKYBLOCK_NORTH_STAR").get(0);
				result += " §8+ §d" + amount + " North Stars";
			}
		}

		if (upgradeOnly) {
			upgradeCostFormatCache.put(internalName, result);
		} else {
			fullCostFormatCache.put(internalName, result);
		}

		return result;
	}

	public double calculateUpgradeCosts(Minion minion, boolean upgradeOnly) {
		MinionSource source = minion.getMinionSource();

		if (upgradeOnly) {
			if (minion.getCustomSource() != null) {
				return 0;
			}
		}

		if (source instanceof CraftingSource) {
			CraftingSource craftingSource = (CraftingSource) source;
			return getCosts(minion, upgradeOnly, craftingSource.getItems());

		} else if (source instanceof NpcSource) {
			NpcSource npcSource = (NpcSource) source;
			double upgradeCost = getCosts(minion, upgradeOnly, npcSource.getItems());
			long coins = npcSource.getCoins();
			upgradeCost += coins;

			return upgradeCost;
		}

		return 0;
	}

	private double getCosts(Minion minion, boolean upgradeOnly, ArrayListMultimap<String, Integer> items) {
		double upgradeCost = 0;
		for (Map.Entry<String, Integer> entry : items.entries()) {
			String internalName = entry.getKey();
			if (internalName.equals("SKYBLOCK_PELT")) continue;
			double price = getPrice(internalName);
			int amount = entry.getValue();
			upgradeCost += price * amount;
		}
		if (!upgradeOnly) {
			Minion parent = minion.getParent();
			if (parent != null) {
				upgradeCost += calculateUpgradeCosts(parent, false);
			}
		}
		return upgradeCost;
	}

	public double getPrice(String internalName) {
		//Is minion
		if (internalName.contains("_GENERATOR_")) {
			return calculateUpgradeCosts(manager.getMinionById(internalName), false);
		}

		//Is bazaar item
		JsonObject bazaarInfo = NotEnoughUpdates.INSTANCE.manager.auctionManager.getBazaarInfo(internalName);
		if (bazaarInfo != null) {
			String bazaarMode;
			if (manager.getOverlay().isUseInstantBuyPrice()) {
				bazaarMode = "curr_buy";
			} else {
				bazaarMode = "curr_sell";
			}
			if (!bazaarInfo.has(bazaarMode)) {

				// Use buy order price when no sell offer exist. (e.g. inferno apex)
				if (bazaarMode.equals("curr_buy")) {
					bazaarMode = "curr_sell";
					if (!bazaarInfo.has(bazaarMode)) {
						System.err.println(bazaarMode + " does not exist for '" + internalName + "'");
						return 0;
					}
				} else {
					System.err.println(bazaarMode + " does not exist for '" + internalName + "'");
					return 0;
				}
			}
			return bazaarInfo.get(bazaarMode).getAsDouble();
		}

		//is ah bin
		double avgBinPrice = NotEnoughUpdates.INSTANCE.manager.auctionManager.getItemAvgBin(internalName);
		if (avgBinPrice >= 1) return avgBinPrice;

		//is ah without bin
		JsonObject auctionInfo = NotEnoughUpdates.INSTANCE.manager.auctionManager.getItemAuctionInfo(internalName);
		if (auctionInfo == null) {
			//only wood axe and similar useless items
			return 1;
		}
		return (auctionInfo.get("price").getAsFloat() / auctionInfo.get("count").getAsFloat());
	}

	public String formatCoins(double coins) {
		return formatCoins(coins, "");
	}

	public String formatCoins(double coins, String extraFormat) {
		int i = coins < 3 ? 1 : 0;
		String format = Utils.shortNumberFormat(coins, i);
		return "§6" + extraFormat + format + " coins";
	}
}
