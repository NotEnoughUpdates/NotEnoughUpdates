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

package io.github.moulberry.notenoughupdates.profileviewer.rift;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.core.util.ArrowPagesUtils;
import io.github.moulberry.notenoughupdates.core.util.StringUtils;
import io.github.moulberry.notenoughupdates.miscfeatures.PetInfoOverlay;
import io.github.moulberry.notenoughupdates.profileviewer.GuiProfileViewer;
import io.github.moulberry.notenoughupdates.profileviewer.GuiProfileViewerPage;
import io.github.moulberry.notenoughupdates.profileviewer.ProfileViewer;
import io.github.moulberry.notenoughupdates.profileviewer.SkyblockProfiles;
import io.github.moulberry.notenoughupdates.util.ItemUtils;
import io.github.moulberry.notenoughupdates.util.PetLeveling;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

public class RiftPage extends GuiProfileViewerPage {

	private static final ResourceLocation pv_rift = new ResourceLocation("notenoughupdates:pv_rift.png");

	int pages = 0;

	int onPage = 0;
	private static final ResourceLocation CHEST_GUI_TEXTURE =
		new ResourceLocation("textures/gui/container/generic_54.png");

	boolean inInventory = true; // false = in enderchest

	int guiLeft;
	int guiTop;

	private final ItemStack fillerStack = new ItemStack(Item.getItemFromBlock(Blocks.stained_glass_pane), 1, 15);

	public RiftPage(GuiProfileViewer instance) {
		super(instance);
	}

