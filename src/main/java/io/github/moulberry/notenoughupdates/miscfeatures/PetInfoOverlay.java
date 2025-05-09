/*
 * Copyright (C) 2022-2023 NotEnoughUpdates contributors
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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.core.config.ConfigUtil;
import io.github.moulberry.notenoughupdates.core.config.Position;
import io.github.moulberry.notenoughupdates.core.util.StringUtils;
import io.github.moulberry.notenoughupdates.core.util.lerp.LerpUtils;
import io.github.moulberry.notenoughupdates.events.SlotClickEvent;
import io.github.moulberry.notenoughupdates.listener.RenderListener;
import io.github.moulberry.notenoughupdates.miscfeatures.tablisttutorial.TablistAPI;
import io.github.moulberry.notenoughupdates.miscgui.itemcustomization.ItemCustomizeManager;
import io.github.moulberry.notenoughupdates.options.NEUConfig;
import io.github.moulberry.notenoughupdates.overlays.TextOverlay;
import io.github.moulberry.notenoughupdates.overlays.TextOverlayStyle;
import io.github.moulberry.notenoughupdates.util.Constants;
import io.github.moulberry.notenoughupdates.util.ItemResolutionQuery;
import io.github.moulberry.notenoughupdates.util.PetLeveling;
import io.github.moulberry.notenoughupdates.util.SBInfo;
import io.github.moulberry.notenoughupdates.util.SkyBlockTime;
import io.github.moulberry.notenoughupdates.util.Utils;
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
import net.minecraft.util.IChatComponent;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.commons.lang3.text.WordUtils;
import org.lwjgl.util.vector.Vector2f;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
	private static final Pattern PET_CONTAINER = Pattern.compile("^Pets(?::)?(?: \"\\w+\")?(?: \\((\\d)\\/(\\d)\\))?");
	private static final Pattern TAB_LIST_XP = Pattern.compile(
		"([0-9,]+\\.?[0-9]*)/([0-9,]+\\.?[0-9]*)[kM]? XP \\(\\d+\\.?\\d*%\\)");
	private static final Pattern TAB_LIST_XP_OVERFLOW = Pattern.compile("\\+([0-9,]+\\.?[0-9]*) XP");
	private static final Pattern TAB_LIST_PET_NAME = Pattern.compile("§.\\[Lvl (\\d+)\\](?: §8\\[§6\\d+§8§.✦§8])? §(.)(.+)");
	private static final Pattern TAB_LIST_PET_ITEM = Pattern.compile("§[fa956d4][a-zA-Z- ]+");

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
			for (Rarity rarity : Rarity.values()) {
				if (rarity.chatFormatting.equals(chatFormatting))
					return rarity;
			}
			return COMMON;
		}

		public PetInfoOverlay.Rarity nextRarity() {
			switch (this) {
				case COMMON:
					return PetInfoOverlay.Rarity.UNCOMMON;
				case UNCOMMON:
					return PetInfoOverlay.Rarity.RARE;
				case RARE:
					return PetInfoOverlay.Rarity.EPIC;
				case EPIC:
					return PetInfoOverlay.Rarity.LEGENDARY;
				case LEGENDARY:
					return PetInfoOverlay.Rarity.MYTHIC;
			}
			return null;
		}
	}

	private static final HashMap<Integer, Integer> removeMap = new HashMap<>();

	public static class PetConfig {
		public HashMap<Integer, Pet> petMap = new HashMap<>();

		private int selectedPet = -1;
		private int selectedPet2 = -1;

		public int tamingLevel = 1;
		public float beastMultiplier = 0;
	}

	private static long lastPetSelect = -1;
	private static PetConfig config = new PetConfig();

	public static PetConfig getConfig() {
		return config;
	}

	private static long lastUpdate = 0;
	private static float levelXpLast = 0;

	private static float xpGainHourLast = -1;
	private static float xpGainHour = -1;
	private static int pauseCountdown = 0;

	private static float xpGainHourSecondPet = -1;

	public static void loadConfig(File file) {
		config = ConfigUtil.loadConfig(PetConfig.class, file, GSON);
		if (config == null) {
			config = new PetConfig();
		}
	}

	public static void saveConfig(File file) {
		ConfigUtil.saveConfig(config, file, GSON);
	}

	public static void clearPet() {
		config.selectedPet = -1;
		config.selectedPet2 = -1;
	}

	public static void setCurrentPet(int index) {
		config.selectedPet2 = config.selectedPet;
		xpGainHourSecondPet = xpGainHour;
		xpGainHourLast = xpGainHour;
		xpHourMap.clear();
		xpGainHourLast = -1;
		xpGainHour = -1;
		config.selectedPet = index;
		lastPetCorrect = System.currentTimeMillis();
	}

	public static Pet getCurrentPet() {
		return config.petMap.get(config.selectedPet);
	}

	public static Pet getCurrentPet2() {
		if (!NotEnoughUpdates.INSTANCE.config.petOverlay.dualPets) return null;
		if (config.selectedPet == config.selectedPet2) return null;
		return config.petMap.get(config.selectedPet2);
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
			float distXp = Math.abs(pet.petLevel.getCurrentLevel() - petLevel);

			if (closestPet == null || distXp < closestXp) {
				closestXp = distXp;
				closestPet = pet;
			}
		}

		return closestPet;
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

	private float interp(float now, float last) {
		float interp = now;
		if (last >= 0 && last != now) {
			float factor = (System.currentTimeMillis() - lastUpdate) / 1000f;
			factor = LerpUtils.clampZeroOne(factor);
			interp = last + (now - last) * factor;
		}
		return interp;
	}

	public static Pet getPetFromStack(NBTTagCompound tag) {
		if (Constants.PETS == null || Constants.PETS.get("pet_levels") == null ||
			Constants.PETS.get("pet_levels") instanceof JsonNull) {
			Utils.showOutdatedRepoNotification("pets.json");
			return null;
		}

		String petType = null;
		Rarity rarity = null;
		String heldItem = null;
		PetLeveling.PetLevel level = null;
		String skin = null;
		int skinVariantSelected = -1;

		if (tag != null && tag.hasKey("ExtraAttributes")) {
			NBTTagCompound ea = tag.getCompoundTag("ExtraAttributes");
			if (ea.hasKey("petInfo")) {
				JsonObject petInfo = new JsonParser().parse(ea.getString("petInfo")).getAsJsonObject();
				petType = petInfo.get("type").getAsString();
				rarity = Rarity.valueOf(petInfo.get("tier").getAsString());
				if (petInfo.has("heldItem")) {
					heldItem = petInfo.get("heldItem").getAsString();
				}

				if ("PET_ITEM_TIER_BOOST".equals(heldItem)) {
					if (rarity != Rarity.MYTHIC) {
						rarity = rarity.nextRarity();
					}
				}

				// Should only default if from item list and repo missing exp: 0
				level = PetLeveling.getPetLevelingForPet(petType, rarity)
													 .getPetLevel(Utils.getElementAsFloat(petInfo.get("exp"), 0));
				if (petInfo.has("skin")) {
					skin = "PET_SKIN_" + petInfo.get("skin").getAsString();
				}

				if (petInfo.has("extraData")) {
					JsonObject animatedSkulls = Constants.ANIMATEDSKULLS;
					if (animatedSkulls != null && animatedSkulls.has("pet_skin_nbt_name")) {
						JsonObject extraData = petInfo.get("extraData").getAsJsonObject();
						JsonArray nbtNames = animatedSkulls.get("pet_skin_nbt_name").getAsJsonArray();

						for (JsonElement nbtName : nbtNames) {
							String nbt = nbtName.getAsString();
							if (petInfo.has(nbt)) {
								skinVariantSelected = extraData.get(nbt).getAsInt();
							}
						}
					}
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
			petTypes.has(pet.petType) ? petTypes
				.get(pet.petType.toUpperCase(Locale.ROOT))
				.getAsString()
				.toLowerCase(Locale.ROOT) : "unknown";
		pet.skin = skin;
		pet.skinVariantSelected = skinVariantSelected;

		return pet;
	}

	private int firstPetLines = 0;
	private int secondPetLines = 0;

	@Override
	public void updateFrequent() {
		Pet currentPet = getCurrentPet();
		if (!NotEnoughUpdates.INSTANCE.config.petOverlay.enablePetInfo || currentPet == null ||
			Objects.equals(SBInfo.getInstance().getLocation(), "rift")) {
			overlayStrings = null;
		} else {
			firstPetLines = 0;
			secondPetLines = 0;
			overlayStrings = new ArrayList<>();

			overlayStrings.addAll(createStringsForPet(currentPet, false));
			firstPetLines = overlayStrings.size();

			Pet currentPet2 = getCurrentPet2();
			if (currentPet2 != null) {
				overlayStrings.add("");
				if (firstPetLines == 1) {
					overlayStrings.add("");
				}
				overlayStrings.addAll(createStringsForPet(currentPet2, true));
				secondPetLines = overlayStrings.size() - firstPetLines - 1;
				if (firstPetLines == 1) {
					secondPetLines--;
				}
			}
		}
	}

	public float getLevelPercent(Pet pet) {
		if (pet == null) return 0;
		try {
			if (pet.petLevel.getMaxLevel() == pet.petLevel.getCurrentLevel()) {
				return 100;
			}
			return Float.parseFloat(StringUtils.formatToTenths(Math.min(
				pet.petLevel.getPercentageToNextLevel() * 100f,
				100f
			)));
		} catch (Exception ignored) {
			return 0;
		}
	}

	private List<String> createStringsForPet(Pet currentPet, boolean secondPet) {
		float levelXp = currentPet.petLevel.getExpInCurrentLevel();
		if (!secondPet) levelXp = interp(currentPet.petLevel.getExpInCurrentLevel(), levelXpLast);
		if (levelXp < 0) levelXp = 0;

		String petName =
			EnumChatFormatting.GREEN + "[Lvl " + currentPet.petLevel.getCurrentLevel() + "] " +
				currentPet.rarity.chatFormatting +
				getPetNameFromId(currentPet.petType, currentPet.petLevel.getCurrentLevel());
		if (currentPet.skin != null) {
			JsonObject skinJson = NotEnoughUpdates.INSTANCE.manager.getItemInformation().get(currentPet.skin);
			if (skinJson != null) {
				String displayName = NotEnoughUpdates.INSTANCE.manager.jsonToStack(skinJson).getDisplayName();
				String colourSt = Character.toString(Utils.getPrimaryColourCode(displayName));
				petName += "§" + colourSt + " ✦";
			}
		}

		float levelPercent = getLevelPercent(currentPet);
		String lvlStringShort = null;
		String lvlString = null;

		if (levelPercent != 100 || !NotEnoughUpdates.INSTANCE.config.petOverlay.hidePetLevelProgress) {
			long xpForNextLevel = currentPet.petLevel.getExpRequiredForNextLevel();
			float visualXp = levelXp;
			if (levelPercent == 100) {
				if (xpForNextLevel > levelXp) {
					visualXp = xpForNextLevel;
				}
			}
			lvlStringShort = EnumChatFormatting.AQUA + "" + roundFloat(visualXp) + "/" +
				roundFloat(xpForNextLevel)
				+ EnumChatFormatting.YELLOW + " (" + levelPercent + "%)";

			lvlString = EnumChatFormatting.AQUA + "" +
				Utils.shortNumberFormat(Math.min(visualXp, xpForNextLevel), 0) + "/" +
				Utils.shortNumberFormat(xpForNextLevel, 0)
				+ EnumChatFormatting.YELLOW + " (" + levelPercent + "%)";
		}

		float xpGain;
		if (!secondPet) {
			xpGain = interp(xpGainHour, xpGainHourLast);
		} else {
			xpGain = xpGainHourSecondPet;
		}
		if (xpGain < 0) xpGain = 0;
		String xpGainString = EnumChatFormatting.AQUA + "XP/h: " +
			EnumChatFormatting.YELLOW + roundFloat(xpGain);
		if (!secondPet && xpGain > 0 &&
			(levelXp != levelXpLast || System.currentTimeMillis() - lastXpUpdateNonZero > 4500)) {
			if (pauseCountdown <= 0) {
				xpGainString += EnumChatFormatting.RED + " (PAUSED)";
			} else {
				pauseCountdown--;
			}
		} else {
			pauseCountdown = 60;
		}

		String totalXpString =
			EnumChatFormatting.AQUA + "Total XP: " + EnumChatFormatting.YELLOW +
				roundFloat(currentPet.petLevel.getExpTotal());

		String petItemStr = EnumChatFormatting.AQUA + "Held Item: " + EnumChatFormatting.RED + "None";
		if (currentPet.petItem != null) {
			JsonObject json = NotEnoughUpdates.INSTANCE.manager.getItemInformation().get(currentPet.petItem);
			if (json != null) {
				String name;
				if (!NotEnoughUpdates.INSTANCE.config.petOverlay.petItemIcon)
					name = NotEnoughUpdates.INSTANCE.manager.jsonToStack(json).getDisplayName();
				else name = "";
				petItemStr = EnumChatFormatting.AQUA + "Held Item: " + name;
			}
		}

		String etaStr = null;
		String etaMaxStr = null;
		if (currentPet.petLevel.getCurrentLevel() < currentPet.petLevel.getMaxLevel()) {
			float remaining = currentPet.petLevel.getExpRequiredForNextLevel() - currentPet.petLevel.getExpInCurrentLevel();
			if (remaining > 0) {
				if (xpGain < 1000) {
					etaStr = EnumChatFormatting.AQUA + "Until L" + (currentPet.petLevel.getCurrentLevel() + 1) + ": " +
						EnumChatFormatting.YELLOW + "N/A";
				} else {
					etaStr = EnumChatFormatting.AQUA + "Until L" + (currentPet.petLevel.getCurrentLevel() + 1) + ": " +
						EnumChatFormatting.YELLOW + Utils.prettyTime((long) (remaining) * 1000 * 60 * 60 / (long) xpGain);
				}
			}

			if (currentPet.petLevel.getCurrentLevel() < (currentPet.petLevel.getMaxLevel() - 1) ||
				!NotEnoughUpdates.INSTANCE.config.petOverlay.petOverlayText.contains(6)) {
				float remainingMax = currentPet.petLevel.getExpRequiredForMaxLevel() - currentPet.petLevel.getExpTotal();
				if (remaining > 0) {
					if (xpGain < 1000) {
						etaMaxStr = EnumChatFormatting.AQUA + "Until L" + currentPet.petLevel.getMaxLevel() + ": " +
							EnumChatFormatting.YELLOW + "N/A";
					} else {
						etaMaxStr = EnumChatFormatting.AQUA + "Until L" + currentPet.petLevel.getMaxLevel() + ": " +
							EnumChatFormatting.YELLOW + Utils.prettyTime((long) (remainingMax) * 1000 * 60 * 60 / (long) xpGain);
					}
				}
			}
		}

		String finalEtaStr = etaStr;
		String finalEtaMaxStr = etaMaxStr;
		String finalXpGainString = xpGainString;
		String finalPetItemStr = petItemStr;
		String finalLvlString = lvlString;
		String finalLvlStringShort = lvlStringShort;
		String finalPetName = petName;
		return new ArrayList<String>() {{
			for (int index : NotEnoughUpdates.INSTANCE.config.petOverlay.petOverlayText) {
				switch (index) {
					case 0:
						add(finalPetName);
						break;
					case 1:
						if (finalLvlStringShort != null) add(finalLvlStringShort);
						break;
					case 2:
						if (finalLvlString != null) add(finalLvlString);
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
	public boolean isEnabled() {
		return NotEnoughUpdates.INSTANCE.config.petOverlay.enablePetInfo;
	}

	public void update() {
		if (!NotEnoughUpdates.INSTANCE.config.petOverlay.enablePetInfo) {
			overlayStrings = null;
			return;
		}

		Pet currentPet = getCurrentPet();
		if (currentPet == null) {
			overlayStrings = null;
		} else {
			lastUpdate = System.currentTimeMillis();
			levelXpLast = currentPet.petLevel.getExpInCurrentLevel();
			updatePetLevels();
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
			Vector2f position = getPosition(overlayWidth, overlayHeight, false);
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

		if (NotEnoughUpdates.INSTANCE.config.petOverlay.petOverlayIcon) {
			int mythicRarity = currentPet.rarity.petId;
			JsonObject petItem = NotEnoughUpdates.INSTANCE.manager.getItemInformation().get(
				currentPet.skin != null ? currentPet.skin : (currentPet.petType + ";" + mythicRarity));

			if (petItem == null && currentPet.rarity.petId == 5) {
				petItem = NotEnoughUpdates.INSTANCE.manager.getItemInformation().get(
					currentPet.skin != null ? currentPet.skin : (currentPet.petType + ";" + 4));
			}

			if (petItem != null) {
				Vector2f position = getPosition(overlayWidth, overlayHeight, true);
				int x = (int) position.x;
				int y = (int) position.y;

				ItemStack stack = NotEnoughUpdates.INSTANCE.manager.jsonToStack(petItem);
				getAnimatedSkin(stack, currentPet);
				GlStateManager.enableDepth();
				GlStateManager.pushMatrix();
				Utils.pushGuiScale(NotEnoughUpdates.INSTANCE.config.locationedit.guiScale);

				if (firstPetLines == 1) y -= 9;
				if (firstPetLines == 2) y -= 3;

				GlStateManager.translate(x - 2, y - 2, 0);
				GlStateManager.scale(2, 2, 1);
				Utils.drawItemStack(stack, 0, 0);
				Utils.pushGuiScale(0);
				GlStateManager.popMatrix();
			}

			Pet currentPet2 = getCurrentPet2();
			if (currentPet2 != null) {
				JsonObject petItem2 = NotEnoughUpdates.INSTANCE.manager.getItemInformation().get(
					currentPet2.skin != null ? currentPet2.skin : (currentPet2.petType + ";" + currentPet2.rarity.petId));
				if (petItem2 != null) {
					Vector2f position = getPosition(overlayWidth, overlayHeight, true);
					int x = (int) position.x;
					int y = (int) position.y + (overlayStrings.size() - secondPetLines) * 10;

					ItemStack stack = NotEnoughUpdates.INSTANCE.manager.jsonToStack(petItem2);
					getAnimatedSkin(stack, currentPet2);
					GlStateManager.enableDepth();
					GlStateManager.pushMatrix();
					Utils.pushGuiScale(NotEnoughUpdates.INSTANCE.config.locationedit.guiScale);

					if (secondPetLines == 1) y -= 9;
					if (secondPetLines == 2) y -= 3;

					GlStateManager.translate(x - 2, y - 2, 0);
					GlStateManager.scale(2, 2, 1);
					Utils.drawItemStack(stack, 0, 0);
					Utils.pushGuiScale(0);
					GlStateManager.popMatrix();
				}
			}
		}

		if (NotEnoughUpdates.INSTANCE.config.petOverlay.petItemIcon) {
			int backgroundOffset = (NotEnoughUpdates.INSTANCE.config.petOverlay.petInfoOverlayStyle == 0) ? 0 : 5;
			if (currentPet.petItem != null) {
				JsonObject petHeldItem = NotEnoughUpdates.INSTANCE.manager.getItemInformation().get(currentPet.petItem);

				if (petHeldItem != null) {
					Vector2f position = getPosition(overlayWidth, overlayHeight, true);
					int xOffset = NotEnoughUpdates.INSTANCE.config.petOverlay.petOverlayIcon ? 0 : 25;
					int x = (int) position.x - xOffset;
					int y = (int) position.y;

					ItemStack stack = NotEnoughUpdates.INSTANCE.manager.jsonToStack(petHeldItem);

					int counter = 0;
					for (String line : overlayStrings) {
						if (line.contains("Held Item:")) {
							break;
						}
						counter++;
					}
					if (counter >= overlayStrings.size()) {
						return;
					}

					GlStateManager.enableDepth();
					GlStateManager.pushMatrix();
					Utils.pushGuiScale(NotEnoughUpdates.INSTANCE.config.locationedit.guiScale);
					GlStateManager.translate(x + 77, y + (10 * counter) + 2 - backgroundOffset, 0);
					Utils.drawItemStack(stack, 0, 0);
					Utils.pushGuiScale(0);
					GlStateManager.popMatrix();
				}
			}

			Pet currentPet2 = getCurrentPet2();
			if (currentPet2 != null && currentPet2.petItem != null) {
				JsonObject petHeldItem = NotEnoughUpdates.INSTANCE.manager.getItemInformation().get(currentPet2.petItem);

				if (petHeldItem != null) {
					Vector2f position = getPosition(overlayWidth, overlayHeight, true);
					int x = (int) position.x;
					int y = (int) position.y + (overlayStrings.size() - secondPetLines) * 10;

					ItemStack stack = NotEnoughUpdates.INSTANCE.manager.jsonToStack(petHeldItem);

					int counter = 0;
					for (String line : overlayStrings) {
						if (line.contains("Held Item:")) {
							break;
						}
						counter++;
					}
					if (counter >= overlayStrings.size()) {
						return;
					}

					GlStateManager.enableDepth();
					GlStateManager.pushMatrix();
					Utils.pushGuiScale(NotEnoughUpdates.INSTANCE.config.locationedit.guiScale);
					GlStateManager.translate(x + 77, y + (10 * counter) + 2 - backgroundOffset, 0);
					Utils.drawItemStack(stack, 0, 0);
					Utils.pushGuiScale(0);
					GlStateManager.popMatrix();
				}
			}
		}
	}

	private void getAnimatedSkin(ItemStack stack, Pet currentPet) {
		NBTTagCompound tagCompound = stack.getTagCompound();
		if (tagCompound != null) {
			String skin = currentPet.skin;
			if (currentPet.skinVariantSelected >= 0) {
				JsonObject animatedSkulls = Constants.ANIMATEDSKULLS;
				if (animatedSkulls == null) return;
				if (!animatedSkulls.has("pet_skin_variant")) return;
				JsonElement pet_skin_variant = animatedSkulls.get("pet_skin_variant");
				if (!pet_skin_variant.getAsJsonObject().has(skin)) return;
				JsonArray skinsArray = pet_skin_variant.getAsJsonObject().get(skin).getAsJsonArray();
				if (skinsArray.size() <= currentPet.skinVariantSelected) return;
				skin = skinsArray.get(currentPet.skinVariantSelected).getAsString();
			}
			if ("PET_SKIN_FOUR_SEASONS_GRIFFIN".equals(skin)) {
				String monthName = SkyBlockTime.now().getMonthName();
				if (monthName.contains("Spring")) {
					skin = "PET_SKIN_FOUR_SEASONS_GRIFFIN_SPRING";
				} else if (monthName.contains("Summer")) {
					skin = "PET_SKIN_FOUR_SEASONS_GRIFFIN_SUMMER";
				} else if (monthName.contains("Autumn")) {
					skin = "PET_SKIN_FOUR_SEASONS_GRIFFIN_AUTUMN";
				} else if (monthName.contains("Winter")) {
					skin = "PET_SKIN_FOUR_SEASONS_GRIFFIN_WINTER";
				}

			}
			NBTTagCompound customSkull = ItemCustomizeManager.getAnimatedCustomSkull(skin, "");
			if (customSkull != null) {
				tagCompound.removeTag("SkullOwner");
				tagCompound.setTag("SkullOwner", customSkull);
			}
		}
	}

	@SubscribeEvent
	public void onStackClick(SlotClickEvent event) {
		// 0 through 8 are the mouse as well as the keyboard buttons, allow all of those
		if (event.clickedButton < 0 || event.clickedButton > 8) return;
		// Ignore RMB clicks, which convert the pet to an item
		if (event.clickedButton == 1 && event.clickType == 0) return;
		// Ignore shift clicks, which don't work
		if (event.clickType == 1) return;

		int slotIdMod = (event.slotId - 10) % 9;
		if (event.slotId >= 10 && event.slotId <= 43 && slotIdMod >= 0 && slotIdMod <= 6 &&
			Minecraft.getMinecraft().currentScreen instanceof GuiChest) {
			GuiChest chest = (GuiChest) Minecraft.getMinecraft().currentScreen;
			ContainerChest container = (ContainerChest) chest.inventorySlots;
			IInventory lower = container.getLowerChestInventory();
			String containerName = lower.getDisplayName().getUnformattedText();

			if (lower.getSizeInventory() >= 54 && event.guiContainer.inventorySlots.windowId == container.windowId) {
				int page = 0;
				boolean isPets = isPetMenu(containerName,container);

				if (isPets) {
					try {
						Matcher matcher = PET_CONTAINER.matcher(containerName);
						if (matcher.find()) {
							page = Integer.parseInt(matcher.group(1)) - 1;
						}
					} catch (NumberFormatException ignored) {
					}
					boolean isRemoving = event.clickedButton == 1;

					int newSelected = (event.slotId - 10) - (event.slotId - 10) / 9 * 2 + page * 28;

					lastPetSelect = System.currentTimeMillis();

					if (isRemoving) {
						if (newSelected == config.selectedPet) {
							clearPet();
						} else if (config.selectedPet > newSelected) {
							config.selectedPet--;
						}
					} else {
						setCurrentPet(newSelected);

						if (event.slot.getStack() != null && event.slot.getStack().getTagCompound() != null) {
							Pet pet = getPetFromStack(event.slot.getStack().getTagCompound());
							if (pet != null) {
								config.petMap.put(config.selectedPet, pet);
							}
						}
					}
				}
			}
		}
	}

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
				boolean isPets = isPetMenu(containerName,container);

				if (isPets) {
					try {
						Matcher matcher = PET_CONTAINER.matcher(containerName);
						if (matcher.find()) {
							page = Integer.parseInt(matcher.group(1)) - 1;
							maxPage = Integer.parseInt(matcher.group(2));
						}
					} catch (NumberFormatException ignored) {
					}
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
				} else if (containerName.startsWith("Your Equipment")) {
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

								String name = petInfoObject.get("type").getAsString();

								float petXp = petInfoObject.get("exp").getAsFloat();

								double petLevel = PetLeveling.getPetLevelingForPet(name, Rarity.valueOf(rarityString))
																						 .getPetLevel(petXp)
																						 .getCurrentLevel();

								String petItem = "";
								if (petInfoObject.has("heldItem")) {
									petItem = petInfoObject.get("heldItem").getAsString();
								}
								int index = getClosestPetIndex(name, rarity, petItem, (float) petLevel);
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

	private boolean isPetMenu(String containerName, ContainerChest container) {
		Matcher matcher = PET_CONTAINER.matcher(containerName);
		if (!matcher.find()) return false;

		List<String> backLore;
		try {
			backLore = container.getSlot(48).getStack().getTooltip(null, false);
		} catch (Exception _) {
			return false;
		}
		for (String line : backLore) {
			if (line.contains("To Select Process (Slot #")) {
				return false;
			}
		}
		return true;
	}

	private static HashMap<Long, Float> xpHourMap = new HashMap<>();
	private long lastXpUpdate = -1;
	private long lastXpUpdateNonZero = -1;
	private long lastPaused = -1;
	private static long lastPetCorrect = -1;

	public void updatePetLevels() {
		float totalGain = 0;

		Pet currentPet = getCurrentPet();

		JsonObject petsJson = Constants.PETS;
		if (petsJson == null) {
			Utils.showOutdatedRepoNotification("pets.json");
			return;
		}

		if ("rift".equals(SBInfo.getInstance().getLocation())) return;

		List<String> widgetLines = TablistAPI.getWidgetLines(TablistAPI.WidgetNames.PET);
		for (int i = 0; i < widgetLines.size(); i++) {
			String line = widgetLines.get(i);
			String lineWithColours = line.replace("§r", "").trim();
			line = Utils.cleanColour(line).replace(",", "").replace("✦", "").trim();
			Matcher normalXPMatcher = TAB_LIST_XP.matcher(line);
			Matcher overflowXPMatcher = TAB_LIST_XP_OVERFLOW.matcher(line);

			Matcher petNameMatcher = TAB_LIST_PET_NAME.matcher(lineWithColours);
			Matcher petItemMatcher = TAB_LIST_PET_ITEM.matcher(lineWithColours);

			if (petNameMatcher.matches()) {
				String petName = petNameMatcher.group(3);
				int petLevel = 1;
				try {
					petLevel = Integer.parseInt(petNameMatcher.group(1));
				} catch (NumberFormatException ignored) {
					Utils.addChatMessage(EnumChatFormatting.RED + "[NEU] Invalid number in tab list: " + petNameMatcher.group(1));
				}

				if (!getPetNameFromId(currentPet.petType, currentPet.petLevel.getCurrentLevel()).equalsIgnoreCase(petName)) {
					if (lastPetCorrect == -1 || lastPetCorrect > 0 && System.currentTimeMillis() - lastPetCorrect > 6000) {
						int rarity = getRarityByColor(petNameMatcher.group(2)).petId;
						String petItem = "";
						if (widgetLines.size() > i + 1) {
							String nextLine = widgetLines.get(i + 1).replace("§r", "").trim();
							Matcher nextLinePetItemMatcher = TAB_LIST_PET_ITEM.matcher(nextLine);
							if (nextLinePetItemMatcher.matches()) {
								petItem = getInternalIdForPetItemDisplayName(nextLinePetItemMatcher.group(0));
							}
						}

						String internalName = ItemResolutionQuery.findInternalNameByDisplayName(lineWithColours, true);
						if (internalName == null) continue;
						String[] split = internalName.split(";");
						if (split.length > 0) {
							internalName = split[0];
						}

						if ((currentPet.petItem != null && !petItem.isEmpty() && !currentPet.petItem.equals(petItem)) || currentPet.rarity.petId != rarity ||
							currentPet.petLevel.getCurrentLevel() != petLevel) {
							int closestPetIndex = getClosestPetIndex(internalName, rarity, petItem, petLevel);

							if (closestPetIndex != -1 && closestPetIndex != config.selectedPet) {
								//If it is -1 your petcache is probably outdated and you need to open /pets, but im sure they can work it out
								setCurrentPet(closestPetIndex);
							}
						}
						lastPetCorrect = System.currentTimeMillis();
					}
					break;
				} else {
					lastPetCorrect = System.currentTimeMillis();
				}

				PetLeveling.ExpLadder petLadder = PetLeveling.getPetLevelingForPet(currentPet.petType, currentPet.rarity);
				if (currentPet.petLevel.getCurrentLevel() != petLevel) {
					long baseLevelXp = petLadder.getPetExpForLevel(petLevel);
					currentPet.petLevel.setExpTotal(baseLevelXp);
					currentPet.petLevel = petLadder.getPetLevel(currentPet.petLevel.getExpTotal());
				}

			}

			if (petItemMatcher.matches()) {
				String petItem = getInternalIdForPetItemDisplayName(petItemMatcher.group(0));
				if (!Objects.equals(currentPet.petItem, petItem) && currentPet.petItem != null && !currentPet.petItem.isEmpty()) {
					int closestPetIndex = getClosestPetIndex(
						currentPet.petType,
						currentPet.rarity.petId,
						petItem,
						currentPet.petLevel.getCurrentLevel()
					);

					if (config.selectedPet != closestPetIndex && closestPetIndex != -1) {
						setCurrentPet(closestPetIndex);
					}
				}
			}

			if (normalXPMatcher.matches() || overflowXPMatcher.matches()) {
				String xpString;
				if (normalXPMatcher.matches()) xpString = normalXPMatcher.group(1);
				else xpString = overflowXPMatcher.group(1);
				xpString = xpString.replace(",", "");
				float xpNumber = 0;
				try {
					xpNumber = Float.parseFloat(xpString);
				} catch (NumberFormatException e) {
					Utils.addChatMessage(EnumChatFormatting.RED + "[NEU] Invalid number in tab list: " + xpString);
				}
				PetLeveling.ExpLadder petLadder = PetLeveling.getPetLevelingForPet(currentPet.petType, currentPet.rarity);

				long petExpForLevel = petLadder.getPetExpForLevel(currentPet.petLevel.getCurrentLevel());
				float expTotalBefore = currentPet.petLevel.getExpTotal();
				currentPet.petLevel.setExpTotal(xpNumber + petExpForLevel);
				float expTotalAfter = currentPet.petLevel.getExpTotal();
				totalGain = expTotalAfter - expTotalBefore;
				xpGainHourLast = xpGainHour;
				int seconds = 15;

				if (pauseCountdown > 0 || totalGain > 0) {
					long updateTime = 0;
					if (System.currentTimeMillis() - lastPaused < 1000 * (seconds + 1)) updateTime = lastPaused;

					Iterator<Map.Entry<Long, Float>> iterator = xpHourMap.entrySet().iterator();
					while (iterator.hasNext()) {
						Map.Entry<Long, Float> entry = iterator.next();
						long keyTime = entry.getKey();
						if (updateTime > 0) {
							keyTime = updateTime;
						}
						if (System.currentTimeMillis() - keyTime > seconds * 1000) {
							iterator.remove();
						}
					}

					if (totalGain != 0 || System.currentTimeMillis() - lastXpUpdate > 4500) {
						xpHourMap.put(System.currentTimeMillis(), totalGain);
						lastXpUpdate = System.currentTimeMillis();
					}
					if (totalGain > 0) {
						pauseCountdown = 60;
						lastXpUpdateNonZero = System.currentTimeMillis();
					}

					float averageXp = 0;
					for (float value : xpHourMap.values()) {
						averageXp += value;
					}

					if (!xpHourMap.isEmpty()) xpGainHour = (averageXp) * ((float) (60 * 60) / seconds);
					else xpGainHour = 0;
				} else {
					lastPaused = System.currentTimeMillis();
				}

				currentPet.petLevel = petLadder.getPetLevel(currentPet.petLevel.getExpTotal());
			}
		}
	}

	public static class Pet {
		public String petType;
		public Rarity rarity;
		public PetLeveling.PetLevel petLevel;
		public String petXpType;
		public String petItem;
		public String skin;
		public int skinVariantSelected;
		public int candyUsed;

		public String getPetId(boolean withoutBoost) {
			boolean shouldDecreaseRarity = withoutBoost && "PET_ITEM_TIER_BOOST".equals(petItem);
			return petType + ";" + (shouldDecreaseRarity ? rarity.petId - 1 : rarity.petId);
		}

	}

	public String roundFloat(float f) {
		if (f % 1 < 0.05f) {
			return StringUtils.formatNumber((int) f);
		} else {
			String s = Utils.floatToString(f, 1);
			if (s.contains(".")) {
				return StringUtils.formatNumber((int) f) + '.' + s.split("\\.")[1];
			} else if (s.contains(",")) {
				return StringUtils.formatNumber((int) f) + ',' + s.split(",")[1];
			} else {
				return s;
			}
		}
	}

	private int lastLevelHovered = 0;

	private static final Pattern AUTOPET_EQUIP = Pattern.compile(
		"§cAutopet §eequipped your §7\\[Lvl (?<level>\\d+)](?: §8\\[§6\\d+§8§.✦§8])? §(?<rarityColor>.)(?<name>.*)§e! §a§lVIEW RULE§r");

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onChatReceived(ClientChatReceivedEvent event) {
		NEUConfig config = NotEnoughUpdates.INSTANCE.config;
		if (config.petOverlay.enablePetInfo || config.itemOverlays.enableMonkeyCheck || config.petOverlay.petInvDisplay) {
			if (event.type == 0) {
				String chatMessage = Utils.cleanColour(event.message.getUnformattedText());

				Matcher autopetMatcher = AUTOPET_EQUIP.matcher(event.message.getFormattedText());
				if (autopetMatcher.matches()) {
					try {
						lastLevelHovered = Integer.parseInt(autopetMatcher.group("level"));
					} catch (NumberFormatException ignored) {
					}

					String petName = autopetMatcher.group("name");
					Rarity rarity = getRarityByColor(autopetMatcher.group("rarityColor"));

					String pet = Utils.cleanColour(petName)
														.replaceAll("[^\\w ]", "").trim()
														.replace(" ", "_").toUpperCase(Locale.ROOT);
					List<IChatComponent> siblings = event.message.getChatStyle().getChatHoverEvent().getValue().getSiblings();
					String petItem = "";
					if (siblings.size() > 6) {
						int i = -1;
						for (IChatComponent sibling : siblings) {
							i++;
							if (!sibling.getUnformattedText().startsWith("Held Item:")) continue;
							IChatComponent iChatComponent = siblings.get(i+1);
							String formattedText = iChatComponent.getChatStyle().getColor() + iChatComponent.getUnformattedText();
							petItem = getInternalIdForPetItemDisplayName(formattedText);
						}
					} else {
						//this *should* make it only match with only pets without items
						petItem = null;
					}
					setCurrentPet(getClosestPetIndex(pet, rarity.petId, petItem, lastLevelHovered));
					if (PetInfoOverlay.config.selectedPet == -1) {
							setCurrentPet(getClosestPetIndex(pet, rarity.petId - 1, petItem, lastLevelHovered));
						if (getCurrentPet() != null && !"PET_ITEM_TIER_BOOST".equals(getCurrentPet().petItem)) {
							PetInfoOverlay.config.selectedPet = -1;
							Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(
								EnumChatFormatting.RED + "[NEU] Can't find pet \u00a7" + petName +
									EnumChatFormatting.RED + " try revisiting all pages of /pets."));
						}
					}
				} else if ((chatMessage.toLowerCase(Locale.ROOT).startsWith("you despawned your")) || (chatMessage.toLowerCase(
					Locale.ROOT).contains(
					"switching to profile"))
					|| (chatMessage.toLowerCase(Locale.ROOT).contains("transferring you to a new island..."))) {
					clearPet();
				}
			}
		}
	}

	private static Rarity getRarityByColor(String colChar) {
		EnumChatFormatting col = EnumChatFormatting.RESET;
		for (EnumChatFormatting formatting : EnumChatFormatting.values()) {
			if (formatting.toString().equals("§" + colChar)) {
				col = formatting;
				break;
			}

		}
		Rarity rarity = Rarity.COMMON;
		if (col != EnumChatFormatting.RESET) {
			rarity = Rarity.getRarityFromColor(col);
		}
		return rarity;
	}

	static boolean shownMissingRepo = false;

	private static String getPetNameFromId(String petId, int petLevel) {
		JsonObject pets = Constants.PETS;
		String defaultName = WordUtils.capitalizeFully(petId.replace("_", " "));
		if (pets == null) return defaultName;
		if (!pets.has("id_to_display_name")) {
			if (!shownMissingRepo) {
				Utils.showOutdatedRepoNotification("pets.json id_to_display_name");
				shownMissingRepo = true;
			}
			return defaultName;
		}

		if ("GOLDEN_DRAGON".equals(petId)) {
			if (petLevel < 100) return "Golden Dragon Egg";
			return defaultName;
		}

		JsonObject idToDisplayName = pets.get("id_to_display_name").getAsJsonObject();
		if (idToDisplayName.has(petId)) {
			return idToDisplayName.get(petId).getAsString();
		}
		return defaultName;
	}

	private static boolean hasRepoPopupped = false;

	private static String getInternalIdForPetItemDisplayName(String displayName) {
		JsonObject pets = Constants.PETS;
		String defaultName = displayName.replace(" ", "_").replace("-", "_").toUpperCase(Locale.ROOT);
		defaultName = Utils.cleanColour(defaultName).trim();
		if (pets == null) return defaultName;
		if (!pets.has("pet_item_display_name_to_id")) {
			if (!hasRepoPopupped) Utils.showOutdatedRepoNotification("pets.json pet_item_display_name_to_id");
			hasRepoPopupped = true;
			return defaultName;
		}

		JsonObject petItemDisplayNameToId = pets.get("pet_item_display_name_to_id").getAsJsonObject();
		if (petItemDisplayNameToId.has(displayName)) {
			return petItemDisplayNameToId.get(displayName).getAsString();
		}

		return defaultName;
	}
}
