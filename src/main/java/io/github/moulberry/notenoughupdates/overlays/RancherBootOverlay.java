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

package io.github.moulberry.notenoughupdates.overlays;

import io.github.moulberry.notenoughupdates.core.GuiElementTextField;
import io.github.moulberry.notenoughupdates.core.util.GuiElementSlider;
import io.github.moulberry.notenoughupdates.mixins.AccessorGuiEditSign;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiEditSign;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntitySign;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.util.HashMap;

public class RancherBootOverlay {
	private static int selectedIndex = 0;

	private static final HashMap<Integer, Integer> currentSpeeds = new HashMap<>();
	private static final GuiElementSlider slider =
		new GuiElementSlider(0, 0, 145, 100, 400, 1, 300, (val) -> setValue(val.intValue()));
	private static final GuiElementTextField textField =
		new GuiElementTextField("", 48, 20, GuiElementTextField.NUM_ONLY);
	private static boolean textFieldClicked = false;

	public static boolean shouldReplace() {
		if (true) return false;
		//if(!NotEnoughUpdates.INSTANCE.config.auctionHouseSearch.enableSearchOverlay) return false;

		if (!(Minecraft.getMinecraft().currentScreen instanceof GuiEditSign)) return false;

		TileEntitySign tes = ((AccessorGuiEditSign) Minecraft.getMinecraft().currentScreen).getTileSign();

		if (tes == null) return false;
		if (tes.getPos().getY() != 0) return false;
		if (!tes.signText[1].getUnformattedText().equals("^^^^^^")) return false;
		if (!tes.signText[2].getUnformattedText().equals("Set your")) return false;
		return tes.signText[3].getUnformattedText().equals("speed cap!");
	}

	public static void render() {
		ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
		int width = scaledResolution.getScaledWidth();
		int height = scaledResolution.getScaledHeight();
		int mouseX = Mouse.getX() * width / Minecraft.getMinecraft().displayWidth;
		int mouseY = height - Mouse.getY() * height / Minecraft.getMinecraft().displayHeight - 1;

		Utils.drawGradientRect(0, 0, width, height, -1072689136, -804253680);

		int topY = height / 4;

		//Gui.drawRect(width/2-100, topY, width/2+48, topY+20, 0xffffffff);
		Gui.drawRect(width / 2 + 52, topY, width / 2 + 100, topY + 20, 0xffffffff);

		textField.render(width / 2 + 52, topY);

		slider.x = width / 2 - 100;
		slider.y = topY;
		slider.render();

		int numIcons = 3;
		int iconsLeft = width / 2 - (numIcons * 25 - 5) / 2;

		for (int i = 0; i < numIcons; i++) {
			Gui.drawRect(
				iconsLeft + i * 25,
				topY + 25,
				iconsLeft + i * 25 + 20,
				topY + 45,
				selectedIndex == i ? 0xff0000ff : 0xff808080
			);
			Utils.drawItemStack(new ItemStack(Items.carrot), iconsLeft + i * 25 + 2, topY + 25 + 2);
			Utils.drawStringCentered("" + currentSpeeds.get(i), iconsLeft + i * 25 + 10, topY + 52, true, 0xffffffff);
			//Minecraft.getMinecraft().fontRendererObj.drawString("\u2710", iconsLeft+i*25+15, topY+40, 0xffffff, false);
		}

		//Minecraft.getMinecraft().fontRendererObj.drawString("Hello!", 100, 100, 0xffffff);
	}

	public static void close() {

	}

	public static void keyEvent() {
		if (Keyboard.getEventKey() == Keyboard.KEY_ESCAPE) {
			Minecraft.getMinecraft().displayGuiScreen(null);
		} else {
			slider.keyboardInput();

			if (Keyboard.getEventKeyState()) {
				textField.keyTyped(Keyboard.getEventCharacter(), Keyboard.getEventKey());
				if (textField.getText().length() > 5) textField.setText(textField.getText().substring(0, 5));

				try {
					setCurrentSpeed(Integer.parseInt(textField.getText().trim()));
					slider.setValue(getCurrentSpeed());
					textField.setCustomBorderColour(0xfeffffff);
				} catch (NumberFormatException ignored) {
					textField.setCustomBorderColour(0xffff0000);
				}
			}
		}
	}

	private static int getCurrentSpeed() {
		return currentSpeeds.get(selectedIndex);
	}

	private static void setCurrentSpeed(int speed) {
		currentSpeeds.put(selectedIndex, speed);
	}

	public static void setValue(int value) {
		setCurrentSpeed(value);
		textField.setText("" + getCurrentSpeed());
	}

	public static void mouseEvent() {
		ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
		int width = scaledResolution.getScaledWidth();
		int height = scaledResolution.getScaledHeight();
		int mouseX = Mouse.getX() * width / Minecraft.getMinecraft().displayWidth;
		int mouseY = height - Mouse.getY() * height / Minecraft.getMinecraft().displayHeight - 1;

		int topY = height / 4;

		slider.mouseInput(mouseX, mouseY);

		if (!Mouse.getEventButtonState() && Mouse.getEventButton() == -1 && textFieldClicked) {
			textField.mouseClickMove(mouseX - 2, topY + 10, 0, 0);
		}

		if (Mouse.getEventButton() != -1) {
			textFieldClicked = false;
		}

		if (mouseX > width / 2 + 52 && mouseX < width / 2 + 100 && mouseY > topY && mouseY < topY + 20) {
			if (Mouse.getEventButtonState()) {
				textField.mouseClicked(mouseX, mouseY, Mouse.getEventButton());
				textFieldClicked = true;
			}
		}

		if (Mouse.getEventButtonState()) {
			int numIcons = 3;
			int iconsLeft = width / 2 - (numIcons * 25 - 5) / 2;

			for (int i = 0; i < numIcons; i++) {
				if (mouseX > iconsLeft + i * 25 && mouseX < iconsLeft + i * 25 + 20 && mouseY > topY + 25 &&
					mouseY < topY + 45) {
					if (i != selectedIndex) {
						selectedIndex = i;
						slider.setValue(getCurrentSpeed());
						textField.setText("" + getCurrentSpeed());
					}
					return;
				}
				//Minecraft.getMinecraft().fontRendererObj.drawString("\u2710", iconsLeft+i*25+15, topY+40, 0xffffff, false);
			}
		}
	}
}
