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

package io.github.moulberry.notenoughupdates.listener;

import com.google.gson.JsonObject;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.core.util.StringUtils;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ItemTooltipEssenceShopListener {
	private final NotEnoughUpdates neu;

	private final Pattern ESSENCE_PATTERN = Pattern.compile("§5§o§d([\\d,]+) (.+) Essence");

	public ItemTooltipEssenceShopListener(NotEnoughUpdates neu) {
		this.neu = neu;
	}

	@SubscribeEvent
	public void onItemTooltip(ItemTooltipEvent event) {
		if (!neu.isOnSkyblock()) return;
		if (event.toolTip == null) return;
		if (!Utils.getOpenChestName().endsWith(" Essence Shop")) return;
		if (!NotEnoughUpdates.INSTANCE.config.tooltipTweaks.essencePriceInEssenceShop) return;

		List<String> newToolTip = new ArrayList<>();
		boolean next = false;
		for (String line : event.toolTip) {

			if (next) {
				next = false;
				Matcher matcher = ESSENCE_PATTERN.matcher(line);
				if (matcher.matches()) {
					String rawNumber = matcher.group(1).replace(",", "");
					int amount = Integer.parseInt(rawNumber);
					String type = matcher.group(2);

					String essenceName = "ESSENCE_" + type.toUpperCase();
					JsonObject bazaarInfo = neu.manager.auctionManager.getBazaarInfo(essenceName);

					if (bazaarInfo != null && bazaarInfo.has("curr_sell")) {
						float bazaarPrice = bazaarInfo.get("curr_sell").getAsFloat();
						double price = bazaarPrice * amount;
						String format = StringUtils.shortNumberFormat(price);
						newToolTip.add(line + " §7(§6" + format + " coins§7)");
						continue;
					}
				}
			}

			if (line.contains("Cost")) {
				next = true;
			}
			newToolTip.add(line);
		}

		event.toolTip.clear();
		event.toolTip.addAll(newToolTip);
	}
}
