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
import io.github.moulberry.notenoughupdates.NEUManager;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.autosubscribe.NEUAutoSubscribe;
import io.github.moulberry.notenoughupdates.events.ButtonExclusionZoneEvent;
import io.github.moulberry.notenoughupdates.events.GuiInventoryBackgroundDrawnEvent;
import io.github.moulberry.notenoughupdates.miscfeatures.PetInfoOverlay;
import io.github.moulberry.notenoughupdates.miscgui.GuiInvButtonEditor;
import io.github.moulberry.notenoughupdates.mixins.AccessorGuiContainer;
import io.github.moulberry.notenoughupdates.options.NEUConfig;
import io.github.moulberry.notenoughupdates.util.ItemUtils;
import io.github.moulberry.notenoughupdates.util.Rectangle;
import io.github.moulberry.notenoughupdates.util.SBInfo;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@NEUAutoSubscribe
public class EquipmentOverlay {
	public static EquipmentOverlay INSTANCE = new EquipmentOverlay();

	// <editor-fold desc="resources">
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
	//</editor-fold>

	//<editor-fold desc="dynamic resources">
	public ResourceLocation getCustomEquipmentTexture(boolean isPetRendering) {
		switch (NotEnoughUpdates.INSTANCE.config.customArmour.colourStyle) {
			case 0:
				return ARMOR_DISPLAY;
			case 1:
				return ARMOR_DISPLAY_GREY;
			case 2:
				return ARMOR_DISPLAY_DARK;
			case 3:
				return NotEnoughUpdates.INSTANCE.config.petOverlay.colourStyle == 3 && isPetRendering
					? ARMOR_DISPLAY_TRANSPARENT_PET
					: ARMOR_DISPLAY_TRANSPARENT;
			case 4:
				return ARMOR_DISPLAY_FSR;
		}
		return null;
	}

	public ResourceLocation getCustomPetTexture(boolean isArmorRendering) {
		switch (NotEnoughUpdates.INSTANCE.config.petOverlay.colourStyle) {
			case 0:
				return isArmorRendering ? PET_ARMOR_DISPLAY : PET_DISPLAY;
			case 1:
				return isArmorRendering ? PET_ARMOR_DISPLAY_GREY : PET_DISPLAY_GREY;
			case 2:
				return isArmorRendering ? PET_ARMOR_DISPLAY_DARK : PET_DISPLAY_DARK;
			case 3:
				return isArmorRendering ? PET_ARMOR_DISPLAY_TRANSPARENT : PET_DISPLAY_TRANSPARENT;
			case 4:
				return isArmorRendering ? PET_ARMOR_DISPLAY_FSR : PET_DISPLAY_FSR;
		}
		return null;
	}
	//</editor-fold>

	//<editor-fold desc="pixel constants">
	public static final int EQUIPMENT_SLOT_OFFSET_Y = 8;
	public static final int ARMOR_OVERLAY_OVERHAND_WIDTH = 24;
	public static final int ARMOR_OVERLAY_HEIGHT = 86;
	public static final int ARMOR_OVERLAY_WIDTH = 31;
	final static int PET_OVERLAY_HEIGHT = 32;
	final static int PET_OVERLAY_WIDTH = 31;
	public static final int PET_OVERLAY_OFFSET_Y = ARMOR_OVERLAY_HEIGHT - 14 /* overlaying pixels */;
	//</editor-fold>

	public boolean shouldRenderPets;
	public boolean shouldRenderArmorHud;

	public ItemStack petStack;

	private Map<String, Map<Integer, ItemStack>> profileCache = new HashMap<>();

	//<editor-fold desc="events">
	@SubscribeEvent
	public void onButtonExclusionZones(ButtonExclusionZoneEvent event) {
		if (isRenderingArmorHud()) {
			event.blockArea(
				new Rectangle(
					event.getGuiBaseRect().getRight() - 200,
					event.getGuiBaseRect().getTop(),
					50, 84
				),
				ButtonExclusionZoneEvent.PushDirection.TOWARDS_LEFT
			);
		}
		if (isRenderingPetHud()) {
			event.blockArea(
				new Rectangle(
					event.getGuiBaseRect().getRight() - 200,
					event.getGuiBaseRect().getTop() + 60,
					50, 60
				),
				ButtonExclusionZoneEvent.PushDirection.TOWARDS_LEFT
			);
		}
	}

