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
import io.github.moulberry.notenoughupdates.util.ItemUtils;
import net.minecraft.item.ItemStack;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Ingredient {

	public static final String SKYBLOCK_COIN = "SKYBLOCK_COIN";
	private final double count;
	private final String internalItemId;
	private final NEUManager manager;
	private ItemStack itemStack;

	public Ingredient(NEUManager manager, String ingredientIdentifier) {
		this.manager = manager;
		String[] parts = ingredientIdentifier.split(":");
		internalItemId = parts[0];
		if (parts.length == 2) {
			count = Double.parseDouble(parts[1]);
		} else if (parts.length == 1) {
			count = 1;
		} else {
			throw new IllegalArgumentException("Could not parse ingredient " + ingredientIdentifier);
		}
	}

	public Ingredient(NEUManager manager, String internalItemId, double count) {
		this.manager = manager;
		this.count = count;
		this.internalItemId = internalItemId;
	}

	private Ingredient(NEUManager manager, double coinValue) {
		this.manager = manager;
		this.internalItemId = SKYBLOCK_COIN;
		this.count = coinValue;
	}

	public static Set<Ingredient> mergeIngredients(Iterable<Ingredient> ingredients) {
		Map<String, Ingredient> newIngredients = new HashMap<>();
		for (Ingredient i : ingredients) {
			newIngredients.merge(
				i.getInternalItemId(),
				i,
				(a, b) -> new Ingredient(i.manager, i.internalItemId, a.count + b.count)
			);
		}
		return new HashSet<>(newIngredients.values());
	}

	public static Ingredient coinIngredient(NEUManager manager, int coins) {
		return new Ingredient(manager, coins);
	}

	public boolean isCoins() {
		return "SKYBLOCK_COIN".equals(internalItemId);
	}

	public double getCount() {
		return count;
	}

	public String getInternalItemId() {
		return internalItemId;
	}

	public ItemStack getItemStack() {
		if (itemStack != null) return itemStack;
		if (isCoins()) {
			return ItemUtils.getCoinItemStack(count);
		}
		JsonObject itemInfo = manager.getItemInformation().get(internalItemId);
		itemStack = manager.jsonToStack(itemInfo);
		itemStack.stackSize = (int) count;
		return itemStack;
	}

	public String serialize() {
		return internalItemId + ":" + count;
	}
}
