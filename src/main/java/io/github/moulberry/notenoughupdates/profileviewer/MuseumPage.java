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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.core.util.StringUtils;
import io.github.moulberry.notenoughupdates.util.Constants;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static io.github.moulberry.notenoughupdates.profileviewer.GuiProfileViewer.pv_elements;

public class MuseumPage extends GuiProfileViewerPage {
	private static final ResourceLocation pv_inventories =
		new ResourceLocation("notenoughupdates:pv_inventories.png");
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
	private static final ResourceLocation CHEST_GUI_TEXTURE =
		new ResourceLocation("textures/gui/container/generic_54.png");
	private static String selectedMuseumCategory = "weapons";
	JsonObject museum = Constants.MUSEUM;
	int pageArrowsHeight = 164;
	int pages = 0;
	int onPage = 0;
	String currentItemSelected = null;
	JsonArray selectedItem = null;

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

		SkyblockProfiles.SkyblockProfile.MuseumData museumData = selectedProfile.getMuseumData();
		long value = museumData.getValue();

		if (value == -2) {
			String message = EnumChatFormatting.RED + "Museum API Disabled!";
			Utils.drawStringCentered(message, guiLeft + 250, guiTop + 101, true, 0);
			return;
		}
		if (value == -1) {
			String message = EnumChatFormatting.YELLOW + "Museum Data Loading!";
			Utils.drawStringCentered(message, guiLeft + 250, guiTop + 101, true, 0);
			return;
		}
		if (value == -3 || museum == null) {
			String message = EnumChatFormatting.RED + "Missing Repo Data!";
			Utils.drawStringCentered(message, guiLeft + 250, guiTop + 101, true, 0);
			return;
		}

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

		Utils.renderAlignedString(
			EnumChatFormatting.GOLD + "Museum Value",
			EnumChatFormatting.WHITE + StringUtils.shortNumberFormat(value),
			guiLeft + 21,
			guiTop + 25,
			114
		);

		int donated =
			museumData.getWeaponItems().size() + museumData.getArmorItems().size() + museumData.getRaritiesItems().size();
		Utils.renderAlignedString(
			EnumChatFormatting.BLUE + "Total Donations",
			EnumChatFormatting.WHITE + "" + donated,
			guiLeft + 21,
			guiTop + 45,
			114
		);
		int maximum = getMaximum("total");
		getInstance().renderBar(guiLeft + 20, guiTop + 55, 116, (float) donated / maximum);

		donated = museumData.getWeaponItems().size();
		Utils.renderAlignedString(
			EnumChatFormatting.BLUE + "Weapons Donated",
			EnumChatFormatting.WHITE + "" + donated,
			guiLeft + 21,
			guiTop + 70,
			114
		);
		maximum = getMaximum("weapons");
		getInstance().renderBar(guiLeft + 20, guiTop + 80, 116, (float) donated / maximum);

		donated = museumData.getArmorItems().size();
		Utils.renderAlignedString(
			EnumChatFormatting.BLUE + "Armor Donated",
			EnumChatFormatting.WHITE + "" + donated,
			guiLeft + 21,
			guiTop + 95,
			114
		);
		maximum = getMaximum("armor");
		getInstance().renderBar(guiLeft + 20, guiTop + 105, 116, (float) donated / maximum);

		donated = museumData.getRaritiesItems().size();
		Utils.renderAlignedString(
			EnumChatFormatting.BLUE + "Rarities Donated",
			EnumChatFormatting.WHITE + "" + donated,
			guiLeft + 21,
			guiTop + 120,
			114
		);
		maximum = getMaximum("rarities");
		getInstance().renderBar(guiLeft + 20, guiTop + 130, 116, (float) donated / maximum);

		donated = museumData.getSpecialItems().size();
		Utils.renderAlignedString(
			EnumChatFormatting.BLUE + "Special Items Donated",
			EnumChatFormatting.WHITE + String.valueOf(donated),
			guiLeft + 21,
			guiTop + 145,
			114
		);

