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

import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.autosubscribe.NEUAutoSubscribe;
import io.github.moulberry.notenoughupdates.events.SlotClickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;

@NEUAutoSubscribe
public class RecipeSearchOverlay extends SearchOverlayScreen {

	public RecipeSearchOverlay() {
		super(new TileEntitySign());
		this.guiType = GuiType.RECIPE;
	}

	public static boolean shouldReplace() {
		return Minecraft.getMinecraft().currentScreen instanceof RecipeSearchOverlay;
	}

	@SubscribeEvent
	public void onSlotClick(SlotClickEvent event) {
		if (!NotEnoughUpdates.INSTANCE.config.recipeTweaks.enableSearchOverlay) return;
		ItemStack stack = event.slot.getStack();
		if ((event.slot.slotNumber == 50 || event.slot.slotNumber == 51) && stack != null && stack.hasDisplayName() && stack.getItem() == Items.sign && stack.getDisplayName().equals("§aSearch Recipes")) {
			event.setCanceled(true);
			NotEnoughUpdates.INSTANCE.openGui = new RecipeSearchOverlay();
		}
	}


	@Override
	public boolean enableSearchOverlay() {
		return NotEnoughUpdates.INSTANCE.config.recipeTweaks.enableSearchOverlay;
	}

	@Override
	public ArrayList<String> previousSearches() {
		return NotEnoughUpdates.INSTANCE.config.hidden.previousRecipeSearches;
	}

	@Override
	public int searchHistorySize() {
		return NotEnoughUpdates.INSTANCE.config.recipeTweaks.recipeSearchHistorySize;
	}

	@Override
	public boolean showPastSearches() {
		return NotEnoughUpdates.INSTANCE.config.recipeTweaks.showPastSearches;
	}

	@Override
	public boolean escFullClose() {
		return NotEnoughUpdates.INSTANCE.config.recipeTweaks.escFullClose;
	}

	@Override
	public boolean keepPreviousSearch() {
		return NotEnoughUpdates.INSTANCE.config.recipeTweaks.keepPreviousSearch;
	}

	@Override
	public GuiType currentGuiType() {
		return GuiType.RECIPE;
	}
}
