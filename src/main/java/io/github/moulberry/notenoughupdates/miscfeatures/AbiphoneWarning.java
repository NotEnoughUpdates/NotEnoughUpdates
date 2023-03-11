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

package io.github.moulberry.notenoughupdates.miscfeatures;

import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.autosubscribe.NEUAutoSubscribe;
import io.github.moulberry.notenoughupdates.core.GuiElement;
import io.github.moulberry.notenoughupdates.core.util.render.RenderUtils;
import io.github.moulberry.notenoughupdates.core.util.render.TextRenderUtils;
import io.github.moulberry.notenoughupdates.events.SlotClickEvent;
import io.github.moulberry.notenoughupdates.util.ItemUtils;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@NEUAutoSubscribe
public class AbiphoneWarning extends GuiElement {
	private static final AbiphoneWarning INSTANCE = new AbiphoneWarning();

	private boolean showWarning = false;
	private String contactName = null;
	private int contactSlot = -1;

	public static AbiphoneWarning getInstance() {
		return INSTANCE;
	}

	private boolean shouldPerformCheck() {
		if (!NotEnoughUpdates.INSTANCE.hasSkyblockScoreboard()) {
			showWarning = false;
			return false;
		}

		if (Utils.getOpenChestName().startsWith("Abiphone ")) {
			return true;
		} else {
			showWarning = false;
			return false;
		}
	}

	public boolean shouldShow() {
		return shouldPerformCheck() && showWarning;
	}

	@SubscribeEvent
	public void onMouseClick(SlotClickEvent event) {
		if (!shouldPerformCheck()) return;
		if (!NotEnoughUpdates.INSTANCE.config.misc.abiphoneWarning) return;
		if (event.slotId == -999) return;
		if (event.clickedButton == 0) return;

		GuiChest chest = (GuiChest) Minecraft.getMinecraft().currentScreen;

		ItemStack clickedContact = chest.inventorySlots.getSlot(event.slotId).getStack();
		if (clickedContact == null) return;

		List<String> list = ItemUtils.getLore(clickedContact);
		if (list.isEmpty()) return;

		String last = list.get(list.size() - 1);
		if (last.contains("Right-click to remove contact!")) {
			showWarning = true;
			contactName = clickedContact.getDisplayName();
			contactSlot = event.slotId;
			event.setCanceled(true);
		}
	}

	public void overrideIsMouseOverSlot(Slot slot, int mouseX, int mouseY, CallbackInfoReturnable<Boolean> cir) {
		if (shouldShow()) {
			cir.setReturnValue(false);
		}
	}

	@Override
	public void render() {
		final ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
		final int width = scaledResolution.getScaledWidth();
		final int height = scaledResolution.getScaledHeight();

		GlStateManager.disableLighting();

		GlStateManager.pushMatrix();
		GlStateManager.translate(0, 0, 500);

		Gui.drawRect(0, 0, width, height, 0x80000000);

		RenderUtils.drawFloatingRectDark(width / 2 - 90, height / 2 - 45, 180, 90);

		int neuLength = Minecraft.getMinecraft().fontRendererObj.getStringWidth("\u00a7lNEU");
		Minecraft.getMinecraft().fontRendererObj.drawString(
			"\u00a7lNEU",
			width / 2 + 90 - neuLength - 3, height / 2 - 45 + 4, 0xff000000
		);

		TextRenderUtils.drawStringCenteredScaledMaxWidth(
			"Are you SURE?",
			width / 2, height / 2 - 45 + 10, false, 170, 0xffff4040
		);

		String sellLine = "\u00a77[ \u00a7r" + contactName + "\u00a77 ]";

		TextRenderUtils.drawStringCenteredScaledMaxWidth(
			sellLine,
			width / 2, height / 2 - 45 + 25, false, 170, 0xffffffff
		);

		TextRenderUtils.drawStringCenteredScaledMaxWidth(
			"Continue removing this contact?",
			width / 2, height / 2 - 45 + 50, false, 170, 0xffa0a0a0
		);

		RenderUtils.drawFloatingRectDark(width / 2 - 43, height / 2 + 23, 40, 16, false);
		RenderUtils.drawFloatingRectDark(width / 2 + 3, height / 2 + 23, 40, 16, false);

		TextRenderUtils.drawStringCenteredScaledMaxWidth(
			EnumChatFormatting.GREEN + "[Y]es",
			width / 2 - 23, height / 2 + 31, true, 36, 0xff00ff00
		);
		TextRenderUtils.drawStringCenteredScaledMaxWidth(
			EnumChatFormatting.RED + "[N]o",
			width / 2 + 23, height / 2 + 31, true, 36, 0xffff0000
		);

		GlStateManager.popMatrix();
	}

	@Override
	public boolean mouseInput(int mouseX, int mouseY) {
		final ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
		final int width = scaledResolution.getScaledWidth();
		final int height = scaledResolution.getScaledHeight();

		if (Mouse.getEventButtonState()) {
			if (mouseY >= height / 2 + 23 && mouseY <= height / 2 + 23 + 16) {
				if (mouseX >= width / 2 - 43 && mouseX <= width / 2 - 3) {
					makeClick();
				}
				showWarning = false;
			}

			if (mouseX < width / 2 - 90 || mouseX > width / 2 + 90 ||
				mouseY < height / 2 - 45 || mouseY > height / 2 + 45) {
				showWarning = false;
			}
		}

		return false;
	}

	@Override
	public boolean keyboardInput() {
		if (!Keyboard.getEventKeyState()) {
			if (Keyboard.getEventKey() == Keyboard.KEY_Y || Keyboard.getEventKey() == Keyboard.KEY_RETURN) {
				makeClick();
			}
			showWarning = false;
		}

		return false;
	}

	private void makeClick() {
		if (contactSlot != -1) {
			GuiChest chest = (GuiChest) Minecraft.getMinecraft().currentScreen;
			Minecraft.getMinecraft().playerController.windowClick(chest.inventorySlots.windowId,
				contactSlot, 1, 0, Minecraft.getMinecraft().thePlayer
			);
			contactSlot = -1;
		}
	}
}
