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

import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.core.config.Position;
import io.github.moulberry.notenoughupdates.miscfeatures.PetInfoOverlay;
import io.github.moulberry.notenoughupdates.options.NEUConfig;
import io.github.moulberry.notenoughupdates.util.SBInfo;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import java.awt.*;
import java.util.List;
import java.util.Objects;

public class EquipmentOverlay {

	private static final ResourceLocation ARMOR_DISPLAY = new ResourceLocation(
		"notenoughupdates:armordisplay/armordisplay.png");
	private static final ResourceLocation ARMOR_DISPLAY_GREY = new ResourceLocation(
		"notenoughupdates:armordisplay/armordisplay_grey.png");
	private static final ResourceLocation ARMOR_DISPLAY_DARK = new ResourceLocation(
		"notenoughupdates:armordisplay/armordisplay_phq_dark.png");
	private static final ResourceLocation ARMOR_DISPLAY_FSR = new ResourceLocation(
		"notenoughupdates:armordisplay/armordisplay_fsr.png");
	private static final ResourceLocation ARMOR_DISPLAY_TRANSPARENT = new ResourceLocation(
		"notenoughupdates:armordisplay/armordisplay_transparent.png");
	private static final ResourceLocation ARMOR_DISPLAY_TRANSPARENT_PET = new ResourceLocation(
		"notenoughupdates:armordisplay/armordisplay_transparent_pet.png");

	private static final ResourceLocation QUESTION_MARK = new ResourceLocation("notenoughupdates:pv_unknown.png");

	private static final ResourceLocation PET_DISPLAY = new ResourceLocation(
		"notenoughupdates:petdisplay/petdisplaysolo.png");
	private static final ResourceLocation PET_DISPLAY_GREY = new ResourceLocation(
		"notenoughupdates:petdisplay/petdisplaysolo_dark.png");
	private static final ResourceLocation PET_DISPLAY_DARK = new ResourceLocation(
		"notenoughupdates:petdisplay/petdisplaysolo_phqdark.png");
	private static final ResourceLocation PET_DISPLAY_FSR = new ResourceLocation(
		"notenoughupdates:petdisplay/petdisplaysolo_fsr.png");
	private static final ResourceLocation PET_DISPLAY_TRANSPARENT = new ResourceLocation(
		"notenoughupdates:petdisplay/petdisplaysolo_transparent.png");

	private static final ResourceLocation PET_ARMOR_DISPLAY = new ResourceLocation(
		"notenoughupdates:petdisplay/petdisplayarmor.png");
	private static final ResourceLocation PET_ARMOR_DISPLAY_GREY = new ResourceLocation(
		"notenoughupdates:petdisplay/petdisplayarmor_dark.png");
	private static final ResourceLocation PET_ARMOR_DISPLAY_DARK = new ResourceLocation(
		"notenoughupdates:petdisplay/petdisplayarmor_phqdark.png");
	private static final ResourceLocation PET_ARMOR_DISPLAY_FSR = new ResourceLocation(
		"notenoughupdates:petdisplay/petdisplayarmor_fsr.png");
	private static final ResourceLocation PET_ARMOR_DISPLAY_TRANSPARENT = new ResourceLocation(
		"notenoughupdates:petdisplay/petdisplayarmor_transparent.png");


	private static boolean renderingPetHud;
	private static boolean renderingArmorHud;
	public static boolean shouldUseCachedPet;
	public static long cachedPetTimer;