	@SubscribeEvent
	public void onGuiTick(TickEvent.ClientTickEvent event) {
		if (event.phase != TickEvent.Phase.START || event.side != Side.CLIENT) return;
		updateGuiInfo(Minecraft.getMinecraft().currentScreen);
	}

	@SubscribeEvent
	public void onGuiInit(GuiScreenEvent.InitGuiEvent event) {
		updateGuiInfo(event.gui);
	}

	@SubscribeEvent
	public void onRenderGuiPost(GuiInventoryBackgroundDrawnEvent event) {
		if (!(event.getContainer() instanceof GuiInventory)) return;
		GuiInventory inventory = ((GuiInventory) event.getContainer());
		renderGuis(inventory);
	}

	//</editor-fold>

	public void renderGuis(GuiInventory inventory) {
		int width = Utils.peekGuiScale().getScaledWidth();
		int height = Utils.peekGuiScale().getScaledHeight();
		int mouseX = Mouse.getX() * width / Minecraft.getMinecraft().displayWidth;
		int mouseY = height - Mouse.getY() * height / Minecraft.getMinecraft().displayHeight - 1;

		GL11.glColor4f(1F, 1F, 1F, 1F);
		if (shouldRenderArmorHud) {
			renderEquipmentGui(inventory, mouseX, mouseY, width, height);
		}

		if (shouldRenderPets) {
			renderPets(inventory, mouseX, mouseY, width, height);
		}
	}

	public void renderEquipmentGui(GuiInventory guiScreen, int mouseX, int mouseY, int width, int height) {
		AccessorGuiContainer container = ((AccessorGuiContainer) guiScreen);

		int overlayLeft = container.getGuiLeft() - ARMOR_OVERLAY_OVERHAND_WIDTH;
		int overlayTop = container.getGuiTop();

		ResourceLocation equipmentTexture = getCustomEquipmentTexture(shouldRenderPets);
		Minecraft.getMinecraft().getTextureManager().bindTexture(equipmentTexture);

		Utils.drawTexturedRect(overlayLeft, overlayTop, ARMOR_OVERLAY_WIDTH, ARMOR_OVERLAY_HEIGHT, GL11.GL_NEAREST);

		List<String> tooltipToDisplay = new ArrayList<>();
		drawSlot(slot1, overlayLeft + 8, overlayTop + EQUIPMENT_SLOT_OFFSET_Y, mouseX, mouseY, tooltipToDisplay);
		drawSlot(slot2, overlayLeft + 8, overlayTop + EQUIPMENT_SLOT_OFFSET_Y + 18, mouseX, mouseY, tooltipToDisplay);
		drawSlot(slot3, overlayLeft + 8, overlayTop + EQUIPMENT_SLOT_OFFSET_Y + 36, mouseX, mouseY, tooltipToDisplay);
		drawSlot(slot4, overlayLeft + 8, overlayTop + EQUIPMENT_SLOT_OFFSET_Y + 54, mouseX, mouseY, tooltipToDisplay);

		if (slot1 == null) {
			Minecraft.getMinecraft().getTextureManager().bindTexture(QUESTION_MARK);
			GlStateManager.color(1, 1, 1, 1);
			Utils.drawTexturedRect(overlayLeft + 8, overlayTop + EQUIPMENT_SLOT_OFFSET_Y, 16, 16, GL11.GL_NEAREST);

			tooltipToDisplay = Lists.newArrayList(
				EnumChatFormatting.RED + "Warning",
				EnumChatFormatting.GREEN + "You need to open /equipment",
				EnumChatFormatting.GREEN + "to cache your armour"
			);
			if (Utils.isWithinRect(mouseX, mouseY, overlayLeft + 8, overlayTop + 8, 16, 16)
				&& NotEnoughUpdates.INSTANCE.config.customArmour.sendWardrobeCommand
				&& Mouse.getEventButtonState()
				&& Minecraft.getMinecraft().thePlayer.inventory.getItemStack() == null) {
				NotEnoughUpdates.INSTANCE.trySendCommand("/equipment");
			}

		}
		if (tooltipToDisplay.size() > 0 &&
			Utils.isWithinRect(
				mouseX, mouseY,
				overlayLeft, overlayTop,
				ARMOR_OVERLAY_OVERHAND_WIDTH, ARMOR_OVERLAY_HEIGHT
			)) {
			Utils.drawHoveringText(
				tooltipToDisplay,
				mouseX - calculateTooltipXOffset(tooltipToDisplay), mouseY, width, height, -1
			);
		}

	}

