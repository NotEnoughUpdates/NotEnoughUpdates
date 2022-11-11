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
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public enum RecipeType {
	CRAFTING("crafting", "Crafting", CraftingRecipe::parseCraftingRecipe, new ItemStack(Blocks.crafting_table)),
	FORGE("forge", "Forging", ForgeRecipe::parseForgeRecipe, new ItemStack(Blocks.anvil)),
	TRADE("trade", "Trading", VillagerTradeRecipe::parseStaticRecipe, new ItemStack(Items.emerald)),
	MOB_LOOT("drops", "Mob Loot", MobLootRecipe::parseRecipe, new ItemStack(Items.diamond_sword)),
	NPC_SHOP("npc_shop", "NPC Item Shop", ItemShopRecipe::parseItemRecipe, new ItemStack(Items.wheat_seeds)),
	ESSENCE_UPGRADES("", "Essence Upgrades", null, new ItemStack(Items.nether_star)),
	KAT_UPGRADE("katgrade", "Katsitting", KatRecipe::parseRecipe, new ItemStack(Blocks.red_flower));

	private final String id;
	private final String label;
	private final RecipeFactory recipeFactory;
	private final ItemStack icon;

	RecipeType(String id, String label, RecipeFactory recipeFactory, ItemStack icon) {
		this.id = id;
		this.label = label;
		this.recipeFactory = recipeFactory;
		this.icon = icon;
		icon.setStackDisplayName("neurecipe-" + id);
	}

	public String getId() {
		return id;
	}

	public String getLabel() {
		return label;
	}

	public RecipeFactory getRecipeFactory() {
		return recipeFactory;
	}

	public ItemStack getIcon() {
		return icon;
	}

	public NeuRecipe createRecipe(NEUManager manager, JsonObject recipe, JsonObject outputItemJson) {
		return recipeFactory.createRecipe(manager, recipe, outputItemJson);
	}

	public static RecipeType getRecipeTypeForId(String id) {
		for (RecipeType value : values()) {
			if (value.id.equals(id)) return value;
		}
		return null;
	}

	@FunctionalInterface
	interface RecipeFactory {
		NeuRecipe createRecipe(NEUManager manager, JsonObject recipe, JsonObject outputItemJson);
	}
}
