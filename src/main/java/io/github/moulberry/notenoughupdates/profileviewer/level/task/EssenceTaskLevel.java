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

package io.github.moulberry.notenoughupdates.profileviewer.level.task;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.profileviewer.level.LevelPage;
import io.github.moulberry.notenoughupdates.util.Constants;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.util.EnumChatFormatting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EssenceTaskLevel extends GuiTaskLevel {

	public EssenceTaskLevel(LevelPage levelPage) {
		super(levelPage);
	}

	@Override
	public void drawTask(JsonObject object, int mouseX, int mouseY, int guiLeft, int guiTop) {
		List<String> lore = new ArrayList<>();

		JsonObject categoryXp = levelPage.getConstant().get("category_xp").getAsJsonObject();
		JsonObject essenceShopTask = levelPage.getConstant().get("essence_shop_task").getAsJsonObject();
		JsonArray essenceSteps = essenceShopTask.get("essence_shop_xp").getAsJsonArray();
		JsonObject essencePerks = object.has("perks") ? object.get("perks").getAsJsonObject() : new JsonObject();

		Map<String, EssenceShop> loreMap = new HashMap<>();
		for (Map.Entry<String, JsonElement> stringJsonElementEntry : Constants.ESSENCESHOPS.entrySet()) {
			String name = stringJsonElementEntry.getKey();
			JsonObject individualObjects = stringJsonElementEntry.getValue().getAsJsonObject();
			for (Map.Entry<String, JsonElement> jsonElementEntry : individualObjects.entrySet()) {
				String key = jsonElementEntry.getKey();
				if (!essencePerks.has(key)) {
					continue;
				}

				int essenceAmount = essencePerks.get(key).getAsInt();

				int amountReceivedForEach = 0;
				for (int i = essenceAmount - 1; i >= 0; i--) {
					amountReceivedForEach += essenceSteps.get(i).getAsInt();
				}
				if (!loreMap.containsKey(name)) {
					EssenceShop value = new EssenceShop();
					value.current += amountReceivedForEach;
					value.name = name;
					loreMap.put(name, value);
				} else {
					EssenceShop essenceShop = loreMap.get(name);
					essenceShop.current += amountReceivedForEach;
				}
			}
		}

		// bad workaround (pls fix later maybe)
		for (Map.Entry<String, JsonElement> stringJsonElementEntry : essenceShopTask.entrySet()) {
			String key = stringJsonElementEntry.getKey();
			if (!key.endsWith("_shop")) continue;
			String name = key.split("_shop")[0].toUpperCase();
			if (!loreMap.containsKey(name)) {
				loreMap.put(name, new EssenceShop().setName(name).setCurrent(0));
			}
		}

		int total = 0;
		for (Map.Entry<String, EssenceShop> stringEssenceShopEntry : loreMap.entrySet()) {
			String key = stringEssenceShopEntry.getKey();
			EssenceShop value = stringEssenceShopEntry.getValue();
			JsonObject jsonObject = NotEnoughUpdates.INSTANCE.manager
				.createItemResolutionQuery()
				.withKnownInternalName(key)
				.resolveToItemListJson();
			if (jsonObject == null){
				Utils.showOutdatedRepoNotification();
				continue;
			}
			value.name = jsonObject
				.get("displayname")
				.getAsString();
			String name = key.toLowerCase() + "_shop";
			if (!essenceShopTask.has(name)) continue;
			value.max = essenceShopTask.get(name).getAsInt();
			lore.add(levelPage.buildLore(
				EnumChatFormatting.getTextWithoutFormattingCodes(value.name),
				value.current,
				value.max,
				false
			));
			total += value.current;
		}

		levelPage.renderLevelBar(
			"Essence",
			NotEnoughUpdates.INSTANCE.manager.createItemResolutionQuery()
																			 .withKnownInternalName("ESSENCE_WITHER")
																			 .resolveToItemStack(),
			guiLeft + 299, guiTop + 25,
			110,
			total,
			total,
			categoryXp.get("essence_shop_task").getAsInt(),
			mouseX, mouseY,
			true,
			lore
		);
	}

	class EssenceShop {
		String name;
		double max;
		double current;

		public EssenceShop setCurrent(double current) {
			this.current = current;
			return this;
		}

		public EssenceShop setName(String name) {
			this.name = name;
			return this;
		}
	}
}