		Utils.drawStringCentered(
			museumCategories.get(selectedMuseumCategory).getDisplayName(),
			guiLeft + 251, guiTop + 14, true, 4210752
		);

		if (pages == 0) {
			setPage(selectedMuseumCategory);
		}

		boolean leftHovered = false;
		boolean rightHovered = false;
		if (Mouse.isButtonDown(0)) {
			if (mouseY > guiTop + pageArrowsHeight && mouseY < guiTop + pageArrowsHeight + 16) {
				if (mouseX > guiLeft + 251 - 12 && mouseX < guiLeft + 251 + 12) {
					if (mouseX < guiLeft + 251) {
						leftHovered = true;
					} else {
						rightHovered = true;
					}
				}
			}
		}
		Minecraft.getMinecraft().getTextureManager().bindTexture(GuiProfileViewer.resource_packs);

		if (onPage > 0) {
			Utils.drawTexturedRect(
				guiLeft + 251 - 12,
				guiTop + pageArrowsHeight,
				12,
				16,
				29 / 256f,
				53 / 256f,
				!leftHovered ? 0 : 32 / 256f,
				!leftHovered ? 32 / 256f : 64 / 256f,
				GL11.GL_NEAREST
			);
		}
		if (onPage < pages && pages > 1) {
			Utils.drawTexturedRect(
				guiLeft + 251,
				guiTop + pageArrowsHeight,
				12,
				16,
				5 / 256f,
				29 / 256f,
				!rightHovered ? 0 : 32 / 256f,
				!rightHovered ? 32 / 256f : 64 / 256f,
				GL11.GL_NEAREST
			);
		}

		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().getTextureManager().bindTexture(CHEST_GUI_TEXTURE);

		int inventoryRows = 4;
		int invSizeY = inventoryRows * 18 + 17 + 7;

		int inventoryX = guiLeft + 251 - 176 / 2;
		int inventoryY = guiTop + 101 - invSizeY / 2;
		getInstance().drawTexturedModalRect(inventoryX, inventoryY, 0, 0, 176, inventoryRows * 18 + 17);
		getInstance().drawTexturedModalRect(inventoryX, inventoryY + inventoryRows * 18 + 17, 0, 215, 176, 7);

		JsonArray categoryItems = new JsonArray();
		Map<String, JsonArray> categoryDonated = new HashMap<>();
		switch (selectedMuseumCategory) {
			case "weapons":
				categoryItems = museum.get("weapons").getAsJsonArray();
				categoryDonated = museumData.getWeaponItems();
				break;
			case "armor":
				categoryItems = museum.get("armor").getAsJsonArray();
				categoryDonated = museumData.getArmorItems();
				break;
			case "rarities":
				categoryItems = museum.get("rarities").getAsJsonArray();
				categoryDonated = museumData.getRaritiesItems();
				break;
			case "special":
				pages = (int) Math.floor(donated / 28.0);

				List<JsonArray> specialItems = museumData.getSpecialItems();

				int startIndex = onPage * 28;
				int endIndex = Math.min(startIndex + 28, specialItems.size());

				int row = 0;
				int slot = 0;
				for (int i = startIndex; i < endIndex; i++) {
					JsonArray items = specialItems.get(i);
					JsonObject item = (JsonObject) items.get(0);
					ItemStack stack = NotEnoughUpdates.INSTANCE.manager.jsonToStack(item, true);

					if (slot % 7 == 0 && slot > 1) {
						slot = 0;
						row++;
					}

					int x = guiLeft + (inventoryX - guiLeft) + 8 + (slot * 18) + 18;
					int y = guiTop + 71 + (row * 18);
					slot++;

					if ((mouseX >= x && mouseX <= x + 16) &&
						(mouseY >= y && mouseY <= y + 16)) {
						getInstance().tooltipToDisplay = stack.getTooltip(Minecraft.getMinecraft().thePlayer, false);
						String itemID = item.get("internalname").getAsString();
						if (Mouse.isButtonDown(0) && museumData.getSavedItems().containsKey(itemID)) {
							selectedItem = items;
							currentItemSelected = itemID;
						}
					}
					Utils.drawItemStack(stack, x, y);
				}
				break;
			default:
		}

