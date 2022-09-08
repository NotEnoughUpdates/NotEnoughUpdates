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
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.UUID;

public class DungeonPage extends GuiProfileViewerPage {

	private static final ResourceLocation pv_dung = new ResourceLocation("notenoughupdates:pv_dung.png");
	private static final ItemStack DEADBUSH = new ItemStack(Item.getItemFromBlock(Blocks.deadbush));
	private static final String[] dungSkillsName = { "Healer", "Mage", "Berserk", "Archer", "Tank" };
	private static final ItemStack[] BOSS_HEADS = new ItemStack[7];
	private static final ItemStack[] dungSkillsStack = {
		new ItemStack(Items.potionitem, 1, 16389),
		new ItemStack(Items.blaze_rod),
		new ItemStack(Items.iron_sword),
		new ItemStack(Items.bow),
		new ItemStack(Items.leather_chestplate),
	};
	private static final String[] bossFloorArr = { "Bonzo", "Scarf", "Professor", "Thorn", "Livid", "Sadan", "Necron" };
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
	private final HashMap<String, HashMap<String, ProfileViewer.Level>> levelObjClasseses = new HashMap<>();

	private final HashMap<String, ProfileViewer.Level> levelObjCatas = new HashMap<>();
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

		ProfileViewer.Profile profile = GuiProfileViewer.getProfile();
		String profileId = GuiProfileViewer.getProfileId();
		JsonObject hypixelInfo = profile.getHypixelProfile();
		if (hypixelInfo == null) return;
		JsonObject profileInfo = profile.getProfileInformation(profileId);
		if (profileInfo == null) return;

		JsonObject leveling = Constants.LEVELING;
		if (leveling == null) return;

		int sectionWidth = 110;

		String dungeonString = onMasterMode ? "master_catacombs" : "catacombs";

		Utils.renderShadowedString(
			EnumChatFormatting.RED + (onMasterMode ? "Master Mode" : "Catacombs"),
			(guiLeft + getInstance().sizeX / 2),
			guiTop + 5,
			sectionWidth
		);

		ProfileViewer.Level levelObjCata = levelObjCatas.get(profileId);
		//Catacombs level thingy
		{
			if (levelObjCata == null) {
				float cataXp = Utils.getElementAsFloat(Utils.getElement(profileInfo, "dungeons.dungeon_types.catacombs.experience"), 0);
				levelObjCata =
					ProfileViewer.getLevel(
						Utils.getElementOrDefault(leveling, "catacombs", new JsonArray()).getAsJsonArray(),
						cataXp,
						99,
						false
					);
				levelObjCata.totalXp = cataXp;
				levelObjCatas.put(profileId, levelObjCata);
			}

			String skillName = EnumChatFormatting.RED + "Catacombs";
			float level = levelObjCata.level;
			int levelFloored = (int) Math.floor(level);

			if (floorLevelTo == -1 && levelFloored >= 0) {
				dungeonLevelTextField.setText("" + (levelFloored + 1));
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
					(Utils.getElementAsFloat(Utils.getElement(profileInfo, "dungeons.dungeon_types.catacombs.tier_completions." + 5), 0)); //this can prob be done better
				float F6 =
					(Utils.getElementAsFloat(Utils.getElement(profileInfo, "dungeons.dungeon_types.catacombs.tier_completions." + 6), 0));
				float F7 =
					(Utils.getElementAsFloat(Utils.getElement(profileInfo, "dungeons.dungeon_types.catacombs.tier_completions." + 7), 0));
				if (F5 > 150) {
					F5 = 150;
				}
				if (F6 > 100) {
					F6 = 100;
				}
				if (F7 > 50) {
					F7 = 50;
				}
				float xpF5 = 2000 * (F5 / 100 + 1);
				float xpF6 = 4000 * (F6 / 100 + 1);
				float xpF7 = 20000 * (F7 / 100 + 1);
				if (!Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
					xpF5 *= 1.1;
					xpF6 *= 1.1;
					xpF7 *= 1.1;
				}

				long runsF5 = (int) Math.ceil(floorLevelToXP / xpF5);
				long runsF6 = (int) Math.ceil(floorLevelToXP / xpF6);
				long runsF7 = (int) Math.ceil(floorLevelToXP / xpF7);

				float timeF5 = Utils.getElementAsFloat(
					Utils.getElement(profileInfo, "dungeons.dungeon_types.catacombs.fastest_time_s_plus.5"),
					0
				);
				float timeF6 = Utils.getElementAsFloat(
					Utils.getElement(profileInfo, "dungeons.dungeon_types.catacombs.fastest_time_s_plus.6"),
					0
				);
				float timeF7 = Utils.getElementAsFloat(
					Utils.getElement(profileInfo, "dungeons.dungeon_types.catacombs.fastest_time_s_plus.7"),
					0
				);

				getInstance().tooltipToDisplay =
					Lists.newArrayList(
						String.format("# F5 Runs (%s xp) : %d", StringUtils.shortNumberFormat(xpF5), runsF5),
						String.format("# F6 Runs (%s xp) : %d", StringUtils.shortNumberFormat(xpF6), runsF6),
						String.format("# F7 Runs (%s xp) : %d", StringUtils.shortNumberFormat(xpF7), runsF7),
						""
					);
				boolean hasTime = false;
				if (timeF5 > 1000) {
					getInstance()
						.tooltipToDisplay.add(String.format("Expected Time (F5) : %s", Utils.prettyTime(runsF5 * (long) (timeF5 * 1.2))));
					hasTime = true;
				}
				if (timeF6 > 1000) {
					getInstance()
						.tooltipToDisplay.add(String.format("Expected Time (F6) : %s", Utils.prettyTime(runsF6 * (long) (timeF6 * 1.2))));
					hasTime = true;
				}
				if (timeF7 > 1000) {
					getInstance()
						.tooltipToDisplay.add(String.format("Expected Time (F7) : %s", Utils.prettyTime(runsF7 * (long) (timeF7 * 1.2))));
					hasTime = true;
				}
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
						.tooltipToDisplay.add("[Hold " + EnumChatFormatting.YELLOW + "CTRL" + EnumChatFormatting.GRAY + " to see details]");
				}
			}

