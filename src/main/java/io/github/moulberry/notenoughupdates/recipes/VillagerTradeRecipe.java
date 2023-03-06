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
import com.mojang.authlib.GameProfile;
import io.github.moulberry.notenoughupdates.NEUManager;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.miscgui.GuiItemRecipe;
import io.github.moulberry.notenoughupdates.util.SBInfo;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.util.ResourceLocation;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class VillagerTradeRecipe implements NeuRecipe {

	public static final int COST_SLOT_X = 52;
	public static final int COST_SLOT_Y = 66;
	public static final int RESULT_SLOT_Y = 66;
	public static final int RESULT_SLOT_X = 124;

	private static class Holder { // This holder object exists to defer initialization to first access
		private static final GameProfile DREAM_PROFILE = new GameProfile(UUID.fromString(
			"ec70bcaf-702f-4bb8-b48d-276fa52a780c"), "Dream");
		private static final EntityLivingBase DEMO_DREAM = new AbstractClientPlayer(null, DREAM_PROFILE) {
			@Override
			protected NetworkPlayerInfo getPlayerInfo() {
				return new NetworkPlayerInfo(DREAM_PROFILE) {
					@Override
					public ResourceLocation getLocationSkin() {
						return new ResourceLocation("notenoughupdates", "dreamskin.png");
					}
				};
			}
		};
		private static final EntityLivingBase DEMO_VILLAGER = new EntityVillager(null);

		private static boolean isAprilFirst() {
			Calendar cal = Calendar.getInstance();
			return cal.get(Calendar.DAY_OF_MONTH) == 1 && cal.get(Calendar.MONTH) == Calendar.APRIL;
		}

		private static final EntityLivingBase DEMO_ENTITY = isAprilFirst() ? DEMO_DREAM : DEMO_VILLAGER;

	}

	private final static ResourceLocation BACKGROUND = new ResourceLocation(
		"notenoughupdates",
		"textures/gui/villager_recipe_tall.png"
	);

	private final Ingredient result;
	private final Ingredient cost;
	private final int minCost, maxCost;

	public VillagerTradeRecipe(Ingredient result, Ingredient cost, int minCost, int maxCost) {
		this.result = result;
		this.cost = cost;
		this.minCost = minCost;
		this.maxCost = maxCost;
	}

	public VillagerTradeRecipe(Ingredient result, Ingredient cost) {
		this(result, cost, -1, -1);
	}

	public boolean hasVariableCost() {
		return minCost != -1 && maxCost != -1;
	}

	@Override
	public RecipeType getType() {
		return RecipeType.TRADE;
	}

	@Override
	public Set<Ingredient> getIngredients() {
		return Sets.newHashSet(cost);
	}

	@Override
	public Set<Ingredient> getOutputs() {
		return Sets.newHashSet(result);
	}

	@Override
	public List<RecipeSlot> getSlots() {
		return Arrays.asList(
			new RecipeSlot(COST_SLOT_X, COST_SLOT_Y, cost.getItemStack()),
			new RecipeSlot(RESULT_SLOT_X, RESULT_SLOT_Y, result.getItemStack())
		);
	}

	@Override
	public boolean shouldUseForCraftCost() {
		return false;
	}

	@Override
	public boolean isAvailable() {
		return SBInfo.getInstance().getCurrentMode() == SBInfo.Gamemode.STRANDED ||
			NotEnoughUpdates.INSTANCE.config.hidden.dev;
	}

	@Override
	public void drawExtraInfo(GuiItemRecipe gui, int mouseX, int mouseY) {
		if (hasVariableCost()) {
			Utils.drawStringCenteredScaledMaxWidth(
				minCost + " - " + maxCost, gui.guiLeft + 50, gui.guiTop + 90, false, 75, 0xff00ff
			);

		}
	}

	@Override
	public void drawExtraBackground(GuiItemRecipe gui, int mouseX, int mouseY) {
		GuiInventory.drawEntityOnScreen(
			gui.guiLeft + 90,
			gui.guiTop + 100,
			30,
			gui.guiLeft - mouseX + 110,
			gui.guiTop + 60 - mouseY,
			Holder.DEMO_ENTITY
		);
	}

	@Override
	public JsonObject serialize() {
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("type", "trade");
		jsonObject.addProperty("result", result.serialize());
		jsonObject.addProperty("cost", cost.serialize());
		if (minCost > 0)
			jsonObject.addProperty("min", minCost);
		if (maxCost > 0)
			jsonObject.addProperty("max", maxCost);
		return jsonObject;
	}

	@Override
	public ResourceLocation getBackground() {
		return BACKGROUND;
	}

	public static VillagerTradeRecipe parseStaticRecipe(NEUManager manager, JsonObject recipe, JsonObject result) {
		return new VillagerTradeRecipe(
			new Ingredient(manager, recipe.get("result").getAsString()),
			new Ingredient(manager, recipe.get("cost").getAsString()),
			recipe.has("min") ? recipe.get("min").getAsInt() : -1,
			recipe.has("max") ? recipe.get("max").getAsInt() : -1
		);
	}

}
