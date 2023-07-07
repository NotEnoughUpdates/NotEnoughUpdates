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

package io.github.moulberry.notenoughupdates.infopanes;

import com.google.gson.JsonObject;
import io.github.moulberry.notenoughupdates.NEUManager;
import io.github.moulberry.notenoughupdates.NEUOverlay;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Provides some dev functions used to help with adding new items/detecting missing items.
 */
public class DevInfoPane extends TextInfoPane {
	public DevInfoPane(NEUOverlay overlay, NEUManager manager) {
		super(overlay, manager, "Missing Items", "");
		text = getText();
	}

	public String getText() {
		StringBuilder text = new StringBuilder(0);

		for (String internalname : manager.auctionManager.getItemAuctionInfoKeySet()) {
			if (internalname.matches("^.*-[0-9]{1,3}$")) continue;
			if (!manager.isValidInternalName(internalname)) {
				if (internalname.equals("RUNE") || internalname.contains("PARTY_HAT_CRAB") || internalname.equals("ABICASE") ||
					internalname.equals("PARTY_HAT_SLOTH"))
					continue;
				text.append(internalname).append("\n");
			}
		}
		return text.toString();
	}

	//#region add vanilla items
	AtomicBoolean running = new AtomicBoolean(false);
	ScheduledExecutorService ses = Executors.newScheduledThreadPool(1);

