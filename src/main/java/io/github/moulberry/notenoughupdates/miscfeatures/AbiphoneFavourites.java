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
import io.github.moulberry.notenoughupdates.autosubscribe.NEUAutoSubscribe;
import io.github.moulberry.notenoughupdates.core.config.KeybindHelper;
import io.github.moulberry.notenoughupdates.core.util.StringUtils;
import io.github.moulberry.notenoughupdates.core.util.render.RenderUtils;
import io.github.moulberry.notenoughupdates.events.GuiContainerBackgroundDrawnEvent;
import io.github.moulberry.notenoughupdates.events.ReplaceItemEvent;
import io.github.moulberry.notenoughupdates.events.SlotClickEvent;
import io.github.moulberry.notenoughupdates.options.NEUConfig;
import io.github.moulberry.notenoughupdates.util.ItemUtils;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

@NEUAutoSubscribe
public class AbiphoneFavourites {

	private static final AbiphoneFavourites INSTANCE = new AbiphoneFavourites();
	private long lastClick = 0L;

	public static AbiphoneFavourites getInstance() {
		return INSTANCE;
	}

	private final ItemStack ITEM_STACK_FAVOURITE_ONLY = Utils.createItemStack(
		Items.diamond,
		"§6Show only favourite contacts",
		"§7Non favourite contacts are hidden.",
		"§7Only favourite contacts can be called.",
		"§8This is a NEU feature and not made by hypixel.",
		" ",
		"§eClick to show all contacts!"
	);
	private final ItemStack ITEM_STACK_ALL = Utils.createItemStack(
		Items.emerald,
		"§aShow all contacts",
		"§7Favourite contacts are marked §6orange§7.",
		"§7All contacts can be called.",
		"§8This is a NEU feature and not made by hypixel.",
		" ",
		"§eClick to show only favourite contacts!"
	);

	@SubscribeEvent
	public void onItemTooltip(ItemTooltipEvent event) {
		if (isWrongInventory()) return;

		List<String> list = event.toolTip;
		if (list == null) return;
		if (list.isEmpty()) return;

		ItemStack stack = event.itemStack;
		if (!isContact(stack)) return;
		String rawName = stack.getDisplayName();
		String name = StringUtils.cleanColour(rawName);

		if (isAbiphoneShowOnlyFavourites()) {
			if (!getFavouriteContacts().contains(name)) {
				list.clear();
				return;
			}
		}

		if (isAbiphoneShowOnlyFavourites()) {
			list.removeIf(s -> s.contains("§eRight-click to remove contact!"));
			return;
		}

		int index = list.indexOf("§5§o") + 1;
		if (getFavouriteContacts().contains(name)) {
			list.set(0, rawName + " §f- §6Favourite");
			list.add(index, "§eShift-click to remove from the favourites!");
		} else {
			list.add(index, "§eShift-click to add to the favourites!");
		}

		if (KeybindHelper.isKeyPressed(NotEnoughUpdates.INSTANCE.manager.keybindFavourite.getKeyCode())) {
			if (System.currentTimeMillis() > lastClick + 500) {
				toggleFavouriteContact(rawName, name);
				lastClick = System.currentTimeMillis();
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onStackClick(SlotClickEvent event) {
		if (isWrongInventory()) return;

		ItemStack stack = event.slot.getStack();
		if (stack == null || stack.getDisplayName() == null) return;

		if ((stack == ITEM_STACK_FAVOURITE_ONLY || stack == ITEM_STACK_ALL)) {
			if (System.currentTimeMillis() > lastClick + 200) {
				NEUConfig.HiddenProfileSpecific profileSpecific = NotEnoughUpdates.INSTANCE.config.getProfileSpecific();
				if (profileSpecific != null) {
					profileSpecific.abiphoneShowOnlyFavourites =
						!isAbiphoneShowOnlyFavourites();
					lastClick = System.currentTimeMillis();
				}
			}
			event.setCanceled(true);
			return;
		}

		if (!isContact(stack)) return;

		int clickType = event.clickType;
		int clickedButton = event.clickedButton;

		//allows removing the contact
		if (clickType == 0 && clickedButton == 1) {
			if (!isAbiphoneShowOnlyFavourites()) {
				return;
			}
		}
		String rawName = stack.getDisplayName();
		String name = StringUtils.cleanColour(rawName);

		//allows calling
		if (clickType == 0 && clickedButton == 0) {
			if (!isAbiphoneShowOnlyFavourites() || getFavouriteContacts().contains(name)) {
				return;
			}
		}

		//toggle favourite contact
		if (clickType == 1) {
			if (!isAbiphoneShowOnlyFavourites()) {
				toggleFavouriteContact(rawName, name);
			}
		}

		event.setCanceled(true);
	}

	@SubscribeEvent
	public void replaceItem(ReplaceItemEvent event) {
		IChatComponent chatComponent = event.getInventory().getDisplayName();
		if (chatComponent == null || isWrongInventory()) return;
		ItemStack original = event.getOriginal();
		if (original == null) return;
		if (original.getItem() != Item.getItemFromBlock(Blocks.stained_glass_pane)) return;

		if (event.getSlotNumber() > 2 && event.getSlotNumber() < 6) {
			event.replaceWith(isAbiphoneShowOnlyFavourites() ? ITEM_STACK_FAVOURITE_ONLY : ITEM_STACK_ALL);
		}
	}

	private void toggleFavouriteContact(String rawName, String name) {
		if (getFavouriteContacts().contains(name)) {
			getFavouriteContacts().remove(name);
			Utils.addChatMessage("§e[NEU] Removed §r" + rawName + " §efrom your favourite contacts!");
		} else {
			getFavouriteContacts().add(name);
			Utils.addChatMessage("§e[NEU] Added §r" + rawName + " §eto your favourite contacts!");
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

	@SubscribeEvent
	public void onDrawBackground(GuiContainerBackgroundDrawnEvent event) {
		if (isWrongInventory()) return;

		GuiContainer container = event.getContainer();

		for (Slot slot : container.inventorySlots.inventorySlots) {
			if (slot == null) continue;
			ItemStack stack = slot.getStack();
			if (stack == null) continue;

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

	private boolean isWrongInventory() {
		return !NotEnoughUpdates.INSTANCE.hasSkyblockScoreboard()
			|| !NotEnoughUpdates.INSTANCE.config.misc.abiphoneFavourites
			|| Utils.getOpenChestName().equals("Abiphone Shop")
			|| !Utils.getOpenChestName().startsWith("Abiphone ");
	}

	private boolean isContact(ItemStack stack) {
		for (String line : ItemUtils.getLore(stack)) {
			if (line.equals("§eLeft-click to call!") || line.equals("§eClick to call!")) {
				return true;
			}
		}

		return false;
	}

	private List<String> getFavouriteContacts() {
		NEUConfig.HiddenProfileSpecific profileSpecific = NotEnoughUpdates.INSTANCE.config.getProfileSpecific();
		if (profileSpecific != null) {
			return profileSpecific.abiphoneFavouriteContacts;
		}
		return new ArrayList<>();
	}

	private boolean isAbiphoneShowOnlyFavourites() {
		NEUConfig.HiddenProfileSpecific profileSpecific = NotEnoughUpdates.INSTANCE.config.getProfileSpecific();
		if (profileSpecific != null) {
			return profileSpecific.abiphoneShowOnlyFavourites;
		}
		return false;
	}
}
