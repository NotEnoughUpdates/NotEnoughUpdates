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
import io.github.moulberry.notenoughupdates.profileviewer.data.APIDataJson;
import io.github.moulberry.notenoughupdates.profileviewer.level.LevelPage;
import lombok.var;
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

		SkyblockProfiles.SkyblockProfile selectedProfile = GuiProfileViewer.getSelectedProfile();
		if (selectedProfile == null) {
			return;
		}
		APIDataJson data = selectedProfile.getAPIDataJson();
		if (data == null) {
			return;
		}
		if (data.nether_island_player_data == null) {
			return;
		}
		var netherData = data.nether_island_player_data;
		int sbXpAccessoryUpgrade = 0;
		int sbXpReaperPeppers = 0;
		int sbXpUnlockedPowers = 0;
		int sbXpAbiphone = 0;
		int sbXpRefinedJyrre = 0;
		if (data.accessory_bag_storage != null && data.accessory_bag_storage.unlocked_powers != null) {
			sbXpAccessoryUpgrade = data.accessory_bag_storage.bag_upgrades_purchased * miscellaneousTask.get(
				"accessory_bag_upgrades_xp").getAsInt();
			sbXpReaperPeppers = data.player_data.reaper_peppers_eaten * miscellaneousTask.get("reaper_peppers_xp").getAsInt();
			sbXpUnlockedPowers = data.accessory_bag_storage.unlocked_powers.size() * miscellaneousTask.get(
				"unlocking_powers_xp").getAsInt();
			sbXpRefinedJyrre =
				data.winter_player_data.refined_jyrre_uses * miscellaneousTask.get("refined_jyrre_xp").getAsInt();
		}

		int sbXpDojo = 0;
		int sbXpRelays = 0;
		if (object.has("nether_island_player_data")) {
			JsonObject netherIslandPlayerData = object.getAsJsonObject("nether_island_player_data");
			JsonObject abiphoneObject = netherIslandPlayerData.getAsJsonObject("abiphone");

			if (abiphoneObject != null && netherData.abiphone != null) {
				int repairedIndex = netherData.abiphone.operator_chip.repaired_index;
				sbXpRelays += (repairedIndex + 1) * miscellaneousTask.get("unlocking_relays_xp").getAsInt();
			}

			if (netherData.dojo != null) {
				JsonObject dojoScoresObj = netherData.dojo;

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
				if (abiphoneObject != null && abiphoneObject.has("active_contacts")) {
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
		int personalBankUpgrade = data.profile.personal_bank_upgrade;
		JsonArray personalBankUpgradesXp = miscellaneousTask.getAsJsonArray("personal_bank_upgrades_xp");
		for (int i = 1; i <= personalBankUpgrade; i++) {
			sbXpPersonalBank += personalBankUpgradesXp.get(i - 1).getAsInt();
		}

		int sbXpTimeCharm = 0;
		if (data.rift != null && data.rift.gallery != null && data.rift.gallery.secured_trophies != null) {
			JsonArray timecharms = data.rift.gallery.secured_trophies;
			sbXpTimeCharm += timecharms.size() * miscellaneousTask.get("timecharm_xp").getAsInt();
		}

		int sbXpBurger = 0;
		if (data.rift != null) {
			sbXpBurger = data.rift.castle.grubber_stacks * miscellaneousTask.get("mcgrubber_burger_xp").getAsInt();
		}

		int sbXpSerum = 0;
		if (miscellaneousTask.has("metaphysical_serum_xp")) {
			sbXpSerum = miscellaneousTask.get("metaphysical_serum_xp").getAsInt() * data.experimentation.serums_drank;
		}

		List<String> lore = new ArrayList<>();

		lore.add(levelPage.buildLore("Accessory Bag Upgrades",
			sbXpAccessoryUpgrade, 0, true
		));
		int xpConsumableItems = sbXpReaperPeppers + sbXpBurger + sbXpSerum + sbXpRefinedJyrre;
		lore.add(levelPage.buildLore("Consumable Items",
			xpConsumableItems, miscellaneousTask.get("consumable_items").getAsInt(), false
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


		int totalXp =sbXpDojo + sbXpGainedHarp + sbXpAbiphone +
			sbXpCommunityUpgrade + sbXpPersonalBank + sbXpTimeCharm + sbXpRelays + xpConsumableItems;
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
