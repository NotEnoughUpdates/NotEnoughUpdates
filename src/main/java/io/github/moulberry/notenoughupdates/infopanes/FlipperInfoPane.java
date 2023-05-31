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

package io.github.moulberry.notenoughupdates.infopanes;

import io.github.moulberry.notenoughupdates.NEUManager;
import io.github.moulberry.notenoughupdates.NEUOverlay;
import io.github.moulberry.notenoughupdates.itemeditor.GuiElementTextField;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;

import java.awt.*;

import static io.github.moulberry.notenoughupdates.itemeditor.GuiElementTextField.NO_SPACE;
import static io.github.moulberry.notenoughupdates.itemeditor.GuiElementTextField.NUM_ONLY;
import static io.github.moulberry.notenoughupdates.util.GuiTextures.off;
import static io.github.moulberry.notenoughupdates.util.GuiTextures.on;

/**
 * Not currently used
 */
public class FlipperInfoPane extends InfoPane {
	protected String title;
	protected String text;

	GuiElementTextField minPrice = new GuiElementTextField("0", NUM_ONLY | NO_SPACE);
	GuiElementTextField maxPrice = new GuiElementTextField("100000000", NUM_ONLY | NO_SPACE);
	GuiElementTextField priceDiff = new GuiElementTextField("1000000", NUM_ONLY | NO_SPACE);

	public FlipperInfoPane(NEUOverlay overlay, NEUManager manager, String title, String text) {
		super(overlay, manager);
		this.title = title;
		this.text = text;

		minPrice.setSize(60, 16);
		maxPrice.setSize(60, 16);
		priceDiff.setSize(60, 16);
	}

	public void render(
		int width,
		int height,
		Color bg,
		Color fg,
		ScaledResolution scaledresolution,
		int mouseX,
		int mouseY
	) {
		FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;

		int paneWidth = (int) (width / 3 * overlay.getWidthMult());
		int rightSide = (int) (width * overlay.getInfoPaneOffsetFactor());
		int leftSide = rightSide - paneWidth;

		int titleLen = fr.getStringWidth(title);
		fr.drawString(title, (leftSide + rightSide - titleLen) / 2, overlay.getBoxPadding() + 5,
			Color.WHITE.getRGB()
		);

		int y = 0;
		y += renderParagraph(width, height, y, "Bazaar Flips", bg);
		//draw controls
		y += 20;
		y += renderParagraph(width, height, y, "-- Strong Dragon Fragments", bg);
		y += renderParagraph(width, height, y, "-- Strong Dragon Fragments", bg);
		y += renderParagraph(width, height, y, "-- Strong Dragon Fragments", bg);
		y += renderParagraph(width, height, y, "-- Strong Dragon Fragments", bg);

		y += renderParagraph(width, height, y, "AH Flips", bg);
		//min price, max price, price diff, blacklist stackables
		//GuiElementButton stackables = new GuiElementButton("1000000", NUM_ONLY | NO_SPACE);

		y += 10;
		int x = 10;
		fr.drawString("Min Price: ", x, y, Color.WHITE.getRGB());
		minPrice.render(x, y + 10);
		x += 70;
		fr.drawString("Max Price: ", x, y, Color.WHITE.getRGB());
		maxPrice.render(x, y + 10);
		x += 70;
		fr.drawString("Price Diff: ", x, y, Color.WHITE.getRGB());
		priceDiff.render(x, y + 10);
		x += 70;
		fr.drawString("Incl. Stackables: ", x, y, Color.WHITE.getRGB());
		drawButton(x, y, false);

		drawRect(leftSide + overlay.getBoxPadding() - 5, overlay.getBoxPadding() - 5,
			rightSide - overlay.getBoxPadding() + 5, height - overlay.getBoxPadding() + 5, bg.getRGB()
		);
	}

	private void drawButton(int x, int y, boolean enabled) {
		GlStateManager.color(1f, 1f, 1f, 1f);
		Minecraft.getMinecraft().getTextureManager().bindTexture((enabled) ? on : off);
		Utils.drawTexturedRect(x, y, 48, 16);
	}

	public void mouseInput(int width, int height, int mouseX, int mouseY, boolean mouseDown) {}

	public boolean keyboardInput() {
		return false;
	}

	private int renderParagraph(int width, int height, int startY, String text, Color bg) {

		int paneWidth = (int) (width / 3 * overlay.getWidthMult());
		int rightSide = (int) (width * overlay.getInfoPaneOffsetFactor());
		int leftSide = rightSide - paneWidth;

		int yOff = 0;
		for (String line : text.split("\n")) {
			yOff += Utils.renderStringTrimWidth(line, false, leftSide + overlay.getBoxPadding() + 5,
				startY + overlay.getBoxPadding() + 10 + yOff,
				width * 1 / 3 - overlay.getBoxPadding() * 2 - 10, Color.WHITE.getRGB(), -1
			);
			yOff += 16;
		}

		return yOff;
	}
}
