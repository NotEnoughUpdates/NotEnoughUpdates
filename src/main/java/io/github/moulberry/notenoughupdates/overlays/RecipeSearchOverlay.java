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
import io.github.moulberry.notenoughupdates.events.ReplaceItemEvent;
import io.github.moulberry.notenoughupdates.events.SlotClickEvent;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.EnumChatFormatting;
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
		ItemStack stack = event.slot.getStack();
		if (enableSearchOverlay() && (event.slot.slotNumber == 50 || event.slot.slotNumber == 51) && stack != null && stack.hasDisplayName() && stack.getItem() == Items.sign && stack.getDisplayName().equals("§aSearch Recipes")) {
			event.setCanceled(true);
			NotEnoughUpdates.INSTANCE.openGui = new RecipeSearchOverlay();
		}
		if (shouldAddPickaxe() && event.slot.slotNumber == 32 && Utils.getOpenChestName().equals("Craft Item")) {
			event.setCanceled(true);
			NotEnoughUpdates.INSTANCE.openGui = new RecipeSearchOverlay();
		}
	}

	private static final ItemStack recipeSearchStack = Utils.createItemStack(
		Items.golden_pickaxe,
		EnumChatFormatting.GREEN + "Recipe Search",
		EnumChatFormatting.YELLOW + "Click to open Recipe Search!"
	);

	@SubscribeEvent
	public void slotReplace(ReplaceItemEvent event) {
		if (event.getInventory() instanceof InventoryPlayer) return;
		if (!shouldAddPickaxe()) return;
		if (event.getSlotNumber() != 32 || !Utils.getOpenChestName().equals("Craft Item")) return;
		if (event.getOriginal() == null || event.getOriginal().getItem() != Item.getItemFromBlock(Blocks.stained_glass_pane) ||
		!event.getOriginal().getDisplayName().equals(" ")) return;
		event.replaceWith(recipeSearchStack);
	}


	@Override
	public boolean enableSearchOverlay() { // enables the button in /recipes, not the whole overlay
		return NotEnoughUpdates.INSTANCE.config.recipeTweaks.enableSearchOverlay;
	}

	public boolean shouldAddPickaxe() {
		return NotEnoughUpdates.INSTANCE.config.recipeTweaks.addPickaxeStack;
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
