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

package io.github.moulberry.notenoughupdates.recipes;

import io.github.moulberry.notenoughupdates.NEUManager;
import io.github.moulberry.notenoughupdates.miscfeatures.EnchantingSolvers;
import io.github.moulberry.notenoughupdates.mixins.AccessorGuiContainer;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;

import java.util.function.BiConsumer;

public class CraftingOverlay {

	private final NEUManager manager;
	private CraftingRecipe currentRecipe = null;

	public CraftingOverlay(NEUManager manager) {
		this.manager = manager;
		MinecraftForge.EVENT_BUS.register(this);
	}

	public void setShownRecipe(CraftingRecipe recipe) {
		currentRecipe = recipe;
	}

	private void forEachSlot(ContainerChest chest, BiConsumer<Ingredient, Slot> block) {
		for (int i = 0; i < 9; i++) {
			Ingredient recipeIngredient = currentRecipe.getInputs()[i];
			Slot slot = chest.inventorySlots.get(10 + 9 * (i / 3) + (i % 3));
			block.accept(recipeIngredient, slot);
		}
	}

	private void forEachHoveredSlot(
		GuiChest gui,
		ContainerChest chest,
		int mouseX,
		int mouseY,
		BiConsumer<Ingredient, Slot> block
	) {
		forEachSlot(chest, (recipeIngredient, slot) -> {
			if (Utils.isWithinRect(
				mouseX, mouseY,
				slot.xDisplayPosition + ((AccessorGuiContainer) gui).getGuiLeft(),
				slot.yDisplayPosition + ((AccessorGuiContainer) gui).getGuiTop(),
				16, 16
			))
				block.accept(recipeIngredient, slot);
		});
	}

	private void runIfCraftingOverlayIsPresent(Gui gui, BiConsumer<GuiChest, ContainerChest> block) {
		if (currentRecipe == null) return;
		if (!(gui instanceof GuiChest)) return;
		GuiChest guiChest = (GuiChest) gui;
		ContainerChest chest = (ContainerChest) guiChest.inventorySlots;
		IInventory chestInventory = chest.getLowerChestInventory();
		if (!"Craft Item".equals(chestInventory.getDisplayName().getUnformattedText())) return;
		block.accept(guiChest, chest);
	}

	@SubscribeEvent
	public void onGuiOpen(GuiOpenEvent event) {
		currentRecipe = null;
	}

	@SubscribeEvent
	public void onRender(GuiScreenEvent.DrawScreenEvent.Post event) {
		runIfCraftingOverlayIsPresent(event.gui, (guiChest, chest) -> {
			renderSlots(guiChest, chest);
			if (currentRecipe.getCraftText() != null) {
				FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;
				fontRenderer.drawStringWithShadow(
					currentRecipe.getCraftText(),
					Utils.peekGuiScale().getScaledWidth() / 2f - fontRenderer.getStringWidth(currentRecipe.getCraftText()) / 2f,
					((AccessorGuiContainer) guiChest).getGuiTop() - 15f, 0x808080
				);
			}
			renderTooltip(guiChest, chest);
		});
	}

	@SubscribeEvent
	public void onKeyDown(GuiScreenEvent.KeyboardInputEvent.Pre event) {
		if (!Keyboard.getEventKeyState() ||
			(Keyboard.getEventKey() != Keyboard.KEY_U && Keyboard.getEventKey() != Keyboard.KEY_R))
			return;
		if (EnchantingSolvers.currentSolver != EnchantingSolvers.SolverType.NONE) return;

		runIfCraftingOverlayIsPresent(event.gui, (guiChest, chest) -> {
			int mouseX = Utils.getMouseX();
			int mouseY = Utils.getMouseY();

			forEachHoveredSlot(guiChest, chest, mouseX, mouseY, (recipeIngredient, slot) -> {
				if (slot.getStack() == null && recipeIngredient != null) {
					if (Keyboard.getEventKey() == Keyboard.KEY_R)
						manager.showRecipe(recipeIngredient.getInternalItemId());
					if (Keyboard.getEventKey() == Keyboard.KEY_U)
						manager.displayGuiItemRecipe(recipeIngredient.getInternalItemId());
				}
			});
		});
	}

	private void renderTooltip(GuiChest guiChest, ContainerChest chest) {
		int mouseX = Utils.getMouseX();
		int mouseY = Utils.getMouseY();
		forEachHoveredSlot(guiChest, chest, mouseX, mouseY, (recipeIngredient, slot) -> {
			ItemStack actualItem = slot.getStack();
			if (actualItem == null && recipeIngredient != null) {
				Utils.drawHoveringText(
					recipeIngredient.getItemStack().getTooltip(Minecraft.getMinecraft().thePlayer, false),
					mouseX, mouseY,
					Utils.peekGuiScale().getScaledWidth(), Utils.peekGuiScale().getScaledHeight(), -1
				);
			}
		});
	}

	private void renderSlots(GuiChest guiChest, ContainerChest chest) {
		forEachSlot(chest, (recipeIngredient, slot) -> {
			ItemStack actualItem = slot.getStack();
			if (actualItem != null && (recipeIngredient == null ||
				!recipeIngredient.getInternalItemId().equals(manager.getInternalNameForItem(actualItem)) ||
				actualItem.stackSize < recipeIngredient.getCount())) {
				drawItemStack(guiChest, slot, actualItem);
			}
			if (recipeIngredient != null && actualItem == null) {
				drawItemStack(guiChest, slot, recipeIngredient.getItemStack());
			}
		});
	}

	private void drawItemStack(GuiChest gui, Slot slot, ItemStack item) {
		int slotX = slot.xDisplayPosition + ((AccessorGuiContainer) gui).getGuiLeft();
		int slotY = slot.yDisplayPosition + ((AccessorGuiContainer) gui).getGuiTop();
		Gui.drawRect(slotX, slotY, slotX + 16, slotY + 16, 0x64ff0000);
		if (item != null)
			Utils.drawItemStack(item, slotX, slotY);
	}

}
