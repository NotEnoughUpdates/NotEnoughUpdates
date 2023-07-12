/*
 * Copyright (C) 2023 NotEnoughUpdates contributors
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

package io.github.moulberry.notenoughupdates.profileviewer;

import io.github.moulberry.notenoughupdates.core.util.StringUtils;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import static io.github.moulberry.notenoughupdates.profileviewer.GuiProfileViewer.pv_elements;

public class MuseumPage extends GuiProfileViewerPage {
	private static final ResourceLocation pv_museum = new ResourceLocation("notenoughupdates:pv_museum.png");
	private static final LinkedHashMap<String, ItemStack> museumCategories = new LinkedHashMap<String, ItemStack>() {
		{
			put("weapons", Utils.createItemStack(Items.diamond_sword, EnumChatFormatting.GOLD + "Weapons"));
			put("armor", Utils.createItemStack(Items.diamond_chestplate, EnumChatFormatting.GOLD + "Armor Sets"));
			put(
				"rarities", Utils.createSkull(
				EnumChatFormatting.GOLD + "Rarities",
				"b569ed03-94ae-3da9-a01d-9726633d5b8b",
				"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODZhZGRiZDVkZWRhZDQwOTk5NDczYmU0YTdmNDhmNjIzNmE3OWEwZGNlOTcxYjVkYmQ3MzcyMDE0YWUzOTRkIn19fQ"
				)
			);
			put("special", Utils.createItemStack(Items.cake, EnumChatFormatting.GOLD + "Special Items"));
		}
	};

	private static String selectedMuseumCategory = "weapons";

	public MuseumPage(GuiProfileViewer instance) {super(instance);}

	@Override
	public void drawPage(int mouseX, int mouseY, float partialTicks) {
		int guiLeft = GuiProfileViewer.getGuiLeft();
		int guiTop = GuiProfileViewer.getGuiTop();

		SkyblockProfiles.SkyblockProfile selectedProfile = getSelectedProfile();
		if (selectedProfile == null) {
			return;
		}

		Minecraft.getMinecraft().getTextureManager().bindTexture(pv_museum);
		Utils.drawTexturedRect(guiLeft, guiTop, getInstance().sizeX, getInstance().sizeY, GL11.GL_NEAREST);

		// todo api off warning

		int xIndex = 0;
		for (Map.Entry<String, ItemStack> entry : museumCategories.entrySet()) {
			Minecraft.getMinecraft().getTextureManager().bindTexture(pv_elements);

			if (entry.getKey().equals(selectedMuseumCategory)) {
				Utils.drawTexturedRect(
					guiLeft + 16 + 34 * xIndex,
					guiTop + 172,
					20,
					20,
					20 / 256f,
					0,
					20 / 256f,
					0,
					GL11.GL_NEAREST
				);
				Utils.drawItemStackWithText(entry.getValue(), guiLeft + 19 + 34 * xIndex, guiTop + 175, "" + (xIndex + 1));
			} else {
				Utils.drawTexturedRect(
					guiLeft + 16 + 34 * xIndex,
					guiTop + 172,
					20,
					20,
					0,
					20 / 256f,
					0,
					20 / 256f,
					GL11.GL_NEAREST
				);
				Utils.drawItemStackWithText(entry.getValue(), guiLeft + 18 + 34 * xIndex, guiTop + 174, "" + (xIndex + 1));
			}
			xIndex++;
		}

		SkyblockProfiles.SkyblockProfile.MuseumData museumData = selectedProfile.getMuseumData();
		long value = museumData.getValue();

		// todo get values
		Utils.renderAlignedString(
			EnumChatFormatting.GOLD + "Museum Value",
			EnumChatFormatting.WHITE + StringUtils.shortNumberFormat(value),
			guiLeft + 21,
			guiTop + 25,
			114
		);

		Utils.renderAlignedString(
			EnumChatFormatting.BLUE + "Total Donations",
			EnumChatFormatting.WHITE + "XXX",
			guiLeft + 21,
			guiTop + 45,
			114
		);
		getInstance().renderBar(guiLeft + 20, guiTop + 55, 116, 0);

		Utils.renderAlignedString(
			EnumChatFormatting.BLUE + "Weapons Donated",
			EnumChatFormatting.WHITE + "XXX",
			guiLeft + 21,
			guiTop + 70,
			114
		);
		getInstance().renderBar(guiLeft + 20, guiTop + 80, 116, 0);

		Utils.renderAlignedString(
			EnumChatFormatting.BLUE + "Armor Donated",
			EnumChatFormatting.WHITE + "XXX",
			guiLeft + 21,
			guiTop + 95,
			114
		);
		getInstance().renderBar(guiLeft + 20, guiTop + 105, 116, 0);

		Utils.renderAlignedString(
			EnumChatFormatting.BLUE + "Rarities Donated",
			EnumChatFormatting.WHITE + "XXX",
			guiLeft + 21,
			guiTop + 120,
			114
		);
		getInstance().renderBar(guiLeft + 20, guiTop + 130, 116, 0);

		Utils.renderAlignedString(
			EnumChatFormatting.BLUE + "Special Items Donated",
			EnumChatFormatting.WHITE + "XXX",
			guiLeft + 21,
			guiTop + 145,
			114
		);

		Utils.drawStringCentered(
			museumCategories.get(selectedMuseumCategory).getDisplayName(),
			guiLeft + 280, guiTop + 14, true, 4210752
		);
	}

	@Override
	public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
		int guiLeft = GuiProfileViewer.getGuiLeft();
		int guiTop = GuiProfileViewer.getGuiTop();
		int xIndex = 0;
		for (Map.Entry<String, ItemStack> entry : museumCategories.entrySet()) {
			if (mouseX > guiLeft + 16 + 34 * xIndex && mouseX < guiLeft + 16 + 34 * xIndex + 20) {
				if (mouseY > guiTop + 172 && mouseY < guiTop + 172 + 20) {
					selectedMuseumCategory = entry.getKey();
					Utils.playPressSound();
					return;
				}
			}
			xIndex++;
		}
	}

	@Override
	public void keyTyped(char typedChar, int keyCode) throws IOException {
		switch (keyCode) {
			case Keyboard.KEY_1:
			case Keyboard.KEY_NUMPAD1:
				selectedMuseumCategory = "weapons";
				break;
			case Keyboard.KEY_2:
			case Keyboard.KEY_NUMPAD2:
				selectedMuseumCategory = "armor";
				break;
			case Keyboard.KEY_3:
			case Keyboard.KEY_NUMPAD3:
				selectedMuseumCategory = "rarities";
				break;
			case Keyboard.KEY_4:
			case Keyboard.KEY_NUMPAD4:
				selectedMuseumCategory = "special";
				break;
			default:
				getInstance().inventoryTextField.keyTyped(typedChar, keyCode);
				return;
		}
		Utils.playPressSound();
		getInstance().inventoryTextField.keyTyped(typedChar, keyCode);
	}
}
