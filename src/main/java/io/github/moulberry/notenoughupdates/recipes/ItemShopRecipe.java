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
import com.google.gson.JsonPrimitive;
import io.github.moulberry.notenoughupdates.NEUManager;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.miscgui.GuiItemRecipe;
import io.github.moulberry.notenoughupdates.miscgui.GuiNavigation;
import io.github.moulberry.notenoughupdates.util.JsonUtils;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ItemShopRecipe implements NeuRecipe {
	public static ResourceLocation BACKGROUND = new ResourceLocation(
		"notenoughupdates",
		"textures/gui/item_shop_recipe.png"
	);

	private static final int SLOT_IMAGE_U = 176;
	private static final int SLOT_IMAGE_V = 0;

	private static final int SLOT_IMAGE_SIZE = 18;
	public static final int RESULT_SLOT_Y = 66;
	public static final int RESULT_SLOT_X = 124;

	public static final int BUTTON_X = 130;
	public static final int BUTTON_Y = 16;

	private static final int COST_SLOT_X = 30;
	private static final int COST_SLOT_SPACING = 6;
	private static final int ROW_SPACING = 18;
	private final List<Ingredient> cost;
	private final Ingredient result;
	private boolean selected;

	private final Ingredient npcIngredient;
	private final boolean hasWaypoint;
	private final JsonObject npcObject;

	public ItemShopRecipe(Ingredient npcIngredient, List<Ingredient> cost, Ingredient result, JsonObject npcObject) {
		this.npcIngredient = npcIngredient;
		this.cost = cost;
		this.result = result;
		this.npcObject = npcObject;
		hasWaypoint = NotEnoughUpdates.INSTANCE.navigation.isValidWaypoint(npcObject);
	}

	@Override
	public Set<Ingredient> getCatalystItems() {
		return Sets.newHashSet(npcIngredient);
	}

	@Override
	public Set<Ingredient> getIngredients() {
		return new HashSet<>(cost);
	}

	@Override
	public Set<Ingredient> getOutputs() {
		return Sets.newHashSet(result);
	}

	@Override
	public List<RecipeSlot> getSlots() {
		List<RecipeSlot> slots = new ArrayList<>();

		int i = 0;
		int colCount = cost.size() / 4;
		int startX = COST_SLOT_X - 8 * (colCount - 1);
		int rowSize = cost.size();
		if (rowSize > 4) rowSize = 4;
		int startY = RESULT_SLOT_Y + 8 - (SLOT_IMAGE_SIZE * rowSize + COST_SLOT_SPACING * (rowSize - 1)) / 2;
		for (Ingredient ingredient : cost) {
			slots.add(new RecipeSlot(
				startX + (i / 4) * ROW_SPACING + 1,
				startY + (i % 4) * (SLOT_IMAGE_SIZE + COST_SLOT_SPACING) + 1,
				ingredient.getItemStack()
			));
			i++;
		}
		slots.add(new RecipeSlot(RESULT_SLOT_X, RESULT_SLOT_Y, result.getItemStack()));
		return slots;
	}

	@Override
	public void drawExtraBackground(GuiItemRecipe gui, int mouseX, int mouseY) {
		Minecraft.getMinecraft().getTextureManager().bindTexture(BACKGROUND);
		int colCount = cost.size() / 4;
		int startX = COST_SLOT_X - 8 * (colCount - 1);
		int rowSize = cost.size();
		if (rowSize > 4) rowSize = 4;
		int startY = RESULT_SLOT_Y + 8 - (SLOT_IMAGE_SIZE * rowSize + COST_SLOT_SPACING * (rowSize - 1)) / 2;
		for (int i = 0; i < cost.size(); i++) {
			gui.drawTexturedModalRect(
				gui.guiLeft + startX + (i / 4) * ROW_SPACING,
				gui.guiTop + startY + (i % 4) * (SLOT_IMAGE_SIZE + COST_SLOT_SPACING),
				SLOT_IMAGE_U, SLOT_IMAGE_V,
				SLOT_IMAGE_SIZE, SLOT_IMAGE_SIZE
			);
		}
		if (!hasWaypoint) return;
		Minecraft.getMinecraft().getTextureManager().bindTexture(GuiNavigation.BACKGROUND);
		selected = npcIngredient.getInternalItemId().equals(NotEnoughUpdates.INSTANCE.navigation.getInternalname());
		gui.drawTexturedModalRect(
			gui.guiLeft + BUTTON_X,
			gui.guiTop + BUTTON_Y,
			selected ? GuiNavigation.TICK_POSITION_U : GuiNavigation.PIN_POSITION_U,
			selected ? GuiNavigation.TICK_POSITION_V : GuiNavigation.PIN_POSITION_V,
			GuiNavigation.ICON_SIZE,
			GuiNavigation.ICON_SIZE
		);
	}

	@Override
	public void mouseClicked(GuiItemRecipe gui, int mouseX, int mouseY, int mouseButton) {
		if (hasWaypoint && Utils.isWithinRect(
			mouseX - gui.guiLeft,
			mouseY - gui.guiTop,
			BUTTON_X,
			BUTTON_Y,
			GuiNavigation.ICON_SIZE,
			GuiNavigation.ICON_SIZE
		)) {
			boolean shiftPressed = Keyboard.isKeyDown(Keyboard.KEY_RSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_LSHIFT);
			if (selected && !shiftPressed) {
				NotEnoughUpdates.INSTANCE.navigation.untrackWaypoint();
			} else {
				NotEnoughUpdates.INSTANCE.navigation.trackWaypoint(npcIngredient.getInternalItemId());
				if (shiftPressed) {
					NotEnoughUpdates.INSTANCE.navigation.useWarpCommand();
				}
			}
			Utils.playPressSound();
			selected = !selected;
		}
	}

	@Override
	public String getTitle() {
		return npcObject.get("displayname").getAsString();
	}

	@Override
	public RecipeType getType() {
		return RecipeType.NPC_SHOP;
	}

	@Override
	public boolean hasVariableCost() {
		return false;
	}

	@Override
	public JsonObject serialize() {
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("type", "npc_shop");
		jsonObject.addProperty("result", result.serialize());
		jsonObject.add(
			"cost",
			JsonUtils.transformListToJsonArray(cost, costItem -> new JsonPrimitive(costItem.serialize()))
		);
		return jsonObject;
	}

	@Override
	public ResourceLocation getBackground() {
		return BACKGROUND;
	}

	public static NeuRecipe parseItemRecipe(NEUManager neuManager, JsonObject recipe, JsonObject outputItemJson) {
		return new ItemShopRecipe(
			new Ingredient(neuManager, outputItemJson.get("internalname").getAsString()),
			JsonUtils.transformJsonArrayToList(
				recipe.getAsJsonArray("cost"),
				it -> new Ingredient(neuManager, it.getAsString())
			),
			new Ingredient(neuManager, recipe.get("result").getAsString()),
			outputItemJson
		);
	}
}
