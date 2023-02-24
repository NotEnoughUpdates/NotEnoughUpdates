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
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import io.github.moulberry.notenoughupdates.NEUManager;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.miscgui.GuiItemRecipe;
import io.github.moulberry.notenoughupdates.options.NEUConfig;
import io.github.moulberry.notenoughupdates.util.HotmInformation;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class ForgeRecipe implements NeuRecipe {

	private static final ResourceLocation BACKGROUND = new ResourceLocation(
		"notenoughupdates",
		"textures/gui/forge_recipe_tall.png"
	);

	private static final int SLOT_IMAGE_U = 176;
	private static final int SLOT_IMAGE_V = 0;
	private static final int SLOT_IMAGE_SIZE = 18;
	private static final int SLOT_PADDING = 1;
	private static final int EXTRA_INFO_MAX_WIDTH = 75;
	public static final int EXTRA_INFO_X = 132;
	public static final int EXTRA_INFO_Y = 55;

	public enum ForgeType {
		REFINING, ITEM_FORGING
	}

	private final NEUManager manager;
	private final List<Ingredient> inputs;
	private final Ingredient output;
	private final int hotmLevel;
	private final int timeInSeconds; // TODO: quick forge
	private List<RecipeSlot> slots;

	public ForgeRecipe(
		NEUManager manager,
		List<Ingredient> inputs,
		Ingredient output,
		int durationInSeconds,
		int hotmLevel
	) {
		this.manager = manager;
		this.inputs = inputs;
		this.output = output;
		this.hotmLevel = hotmLevel;
		this.timeInSeconds = durationInSeconds;
	}

	public List<Ingredient> getInputs() {
		return inputs;
	}

	public Ingredient getOutput() {
		return output;
	}

	public int getTimeInSeconds() {
		return timeInSeconds;
	}

	@Override
	public ResourceLocation getBackground() {
		return BACKGROUND;
	}

	@Override
	public RecipeType getType() {
		return RecipeType.FORGE;
	}

	@Override
	public Set<Ingredient> getIngredients() {
		return Sets.newHashSet(inputs);
	}

	@Override
	public boolean hasVariableCost() {
		return false;
	}

	@Override
	public Set<Ingredient> getOutputs() {
		return Collections.singleton(output);
	}

	@Override
	public List<RecipeSlot> getSlots() {
		if (slots != null) return slots;
		slots = new ArrayList<>();
		for (int i = 0; i < inputs.size(); i++) {
			Ingredient input = inputs.get(i);
			ItemStack itemStack = input.getItemStack();
			if (itemStack == null) continue;
			int[] slotCoordinates = getSlotCoordinates(i, inputs.size());
			slots.add(new RecipeSlot(slotCoordinates[0], slotCoordinates[1], itemStack));
		}
		slots.add(new RecipeSlot(124, 66, output.getItemStack()));
		return slots;
	}

	@Override
	public void drawExtraBackground(GuiItemRecipe gui, int mouseX, int mouseY) {
		Minecraft.getMinecraft().getTextureManager().bindTexture(BACKGROUND);
		for (int i = 0; i < inputs.size(); i++) {
			int[] slotCoordinates = getSlotCoordinates(i, inputs.size());
			gui.drawTexturedModalRect(
				gui.guiLeft + slotCoordinates[0] - SLOT_PADDING, gui.guiTop + slotCoordinates[1] - SLOT_PADDING,
				SLOT_IMAGE_U, SLOT_IMAGE_V,
				SLOT_IMAGE_SIZE, SLOT_IMAGE_SIZE
			);
		}
	}

	@Override
	public void drawExtraInfo(GuiItemRecipe gui, int mouseX, int mouseY) {
		if (timeInSeconds > 0)
			Utils.drawStringCenteredScaledMaxWidth(
				formatDuration(timeInSeconds),
				gui.guiLeft + EXTRA_INFO_X,
				gui.guiTop + EXTRA_INFO_Y,
				false,
				EXTRA_INFO_MAX_WIDTH,
				0xff00ff
			);
	}

	@Override
	public void drawHoverInformation(GuiItemRecipe gui, int mouseX, int mouseY) {
		NEUConfig.HiddenProfileSpecific profileSpecific = NotEnoughUpdates.INSTANCE.config.getProfileSpecific();
		if (profileSpecific == null) return;

		if (timeInSeconds <= 0 || !gui.isWithinRect(
			mouseX, mouseY,
			gui.guiLeft + EXTRA_INFO_X - EXTRA_INFO_MAX_WIDTH / 2,
			gui.guiTop + EXTRA_INFO_Y - 8,
			EXTRA_INFO_MAX_WIDTH, 16
		)) return;

		int level = profileSpecific.hotmTree.getOrDefault("Quick Forge", 0);
		if (level == 0) return;
		int reducedTime = getReducedTime(level);

		Utils.drawHoveringText(
			Collections.singletonList(
				EnumChatFormatting.YELLOW + formatDuration(reducedTime) + " with Quick Forge (Level " + level + ")"),
			mouseX, mouseY, gui.width, gui.height, 500);
	}

	public int getReducedTime(int quickForgeUpgradeLevel) {
		return HotmInformation.getQuickForgeMultiplier(quickForgeUpgradeLevel) * timeInSeconds / 1000;
	}

	@Override
	public JsonObject serialize() {
		JsonObject object = new JsonObject();
		JsonArray ingredients = new JsonArray();
		for (Ingredient input : inputs) {
			ingredients.add(new JsonPrimitive(input.serialize()));
		}
		object.addProperty("type", "forge");
		object.add("inputs", ingredients);
		object.addProperty("count", output.getCount());
		object.addProperty("overrideOutputId", output.getInternalItemId());
		if (hotmLevel >= 0)
			object.addProperty("hotmLevel", hotmLevel);
		if (timeInSeconds >= 0)
			object.addProperty("duration", timeInSeconds);
		return object;
	}

	static ForgeRecipe parseForgeRecipe(NEUManager manager, JsonObject recipe, JsonObject output) {
		List<Ingredient> ingredients = new ArrayList<>();
		for (JsonElement element : recipe.getAsJsonArray("inputs")) {
			String ingredientString = element.getAsString();
			ingredients.add(new Ingredient(manager, ingredientString));
		}
		String internalItemId = output.get("internalname").getAsString();
		if (recipe.has("overrideOutputId"))
			internalItemId = recipe.get("overrideOutputId").getAsString();
		int resultCount = 1;
		if (recipe.has("count")) {
			resultCount = recipe.get("count").getAsInt();
		}
		int duration = -1;
		if (recipe.has("duration")) {
			duration = recipe.get("duration").getAsInt();
		}
		int hotmLevel = -1;
		if (recipe.has("hotmLevel")) {
			hotmLevel = recipe.get("hotmLevel").getAsInt();
		}
		return new ForgeRecipe(
			manager,
			ingredients,
			new Ingredient(manager, internalItemId, resultCount),
			duration,
			hotmLevel
		);
	}

	private static final int RECIPE_CENTER_X = 49;
	private static final int RECIPE_CENTER_Y = 74;
	private static final int SLOT_DISTANCE_FROM_CENTER = 30;
	private static final int RECIPE_FALLBACK_X = 20;
	private static final int RECIPE_FALLBACK_Y = 15;

	static int[] getSlotCoordinates(int slotNumber, int totalSlotCount) {
		if (totalSlotCount > 8) {
			return new int[]{
				RECIPE_FALLBACK_X + (slotNumber % 4) * GuiItemRecipe.SLOT_SPACING,
				RECIPE_FALLBACK_Y + (slotNumber / 4) * GuiItemRecipe.SLOT_SPACING,
			};
		}
		if (totalSlotCount == 1) {
			return new int[]{
				RECIPE_CENTER_X - GuiItemRecipe.SLOT_SIZE / 2,
				RECIPE_CENTER_Y - GuiItemRecipe.SLOT_SIZE / 2
			};
		}
		double rad = Math.PI * 2 * slotNumber / totalSlotCount;
		int x = (int) (Math.cos(rad) * SLOT_DISTANCE_FROM_CENTER);
		int y = (int) (Math.sin(rad) * SLOT_DISTANCE_FROM_CENTER);
		return new int[]{
			RECIPE_CENTER_X + x - GuiItemRecipe.SLOT_SIZE / 2,
			RECIPE_CENTER_Y + y - GuiItemRecipe.SLOT_SIZE / 2
		};
	}

	static String formatDuration(int seconds) {
		int minutes = seconds / 60;
		seconds %= 60;
		int hours = minutes / 60;
		minutes %= 60;
		int days = hours / 24;
		hours %= 24;
		StringBuilder sB = new StringBuilder();
		if (days != 0) sB.append(days).append("d ");
		if (hours != 0) sB.append(hours).append("h ");
		if (minutes != 0) sB.append(minutes).append("m ");
		if (seconds != 0) sB.append(seconds).append("s ");
		return sB.substring(0, sB.length() - 1);
	}
}
