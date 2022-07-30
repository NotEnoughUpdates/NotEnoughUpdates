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

package io.github.moulberry.notenoughupdates.miscgui.minionhelper.loaders.repo;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.core.util.StringUtils;
import io.github.moulberry.notenoughupdates.miscgui.minionhelper.Minion;
import io.github.moulberry.notenoughupdates.miscgui.minionhelper.MinionHelperManager;
import io.github.moulberry.notenoughupdates.miscgui.minionhelper.requirements.CollectionRequirement;
import io.github.moulberry.notenoughupdates.miscgui.minionhelper.requirements.ReputationRequirement;
import io.github.moulberry.notenoughupdates.miscgui.minionhelper.requirements.SlayerRequirement;
import io.github.moulberry.notenoughupdates.miscgui.minionhelper.sources.CraftingSource;
import io.github.moulberry.notenoughupdates.util.Utils;

import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

public class MinionHelperRepoMinionLoader {

	private final MinionHelperRepoLoader repoLoader;
	private final MinionHelperManager manager;

	public MinionHelperRepoMinionLoader(MinionHelperRepoLoader repoLoader, MinionHelperManager manager) {
		this.repoLoader = repoLoader;
		this.manager = manager;
	}

	void loadMinionData() {
		TreeMap<String, JsonObject> itemInformation = NotEnoughUpdates.INSTANCE.manager.getItemInformation();

		for (Map.Entry<String, Minion> entry : manager.getAllMinions().entrySet()) {
			String internalName = entry.getKey();
			if (!itemInformation.containsKey(internalName)) continue;
			Minion minion = entry.getValue();

			JsonObject jsonObject = itemInformation.get(internalName);
			if (jsonObject.has("displayname")) {
				String displayName = jsonObject.get("displayname").getAsString();
				displayName = StringUtils.cleanColour(displayName);
				displayName = StringUtils.removeLastWord(displayName, " ");
				minion.setDisplayName(displayName);
			}

			if (jsonObject.has("recipe")) {
				loadRecipes(minion, jsonObject);
			}

			loadRequirements(minion, jsonObject);
		}
	}

	private void loadRequirements(Minion minion, JsonObject jsonObject) {
		for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
			String name = entry.getKey();
			if (name.endsWith("_req") || name.equals("crafttext")) {
				String value = entry.getValue().getAsString();

				try {
					switch (name) {
						case "reputation_req": {
							String[] split = value.split(":");
							String reputationType = split[0];
							int reputation = Integer.parseInt(split[1]);
							minion.getRequirements().add(new ReputationRequirement(reputationType, reputation));
							break;
						}
						case "crafttext": {
							if (minion.getTier() != 1) break;
							if (value.isEmpty()) break;

							String rawCollection = value.split(Pattern.quote(": "))[1];
							String cleanCollection = StringUtils.removeLastWord(rawCollection, " ");
							String rawTier = rawCollection.substring(cleanCollection.length() + 1);
							int tier = Utils.parseRomanNumeral(rawTier);
							minion.getRequirements().add(new CollectionRequirement(cleanCollection, tier));
							break;
						}
						case "slayer_req": {
							String[] split = value.split("_");
							String slayerType = split[0].toLowerCase();
							int tier = Integer.parseInt(split[1]);
							minion.getRequirements().add(new SlayerRequirement(slayerType, tier));
							break;
						}
					}
				} catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
					repoLoader.errorWhileLoading = true;
					if (NotEnoughUpdates.INSTANCE.config.hidden.dev) {
						Utils.addChatMessage(
							"§c[NEU] Error in MinionHelperRepoLoader while loading repo entry " + minion.getDisplayName() + " " +
								minion.getTier() + ": " +
								e.getClass().getSimpleName() + ": " + e.getMessage());
					}
					e.printStackTrace();
				}
			}
		}
	}

	private void loadRecipes(Minion minion, JsonObject jsonObject) {
		JsonObject recipes = jsonObject.get("recipe").getAsJsonObject();
		RecipeBuilder builder = new RecipeBuilder(manager);
		for (Map.Entry<String, JsonElement> entry : recipes.entrySet()) {
			String rawString = entry.getValue().getAsString();

			builder.addLine(minion, rawString);
		}

		minion.setMinionSource(new CraftingSource(builder.getItems()));
		minion.setParent(builder.getParent());
	}
}