		if (categoryItems != null) {
			int row = 0;
			int slot = 0;
			int startIndex = onPage * 28;
			int endIndex = Math.min(startIndex + 28, categoryItems.size());
			for (int i = startIndex; i < endIndex; i++) {
				boolean actualItem = false;
				JsonElement donatedItem = categoryItems.get(i);
				String itemID = donatedItem.getAsString();

				if (slot % 7 == 0 && slot > 1) {
					slot = 0;
					row++;
				}

				int x = guiLeft + (inventoryX - guiLeft) + 8 + (slot * 18) + 18;
				int y = guiTop + 71 + (row * 18);
				slot++;

				JsonObject nameMappings = museum.get("armor_to_id").getAsJsonObject();
				String mappedName = itemID;
				if (nameMappings.has(itemID)) {
					mappedName = nameMappings.get(itemID).getAsString();
				}
				String displayName = NotEnoughUpdates.INSTANCE.manager.getDisplayName(mappedName);

				ItemStack stack = Utils.createItemStack(Items.dye, displayName, 8, EnumChatFormatting.RED + "Missing");
				JsonArray items = new JsonArray();
				if (categoryDonated.containsKey(itemID)) {
					items = categoryDonated.get(itemID);
					JsonObject item = (JsonObject) items.get(0);
					if (!Objects.equals(item.get("internalname").getAsString(), "_")) {
						actualItem = true;
					}
					stack = NotEnoughUpdates.INSTANCE.manager.jsonToStack(item, true);
				}

				if ((mouseX >= x && mouseX <= x + 16) &&
					(mouseY >= y && mouseY <= y + 16)) {
					if (Mouse.isButtonDown(0) && museumData.getSavedItems().containsKey(itemID) && actualItem) {
						selectedItem = items;
						currentItemSelected = itemID;
					}
					getInstance().tooltipToDisplay = stack.getTooltip(Minecraft.getMinecraft().thePlayer, false);
				}
				Utils.drawItemStack(stack, x, y);
			}
		}