	String[] bukkitList = new String[]{
		"ACACIA_DOOR_ITEM",
		"ACACIA_FENCE",
		"ACACIA_FENCE_GATE",
		"ACACIA_STAIRS",
		"ACTIVATOR_RAIL",
		"ANVIL",
		"APPLE",
		"ARMOR_STAND",
		"ARROW",
		"BAKED_POTATO",
		"BANNER",
		"BARRIER",
		"BEACON",
		"BED",
		"BEDROCK",
		"BIRCH_DOOR_ITEM",
		"BIRCH_FENCE",
		"BIRCH_FENCE_GATE",
		"BIRCH_WOOD_STAIRS@birch_stairs",
		"BLAZE_POWDER",
		"BLAZE_ROD",
		"BOAT",
		"BONE",
		"BOOK",
		"BOOK_AND_QUILL@writable_book",
		"BOOKSHELF",
		"BOW",
		"BOWL",
		"BREAD",
		"BREWING_STAND_ITEM",
		"BRICK@brick_block",
		"BRICK_STAIRS",
		"BROWN_MUSHROOM",
		"BUCKET",
		"CACTUS",
		"CAKE",
		"CARPET",
		"CARROT_ITEM",
		"CARROT_STICK@carrot_on_a_stick",
		"CAULDRON_ITEM",
		"CHAINMAIL_BOOTS",
		"CHAINMAIL_CHESTPLATE",
		"CHAINMAIL_HELMET",
		"CHAINMAIL_LEGGINGS",
		"CHEST",
		"CLAY",
		"CLAY_BALL",
		"CLAY_BRICK@brick",
		"COAL",
		"COAL_BLOCK",
		"COAL_ORE",
		"COBBLE_WALL@cobblestone_wall",
		"COBBLESTONE",
		"COBBLESTONE_STAIRS@stone_stairs",
		"COMMAND@command_block",
		"COMMAND_MINECART@command_block_minecart",
		"COMPASS",
		"COOKED_BEEF",
		"COOKED_CHICKEN",
		"COOKED_FISH",
		"COOKED_MUTTON",
		"COOKED_RABBIT",
		"COOKIE",
		"DARK_OAK_DOOR_ITEM",
		"DARK_OAK_FENCE",
		"DARK_OAK_FENCE_GATE",
		"DARK_OAK_STAIRS",
		"DAYLIGHT_DETECTOR",
		"DEAD_BUSH@deadbush",
		"DETECTOR_RAIL",
		"DIAMOND",
		"DIAMOND_AXE",
		"DIAMOND_BARDING@diamond_horse_armor",
		"DIAMOND_BLOCK",
		"DIAMOND_BOOTS",
		"DIAMOND_CHESTPLATE",
		"DIAMOND_HELMET",
		"DIAMOND_HOE",
		"DIAMOND_LEGGINGS",
		"DIAMOND_ORE",
		"DIAMOND_PICKAXE",
		"DIAMOND_SPADE@diamond_shovel",
		"DIAMOND_SWORD",
		"DIODE@repeater",
		"DIRT",
		"DISPENSER",
		"DOUBLE_PLANT",
		"DRAGON_EGG",
		"DROPPER",
		"EGG",
		"EMERALD",
		"EMERALD_BLOCK",
		"EMERALD_ORE",
		"EMPTY_MAP@map",
		"ENCHANTED_BOOK",
		"ENCHANTMENT_TABLE@enchanting_table",
		"ENDER_CHEST",
		"ENDER_PEARL",
		"ENDER_PORTAL_FRAME@end_portal_frame",
		"ENDER_STONE@end_stone",
		"EXP_BOTTLE@experience_bottle",
		"EXPLOSIVE_MINECART@tnt_minecart",
		"EYE_OF_ENDER@ender_eye",
		"FEATHER",
		"FENCE",
		"FENCE_GATE",
		"FERMENTED_SPIDER_EYE",
		"FIREBALL@fire_charge",
		"FIREWORK@fireworks",
		"FIREWORK_CHARGE",
		"FISHING_ROD",
		"FLINT",
		"FLINT_AND_STEEL",
		"FLOWER_POT_ITEM",
		"FURNACE",
		"GHAST_TEAR",
		"GLASS",
		"GLASS_BOTTLE",
		"GLOWSTONE",
		"GLOWSTONE_DUST",
		"GOLD_AXE@golden_axe",
		"GOLD_BARDING@golden_horse_armor",
		"GOLD_BLOCK",
		"GOLD_BOOTS@golden_boots",
		"GOLD_CHESTPLATE@golden_chestplate",
		"GOLD_HELMET@golden_helmet",
		"GOLD_HOE@golden_hoe",
		"GOLD_INGOT",
		"GOLD_LEGGINGS@golden_leggings",
		"GOLD_NUGGET",
		"GOLD_ORE",
		"GOLD_PICKAXE@golden_pickaxe",
		"GOLD_PLATE@light_weighted_pressure_plate",
		"GOLD_RECORD@record_13",
		"GOLD_SPADE@golden_shovel",
		"GOLD_SWORD@golden_sword",
		"GOLDEN_APPLE",
		"GOLDEN_CARROT",
		"GRASS",
		"GRAVEL",
		"GREEN_RECORD@record_cat",
		"GRILLED_PORK@cooked_porkchop",
		"HARD_CLAY@hardened_clay",
		"HAY_BLOCK",
		"HOPPER",
		"HOPPER_MINECART",
		"ICE",
		"INK_SACK@dye",
		"IRON_AXE",
		"IRON_BARDING@iron_horse_armor",
		"IRON_BLOCK",
		"IRON_BOOTS",
		"IRON_CHESTPLATE",
		"IRON_DOOR",
		"IRON_FENCE@iron_bars",
		"IRON_HELMET",
		"IRON_HOE",
		"IRON_INGOT",
		"IRON_LEGGINGS",
		"IRON_ORE",
		"IRON_PICKAXE",
		"IRON_PLATE@heavy_weighted_pressure_plate",
		"IRON_SPADE@iron_shovel",
		"IRON_SWORD",
		"IRON_TRAPDOOR",
		"ITEM_FRAME",
		"JACK_O_LANTERN@lit_pumpkin",
		"JUKEBOX",
		"JUNGLE_DOOR_ITEM",
		"JUNGLE_FENCE",
		"JUNGLE_FENCE_GATE",
		"JUNGLE_WOOD_STAIRS@jungle_stairs",
		"LADDER",
		"LAPIS_BLOCK",
		"LAPIS_ORE",
		"LAVA_BUCKET",
		"LEASH@lead",
		"LEATHER",
		"LEATHER_BOOTS",
		"LEATHER_CHESTPLATE",
		"LEATHER_HELMET",
		"LEATHER_LEGGINGS",
		"LEAVES",
		"LEAVES_2@leaves2",
		"LEVER",
		"LOG",
		"LOG_2@log2",
		"LONG_GRASS@tallgrass",
		"MAGMA_CREAM",
		"MAP",
		"MELON",
		"MELON_BLOCK",
		"MELON_SEEDS",
		"MILK_BUCKET",
		"MINECART",
		"MOB_SPAWNER",
		"MONSTER_EGG",
		"MONSTER_EGGS@spawn_egg",
		"MOSSY_COBBLESTONE",
		"MUSHROOM_SOUP@mushroom_stew",
		"MUTTON",
		"MYCEL@mycelium",
		"NAME_TAG",
		"NETHER_BRICK",
		"NETHER_BRICK_ITEM",
		"NETHER_BRICK_STAIRS",
		"NETHER_FENCE@nether_brick_fence",
		"NETHER_STAR",
		"NETHER_WARTS@nether_wart",
		"NETHERRACK",
		"NOTE_BLOCK@noteblock",
		"OBSIDIAN",
		"PACKED_ICE",
		"PAINTING",
		"PAPER",
		"PISTON_BASE@piston",
		"PISTON_STICKY_BASE@sticky_piston",
		"POISONOUS_POTATO",
		"PORK@porkchop",
		"POTATO_ITEM",
		"POTION",
		"POWERED_MINECART@furnace_minecart",
		"POWERED_RAIL@golden_rail",
		"PRISMARINE",
		"PRISMARINE_CRYSTALS",
		"PRISMARINE_SHARD",
		"PUMPKIN",
		"PUMPKIN_PIE",
		"PUMPKIN_SEEDS",
		"QUARTZ",
		"QUARTZ_BLOCK",
		"QUARTZ_ORE",
		"QUARTZ_STAIRS",
		"RABBIT",
		"RABBIT_FOOT",
		"RABBIT_HIDE",
		"RABBIT_STEW",
		"RAILS@rail",
		"RAW_BEEF@beef",
		"RAW_CHICKEN@chicken",
		"RAW_FISH@fish",
		"RECORD_10@record_ward",
		"RECORD_11",
		"RECORD_12@record_wait",
		"RECORD_3@record_blocks",
		"RECORD_4@record_chirp",
		"RECORD_5@record_far",
		"RECORD_6@record_mall",
		"RECORD_7@record_mellohi",
		"RECORD_8@record_stal",
		"RECORD_9@record_strad",
		"RED_MUSHROOM",
		"RED_ROSE@red_flower",
		"RED_SANDSTONE",
		"RED_SANDSTONE_STAIRS",
		"REDSTONE",
		"REDSTONE_BLOCK",
		"REDSTONE_COMPARATOR@comparator",
		"REDSTONE_LAMP_OFF@redstone_lamp",
		"REDSTONE_ORE",
		"REDSTONE_TORCH_ON@redstone_torch",
		"ROTTEN_FLESH",
		"SADDLE",
		"SAND",
		"SANDSTONE",
		"SANDSTONE_STAIRS",
		"SAPLING",
		"SEA_LANTERN",
		"SEEDS@wheat_seeds",
		"SHEARS",
		"SIGN",
		"SKULL_ITEM",
		"SLIME_BALL",
		"SLIME_BLOCK@slime",
		"SMOOTH_BRICK@stonebrick",
		"SMOOTH_STAIRS@stone_brick_stairs",
		"SNOW@snow_layer",
		"SNOW_BALL@snowball",
		"SNOW_BLOCK@snow",
		"SOUL_SAND",
		"SPECKLED_MELON",
		"SPIDER_EYE",
		"SPONGE",
		"SPRUCE_DOOR_ITEM",
		"SPRUCE_FENCE",
		"SPRUCE_FENCE_GATE",
		"SPRUCE_WOOD_STAIRS@spruce_stairs",
		"STAINED_CLAY@stained_hardened_clay",
		"STAINED_GLASS",
		"STAINED_GLASS_PANE",
		"STEP@stone_slab",
		"STICK",
		"STONE",
		"STONE_AXE",
		"STONE_BUTTON",
		"STONE_HOE",
		"STONE_PICKAXE",
		"STONE_PLATE@stone_pressure_plate",
		"STONE_SLAB2",
		"STONE_SPADE@stone_shovel",
		"STONE_SWORD",
		"STORAGE_MINECART@chest_minecart",
		"STRING",
		"SUGAR",
		"SUGAR_CANE@reeds",
		"SULPHUR@gunpowder",
		"THIN_GLASS@glass_pane",
		"TNT",
		"TORCH",
		"TRAP_DOOR@trapdoor",
		"TRAPPED_CHEST",
		"TRIPWIRE_HOOK",
		"VINE",
		"WATCH@clock",
		"WATER_BUCKET",
		"WATER_LILY@waterlily",
		"WEB",
		"WHEAT",
		"WOOD@planks",
		"WOOD_AXE@wooden_axe",
		"WOOD_BUTTON@wooden_button",
		"WOOD_DOOR@wooden_door",
		"WOOD_HOE@wooden_hoe",
		"WOOD_PICKAXE@wooden_pickaxe",
		"WOOD_PLATE@wooden_pressure_plate",
		"WOOD_SPADE@wooden_shovel",
		"WOOD_STAIRS@oak_stairs",
		"WOOD_STEP@wooden_slab",
		"WOOD_SWORD@wooden_sword",
		"WOOL",
		"WORKBENCH@crafting_table",
		"WRITTEN_BOOK",
		"YELLOW_FLOWER"
	};