	@Override
	public void drawPage(int mouseX, int mouseY, float partialTicks) {
		guiLeft = GuiProfileViewer.getGuiLeft();
		guiTop = GuiProfileViewer.getGuiTop();

		Minecraft.getMinecraft().getTextureManager().bindTexture(pv_rift);
		Utils.drawTexturedRect(guiLeft, guiTop, getInstance().sizeX, getInstance().sizeY, GL11.GL_NEAREST);

		SkyblockProfiles.SkyblockProfile selectedProfile = getSelectedProfile();
		if (selectedProfile == null) {
			drawErrorMessage();
			return;
		}
		JsonObject profileInfo = selectedProfile.getProfileJson();
		if (!profileInfo.has("rift")) {
			drawErrorMessage();
			return;
		}

		JsonObject riftData = profileInfo.getAsJsonObject("rift");
		JsonObject riftInventory = riftData.getAsJsonObject("inventory");
		if (riftInventory == null) {
			drawErrorMessage();
			return;
		}

		JsonObject riftArmor = riftInventory.getAsJsonObject("inv_armor");
		if (riftArmor != null && riftArmor.has("data")) {
			List<JsonObject> armorData = readBase64(riftArmor.get("data").getAsString());
			drawArmorAndEquipment(armorData, guiLeft, guiTop, 27, 64, mouseX, mouseY, true);
		}

		if (riftInventory.has("equippment_contents") &&
			riftInventory.getAsJsonObject("equippment_contents").has("data")) {
			List<JsonObject> equipmentData = readBase64(riftInventory
				.getAsJsonObject("equippment_contents")
				.get("data")
				.getAsString());
			drawArmorAndEquipment(equipmentData, guiLeft, guiTop, 46, 64, mouseX, mouseY, false);
		}

		// pet
		Minecraft.getMinecraft().getTextureManager().bindTexture(GuiProfileViewer.pv_elements);
		Utils.drawTexturedRect(guiLeft + 35, guiTop + 156, 20, 20, 0, 20 / 256f, 0, 20 / 256f, GL11.GL_NEAREST);

		JsonObject deadCats = riftData.getAsJsonObject("dead_cats");
		if (!deadCats.entrySet().isEmpty() && deadCats.has("found_cata")) {
			JsonArray foundCats = deadCats.getAsJsonArray("found_cats");

			int size = foundCats.size();
			int riftTime = size * 15;
			int manaRegen = size * 2;

			JsonObject montezuma = deadCats.getAsJsonObject("montezuma");
			String montezumaType = montezuma.get("type").getAsString();

			PetInfoOverlay.Pet pet = new PetInfoOverlay.Pet();
			pet.petLevel = new PetLeveling.PetLevel(100, 100, 0, 0, 0, montezuma.get("exp").getAsInt());
			pet.rarity = PetInfoOverlay.Rarity.valueOf(montezuma.get("tier").getAsString().toUpperCase());
			pet.petType = montezumaType;
			pet.candyUsed = montezuma.get("candyUsed").getAsInt();
			ItemStack petItemstackFromPetInfo = ItemUtils.createPetItemstackFromPetInfo(pet);
			Utils.drawItemStack(petItemstackFromPetInfo, guiLeft + 37, guiTop + 158, true);

			if ((mouseX > guiLeft + 37 && mouseX < guiLeft + 37 + 20) &&
				(mouseY > guiTop + 158 && mouseY < guiTop + 158 + 20)) {
				List<String> tooltip = petItemstackFromPetInfo.getTooltip(Minecraft.getMinecraft().thePlayer, false);
				tooltip.set(3, "§7Found: §9" + size + "/9 Soul Pieces");
				tooltip.set(5, "§7Rift Time: §a+" + riftTime + "s");
				tooltip.set(6, "§7Mana Regen: §a+" + manaRegen + "%");

				getInstance().tooltipToDisplay = tooltip;
			}
		}

		int motesPurse = 0;
		if (profileInfo.has("motes_purse")) {
			motesPurse = profileInfo.get("motes_purse").getAsInt();
		}
		Utils.drawStringCenteredScaledMaxWidth(
			"§dMotes: §f" + Utils.shortNumberFormat(motesPurse, 0),
			guiLeft + 45,
			guiTop + 16,
			true,
			84,
			0
		);

		if ((mouseX > guiLeft + 3 && mouseX < guiLeft + 90) &&
			(mouseY > guiTop + 3 && mouseY < guiTop + 25)) {
			JsonObject stats = profileInfo.get("stats").getAsJsonObject();
			if (stats.has("rift_lifetime_motes_earned")) {
				getInstance().tooltipToDisplay = Collections.singletonList(
					"§dLifetime Motes: §f" + Utils.shortNumberFormat(stats.get("rift_lifetime_motes_earned").getAsInt(), 0));
			}
		}

		// Timecharms

		JsonObject gallery = riftData.getAsJsonObject("gallery");
		JsonArray timecharm = gallery.getAsJsonArray("secured_trophies");
		// 346, 16

		if (timecharm != null) {
			Utils.drawStringScaled(
				EnumChatFormatting.RED + "Timecharms: §f" + timecharm.size() + "/7",
				guiLeft + 336,
				guiTop + 39,
				true,
				0,
				1f
			);

			if ((mouseX > guiLeft + 336 && mouseX < guiLeft + 336 + 80) &&
				(mouseY > guiTop + 39 && mouseY < guiTop + 39 + 15)) {

				List<String> displayNames = new ArrayList<>();
				for (JsonElement jsonElement : timecharm) {
					String timecharmType = jsonElement.getAsJsonObject().get("type").getAsString();
					String displayName = NotEnoughUpdates.INSTANCE.manager.createItemResolutionQuery().withKnownInternalName(
						"RIFT_TROPHY_" + timecharmType.toUpperCase()).resolveToItemStack().getDisplayName();
					displayNames.add(displayName + "§7: §a✔");
				}
				getInstance().tooltipToDisplay = displayNames;
			}

			renderItem("GLASS", 316, 36, guiLeft, guiTop);
		}

		JsonObject castleData = riftData.getAsJsonObject("castle");

		int grubberStacks = 0;
		if (castleData.has("grubber_stacks")) {
			grubberStacks = castleData.get("grubber_stacks").getAsInt();
		}

		Utils.drawStringScaled(
			EnumChatFormatting.GOLD + "Burger: §f" + grubberStacks + "/5",
			guiLeft + 331,
			guiTop + 87,
			true,
			0,
			1f
		);
		renderItem("MCGRUBBER_BURGER", 314, +84, guiLeft, guiTop);

		ProfileViewer.Level vampire = selectedProfile.getLevelingInfo().get("vampire");

		Utils.renderAlignedString(
			"§6Vampire",
			EnumChatFormatting.WHITE.toString() + (int) vampire.level,
			guiLeft + 336,
			guiTop + 61,
			60
		);

		if (vampire.maxed) {
			getInstance().renderGoldBar(guiLeft + 320, guiTop + 69, 90);
		} else {
			getInstance().renderBar(guiLeft + 320, guiTop + 69, 90, vampire.level % 1);
		}

		if (mouseX > guiLeft + 300 && mouseX < guiLeft + 410) {
			if (mouseY > guiTop + 58 && mouseY < guiTop + 80) {
				getInstance().tooltipToDisplay = new ArrayList<>();
				List<String> tooltipToDisplay = getInstance().tooltipToDisplay;
				tooltipToDisplay.add("§6Vampire Slayer");
				if (vampire.maxed) {
					tooltipToDisplay.add(
						EnumChatFormatting.GRAY + "Progress: " + EnumChatFormatting.GOLD + "MAXED!");
				} else {
					int maxXp = (int) vampire.maxXpForLevel;
					getInstance()
						.tooltipToDisplay.add(
							EnumChatFormatting.GRAY +
								"Progress: " +
								EnumChatFormatting.DARK_PURPLE +
								StringUtils.shortNumberFormat(Math.round((vampire.level % 1) * maxXp)) +
								"/" +
								StringUtils.shortNumberFormat(maxXp));
				}
			}
		}

		JsonObject enigma = riftData.getAsJsonObject("enigma");
		int foundSouls = 0;
		if (enigma.has("found_souls")) {
			foundSouls = enigma.getAsJsonArray("found_souls").size();
		}

		Utils.drawStringScaled(
			EnumChatFormatting.DARK_PURPLE + "Enigma Souls: §f" + foundSouls + "/42",
			guiLeft + 331,
			guiTop + 110,
			true,
			0,
			0.9f
		);

		renderItem("SKYBLOCK_ENIGMA_SOUL", 314, 106, guiLeft, guiTop);

		// button

		addInventoryButton(156, 16, guiLeft, guiTop, mouseX, mouseY, "§7Inventory", "CHEST");
		addInventoryButton(222, 16, guiLeft, guiTop, mouseX, mouseY, "§7Ender Chest", "ENDER_CHEST");

		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().getTextureManager().bindTexture(CHEST_GUI_TEXTURE);

		int inventoryRows = inInventory ? 4 : 5;
		int invSizeY = inventoryRows * 18 + 17 + 7;

		int inventoryX = guiLeft + 203 - 176 / 2;
		int inventoryY = guiTop + 130 - invSizeY / 2;
		getInstance().drawTexturedModalRect(inventoryX, inventoryY, 0, 0, 176, inventoryRows * 18 + 17);
		getInstance().drawTexturedModalRect(inventoryX, inventoryY + inventoryRows * 18 + 17, 0, 215, 176, 7);

		Utils.drawStringF(
			inInventory ? "Inventory" : "Ender Chest",
			guiLeft + 122,
			inInventory ? guiTop + 87 + 1 : guiTop + 79,
			false,
			4210752
		);

		if (!inInventory) {
			if (!riftInventory.has("ender_chest_contents")) {
				drawErrorMessage();
				return;
			}
			JsonObject enderChestContents = riftInventory.getAsJsonObject("ender_chest_contents");
			String data = enderChestContents.get("data").getAsString();
			List<JsonObject> jsonObjects = readBase64(data);
			pages = (int) (Math.ceil(jsonObjects.size() / 45d));

			drawArrows(onPage, pages, 190, 77);

			for (int i = 0; i <= pages; i++) {
				if (i != onPage) continue;

				List<JsonObject> page = jsonObjects.subList(
					Math.min(i == 0 ? 0 : i * 45, jsonObjects.size() - 45),
					i == 0 ? 45 : jsonObjects.size()
				); // if anybody has an idea how to make this less hard coded on 2 pages (more pages) please do it for me, i am doing this at 4 am

				int row = 0;
				int slot = 0;

				for (int j = 0; j < page.size(); j++) {
					JsonObject jsonObject = page.get(j);
					if (j % 9 == 0 && j > 0) {
						slot = 0;
						row++;
					}

					int x = (inventoryX - guiLeft) + 8 + (slot * 18);
					int y = 91 + (row * 18);
					slot++;
					if (jsonObject != null) {
						ItemStack itemStack = NotEnoughUpdates.INSTANCE.manager.jsonToStack(jsonObject);

						if ((mouseX >= guiLeft + x && mouseX <= guiLeft + x + 16) &&
							(mouseY >= guiTop + y && mouseY <= guiTop + y + 16)) {
							getInstance().tooltipToDisplay =
								itemStack.getTooltip(
									Minecraft.getMinecraft().thePlayer,
									Minecraft.getMinecraft().gameSettings.advancedItemTooltips
								);
						}
						renderItem(itemStack, x, y, guiLeft, guiTop);
					}
				}
			}
		} else {

			if (riftInventory == null || !riftInventory.has("inv_contents")) {
				drawErrorMessage();
				return;
			}
			String invData = riftInventory.getAsJsonObject("inv_contents").get("data").getAsString();
			List<JsonObject> jsonObjects = readBase64(invData);

			List<JsonObject> hotbar = new ArrayList<>();
			for (int i = 0; i < 9; i++) {
				hotbar.add(jsonObjects.get(i));
			}
			jsonObjects.removeAll(hotbar);
			int hotbarSlot = 0;
			for (JsonObject jsonObject : hotbar) {
				if (jsonObject != null) {
					int drawX = 123 + (hotbarSlot * 18);
					ItemStack itemStack = NotEnoughUpdates.INSTANCE.manager.jsonToStack(jsonObject);

					if ((mouseX >= guiLeft + drawX && mouseX <= guiLeft + drawX + 16) &&
						(mouseY >= guiTop + 154 && mouseY <= guiTop + 154 + 16)) {
						getInstance().tooltipToDisplay =
							itemStack.getTooltip(
								Minecraft.getMinecraft().thePlayer,
								Minecraft.getMinecraft().gameSettings.advancedItemTooltips
							);
					}

					renderItem(itemStack, drawX, 154, guiLeft, guiTop);
				}
				hotbarSlot++;

			}

			int row = 1;
			int slot = 0;
			for (int i = 0; i < jsonObjects.size(); i++) {
				JsonObject jsonObject = jsonObjects.get(i);
				if (i % 9 == 0 && i > 0) {
					slot = 0;
					row++;
				}

				if (jsonObject != null) {
					ItemStack itemStack = NotEnoughUpdates.INSTANCE.manager.jsonToStack(jsonObject);
					int x1 = (inventoryX - guiLeft) + (slot * 18) + 8;
					int y1 = (inventoryY - guiTop) + (row * 18);
					if ((mouseX >= guiLeft + x1 && mouseX <= guiLeft + x1 + 16) &&
						(mouseY >= guiTop + y1 && mouseY <= guiTop + y1 + 16)) {
						getInstance().tooltipToDisplay =
							itemStack.getTooltip(
								Minecraft.getMinecraft().thePlayer,
								Minecraft.getMinecraft().gameSettings.advancedItemTooltips
							);
					}
					renderItem(itemStack, x1, y1, guiLeft, guiTop);
				}
				slot++;
			}
		}
	}

