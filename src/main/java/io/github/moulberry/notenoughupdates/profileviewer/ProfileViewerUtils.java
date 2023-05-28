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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class ProfileViewerUtils {

	public static JsonArray readInventoryInfo(JsonObject profileInfo, String bagName) {
		String bytes = Utils.getElementAsString(Utils.getElement(profileInfo, bagName + ".data"), "Hz8IAAAAAAAAAD9iYD9kYD9kAAMAPwI/Gw0AAAA=");

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
			return new AbstractMap.SimpleEntry<>(tag.getCompoundTag("ExtraAttributes").getString("id"),
				Utils.getRarityFromLore(lastElementJsonArray)
			);
		}).sorted(Comparator.comparingInt(e -> -e.getValue())).collect(Collectors.toMap(
			Map.Entry::getKey, Map.Entry::getValue, (v1, v2) -> v1, LinkedHashMap::new
		));

		Set<String> ignoredTalismans = new HashSet<>();
		int powerAmount = 0;
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
}
