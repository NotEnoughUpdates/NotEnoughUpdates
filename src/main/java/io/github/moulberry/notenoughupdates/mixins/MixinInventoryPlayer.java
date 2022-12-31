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

package io.github.moulberry.notenoughupdates.mixins;

import io.github.moulberry.notenoughupdates.events.ReplaceItemEvent;
import io.github.moulberry.notenoughupdates.miscfeatures.SlotLocking;
import io.github.moulberry.notenoughupdates.miscgui.InventoryStorageSelector;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(InventoryPlayer.class)
public class MixinInventoryPlayer {
	@Inject(method = "changeCurrentItem", at = @At("RETURN"))
	public void changeCurrentItemReturn(int direction, CallbackInfo ci) {
		InventoryPlayer $this = (InventoryPlayer) (Object) this;

		$this.currentItem = InventoryStorageSelector.getInstance().onScroll(direction, $this.currentItem);
	}

	@Inject(method = "changeCurrentItem", at = @At("HEAD"))
	public void changeCurrentItemHead(int direction, CallbackInfo ci) {
		InventoryPlayer $this = (InventoryPlayer) (Object) this;

		SlotLocking.getInstance().changedSlot($this.currentItem);
	}

	@Shadow public ItemStack[] mainInventory;
	@Shadow public ItemStack[] armorInventory;

	@Inject(method = "getStackInSlot", at = @At("HEAD"), cancellable = true)
	public void on(int index, CallbackInfoReturnable<ItemStack> cir) {
		ItemStack itemStack =
			index < mainInventory.length
				? this.mainInventory[index]
				: this.armorInventory[index - mainInventory.length];
		ReplaceItemEvent replaceItemEventInventory = new ReplaceItemEvent(
			itemStack,
			((InventoryPlayer) (Object) this),
			index
		);
		replaceItemEventInventory.post();
		if (replaceItemEventInventory.getReplacement() != replaceItemEventInventory.getOriginal()) {
			cir.setReturnValue(replaceItemEventInventory.getReplacement());
		}
	}
}
