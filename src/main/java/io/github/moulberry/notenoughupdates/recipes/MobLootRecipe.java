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
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import io.github.moulberry.notenoughupdates.NEUManager;
import io.github.moulberry.notenoughupdates.core.util.StringUtils;
import io.github.moulberry.notenoughupdates.miscfeatures.entityviewer.EntityViewer;
import io.github.moulberry.notenoughupdates.miscgui.GuiItemRecipe;
import io.github.moulberry.notenoughupdates.profileviewer.GuiProfileViewer;
import io.github.moulberry.notenoughupdates.profileviewer.Panorama;
import io.github.moulberry.notenoughupdates.util.ItemUtils;
import io.github.moulberry.notenoughupdates.util.JsonUtils;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class MobLootRecipe implements NeuRecipe {

	private static final int MOB_POS_X = 38, MOB_POS_Y = 100;
	private static final int SLOT_POS_X = 82, SLOT_POS_Y = 24;

	public static class MobDrop {
		public final Ingredient drop;
		public final String chance;
		public final List<String> extra;

		private ItemStack itemStack;

		public MobDrop(Ingredient drop, String chance, List<String> extra) {
			this.drop = drop;
			this.chance = chance;
			this.extra = extra;
		}

		public ItemStack getItemStack() {
			if (itemStack == null) {
				itemStack = drop.getItemStack().copy();
				List<String> arrayList = new ArrayList<>(extra);
				arrayList.add("§r§e§lDrop Chance: §6" + formatDropChance());
				ItemUtils.appendLore(itemStack, arrayList);
			}
			return itemStack;
		}

		private String formatDropChance() {
			if (chance == null) {
				return "";
			}
			
			if (!chance.endsWith("%")) {
				return chance;
			}

			String chanceText = chance.substring(0, chance.length() - 1);
			int chanceIn;
			try {
				chanceIn = (int) (100.0 / Double.parseDouble(chanceText));
			} catch (NumberFormatException e) {
				return chance;
			}

			String format = StringUtils.formatNumber(chanceIn);
			return "1/" + format + " (" + chance + ")";
		}
	}

	public static ResourceLocation BACKGROUND = new ResourceLocation(
		"notenoughupdates",
		"textures/gui/mob_loot_tall.png"
	);
	private final Ingredient mobIngredient;
	private final List<MobDrop> drops;
	private final int coins;
	private final int combatXp;
	private final int xp;
	private final String name;
	private final String render;
	private final int level;
	private final List<String> extra;
	private EntityLivingBase entityLivingBase;

	private final String panoName;

	private ResourceLocation[] panos = null;

	public MobLootRecipe(
		Ingredient mobIngredient,
		List<MobDrop> drops,
		int level,
		int coins,
		int xp,
		int combatXp,
		String name,
		String render,
		List<String> extra,
		String panoName
	) {
		this.mobIngredient = mobIngredient;
		this.drops = drops;
		this.level = level;
		this.coins = coins;
		this.xp = xp;
		this.extra = extra;
		this.combatXp = combatXp;
		this.name = name;
		this.render = render;
		this.panoName = panoName;
	}

	public String getName() {
		return name;
	}

	public List<MobDrop> getDrops() {
		return drops;
	}

	public int getCoins() {
		return coins;
	}

	public int getCombatXp() {
		return combatXp;
	}

	public Ingredient getMob() {
		return mobIngredient;
	}

	public int getXp() {
		return xp;
	}

	public String getRender() {
		return render;
	}

	public synchronized EntityLivingBase getRenderEntity() {
		if (entityLivingBase == null) {
			if (render == null) return null;
			if (render.startsWith("@")) {
				entityLivingBase = EntityViewer.constructEntity(new ResourceLocation(render.substring(1)));
			} else {
				entityLivingBase = EntityViewer.constructEntity(render, Collections.emptyList());
			}
		}
		return entityLivingBase;
	}

	@Override
	public Set<Ingredient> getIngredients() {
		return Sets.newHashSet(mobIngredient);
	}

	@Override
	public Set<Ingredient> getCatalystItems() {
		return Sets.newHashSet(mobIngredient);
	}

	@Override
	public Set<Ingredient> getOutputs() {
		return drops.stream().map(it -> it.drop).collect(Collectors.toSet());
	}

	@Override
	public String getTitle() {
		return getFullMobName();
	}

	public String getFullMobName() {
		return (level > 0 ? "§8[§7Lv " + level + "§8] §c" : "§c") + name;
	}

	@Override
	public List<RecipeSlot> getSlots() {
		List<RecipeSlot> slots = new ArrayList<>();
		BiConsumer<Integer, ItemStack> addSlot = (sl, is) -> slots.add(
			new RecipeSlot(
				SLOT_POS_X + (sl % 5) * 16,
				SLOT_POS_Y + (sl / 5) * 16,
				is
			));
		int i = 0;
		for (; i < drops.size(); i++) {
			MobDrop mobDrop = drops.get(i);
			addSlot.accept(i, mobDrop.getItemStack());
		}
		return slots;
	}

	@Override
	public RecipeType getType() {
		return RecipeType.MOB_LOOT;
	}

	@Override
	public boolean shouldUseForCraftCost() {
		return false;
	}

	@Override
	public boolean hasVariableCost() {
		return true;
	}

	public static final int PANORAMA_POS_X = 13;
	public static final int PANORAMA_POS_Y = 23;
	public static final int PANORAMA_WIDTH = 50;
	public static final int PANORAMA_HEIGHT = 80;

	@Override
	public void drawExtraBackground(GuiItemRecipe gui, int mouseX, int mouseY) {
		if (panos == null) {
			panos = Panorama.getPanoramasForLocation(panoName, "day");
		}
		Panorama.drawPanorama(
			((System.nanoTime() / 20000000000F) % 1) * 360,
			gui.guiLeft + PANORAMA_POS_X,
			gui.guiTop + PANORAMA_POS_Y,
			PANORAMA_WIDTH,
			PANORAMA_HEIGHT,
			0F,
			0F,
			panos
		);
		if (getRenderEntity() != null)
			EntityViewer.renderEntity(entityLivingBase, gui.guiLeft + MOB_POS_X, gui.guiTop + MOB_POS_Y, mouseX, mouseY);
	}

	@Override
	public void drawHoverInformation(GuiItemRecipe gui, int mouseX, int mouseY) {
		if (gui.isWithinRect(
			mouseX,
			mouseY,
			gui.guiLeft + PANORAMA_POS_X,
			gui.guiTop + PANORAMA_POS_Y,
			PANORAMA_WIDTH,
			PANORAMA_HEIGHT
		)) {
			List<String> stuff = new ArrayList<>();
			stuff.add(getFullMobName());
			stuff.add("");
			if (coins > 0)
				stuff.add("§r§6Coins: " + coins);
			if (xp > 0)
				stuff.add("§r§aExperience: " + xp);
			if (combatXp > 0)
				stuff.add("§r§bCombat Experience: " + combatXp);
			stuff.addAll(extra);
			Utils.drawHoveringText(stuff, mouseX, mouseY, gui.width, gui.height, -1);
		}
	}

	@Override
	public int[] getPageFlipPositionLeftTopCorner() {
		return new int[]{14, 118};
	}

	@Override
	public JsonObject serialize() {
		JsonObject recipe = new JsonObject();
		recipe.addProperty("level", level);
		recipe.addProperty("coins", coins);
		recipe.addProperty("xp", xp);
		recipe.addProperty("combat_xp", combatXp);
		recipe.addProperty("name", name);
		recipe.addProperty("render", render);
		recipe.addProperty("type", getType().getId());
		recipe.addProperty("panorama", "unknown");
		recipe.add("extra", JsonUtils.transformListToJsonArray(extra, JsonPrimitive::new));
		recipe.add("drops", JsonUtils.transformListToJsonArray(drops, drop -> {
			JsonObject dropObject = new JsonObject();
			dropObject.addProperty("id", drop.drop.serialize());
			dropObject.add("extra", JsonUtils.transformListToJsonArray(drop.extra, JsonPrimitive::new));
			dropObject.addProperty("chance", drop.chance);
			return dropObject;
		}));
		return recipe;
	}

	@Override
	public ResourceLocation getBackground() {
		return BACKGROUND;
	}

	public static MobLootRecipe parseRecipe(NEUManager manager, JsonObject recipe, JsonObject outputItemJson) {
		List<MobDrop> drops = new ArrayList<>();
		for (JsonElement jsonElement : recipe.getAsJsonArray("drops")) {
			if (jsonElement.isJsonPrimitive()) {
				drops.add(new MobDrop(new Ingredient(manager, jsonElement.getAsString()), null, Collections.emptyList()));
			} else {
				JsonObject jsonObject = jsonElement.getAsJsonObject();
				drops.add(
					new MobDrop(
						new Ingredient(manager, jsonObject.get("id").getAsString()),
						jsonObject.has("chance") ? jsonObject.get("chance").getAsString() : null,
						JsonUtils.getJsonArrayOrEmpty(jsonObject, "extra", JsonElement::getAsString)
					));
			}
		}

		return new MobLootRecipe(
			new Ingredient(manager, outputItemJson.get("internalname").getAsString(), 1),
			drops,
			recipe.has("level") ? recipe.get("level").getAsInt() : 0,
			recipe.has("coins") ? recipe.get("coins").getAsInt() : 0,
			recipe.has("xp") ? recipe.get("xp").getAsInt() : 0,
			recipe.has("combat_xp") ? recipe.get("combat_xp").getAsInt() : 0,
			recipe.get("name").getAsString(),
			recipe.has("render") && !recipe.get("render").isJsonNull() ? recipe.get("render").getAsString() : null,
			JsonUtils.getJsonArrayOrEmpty(recipe, "extra", JsonElement::getAsString),
			recipe.has("panorama") ? recipe.get("panorama").getAsString() : "unknown"
		);
	}
}
