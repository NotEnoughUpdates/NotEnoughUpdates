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

import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.core.util.StringUtils;
import io.github.moulberry.notenoughupdates.util.ItemUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

import java.text.NumberFormat;
import java.util.LinkedList;
import java.util.Locale;

public class PowerStoneStatsDisplay {
	private static PowerStoneStatsDisplay instance = null;
	//	DecimalFormat decimalFormat = new DecimalFormat("#,##0");
	NumberFormat format = NumberFormat.getInstance(Locale.US);

	//TODO remove method
	private String formatNumber(double number) {
//		return decimalFormat.format(number).replace(".", ",");
		return format.format(number);
	}

	public static PowerStoneStatsDisplay getInstance() {
		if (instance == null) {
			instance = new PowerStoneStatsDisplay();
		}
		return instance;
	}

	@SubscribeEvent
	public void onTick(TickEvent event) {
		if (!NotEnoughUpdates.INSTANCE.config.tooltipTweaks.powerStoneStats) return;
		GuiScreen currentScreen = Minecraft.getMinecraft().currentScreen;
		if (currentScreen == null) return;
		if (!(currentScreen instanceof GuiChest)) return;
		ContainerChest container = (ContainerChest) ((GuiChest) currentScreen).inventorySlots;
		IInventory menu = container.getLowerChestInventory();
		String title = menu.getDisplayName().getUnformattedText();

		if (title.equals("SkyBlock Menu")) {
			EntityPlayerSP p = Minecraft.getMinecraft().thePlayer;
			Container openContainer = p.openContainer;
			for (Slot slot : openContainer.inventorySlots) {
				ItemStack stack = slot.getStack();
				if (stack != null) {
					String displayName = stack.getDisplayName();
					if ("§aAccessory Bag".equals(displayName)) {
						for (String line : ItemUtils.getLore(stack)) {
							if (line.startsWith("§7Magical Power: ")) {
								String rawNumber = line.split("§6")[1].replace(",", "");
								NotEnoughUpdates.INSTANCE.config.getProfileSpecific().magicalPower = Integer.parseInt(rawNumber);
							}
						}
					}
				}
			}
		}
	}

	@SubscribeEvent
	public void onItemTooltipLow(ItemTooltipEvent event) {
		if (!NotEnoughUpdates.INSTANCE.config.tooltipTweaks.powerStoneStats) return;

		ItemStack itemStack = event.itemStack;
		if (itemStack == null) return;
		LinkedList<String> lore = ItemUtils.getLore(itemStack);

		boolean isPowerStone = false;
		for (String line : lore) {
			//§8Power Stone
			if (line.equals("§8Power Stone")) {
				isPowerStone = true;
				break;
			}
		}

		if (!isPowerStone) return;

		//TODO remove before push
		boolean shiftPressed = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT);
		if (!shiftPressed) return;

		int baseMagicalPower = 0;

//		System.out.println("");
//		System.out.println("");
//		System.out.println("");
//		System.out.println("");
//		System.out.println("");
//		System.out.println("");
//		int id = 0;
//		for (String s : lore) {
//			System.out.println("lore " + id + " '" + s + "'");
//			id++;
//		}
//		System.out.println("");
//		id = 0;
//		for (String s : event.toolTip) {
//			System.out.println("event.toolTip " + id + " '" + s + "'");
//			id++;
//		}
//		System.out.println("");

		int index = 0;
		boolean foundValue = false;
		for (String line : new LinkedList<>(lore)) {
			index++;

			//§7At §61,000 Magical Power§7:
			if (line.startsWith("§7At ")) {
				//TODO test debug for neu inventory
				if (line.contains("§k")) return;
				String rawNumber = StringUtils.substringBetween(line, "§7At ", " Magical");
				baseMagicalPower = StringUtils.cleanAndParseInt(rawNumber);
				int magicalPower = NotEnoughUpdates.INSTANCE.config.getProfileSpecific().magicalPower;
				event.toolTip.set(index, "§7At §6" + formatNumber(magicalPower) + " Magical Power§7:");

				foundValue = true;
				continue;
			}

			String cleanLine = StringUtils.cleanColour(line);

			if (foundValue) {
				if (cleanLine.equals("")) {
					foundValue = false;
					continue;
				}

				if (cleanLine.startsWith("+")) {
					System.out.println("");
					System.out.println("line: '" + line + "'");
					System.out.println("cleanLine: '" + cleanLine + "'");
					String rawNumber = StringUtils.substringBetween(line, "+", " ");
					System.out.println("rawNumber: '" + rawNumber + "'");
					int number = StringUtils.cleanAndParseInt(rawNumber.substring(0, rawNumber.length() - 1));
					System.out.println("number: '" + number + "'");

					//§5§o§c+349❁ Strength

					double currentNumber = 0.0 + number;
					double realMagicalPower = 0.0 + NotEnoughUpdates.INSTANCE.config.getProfileSpecific().magicalPower;
					double currentMagicalPower = 0.0 + baseMagicalPower;

					double realNumber = (currentNumber / currentMagicalPower) * realMagicalPower;
					String format = formatNumber((int) realNumber);
					System.out.println("format: '" + format + "'");

					String realFormat = format + rawNumber.substring(rawNumber.length() - 1);
					System.out.println("realFormat: '" + realFormat + "'");
					String finalLine = line.replace(rawNumber, realFormat);
					System.out.println("finalLine: '" + finalLine + "'");

					event.toolTip.set(index, finalLine);
				}
			}
		}
		System.out.println("");
		System.out.println("");
		System.out.println("");
	}

}
