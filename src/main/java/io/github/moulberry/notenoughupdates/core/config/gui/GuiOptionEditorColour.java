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

import io.github.moulberry.notenoughupdates.core.ChromaColour;
import io.github.moulberry.notenoughupdates.core.GuiElementColour;
import io.github.moulberry.notenoughupdates.core.config.struct.ConfigProcessor;
import io.github.moulberry.notenoughupdates.core.util.render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.input.Mouse;

import static io.github.moulberry.notenoughupdates.util.GuiTextures.button_white;

public class GuiOptionEditorColour extends GuiOptionEditor {
	private GuiElementColour colourElement = null;

	public GuiOptionEditorColour(ConfigProcessor.ProcessedOption option) {
		super(option);
	}

	public String getChromaString() {
		return (String) option.get();
	}

	@Override
	public void render(int x, int y, int width) {
		super.render(x, y, width);
		int height = getHeight();

		int argb = ChromaColour.specialToChromaRGB(getChromaString());
		int r = (argb >> 16) & 0xFF;
		int g = (argb >> 8) & 0xFF;
		int b = argb & 0xFF;
		GlStateManager.color(r / 255f, g / 255f, b / 255f, 1);
		Minecraft.getMinecraft().getTextureManager().bindTexture(button_white);
		RenderUtils.drawTexturedRect(x + width / 6 - 24, y + height - 7 - 14, 48, 16);
	}

	@Override
	public void renderOverlay(int x, int y, int width) {
		if (colourElement != null) {
			colourElement.render();
		}
	}

	@Override
	public boolean mouseInputOverlay(int x, int y, int width, int mouseX, int mouseY) {
		return colourElement != null && colourElement.mouseInput(mouseX, mouseY);
	}

	@Override
	public boolean mouseInput(int x, int y, int width, int mouseX, int mouseY) {
		int height = getHeight();

		if (Mouse.getEventButtonState() && Mouse.getEventButton() == 0 &&
			mouseX > x + width / 6 - 24 && mouseX < x + width / 6 + 24 &&
			mouseY > y + height - 7 - 14 && mouseY < y + height - 7 + 2) {
			colourElement = new GuiElementColour(mouseX, mouseY, () -> (String) option.get(),
				option::set, () -> colourElement = null);
		}

		return false;
	}

	@Override
	public boolean keyboardInput() {
		return colourElement != null && colourElement.keyboardInput();
	}
}
