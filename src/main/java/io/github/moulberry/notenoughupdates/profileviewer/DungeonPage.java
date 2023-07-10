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

package io.github.moulberry.notenoughupdates.profileviewer;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.core.util.StringUtils;
import io.github.moulberry.notenoughupdates.itemeditor.GuiElementTextField;
import io.github.moulberry.notenoughupdates.profileviewer.weight.weight.Weight;
import io.github.moulberry.notenoughupdates.util.Constants;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.text.WordUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class DungeonPage extends GuiProfileViewerPage {

	private static final ResourceLocation pv_dung = new ResourceLocation("notenoughupdates:pv_dung.png");
	private static final ItemStack DEADBUSH = new ItemStack(Item.getItemFromBlock(Blocks.deadbush));
	private static final ItemStack[] BOSS_HEADS = new ItemStack[7];
	private static final Map<String, ItemStack> classToIcon = new HashMap<String, ItemStack>() {{
		put("healer", new ItemStack(Items.potionitem, 1, 16389));
		put("mage", new ItemStack(Items.blaze_rod));
		put("berserk", new ItemStack(Items.iron_sword));
		put("archer", new ItemStack(Items.bow));
		put("tank", new ItemStack(Items.leather_chestplate));
	}};
	private static final String[] bossFloorArr = {"Bonzo", "Scarf", "Professor", "Thorn", "Livid", "Sadan", "Necron"};
	private static final String[] bossFloorHeads = {
		"12716ecbf5b8da00b05f316ec6af61e8bd02805b21eb8e440151468dc656549c",
		"7de7bbbdf22bfe17980d4e20687e386f11d59ee1db6f8b4762391b79a5ac532d",
		"9971cee8b833a62fc2a612f3503437fdf93cad692d216b8cf90bbb0538c47dd8",
		"8b6a72138d69fbbd2fea3fa251cabd87152e4f1c97e5f986bf685571db3cc0",
		"c1007c5b7114abec734206d4fc613da4f3a0e99f71ff949cedadc99079135a0b",
		"fa06cb0c471c1c9bc169af270cd466ea701946776056e472ecdaeb49f0f4a4dc",
		"a435164c05cea299a3f016bbbed05706ebb720dac912ce4351c2296626aecd9a",
	};
	private static final LinkedHashMap<String, ItemStack> dungeonsModeIcons = new LinkedHashMap<String, ItemStack>() {
		{
			put(
				"catacombs",
				Utils.editItemStackInfo(
					NotEnoughUpdates.INSTANCE.manager.jsonToStack(
						NotEnoughUpdates.INSTANCE.manager.getItemInformation().get("DUNGEON_STONE")
					),
					EnumChatFormatting.GRAY + "Normal Mode",
					true
				)
			);
			put(
				"master_catacombs",
				Utils.editItemStackInfo(
					NotEnoughUpdates.INSTANCE.manager.jsonToStack(
						NotEnoughUpdates.INSTANCE.manager.getItemInformation().get("MASTER_SKULL_TIER_7")
					),
					EnumChatFormatting.GRAY + "Master Mode",
					true
				)
			);
		}
	};

	private static int floorTime = 7;
	private final GuiElementTextField dungeonLevelTextField = new GuiElementTextField("", GuiElementTextField.SCALE_TEXT);
	private int floorLevelTo = -1;
	private long floorLevelToXP = -1;
	private boolean onMasterMode = false;

	public DungeonPage(GuiProfileViewer instance) {
		super(instance);
	}

	@Override
	public void drawPage(int mouseX, int mouseY, float partialTicks) {
		FontRenderer fontRendererObj = Minecraft.getMinecraft().fontRendererObj;
		int guiLeft = GuiProfileViewer.getGuiLeft();
		int guiTop = GuiProfileViewer.getGuiTop();

		Minecraft.getMinecraft().getTextureManager().bindTexture(pv_dung);
		Utils.drawTexturedRect(guiLeft, guiTop, getInstance().sizeX, getInstance().sizeY, GL11.GL_NEAREST);

		JsonObject leveling = Constants.LEVELING;
		if (leveling == null) return;

		SkyblockProfiles.SkyblockProfile selectedProfile = getSelectedProfile();
		if (selectedProfile == null) {
			return;
		}

		Map<String, ProfileViewer.Level> levelingInfo = selectedProfile.getLevelingInfo();
		if (levelingInfo == null) {
			return;
		}
		JsonObject profileInfo = selectedProfile.getProfileJson();
		JsonObject hypixelInfo = GuiProfileViewer.getProfile().getHypixelProfile();

		int sectionWidth = 110;
		String dungeonString = onMasterMode ? "master_catacombs" : "catacombs";

		Utils.renderShadowedString(
			EnumChatFormatting.RED + (onMasterMode ? "Master Mode" : "Catacombs"),
			(guiLeft + getInstance().sizeX / 2),
			guiTop + 5,
			sectionWidth
		);

		ProfileViewer.Level levelObjCata = levelingInfo.get("cosmetic_catacombs");
		{
			String skillName = EnumChatFormatting.RED + "Catacombs";
			float level = levelObjCata.level;
			int levelFloored = (int) Math.floor(level);

			if (floorLevelTo == -1 && levelFloored >= 0) {
				dungeonLevelTextField.setText(String.valueOf(levelFloored + 1));
				calculateFloorLevelXP();
			}

			int x = guiLeft + 23;
			int y = guiTop + 25;

			getInstance().renderXpBar(skillName, DEADBUSH, x, y, sectionWidth, levelObjCata, mouseX, mouseY);

			Utils.renderAlignedString(
				EnumChatFormatting.YELLOW + "Until Cata " + floorLevelTo + ": ",
				EnumChatFormatting.WHITE + StringUtils.shortNumberFormat((double) floorLevelToXP),
				x,
				y + 16,
				sectionWidth
			);

			if (mouseX > x && mouseX < x + sectionWidth && mouseY > y + 16 && mouseY < y + 24 && !onMasterMode) {
				float F5 =
					(Utils.getElementAsFloat(Utils.getElement(
						profileInfo,
						"dungeons.dungeon_types.catacombs.tier_completions." + 5
					), 0)); //this can prob be done better
				float F6 =
					(Utils.getElementAsFloat(Utils.getElement(
						profileInfo,
						"dungeons.dungeon_types.catacombs.tier_completions." + 6
					), 0));
				float F7 =
					(Utils.getElementAsFloat(Utils.getElement(
						profileInfo,
						"dungeons.dungeon_types.catacombs.tier_completions." + 7
					), 0));
				if (F5 > 150) {
					F5 = 150;
				}
				if (F6 > 100) {
					F6 = 100;
				}
				if (F7 > 50) {
					F7 = 50;
				}
				float xpF5 = 2400 * (F5 / 100 + 1);
				float xpF6 = 4880 * (F6 / 100 + 1);
				float xpF7 = 28000 * (F7 / 100 + 1);
				if (!Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
					xpF5 *= 1.1;
					xpF6 *= 1.1;
					xpF7 *= 1.1;
				}

				long runsF5 = (int) Math.ceil(floorLevelToXP / xpF5);
				long runsF6 = (int) Math.ceil(floorLevelToXP / xpF6);
				long runsF7 = (int) Math.ceil(floorLevelToXP / xpF7);

				float timeF5 = getElementAsFloat(profileInfo, "dungeons.dungeon_types.catacombs.fastest_time_s_plus.5");
				float timeF6 = getElementAsFloat(profileInfo, "dungeons.dungeon_types.catacombs.fastest_time_s_plus.6");
				float timeF7 = getElementAsFloat(profileInfo, "dungeons.dungeon_types.catacombs.fastest_time_s_plus.7");

				getInstance().tooltipToDisplay =
					Lists.newArrayList(
						EnumChatFormatting.YELLOW + "Remaining XP: " + EnumChatFormatting.GRAY +
							String.format("%,d", floorLevelToXP),
						String.format("# F5 Runs (%s xp) : %d", StringUtils.shortNumberFormat(xpF5), runsF5),
						String.format("# F6 Runs (%s xp) : %d", StringUtils.shortNumberFormat(xpF6), runsF6),
						String.format("# F7 Runs (%s xp) : %d", StringUtils.shortNumberFormat(xpF7), runsF7),
						""
					);
				boolean hasTime = false;
				hasTime = isHasTime(timeF5, "Expected Time (F5) : %s", runsF5, hasTime);
				hasTime = isHasTime(timeF6, "Expected Time (F6) : %s", runsF6, hasTime);
				hasTime = isHasTime(timeF7, "Expected Time (F7) : %s", runsF7, hasTime);
				if (hasTime) {
					getInstance().tooltipToDisplay.add("");
				}
				if (!Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
					getInstance()
						.tooltipToDisplay.add(
							"[Hold " + EnumChatFormatting.YELLOW + "SHIFT" + EnumChatFormatting.GRAY + " to show without Expert Ring]"
						);
				}
				if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
					if (!Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) getInstance().tooltipToDisplay.add("");
					getInstance().tooltipToDisplay.add("Number of runs is calculated as [Remaining XP]/[XP per Run].");
					getInstance().tooltipToDisplay.add("The [XP per Run] is the average xp gained from an S+ run");
					getInstance()
						.tooltipToDisplay.add(
							"The " +
								EnumChatFormatting.DARK_PURPLE +
								"Catacombs Expert Ring" +
								EnumChatFormatting.GRAY +
								" is assumed to be used, unless " +
								EnumChatFormatting.YELLOW +
								"SHIFT" +
								EnumChatFormatting.GRAY +
								" is held."
						);
					getInstance().tooltipToDisplay.add("[Time per run] is calculated using Fastest S+ x 120%");
				} else {
					getInstance()
						.tooltipToDisplay.add(
							"[Hold " + EnumChatFormatting.YELLOW + "CTRL" + EnumChatFormatting.GRAY + " to see details]");
				}
			}

			if (mouseX > x && mouseX < x + sectionWidth && mouseY > y + 16 && mouseY < y + 24 && onMasterMode) {
				float M3 = Math.min(getElementAsFloat(profileInfo, "dungeons.dungeon_types.master_catacombs.tier_completions.3"), 50);
				float M4 = Math.min(getElementAsFloat(profileInfo, "dungeons.dungeon_types.master_catacombs.tier_completions.4"), 50);
				float M5 = Math.min(getElementAsFloat(profileInfo, "dungeons.dungeon_types.master_catacombs.tier_completions.5"), 50);
				float M6 = Math.min(getElementAsFloat(profileInfo, "dungeons.dungeon_types.master_catacombs.tier_completions.6"), 50);
				float M7 = Math.min(getElementAsFloat(profileInfo, "dungeons.dungeon_types.master_catacombs.tier_completions.7"), 50);

				float xpM3 = 35000 * (M3 / 100 + 1);
				float xpM4 = 55000 * (M4 / 100 + 1);
				float xpM5 = 70000 * (M5 / 100 + 1);
				float xpM6 = 100000 * (M6 / 100 + 1);
				float xpM7 = 300000 * (M7 / 100 + 1);
				//No clue if M3 or M4 xp values are right
				if (!Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
					xpM3 *= 1.1;
					xpM4 *= 1.1;
					xpM5 *= 1.1;
					xpM6 *= 1.1;
					xpM7 *= 1.1;
				}

				long runsM3 = (int) Math.ceil(floorLevelToXP / xpM3);
				long runsM4 = (int) Math.ceil(floorLevelToXP / xpM4);
				long runsM5 = (int) Math.ceil(floorLevelToXP / xpM5);
				long runsM6 = (int) Math.ceil(floorLevelToXP / xpM6);
				long runsM7 = (int) Math.ceil(floorLevelToXP / xpM7);

				float timeM3 = getElementAsFloat(profileInfo, "dungeons.dungeon_types.master_catacombs.fastest_time_s_plus.3");
				float timeM4 = getElementAsFloat(profileInfo, "dungeons.dungeon_types.master_catacombs.fastest_time_s_plus.4");
				float timeM5 = getElementAsFloat(profileInfo, "dungeons.dungeon_types.master_catacombs.fastest_time_s_plus.5");
				float timeM6 = getElementAsFloat(profileInfo, "dungeons.dungeon_types.master_catacombs.fastest_time_s_plus.6");
				float timeM7 = getElementAsFloat(profileInfo, "dungeons.dungeon_types.master_catacombs.fastest_time_s_plus.7");

				getInstance().tooltipToDisplay =
					Lists.newArrayList(
						EnumChatFormatting.YELLOW + "Remaining XP: " + EnumChatFormatting.GRAY +
							String.format("%,d", floorLevelToXP),
						String.format("# M3 Runs (%s xp) : %d", StringUtils.shortNumberFormat(xpM3), runsM3),
						String.format("# M4 Runs (%s xp) : %d", StringUtils.shortNumberFormat(xpM4), runsM4),
						String.format("# M5 Runs (%s xp) : %d", StringUtils.shortNumberFormat(xpM5), runsM5),
						String.format("# M6 Runs (%s xp) : %d", StringUtils.shortNumberFormat(xpM6), runsM6),
						String.format("# M7 Runs (%s xp) : %d", StringUtils.shortNumberFormat(xpM7), runsM7),
						""
					);
				boolean hasTime = false;
				hasTime = isHasTime(timeM3, "Expected Time (M3) : %s", runsM3, hasTime);
				hasTime = isHasTime(timeM4, "Expected Time (M4) : %s", runsM4, hasTime);
				hasTime = isHasTime(timeM5, "Expected Time (M5) : %s", runsM5, hasTime);
				hasTime = isHasTime(timeM6, "Expected Time (M6) : %s", runsM6, hasTime);
				hasTime = isHasTime(timeM7, "Expected Time (M7) : %s", runsM7, hasTime);
				if (hasTime) {
					getInstance().tooltipToDisplay.add("");
				}
				if (!Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
					getInstance()
						.tooltipToDisplay.add(
							"[Hold " + EnumChatFormatting.YELLOW + "SHIFT" + EnumChatFormatting.GRAY + " to show without Expert Ring]"
						);
				}
				if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
					if (!Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) getInstance().tooltipToDisplay.add("");
					getInstance().tooltipToDisplay.add("Number of runs is calculated as [Remaining XP]/[XP per Run].");
					getInstance().tooltipToDisplay.add("The [XP per Run] is the average xp gained from an S+ run");
					getInstance()
						.tooltipToDisplay.add(
							"The " +
								EnumChatFormatting.DARK_PURPLE +
								"Catacombs Expert Ring" +
								EnumChatFormatting.GRAY +
								" is assumed to be used, unless " +
								EnumChatFormatting.YELLOW +
								"SHIFT" +
								EnumChatFormatting.GRAY +
								" is held."
						);
					getInstance().tooltipToDisplay.add("[Time per run] is calculated using Fastest S+ x 120%");
				} else {
					getInstance()
						.tooltipToDisplay.add(
							"[Hold " + EnumChatFormatting.YELLOW + "CTRL" + EnumChatFormatting.GRAY + " to see details]");
				}
			}

			dungeonLevelTextField.setSize(20, 10);
			dungeonLevelTextField.render(x + 22, y + 29);
			int calcLen = fontRendererObj.getStringWidth("Calculate");
			Utils.renderShadowedString(
				EnumChatFormatting.WHITE + "Calculate",
				x + sectionWidth - 17 - calcLen / 2f,
				y + 30,
				100
			);

			// Random stats
			float secrets = -1;
			if (hypixelInfo != null) {
				secrets = Utils.getElementAsFloat(Utils.getElement(hypixelInfo, "achievements.skyblock_treasure_hunter"), 0);
			}
			float totalRunsF = 0;
			float totalRunsF5 = 0;
			for (int i = 1; i <= 7; i++) {
				float runs = getElementAsFloat(profileInfo, "dungeons.dungeon_types.catacombs.tier_completions." + i);
				totalRunsF += runs;
				if (i >= 5) {
					totalRunsF5 += runs;
				}
			}
			float totalRunsM = 0;
			float totalRunsM5 = 0;
			for (int i = 1; i <= 7; i++) {
				float runs = getElementAsFloat(profileInfo, "dungeons.dungeon_types.master_catacombs.tier_completions." + i);
				totalRunsM += runs;
				if (i >= 5) {
					totalRunsM5 += runs;
				}
			}
			float totalRuns = totalRunsF + totalRunsM;

			float mobKills;
			float mobKillsF = 0;
			for (int i = 1; i <= 7; i++) {
				float kills = getElementAsFloat(profileInfo, "dungeons.dungeon_types.catacombs.mobs_killed." + i);
				mobKillsF += kills;
			}
			float mobKillsM = 0;
			for (int i = 1; i <= 7; i++) {
				float kills = getElementAsFloat(profileInfo, "dungeons.dungeon_types.master_catacombs.mobs_killed." + i);
				mobKillsM += kills;
			}
			mobKills = mobKillsF + mobKillsM;

			int miscTopY = y + 55;

			Utils.renderAlignedString(
				EnumChatFormatting.YELLOW + "Total Runs " + (onMasterMode ? "M" : "F"),
				EnumChatFormatting.WHITE.toString() + ((int) (onMasterMode ? totalRunsM : totalRunsF)),
				x,
				miscTopY,
				sectionWidth
			);
			Utils.renderAlignedString(
				EnumChatFormatting.YELLOW + "Total Runs (" + (onMasterMode ? "M" : "F") + "5-7)  ",
				EnumChatFormatting.WHITE.toString() + ((int) (onMasterMode ? totalRunsM5 : totalRunsF5)),
				x,
				miscTopY + 10,
				sectionWidth
			);
			Utils.renderAlignedString(
				EnumChatFormatting.YELLOW + "Secrets (Total)  ",
				EnumChatFormatting.WHITE + (secrets == -1 ? "?" : StringUtils.shortNumberFormat(secrets)),
				x,
				miscTopY + 20,
				sectionWidth
			);
			Utils.renderAlignedString(
				EnumChatFormatting.YELLOW + "Secrets (/Run)  ",
				EnumChatFormatting.WHITE.toString() + (secrets == -1 ? "?" : (Math.round(
					secrets / Math.max(1, totalRuns) * 100) / 100f)),
				x,
				miscTopY + 30,
				sectionWidth
			);
			Utils.renderAlignedString(
				EnumChatFormatting.YELLOW + "Mob Kills (Total)  ",
				EnumChatFormatting.WHITE + StringUtils.shortNumberFormat(mobKills),
				x,
				miscTopY + 40,
				sectionWidth
			);

			int y3 = y + 117;

			for (int i = 1; i <= 7; i++) {
				int w = fontRendererObj.getStringWidth(String.valueOf(i));
				int bx = x + sectionWidth * i / 8 - w / 2;
				GlStateManager.color(1, 1, 1, 1);
				Utils.renderShadowedString(EnumChatFormatting.WHITE.toString() + i, bx + w / 2, y3, 10);
			}

			float timeNorm = getElementAsFloat(profileInfo, "dungeons.dungeon_types." + dungeonString + ".fastest_time." + floorTime);
			float timeS = getElementAsFloat(profileInfo, "dungeons.dungeon_types." + dungeonString + ".fastest_time_s." + floorTime);
			float timeSPLUS = getElementAsFloat(
				profileInfo,
				"dungeons.dungeon_types." + dungeonString + ".fastest_time_s_plus." + floorTime
			);
			String timeNormStr = timeNorm <= 0 ? "N/A" : Utils.prettyTime((long) timeNorm);
			String timeSStr = timeS <= 0 ? "N/A" : Utils.prettyTime((long) timeS);
			String timeSPlusStr = timeSPLUS <= 0 ? "N/A" : Utils.prettyTime((long) timeSPLUS);
			Utils.renderAlignedString(
				EnumChatFormatting.YELLOW + "Floor " + floorTime + " ",
				EnumChatFormatting.WHITE + timeNormStr,
				x,
				y3 + 10,
				sectionWidth
			);
			Utils.renderAlignedString(
				EnumChatFormatting.YELLOW + "Floor " + floorTime + " S",
				EnumChatFormatting.WHITE + timeSStr,
				x,
				y3 + 20,
				sectionWidth
			);
			Utils.renderAlignedString(
				EnumChatFormatting.YELLOW + "Floor " + floorTime + " S+",
				EnumChatFormatting.WHITE + timeSPlusStr,
				x,
				y3 + 30,
				sectionWidth
			);
		}

		//Completions
		{
			int x = guiLeft + 161;
			int y = guiTop + 27;

			Utils.renderShadowedString(EnumChatFormatting.RED + "Boss Collections", x + sectionWidth / 2, y, sectionWidth);
			for (int i = 1; i <= 7; i++) {
				float compl = getElementAsFloat(profileInfo, "dungeons.dungeon_types." + dungeonString + ".tier_completions." + i);

				if (BOSS_HEADS[i - 1] == null) {
					String textureLink = bossFloorHeads[i - 1];

					String b64Decoded =
						"{\"textures\":{\"SKIN\":{\"url\":\"http://textures.minecraft.net/texture/" + textureLink + "\"}}}";
					String b64Encoded = new String(Base64.getEncoder().encode(b64Decoded.getBytes()));

					ItemStack stack = new ItemStack(Items.skull, 1, 3);
					NBTTagCompound nbt = new NBTTagCompound();
					NBTTagCompound skullOwner = new NBTTagCompound();
					NBTTagCompound properties = new NBTTagCompound();
					NBTTagList textures = new NBTTagList();
					NBTTagCompound textures_0 = new NBTTagCompound();

					String uuid = UUID.nameUUIDFromBytes(b64Encoded.getBytes()).toString();
					skullOwner.setString("Id", uuid);
					skullOwner.setString("Name", uuid);

					textures_0.setString("Value", b64Encoded);
					textures.appendTag(textures_0);

					properties.setTag("textures", textures);
					skullOwner.setTag("Properties", properties);
					nbt.setTag("SkullOwner", skullOwner);
					stack.setTagCompound(nbt);

					BOSS_HEADS[i - 1] = stack;
				}

				GlStateManager.pushMatrix();
				GlStateManager.translate(x - 4, y + 10 + 20 * (i - 1), 0);
				GlStateManager.scale(1.3f, 1.3f, 1);
				Utils.drawItemStack(BOSS_HEADS[i - 1], 0, 0);
				GlStateManager.popMatrix();

				Utils.renderAlignedString(
					String.format(
						EnumChatFormatting.YELLOW + "%s (" + (onMasterMode ? "M" : "F") + "%d) ",
						bossFloorArr[i - 1],
						i
					),
					EnumChatFormatting.WHITE.toString() + (int) compl,
					x + 16,
					y + 18 + 20 * (i - 1),
					sectionWidth - 15
				);
			}
		}

		// Classes
		{
			int x = guiLeft + 298;
			int y = guiTop + 27;

			Utils.renderShadowedString(
				EnumChatFormatting.DARK_PURPLE + "Class Levels",
				x + sectionWidth / 2,
				y,
				sectionWidth
			);

			JsonElement activeClassElement = Utils.getElement(profileInfo, "dungeons.selected_dungeon_class");
			String activeClass = null;
			if (activeClassElement instanceof JsonPrimitive && ((JsonPrimitive) activeClassElement).isString()) {
				activeClass = activeClassElement.getAsString();
			}

			float classLevelSum = 0;
			for (int i = 0; i < Weight.DUNGEON_CLASS_NAMES.size(); i++) {
				String className = Weight.DUNGEON_CLASS_NAMES.get(i);

				String colour = className.equalsIgnoreCase(activeClass) ? EnumChatFormatting.GREEN.toString() : EnumChatFormatting.WHITE.toString();
				ProfileViewer.Level levelObj = levelingInfo.get("cosmetic_" + className);
				classLevelSum += levelObj.level;

				getInstance()
					.renderXpBar(
						colour + WordUtils.capitalizeFully(className),
						classToIcon.get(className),
						x,
						y + 20 + 24 * i,
						sectionWidth,
						levelObj,
						mouseX,
						mouseY
					);
			}

			ProfileViewer.Level classAverage = new ProfileViewer.Level();
			classAverage.level = classLevelSum / Weight.DUNGEON_CLASS_NAMES.size();
			if (classAverage.level >= 50) {
				classAverage.maxed = true;
			}

			getInstance().renderXpBar(
				EnumChatFormatting.WHITE + "Class Average",
				new ItemStack(Items.nether_star),
				x,
				y + 20 + 24 * 5,
				sectionWidth,
				classAverage,
				mouseX, mouseY
			);
		}

		drawSideButtons();
	}

	private boolean isHasTime(float fastestTime, String format, long runsAmount, boolean hasTime) {
		if (fastestTime > 1000) {
			getInstance()
				.tooltipToDisplay.add(String.format(
					format,
					Utils.prettyTime(runsAmount * (long) (fastestTime * 1.2))
				));
			hasTime = true;
		}
		return hasTime;
	}

	private static float getElementAsFloat(JsonObject profileInfo, String path) {
		return Utils.getElementAsFloat(Utils.getElement(profileInfo, path), 0);
	}

	@Override
	public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		FontRenderer fontRendererObj = Minecraft.getMinecraft().fontRendererObj;
		int guiLeft = GuiProfileViewer.getGuiLeft();
		int guiTop = GuiProfileViewer.getGuiTop();

		if (mouseX >= guiLeft + 45 && mouseX <= guiLeft + 65 && mouseY >= guiTop + 54 && mouseY <= guiTop + 64) {
			dungeonLevelTextField.mouseClicked(mouseX, mouseY, mouseButton);
		} else {
			dungeonLevelTextField.otherComponentClick();
		}

		int cW = fontRendererObj.getStringWidth("Calculate");
		if (mouseX >= guiLeft + 23 + 110 - 17 - cW && mouseX <= guiLeft + 23 + 110 - 17 && mouseY >= guiTop + 55 &&
			mouseY <= guiTop + 65) {
			calculateFloorLevelXP();
		}

		int y = guiTop + 142;

		if (mouseY >= y - 2 && mouseY <= y + 9) {
			for (int i = 1; i <= 7; i++) {
				int w = fontRendererObj.getStringWidth(String.valueOf(i));

				int x = guiLeft + 23 + 110 * i / 8 - w / 2;

				if (mouseX >= x - 2 && mouseX <= x + 7) {
					floorTime = i;
					return false;
				}
			}
		}
		if (mouseX >= guiLeft - 29 && mouseX <= guiLeft) {
			if (mouseY >= guiTop && mouseY <= guiTop + 28) {
				onMasterMode = false;
			} else if (mouseY + 28 >= guiTop && mouseY <= guiTop + 28 * 2) {
				onMasterMode = true;
			}
		}
		return false;
	}

	@Override
	public void keyTyped(char typedChar, int keyCode) throws IOException {
		dungeonLevelTextField.keyTyped(typedChar, keyCode);
	}

	private void drawSideButtons() {
		GlStateManager.enableDepth();
		GlStateManager.translate(0, 0, 5);
		if (onMasterMode) {
			Utils.drawPvSideButton(1, dungeonsModeIcons.get("master_catacombs"), true, getInstance());
		} else {
			Utils.drawPvSideButton(0, dungeonsModeIcons.get("catacombs"), true, getInstance());
		}
		GlStateManager.translate(0, 0, -3);

		GlStateManager.translate(0, 0, -2);
		if (!onMasterMode) {
			Utils.drawPvSideButton(1, dungeonsModeIcons.get("master_catacombs"), false, getInstance());
		} else {
			Utils.drawPvSideButton(0, dungeonsModeIcons.get("catacombs"), false, getInstance());
		}
		GlStateManager.disableDepth();
	}

	private void calculateFloorLevelXP() {
		JsonObject leveling = Constants.LEVELING;
		if (leveling == null) return;
		SkyblockProfiles.SkyblockProfile selectedProfile = getSelectedProfile();
		if (selectedProfile == null) return;
		ProfileViewer.Level levelObjCata = selectedProfile.getLevelingInfo().get("cosmetic_catacombs");

		try {
			dungeonLevelTextField.setCustomBorderColour(0xffffffff);
			floorLevelTo = Integer.parseInt(dungeonLevelTextField.getText());

			JsonArray levelingArray = leveling.getAsJsonArray("catacombs");

			float remaining = -((levelObjCata.level % 1) * levelObjCata.maxXpForLevel);

			for (int level = 0; level < Math.min(floorLevelTo, levelingArray.size()); level++) {
				if (level < Math.floor(levelObjCata.level)) {
					continue;
				}
				remaining += levelingArray.get(level).getAsFloat();
			}

			if (remaining < 0) {
				remaining = 0;
			}
			floorLevelToXP = (long) remaining;
		} catch (Exception e) {
			dungeonLevelTextField.setCustomBorderColour(0xffff0000);
		}
	}
}
