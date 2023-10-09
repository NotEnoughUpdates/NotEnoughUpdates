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

package io.github.moulberry.notenoughupdates.core.config;

import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.core.config.Position;
import io.github.moulberry.notenoughupdates.overlays.OverlayManager;
import io.github.moulberry.notenoughupdates.overlays.TextOverlay;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.function.Supplier;

public class GuiPositionEditor extends GuiScreen {
	private final ArrayList<Position> positions;
	private final ArrayList<Position> originalPositions;
	private final ArrayList<Integer> elementWidths;
	private final ArrayList<Integer> elementHeights;
	private final ArrayList<Supplier<Boolean>> shouldRenderSupplier;
	private final Runnable positionChangedCallback;
	private final Runnable closedCallback;
	private int grabbedX = 0;
	private int grabbedY = 0;
	private int clickedPos = -1;
	private int oldGuiScale = -1;
	public static boolean renderDrill = false;


	public GuiPositionEditor(
		LinkedHashMap<TextOverlay, Position> overlayPositions,
		Runnable positionChangedCallback,
		Runnable closedCallback
	) {
		shouldRenderSupplier = new ArrayList<>();
		ArrayList<Position> pos = new ArrayList<>();
		ArrayList<Position> ogPos = new ArrayList<>();
		ArrayList<Integer> width = new ArrayList<>();
		ArrayList<Integer> height = new ArrayList<>();
		for (int i = 0; i < overlayPositions.size(); i++) {
			TextOverlay overlay = new ArrayList<>(overlayPositions.keySet()).get(i);
			pos.add(overlayPositions.get(overlay));
			ogPos.add(pos.get(i).clone());
			width.add((int) overlay.getDummySize().x);
			height.add((int) overlay.getDummySize().y);
			shouldRenderSupplier.add(() -> {
				if (overlay.isEnabled()) {
					overlay.renderDummy();
					OverlayManager.dontRenderOverlay.add(overlay.getClass());
					return true;
				}
				return false;
			});
		}


		this.positions = pos;
		this.originalPositions = ogPos;
		this.elementWidths = width;
		this.elementHeights = height;
		this.positionChangedCallback = positionChangedCallback;
		this.closedCallback = closedCallback;
		int newGuiScale = NotEnoughUpdates.INSTANCE.config.locationedit.guiScale;
		if (newGuiScale != 0) {
			if (Minecraft.getMinecraft().gameSettings.guiScale != 0) {
				this.oldGuiScale = Minecraft.getMinecraft().gameSettings.guiScale;
			} else {
				this.oldGuiScale = 4;
			}
			if (newGuiScale == 4) Minecraft.getMinecraft().gameSettings.guiScale = 0;
			else Minecraft.getMinecraft().gameSettings.guiScale = NotEnoughUpdates.INSTANCE.config.locationedit.guiScale;
		}
	}

	@Override
	public void onGuiClosed() {
		super.onGuiClosed();
		closedCallback.run();
		renderDrill = false;
		clickedPos = -1;
		if (this.oldGuiScale != -1) Minecraft.getMinecraft().gameSettings.guiScale = this.oldGuiScale;
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		GlStateManager.pushMatrix();
		ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());

		this.width = scaledResolution.getScaledWidth();
		this.height = scaledResolution.getScaledHeight();
		mouseX = Mouse.getX() * width / Minecraft.getMinecraft().displayWidth;
		mouseY = height - Mouse.getY() * height / Minecraft.getMinecraft().displayHeight - 1;

		drawDefaultBackground();
		renderDrill = true;
		for (Position position : positions) {
			if (!shouldRenderSupplier.get(positions.indexOf(position)).get()) {
				continue;
			}
			int elementHeight = elementHeights.get(positions.indexOf(position));
			int elementWidth = elementWidths.get(positions.indexOf(position));
			if (position.getClicked()) {
				grabbedX += position.moveX(mouseX - grabbedX, elementWidth, scaledResolution);
				grabbedY += position.moveY(mouseY - grabbedY, elementHeight, scaledResolution);
			}

			int x = position.getAbsX(scaledResolution, elementWidth);
			int y = position.getAbsY(scaledResolution, elementHeight);

			if (position.isCenterX()) x -= elementWidth / 2;
			if (position.isCenterY()) y -= elementHeight / 2;
			Gui.drawRect(x, y, x + elementWidth, y + elementHeight, 0x80404040);
		}
		Utils.drawStringCentered("Position Editor", scaledResolution.getScaledWidth() / 2, 8, true, 0xffffff);
		Utils.drawStringCentered(
			"R to Reset - Arrow keys/mouse to move",
			scaledResolution.getScaledWidth() / 2, 18, true, 0xffffff
		);
		GlStateManager.popMatrix();
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);

		if (mouseButton == 0) {
			ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
			mouseX = Mouse.getX() * width / Minecraft.getMinecraft().displayWidth;
			mouseY = height - Mouse.getY() * height / Minecraft.getMinecraft().displayHeight - 1;
			for (int i = positions.size() - 1; i >= 0; i--) {
				Position position = positions.get(i);
				int elementHeight = elementHeights.get(positions.indexOf(position));
				int elementWidth = elementWidths.get(positions.indexOf(position));
				int x = position.getAbsX(scaledResolution, elementWidth);
				int y = position.getAbsY(scaledResolution, elementHeight);
				if (position.isCenterX()) x -= elementWidth / 2;
				if (position.isCenterY()) y -= elementHeight / 2;
				if (!position.getClicked()) {
					if (mouseX >= x && mouseY >= y &&
						mouseX <= x + elementWidth && mouseY <= y + elementHeight) {
						clickedPos = i;
						position.setClicked(true);
						grabbedX = mouseX;
						grabbedY = mouseY;
						break;
					}
				}

			}
		}
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		if (clickedPos != -1) {
			Position position = positions.get(clickedPos);
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
				ScaledResolution scaledResolution = Utils.pushGuiScale(-1);
				mouseX = Mouse.getX() * width / Minecraft.getMinecraft().displayWidth;
				mouseY = height - Mouse.getY() * height / Minecraft.getMinecraft().displayHeight - 1;

				grabbedX += position.moveX(mouseX - grabbedX, elementWidth, scaledResolution);
				grabbedY += position.moveY(mouseY - grabbedY, elementHeight, scaledResolution);
				positionChangedCallback.run();

				Utils.pushGuiScale(-1);
			}
		}
	}
}