	private ItemStack getRepoPetStack() {
		NEUManager manager = NotEnoughUpdates.INSTANCE.manager;
		PetInfoOverlay.Pet currentPet = PetInfoOverlay.getCurrentPet();
		if (currentPet == null) return null;

		ItemStack item = ItemUtils.createPetItemstackFromPetInfo(currentPet);
		item = ItemUtils.petToolTipXPExtendPetOverlay(item);

		if (item != null) {
			return item;
		}
		item = manager.createItem(currentPet.getPetId(true));
		return item;
	}

	private void updateGuiInfo(GuiScreen screen) {
		if (getWardrobeSlot(10) != null) {
			slot1 = getWardrobeSlot(10);
			slot2 = getWardrobeSlot(19);
			slot3 = getWardrobeSlot(28);
			slot4 = getWardrobeSlot(37);
		}

		if ((screen instanceof GuiChest || screen instanceof GuiInventory) &&
			NotEnoughUpdates.INSTANCE.config.petOverlay.petInvDisplay) {
			petStack = getRepoPetStack();
		}
		if ((!(screen instanceof GuiInventory) && !(screen instanceof GuiInvButtonEditor))
			|| !NotEnoughUpdates.INSTANCE.config.misc.hidePotionEffect
			|| !NotEnoughUpdates.INSTANCE.hasSkyblockScoreboard()) {
			shouldRenderPets = shouldRenderArmorHud = false;
			return;
		}
		shouldRenderPets = NotEnoughUpdates.INSTANCE.config.petOverlay.petInvDisplay && petStack != null;
		shouldRenderArmorHud = NotEnoughUpdates.INSTANCE.config.customArmour.enableArmourHud;
	}

	private void drawSlot(ItemStack stack, int x, int y, int mouseX, int mouseY, List<String> tooltip) {
		if (stack == null) return;
		Utils.drawItemStack(stack, x, y, true);
		if (Utils.isWithinRect(mouseX, mouseY, x, y, 16, 16)) {
			List<String> tt = stack.getTooltip(Minecraft.getMinecraft().thePlayer, false);
			if (shouldShowEquipmentTooltip(tt))
				tooltip.addAll(tt);
			if (NotEnoughUpdates.INSTANCE.config.customArmour.sendWardrobeCommand
				&& Mouse.getEventButtonState()) {
				NotEnoughUpdates.INSTANCE.trySendCommand("/equipment");
			}
		}
	}

