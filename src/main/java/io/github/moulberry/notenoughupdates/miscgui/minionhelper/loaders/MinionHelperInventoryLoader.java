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

package io.github.moulberry.notenoughupdates.miscgui.minionhelper.loaders;

import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.core.util.StringUtils;
import io.github.moulberry.notenoughupdates.miscgui.minionhelper.Minion;
import io.github.moulberry.notenoughupdates.miscgui.minionhelper.MinionHelperManager;
import io.github.moulberry.notenoughupdates.miscgui.minionhelper.MinionHelperOverlay;
import io.github.moulberry.notenoughupdates.util.ItemUtils;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.List;

public class MinionHelperInventoryLoader {
	private static MinionHelperInventoryLoader instance = null;
	private final MinionHelperManager manager = MinionHelperManager.getInstance();
	private final List<String> loadedAlready = new ArrayList<>();

	public static MinionHelperInventoryLoader getInstance() {
		if (instance == null) {
			instance = new MinionHelperInventoryLoader();
		}
		return instance;
	}

	int ticks = 0;

	@SubscribeEvent
	public void onTick(TickEvent.ClientTickEvent event) {
		if (!NotEnoughUpdates.INSTANCE.hasSkyblockScoreboard()) return;
		if (!manager.isReadyToUse()) return;
		ticks++;

		if (ticks % 5 != 0) return;

		if (manager.inCraftedMinionsInventory()) {
			checkInv();
		} else {
			loadedAlready.clear();
		}
	}

	private void checkInv() {
		Container openContainer = Minecraft.getMinecraft().thePlayer.openContainer;
		if (openContainer instanceof ContainerChest) {

			Slot firstSlot = openContainer.inventorySlots.get(10);
			boolean shouldLoad = false;
			if (firstSlot != null) {
				if (firstSlot.getHasStack()) {
					ItemStack stack = firstSlot.getStack();
					String displayName = stack.getDisplayName();
					if (!loadedAlready.contains(displayName)) {
						loadedAlready.add(displayName);
						shouldLoad = true;
					}
				}
			}

			if (!shouldLoad) return;

			int crafted = 0;
			for (Slot slot : openContainer.inventorySlots) {
				if (!slot.getHasStack()) continue;
				ItemStack stack = slot.getStack();
				if (stack == null) continue;
				if (slot.slotNumber != slot.getSlotIndex()) continue;

				String displayName = stack.getDisplayName();
				if (!displayName.contains(" Minion")) continue;

				displayName = StringUtils.cleanColour(displayName);
				int index = 0;
				for (String line : ItemUtils.getLore(stack)) {
					index++;
					if (!line.contains("Tier")) {
						continue;
					}
					if (line.contains("§a")) {
						Minion minion = manager.getMinionByName(displayName, index);
						if (!minion.isCrafted()) {
							minion.setCrafted(true);
							crafted++;
						}
					}
				}
			}
			if (crafted > 0) {
				MinionHelperOverlay.getInstance().resetCache();
			}
		}
	}
}
