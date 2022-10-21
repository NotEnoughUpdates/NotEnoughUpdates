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

package io.github.moulberry.notenoughupdates.profileviewer;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import io.github.moulberry.notenoughupdates.NEUManager;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.util.Constants;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumChatFormatting;

import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProfileViewer {

	private static final HashMap<String, String> petRarityToNumMap = new HashMap<String, String>() {
		{
			put("COMMON", "0");
			put("UNCOMMON", "1");
			put("RARE", "2");
			put("EPIC", "3");
			put("LEGENDARY", "4");
			put("MYTHIC", "5");
		}
	};
	private static final LinkedHashMap<String, ItemStack> skillToSkillDisplayMap =
		new LinkedHashMap<String, ItemStack>() {
			{
				put("taming", Utils.createItemStack(Items.spawn_egg, EnumChatFormatting.LIGHT_PURPLE + "Taming"));
				put("mining", Utils.createItemStack(Items.stone_pickaxe, EnumChatFormatting.GRAY + "Mining"));
				put(
					"foraging",
					Utils.createItemStack(Item.getItemFromBlock(Blocks.sapling), EnumChatFormatting.DARK_GREEN + "Foraging")
				);
				put(
					"enchanting",
					Utils.createItemStack(Item.getItemFromBlock(Blocks.enchanting_table), EnumChatFormatting.GREEN + "Enchanting")
				);
				put(
					"carpentry",
					Utils.createItemStack(Item.getItemFromBlock(Blocks.crafting_table), EnumChatFormatting.DARK_RED + "Carpentry")
				);
				put("farming", Utils.createItemStack(Items.golden_hoe, EnumChatFormatting.YELLOW + "Farming"));
				put("combat", Utils.createItemStack(Items.stone_sword, EnumChatFormatting.RED + "Combat"));
				put("fishing", Utils.createItemStack(Items.fishing_rod, EnumChatFormatting.AQUA + "Fishing"));
				put("alchemy", Utils.createItemStack(Items.brewing_stand, EnumChatFormatting.BLUE + "Alchemy"));
				put("runecrafting", Utils.createItemStack(Items.magma_cream, EnumChatFormatting.DARK_PURPLE + "Runecrafting"));
				put("social", Utils.createItemStack(Items.emerald, EnumChatFormatting.DARK_GREEN + "Social"));
				// put("catacombs", Utils.createItemStack(Item.getItemFromBlock(Blocks.deadbush), EnumChatFormatting.GOLD+"Catacombs"));
				put("zombie", Utils.createItemStack(Items.rotten_flesh, EnumChatFormatting.GOLD + "Rev Slayer"));
				put("spider", Utils.createItemStack(Items.spider_eye, EnumChatFormatting.GOLD + "Tara Slayer"));
				put("wolf", Utils.createItemStack(Items.bone, EnumChatFormatting.GOLD + "Sven Slayer"));
				put("enderman", Utils.createItemStack(Items.ender_pearl, EnumChatFormatting.GOLD + "Ender Slayer"));
				put("blaze", Utils.createItemStack(Items.blaze_rod, EnumChatFormatting.GOLD + "Blaze Slayer"));
			}
		};
	private static final ItemStack CAT_FARMING = Utils.createItemStack(
		Items.golden_hoe,
		EnumChatFormatting.YELLOW + "Farming"
	);
	private static final ItemStack CAT_MINING = Utils.createItemStack(
		Items.stone_pickaxe,
		EnumChatFormatting.GRAY + "Mining"
	);
	private static final ItemStack CAT_COMBAT = Utils.createItemStack(
		Items.stone_sword,
		EnumChatFormatting.RED + "Combat"
	);
	private static final ItemStack CAT_FORAGING = Utils.createItemStack(
		Item.getItemFromBlock(Blocks.sapling),
		EnumChatFormatting.DARK_GREEN + "Foraging"
	);
	private static final ItemStack CAT_FISHING = Utils.createItemStack(
		Items.fishing_rod,
		EnumChatFormatting.AQUA + "Fishing"
	);
	private static final LinkedHashMap<ItemStack, List<String>> collectionCatToCollectionMap =
		new LinkedHashMap<ItemStack, List<String>>() {
			{
				put(
					CAT_FARMING,
					Utils.createList(
						"WHEAT",
						"CARROT_ITEM",
						"POTATO_ITEM",
						"PUMPKIN",
						"MELON",
						"SEEDS",
						"MUSHROOM_COLLECTION",
						"INK_SACK:3",
						"CACTUS",
						"SUGAR_CANE",
						"FEATHER",
						"LEATHER",
						"PORK",
						"RAW_CHICKEN",
						"MUTTON",
						"RABBIT",
						"NETHER_STALK"
					)
				);
				put(
					CAT_MINING,
					Utils.createList(
						"COBBLESTONE",
						"COAL",
						"IRON_INGOT",
						"GOLD_INGOT",
						"DIAMOND",
						"INK_SACK:4",
						"EMERALD",
						"REDSTONE",
						"QUARTZ",
						"OBSIDIAN",
						"GLOWSTONE_DUST",
						"GRAVEL",
						"ICE",
						"NETHERRACK",
						"SAND",
						"ENDER_STONE",
						null,
						"MITHRIL_ORE",
						"HARD_STONE",
						"GEMSTONE_COLLECTION",
						"MYCEL",
						"SAND:1",
						"SULPHUR_ORE"
					)
				);
				put(
					CAT_COMBAT,
					Utils.createList(
						"ROTTEN_FLESH",
						"BONE",
						"STRING",
						"SPIDER_EYE",
						"SULPHUR",
						"ENDER_PEARL",
						"GHAST_TEAR",
						"SLIME_BALL",
						"BLAZE_ROD",
						"MAGMA_CREAM",
						null,
						null,
						null,
						null,
						"CHILI_PEPPER"
					)
				);
				put(CAT_FORAGING, Utils.createList("LOG", "LOG:1", "LOG:2", "LOG_2:1", "LOG_2", "LOG:3", null));
				put(
					CAT_FISHING,
					Utils.createList(
						"RAW_FISH",
						"RAW_FISH:1",
						"RAW_FISH:2",
						"RAW_FISH:3",
						"PRISMARINE_SHARD",
						"PRISMARINE_CRYSTALS",
						"CLAY_BALL",
						"WATER_LILY",
						"INK_SACK",
						"SPONGE",
						"MAGMA_FISH"
					)
				);
			}
		};
	private static final LinkedHashMap<ItemStack, List<String>> collectionCatToMinionMap =
		new LinkedHashMap<ItemStack, List<String>>() {
			{
				put(
					CAT_FARMING,
					Utils.createList(
						"WHEAT",
						"CARROT",
						"POTATO",
						"PUMPKIN",
						"MELON",
						null,
						"MUSHROOM",
						"COCOA",
						"CACTUS",
						"SUGAR_CANE",
						"CHICKEN",
						"COW",
						"PIG",
						null,
						"SHEEP",
						"RABBIT",
						"NETHER_WARTS"
					)
				);
				put(
					CAT_MINING,
					Utils.createList(
						"COBBLESTONE",
						"COAL",
						"IRON",
						"GOLD",
						"DIAMOND",
						"LAPIS",
						"EMERALD",
						"REDSTONE",
						"QUARTZ",
						"OBSIDIAN",
						"GLOWSTONE",
						"GRAVEL",
						"ICE",
						null,
						"SAND",
						"ENDER_STONE",
						"SNOW",
						"MITHRIL",
						"HARD_STONE",
						null,
						"MYCELIUM",
						"RED_SAND",
						null
					)
				);
				put(
					CAT_COMBAT,
					Utils.createList(
						"ZOMBIE",
						"SKELETON",
						"SPIDER",
						"CAVESPIDER",
						"CREEPER",
						"ENDERMAN",
						"GHAST",
						"SLIME",
						"BLAZE",
						"MAGMA_CUBE",
						"REVENANT",
						"TARANTULA",
						"VOIDLING",
						"INFERNO"
					)
				);
				put(CAT_FORAGING, Utils.createList("OAK", "SPRUCE", "BIRCH", "DARK_OAK", "ACACIA", "JUNGLE", "FLOWER"));
				put(CAT_FISHING, Utils.createList("FISHING", null, null, null, null, null, "CLAY", null, null, null));
			}
		};
	private static final LinkedHashMap<String, ItemStack> collectionToCollectionDisplayMap =
		new LinkedHashMap<String, ItemStack>() {
			{
				/* FARMING COLLECTIONS */
				put("WHEAT", Utils.createItemStack(Items.wheat, EnumChatFormatting.YELLOW + "Wheat"));
				put("CARROT_ITEM", Utils.createItemStack(Items.carrot, EnumChatFormatting.YELLOW + "Carrot"));
				put("POTATO_ITEM", Utils.createItemStack(Items.potato, EnumChatFormatting.YELLOW + "Potato"));
				put(
					"PUMPKIN",
					Utils.createItemStack(Item.getItemFromBlock(Blocks.pumpkin), EnumChatFormatting.YELLOW + "Pumpkin")
				);
				put("MELON", Utils.createItemStack(Items.melon, EnumChatFormatting.YELLOW + "Melon"));
				put("SEEDS", Utils.createItemStack(Items.wheat_seeds, EnumChatFormatting.YELLOW + "Seeds"));
				put(
					"MUSHROOM_COLLECTION",
					Utils.createItemStack(Item.getItemFromBlock(Blocks.red_mushroom), EnumChatFormatting.YELLOW + "Mushroom")
				);
				put("INK_SACK:3", Utils.createItemStack(Items.dye, EnumChatFormatting.YELLOW + "Cocoa Beans", 3));
				put(
					"CACTUS",
					Utils.createItemStack(Item.getItemFromBlock(Blocks.cactus), EnumChatFormatting.YELLOW + "Cactus")
				);
				put("SUGAR_CANE", Utils.createItemStack(Items.reeds, EnumChatFormatting.YELLOW + "Sugar Cane"));
				put("FEATHER", Utils.createItemStack(Items.feather, EnumChatFormatting.YELLOW + "Feather"));
				put("LEATHER", Utils.createItemStack(Items.leather, EnumChatFormatting.YELLOW + "Leather"));
				put("PORK", Utils.createItemStack(Items.porkchop, EnumChatFormatting.YELLOW + "Raw Porkchop"));
				put("RAW_CHICKEN", Utils.createItemStack(Items.chicken, EnumChatFormatting.YELLOW + "Raw Chicken"));
				put("MUTTON", Utils.createItemStack(Items.mutton, EnumChatFormatting.YELLOW + "Mutton"));
				put("RABBIT", Utils.createItemStack(Items.rabbit, EnumChatFormatting.YELLOW + "Raw Rabbit"));
				put("NETHER_STALK", Utils.createItemStack(Items.nether_wart, EnumChatFormatting.YELLOW + "Nether Wart"));

				/* MINING COLLECTIONS */
				put(
					"COBBLESTONE",
					Utils.createItemStack(Item.getItemFromBlock(Blocks.cobblestone), EnumChatFormatting.GRAY + "Cobblestone")
				);
				put("COAL", Utils.createItemStack(Items.coal, EnumChatFormatting.GRAY + "Coal"));
				put("IRON_INGOT", Utils.createItemStack(Items.iron_ingot, EnumChatFormatting.GRAY + "Iron Ingot"));
				put("GOLD_INGOT", Utils.createItemStack(Items.gold_ingot, EnumChatFormatting.GRAY + "Gold Ingot"));
				put("DIAMOND", Utils.createItemStack(Items.diamond, EnumChatFormatting.GRAY + "Diamond"));
				put("INK_SACK:4", Utils.createItemStack(Items.dye, EnumChatFormatting.GRAY + "Lapis Lazuli", 4));
				put("EMERALD", Utils.createItemStack(Items.emerald, EnumChatFormatting.GRAY + "Emerald"));
				put("REDSTONE", Utils.createItemStack(Items.redstone, EnumChatFormatting.GRAY + "Redstone"));
				put("QUARTZ", Utils.createItemStack(Items.quartz, EnumChatFormatting.GRAY + "Nether Quartz"));
				put(
					"OBSIDIAN",
					Utils.createItemStack(Item.getItemFromBlock(Blocks.obsidian), EnumChatFormatting.GRAY + "Obsidian")
				);
				put("GLOWSTONE_DUST", Utils.createItemStack(Items.glowstone_dust, EnumChatFormatting.GRAY + "Glowstone Dust"));
				put("GRAVEL", Utils.createItemStack(Item.getItemFromBlock(Blocks.gravel), EnumChatFormatting.GRAY + "Gravel"));
				put("ICE", Utils.createItemStack(Item.getItemFromBlock(Blocks.ice), EnumChatFormatting.GRAY + "Ice"));
				put(
					"NETHERRACK",
					Utils.createItemStack(Item.getItemFromBlock(Blocks.netherrack), EnumChatFormatting.GRAY + "Netherrack")
				);
				put("SAND", Utils.createItemStack(Item.getItemFromBlock(Blocks.sand), EnumChatFormatting.GRAY + "Sand"));
				put(
					"ENDER_STONE",
					Utils.createItemStack(Item.getItemFromBlock(Blocks.end_stone), EnumChatFormatting.GRAY + "End Stone")
				);
				put("MITHRIL_ORE", Utils.createItemStack(Items.prismarine_crystals, EnumChatFormatting.GRAY + "Mithril"));
				put(
					"HARD_STONE",
					Utils.createItemStack(Item.getItemFromBlock(Blocks.stone), EnumChatFormatting.GRAY + "Hard Stone")
				);
				put(
					"GEMSTONE_COLLECTION",
					Utils.createSkull(
						EnumChatFormatting.GRAY + "Gemstone",
						"e942eb66-a350-38e5-aafa-0dfc3e17b4ac",
						"ewogICJ0aW1lc3RhbXAiIDogMTYxODA4Mzg4ODc3MSwKICAicHJvZmlsZUlkIiA6ICJjNTBhZmE4YWJlYjk0ZTQ1OTRiZjFiNDI1YTk4MGYwMiIsCiAgInByb2ZpbGVOYW1lIiA6ICJUd29FQmFlIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2FhYzE1ZjZmY2YyY2U5NjNlZjRjYTcxZjFhODY4NWFkYjk3ZWI3NjllMWQxMTE5NGNiYmQyZTk2NGE4ODk3OGMiCiAgICB9CiAgfQp9"
					)
				);
				put(
					"MYCEL",
					Utils.createItemStack(Item.getItemFromBlock(Blocks.mycelium), EnumChatFormatting.GRAY + "Mycelium")
				);
				put(
					"SAND:1",
					Utils.createItemStack(Item.getItemFromBlock(Blocks.sand), EnumChatFormatting.GRAY + "Red Sand", 1)
				);
				put("SULPHUR_ORE", Utils.createItemStack(Items.glowstone_dust, EnumChatFormatting.GRAY + "Sulphur"));

				/* COMBAT COLLECTIONS */
				put("ROTTEN_FLESH", Utils.createItemStack(Items.rotten_flesh, EnumChatFormatting.RED + "Rotten Flesh"));
				put("BONE", Utils.createItemStack(Items.bone, EnumChatFormatting.RED + "Bone"));
				put("STRING", Utils.createItemStack(Items.string, EnumChatFormatting.RED + "String"));
				put("SPIDER_EYE", Utils.createItemStack(Items.spider_eye, EnumChatFormatting.RED + "Spider Eye"));
				put("SULPHUR", Utils.createItemStack(Items.gunpowder, EnumChatFormatting.RED + "Gunpowder"));
				put("ENDER_PEARL", Utils.createItemStack(Items.ender_pearl, EnumChatFormatting.RED + "Ender Pearl"));
				put("GHAST_TEAR", Utils.createItemStack(Items.ghast_tear, EnumChatFormatting.RED + "Ghast Tear"));
				put("SLIME_BALL", Utils.createItemStack(Items.slime_ball, EnumChatFormatting.RED + "Slimeball"));
				put("BLAZE_ROD", Utils.createItemStack(Items.blaze_rod, EnumChatFormatting.RED + "Blaze Rod"));
				put("MAGMA_CREAM", Utils.createItemStack(Items.magma_cream, EnumChatFormatting.RED + "Magma Cream"));
				put(
					"CHILI_PEPPER",
					Utils.createSkull(
						EnumChatFormatting.RED + "Chili Pepper",
						"3d47abaa-b40b-3826-b20c-d83a7f053bd9",
						"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjg1OWM4ZGYxMTA5YzA4YTc1NjI3NWYxZDI4ODdjMjc0ODA0OWZlMzM4Nzc3NjlhN2I0MTVkNTZlZGE0NjlkOCJ9fX0"
					)
				);

				/* FORAGING COLLECTIONS */
				put(
					"LOG",
					Utils.createItemStack(Item.getItemFromBlock(Blocks.log), EnumChatFormatting.DARK_GREEN + "Oak Wood")
				);
				put(
					"LOG:1",
					Utils.createItemStack(Item.getItemFromBlock(Blocks.log), EnumChatFormatting.DARK_GREEN + "Spruce Wood", 1)
				);
				put(
					"LOG:2",
					Utils.createItemStack(Item.getItemFromBlock(Blocks.log), EnumChatFormatting.DARK_GREEN + "Birch Wood", 2)
				);
				put(
					"LOG_2:1",
					Utils.createItemStack(Item.getItemFromBlock(Blocks.log2), EnumChatFormatting.DARK_GREEN + "Dark Oak Wood", 1)
				);
				put(
					"LOG_2",
					Utils.createItemStack(Item.getItemFromBlock(Blocks.log2), EnumChatFormatting.DARK_GREEN + "Acacia Wood")
				);
				put(
					"LOG:3",
					Utils.createItemStack(Item.getItemFromBlock(Blocks.log), EnumChatFormatting.DARK_GREEN + "Jungle Wood", 3)
				);

				/* FISHING COLLECTIONS */
				put("RAW_FISH", Utils.createItemStack(Items.fish, EnumChatFormatting.AQUA + "Raw Fish"));
				put("RAW_FISH:1", Utils.createItemStack(Items.fish, EnumChatFormatting.AQUA + "Raw Salmon", 1));
				put("RAW_FISH:2", Utils.createItemStack(Items.fish, EnumChatFormatting.AQUA + "Clownfish", 2));
				put("RAW_FISH:3", Utils.createItemStack(Items.fish, EnumChatFormatting.AQUA + "Pufferfish", 3));
				put(
					"PRISMARINE_SHARD",
					Utils.createItemStack(Items.prismarine_shard, EnumChatFormatting.AQUA + "Prismarine Shard")
				);
				put(
					"PRISMARINE_CRYSTALS",
					Utils.createItemStack(Items.prismarine_crystals, EnumChatFormatting.AQUA + "Prismarine Crystals")
				);
				put("CLAY_BALL", Utils.createItemStack(Items.clay_ball, EnumChatFormatting.AQUA + "Clay"));
				put(
					"WATER_LILY",
					Utils.createItemStack(Item.getItemFromBlock(Blocks.waterlily), EnumChatFormatting.AQUA + "Lily Pad")
				);
				put("INK_SACK", Utils.createItemStack(Items.dye, EnumChatFormatting.AQUA + "Ink Sac"));
				put("SPONGE", Utils.createItemStack(Item.getItemFromBlock(Blocks.sponge), EnumChatFormatting.AQUA + "Sponge"));
				put(
					"MAGMA_FISH",
					Utils.createSkull(
						EnumChatFormatting.AQUA + "Magmafish",
						"5c53195c-5b98-3476-9731-c32647b22723",
						"ewogICJ0aW1lc3RhbXAiIDogMTY0MjQ4ODA3MDY2NiwKICAicHJvZmlsZUlkIiA6ICIzNDkxZjJiOTdjMDE0MWE2OTM2YjFjMjJhMmEwMGZiNyIsCiAgInByb2ZpbGVOYW1lIiA6ICJKZXNzc3N1aGgiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjU2YjU5NTViMjk1NTIyYzk2ODk0ODE5NjBjMDFhOTkyY2ExYzc3NTRjZjRlZTMxM2M4ZGQwYzM1NmQzMzVmIgogICAgfQogIH0KfQ"
					)
				);
			}
		};
	private static final AtomicBoolean updatingResourceCollection = new AtomicBoolean(false);
	private static JsonObject resourceCollection = null;
	private final NEUManager manager;
	private final HashMap<String, JsonObject> uuidToHypixelProfile = new HashMap<>();
	private final HashMap<String, Profile> uuidToProfileMap = new HashMap<>();
	private final HashMap<String, String> nameToUuid = new HashMap<>();

	public ProfileViewer(NEUManager manager) {
		this.manager = manager;
	}

	public static LinkedHashMap<ItemStack, List<String>> getCollectionCatToMinionMap() {
		return collectionCatToMinionMap;
	}

	public static LinkedHashMap<String, ItemStack> getCollectionToCollectionDisplayMap() {
		return collectionToCollectionDisplayMap;
	}

	public static LinkedHashMap<ItemStack, List<String>> getCollectionCatToCollectionMap() {
		return collectionCatToCollectionMap;
	}

	public static Map<String, ItemStack> getSkillToSkillDisplayMap() {
		return Collections.unmodifiableMap(skillToSkillDisplayMap);
	}

	public static Level getLevel(JsonArray levelingArray, float xp, int levelCap, boolean cumulative) {
		Level levelObj = new Level();
		levelObj.totalXp = xp;
		levelObj.maxLevel = levelCap;

		for (int level = 0; level < levelingArray.size(); level++) {
			float levelXp = levelingArray.get(level).getAsFloat();

			if (levelXp > xp) {
				if (cumulative) {
					float previous = level > 0 ? levelingArray.get(level - 1).getAsFloat() : 0;
					levelObj.maxXpForLevel = (levelXp - previous);
					levelObj.level = 1 + level + (xp - levelXp) / levelObj.maxXpForLevel;
				} else {
					levelObj.maxXpForLevel = levelXp;
					levelObj.level = level + xp / levelXp;
				}

				if (levelObj.level > levelCap) {
					levelObj.level = levelCap;
					levelObj.maxed = true;
				}

				return levelObj;
			} else {
				if (!cumulative) {
					xp -= levelXp;
				}
			}
		}

		levelObj.level = Math.min(levelingArray.size(), levelCap);
		levelObj.maxed = true;
		return levelObj;
	}

	public static JsonObject getResourceCollectionInformation() {
		if (resourceCollection != null) return resourceCollection;
		if (updatingResourceCollection.get()) return null;

		updatingResourceCollection.set(true);

		NotEnoughUpdates.INSTANCE.manager.apiUtils
			.newHypixelApiRequest("resources/skyblock/collections")
			.requestJson()
			.thenAccept(jsonObject -> {
				updatingResourceCollection.set(false);
				if (jsonObject != null && jsonObject.has("success") && jsonObject.get("success").getAsBoolean()) {
					resourceCollection = jsonObject.get("collections").getAsJsonObject();
				}
			});
		return null;
	}

	public void getHypixelProfile(String name, Consumer<JsonObject> callback) {
		String nameF = name.toLowerCase();
		manager.apiUtils
			.newHypixelApiRequest("player")
			.queryArgument("name", nameF)
			.requestJson()
			.thenAccept(jsonObject -> {
					if (
						jsonObject != null &&
							jsonObject.has("success") &&
							jsonObject.get("success").getAsBoolean() &&
							jsonObject.get("player").isJsonObject()
					) {
						nameToUuid.put(nameF, jsonObject.get("player").getAsJsonObject().get("uuid").getAsString());
						uuidToHypixelProfile.put(
							jsonObject.get("player").getAsJsonObject().get("uuid").getAsString(),
							jsonObject.get("player").getAsJsonObject()
						);
						if (callback != null) callback.accept(jsonObject);
					} else {
						if (callback != null) callback.accept(null);
					}
				}
			);
	}

	public void putNameUuid(String name, String uuid) {
		nameToUuid.put(name, uuid);
	}

	public void getPlayerUUID(String name, Consumer<String> uuidCallback) {
		String nameF = name.toLowerCase();
		if (nameToUuid.containsKey(nameF)) {
			uuidCallback.accept(nameToUuid.get(nameF));
			return;
		}

		manager.apiUtils
			.request()
			.url("https://api.mojang.com/users/profiles/minecraft/" + nameF)
			.requestJson()
			.thenAccept(jsonObject -> {
				if (jsonObject.has("id") && jsonObject.get("id").isJsonPrimitive() &&
					((JsonPrimitive) jsonObject.get("id")).isString()) {
					String uuid = jsonObject.get("id").getAsString();
					nameToUuid.put(nameF, uuid);
					uuidCallback.accept(uuid);
					return;
				}
				uuidCallback.accept(null);
			});
	}

	public void getProfileByName(String name, Consumer<Profile> callback) {
		String nameF = name.toLowerCase();

		if (nameToUuid.containsKey(nameF) && nameToUuid.get(nameF) == null) {
			callback.accept(null);
			return;
		}

		getPlayerUUID(
			nameF,
			uuid -> {
				if (uuid == null) {
					getHypixelProfile(
						nameF,
						jsonObject -> {
							if (jsonObject != null) {
								callback.accept(getProfileReset(nameToUuid.get(nameF), ignored -> {}));
							} else {
								callback.accept(null);
								nameToUuid.put(nameF, null);
							}
						}
					);
				} else {
					if (!uuidToHypixelProfile.containsKey(uuid)) {
						getHypixelProfile(nameF, jsonObject -> {});
					}
					callback.accept(getProfileReset(uuid, ignored -> {}));
				}
			}
		);
	}

	public Profile getProfile(String uuid, Consumer<Profile> callback) {
		Profile profile = uuidToProfileMap.computeIfAbsent(uuid, k -> new Profile(uuid));
		if (profile.skyblockProfiles != null) {
			callback.accept(profile);
		} else {
			profile.getSkyblockProfiles(() -> callback.accept(profile));
		}
		return profile;
	}

	public Profile getProfileReset(String uuid, Consumer<Profile> callback) {
		if (uuidToProfileMap.containsKey(uuid)) uuidToProfileMap.get(uuid).resetCache();
		return getProfile(uuid, callback);
	}

	public static class Level {

		public float level = 0;
		public float maxXpForLevel = 0;
		public boolean maxed = false;
		public int maxLevel;
		public float totalXp;
	}

	public class Profile {

		private final String uuid;
		private final HashMap<String, JsonObject> profileMap = new HashMap<>();
		private final HashMap<String, JsonObject> petsInfoMap = new HashMap<>();
		private final HashMap<String, List<JsonObject>> coopProfileMap = new HashMap<>();
		private final HashMap<String, Map<String, Level>> skyblockInfoCache = new HashMap<>();
		private final HashMap<String, JsonObject> inventoryCacheMap = new HashMap<>();
		private final HashMap<String, JsonObject> collectionInfoMap = new HashMap<>();
		private final List<String> profileNames = new ArrayList<>();
		private final HashMap<String, PlayerStats.Stats> stats = new HashMap<>();
		private final HashMap<String, PlayerStats.Stats> passiveStats = new HashMap<>();
		private final HashMap<String, Long> networth = new HashMap<>();
		private final AtomicBoolean updatingSkyblockProfilesState = new AtomicBoolean(false);
		private final AtomicBoolean updatingGuildInfoState = new AtomicBoolean(false);
		private final AtomicBoolean updatingPlayerStatusState = new AtomicBoolean(false);
		private final AtomicBoolean updatingBingoInfo = new AtomicBoolean(false);
		private final Pattern COLL_TIER_PATTERN = Pattern.compile("_(-?\\d+)");
		private String latestProfile = null;
		private JsonArray skyblockProfiles = null;
		private JsonObject guildInformation = null;
		private JsonObject playerStatus = null;
		private JsonObject bingoInformation = null;
		private long lastPlayerInfoState = 0;
		private long lastStatusInfoState = 0;
		private long lastGuildInfoState = 0;
		private long lastBingoInfoState = 0;

		public Profile(String uuid) {
			this.uuid = uuid;
		}

		public JsonObject getPlayerStatus() {
			if (playerStatus != null) return playerStatus;
			if (updatingPlayerStatusState.get()) return null;

			long currentTime = System.currentTimeMillis();
			if (currentTime - lastStatusInfoState < 15 * 1000) return null;
			lastStatusInfoState = currentTime;
			updatingPlayerStatusState.set(true);

			HashMap<String, String> args = new HashMap<>();
			args.put("uuid", "" + uuid);
			manager.apiUtils
				.newHypixelApiRequest("status")
				.queryArgument("uuid", "" + uuid)
				.requestJson()
				.handle((jsonObject, ex) -> {
					updatingPlayerStatusState.set(false);

					if (jsonObject != null && jsonObject.has("success") && jsonObject.get("success").getAsBoolean()) {
						playerStatus = jsonObject.get("session").getAsJsonObject();
					}
					return null;
				});
			return null;
		}

		public JsonObject getBingoInformation() {
			long currentTime = System.currentTimeMillis();
			if (bingoInformation != null && currentTime - lastBingoInfoState < 15 * 1000) return bingoInformation;
			if (updatingBingoInfo.get() && bingoInformation != null) return bingoInformation;
			if (updatingBingoInfo.get() && bingoInformation == null) return null;

			lastBingoInfoState = currentTime;
			updatingBingoInfo.set(true);

			NotEnoughUpdates.INSTANCE.manager.apiUtils
				.newHypixelApiRequest("skyblock/bingo")
				.queryArgument("uuid", "" + uuid)
				.requestJson()
				.handle(((jsonObject, throwable) -> {
					updatingBingoInfo.set(false);

					if (jsonObject != null && jsonObject.has("success") && jsonObject.get("success").getAsBoolean()) {
						bingoInformation = jsonObject;
					} else {
						bingoInformation = null;
					}
					return null;
				}));
			return bingoInformation != null ? bingoInformation : null;
		}

		public long getNetWorth(String profileName) {
			if (profileName == null) profileName = latestProfile;
			if (networth.get(profileName) != null) return networth.get(profileName);
			if (getProfileInformation(profileName) == null) return -1;
			if (getInventoryInfo(profileName) == null) return -1;

			JsonObject inventoryInfo = getInventoryInfo(profileName);
			JsonObject profileInfo = getProfileInformation(profileName);

			HashMap<String, Long> mostExpensiveInternal = new HashMap<>();

			long networth = 0;
			for (Map.Entry<String, JsonElement> entry : inventoryInfo.entrySet()) {
				if (entry.getValue().isJsonArray()) {
					for (JsonElement element : entry.getValue().getAsJsonArray()) {
						if (element != null && element.isJsonObject()) {
							JsonObject item = element.getAsJsonObject();
							String internalname = item.get("internalname").getAsString();

							if (manager.auctionManager.isVanillaItem(internalname)) continue;

							JsonObject bzInfo = manager.auctionManager.getBazaarInfo(internalname);

							long auctionPrice;
							if (bzInfo != null && bzInfo.has("curr_sell")) {
								auctionPrice = (int) bzInfo.get("curr_sell").getAsFloat();
							} else {
								auctionPrice = (long) manager.auctionManager.getItemAvgBin(internalname);
								if (auctionPrice <= 0) {
									auctionPrice = manager.auctionManager.getLowestBin(internalname);
								}
							}

							try {
								if (item.has("item_contents")) {
									JsonArray bytesArr = item.get("item_contents").getAsJsonArray();
									byte[] bytes = new byte[bytesArr.size()];
									for (int bytesArrI = 0; bytesArrI < bytesArr.size(); bytesArrI++) {
										bytes[bytesArrI] = bytesArr.get(bytesArrI).getAsByte();
									}
									NBTTagCompound contents_nbt = CompressedStreamTools.readCompressed(new ByteArrayInputStream(bytes));
									NBTTagList items = contents_nbt.getTagList("i", 10);
									for (int j = 0; j < items.tagCount(); j++) {
										if (items.getCompoundTagAt(j).getKeySet().size() > 0) {
											NBTTagCompound nbt = items.getCompoundTagAt(j).getCompoundTag("tag");
											String internalname2 = manager.getInternalnameFromNBT(nbt);
											if (internalname2 != null) {
												if (manager.auctionManager.isVanillaItem(internalname2)) continue;

												JsonObject bzInfo2 = manager.auctionManager.getBazaarInfo(internalname2);

												long auctionPrice2;
												if (bzInfo2 != null && bzInfo2.has("curr_sell")) {
													auctionPrice2 = (int) bzInfo2.get("curr_sell").getAsFloat();
												} else {
													auctionPrice2 = (long) manager.auctionManager.getItemAvgBin(internalname2);
													if (auctionPrice2 <= 0) {
														auctionPrice2 = manager.auctionManager.getLowestBin(internalname2);
													}
												}

												int count2 = items.getCompoundTagAt(j).getByte("Count");

												mostExpensiveInternal.put(
													internalname2,
													auctionPrice2 * count2 + mostExpensiveInternal.getOrDefault(internalname2, 0L)
												);
												networth += auctionPrice2 * count2;
											}
										}
									}
								}
							} catch (IOException ignored) {
							}

							int count = 1;
							if (element.getAsJsonObject().has("count")) {
								count = element.getAsJsonObject().get("count").getAsInt();
							}
							mostExpensiveInternal.put(
								internalname,
								auctionPrice * count + mostExpensiveInternal.getOrDefault(internalname, 0L)
							);
							networth += auctionPrice * count;
						}
					}
				}
			}
			if (networth == 0) return -1;

			networth = (int) (networth * 1.3f);

			JsonObject petsInfo = getPetsInfo(profileName);
			if (petsInfo != null && petsInfo.has("pets")) {
				if (petsInfo.get("pets").isJsonArray()) {
					JsonArray pets = petsInfo.get("pets").getAsJsonArray();
					for (JsonElement element : pets) {
						if (element.isJsonObject()) {
							JsonObject pet = element.getAsJsonObject();

							String petname = pet.get("type").getAsString();
							String tier = pet.get("tier").getAsString();
							String tierNum = petRarityToNumMap.get(tier);
							if (tierNum != null) {
								String internalname2 = petname + ";" + tierNum;
								JsonObject info2 = manager.auctionManager.getItemAuctionInfo(internalname2);
								if (info2 == null || !info2.has("price") || !info2.has("count")) continue;
								int auctionPrice2 = (int) (info2.get("price").getAsFloat() / info2.get("count").getAsFloat());

								networth += auctionPrice2;
							}
						}
					}
				}
			}

			float bankBalance = Utils.getElementAsFloat(Utils.getElement(profileInfo, "banking.balance"), 0);
			float purseBalance = Utils.getElementAsFloat(Utils.getElement(profileInfo, "coin_purse"), 0);

			networth += bankBalance + purseBalance;

			this.networth.put(profileName, networth);
			return networth;
		}

		public String getLatestProfile() {
			return latestProfile;
		}

		public JsonArray getSkyblockProfiles(Runnable runnable) {
			if (skyblockProfiles != null) return skyblockProfiles;

			long currentTime = System.currentTimeMillis();

			if (currentTime - lastPlayerInfoState < 15 * 1000 && updatingSkyblockProfilesState.get()) return null;
			lastPlayerInfoState = currentTime;
			updatingSkyblockProfilesState.set(true);

			manager.apiUtils
				.newHypixelApiRequest("skyblock/profiles")
				.queryArgument("uuid", "" + uuid)
				.requestJson()
				.handle((jsonObject, throwable) -> {
					updatingSkyblockProfilesState.set(false);

					if (jsonObject != null && jsonObject.has("success") && jsonObject.get("success").getAsBoolean()) {
						if (!jsonObject.has("profiles")) return null;
						skyblockProfiles = jsonObject.get("profiles").getAsJsonArray();

						String lastCuteName = null;
						long lastLastSave = 0;

						profileNames.clear();

						for (JsonElement profileEle : skyblockProfiles) {
							JsonObject profile = profileEle.getAsJsonObject();

							if (!profile.has("members")) continue;
							JsonObject members = profile.get("members").getAsJsonObject();

							if (members.has(uuid)) {
								JsonObject member = members.get(uuid).getAsJsonObject();

								if (member.has("coop_invitation")) {
									if (!member.get("coop_invitation").getAsJsonObject().get("confirmed").getAsBoolean()) {
										continue;
									}
								}

								String cuteName = profile.get("cute_name").getAsString();
								profileNames.add(cuteName);
								if (profile.has("selected") && profile.get("selected").getAsBoolean()) {
									lastCuteName = cuteName;
									break;
								}
								if (lastCuteName == null) lastCuteName = cuteName;
								if (member.has("last_save")) {
									long lastSave = member.get("last_save").getAsLong();
									if (lastSave > lastLastSave) {
										lastLastSave = lastSave;
										lastCuteName = cuteName;
									}
								}
							}
						}
						latestProfile = lastCuteName;

						if (runnable != null) runnable.run();
					}
					return null;
				});
			return null;
		}

		public JsonObject getGuildInformation(Runnable runnable) {
			if (guildInformation != null) return guildInformation;

			long currentTime = System.currentTimeMillis();

			if (currentTime - lastGuildInfoState < 15 * 1000 && updatingGuildInfoState.get()) return null;
			lastGuildInfoState = currentTime;
			updatingGuildInfoState.set(true);

			manager.apiUtils
				.newHypixelApiRequest("guild")
				.queryArgument("player", "" + uuid)
				.requestJson()
				.handle((jsonObject, ex) -> {
					updatingGuildInfoState.set(false);

					if (jsonObject != null && jsonObject.has("success") && jsonObject.get("success").getAsBoolean()) {
						if (!jsonObject.has("guild")) return null;

						guildInformation = jsonObject.get("guild").getAsJsonObject();

						if (runnable != null) runnable.run();
					}
					return null;
				});
			return null;
		}

		public List<String> getProfileNames() {
			return profileNames;
		}

		public JsonObject getProfileInformation(String profileName) {
			JsonArray playerInfo = getSkyblockProfiles(() -> {});
			if (playerInfo == null) return null;
			if (profileName == null) profileName = latestProfile;
			if (profileMap.containsKey(profileName)) return profileMap.get(profileName);

			for (int i = 0; i < skyblockProfiles.size(); i++) {
				if (!skyblockProfiles.get(i).isJsonObject()) {
					skyblockProfiles = null;
					return null;
				}
				JsonObject profile = skyblockProfiles.get(i).getAsJsonObject();
				if (profile.get("cute_name").getAsString().equalsIgnoreCase(profileName)) {
					if (!profile.has("members")) return null;
					JsonObject members = profile.get("members").getAsJsonObject();
					if (!members.has(uuid)) continue;
					JsonObject profileInfo = members.get(uuid).getAsJsonObject();
					if (profile.has("banking")) {
						profileInfo.add("banking", profile.get("banking").getAsJsonObject());
					}
					if (profile.has("game_mode")) {
						profileInfo.add("game_mode", profile.get("game_mode"));
					}
					profileMap.put(profileName, profileInfo);
					return profileInfo;
				}
			}

			return null;
		}

		public List<JsonObject> getCoopProfileInformation(String profileName) {
			JsonArray playerInfo = getSkyblockProfiles(() -> {});
			if (playerInfo == null) return null;
			if (profileName == null) profileName = latestProfile;
			if (coopProfileMap.containsKey(profileName)) return coopProfileMap.get(profileName);

			for (int i = 0; i < skyblockProfiles.size(); i++) {
				if (!skyblockProfiles.get(i).isJsonObject()) {
					skyblockProfiles = null;
					return null;
				}
				JsonObject profile = skyblockProfiles.get(i).getAsJsonObject();
				if (profile.get("cute_name").getAsString().equalsIgnoreCase(profileName)) {
					if (!profile.has("members")) return null;
					JsonObject members = profile.get("members").getAsJsonObject();
					if (!members.has(uuid)) return null;
					List<JsonObject> coopList = new ArrayList<>();
					for (Map.Entry<String, JsonElement> islandMember : members.entrySet()) {
						if (!islandMember.getKey().equals(uuid)) {
							JsonObject coopProfileInfo = islandMember.getValue().getAsJsonObject();
							coopList.add(coopProfileInfo);
						}
					}
					coopProfileMap.put(profileName, coopList);
					return coopList;
				}
			}

			return null;
		}

		public void resetCache() {
			skyblockProfiles = null;
			guildInformation = null;
			playerStatus = null;
			stats.clear();
			passiveStats.clear();
			profileNames.clear();
			profileMap.clear();
			coopProfileMap.clear();
			petsInfoMap.clear();
			skyblockInfoCache.clear();
			inventoryCacheMap.clear();
			collectionInfoMap.clear();
			networth.clear();
		}

		public int getCap(JsonObject leveling, String skillName) {
			JsonElement capsElement = Utils.getElement(leveling, "leveling_caps");
			return capsElement != null && capsElement.isJsonObject() && capsElement.getAsJsonObject().has(skillName)
				? capsElement.getAsJsonObject().get(skillName).getAsInt()
				: 50;
		}

		public Map<String, Level> getSkyblockInfo(String profileName) {
			JsonObject profileInfo = getProfileInformation(profileName);

			if (profileInfo == null) return null;
			if (profileName == null) profileName = latestProfile;
			List<JsonObject> coopProfileInfo = getCoopProfileInformation(profileName);
			if (skyblockInfoCache.containsKey(profileName)) return skyblockInfoCache.get(profileName);

			JsonObject leveling = Constants.LEVELING;
			if (leveling == null || !leveling.has("social")) {
				Utils.showOutdatedRepoNotification();
				return null;
			}

			Map<String, Level> out = new HashMap<>();

			List<String> skills = Arrays.asList(
				"taming",
				"mining",
				"foraging",
				"enchanting",
				"carpentry",
				"farming",
				"combat",
				"fishing",
				"alchemy",
				"runecrafting",
				"social"
			);
			float totalSkillXP = 0;
			for (String skillName : skills) {
				float skillExperience = Utils.getElementAsFloat(
					Utils.getElement(profileInfo, "experience_skill_" + (skillName.equals("social") ? "social2" : skillName)),
					0
				);
				// Get the coop's social skill experience since social is a shared skill
				if (skillName.equals("social")) {
					for (JsonObject coopProfile : coopProfileInfo) {
						skillExperience += Utils.getElementAsFloat(
							Utils.getElement(coopProfile, "experience_skill_social2"),
							0
						);
					}
				}
				totalSkillXP += skillExperience;

				JsonArray levelingArray = Utils.getElement(leveling, "leveling_xp").getAsJsonArray();
				if (skillName.equals("runecrafting")) {
					levelingArray = Utils.getElement(leveling, "runecrafting_xp").getAsJsonArray();
				} else if (skillName.equals("social")) {
					levelingArray = Utils.getElement(leveling, "social").getAsJsonArray();
				}

				int maxLevel =
					getCap(leveling, skillName) +
						(
							skillName.equals("farming")
								? Utils.getElementAsInt(Utils.getElement(profileInfo, "jacob2.perks.farming_level_cap"), 0)
								: 0
						);
				out.put(skillName, getLevel(levelingArray, skillExperience, maxLevel, false));
			}

			// Skills API disabled?
			if (totalSkillXP <= 0) {
				return null;
			}

			out.put(
				"hotm",
				getLevel(
					Utils.getElement(leveling, "leveling_xp").getAsJsonArray(),
					Utils.getElementAsFloat(Utils.getElement(profileInfo, "mining_core.experience"), 0),
					getCap(leveling, "HOTM"),
					false
				)
			);

			out.put(
				"catacombs",
				getLevel(
					Utils.getElement(leveling, "catacombs").getAsJsonArray(),
					Utils.getElementAsFloat(Utils.getElement(profileInfo, "dungeons.dungeon_types.catacombs.experience"), 0),
					getCap(leveling, "catacombs"),
					false
				)
			);

			List<String> dungeonClasses = Arrays.asList("healer", "tank", "mage", "archer", "berserk");
			for (String className : dungeonClasses) {
				float classExperience = Utils.getElementAsFloat(
					Utils.getElement(profileInfo, "dungeons.player_classes." + className + ".experience"),
					0
				);
				out.put(
					className,
					getLevel(
						Utils.getElement(leveling, "catacombs").getAsJsonArray(),
						classExperience,
						getCap(leveling, "catacombs"),
						false
					)
				);
			}

			List<String> slayers = Arrays.asList("zombie", "spider", "wolf", "enderman", "blaze");
			for (String slayerName : slayers) {
				float slayerExperience = Utils.getElementAsFloat(Utils.getElement(
					profileInfo,
					"slayer_bosses." + slayerName + ".xp"
				), 0);
				out.put(
					slayerName,
					getLevel(Utils.getElement(leveling, "slayer_xp." + slayerName).getAsJsonArray(), slayerExperience, 9, true)
				);
			}

			skyblockInfoCache.put(profileName, out);

			return out;
		}

		public JsonObject getInventoryInfo(String profileName) {
			JsonObject profileInfo = getProfileInformation(profileName);
			if (profileInfo == null) return null;
			if (profileName == null) profileName = latestProfile;
			if (inventoryCacheMap.containsKey(profileName)) return inventoryCacheMap.get(profileName);

			String inv_armor_bytes = Utils.getElementAsString(
				Utils.getElement(profileInfo, "inv_armor.data"),
				"Hz8IAAAAAAAAAD9iYD9kYD9kAAMAPwI/Gw0AAAA="
			);
			String fishing_bag_bytes = Utils.getElementAsString(
				Utils.getElement(profileInfo, "fishing_bag.data"),
				"Hz8IAAAAAAAAAD9iYD9kYD9kAAMAPwI/Gw0AAAA="
			);
			String quiver_bytes = Utils.getElementAsString(
				Utils.getElement(profileInfo, "quiver.data"),
				"Hz8IAAAAAAAAAD9iYD9kYD9kAAMAPwI/Gw0AAAA="
			);
			String ender_chest_contents_bytes = Utils.getElementAsString(
				Utils.getElement(profileInfo, "ender_chest_contents.data"),
				"Hz8IAAAAAAAAAD9iYD9kYD9kAAMAPwI/Gw0AAAA="
			);
			//Todo clean this up
			//Fake string is so for I loop works the same
			String backpack_contents_json_fake = "fake should fix later";
			JsonObject backpack_contents_json = (JsonObject) Utils.getElement(profileInfo, "backpack_contents");
			JsonObject backpack_icons = (JsonObject) Utils.getElement(profileInfo, "backpack_icons");
			String personal_vault_contents_bytes = Utils.getElementAsString(
				Utils.getElement(profileInfo, "personal_vault_contents.data"),
				"Hz8IAAAAAAAAAD9iYD9kYD9kAAMAPwI/Gw0AAAA="
			);
			String wardrobe_contents_bytes = Utils.getElementAsString(
				Utils.getElement(profileInfo, "wardrobe_contents.data"),
				"Hz8IAAAAAAAAAD9iYD9kYD9kAAMAPwI/Gw0AAAA="
			);
			String potion_bag_bytes = Utils.getElementAsString(
				Utils.getElement(profileInfo, "potion_bag.data"),
				"Hz8IAAAAAAAAAD9iYD9kYD9kAAMAPwI/Gw0AAAA="
			);
			String inv_contents_bytes = Utils.getElementAsString(
				Utils.getElement(profileInfo, "inv_contents.data"),
				"Hz8IAAAAAAAAAD9iYD9kYD9kAAMAPwI/Gw0AAAA="
			);
			String talisman_bag_bytes = Utils.getElementAsString(
				Utils.getElement(profileInfo, "talisman_bag.data"),
				"Hz8IAAAAAAAAAD9iYD9kYD9kAAMAPwI/Gw0AAAA="
			);
			String candy_inventory_contents_bytes = Utils.getElementAsString(
				Utils.getElement(profileInfo, "candy_inventory_contents.data"),
				"Hz8IAAAAAAAAAD9iYD9kYD9kAAMAPwI/Gw0AAAA="
			);
			String equipment_contents_bytes = Utils.getElementAsString(
				Utils.getElement(profileInfo, "equippment_contents.data"),
				"Hz8IAAAAAAAAAD9iYD9kYD9kAAMAPwI/Gw0AAAA="
			);

			JsonObject inventoryInfo = new JsonObject();

			String[] inv_names = new String[]{
				"inv_armor",
				"fishing_bag",
				"quiver",
				"ender_chest_contents",
				"backpack_contents",
				"personal_vault_contents",
				"wardrobe_contents",
				"potion_bag",
				"inv_contents",
				"talisman_bag",
				"candy_inventory_contents",
				"equippment_contents",
			};
			String[] inv_bytes = new String[]{
				inv_armor_bytes,
				fishing_bag_bytes,
				quiver_bytes,
				ender_chest_contents_bytes,
				backpack_contents_json_fake,
				personal_vault_contents_bytes,
				wardrobe_contents_bytes,
				potion_bag_bytes,
				inv_contents_bytes,
				talisman_bag_bytes,
				candy_inventory_contents_bytes,
				equipment_contents_bytes,
			};
			for (int i = 0; i < inv_bytes.length; i++) {
				try {
					String bytes = inv_bytes[i];

					JsonArray contents = new JsonArray();

					if (inv_names[i].equals("backpack_contents")) {
						JsonObject temp = getBackpackData(backpack_contents_json, backpack_icons);
						contents = (JsonArray) temp.get("contents");
						inventoryInfo.add("backpack_sizes", temp.get("backpack_sizes"));
					} else {
						NBTTagCompound inv_contents_nbt = CompressedStreamTools.readCompressed(
							new ByteArrayInputStream(Base64.getDecoder().decode(bytes))
						);
						NBTTagList items = inv_contents_nbt.getTagList("i", 10);
						for (int j = 0; j < items.tagCount(); j++) {
							JsonObject item = manager.getJsonFromNBTEntry(items.getCompoundTagAt(j));
							contents.add(item);
						}
					}
					inventoryInfo.add(inv_names[i], contents);
				} catch (IOException e) {
					inventoryInfo.add(inv_names[i], new JsonArray());
				}
			}

			inventoryCacheMap.put(profileName, inventoryInfo);

			return inventoryInfo;
		}

		public JsonObject getBackpackData(JsonObject backpackContentsJson, JsonObject backpackIcons) {
			if (backpackContentsJson == null || backpackIcons == null) {
				JsonObject bundledReturn = new JsonObject();
				bundledReturn.add("contents", new JsonArray());
				bundledReturn.add("backpack_sizes", new JsonArray());

				return bundledReturn;
			}

			String[] backpackArray = new String[0];

			//Create backpack array which sizes up
			for (Map.Entry<String, JsonElement> backpackIcon : backpackIcons.entrySet()) {
				if (backpackIcon.getValue() instanceof JsonObject) {
					JsonObject backpackData = (JsonObject) backpackContentsJson.get(backpackIcon.getKey());
					String bytes = Utils.getElementAsString(backpackData.get("data"), "Hz8IAAAAAAAAAD9iYD9kYD9kAAMAPwI/Gw0AAAA=");
					backpackArray = growArray(bytes, Integer.parseInt(backpackIcon.getKey()), backpackArray);
				}
			}

			//reduce backpack array to filter out not existent backpacks
			{
				String[] tempBackpackArray = new String[0];
				for (String s : backpackArray) {
					if (s != null) {
						String[] veryTempBackpackArray = new String[tempBackpackArray.length + 1];
						System.arraycopy(tempBackpackArray, 0, veryTempBackpackArray, 0, tempBackpackArray.length);

						veryTempBackpackArray[veryTempBackpackArray.length - 1] = s;
						tempBackpackArray = veryTempBackpackArray;
					}
				}
				backpackArray = tempBackpackArray;
			}

			JsonArray backpackSizes = new JsonArray();
			JsonArray contents = new JsonArray();

			for (String backpack : backpackArray) {
				try {
					NBTTagCompound inv_contents_nbt = CompressedStreamTools.readCompressed(
						new ByteArrayInputStream(Base64.getDecoder().decode(backpack))
					);
					NBTTagList items = inv_contents_nbt.getTagList("i", 10);

					backpackSizes.add(new JsonPrimitive(items.tagCount()));
					for (int j = 0; j < items.tagCount(); j++) {
						JsonObject item = manager.getJsonFromNBTEntry(items.getCompoundTagAt(j));
						contents.add(item);
					}
				} catch (IOException ignored) {
				}
			}

			JsonObject bundledReturn = new JsonObject();
			bundledReturn.add("contents", contents);
			bundledReturn.add("backpack_sizes", backpackSizes);

			return bundledReturn;
		}

		public String[] growArray(String bytes, int index, String[] oldArray) {
			int newSize = Math.max(index + 1, oldArray.length);

			String[] newArray = new String[newSize];
			System.arraycopy(oldArray, 0, newArray, 0, oldArray.length);
			newArray[index] = bytes;

			return newArray;
		}

		public JsonObject getPetsInfo(String profileName) {
			JsonObject profileInfo = getProfileInformation(profileName);
			if (profileInfo == null) return null;
			if (petsInfoMap.containsKey(profileName)) return petsInfoMap.get(profileName);

			JsonObject petsInfo = new JsonObject();
			JsonElement petsElement = profileInfo.get("pets");
			if (petsElement != null && petsElement.isJsonArray()) {
				JsonObject activePet = null;
				JsonArray pets = petsElement.getAsJsonArray();
				for (int i = 0; i < pets.size(); i++) {
					JsonObject pet = pets.get(i).getAsJsonObject();
					if (pet.has("active") && pet.get("active").getAsBoolean()) {
						activePet = pet;
						break;
					}
				}
				petsInfo.add("active_pet", activePet);
				petsInfo.add("pets", pets);
				petsInfoMap.put(profileName, petsInfo);
				return petsInfo;
			}
			return null;
		}

		public JsonObject getCollectionInfo(String profileName) {
			JsonObject profileInfo = getProfileInformation(profileName);
			if (profileInfo == null) return null;
			JsonObject resourceCollectionInfo = getResourceCollectionInformation();
			if (resourceCollectionInfo == null) return null;
			if (profileName == null) profileName = latestProfile;
			if (collectionInfoMap.containsKey(profileName)) return collectionInfoMap.get(profileName);

			List<JsonObject> coopMembers = getCoopProfileInformation(profileName);
			JsonElement unlocked_coll_tiers_element = Utils.getElement(profileInfo, "unlocked_coll_tiers");
			JsonElement crafted_generators_element = Utils.getElement(profileInfo, "crafted_generators");
			JsonObject fakeMember = new JsonObject();
			fakeMember.add("crafted_generators", crafted_generators_element);
			coopMembers.add(coopMembers.size(), fakeMember);
			JsonElement collectionInfoElement = Utils.getElement(profileInfo, "collection");

			if (unlocked_coll_tiers_element == null || collectionInfoElement == null) {
				return null;
			}

			JsonObject collectionInfo = new JsonObject();
			JsonObject collectionTiers = new JsonObject();
			JsonObject minionTiers = new JsonObject();
			JsonObject personalAmounts = new JsonObject();
			JsonObject totalAmounts = new JsonObject();

			if (collectionInfoElement.isJsonObject()) {
				personalAmounts = collectionInfoElement.getAsJsonObject();
			}

			for (Map.Entry<String, JsonElement> entry : personalAmounts.entrySet()) {
				totalAmounts.addProperty(entry.getKey(), entry.getValue().getAsLong());
			}

			List<JsonObject> coopProfiles = getCoopProfileInformation(profileName);
			if (coopProfiles != null) {
				for (JsonObject coopProfile : coopProfiles) {
					JsonElement coopCollectionInfoElement = Utils.getElement(coopProfile, "collection");
					if (coopCollectionInfoElement != null && coopCollectionInfoElement.isJsonObject()) {
						for (Map.Entry<String, JsonElement> entry : coopCollectionInfoElement.getAsJsonObject().entrySet()) {
							float existing = Utils.getElementAsFloat(totalAmounts.get(entry.getKey()), 0);
							totalAmounts.addProperty(entry.getKey(), existing + entry.getValue().getAsLong());
						}
					}
				}
			}

			if (unlocked_coll_tiers_element.isJsonArray()) {
				JsonArray unlocked_coll_tiers = unlocked_coll_tiers_element.getAsJsonArray();
				for (int i = 0; i < unlocked_coll_tiers.size(); i++) {
					String unlocked = unlocked_coll_tiers.get(i).getAsString();

					Matcher matcher = COLL_TIER_PATTERN.matcher(unlocked);

					if (matcher.find()) {
						String tier_str = matcher.group(1);
						int tier = Integer.parseInt(tier_str);
						String coll = unlocked.substring(0, unlocked.length() - (matcher.group().length()));
						if (!collectionTiers.has(coll) || collectionTiers.get(coll).getAsInt() < tier) {
							collectionTiers.addProperty(coll, tier);
						}
					}
				}
			}
			for (JsonObject current_member_info : coopMembers) {
				if (
					!current_member_info.has("crafted_generators") || !current_member_info.get("crafted_generators").isJsonArray()
				) continue;
				JsonArray crafted_generators = Utils.getElement(current_member_info, "crafted_generators").getAsJsonArray();
				for (int j = 0; j < crafted_generators.size(); j++) {
					String unlocked = crafted_generators.get(j).getAsString();
					Matcher matcher = COLL_TIER_PATTERN.matcher(unlocked);
					if (matcher.find()) {
						String tierString = matcher.group(1);
						int tier = Integer.parseInt(tierString);
						String coll = unlocked.substring(0, unlocked.length() - (matcher.group().length()));
						if (!minionTiers.has(coll) || minionTiers.get(coll).getAsInt() < tier) {
							minionTiers.addProperty(coll, tier);
						}
					}
				}
			}

			JsonObject maxAmount = new JsonObject();
			JsonObject updatedCollectionTiers = new JsonObject();
			for (Map.Entry<String, JsonElement> totalAmountsEntry : totalAmounts.entrySet()) {
				String collName = totalAmountsEntry.getKey();
				int collTier = (int) Utils.getElementAsFloat(collectionTiers.get(collName), 0);

				int currentAmount = (int) Utils.getElementAsFloat(totalAmounts.get(collName), 0);
				if (currentAmount > 0) {
					for (Map.Entry<String, JsonElement> resourceEntry : resourceCollectionInfo.entrySet()) {
						JsonElement tiersElement = Utils.getElement(resourceEntry.getValue(), "items." + collName + ".tiers");
						if (tiersElement != null && tiersElement.isJsonArray()) {
							JsonArray tiers = tiersElement.getAsJsonArray();
							int maxTierAcquired = -1;
							int maxAmountRequired = -1;
							for (int i = 0; i < tiers.size(); i++) {
								JsonObject tierInfo = tiers.get(i).getAsJsonObject();
								int tier = tierInfo.get("tier").getAsInt();
								int amountRequired = tierInfo.get("amountRequired").getAsInt();
								if (currentAmount >= amountRequired) {
									maxTierAcquired = tier;
								}
								maxAmountRequired = amountRequired;
							}
							if (maxTierAcquired >= 0 && maxTierAcquired > collTier) {
								updatedCollectionTiers.addProperty(collName, maxTierAcquired);
							}
							maxAmount.addProperty(collName, maxAmountRequired);
						}
					}
				}
			}

			for (Map.Entry<String, JsonElement> collectionTiersEntry : updatedCollectionTiers.entrySet()) {
				collectionTiers.add(collectionTiersEntry.getKey(), collectionTiersEntry.getValue());
			}

			collectionInfo.add("minion_tiers", minionTiers);
			collectionInfo.add("max_amounts", maxAmount);
			collectionInfo.add("personal_amounts", personalAmounts);
			collectionInfo.add("total_amounts", totalAmounts);
			collectionInfo.add("collection_tiers", collectionTiers);

			collectionInfoMap.put(profileName, collectionInfo);

			return collectionInfo;
		}

		public PlayerStats.Stats getPassiveStats(String profileName) {
			if (passiveStats.get(profileName) != null) return passiveStats.get(profileName);
			JsonObject profileInfo = getProfileInformation(profileName);
			if (profileInfo == null) return null;

			PlayerStats.Stats passiveStats = PlayerStats.getPassiveBonuses(getSkyblockInfo(profileName), profileInfo);

			if (passiveStats != null) {
				passiveStats.add(PlayerStats.getBaseStats());
			}

			this.passiveStats.put(profileName, passiveStats);

			return passiveStats;
		}

		public PlayerStats.Stats getStats(String profileName) {
			if (stats.get(profileName) != null) return stats.get(profileName);
			JsonObject profileInfo = getProfileInformation(profileName);
			if (profileInfo == null) {
				return null;
			}

			PlayerStats.Stats stats = PlayerStats.getStats(
				getSkyblockInfo(profileName),
				getInventoryInfo(profileName),
				getCollectionInfo(profileName),
				getPetsInfo(profileName),
				profileInfo
			);
			if (stats == null) return null;
			this.stats.put(profileName, stats);
			return stats;
		}

		public String getUuid() {
			return uuid;
		}

		public @Nullable JsonObject getHypixelProfile() {
			return uuidToHypixelProfile.getOrDefault(uuid, null);
		}
	}
}
