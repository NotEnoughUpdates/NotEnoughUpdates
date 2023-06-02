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
import io.github.moulberry.notenoughupdates.profileviewer.CrimsonIslePage;
import io.github.moulberry.notenoughupdates.profileviewer.GuiProfileViewer;
import io.github.moulberry.notenoughupdates.profileviewer.ProfileViewer;
import io.github.moulberry.notenoughupdates.profileviewer.SkyblockProfiles;
import io.github.moulberry.notenoughupdates.profileviewer.level.LevelPage;
import io.github.moulberry.notenoughupdates.profileviewer.weight.weight.Weight;
import io.github.moulberry.notenoughupdates.util.Constants;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SlayingTaskLevel extends GuiTaskLevel {

	private final int[] bossLow = {25, 50, 100, 150, 250, 1000};
	private final int[] thorn = {25, 50, 150, 250, 400, 1000};
	private final int[] bossHigh = {50, 100, 150, 250, 500, 750, 1000};

	public SlayingTaskLevel(LevelPage levelPage) {
		super(levelPage);
	}

	@Override
	public void drawTask(JsonObject object, int mouseX, int mouseY, int guiLeft, int guiTop) {
		// slayer
		JsonObject slayingTask = levelPage.getConstant().get("slaying_task").getAsJsonObject();
		JsonArray slayerLevelUpXp = slayingTask.get("slayer_level_up_xp").getAsJsonArray();

		SkyblockProfiles.SkyblockProfile selectedProfile = GuiProfileViewer.getSelectedProfile();
		if (selectedProfile == null) {
			return;
		}

		Map<String, ProfileViewer.Level> skyblockInfo = selectedProfile.getLevelingInfo();
		int sbXpGainedSlayer = 0;
		if (skyblockInfo != null) {
			for (String slayer : Weight.SLAYER_NAMES) {
				ProfileViewer.Level level = skyblockInfo.get(slayer);
				for (int i = 0; i < (int) level.level; i++) {
					int asInt = slayerLevelUpXp.get(i).getAsInt();
					sbXpGainedSlayer += asInt;
				}
			}
		}

		JsonObject bossCollectionsXp = slayingTask.getAsJsonObject("boss_collections_xp");

		HashMap<String, Double> allComps = new HashMap<>();
		JsonElement normalCompletions = Utils
			.getElement(object, "dungeons.dungeon_types.catacombs.tier_completions");

		JsonElement masterCompletions = Utils
			.getElement(object, "dungeons.dungeon_types.master_catacombs.tier_completions");

		if (normalCompletions != null) {
			HashMap<String, Double> normalCompMap = NotEnoughUpdates.INSTANCE.manager.gson.fromJson(
				normalCompletions.getAsJsonObject(),
				HashMap.class
			);
			normalCompMap.forEach((floor, value) -> {
				if (allComps.containsKey(floor)) {
					allComps.put(floor, allComps.get(floor) + value);
				} else {
					allComps.put(floor, value);
				}
			});
		}
		if (masterCompletions != null) {
			HashMap<String, Double> masterCompMap = NotEnoughUpdates.INSTANCE.manager.gson.fromJson(
				masterCompletions.getAsJsonObject(),
				HashMap.class
			);

			masterCompMap.forEach((floor, value) -> {
				if (allComps.containsKey(floor)) {
					allComps.put(floor, allComps.get(floor) + value);
				} else {
					allComps.put(floor, value);
				}
			});
		}
		// THIS SERVER IS AWESOME I LOVE CONSISTENCY!!!!!!!

		int bossCollectionXp = 0;
		JsonArray dungeonCollectionXp = bossCollectionsXp.getAsJsonArray("dungeon_collection_xp");
		for (int i = 1; i <= 7; i++) {
			if (!allComps.containsKey(String.valueOf(i))) continue;
			double value = allComps.get(String.valueOf(i));
			switch (i) {
				case 1:
				case 2:
				case 3:
					bossCollectionXp += loopThroughCollection(bossLow, value, dungeonCollectionXp);
					break;
				case 4:
					bossCollectionXp += loopThroughCollection(thorn, value, dungeonCollectionXp);
					break;
				case 5:
				case 6:
				case 7:
					bossCollectionXp += loopThroughCollection(bossHigh, value, dungeonCollectionXp);
					break;
			}
		}

		JsonArray defeatKuudraXp = slayingTask.get("defeat_kuudra_xp").getAsJsonArray();
		// kuudra

		int sbXpDefeatKuudra = 0;

		int kuudraBossCollection = 0;
		if (object.has("nether_island_player_data")) {
			JsonObject jsonObject = object.getAsJsonObject("nether_island_player_data").getAsJsonObject(
				"kuudra_completed_tiers");
			for (Map.Entry<String, JsonElement> stringJsonElementEntry : jsonObject.entrySet()) {
				String key = stringJsonElementEntry.getKey();
				int value = stringJsonElementEntry.getValue().getAsInt();

				int i = 0;
				for (String kuudraTier : CrimsonIslePage.KUUDRA_TIERS) {
					if (key.equals(kuudraTier)) {
						sbXpDefeatKuudra += defeatKuudraXp.get(i).getAsInt();
						kuudraBossCollection += (i + 1) * value;
					}
					i++;
				}
			}
			if (kuudraBossCollection >= 10) bossCollectionXp += 10;
			if (kuudraBossCollection >= 100) bossCollectionXp += 15;
			if (kuudraBossCollection >= 500) bossCollectionXp += 20;
			if (kuudraBossCollection >= 2000) bossCollectionXp += 25;
			if (kuudraBossCollection >= 5000) bossCollectionXp += 30;
		}

		int sbXpBestiary = 0;
		int bestiaryTiers = GuiProfileViewer.getSelectedProfile().getBestiaryLevel();
		sbXpBestiary += bestiaryTiers;
		sbXpBestiary = sbXpBestiary + (sbXpBestiary / 10) * 2;

		int mythologicalKillsXp = 0;
		if (object.has("stats")) {
			JsonObject stats = object.get("stats").getAsJsonObject();
			if (stats.has("mythos_kills")) mythologicalKillsXp += (stats.get("mythos_kills").getAsLong() / 100);
		}

		int mythologicalKillsMax = slayingTask.get("mythological_kills").getAsInt();
		if (mythologicalKillsXp > mythologicalKillsMax) mythologicalKillsXp = mythologicalKillsMax;

		// dragons
		int sbXpFromDragonKills = 0;
		JsonObject slayDragonsXp = slayingTask.getAsJsonObject("slay_dragons_xp");
		for (Map.Entry<String, JsonElement> stringJsonElementEntry : slayDragonsXp.entrySet()) {
			String key = stringJsonElementEntry.getKey();
			int value = stringJsonElementEntry.getValue().getAsInt();
			// kills_superior_dragon_100
			float element = Utils.getElementAsFloat(Utils.getElement(object, "bestiary.kills_" + key + "_100"), 0);
			if (element > 0) {
				sbXpFromDragonKills += value;
			}
		}

		// slayer kills
		int sbXpFromSlayerDefeat = 0;

		JsonArray defeatSlayersXp = slayingTask.get("defeat_slayers_xp").getAsJsonArray();
		JsonObject slayerToTier = Constants.LEVELING.getAsJsonObject("slayer_to_highest_tier");
		if (slayerToTier == null) {
			Utils.showOutdatedRepoNotification();
			return;
		}
		for (Map.Entry<String, JsonElement> entry : slayerToTier.entrySet()) {
			int maxLevel = entry.getValue().getAsInt();
			for (int i = 0; i < 5; i++) {
				if (i >= maxLevel) break;
				float tier = Utils.getElementAsFloat(
					Utils.getElement(object, "slayer_bosses." + entry.getKey() + ".boss_kills_tier_" + i),
					0
				);
				if (tier != 0) {
					int value = defeatSlayersXp.get(i).getAsInt();
					sbXpFromSlayerDefeat += value;
				}
			}
		}

		// arachne
		JsonArray defeatArachneXp = slayingTask.get("defeat_arachne_xp").getAsJsonArray();
		int sbXpGainedArachne = 0;

		JsonElement tier1 = Utils.getElement(object, "bestiary.kills_arachne_300");
		if (tier1 != null) {
			sbXpGainedArachne += defeatArachneXp.get(0).getAsInt();
		}

		JsonElement tier2 = Utils.getElement(object, "bestiary.kills_arachne_500");
		if (tier2 != null) {
			sbXpGainedArachne += defeatArachneXp.get(1).getAsInt();
		}

		List<String> lore = new ArrayList<>();

		int slayerLevelUpMax = slayingTask.get("slayer_level_up").getAsInt();
		int bossCollectionsMax = slayingTask.get("boss_collections").getAsInt();
		int slayDragonsMax = slayingTask.get("slay_dragons").getAsInt();
		int defeatSlayersMax = slayingTask.get("defeat_slayers").getAsInt();
		int defeatKuudraMax = slayingTask.get("defeat_kuudra").getAsInt();
		int defeatArachneMax = slayingTask.get("defeat_arachne").getAsInt();

		lore.add(levelPage.buildLore("Slayer Level Up", sbXpGainedSlayer, slayerLevelUpMax, false));
		lore.add(levelPage.buildLore("Boss Collections", bossCollectionXp, bossCollectionsMax, false));
		lore.add(levelPage.buildLore("Bestiary Progress", sbXpBestiary, 0, true));
		lore.add(levelPage.buildLore("Mythological Kills", mythologicalKillsXp, mythologicalKillsMax, false));
		lore.add(levelPage.buildLore("Slay Dragons", sbXpFromDragonKills, slayDragonsMax, false));
		lore.add(levelPage.buildLore("Defeat Slayers", sbXpFromSlayerDefeat, defeatSlayersMax, false));
		lore.add(levelPage.buildLore("Defeat Kuudra", sbXpDefeatKuudra, defeatKuudraMax, false));
		lore.add(levelPage.buildLore("Defeat Arachne", sbXpGainedArachne, defeatArachneMax, false));

		int slayingTaskMax = levelPage.getConstant().getAsJsonObject("category_xp").get("slaying_task").getAsInt();

		int totalXp = sbXpGainedSlayer + bossCollectionXp + mythologicalKillsXp +
			sbXpFromDragonKills + sbXpFromSlayerDefeat + sbXpDefeatKuudra + sbXpGainedArachne;
		levelPage.renderLevelBar(
			"Slaying Task",
			new ItemStack(Items.golden_sword),
			guiLeft + 23, guiTop + 85,
			110,
			0,
			totalXp,
			slayingTaskMax,
			mouseX, mouseY,
			true,
			lore
		);
	}

	private int loopThroughCollection(int[] array, double value, JsonArray jsonArray) {
		int i = 0;
		int gain = 0;
		for (int bossReq : array) {
			if (value >= bossReq) {
				int gained = jsonArray.get(i).getAsInt();
				gain += gained;
			}
			i++;
		}
		return gain;
	}
}
