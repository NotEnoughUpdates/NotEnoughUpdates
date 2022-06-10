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

import io.github.moulberry.notenoughupdates.core.GuiElementTextField;
import io.github.moulberry.notenoughupdates.core.config.struct.ConfigProcessor;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

public class GuiOptionEditorText extends GuiOptionEditor {
	private final GuiElementTextField textField;

	public GuiOptionEditorText(ConfigProcessor.ProcessedOption option) {
		super(option);

		textField = new GuiElementTextField((String) option.get(), 0);
	}

	@Override
	public void render(int x, int y, int width) {
		super.render(x, y, width);
		int height = getHeight();

		int fullWidth = Math.min(width / 3 - 10, 80);

		int textFieldX = x + width / 6 - fullWidth / 2;
		if (textField.getFocus()) {
			fullWidth = Math.max(
				fullWidth,
				Minecraft.getMinecraft().fontRendererObj.getStringWidth(textField.getText()) + 10
			);
		}

		textField.setSize(fullWidth, 16);

		textField.render(textFieldX, y + height - 7 - 14);
	}

	@Override
	public boolean mouseInput(int x, int y, int width, int mouseX, int mouseY) {
		int height = getHeight();

		int fullWidth = Math.min(width / 3 - 10, 80);

		int textFieldX = x + width / 6 - fullWidth / 2;

		if (textField.getFocus()) {
			fullWidth = Math.max(
				fullWidth,
				Minecraft.getMinecraft().fontRendererObj.getStringWidth(textField.getText()) + 10
			);
		}

		int textFieldY = y + height - 7 - 14;
		textField.setSize(fullWidth, 16);

		if (Mouse.getEventButtonState() && (Mouse.getEventButton() == 0 || Mouse.getEventButton() == 1)) {
			if (mouseX > textFieldX && mouseX < textFieldX + fullWidth &&
				mouseY > textFieldY && mouseY < textFieldY + 16) {
				textField.mouseClicked(mouseX, mouseY, Mouse.getEventButton());
				return true;
			}
			textField.unfocus();
		}

		return false;
	}

	@Override
	public boolean keyboardInput() {
		if (Keyboard.getEventKeyState() && textField.getFocus()) {
			textField.keyTyped(Keyboard.getEventCharacter(), Keyboard.getEventKey());

			try {
				textField.setCustomBorderColour(0xffffffff);
				option.set(textField.getText());
			} catch (Exception e) {
				textField.setCustomBorderColour(0xffff0000);
			}

			return true;
		}
		return false;
	}
}