	private void drawArrows(int page, int maxPages, int x, int y) {
		ArrowPagesUtils.onDraw(guiLeft, guiTop, new int[]{x, y}, page, maxPages);
	}

	@Override
	public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		if ((mouseX >= guiLeft + 156 - 1 && mouseX <= guiLeft + 156 + 20) &&
			(mouseY >= guiTop + 16 && mouseY <= guiTop + 16 + 20)) {
			inInventory = true;
			Utils.playPressSound();
		}
		ArrowPagesUtils.onPageSwitchMouse(
			guiLeft,
			guiTop,
			new int[]{190, 77},
			onPage,
			pages,
			pageChange -> {
				onPage = pageChange;
			}
		);

		if ((mouseX >= guiLeft + 222 - 1 && mouseX <= guiLeft + 222 + 20) &&
			(mouseY >= guiTop + 16 && mouseY <= guiTop + 16 + 20)) {
			inInventory = false;
			Utils.playPressSound();
		}
		return false;
	}

	@Override
	public void keyTyped(char typedChar, int keyCode) throws IOException {
		super.keyTyped(typedChar, keyCode);

		if (getInstance().playerNameTextField.getFocus()) return;
		switch (keyCode) {
			case Keyboard.KEY_1:
				inInventory = true;
				Utils.playPressSound();
				break;
			case Keyboard.KEY_2:
				inInventory = false;
				Utils.playPressSound();
				break;
		}
	}

	public void addInventoryButton(
		int x,
		int y,
		int guiLeft,
		int guiTop,
		int mouseX,
		int mouseY,
		String title,
		String internalNameForItem
	) {
		Minecraft.getMinecraft().getTextureManager().bindTexture(GuiProfileViewer.pv_elements);
		if (internalNameForItem.equals("CHEST") && inInventory) {
			Utils.drawTexturedRect(guiLeft + x, guiTop + y, 20, 20, 20 / 256f, 0, 20 / 256f, 0, GL11.GL_NEAREST);
		} else if (internalNameForItem.equals("ENDER_CHEST") && !inInventory) {
			Utils.drawTexturedRect(guiLeft + x, guiTop + y, 20, 20, 20 / 256f, 0, 20 / 256f, 0, GL11.GL_NEAREST);
		} else {
			// should never happen
			Utils.drawTexturedRect(guiLeft + x, guiTop + y, 20, 20, 0, 20 / 256f, 0, 20 / 256f, GL11.GL_NEAREST);
		}
		renderItem(internalNameForItem, x + 2, y + 2, guiLeft, guiTop);

		if ((mouseX >= guiLeft + x - 1 && mouseX <= guiLeft + x + 20) &&
			(mouseY >= guiTop + y && mouseY <= guiTop + y + 20)) {
			getInstance().tooltipToDisplay = Collections.singletonList(title);
		}
	}

	public void renderItem(String internalName, int x, int y, int guiLeft, int guiTop) {
		ItemStack itemStack = NotEnoughUpdates.INSTANCE.manager.createItemResolutionQuery().withKnownInternalName(
			internalName).resolveToItemStack();
		renderItem(itemStack, x, y, guiLeft, guiTop);
	}

	public void renderItem(ItemStack itemStack, int x, int y, int guiLeft, int guiTop) {
		GlStateManager.disableLighting();
		RenderHelper.enableGUIStandardItemLighting();
		Utils.drawItemStack(itemStack, guiLeft + x, guiTop + y);
		GlStateManager.enableLighting();
		RenderHelper.disableStandardItemLighting();
	}

	public void drawArmorAndEquipment(
		List<JsonObject> jsonObjects,
		int guiLeft,
		int guiTop,
		int x,
		int y,
		int mouseX,
		int mouseY,
		boolean reverse
	) {

		ItemStack[] itemStacks = new ItemStack[4];
		if (reverse) Collections.reverse(jsonObjects); // is this intensive??
		for (int i = 0; i < jsonObjects.size(); i++) {
			JsonObject json = jsonObjects.get(i);
			if (json == null) continue;
			ItemStack itemStack = NotEnoughUpdates.INSTANCE.manager.jsonToStack(json, false);
			if (itemStack == null) itemStack = fillerStack;
			itemStacks[i] = itemStack;
		}

		for (int i = 0; i < itemStacks.length; i++) {
			ItemStack stack = itemStacks[i];
			if (stack == null) continue;
			Utils.drawItemStack(stack, guiLeft + x, guiTop + y + (i * 18), true);
			if (stack == fillerStack) continue;
			if ((mouseX >= guiLeft + x - 1 && mouseX <= guiLeft + x + 16 + 1) &&
				(mouseY >= guiTop + y + (i * 18) && mouseY <= guiTop + y + (i * 18) + 16)) {
				getInstance().tooltipToDisplay =
					stack.getTooltip(
						Minecraft.getMinecraft().thePlayer,
						Minecraft.getMinecraft().gameSettings.advancedItemTooltips
					);
			}
		}
	}

	public List<JsonObject> readBase64(String data) {
		List<JsonObject> itemStacks = new ArrayList<>();
		try {
			NBTTagList items = CompressedStreamTools.readCompressed(
				new ByteArrayInputStream(Base64.getDecoder().decode(data))
			).getTagList("i", 10);
			for (int j = 0; j < items.tagCount(); j++) {
				JsonObject item = NotEnoughUpdates.INSTANCE.manager.getJsonFromNBTEntry(items.getCompoundTagAt(j));
				itemStacks.add(item);
			}
		} catch (IOException ignored) {
		}
		return itemStacks;
	}

	public void drawErrorMessage() {
		String message = EnumChatFormatting.RED + "No Rift data available!";
		Utils.drawStringCentered(message, guiLeft + 431 / 2f, guiTop + 101, true, 0);
	}
}
