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

import io.github.moulberry.notenoughupdates.events.SlotClickEvent;
import io.github.moulberry.notenoughupdates.overlays.AuctionSearchOverlay;
import io.github.moulberry.notenoughupdates.overlays.RecipeSearchOverlay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiEditSign;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Objects;

public class RecipeSearch {
	private static final RecipeSearch INSTANCE = new RecipeSearch();

	public static RecipeSearch getInstance() {
		return INSTANCE;
	}

	@SubscribeEvent
	public void onGuiOpen(SlotClickEvent event) {
		ContainerChest chest = (ContainerChest) event.guiContainer.inventorySlots;
		String guiName = chest.getLowerChestInventory().getName();
		if (!Objects.equals(guiName, "Craft Item")) return;
		if (event.slot.slotNumber != 32) return;
		Minecraft.getMinecraft().displayGuiScreen(new GuiEditSign(new TileEntitySign()));
		event.setCanceled(true);
	}
}
