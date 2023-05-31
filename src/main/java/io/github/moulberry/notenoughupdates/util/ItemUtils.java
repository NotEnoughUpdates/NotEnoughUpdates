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

package io.github.moulberry.notenoughupdates.util;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.core.util.StringUtils;
import io.github.moulberry.notenoughupdates.listener.ItemTooltipListener;
import io.github.moulberry.notenoughupdates.miscfeatures.PetInfoOverlay;
import io.github.moulberry.notenoughupdates.profileviewer.GuiProfileViewer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;

import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

public class ItemUtils {
	private static final Gson smallPrintingGson = new Gson();

	public static ItemStack createSkullItemStack(String displayName, String uuid, String skinAbsoluteUrl) {
		JsonObject object = new JsonObject();
		JsonObject textures = new JsonObject();
		JsonObject skin = new JsonObject();
		skin.addProperty("url", skinAbsoluteUrl);
		textures.add("SKIN", skin);
		object.add("textures", textures);
		String json = smallPrintingGson.toJson(object);
		return Utils.createSkull(
			displayName,
			uuid,
			Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8))
		);
	}

	public static ItemStack getCoinItemStack(double coinAmount) {
		String uuid = "2070f6cb-f5db-367a-acd0-64d39a7e5d1b";
		String texture =
			"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTM4MDcxNzIxY2M1YjRjZDQwNmNlNDMxYTEzZjg2MDgzYTg5NzNlMTA2NGQyZjg4OTc4Njk5MzBlZTZlNTIzNyJ9fX0=";
		if (coinAmount >= 100000) {
			uuid = "94fa2455-2881-31fe-bb4e-e3e24d58dbe3";
			texture =
				"eyJ0aW1lc3RhbXAiOjE2MzU5NTczOTM4MDMsInByb2ZpbGVJZCI6ImJiN2NjYTcxMDQzNDQ0MTI4ZDMwODllMTNiZGZhYjU5IiwicHJvZmlsZU5hbWUiOiJsYXVyZW5jaW8zMDMiLCJzaWduYXR1cmVSZXF1aXJlZCI6dHJ1ZSwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2M5Yjc3OTk5ZmVkM2EyNzU4YmZlYWYwNzkzZTUyMjgzODE3YmVhNjQwNDRiZjQzZWYyOTQzM2Y5NTRiYjUyZjYiLCJtZXRhZGF0YSI6eyJtb2RlbCI6InNsaW0ifX19fQo=";
		}
		if (coinAmount >= 10000000) {
			uuid = "0af8df1f-098c-3b72-ac6b-65d65fd0b668";
			texture =
				"ewogICJ0aW1lc3RhbXAiIDogMTYzNTk1NzQ4ODQxNywKICAicHJvZmlsZUlkIiA6ICJmNThkZWJkNTlmNTA0MjIyOGY2MDIyMjExZDRjMTQwYyIsCiAgInByb2ZpbGVOYW1lIiA6ICJ1bnZlbnRpdmV0YWxlbnQiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvN2I5NTFmZWQ2YTdiMmNiYzIwMzY5MTZkZWM3YTQ2YzRhNTY0ODE1NjRkMTRmOTQ1YjZlYmMwMzM4Mjc2NmQzYiIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9";
		}
		ItemStack skull = Utils.createSkull(
			"§r§6" + StringUtils.formatNumber(coinAmount) + " Coins",
			uuid,
			texture
		);
		NBTTagCompound extraAttributes = skull.getTagCompound().getCompoundTag("ExtraAttributes");
		extraAttributes.setString("id", "SKYBLOCK_COIN");
		skull.getTagCompound().setTag("ExtraAttributes", extraAttributes);
		return skull;
	}

	public static ItemStack createQuestionMarkSkull(String label) {
		return Utils.createSkull(
			label,
			"00000000-0000-0000-0000-000000000000",
			"bc8ea1f51f253ff5142ca11ae45193a4ad8c3ab5e9c6eec8ba7a4fcb7bac40"
		);
	}

	public static NBTTagCompound getOrCreateTag(ItemStack is) {
		if (is.hasTagCompound()) return is.getTagCompound();
		NBTTagCompound nbtTagCompound = new NBTTagCompound();
		is.setTagCompound(nbtTagCompound);
		return nbtTagCompound;
	}

	public static void appendLore(ItemStack is, List<String> moreLore) {
		NBTTagCompound tagCompound = is.getTagCompound();
		if (tagCompound == null) {
			tagCompound = new NBTTagCompound();
		}
		NBTTagCompound display = tagCompound.getCompoundTag("display");
		NBTTagList lore = display.getTagList("Lore", 8);
		for (String s : moreLore) {
			lore.appendTag(new NBTTagString(s));
		}
		display.setTag("Lore", lore);
		tagCompound.setTag("display", display);
		is.setTagCompound(tagCompound);
	}

	public static void setLore(ItemStack is, List<String> newLore) {
		NBTTagCompound tagCompound = is.getTagCompound();
		if (tagCompound == null) {
			tagCompound = new NBTTagCompound();
		}

		NBTTagCompound display = tagCompound.getCompoundTag("display");
		NBTTagList lore = new NBTTagList();
		for (String s : newLore) {
			lore.appendTag(new NBTTagString(s));
		}
		display.setTag("Lore", lore);
		tagCompound.setTag("display", display);
		is.setTagCompound(tagCompound);
	}

	public static List<String> getLore(ItemStack is) {
		if (is == null) return new ArrayList<>();
		return getLore(is.getTagCompound());
	}

	public static List<String> getLore(NBTTagCompound tagCompound) {
		if (tagCompound == null) {
			return Collections.emptyList();
		}
		NBTTagList tagList = tagCompound.getCompoundTag("display").getTagList("Lore", 8);
		List<String> list = new ArrayList<>();
		for (int i = 0; i < tagList.tagCount(); i++) {
			list.add(tagList.getStringTagAt(i));
		}
		return list;
	}

	public static String getDisplayName(NBTTagCompound compound) {
		if (compound == null) return null;
		String string = compound.getCompoundTag("display").getString("Name");
		if (string == null || string.isEmpty())
			return null;
		return string;
	}

	public static String fixEnchantId(String enchId, boolean useId) {
		if (Constants.ENCHANTS != null && Constants.ENCHANTS.has("enchant_mapping_id") &&
			Constants.ENCHANTS.has("enchant_mapping_item")) {
			JsonArray mappingFrom = Constants.ENCHANTS.getAsJsonArray("enchant_mapping_" + (useId ? "id" : "item"));
			JsonArray mappingTo = Constants.ENCHANTS.getAsJsonArray("enchant_mapping_" + (useId ? "item" : "id"));

			for (int i = 0; i < mappingFrom.size(); i++) {
				if (mappingFrom.get(i).getAsString().equals(enchId)) {
					return mappingTo.get(i).getAsString();
				}
			}

		}
		return enchId;
	}

	/**
	 * Mutates baseValues
	 */
	public static <T> void modifyReplacement(
		Map<String, String> baseValues,
		Map<String, T> modifiers,
		BiFunction<String, T, String> mapper
	) {
		if (modifiers == null || baseValues == null) return;
		for (Map.Entry<String, T> modifier : modifiers.entrySet()) {
			String baseValue = baseValues.get(modifier.getKey());
			if (baseValue == null) continue;
			try {
				baseValues.put(modifier.getKey(), mapper.apply(baseValue, modifier.getValue()));
			} catch (Exception e) {
				System.out.println("Exception during replacement mapping: ");
				e.printStackTrace();
			}
		}
	}

	public static String applyReplacements(Map<String, String> replacements, String text) {
		for (Map.Entry<String, String> replacement : replacements.entrySet()) {
			String search = "{" + replacement.getKey() + "}";
			text = text.replace(search, replacement.getValue());
		}
		return text;
	}

	public static NBTTagCompound getExtraAttributes(ItemStack itemStack) {
		NBTTagCompound tag = getOrCreateTag(itemStack);
		NBTTagCompound extraAttributes = tag.getCompoundTag("ExtraAttributes");
		tag.setTag("ExtraAttributes", extraAttributes);
		return extraAttributes;
	}

	public static ItemStack createPetItemstackFromPetInfo(PetInfoOverlay.Pet currentPet) {
		if (currentPet == null) {
			ItemStack stack = ItemUtils.createQuestionMarkSkull(EnumChatFormatting.RED + "Unknown Pet");
			appendLore(stack, Arrays.asList(
				"§cNull Pet",
				"",
				"§cIf you expected it to be there please send a message in",
				"§c§l#neu-support §r§con §ldiscord.gg/moulberry"
			));
			return stack;
		}
		String petname = currentPet.petType;
		String tier = Utils.getRarityFromInt(currentPet.rarity.petId).toUpperCase();
		String heldItem = currentPet.petItem;
		String skin = currentPet.skin;
		JsonObject heldItemJson = heldItem == null ? null : NotEnoughUpdates.INSTANCE.manager.getItemInformation().get(
			heldItem);
		String petId = currentPet.getPetId(false);
		float exp = currentPet.petLevel.getExpTotal();

		PetLeveling.PetLevel levelObj = PetLeveling
			.getPetLevelingForPet(petname, PetInfoOverlay.Rarity.valueOf(tier))
			.getPetLevel(exp);

		int level = levelObj.getCurrentLevel();

		ItemStack petItemstack = NotEnoughUpdates.INSTANCE.manager
			.createItemResolutionQuery()
			.withKnownInternalName(petId)
			.resolveToItemStack(false);
		if (petItemstack == null) {
			petItemstack = ItemUtils.createQuestionMarkSkull(EnumChatFormatting.RED + "Unknown Pet");
			appendLore(petItemstack, Arrays.asList(
				"§cThis pet is not saved in the repository",
				"",
				"§cIf you expected it to be there please send a message in",
				"§c§l#neu-support §r§con §ldiscord.gg/moulberry"
			));
		}
		Map<String, String> replacements = NotEnoughUpdates.INSTANCE.manager.getPetLoreReplacements(
			petname,
			tier,
			MathHelper.floor_float(level)
		);

		if (heldItem != null) {
			modifyReplacement(replacements, GuiProfileViewer.PET_STAT_BOOSTS.get(heldItem), (original, modifier) ->
				"" + MathHelper.floor_float(Float.parseFloat(original) + modifier));
			modifyReplacement(replacements, GuiProfileViewer.PET_STAT_BOOSTS_MULT.get(heldItem), (original, modifier) ->
				"" + MathHelper.floor_float(Float.parseFloat(original) * modifier));
		}

		NBTTagCompound tag = getOrCreateTag(petItemstack);
		if (tag.hasKey("display", 10)) {
			NBTTagCompound displayTag = tag.getCompoundTag("display");
			if (displayTag.hasKey("Lore", 9)) {
				List<String> newLore = new ArrayList<>();
				NBTTagList lore = displayTag.getTagList("Lore", 8);
				int secondLastBlankLine = -1, lastBlankLine = -1;
				for (int j = 0; j < lore.tagCount(); j++) {
					String line = lore.getStringTagAt(j);
					if (line.trim().isEmpty()) {
						secondLastBlankLine = lastBlankLine;
						lastBlankLine = j;
					}
					line = applyReplacements(replacements, line);
					newLore.add(line);
				}
				if (skin != null) {
					JsonObject petSkin = NotEnoughUpdates.INSTANCE.manager
						.createItemResolutionQuery()
						.withKnownInternalName("PET_SKIN_" + skin)
						.resolveToItemListJson();
					if (petSkin != null) {
						try {
							NBTTagCompound nbt = JsonToNBT.getTagFromJson(petSkin.get("nbttag").getAsString());
							tag.setTag("SkullOwner", nbt.getTag("SkullOwner"));
							String name = petSkin.get("displayname").getAsString();
							if (name != null) {
								name = Utils.cleanColour(name);
								newLore.set(0, newLore.get(0) + ", " + name);
							}
						} catch (NBTException e) {
							e.printStackTrace();
						}
					}
				}
				for (int i = 0; i < newLore.size(); i++) {
					String cleaned = Utils.cleanColour(newLore.get(i));
					if (cleaned.equals("Right-click to add this pet to")) {
						if (heldItem == null) newLore.remove(i + 2);
						newLore.remove(i + 1);
						newLore.remove(i);
						secondLastBlankLine = i - 1;
						break;
					}
				}
				if (secondLastBlankLine != -1) {
					List<String> petItemLore = new ArrayList<>();
					if (heldItem != null) {
						if (heldItemJson == null) {
							petItemLore.add(EnumChatFormatting.RED + "Could not find held item in repo!");
						} else {
							petItemLore.add(EnumChatFormatting.GOLD + "Held Item: " + heldItemJson.get("displayname").getAsString());
							List<String> heldItemLore = JsonUtils.getJsonArrayOrEmpty(heldItemJson, "lore", JsonElement::getAsString);
							int blanks = 0;
							for (String heldItemLoreLine : heldItemLore) {
								if (heldItemLoreLine.trim().isEmpty()) {
									blanks++;
								} else if (blanks == 2) {
									petItemLore.add(heldItemLoreLine);
								} else if (blanks > 2) {
									break;
								}
							}
						}
					}
					if (currentPet.candyUsed > 0) {
						if (petItemLore.size() > 0) {
							petItemLore.add("");
						}
						petItemLore.add("§a(" + currentPet.candyUsed + "/10) Pet Candy Used");
						if (heldItem == null) petItemLore.add("");
					}
					newLore.addAll(secondLastBlankLine + 1, petItemLore);
				}
				NBTTagList temp = new NBTTagList();
				for (String loreLine : newLore) {
					temp.appendTag(new NBTTagString(loreLine));
				}
				displayTag.setTag("Lore", temp);
			}

			if (displayTag.hasKey("Name", 8)) {
				String displayName = displayTag.getString("Name");
				displayName = applyReplacements(replacements, displayName);
				displayTag.setTag("Name", new NBTTagString(displayName));
			}
			tag.setTag("display", displayTag);
		}

		// Adds the missing pet fields to the tag
		NBTTagCompound extraAttributes = new NBTTagCompound();
		JsonObject petInfo = new JsonObject();
		if (tag.hasKey("ExtraAttributes", 10)) {
			extraAttributes = tag.getCompoundTag("ExtraAttributes");
			if (extraAttributes.hasKey("petInfo", 8)) {
				petInfo = new JsonParser().parse(extraAttributes.getString("petInfo")).getAsJsonObject();
			}
		}
		petInfo.addProperty("exp", exp);
		petInfo.addProperty("tier", tier);
		petInfo.addProperty("type", petname);
		if (heldItem != null) {
			petInfo.addProperty("heldItem", heldItem);
		}
		if (skin != null) {
			petInfo.addProperty("skin", skin);
		}
		extraAttributes.setString("petInfo", petInfo.toString());
		tag.setTag("ExtraAttributes", extraAttributes);
		petItemstack.setTagCompound(tag);
		return petItemstack;
	}

	private static final DecimalFormat decimalFormatter = new DecimalFormat("#,###,###.###");

	public static ItemStack petToolTipXPExtendPetOverlay(ItemStack stack) {
		NBTTagCompound tag = stack.getTagCompound() == null ? new NBTTagCompound() : stack.getTagCompound();
		if (tag.hasKey("display", 10)) {
			NBTTagCompound display = tag.getCompoundTag("display");
			if (display.hasKey("Lore", 9)) {
				NBTTagList lore = display.getTagList("Lore", 8);
				if (Utils.cleanColour(lore.getStringTagAt(0)).matches(ItemTooltipListener.petToolTipRegex) &&
					lore.tagCount() > 7) {

					PetLeveling.PetLevel petLevel;

					PetInfoOverlay.Pet pet = PetInfoOverlay.getPetFromStack(
						stack.getTagCompound()
					);
					if (pet == null) return stack;
					petLevel = pet.petLevel;
					if (petLevel == null) return stack;

					NBTTagList newLore = new NBTTagList();
					int maxLvl = 100;
					if (Constants.PETS != null && Constants.PETS.has("custom_pet_leveling") &&
						Constants.PETS.getAsJsonObject("custom_pet_leveling").has(pet.petType.toUpperCase()) &&
						Constants.PETS.getAsJsonObject("custom_pet_leveling").getAsJsonObject(pet.petType.toUpperCase()).has(
							"max_level")) {
						maxLvl =
							Constants.PETS
								.getAsJsonObject("custom_pet_leveling")
								.getAsJsonObject(pet.petType.toUpperCase())
								.get("max_level")
								.getAsInt();
					}
					for (int i = 0; i < lore.tagCount(); i++) {
						if (i == lore.tagCount() - 2) {
							newLore.appendTag(new NBTTagString(""));
							if (petLevel.getCurrentLevel() >= maxLvl) {
								newLore.appendTag(new NBTTagString(
									EnumChatFormatting.AQUA + "" + EnumChatFormatting.BOLD + "MAX LEVEL"));
							} else {
								double levelPercent = (Math.round(petLevel.getPercentageToNextLevel() * 1000) / 10.0);
								newLore.appendTag(new NBTTagString(
									EnumChatFormatting.GRAY + "Progress to Level " + (petLevel.getCurrentLevel() + 1) + ": " +
										EnumChatFormatting.YELLOW + levelPercent + "%"));
								StringBuilder sb = new StringBuilder();

								for (int j = 0; j < 20; j++) {
									if (j < (levelPercent / 5)) {
										sb.append(EnumChatFormatting.DARK_GREEN);
									} else {
										sb.append(EnumChatFormatting.WHITE);
									}
									sb.append(EnumChatFormatting.BOLD + "" + EnumChatFormatting.STRIKETHROUGH + " ");
								}
								newLore.appendTag(new NBTTagString(sb.toString()));
								newLore.appendTag(new NBTTagString(
									EnumChatFormatting.GRAY + "EXP: " + EnumChatFormatting.YELLOW +
										decimalFormatter.format(petLevel.getExpInCurrentLevel()) +
										EnumChatFormatting.GOLD + "/" + EnumChatFormatting.YELLOW +
										decimalFormatter.format(petLevel.getExpRequiredForNextLevel())
								));
							}
						}
						newLore.appendTag(lore.get(i));
					}
					display.setTag("Lore", newLore);
					tag.setTag("display", display);
				}
			}
		}
		stack.setTagCompound(tag);
		return stack;
	}

	public static boolean isSoulbound(ItemStack item) {
		return ItemUtils.getLore(item).stream()
										.anyMatch(line -> line.equals("§8§l* §8Co-op Soulbound §8§l*") ||
											line.equals("§8§l* Soulbound §8§l*"));
	}

	public static String fixDraconicId(String id) {
		if (id.equals("ASPECT_OF_THE_DRAGONS")) return "ASPECT_OF_THE_DRAGON";
		if (id.equals("ENDER_HELMET")) return "END_HELMET";
		if (id.equals("ENDER_CHESTPLATE")) return "END_CHESTPLATE";
		if (id.equals("ENDER_LEGGINGS")) return "END_LEGGINGS";
		if (id.equals("ENDER_BOOTS")) return "END_BOOTS";
		return id;
	}

}
