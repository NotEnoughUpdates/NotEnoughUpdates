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

import java.awt.*;

public class GuiElementButton extends GuiElementText {
	private final Runnable callback;

	public GuiElementButton(String text, int colour, Runnable callback) {
		super(text, colour);
		this.callback = callback;
	}

	@Override
	public int getHeight() {
		return super.getHeight() + 5;
	}

	@Override
	public int getWidth() {
		return super.getWidth() + 10;
	}

	@Override
	public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		callback.run();
	}

	@Override
	public void render(int x, int y) {
		drawRect(x, y, x + getWidth(), y + super.getHeight(), Color.WHITE.getRGB());
		drawRect(x + 1, y + 1, x + getWidth() - 1, y + super.getHeight() - 1, Color.BLACK.getRGB());
		super.render(x + 5, y - 1);
	}
}
