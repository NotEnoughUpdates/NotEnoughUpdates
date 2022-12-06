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

package io.github.moulberry.notenoughupdates.core.config.gui;

import io.github.moulberry.notenoughupdates.core.config.KeybindHelper;
import io.github.moulberry.notenoughupdates.core.config.struct.ConfigProcessor;
import io.github.moulberry.notenoughupdates.core.util.render.RenderUtils;
import io.github.moulberry.notenoughupdates.core.util.render.TextRenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import static io.github.moulberry.notenoughupdates.util.GuiTextures.button_tex;

public class GuiOptionEditorKeybind extends GuiOptionEditor {
	private static final ResourceLocation RESET = new ResourceLocation("notenoughupdates:itemcustomize/reset.png");

	private final int defaultKeyCode;
	private boolean editingKeycode;

	public GuiOptionEditorKeybind(ConfigProcessor.ProcessedOption option, int defaultKeyCode) {
		super(option);
		this.defaultKeyCode = defaultKeyCode;
	}

	public int getKeyCode() {
		return (int) option.get();
	}

	@Override
	public void render(int x, int y, int width) {
		super.render(x, y, width);

		int height = getHeight();

		GlStateManager.color(1, 1, 1, 1);
		Minecraft.getMinecraft().getTextureManager().bindTexture(button_tex);
		RenderUtils.drawTexturedRect(x + width / 6 - 24, y + height - 7 - 14, 48, 16);

		String keyName = KeybindHelper.getKeyName(getKeyCode());
		String text = editingKeycode ? "> " + keyName + " <" : keyName;
		TextRenderUtils.drawStringCenteredScaledMaxWidth(text,
			Minecraft.getMinecraft().fontRendererObj,
			x + width / 6, y + height - 7 - 6,
			false, 40, 0xFF303030
		);

		Minecraft.getMinecraft().getTextureManager().bindTexture(RESET);
		GlStateManager.color(1, 1, 1, 1);
		RenderUtils.drawTexturedRect(x + width / 6 - 24 + 48 + 3, y + height - 7 - 14 + 3, 10, 11, GL11.GL_NEAREST);
	}

	@Override
	public boolean mouseInput(int x, int y, int width, int mouseX, int mouseY) {
		if (Mouse.getEventButtonState() && Mouse.getEventButton() != -1 && editingKeycode) {
			editingKeycode = false;
			option.set(Mouse.getEventButton() - 100);
			return true;
		}

		if (Mouse.getEventButtonState() && Mouse.getEventButton() == 0) {
			int height = getHeight();
			if (mouseX > x + width / 6 - 24 && mouseX < x + width / 6 + 24 &&
				mouseY > y + height - 7 - 14 && mouseY < y + height - 7 + 2) {
				editingKeycode = true;
				return true;
			}
			if (mouseX > x + width / 6 - 24 + 48 + 3 && mouseX < x + width / 6 - 24 + 48 + 13 &&
				mouseY > y + height - 7 - 14 + 3 && mouseY < y + height - 7 - 14 + 3 + 11) {
				option.set(defaultKeyCode);
				return true;
			}
		}

		return false;
	}

	@Override
	public boolean keyboardInput() {
		if (editingKeycode) {
			editingKeycode = false;
			int keyCode = -1;
			if (Keyboard.getEventKey() == Keyboard.KEY_ESCAPE) {
				keyCode = 0;
			} else if (Keyboard.getEventKey() != 0) {
				keyCode = Keyboard.getEventKey();
			}
			if (keyCode > 256) keyCode = 0;
			if (keyCode != -1)
				option.set(keyCode);
			return true;
		}
		return false;
	}
}
