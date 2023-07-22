/*
 * Copyright (C) 2023 NotEnoughUpdates contributors
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
import io.github.moulberry.notenoughupdates.util.JsonUtils;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumChatFormatting;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.Base64;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ProfileViewerUtils {
	static Map<String, ItemStack> playerSkullCache = new HashMap<>();
	static Map<String, Long> lastSoopyRequestTime = new HashMap<>();
	static Map<String, JsonObject> soopyDataCache = new HashMap<>();

	public static JsonArray readInventoryInfo(JsonObject profileInfo, String bagName) {
		String bytes = Utils.getElementAsString(
			Utils.getElement(profileInfo, bagName + ".data"),
			"Hz8IAAAAAAAAAD9iYD9kYD9kAAMAPwI/Gw0AAAA="
		);

		NBTTagCompound nbt;
		try {
			nbt = CompressedStreamTools.readCompressed(
				new ByteArrayInputStream(Base64.getDecoder().decode(bytes))
			);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		NBTTagList items = nbt.getTagList("i", 10);
		JsonArray contents = new JsonArray();
		NEUManager manager = NotEnoughUpdates.INSTANCE.manager;
		for (int j = 0; j < items.tagCount(); j++) {
			contents.add(manager.getJsonFromNBTEntry(items.getCompoundTagAt(j)));
		}

		return contents;
	}

	public static int getMagicalPower(JsonArray talismanBag, JsonObject profileInfo) {
		Map<String, Integer> accessories = JsonUtils.getJsonArrayAsStream(talismanBag).map(o -> {
			try {
				return JsonToNBT.getTagFromJson(o.getAsJsonObject().get("nbttag").getAsString());
			} catch (Exception ignored) {
				return null;
			}
		}).filter(Objects::nonNull).map(tag -> {
			NBTTagList loreTagList = tag.getCompoundTag("display").getTagList("Lore", 8);
			String lastElement = loreTagList.getStringTagAt(loreTagList.tagCount() - 1);
			if (lastElement.contains(EnumChatFormatting.OBFUSCATED.toString())) {
				lastElement = lastElement.substring(lastElement.indexOf(' ')).trim().substring(4);
			}
			JsonArray lastElementJsonArray = new JsonArray();
			lastElementJsonArray.add(new JsonPrimitive(lastElement));
			return new AbstractMap.SimpleEntry<>(
				tag.getCompoundTag("ExtraAttributes").getString("id"),
				Utils.getRarityFromLore(lastElementJsonArray)
			);
		}).sorted(Comparator.comparingInt(e -> -e.getValue())).collect(Collectors.toMap(
			Map.Entry::getKey, Map.Entry::getValue, (v1, v2) -> v1, LinkedHashMap::new
		));

		Set<String> ignoredTalismans = new HashSet<>();

		// While there are several crab hats, only one of them counts towards the magical power
		boolean countedCrabHat = false;
		int powerAmount = 0;

		if (profileInfo.has("rift") && profileInfo.getAsJsonObject("rift").has("access") && profileInfo.getAsJsonObject(
			"rift").getAsJsonObject("access").has("consumed_prism")) { // should be true when existing ?
			powerAmount += 11;
		}

		for (Map.Entry<String, Integer> entry : accessories.entrySet()) {
			if (ignoredTalismans.contains(entry.getKey())) continue;

			JsonArray children = Utils
				.getElementOrDefault(Constants.PARENTS, entry.getKey(), new JsonArray())
				.getAsJsonArray();
			for (JsonElement child : children) {
				ignoredTalismans.add(child.getAsString());
			}

			if (entry.getKey().equals("HEGEMONY_ARTIFACT")) {
				switch (entry.getValue()) {
					case 4:
						powerAmount += 16;
						break;
					case 5:
						powerAmount += 22;
						break;
				}
			}

			if (entry.getKey().equals("ABICASE")) {
				if (profileInfo != null && profileInfo.has("nether_island_player_data")) {
					JsonObject data = profileInfo.get("nether_island_player_data").getAsJsonObject();
					if (data.has("abiphone") && data.get("abiphone").getAsJsonObject().has("active_contacts")) { // BatChest
						int contact = data.get("abiphone").getAsJsonObject().get("active_contacts").getAsJsonArray().size();
						powerAmount += Math.floor(contact / 2);
					}
				}
			}

			if (entry.getKey().startsWith("PARTY_HAT")) {
				if (countedCrabHat) continue;
				countedCrabHat = true;
			}

			switch (entry.getValue()) {
				case 0:
				case 6:
					powerAmount += 3;
					break;
				case 1:
				case 7:
					powerAmount += 5;
					break;
				case 2:
					powerAmount += 8;
					break;
				case 3:
					powerAmount += 12;
					break;
				case 4:
					powerAmount += 16;
					break;
				case 5:
					powerAmount += 22;
					break;
			}
		}
		return powerAmount;
	}

	public static int getLevelingCap(JsonObject leveling, String skillName) {
		JsonElement capsElement = Utils.getElement(leveling, "leveling_caps");
		return capsElement != null && capsElement.isJsonObject() && capsElement.getAsJsonObject().has(skillName)
			? capsElement.getAsJsonObject().get(skillName).getAsInt()
			: 50;
	}

	public static ProfileViewer.Level getLevel(JsonArray levelingArray, float xp, int levelCap, boolean cumulative) {
		ProfileViewer.Level levelObj = new ProfileViewer.Level();
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

	public static void saveSearch(String username) {
		if (username == null) return;
		String nameLower = username.toLowerCase();
		if (nameLower.equals(Minecraft.getMinecraft().thePlayer.getName().toLowerCase())) return;
		List<String> previousProfileSearches = NotEnoughUpdates.INSTANCE.config.hidden.previousProfileSearches;
		previousProfileSearches.remove(nameLower);
		previousProfileSearches.add(0, nameLower);
		while (previousProfileSearches.size() > 6) {
			previousProfileSearches.remove(previousProfileSearches.size() - 1);
		}
	}

	private static ItemStack fallBackSkull() {
		return Utils.createSkull(
			"Simon",
			"f3c4dfb91c7b40ac81fd462538538523",
			"ewogICJ0aW1lc3RhbXAiIDogMTY4NzQwMTM4MjY4MywKICAicHJvZmlsZUlkIiA6ICJmM2M0ZGZiOTFjN2I0MGFjODFmZDQ2MjUzODUzODUyMyIsCiAgInByb2ZpbGVOYW1lIiA6ICJTaW1vbiIsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9kN2ViYThhZWU0ZmQxMTUxMmI3ZTFhMjc5YTE0YWM2NDhlNDQzNDgxYjlmMzcxMzZhNzEwMThkMzg3Mjk0Y2YzIgogICAgfQogIH0KfQ"
		);
	}

	public static ItemStack getPlayerData(String username) {
		if (username == null) return new ItemStack(Blocks.stone);
		String nameLower = username.toLowerCase();
		if (!playerSkullCache.containsKey(nameLower)) {
			playerSkullCache.put(nameLower, fallBackSkull());

			getPlayerSkull(nameLower, skull -> {
				if (skull == null) {
					skull = fallBackSkull();
				}

				playerSkullCache.put(nameLower, skull);
			});
		}
		return playerSkullCache.get(nameLower);
	}

	private static void getPlayerSkull(String username, Consumer<ItemStack> callback) {
		if (NotEnoughUpdates.profileViewer.nameToUuid.containsKey(username) &&
			NotEnoughUpdates.profileViewer.nameToUuid.get(username) == null) {
			callback.accept(null);
			return;
		}
		NotEnoughUpdates.profileViewer.getPlayerUUID(username, uuid -> {
			if (uuid == null) {
				callback.accept(null);
			} else {
				NotEnoughUpdates.INSTANCE.manager.apiUtils
					.request()
					.url("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid + "?unsigned=false")
					.requestJson()
					.thenAcceptAsync(jsonObject -> {
						if (jsonObject.has("properties") && jsonObject.get("properties").isJsonArray()) {
							JsonArray propertiesArray = jsonObject.getAsJsonArray("properties");
							if (propertiesArray.size() > 0) {
								JsonObject textureObject = propertiesArray.get(0).getAsJsonObject();
								if (textureObject.has("value") && textureObject.get("value").isJsonPrimitive()
									&& textureObject.get("value").getAsJsonPrimitive().isString()) {

									ItemStack skull = Utils.createSkull(
										username,
										uuid,
										textureObject.get("value").getAsString()
									);
									callback.accept(skull);
								}
							}
						}
					});
			}
		});
	}
}