	public void renderPets(GuiInventory inventory, int mouseX, int mouseY, int width, int height) {
		ItemUtils.getOrCreateTag(petStack).setBoolean(
			"NEUHIDEPETTOOLTIP",
			NotEnoughUpdates.INSTANCE.config.petOverlay.hidePetTooltip
		);
		ItemStack petInfo = petStack;

		ResourceLocation customPetTexture = getCustomPetTexture(isRenderingArmorHud());
		Minecraft.getMinecraft().getTextureManager().bindTexture(customPetTexture);
		GlStateManager.color(1, 1, 1, 1);

		AccessorGuiContainer container = ((AccessorGuiContainer) inventory);

		int overlayLeft = container.getGuiLeft() - ARMOR_OVERLAY_OVERHAND_WIDTH;
		int overlayTop = container.getGuiTop() + PET_OVERLAY_OFFSET_Y;

		Utils.drawTexturedRect(overlayLeft, overlayTop, PET_OVERLAY_WIDTH, PET_OVERLAY_HEIGHT, GL11.GL_NEAREST);
		GlStateManager.bindTexture(0);

		Utils.drawItemStack(petInfo, overlayLeft + 8, overlayTop + 8, true);

		List<String> tooltipToDisplay;
		if (Utils.isWithinRect(mouseX, mouseY, overlayLeft + 8, overlayTop + 8, 16, 16)) {
			if (NotEnoughUpdates.INSTANCE.config.petOverlay.sendPetsCommand
				&& Minecraft.getMinecraft().thePlayer.inventory.getItemStack() == null
				&& Mouse.getEventButtonState()) {
				NotEnoughUpdates.INSTANCE.trySendCommand("/pets");
			}
			tooltipToDisplay = petInfo.getTooltip(Minecraft.getMinecraft().thePlayer, false);
			Utils.drawHoveringText(
				tooltipToDisplay,
				mouseX - calculateTooltipXOffset(tooltipToDisplay),
				mouseY, width, height, -1
			);
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
		}

		NEUConfig.HiddenProfileSpecific profileSpecific = NotEnoughUpdates.INSTANCE.config.getProfileSpecific();
		if (profileSpecific == null) return null;

		profileCache.putIfAbsent(lastProfile, new HashMap<>());
		Map<Integer, ItemStack> cache = profileCache.get(lastProfile);
		if (isInNamedGui("Your Equipment")) {
			ItemStack itemStack = getChestSlotsAsItemStack(armourSlot);
			if (itemStack != null) {
				JsonObject itemToSave = NotEnoughUpdates.INSTANCE.manager.getJsonForItem(itemStack);
				if (!itemToSave.has("internalname")) {
					//would crash without internalName when trying to construct the ItemStack again
					itemToSave.add("internalname", new JsonPrimitive("_"));
				}
				profileSpecific.savedEquipment.put(armourSlot, itemToSave);
				cache.put(armourSlot, itemStack);
				return itemStack;
			}
		} else {
			if (profileSpecific.savedEquipment.containsKey(armourSlot)) {
				if (cache.containsKey(armourSlot)) {
					return cache.get(armourSlot);
				}
				//don't use cache since the internalName is identical in most cases
				JsonObject jsonObject = profileSpecific.savedEquipment.get(armourSlot);
				if (jsonObject != null) {
					ItemStack result = NotEnoughUpdates.INSTANCE.manager.jsonToStack(jsonObject.getAsJsonObject(), false);
					cache.put(armourSlot, result);
					return result;
				}
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
		return INSTANCE.shouldRenderArmorHud;
	}

	public static boolean isRenderingPetHud() {
		return INSTANCE.shouldRenderPets;
	}

	private boolean shouldShowEquipmentTooltip(List<String> toolTip) {
		return !toolTip.get(0).equals("§o§7Empty Equipment Slot§r");
	}

	/**
	 * Calculates the width of the longest String in the tooltip, which can be used to offset the entire tooltip to the left more precisely
	 *
	 * @param tooltipToDisplay tooltip
	 * @return offset to apply
	 */
	private int calculateTooltipXOffset(List<String> tooltipToDisplay) {
		int offset = 0;
		if (tooltipToDisplay != null) {
			for (String line : tooltipToDisplay) {
				int lineWidth = Minecraft.getMinecraft().fontRendererObj.getStringWidth(line);
				if (lineWidth > offset) {
					offset = lineWidth;
				}
			}
		}
		return offset + 20;
	}

	public void renderPreviewArmorHud() {
		if (!NotEnoughUpdates.INSTANCE.config.customArmour.enableArmourHud ||
			!(Minecraft.getMinecraft().currentScreen instanceof GuiInvButtonEditor)) return;
		GuiInvButtonEditor container = (GuiInvButtonEditor) Minecraft.getMinecraft().currentScreen;

		int overlayLeft = container.getGuiLeft() - ARMOR_OVERLAY_OVERHAND_WIDTH;
		int overlayTop = container.getGuiTop();

		ResourceLocation equipmentTexture = getCustomEquipmentTexture(shouldRenderPets);
		Minecraft.getMinecraft().getTextureManager().bindTexture(equipmentTexture);

		Utils.drawTexturedRect(overlayLeft, overlayTop, ARMOR_OVERLAY_WIDTH, ARMOR_OVERLAY_HEIGHT, GL11.GL_NEAREST);
	}

	public void renderPreviewPetInvHud() {
		if (!NotEnoughUpdates.INSTANCE.config.petOverlay.petInvDisplay ||
			!(Minecraft.getMinecraft().currentScreen instanceof GuiInvButtonEditor)) return;
		GuiInvButtonEditor container = (GuiInvButtonEditor) Minecraft.getMinecraft().currentScreen;
		int overlayLeft = container.getGuiLeft() - ARMOR_OVERLAY_OVERHAND_WIDTH;
		int overlayTop = container.getGuiTop() + PET_OVERLAY_OFFSET_Y;

		ResourceLocation petHudTexture = getCustomPetTexture(shouldRenderArmorHud);
		Minecraft.getMinecraft().getTextureManager().bindTexture(petHudTexture);

		Utils.drawTexturedRect(overlayLeft, overlayTop, PET_OVERLAY_WIDTH, PET_OVERLAY_HEIGHT, GL11.GL_NEAREST);
	}

	public ItemStack slot1 = null;
	public ItemStack slot2 = null;
	public ItemStack slot3 = null;
	public ItemStack slot4 = null;
	private String lastProfile;

}
