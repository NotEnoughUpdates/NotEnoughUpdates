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

import com.google.gson.JsonObject;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.autosubscribe.NEUAutoSubscribe;
import io.github.moulberry.notenoughupdates.core.GuiElement;
import io.github.moulberry.notenoughupdates.core.util.render.RenderUtils;
import io.github.moulberry.notenoughupdates.core.util.render.TextRenderUtils;
import io.github.moulberry.notenoughupdates.events.SlotClickEvent;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@NEUAutoSubscribe
public class AuctionBINWarning extends GuiElement {
	private static final AuctionBINWarning INSTANCE = new AuctionBINWarning();

	public static AuctionBINWarning getInstance() {
		return INSTANCE;
	}

	private static final Pattern ITEM_PRICE_REGEX = Pattern.compile("\u00a7fItem price: \u00a76([0-9,]+) coins");

	private boolean showWarning = false;
	private List<String> sellingTooltip;
	private String sellingName;
	private long sellingPrice;
	private long lowestPrice;
	private long buyPercentage;
	private int sellStackAmount;
	private boolean isALoss = true;

	private boolean shouldPerformCheck() {
		if (!NotEnoughUpdates.INSTANCE.hasSkyblockScoreboard()) {
			sellingTooltip = null;
			showWarning = false;
			return false;
		}

		if (Utils.getOpenChestName().startsWith("Create BIN Auction")) {
			return true;
		} else {
			sellingTooltip = null;
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

		if (event.slotId != 29) {
			return;
		}

		sellingPrice = -1;

		ItemStack priceStack = event.guiContainer.inventorySlots.getSlot(31).getStack();
		if (priceStack != null) {
			String displayName = priceStack.getDisplayName();
			Matcher priceMatcher = ITEM_PRICE_REGEX.matcher(displayName);

			if (priceMatcher.matches()) {
				try {
					sellingPrice = Long.parseLong(priceMatcher.group(1).replace(",", ""));
				} catch (NumberFormatException ignored) {
				}
			}
		}

		ItemStack sellStack = event.guiContainer.inventorySlots.getSlot(13).getStack();
		if (sellStack == null) return;

		String internalname = NotEnoughUpdates.INSTANCE.manager.getInternalNameForItem(sellStack);
		sellStackAmount = sellStack.stackSize;

		if (internalname == null) {
			return;
		}

		JsonObject itemInfo = NotEnoughUpdates.INSTANCE.manager.getItemInformation().get(internalname);
		if (itemInfo == null || !itemInfo.has("displayname")) {
			sellingName = internalname;
		} else {
			sellingName = itemInfo.get("displayname").getAsString();
		}

		sellingTooltip = sellStack.getTooltip(
			Minecraft.getMinecraft().thePlayer,
			Minecraft.getMinecraft().gameSettings.advancedItemTooltips
		);

		lowestPrice = NotEnoughUpdates.INSTANCE.manager.auctionManager.getLowestBin(internalname);
		if (lowestPrice <= 0) {
			lowestPrice = (int) NotEnoughUpdates.INSTANCE.manager.auctionManager.getItemAvgBin(internalname);
		}

		float undercutFactor = 1 - NotEnoughUpdates.INSTANCE.config.ahTweaks.warningThreshold / 100;
		if (undercutFactor < 0) undercutFactor = 0;
		if (undercutFactor > 1) undercutFactor = 1;
		float overcutFactor = 1 - NotEnoughUpdates.INSTANCE.config.ahTweaks.overcutWarningThreshold / 100;
		if (overcutFactor < 0) overcutFactor = 0;
		if (overcutFactor > 1) overcutFactor = 1;

		if (lowestPrice == -1) {
			return;
		}
		if (NotEnoughUpdates.INSTANCE.config.ahTweaks.underCutWarning &&
			(sellingPrice > 0 && lowestPrice > 0 && sellingPrice < sellStackAmount * lowestPrice * undercutFactor)) {
			showWarning = true;
			event.setCanceled(true);
		} else if (NotEnoughUpdates.INSTANCE.config.ahTweaks.overCutWarning &&
			(sellingPrice > 0 && lowestPrice > 0 && sellingPrice > sellStackAmount * lowestPrice * (overcutFactor + 1))) {
			showWarning = true;
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
			width / 2 + 90 - neuLength - 3,
			height / 2 - 45 + 4,
			0xff000000
		);

		TextRenderUtils.drawStringCenteredScaledMaxWidth("Are you SURE?",
			width / 2, height / 2 - 45 + 10, false, 170, 0xffff4040
		);

		String lowestPriceStr;
		if (lowestPrice * sellStackAmount > 999) {
			lowestPriceStr = Utils.shortNumberFormat(lowestPrice * sellStackAmount, 0);
		} else {
			lowestPriceStr = "" + lowestPrice * sellStackAmount;
		}

		String sellingPriceStr;
		if (sellingPrice > 999) {
			sellingPriceStr = Utils.shortNumberFormat(sellingPrice, 0);
		} else {
			sellingPriceStr = "" + sellingPrice;
		}

		String sellLine = "\u00a77[ \u00a7r" + sellingName + "\u00a77 ]";

		TextRenderUtils.drawStringCenteredScaledMaxWidth(sellLine,
			width / 2, height / 2 - 45 + 25, false, 170, 0xffffffff
		);
		TextRenderUtils.drawStringCenteredScaledMaxWidth(
			(lowestPrice > 0
				? "has a lowest BIN of \u00a76" + lowestPriceStr + "\u00a7r coins"
				: "\u00a7cWarning: No lowest BIN found!"),
			width / 2, height / 2 - 45 + 34, false, 170, 0xffa0a0a0
		);

		if (sellingPrice > lowestPrice * sellStackAmount) {
			buyPercentage = sellingPrice * 100 / (lowestPrice * sellStackAmount);
			isALoss = false;
		} else if (sellingPrice < lowestPrice * sellStackAmount) {
			buyPercentage = 100 - sellingPrice * 100 / (lowestPrice * sellStackAmount);
			if (buyPercentage <= 0) buyPercentage = 1;
			isALoss = true;
		}

		TextRenderUtils.drawStringCenteredScaledMaxWidth(
			"Continue selling it for",
			width / 2, height / 2 - 45 + 50, false, 170, 0xffa0a0a0
		);
		TextRenderUtils.drawStringCenteredScaledMaxWidth(
			"\u00a76" + sellingPriceStr + "\u00a7r coins?" +
				(lowestPrice > 0 ? "(\u00a7" + (isALoss ? "c-" : "a+") + (buyPercentage >= 100 ? buyPercentage - 100 : buyPercentage) + "%\u00a7r)" : ""),
			width / 2, height / 2 - 45 + 59, false, 170, 0xffa0a0a0
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

		if (sellingTooltip != null) {
			int mouseX = Mouse.getX() * width / Minecraft.getMinecraft().displayWidth;
			int mouseY = height - Mouse.getY() * height / Minecraft.getMinecraft().displayHeight - 1;

			int sellLineLength = Minecraft.getMinecraft().fontRendererObj.getStringWidth(sellLine);

			if (mouseX >= width / 2 - sellLineLength / 2 && mouseX <= width / 2 + sellLineLength / 2 &&
				mouseY >= height / 2 - 45 + 20 && mouseY <= height / 2 - 45 + 30) {
				Utils.drawHoveringText(sellingTooltip, mouseX, mouseY, width, height, -1);
			}
		}

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
					GuiChest chest = (GuiChest) Minecraft.getMinecraft().currentScreen;
					Minecraft.getMinecraft().playerController.windowClick(chest.inventorySlots.windowId,
						29, 0, 0, Minecraft.getMinecraft().thePlayer
					);
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
				GuiChest chest = (GuiChest) Minecraft.getMinecraft().currentScreen;
				Minecraft.getMinecraft().playerController.windowClick(chest.inventorySlots.windowId,
					29, 0, 0, Minecraft.getMinecraft().thePlayer
				);
			}
			showWarning = false;
		}

		return false;
	}
}
