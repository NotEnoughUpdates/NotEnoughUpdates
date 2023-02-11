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
import net.minecraft.block.Block;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public abstract class MixinItemRenderer {
	@Shadow
	@Final
	private RenderItem itemRenderer;

	@Shadow
	protected abstract boolean isBlockTranslucent(Block blockIn);

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

	@Inject(method = "renderItem", at = @At("HEAD"), cancellable = true)
	public void renderItem(
		EntityLivingBase entityIn,
		ItemStack heldStack,
		ItemCameraTransforms.TransformType transform,
		CallbackInfo ci
	) {
		ci.cancel();
		if (heldStack != null) {
			ItemStack newStack = heldStack.copy();
			newStack.setItem(ItemCustomizeManager.getCustomItem(newStack));
			Item item = newStack.getItem();
			Block block = Block.getBlockFromItem(item);
			GlStateManager.pushMatrix();
			if (this.itemRenderer.shouldRenderItemIn3D(newStack)) {
				GlStateManager.scale(2.0f, 2.0f, 2.0f);
				if (this.isBlockTranslucent(block)) {
					GlStateManager.depthMask(false);
				}
			}
			this.itemRenderer.renderItemModelForEntity(newStack, entityIn, transform);
			if (this.isBlockTranslucent(block)) {
				GlStateManager.depthMask(true);
			}
			GlStateManager.popMatrix();
		}
	}
}
