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

package io.github.moulberry.notenoughupdates.miscfeatures;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.core.config.Position;
import io.github.moulberry.notenoughupdates.core.util.StringUtils;
import io.github.moulberry.notenoughupdates.core.util.lerp.LerpUtils;
import io.github.moulberry.notenoughupdates.listener.RenderListener;
import io.github.moulberry.notenoughupdates.options.NEUConfig;
import io.github.moulberry.notenoughupdates.overlays.EquipmentOverlay;
import io.github.moulberry.notenoughupdates.overlays.TextOverlay;
import io.github.moulberry.notenoughupdates.overlays.TextOverlayStyle;
import io.github.moulberry.notenoughupdates.profileviewer.GuiProfileViewer;
import io.github.moulberry.notenoughupdates.profileviewer.ProfileViewer;
import io.github.moulberry.notenoughupdates.util.Constants;
import io.github.moulberry.notenoughupdates.util.ProfileApiSyncer;
import io.github.moulberry.notenoughupdates.util.Utils;
import io.github.moulberry.notenoughupdates.util.XPInformation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Items;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.commons.lang3.text.WordUtils;
import org.lwjgl.util.vector.Vector2f;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class PetInfoOverlay extends TextOverlay {
	private static final Pattern XP_BOOST_PATTERN = Pattern.compile(
		"PET_ITEM_(COMBAT|FISHING|MINING|FORAGING|ALL|FARMING)_(SKILL|SKILLS)_BOOST_(COMMON|UNCOMMON|RARE|EPIC)");
	private static final Pattern PET_CONTAINER_PAGE = Pattern.compile("\\((\\d)/(\\d)\\) Pets");

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	public PetInfoOverlay(
		Position position,
		Supplier<List<String>> dummyStrings,
		Supplier<TextOverlayStyle> styleSupplier
	) {
		super(position, dummyStrings, styleSupplier);
	}

	public enum Rarity {
		COMMON(0, 0, 1, EnumChatFormatting.WHITE),
		UNCOMMON(6, 1, 2, EnumChatFormatting.GREEN),
		RARE(11, 2, 3, EnumChatFormatting.BLUE),
		EPIC(16, 3, 4, EnumChatFormatting.DARK_PURPLE),
		LEGENDARY(20, 4, 5, EnumChatFormatting.GOLD),
		MYTHIC(20, 5, 5, EnumChatFormatting.LIGHT_PURPLE);

		public final int petOffset;
		public final EnumChatFormatting chatFormatting;
		public final int petId;
		public final int beastcreatMultiplyer;

		Rarity(int petOffset, int petId, int beastcreatMultiplyer, EnumChatFormatting chatFormatting) {
			this.chatFormatting = chatFormatting;
			this.petOffset = petOffset;
			this.petId = petId;
			this.beastcreatMultiplyer = beastcreatMultiplyer;
		}

		public static Rarity getRarityFromColor(EnumChatFormatting chatFormatting) {
			for (int i = 0; i < Rarity.values().length; i++) {
				if (Rarity.values()[i].chatFormatting.equals(chatFormatting))
					return Rarity.values()[i];
			}
			return COMMON;
		}
	}

	public static class Pet {
		public String petType;
		public Rarity rarity;
		public GuiProfileViewer.PetLevel petLevel;
		public String petXpType;
		public String petItem;
		public String skin;

		public String getPetId(boolean withoutBoost) {
			return petType + ";" + (withoutBoost ? rarity.petId - 1 : rarity.petId);

		}

	}

	public static class PetConfig {
		public HashMap<Integer, Pet> petMap = new HashMap<>();

		private int selectedPet = -1;
		private int selectedPet2 = -1;

		public int tamingLevel = 1;
		public float beastMultiplier = 0;
	}

	private static long lastPetSelect = -1;
	private static PetConfig config = new PetConfig();

	private static long lastUpdate = 0;
	private static float levelXpLast = 0;

	private static final LinkedList<Float> xpGainQueue = new LinkedList<>();
	private static float xpGainHourLast = -1;
	private static float xpGainHour = -1;
	private static int pauseCountdown = 0;

	private static float xpGainHourSecondPet = -1;

	private int xpAddTimer = 0;

	public static void loadConfig(File file) {
		try (
			BufferedReader reader = new BufferedReader(new InputStreamReader(
				Files.newInputStream(file.toPath()),
				StandardCharsets.UTF_8
			))
		) {
			config = GSON.fromJson(reader, PetConfig.class);
		} catch (Exception ignored) {
		}
		if (config == null) {
			config = new PetConfig();
		}
	}

	public static void saveConfig(File file) {
		try {
			file.createNewFile();
			try (
				BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
					Files.newOutputStream(file.toPath()),
					StandardCharsets.UTF_8
				))
			) {
				writer.write(GSON.toJson(config));
			}
		} catch (Exception ignored) {
		}
	}

	public static void clearPet() {
		config.selectedPet = -1;
		config.selectedPet2 = -1;
	}

	public static void setCurrentPet(int index) {
		config.selectedPet2 = config.selectedPet;
		xpGainHourSecondPet = xpGainHour;
		xpGainHourLast = xpGainHour;
		xpGainQueue.clear();
		config.selectedPet = index;
	}

	public static Pet getCurrentPet() {
		return config.petMap.get(config.selectedPet);
	}

	public static Pet getCurrentPet2() {
		if (!NotEnoughUpdates.INSTANCE.config.petOverlay.dualPets) return null;
		if (config.selectedPet == config.selectedPet2) return null;
		return config.petMap.get(config.selectedPet2);
	}

	public float getLevelPercent(Pet pet) {
		DecimalFormat df = new DecimalFormat("#.#", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
		if (pet == null) return 0;
		try {
			return Float.parseFloat(df.format(pet.petLevel.levelPercentage * 100f));
		} catch (Exception ignored) {
			return 0;
		}
	}

	private static int getIdForPet(Pet pet) {
		for (Map.Entry<Integer, Pet> entry : config.petMap.entrySet()) {
			if (entry.getValue() == pet) {
				return entry.getKey();
			}
		}
		return -1;
	}

	private static int getClosestPetIndex(String petType, int petId, String petItem, float petLevel) {
		Pet pet = getClosestPet(petType, petId, petItem, petLevel);
		if (pet == null) {
			return -1;
		} else {
			return getIdForPet(pet);
		}
	}

	private static Pet getClosestPet(String petType, int petId, String petItem, float petLevel) {
		Set<Pet> pets = config.petMap.values().stream().filter(pet -> pet.petType.equals(petType) &&
			pet.rarity.petId == petId).collect(
			Collectors.toSet());

		if (pets.isEmpty()) {
			return null;
		}

		if (pets.size() == 1) {
			return pets.iterator().next();
		}

		Set<Pet> itemMatches = pets
			.stream()
			.filter(pet -> Objects.equals(petItem, pet.petItem))
			.collect(Collectors.toSet());

		if (itemMatches.size() == 1) {
			return itemMatches.iterator().next();
		}
		if (itemMatches.size() > 1) {
			pets = itemMatches;
		}

		float closestXp = -1;
		Pet closestPet = null;

		for (Pet pet : pets) {
			float distXp = Math.abs(pet.petLevel.level - petLevel);

			if (closestPet == null || distXp < closestXp) {
				closestXp = distXp;
				closestPet = pet;
			}
		}

		return closestPet;
	}

	private static void getAndSetPet(ProfileViewer.Profile profile) {
		Map<String, ProfileViewer.Level> skyblockInfo = profile.getSkyblockInfo(profile.getLatestProfile());
		JsonObject invInfo = profile.getInventoryInfo(profile.getLatestProfile());
		JsonObject profileInfo = profile.getProfileInformation(profile.getLatestProfile());
		if (invInfo != null && profileInfo != null) {
			JsonObject stats = profileInfo.get("stats").getAsJsonObject();
			boolean hasBeastmasterCrest = false;
			Rarity currentBeastRarity = Rarity.COMMON;
			for (JsonElement talisman : invInfo.get("talisman_bag").getAsJsonArray()) {
				if (talisman.isJsonNull()) continue;
				String internalName = talisman.getAsJsonObject().get("internalname").getAsString();
				if (internalName.startsWith("BEASTMASTER_CREST")) {
					hasBeastmasterCrest = true;
					try {
						Rarity talismanRarity = Rarity.valueOf(internalName.replace("BEASTMASTER_CREST_", ""));
						if (talismanRarity.beastcreatMultiplyer > currentBeastRarity.beastcreatMultiplyer)
							currentBeastRarity = talismanRarity;
					} catch (Exception ignored) {
					}
				}
			}
			if (hasBeastmasterCrest) {
				if (stats.has("mythos_kills")) {
					int mk = stats.get("mythos_kills").getAsInt();
					float petXpBoost = mk > 10000 ? 1f : mk > 7500 ? 0.9f : mk > 5000 ? 0.8f : mk > 2500 ? 0.7f :
						mk > 1000
							? 0.6f
							: mk > 500
								? 0.5f
								: mk > 250
									? 0.4f
									: mk > 100
										? 0.3f
										: mk > 25 ? 0.2f : 0.1f;
					config.beastMultiplier = petXpBoost * currentBeastRarity.beastcreatMultiplyer;
				} else {
					config.beastMultiplier = 0.1f * currentBeastRarity.beastcreatMultiplyer;
				}
			}
		}
		if (skyblockInfo != null) config.tamingLevel = (int) skyblockInfo.get("taming").level;

		//JsonObject petObject = profile.getPetsInfo(profile.getLatestProfile());
        /*JsonObject petsJson = Constants.PETS;
        if(petsJson != null) {
            if(petObject != null) {
                boolean forceUpdateLevels = System.currentTimeMillis() - lastXpGain > 30000;
                Set<String> foundPets = new HashSet<>();
                Set<Pet> addedPets = new HashSet<>();
                for(int i = 0; i < petObject.getAsJsonArray("pets").size(); i++) {
                    JsonElement petElement = petObject.getAsJsonArray("pets").get(i);
                    JsonObject petObj = petElement.getAsJsonObject();
                    Pet pet = new Pet();
                    pet.petType = petObj.get("type").getAsString();
                    Rarity rarity;
                    try {
                        rarity = Rarity.valueOf(petObj.get("tier").getAsString());
                    } catch(Exception ignored) {
                        rarity = Rarity.COMMON;
                    }
                    pet.rarity = rarity;
                    pet.petLevel = GuiProfileViewer.getPetLevel(petsJson.get("pet_levels").getAsJsonArray(), rarity.petOffset, petObj.get("exp").getAsFloat());
                    JsonElement heldItem = petObj.get("heldItem");
                    pet.petItem = heldItem.isJsonNull() ? null : heldItem.getAsString();
                    if(rarity != Rarity.MYTHIC && pet.petItem != null && pet.petItem.equals("PET_ITEM_TIER_BOOST")) {
                        rarity = Rarity.values()[rarity.ordinal()+1];
                    }
                    JsonObject petTypes = petsJson.get("pet_types").getAsJsonObject();
                    pet.petXpType = petTypes.has(pet.petType) ? petTypes.get(pet.petType.toUpperCase()).getAsString().toLowerCase() : "unknown";

                    Pet closest = null;
                    if(petList.containsKey(pet.petType + ";" + pet.rarity.petId)) {
                        closest = getClosestPet(pet);
                        if(addedPets.contains(closest)) {
                            closest = null;
                        }

                        if(closest != null) {
                            if(!forceUpdateLevels || Math.floor(pet.petLevel.level) < Math.floor(closest.petLevel.level)) {
                                pet.petLevel = closest.petLevel;
                            }
                            petList.get(pet.petType + ";" + pet.rarity.petId).remove(closest);
                        }
                    }
                    foundPets.add(pet.petType + ";" + pet.rarity.petId);
                    petList.computeIfAbsent(pet.petType + ";" + pet.rarity.petId, k->new HashSet<>()).add(pet);
                    addedPets.add(pet);

                    if(petObj.get("active").getAsBoolean()) {
                        if(currentPet == null && !setActivePet) {
                            currentPet = pet;
                        } else if(closest == currentPet) {
                            currentPet = pet;
                        }
                    }
                }
                petList.keySet().retainAll(foundPets);
                setActivePet = true;
            }
        }*/
	}

	private float interp(float now, float last) {
		float interp = now;
		if (last >= 0 && last != now) {
			float factor = (System.currentTimeMillis() - lastUpdate) / 1000f;
			factor = LerpUtils.clampZeroOne(factor);
			interp = last + (now - last) * factor;
		}
		return interp;
	}

	private List<String> createStringsForPet(Pet currentPet, boolean secondPet) {
		float levelXp = currentPet.petLevel.levelXp;
		if (!secondPet) levelXp = interp(currentPet.petLevel.levelXp, levelXpLast);
		if (levelXp < 0) levelXp = 0;

		String petName =
			EnumChatFormatting.GREEN + "[Lvl " + (int) currentPet.petLevel.level + "] " + currentPet.rarity.chatFormatting +
				WordUtils.capitalizeFully(currentPet.petType.replace("_", " "));

		String lvlStringShort = EnumChatFormatting.AQUA + "" + roundFloat(levelXp) + "/" +
			roundFloat(currentPet.petLevel.currentLevelRequirement)
			+ EnumChatFormatting.YELLOW + " (" + getLevelPercent(currentPet) + "%)";

		String lvlString = EnumChatFormatting.AQUA + "" + Utils.shortNumberFormat(levelXp, 0) + "/" +
			Utils.shortNumberFormat(currentPet.petLevel.currentLevelRequirement, 0)
			+ EnumChatFormatting.YELLOW + " (" + getLevelPercent(currentPet) + "%)";

		float xpGain;
		if (!secondPet) {
			xpGain = interp(xpGainHour, xpGainHourLast);
		} else {
			xpGain = xpGainHourSecondPet;
		}
		if (xpGain < 0) xpGain = 0;
		String xpGainString = EnumChatFormatting.AQUA + "XP/h: " +
			EnumChatFormatting.YELLOW + roundFloat(xpGain);
		if (!secondPet && xpGain > 0 && levelXp != levelXpLast) {
			if (pauseCountdown <= 0) {
				xpGainString += EnumChatFormatting.RED + " (PAUSED)";
			} else {
				pauseCountdown--;
			}
		} else {
			pauseCountdown = 60;
		}

		String totalXpString =
			EnumChatFormatting.AQUA + "Total XP: " + EnumChatFormatting.YELLOW + roundFloat(currentPet.petLevel.totalXp);

		String petItemStr = EnumChatFormatting.AQUA + "Held Item: " + EnumChatFormatting.RED + "None";
		if (currentPet.petItem != null) {
			JsonObject json = NotEnoughUpdates.INSTANCE.manager.getItemInformation().get(currentPet.petItem);
			if (json != null) {
				String name = NotEnoughUpdates.INSTANCE.manager.jsonToStack(json).getDisplayName();
				petItemStr = EnumChatFormatting.AQUA + "Held Item: " + name;
			}
		}

		String etaStr = null;
		String etaMaxStr = null;
		if (currentPet.petLevel.level < currentPet.petLevel.maxLevel) {
			float remaining = currentPet.petLevel.currentLevelRequirement - currentPet.petLevel.levelXp;
			if (remaining > 0) {
				if (xpGain < 1000) {
					etaStr = EnumChatFormatting.AQUA + "Until L" + (int) (currentPet.petLevel.level + 1) + ": " +
						EnumChatFormatting.YELLOW + "N/A";
				} else {
					etaStr = EnumChatFormatting.AQUA + "Until L" + (int) (currentPet.petLevel.level + 1) + ": " +
						EnumChatFormatting.YELLOW + Utils.prettyTime((long) (remaining) * 1000 * 60 * 60 / (long) xpGain);
				}
			}

			if (currentPet.petLevel.level < (currentPet.petLevel.maxLevel - 1) ||
				!NotEnoughUpdates.INSTANCE.config.petOverlay.petOverlayText.contains(6)) {
				float remainingMax = currentPet.petLevel.maxXP - currentPet.petLevel.totalXp;
				if (remaining > 0) {
					if (xpGain < 1000) {
						etaMaxStr = EnumChatFormatting.AQUA + "Until L" + currentPet.petLevel.maxLevel + ": " +
							EnumChatFormatting.YELLOW + "N/A";
					} else {
						etaMaxStr = EnumChatFormatting.AQUA + "Until L" + currentPet.petLevel.maxLevel + ": " +
							EnumChatFormatting.YELLOW + Utils.prettyTime((long) (remainingMax) * 1000 * 60 * 60 / (long) xpGain);
					}
				}
			}
		}

		String finalEtaStr = etaStr;
		String finalEtaMaxStr = etaMaxStr;
		String finalXpGainString = xpGainString;
		String finalPetItemStr = petItemStr;
		return new ArrayList<String>() {{
			for (int index : NotEnoughUpdates.INSTANCE.config.petOverlay.petOverlayText) {
				switch (index) {
					case 0:
						add(petName);
						break;
					case 1:
						add(lvlStringShort);
						break;
					case 2:
						add(lvlString);
						break;
					case 3:
						add(finalXpGainString);
						break;
					case 4:
						add(totalXpString);
						break;
					case 5:
						add(finalPetItemStr);
						break;
					case 6:
						if (finalEtaStr != null) add(finalEtaStr);
						break;
					case 7:
						if (finalEtaMaxStr != null) add(finalEtaMaxStr);
						break;
				}
			}
		}};
	}

	@Override
	public void updateFrequent() {
		Pet currentPet = getCurrentPet();
		if (!NotEnoughUpdates.INSTANCE.config.petOverlay.enablePetInfo || currentPet == null) {
			overlayStrings = null;
		} else {
			overlayStrings = new ArrayList<>();

			overlayStrings.addAll(createStringsForPet(currentPet, false));

			Pet currentPet2 = getCurrentPet2();
			if (currentPet2 != null) {
				overlayStrings.add("");
				overlayStrings.addAll(createStringsForPet(currentPet2, true));
			}

		}
	}

	public void update() {
		if (!NotEnoughUpdates.INSTANCE.config.petOverlay.enablePetInfo &&
			!NotEnoughUpdates.INSTANCE.config.itemOverlays.enableMonkeyCheck) {
			overlayStrings = null;
			return;
		}

		int updateTime = 60000;

		if (NotEnoughUpdates.INSTANCE.hasSkyblockScoreboard()) {
			ProfileApiSyncer.getInstance().requestResync("petinfo", updateTime, () -> {}, PetInfoOverlay::getAndSetPet);
		}

		Pet currentPet = getCurrentPet();
		if (currentPet == null) {
			overlayStrings = null;
		} else {
			lastUpdate = System.currentTimeMillis();
			levelXpLast = currentPet.petLevel.levelXp;
			updatePetLevels();
		}
	}

	public static Pet getPetFromStack(NBTTagCompound tag) {
		if (Constants.PETS == null || Constants.PETS.get("pet_levels") == null ||
			Constants.PETS.get("pet_levels") instanceof JsonNull) {
			Utils.showOutdatedRepoNotification();
			return null;
		}

		String petType = null;
		Rarity rarity = null;
		String heldItem = null;
		GuiProfileViewer.PetLevel level = null;
		String skin = null;

		if (tag != null && tag.hasKey("ExtraAttributes")) {
			NBTTagCompound ea = tag.getCompoundTag("ExtraAttributes");
			if (ea.hasKey("petInfo")) {
				JsonObject petInfo = new JsonParser().parse(ea.getString("petInfo")).getAsJsonObject();
				petType = petInfo.get("type").getAsString();
				rarity = Rarity.valueOf(petInfo.get("tier").getAsString());
				level = GuiProfileViewer.getPetLevel(
					petType,
					rarity.name(),
					Utils.getElementAsFloat(petInfo.get("exp"), 0) // Should only default if from item list and repo missing exp:0
				);
				if (petInfo.has("heldItem")) {
					heldItem = petInfo.get("heldItem").getAsString();
				}
				if (petInfo.has("skin")) {
					skin = "PET_SKIN_" + petInfo.get("skin").getAsString();
				}
			}
		}

		if (petType == null) {
			return null;
		}

		Pet pet = new Pet();
		pet.petItem = heldItem;
		pet.petLevel = level;
		pet.rarity = rarity;
		pet.petType = petType;
		JsonObject petTypes = Constants.PETS.get("pet_types").getAsJsonObject();
		pet.petXpType =
			petTypes.has(pet.petType) ? petTypes.get(pet.petType.toUpperCase()).getAsString().toLowerCase() : "unknown";
		pet.skin = skin;

		return pet;
	}

	private static final HashMap<Integer, Integer> removeMap = new HashMap<>();

	@SubscribeEvent
	public void onTick(TickEvent.ClientTickEvent event) {
		if (Minecraft.getMinecraft().currentScreen instanceof GuiChest && RenderListener.inventoryLoaded) {
			GuiChest chest = (GuiChest) Minecraft.getMinecraft().currentScreen;
			ContainerChest container = (ContainerChest) chest.inventorySlots;
			IInventory lower = container.getLowerChestInventory();
			String containerName = lower.getDisplayName().getUnformattedText();

			if (lower.getSizeInventory() >= 54) {
				int page = 0;
				int maxPage = 1;
				boolean isPets = false;

				if (containerName.equals("Pets")) {
					isPets = true;
				} else {
					Matcher matcher = PET_CONTAINER_PAGE.matcher(containerName);
					if (matcher.matches()) {
						try {
							page = Integer.parseInt(matcher.group(1)) - 1;
							maxPage = Integer.parseInt(matcher.group(2));
							isPets = true;
						} catch (NumberFormatException ignored) {
						}
					}
				}
				if (isPets) {
					boolean hasItem = false;
					for (int i = 0; i < lower.getSizeInventory(); i++) {
						if (lower.getStackInSlot(i) != null) {
							hasItem = true;
							break;
						}
					}
					if (!hasItem) return;

					Set<Integer> clear = new HashSet<>();
					for (int i : config.petMap.keySet()) {
						if (i >= maxPage * 28) {
							clear.add(i);
						}
					}
					config.petMap.keySet().removeAll(clear);

					Set<Integer> removeSet = new HashSet<>();
					long currentTime = System.currentTimeMillis();
					for (int index = 0; index < 28; index++) {
						int petIndex = page * 28 + index;
						int itemIndex = 10 + index + index / 7 * 2;

						ItemStack stack = lower.getStackInSlot(itemIndex);

						if (stack == null || !stack.hasTagCompound()) {
							if (index < 27) {
								int itemIndexNext = 10 + (index + 1) + (index + 1) / 7 * 2;
								ItemStack stackNext = lower.getStackInSlot(itemIndexNext);

								if (stackNext == null || !stackNext.hasTagCompound()) {
									int old = removeMap.getOrDefault(petIndex, 0);
									if (old >= 20) {
										config.petMap.remove(petIndex);
									} else {
										removeSet.add(petIndex);
										removeMap.put(petIndex, old + 1);
									}
								}
							}
						} else {
							String[] lore = NotEnoughUpdates.INSTANCE.manager.getLoreFromNBT(stack.getTagCompound());
							Pet pet = getPetFromStack(stack.getTagCompound());
							if (pet != null) {
								config.petMap.put(petIndex, pet);

								if (currentTime - lastPetSelect > 500) {
									boolean foundDespawn = false;
									for (String line : lore) {
										if (line.startsWith("\u00a77\u00a7cClick to despawn")) {
											config.selectedPet = petIndex;
											foundDespawn = true;
											break;
										}
										if (line.equals("\u00a77\u00a77Selected pet: \u00a7cNone")) {
											clearPet();
										}
									}
									if (!foundDespawn && config.selectedPet == petIndex && currentTime - lastPetSelect > 500) {
										clearPet();
									}
								}
							}
						}
					}
					removeMap.keySet().retainAll(removeSet);
				} else if (containerName.equals("Your Equipment")) {
					ItemStack petStack = lower.getStackInSlot(47);
					if (petStack != null && petStack.getItem() == Items.skull) {
						NBTTagCompound tag = petStack.getTagCompound();

						if (tag.hasKey("ExtraAttributes", 10)) {
							NBTTagCompound ea = tag.getCompoundTag("ExtraAttributes");
							if (ea.hasKey("petInfo")) {
								JsonParser jsonParser = new JsonParser();

								JsonObject petInfoObject = jsonParser.parse(ea.getString("petInfo")).getAsJsonObject();

								JsonObject jsonStack = NotEnoughUpdates.INSTANCE.manager.getJsonForItem(petStack);
								if (jsonStack == null || !jsonStack.has("lore") || !petInfoObject.has("exp")) {
									return;
								}

								int rarity = Utils.getRarityFromLore(jsonStack.get("lore").getAsJsonArray());
								String rarityString = Utils.getRarityFromInt(rarity);

								String name = StringUtils.cleanColour(petStack.getDisplayName());
								name = name.substring(name.indexOf(']') + 1).trim().replace(' ', '_').toUpperCase();

								float petXp = petInfoObject.get("exp").getAsFloat();

								double petLevel = GuiProfileViewer.getPetLevel(name, rarityString, petXp).level;
								int index = getClosestPetIndex(name, rarity, "", (float) petLevel);
								if (index != config.selectedPet) {
									clearPet();
									setCurrentPet(index);
								}
							}
						}
					}
				}
			}
		}
	}

	@Override
	protected Vector2f getSize(List<String> strings) {
		if (!NotEnoughUpdates.INSTANCE.config.petOverlay.petOverlayIcon) return super.getSize(strings);
		return super.getSize(strings).translate(25, 0);
	}

	@Override
	protected Vector2f getTextOffset() {
		if (!NotEnoughUpdates.INSTANCE.config.petOverlay.petOverlayIcon) return super.getTextOffset();
		if (this.styleSupplier.get() != TextOverlayStyle.BACKGROUND) return super.getTextOffset().translate(30, 0);
		return super.getTextOffset().translate(25, 0);
	}

	@Override
	public void renderDummy() {
		super.renderDummy();

		if (!NotEnoughUpdates.INSTANCE.config.petOverlay.petOverlayIcon) return;

		JsonObject petItem = NotEnoughUpdates.INSTANCE.manager.getItemInformation().get("ROCK;0");
		if (petItem != null) {
			Vector2f position = getPosition(overlayWidth, overlayHeight);
			int x = (int) position.x;
			int y = (int) position.y;

			ItemStack stack = NotEnoughUpdates.INSTANCE.manager.jsonToStack(petItem);
			GlStateManager.enableDepth();
			GlStateManager.pushMatrix();
			GlStateManager.translate(x - 2, y - 2, 0);
			GlStateManager.scale(2, 2, 1);
			Utils.drawItemStack(stack, 0, 0);
			GlStateManager.popMatrix();
		}
	}

	@Override
	public void render() {
		super.render();

		Pet currentPet = getCurrentPet();
		if (currentPet == null) {
			overlayStrings = null;
			return;
		}

		if (overlayStrings == null) {
			return;
		}

		if (!NotEnoughUpdates.INSTANCE.config.petOverlay.petOverlayIcon) return;
		int mythicRarity = currentPet.rarity.petId;
		if (currentPet.rarity.petId == 5) {
			mythicRarity = 4;
		}
		JsonObject petItem = NotEnoughUpdates.INSTANCE.manager.getItemInformation().get(
			currentPet.skin != null ? currentPet.skin : (currentPet.petType + ";" + mythicRarity));
		if (petItem != null) {
			Vector2f position = getPosition(overlayWidth, overlayHeight);
			int x = (int) position.x;
			int y = (int) position.y;

			ItemStack stack = NotEnoughUpdates.INSTANCE.manager.jsonToStack(petItem);
			GlStateManager.enableDepth();
			GlStateManager.pushMatrix();
			GlStateManager.translate(x - 2, y - 2, 0);
			GlStateManager.scale(2, 2, 1);
			Utils.drawItemStack(stack, 0, 0);
			GlStateManager.popMatrix();
		}

		Pet currentPet2 = getCurrentPet2();
		if (currentPet2 != null) {
			JsonObject petItem2 = NotEnoughUpdates.INSTANCE.manager.getItemInformation().get(
				currentPet2.skin != null ? currentPet2.skin : (currentPet2.petType + ";" + currentPet2.rarity.petId));
			if (petItem2 != null) {
				Vector2f position = getPosition(overlayWidth, overlayHeight);
				int x = (int) position.x;
				int y = (int) position.y + NotEnoughUpdates.INSTANCE.config.petOverlay.petOverlayText.size() * 10 + 10;

				ItemStack stack = NotEnoughUpdates.INSTANCE.manager.jsonToStack(petItem2);
				GlStateManager.enableDepth();
				GlStateManager.pushMatrix();
				GlStateManager.translate(x - 2, y - 2, 0);
				GlStateManager.scale(2, 2, 1);
				Utils.drawItemStack(stack, 0, 0);
				GlStateManager.popMatrix();
			}
		}
	}

	public static float getBoostMultiplier(String boostName) {
		if (boostName == null) return 1;
		boostName = boostName.toLowerCase();
		if (boostName.equalsIgnoreCase("PET_ITEM_ALL_SKILLS_BOOST_COMMON")) {
			return 1.1f;
		} else if (boostName.equalsIgnoreCase("ALL_SKILLS_SUPER_BOOST")) {
			return 1.2f;
		} else if (boostName.endsWith("epic")) {
			return 1.5f;
		} else if (boostName.endsWith("rare")) {
			return 1.4f;
		} else if (boostName.endsWith("uncommon")) {
			return 1.3f;
		} else if (boostName.endsWith("common")) {
			return 1.2f;
		} else {
			return 1;
		}
	}

	private static List<String> validXpTypes = Lists.newArrayList(
		"mining",
		"foraging",
		"enchanting",
		"farming",
		"combat",
		"fishing",
		"alchemy"
	);

	public static void onStackClick(ItemStack stack, int windowId, int slotId, int mouseButtonClicked, int mode) {
		if (mode != 0) return;
		if (mouseButtonClicked != 0 && mouseButtonClicked != 1) return;

		int slotIdMod = (slotId - 10) % 9;
		if (slotId >= 10 && slotId <= 43 && slotIdMod >= 0 && slotIdMod <= 6 &&
			Minecraft.getMinecraft().currentScreen instanceof GuiChest) {
			GuiChest chest = (GuiChest) Minecraft.getMinecraft().currentScreen;
			ContainerChest container = (ContainerChest) chest.inventorySlots;
			IInventory lower = container.getLowerChestInventory();
			String containerName = lower.getDisplayName().getUnformattedText();

			if (lower.getSizeInventory() >= 54 && windowId == container.windowId) {
				int page = 0;
				boolean isPets = false;

				if (containerName.equals("Pets")) {
					isPets = true;
				} else {
					Matcher matcher = PET_CONTAINER_PAGE.matcher(containerName);
					if (matcher.matches()) {
						try {
							page = Integer.parseInt(matcher.group(1)) - 1;
							isPets = true;
						} catch (NumberFormatException ignored) {
						}
					}
				}

				if (isPets) {
					ItemStack removingStack = lower.getStackInSlot(50);
					boolean isRemoving =
						removingStack != null && removingStack.getItem() == Items.dye && removingStack.getItemDamage() == 10;

					int newSelected = (slotId - 10) - (slotId - 10) / 9 * 2 + page * 28;

					lastPetSelect = System.currentTimeMillis();

					if (isRemoving) {
						if (newSelected == config.selectedPet) {
							clearPet();
						} else if (config.selectedPet > newSelected) {
							config.selectedPet--;
						}
					} else {
						setCurrentPet(newSelected);

						Pet pet = getPetFromStack(stack.getTagCompound());
						if (pet != null) {
							config.petMap.put(config.selectedPet, pet);
						}
					}
				}
			}
		}
	}

	public static float getXpGain(Pet pet, float xp, String xpType) {
		if (pet.petLevel.level >= pet.petLevel.maxLevel) return 0;

		if (validXpTypes == null)
			validXpTypes = Lists.newArrayList("mining", "foraging", "enchanting", "farming", "combat", "fishing", "alchemy");
		if (!validXpTypes.contains(xpType.toLowerCase())) return 0;

		float tamingPercent = 1.0f + (config.tamingLevel / 100f);
		xp = xp * tamingPercent;
		xp = xp + (xp * config.beastMultiplier / 100f);
		if (pet.petXpType != null && !pet.petXpType.equalsIgnoreCase(xpType)) {
			xp = xp / 3f;

			if (xpType.equalsIgnoreCase("alchemy") || xpType.equalsIgnoreCase("enchanting")) {
				xp = xp / 4f;
			}
		}
		if (xpType.equalsIgnoreCase("mining") || xpType.equalsIgnoreCase("fishing")) {
			xp = xp * 1.5f;
		}
		if (pet.petItem != null) {
			Matcher petItemMatcher = XP_BOOST_PATTERN.matcher(pet.petItem);
			if ((petItemMatcher.matches() && petItemMatcher.group(1).equalsIgnoreCase(xpType))
				|| pet.petItem.equalsIgnoreCase("ALL_SKILLS_SUPER_BOOST")) {
				xp = xp * getBoostMultiplier(pet.petItem);
			}
		}
		return xp;
	}

	private final HashMap<String, Float> skillInfoMapLast = new HashMap<>();

	public void updatePetLevels() {
		HashMap<String, XPInformation.SkillInfo> skillInfoMap = XPInformation.getInstance().getSkillInfoMap();

		float totalGain = 0;

		Pet currentPet = getCurrentPet();
		for (Map.Entry<String, XPInformation.SkillInfo> entry : skillInfoMap.entrySet()) {
			if (entry.getValue().level == 50 && entry.getValue().fromApi) continue;

			float skillXp = entry.getValue().totalXp;
			if (skillInfoMapLast.containsKey(entry.getKey())) {
				float skillXpLast = skillInfoMapLast.get(entry.getKey());

				if (skillXpLast <= 0) {
					skillInfoMapLast.put(entry.getKey(), skillXp);
				} else if (skillXp > skillXpLast) {

					float deltaXp = skillXp - skillXpLast;

					float gain = getXpGain(currentPet, deltaXp, entry.getKey().toUpperCase());
					totalGain += gain;

					skillInfoMapLast.put(entry.getKey(), skillXp);
				}
			} else {
				skillInfoMapLast.put(entry.getKey(), skillXp);
			}
		}

		xpGainHourLast = xpGainHour;
		if (xpAddTimer > 0 || totalGain > 0) {
			if (totalGain > 0) {
				xpAddTimer = 10;
			} else {
				xpAddTimer--;
			}

			currentPet.petLevel.totalXp += totalGain;

			xpGainQueue.add(0, totalGain);
			while (xpGainQueue.size() > 30) {
				xpGainQueue.removeLast();
			}

			if (xpGainQueue.size() > 1) {
				float tot = 0;
				float greatest = 0;
				for (float f : xpGainQueue) {
					tot += f;
					greatest = Math.max(greatest, f);
				}

				xpGainHour = (tot - greatest) * (60 * 60) / (xpGainQueue.size() - 1);
			}
		}

		JsonObject petsJson = Constants.PETS;
		if (currentPet != null && petsJson != null) {
			currentPet.petLevel = GuiProfileViewer.getPetLevel(
				currentPet.petType,
				currentPet.rarity.name(),
				currentPet.petLevel.totalXp
			);
		}
	}

	public String roundFloat(float f) {
		if (f % 1 < 0.05f) {
			return NumberFormat.getNumberInstance().format((int) f);
		} else {
			String s = Utils.floatToString(f, 1);
			if (s.contains(".")) {
				return NumberFormat.getNumberInstance().format((int) f) + '.' + s.split("\\.")[1];
			} else if (s.contains(",")) {
				return NumberFormat.getNumberInstance().format((int) f) + ',' + s.split(",")[1];
			} else {
				return s;
			}
		}
	}

	@SubscribeEvent
	public void switchWorld(WorldEvent.Load event) {
		if (NotEnoughUpdates.INSTANCE.hasSkyblockScoreboard()) {
			ProfileApiSyncer.getInstance().requestResync("petinfo_quick", 10000, () -> {}, PetInfoOverlay::getAndSetPet);
		}
	}

	private int lastLevelHovered = 0;

	private static final Pattern AUTOPET_EQUIP = Pattern.compile(
		"\u00a7cAutopet \u00a7eequipped your \u00a77\\[Lvl (\\d+)] \u00a7(.{2,})\u00a7e! \u00a7a\u00a7lVIEW RULE\u00a7r");

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onChatReceived(ClientChatReceivedEvent event) {
		NEUConfig config = NotEnoughUpdates.INSTANCE.config;
		if (NotEnoughUpdates.INSTANCE.hasSkyblockScoreboard() &&
			(config.petOverlay.enablePetInfo || config.itemOverlays.enableMonkeyCheck || config.petOverlay.petInvDisplay)) {
			if (event.type == 0) {
				String chatMessage = Utils.cleanColour(event.message.getUnformattedText());

				Matcher autopetMatcher = AUTOPET_EQUIP.matcher(event.message.getFormattedText());
				if (event.message.getUnformattedText().startsWith("You summoned your") ||
					System.currentTimeMillis() - EquipmentOverlay.cachedPetTimer < 500) {
					EquipmentOverlay.cachedPetTimer = System.currentTimeMillis();
					EquipmentOverlay.shouldUseCachedPet = false;
				} else if (autopetMatcher.matches()) {
					EquipmentOverlay.shouldUseCachedPet = false;
					try {
						lastLevelHovered = Integer.parseInt(autopetMatcher.group(1));
					} catch (NumberFormatException ignored) {
					}

					String petStringMatch = autopetMatcher.group(2);
					char colChar = petStringMatch.charAt(0);
					EnumChatFormatting col = EnumChatFormatting.RESET;
					for (EnumChatFormatting formatting : EnumChatFormatting.values()) {
						if (formatting.toString().equals("\u00a7" + colChar)) {
							col = formatting;
							break;
						}

					}
					Rarity rarity = Rarity.COMMON;
					if (col != EnumChatFormatting.RESET) {
						rarity = Rarity.getRarityFromColor(col);
					}

					String pet = Utils.cleanColour(petStringMatch.substring(1))
														.replaceAll("[^\\w ]", "").trim()
														.replace(" ", "_").toUpperCase();

					setCurrentPet(getClosestPetIndex(pet, rarity.petId, "", lastLevelHovered));
					if (PetInfoOverlay.config.selectedPet == -1) {
						Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(
							EnumChatFormatting.RED + "[NEU] Can't find pet \u00a7" + petStringMatch +
								EnumChatFormatting.RED + " try revisiting all pages of /pets."));
					}
				} else if ((chatMessage.toLowerCase().startsWith("you despawned your")) || (chatMessage.toLowerCase().contains(
					"switching to profile"))
					|| (chatMessage.toLowerCase().contains("transferring you to a new island..."))) {
					clearPet();
				}
			}
		}
	}
}
