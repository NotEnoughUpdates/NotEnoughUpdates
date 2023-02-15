/*
 * Copyright (C) 2023 NotEnoughUpdates contributors
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

import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.TooltipTextScrolling;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraftforge.fml.client.config.GuiUtils;
import org.lwjgl.input.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = GuiUtils.class, remap = false)
public class MixinGuiUtils {
	@Inject(method = "drawHoveringText", at = @At("HEAD"), cancellable = true)
	private static void drawHoveringText_head(
		List<String> textLines, int mouseX, int mouseY, int screenWidth, int screenHeight, int maxTextWidth, FontRenderer font, CallbackInfo ci) {
		if (NotEnoughUpdates.INSTANCE.config.tooltipTweaks.customTooltips) {
			Utils.drawHoveringText(textLines, mouseX, mouseY, screenWidth, screenHeight, maxTextWidth, font);
			ci.cancel();
		} else if (NotEnoughUpdates.INSTANCE.config.tooltipTweaks.guiScale != 0) {
			Utils.pushGuiScale(NotEnoughUpdates.INSTANCE.config.tooltipTweaks.guiScale);
		}
	}

	@ModifyVariable(method = "drawHoveringText", at = @At(value = "HEAD"), ordinal = 0, argsOnly = true)
	private static int drawHoveringText_modifyMouseX(int mouseX) {
		return Mouse.getX() * Utils.pushGuiScale(NotEnoughUpdates.INSTANCE.config.tooltipTweaks.guiScale).getScaledWidth() / Minecraft.getMinecraft().displayWidth;
	}

	@ModifyVariable(method = "drawHoveringText", at = @At(value = "HEAD"), ordinal = 1, argsOnly = true)
	private static int drawHoveringText_modifyMouseY(int mouseY) {
		return Utils.pushGuiScale(NotEnoughUpdates.INSTANCE.config.tooltipTweaks.guiScale).getScaledHeight() -
			Mouse.getY() * Utils.pushGuiScale(NotEnoughUpdates.INSTANCE.config.tooltipTweaks.guiScale).getScaledHeight() / Minecraft.getMinecraft().displayHeight;
	}


	@ModifyVariable(method = "drawHoveringText", at = @At(value = "HEAD"), ordinal = 2, argsOnly = true)
	private static int drawHoveringText_modifyWidth(int width) {
		return Utils.pushGuiScale(NotEnoughUpdates.INSTANCE.config.tooltipTweaks.guiScale).getScaledWidth();
	}

	@ModifyVariable(method = "drawHoveringText", at = @At(value = "HEAD"), ordinal = 3, argsOnly = true)
	private static int drawHoveringText_modifyHeight(int height) {
		return Utils.pushGuiScale(NotEnoughUpdates.INSTANCE.config.tooltipTweaks.guiScale).getScaledHeight();
	}

	@Inject(method = "drawHoveringText", at = @At("TAIL"))
	private static void drawHoveringText_tail(
		List<String> textLines, int mouseX, int mouseY, int screenWidth, int screenHeight, int maxTextWidth, FontRenderer font, CallbackInfo ci) {
		Utils.resetGuiScale();
	}
	@ModifyVariable(at = @At("HEAD"), method = "drawHoveringText")
	private static List<String> onDrawHoveringText(
		List<String> textLines
	) {
		return TooltipTextScrolling.handleTextLineRendering(textLines);
	}
}