		if (currentItemSelected != null) {
			int size = selectedItem.size();
			int startX = guiLeft + 375 + 5;
			Minecraft.getMinecraft().getTextureManager().bindTexture(pv_inventories);
			switch (size) {
				case 1:
					Utils.drawTexturedRect(
						guiLeft + 375,
						guiTop + 100,
						26,
						32,
						75 / 101f,
						1,
						69 / 101f,
						1,
						GL11.GL_NEAREST
					);
					break;
				case 3:
					Utils.drawTexturedRect(
						guiLeft + 375,
						guiTop + 100,
						26,
						68,
						75 / 101f,
						1,
						0,
						68 / 101f,
						GL11.GL_NEAREST
					);
					break;
				case 4:
					Utils.drawTexturedRect(
						guiLeft + 375,
						guiTop + 100,
						26,
						86,
						47 / 101f,
						73 / 101f,
						0,
						86 / 101f,
						GL11.GL_NEAREST
					);
					break;
				default:
					Utils.drawTexturedRect(
						guiLeft + 365,
						guiTop + 100,
						45,
						86,
						0,
						45 / 101f,
						0,
						86 / 101f,
						GL11.GL_NEAREST
					);
					startX = guiLeft + 365 + 5;
			}

			int startY = guiTop + 100 + 8;
			int row = 0;
			int column = 0;
			boolean is_five = false;
			if (size == 5) {
				size = 8;
				is_five = true;
			}
			for (int i = 0; i < size; i++) {
				ItemStack stack = new ItemStack(Blocks.barrier);
				if (!is_five || i < 5) {
					JsonObject item = (JsonObject) selectedItem.get(i);
					stack = NotEnoughUpdates.INSTANCE.manager.jsonToStack(item, true);
				}

				if (row % 4 == 0 && row > 1) {
					column = 1;
					row = 0;
				}

				int x = startX + (column * 19);
				int y = startY + (row * 18);

				Utils.drawItemStack(stack, x, y);

				if ((mouseX >= x && mouseX <= x + 16) &&
					(mouseY >= y && mouseY <= y + 16)) {
					if (!is_five || i < 5) {
						getInstance().tooltipToDisplay = stack.getTooltip(Minecraft.getMinecraft().thePlayer, false);
					}
				}
				row++;
			}

			Pair<Long, Boolean> itemData = museumData.getSavedItems().get(currentItemSelected);
			String donationStatus =
				itemData.getRight() ? EnumChatFormatting.YELLOW + "Borrowing" : EnumChatFormatting.GREEN + "In Museum";
			String donationTime = Utils.timeSinceMillisecond(itemData.getLeft());

			Utils.drawStringCentered(EnumChatFormatting.BLUE + "Donated", guiLeft + 391, guiTop + 35, true, 4210752);
			Utils.drawStringCentered(EnumChatFormatting.WHITE + donationTime, guiLeft + 391, guiTop + 47, true, 4210752);
			Utils.drawStringCentered(EnumChatFormatting.BLUE + "Currently", guiLeft + 391, guiTop + 70, true, 4210752);
			Utils.drawStringCentered(donationStatus, guiLeft + 391, guiTop + 82, true, 4210752);
		}
	}

	@Override
	public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
		int guiLeft = GuiProfileViewer.getGuiLeft();
		int guiTop = GuiProfileViewer.getGuiTop();
		int xIndex = 0;
		for (Map.Entry<String, ItemStack> entry : museumCategories.entrySet()) {
			if (mouseX > guiLeft + 16 + 34 * xIndex && mouseX < guiLeft + 16 + 34 * xIndex + 20) {
				if (mouseY > guiTop + 172 && mouseY < guiTop + 172 + 20) {
					setPage(entry.getKey());
					Utils.playPressSound();
					return;
				}
			}
			xIndex++;
		}

		if (mouseY > guiTop + pageArrowsHeight && mouseY < guiTop + pageArrowsHeight + 16) {
			if (mouseX > guiLeft + 251 - 12 && mouseX < guiLeft + 251 + 12) {
				if (mouseX < guiLeft + 251) {
					if (onPage > 0) onPage--;
				} else {
					if (onPage < pages) onPage++;
				}
			}
		}
	}

	@Override
	public void keyTyped(char typedChar, int keyCode) throws IOException {
		switch (keyCode) {
			case Keyboard.KEY_1:
			case Keyboard.KEY_NUMPAD1:
				setPage("weapons");
				break;
			case Keyboard.KEY_2:
			case Keyboard.KEY_NUMPAD2:
				setPage("armor");
				break;
			case Keyboard.KEY_3:
			case Keyboard.KEY_NUMPAD3:
				setPage("rarities");
				break;
			case Keyboard.KEY_4:
			case Keyboard.KEY_NUMPAD4:
				setPage("special");
				break;
			default:
				return;
		}
		Utils.playPressSound();
	}

	private void setPage(String pageName) {
		selectedMuseumCategory = pageName;
		onPage = 0;
		pages = (int) Math.floor(getMaximum(pageName) / 28.0);
	}

	private int getMaximum(String name) {
		if (museum != null && museum.has("max_values")) {
			JsonObject maxValues = museum.get("max_values").getAsJsonObject();
			if (maxValues.has(name)) {
				return maxValues.get(name).getAsInt();
			}
		}
		return 0;
	}
}
