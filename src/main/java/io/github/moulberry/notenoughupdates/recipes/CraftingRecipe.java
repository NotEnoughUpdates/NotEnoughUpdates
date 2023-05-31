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

import com.google.common.collect.Sets;
import com.google.gson.JsonObject;
import io.github.moulberry.notenoughupdates.NEUManager;
import io.github.moulberry.notenoughupdates.miscgui.GuiItemRecipe;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class CraftingRecipe implements NeuRecipe {

	public static final ResourceLocation BACKGROUND = new ResourceLocation("notenoughupdates",
		"textures/gui/crafting_table_tall.png");

	private static final int EXTRA_STRING_X = 132;
	private static final int EXTRA_STRING_Y = 50;

	private final NEUManager manager;
	private final Ingredient[] inputs;
	private final String extraText;
	private final Ingredient outputIngredient;
	private List<RecipeSlot> slots;

	public CraftingRecipe(NEUManager manager, Ingredient[] inputs, Ingredient output, String extra) {
		this.manager = manager;
		this.inputs = inputs;
		this.outputIngredient = output;
		this.extraText = extra;
		if (inputs.length != 9)
			throw new IllegalArgumentException("Cannot construct crafting recipe with non standard crafting grid size");
	}

	@Override
	public Set<Ingredient> getIngredients() {
		Set<Ingredient> ingredients = Sets.newHashSet(inputs);
		ingredients.remove(null);
		return ingredients;
	}

	@Override
	public RecipeType getType() {
		return RecipeType.CRAFTING;
	}

	@Override
	public boolean hasVariableCost() {
		return false;
	}

	@Override
	public Set<Ingredient> getOutputs() {
		return Collections.singleton(getOutput());
	}

	public Ingredient getOutput() {
		return outputIngredient;
	}

	public Ingredient[] getInputs() {
		return inputs;
	}

	@Override
	public List<RecipeSlot> getSlots() {
		if (slots != null) return slots;
		slots = new ArrayList<>();
		for (int x = 0; x < 3; x++) {
			for (int y = 0; y < 3; y++) {
				Ingredient input = inputs[x + y * 3];
				if (input == null) continue;
				ItemStack item = input.getItemStack();
				if (item == null) continue;
				slots.add(new RecipeSlot(30 + x * GuiItemRecipe.SLOT_SPACING, 48 + y * GuiItemRecipe.SLOT_SPACING, item));
			}
		}
		slots.add(new RecipeSlot(124, 66, outputIngredient.getItemStack()));
		return slots;
	}

	public String getCraftText() {
		return extraText;
	}

	@Override
	public ResourceLocation getBackground() {
		return BACKGROUND;
	}

	@Override
	public void drawExtraInfo(GuiItemRecipe gui, int mouseX, int mouseY) {
		String craftingText = getCraftText();
		if (craftingText != null)
			Utils.drawStringCenteredScaledMaxWidth(craftingText,
				gui.guiLeft + EXTRA_STRING_X, gui.guiTop + EXTRA_STRING_Y, false, 75, 0x404040
			);
	}

	@Override
	public JsonObject serialize() {
		JsonObject object = new JsonObject();
		object.addProperty("type", "crafting");
		object.addProperty("count", outputIngredient.getCount());
		object.addProperty("overrideOutputId", outputIngredient.getInternalItemId());
		for (int i = 0; i < 9; i++) {
			Ingredient ingredient = inputs[i];
			if (ingredient == null) continue;
			String[] x = {"1", "2", "3"};
			String[] y = {"A", "B", "C"};
			String name = x[i / 3] + y[i % 3];
			object.addProperty(name, ingredient.serialize());
		}
		if (extraText != null)
			object.addProperty("crafttext", extraText);
		return object;
	}

	public static CraftingRecipe parseCraftingRecipe(NEUManager manager, JsonObject recipe, JsonObject outputItem) {
		Ingredient[] craftMatrix = new Ingredient[9];

		String[] x = {"1", "2", "3"};
		String[] y = {"A", "B", "C"};
		for (int i = 0; i < 9; i++) {
			String name = y[i / 3] + x[i % 3];
			if (!recipe.has(name)) continue;
			String item = recipe.get(name).getAsString();
			if (item == null || item.isEmpty()) continue;
			craftMatrix[i] = new Ingredient(manager, item);
		}
		int resultCount = 1;
		if (recipe.has("count"))
			resultCount = recipe.get("count").getAsInt();
		String extra = null;
		if (outputItem.has("crafttext"))
			extra = outputItem.get("crafttext").getAsString();
		if (recipe.has("crafttext"))
			extra = recipe.get("crafttext").getAsString();
		String outputItemId = outputItem.get("internalname").getAsString();
		if (recipe.has("overrideOutputId"))
			outputItemId = recipe.get("overrideOutputId").getAsString();
		return new CraftingRecipe(manager, craftMatrix, new Ingredient(manager, outputItemId, resultCount), extra);
	}
}
