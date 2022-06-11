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

package io.github.moulberry.notenoughupdates.itemeditor;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

public class GuiElementText extends GuiElement {
	protected String text;
	private final int colour;

	public GuiElementText(String text, int colour) {
		this.text = text;
		this.colour = colour;
	}

	@Override
	public int getHeight() {
		return 18;
	}

	@Override
	public int getWidth() {
		FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
		return fr.getStringWidth(text);
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	@Override
	public void render(int x, int y) {
		FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;

		fr.drawString(text, x, y + 6, colour);
	}
}
