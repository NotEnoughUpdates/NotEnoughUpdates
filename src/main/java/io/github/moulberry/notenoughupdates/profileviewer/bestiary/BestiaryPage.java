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

package io.github.moulberry.notenoughupdates.profileviewer.bestiary;

import io.github.moulberry.notenoughupdates.profileviewer.GuiProfileViewer;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class BestiaryPage {

	private static int guiLeft;
	private static int guiTop;
	public static final ResourceLocation pv_elements = new ResourceLocation("notenoughupdates:pv_elements.png");
	private static ItemStack selectedBestiaryLocation = null;

	public static void renderPage(int mouseX, int mouseY) {
		guiLeft = GuiProfileViewer.getGuiLeft();
		guiTop = GuiProfileViewer.getGuiTop();

		int bestiarySize = BestiaryData.getBestiaryLocations().size();
		int bestiaryXSize = (int) (350f / (bestiarySize - 1 + 0.0000001f));


		int yIndex = 0;
		for (ItemStack stack : BestiaryData.getBestiaryLocations().keySet()) {
			Minecraft.getMinecraft().getTextureManager().bindTexture(pv_elements);
			if (stack == selectedBestiaryLocation) {
				Utils.drawTexturedRect(guiLeft + 30 + bestiaryXSize * yIndex, guiTop + 10, 20, 20,
					20 / 256f, 0, 20 / 256f, 0, GL11.GL_NEAREST
				);
				Utils.drawItemStack(
					stack,
					guiLeft + 32 + bestiaryXSize * yIndex,
					guiTop + 12
				);
			} else {
				Utils.drawTexturedRect(guiLeft + 30 + bestiaryXSize * yIndex, guiTop + 10, 20, 20,
					0, 20 / 256f, 0, 20 / 256f, GL11.GL_NEAREST
				);
				Utils.drawItemStack(stack, guiLeft + 32 + bestiaryXSize * yIndex, guiTop + 12);
			}
			yIndex++;
		}

	}

	public static void mouseReleased(int mouseX, int mouseY, int mouseButton) {
		int bestiarySize = BestiaryData.getBestiaryLocations().size();
		int bestiaryYSize = (int) (350f / (bestiarySize - 1 + 0.0000001f));
		int yIndex = 0;
		for (ItemStack stack : BestiaryData.getBestiaryLocations().keySet()) {
			if (mouseX > guiLeft + 30 + bestiaryYSize * yIndex &&
				mouseX < guiLeft + 30 + bestiaryYSize * yIndex + 20) {
				if (mouseY > guiTop + 10 &&	mouseY < guiTop + 10 + 20) {
					selectedBestiaryLocation = stack;
					Utils.playPressSound();
					return;
				}
			}
			yIndex++;
		}
	}
}
