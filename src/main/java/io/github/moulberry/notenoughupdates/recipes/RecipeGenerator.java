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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.util.Debouncer;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.init.Items;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class RecipeGenerator {
	public static final String DURATION = "Duration: ";
	public static final String COINS_SUFFIX = " Coins";

	private final NotEnoughUpdates neu;

	private final Map<String, String> savedForgingDurations = new HashMap<>();

	private final Debouncer debouncer = new Debouncer(1000 * 1000 * 50 /* 50 ms */);
	private final Debouncer durationDebouncer = new Debouncer(1000 * 1000 * 500);

	public RecipeGenerator(NotEnoughUpdates neu) {
		this.neu = neu;
	}

	@SubscribeEvent
	public void onTick(TickEvent event) {
		if (!neu.config.apiData.repositoryEditing) return;
		GuiScreen currentScreen = Minecraft.getMinecraft().currentScreen;
		if (currentScreen == null) return;
		if (!(currentScreen instanceof GuiChest)) return;
		analyzeUI((GuiChest) currentScreen);
	}

	private boolean shouldSaveRecipe() {
		return Keyboard.isKeyDown(Keyboard.KEY_O) && debouncer.trigger();
	}

	public void analyzeUI(GuiChest gui) {
		ContainerChest container = (ContainerChest) gui.inventorySlots;
		IInventory menu = container.getLowerChestInventory();
		String uiTitle = menu.getDisplayName().getUnformattedText();
		EntityPlayerSP p = Minecraft.getMinecraft().thePlayer;
		if (uiTitle.startsWith("Item Casting") || uiTitle.startsWith("Refine")) {
			if (durationDebouncer.trigger())
				parseAllForgeItemMetadata(menu);
		}
		boolean saveRecipe = shouldSaveRecipe();
		if (uiTitle.equals("Confirm Process") && saveRecipe) {
			ForgeRecipe recipe = parseSingleForgeRecipe(menu);
			if (recipe == null) {
				Utils.addChatMessage(
					"" + EnumChatFormatting.DARK_RED + EnumChatFormatting.BOLD + "Could not parse recipe for this UI");
			} else {
				Utils.addChatMessage("" + EnumChatFormatting.GREEN + EnumChatFormatting.BOLD + "Parsed recipe:");
				Utils.addChatMessage("" + EnumChatFormatting.AQUA + " Inputs:");
				for (Ingredient i : recipe.getInputs())
					Utils.addChatMessage("  - " + EnumChatFormatting.AQUA + i.getInternalItemId() + " x " + i.getCount());
				Utils.addChatMessage("" + EnumChatFormatting.AQUA + " Output: " + EnumChatFormatting.GOLD +
					recipe.getOutput().getInternalItemId() + " x " + recipe.getOutput().getCount());
				Utils.addChatMessage(
					"" + EnumChatFormatting.AQUA + " Time: " + EnumChatFormatting.GRAY + recipe.getTimeInSeconds() +
						" seconds (no QF) .");
				boolean saved = false;
				try {
					saved = saveRecipes(recipe.getOutput().getInternalItemId(), Collections.singletonList(recipe));
				} catch (IOException ignored) {
				}
				if (!saved)
					Utils.addChatMessage(
						"" + EnumChatFormatting.DARK_RED + EnumChatFormatting.BOLD + EnumChatFormatting.OBFUSCATED + "#" +
							EnumChatFormatting.RESET + EnumChatFormatting.DARK_RED + EnumChatFormatting.BOLD + " ERROR " +
							EnumChatFormatting.DARK_RED + EnumChatFormatting.BOLD + EnumChatFormatting.OBFUSCATED + "#" +
							EnumChatFormatting.RESET + EnumChatFormatting.DARK_RED + EnumChatFormatting.BOLD +
							" Failed to save recipe. Does the item already exist?");
			}
		}
		if (saveRecipe) attemptToSaveBestiary(menu);
	}

	private List<String> getLore(ItemStack item) {
		NBTTagList loreTag = item.getTagCompound().getCompoundTag("display").getTagList("Lore", 8);
		List<String> loreList = new ArrayList<>();
		for (int i = 0; i < loreTag.tagCount(); i++) {
			loreList.add(loreTag.getStringTagAt(i));
		}
		return loreList;
	}

	// §8[§7Lv1§8] §fZombie Villager
	private static final Pattern MOB_DISPLAY_NAME_PATTERN = Pattern.compile("^§8\\[§7Lv(?<level>\\d+)§8] (?<name>.*)$");
	// §7Coins per Kill: §61
	// §7Combat Exp: §3120
	// §8 ■ §7§5Skeleton Grunt Helmet §8(§a5%§8)
	// §8 ■ §7§fRotten Flesh
	// §8 ■ Dragon Essence §8x3-5
	private static final Pattern LORE_PATTERN = Pattern.compile("^(?:" +
		"§7Coins per Kill: §6(?<coins>[,\\d]+)|" +
		"§7Combat Exp: §3(?<combatxp>[,\\d]+)|" +
		"§7XP Orbs: §3(?<xp>[,\\d]+)|" +
		"§8 ■ (?:§7)?(?<dropName>(?:§.)?.+?)(?: §8\\(§a(?<dropChances>[\\d.<]+%)§8\\)| §8(?<dropCount>x.*))?|" +
		"§7Kills: §a[,\\d]+|" +
		"§.[a-zA-Z]+ Loot|" +
		"§7Deaths: §a[,\\d]+|" +
		" §8■ (?<missing>§c\\?\\?\\?)|" +
		"" +
		")$");

	private void attemptToSaveBestiary(IInventory menu) {
		if (!menu.getDisplayName().getUnformattedText().contains("➜")) return;
		ItemStack backArrow = menu.getStackInSlot(48);
		if (backArrow == null || backArrow.getItem() != Items.arrow) return;
		if (!getLore(backArrow).stream().anyMatch(it -> it.startsWith("§7To Bestiary ➜"))) return;
		List<NeuRecipe> recipes = new ArrayList<>();
		String internalMobName =
			menu.getDisplayName().getUnformattedText().split("➜")[1].toUpperCase(Locale.ROOT).trim() + "_MONSTER";
		for (int i = 9; i < 44; i++) {
			ItemStack mobStack = menu.getStackInSlot(i);
			if (mobStack == null || mobStack.getItem() != Items.skull) continue;
			Matcher matcher = MOB_DISPLAY_NAME_PATTERN.matcher(mobStack.getDisplayName());
			if (!matcher.matches()) continue;
			String name = matcher.group("name");
			int level = parseIntIgnoringCommas(matcher.group("level"));
			List<String> mobLore = getLore(mobStack);
			int coins = 0, xp = 0, combatXp = 0;
			List<MobLootRecipe.MobDrop> drops = new ArrayList<>();
			for (String loreLine : mobLore) {
				Matcher loreMatcher = LORE_PATTERN.matcher(loreLine);
				if (!loreMatcher.matches()) {
					Utils.addChatMessage("[WARNING] Unknown lore line: " + loreLine);
					continue;
				}
				if (loreMatcher.group("coins") != null)
					coins = parseIntIgnoringCommas(loreMatcher.group("coins"));
				if (loreMatcher.group("combatxp") != null)
					combatXp = parseIntIgnoringCommas(loreMatcher.group("combatxp"));
				if (loreMatcher.group("xp") != null)
					xp = parseIntIgnoringCommas(loreMatcher.group("xp"));
				if (loreMatcher.group("dropName") != null) {
					String dropName = loreMatcher.group("dropName");
					List<JsonObject> possibleItems = neu.manager.getItemInformation().values().stream().filter(it -> it.get(
						"displayname").getAsString().equals(dropName)).collect(Collectors.toList());
					if (possibleItems.size() != 1) {
						Utils.addChatMessage("[WARNING] Could not parse drop, ambiguous or missing item information: " + loreLine);
						continue;
					}
					Ingredient item = new Ingredient(neu.manager, possibleItems.get(0).get("internalname").getAsString());
					String chance = loreMatcher.group("dropChances") != null
						? loreMatcher.group("dropChances")
						: loreMatcher.group("dropCount");
					drops.add(new MobLootRecipe.MobDrop(item, chance, new ArrayList<>()));
				}
				if (loreMatcher.group("missing") != null) {
					Utils.addChatMessage("[WARNING] You are missing Bestiary levels for drop: " + loreLine);
				}
			}
			recipes.add(new MobLootRecipe(
				new Ingredient(neu.manager, internalMobName, 1),
				drops,
				level,
				coins,
				xp,
				combatXp,
				name,
				null,
				new ArrayList<>(),
				"unknown"
			));
		}
		boolean saved = false;
		try {
			saved = saveRecipes(internalMobName, recipes);
		} catch (IOException ignored) {
		}
		if (!saved)
			Utils.addChatMessage(
				"" + EnumChatFormatting.DARK_RED + EnumChatFormatting.BOLD + EnumChatFormatting.OBFUSCATED + "#" +
					EnumChatFormatting.RESET + EnumChatFormatting.DARK_RED + EnumChatFormatting.BOLD + " ERROR " +
					EnumChatFormatting.DARK_RED + EnumChatFormatting.BOLD + EnumChatFormatting.OBFUSCATED + "#" +
					EnumChatFormatting.RESET + EnumChatFormatting.DARK_RED + EnumChatFormatting.BOLD +
					" Failed to save recipe. Does the item already exist?"); // TODO: MERGE CODE OVER
	}

	private int parseIntIgnoringCommas(String text) {
		return Integer.parseInt(text.replace(",", ""));
	}

	/*{
	id: "minecraft:skull",
	Count: 1b,
	tag: {
			overrideMeta: 1b,
			SkullOwner: {
					Id: "2005daad-730b-363c-abae-e6f3830816fb",
					Properties: {
							textures: [{
									Value: "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTZjMGIzNmQ1M2ZmZjY5YTQ5YzdkNmYzOTMyZjJiMGZlOTQ4ZTAzMjIyNmQ1ZTgwNDVlYzU4NDA4YTM2ZTk1MSJ9fX0="
							}]
					}
			},
			display: {
					Lore: ["§7Coins per Kill: §610", "§7Combat Exp: §340", "§7XP Orbs: §312", "", "§7Kills: §a990", "§7Deaths: §a2", "", "§fCommon Loot", "§8 ■ §7§fEnder Pearl §8x1-3", "", "§9Rare Loot", "§8 ■ §7§aEnchanted Ender Pearl §8(§a1%§8)", "", "§6Legendary Loot", "§8 ■ §7§7[Lvl 1] §aEnderman §8(§a0.02%§8)", "§8 ■ §7§7[Lvl 1] §fEnderman §8(§a0.05%§8)", "§8 ■ §7§5Ender Helmet §8(§a0.1%§8)", "§8 ■ §7§5Ender Boots §8(§a0.1%§8)", "§8 ■ §7§5Ender Leggings §8(§a0.1%§8)", "§8 ■ §7§5Ender Chestplate §8(§a0.1%§8)", "", "§dRNGesus Loot", " §8■ §c???"],
					Name: "§8[§7Lv42§8] §fEnderman"
			},
			AttributeModifiers: []
	},
	Damage: 3s
}*/
	public boolean saveRecipes(String relevantItem, List<NeuRecipe> recipes) throws IOException {
		relevantItem = relevantItem.replace(" ", "_");
		JsonObject outputJson = neu.manager.readJsonDefaultDir(relevantItem + ".json");
		if (outputJson == null) return false;
		outputJson.addProperty("clickcommand", "viewrecipe");
		JsonArray array = new JsonArray();
		for (NeuRecipe recipe : recipes) {
			array.add(recipe.serialize());
		}
		outputJson.add("recipes", array);
		neu.manager.writeJsonDefaultDir(outputJson, relevantItem + ".json");
		neu.manager.loadItem(relevantItem);
		return true;
	}

	public ForgeRecipe parseSingleForgeRecipe(IInventory chest) {
		int durationInSeconds = -1;
		List<Ingredient> inputs = new ArrayList<>();
		Ingredient output = null;
		for (int i = 0; i < chest.getSizeInventory(); i++) {
			int col = i % 9;
			ItemStack itemStack = chest.getStackInSlot(i);
			if (itemStack == null) continue;
			String name = Utils.cleanColour(itemStack.getDisplayName());
			String internalId = neu.manager.getInternalNameForItem(itemStack);
			Ingredient ingredient = null;
			if (itemStack.getDisplayName().endsWith(COINS_SUFFIX)) {
				int coinCost = Integer.parseInt(
					name.substring(0, name.length() - COINS_SUFFIX.length())
							.replace(",", ""));
				ingredient = Ingredient.coinIngredient(neu.manager, coinCost);
			} else if (internalId != null) {
				ingredient = new Ingredient(neu.manager, internalId, itemStack.stackSize);
			}
			if (ingredient == null) continue;
			if (col < 4) {
				inputs.add(ingredient);
			} else {
				output = ingredient;
			}
		}
		if (output == null || inputs.isEmpty()) return null;
		if (savedForgingDurations.containsKey(output.getInternalItemId()))
			durationInSeconds = parseDuration(savedForgingDurations.get(output.getInternalItemId()));
		return new ForgeRecipe(
			neu.manager,
			new ArrayList<>(Ingredient.mergeIngredients(inputs)),
			output,
			durationInSeconds,
			-1
		);
	}

	private static final Map<Character, Integer> durationSuffixLengthMap = new HashMap<Character, Integer>() {{
		put('d', 60 * 60 * 24);
		put('h', 60 * 60);
		put('m', 60);
		put('s', 1);
	}};

	public int parseDuration(String durationString) {
		String[] parts = durationString.split(" ");
		int timeInSeconds = 0;
		for (String part : parts) {
			char signifier = part.charAt(part.length() - 1);
			int value = Integer.parseInt(part.substring(0, part.length() - 1));
			if (!durationSuffixLengthMap.containsKey(signifier)) {
				return -1;
			}
			timeInSeconds += value * durationSuffixLengthMap.get(signifier);
		}
		return timeInSeconds;
	}

	private void parseAllForgeItemMetadata(IInventory chest) {
		for (int i = 0; i < chest.getSizeInventory(); i++) {
			ItemStack stack = chest.getStackInSlot(i);
			if (stack == null) continue;
			String internalName = neu.manager.getInternalNameForItem(stack);
			if (internalName == null) continue;
			List<String> tooltip = stack.getTooltip(Minecraft.getMinecraft().thePlayer, false);
			String durationInfo = null;
			for (String s : tooltip) {
				String info = Utils.cleanColour(s);
				if (info.startsWith(DURATION)) {
					durationInfo = info.substring(DURATION.length());
				}
			}
			if (durationInfo != null)
				savedForgingDurations.put(internalName, durationInfo);
		}
	}

}
