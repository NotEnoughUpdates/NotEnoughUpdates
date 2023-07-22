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
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.profileviewer.bestiary.BestiaryData;
import io.github.moulberry.notenoughupdates.profileviewer.weight.senither.SenitherWeight;
import io.github.moulberry.notenoughupdates.profileviewer.weight.weight.Weight;
import io.github.moulberry.notenoughupdates.util.Constants;
import io.github.moulberry.notenoughupdates.util.UrsaClient;
import io.github.moulberry.notenoughupdates.util.Utils;
import io.github.moulberry.notenoughupdates.util.hypixelapi.ProfileCollectionInfo;
import lombok.Getter;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumChatFormatting;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class SkyblockProfiles {
	private static final String defaultNbtData = "Hz8IAAAAAAAAAD9iYD9kYD9kAAMAPwI/Gw0AAAA=";
	private static final String moulberryUuid = "d0e05de76067454dbeaec6d19d886191";
	private static final List<String> inventoryNames = Arrays.asList(
		"inv_armor",
		"fishing_bag",
		"quiver",
		"ender_chest_contents",
		"backpack_contents",
		"personal_vault_contents",
		"wardrobe_contents",
		"potion_bag",
		"inv_contents",
		"talisman_bag",
		"candy_inventory_contents",
		"equippment_contents"
	);
	private static final List<String> skills = Arrays.asList(
		"taming",
		"mining",
		"foraging",
		"enchanting",
		"carpentry",
		"farming",
		"combat",
		"fishing",
		"alchemy",
		"runecrafting",
		"social"
	);
	private final ProfileViewer profileViewer;
	// TODO: replace with UUID type
	private final String uuid;
	private final AtomicBoolean updatingSkyblockProfilesState = new AtomicBoolean(false);
	private final AtomicBoolean updatingGuildInfoState = new AtomicBoolean(false);
	private final AtomicBoolean updatingPlayerStatusState = new AtomicBoolean(false);
	private final AtomicBoolean updatingSoopyData = new AtomicBoolean(false);
	private final AtomicBoolean updatingBingoInfo = new AtomicBoolean(false);
	@Getter
	private Map<String, SkyblockProfile> nameToProfile = null;
	private JsonArray profilesArray;
	private List<String> profileNames = new ArrayList<>();
	private String latestProfileName;
	private long soopyNetworthLeaderboardPosition = -1; // -1 = default, -2 = loading, -3 = error
	private long soopyWeightLeaderboardPosition = -1; // -1 = default, -2 = loading, -3 = error
	private JsonObject guildInformation = null;
	// Assume the player is in a guild until proven otherwise
	private boolean isInGuild = true;
	private JsonObject playerStatus = null;
	private JsonObject bingoInformation = null;
	private long lastPlayerInfoState = 0;
	private long lastStatusInfoState = 0;
	private long lastGuildInfoState = 0;
	private long lastBingoInfoState = 0;

	public SkyblockProfiles(ProfileViewer profileViewer, String uuid) {
		this.profileViewer = profileViewer;
		this.uuid = uuid;
	}

	public JsonObject getPlayerStatus() {
		if (playerStatus != null) return playerStatus;
		if (updatingPlayerStatusState.get()) return null;

		long currentTime = System.currentTimeMillis();
		if (currentTime - lastStatusInfoState < 15 * 1000) return null;
		lastStatusInfoState = currentTime;
		updatingPlayerStatusState.set(true);

		profileViewer.getManager().ursaClient
			.get(UrsaClient.status(Utils.parseDashlessUUID(uuid)))
			.handle((jsonObject, ex) -> {
				updatingPlayerStatusState.set(false);

				if (jsonObject != null && jsonObject.has("success") && jsonObject.get("success").getAsBoolean()) {
					playerStatus = jsonObject.get("session").getAsJsonObject();
				}
				return null;
			});
		return null;
	}

	public JsonObject getBingoInformation() {
		long currentTime = System.currentTimeMillis();
		if (bingoInformation != null && currentTime - lastBingoInfoState < 15 * 1000) return bingoInformation;
		if (updatingBingoInfo.get() && bingoInformation != null) return bingoInformation;
		if (updatingBingoInfo.get() && bingoInformation == null) return null;

		lastBingoInfoState = currentTime;
		updatingBingoInfo.set(true);

		NotEnoughUpdates.INSTANCE.manager.ursaClient
			.get(UrsaClient.bingo(Utils.parseDashlessUUID(uuid)))
			.handle(((jsonObject, throwable) -> {
				updatingBingoInfo.set(false);

				if (jsonObject != null && jsonObject.has("success") && jsonObject.get("success").getAsBoolean()) {
					bingoInformation = jsonObject;
				} else {
					bingoInformation = null;
				}
				return null;
			}));
		return bingoInformation != null ? bingoInformation : null;
	}

	/**
	 * -1 = default, -2 = loading, -3 = error
	 * >= 0 = actual position
	 */
	public long getSoopyNetworthLeaderboardPosition() {
		if (soopyNetworthLeaderboardPosition == -1) {
			loadSoopyData(() -> {});
		}
		return moulberryUuid.equals(uuid) ? 1 : soopyNetworthLeaderboardPosition;
	}

	public long getSoopyWeightLeaderboardPosition() {
		if (soopyWeightLeaderboardPosition == -1) {
			loadSoopyData(() -> {});
		}
		return moulberryUuid.equals(uuid) ? 1 : soopyWeightLeaderboardPosition;
	}

	public boolean isProfileMaxSoopyWeight(String profileName) {
		String highestProfileName = "";
		double largestProfileWeight = 0;

		for (Map.Entry<String, SkyblockProfile> profileEntry : nameToProfile.entrySet()) {
			if (!profileEntry.getValue().skillsApiEnabled()) {
				continue;
			}

			Map<String, ProfileViewer.Level> levelingInfo = profileEntry.getValue().getLevelingInfo();
			if (levelingInfo == null) {
				continue;
			}

			double weightValue = new SenitherWeight(levelingInfo).getTotalWeight().getRaw();
			if (weightValue > largestProfileWeight) {
				largestProfileWeight = weightValue;
				highestProfileName = profileEntry.getKey();
			}
		}

		return highestProfileName.equals(profileName);
	}

	private void loadSoopyData(Runnable callback) {
		if (updatingSoopyData.get()) {
			return;
		}

		updatingSoopyData.set(true);

		soopyNetworthLeaderboardPosition = -2; // Loading
		profileViewer.getManager().apiUtils
			.request()
			.url("https://soopy.dev/api/v2/leaderboard/networth/user/" + this.uuid)
			.requestJson()
			.handle((jsonObject, throwable) -> {
				if (jsonObject == null || !jsonObject.has("success") || !jsonObject.get("success").getAsBoolean()
					|| !jsonObject.has("data")
					|| !jsonObject.getAsJsonObject("data").has("data")
					|| !jsonObject.getAsJsonObject("data").getAsJsonObject("data").has("position")) {
					soopyNetworthLeaderboardPosition = -3; // Error
				} else {
					soopyNetworthLeaderboardPosition = jsonObject.getAsJsonObject("data").getAsJsonObject("data").get(
						"position").getAsLong();
				}
				return null;
			});

		soopyWeightLeaderboardPosition = -2; // Loading
		profileViewer.getManager().apiUtils
			.request()
			.url("https://soopy.dev/api/v2/leaderboard/weight/user/" + this.uuid)
			.requestJson()
			.handle((jsonObject, throwable) -> {
				if (jsonObject == null || !jsonObject.has("success") || !jsonObject.get("success").getAsBoolean()
					|| !jsonObject.has("data")
					|| !jsonObject.getAsJsonObject("data").has("data")
					|| !jsonObject.getAsJsonObject("data").getAsJsonObject("data").has("position")) {
					soopyWeightLeaderboardPosition = -3; // Error
				} else {
					soopyWeightLeaderboardPosition = jsonObject.getAsJsonObject("data").getAsJsonObject("data").get(
						"position").getAsLong();
				}
				return null;
			});

		long currentTime = System.currentTimeMillis();
		if (ProfileViewerUtils.lastSoopyRequestTime.containsKey(uuid)) {
			if (currentTime - ProfileViewerUtils.lastSoopyRequestTime.get(uuid) < 5 * 1000 * 60) {
				if (ProfileViewerUtils.soopyDataCache.containsKey(uuid)) {
					updateSoopyNetworth(ProfileViewerUtils.soopyDataCache.get(uuid));
					callback.run();
				}
			}
		} else {
			ProfileViewerUtils.lastSoopyRequestTime.put(uuid, currentTime);
			profileViewer.getManager().apiUtils
				.request()
				.url("https://soopy.dev/api/v2/player_networth/" + this.uuid)
				.method("POST")
				.postData("application/json", profilesArray.toString())
				.requestJson()
				.handle((jsonObject, throwable) -> {
					updateSoopyNetworth(jsonObject);
					ProfileViewerUtils.soopyDataCache.put(uuid, jsonObject);
					callback.run();
					return null;
				});
		}
	}

	private void updateSoopyNetworth(JsonObject jsonObject) {
		if (jsonObject == null || !jsonObject.has("success") || !jsonObject.get("success").getAsBoolean()) {
			for (Map.Entry<String, SkyblockProfile> entry : nameToProfile.entrySet()) {
				entry.getValue().soopyNetworth = new SoopyNetworth(null);
			}
		} else {
			for (Map.Entry<String, SkyblockProfile> entry : nameToProfile.entrySet()) {
				String profileId = entry.getValue().getOuterProfileJson().get("profile_id").getAsString().replace("-", "");
				JsonElement curProfileData = jsonObject.getAsJsonObject("data").get(profileId);
				if (curProfileData == null) {
					entry.getValue().soopyNetworth = new SoopyNetworth(null);
				} else {
					entry.getValue().soopyNetworth = new SoopyNetworth(curProfileData.getAsJsonObject());
				}
			}
		}
		updatingSoopyData.set(false);
	}

	public AtomicBoolean getUpdatingSkyblockProfilesState() {
		return updatingSkyblockProfilesState;
	}

	public @Nullable SkyblockProfile getProfile(String profileName) {
		return nameToProfile == null ? null : nameToProfile.get(profileName);
	}

	public SkyblockProfile getLatestProfile() {
		return nameToProfile.get(latestProfileName);
	}

	public String getLatestProfileName() {
		return latestProfileName;
	}

	public Map<String, SkyblockProfile> getOrLoadSkyblockProfiles(Runnable runnable) {
		if (nameToProfile != null) {
			return nameToProfile;
		}

		long currentTime = System.currentTimeMillis();
		if (currentTime - lastPlayerInfoState < 15 * 1000 && updatingSkyblockProfilesState.get()) {
			return null;
		}
		lastPlayerInfoState = currentTime;
		updatingSkyblockProfilesState.set(true);

		profileViewer.getManager().ursaClient
			.get(UrsaClient.profiles(Utils.parseDashlessUUID(uuid)))
			.handle((profilesJson, throwable) -> {
				if (profilesJson != null && profilesJson.has("success")
					&& profilesJson.get("success").getAsBoolean() && profilesJson.has("profiles")) {
					Map<String, SkyblockProfile> nameToProfile = new HashMap<>();
					String latestProfileName = null;
					List<String> profileNames = new ArrayList<>();

					// player has no skyblock profiles
					if (profilesJson.get("profiles").isJsonNull()) {
						updatingSkyblockProfilesState.set(false);
						this.nameToProfile = new HashMap<>();
						return null;
					}

					for (JsonElement profileEle : profilesJson.getAsJsonArray("profiles")) {
						JsonObject profile = profileEle.getAsJsonObject();

						if (!profile.has("members")) {
							continue;
						}

						JsonObject members = profile.getAsJsonObject("members");
						if (members.has(uuid)) {
							JsonObject member = members.getAsJsonObject(uuid);
							if (member.has("coop_invitation")) {
								if (!member.getAsJsonObject("coop_invitation").get("confirmed").getAsBoolean()) {
									continue;
								}
							}

							String profileName = profile.get("cute_name").getAsString();
							if (profile.has("selected") && profile.get("selected").getAsBoolean()) {
								latestProfileName = profileName;
							}
							nameToProfile.put(profileName, new SkyblockProfile(profile));
							profileNames.add(profileName);
						}
					}

					this.nameToProfile = nameToProfile;
					this.profilesArray = profilesJson.getAsJsonArray("profiles");
					this.latestProfileName = latestProfileName;
					this.profileNames.addAll(profileNames);
					updatingSkyblockProfilesState.set(false);

					if (runnable != null) {
						runnable.run();
					}
				}
				return null;
			});

		return null;
	}

	public JsonObject getOrLoadGuildInformation(Runnable runnable) {
		if (guildInformation != null) {
			return guildInformation;
		}

		long currentTime = System.currentTimeMillis();
		if (currentTime - lastGuildInfoState < 15 * 1000 && updatingGuildInfoState.get()) {
			return null;
		}
		lastGuildInfoState = currentTime;
		updatingGuildInfoState.set(true);

		profileViewer.getManager().ursaClient
			.get(UrsaClient.guild(Utils.parseDashlessUUID(uuid)))
			.handle((jsonObject, ex) -> {
				updatingGuildInfoState.set(false);

				if (jsonObject != null && jsonObject.has("success") && jsonObject.get("success").getAsBoolean()) {
					if (jsonObject.get("guild").isJsonNull()) {
						isInGuild = false;
						return null;
					}

					guildInformation = jsonObject.getAsJsonObject("guild");

					if (runnable != null) {
						runnable.run();
					}
				}
				return null;
			});

		return null;
	}

	public boolean isPlayerInGuild() {
		return isInGuild;
	}

	public List<String> getProfileNames() {
		return profileNames;
	}

	public void resetCache() {
		profilesArray = null;
		profileNames = new ArrayList<>();
		guildInformation = null;
		playerStatus = null;
		nameToProfile = null;
	}

	public String getUuid() {
		return uuid;
	}

	public @Nullable JsonObject getHypixelProfile() {
		return profileViewer.getUuidToHypixelProfile().getOrDefault(uuid, null);
	}

	public static class SoopyNetworth {
		private final Map<String, Long> categoryToTotal = new LinkedHashMap<>();
		private long networth;

		private SoopyNetworth(JsonObject nwData) {
			if (nwData == null || nwData.isJsonNull() || nwData.get("total").isJsonNull()) {
				networth = -2;
				return;
			}

			networth = nwData.get("total").getAsLong();
			Map<String, Long> categoryToTotal = new HashMap<>();
			for (Map.Entry<String, JsonElement> entry : nwData.get("categories").getAsJsonObject().entrySet()) {
				if (!entry.getValue().isJsonNull()) {
					categoryToTotal.put(entry.getKey(), entry.getValue().getAsLong());
				}
			}

			// Sort based on category value
			categoryToTotal.entrySet().stream()
										 .sorted(Comparator.comparingLong(e -> -e.getValue()))
										 .forEachOrdered(e -> this.categoryToTotal.put(e.getKey(), e.getValue()));
		}

		private SoopyNetworth asLoading() {
			networth = -1;
			return this;
		}

		/**
		 * @return -2 = error, -1 = loading, >= 0 = success
		 */
		public long getNetworth() {
			return networth;
		}

		public Map<String, Long> getCategoryToTotal() {
			return categoryToTotal;
		}
	}

	public class SkyblockProfile {

		private final JsonObject outerProfileJson;
		private final String gamemode;
		private Integer magicPower = null;
		private Double skyblockLevel = null;
		private EnumChatFormatting skyBlockExperienceColour = null;
		private Map<String, JsonArray> inventoryNameToInfo = null;
		private Map<String, ProfileViewer.Level> levelingInfo = null;
		private JsonObject petsInfo = null;
		private ProfileCollectionInfo collectionsInfo = null;
		private PlayerStats.Stats stats;
		private Long networth = null;
		private SoopyNetworth soopyNetworth = null;
		private MuseumData museumData = null;
		private final AtomicBoolean updatingMuseumData = new AtomicBoolean(false);

		public class MuseumData {
			private long museumValue;
			private final Map<String, JsonArray> weaponItems = new HashMap<>();
			private final Map<String, JsonArray> armorItems = new HashMap<>();
			private final Map<String, JsonArray> raritiesItems = new HashMap<>();
			private final List<JsonArray> specialItems = new ArrayList<>();
			private final Map<String, Pair<Long, Boolean>> savedItems = new HashMap<>();

			private MuseumData(JsonObject museumJson) {
				JsonObject museum = Constants.MUSEUM;
				if (museum == null) {
					Utils.showOutdatedRepoNotification();
					museumValue = -3;
					return;
				}
				if (museumJson == null || museumJson.isJsonNull() || museumJson.get("members").isJsonNull()) {
					museumValue = -2;
					return;
				}
				JsonObject members = museumJson.get("members").getAsJsonObject();
				if (members == null || members.isJsonNull() || !members.has(uuid)) {
					museumValue = -2;
					return;
				}
				JsonObject member = members.get(uuid).getAsJsonObject();
				if (member == null || member.isJsonNull() || !member.has("value")) {
					museumValue = -2;
					return;
				}
				museumValue = member.get("value").getAsLong();
				if (member.has("items")) {
					JsonObject museumItemsData = member.get("items").getAsJsonObject();
					if (museumItemsData != null) {
						for (Map.Entry<String, JsonElement> entry : museumItemsData.entrySet()) {
							JsonObject itemJson = entry.getValue().getAsJsonObject();
							JsonArray contents = parseNbt(itemJson);
							String itemName = entry.getKey();
							Long donationTime = itemJson.get("donated_time").getAsLong();
							boolean borrowing = false;
							if (itemJson.has("borrowing")) {
								borrowing = itemJson.get("borrowing").getAsBoolean();
							}

							getDataAndChildren(itemName, Pair.of(donationTime, borrowing));
							processItems(itemName, contents);
						}
					}
				}

				if (member.has("special")) {
					JsonArray specialItemsData = member.get("special").getAsJsonArray();
					if (specialItemsData != null) {
						for (JsonElement element : specialItemsData) {
							JsonObject itemData = element.getAsJsonObject();
							JsonArray contents = parseNbt(itemData);

							long donationTime = itemData.get("donated_time").getAsLong();
							JsonObject firstItem = contents.get(0).getAsJsonObject();
							String itemID = firstItem.get("internalname").getAsString();
							getDataAndChildren(itemID, Pair.of(donationTime, false));

							specialItems.add(contents);
						}
					}
				}
			}

			private JsonArray parseNbt(JsonObject nbt) {
				JsonArray contents = new JsonArray();
				JsonObject contentItems = nbt.get("items").getAsJsonObject();
				String contentBytes = contentItems.get("data").getAsString();

				try {
					NBTTagList items = CompressedStreamTools.readCompressed(
						new ByteArrayInputStream(Base64.getDecoder().decode(contentBytes))
					).getTagList("i", 10);
					for (int j = 0; j < items.tagCount(); j++) {
						JsonObject item = profileViewer.getManager().getJsonFromNBTEntry(items.getCompoundTagAt(j));
						if (item == null) {
							continue;
						}
						contents.add(item);
					}
				} catch (IOException ignored) {
				}
				return contents;
			}

			private void processItems(String itemName, JsonArray contents) {
				JsonObject museum = Constants.MUSEUM;
				storeItem(itemName, contents, museum.get("weapons").getAsJsonArray(), weaponItems);
				storeItem(itemName, contents, museum.get("armor").getAsJsonArray(), armorItems);
				storeItem(itemName, contents, museum.get("rarities").getAsJsonArray(), raritiesItems);
			}

			private void storeItem(String itemName, JsonArray contents, JsonArray items, Map<String, JsonArray> itemMap) {
				for (JsonElement item : items) {
					if (Objects.equals(item.getAsString(), itemName)) {
						itemMap.put(itemName, contents);
						return;
					}
				}
			}

			private void getDataAndChildren(String name, Pair<Long, Boolean> itemData) {
				JsonObject children = Constants.MUSEUM.get("children").getAsJsonObject();
				if (savedItems.containsKey(name)) return;
				savedItems.put(name, itemData);
				if (children.has(name)) {
					String childId = children.get(name).getAsString();
					String childName = childId;
					JsonObject nameMappings = Constants.MUSEUM.get("armor_to_id").getAsJsonObject();
					if (nameMappings.has(childId)) {
						childName = nameMappings.get(childId).getAsString();
					}
					String displayName = NotEnoughUpdates.INSTANCE.manager.getDisplayName(childName);
					ItemStack stack = Utils.createItemStack(Items.dye, displayName, 10, "Donated as higher tier");
					JsonObject item = profileViewer.getManager().getJsonForItem(stack);
					item.add("internalname", new JsonPrimitive("_"));
					JsonArray itemArray = new JsonArray();
					itemArray.add(item);
					processItems(childId, itemArray);

					getDataAndChildren(childId, itemData);
				}
			}

			private MuseumData asLoading() {
				museumValue = -1;
				return this;
			}

			public long getValue() {
				return museumValue;
			}
			public Map<String, JsonArray> getWeaponItems() {
				return weaponItems;
			}
			public Map<String, JsonArray> getArmorItems() {
				return armorItems;
			}
			public Map<String, JsonArray> getRaritiesItems() {
				return raritiesItems;
			}
			public List<JsonArray> getSpecialItems() {
				return specialItems;
			}
			public Map<String, Pair<Long, Boolean>> getSavedItems() {
				return savedItems;
			}
		}

		private void loadMuseumData() {
			if (updatingMuseumData.get()) {
				return;
			}

			updatingMuseumData.set(true);
			String profileId = getOuterProfileJson().get("profile_id").getAsString();
			profileViewer.getManager().ursaClient.get(UrsaClient.museumForProfile(profileId))
			 .handle((museumJson, throwable) -> {
				 if (museumJson != null && museumJson.has("success")
					 && museumJson.get("success").getAsBoolean() && museumJson.has("members")) {
					 museumData = new MuseumData(museumJson);
					 return null;
				 }
				 return null;
			 });
		}

		public SkyblockProfile(JsonObject outerProfileJson) {
			this.outerProfileJson = outerProfileJson;
			this.gamemode = Utils.getElementAsString(outerProfileJson.get("game_mode"), null);
		}

		public JsonObject getOuterProfileJson() {
			return outerProfileJson;
		}

		/**
		 * @return Profile json with UUID of {@link SkyblockProfiles#uuid}
		 */
		public JsonObject getProfileJson() {
			return Utils.getElement(outerProfileJson, "members." + SkyblockProfiles.this.uuid).getAsJsonObject();
		}

		public @Nullable String getGamemode() {
			return gamemode;
		}

		/**
		 * Calculates the amount of Magical Power the player has using the list of accessories
		 *
		 * @return the amount of Magical Power or -1
		 */
		public int getMagicalPower() {
			if (magicPower != null) {
				return magicPower;
			}

			Map<String, JsonArray> inventoryInfo = getInventoryInfo();
			if (!inventoryInfo.containsKey("talisman_bag")) {
				return -1;
			}

			return magicPower = ProfileViewerUtils.getMagicalPower(inventoryInfo.get("talisman_bag"), getProfileJson());
		}

		public Map<String, JsonArray> getInventoryInfo() {
			if (inventoryNameToInfo != null) {
				return inventoryNameToInfo;
			}

			JsonObject profileJson = getProfileJson();
			inventoryNameToInfo = new HashMap<>();

			for (String invName : inventoryNames) {
				JsonArray contents = new JsonArray();

				if (invName.equals("backpack_contents")) {
					JsonObject backpackData = getBackpackData(Utils.getElement(profileJson, "backpack_contents"));
					inventoryNameToInfo.put("backpack_sizes", backpackData.getAsJsonArray("backpack_sizes"));
					contents = backpackData.getAsJsonArray("contents");
				} else {
					String contentBytes = Utils.getElementAsString(
						Utils.getElement(profileJson, invName + ".data"),
						defaultNbtData
					);

					try {
						NBTTagList items = CompressedStreamTools.readCompressed(
							new ByteArrayInputStream(Base64.getDecoder().decode(contentBytes))
						).getTagList("i", 10);
						for (int j = 0; j < items.tagCount(); j++) {
							JsonObject item = profileViewer.getManager().getJsonFromNBTEntry(items.getCompoundTagAt(j));
							contents.add(item);
						}
					} catch (IOException ignored) {
					}
				}

				inventoryNameToInfo.put(invName, contents);
			}

			return inventoryNameToInfo;
		}

		private JsonObject getBackpackData(JsonElement backpackContentsJson) {
			// JsonArray of JsonObjects
			JsonArray contents = new JsonArray();
			JsonArray backpackSizes = new JsonArray();

			JsonObject bundledReturn = new JsonObject();
			bundledReturn.add("contents", contents);
			bundledReturn.add("backpack_sizes", backpackSizes);

			if (backpackContentsJson == null || !backpackContentsJson.isJsonObject()) {
				return bundledReturn;
			}

			for (Map.Entry<String, JsonElement> backpack : backpackContentsJson.getAsJsonObject().entrySet()) {
				if (backpack.getValue().isJsonObject()) {
					try {
						String bytes = Utils.getElementAsString(backpack.getValue().getAsJsonObject().get("data"), defaultNbtData);
						NBTTagList items = CompressedStreamTools.readCompressed(
							new ByteArrayInputStream(Base64.getDecoder().decode(bytes))
						).getTagList("i", 10);

						backpackSizes.add(new JsonPrimitive(items.tagCount()));
						for (int j = 0; j < items.tagCount(); j++) {
							contents.add(profileViewer.getManager().getJsonFromNBTEntry(items.getCompoundTagAt(j)));
						}
					} catch (IOException ignored) {
					}
				}
			}
			return bundledReturn;
		}

		public EnumChatFormatting getSkyblockLevelColour() {
			if (Constants.SBLEVELS == null || !Constants.SBLEVELS.has("sblevel_colours")) {
				Utils.showOutdatedRepoNotification();
				return EnumChatFormatting.WHITE;
			}

			if (skyBlockExperienceColour != null) {
				return skyBlockExperienceColour;
			}

			double skyblockLevel = getSkyblockLevel();
			EnumChatFormatting levelColour = EnumChatFormatting.WHITE;

			JsonObject sblevelColours = Constants.SBLEVELS.getAsJsonObject("sblevel_colours");
			try {
				for (Map.Entry<String, JsonElement> stringJsonElementEntry : sblevelColours.entrySet()) {
					int nextLevelBracket = Integer.parseInt(stringJsonElementEntry.getKey());
					EnumChatFormatting valueByName = EnumChatFormatting.getValueByName(stringJsonElementEntry
						.getValue()
						.getAsString());
					if (skyblockLevel >= nextLevelBracket) {
						levelColour = valueByName;
					}
				}
			} catch (RuntimeException ignored) {
				// catch both numberformat and getValueByName being wrong
			}

			return skyBlockExperienceColour = levelColour;
		}

		public double getSkyblockLevel() {
			if (skyblockLevel != null) {
				return skyblockLevel;
			}

			int element = Utils.getElementAsInt(Utils.getElement(getProfileJson(), "leveling.experience"), 0);
			return skyblockLevel = (element / 100.0);
		}

		public boolean skillsApiEnabled() {
			return getProfileJson().has("experience_skill_combat");
		}

		/**
		 * NOTE: will NOT return null if skills api is disabled, use {@link SkyblockProfile#skillsApiEnabled()} instead
		 * This can still return null if the leveling constant is not up-to-date
		 *
		 * @return Map containing skills, slayers, HOTM, dungeons & dungeon classes
		 */
		public Map<String, ProfileViewer.Level> getLevelingInfo() {
			if (levelingInfo != null) {
				return levelingInfo;
			}

			JsonObject leveling = Constants.LEVELING;
			if (leveling == null || !leveling.has("social")) {
				Utils.showOutdatedRepoNotification();
				return null;
			}

			JsonObject profileJson = getProfileJson();
			Map<String, ProfileViewer.Level> out = new HashMap<>();

			for (String skillName : skills) {
				float skillExperience = 0;
				if (skillName.equals("social")) {
					// Get the coop's social skill experience since social is a shared skill
					for (Map.Entry<String, JsonElement> memberProfileJson : Utils
						.getElement(outerProfileJson, "members")
						.getAsJsonObject()
						.entrySet()) {
						skillExperience += Utils.getElementAsFloat(
							Utils.getElement(memberProfileJson.getValue(), "experience_skill_social2"),
							0
						);
					}
				} else {
					skillExperience += Utils.getElementAsFloat(
						Utils.getElement(profileJson, "experience_skill_" + skillName),
						0
					);
				}

				JsonArray levelingArray = Utils.getElement(leveling, "leveling_xp").getAsJsonArray();
				if (skillName.equals("runecrafting")) {
					levelingArray = Utils.getElement(leveling, "runecrafting_xp").getAsJsonArray();
				} else if (skillName.equals("social")) {
					levelingArray = Utils.getElement(leveling, "social").getAsJsonArray();
				}

				int maxLevel = ProfileViewerUtils.getLevelingCap(leveling, skillName);
				if (skillName.equals("farming")) {
					maxLevel += Utils.getElementAsInt(Utils.getElement(profileJson, "jacob2.perks.farming_level_cap"), 0);
				}

				out.put(skillName, ProfileViewerUtils.getLevel(levelingArray, skillExperience, maxLevel, false));
			}

			out.put(
				"hotm",
				ProfileViewerUtils.getLevel(
					Utils.getElement(leveling, "HOTM").getAsJsonArray(),
					Utils.getElementAsFloat(Utils.getElement(profileJson, "mining_core.experience"), 0),
					ProfileViewerUtils.getLevelingCap(leveling, "HOTM"),
					false
				)
			);

			out.put(
				"catacombs",
				ProfileViewerUtils.getLevel(
					Utils.getElement(leveling, "catacombs").getAsJsonArray(),
					Utils.getElementAsFloat(Utils.getElement(profileJson, "dungeons.dungeon_types.catacombs.experience"), 0),
					ProfileViewerUtils.getLevelingCap(leveling, "catacombs"),
					false
				)
			);
			out.put(
				"cosmetic_catacombs",
				ProfileViewerUtils.getLevel(
					Utils.getElement(leveling, "catacombs").getAsJsonArray(),
					Utils.getElementAsFloat(Utils.getElement(profileJson, "dungeons.dungeon_types.catacombs.experience"), 0),
					99,
					false
				)
			);

			for (String className : Weight.DUNGEON_CLASS_NAMES) {
				float classExperience = Utils.getElementAsFloat(
					Utils.getElement(profileJson, "dungeons.player_classes." + className + ".experience"),
					0
				);
				out.put(
					className,
					ProfileViewerUtils.getLevel(
						Utils.getElement(leveling, "catacombs").getAsJsonArray(),
						classExperience,
						ProfileViewerUtils.getLevelingCap(leveling, "catacombs"),
						false
					)
				);
				out.put(
					"cosmetic_" + className,
					ProfileViewerUtils.getLevel(
						Utils.getElement(leveling, "catacombs").getAsJsonArray(),
						classExperience,
						99,
						false
					)
				);
			}

			for (String slayerName : Weight.SLAYER_NAMES) {
				float slayerExperience = Utils.getElementAsFloat(Utils.getElement(
					profileJson,
					"slayer_bosses." + slayerName + ".xp"
				), 0);
				out.put(
					slayerName,
					ProfileViewerUtils.getLevel(
						Utils.getElement(leveling, "slayer_xp." + slayerName).getAsJsonArray(),
						slayerExperience,
						slayerName.equals("vampire") ? 5 : 9,
						true
					)
				);
			}

			return levelingInfo = out;
		}

		public int getBestiaryLevel() {
			int beLevel = 0;
			for (ItemStack items : BestiaryData.getBestiaryLocations().keySet()) {
				List<String> mobs = BestiaryData.getBestiaryLocations().get(items);
				if (mobs != null) {
					for (String mob : mobs) {
						if (mob != null) {
							float kills = Utils.getElementAsFloat(Utils.getElement(getProfileJson(), "bestiary.kills_" + mob), 0);
							String type;
							if (BestiaryData.getMobType().get(mob) != null) {
								type = BestiaryData.getMobType().get(mob);
							} else {
								type = "MOB";
							}
							JsonObject leveling = Constants.LEVELING;
							ProfileViewer.Level level = null;
							if (leveling != null && Utils.getElement(leveling, "bestiary." + type) != null) {
								JsonArray levelingArray = Utils.getElement(leveling, "bestiary." + type).getAsJsonArray();
								int levelCap = Utils.getElementAsInt(Utils.getElement(leveling, "bestiary.caps." + type), 0);
								level = ProfileViewerUtils.getLevel(levelingArray, kills, levelCap, false);
							}

							float levelNum = 0;
							if (level != null) {
								levelNum = level.level;
							}
							beLevel += (int) Math.floor(levelNum);
						}
					}
				}
			}
			return beLevel;
		}

		public JsonObject getPetsInfo() {
			if (petsInfo != null) {
				return petsInfo;
			}

			JsonElement petsEle = getProfileJson().get("pets");
			if (petsEle != null && petsEle.isJsonArray()) {
				JsonArray petsArr = petsEle.getAsJsonArray();
				JsonObject activePet = null;

				for (JsonElement petEle : petsEle.getAsJsonArray()) {
					JsonObject petObj = petEle.getAsJsonObject();
					if (petObj.has("active") && petObj.get("active").getAsBoolean()) {
						activePet = petObj;
						break;
					}
				}

				// TODO: STOP DOING THIS AAAAA
				petsInfo = new JsonObject();
				petsInfo.add("active_pet", activePet);
				petsInfo.add("pets", petsArr);
				return petsInfo;
			}

			return null;
		}

		public ProfileCollectionInfo getCollectionInfo() {
			if (collectionsInfo != null) {
				return collectionsInfo;
			}

			// TODO: Is this supposed to be async?
			return collectionsInfo = ProfileCollectionInfo.getCollectionData(outerProfileJson, uuid).getNow(null);
		}

		public PlayerStats.Stats getStats() {
			if (stats != null) {
				return stats;
			}

			if (skillsApiEnabled()) {
				return null;
			}

			return stats = PlayerStats.getStats(
				getLevelingInfo(),
				getInventoryInfo(),
				getPetsInfo(),
				getProfileJson()
			);
		}

		public long getNetworth() {
			if (networth != null) {
				return networth;
			}

			Map<String, JsonArray> inventoryInfo = getInventoryInfo();
			JsonObject profileInfo = getProfileJson();

			HashMap<String, Long> mostExpensiveInternal = new HashMap<>();

			long networth = 0;
			for (Map.Entry<String, JsonArray> entry : inventoryInfo.entrySet()) {
				for (JsonElement element : entry.getValue()) {
					if (element != null && element.isJsonObject()) {
						JsonObject item = element.getAsJsonObject();
						String internalName = item.get("internalname").getAsString();

						if (profileViewer.getManager().auctionManager.isVanillaItem(internalName)) {
							continue;
						}

						JsonObject bzInfo = profileViewer.getManager().auctionManager.getBazaarInfo(internalName);

						long auctionPrice;
						if (bzInfo != null && bzInfo.has("curr_sell")) {
							auctionPrice = (int) bzInfo.get("curr_sell").getAsFloat();
						} else {
							auctionPrice = (long) profileViewer.getManager().auctionManager.getItemAvgBin(internalName);
							if (auctionPrice <= 0) {
								auctionPrice = profileViewer.getManager().auctionManager.getLowestBin(internalName);
							}
						}

						// Backpack, cake bag, etc
						try {
							if (item.has("item_contents")) {
								JsonArray bytesArr = item.get("item_contents").getAsJsonArray();
								byte[] bytes = new byte[bytesArr.size()];
								for (int bytesArrI = 0; bytesArrI < bytesArr.size(); bytesArrI++) {
									bytes[bytesArrI] = bytesArr.get(bytesArrI).getAsByte();
								}
								NBTTagCompound contents_nbt = CompressedStreamTools.readCompressed(new ByteArrayInputStream(bytes));
								NBTTagList items = contents_nbt.getTagList("i", 10);
								for (int j = 0; j < items.tagCount(); j++) {
									if (items.getCompoundTagAt(j).getKeySet().size() > 0) {
										NBTTagCompound nbt = items.getCompoundTagAt(j).getCompoundTag("tag");
										String internalname2 =
											profileViewer.getManager().createItemResolutionQuery().withItemNBT(nbt).resolveInternalName();
										if (internalname2 != null) {
											if (profileViewer.getManager().auctionManager.isVanillaItem(internalname2)) continue;

											JsonObject bzInfo2 = profileViewer.getManager().auctionManager.getBazaarInfo(internalname2);

											long auctionPrice2;
											if (bzInfo2 != null && bzInfo2.has("curr_sell")) {
												auctionPrice2 = (int) bzInfo2.get("curr_sell").getAsFloat();
											} else {
												auctionPrice2 = (long) profileViewer.getManager().auctionManager.getItemAvgBin(internalname2);
												if (auctionPrice2 <= 0) {
													auctionPrice2 = profileViewer.getManager().auctionManager.getLowestBin(internalname2);
												}
											}

											int count2 = items.getCompoundTagAt(j).getByte("Count");

											mostExpensiveInternal.put(
												internalname2,
												auctionPrice2 * count2 + mostExpensiveInternal.getOrDefault(internalname2, 0L)
											);
											networth += auctionPrice2 * count2;
										}
									}
								}
							}
						} catch (IOException ignored) {
						}

						int count = 1;
						if (element.getAsJsonObject().has("count")) {
							count = element.getAsJsonObject().get("count").getAsInt();
						}
						mostExpensiveInternal.put(
							internalName,
							auctionPrice * count + mostExpensiveInternal.getOrDefault(internalName, 0L)
						);
						networth += auctionPrice * count;
					}
				}
			}
			if (networth == 0) return -1;

			networth = (int) (networth * 1.3f);

			JsonObject petsInfo = getPetsInfo();
			if (petsInfo != null && petsInfo.has("pets")) {
				if (petsInfo.get("pets").isJsonArray()) {
					JsonArray pets = petsInfo.get("pets").getAsJsonArray();
					for (JsonElement element : pets) {
						if (element.isJsonObject()) {
							JsonObject pet = element.getAsJsonObject();

							String petname = pet.get("type").getAsString();
							String tier = pet.get("tier").getAsString();
							String tierNum = GuiProfileViewer.RARITY_TO_NUM.get(tier);
							if (tierNum != null) {
								String internalname2 = petname + ";" + tierNum;
								JsonObject info2 = profileViewer.getManager().auctionManager.getItemAuctionInfo(internalname2);
								if (info2 == null || !info2.has("price") || !info2.has("count")) continue;
								int auctionPrice2 = (int) (info2.get("price").getAsFloat() / info2.get("count").getAsFloat());

								networth += auctionPrice2;
							}
						}
					}
				}
			}

			float bankBalance = Utils.getElementAsFloat(Utils.getElement(profileInfo, "banking.balance"), 0);
			float purseBalance = Utils.getElementAsFloat(Utils.getElement(profileInfo, "coin_purse"), 0);
			networth += bankBalance + purseBalance;

			return this.networth = networth;
		}

		public SoopyNetworth getSoopyNetworth(Runnable callback) {
			if (soopyNetworth != null) {
				return soopyNetworth;
			}

			loadSoopyData(callback);
			return new SoopyNetworth(null).asLoading();
		}

		public MuseumData getMuseumData() {
			if (museumData != null) {
				return museumData;
			}

			loadMuseumData();
			return new MuseumData(null).asLoading();
		}
	}
}
