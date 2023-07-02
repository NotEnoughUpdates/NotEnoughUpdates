/*
 * Copyright (C) 2023 NotEnoughUpdates contributors
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

package io.github.moulberry.notenoughupdates.miscfeatures;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.autosubscribe.NEUAutoSubscribe;
import io.github.moulberry.notenoughupdates.util.ItemResolutionQuery;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@NEUAutoSubscribe
public class GardenNpcPrices {
	private final Pattern itemRegex = Pattern.compile("§5§o §.([a-zA-Z \\-]+)(?:§8x(\\d+))?");
	//§5§o §aEnchanted Cactus Green §8x421
	//§5§o §aEnchanted Hay Bale §8x62
	//§5§o §9Enchanted Cookie §8x4
	//§5§o §9Tightly-Tied Hay Bale
	private final Pattern copperRegex = Pattern.compile(".* §8\\+§c(\\d+) Copper.*");
	//§5§o §8+§c24 Copper
	//§5§o §8+§c69 Copper
	//§5§o §8+§cNaN Copper
	private Map<List<String>, List<String>> prices = new HashMap<>();

	@SubscribeEvent
	public void onGardenNpcPrices(ItemTooltipEvent event) {
		if (!NotEnoughUpdates.INSTANCE.config.tooltipTweaks.gardenNpcPrice) return;
		if (event.toolTip.size() <= 2 || event.itemStack.getItem() != Item.getItemFromBlock(Blocks.stained_hardened_clay)) return;

		List<String> tooltipCopy = new ArrayList<>(event.toolTip);
		if (prices.get(tooltipCopy) == null) {
			for (int i = 2; i < event.toolTip.size(); i++) {
				Matcher matcher = itemRegex.matcher(event.toolTip.get(i));

				if (matcher.matches()) {
					int amount = 1;
					if (matcher.group(2) != null) amount = Integer.parseInt(matcher.group(2));
					double cost = calculateCost(ItemResolutionQuery.findInternalNameByDisplayName(matcher.group(1).trim(), false), amount);
					event.toolTip.set(i, event.toolTip.get(i) + " §7(§6" + (cost == 0 ? "?" : Utils.shortNumberFormat(cost, 0)) + "§7 coins)");
				}

				else if (NotEnoughUpdates.INSTANCE.config.tooltipTweaks.copperCoins) {
					Matcher copperMatcher = copperRegex.matcher(event.toolTip.get(i));
					if (copperMatcher.matches()) {
						int amount = Integer.parseInt(copperMatcher.group(1));
						Object[] copperMax = calculateCopper(amount);
						event.toolTip.set(i, event.toolTip.get(i) + " §7(§6" + Utils.shortNumberFormat((Double) copperMax[1], 0) + "§7 selling "+copperMax[0]+"§7)");
					}
				}

				else {
					prices.put(tooltipCopy, event.toolTip);
				}
			}
		} else {
			event.toolTip.clear();
			event.toolTip.addAll(prices.get(tooltipCopy));
		}
	}

	public Object[] calculateCopper(int amount) {
		JsonObject skymart = Utils.getConstant("skymart", NotEnoughUpdates.INSTANCE.manager.gson);
		Map<String, Double> prices = new HashMap<>();
		for (Map.Entry<String, JsonElement> entry : skymart.entrySet()) {
			String internalName = entry.getKey();
			JsonObject item = entry.getValue().getAsJsonObject();
			if (!Objects.equals(item.get("currency").getAsString(), "copper") || (item.get("isWorthless").getAsBoolean() && NotEnoughUpdates.INSTANCE.config.tooltipTweaks.ignoreBeginnerItems)) continue;
			boolean isBazaar = NotEnoughUpdates.INSTANCE.manager.auctionManager.getBazaarInfo(internalName)!=null;
			if (!isBazaar&&NotEnoughUpdates.INSTANCE.config.tooltipTweaks.ignoreAHItems) continue;
			double price = NotEnoughUpdates.INSTANCE.manager.auctionManager.getBazaarOrBin(internalName, false) /item.get("price").getAsInt()*amount;
			prices.put(item.get("display").getAsString(), price);
		}
		Map.Entry<String, Double> maxPrice = Collections.max(prices.entrySet(), Map.Entry.comparingByValue());
		return new Object[]{maxPrice.getKey(), maxPrice.getValue()};
	}

	public double calculateCost(String internalName, int amount) {
		double price = NotEnoughUpdates.INSTANCE.manager.auctionManager.getBazaarOrBin(internalName, false);
		if (price != -1) {
			return price * amount;
		}
		return 0d;
	}

	@SubscribeEvent
	public void onWorldLoad(WorldEvent.Load event) {
		prices.clear();
	}
}
