/*
 * Copyright (C) 2024 NotEnoughUpdates contributors
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

package io.github.moulberry.notenoughupdates.profileviewer;

import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.autosubscribe.NEUAutoSubscribe;
import io.github.moulberry.notenoughupdates.events.ReplaceItemEvent;
import io.github.moulberry.notenoughupdates.events.SlotClickEvent;
import io.github.moulberry.notenoughupdates.miscfeatures.BetterContainers;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@NEUAutoSubscribe
public class ProfileViewerButton {

	private static final ItemStack profileViewerStack = Utils.createItemStack(
		Item.getItemFromBlock(Blocks.command_block),
		EnumChatFormatting.GREEN + "Profile Viewer",
		EnumChatFormatting.YELLOW + "Click to open NEU profile viewer!"
	);


	String username = "";
	int replaceSlot = -1;

	@SubscribeEvent
	public void onSlotClick(SlotClickEvent event) {
		if (!Utils.getOpenChestName().contains(" Profile") || event.guiContainer.inventorySlots.inventorySlots.size() < 54) {
			username = "";
			replaceSlot = -1;
			return;
		}
		if (!username.isEmpty() && event.slot.slotNumber == replaceSlot &&
			isReplacedStack(event.slot.getStack())) {
			Utils.playPressSound();
			event.setCanceled(true);
			NotEnoughUpdates.profileViewer.loadPlayerByName(username, profile -> {
				if (profile == null) {
					Utils.addChatMessage(EnumChatFormatting.RED + "Invalid player name. Maybe the API is down?");
				} else {
					profile.resetCache();
					ProfileViewerUtils.saveSearch(username);
					NotEnoughUpdates.INSTANCE.openGui = new GuiProfileViewer(profile);
				}
			});
		}
		//username = "";
	}

	@SubscribeEvent
	public void itemReplace(ReplaceItemEvent event) {
		if (!Utils.getOpenChestName().contains(" Profile")) {
			username = "";
			replaceSlot = -1;
			return;
		}
		if (replaceSlot == event.getSlotNumber()) {
			event.replaceWith(profileViewerStack);
		} else if (!username.isEmpty() && replaceSlot == -1 && event.getSlotNumber() > 9 &&
			(event.getSlotNumber() % 9 == 6 || event.getSlotNumber() % 9 == 7) &&
			BetterContainers.isBlankStack(-1, event.getOriginal())) {
			event.replaceWith(profileViewerStack);
			replaceSlot = event.getSlotNumber();
		} else if (event.getSlotNumber() == 22) {
			ItemStack stack = event.getOriginal();
			if (stack != null && stack.getTagCompound() != null) {
				NBTTagCompound tag = stack.getTagCompound();
				String tagName = tag.getCompoundTag("SkullOwner").getString("Name");
				String displayName = Utils.cleanColour(stack.getDisplayName());
				if (displayName.length() - tagName.length() >= 0 && tagName.equals(displayName.substring(
					displayName.length() - tagName.length()))) {
					username = tagName;
					return;
				}
			}
			username = "";
		}
	}

	private static boolean isReplacedStack(ItemStack stack) {
		return stack != null && stack.getItem() == Item.getItemFromBlock(Blocks.command_block);
	}
}
