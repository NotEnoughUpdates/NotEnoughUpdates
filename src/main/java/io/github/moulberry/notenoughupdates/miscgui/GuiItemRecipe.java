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

package io.github.moulberry.notenoughupdates.miscgui;

import com.google.common.collect.ImmutableList;
import io.github.moulberry.notenoughupdates.NEUManager;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.core.util.ArrowPagesUtils;
import io.github.moulberry.notenoughupdates.recipes.NeuRecipe;
import io.github.moulberry.notenoughupdates.recipes.RecipeHistory;
import io.github.moulberry.notenoughupdates.recipes.RecipeSlot;
import io.github.moulberry.notenoughupdates.recipes.RecipeType;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GuiItemRecipe extends GuiScreen {
	public static final ResourceLocation tabsTexture = new ResourceLocation("notenoughupdates", "textures/gui/tab.png");

	public static final int SLOT_SIZE = 16;
	public static final int SLOT_SPACING = SLOT_SIZE + 2;
	public static final int TITLE_X = 28;
	public static final int TITLE_Y = 6;
	public static final int HOTBAR_SLOT_X = 8;
	public static final int HOTBAR_SLOT_Y = 197;
	public static final int PLAYER_INVENTORY_X = 8;
	public static final int PLAYER_INVENTORY_Y = 140;
	public static final int TAB_POS_X = -26;
	public static final int TAB_POS_Y = 8;
	public static final int TAB_OFFSET_Y = 30;
	public static final int TAB_SIZE_X = 26;
	public static final int TAB_SIZE_Y = 30;
	public static final int TAB_TEXTURE_SIZE_X = 29;

	private int currentIndex = 0;
	private int currentTab = 0;

	private final Map<RecipeType, List<NeuRecipe>> craftingRecipes = new HashMap<>();
	private final List<RecipeType> tabs = new ArrayList<>();
	private final NEUManager manager;

	public int guiLeft = 0;
	public int guiTop = 0;
	public int xSize = 176;
	public int ySize = 222;

	public GuiItemRecipe(List<NeuRecipe> unsortedRecipes, NEUManager manager) {
		this.manager = manager;

		for (NeuRecipe recipe : unsortedRecipes) {
			craftingRecipes.computeIfAbsent(recipe.getType(), ignored -> new ArrayList<>()).add(recipe);
			if (!tabs.contains(recipe.getType()))
				tabs.add(recipe.getType());
		}
		tabs.sort(Comparator.naturalOrder());
		changeRecipe(0, 0);
	}

	@Override
	public void initGui() {
		this.guiLeft = (width - this.xSize) / 2;
		this.guiTop = (height - this.ySize) / 2;
	}

	public NeuRecipe getCurrentRecipe() {
		List<NeuRecipe> currentRecipes = getCurrentRecipeList();
		currentIndex = MathHelper.clamp_int(currentIndex, 0, currentRecipes.size() - 1);
		return currentRecipes.get(currentIndex);
	}

	public List<NeuRecipe> getCurrentRecipeList() {
		return craftingRecipes.get(getCurrentTab());
	}

	public RecipeType getCurrentTab() {
		currentTab = MathHelper.clamp_int(currentTab, 0, tabs.size() - 1);
		return tabs.get(currentTab);
	}

	public boolean isWithinRect(int x, int y, int topLeftX, int topLeftY, int width, int height) {
		return topLeftX <= x && x < topLeftX + width
			&& topLeftY <= y && y < topLeftY + height;
	}

	private ImmutableList<RecipeSlot> getAllRenderedSlots() {
		return ImmutableList.<RecipeSlot>builder()
												.addAll(getPlayerInventory())
												.addAll(getCurrentRecipe().getSlots()).build();
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();

		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

		NeuRecipe currentRecipe = getCurrentRecipe();

		Minecraft.getMinecraft().getTextureManager().bindTexture(currentRecipe.getBackground());
		this.drawTexturedModalRect(guiLeft, guiTop, 0, 0, this.xSize, this.ySize);

		drawTabs();

		currentRecipe.drawExtraBackground(this, mouseX, mouseY);

		List<RecipeSlot> slots = getAllRenderedSlots();
		for (RecipeSlot slot : slots) {
			Utils.drawItemStack(slot.getItemStack(), slot.getX(this), slot.getY(this), true);
		}

		int[] topLeft = currentRecipe.getPageFlipPositionLeftTopCorner();
		ArrowPagesUtils.onDraw(guiLeft, guiTop, topLeft, currentIndex, getCurrentRecipeList().size());

		Utils.drawStringScaledMaxWidth(
			currentRecipe.getTitle(),
			guiLeft + TITLE_X,
			guiTop + TITLE_Y,
			false,
			xSize - 38,
			0x404040
		);

		currentRecipe.drawExtraInfo(this, mouseX, mouseY);
		super.drawScreen(mouseX, mouseY, partialTicks);
		for (RecipeSlot slot : slots) {
			if (isWithinRect(mouseX, mouseY, slot.getX(this), slot.getY(this), SLOT_SIZE, SLOT_SIZE)) {
				if (slot.getItemStack() == null) continue;
				Utils.drawHoveringText(
					slot.getItemStack().getTooltip(Minecraft.getMinecraft().thePlayer, false),
					mouseX, mouseY, width, height, -1
				);
			}
		}
		currentRecipe.drawHoverInformation(this, mouseX, mouseY);
		drawTabHoverInformation(mouseX, mouseY);
	}

	private void drawTabHoverInformation(int mouseX, int mouseY) {
		if (tabs.size() < 2) return;
		for (int i = 0; i < tabs.size(); i++) {
			if (isWithinRect(
				mouseX - guiLeft,
				mouseY - guiTop,
				TAB_POS_X,
				TAB_POS_Y + TAB_OFFSET_Y * i,
				TAB_SIZE_X,
				TAB_SIZE_Y
			)) {
				RecipeType type = tabs.get(i);
				Utils.drawHoveringText(
					Arrays.asList(
						"" + EnumChatFormatting.RESET + EnumChatFormatting.GREEN + type.getLabel(),
						"" + EnumChatFormatting.RESET + EnumChatFormatting.GRAY + craftingRecipes.get(type).size() + " Recipes"
					),
					mouseX, mouseY, width, height, -1
				);
				return;
			}
		}
	}

	private void drawTabs() {
		if (tabs.size() < 2) return;
		for (int i = 0; i < tabs.size(); i++) {
			RecipeType recipeType = tabs.get(i);
			int tabPosX = guiLeft + TAB_POS_X, tabPosY = guiTop + TAB_OFFSET_Y * i + TAB_POS_Y;
			int textureOffset = 0;
			if (currentTab == i) {
				textureOffset = 30;
			}
			Minecraft.getMinecraft().getTextureManager().bindTexture(tabsTexture);
			drawTexturedModalRect(
				tabPosX, tabPosY,
				0, textureOffset,
				TAB_TEXTURE_SIZE_X, TAB_SIZE_Y
			);
			Utils.drawItemStack(recipeType.getIcon(), tabPosX + 7, tabPosY + 7);
		}
	}

	public List<RecipeSlot> getPlayerInventory() {
		List<RecipeSlot> slots = new ArrayList<>();
		ItemStack[] inventory = Minecraft.getMinecraft().thePlayer.inventory.mainInventory;
		int hotbarSize = InventoryPlayer.getHotbarSize();
		for (int i = 0; i < inventory.length; i++) {
			ItemStack item = inventory[i];
			if (item == null || item.stackSize == 0) continue;
			int row = i / hotbarSize;
			int col = i % hotbarSize;
			if (row == 0)
				slots.add(new RecipeSlot(HOTBAR_SLOT_X + i * SLOT_SPACING, HOTBAR_SLOT_Y + 1, item));
			else
				slots.add(new RecipeSlot(
					PLAYER_INVENTORY_X + col * SLOT_SPACING,
					PLAYER_INVENTORY_Y + (row - 1) * SLOT_SPACING,
					item
				));
		}
		return slots;
	}

	@Override
	public void handleKeyboardInput() throws IOException {
		super.handleKeyboardInput();

		ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
		int width = scaledResolution.getScaledWidth();
		int height = scaledResolution.getScaledHeight();
		int mouseX = Mouse.getX() * width / Minecraft.getMinecraft().displayWidth;
		int mouseY = height - Mouse.getY() * height / Minecraft.getMinecraft().displayHeight - 1;
		int keyPressed = Keyboard.getEventKey() == 0 ? Keyboard.getEventCharacter() + 256 : Keyboard.getEventKey();
		if (Keyboard.getEventKeyState()) return;
		for (RecipeSlot slot : getAllRenderedSlots()) {
			if (isWithinRect(mouseX, mouseY, slot.getX(this), slot.getY(this), SLOT_SIZE, SLOT_SIZE)) {
				ItemStack itemStack = slot.getItemStack();
				if (keyPressed == manager.keybindViewRecipe.getKeyCode()) {
					manager.displayGuiItemRecipe(manager.getInternalNameForItem(itemStack));
				} else if (keyPressed == manager.keybindViewUsages.getKeyCode()) {
					manager.displayGuiItemUsages(manager.getInternalNameForItem(itemStack));
				}
			}
		}

		if (keyPressed == manager.keybindPreviousRecipe.getKeyCode()) {
			NotEnoughUpdates.INSTANCE.openGui = RecipeHistory.getPrevious();
		} else if (keyPressed == manager.keybindNextRecipe.getKeyCode()) {
			NotEnoughUpdates.INSTANCE.openGui = RecipeHistory.getNext();
		}
	}

	public void changeRecipe(int tabIndex, int recipeIndex) {
		buttonList.removeAll(getCurrentRecipe().getExtraButtons(this));
		currentTab = tabIndex;
		currentIndex = recipeIndex;
		buttonList.addAll(getCurrentRecipe().getExtraButtons(this));
	}

	@Override
	protected void actionPerformed(GuiButton p_actionPerformed_1_) {
		getCurrentRecipe().actionPerformed(p_actionPerformed_1_);
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		NeuRecipe currentRecipe = getCurrentRecipe();
		int[] topLeft = currentRecipe.getPageFlipPositionLeftTopCorner();

		if (mouseButton == 3) {
			NotEnoughUpdates.INSTANCE.openGui = RecipeHistory.getPrevious();
		} else if (mouseButton == 4) {
			NotEnoughUpdates.INSTANCE.openGui = RecipeHistory.getNext();
		}

		if (ArrowPagesUtils.onPageSwitchMouse(
			guiLeft,
			guiTop,
			topLeft,
			currentIndex,
			getCurrentRecipeList().size(),
			pageChange ->
				changeRecipe(currentTab, pageChange)
		)) return;

		for (int i = 0; i < tabs.size(); i++) {
			if (isWithinRect(
				mouseX - guiLeft,
				mouseY - guiTop,
				TAB_POS_X,
				TAB_POS_Y + TAB_OFFSET_Y * i,
				TAB_SIZE_X,
				TAB_SIZE_Y
			)) {
				changeRecipe(i, 0);
				Utils.playPressSound();
				return;
			}
		}

		for (RecipeSlot slot : getAllRenderedSlots()) {
			if (isWithinRect(mouseX, mouseY, slot.getX(this), slot.getY(this), SLOT_SIZE, SLOT_SIZE)) {
				ItemStack itemStack = slot.getItemStack();
				if (mouseButton == 0) {
					manager.displayGuiItemRecipe(manager.getInternalNameForItem(itemStack));
					return;
				} else if (mouseButton == 1) {
					manager.displayGuiItemUsages(manager.getInternalNameForItem(itemStack));
					return;
				}
			}
		}

		currentRecipe.mouseClicked(this, mouseX, mouseY, mouseButton);
	}

	@Override
	public void handleMouseInput() throws IOException {
		super.handleMouseInput();
		ScaledResolution scaledResolution = Utils.peekGuiScale();
		int mouseX = Mouse.getX() * scaledResolution.getScaledWidth() / Minecraft.getMinecraft().displayWidth;
		int mouseY = scaledResolution.getScaledHeight() -
			Mouse.getY() * scaledResolution.getScaledHeight() / Minecraft.getMinecraft().displayHeight - 1;
		getCurrentRecipe().genericMouseInput(mouseX, mouseY);
	}

	public void arrowKeyboardInput() {
		ArrowPagesUtils.onPageSwitchKey(currentIndex, getCurrentRecipeList().size(), pageChange ->
			changeRecipe(currentTab, pageChange));
	}
}
