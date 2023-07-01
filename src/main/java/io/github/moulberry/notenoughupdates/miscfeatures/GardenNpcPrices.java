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
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@NEUAutoSubscribe
public class GardenNpcPrices {
	private enum SkyMartItem {

		GARDEN_SCYTHE("GARDEN_SCYTHE", EnumChatFormatting.BLUE+"Garden Scythe", 20, true),
		BUILDERS_RULER("BUILDERS_RULER", EnumChatFormatting.GOLD+"Builder's Ruler", 20, true),
		SUNDER("SUNDER;1", EnumChatFormatting.BLUE+"Sunder I", 10, false),
		DEDICATION("DEDICATION;1", EnumChatFormatting.BLUE+"Dedication", 250, false),
		GREEN_THUMB("GREEN_THUMB;1", EnumChatFormatting.BLUE+"Green Thumb", 1500, false),
		LOTUS_BRACELET("LOTUS_BRACELET", EnumChatFormatting.BLUE+"Lotus Bracelet", 50, false),
		LOTUS_BELT("LOTUS_BELT", EnumChatFormatting.BLUE+"Lotus Belt", 100, false),
		LOTUS_NECKLACE("LOTUS_NECKLACE", EnumChatFormatting.BLUE+"Lotus Necklace", 200, false),
		LOTUS_CLOAK("LOTUS_CLOAK", EnumChatFormatting.BLUE+"Lotus Cloak", 500, false),
		YELLOW_BANDANA("YELLOW_BANDANA", EnumChatFormatting.BLUE+"Yellow Bandana", 300, false),
		BASIC_GARDENING_HOE("BASIC_GARDENING_HOE", EnumChatFormatting.GREEN+"Basic Gardening Hoe", 5, true),
		ADVANCED_GARDENING_HOE("ADVANCED_GARDENING_HOE", EnumChatFormatting.BLUE+"Advanced Gardening Hoe", 25, true),
		BASIC_GARDENING_AXE("BASIC_GARDENING_AXE", EnumChatFormatting.GREEN+"Basic Gardening Axe", 5, true),
		ADVANCED_GARDENING_AXE("ADVANCED_GARDENING_AXE", EnumChatFormatting.BLUE+"Advanced Gardening Axe", 25, true),
		SKYMART_BROCHURE("SKYMART_BROCHURE", EnumChatFormatting.BLUE+"SkyMart Brochure", 100, false),
		LARGE_WALNUT("LARGE_WALNUT", EnumChatFormatting.BLUE+"Large Walnut", 150, false),
		HIVE_BARN_SKIN("HIVE_BARN_SKIN", EnumChatFormatting.GOLD+"Hive Barn Skin", 10000, false),
		TRADING_POST_BARN_SKIN("TRADING_POST_BARN_SKIN", EnumChatFormatting.GREEN+"Trading Post Barn Skin", 1000, false),
		AUTUMN_HUT_BARN_SKIN("AUTUMN_HUT_BARN_SKIN", EnumChatFormatting.GREEN+"Autumn Hut Barn Skin", 2000, false),
		CASTLE_BARN_SKIN("CASTLE_BARN_SKIN", EnumChatFormatting.GOLD+"Castle Barn Skin", 15000, false),
		BAMBOO_BARN_SKIN("BAMBOO_BARN_SKIN", EnumChatFormatting.DARK_PURPLE+"Bamboo Barn Skin", 7500, false),
		;
		private final String internalName;
		private final String displayName;
		private final int copperPrice;
		private final boolean isWorthless;
		SkyMartItem(String internalName, String displayName, int copperPrice, boolean isWorthless) {
			this.internalName = internalName;
			this.displayName = displayName;
			this.copperPrice = copperPrice;
			this.isWorthless = isWorthless;
		}
	}
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
		Map<String, Double> prices = new HashMap<>();
		for (SkyMartItem item : SkyMartItem.values()) {
			if (item.isWorthless && NotEnoughUpdates.INSTANCE.config.tooltipTweaks.ignoreBeginnerItems) continue;
			boolean isBazaar = NotEnoughUpdates.INSTANCE.manager.auctionManager.getBazaarInfo(item.internalName)!=null;
			if (!isBazaar&&NotEnoughUpdates.INSTANCE.config.tooltipTweaks.ignoreAHItems) continue;
			double price = NotEnoughUpdates.INSTANCE.manager.auctionManager.getBazaarOrBin(item.internalName, false) /item.copperPrice*amount;
			prices.put(item.displayName, price);
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
