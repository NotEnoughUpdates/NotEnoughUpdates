/*
 * Copyright (C) 2022-2023 NotEnoughUpdates contributors
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

import io.github.moulberry.notenoughupdates.NEUOverlay;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.core.ChromaColour;
import io.github.moulberry.notenoughupdates.listener.RenderListener;
import io.github.moulberry.notenoughupdates.miscfeatures.ItemCooldowns;
import io.github.moulberry.notenoughupdates.miscfeatures.ItemCustomizeManager;
import io.github.moulberry.notenoughupdates.profileviewer.GuiProfileViewer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({RenderItem.class})
public abstract class MixinRenderItem {
	private static void func_181565_a(
		WorldRenderer w, int x, int y, float width, int height,
		int r, int g, int b, int a
	) {
		w.begin(7, DefaultVertexFormats.POSITION_COLOR);
		w.pos((x + 0), (y + 0), 0.0D)
		 .color(r, g, b, a).endVertex();
		w.pos((x + 0), (y + height), 0.0D)
		 .color(r, g, b, a).endVertex();
		w.pos((x + width), (y + height), 0.0D)
		 .color(r, g, b, a).endVertex();
		w.pos((x + width), (y + 0), 0.0D)
		 .color(r, g, b, a).endVertex();
		Tessellator.getInstance().draw();
	}

	private static String customEnchGlint = null;

	@Redirect(method = "renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/resources/model/IBakedModel;)V",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/item/ItemStack;hasEffect()Z"
		)
	)
	public boolean renderItem_hasEffect(ItemStack stack) {
		ItemCustomizeManager.ItemData data = ItemCustomizeManager.getDataForItem(stack);
		if (data != null) {
			customEnchGlint = data.customGlintColour;
			if (data.overrideEnchantGlint) {
				return data.enchantGlintValue;
			}
		} else {
			customEnchGlint = null;
		}

		return stack.hasEffect();
	}

	@Redirect(method = "renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/resources/model/IBakedModel;)V",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/tileentity/TileEntityItemStackRenderer;renderByItem(Lnet/minecraft/item/ItemStack;)V"
		)
	)
	public void renderItem_renderByItem(TileEntityItemStackRenderer tileEntityItemStackRenderer, ItemStack stack) {
		GL11.glPushMatrix();
		tileEntityItemStackRenderer.renderByItem(stack);
		GL11.glPopMatrix();

		ItemCustomizeManager.ItemData data = ItemCustomizeManager.getDataForItem(stack);
		if (data != null) {
			if (data.overrideEnchantGlint && data.enchantGlintValue) {
				ItemCustomizeManager.renderEffectHook(data.customGlintColour, (color) -> {
					float red = ((color >> 16) & 0xFF) / 255f;
					float green = ((color >> 8) & 0xFF) / 255f;
					float blue = (color & 0xFF) / 255f;
					float alpha = ((color >> 24) & 0xFF) / 255f;

					GlStateManager.color(red, green, blue, alpha);

					GlStateManager.scale(1 / 8f, 1 / 8f, 1 / 8f);
					GlStateManager.matrixMode(GL11.GL_MODELVIEW);
					GL11.glPushMatrix();
					ItemCustomizeManager.disableTextureBinding = true;
					tileEntityItemStackRenderer.renderByItem(stack);
					ItemCustomizeManager.disableTextureBinding = false;
					GL11.glPopMatrix();

				});
			}
		}
	}

	@Redirect(method = "renderQuads",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/item/Item;getColorFromItemStack(Lnet/minecraft/item/ItemStack;I)I"
		)
	)
	public int renderItem_renderByItem(Item item, ItemStack stack, int renderPass) {
		if (renderPass == 0) {
			ItemCustomizeManager.ItemData data = ItemCustomizeManager.getDataForItem(stack);
			if (data != null && data.customLeatherColour != null && ItemCustomizeManager.shouldRenderLeatherColour(stack)) {
				return ChromaColour.specialToChromaRGB(data.customLeatherColour);
			}
		}

		return item.getColorFromItemStack(stack, renderPass);
	}

	@Inject(method = "renderEffect", at = @At("HEAD"), cancellable = true)
	public void renderEffect(IBakedModel model, CallbackInfo ci) {
		if (ItemCustomizeManager.renderEffectHook(customEnchGlint, (color) -> renderModel(model, color))) {
			ci.cancel();
		}
	}

	@Shadow
	abstract void renderModel(IBakedModel model, int color);

	@Inject(method = "renderItemIntoGUI", at = @At("HEAD"))
	public void renderItemHead(ItemStack stack, int x, int y, CallbackInfo ci) {
		if (NotEnoughUpdates.INSTANCE.overlay.searchMode && RenderListener.drawingGuiScreen && NotEnoughUpdates.INSTANCE.isOnSkyblock() && !(Minecraft.getMinecraft().currentScreen instanceof GuiProfileViewer)) {
			boolean matches = false;

			GuiTextField textField = NotEnoughUpdates.INSTANCE.overlay.getTextField();

			if (textField.getText().trim().isEmpty()) {
				matches = true;
			} else if (stack != null) {
				for (String search : textField.getText().split("\\|")) {
					matches |= NotEnoughUpdates.INSTANCE.manager.doesStackMatchSearch(stack, search.trim());
				}
			}
			if (matches) {
				GlStateManager.pushMatrix();
				GlStateManager.translate(0, 0, 100 + Minecraft.getMinecraft().getRenderItem().zLevel);
				GlStateManager.depthMask(false);
				Gui.drawRect(x, y, x + 16, y + 16, NEUOverlay.overlayColourLight);
				GlStateManager.depthMask(true);
				GlStateManager.popMatrix();
			}
		}
	}

	@Inject(method = "renderItemIntoGUI", at = @At("RETURN"))
	public void renderItemReturn(ItemStack stack, int x, int y, CallbackInfo ci) {
		if (stack != null && stack.stackSize != 1) return;
		if (NotEnoughUpdates.INSTANCE.overlay.searchMode && RenderListener.drawingGuiScreen && NotEnoughUpdates.INSTANCE.isOnSkyblock() && !(Minecraft.getMinecraft().currentScreen instanceof GuiProfileViewer)) {
			boolean matches = false;

			GuiTextField textField = NotEnoughUpdates.INSTANCE.overlay.getTextField();

			if (textField.getText().trim().isEmpty()) {
				matches = true;
			} else if (stack != null) {
				for (String search : textField.getText().split("\\|")) {
					matches |= NotEnoughUpdates.INSTANCE.manager.doesStackMatchSearch(stack, search.trim());
				}
			}
			if (!matches) {
				GlStateManager.pushMatrix();
				GlStateManager.translate(0, 0, 110 + Minecraft.getMinecraft().getRenderItem().zLevel);
				Gui.drawRect(x, y, x + 16, y + 16, NEUOverlay.overlayColourDark);
				GlStateManager.popMatrix();
			}
		}
	}

	@Inject(method = "renderItemOverlayIntoGUI", at = @At("RETURN"))
	public void renderItemOverlayIntoGUI(
		FontRenderer fr,
		ItemStack stack,
		int xPosition,
		int yPosition,
		String text,
		CallbackInfo ci
	) {
		if (stack != null && stack.stackSize != 1) {
			if (NotEnoughUpdates.INSTANCE.overlay.searchMode && RenderListener.drawingGuiScreen && NotEnoughUpdates.INSTANCE.isOnSkyblock() && !(Minecraft.getMinecraft().currentScreen instanceof GuiProfileViewer)) {
				boolean matches = false;

				GuiTextField textField = NotEnoughUpdates.INSTANCE.overlay.getTextField();

				if (textField.getText().trim().isEmpty()) {
					matches = true;
				} else {
					for (String search : textField.getText().split("\\|")) {
						matches |= NotEnoughUpdates.INSTANCE.manager.doesStackMatchSearch(stack, search.trim());
					}
				}
				if (!matches) {
					GlStateManager.pushMatrix();
					GlStateManager.translate(0, 0, 110 + Minecraft.getMinecraft().getRenderItem().zLevel);
					GlStateManager.disableDepth();
					Gui.drawRect(xPosition, yPosition, xPosition + 16, yPosition + 16, NEUOverlay.overlayColourDark);
					GlStateManager.enableDepth();
					GlStateManager.popMatrix();
				}
			}
		}

		if (stack == null) return;

		float damageOverride = ItemCooldowns.getDurabilityOverride(stack);

		if (damageOverride >= 0) {
			float barX = 13.0f - damageOverride * 13.0f;
			int col = (int) Math.round(255.0D - damageOverride * 255.0D);
			GlStateManager.disableLighting();
			GlStateManager.disableDepth();
			GlStateManager.disableTexture2D();
			GlStateManager.disableAlpha();
			GlStateManager.disableBlend();

			Tessellator tessellator = Tessellator.getInstance();
			WorldRenderer worldrenderer = tessellator.getWorldRenderer();
			func_181565_a(worldrenderer, xPosition + 2, yPosition + 13, 13, 2, 0, 0, 0, 255);
			func_181565_a(worldrenderer, xPosition + 2, yPosition + 13, 12, 1, (255 - col) / 4, 64, 0, 255);
			func_181565_a(worldrenderer, xPosition + 2, yPosition + 13, barX, 1, 255 - col, col, 0, 255);

			GlStateManager.enableAlpha();
			GlStateManager.enableTexture2D();
			GlStateManager.enableLighting();
			GlStateManager.enableDepth();
		}
	}

	@Redirect(method = "renderItemOverlayIntoGUI", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/Item;showDurabilityBar(Lnet/minecraft/item/ItemStack;)Z"))
	public boolean renderItemOverlayIntoGUI_showDurabilityBar(
		Item instance, ItemStack stack
	) {
		if (ItemCustomizeManager.hasCustomItem(stack)) return false;
		return stack.getItem().showDurabilityBar(stack);
	}
}