	public void renderInfoHuds() {
		FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;

		Utils.resetGuiScale();
		Utils.pushGuiScale(Minecraft.getMinecraft().gameSettings.guiScale);

		int width = Utils.peekGuiScale().getScaledWidth();
		int height = Utils.peekGuiScale().getScaledHeight();
		int mouseX = Mouse.getX() * width / Minecraft.getMinecraft().displayWidth;
		int mouseY = height - Mouse.getY() * height / Minecraft.getMinecraft().displayHeight - 1;
		GuiScreen guiScreen = Minecraft.getMinecraft().currentScreen;

		if (NotEnoughUpdates.INSTANCE.config.customArmour.enableArmourHud &&
			NotEnoughUpdates.INSTANCE.config.misc.hidePotionEffect
			&& NotEnoughUpdates.INSTANCE.hasSkyblockScoreboard()) {
			if (getWardrobeSlot(10) != null) {
				slot1 = getWardrobeSlot(10);
				slot2 = getWardrobeSlot(19);
				slot3 = getWardrobeSlot(28);
				slot4 = getWardrobeSlot(37);
			}
			if (guiScreen instanceof GuiInventory) {
				renderingArmorHud = true;

				List<String> tooltipToDisplay = null;
				if (NotEnoughUpdates.INSTANCE.config.customArmour.colourStyle == 0) {
					Minecraft.getMinecraft().getTextureManager().bindTexture(ARMOR_DISPLAY);
				}
				if (NotEnoughUpdates.INSTANCE.config.customArmour.colourStyle == 1) {
					Minecraft.getMinecraft().getTextureManager().bindTexture(ARMOR_DISPLAY_GREY);
				}
				if (NotEnoughUpdates.INSTANCE.config.customArmour.colourStyle == 2) {
					Minecraft.getMinecraft().getTextureManager().bindTexture(ARMOR_DISPLAY_DARK);
				}
				if (NotEnoughUpdates.INSTANCE.config.customArmour.colourStyle == 3) {
					if (NotEnoughUpdates.INSTANCE.config.petOverlay.colourStyle == 3 &&
						NotEnoughUpdates.INSTANCE.config.petOverlay.petInvDisplay && petSlot != null) {
						Minecraft.getMinecraft().getTextureManager().bindTexture(ARMOR_DISPLAY_TRANSPARENT_PET);
					} else {
						Minecraft.getMinecraft().getTextureManager().bindTexture(ARMOR_DISPLAY_TRANSPARENT);
					}
				}
				if (NotEnoughUpdates.INSTANCE.config.customArmour.colourStyle == 4) {
					Minecraft.getMinecraft().getTextureManager().bindTexture(ARMOR_DISPLAY_FSR);
				}

				GlStateManager.color(1, 1, 1, 1);
				GL11.glTranslatef(0, 0, 401);
				float yNumber = (float) (height - 167) / 2f;
				Utils.drawTexturedRect((float) ((width - 224.1) / 2f), yNumber, 31, 86, GL11.GL_NEAREST);
				GlStateManager.bindTexture(0);

				Utils.drawItemStack(slot1, (int) ((width - 208) / 2f), (int) ((height + 60) / 2f - 105), true);
				Utils.drawItemStack(slot2, (int) ((width - 208) / 2f), (int) ((height + 60) / 2f - 105) + 18, true);
				Utils.drawItemStack(slot3, (int) ((width - 208) / 2f), (int) ((height + 60) / 2f - 105) + 36, true);
				Utils.drawItemStack(slot4, (int) ((width - 208) / 2f), (int) ((height + 60) / 2f - 105) + 54, true);
				if (slot1 == null) {
					Minecraft.getMinecraft().getTextureManager().bindTexture(QUESTION_MARK);
					GlStateManager.color(1, 1, 1, 1);
					Utils.drawTexturedRect(((width - 208) / 2f), ((height + 60) / 2f - 105), 16, 16, GL11.GL_NEAREST);
					GlStateManager.bindTexture(0);

					tooltipToDisplay = Lists.newArrayList(
						EnumChatFormatting.RED + "Warning",
						EnumChatFormatting.GREEN + "You need to open /equipment",
						EnumChatFormatting.GREEN + "To cache your armour"
					);
					if (mouseX >= ((width - 208) / 2f) && mouseX < ((width - 208) / 2f) + 16) {
						if (mouseY >= ((height + 60) / 2f - 105) && mouseY <= ((height + 60) / 2f - 105) + 70 &&
							NotEnoughUpdates.INSTANCE.config.customArmour.sendWardrobeCommand) {
							if (Minecraft.getMinecraft().thePlayer.inventory.getItemStack() == null) {
								if (Mouse.getEventButtonState()) {
									if (ClientCommandHandler.instance.executeCommand(Minecraft.getMinecraft().thePlayer, "/equipment") ==
										0) {
										NotEnoughUpdates.INSTANCE.sendChatMessage("/equipment");
									}
								}
							}
						}
						if (mouseY >= ((height + 60) / 2f - 105) && mouseY <= ((height + 60) / 2f - 105) + 16) {
							Utils.drawHoveringText(tooltipToDisplay, mouseX, mouseY, width, height, -1, fr);
						}
					}
					GL11.glTranslatef(0, 0, -401);
				}
				if (slot1 != null && slot2 != null && slot3 != null && slot4 != null) {
					if (mouseX >= ((width - 208) / 2f) && mouseX < ((width - 208) / 2f) + 16) {
						if (mouseY >= ((height + 60) / 2f - 105) && mouseY <= ((height + 60) / 2f - 105) + 70 &&
							NotEnoughUpdates.INSTANCE.config.customArmour.sendWardrobeCommand) {
							if (Minecraft.getMinecraft().thePlayer.inventory.getItemStack() == null) {
								if (Mouse.getEventButtonState()) {
									if (ClientCommandHandler.instance.executeCommand(Minecraft.getMinecraft().thePlayer, "/equipment") ==
										0) {
										NotEnoughUpdates.INSTANCE.sendChatMessage("/equipment");
									}
								}
							}
						}

						if (mouseY >= ((height + 60) / 2f - 105) && mouseY <= ((height + 60) / 2f - 105) + 16) {
							tooltipToDisplay = slot1.getTooltip(Minecraft.getMinecraft().thePlayer, false);
							if (shouldShowEquipmentTooltip(tooltipToDisplay)) {
								Utils.drawHoveringText(
									tooltipToDisplay,
									mouseX - calculateTooltipXOffset(tooltipToDisplay, fr),
									mouseY,
									width,
									height,
									-1,
									fr
								);
							}
						}
						if (mouseY >= ((height + 60) / 2f - 105) + 18 && mouseY <= ((height + 60) / 2f - 105) + 34) {
							tooltipToDisplay = slot2.getTooltip(Minecraft.getMinecraft().thePlayer, false);
							if (shouldShowEquipmentTooltip(tooltipToDisplay)) {
								Utils.drawHoveringText(
									tooltipToDisplay,
									mouseX - calculateTooltipXOffset(tooltipToDisplay, fr),
									mouseY,
									width,
									height,
									-1,
									fr
								);
							}
						}
						if (mouseY >= ((height + 60) / 2f - 105) + 36 && mouseY <= ((height + 60) / 2f - 105) + 52) {
							tooltipToDisplay = slot3.getTooltip(Minecraft.getMinecraft().thePlayer, false);
							if (shouldShowEquipmentTooltip(tooltipToDisplay)) {
								Utils.drawHoveringText(
									tooltipToDisplay,
									mouseX - calculateTooltipXOffset(tooltipToDisplay, fr),
									mouseY,
									width,
									height,
									-1,
									fr
								);
							}
						}
						if (mouseY >= ((height + 60) / 2f - 105) + 54 && mouseY <= ((height + 60) / 2f - 105) + 70) {
							tooltipToDisplay = slot4.getTooltip(Minecraft.getMinecraft().thePlayer, false);
							if (shouldShowEquipmentTooltip(tooltipToDisplay)) {
								Utils.drawHoveringText(
									tooltipToDisplay,
									mouseX - calculateTooltipXOffset(tooltipToDisplay, fr),
									mouseY,
									width,
									height,
									-1,
									fr
								);
							}
						}
					}
					GL11.glTranslatef(0, 0, -401);
				}
			}
		}
		if (PetInfoOverlay.getCurrentPet() != null) {
			if (NotEnoughUpdates.INSTANCE.config.petOverlay.petInvDisplay
				&& (NotEnoughUpdates.INSTANCE.manager
				.jsonToStack(NotEnoughUpdates.INSTANCE.manager
					.getItemInformation()
					.get(PetInfoOverlay.getCurrentPet().petType + ";" + PetInfoOverlay.getCurrentPet().rarity.petId))
				.hasDisplayName()
				|| NotEnoughUpdates.INSTANCE.manager
				.jsonToStack(NotEnoughUpdates.INSTANCE.manager
					.getItemInformation()
					.get(PetInfoOverlay.getCurrentPet().petType + ";" + (PetInfoOverlay.getCurrentPet().rarity.petId - 1)))
				.hasDisplayName())
				&& NotEnoughUpdates.INSTANCE.config.misc.hidePotionEffect &&
				NotEnoughUpdates.INSTANCE.hasSkyblockScoreboard()) {
				if (!NotEnoughUpdates.INSTANCE.manager
					.jsonToStack(
						NotEnoughUpdates.INSTANCE.manager
							.getItemInformation()
							.get(PetInfoOverlay.getCurrentPet().petType + ";" + PetInfoOverlay.getCurrentPet().rarity.petId))
					.hasDisplayName()) {
					petSlot = NotEnoughUpdates.INSTANCE.manager.jsonToStack(
						NotEnoughUpdates.INSTANCE.manager.getItemInformation().get(
							PetInfoOverlay.getCurrentPet().petType + ";" + (PetInfoOverlay.getCurrentPet().rarity.petId - 1)));
				} else {
					petSlot = NotEnoughUpdates.INSTANCE.manager.jsonToStack(
						NotEnoughUpdates.INSTANCE.manager.getItemInformation().get(
							PetInfoOverlay.getCurrentPet().petType + ";" + PetInfoOverlay.getCurrentPet().rarity.petId));
				}
				if (petSlot == null) {
					return;
				}
				petSlot.getTagCompound().setBoolean(
					"NEUHIDEPETTOOLTIP",
					NotEnoughUpdates.INSTANCE.config.petOverlay.hidePetTooltip
				);
				ItemStack petInfo = petSlot;

				if (guiScreen instanceof GuiInventory) {
					GL11.glTranslatef(0, 0, 401);
					if (!NotEnoughUpdates.INSTANCE.config.customArmour.enableArmourHud) {
						if (NotEnoughUpdates.INSTANCE.config.petOverlay.colourStyle == 0) {
							Minecraft.getMinecraft().getTextureManager().bindTexture(PET_DISPLAY);
						}
						if (NotEnoughUpdates.INSTANCE.config.petOverlay.colourStyle == 1) {
							Minecraft.getMinecraft().getTextureManager().bindTexture(PET_DISPLAY_GREY);
						}
						if (NotEnoughUpdates.INSTANCE.config.petOverlay.colourStyle == 2) {
							Minecraft.getMinecraft().getTextureManager().bindTexture(PET_DISPLAY_DARK);
						}
						if (NotEnoughUpdates.INSTANCE.config.petOverlay.colourStyle == 3) {
							Minecraft.getMinecraft().getTextureManager().bindTexture(PET_DISPLAY_TRANSPARENT);
						}
						if (NotEnoughUpdates.INSTANCE.config.petOverlay.colourStyle == 4) {
							Minecraft.getMinecraft().getTextureManager().bindTexture(PET_DISPLAY_FSR);
						}
					} else {
						if (NotEnoughUpdates.INSTANCE.config.petOverlay.colourStyle == 0) {
							Minecraft.getMinecraft().getTextureManager().bindTexture(PET_ARMOR_DISPLAY);
						}
						if (NotEnoughUpdates.INSTANCE.config.petOverlay.colourStyle == 1) {
							Minecraft.getMinecraft().getTextureManager().bindTexture(PET_ARMOR_DISPLAY_GREY);
						}
						if (NotEnoughUpdates.INSTANCE.config.petOverlay.colourStyle == 2) {
							Minecraft.getMinecraft().getTextureManager().bindTexture(PET_ARMOR_DISPLAY_DARK);
						}
						if (NotEnoughUpdates.INSTANCE.config.petOverlay.colourStyle == 3) {
							Minecraft.getMinecraft().getTextureManager().bindTexture(PET_ARMOR_DISPLAY_TRANSPARENT);
						}
						if (NotEnoughUpdates.INSTANCE.config.petOverlay.colourStyle == 4) {
							Minecraft.getMinecraft().getTextureManager().bindTexture(PET_ARMOR_DISPLAY_FSR);
						}
					}

					GlStateManager.color(1, 1, 1, 1);
					float yNumber = (float) (height - 23) / 2f;
					Utils.drawTexturedRect((float) ((width - 224.1) / 2f), yNumber, 31, 32, GL11.GL_NEAREST);
					GlStateManager.bindTexture(0);

					Utils.drawItemStack(petInfo, (int) ((width - 208) / 2f), (int) ((height + 60) / 2f - 105) + 72, true);
					renderingPetHud = true;

					List<String> tooltipToDisplay = null;
					if (petInfo != null) {
						if (mouseX >= ((width - 208) / 2f) && mouseX < ((width - 208) / 2f) + 16) {
							if (mouseY >= ((height + 60) / 2f - 105) + 72 && mouseY <= ((height + 60) / 2f - 105) + 88 &&
								NotEnoughUpdates.INSTANCE.config.petOverlay.sendPetsCommand) {
								if (Minecraft.getMinecraft().thePlayer.inventory.getItemStack() == null) {
									if (Mouse.getEventButtonState()) {
										if (ClientCommandHandler.instance.executeCommand(Minecraft.getMinecraft().thePlayer, "/pets") ==
											0) {
											NotEnoughUpdates.INSTANCE.sendChatMessage("/pets");
										}
									}
								}
								tooltipToDisplay = petInfo.getTooltip(Minecraft.getMinecraft().thePlayer, false);
								Utils.drawHoveringText(
									tooltipToDisplay,
									mouseX - calculateTooltipXOffset(tooltipToDisplay, fr),
									mouseY,
									width,
									height,
									-1,
									fr
								);
								GL11.glTranslatef(0, 0, -80);
							}
						}

					}
				}
			}
		}
	}