			if (mouseX > x && mouseX < x + sectionWidth && mouseY > y + 16 && mouseY < y + 24 && onMasterMode) {
				float M3 =
					(
						Utils.getElementAsFloat(
							Utils.getElement(profileInfo, "dungeons.dungeon_types.master_catacombs.tier_completions." + 3),
							0
						)
					);
				float M4 =
					(
						Utils.getElementAsFloat(
							Utils.getElement(profileInfo, "dungeons.dungeon_types.master_catacombs.tier_completions." + 4),
							0
						)
					);
				float M5 =
					(
						Utils.getElementAsFloat(
							Utils.getElement(profileInfo, "dungeons.dungeon_types.master_catacombs.tier_completions." + 5),
							0
						)
					); //this can prob be done better
				float M6 =
					(
						Utils.getElementAsFloat(
							Utils.getElement(profileInfo, "dungeons.dungeon_types.master_catacombs.tier_completions." + 6),
							0
						)
					);
				if (M3 > 50) {
					M3 = 50;
				}
				if (M4 > 50) {
					M4 = 50;
				}
				if (M5 > 50) {
					M5 = 50;
				}
				if (M6 > 50) {
					M6 = 50;
				}
				float xpM3 = 36500 * (M3 / 100 + 1);
				float xpM4 = 48500 * (M4 / 100 + 1);
				float xpM5 = 70000 * (M5 / 100 + 1);
				float xpM6 = 100000 * (M6 / 100 + 1);
				//No clue if M3 or M4 xp values are right
				if (!Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
					xpM3 *= 1.1;
					xpM4 *= 1.1;
					xpM5 *= 1.1;
					xpM6 *= 1.1;
				}

				long runsM3 = (int) Math.ceil(floorLevelToXP / xpM3);
				long runsM4 = (int) Math.ceil(floorLevelToXP / xpM4);
				long runsM5 = (int) Math.ceil(floorLevelToXP / xpM5);
				long runsM6 = (int) Math.ceil(floorLevelToXP / xpM6);

				float timeM3 = Utils.getElementAsFloat(
					Utils.getElement(profileInfo, "dungeons.dungeon_types.master_catacombs.fastest_time_s_plus.3"),
					0
				);
				float timeM4 = Utils.getElementAsFloat(
					Utils.getElement(profileInfo, "dungeons.dungeon_types.master_catacombs.fastest_time_s_plus.4"),
					0
				);
				float timeM5 = Utils.getElementAsFloat(
					Utils.getElement(profileInfo, "dungeons.dungeon_types.master_catacombs.fastest_time_s_plus.5"),
					0
				);
				float timeM6 = Utils.getElementAsFloat(
					Utils.getElement(profileInfo, "dungeons.dungeon_types.master_catacombs.fastest_time_s_plus.6"),
					0
				);

				getInstance().tooltipToDisplay =
					Lists.newArrayList(
						String.format("# M3 Runs (%s xp) : %d", StringUtils.shortNumberFormat(xpM3), runsM3),
						String.format("# M4 Runs (%s xp) : %d", StringUtils.shortNumberFormat(xpM4), runsM4),
						String.format("# M5 Runs (%s xp) : %d", StringUtils.shortNumberFormat(xpM5), runsM5),
						String.format("# M6 Runs (%s xp) : %d", StringUtils.shortNumberFormat(xpM6), runsM6),
						""
					);
				boolean hasTime = false;
				if (timeM3 > 1000) {
					getInstance()
						.tooltipToDisplay.add(String.format("Expected Time (M3) : %s", Utils.prettyTime(runsM3 * (long) (timeM3 * 1.2))));
					hasTime = true;
				}
				if (timeM4 > 1000) {
					getInstance()
						.tooltipToDisplay.add(String.format("Expected Time (M4) : %s", Utils.prettyTime(runsM4 * (long) (timeM4 * 1.2))));
					hasTime = true;
				}
				if (timeM5 > 1000) {
					getInstance()
						.tooltipToDisplay.add(String.format("Expected Time (M5) : %s", Utils.prettyTime(runsM5 * (long) (timeM5 * 1.2))));
					hasTime = true;
				}
				if (timeM6 > 1000) {
					getInstance()
						.tooltipToDisplay.add(String.format("Expected Time (M6) : %s", Utils.prettyTime(runsM6 * (long) (timeM6 * 1.2))));
					hasTime = true;
				}
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
						.tooltipToDisplay.add("[Hold " + EnumChatFormatting.YELLOW + "CTRL" + EnumChatFormatting.GRAY + " to see details]");
				}
			}

			dungeonLevelTextField.setSize(20, 10);
			dungeonLevelTextField.render(x + 22, y + 29);
			int calcLen = fontRendererObj.getStringWidth("Calculate");
			Utils.renderShadowedString(EnumChatFormatting.WHITE + "Calculate", x + sectionWidth - 17 - calcLen / 2f, y + 30, 100);

			//Random stats

			float secrets = Utils.getElementAsFloat(Utils.getElement(hypixelInfo, "achievements.skyblock_treasure_hunter"), 0);
			float totalRunsF = 0;
			float totalRunsF5 = 0;
			for (int i = 1; i <= 7; i++) {
				float runs = Utils.getElementAsFloat(
					Utils.getElement(profileInfo, "dungeons.dungeon_types.catacombs.tier_completions." + i),
					0
				);
				totalRunsF += runs;
				if (i >= 5) {
					totalRunsF5 += runs;
				}
			}
			float totalRunsM = 0;
			float totalRunsM5 = 0;
			for (int i = 1; i <= 7; i++) {
				float runs = Utils.getElementAsFloat(
					Utils.getElement(profileInfo, "dungeons.dungeon_types.master_catacombs.tier_completions." + i),
					0
				);
				totalRunsM += runs;
				if (i >= 5) {
					totalRunsM5 += runs;
				}
			}
			float totalRuns = totalRunsF + totalRunsM;

			float mobKills;
			float mobKillsF = 0;
			for (int i = 1; i <= 7; i++) {
				float kills = Utils.getElementAsFloat(
					Utils.getElement(profileInfo, "dungeons.dungeon_types.catacombs.mobs_killed." + i),
					0
				);
				mobKillsF += kills;
			}
			float mobKillsM = 0;
			for (int i = 1; i <= 7; i++) {
				float kills = Utils.getElementAsFloat(
					Utils.getElement(profileInfo, "dungeons.dungeon_types.master_catacombs.mobs_killed." + i),
					0
				);
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
				EnumChatFormatting.WHITE + StringUtils.shortNumberFormat(secrets),
				x,
				miscTopY + 20,
				sectionWidth
			);
			Utils.renderAlignedString(
				EnumChatFormatting.YELLOW + "Secrets (/Run)  ",
				EnumChatFormatting.WHITE.toString() + (Math.round(secrets / Math.max(1, totalRuns) * 100) / 100f),
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
				int w = fontRendererObj.getStringWidth("" + i);

				int bx = x + sectionWidth * i / 8 - w / 2;

				boolean invert = i == floorTime;
				float uMin = 20 / 256f;
				float uMax = 29 / 256f;
				float vMin = 0 / 256f;
				float vMax = 11 / 256f;

				GlStateManager.color(1, 1, 1, 1);
				Minecraft.getMinecraft().getTextureManager().bindTexture(GuiProfileViewer.pv_elements);
				Utils.drawTexturedRect(
					bx - 2,
					y3 - 2,
					9,
					11,
					invert ? uMax : uMin,
					invert ? uMin : uMax,
					invert ? vMax : vMin,
					invert ? vMin : vMax,
					GL11.GL_NEAREST
				);

				Utils.renderShadowedString(EnumChatFormatting.WHITE.toString() + i, bx + w / 2, y3, 10);
			}

			float timeNorm = Utils.getElementAsFloat(
				Utils.getElement(profileInfo, "dungeons.dungeon_types." + dungeonString + ".fastest_time." + floorTime),
				0
			);
			float timeS = Utils.getElementAsFloat(
				Utils.getElement(profileInfo, "dungeons.dungeon_types." + dungeonString + ".fastest_time_s." + floorTime),
				0
			);
			float timeSPLUS = Utils.getElementAsFloat(
				Utils.getElement(profileInfo, "dungeons.dungeon_types." + dungeonString + ".fastest_time_s_plus." + floorTime),
				0
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
				float compl = Utils.getElementAsFloat(
					Utils.getElement(profileInfo, "dungeons.dungeon_types." + dungeonString + ".tier_completions." + i),
					0
				);

				if (BOSS_HEADS[i - 1] == null) {
					String textureLink = bossFloorHeads[i - 1];

					String b64Decoded = "{\"textures\":{\"SKIN\":{\"url\":\"http://textures.minecraft.net/texture/" + textureLink + "\"}}}";
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
					String.format(EnumChatFormatting.YELLOW + "%s (" + (onMasterMode ? "M" : "F") + "%d) ", bossFloorArr[i - 1], i),
					EnumChatFormatting.WHITE.toString() + (int) compl,
					x + 16,
					y + 18 + 20 * (i - 1),
					sectionWidth - 15
				);
			}
		}

		//Skills
		{
			int x = guiLeft + 298;
			int y = guiTop + 27;

			//Gui.drawRect(x, y, x+120, y+147, 0xffffffff);

			Utils.renderShadowedString(EnumChatFormatting.DARK_PURPLE + "Class Levels", x + sectionWidth / 2, y, sectionWidth);

			JsonElement activeClassElement = Utils.getElement(profileInfo, "dungeons.selected_dungeon_class");
			String activeClass = null;
			if (activeClassElement instanceof JsonPrimitive && ((JsonPrimitive) activeClassElement).isString()) {
				activeClass = activeClassElement.getAsString();
			}

			for (int i = 0; i < dungSkillsName.length; i++) {
				String skillName = dungSkillsName[i];

				HashMap<String, ProfileViewer.Level> levelObjClasses = levelObjClasseses.computeIfAbsent(profileId, k -> new HashMap<>());
				if (!levelObjClasses.containsKey(skillName)) {
					float cataXp = Utils.getElementAsFloat(
						Utils.getElement(profileInfo, "dungeons.player_classes." + skillName.toLowerCase() + ".experience"),
						0
					);
					ProfileViewer.Level levelObj = ProfileViewer.getLevel(
						Utils.getElementOrDefault(leveling, "catacombs", new JsonArray()).getAsJsonArray(),
						cataXp,
						50,
						false
					);
					levelObjClasses.put(skillName, levelObj);
				}

				String colour = EnumChatFormatting.WHITE.toString();
				if (skillName.toLowerCase().equals(activeClass)) {
					colour = EnumChatFormatting.GREEN.toString();
				}

				ProfileViewer.Level levelObj = levelObjClasses.get(skillName);

				getInstance()
					.renderXpBar(colour + skillName, dungSkillsStack[i], x, y + 20 + 29 * i, sectionWidth, levelObj, mouseX, mouseY);
			}
		}

		drawSideButtons();
	}

	@Override
	public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		FontRenderer fontRendererObj = Minecraft.getMinecraft().fontRendererObj;
		int guiLeft = GuiProfileViewer.getGuiLeft();
		int guiTop = GuiProfileViewer.getGuiTop();

		if (mouseX >= guiLeft + 50 && mouseX <= guiLeft + 70 && mouseY >= guiTop + 54 && mouseY <= guiTop + 64) {
			dungeonLevelTextField.mouseClicked(mouseX, mouseY, mouseButton);
		} else {
			dungeonLevelTextField.otherComponentClick();
		}

		int cW = fontRendererObj.getStringWidth("Calculate");
		if (mouseX >= guiLeft + 23 + 110 - 17 - cW && mouseX <= guiLeft + 23 + 110 - 17 && mouseY >= guiTop + 55 && mouseY <= guiTop + 65) {
			calculateFloorLevelXP();
		}

		int y = guiTop + 142;

		if (mouseY >= y - 2 && mouseY <= y + 9) {
			for (int i = 1; i <= 7; i++) {
				int w = fontRendererObj.getStringWidth("" + i);

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
			drawSideButton(1, dungeonsModeIcons.get("master_catacombs"), true);
		} else {
			drawSideButton(0, dungeonsModeIcons.get("catacombs"), true);
		}
		GlStateManager.translate(0, 0, -3);

		GlStateManager.translate(0, 0, -2);
		if (!onMasterMode) {
			drawSideButton(1, dungeonsModeIcons.get("master_catacombs"), false);
		} else {
			drawSideButton(0, dungeonsModeIcons.get("catacombs"), false);
		}
		GlStateManager.disableDepth();
	}

	private void drawSideButton(int yIndex, ItemStack itemStack, boolean pressed) {
		int guiLeft = GuiProfileViewer.getGuiLeft();
		int guiTop = GuiProfileViewer.getGuiTop();

		GlStateManager.disableLighting();
		GlStateManager.enableBlend();
		GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlStateManager.enableAlpha();
		GlStateManager.alphaFunc(516, 0.1F);

		int x = guiLeft - 28;
		int y = guiTop + yIndex * 28;

		float uMin = 193 / 256f;
		float uMax = 223 / 256f;
		float vMin = 200 / 256f;
		float vMax = 228 / 256f;
		if (pressed) {
			uMin = 224 / 256f;
			uMax = 1f;

			if (yIndex != 0) {
				vMin = 228 / 256f;
				vMax = 1f;
			}

			getInstance().renderBlurredBackground(getInstance().width, getInstance().height, x + 2, y + 2, 30, 28 - 4);
		} else {
			getInstance().renderBlurredBackground(getInstance().width, getInstance().height, x + 2, y + 2, 28 - 2, 28 - 4);
		}

		GlStateManager.disableLighting();
		GlStateManager.enableBlend();
		GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlStateManager.enableAlpha();
		GlStateManager.alphaFunc(516, 0.1F);

		Minecraft.getMinecraft().getTextureManager().bindTexture(GuiProfileViewer.pv_elements);

		Utils.drawTexturedRect(x, y, pressed ? 32 : 28, 28, uMin, uMax, vMin, vMax, GL11.GL_NEAREST);

		GlStateManager.enableDepth();
		Utils.drawItemStack(itemStack, x + 8, y + 7);
	}

	private void calculateFloorLevelXP() {
		JsonObject leveling = Constants.LEVELING;
		if (leveling == null) return;
		ProfileViewer.Level levelObjCata = levelObjCatas.get(GuiProfileViewer.getProfileId());
		if (levelObjCata == null) return;

		try {
			dungeonLevelTextField.setCustomBorderColour(0xffffffff);
			floorLevelTo = Integer.parseInt(dungeonLevelTextField.getText());

			JsonArray levelingArray = Utils.getElementOrDefault(leveling, "catacombs", new JsonArray()).getAsJsonArray();

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