	private void addStack(ItemStack stackToAdd, int depth) {
		if (depth > 16) return;

		String regName2 = stackToAdd.getItem().getRegistryName().replace("minecraft:", "");
		String internalname = null;
		for (String bukkit2 : bukkitList) {
			if (bukkit2.equalsIgnoreCase(regName2) ||
				(bukkit2.contains("@") && bukkit2.split("@")[1].equalsIgnoreCase(regName2))) {
				internalname = bukkit2.split("@")[0];
				break;
			}
		}
		if (internalname == null) return;

		if (stackToAdd.getItemDamage() != 0 && stackToAdd.getItemDamage() < 32000) {
			internalname += "-" + stackToAdd.getItemDamage();
		}

		if (manager.getItemInformation().containsKey(internalname)) return;

		JsonObject recipeJson = null;
		for (IRecipe recipe : CraftingManager.getInstance().getRecipeList()) {
			ItemStack out = recipe.getRecipeOutput();
			if (out != null && out.getItem() == stackToAdd.getItem() &&
				(stackToAdd.getItemDamage() >= 32000 || out.getItemDamage() == stackToAdd.getItemDamage())) {
				recipeJson = new JsonObject();

				if (recipe instanceof ShapedRecipes) {
					ShapedRecipes shaped = (ShapedRecipes) recipe;

					String[] x = {"1", "2", "3"};
					String[] y = {"A", "B", "C"};
					for (int i = 0; i < 9; i++) {
						int xi = i % 3;
						int yi = i / 3;

						String stacki = "";

						int recipeIndex = i - (3 - shaped.recipeWidth) * yi;
						if (xi < shaped.recipeWidth && recipeIndex < shaped.recipeItems.length) {
							ItemStack stack = shaped.recipeItems[recipeIndex];
							if (stack != null) {
								if (stack.getItem() != stackToAdd.getItem() ||
									(stackToAdd.getItemDamage() < 32000 && stack.getItemDamage() != stackToAdd.getItemDamage())) {
									addStack(stack, depth + 1);
								}

								Item stackItem = stack.getItem();
								String regName = stackItem.getRegistryName().replace("minecraft:", "");
								for (String bukkit2 : bukkitList) {
									if (bukkit2.equalsIgnoreCase(regName) ||
										(bukkit2.contains("@") && bukkit2.split("@")[1].equalsIgnoreCase(regName))) {
										stacki = bukkit2.split("@")[0];
										break;
									}
								}
								if (!stacki.isEmpty()) {
									if (stack.getItemDamage() != 0 && stack.getItemDamage() < 32000) {
										stacki += "-" + stack.getItemDamage();
									}
									stacki += ":" + stack.stackSize;
								}
							}
						}

						recipeJson.addProperty(y[yi] + x[xi], stacki);
					}
					break;
				} else if (recipe instanceof ShapedOreRecipe) {
					ShapedOreRecipe shaped = (ShapedOreRecipe) recipe;
					int width = (int) Utils.getField(ShapedOreRecipe.class, recipe, "width");
					String[] x = {"1", "2", "3"};
					String[] y = {"A", "B", "C"};
					for (int i = 0; i < 9; i++) {
						int xi = i % 3;
						int yi = i / 3;

						String stacki = "";

						int recipeIndex = i - (3 - width) * yi;
						if (xi < width && recipeIndex < shaped.getRecipeSize()) {
							ItemStack stack = null;
							if (recipeIndex < shaped.getRecipeSize()) {
								Object o = shaped.getInput()[recipeIndex];
								if (o instanceof ItemStack) {
									stack = (ItemStack) o;
								} else if (o instanceof List<?>) {
									for (Object o2 : (List<?>) o) {
										if (o2 instanceof ItemStack) {
											stack = (ItemStack) o2;
											break;
										}
									}
								}
							}
							if (stack != null) {
								if (stack.getItem() != stackToAdd.getItem() ||
									(stackToAdd.getItemDamage() < 32000 && stack.getItemDamage() != stackToAdd.getItemDamage())) {
									addStack(stack, depth + 1);
								}
								Item stackItem = stack.getItem();
								String regName = stackItem.getRegistryName().replace("minecraft:", "");
								for (String bukkit2 : bukkitList) {
									if (bukkit2.equalsIgnoreCase(regName) || bukkit2.equalsIgnoreCase(regName + "_ITEM") ||
										(bukkit2.contains("@") && bukkit2.split("@")[1].equalsIgnoreCase(regName))) {
										stacki = bukkit2.split("@")[0];
										break;
									}
								}
								if (!stacki.isEmpty()) {
									if (stack.getItemDamage() != 0 && stack.getItemDamage() < 32000) {
										stacki += "-" + stack.getItemDamage();
									}
									stacki += ":1";
								}
							}
						}

						recipeJson.addProperty(y[yi] + x[xi], stacki);
					}
				} else if (recipe instanceof ShapelessRecipes) {
					ShapelessRecipes shapeless = (ShapelessRecipes) recipe;
					String[] x = {"1", "2", "3"};
					String[] y = {"A", "B", "C"};
					for (int i = 0; i < 9; i++) {
						int xi = i % 3;
						int yi = i / 3;

						String stacki = "";

						ItemStack stack = null;
						if (i < shapeless.recipeItems.size()) {
							stack = shapeless.recipeItems.get(i);
						}
						if (stack != null) {
							if (stack.getItem() != stackToAdd.getItem() ||
								(stackToAdd.getItemDamage() < 32000 && stack.getItemDamage() != stackToAdd.getItemDamage())) {
								addStack(stack, depth + 1);
							}
							Item stackItem = stack.getItem();
							String regName = stackItem.getRegistryName().replace("minecraft:", "");
							for (String bukkit2 : bukkitList) {
								if (bukkit2.equalsIgnoreCase(regName) || bukkit2.equalsIgnoreCase(regName + "_ITEM") ||
									(bukkit2.contains("@") && bukkit2.split("@")[1].equalsIgnoreCase(regName))) {
									stacki = bukkit2.split("@")[0];
									break;
								}
							}
							if (!stacki.isEmpty()) {
								if (stack.getItemDamage() != 0 && stack.getItemDamage() < 32000) {
									stacki += "-" + stack.getItemDamage();
								}
								stacki += ":1";
							}
						}

						recipeJson.addProperty(y[yi] + x[xi], stacki);
					}
					break;
				} else if (recipe instanceof ShapelessOreRecipe) {
					ShapelessOreRecipe shapeless = (ShapelessOreRecipe) recipe;
					String[] x = {"1", "2", "3"};
					String[] y = {"A", "B", "C"};
					for (int i = 0; i < 9; i++) {
						int xi = i % 3;
						int yi = i / 3;

						String stacki = "";

						ItemStack stack = null;
						if (i < shapeless.getRecipeSize()) {
							Object o = shapeless.getInput().get(i);
							if (o instanceof ItemStack) {
								stack = (ItemStack) o;
							} else if (o instanceof List<?>) {
								for (Object o2 : (List<?>) o) {
									if (o2 instanceof ItemStack) {
										stack = (ItemStack) o2;
										break;
									}
								}
							}
						}
						if (stack != null) {
							if (stack.getItem() != stackToAdd.getItem() ||
								(stackToAdd.getItemDamage() < 32000 && stack.getItemDamage() != stackToAdd.getItemDamage())) {
								addStack(stack, depth + 1);
							}
							Item stackItem = stack.getItem();
							String regName = stackItem.getRegistryName().replace("minecraft:", "");
							for (String bukkit2 : bukkitList) {
								if (bukkit2.equalsIgnoreCase(regName) || bukkit2.equalsIgnoreCase(regName + "_ITEM") ||
									(bukkit2.contains("@") && bukkit2.split("@")[1].equalsIgnoreCase(regName))) {
									stacki = bukkit2.split("@")[0];
									break;
								}
							}
							if (!stacki.isEmpty()) {
								if (stack.getItemDamage() != 0 && stack.getItemDamage() < 32000) {
									stacki += "-" + stack.getItemDamage();
								}
								stacki += ":1";
							}
						}

						recipeJson.addProperty(y[yi] + x[xi], stacki);
					}
					break;
				}
			}

		}
		ItemStack res = Utils.createItemStack(
			stackToAdd.getItem(),
			EnumChatFormatting.WHITE + stackToAdd.getItem().getItemStackDisplayName(stackToAdd),
			EnumChatFormatting.WHITE.toString() + EnumChatFormatting.BOLD + "COMMON"
		);
		if (stackToAdd.getItemDamage() != 0 && stackToAdd.getItemDamage() < 32000) {
			res.setItemDamage(stackToAdd.getItemDamage());
		}
		res.getTagCompound().setInteger("HideFlags", 254);
		NBTTagCompound ea = new NBTTagCompound();
		ea.setString("id", internalname);
		res.getTagCompound().setTag("ExtraAttributes", ea);

		JsonObject json = manager.getJsonForItem(res);
		if (stackToAdd.getItemDamage() != 0 && stackToAdd.getItemDamage() < 32000) {
			json.addProperty("parent", internalname.split("-")[0]);
		}

		json.addProperty("internalname", internalname);
		json.addProperty("modver", NotEnoughUpdates.VERSION);
		json.addProperty("vanilla", true);

		if (recipeJson != null) {
			json.add("recipe", recipeJson);
			json.addProperty("clickcommand", "viewrecipe");
		} else {
			json.addProperty("clickcommand", "");
		}

		json.addProperty("modver", NotEnoughUpdates.VERSION);

		try {
			Utils.addChatMessage("Added: " + internalname);
			manager.writeJsonDefaultDir(json, internalname + ".json");
			manager.loadItem(internalname);
		} catch (IOException ignored) {
		}
	}

