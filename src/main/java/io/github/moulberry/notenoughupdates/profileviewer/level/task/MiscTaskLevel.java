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
import io.github.moulberry.notenoughupdates.profileviewer.CrimsonIslePage;
import io.github.moulberry.notenoughupdates.profileviewer.GuiProfileViewer;
import io.github.moulberry.notenoughupdates.profileviewer.SkyblockProfiles;
import io.github.moulberry.notenoughupdates.profileviewer.level.LevelPage;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class MiscTaskLevel extends GuiTaskLevel {

	public MiscTaskLevel(LevelPage levelPage) {
		super(levelPage);
	}

	@Override
	public void drawTask(JsonObject object, int mouseX, int mouseY, int guiLeft, int guiTop) {
		JsonObject miscellaneousTask = levelPage.getConstant().getAsJsonObject("miscellaneous_task");
		// I love doing this on god!!!

		int sbXpAccessoryUpgrade = 0;
		int sbXpReaperPeppers = 0;
		int sbXpUnlockedPowers = 0;
		int sbXpAbiphone = 0;
		if (object.has("accessory_bag_storage")) {
			JsonObject accessoryBagStorage = object.getAsJsonObject("accessory_bag_storage");

			sbXpAccessoryUpgrade = Utils.getElementAsInt(Utils.getElement(
				accessoryBagStorage,
				"bag_upgrades_purchased"
			), 0) * miscellaneousTask.get("accessory_bag_upgrades_xp").getAsInt();
			sbXpReaperPeppers =
				miscellaneousTask.get("reaper_peppers_xp").getAsInt() * Utils.getElementAsInt(Utils.getElement(
					object,
					"reaper_peppers_eaten"
				), 0);
			if (accessoryBagStorage.has("unlocked_powers")) sbXpUnlockedPowers = accessoryBagStorage.getAsJsonArray(
				"unlocked_powers").size() * miscellaneousTask.get("unlocking_powers_xp").getAsInt();
		}

		int sbXpDojo = 0;
		int sbXpRelays = 0;
		if (object.has("nether_island_player_data")) {
			JsonObject netherIslandPlayerData = object.getAsJsonObject("nether_island_player_data");
			JsonObject abiphoneObject = netherIslandPlayerData.getAsJsonObject("abiphone");

			if (abiphoneObject.has("operator_chip") &&
				abiphoneObject.getAsJsonObject("operator_chip").has("repaired_index")) {
				int repairedIndex = abiphoneObject.getAsJsonObject("operator_chip").get("repaired_index").getAsInt();
				sbXpRelays += (repairedIndex + 1) * miscellaneousTask.get("unlocking_relays_xp").getAsInt();
			}

			if (netherIslandPlayerData.has("dojo")) {
				JsonObject dojoScoresObj = netherIslandPlayerData.getAsJsonObject("dojo");

				int pointsTotal = 0;
				for (int i = 0; i < CrimsonIslePage.apiDojoTestNames.size(); i++) {
					for (Map.Entry<String, JsonElement> dojoData : dojoScoresObj.entrySet()) {
						if (dojoData.getKey().equals("dojo_points_" + CrimsonIslePage.apiDojoTestNames.keySet().toArray()[i])) {
							pointsTotal += dojoData.getValue().getAsInt();
						}
					}
				}
				int index = getRankIndex(pointsTotal);
				JsonArray theDojoXp = miscellaneousTask.getAsJsonArray("the_dojo_xp");
				for (int i = 0; i < index; i++) {
					sbXpDojo += theDojoXp.get(i).getAsInt();
				}
			}

			// abiphone
			JsonObject leveling = object.getAsJsonObject("leveling");
			if (leveling != null && leveling.has("completed_tasks")) {
				JsonArray completedTask = leveling.get("completed_tasks").getAsJsonArray();
				Stream<JsonElement> stream = StreamSupport.stream(completedTask.spliterator(), true);
				long activeContacts = stream.map(JsonElement::getAsString).filter(s -> s.startsWith("ABIPHONE_")).count();
				if (abiphoneObject.has("active_contacts")) {
					sbXpAbiphone = (int) activeContacts * miscellaneousTask.get("abiphone_contacts_xp").getAsInt();
				}
			}
		}

		// harp
		int sbXpGainedHarp = 0;
		JsonObject harpSongsNames = miscellaneousTask.get("harp_songs_names").getAsJsonObject();

		JsonObject leveling = object.getAsJsonObject("leveling");
		if (leveling != null && leveling.has("completed_tasks")) {
			JsonArray completedTasks = leveling.get("completed_tasks").getAsJsonArray();
			for (JsonElement completedTask : completedTasks) {
				String name = completedTask.getAsString();
				String harpName = name.substring(0, name.lastIndexOf("_"));
				if (harpSongsNames.has(harpName)) sbXpGainedHarp += harpSongsNames.get(harpName).getAsInt() / 4;
			}
		}

		SkyblockProfiles.SkyblockProfile selectedProfile = GuiProfileViewer.getSelectedProfile();
		if (selectedProfile == null) {
			return;
		}

		// community upgrades
		int sbXpCommunityUpgrade = 0;
		JsonObject profileInformation = selectedProfile.getOuterProfileJson();
		if (profileInformation != null && profileInformation.has("community_upgrades")) {
			JsonObject communityUpgrades = profileInformation.getAsJsonObject("community_upgrades");
			JsonArray upgradeStates = communityUpgrades.getAsJsonArray("upgrade_states");
			JsonObject communityShopUpgradesMax = miscellaneousTask.getAsJsonObject("community_shop_upgrades_max");

			int communityShopUpgradesXp = miscellaneousTask.get("community_shop_upgrades_xp").getAsInt();
			if (upgradeStates != null) {
				for (
					JsonElement upgradeState : upgradeStates) {
					if (!upgradeState.isJsonObject()) continue;
					JsonObject value = upgradeState.getAsJsonObject();
					String upgrade = value.get("upgrade").getAsString();
					int tier = value.get("tier").getAsInt();
					if (communityShopUpgradesMax.has(upgrade)) {
						int max = communityShopUpgradesMax.get(upgrade).getAsInt();
						if (max >= tier) {
							sbXpCommunityUpgrade += communityShopUpgradesXp;
						}
					}
				}
			}
		}

		// personal bank
		int sbXpPersonalBank = 0;
		if (object.has("personal_bank_upgrade")) {
			int personalBankUpgrade = object.get("personal_bank_upgrade").getAsInt();
			JsonArray personalBankUpgradesXpArr = miscellaneousTask.getAsJsonArray("personal_bank_upgrades_xp");
			for (int i = 1; i <= personalBankUpgrade; i++) {
				sbXpPersonalBank += personalBankUpgradesXpArr.get(i - 1).getAsInt();
			}
		}

		int sbXpTimeCharm = 0;
		if (object.has("rift") &&
			object.getAsJsonObject("rift").has("gallery") &&
			object.getAsJsonObject("rift").getAsJsonObject("gallery").has("secured_trophies")) {
			JsonArray timeCharms = object.getAsJsonObject("rift").getAsJsonObject("gallery").getAsJsonArray(
				"secured_trophies");
			sbXpTimeCharm += timeCharms.size() * miscellaneousTask.get("timecharm_xp").getAsInt();
		}

		List<String> lore = new ArrayList<>();

		lore.add(levelPage.buildLore("Accessory Bag Upgrades",
			sbXpAccessoryUpgrade, 0, true
		));
		lore.add(levelPage.buildLore("Reaper Peppers",
			sbXpReaperPeppers, miscellaneousTask.get("reaper_peppers").getAsInt(), false
		));
		lore.add(levelPage.buildLore("Timecharms",
			sbXpTimeCharm, miscellaneousTask.get("timecharm").getAsInt(), false
		));
		lore.add(levelPage.buildLore("Unlocking Powers",
			sbXpUnlockedPowers, 0, true
		));
		lore.add(levelPage.buildLore("The Dojo",
			sbXpDojo, miscellaneousTask.get("the_dojo").getAsInt(), false
		));
		lore.add(levelPage.buildLore("Harp Songs",
			sbXpGainedHarp, miscellaneousTask.get("harp_songs").getAsInt(), false
		));
		lore.add(levelPage.buildLore("Abiphone Contacts",
			sbXpAbiphone, miscellaneousTask.get("abiphone_contacts").getAsInt(), false
		));
		lore.add(levelPage.buildLore("Community Shop Upgrades",
			sbXpCommunityUpgrade, miscellaneousTask.get("community_shop_upgrades").getAsInt(), false
		));
		lore.add(levelPage.buildLore("Personal Bank Upgrades",
			sbXpPersonalBank, miscellaneousTask.get("personal_bank_upgrades").getAsInt(), false
		));

		lore.add(levelPage.buildLore("Upgraded Relays",
			sbXpRelays, miscellaneousTask.get("unlocking_relays").getAsInt(), false
		));

		int totalXp = sbXpReaperPeppers + sbXpDojo + sbXpGainedHarp + sbXpAbiphone +
			sbXpCommunityUpgrade + sbXpPersonalBank + sbXpTimeCharm + sbXpRelays;
		levelPage.renderLevelBar(
			"Misc. Task",
			new ItemStack(Items.map),
			guiLeft + 299, guiTop + 55,
			110,
			0,
			totalXp,
			levelPage.getConstant().getAsJsonObject("category_xp").get("miscellaneous_task").getAsInt(),
			mouseX, mouseY,
			true,
			lore
		);
	}

	private int getRankIndex(int pointsTotal) {
		AtomicInteger index = new AtomicInteger();
		CrimsonIslePage.dojoPointsToRank.forEach((required, name) -> {
			if (pointsTotal > required) {
				index.getAndIncrement();
			}
		});
		return index.get();
	}
}
