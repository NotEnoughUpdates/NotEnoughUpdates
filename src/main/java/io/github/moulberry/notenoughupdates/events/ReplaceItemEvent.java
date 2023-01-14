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

package io.github.moulberry.notenoughupdates.events;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public class ReplaceItemEvent extends NEUEvent {

	final ItemStack original;
	final IInventory inventory;
	final int slotNumber;
	ItemStack replaceWith;

	public ReplaceItemEvent(ItemStack original, IInventory inventory, int slotNumber) {
		this.original = original;
		this.inventory = inventory;
		this.slotNumber = slotNumber;
		this.replaceWith = original;
	}

	public ItemStack getOriginal() {
		return original;
	}

	public IInventory getInventory() {
		return inventory;
	}

	public int getSlotNumber() {
		return slotNumber;
	}

	public ItemStack getReplacement() {
		return replaceWith;
	}

	public void replaceWith(ItemStack is) {
		this.replaceWith = is;
	}
}
