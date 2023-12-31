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

import lombok.Getter;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public class ReplaceItemEvent extends NEUEvent {

	@Getter
	final ItemStack original;
	@Getter
	final IInventory inventory;
	@Getter
	final int slotNumber;
	ItemStack replaceWith;

	public ReplaceItemEvent(ItemStack original, IInventory inventory, int slotNumber) {
		this.original = original;
		this.inventory = inventory;
		this.slotNumber = slotNumber;
		this.replaceWith = original;
	}

	public ItemStack getReplacement() {
		return replaceWith;
	}

	public void replaceWith(ItemStack is) {
		this.replaceWith = is;
	}
}
