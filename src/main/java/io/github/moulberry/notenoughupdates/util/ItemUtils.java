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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.miscfeatures.PetInfoOverlay;
import io.github.moulberry.notenoughupdates.profileviewer.GuiProfileViewer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.EnumChatFormatting;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ItemUtils {

	public static ItemStack getCoinItemStack(long coinAmount) {
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
			"\u00A7r\u00A76" + NumberFormat.getInstance(Locale.US).format(coinAmount) + " Coins",
			uuid,
			texture
		);
		NBTTagCompound extraAttributes = skull.getTagCompound().getCompoundTag("ExtraAttributes");
		extraAttributes.setString("id", "SKYBLOCK_COIN");
		skull.getTagCompound().setTag("ExtraAttributes", extraAttributes);
		return skull;
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

	public static List<String> getLore(ItemStack is) {
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

	public static ItemStack getPetLore(PetInfoOverlay.Pet currentPet) {
		JsonObject pet = NotEnoughUpdates.INSTANCE.manager.getJsonForItem(NotEnoughUpdates.INSTANCE.manager.createItem(currentPet.getPetId(false)));
		String petname = currentPet.petType;
		String tier = Utils.getRarityFromInt(currentPet.rarity.petId).toUpperCase();
		String heldItem = currentPet.petItem;
		String skin = currentPet.skin;
		JsonObject heldItemJson = heldItem == null ? null : NotEnoughUpdates.INSTANCE.manager.getItemInformation().get(heldItem);
		String tierNum = GuiProfileViewer.MINION_RARITY_TO_NUM.get(tier);
		float exp = currentPet.petLevel.totalXp;
		if (tierNum == null) return null;

		if (heldItem != null && heldItem.equals("PET_ITEM_TIER_BOOST")) {
			tierNum = "" + (Integer.parseInt(tierNum) + 1);
		}

		GuiProfileViewer.PetLevel levelObj = GuiProfileViewer.getPetLevel(petname, tier, exp);

		float level = levelObj.level;
		float currentLevelRequirement = levelObj.currentLevelRequirement;
		float maxXP = levelObj.maxXP;
		pet.addProperty("level", level);
		pet.addProperty("currentLevelRequirement", currentLevelRequirement);
		pet.addProperty("maxXP", maxXP);

		JsonObject petItem = NotEnoughUpdates.INSTANCE.manager.getItemInformation().get(petname + ";" + tierNum);
		ItemStack stack;
		if (petItem == null) {
			return null;
		} else {
			stack = NotEnoughUpdates.INSTANCE.manager.jsonToStack(petItem, false, false);
			HashMap<String, String> replacements = NotEnoughUpdates.INSTANCE.manager.getLoreReplacements(
				petname,
				tier,
				(int) Math.floor(level)
			);

			if (heldItem != null) {
				HashMap<String, Float> petStatBoots = GuiProfileViewer.PET_STAT_BOOSTS.get(heldItem);
				HashMap<String, Float> petStatBootsMult = GuiProfileViewer.PET_STAT_BOOSTS_MULT.get(heldItem);
				if (petStatBoots != null) {
					for (Map.Entry<String, Float> entryBoost : petStatBoots.entrySet()) {
						try {
							float value = Float.parseFloat(replacements.get(entryBoost.getKey()));
							replacements.put(entryBoost.getKey(), String.valueOf((int) Math.floor(value + entryBoost.getValue())));
						} catch (Exception ignored) {}
					}
				}
				if (petStatBootsMult != null) {
					for (Map.Entry<String, Float> entryBoost : petStatBootsMult.entrySet()) {
						try {
							float value = Float.parseFloat(replacements.get(entryBoost.getKey()));
							replacements.put(entryBoost.getKey(), String.valueOf((int) Math.floor(value * entryBoost.getValue())));
						} catch (Exception ignored) {}
					}
				}
			}

			NBTTagCompound tag = stack.getTagCompound() == null ? new NBTTagCompound() : stack.getTagCompound();
			if (tag.hasKey("display", 10)) {
				NBTTagCompound display = tag.getCompoundTag("display");
				if (display.hasKey("Lore", 9)) {
					NBTTagList newLore = new NBTTagList();
					NBTTagList lore = display.getTagList("Lore", 8);
					HashMap<Integer, Integer> blankLocations = new HashMap<>();
					for (int j = 0; j < lore.tagCount(); j++) {
						String line = lore.getStringTagAt(j);
						if (line.trim().isEmpty()) {
							blankLocations.put(blankLocations.size(), j);
						}
						for (Map.Entry<String, String> replacement : replacements.entrySet()) {
							line = line.replace("{" + replacement.getKey() + "}", replacement.getValue());
						}
						newLore.appendTag(new NBTTagString(line));
					}
					Integer secondLastBlank = blankLocations.get(blankLocations.size() - 2);
					if (skin != null) {
						JsonObject petSkin = NotEnoughUpdates.INSTANCE.manager.getItemInformation().get("PET_SKIN_" + skin);
						if (petSkin != null) {
							try {
								NBTTagCompound nbt = JsonToNBT.getTagFromJson(petSkin.get("nbttag").getAsString());
								tag.setTag("SkullOwner", nbt.getTag("SkullOwner"));
								String name = petSkin.get("displayname").getAsString();
								if (name != null) {
									name = Utils.cleanColour(name);
									newLore.set(0, new NBTTagString(newLore.get(0).toString().replace("\"", "") + ", " + name));
								}
							} catch (NBTException e) {
								e.printStackTrace();
							}
						}
					}
					for (int i = 0; i < newLore.tagCount(); i++) {
						String cleaned = Utils.cleanColour(newLore.get(i).toString());
						if (cleaned.equals("\"Right-click to add this pet to\"")) {
							newLore.removeTag(i + 1);
							newLore.removeTag(i);
							secondLastBlank = i - 1;
							break;
						}
					}
					NBTTagList temp = new NBTTagList();
					for (int i = 0; i < newLore.tagCount(); i++) {
						temp.appendTag(newLore.get(i));
						if (secondLastBlank != null && i == secondLastBlank) {
							if (heldItem != null) {
								temp.appendTag(
									new NBTTagString(
										EnumChatFormatting.GOLD + "Held Item: " + heldItemJson.get("displayname").getAsString()
									)
								);
								int blanks = 0;
								JsonArray heldItemLore = heldItemJson.get("lore").getAsJsonArray();
								for (int k = 0; k < heldItemLore.size(); k++) {
									String heldItemLine = heldItemLore.get(k).getAsString();
									if (heldItemLine.trim().isEmpty()) {
										blanks++;
									} else if (blanks == 2) {
										temp.appendTag(new NBTTagString(heldItemLine));
									} else if (blanks > 2) {
										break;
									}
								}
								temp.appendTag(new NBTTagString());
							}
							temp.removeTag(temp.tagCount() - 1);
						}
					}
					newLore = temp;
					display.setTag("Lore", newLore);
				}
				if (display.hasKey("Name", 8)) {
					String displayName = display.getString("Name");
					for (Map.Entry<String, String> replacement : replacements.entrySet()) {
						displayName = displayName.replace("{" + replacement.getKey() + "}", replacement.getValue());
					}
					display.setTag("Name", new NBTTagString(displayName));
				}
				tag.setTag("display", display);
			}

			// Adds the missing pet fields to the tag
			NBTTagCompound extraAttributes = new NBTTagCompound();
			JsonObject petInfo = new JsonObject();
			if(tag.hasKey("ExtraAttributes", 10)) {
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
			stack.setTagCompound(tag);
		}
		return stack;
	}

}
