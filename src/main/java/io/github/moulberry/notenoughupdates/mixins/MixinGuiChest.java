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

import io.github.moulberry.notenoughupdates.miscfeatures.BetterContainers;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin({GuiChest.class})
public class MixinGuiChest {
	private static final String TARGET = "Lnet/minecraft/client/renderer/texture/TextureManager;" +
		"bindTexture(Lnet/minecraft/util/ResourceLocation;)V";

	@Redirect(method = "drawGuiContainerBackgroundLayer", at = @At(value = "INVOKE", target = TARGET))
	public void drawGuiContainerBackgroundLayer_bindTexture(TextureManager textureManager, ResourceLocation location) {
		BetterContainers.bindHook(textureManager, location);
	}

	private static final String TARGET_DRAWSTRING =
		"Lnet/minecraft/client/gui/FontRenderer;drawString(Ljava/lang/String;III)I";

	@Redirect(method = "drawGuiContainerForegroundLayer", at = @At(value = "INVOKE", target = TARGET_DRAWSTRING))
	public int drawGuiContainerForegroundLayer_drawString(
		FontRenderer fontRenderer,
		String text,
		int x,
		int y,
		int color
	) {
		return fontRenderer.drawString(
			text,
			x,
			y,
			BetterContainers.isOverriding() ? BetterContainers.getTextColour() : color
		);
	}

	private static final String TARGET_SBADRAWSTRING = "Lcodes/biscuit/skyblockaddons/asm/hooks/GuiChestHook;" +
		"drawString(Lnet/minecraft/client/gui/FontRenderer;Ljava/lang/String;III)I";

	@Redirect(method = "drawGuiContainerForegroundLayer", at = @At(value = "INVOKE", target = TARGET_SBADRAWSTRING, remap = false), expect = 0)
	public int drawGuiContainerForegroundLayer_SBA_drawString(
		FontRenderer fontRenderer,
		String text,
		int x,
		int y,
		int color
	) {
		try {
			return (int) Class.forName("codes.biscuit.skyblockaddons.asm.hooks.GuiChestHook")
												.getDeclaredMethod(
													"drawString",
													FontRenderer.class,
													String.class,
													int.class,
													int.class,
													int.class
												)
												.invoke(
													null,
													fontRenderer,
													text,
													x,
													y,
													BetterContainers.isOverriding() ? BetterContainers.getTextColour() : color
												);
		} catch (Exception ignored) {
		}
		return fontRenderer.drawString(
			text,
			x,
			y,
			BetterContainers.isOverriding() ? BetterContainers.getTextColour() : color
		);
	}
}
