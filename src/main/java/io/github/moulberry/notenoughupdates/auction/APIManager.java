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

package io.github.moulberry.notenoughupdates.auction;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.moulberry.notenoughupdates.ItemPriceInformation;
import io.github.moulberry.notenoughupdates.NEUManager;
import io.github.moulberry.notenoughupdates.miscgui.pricegraph.LocalGraphDataProvider;
import io.github.moulberry.notenoughupdates.recipes.Ingredient;
import io.github.moulberry.notenoughupdates.recipes.ItemShopRecipe;
import io.github.moulberry.notenoughupdates.recipes.NeuRecipe;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class APIManager {
	private final NEUManager manager;

	private final int LOWEST_BIN_UPDATE_INTERVAL = 2 * 60 * 1000; // 2 minutes
	private final int AUCTION_AVG_UPDATE_INTERVAL = 5 * 60 * 1000; // 5 minutes
	private final int BAZAAR_UPDATE_INTERVAL = 5 * 60 * 1000; // 5 minutes

	private JsonObject lowestBins = null;
	private JsonObject auctionPricesAvgLowestBinJson = null;

	private JsonObject bazaarJson = null;
	private JsonObject auctionPricesJson = null;
	private final HashMap<String, CraftInfo> craftCost = new HashMap<>();

	private boolean didFirstUpdate = false;
	private long lastAuctionAvgUpdate = 0;
	private long lastBazaarUpdate = 0;
	private long lastLowestBinUpdate = 0;

	public APIManager(NEUManager manager) {
		this.manager = manager;
	}

	public void tick() {
		long currentTime = System.currentTimeMillis();
		if (currentTime - lastAuctionAvgUpdate > AUCTION_AVG_UPDATE_INTERVAL) {
			lastAuctionAvgUpdate = currentTime - AUCTION_AVG_UPDATE_INTERVAL + 60 * 1000; // Try again in 1 minute on failure
			updateAvgPrices();
		}
		if (currentTime - lastBazaarUpdate > BAZAAR_UPDATE_INTERVAL) {
			lastBazaarUpdate = currentTime - BAZAAR_UPDATE_INTERVAL + 60 * 1000; // Try again in 1 minute on failure
			updateBazaar();
		}
		if (currentTime - lastLowestBinUpdate > LOWEST_BIN_UPDATE_INTERVAL) {
			lastLowestBinUpdate = currentTime - LOWEST_BIN_UPDATE_INTERVAL + 30 * 1000;  // Try again in 30 seconds on failure
			updateLowestBin();
		}
	}

	public Set<String> getLowestBinKeySet() {
		if (lowestBins == null) return new HashSet<>();
		HashSet<String> keys = new HashSet<>();
		for (Map.Entry<String, JsonElement> entry : lowestBins.entrySet()) {
			keys.add(entry.getKey());
		}
		return keys;
	}

	public long getLowestBin(String internalName) {
		if (lowestBins != null && lowestBins.has(internalName)) {
			JsonElement e = lowestBins.get(internalName);
			if (e.isJsonPrimitive() && e.getAsJsonPrimitive().isNumber()) {
				return e.getAsLong();
			}
		}
		return -1;
	}

	public void updateLowestBin() {
		manager.apiUtils
			.newMoulberryRequest("lowestbin.json.gz")
			.gunzip()
			.requestJson()
			.thenAcceptAsync(jsonObject -> {
				if (lowestBins == null) {
					lowestBins = new JsonObject();
				}
				if (!jsonObject.entrySet().isEmpty()) {
					lastLowestBinUpdate = System.currentTimeMillis();
				}
				for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
					lowestBins.add(entry.getKey(), entry.getValue());
				}
				if (!didFirstUpdate) {
					ItemPriceInformation.updateAuctionableItemsList();
					didFirstUpdate = true;
				}
				LocalGraphDataProvider.INSTANCE.savePrices(lowestBins, false);
			});
	}

	public long getLastLowestBinUpdateTime() {
		return lastLowestBinUpdate;
	}

	private static final Pattern BAZAAR_ENCHANTMENT_PATTERN = Pattern.compile("ENCHANTMENT_(\\D*)_(\\d+)");

	public String transformHypixelBazaarToNEUItemId(String hypixelId) {
		Matcher matcher = BAZAAR_ENCHANTMENT_PATTERN.matcher(hypixelId);
		if (matcher.matches()) {
			return matcher.group(1) + ";" + matcher.group(2);
		}
		return hypixelId.replace(":", "-");
	}

	public void updateBazaar() {
		manager.apiUtils
			.newAnonymousHypixelApiRequest("skyblock/bazaar")
			.requestJson()
			.thenAcceptAsync(jsonObject -> {
				if (!jsonObject.get("success").getAsBoolean()) return;

				craftCost.clear();
				bazaarJson = new JsonObject();
				JsonObject products = jsonObject.get("products").getAsJsonObject();
				for (Map.Entry<String, JsonElement> entry : products.entrySet()) {
					if (entry.getValue().isJsonObject()) {
						JsonObject productInfo = new JsonObject();

						JsonObject product = entry.getValue().getAsJsonObject();
						JsonObject quickStatus = product.getAsJsonObject("quick_status");
						if (!hasData(quickStatus)) continue;

						productInfo.addProperty("avg_buy", quickStatus.get("buyPrice").getAsFloat());
						productInfo.addProperty("avg_sell", quickStatus.get("sellPrice").getAsFloat());

						float instasellsWeekly = quickStatus.get("sellMovingWeek").getAsFloat();
						float instabuysWeekly = quickStatus.get("buyMovingWeek").getAsFloat();
						productInfo.addProperty("instasells_weekly", instasellsWeekly);
						productInfo.addProperty("instabuys_weekly", instabuysWeekly);
						productInfo.addProperty("instasells_daily", instasellsWeekly / 7);
						productInfo.addProperty("instabuys_daily", instabuysWeekly / 7);
						productInfo.addProperty("instasells_hourly", instasellsWeekly / 7 / 24);
						productInfo.addProperty("instabuys_hourly", instabuysWeekly / 7 / 24);

						for (JsonElement element : product.get("sell_summary").getAsJsonArray()) {
							if (element.isJsonObject()) {
								JsonObject sellSummaryFirst = element.getAsJsonObject();
								productInfo.addProperty("curr_sell", sellSummaryFirst.get("pricePerUnit").getAsFloat());
								break;
							}
						}

						for (JsonElement element : product.get("buy_summary").getAsJsonArray()) {
							if (element.isJsonObject()) {
								JsonObject sellSummaryFirst = element.getAsJsonObject();
								productInfo.addProperty("curr_buy", sellSummaryFirst.get("pricePerUnit").getAsFloat());
								break;
							}
						}

						bazaarJson.add(transformHypixelBazaarToNEUItemId(entry.getKey()), productInfo);
					}
				}
				LocalGraphDataProvider.INSTANCE.savePrices(bazaarJson, true);
			});
	}

	private static boolean hasData(JsonObject quickStatus) {
		for (Map.Entry<String, JsonElement> e : quickStatus.entrySet()) {
			String key = e.getKey();
			if (!key.equals("productId")) {
				double value = e.getValue().getAsDouble();
				if (value != 0) {
					return true;
				}
			}
		}
		return false;
	}

	public void updateAvgPrices() {
		manager.apiUtils
			.newMoulberryRequest("auction_averages/3day.json.gz")
			.gunzip().requestJson().thenAcceptAsync((jsonObject) -> {
						 craftCost.clear();
						 auctionPricesJson = jsonObject;
						 lastAuctionAvgUpdate = System.currentTimeMillis();
					 });
		manager.apiUtils
			.newMoulberryRequest("auction_averages_lbin/1day.json.gz")
			.gunzip().requestJson()
			.thenAcceptAsync((jsonObject) -> {
				auctionPricesAvgLowestBinJson = jsonObject;
			});
	}

	public Set<String> getItemAuctionInfoKeySet() {
		if (auctionPricesJson == null) return new HashSet<>();
		HashSet<String> keys = new HashSet<>();
		for (Map.Entry<String, JsonElement> entry : auctionPricesJson.entrySet()) {
			keys.add(entry.getKey());
		}
		return keys;
	}

	public JsonObject getItemAuctionInfo(String internalname) {
		if (auctionPricesJson == null) return null;
		JsonElement e = auctionPricesJson.get(internalname);
		if (e == null) {
			return null;
		}
		return e.getAsJsonObject();
	}

	public double getItemAvgBin(String internalName) {
		if (auctionPricesAvgLowestBinJson == null) return -1;
		JsonElement e = auctionPricesAvgLowestBinJson.get(internalName);
		if (e == null) {
			return -1;
		}
		return Math.round(e.getAsDouble());
	}

	public Set<String> getBazaarKeySet() {
		if (bazaarJson == null) return new HashSet<>();
		HashSet<String> keys = new HashSet<>();
		for (Map.Entry<String, JsonElement> entry : bazaarJson.entrySet()) {
			keys.add(entry.getKey());
		}
		return keys;
	}

	public double getBazaarOrBin(String internalName, boolean useSellingPrice) {
		String curr = (useSellingPrice ? "curr_sell" : "curr_buy");
		JsonObject bazaarInfo = manager.auctionManager.getBazaarInfo(internalName);
		if (bazaarInfo != null && bazaarInfo.get(curr) != null) {
			return bazaarInfo.get(curr).getAsFloat();
		} else {
			return manager.auctionManager.getLowestBin(internalName);
		}
	}

	public JsonObject getBazaarInfo(String internalName) {
		if (bazaarJson == null) return null;
		JsonElement e = bazaarJson.get(internalName);
		if (e == null) {
			return null;
		}
		return e.getAsJsonObject();
	}

	private static final List<String> hardcodedVanillaItems = Utils.createList(
		"WOOD_AXE", "WOOD_HOE", "WOOD_PICKAXE", "WOOD_SPADE", "WOOD_SWORD",
		"GOLD_AXE", "GOLD_HOE", "GOLD_PICKAXE", "GOLD_SPADE", "GOLD_SWORD",
		"ROOKIE_HOE"
	);

	public boolean isVanillaItem(String internalName) {
		if (hardcodedVanillaItems.contains(internalName)) return true;

		//Removes trailing numbers and underscores, eg. LEAVES_2-3 -> LEAVES
		String vanillaName = internalName.split("-")[0];
		if (manager.getItemInformation().containsKey(vanillaName)) {
			JsonObject json = manager.getItemInformation().get(vanillaName);
			if (json != null && json.has("vanilla") && json.get("vanilla").getAsBoolean()) return true;
		}
		return Item.itemRegistry.getObject(new ResourceLocation(vanillaName)) != null;
	}

	public static class CraftInfo {
		public boolean fromRecipe = false;
		public boolean vanillaItem = false;
		public double craftCost = -1;
	}

	public CraftInfo getCraftCost(String internalName) {
		return getCraftCost(internalName, new HashSet<>());
	}

	/**
	 * Recursively calculates the cost of crafting an item from raw materials.
	 */
	private CraftInfo getCraftCost(String internalName, Set<String> visited) {
		if (craftCost.containsKey(internalName)) return craftCost.get(internalName);
		if (visited.contains(internalName)) return null;
		visited.add(internalName);

		boolean vanillaItem = isVanillaItem(internalName);
		double craftCost = Double.POSITIVE_INFINITY;

		JsonObject auctionInfo = getItemAuctionInfo(internalName);
		double lowestBin = getLowestBin(internalName);
		JsonObject bazaarInfo = getBazaarInfo(internalName);

		if (bazaarInfo != null && bazaarInfo.get("curr_buy") != null) {
			craftCost = bazaarInfo.get("curr_buy").getAsFloat();
		}
		//Don't use auction prices for vanilla items cuz people like to transfer money, messing up the cost of vanilla items.
		if (!vanillaItem) {
			if (lowestBin > 0) {
				craftCost = Math.min(lowestBin, craftCost);
			} else if (auctionInfo != null) {
				float auctionPrice = auctionInfo.get("price").getAsFloat() / auctionInfo.get("count").getAsInt();
				craftCost = Math.min(auctionPrice, craftCost);
			}
		}

		Set<NeuRecipe> recipes = manager.getRecipesFor(internalName);
		boolean fromRecipe = false;
		if (recipes != null)
			RECIPE_ITER:
				for (NeuRecipe recipe : recipes) {
					if (recipe instanceof ItemShopRecipe) {
						if (vanillaItem) {
							continue;
						}
					}
					if (recipe.hasVariableCost() || !recipe.shouldUseForCraftCost()) continue;
					float craftPrice = 0;
					for (Ingredient i : recipe.getIngredients()) {
						if (i.isCoins()) {
							craftPrice += i.getCount();
							continue;
						}
						CraftInfo ingredientCraftCost = getCraftCost(i.getInternalItemId(), visited);
						if (ingredientCraftCost == null)
							continue RECIPE_ITER; // Skip recipes with items further up the chain
						craftPrice += ingredientCraftCost.craftCost * i.getCount();
					}
					int resultCount = 0;
					for (Ingredient item : recipe.getOutputs())
						if (item.getInternalItemId().equals(internalName))
							resultCount += item.getCount();

					if (resultCount == 0)
						continue;
					float craftPricePer = craftPrice / resultCount;
					if (craftPricePer < craftCost) {
						fromRecipe = true;
						craftCost = craftPricePer;
					}
				}
		visited.remove(internalName);
		if (Double.isInfinite(craftCost)) {
			return null;
		}
		CraftInfo craftInfo = new CraftInfo();
		craftInfo.vanillaItem = vanillaItem;
		craftInfo.craftCost = craftCost;
		craftInfo.fromRecipe = fromRecipe;
		this.craftCost.put(internalName, craftInfo);
		return craftInfo;
	}
}
