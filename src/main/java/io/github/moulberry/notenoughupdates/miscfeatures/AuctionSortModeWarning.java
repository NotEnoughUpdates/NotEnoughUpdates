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
import io.github.moulberry.notenoughupdates.core.util.render.RenderUtils;
import io.github.moulberry.notenoughupdates.core.util.render.TextRenderUtils;
import io.github.moulberry.notenoughupdates.mixins.AccessorGuiContainer;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;

import java.util.List;

public class AuctionSortModeWarning {
	private static final AuctionSortModeWarning INSTANCE = new AuctionSortModeWarning();

	public static AuctionSortModeWarning getInstance() {
		return INSTANCE;
	}

	private boolean isAuctionBrowser() {
		return NotEnoughUpdates.INSTANCE.config.ahTweaks.enableSortWarning &&
			(Utils.getOpenChestName().startsWith("Auctions Browser") ||
				Utils.getOpenChestName().startsWith("Auctions: \""));
	}

	public void onPostGuiRender() {
		if (!isAuctionBrowser()) return;
		GuiChest chest = (GuiChest) Minecraft.getMinecraft().currentScreen;

		ItemStack stack = chest.inventorySlots.getSlot(50).getStack();

		if (stack == null) return;
		List<String> tooltip = stack.getTooltip(Minecraft.getMinecraft().thePlayer, false);

		String selectedSort = null;
		for (String line : tooltip) {
			if (line.startsWith("\u00a75\u00a7o\u00a7b\u25B6 ")) {
				selectedSort = Utils.cleanColour(line.substring("\u00a75\u00a7o\u00a7b\u25B6 ".length()));
			}
		}

		if (selectedSort == null) return;
		if (selectedSort.trim().equals("Lowest Price")) return;
		GlStateManager.disableLighting();
		GlStateManager.pushMatrix();
		GlStateManager.translate(0, 0, 500);

		String selectedColour = "\u00a7e";

		if (selectedSort.trim().equals("Highest Price")) {
			selectedColour = "\u00a7c";
		}

		String warningText = "\u00a7aSort: " + selectedColour + selectedSort;
		int warningLength = Minecraft.getMinecraft().fontRendererObj.getStringWidth(warningText);

		int centerX =
			((AccessorGuiContainer) chest).getGuiLeft() + ((AccessorGuiContainer) chest).getXSize() / 2 + 9;
		int centerY = ((AccessorGuiContainer) chest).getGuiTop() + 26;

		RenderUtils.drawFloatingRectDark(centerX - warningLength / 2 - 4, centerY - 6,
			warningLength + 8, 12, false
		);
		TextRenderUtils.drawStringCenteredScaledMaxWidth(warningText, centerX, centerY, true, chest.width / 2, 0xffffffff);
		GlStateManager.popMatrix();
	}
}
