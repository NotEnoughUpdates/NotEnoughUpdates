package io.github.moulberry.notenoughupdates.recipes;

import com.google.gson.JsonObject;
import io.github.moulberry.notenoughupdates.NEUManager;
import io.github.moulberry.notenoughupdates.miscgui.GuiItemRecipe;
import net.minecraft.util.ResourceLocation;

import java.util.List;
import java.util.Set;

public interface NeuRecipe {
	Set<Ingredient> getIngredients();

	Set<Ingredient> getOutputs();

	List<RecipeSlot> getSlots();

	RecipeType getType();

	default void drawExtraInfo(GuiItemRecipe gui, int mouseX, int mouseY) {
	}

	default void drawExtraBackground(GuiItemRecipe gui, int mouseX, int mouseY) {
	}

	default void drawHoverInformation(GuiItemRecipe gui, int mouseX, int mouseY) {
	}

	boolean hasVariableCost();

	JsonObject serialize();

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
}
