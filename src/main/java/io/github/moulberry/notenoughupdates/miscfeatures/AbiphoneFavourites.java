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
import io.github.moulberry.notenoughupdates.core.util.render.RenderUtils;
import io.github.moulberry.notenoughupdates.events.SlotClickEvent;
import io.github.moulberry.notenoughupdates.util.ItemUtils;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;
import java.util.List;

public class AbiphoneFavourites {

	private static final AbiphoneFavourites INSTANCE = new AbiphoneFavourites();
	private long lastClick = 0L;

	public static AbiphoneFavourites getInstance() {
		return INSTANCE;
	}

	@SubscribeEvent
	public void onItemTooltip(ItemTooltipEvent event) {
		if (isWrongInventory()) return;

		List<String> list = event.toolTip;
		if (list == null) return;
		if (list.isEmpty()) return;

		ItemStack stack = event.itemStack;

		if (isBorder(stack)) {
			list.clear();
			if (!isAbiphoneShowOnlyFavourites()) {
				list.add("§aShow all contacts");
				list.add("§7Favourite contacts are marked §6orange§7.");
				list.add(" ");
				list.add("§eClick to show only favourite contacts!");
			} else {
				list.add("§6Show only favourite contacts");
				list.add("§7Non favourite contacts are hidden.");
				list.add(" ");
				list.add("§eClick to show all contacts!");
			}
			return;
		}

		if (!isContact(stack)) return;
		String rawName = stack.getDisplayName();
		String name = StringUtils.cleanColour(rawName);

		if (isAbiphoneShowOnlyFavourites()) {
			if (!getFavouriteContacts().contains(name)) {
				list.clear();
				return;
			}
		}

		//removes "to remove contact" line
		list.remove(list.size() - 1);

		if (getFavouriteContacts().contains(name)) {
			if (!isAbiphoneShowOnlyFavourites()) {
				list.set(0, rawName + " §f- §6Favourite");
				list.add("§eShift-Click to remove from the favourites!");
			}
		} else {
			list.remove(list.size() - 1);
			list.add("§eShift-Click to add to the favourites!");
			list.set(0, "§7" + StringUtils.cleanColour(list.get(0)));
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onStackClick(SlotClickEvent event) {
		if (isWrongInventory()) return;

		ItemStack stack = event.slot.getStack();
		if (stack == null || stack.getDisplayName() == null) return;

		if (isBorder(stack)) {
			if (System.currentTimeMillis() > lastClick + 200) {
				NotEnoughUpdates.INSTANCE.config.getProfileSpecific().abiphoneShowOnlyFavourites =
					!isAbiphoneShowOnlyFavourites();
				lastClick = System.currentTimeMillis();
			}
			event.setCanceled(true);
			return;
		}

		if (!isContact(stack)) return;

		int clickedButton = event.clickedButton;
		//prevents removing the contact
		if (clickedButton == 1) {
			event.setCanceled(true);
			return;
		}

		String rawName = stack.getDisplayName();
		String name = StringUtils.cleanColour(rawName);
		int clickType = event.clickType;

		//prevents calling non favourite contacts
		if (clickedButton == 0 && (clickType == 0 || clickType == 6)) {
			if (!getFavouriteContacts().contains(name)) {
				event.setCanceled(true);
				return;
			}
		}

		//toggle favourite contact
		if (clickType == 1) {
			if (!isAbiphoneShowOnlyFavourites()) {
				if (getFavouriteContacts().contains(name)) {
					getFavouriteContacts().remove(name);
					Utils.addChatMessage("§e[NEU] Removed §r" + rawName + " §efrom your favourite contacts!");
				} else {
					getFavouriteContacts().add(name);
					Utils.addChatMessage("§e[NEU] Added §r" + rawName + " §eto your favourite contacts!");
				}
			}
			event.setCanceled(true);
		}
	}

	public boolean onRenderStack(ItemStack stack) {
		if (isWrongInventory()) return false;

		if (stack == null || stack.getDisplayName() == null) return false;

		if (!isContact(stack)) return false;

		String rawName = stack.getDisplayName();
		String name = StringUtils.cleanColour(rawName);

		return isAbiphoneShowOnlyFavourites() && !getFavouriteContacts().contains(name);
	}

	public void onDrawBackground(GuiScreen screen) {
		if (isWrongInventory()) return;

		GuiContainer container = (GuiContainer) screen;

		for (Slot slot : container.inventorySlots.inventorySlots) {
			if (slot == null) continue;
			ItemStack stack = slot.getStack();
			if (stack == null) continue;

			if (isBorder(stack)) {
				if (slot.slotNumber > 2 && slot.slotNumber < 6) {
					if (!isAbiphoneShowOnlyFavourites()) {
						RenderUtils.highlightSlot(slot, Color.GREEN);
					} else {
						RenderUtils.highlightSlot(slot, Color.ORANGE);
					}
				}
			}

			if (!isContact(stack)) continue;

			String rawName = stack.getDisplayName();
			String name = StringUtils.cleanColour(rawName);

			if (!isAbiphoneShowOnlyFavourites()) {
				if (getFavouriteContacts().contains(name)) {
					RenderUtils.highlightSlot(slot, Color.ORANGE);
				}
			}
		}
	}

	private static boolean isWrongInventory() {
		if (!NotEnoughUpdates.INSTANCE.hasSkyblockScoreboard()) return true;
		if (!NotEnoughUpdates.INSTANCE.config.misc.abiphoneFavourites) return true;
		if (!Utils.getOpenChestName().startsWith("Abiphone ")) return true;

		return false;
	}

	private static boolean isBorder(ItemStack stack) {
		return stack.getUnlocalizedName().startsWith("tile.thinStainedGlass.");
	}

	private static boolean isContact(ItemStack stack) {
		for (String line : ItemUtils.getLore(stack)) {
			if (line.equals("§eLeft-click to call!")) {
				return true;
			}
		}

		return false;
	}

	private List<String> getFavouriteContacts() {
		return NotEnoughUpdates.INSTANCE.config.getProfileSpecific().abiphoneFavouriteContacts;
	}

	private static boolean isAbiphoneShowOnlyFavourites() {
		return NotEnoughUpdates.INSTANCE.config.getProfileSpecific().abiphoneShowOnlyFavourites;
	}
}
