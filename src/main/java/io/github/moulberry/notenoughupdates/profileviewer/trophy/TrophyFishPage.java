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

package io.github.moulberry.notenoughupdates.profileviewer.trophy;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.profileviewer.GuiProfileViewer;
import io.github.moulberry.notenoughupdates.profileviewer.GuiProfileViewerPage;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TrophyFishPage extends GuiProfileViewerPage {

	public static final ResourceLocation pv_elements = new ResourceLocation("notenoughupdates:pv_elements.png");
	private static final Map<String, EnumChatFormatting> internalTrophyFish = new HashMap<String, EnumChatFormatting>() {
		{
			put("gusher", EnumChatFormatting.WHITE);
			put("flyfish", EnumChatFormatting.GREEN);
			put("moldfin", EnumChatFormatting.DARK_PURPLE);
			put("vanille", EnumChatFormatting.BLUE);
			put("blobfish", EnumChatFormatting.WHITE);
			put("mana_ray", EnumChatFormatting.BLUE);
			put("slugfish", EnumChatFormatting.GREEN);
			put("soul_fish", EnumChatFormatting.DARK_PURPLE);
			put("lava_horse", EnumChatFormatting.BLUE);
			put("golden_fish", EnumChatFormatting.GOLD);
			put("karate_fish", EnumChatFormatting.DARK_PURPLE);
			put("skeleton_fish", EnumChatFormatting.DARK_PURPLE);
			put("sulphur_skitter", EnumChatFormatting.WHITE);
			put("obfuscated_fish_1", EnumChatFormatting.WHITE);
			put("obfuscated_fish_2", EnumChatFormatting.GREEN);
			put("obfuscated_fish_3", EnumChatFormatting.BLUE);
			put("volcanic_stonefish", EnumChatFormatting.BLUE);
			put("steaming_hot_flounder", EnumChatFormatting.WHITE);
		}
	};
	private static final LinkedHashMap<ItemStack, Pair<String, Integer>> armorHelmets = new LinkedHashMap<ItemStack, Pair<String, Integer>>() {
		{
			put(
				NotEnoughUpdates.INSTANCE.manager.jsonToStack(
					NotEnoughUpdates.INSTANCE.manager.getItemInformation().get("BRONZE_HUNTER_HELMET")
				),
				Pair.of(EnumChatFormatting.GREEN + "Novice Fisher", 1)
			);
			put(
				NotEnoughUpdates.INSTANCE.manager.jsonToStack(
					NotEnoughUpdates.INSTANCE.manager.getItemInformation().get("SILVER_HUNTER_HELMET")
				),
				Pair.of(EnumChatFormatting.BLUE + "Adept Fisher", 2)
			);
			put(
				NotEnoughUpdates.INSTANCE.manager.jsonToStack(
					NotEnoughUpdates.INSTANCE.manager.getItemInformation().get("GOLD_HUNTER_HELMET")
				),
				Pair.of(EnumChatFormatting.DARK_PURPLE + "Expert Fisher", 3)
			);
			put(
				NotEnoughUpdates.INSTANCE.manager.jsonToStack(
					NotEnoughUpdates.INSTANCE.manager.getItemInformation().get("DIAMOND_HUNTER_HELMET")
				),
				Pair.of(EnumChatFormatting.GOLD + "Master Fisher", 4)
			);
		}
	};
	private static final Map<Integer, Pair<Integer, Integer>> slotLocations = new HashMap<Integer, Pair<Integer, Integer>>() {
		{
			put(0, Pair.of(277, 46));
			put(1, Pair.of(253, 58));
			put(2, Pair.of(301, 58));
			put(3, Pair.of(229, 70));
			put(4, Pair.of(325, 70));
			put(5, Pair.of(277, 70));
			put(6, Pair.of(253, 82));
			put(7, Pair.of(301, 82));
			put(8, Pair.of(229, 94));
			put(9, Pair.of(325, 94));
			put(10, Pair.of(253, 106));
			put(11, Pair.of(301, 106));
			put(12, Pair.of(277, 118));
			put(13, Pair.of(229, 118));
			put(14, Pair.of(325, 118));
			put(15, Pair.of(253, 130));
			put(16, Pair.of(301, 130));
			put(17, Pair.of(277, 142));
		}
	};
	private static final ResourceLocation TROPHY_FISH_TEXTURE = new ResourceLocation("notenoughupdates:pv_trophy_fish_tab.png");
	private static final NumberFormat NUMBER_FORMAT = NumberFormat.getInstance(Locale.US);
	private static final String checkX = "§c✖";
	private static final String check = "§a✔";
	private final Map<String, Integer> total = new HashMap<>();
	private final Map<String, TrophyFish> trophyFishList = new HashMap<>();
	private long totalCount = 0;
	private static List<String> tooltipToDisplay = null;

	public TrophyFishPage(GuiProfileViewer instance) {
		super(instance);
	}

	@Override
	public void drawPage(int mouseX, int mouseY, float partialTicks) {
		int guiLeft = GuiProfileViewer.getGuiLeft();
		int guiTop = GuiProfileViewer.getGuiTop();

		trophyFishList.clear();

		JsonObject profileInformation = GuiProfileViewer.getProfile().getProfileInformation(GuiProfileViewer.getProfileId());
		if (profileInformation == null || !profileInformation.has("trophy_fish")) {
			Utils.drawStringCentered(
				EnumChatFormatting.RED + "No data found",
				Minecraft.getMinecraft().fontRendererObj,
				guiLeft + 431 / 2f,
				guiTop + 101,
				true,
				0
			);
			return;
		}
		JsonObject trophyObject = profileInformation.get("trophy_fish").getAsJsonObject();

		loadTrophyInformation(trophyObject);

		ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
		int width = scaledResolution.getScaledWidth();
		int height = scaledResolution.getScaledHeight();

		Minecraft.getMinecraft().getTextureManager().bindTexture(TROPHY_FISH_TEXTURE);
		Utils.drawTexturedRect(guiLeft, guiTop, 431, 202, GL11.GL_NEAREST);

		GlStateManager.color(1, 1, 1, 1);
		GlStateManager.disableLighting();
		RenderHelper.enableGUIStandardItemLighting();

		JsonObject stats = profileInformation.get("stats").getAsJsonObject();

		int thunderKills = 0;
		if (stats.has("kills_thunder")) {
			thunderKills = stats.getAsJsonObject().get("kills_thunder").getAsInt();
		}
		ItemStack thunder_sc = NotEnoughUpdates.INSTANCE.manager.jsonToStack(
			NotEnoughUpdates.INSTANCE.manager.getItemInformation().get("THUNDER_SC")
		);
		Minecraft.getMinecraft().getRenderItem().renderItemIntoGUI(thunder_sc, guiLeft + 16, guiTop + 108);

		Utils.drawStringF(
			EnumChatFormatting.AQUA + "Thunder Kills: §f" + thunderKills,
			Minecraft.getMinecraft().fontRendererObj,
			guiLeft + 36,
			guiTop + 112,
			true,
			0
		);

		ItemStack lord_jawbus_sc = NotEnoughUpdates.INSTANCE.manager.jsonToStack(
			NotEnoughUpdates.INSTANCE.manager.getItemInformation().get("LORD_JAWBUS_SC")
		);
		Minecraft.getMinecraft().getRenderItem().renderItemIntoGUI(lord_jawbus_sc, guiLeft + 16, guiTop + 120);
		int jawbusKills = 0;
		if (stats.has("kills_lord_jawbus")) {
			jawbusKills = stats.getAsJsonObject().get("kills_lord_jawbus").getAsInt();
		}

		Utils.drawStringF(
			EnumChatFormatting.AQUA + "Lord Jawbus Kills: §f" + jawbusKills,
			Minecraft.getMinecraft().fontRendererObj,
			guiLeft + 36,
			guiTop + 124,
			true,
			0
		);

		ItemStack fishing_rod = NotEnoughUpdates.INSTANCE.manager.jsonToStack(
			NotEnoughUpdates.INSTANCE.manager.getItemInformation().get("FISHING_ROD")
		);
		Minecraft.getMinecraft().getRenderItem().renderItemIntoGUI(fishing_rod, guiLeft + 20, guiTop + 21);

		Utils.drawStringF(
			EnumChatFormatting.AQUA + "Total Caught: §f" + totalCount,
			Minecraft.getMinecraft().fontRendererObj,
			guiLeft + 38,
			guiTop + 25,
			true,
			0
		);

		ArrayList<TrophyFish> arrayList = new ArrayList<>(trophyFishList.values());
		arrayList.sort((c1, c2) -> Integer.compare(c2.getTotal(), c1.getTotal()));

		int x;
		int y;
		for (TrophyFish value : arrayList) {
			x = guiLeft + slotLocations.get(arrayList.indexOf(value)).getLeft();
			y = guiTop + slotLocations.get(arrayList.indexOf(value)).getRight();
			RenderHelper.enableGUIStandardItemLighting();
			Minecraft.getMinecraft().getTextureManager().bindTexture(pv_elements);
			Map<TrophyFish.TrophyFishRarity, Integer> trophyFishRarityIntegerMap = value.getTrophyFishRarityIntegerMap();
			if (trophyFishRarityIntegerMap.containsKey(TrophyFish.TrophyFishRarity.BRONZE)) {
				GlStateManager.color(255 / 255f, 130 / 255f, 0 / 255f, 1);
			}
			if (trophyFishRarityIntegerMap.containsKey(TrophyFish.TrophyFishRarity.SILVER)) {
				GlStateManager.color(192 / 255f, 192 / 255f, 192 / 255f, 1);
			}
			if (trophyFishRarityIntegerMap.containsKey(TrophyFish.TrophyFishRarity.GOLD)) {
				GlStateManager.color(1, 0.82F, 0, 1);
			}
			if (trophyFishRarityIntegerMap.containsKey(TrophyFish.TrophyFishRarity.DIAMOND)) {
				GlStateManager.color(31 / 255f, 216 / 255f, 241 / 255f, 1);
			}
			Utils.drawTexturedRect(x - 2, y - 2, 20, 20, 0, 20 / 256f, 0, 20 / 256f, GL11.GL_NEAREST);
			GlStateManager.color(1, 1, 1, 1);
			Minecraft.getMinecraft().getRenderItem().renderItemIntoGUI(getItem(value.getName()), x, y);

			if (mouseX >= x && mouseX < x + 24) {
				if (mouseY >= y && mouseY <= y + 24) {
					tooltipToDisplay = new ArrayList<>();
					tooltipToDisplay.addAll(getTooltip(value.getName(), value.getTrophyFishRarityIntegerMap()));
				}
			}
		}

		if (arrayList.size() != internalTrophyFish.size()) {
			List<String> clonedList = new ArrayList<>(internalTrophyFish.size());
			clonedList.addAll(internalTrophyFish.keySet());
			clonedList.removeAll(fixStringName(new ArrayList<>(trophyFishList.keySet())));
			for (String difference : clonedList) {
				RenderHelper.enableGUIStandardItemLighting();
				x = guiLeft + slotLocations.get(clonedList.indexOf(difference) + (trophyFishList.keySet().size())).getLeft();
				y = guiTop + slotLocations.get(clonedList.indexOf(difference) + (trophyFishList.keySet().size())).getRight();
				ItemStack itemStack = new ItemStack(Items.dye, 1, 8);
				Minecraft.getMinecraft().getRenderItem().renderItemIntoGUI(itemStack, x, y);
				if (mouseX >= x && mouseX < x + 24) {
					if (mouseY >= y && mouseY <= y + 24) {
						tooltipToDisplay = new ArrayList<>();
						tooltipToDisplay.addAll(getTooltip(difference, null));
						GlStateManager.color(1, 1, 1, 1);
					}
				}
				Minecraft.getMinecraft().getTextureManager().bindTexture(pv_elements);
				Utils.drawTexturedRect(x - 2, y - 2, 20, 20, 0, 20 / 256f, 0, 20 / 256f, GL11.GL_NEAREST);
			}
		}

		if (!trophyObject.has("rewards")) return;

		int[] trophiesPerTier = getTrophiesPerTier(trophyObject);
		JsonArray rewards = trophyObject.get("rewards").getAsJsonArray();
		int i = 0;
		for (ItemStack itemStack : armorHelmets.keySet()) {
			RenderHelper.enableGUIStandardItemLighting();
			int integer = armorHelmets.get(itemStack).getRight();
			x = guiLeft + 15;
			y = guiTop + 50 + i;

			Minecraft.getMinecraft().getRenderItem().renderItemIntoGUI(itemStack, x, y);
			Utils.drawStringF(armorHelmets.get(itemStack).getLeft(), Minecraft.getMinecraft().fontRendererObj, x + 20, y + 4, true, 0);

			int hasValue = trophiesPerTier[integer - 1];
			int neededValue = integer == 1 ? 15 : 18;
			String neededText = "§c" + hasValue + "/" + neededValue;

			try {
				JsonElement jsonElement = rewards.get(integer - 1);
				if (!jsonElement.isJsonNull()) {
					Utils.drawStringF(check, Minecraft.getMinecraft().fontRendererObj, x + 100, y + 2, true, 0);
				} else {
					Utils.drawStringF(neededText, Minecraft.getMinecraft().fontRendererObj, x + 100, y + 4, true, 0);
				}
			} catch (IndexOutOfBoundsException exception) {
				Utils.drawStringF(neededText, Minecraft.getMinecraft().fontRendererObj, x + 100, y + 4, true, 0);
			}
			i += 10;
		}

		if (tooltipToDisplay != null) {
			Utils.drawHoveringText(
				tooltipToDisplay,
				mouseX,
				mouseY,
				getInstance().width,
				getInstance().height,
				-1,
				Minecraft.getMinecraft().fontRendererObj
			);
			tooltipToDisplay = null;
		}

		GlStateManager.enableLighting();
	}

	private int[] getTrophiesPerTier(JsonObject trophyFish) {
		int[] trophiesPerTier = new int[] { 0, 0, 0, 0 };
		for (String fishType : internalTrophyFish.keySet()) {
			int highestTier = 0;
			if (trophyFish.has((fishType + "_bronze"))) highestTier = 1;
			if (trophyFish.has((fishType + "_silver"))) highestTier = 2;
			if (trophyFish.has((fishType + "_gold"))) highestTier = 3;
			if (trophyFish.has((fishType + "_diamond"))) highestTier = 4;

			if (highestTier >= 1) trophiesPerTier[0]++;
			if (highestTier >= 2) trophiesPerTier[1]++;
			if (highestTier >= 3) trophiesPerTier[2]++;
			if (highestTier >= 4) trophiesPerTier[3]++;
		}
		return trophiesPerTier;
	}

	private List<String> getTooltip(String name, Map<TrophyFish.TrophyFishRarity, Integer> trophyFishRarityIntegerMap) {
		List<String> tooltip = new ArrayList<>();
		tooltip.add(internalTrophyFish.get(name.toLowerCase(Locale.US).replace(" ", "_")) + WordUtils.capitalize(name.replace("_", " ")));

		List<String> lore = readLoreFromRepo(name.toUpperCase(Locale.US));
		List<String> description = readDescriptionFromLore(lore);
		tooltip.addAll(description);
		tooltip.add(" ");

		if (trophyFishRarityIntegerMap == null) {
			tooltip.add(EnumChatFormatting.RED + checkX + " Not Discovered");
			tooltip.add(" ");
		}

		tooltip.add(display(trophyFishRarityIntegerMap, TrophyFish.TrophyFishRarity.DIAMOND, EnumChatFormatting.AQUA));
		tooltip.add(display(trophyFishRarityIntegerMap, TrophyFish.TrophyFishRarity.GOLD, EnumChatFormatting.GOLD));
		tooltip.add(display(trophyFishRarityIntegerMap, TrophyFish.TrophyFishRarity.SILVER, EnumChatFormatting.GRAY));
		tooltip.add(display(trophyFishRarityIntegerMap, TrophyFish.TrophyFishRarity.BRONZE, EnumChatFormatting.DARK_GRAY));
		if (trophyFishList.get(name) != null) {
			tooltip.add(" ");
			tooltip.add(EnumChatFormatting.GRAY + "Total: " + EnumChatFormatting.GOLD +
				NUMBER_FORMAT.format(trophyFishList.get(name).getTotal()));
		}
		return tooltip;
	}

	private String display(
		Map<TrophyFish.TrophyFishRarity, Integer> trophyFishRarityIntegerMap,
		TrophyFish.TrophyFishRarity rarity,
		EnumChatFormatting color
	) {
		String name = WordUtils.capitalize(rarity.name().toLowerCase(Locale.US));
		if (trophyFishRarityIntegerMap == null) {
			return color + name + ": " + checkX;
		}

		if (trophyFishRarityIntegerMap.containsKey(rarity)) {
			return color + name + ": " + EnumChatFormatting.GOLD + NUMBER_FORMAT.format(trophyFishRarityIntegerMap.get(rarity));
		} else {
			return color + name + ": " + checkX;
		}
	}

	private ItemStack getItem(String name) {
		String repoName = name.toUpperCase(Locale.US).replace(" ", "_") + "_BRONZE";
		JsonObject jsonItem = NotEnoughUpdates.INSTANCE.manager.getItemInformation().get(repoName);
		return NotEnoughUpdates.INSTANCE.manager.jsonToStack(jsonItem);
	}

	private void loadTrophyInformation(JsonObject trophyObject) {
		Map<String, List<Pair<TrophyFish.TrophyFishRarity, Integer>>> trophyFishRarityIntegerMap = new HashMap<>();
		totalCount = 0;
		for (Map.Entry<String, JsonElement> stringJsonElementEntry : trophyObject.entrySet()) {
			String key = stringJsonElementEntry.getKey();
			if (key.equalsIgnoreCase("rewards") || key.equalsIgnoreCase("total_caught")) {
				if (key.equalsIgnoreCase("total_caught")) {
					totalCount = stringJsonElementEntry.getValue().getAsInt();
				}
				continue;
			}

			String[] s = key.split("_");
			String type = s[s.length - 1];
			TrophyFish.TrophyFishRarity trophyFishRarity;
			int value = stringJsonElementEntry.getValue().getAsInt();

			if (key.startsWith("golden_fish_")) {
				type = s[2];
			}
			try {
				trophyFishRarity = TrophyFish.TrophyFishRarity.valueOf(type.toUpperCase(Locale.US));
			} catch (IllegalArgumentException ignored) {
				total.put(WordUtils.capitalize(key), value);
				continue;
			}

			String replace = key.replace("_" + type, "");
			String name = WordUtils.capitalize(replace);
			List<Pair<TrophyFish.TrophyFishRarity, Integer>> pairs;

			if (trophyFishRarityIntegerMap.containsKey(name)) {
				pairs = trophyFishRarityIntegerMap.get(name);
			} else {
				pairs = new ArrayList<>();
			}
			pairs.add(Pair.of(trophyFishRarity, value));
			trophyFishRarityIntegerMap.put(name, pairs);
		}

		trophyFishRarityIntegerMap.forEach((name, pair) -> {
			if (!trophyFishList.containsKey(name)) {
				TrophyFish trophyFish = new TrophyFish(name, new HashMap<>());
				trophyFish.addTotal(total.get(name));
				for (Pair<TrophyFish.TrophyFishRarity, Integer> pair1 : pair) {
					trophyFish.add(pair1.getKey(), pair1.getValue());
				}
				trophyFishList.put(name, trophyFish);
			} else {
				TrophyFish trophyFish = trophyFishList.get(name);
				for (Pair<TrophyFish.TrophyFishRarity, Integer> pair1 : pair) {
					trophyFish.add(pair1.getKey(), pair1.getValue());
				}
			}
		});
	}

	private List<String> fixStringName(List<String> list) {
		List<String> fixedList = new ArrayList<>();
		for (String s : list) {
			fixedList.add(s.toLowerCase(Locale.US).replace(" ", "_"));
		}
		return fixedList;
	}

	private List<String> readDescriptionFromLore(List<String> lore) {
		List<String> description = new ArrayList<>();
		boolean found = false;

		for (String line : lore) {
			if (!found && line.startsWith("§7")) found = true;
			if (found && line.isEmpty()) break;

			if (found) {
				description.add(line);
			}
		}

		return description;
	}

	private List<String> readLoreFromRepo(String name) {
		String repoName = name.toUpperCase(Locale.US).replace(" ", "_") + "_BRONZE";
		JsonObject jsonItem = NotEnoughUpdates.INSTANCE.manager.getItemInformation().get(repoName);

		List<String> list = new ArrayList<>();
		if (jsonItem != null && jsonItem.has("lore")) {
			for (JsonElement line : jsonItem.getAsJsonArray("lore")) {
				list.add(line.getAsString());
			}
		}

		return list;
	}
}
