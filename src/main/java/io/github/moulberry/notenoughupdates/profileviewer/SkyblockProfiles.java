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
import io.github.moulberry.notenoughupdates.util.Constants;
import io.github.moulberry.notenoughupdates.util.Utils;
import io.github.moulberry.notenoughupdates.util.hypixelapi.ProfileCollectionInfo;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumChatFormatting;

import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class SkyblockProfiles {
	private static final HashMap<String, String> petRarityToNumMap = new HashMap<String, String>() {
		{
			put("COMMON", "0");
			put("UNCOMMON", "1");
			put("RARE", "2");
			put("EPIC", "3");
			put("LEGENDARY", "4");
			put("MYTHIC", "5");
		}
	};
	private static final String defaultNbtData = "Hz8IAAAAAAAAAD9iYD9kYD9kAAMAPwI/Gw0AAAA=";
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
	public Map<String, SkyblockProfile> nameToProfile = null;
	private JsonArray profilesArray;
	private List<String> profileNames = new ArrayList<>();
	private String selectedProfileName;
	private final String uuid;
	private final HashMap<String, SoopyNetworthData> soopyNetworth = new HashMap<>();
	private final AtomicBoolean updatingSkyblockProfilesState = new AtomicBoolean(false);
	private final AtomicBoolean updatingGuildInfoState = new AtomicBoolean(false);
	private final AtomicBoolean updatingPlayerStatusState = new AtomicBoolean(false);
	private final AtomicBoolean updatingSoopyNetworth = new AtomicBoolean(false);
	private final AtomicBoolean updatingBingoInfo = new AtomicBoolean(false);
	private long soopyNetworthLeaderboardPosition = -1; //-1 = default, -2 = loading, -3 = error
	private long soopyWeightLeaderboardPosition = -1; //-1 = default, -2 = loading, -3 = error
	private JsonObject guildInformation = null;
	private JsonObject playerStatus = null;
	private JsonObject bingoInformation = null;
	private long lastPlayerInfoState = 0;
	private long lastStatusInfoState = 0;
	private long lastGuildInfoState = 0;
	private long lastBingoInfoState = 0;

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

		public SkyblockProfile(JsonObject outerProfileJson) {
			this.outerProfileJson = outerProfileJson;
			this.gamemode = outerProfileJson.get("game_mode").getAsString();
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

		public String getGamemode() {
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
					JsonObject backpackContentsObj = Utils.getElement(profileJson, "backpack_contents").getAsJsonObject();
					JsonObject backpackIconsObj = Utils.getElement(profileJson, "backpack_icons").getAsJsonObject();
					JsonObject backpackData = getBackpackData(backpackContentsObj, backpackIconsObj);
					contents = backpackData.getAsJsonArray("contents");
					inventoryNameToInfo.put("backpack_sizes", backpackData.getAsJsonArray("backpack_sizes"));
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
							JsonObject item = profileViewer.manager.getJsonFromNBTEntry(items.getCompoundTagAt(j));
							contents.add(item);
						}
					} catch (IOException ignored) {
					}
				}

				inventoryNameToInfo.put(invName, contents);
			}

			return inventoryNameToInfo;
		}

		public JsonObject getBackpackData(JsonObject backpackContentsJson, JsonObject backpackIcons) {
			// TODO: improve this becausec I CBA to understand this mess
			if (backpackContentsJson == null || backpackIcons == null) {
				JsonObject bundledReturn = new JsonObject();
				bundledReturn.add("contents", new JsonArray());
				bundledReturn.add("backpack_sizes", new JsonArray());

				return bundledReturn;
			}

			String[] backpackArray = new String[0];

			//Create backpack array which sizes up
			for (Map.Entry<String, JsonElement> backpackIcon : backpackIcons.entrySet()) {
				if (backpackIcon.getValue() instanceof JsonObject) {
					JsonObject backpackData = (JsonObject) backpackContentsJson.get(backpackIcon.getKey());
					String bytes = Utils.getElementAsString(backpackData.get("data"), defaultNbtData);
					backpackArray = growArray(bytes, Integer.parseInt(backpackIcon.getKey()), backpackArray);
				}
			}

			//reduce backpack array to filter out not existent backpacks
			{
				String[] tempBackpackArray = new String[0];
				for (String s : backpackArray) {
					if (s != null) {
						String[] veryTempBackpackArray = new String[tempBackpackArray.length + 1];
						System.arraycopy(tempBackpackArray, 0, veryTempBackpackArray, 0, tempBackpackArray.length);

						veryTempBackpackArray[veryTempBackpackArray.length - 1] = s;
						tempBackpackArray = veryTempBackpackArray;
					}
				}
				backpackArray = tempBackpackArray;
			}

			JsonArray backpackSizes = new JsonArray();
			JsonArray contents = new JsonArray();

			for (String backpack : backpackArray) {
				try {
					NBTTagCompound inv_contents_nbt = CompressedStreamTools.readCompressed(
						new ByteArrayInputStream(Base64.getDecoder().decode(backpack))
					);
					NBTTagList items = inv_contents_nbt.getTagList("i", 10);

					backpackSizes.add(new JsonPrimitive(items.tagCount()));
					for (int j = 0; j < items.tagCount(); j++) {
						JsonObject item = profileViewer.manager.getJsonFromNBTEntry(items.getCompoundTagAt(j));
						contents.add(item);
					}
				} catch (IOException ignored) {
				}
			}

			JsonObject bundledReturn = new JsonObject();
			bundledReturn.add("contents", contents);
			bundledReturn.add("backpack_sizes", backpackSizes);

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

		public Map<String, ProfileViewer.Level> getLevelingInfo() {
			if (levelingInfo != null) {
				return levelingInfo;
			}

			JsonObject leveling = Constants.LEVELING;
			if (leveling == null || !leveling.has("social")) {
				Utils.showOutdatedRepoNotification();
				return null;
			}

			Map<String, ProfileViewer.Level> out = new HashMap<>();
			JsonObject profileJson = getProfileJson();

			float totalSkillXP = 0;
			for (String skillName : skills) {
				float skillExperience = 0;
				if (skillName.equals("social")) {
					// Get the coop's social skill experience since social is a shared skill
					for (Map.Entry<String, JsonElement> memberProfileJson : outerProfileJson.entrySet()) {
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

				totalSkillXP += skillExperience;

				JsonArray levelingArray = Utils.getElement(leveling, "leveling_xp").getAsJsonArray();
				if (skillName.equals("runecrafting")) {
					levelingArray = Utils.getElement(leveling, "runecrafting_xp").getAsJsonArray();
				} else if (skillName.equals("social")) {
					levelingArray = Utils.getElement(leveling, "social").getAsJsonArray();
				}

				int maxLevel =
					getLevelingCap(leveling, skillName) +
						(
							skillName.equals("farming")
								? Utils.getElementAsInt(Utils.getElement(profileJson, "jacob2.perks.farming_level_cap"), 0)
								: 0
						);
				out.put(skillName, ProfileViewer.getLevel(levelingArray, skillExperience, maxLevel, false));
			}

			// Skills API disabled?
			// ^ Maybe check if combat exp field exists instead of this
			if (totalSkillXP <= 0) {
				return null;
			}

			out.put(
				"hotm",
				ProfileViewer.getLevel(
					Utils.getElement(leveling, "leveling_xp").getAsJsonArray(),
					Utils.getElementAsFloat(Utils.getElement(profileJson, "mining_core.experience"), 0),
					getLevelingCap(leveling, "HOTM"),
					false
				)
			);

			out.put(
				"catacombs",
				ProfileViewer.getLevel(
					Utils.getElement(leveling, "catacombs").getAsJsonArray(),
					Utils.getElementAsFloat(Utils.getElement(profileJson, "dungeons.dungeon_types.catacombs.experience"), 0),
					getLevelingCap(leveling, "catacombs"),
					false
				)
			);

			List<String> dungeonClasses = Arrays.asList("healer", "tank", "mage", "archer", "berserk");
			for (String className : dungeonClasses) {
				float classExperience = Utils.getElementAsFloat(
					Utils.getElement(profileJson, "dungeons.player_classes." + className + ".experience"),
					0
				);
				out.put(
					className,
					ProfileViewer.getLevel(
						Utils.getElement(leveling, "catacombs").getAsJsonArray(),
						classExperience,
						getLevelingCap(leveling, "catacombs"),
						false
					)
				);
			}
			for (String slayerName : ProfileViewer.SLAYERS) {
				float slayerExperience = Utils.getElementAsFloat(Utils.getElement(
					profileJson,
					"slayer_bosses." + slayerName + ".xp"
				), 0);
				out.put(
					slayerName,
					ProfileViewer.getLevel(
						Utils.getElement(leveling, "slayer_xp." + slayerName).getAsJsonArray(),
						slayerExperience,
						9,
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
								level = ProfileViewer.getLevel(levelingArray, kills, levelCap, false);
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

						if (profileViewer.manager.auctionManager.isVanillaItem(internalName)) {
							continue;
						}

						JsonObject bzInfo = profileViewer.manager.auctionManager.getBazaarInfo(internalName);

						long auctionPrice;
						if (bzInfo != null && bzInfo.has("curr_sell")) {
							auctionPrice = (int) bzInfo.get("curr_sell").getAsFloat();
						} else {
							auctionPrice = (long) profileViewer.manager.auctionManager.getItemAvgBin(internalName);
							if (auctionPrice <= 0) {
								auctionPrice = profileViewer.manager.auctionManager.getLowestBin(internalName);
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
											profileViewer.manager.createItemResolutionQuery().withItemNBT(nbt).resolveInternalName();
										if (internalname2 != null) {
											if (profileViewer.manager.auctionManager.isVanillaItem(internalname2)) continue;

											JsonObject bzInfo2 = profileViewer.manager.auctionManager.getBazaarInfo(internalname2);

											long auctionPrice2;
											if (bzInfo2 != null && bzInfo2.has("curr_sell")) {
												auctionPrice2 = (int) bzInfo2.get("curr_sell").getAsFloat();
											} else {
												auctionPrice2 = (long) profileViewer.manager.auctionManager.getItemAvgBin(internalname2);
												if (auctionPrice2 <= 0) {
													auctionPrice2 = profileViewer.manager.auctionManager.getLowestBin(internalname2);
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
							String tierNum = petRarityToNumMap.get(tier);
							if (tierNum != null) {
								String internalname2 = petname + ";" + tierNum;
								JsonObject info2 = profileViewer.manager.auctionManager.getItemAuctionInfo(internalname2);
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
	}

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

		profileViewer.manager.apiUtils
			.newHypixelApiRequest("status")
			.queryArgument("uuid", uuid)
			.requestJson()
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

		NotEnoughUpdates.INSTANCE.manager.apiUtils
			.newHypixelApiRequest("skyblock/bingo")
			.queryArgument("uuid", uuid)
			.requestJson()
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

	public class SoopyNetworthData {
		private final HashMap<String, Long> categoryWorth;
		private Long totalWorth;
		private final String[] keys;

		SoopyNetworthData(JsonObject nwData) {
			categoryWorth = new HashMap<>();

			if (nwData == null || nwData.isJsonNull()) {
				totalWorth = -1L;
				keys = new String[0];
				return;
			}
			if (nwData.get("total").isJsonNull()) {
				totalWorth = -1L;
				keys = new String[0];
				return;
			}

			totalWorth = nwData.get("total").getAsLong();
			for (Map.Entry<String, JsonElement> entry : nwData.get("categories").getAsJsonObject().entrySet()) {
				if (entry.getValue().isJsonNull()) {
					continue;
				}
				categoryWorth.put(entry.getKey(), entry.getValue().getAsLong());
			}

			//Sort keys based on category value
			keys = categoryWorth
				.keySet()
				.stream()
				.sorted(Comparator.comparingLong(k -> getCategory((String) k)).reversed())
				.toArray(String[]::new);
		}

		private SoopyNetworthData setLoading() {
			totalWorth = -2L;
			return this;
		}

		public long getTotal() {
			return totalWorth;
		}

		public long getCategory(String name) {
			if (categoryWorth.containsKey(name)) return categoryWorth.get(name);
			return 0;
		}

		public String[] getCategories() {
			return keys;
		}
	}

	/**
	 * -1 = default, -2 = loading, -3 = error
	 * >= 0 = actual position
	 */
	public long getSoopyNetworthLeaderboardPosition() {
		return "d0e05de76067454dbeaec6d19d886191".equals(uuid) ? 1 : soopyNetworthLeaderboardPosition;
	}

	public long getSoopyWeightLeaderboardPosition() {
		return "d0e05de76067454dbeaec6d19d886191".equals(uuid) ? 1 : soopyWeightLeaderboardPosition;
	}

	public boolean isProfileMaxSoopyWeight(String profileName) {
		String highestProfileName = "";
		double largestProfileWeight = 0;

		for (Map.Entry<String, SkyblockProfile> profileEntry : nameToProfile.entrySet()) {
			Map<String, ProfileViewer.Level> levelingInfo = profileEntry.getValue().getLevelingInfo();
			if (levelingInfo == null) {
				continue;
			}
			SenitherWeight senitherWeight = new SenitherWeight(levelingInfo);
			double weightValue = senitherWeight.getTotalWeight().getRaw();

			if (weightValue > largestProfileWeight) {
				largestProfileWeight = weightValue;
				highestProfileName = profileEntry.getKey();
			}
		}

		return highestProfileName.equals(profileName);
	}

	/**
	 * Returns SoopyNetworthData with total = -1 if error
	 * Returns null if still loading
	 */
	public SoopyNetworthData getSoopyNetworth(String profileName, Runnable callback) {
		if (profileName == null) profileName = selectedProfileName;
		if (soopyNetworth.get(profileName) != null) {
			callback.run();
			return soopyNetworth.get(profileName);
		}

		getOrLoadSkyblockProfiles(() -> {});
		if (nameToProfile == null)
			return null;                                              //Not sure how to support the callback in these cases
		if (updatingSoopyNetworth.get())
			return new SoopyNetworthData(null).setLoading(); //It shouldent really matter tho as these should never occur in /peek
		updatingSoopyNetworth.set(true);

		soopyNetworthLeaderboardPosition = -2; //loading
		profileViewer.manager.apiUtils
			.request()
			.url("https://soopy.dev/api/v2/leaderboard/networth/user/" + this.uuid)
			.requestJson()
			.handle((jsonObject, throwable) -> {
				if (throwable != null) throwable.printStackTrace();
				if (throwable != null || !jsonObject.has("success") || !jsonObject.get("success").getAsBoolean()
					|| !jsonObject.has("data")
					|| !jsonObject.get("data").getAsJsonObject().has("data")
					|| !jsonObject.get("data").getAsJsonObject().get("data").getAsJsonObject().has("position")) {
					//Something went wrong
					//Set profile lb position to -3 to indicate that
					soopyNetworthLeaderboardPosition = -3; //error
					return null;
				}
				soopyNetworthLeaderboardPosition = jsonObject.get("data").getAsJsonObject().get("data").getAsJsonObject().get(
					"position").getAsLong();
				return null;
			});

		soopyWeightLeaderboardPosition = -2; //loading
		profileViewer.manager.apiUtils
			.request()
			.url("https://soopy.dev/api/v2/leaderboard/weight/user/" + this.uuid)
			.requestJson()
			.handle((jsonObject, throwable) -> {
				if (throwable != null) throwable.printStackTrace();
				if (throwable != null || !jsonObject.has("success") || !jsonObject.get("success").getAsBoolean()
					|| !jsonObject.has("data")
					|| !jsonObject.get("data").getAsJsonObject().has("data")
					|| !jsonObject.get("data").getAsJsonObject().get("data").getAsJsonObject().has("position")) {
					//Something went wrong
					//Set profile lb position to -3 to indicate that
					soopyWeightLeaderboardPosition = -3; //error
					return null;
				}
				soopyWeightLeaderboardPosition = jsonObject.get("data").getAsJsonObject().get("data").getAsJsonObject().get(
					"position").getAsLong();
				return null;
			});

		profileViewer.manager.apiUtils
			.request()
			.url("https://soopy.dev/api/v2/player_networth/" + this.uuid)
			.method("POST")
			.postData("application/json", profilesArray.toString())
			.requestJson()
			.handle((jsonObject, throwable) -> {
				if (throwable != null) throwable.printStackTrace();
				if (throwable != null || !jsonObject.has("success") || !jsonObject.get("success").getAsBoolean()) {
					//Something went wrong
					//Set profile networths to null to indicate that
					for (int i = 0; i < profilesArray.size(); i++) {
						if (!profilesArray.get(i).isJsonObject()) {
							return null;
						}
						JsonObject profile = profilesArray.get(i).getAsJsonObject();

						String cuteName = profile.get("cute_name").getAsString();

						soopyNetworth.put(cuteName, new SoopyNetworthData(null));
					}
					updatingSoopyNetworth.set(false);
					callback.run();
					return null;
				}

				//Success, update networth data
				for (int i = 0; i < profilesArray.size(); i++) {
					if (!profilesArray.get(i).isJsonObject()) {
						return null;
					}
					JsonObject profile = profilesArray.get(i).getAsJsonObject();

					String cuteName = profile.get("cute_name").getAsString();
					String profileId = profile.get("profile_id").getAsString();

					SoopyNetworthData networth;
					if (jsonObject.getAsJsonObject("data").get(profileId).isJsonNull()) {
						networth = new SoopyNetworthData(null);
					} else {
						networth = new SoopyNetworthData(jsonObject.getAsJsonObject("data").get(profileId).getAsJsonObject());
					}

					soopyNetworth.put(cuteName, networth);
				}

				updatingSoopyNetworth.set(false);
				callback.run();
				return null;
			});
		return null;
	}

	public SkyblockProfile getProfile(String profileName) {
		return nameToProfile.get(profileName);
	}

	public SkyblockProfile getSelectedProfile() {
		return nameToProfile.get(getSelectedProfileName());
	}

	public String getSelectedProfileName() {
		return selectedProfileName;
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

		profileViewer.manager.apiUtils
			.newHypixelApiRequest("skyblock/profiles")
			.queryArgument("uuid", uuid)
			.requestJson()
			.handle((profilesJson, throwable) -> {
				updatingSkyblockProfilesState.set(false);

				if (profilesJson != null && profilesJson.has("success")
					&& profilesJson.get("success").getAsBoolean() && profilesJson.has("profiles")) {
					profilesArray = profilesJson.getAsJsonArray("profiles");
					nameToProfile = new HashMap<>();

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
								selectedProfileName = profileName;
							}
							nameToProfile.put(profileName, new SkyblockProfile(profile));
							profileNames.add(profileName);
						}
					}

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

		profileViewer.manager.apiUtils
			.newHypixelApiRequest("guild")
			.queryArgument("player", uuid)
			.requestJson()
			.handle((jsonObject, ex) -> {
				updatingGuildInfoState.set(false);

				if (jsonObject != null && jsonObject.has("success") && jsonObject.get("success").getAsBoolean()) {
					if (!jsonObject.has("guild")) {
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

	public int getLevelingCap(JsonObject leveling, String skillName) {
		JsonElement capsElement = Utils.getElement(leveling, "leveling_caps");
		return capsElement != null && capsElement.isJsonObject() && capsElement.getAsJsonObject().has(skillName)
			? capsElement.getAsJsonObject().get(skillName).getAsInt()
			: 50;
	}

	public String[] growArray(String bytes, int index, String[] oldArray) {
		int newSize = Math.max(index + 1, oldArray.length);

		String[] newArray = new String[newSize];
		System.arraycopy(oldArray, 0, newArray, 0, oldArray.length);
		newArray[index] = bytes;

		return newArray;
	}

	public String getUuid() {
		return uuid;
	}

	public @Nullable JsonObject getHypixelProfile() {
		return profileViewer.uuidToHypixelProfile.getOrDefault(uuid, null);
	}
}
