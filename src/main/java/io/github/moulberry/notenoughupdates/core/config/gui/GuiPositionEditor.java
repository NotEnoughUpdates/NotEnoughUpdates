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

import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.core.GuiScreenElementWrapper;
import io.github.moulberry.notenoughupdates.core.config.Position;
import io.github.moulberry.notenoughupdates.options.NEUConfigEditor;
import io.github.moulberry.notenoughupdates.overlays.OverlayManager;
import io.github.moulberry.notenoughupdates.overlays.TextOverlay;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public class GuiPositionEditor extends GuiScreen {
	private final ArrayList<Position> positions;
	private final ArrayList<Position> originalPositions;
	private final ArrayList<Integer> elementWidths;
	private final ArrayList<Integer> elementHeights;
	private final ArrayList<Runnable> renderCallback;
	private final Runnable positionChangedCallback;
	private final Runnable closedCallback;
	private LinkedHashMap<TextOverlay, Position> overlayPositions;
	private int grabbedX = 0;
	private int grabbedY = 0;

	private int guiScaleOverride = -1;

	public GuiPositionEditor(
		/*Position position, int elementWidth, int elementHeight,*/
		LinkedHashMap<TextOverlay, Position> overlayPositions,
		Runnable renderCallback,
		Runnable positionChangedCallback,
		Runnable closedCallback
	) {
		int i = 0;
		ArrayList<Position> pos = new ArrayList<>();
		ArrayList<Position> ogPos = new ArrayList<>();
		ArrayList<Runnable> renderCallbac = new ArrayList<>();
		ArrayList<Integer> width = new ArrayList<>();
		ArrayList<Integer> height = new ArrayList<>();
		ArrayList<Integer> grabbedDefaults = new ArrayList<>();
		grabbedDefaults.add(0);
		grabbedDefaults.add(0);
		for (TextOverlay overlay : overlayPositions.keySet()) {
			pos.add(overlayPositions.get(overlay));
			ogPos.add(pos.get(i).clone());
			width.add((int) overlay.getDummySize().x);
			height.add((int) overlay.getDummySize().y);
			renderCallbac.add(() -> {
				overlay.renderDummy();
				OverlayManager.dontRenderOverlay = overlay.getClass();
			});
			i++;
		}
		this.positions = pos;
		this.originalPositions = ogPos;
		this.renderCallback = renderCallbac;
		this.elementWidths = width;
		this.elementHeights = height;
		this.positionChangedCallback = positionChangedCallback;
		this.closedCallback =
			(() -> NotEnoughUpdates.INSTANCE.openGui = new GuiScreenElementWrapper(NEUConfigEditor.editor));
	}

	public GuiPositionEditor withScale(int scale) {
		this.guiScaleOverride = scale;
		return this;
	}

	@Override
	public void onGuiClosed() {
		super.onGuiClosed();
		closedCallback.run();
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		ScaledResolution scaledResolution;
		if (guiScaleOverride >= 0) {
			scaledResolution = Utils.pushGuiScale(guiScaleOverride);
		} else {
			scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
		}

		this.width = scaledResolution.getScaledWidth();
		this.height = scaledResolution.getScaledHeight();
		mouseX = Mouse.getX() * width / Minecraft.getMinecraft().displayWidth;
		mouseY = height - Mouse.getY() * height / Minecraft.getMinecraft().displayHeight - 1;

		drawDefaultBackground();
		for (Position position : positions) {
			int elementHeight = elementHeights.get(positions.indexOf(position));
			int elementWidth = elementWidths.get(positions.indexOf(position));
			if (position.getClicked()) {
				grabbedX += position.moveX(mouseX - grabbedX, elementWidth, scaledResolution);
				grabbedY += position.moveY(mouseY - grabbedY, elementHeight, scaledResolution);
			}

			renderCallback.get(positions.indexOf(position)).run();

			int x = position.getAbsX(scaledResolution, elementWidth);
			int y = position.getAbsY(scaledResolution, elementHeight);

			if (position.isCenterX()) x -= elementWidth / 2;
			if (position.isCenterY()) y -= elementHeight / 2;
			Gui.drawRect(x, y, x + elementWidth, y + elementHeight, 0x80404040);

			if (guiScaleOverride >= 0) {
				Utils.pushGuiScale(-1);
			}

			scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
			Utils.drawStringCentered("Position Editor", Minecraft.getMinecraft().fontRendererObj,
				scaledResolution.getScaledWidth() / 2, 8, true, 0xffffff
			);
			Utils.drawStringCentered("R to Reset - Arrow keys/mouse to move", Minecraft.getMinecraft().fontRendererObj,
				scaledResolution.getScaledWidth() / 2, 18, true, 0xffffff
			);
		}
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);

		if (mouseButton == 0) {
			ScaledResolution scaledResolution;
			if (guiScaleOverride >= 0) {
				scaledResolution = Utils.pushGuiScale(guiScaleOverride);
			} else {
				scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
			}
			mouseX = Mouse.getX() * width / Minecraft.getMinecraft().displayWidth;
			mouseY = height - Mouse.getY() * height / Minecraft.getMinecraft().displayHeight - 1;
			boolean overlayClicked = false;
			for (Position position : positions) {
				int elementHeight = elementHeights.get(positions.indexOf(position));
				int elementWidth = elementWidths.get(positions.indexOf(position));
				int x = position.getAbsX(scaledResolution, elementWidth);
				int y = position.getAbsY(scaledResolution, elementHeight);
				if (position.isCenterX()) x -= elementWidth / 2;
				if (position.isCenterY()) y -= elementHeight / 2;
				if (!position.getClicked()) {
					if (mouseX >= x && mouseY >= y &&
						mouseX <= x + elementWidth && mouseY <= y + elementHeight) {
						position.setClicked(true);
						grabbedX = mouseX;
						grabbedY = mouseY;
					}
				}

				if (guiScaleOverride >= 0) {
					Utils.pushGuiScale(-1);
				}
			}
		}
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		for (Position position : positions) {
			int elementHeight = elementHeights.get(positions.indexOf(position));
			int elementWidth = elementWidths.get(positions.indexOf(position));
			if (keyCode == Keyboard.KEY_R) {
				position.set(originalPositions.get(positions.indexOf(position)));
			} else if (!position.getClicked()) {
				boolean shiftHeld = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
				int dist = shiftHeld ? 10 : 1;
				if (keyCode == Keyboard.KEY_DOWN) {
					position.moveY(dist, elementHeight, new ScaledResolution(Minecraft.getMinecraft()));
				} else if (keyCode == Keyboard.KEY_UP) {
					position.moveY(-dist, elementHeight, new ScaledResolution(Minecraft.getMinecraft()));
				} else if (keyCode == Keyboard.KEY_LEFT) {
					position.moveX(-dist, elementWidth, new ScaledResolution(Minecraft.getMinecraft()));
				} else if (keyCode == Keyboard.KEY_RIGHT) {
					position.moveX(dist, elementWidth, new ScaledResolution(Minecraft.getMinecraft()));
				}
			}
		}
		super.keyTyped(typedChar, keyCode);
	}

	@Override
	protected void mouseReleased(int mouseX, int mouseY, int state) {
		super.mouseReleased(mouseX, mouseY, state);
		for (Position position : positions) {
			position.setClicked(false);
		}
	}

	@Override
	protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
		super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
		for (Position position : positions) {
			int elementHeight = elementHeights.get(positions.indexOf(position));
			int elementWidth = elementWidths.get(positions.indexOf(position));
			if (position.getClicked()) {
				ScaledResolution scaledResolution;
				if (guiScaleOverride >= 0) {
					scaledResolution = Utils.pushGuiScale(guiScaleOverride);
				} else {
					scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
				}
				mouseX = Mouse.getX() * width / Minecraft.getMinecraft().displayWidth;
				mouseY = height - Mouse.getY() * height / Minecraft.getMinecraft().displayHeight - 1;

				grabbedX += position.moveX(mouseX - grabbedX, elementWidth, scaledResolution);
				grabbedY += position.moveY(mouseY - grabbedY, elementHeight, scaledResolution);
				positionChangedCallback.run();

				if (guiScaleOverride >= 0) {
					Utils.pushGuiScale(-1);
				}
			}
		}
	}
}
