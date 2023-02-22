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
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.text.NumberFormat;
import java.util.Locale;

@NEUAutoSubscribe
public class GardenNpcPrices {

	private final String itemRegex = "§5§o §.(.*) §8x(\\d+)";
	private final NumberFormat format = NumberFormat.getNumberInstance();

	@SubscribeEvent
	public void onGardenNpcPrices(ItemTooltipEvent event) {
		if (!NotEnoughUpdates.INSTANCE.config.tooltipTweaks.gardenNpcPrice) return;
		if (event.toolTip.size() <= 1) return;
		//§5§o §aEnchanted Cactus Green §8x421
		//§5§o §aEnchanted Hay Bale §8x62
		//§5§o §9Enchanted Cookie §8x4
		if (event.itemStack.getItem() == Item.getItemFromBlock(Blocks.stained_hardened_clay) && event.toolTip.get(2).matches(itemRegex)) {
			String productLine = event.toolTip.get(2);
			double cost = calculateCost(productLine.replaceAll(itemRegex, "$1").replace(" ", "_").toUpperCase(Locale.ROOT), Integer.parseInt(productLine.replaceAll(itemRegex, "$2")));

			event.toolTip.set(2, productLine + " §e(" + format.format(cost) + " coins)");

		}
	}
	public double calculateCost(String internalName, int amount) {
		double price = NotEnoughUpdates.INSTANCE.manager.auctionManager.getBazaarOrBin(internalName, true);
		if (price != -1) {
			return price * amount;
		}
		return 0d;
	}
}
