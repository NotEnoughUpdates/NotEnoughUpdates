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

package io.github.moulberry.notenoughupdates.miscfeatures;

import com.google.gson.JsonObject;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.core.util.StringUtils;
import io.github.moulberry.notenoughupdates.miscgui.TrophyRewardOverlay;
import io.github.moulberry.notenoughupdates.util.ItemUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class BazaarSacksProfit {

	private static BazaarSacksProfit INSTANCE = null;
	private boolean showSellOrderPrice = false;
	private boolean pressedShiftLast = false;

	public static BazaarSacksProfit getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new BazaarSacksProfit();
		}
		return INSTANCE;
	}

	private final Map<String, Integer> prices = new HashMap<>();
	private final Map<String, String> names = new HashMap<>();
	private final List<String> invalidNames = new ArrayList<>();

	@SubscribeEvent
	public void onGuiOpen(GuiOpenEvent event) {
		showSellOrderPrice = false;
		prices.clear();
		names.clear();
		invalidNames.clear();
	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public void onItemTooltipLow(ItemTooltipEvent event) {
		if (!NotEnoughUpdates.INSTANCE.config.tooltipTweaks.bazaarSacksProfit) return;
		if (!inBazaar()) return;

		ItemStack itemStack = event.itemStack;
		String displayName = itemStack.getDisplayName();
		if (!displayName.equals("§bSell Sacks Now")) return;

		boolean shift = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
		if (!pressedShiftLast && shift) {
			showSellOrderPrice = !showSellOrderPrice;
		}
		pressedShiftLast = shift;

		if (prices.isEmpty()) {
			out:
			for (String line : ItemUtils.getLore(itemStack)) {
				if (line.contains("§7x ")) {
					String[] split = line.split("§7x ");
					String rawAmount = StringUtils.cleanColour(split[0]).replace(",", "").substring(1);
					int amount = Integer.parseInt(rawAmount);
					String bazaarName = split[1].split(" §7for")[0];
					for (Map.Entry<String, JsonObject> entry : NotEnoughUpdates.INSTANCE.manager
						.getItemInformation()
						.entrySet()) {
						String internalName = entry.getKey();
						JsonObject object = entry.getValue();
						if (object.has("displayname")) {
							String displayname = object.get("displayname").getAsString();
							if (displayname.equals(bazaarName)) {
								prices.put(internalName, amount);
								names.put(internalName, bazaarName);
								continue out;
							}
						}
					}
					System.out.println("no bazaar item in repo found for '" + bazaarName + "'");
					invalidNames.add(bazaarName);
				}
			}
		}

		event.toolTip.removeIf(line -> line.contains("§7x ") || line.contains("You earn:"));

		Map<String, Float> map = new HashMap<>();
		DecimalFormat formatter = (DecimalFormat) NumberFormat.getNumberInstance(Locale.ENGLISH);
		formatter.applyPattern("#,##0");
		double totalPrice = 0;
		for (Map.Entry<String, Integer> entry : prices.entrySet()) {
			String internalName = entry.getKey();
			int amount = entry.getValue();
			String name = names.get(internalName);

			JsonObject bazaarInfo = NotEnoughUpdates.INSTANCE.manager.auctionManager.getBazaarInfo(internalName);

			float price = 0;
			if (bazaarInfo != null) {

				if (showSellOrderPrice) {
					if (bazaarInfo.has("curr_buy")) {
						price = bazaarInfo.get("curr_buy").getAsFloat();
					} else {
						System.err.println("curr_sell does not exist for '" + internalName + "'");
					}
				} else {
					if (bazaarInfo.has("curr_sell")) {
						price = bazaarInfo.get("curr_sell").getAsFloat();
					} else {
						System.err.println("curr_sell does not exist for '" + internalName + "'");
					}
				}
			}
			float extraPrice = price * amount;
			String priceFormat = formatter.format(extraPrice);
			totalPrice += extraPrice;
			map.put("§a" + formatter.format(amount) + "§7x §f" + name + " §6" + priceFormat, extraPrice);
		}

		if (showSellOrderPrice) {
			event.toolTip.add(4, "§7Sell order price: §6" + formatter.format(totalPrice));
		} else {
			event.toolTip.add(4, "§7Instantly sell price: §6" + formatter.format(totalPrice));
		}

		event.toolTip.add(4, "");
		for (String name : invalidNames) {
			event.toolTip.add(4, name + " §cMissing repo data!");
		}
		for (String text : TrophyRewardOverlay.sortByValue(map).keySet()) {
			event.toolTip.add(4, text);
		}

		event.toolTip.add("");
		if (!showSellOrderPrice) {
			event.toolTip.add("§8[Press SHIFT to show sell order price]");
		} else {
			event.toolTip.add("§8[Press SHIFT to show instantly sell price]");
		}
	}

	public static boolean inBazaar() {
		if (!NotEnoughUpdates.INSTANCE.isOnSkyblock()) return false;

		Minecraft minecraft = Minecraft.getMinecraft();
		if (minecraft == null || minecraft.thePlayer == null) return false;

		Container inventoryContainer = minecraft.thePlayer.openContainer;
		if (!(inventoryContainer instanceof ContainerChest)) return false;
		ContainerChest containerChest = (ContainerChest) inventoryContainer;
		return containerChest.getLowerChestInventory().getDisplayName().getUnformattedText().startsWith("Bazaar ");
	}
}
