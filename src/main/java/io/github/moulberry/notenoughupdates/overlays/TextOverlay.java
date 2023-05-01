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

package io.github.moulberry.notenoughupdates.overlays;

import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.core.config.Position;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.List;
import java.util.function.Supplier;

public abstract class TextOverlay {
	private final Position position;
	protected Supplier<TextOverlayStyle> styleSupplier;
	public int overlayWidth = -1;
	public int overlayHeight = -1;
	public List<String> overlayStrings = null;
	private final Supplier<List<String>> dummyStrings;

	public boolean shouldUpdateFrequent = false;

	private static final int PADDING_X = 5;
	private static final int PADDING_Y = 5;

	public TextOverlay(Position position, Supplier<List<String>> dummyStrings, Supplier<TextOverlayStyle> styleSupplier) {
		this.position = position;
		this.styleSupplier = styleSupplier;
		if (dummyStrings == null) {
			this.dummyStrings = () -> null;
		} else {
			this.dummyStrings = dummyStrings;
		}
	}

	public Vector2f getDummySize() {
		List<String> dummyStrings = this.dummyStrings.get();

		if (dummyStrings != null) {
			return getSize(dummyStrings);
		}
		return new Vector2f(100, 50);
	}

	public boolean isEnabled() {
		return true;
	}

	public void tick() {
		update();
	}

	public void updateFrequent() {
	}

	public abstract void update();

	public void renderDummy() {
		List<String> dummyStrings = this.dummyStrings.get();
		render(dummyStrings, true);
	}

	public void render() {
		if (shouldUpdateFrequent) {
			updateFrequent();
			shouldUpdateFrequent = false;
		}
		render(overlayStrings, false);
	}

	protected Vector2f getSize(List<String> strings) {
		int overlayHeight = 0;
		int overlayWidth = 0;
		for (String s : strings) {
			if (s == null) {
				overlayHeight += 3;
				continue;
			}
			for (String s2 : s.split("\n")) {
				int sWidth = Minecraft.getMinecraft().fontRendererObj.getStringWidth(s2);
				if (sWidth > overlayWidth) {
					overlayWidth = sWidth;
				}
				overlayHeight += 10;
			}
		}
		overlayHeight -= 2;

		int paddingX = 0;
		int paddingY = 0;
		if (styleSupplier.get() == TextOverlayStyle.BACKGROUND) {
			paddingX = PADDING_X;
			paddingY = PADDING_Y;
		}
		return new Vector2f(overlayWidth + paddingX * 2, overlayHeight + paddingY * 2);
	}

	protected Vector2f getTextOffset() {
		return new Vector2f();
	}

	protected Vector2f getPosition(int overlayWidth, int overlayHeight, boolean scaled) {
		GlStateManager.pushMatrix();
		ScaledResolution scaledResolution;
		if (!scaled) scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
		else scaledResolution = Utils.pushGuiScale(NotEnoughUpdates.INSTANCE.config.locationedit.guiScale);

		int x = position.getAbsX(scaledResolution, overlayWidth);
		int y = position.getAbsY(scaledResolution, overlayHeight);
		GlStateManager.popMatrix();
		return new Vector2f(x, y);
	}

	public Position getPosition() {
		return position;
	}

	protected void renderLine(String line, Vector2f position, boolean dummy) {
	}

	private void render(List<String> strings, boolean dummy) {
		if (strings == null) return;

		Vector2f size = getSize(strings);
		overlayHeight = (int) size.y;
		overlayWidth = (int) size.x;

		Vector2f position = getPosition(overlayWidth, overlayHeight, !dummy);
		int x = (int) position.x;
		int y = (int) position.y;

		TextOverlayStyle style = styleSupplier.get();
		if (style == TextOverlayStyle.BACKGROUND) Gui.drawRect(x, y, x + overlayWidth, y + overlayHeight, 0x80000000);

		GlStateManager.enableBlend();
		GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlStateManager.tryBlendFuncSeparate(
			GL11.GL_SRC_ALPHA,
			GL11.GL_ONE_MINUS_SRC_ALPHA,
			GL11.GL_ONE,
			GL11.GL_ONE_MINUS_SRC_ALPHA
		);

		int paddingX = 0;
		int paddingY = 0;
		if (styleSupplier.get() == TextOverlayStyle.BACKGROUND) {
			paddingX = PADDING_X;
			paddingY = PADDING_Y;
		}

		Vector2f textOffset = getTextOffset();
		paddingX += (int) textOffset.x;
		paddingY += (int) textOffset.y;

		int yOff = 0;
		for (String s : strings) {
			if (s == null) {
				yOff += 3;
			} else {
				for (String s2 : s.split("\n")) {
					Vector2f pos = new Vector2f(x + paddingX, y + paddingY + yOff);
					renderLine(s2, pos, dummy);

					int xPad = (int) pos.x;
					int yPad = (int) pos.y;

					if (style == TextOverlayStyle.FULL_SHADOW) {
						String clean = Utils.cleanColourNotModifiers(s2);
						for (int xO = -2; xO <= 2; xO++) {
							for (int yO = -2; yO <= 2; yO++) {
								if (Math.abs(xO) != Math.abs(yO)) {
									Minecraft.getMinecraft().fontRendererObj.drawString(clean,
										xPad + xO / 2f, yPad + yO / 2f,
										new Color(0, 0, 0, 200 / Math.max(Math.abs(xO), Math.abs(yO))).getRGB(), false
									);
								}
							}
						}
					}
					Minecraft.getMinecraft().fontRendererObj.drawString(s2,
						xPad, yPad, 0xffffff, style == TextOverlayStyle.MC_SHADOW
					);

					yOff += 10;
				}
			}
		}
	}
}
