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

import io.github.moulberry.notenoughupdates.miscfeatures.ItemCustomizeManager;
import io.github.moulberry.notenoughupdates.miscgui.InventoryStorageSelector;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ItemRenderer.class)
public abstract class MixinItemRenderer {

	@Redirect(method = "updateEquippedItem", at = @At(
		value = "INVOKE",
		target = "Lnet/minecraft/entity/player/InventoryPlayer;getCurrentItem()Lnet/minecraft/item/ItemStack;"
	))
	public ItemStack modifyStackToRender(InventoryPlayer player) {
		if (InventoryStorageSelector.getInstance().isSlotSelected()) {
			return InventoryStorageSelector.getInstance().getHeldItemOverride();
		}
		return player.getCurrentItem();
	}

	@Redirect(method = "renderItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getItem()Lnet/minecraft/item/Item;"))
	public Item renderItem_getItem(ItemStack stack) {
		return ItemCustomizeManager.useCustomItem(stack).getItem();
	}

	@ModifyArg(method = "renderItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/RenderItem;shouldRenderItemIn3D(Lnet/minecraft/item/ItemStack;)Z"))
	public ItemStack renderItem_shouldRenderItemIn3D(ItemStack stack) {
		return ItemCustomizeManager.useCustomItem(stack);
	}

	@ModifyArg(method = "renderItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/RenderItem;renderItemModelForEntity(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/client/renderer/block/model/ItemCameraTransforms$TransformType;)V", ordinal = 0))
	public ItemStack renderItem_renderItemModelForEntity(ItemStack stack) {
		return ItemCustomizeManager.useCustomItem(stack);
	}
}