	private ItemStack getWardrobeSlot(int armourSlot) {
		if (SBInfo.getInstance().currentProfile == null) {
			return null;
		}

		if (!Objects.equals(SBInfo.getInstance().currentProfile, lastProfile)) {
			lastProfile = SBInfo.getInstance().currentProfile;
			slot1 = null;
			slot2 = null;
			slot3 = null;
			slot4 = null;
			petSlot = null;
		}

		NEUConfig.HiddenProfileSpecific profileSpecific = NotEnoughUpdates.INSTANCE.config.getProfileSpecific();
		if(profileSpecific == null)return null;

		if (isInNamedGui("Your Equipment")) {
			ItemStack itemStack = getChestSlotsAsItemStack(armourSlot);
			if (itemStack != null) {
				JsonObject itemToSave = NotEnoughUpdates.INSTANCE.manager.getJsonForItem(itemStack);
				if (!itemToSave.has("internalname")) {
					//would crash without internalName when trying to construct the ItemStack again
					itemToSave.add("internalname", new JsonPrimitive("_"));
				}
				profileSpecific.savedEquipment.put(armourSlot, itemToSave);
				return itemStack;
			}
		} else {
			if (profileSpecific.savedEquipment.containsKey(armourSlot)) {
				//don't use cache since the internalName is identical in most cases
				return NotEnoughUpdates.INSTANCE.manager.jsonToStack(profileSpecific.savedEquipment
					.get(armourSlot)
					.getAsJsonObject(), false);
			}
		}
		return null;
	}

