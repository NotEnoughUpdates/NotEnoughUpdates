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

package io.github.moulberry.notenoughupdates.core.util;

import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.function.Consumer;

public class ArrowPagesUtils {

	public static final int BUTTON_POSITION_RIGHT_OFFSET_X = 37;
	public static final int PAGE_STRING_OFFSET_X = 22;
	public static final int PAGE_STRING_OFFSET_Y = 6;

	public static final int BUTTON_WIDTH = 7;
	public static final int BUTTON_HEIGHT = 11;

	public static final ResourceLocation resourcePacksTexture = new ResourceLocation("textures/gui/resource_packs.png");

	public static void onDraw(int guiLeft, int guiTop, int[] topLeftButton, int currentPage, int totalPages) {
		if (totalPages < 2) return;

		final ScaledResolution scaledresolution = new ScaledResolution(Minecraft.getMinecraft());
		final int scaledWidth = scaledresolution.getScaledWidth();
		final int scaledHeight = scaledresolution.getScaledHeight();
		int mouseX = Mouse.getX() * scaledWidth / Minecraft.getMinecraft().displayWidth;
		int mouseY = scaledHeight - Mouse.getY() * scaledHeight / Minecraft.getMinecraft().displayHeight - 1;

		int buttonPositionLeftX = topLeftButton[0];
		int buttonPositionRightX = buttonPositionLeftX + BUTTON_POSITION_RIGHT_OFFSET_X;
		int pageStringX = buttonPositionLeftX + PAGE_STRING_OFFSET_X;
		int buttonPositionY = topLeftButton[1];
		int pageStringY = buttonPositionY + PAGE_STRING_OFFSET_Y;

		boolean leftSelected = isWithinRect(
			mouseX - guiLeft,
			mouseY - guiTop,
			buttonPositionLeftX,
			buttonPositionY,
			BUTTON_WIDTH,
			BUTTON_HEIGHT
		);
		boolean rightSelected = isWithinRect(
			mouseX - guiLeft,
			mouseY - guiTop,
			buttonPositionRightX,
			buttonPositionY,
			BUTTON_WIDTH,
			BUTTON_HEIGHT
		);
		Minecraft.getMinecraft().getTextureManager().bindTexture(resourcePacksTexture);

		if (currentPage != 0)
			Utils.drawTexturedRect(
				guiLeft + buttonPositionLeftX, guiTop + buttonPositionY, BUTTON_WIDTH, BUTTON_HEIGHT,
				34 / 256f, 48 / 256f,
				leftSelected ? 37 / 256f : 5 / 256f, leftSelected ? 59 / 256f : 27 / 256f
			);
		if (currentPage != totalPages - 1)
			Utils.drawTexturedRect(
				guiLeft + buttonPositionRightX, guiTop + buttonPositionY, BUTTON_WIDTH, BUTTON_HEIGHT,
				10 / 256f, 24 / 256f,
				rightSelected ? 37 / 256f : 5 / 256f, rightSelected ? 59 / 256f : 27 / 256f
			);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

		String selectedPage = (currentPage + 1) + "/" + totalPages;

		Utils.drawStringCenteredScaledMaxWidth(
			selectedPage, guiLeft + pageStringX, guiTop + pageStringY, false, 28, Color.BLACK.getRGB()
		);
	}

	public static boolean onPageSwitchKey(
		int currentPage,
		int totalPages,
		Consumer<Integer> pageChange
	) {

		int keyPressed = Keyboard.getEventKey() == 0 ? Keyboard.getEventCharacter() + 256 : Keyboard.getEventKey();
		if (Keyboard.getEventKeyState() && keyPressed == Keyboard.KEY_LEFT) {
			int newPage = currentPage - 1;
			pageChange.accept(MathHelper.clamp_int(newPage, 0, totalPages - 1));
			return true;
		}
		if (Keyboard.getEventKeyState() && keyPressed == Keyboard.KEY_RIGHT) {
			int newPage = currentPage + 1;
			pageChange.accept(MathHelper.clamp_int(newPage, 0, totalPages - 1));
			return true;
		}

		return false;
	}

	public static boolean onPageSwitchMouse(
		int guiLeft,
		int guiTop,
		int[] topLeft,
		int currentPage,
		int totalPages,
		Consumer<Integer> pageChange
	) {

		int keyPressed = Keyboard.getEventKey() == 0 ? Keyboard.getEventCharacter() + 256 : Keyboard.getEventKey();
		if (Keyboard.getEventKeyState() && keyPressed == Keyboard.KEY_LEFT) {
			int newPage = currentPage - 1;
			pageChange.accept(MathHelper.clamp_int(newPage, 0, totalPages - 1));
			return true;
		}
		if (Keyboard.getEventKeyState() && keyPressed == Keyboard.KEY_RIGHT) {
			int newPage = currentPage + 1;
			pageChange.accept(MathHelper.clamp_int(newPage, 0, totalPages - 1));
			return true;
		}

		final ScaledResolution scaledresolution = new ScaledResolution(Minecraft.getMinecraft());
		final int scaledWidth = scaledresolution.getScaledWidth();
		final int scaledHeight = scaledresolution.getScaledHeight();
		int mouseX = Mouse.getX() * scaledWidth / Minecraft.getMinecraft().displayWidth;
		int mouseY = scaledHeight - Mouse.getY() * scaledHeight / Minecraft.getMinecraft().displayHeight - 1;

		int buttonPositionLeftX = topLeft[0];
		int buttonPositionRightX = buttonPositionLeftX + BUTTON_POSITION_RIGHT_OFFSET_X;
		int buttonPositionY = topLeft[1];

		if (isWithinRect(
			mouseX - guiLeft,
			mouseY - guiTop,
			buttonPositionLeftX,
			buttonPositionY,
			BUTTON_WIDTH,
			BUTTON_HEIGHT
		) &&
			currentPage > 0) {
			int newPage = currentPage - 1;
			pageChange.accept(MathHelper.clamp_int(newPage, 0, totalPages - 1));
			Utils.playPressSound();
			return true;
		}

		if (isWithinRect(
			mouseX - guiLeft,
			mouseY - guiTop,
			buttonPositionRightX,
			buttonPositionY,
			BUTTON_WIDTH,
			BUTTON_HEIGHT
		) &&
			currentPage < totalPages - 1) {
			int newPage = currentPage + 1;
			pageChange.accept(MathHelper.clamp_int(newPage, 0, totalPages - 1));
			Utils.playPressSound();
			return true;
		}

		return false;
	}

	private static boolean isWithinRect(int x, int y, int topLeftX, int topLeftY, int width, int height) {
		return topLeftX <= x && x < topLeftX + width
			&& topLeftY <= y && y < topLeftY + height;
	}
}
