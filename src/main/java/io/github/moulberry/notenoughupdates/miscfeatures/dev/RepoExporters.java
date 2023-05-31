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

package io.github.moulberry.notenoughupdates.miscfeatures.dev;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.itemeditor.NEUItemEditor;
import io.github.moulberry.notenoughupdates.util.ItemUtils;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StringUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class RepoExporters {

	private static final RepoExporters INSTANCE = new RepoExporters();

	public static RepoExporters getInstance() {
		return INSTANCE;
	}

	public void essenceExporter() {
		try {
			GuiChest eventGui = (GuiChest) Minecraft.getMinecraft().currentScreen;
			ContainerChest cc = (ContainerChest) eventGui.inventorySlots;
			IInventory lower = cc.getLowerChestInventory();
			File file = new File(
				Minecraft.getMinecraft().mcDataDir.getAbsolutePath(),
				"config/notenoughupdates/repo/constants/essencecosts.json"
			);
			String fileContent;
			fileContent = new BufferedReader(new InputStreamReader(
				Files.newInputStream(file.toPath()),
				StandardCharsets.UTF_8
			))
				.lines()
				.collect(Collectors.joining(System.lineSeparator()));
			String id = null;
			JsonObject jsonObject = new JsonParser().parse(fileContent).getAsJsonObject();
			JsonObject newEntry = new JsonObject();
			for (int i = 0; i < 54; i++) {
				ItemStack stack = lower.getStackInSlot(i);
				if (!stack.getDisplayName().isEmpty() && stack.getItem() != Item.getItemFromBlock(Blocks.barrier) &&
					stack.getItem() != Items.arrow) {
					if (stack.getTagCompound().getCompoundTag("display").hasKey("Lore", 9)) {
						int stars = Utils.getNumberOfStars(stack);
						if (stars == 0) continue;

						NBTTagList lore = stack.getTagCompound().getCompoundTag("display").getTagList("Lore", 8);
						int costIndex = 10000;
						id = NotEnoughUpdates.INSTANCE.manager
							.createItemResolutionQuery()
							.withItemStack(stack)
							.resolveInternalName();
						if (jsonObject.has(id)) {
							jsonObject.remove(id);
						}
						for (int j = 0; j < lore.tagCount(); j++) {
							String entry = lore.getStringTagAt(j);
							if (entry.equals("§7Cost")) {
								costIndex = j;
							}
							if (j > costIndex) {
								entry = entry.trim();
								int index = entry.lastIndexOf('x');
								String item, amountString;
								if (index < 0) {
									item = entry.trim() + " x1";
									amountString = "x1";
								} else {
									amountString = entry.substring(index);
									item = entry.substring(0, index).trim();
								}
								item = item.substring(0, item.length() - 3);
								int amount = Integer.parseInt(amountString.trim().replace("x", "").replace(",", ""));
								if (item.endsWith("Essence")) {
									int index2 = entry.indexOf("Essence");
									String typeAndAmount = item.substring(0, index2).trim().substring(2);
									int whitespaceIndex = typeAndAmount.indexOf(' ');
									int essenceAmount = Integer.parseInt(typeAndAmount
										.substring(0, whitespaceIndex)
										.replace(",", ""));
									newEntry.add("type", new JsonPrimitive(typeAndAmount.substring(whitespaceIndex + 1)));
									if (stars == -1) {
										newEntry.add("dungeonize", new JsonPrimitive(essenceAmount));
									} else {
										newEntry.add(String.valueOf(stars), new JsonPrimitive(essenceAmount));
									}
								} else if (item.endsWith("Coins")) {
									int index2 = entry.indexOf("Coins");
									String coinsAmount = item.substring(0, index2).trim().substring(2);
									if (!newEntry.has("items")) {
										newEntry.add("items", new JsonObject());
									}
									if (!newEntry.get("items").getAsJsonObject().has(String.valueOf(stars))) {
										newEntry.get("items").getAsJsonObject().add(String.valueOf(stars), new JsonArray());
									}
									newEntry
										.get("items")
										.getAsJsonObject()
										.get(String.valueOf(stars))
										.getAsJsonArray()
										.add(new JsonPrimitive("SKYBLOCK_COIN:" + coinsAmount.replace(",", "")));
								} else {
									String itemString = "_";
									for (Map.Entry<String, JsonObject> itemEntry : NotEnoughUpdates.INSTANCE.manager
										.getItemInformation()
										.entrySet()) {

										if (itemEntry.getValue().has("displayname")) {
											String name = itemEntry.getValue().get("displayname").getAsString();
											if (name.equals(item)) {
												itemString = itemEntry.getKey() + ":" + amount;
											}
										}
									}
									if (!newEntry.has("items")) {
										newEntry.add("items", new JsonObject());
									}
									if (!newEntry.get("items").getAsJsonObject().has(String.valueOf(stars))) {
										newEntry.get("items").getAsJsonObject().add(String.valueOf(stars), new JsonArray());
									}
									newEntry
										.get("items")
										.getAsJsonObject()
										.get(String.valueOf(stars))
										.getAsJsonArray()
										.add(new JsonPrimitive(itemString));
								}
							}
						}
						jsonObject.add(id, newEntry);
					}
				}
			}
			if (jsonObject.get(id).getAsJsonObject().has("items")) {
				JsonObject itemsObj = jsonObject.get(id).getAsJsonObject().get("items").getAsJsonObject();
				jsonObject.get(id).getAsJsonObject().remove("items");
				jsonObject.get(id).getAsJsonObject().add("items", itemsObj);
			}
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			try {
				try (
					BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
						Files.newOutputStream(file.toPath()),
						StandardCharsets.UTF_8
					))
				) {
					writer.write(gson.toJson(jsonObject));
					Utils.addChatMessage(EnumChatFormatting.AQUA + "Parsed and saved: " + EnumChatFormatting.WHITE + id);
				}
			} catch (IOException ignored) {
				Utils.addChatMessage(EnumChatFormatting.RED + "Error while writing file.");
			}
		} catch (Exception e) {
			e.printStackTrace();
			Utils.addChatMessage(
				EnumChatFormatting.RED + "Error while parsing inventory. Try again or check logs for details.");
		}
	}

	public void draconicAlterExporter() {
		try {
			for (int i = 0; i < 54; i++) {
				File file = null;
				String fileContent = null;
				JsonObject newEntry = new JsonObject();
				JsonObject jsonObject = null;
				String id = null;
				GuiChest eventGui = (GuiChest) Minecraft.getMinecraft().currentScreen;
				ContainerChest cc = (ContainerChest) eventGui.inventorySlots;
				IInventory lower = cc.getLowerChestInventory();
				ItemStack stack = lower.getStackInSlot(i);
				if (stack == null) continue;
				if (!stack.getDisplayName().isEmpty() && stack.getItem() != Item.getItemFromBlock(Blocks.barrier) &&
					stack.getItem() != Items.arrow) {
					if (stack.getTagCompound().getCompoundTag("display").hasKey("Lore", 9)) {

						NBTTagList lore = stack.getTagCompound().getCompoundTag("display").getTagList("Lore", 8);
						int costIndex = 10000;
						id = StringUtils.stripControlCodes(stack.getDisplayName().replace(" ", "_").toUpperCase(Locale.US));
						id = ItemUtils.fixDraconicId(id);
						if (!NotEnoughUpdates.INSTANCE.manager.isValidInternalName(id)) continue;

						file = new File(
							Minecraft.getMinecraft().mcDataDir.getAbsolutePath(),
							"config/notenoughupdates/repo/items/" + id + ".json"
						);
						fileContent = new BufferedReader(new InputStreamReader(
							Files.newInputStream(file.toPath()),
							StandardCharsets.UTF_8
						))
							.lines()
							.collect(Collectors.joining(System.lineSeparator()));
						jsonObject = new JsonParser().parse(fileContent).getAsJsonObject();

						int essence = -1;
						boolean funny = true;
						for (int j = 0; j < lore.tagCount(); j++) {
							String entry = lore.getStringTagAt(j);
							if (entry.equals("§8§m-----------------")) {
								costIndex = j;
							}
							if (j > costIndex) {
								if (j == costIndex + 1) {
									if (entry.startsWith("§7Dragon Essence: §d")) {
										essence = Integer.parseInt(entry.substring("§7Dragon Essence: §d".length()));
									} else {
										funny = false;
									}
									continue;
								} else if (j == costIndex + 2 && funny) continue;
								entry = entry.trim();
								if (!newEntry.has("dragon_items")) {
									newEntry.add("dragon_items", new JsonArray());
								}
								newEntry
									.get("dragon_items")
									.getAsJsonArray()
									.add(new JsonPrimitive(entry.trim()));
							}
						}
						if (essence != -1) jsonObject.add("dragon_essence", new JsonPrimitive(essence));
						jsonObject.add("dragon_items", newEntry.get("dragon_items"));
					}
				}
				if (jsonObject == null) continue;
				if (jsonObject.has("dragon_items")) {
					JsonArray itemsObj = jsonObject.get("dragon_items").getAsJsonArray();
					jsonObject.remove("dragon_items");
					jsonObject.add("dragon_items", itemsObj);
				}
				Gson gson = new GsonBuilder().setPrettyPrinting().create();
				try {
					try (
						BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
							Files.newOutputStream(file.toPath()),
							StandardCharsets.UTF_8
						))
					) {
						writer.write(gson.toJson(jsonObject));
						Utils.addChatMessage(
							EnumChatFormatting.AQUA + "Parsed and saved: " + EnumChatFormatting.WHITE + id);
					}
				} catch (IOException ignored) {
					Utils.addChatMessage(
						EnumChatFormatting.RED + "Error while writing file.");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(
				EnumChatFormatting.RED + "Error while parsing inventory. Try again or check logs for details."));
		}
	}


	public void essenceExporter2() {
		GuiChest eventGui = (GuiChest) Minecraft.getMinecraft().currentScreen;
		ContainerChest cc = (ContainerChest) eventGui.inventorySlots;
		IInventory lower = cc.getLowerChestInventory();

		for (int i = 9; i < 45; i++) {
			ItemStack stack = lower.getStackInSlot(i);
			if (stack == null) continue;
			if (stack.getDisplayName().isEmpty() || stack.getDisplayName().equals(" ")) continue;
			String internalName = NotEnoughUpdates.INSTANCE.manager.getInternalNameForItem(stack);
			if (internalName == null) {
				Utils.addChatMessage(
					EnumChatFormatting.RED + "ERROR: Could not get internal name for: " + EnumChatFormatting.AQUA +
						stack.getDisplayName());
				continue;
			}
			JsonObject itemObject = NotEnoughUpdates.INSTANCE.manager.getJsonForItem(stack);
			JsonArray lore = itemObject.get("lore").getAsJsonArray();
			List<String> loreList = new ArrayList<>();
			for (int j = 0; j < lore.size(); j++) loreList.add(lore.get(j).getAsString());
			if (loreList.get(loreList.size() - 1).equals("§7§eClick to view upgrades!")) {
				loreList.remove(loreList.size() - 1);
				loreList.remove(loreList.size() - 1);
			}

			JsonArray newLore = new JsonArray();
			for (String s : loreList) {
				newLore.add(new JsonPrimitive(s));
			}
			itemObject.remove("lore");
			itemObject.add("lore", newLore);

			if (!NEUItemEditor.saveOnly(internalName, itemObject)) {
				Utils.addChatMessage(
					EnumChatFormatting.RED + "ERROR: Failed to save item: " + EnumChatFormatting.AQUA + stack.getDisplayName());
			}
		}
		Utils.addChatMessage(EnumChatFormatting.AQUA + "Parsed page: " + lower.getDisplayName().getUnformattedText());
	}
}
