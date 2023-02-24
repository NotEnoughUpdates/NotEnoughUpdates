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

import com.mojang.authlib.GameProfile;
import io.github.moulberry.notenoughupdates.miscfeatures.ItemCustomizeManager;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.layers.LayerCustomHead;
import net.minecraft.client.renderer.tileentity.TileEntitySkullRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LayerCustomHead.class)
public class MixinLayerCustomHead {
	private static String customGlintColour = null;

	@Inject(method = "doRenderLayer", at = @At("HEAD"))
	public void doRenderLayer(
		EntityLivingBase entitylivingbaseIn, float p_177141_2_, float p_177141_3_, float partialTicks,
		float p_177141_5_, float p_177141_6_, float p_177141_7_, float scale, CallbackInfo ci
	) {
		ItemStack stack = entitylivingbaseIn.getCurrentArmor(3);

		ItemCustomizeManager.ItemData data = ItemCustomizeManager.getDataForItem(stack);
		if (data != null && data.overrideEnchantGlint && data.enchantGlintValue) {
			customGlintColour = data.customGlintColour;
		} else {
			customGlintColour = null;
		}
	}

	@Redirect(method = "doRenderLayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityLivingBase;getCurrentArmor(I)Lnet/minecraft/item/ItemStack;"))
	public ItemStack doRenderLayer_getCurrentArmor(EntityLivingBase instance, int i) {
		return ItemCustomizeManager.setHeadArmour(instance, i);
	}

	@Redirect(method = "doRenderLayer",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/tileentity/TileEntitySkullRenderer;renderSkull(FFFLnet/minecraft/util/EnumFacing;FILcom/mojang/authlib/GameProfile;I)V"
		)
	)
	public void renderItem_renderSkull(
		TileEntitySkullRenderer tileEntitySkullRenderer, float p_180543_1_, float p_180543_2_,
		float p_180543_3_, EnumFacing p_180543_4_, float p_180543_5_, int p_180543_6_,
		GameProfile p_180543_7_, int p_180543_8_
	) {
		GL11.glPushMatrix();
		tileEntitySkullRenderer.renderSkull(p_180543_1_, p_180543_2_, p_180543_3_, p_180543_4_, p_180543_5_,
			p_180543_6_, p_180543_7_, p_180543_8_
		);
		GL11.glPopMatrix();

		if (customGlintColour != null) {
			ItemCustomizeManager.renderEffectHook(customGlintColour, (color) -> {
				float red = ((color >> 16) & 0xFF) / 255f;
				float green = ((color >> 8) & 0xFF) / 255f;
				float blue = (color & 0xFF) / 255f;
				float alpha = ((color >> 24) & 0xFF) / 255f;

				GlStateManager.color(red, green, blue, alpha);

				GlStateManager.scale(1 / 8f, 1 / 8f, 1 / 8f);
				GlStateManager.matrixMode(GL11.GL_MODELVIEW);
				GL11.glPushMatrix();
				ItemCustomizeManager.disableTextureBinding = true;
				tileEntitySkullRenderer.renderSkull(p_180543_1_, p_180543_2_, p_180543_3_, p_180543_4_, p_180543_5_,
					p_180543_6_, p_180543_7_, p_180543_8_
				);
				ItemCustomizeManager.disableTextureBinding = false;
				GL11.glPopMatrix();
			});
		}
	}

}
