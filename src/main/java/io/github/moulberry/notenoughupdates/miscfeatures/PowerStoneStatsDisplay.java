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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.autosubscribe.NEUAutoSubscribe;
import io.github.moulberry.notenoughupdates.core.util.StringUtils;
import io.github.moulberry.notenoughupdates.events.ProfileDataLoadedEvent;
import io.github.moulberry.notenoughupdates.options.NEUConfig;
import io.github.moulberry.notenoughupdates.profileviewer.ProfileViewerUtils;
import io.github.moulberry.notenoughupdates.util.ItemUtils;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.LinkedList;
import java.util.List;

@NEUAutoSubscribe
public class PowerStoneStatsDisplay {
	private static PowerStoneStatsDisplay instance = null;
	private boolean dirty = true;

	public static PowerStoneStatsDisplay getInstance() {
		if (instance == null) {
			instance = new PowerStoneStatsDisplay();
		}
		return instance;
	}

	@SubscribeEvent
	public void onProfileDataLoaded(ProfileDataLoadedEvent event) {
		JsonObject profileInfo = event.getProfileInfo();
		if (profileInfo == null) return;

		JsonArray inventoryInfo = ProfileViewerUtils.readInventoryInfo(profileInfo, "talisman_bag");
		if (inventoryInfo == null) return;

		NEUConfig.HiddenProfileSpecific configProfileSpecific = NotEnoughUpdates.INSTANCE.config.getProfileSpecific();
		if (configProfileSpecific == null) return;
		configProfileSpecific.magicalPower = ProfileViewerUtils.getMagicalPower(inventoryInfo, profileInfo);
	}

	@SubscribeEvent
	public void onTick(TickEvent event) {
		if (!NotEnoughUpdates.INSTANCE.config.tooltipTweaks.powerStoneStats) return;
		if (!dirty) return;
		if (!Utils.getOpenChestName().equals("Your Bags")) return;

		EntityPlayerSP p = Minecraft.getMinecraft().thePlayer;
		Container openContainer = p.openContainer;
		for (Slot slot : openContainer.inventorySlots) {
			ItemStack stack = slot.getStack();
			if (stack == null) continue;

			String displayName = stack.getDisplayName();
			if (!"§aAccessory Bag".equals(displayName)) continue;
			dirty = false;

			for (String line : ItemUtils.getLore(stack)) {
				if (line.startsWith("§7Magical Power: ")) {
					String rawNumber = line.split("§6")[1].replace(",", "");
					NEUConfig.HiddenProfileSpecific configProfileSpecific = NotEnoughUpdates.INSTANCE.config.getProfileSpecific();
					if (configProfileSpecific == null) return;
					configProfileSpecific.magicalPower = Integer.parseInt(rawNumber);
				}
			}
		}
	}

	@SubscribeEvent
	public void onGuiOpen(GuiOpenEvent event) {
		if (event.gui != null) {
			dirty = true;
		}
	}

	@SubscribeEvent
	public void onItemTooltipLow(ItemTooltipEvent event) {
		if (!NotEnoughUpdates.INSTANCE.config.tooltipTweaks.powerStoneStats) return;

		ItemStack itemStack = event.itemStack;
		if (itemStack == null) return;
		List<String> lore = ItemUtils.getLore(itemStack);

		boolean isPowerStone = false;
		for (String line : lore) {
			if (line.equals("§8Power Stone")) {
				isPowerStone = true;
				break;
			}
		}

		if (!isPowerStone) return;

		NEUConfig.HiddenProfileSpecific configProfileSpecific = NotEnoughUpdates.INSTANCE.config.getProfileSpecific();
		if (configProfileSpecific == null) return;

		int magicalPower = configProfileSpecific.magicalPower;
		if (magicalPower < 1) return;

		double scaledMagicalPower = scalePower(magicalPower);
		double scaledCurrentPower = 0.0;

		int index = 0;
		boolean foundMagicalPower = false;
		for (String line : new LinkedList<>(lore)) {
			index++;
			line = line.replace("§k", "");

			if (line.startsWith("§7At ")) {

				String rawNumber = StringUtils.substringBetween(StringUtils.cleanColour(line), "At ", " Magical");
				if (rawNumber == null) return;

				//This ignores old repo entries in the item browser from neu
				if (rawNumber.equals("mmm")) return;

				try {
					scaledCurrentPower = scalePower(StringUtils.cleanAndParseInt(rawNumber));
				} catch (NumberFormatException ignored) {
					return;
				}

				event.toolTip.set(index, "§7At §6" + StringUtils.formatNumber((double) magicalPower) + " Magical Power§7:");
				foundMagicalPower = true;
				continue;
			}

			if (!foundMagicalPower) continue;

			String cleanLine = StringUtils.cleanColour(line);
			if (cleanLine.equals("")) {
				break;
			}

			for (String operator : new String[]{"+", "-"}) {
				if (!cleanLine.startsWith(operator)) continue;
				String rawStat = StringUtils.cleanColour(StringUtils.substringBetween(line, operator, " "));

				double currentStat;
				try {
					currentStat = 0.0 + StringUtils.cleanAndParseInt(rawStat.substring(0, rawStat.length() - 1));
				} catch (NumberFormatException ignored) {
					continue;
				}
				double realStat = (currentStat / scaledCurrentPower) * scaledMagicalPower;

				String format = StringUtils.formatNumber((double) Math.round(realStat));
				format += rawStat.substring(rawStat.length() - 1);

				event.toolTip.set(index, line.replace(rawStat, format));
			}
		}
	}

	private double scalePower(int magicalPower) {
		return Math.pow(29.97 * (Math.log(0.0019 * magicalPower + 1)), 1.2);
	}
}
