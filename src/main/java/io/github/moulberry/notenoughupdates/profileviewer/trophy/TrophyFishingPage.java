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

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class TrophyFishingPage {

	// i know i use a lot of maps and lists and pairs don't tell me :)

	public static Map<String, EnumChatFormatting> internalTrophyFish = new HashMap<String, EnumChatFormatting>() {
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

	//16,48
	//

	private static LinkedHashMap<ItemStack, Pair<String, Integer>> armorHelmets =
		new LinkedHashMap<ItemStack, Pair<String, Integer>>() {
			{
				put(NotEnoughUpdates.INSTANCE.manager.jsonToStack(NotEnoughUpdates.INSTANCE.manager
					.getItemInformation()
					.get("BRONZE_HUNTER_HELMET")), Pair.of(EnumChatFormatting.GREEN + "Novice Fisher", 1));
				put(NotEnoughUpdates.INSTANCE.manager.jsonToStack(NotEnoughUpdates.INSTANCE.manager
					.getItemInformation()
					.get("SILVER_HUNTER_HELMET")), Pair.of(EnumChatFormatting.BLUE + "Adept Fisher", 2));
				put(NotEnoughUpdates.INSTANCE.manager.jsonToStack(NotEnoughUpdates.INSTANCE.manager
					.getItemInformation()
					.get("GOLD_HUNTER_HELMET")), Pair.of(EnumChatFormatting.DARK_PURPLE + "Expert Fisher", 3));
				put(NotEnoughUpdates.INSTANCE.manager.jsonToStack(NotEnoughUpdates.INSTANCE.manager
					.getItemInformation()
					.get("DIAMOND_HUNTER_HELMET")), Pair.of(EnumChatFormatting.GOLD + "Master Fisher", 4));
			}
		};

	private static Map<Integer, Pair<Integer, Integer>> slotLocations = new HashMap<Integer, Pair<Integer, Integer>>() {
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
	private static long totalCount = 0;
	private static int guiLeft;
	private static int guiTop;

	private static final ResourceLocation TROPHY_FISH_TEXTURE = new ResourceLocation(
		"notenoughupdates:pv_trophy_fish_tab.png");
	private static final Map<String, TrophyFish> trophyFishList = new HashMap<>();

	private static final Map<String, Integer> total = new HashMap<>();

	public static void renderPage(int mouseX, int mouseY) {
		guiLeft = GuiProfileViewer.getGuiLeft();
		guiTop = GuiProfileViewer.getGuiTop();
		JsonObject trophyInformation = getTrophyInformation(null);
		if (trophyInformation == null) {
			return;
		}

		ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
		int width = scaledResolution.getScaledWidth();
		int height = scaledResolution.getScaledHeight();

		Minecraft.getMinecraft().getTextureManager().bindTexture(TROPHY_FISH_TEXTURE);
		Utils.drawTexturedRect(guiLeft, guiTop, 431, 202, GL11.GL_NEAREST);

		GlStateManager.color(1, 1, 1, 1);
		GlStateManager.disableLighting();

		AtomicInteger row = new AtomicInteger();
		AtomicInteger col = new AtomicInteger();

		ArrayList<TrophyFish> arrayList = new ArrayList<>(trophyFishList.values());
		arrayList.sort((c1, c2) -> {
			if (c1.getTotal() > c2.getTotal()) return -1;
			if (c1.getTotal() < c2.getTotal()) return 1;
			return 0;
		});

		AtomicInteger x = new AtomicInteger();
		AtomicInteger y = new AtomicInteger();
		for (TrophyFish value : arrayList) {
			RenderHelper.enableGUIStandardItemLighting();
			x.set(guiLeft + slotLocations.get(arrayList.indexOf(value)).getLeft());
			y.set(guiTop + slotLocations.get(arrayList.indexOf(value)).getRight());
			Minecraft.getMinecraft().getRenderItem().renderItemIntoGUI(
				getItem(value.getName()),
				x.get(),
				y.get()
			);

			if (mouseX >= x.get() && mouseX < x.get() + 24) {
				if (mouseY >= y.get() && mouseY <= y.get() + 24) {
					Utils.drawHoveringText(
						getTooltip(value),
						mouseX,
						mouseY,
						width,
						height,
						-1,
						Minecraft.getMinecraft().fontRendererObj
					);
				}
			}
			col.getAndIncrement();
			if (col.get() == 5) {
				col.set(0);
				row.getAndIncrement();
			}
		}

		if (arrayList.size() != internalTrophyFish.size()) {
			List<String> clonedList = new ArrayList<>(internalTrophyFish.size());
			clonedList.addAll(internalTrophyFish.keySet());
			clonedList.removeAll(fixStringName(new ArrayList<>(trophyFishList.keySet())));
			for (String difference : clonedList) {
				RenderHelper.enableGUIStandardItemLighting();
				x.set(
					guiLeft + slotLocations.get(clonedList.indexOf(difference) + (trophyFishList.keySet().size())).getLeft());
				y.set(
					guiTop + slotLocations.get(clonedList.indexOf(difference) + (trophyFishList.keySet().size())).getRight());
				ItemStack itemStack = new ItemStack(Items.dye, 1, 8);
				Minecraft.getMinecraft().getRenderItem().renderItemIntoGUI(
					itemStack,
					x.get(),
					y.get()
				);
				if (mouseX >= x.get() && mouseX < x.get() + 24) {
					if (mouseY >= y.get() && mouseY <= y.get() + 24) {
						Utils.drawHoveringText(
							getTooltipIfNotFound(difference),
							mouseX,
							mouseY,
							width,
							height,
							-1,
							Minecraft.getMinecraft().fontRendererObj
						);
					}

				}
				col.getAndIncrement();
				if (col.get() == 5) {
					col.set(0);
					row.getAndIncrement();
				}

			}
		}

		AtomicInteger i = new AtomicInteger();
		if (!trophyInformation.has("trophy_fish") &&
			!trophyInformation.get("trophy_fish").getAsJsonObject().has("rewards")) {
			return;
		}

		JsonArray rewards = trophyInformation.get("trophy_fish").getAsJsonObject().get("rewards").getAsJsonArray();
		for (ItemStack itemStack : armorHelmets.keySet()) {
			int integer = armorHelmets.get(itemStack).getRight();
			x.set(guiLeft + 18);
			y.set(guiTop + 50 + i.get());

			Minecraft.getMinecraft().getRenderItem().renderItemIntoGUI(itemStack, x.get(), y.get());
			Utils.drawStringF(armorHelmets.get(itemStack).getLeft(), Minecraft.getMinecraft().fontRendererObj,
				x.get() + 20, y.get() + 4, true, 0
			);
			try {
				JsonElement jsonElement = rewards.get(integer - 1);
				if (!jsonElement.isJsonNull()) {
					Utils.drawStringF(check, Minecraft.getMinecraft().fontRendererObj,
						x.get() + 100, y.get() + 2, true, 0
					);
				} else {
					Utils.drawStringF(checkX, Minecraft.getMinecraft().fontRendererObj,
						x.get() + 100, y.get() + 4, true, 0
					);
				}
			} catch (IndexOutOfBoundsException exception) {
				Utils.drawStringF(checkX, Minecraft.getMinecraft().fontRendererObj,
					x.get() + 100, y.get() + 4, true, 0
				);
			}
			i.set(i.get() + 10);
		}

		ItemStack fishing_rod = NotEnoughUpdates.INSTANCE.manager.jsonToStack(NotEnoughUpdates.INSTANCE.manager.getItemInformation().get("FISHING_ROD"));

		Minecraft.getMinecraft().getRenderItem().renderItemIntoGUI(fishing_rod, guiLeft + 20, guiTop+21);

		Utils.drawStringF(
			EnumChatFormatting.AQUA + "Total Caught: §f" + totalCount,
			Minecraft.getMinecraft().fontRendererObj,
			guiLeft + 38,
			guiTop + 25,
			true,
			0
		);

		JsonObject stats = trophyInformation.get("stats").getAsJsonObject();

		int thunderKills = 0;

		if (stats.has("kills_thunder")) {
			thunderKills = stats.getAsJsonObject().get("kills_thunder").getAsInt();
		}

		int jawbusKills = 0;



		if (stats.has("kills_lord_jawbus")) {
			jawbusKills = stats.getAsJsonObject().get("kills_lord_jawbus").getAsInt();
		}
		ItemStack thunder_sc = NotEnoughUpdates.INSTANCE.manager.jsonToStack(NotEnoughUpdates.INSTANCE.manager.getItemInformation().get("THUNDER_SC"));
		Minecraft.getMinecraft().getRenderItem().renderItemIntoGUI(thunder_sc, guiLeft + 16, guiTop+108);


		Utils.drawStringF(
			EnumChatFormatting.AQUA + "Thunder Kills: §f" + thunderKills,
			Minecraft.getMinecraft().fontRendererObj,
			guiLeft + 36,
			guiTop + 112,
			true,
			0
		);


		ItemStack lord_jawbus_sc = NotEnoughUpdates.INSTANCE.manager.jsonToStack(NotEnoughUpdates.INSTANCE.manager.getItemInformation().get("LORD_JAWBUS_SC"));
		Minecraft.getMinecraft().getRenderItem().renderItemIntoGUI(lord_jawbus_sc, guiLeft + 16, guiTop+120);

		Utils.drawStringF(
			EnumChatFormatting.AQUA + "Lord Jawbus Kills: §f" + jawbusKills,
			Minecraft.getMinecraft().fontRendererObj,
			guiLeft + 36,
			guiTop + 124,
			true,
			0
		);
		GlStateManager.enableLighting();
	}

	private static List<String> getTooltip(TrophyFish fish) {
		List<String> tooltip = new ArrayList<>();
		tooltip.add(
			internalTrophyFish.get(fish.getInternalName()) + WordUtils.capitalize(fish.getName().replace("_", " ")));
		Map<TrophyFish.TrophyFishRarity, Integer> trophyFishRarityIntegerMap = fish.getTrophyFishRarityIntegerMap();
		tooltip.add(" ");
		tooltip.add(display(trophyFishRarityIntegerMap, TrophyFish.TrophyFishRarity.DIAMOND, EnumChatFormatting.AQUA));
		tooltip.add(display(trophyFishRarityIntegerMap, TrophyFish.TrophyFishRarity.GOLD, EnumChatFormatting.GOLD));
		tooltip.add(display(trophyFishRarityIntegerMap, TrophyFish.TrophyFishRarity.SILVER, EnumChatFormatting.GRAY));
		tooltip.add(display(trophyFishRarityIntegerMap, TrophyFish.TrophyFishRarity.BRONZE, EnumChatFormatting.DARK_GRAY
		));
		return tooltip;
	}

	private static List<String> getTooltipIfNotFound(String name) {
		List<String> tooltip = new ArrayList<>();
		tooltip.add(
			internalTrophyFish.get(name.toLowerCase().replace(" ", "_")) + WordUtils.capitalize(name.replace("_", " ")));
		tooltip.add(" ");
		tooltip.add(EnumChatFormatting.RED + checkX + " Not Discovered");
		tooltip.add(" ");
		tooltip.add(display(null, TrophyFish.TrophyFishRarity.DIAMOND, EnumChatFormatting.AQUA));
		tooltip.add(display(null, TrophyFish.TrophyFishRarity.GOLD, EnumChatFormatting.GOLD));
		tooltip.add(display(null, TrophyFish.TrophyFishRarity.SILVER, EnumChatFormatting.GRAY));
		tooltip.add(display(null, TrophyFish.TrophyFishRarity.BRONZE, EnumChatFormatting.DARK_GRAY));
		return tooltip;
	}

	private static final String checkX = "§c✖";
	private static final String check = "§a✔";

	private static String display(
		Map<TrophyFish.TrophyFishRarity, Integer> trophyFishRarityIntegerMap,
		TrophyFish.TrophyFishRarity rarity, EnumChatFormatting color
	) {
		String name = WordUtils.capitalize(rarity.name().toLowerCase());
		if (trophyFishRarityIntegerMap == null) {
			return color + name + ": " + checkX;
		}

		if (trophyFishRarityIntegerMap.containsKey(rarity)) {
			return color + name + ": " + EnumChatFormatting.GOLD +
				trophyFishRarityIntegerMap.get(rarity);
		} else {
			return color + name + ": " + checkX;
		}
	}

	private static ItemStack getItem(String name) {
		String repoName = name.toUpperCase().replace(" ", "_") + "_BRONZE";
		JsonObject jsonItem = NotEnoughUpdates.INSTANCE.manager.getItemInformation().get(repoName);
		return NotEnoughUpdates.INSTANCE.manager.jsonToStack(jsonItem);
	}

	private static JsonObject getTrophyInformation(String profileId) {
		trophyFishList.clear();

		JsonObject trophyFishInformation = GuiProfileViewer.getProfile().getProfileInformation(profileId);
		if (!trophyFishInformation.has("trophy_fish")) {
			Utils.drawStringCentered(EnumChatFormatting.RED + "No data found for player",
				Minecraft.getMinecraft().fontRendererObj,
				guiLeft + 431 / 2f,
				guiTop + 101,
				true,
				0
			);
			return null;
		}
		JsonObject trophyObject = trophyFishInformation.get("trophy_fish").getAsJsonObject();
		Map<String, List<Pair<TrophyFish.TrophyFishRarity, Integer>>> trophyFishRarityIntegerMap = new HashMap<>();
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
				trophyFishRarity = TrophyFish.TrophyFishRarity.valueOf(type.toUpperCase());
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
			if (!TrophyFishingPage.trophyFishList.containsKey(name)) {
				TrophyFish trophyFish = new TrophyFish(name, new HashMap<>());
				trophyFish.addTotal(TrophyFishingPage.total.get(name));
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
		return trophyFishInformation;
	}

	private static List<String> fixStringName(List<String> list) {
		List<String> fixedList = new ArrayList<>();
		for (String s : list) {
			fixedList.add(s.toLowerCase().replace(" ", "_"));
		}
		return fixedList;
	}
}