	@Override
	public boolean keyboardInput() {
		if (running.get() || true) return false;
		if (Keyboard.isKeyDown(Keyboard.KEY_J)) {
			running.set(!running.get());

			for (String bukkit : bukkitList) {
				String internalname = bukkit.split("@")[0];
				if (true || !manager.getItemInformation().containsKey(internalname)) {
					String vanilla = internalname.toLowerCase().replace("_item", "");
					if (bukkit.contains("@")) {
						vanilla = bukkit.split("@")[1];
					}
					Item item = Item.itemRegistry.getObject(new ResourceLocation(vanilla));
					if (item == null) {
						item = Item.getItemFromBlock(Block.blockRegistry.getObject(new ResourceLocation(vanilla)));
					}
					if (item != null) {
						HashMap<Integer, JsonObject> recipeJsonForDamage = new HashMap<>();
						for (IRecipe recipe : CraftingManager.getInstance().getRecipeList()) {
							ItemStack out = recipe.getRecipeOutput();
							if (out != null && out.getItem() == item) {
								System.out.println("Found recipe for : " + internalname + ":" + recipe);
								JsonObject obj = new JsonObject();

								if (recipe instanceof ShapedRecipes) {
									ShapedRecipes shaped = (ShapedRecipes) recipe;
									String[] x = {"1", "2", "3"};
									String[] y = {"A", "B", "C"};
									for (int i = 0; i < 9; i++) {
										int xi = i % 3;
										int yi = i / 3;

										String stacki = "";

										int recipeIndex = i - (3 - shaped.recipeWidth) * yi;
										if (xi < shaped.recipeWidth && recipeIndex < shaped.recipeItems.length) {
											ItemStack stack = shaped.recipeItems[recipeIndex];
											if (stack != null) {
												addStack(stack, 0);
												Item stackItem = stack.getItem();
												String regName = stackItem.getRegistryName().replace("minecraft:", "");
												for (String bukkit2 : bukkitList) {
													if (bukkit2.equalsIgnoreCase(regName) || bukkit2.equalsIgnoreCase(regName + "_ITEM") ||
														(bukkit2.contains("@") && bukkit2.split("@")[1].equalsIgnoreCase(regName))) {
														stacki = bukkit2.split("@")[0];
														break;
													}
												}
												if (!stacki.isEmpty()) {
													if (stack.getItemDamage() != 0 && stack.getItemDamage() < 32000) {
														stacki += "-" + stack.getItemDamage();
													}
													stacki += ":1";
												}
											}
										}

										obj.addProperty(y[yi] + x[xi], stacki);
									}
									recipeJsonForDamage.put(out.getItemDamage() > 32000 ? 0 : out.getItemDamage(), obj);
								} else if (recipe instanceof ShapedOreRecipe) {
									ShapedOreRecipe shaped = (ShapedOreRecipe) recipe;
									int width = (int) Utils.getField(ShapedOreRecipe.class, recipe, "width");
									String[] x = {"1", "2", "3"};
									String[] y = {"A", "B", "C"};
									for (int i = 0; i < 9; i++) {
										int xi = i % 3;
										int yi = i / 3;

										String stacki = "";

										int recipeIndex = i - (3 - width) * yi;
										if (xi < width && recipeIndex < shaped.getRecipeSize()) {
											ItemStack stack = null;
											if (recipeIndex < shaped.getRecipeSize()) {
												Object o = shaped.getInput()[recipeIndex];
												if (o instanceof ItemStack) {
													stack = (ItemStack) o;
												} else if (o instanceof List<?>) {
													for (Object o2 : (List<?>) o) {
														if (o2 instanceof ItemStack) {
															stack = (ItemStack) o2;
															break;
														}
													}
												}
											}
											if (stack != null) {
												addStack(stack, 0);
												Item stackItem = stack.getItem();
												String regName = stackItem.getRegistryName().replace("minecraft:", "");
												for (String bukkit2 : bukkitList) {
													if (bukkit2.equalsIgnoreCase(regName) || bukkit2.equalsIgnoreCase(regName + "_ITEM") ||
														(bukkit2.contains("@") && bukkit2.split("@")[1].equalsIgnoreCase(regName))) {
														stacki = bukkit2.split("@")[0];
														break;
													}
												}
												if (!stacki.isEmpty()) {
													if (stack.getItemDamage() != 0 && stack.getItemDamage() < 32000) {
														stacki += "-" + stack.getItemDamage();
													}
													stacki += ":1";
												}
											}
										}

										obj.addProperty(y[yi] + x[xi], stacki);
									}
									recipeJsonForDamage.put(out.getItemDamage() > 32000 ? 0 : out.getItemDamage(), obj);
								} else if (recipe instanceof ShapelessRecipes) {
									ShapelessRecipes shapeless = (ShapelessRecipes) recipe;
									String[] x = {"1", "2", "3"};
									String[] y = {"A", "B", "C"};
									for (int i = 0; i < 9; i++) {
										int xi = i % 3;
										int yi = i / 3;

										String stacki = "";

										ItemStack stack = null;
										if (i < shapeless.recipeItems.size()) {
											stack = shapeless.recipeItems.get(i);
										}
										if (stack != null) {
											addStack(stack, 0);
											Item stackItem = stack.getItem();
											String regName = stackItem.getRegistryName().replace("minecraft:", "");
											for (String bukkit2 : bukkitList) {
												if (bukkit2.equalsIgnoreCase(regName) || bukkit2.equalsIgnoreCase(regName + "_ITEM") ||
													(bukkit2.contains("@") && bukkit2.split("@")[1].equalsIgnoreCase(regName))) {
													stacki = bukkit2.split("@")[0];
													break;
												}
											}
											if (!stacki.isEmpty()) {
												if (stack.getItemDamage() != 0 && stack.getItemDamage() < 32000) {
													stacki += "-" + stack.getItemDamage();
												}
												stacki += ":1";
											}
										}

										obj.addProperty(y[yi] + x[xi], stacki);
									}
									recipeJsonForDamage.put(out.getItemDamage() > 32000 ? 0 : out.getItemDamage(), obj);
									break;
								} else if (recipe instanceof ShapelessOreRecipe) {
									ShapelessOreRecipe shapeless = (ShapelessOreRecipe) recipe;
									String[] x = {"1", "2", "3"};
									String[] y = {"A", "B", "C"};
									for (int i = 0; i < 9; i++) {
										int xi = i % 3;
										int yi = i / 3;

										String stacki = "";

										ItemStack stack = null;
										if (i < shapeless.getRecipeSize()) {
											Object o = shapeless.getInput().get(i);
											if (o instanceof ItemStack) {
												stack = (ItemStack) o;
											} else if (o instanceof List<?>) {
												for (Object o2 : (List<?>) o) {
													if (o2 instanceof ItemStack) {
														stack = (ItemStack) o2;
														break;
													}
												}
											}
										}
										if (stack != null) {
											addStack(stack, 0);
											Item stackItem = stack.getItem();
											String regName = stackItem.getRegistryName().replace("minecraft:", "");
											for (String bukkit2 : bukkitList) {
												if (bukkit2.equalsIgnoreCase(regName) || bukkit2.equalsIgnoreCase(regName + "_ITEM") ||
													(bukkit2.contains("@") && bukkit2.split("@")[1].equalsIgnoreCase(regName))) {
													stacki = bukkit2.split("@")[0];
													break;
												}
											}
											if (!stacki.isEmpty()) {
												if (stack.getItemDamage() != 0 && stack.getItemDamage() < 32000) {
													stacki += "-" + stack.getItemDamage();
												}
												stacki += ":1";
											}
										}

										obj.addProperty(y[yi] + x[xi], stacki);
									}
									recipeJsonForDamage.put(out.getItemDamage() > 32000 ? 0 : out.getItemDamage(), obj);
									break;
								}
							}
						}

						if (recipeJsonForDamage.isEmpty()) {
							ItemStack res = Utils.createItemStack(
								item,
								EnumChatFormatting.WHITE + item.getItemStackDisplayName(new ItemStack(item)),
								EnumChatFormatting.WHITE.toString() + EnumChatFormatting.BOLD + "COMMON"
							);
							res.getTagCompound().setInteger("HideFlags", 254);
							NBTTagCompound ea = new NBTTagCompound();
							ea.setString("id", internalname);
							res.getTagCompound().setTag("ExtraAttributes", ea);

							JsonObject json = manager.getJsonForItem(res);
							json.addProperty("internalname", internalname);

							json.addProperty("modver", NotEnoughUpdates.VERSION);
							json.addProperty("vanilla", true);

							json.addProperty("clickcommand", "");
							try {
								Utils.addChatMessage("Added: " + internalname);
								manager.writeJsonDefaultDir(json, internalname + ".json");
								manager.loadItem(internalname);
							} catch (IOException ignored) {
							}
						} else {
							System.out.println("writing with recipe:" + internalname);
							for (Map.Entry<Integer, JsonObject> entry : recipeJsonForDamage.entrySet()) {
								ItemStack res = Utils.createItemStack(
									item,
									EnumChatFormatting.WHITE + item.getItemStackDisplayName(new ItemStack(item, 1, entry.getKey())),
									EnumChatFormatting.WHITE.toString() + EnumChatFormatting.BOLD + "COMMON"
								);
								res.setItemDamage(entry.getKey());
								res.getTagCompound().setInteger("HideFlags", 254);
								NBTTagCompound ea = new NBTTagCompound();
								ea.setString("id", internalname);
								res.getTagCompound().setTag("ExtraAttributes", ea);

								JsonObject json = manager.getJsonForItem(res);

								if (entry.getKey() != 0 && entry.getKey() < 32000) {
									json.addProperty("internalname", internalname + "-" + entry.getKey());
									json.addProperty("parent", internalname);
								} else {
									json.addProperty("internalname", internalname);
								}

								json.addProperty("modver", NotEnoughUpdates.VERSION);
								json.addProperty("vanilla", true);
								json.addProperty("clickcommand", "viewrecipe");
								json.add("recipe", entry.getValue());
								try {
									Utils.addChatMessage("Added: " + internalname);
									if (entry.getKey() != 0 && entry.getKey() < 32000) {
										manager.writeJsonDefaultDir(json, internalname + "-" + entry.getKey() + ".json");
									} else {
										manager.writeJsonDefaultDir(json, internalname + ".json");
									}
									manager.loadItem(internalname);
								} catch (IOException ignored) {
								}
							}
						}
					}
				}
			}

		}
		return false;
	}
	//#endregion
}