	private boolean wardrobeOpen = false;

	private boolean isInNamedGui(String guiName) {
		GuiScreen guiScreen = Minecraft.getMinecraft().currentScreen;
		if (guiScreen instanceof GuiChest) {
			GuiChest chest = (GuiChest) Minecraft.getMinecraft().currentScreen;
			ContainerChest container = (ContainerChest) chest.inventorySlots;
			IInventory lower = container.getLowerChestInventory();
			String containerName = lower.getDisplayName().getUnformattedText();
			wardrobeOpen = containerName.contains(guiName);
		}
		if (guiScreen instanceof GuiInventory) {
			wardrobeOpen = false;
		}
		return wardrobeOpen;
	}

	private ItemStack getChestSlotsAsItemStack(int slot) {
		GuiScreen guiScreen = Minecraft.getMinecraft().currentScreen;
		if (guiScreen instanceof GuiChest) {
			GuiChest chest = (GuiChest) Minecraft.getMinecraft().currentScreen;
			return chest.inventorySlots.getSlot(slot).getStack();
		} else {
			return null;
		}
	}

	public static boolean isRenderingArmorHud() {
		return renderingArmorHud;
	}

	public static boolean isRenderingPetHud() {
		return renderingPetHud;
	}
	private boolean shouldShowEquipmentTooltip(List<String> toolTip) {
		return !toolTip.get(0).equals("§o§7Empty Equipment Slot§r");
	}

	/**
	 * Calculates the width of the longest String in the tooltip, which can be used to offset the entire tooltip to the left more precisely
	 *
	 * @param tooltipToDisplay tooltip
	 * @param fr               FontRenderer object
	 * @return offset to apply
	 */
	private int calculateTooltipXOffset(List<String> tooltipToDisplay, FontRenderer fr) {
		int offset = 0;
		if (tooltipToDisplay != null) {
			for (String line : tooltipToDisplay) {
				int lineWidth = fr.getStringWidth(line);
				if (lineWidth > offset) {
					offset = lineWidth;
				}
			}
		}
		return offset + 20;
	}


	public ItemStack slot1 = null;
	public ItemStack slot2 = null;
	public ItemStack slot3 = null;
	public ItemStack slot4 = null;
	public ItemStack petSlot = null;
	private String lastProfile;

}
