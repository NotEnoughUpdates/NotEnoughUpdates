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

import com.google.gson.JsonObject;
import io.github.moulberry.notenoughupdates.NEUManager;
import io.github.moulberry.notenoughupdates.miscgui.GuiItemRecipe;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public interface NeuRecipe {
	Set<Ingredient> getIngredients();

	Set<Ingredient> getOutputs();

	List<RecipeSlot> getSlots();

	RecipeType getType();

	default String getTitle() {
		return getType().getLabel();
	}

	default void drawExtraInfo(GuiItemRecipe gui, int mouseX, int mouseY) {
	}

	default void drawExtraBackground(GuiItemRecipe gui, int mouseX, int mouseY) {
	}

	default void drawHoverInformation(GuiItemRecipe gui, int mouseX, int mouseY) {
	}

	default void mouseClicked(GuiItemRecipe gui, int mouseX, int mouseY, int mouseButton) {}

	default void handleKeyboardInput() {}

	default Set<Ingredient> getCatalystItems() {
		return Collections.emptySet();
	}

	boolean hasVariableCost();

	JsonObject serialize();

	default List<GuiButton> getExtraButtons(GuiItemRecipe guiItemRecipe) {
		return new ArrayList<>();
	}

	ResourceLocation getBackground();

	static NeuRecipe parseRecipe(NEUManager manager, JsonObject recipe, JsonObject output) {
		RecipeType recipeType = RecipeType.CRAFTING;
		if (recipe.has("type")) {
			recipeType = RecipeType.getRecipeTypeForId(recipe.get("type").getAsString());
		}
		if (recipeType == null) return null;
		return recipeType.createRecipe(manager, recipe, output);
	}

	default boolean shouldUseForCraftCost() {
		return true;
	}

	default boolean isAvailable() {
		return true;
	}

	/**
	 * @return an array of length two in the format [leftmost x, topmost y] of the page buttons
	 */
	default int[] getPageFlipPositionLeftTopCorner() {
		return new int[]{110, 90};
	}

	default void actionPerformed(GuiButton button) {}

	default void genericMouseInput(int mouseX, int mouseY) {}
}
