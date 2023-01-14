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

package io.github.moulberry.notenoughupdates.core;

import io.github.moulberry.notenoughupdates.core.util.lerp.LerpUtils;
import io.github.moulberry.notenoughupdates.core.util.render.RenderUtils;
import io.github.moulberry.notenoughupdates.util.GuiTextures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class GuiElementBoolean extends GuiElement {
	public int x;
	public int y;
	private Supplier<Boolean> value;
	private final int clickRadius;
	private final Consumer<Boolean> toggleCallback;

	private boolean previewValue;
	private int animation = 0;
	private long lastMillis = 0;

	private static final int xSize = 48;
	private static final int ySize = 14;

	public GuiElementBoolean(int x, int y, Supplier<Boolean> value, Consumer<Boolean> toggleCallback) {
		this(x, y, value, 0, toggleCallback);
	}

	public GuiElementBoolean(int x, int y, Supplier<Boolean> value, int clickRadius, Consumer<Boolean> toggleCallback) {
		this.x = x;
		this.y = y;
		this.value = value;
		this.previewValue = value.get();
		this.clickRadius = clickRadius;
		this.toggleCallback = toggleCallback;
		this.lastMillis = System.currentTimeMillis();

		if (previewValue) animation = 36;
	}

	@Override
	public void render() {
		GlStateManager.color(1, 1, 1, 1);
		Minecraft.getMinecraft().getTextureManager().bindTexture(GuiTextures.BAR);
		RenderUtils.drawTexturedRect(x, y, xSize, ySize);

		ResourceLocation buttonLoc = GuiTextures.ON;
		long currentMillis = System.currentTimeMillis();
		long deltaMillis = currentMillis - lastMillis;
		lastMillis = currentMillis;
		boolean passedLimit = false;
		if (previewValue != value.get()) {
			if ((previewValue && animation > 12) ||
				(!previewValue && animation < 24)) {
				passedLimit = true;
			}
		}
		if (previewValue != passedLimit) {
			animation += deltaMillis / 10;
		} else {
			animation -= deltaMillis / 10;
		}
		lastMillis -= deltaMillis % 10;

		if (previewValue == value.get()) {
			animation = Math.max(0, Math.min(36, animation));
		} else if (!passedLimit) {
			if (previewValue) {
				animation = Math.max(0, Math.min(12, animation));
			} else {
				animation = Math.max(24, Math.min(36, animation));
			}
		} else {
			if (previewValue) {
				animation = Math.max(12, animation);
			} else {
				animation = Math.min(24, animation);
			}
		}

		int animation = (int) (LerpUtils.sigmoidZeroOne(this.animation / 36f) * 36);
		if (animation < 3) {
			buttonLoc = GuiTextures.OFF;
		} else if (animation < 13) {
			buttonLoc = GuiTextures.ONE;
		} else if (animation < 23) {
			buttonLoc = GuiTextures.TWO;
		} else if (animation < 33) {
			buttonLoc = GuiTextures.THREE;
		}

		Minecraft.getMinecraft().getTextureManager().bindTexture(buttonLoc);
		RenderUtils.drawTexturedRect(x + animation, y, 12, 14);
	}

	@Override
	public boolean mouseInput(int mouseX, int mouseY) {
		if (mouseX > x - clickRadius && mouseX < x + xSize + clickRadius &&
			mouseY > y - clickRadius && mouseY < y + ySize + clickRadius) {
			if (Mouse.getEventButton() == 0) {
				if (Mouse.getEventButtonState()) {
					previewValue = !value.get();
				} else if (previewValue == !value.get()) {
					toggleCallback.accept(!value.get());
				}
			}
		} else {
			previewValue = value.get();
		}
		return false;
	}

	@Override
	public boolean keyboardInput() {
		return false;
	}
}
