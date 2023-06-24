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

package io.github.moulberry.notenoughupdates.profileviewer;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.moulberry.notenoughupdates.core.util.StringUtils;
import io.github.moulberry.notenoughupdates.miscfeatures.PetInfoOverlay;
import io.github.moulberry.notenoughupdates.util.Constants;
import io.github.moulberry.notenoughupdates.util.ItemUtils;
import io.github.moulberry.notenoughupdates.util.PetLeveling;
import io.github.moulberry.notenoughupdates.util.SBInfo;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PetsPage extends GuiProfileViewerPage {

	public static final ResourceLocation pv_pets = new ResourceLocation("notenoughupdates:pv_pets.png");
	private static final int COLLS_XCOUNT = 5;
	private static final int COLLS_YCOUNT = 4;
	private static final float COLLS_XPADDING = (190 - COLLS_XCOUNT * 20) / (float) (COLLS_XCOUNT + 1);
	private static final float COLLS_YPADDING = (202 - COLLS_YCOUNT * 20) / (float) (COLLS_YCOUNT + 1);
	private List<JsonObject> sortedPets = null;
	private List<ItemStack> sortedPetsStack = null;
	private int selectedPet = -1;
	private int petsPage = 0;

	public PetsPage(GuiProfileViewer instance) {
		super(instance);
	}

	@Override
	public void drawPage(int mouseX, int mouseY, float partialTicks) {
		int guiLeft = GuiProfileViewer.getGuiLeft();
		int guiTop = GuiProfileViewer.getGuiTop();

		JsonObject petsJson = Constants.PETS;
		if (petsJson == null) {
			return;
		}

		SkyblockProfiles.SkyblockProfile selectedProfile = getSelectedProfile();
		if (selectedProfile == null) {
			return;
		}

		JsonObject petsInfo = selectedProfile.getPetsInfo();
		if (petsInfo == null) {
			return;
		}

		String location = null;
		JsonObject status = GuiProfileViewer.getProfile().getPlayerStatus();
		if (status != null && status.has("mode")) {
			location = status.get("mode").getAsString();
		}

		getInstance().backgroundRotation += (getInstance().currentTime - getInstance().lastTime) / 400f;
		getInstance().backgroundRotation %= 360;

		String panoramaIdentifier = "day";
		if (SBInfo.getInstance().currentTimeDate != null) {
			if (SBInfo.getInstance().currentTimeDate.getHours() <= 6 ||
				SBInfo.getInstance().currentTimeDate.getHours() >= 20) {
				panoramaIdentifier = "night";
			}
		}

		JsonArray pets = petsInfo.get("pets").getAsJsonArray();
		if (sortedPets == null) {
			sortedPets = new ArrayList<>();
			sortedPetsStack = new ArrayList<>();
			for (int i = 0; i < pets.size(); i++) {
				sortedPets.add(pets.get(i).getAsJsonObject());
			}
			sortedPets.sort((pet1, pet2) -> {
				String tier1 = pet1.get("tier").getAsString();
				String tierNum1 = GuiProfileViewer.RARITY_TO_NUM.get(tier1);
				if (tierNum1 == null) return 1;
				int tierNum1I = Integer.parseInt(tierNum1);
				float exp1 = pet1.get("exp").getAsFloat();

				String tier2 = pet2.get("tier").getAsString();
				String tierNum2 = GuiProfileViewer.RARITY_TO_NUM.get(tier2);
				if (tierNum2 == null) return -1;
				int tierNum2I = Integer.parseInt(tierNum2);
				float exp2 = pet2.get("exp").getAsFloat();

				if (tierNum1I != tierNum2I) {
					return tierNum2I - tierNum1I;
				} else {
					return (int) (exp2 - exp1);
				}
			});
			for (JsonObject pet : sortedPets) {
				PetInfoOverlay.Pet parsedPet = new PetInfoOverlay.Pet();
				parsedPet.petType = pet.get("type").getAsString();
				parsedPet.rarity = PetInfoOverlay.Rarity.valueOf(pet.get("tier").getAsString());
				parsedPet.petLevel = PetLeveling.getPetLevelingForPet(
					parsedPet.petType,
					parsedPet.rarity
				).getPetLevel(pet.get("exp").getAsFloat());
				parsedPet.petXpType = "unknown";
				parsedPet.petItem = Utils.getElementAsString(pet.get("heldItem"), null);
				parsedPet.skin = Utils.getElementAsString(pet.get("skin"), null);
				parsedPet.candyUsed = pet.get("candyUsed").getAsInt();
				sortedPetsStack.add(ItemUtils.createPetItemstackFromPetInfo(parsedPet));
				pet.addProperty("level", parsedPet.petLevel.getCurrentLevel());
				pet.addProperty("currentLevelRequirement", parsedPet.petLevel.getExpRequiredForNextLevel());
				pet.addProperty("maxXP", parsedPet.petLevel.getExpRequiredForMaxLevel());
			}
		}

		Panorama.drawPanorama(
			-getInstance().backgroundRotation,
			guiLeft + 212,
			guiTop + 44,
			81,
			108,
			-0.37f,
			0.6f,
			Panorama.getPanoramasForLocation(location == null ? "dynamic" : location, panoramaIdentifier)
		);

		Minecraft.getMinecraft().getTextureManager().bindTexture(pv_pets);
		Utils.drawTexturedRect(guiLeft, guiTop, getInstance().sizeX, getInstance().sizeY, GL11.GL_NEAREST);

		Utils.drawStringCentered(EnumChatFormatting.DARK_PURPLE + "Pets", guiLeft + 100, guiTop + 14, true, 4210752);
		GlStateManager.color(1, 1, 1, 1);

		JsonElement activePetElement = petsInfo.get("active_pet");
		if (selectedPet == -1 && activePetElement != null && activePetElement.isJsonObject()) {
			JsonObject active = activePetElement.getAsJsonObject();
			for (int i = 0; i < sortedPets.size(); i++) {
				if (sortedPets.get(i) == active) {
					selectedPet = i;
					break;
				}
			}
		}

		boolean leftHovered = false;
		boolean rightHovered = false;
		if (Mouse.isButtonDown(0)) {
			if (mouseY > guiTop + 6 && mouseY < guiTop + 22) {
				if (mouseX > guiLeft + 100 - 20 - 12 && mouseX < guiLeft + 100 - 20) {
					leftHovered = true;
				} else if (mouseX > guiLeft + 100 + 20 && mouseX < guiLeft + 100 + 20 + 12) {
					rightHovered = true;
				}
			}
		}
		Minecraft.getMinecraft().getTextureManager().bindTexture(GuiProfileViewer.resource_packs);

		if (petsPage > 0) {
			Utils.drawTexturedRect(
				guiLeft + 100 - 15 - 12,
				guiTop + 6,
				12,
				16,
				29 / 256f,
				53 / 256f,
				!leftHovered ? 0 : 32 / 256f,
				!leftHovered ? 32 / 256f : 64 / 256f,
				GL11.GL_NEAREST
			);
		}
		if (petsPage < Math.ceil(pets.size() / 20f) - 1) {
			Utils.drawTexturedRect(
				guiLeft + 100 + 15,
				guiTop + 6,
				12,
				16,
				5 / 256f,
				29 / 256f,
				!rightHovered ? 0 : 32 / 256f,
				!rightHovered ? 32 / 256f : 64 / 256f,
				GL11.GL_NEAREST
			);
		}

		for (
			int i = petsPage * 20;
			i < Math.min(petsPage * 20 + 20, Math.min(sortedPetsStack.size(), sortedPets.size()));
			i++
		) {
			JsonObject pet = sortedPets.get(i);
			ItemStack stack = sortedPetsStack.get(i);

			if (pet != null) {
				{
					NBTTagCompound tag = stack.getTagCompound();
					tag.setBoolean("DisablePetExp", true);
					stack.setTagCompound(tag);
				}
				int xIndex = (i % 20) % COLLS_XCOUNT;
				int yIndex = (i % 20) / COLLS_XCOUNT;

				float x = 5 + COLLS_XPADDING + (COLLS_XPADDING + 20) * xIndex;
				float y = 7 + COLLS_YPADDING + (COLLS_YPADDING + 20) * yIndex;

				Minecraft.getMinecraft().getTextureManager().bindTexture(GuiProfileViewer.pv_elements);
				if (i == selectedPet) {
					GlStateManager.color(1, 185 / 255f, 0, 1);
				} else {
					GlStateManager.color(1, 1, 1, 1);
				}
				Utils.drawTexturedRect(guiLeft + x, guiTop + y, 20, 20, 0, 20 / 256f, 0, 20 / 256f, GL11.GL_NEAREST);

				Utils.drawItemStack(stack, guiLeft + (int) x + 2, guiTop + (int) y + 2, true);

				if (mouseX > guiLeft + x && mouseX < guiLeft + x + 20) {
					if (mouseY > guiTop + y && mouseY < guiTop + y + 20) {
						getInstance().tooltipToDisplay = stack.getTooltip(Minecraft.getMinecraft().thePlayer, false);
					}
				}
			}
		}

		if (selectedPet >= 0) {
			ItemStack petStack;
			if (sortedPetsStack.size() <= selectedPet) {
				petStack = ItemUtils.createQuestionMarkSkull("§cInvalid pet selection");
			} else {
				petStack = sortedPetsStack.get(selectedPet);
			}
			String display = petStack.getDisplayName();
			JsonObject pet = sortedPets.get(selectedPet);

			int x = guiLeft + 280;
			float y = guiTop + 67 + 15 * (float) Math.sin(
				((getInstance().currentTime - getInstance().startTime) / 800f) % (2 * Math.PI));

			int displayLen = Minecraft.getMinecraft().fontRendererObj.getStringWidth(display);
			int halfDisplayLen = displayLen / 2;

			GlStateManager.pushMatrix();
			GlStateManager.translate(x, y, 0);

			GuiScreen.drawRect(-halfDisplayLen - 1 - 28, -1, halfDisplayLen + 1 - 28, 8, new Color(0, 0, 0, 100).getRGB());

			Minecraft.getMinecraft().fontRendererObj.drawString(display, -halfDisplayLen - 28, 0, 0, true);

			GlStateManager.enableDepth();
			GlStateManager.translate(-55, 0, 0);
			GlStateManager.scale(3.5f, 3.5f, 1);
			Utils.drawItemStack(petStack, 0, 0);
			GlStateManager.popMatrix();

			float level = pet.get("level").getAsFloat();
			float currentLevelRequirement = pet.get("currentLevelRequirement").getAsFloat();
			float exp = pet.get("exp").getAsFloat();
			float maxXP = pet.get("maxXP").getAsFloat();

			String[] split = display.split("] ");
			String colouredName = split[split.length - 1];

			Utils.renderAlignedString(
				colouredName,
				EnumChatFormatting.WHITE + "Level " + (int) Math.floor(level),
				guiLeft + 319,
				guiTop + 28,
				98
			);

			//Utils.drawStringCenteredScaledMaxWidth(, Minecraft.getMinecraft().fontRendererObj, guiLeft+368, guiTop+28+4, true, 98, 0);
			//renderAlignedString(display, EnumChatFormatting.YELLOW+"[LVL "+Math.floor(level)+"]", guiLeft+319, guiTop+28, 98);
			getInstance().renderBar(guiLeft + 319, guiTop + 38, 98, (float) Math.floor(level) / 100f);

			Utils.renderAlignedString(
				EnumChatFormatting.YELLOW + "To Next LVL",
				EnumChatFormatting.WHITE.toString() + (int) (level % 1 * 100) + "%",
				guiLeft + 319,
				guiTop + 46,
				98
			);
			getInstance().renderBar(guiLeft + 319, guiTop + 56, 98, level % 1);

			Utils.renderAlignedString(
				EnumChatFormatting.YELLOW + "To Max LVL",
				EnumChatFormatting.WHITE.toString() + Math.min(100, (int) (exp / maxXP * 100)) + "%",
				guiLeft + 319,
				guiTop + 64,
				98
			);
			getInstance().renderBar(guiLeft + 319, guiTop + 74, 98, exp / maxXP);

			Utils.renderAlignedString(
				EnumChatFormatting.YELLOW + "Total XP",
				EnumChatFormatting.WHITE + StringUtils.shortNumberFormat(exp),
				guiLeft + 319,
				guiTop + 125,
				98
			);
			Utils.renderAlignedString(
				EnumChatFormatting.YELLOW + "Current LVL XP",
				EnumChatFormatting.WHITE + StringUtils.shortNumberFormat((level % 1) * currentLevelRequirement),
				guiLeft + 319,
				guiTop + 143,
				98
			);
			Utils.renderAlignedString(
				EnumChatFormatting.YELLOW + "Required LVL XP",
				EnumChatFormatting.WHITE + StringUtils.shortNumberFormat(currentLevelRequirement),
				guiLeft + 319,
				guiTop + 161,
				98
			);
		}
	}

	@Override
	public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		if (sortedPets == null) return false;
		for (int i = petsPage * 20; i < Math.min(petsPage * 20 + 20, sortedPets.size()); i++) {
			int xIndex = (i % 20) % COLLS_XCOUNT;
			int yIndex = (i % 20) / COLLS_XCOUNT;

			float x = 5 + COLLS_XPADDING + (COLLS_XPADDING + 20) * xIndex;
			float y = 7 + COLLS_YPADDING + (COLLS_YPADDING + 20) * yIndex;

			int guiLeft = GuiProfileViewer.getGuiLeft();
			int guiTop = GuiProfileViewer.getGuiTop();
			if (mouseX > guiLeft + x && mouseX < guiLeft + x + 20) {
				if (mouseY > guiTop + y && mouseY < guiTop + y + 20) {
					selectedPet = i;
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
		int guiLeft = GuiProfileViewer.getGuiLeft();
		int guiTop = GuiProfileViewer.getGuiTop();

		if (mouseY > guiTop + 6 && mouseY < guiTop + 22) {
			if (mouseX > guiLeft + 100 - 15 - 12 && mouseX < guiLeft + 100 - 20) {
				if (petsPage > 0) {
					petsPage--;
				}
			} else if (mouseX > guiLeft + 100 + 15 && mouseX < guiLeft + 100 + 20 + 12) {
				if (sortedPets != null && petsPage < Math.ceil(sortedPets.size() / 20f) - 1) {
					petsPage++;
				}
			}
		}
	}

	@Override
	public void resetCache() {
		petsPage = 0;
		sortedPets = null;
		sortedPetsStack = null;
		selectedPet = -1;
	}
}
