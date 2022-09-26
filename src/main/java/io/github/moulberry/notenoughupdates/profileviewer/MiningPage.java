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
import com.google.gson.JsonObject;
import io.github.moulberry.notenoughupdates.core.util.StringUtils;
import io.github.moulberry.notenoughupdates.util.Constants;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.function.Supplier;

public class MiningPage extends GuiProfileViewerPage {

	public static final ResourceLocation pv_mining = new ResourceLocation("notenoughupdates:pv_mining.png");
	private static final ItemStack iron_pick = new ItemStack(Items.iron_pickaxe);
	private final HashMap<String, ProfileViewer.Level> levelObjhotms = new HashMap<>();

	public MiningPage(GuiProfileViewer instance) {
		super(instance);
	}

	@Override
	public void drawPage(int mouseX, int mouseY, float partialTicks) {
		int guiLeft = GuiProfileViewer.getGuiLeft();
		int guiTop = GuiProfileViewer.getGuiTop();

		Minecraft.getMinecraft().getTextureManager().bindTexture(pv_mining);
		Utils.drawTexturedRect(guiLeft, guiTop, getInstance().sizeX, getInstance().sizeY, GL11.GL_NEAREST);

		ProfileViewer.Profile profile = GuiProfileViewer.getProfile();
		String profileId = GuiProfileViewer.getProfileId();
		JsonObject profileInfo = profile.getProfileInformation(profileId);
		if (profileInfo == null) return;

		float xStart = 22;
		float yStartTop = 27;

		int x = guiLeft + 23;
		int y = guiTop + 25;
		int sectionWidth = 110;
		JsonObject leveling = Constants.LEVELING;
		ProfileViewer.Level levelObjhotm = levelObjhotms.get(profileId);
		if (levelObjhotm == null) {
			float hotmXp = Utils.getElementAsFloat(Utils.getElement(profileInfo, "mining_core.experience"), 0);
			levelObjhotm =
				ProfileViewer.getLevel(Utils.getElementOrDefault(leveling, "HOTM", new JsonArray()).getAsJsonArray(), hotmXp, 7, false);
			levelObjhotms.put(profileId, levelObjhotm);
		}

		String skillName = EnumChatFormatting.RED + "HOTM";
		//The stats that show
		float mithrilPowder = Utils.getElementAsFloat(Utils.getElement(profileInfo, "mining_core.powder_mithril"), 0);
		float gemstonePowder = Utils.getElementAsFloat(Utils.getElement(profileInfo, "mining_core.powder_gemstone"), 0);
		float mithrilPowderTotal = Utils.getElementAsFloat(Utils.getElement(profileInfo, "mining_core.powder_spent_mithril"), 0);
		float gemstonePowderTotal = (Utils.getElementAsFloat(Utils.getElement(profileInfo, "mining_core.powder_spent_gemstone"), 0));
		String jadeCrystal =
			(Utils.getElementAsString(Utils.getElement(profileInfo, "mining_core.crystals.jade_crystal.state"), "Not Found"));
		float crystalPlacedAmount =
			(Utils.getElementAsFloat(Utils.getElement(profileInfo, "mining_core.crystals.jade_crystal.total_placed"), 0));
		String jadeCrystalString = "§c✖";
		String amethystCrystal =
			(Utils.getElementAsString(Utils.getElement(profileInfo, "mining_core.crystals.amethyst_crystal.state"), "Not Found"));
		String amethystCrystalString = "§c✖";
		String amberCrystal =
			(Utils.getElementAsString(Utils.getElement(profileInfo, "mining_core.crystals.amber_crystal.state"), "Not Found"));
		String amberCrystalString = "§c✖";
		String sapphireCrystal =
			(Utils.getElementAsString(Utils.getElement(profileInfo, "mining_core.crystals.sapphire_crystal.state"), "Not Found"));
		String sapphireCrystalString = "§c✖";
		String topazCrystal =
			(Utils.getElementAsString(Utils.getElement(profileInfo, "mining_core.crystals.topaz_crystal.state"), "Not Found"));
		String topazCrystalString = "§c✖";
		String jasperCrystal =
			(Utils.getElementAsString(Utils.getElement(profileInfo, "mining_core.crystals.jasper_crystal.state"), "Not Found"));
		String jasperCrystalString = "§c✖";
		String rubyCrystal =
			(Utils.getElementAsString(Utils.getElement(profileInfo, "mining_core.crystals.ruby_crystal.state"), "Not Found"));
		String rubyCrystalString = "§c✖";
		int miningFortune = ((Utils.getElementAsInt(Utils.getElement(profileInfo, "mining_core.nodes.mining_fortune"), 0)));
		int miningFortuneStat = ((Utils.getElementAsInt(Utils.getElement(profileInfo, "mining_core.nodes.mining_fortune"), 0)) * 5);
		int miningSpeed = ((Utils.getElementAsInt(Utils.getElement(profileInfo, "mining_core.nodes.mining_speed"), 0)));
		int miningSpeedStat = ((Utils.getElementAsInt(Utils.getElement(profileInfo, "mining_core.nodes.mining_speed"), 0)) * 20);
		int dailyPowder = ((Utils.getElementAsInt(Utils.getElement(profileInfo, "mining_core.nodes.daily_powder"), 0)));
		int dailyPowderStat = ((Utils.getElementAsInt(Utils.getElement(profileInfo, "mining_core.nodes.daily_powder"), 0)) * 36 + 364);
		int effMiner = ((Utils.getElementAsInt(Utils.getElement(profileInfo, "mining_core.nodes.efficient_miner"), 0)));
		float effMinerStat = (float) (
			(Utils.getElementAsFloat(Utils.getElement(profileInfo, "mining_core.nodes.efficient_miner"), 0)) * 0.4 + 10.4
		);
		float effMinerStat2 = (float) (
			(Utils.getElementAsFloat(Utils.getElement(profileInfo, "mining_core.nodes.efficient_miner"), 0)) * .06 + 0.31
		);
		int tittyInsane = ((Utils.getElementAsInt(Utils.getElement(profileInfo, "mining_core.nodes.titanium_insanium"), 0)));
		float tittyInsaneStat = (float) (
			(Utils.getElementAsFloat(Utils.getElement(profileInfo, "mining_core.nodes.titanium_insanium"), 0)) * .1 + 2
		);
		int luckofcave = ((Utils.getElementAsInt(Utils.getElement(profileInfo, "mining_core.nodes.random_event"), 0)));
		int luckofcaveStat = ((Utils.getElementAsInt(Utils.getElement(profileInfo, "mining_core.nodes.random_event"), 0)));
		int madMining = ((Utils.getElementAsInt(Utils.getElement(profileInfo, "mining_core.nodes.mining_madness"), 0)));
		int skyMall = ((Utils.getElementAsInt(Utils.getElement(profileInfo, "mining_core.nodes.daily_effect"), 0)));
		int goblinKiller = ((Utils.getElementAsInt(Utils.getElement(profileInfo, "mining_core.nodes.goblin_killer"), 0)));
		int seasonMine = ((Utils.getElementAsInt(Utils.getElement(profileInfo, "mining_core.nodes.mining_experience"), 0)));
		float seasonMineStat = (float) (
			(Utils.getElementAsFloat(Utils.getElement(profileInfo, "mining_core.nodes.mining_experience"), 0)) * 0.1 + 5
		);
		int quickForge = ((Utils.getElementAsInt(Utils.getElement(profileInfo, "mining_core.nodes.forge_time"), 0)));
		float quickForgeStat = (float) (
			(Utils.getElementAsFloat(Utils.getElement(profileInfo, "mining_core.nodes.forge_time"), 0)) * .5 + 10
		);
		int frontLoad = ((Utils.getElementAsInt(Utils.getElement(profileInfo, "mining_core.nodes.front_loaded"), 0)));
		int orbit = ((Utils.getElementAsInt(Utils.getElement(profileInfo, "mining_core.nodes.experience_orbs"), 0)));
		float orbitStat = (float) (
			(Utils.getElementAsFloat(Utils.getElement(profileInfo, "mining_core.nodes.experience_orbs"), 0)) * .01 + 0.2
		);
		int crystallized = ((Utils.getElementAsInt(Utils.getElement(profileInfo, "mining_core.nodes.fallen_star_bonus"), 0)));
		int crystallizedStat = ((Utils.getElementAsInt(Utils.getElement(profileInfo, "mining_core.nodes.fallen_star_bonus"), 0)) * 6 + 14);
		int professional = ((Utils.getElementAsInt(Utils.getElement(profileInfo, "mining_core.nodes.professional"), 0)));
		int professionalStat = ((Utils.getElementAsInt(Utils.getElement(profileInfo, "mining_core.nodes.professional"), 0)) * 5 + 50);
		int greatExplorer = ((Utils.getElementAsInt(Utils.getElement(profileInfo, "mining_core.nodes.great_explorer"), 0)));
		int greatExplorerStat = ((Utils.getElementAsInt(Utils.getElement(profileInfo, "mining_core.nodes.great_explorer"), 0)) * 4 + 16);
		int fortunate = ((Utils.getElementAsInt(Utils.getElement(profileInfo, "mining_core.nodes.fortunate"), 0)));
		int fortunateStat = ((Utils.getElementAsInt(Utils.getElement(profileInfo, "mining_core.nodes.fortunate"), 0)) * 4 + 20);
		int lonesomeMiner = ((Utils.getElementAsInt(Utils.getElement(profileInfo, "mining_core.nodes.lonesome_miner"), 0)));
		float lonesomeMinerStat = (float) (
			(Utils.getElementAsFloat(Utils.getElement(profileInfo, "mining_core.nodes.lonesome_miner"), 0)) * .5 + 5
		);
		int miningFortune2 = ((Utils.getElementAsInt(Utils.getElement(profileInfo, "mining_core.nodes.mining_fortune_2"), 0)));
		int miningFortune2Stat = ((Utils.getElementAsInt(Utils.getElement(profileInfo, "mining_core.nodes.mining_fortune_2"), 0)) * 5);
		int miningSpeed2 = ((Utils.getElementAsInt(Utils.getElement(profileInfo, "mining_core.nodes.mining_speed_2"), 0)));
		int miningSpeed2Stat = ((Utils.getElementAsInt(Utils.getElement(profileInfo, "mining_core.nodes.mining_speed_2"), 0)) * 40);
		int miningSpeedBoost = ((Utils.getElementAsInt(Utils.getElement(profileInfo, "mining_core.nodes.mining_speed_boost"), 0)));
		int veinSeeker = ((Utils.getElementAsInt(Utils.getElement(profileInfo, "mining_core.nodes.vein_seeker"), 0)));
		int powderBuff = ((Utils.getElementAsInt(Utils.getElement(profileInfo, "mining_core.nodes.powder_buff"), 0)));
		int potm = ((Utils.getElementAsInt(Utils.getElement(profileInfo, "mining_core.nodes.special_0"), 0)));
		int fortnite = ((Utils.getElementAsInt(Utils.getElement(profileInfo, "mining_core.nodes.precision_mining"), 0)));
		int starPowder = ((Utils.getElementAsInt(Utils.getElement(profileInfo, "mining_core.nodes.star_powder"), 0)));
		int pickoblus = ((Utils.getElementAsInt(Utils.getElement(profileInfo, "mining_core.nodes.pickaxe_toss"), 0)));
		int maniacMiner = ((Utils.getElementAsInt(Utils.getElement(profileInfo, "mining_core.nodes.maniac_miner"), 0)));

		if (effMinerStat2 < 1) {
			effMinerStat2 = 1;
		}
		int mole = ((Utils.getElementAsInt(Utils.getElement(profileInfo, "mining_core.nodes.mole"), 0)));
		float moleStat = (float) ((Utils.getElementAsFloat(Utils.getElement(profileInfo, "mining_core.nodes.mole"), 0)) * 0.051);
		double moleperkstat = (double) mole / 20 - 0.55 + 50;
		double moleperkstat2 = (double) Math.round(moleperkstat * 100) / 100;

		float output = Math.round((float) (moleperkstat2 % 1) * 100);
		if (output == 0) {
			output = 100;
		}

		//The logic for some of the stats
		if (Objects.equals(jadeCrystal, "NOT_FOUND")) {
			jadeCrystalString = "§c✖";
		} else if (Objects.equals(jadeCrystal, "FOUND")) {
			jadeCrystalString = "§a✔";
		}
		if (Objects.equals(amethystCrystal, "NOT_FOUND")) {
			amethystCrystalString = "§c✖";
		} else if (Objects.equals(amethystCrystal, "FOUND")) {
			amethystCrystalString = "§a✔";
		}
		if (Objects.equals(amberCrystal, "NOT_FOUND")) {
			amberCrystalString = "§c✖";
		} else if (Objects.equals(amberCrystal, "FOUND")) {
			amberCrystalString = "§a✔";
		}
		if (Objects.equals(sapphireCrystal, "NOT_FOUND")) {
			sapphireCrystalString = "§c✖";
		} else if (Objects.equals(sapphireCrystal, "FOUND")) {
			sapphireCrystalString = "§a✔";
		}
		if (Objects.equals(topazCrystal, "NOT_FOUND")) {
			topazCrystalString = "§c✖";
		} else if (Objects.equals(topazCrystal, "FOUND")) {
			topazCrystalString = "§a✔";
		}
		if (Objects.equals(jasperCrystal, "NOT_FOUND")) {
			jasperCrystalString = "§c✖";
		} else if (Objects.equals(jasperCrystal, "FOUND")) {
			jasperCrystalString = "§a✔";
		}
		if (Objects.equals(rubyCrystal, "NOT_FOUND")) {
			rubyCrystalString = "§c✖";
		} else if (Objects.equals(rubyCrystal, "FOUND")) {
			rubyCrystalString = "§a✔";
		}

		//The rendering of the stats
		//hotm level
		getInstance().renderXpBar(skillName, iron_pick, x, y, sectionWidth, levelObjhotm, mouseX, mouseY);
		//Powder
		Utils.renderAlignedString(
			EnumChatFormatting.DARK_GREEN + "Mithril Powder",
			EnumChatFormatting.WHITE + StringUtils.shortNumberFormat(mithrilPowder),
			guiLeft + xStart,
			guiTop + yStartTop + 24,
			115
		);
		Utils.renderAlignedString(
			EnumChatFormatting.LIGHT_PURPLE + "Gemstone Powder",
			EnumChatFormatting.WHITE + StringUtils.shortNumberFormat(gemstonePowder),
			guiLeft + xStart,
			guiTop + yStartTop + 44,
			115
		);
		Utils.renderAlignedString(
			EnumChatFormatting.DARK_GREEN + "Total Mithril Powder",
			EnumChatFormatting.WHITE + StringUtils.shortNumberFormat(mithrilPowderTotal + mithrilPowder),
			guiLeft + xStart,
			guiTop + yStartTop + 34,
			115
		);
		Utils.renderAlignedString(
			EnumChatFormatting.LIGHT_PURPLE + "Total Gemstone Powder",
			EnumChatFormatting.WHITE + StringUtils.shortNumberFormat(gemstonePowderTotal + gemstonePowder),
			guiLeft + xStart,
			guiTop + yStartTop + 54,
			115
		);
		//Crystals
		Utils.renderAlignedString(
			EnumChatFormatting.GREEN + "Jade Crystal:",
			EnumChatFormatting.WHITE + jadeCrystalString,
			guiLeft + xStart,
			guiTop + yStartTop + 74,
			110
		);
		Utils.renderAlignedString(
			EnumChatFormatting.GOLD + "Amber Crystal:",
			EnumChatFormatting.WHITE + amberCrystalString,
			guiLeft + xStart,
			guiTop + yStartTop + 84,
			110
		);
		Utils.renderAlignedString(
			EnumChatFormatting.DARK_PURPLE + "Amethyst Crystal:",
			EnumChatFormatting.WHITE + amethystCrystalString,
			guiLeft + xStart,
			guiTop + yStartTop + 94,
			110
		);
		Utils.renderAlignedString(
			EnumChatFormatting.AQUA + "Sapphire Crystal:",
			EnumChatFormatting.WHITE + sapphireCrystalString,
			guiLeft + xStart,
			guiTop + yStartTop + 104,
			110
		);
		Utils.renderAlignedString(
			EnumChatFormatting.YELLOW + "Topaz Crystal:",
			EnumChatFormatting.WHITE + topazCrystalString,
			guiLeft + xStart,
			guiTop + yStartTop + 114,
			110
		);
		Utils.renderAlignedString(
			EnumChatFormatting.LIGHT_PURPLE + "Jasper Crystal:",
			EnumChatFormatting.WHITE + jasperCrystalString,
			guiLeft + xStart,
			guiTop + yStartTop + 124,
			110
		);
		Utils.renderAlignedString(
			EnumChatFormatting.RED + "Ruby Crystal:",
			EnumChatFormatting.WHITE + rubyCrystalString,
			guiLeft + xStart,
			guiTop + yStartTop + 134,
			110
		);
		Utils.renderAlignedString(
			EnumChatFormatting.BLUE + "Total Placed Crystals:",
			EnumChatFormatting.WHITE + StringUtils.shortNumberFormat(crystalPlacedAmount),
			guiLeft + xStart,
			guiTop + yStartTop + 149,
			110
		);

		//hotm render
		//Pain

		renderHotmPerk(
			miningSpeed,
			(int) (guiLeft + xStart + 255),
			(int) (guiTop + yStartTop + 138),
			mouseX,
			mouseY,
			() ->
				miningSpeed != 50 && miningSpeed != 0
					? Lists.newArrayList(
						"Mining Speed",
						EnumChatFormatting.GRAY + "Level " + miningSpeed + EnumChatFormatting.DARK_GRAY + "/50",
						"",
						EnumChatFormatting.GRAY +
						"Grants " +
						EnumChatFormatting.GREEN +
						"+" +
						miningSpeedStat +
						EnumChatFormatting.GOLD +
						" ⸕ Mining",
						EnumChatFormatting.GOLD + "Speed" + EnumChatFormatting.GRAY + ".",
						"",
						EnumChatFormatting.GRAY + "Cost",
						EnumChatFormatting.DARK_GREEN +
						"" +
						GuiProfileViewer.numberFormat.format(Math.pow(miningSpeed + 2, 3)) +
						" Mithril Powder"
					)
					: Lists.newArrayList(
						"Mining Speed",
						EnumChatFormatting.GRAY + "Level " + miningSpeed + EnumChatFormatting.DARK_GRAY + "/50",
						"",
						EnumChatFormatting.GRAY +
						"Grants " +
						EnumChatFormatting.GREEN +
						"+" +
						miningSpeedStat +
						EnumChatFormatting.GOLD +
						" ⸕ Mining",
						EnumChatFormatting.GOLD + "Speed" + EnumChatFormatting.GRAY + "."
					),
			50
		);

		renderHotmPerk(
			miningFortune,
			(int) (guiLeft + xStart + 255),
			(int) (guiTop + yStartTop + 114),
			mouseX,
			mouseY,
			() ->
				miningFortune != 0 && miningFortune != 50
					? Lists.newArrayList(
						"Mining Fortune",
						EnumChatFormatting.GRAY + "Level " + miningFortune + EnumChatFormatting.DARK_GRAY + "/50",
						"",
						EnumChatFormatting.GRAY +
						"Grants " +
						EnumChatFormatting.GREEN +
						"+" +
						miningFortuneStat +
						EnumChatFormatting.GOLD +
						" ☘ Mining",
						EnumChatFormatting.GOLD + "Fortune" + EnumChatFormatting.GRAY + ".",
						"",
						EnumChatFormatting.GRAY + "Cost",
						EnumChatFormatting.DARK_GREEN +
						"" +
						GuiProfileViewer.numberFormat.format(Math.pow(miningFortune + 2, 3)) +
						" Mithril Powder"
					)
					: Lists.newArrayList(
						"Mining Fortune",
						EnumChatFormatting.GRAY + "Level " + miningFortune + EnumChatFormatting.DARK_GRAY + "/50",
						"",
						EnumChatFormatting.GRAY +
						"Grants " +
						EnumChatFormatting.GREEN +
						"+" +
						miningFortuneStat +
						EnumChatFormatting.GOLD +
						" ☘ Mining",
						EnumChatFormatting.GOLD + "Fortune" + EnumChatFormatting.GRAY + "."
					),
			50
		);

		renderHotmPerk(
			tittyInsane,
			(int) (guiLeft + xStart + 231),
			(int) (guiTop + yStartTop + 114),
			mouseX,
			mouseY,
			() ->
				tittyInsane != 0 && tittyInsane != 50
					? Lists.newArrayList(
						"Titanium Insanium",
						EnumChatFormatting.GRAY + "Level " + tittyInsane + EnumChatFormatting.DARK_GRAY + "/50",
						"",
						EnumChatFormatting.GRAY + "When mining Mithril Ore, you",
						EnumChatFormatting.GRAY +
						"have a " +
						EnumChatFormatting.GREEN +
						tittyInsaneStat +
						"% " +
						EnumChatFormatting.GRAY +
						"chance to",
						EnumChatFormatting.GRAY + "convert the block into Titanium",
						EnumChatFormatting.GRAY + "Ore.",
						"",
						EnumChatFormatting.GRAY + "Cost",
						EnumChatFormatting.DARK_GREEN +
						"" +
						GuiProfileViewer.numberFormat.format((int) Math.pow(tittyInsane + 2, 3)) +
						" Mithril Powder"
					)
					: Lists.newArrayList(
						"Titanium Insanium",
						EnumChatFormatting.GRAY + "Level " + tittyInsane + EnumChatFormatting.DARK_GRAY + "/50",
						"",
						EnumChatFormatting.GRAY + "When mining Mithril Ore, you",
						EnumChatFormatting.GRAY +
						"have a " +
						EnumChatFormatting.GREEN +
						tittyInsaneStat +
						"% " +
						EnumChatFormatting.GRAY +
						"chance to",
						EnumChatFormatting.GRAY + "convert the block into Titanium",
						EnumChatFormatting.GRAY + "Ore."
					),
			50
		);

		renderPickaxeAbility(
			miningSpeedBoost,
			(int) (guiLeft + xStart + 207),
			(int) (guiTop + yStartTop + 114),
			mouseX,
			mouseY,
			() ->
				potm == 0
					? Lists.newArrayList( // Peak of the mountain == 0
						"Mining Speed Boost",
						"",
						EnumChatFormatting.GRAY + "Pickaxe Ability: Mining Speed Boost",
						EnumChatFormatting.GRAY + "Grants " + EnumChatFormatting.GREEN + "200% " + EnumChatFormatting.GOLD + "⸕ Mining",
						EnumChatFormatting.GOLD +
						"Speed " +
						EnumChatFormatting.GRAY +
						"for " +
						EnumChatFormatting.GREEN +
						"15s" +
						EnumChatFormatting.GRAY,
						EnumChatFormatting.DARK_GRAY + "Cooldown: " + EnumChatFormatting.GREEN + "120s"
					)
					: Lists.newArrayList( // Peak of the mountain > 0
						"Mining Speed Boost",
						"",
						EnumChatFormatting.GRAY + "Pickaxe Ability: Mining Speed Boost",
						EnumChatFormatting.GRAY + "Grants " + EnumChatFormatting.GREEN + "300% " + EnumChatFormatting.GOLD + "⸕ Mining",
						EnumChatFormatting.GOLD +
						"Speed " +
						EnumChatFormatting.GRAY +
						"for " +
						EnumChatFormatting.GREEN +
						"20s" +
						EnumChatFormatting.GRAY,
						EnumChatFormatting.DARK_GRAY + "Cooldown: " + EnumChatFormatting.GREEN + "120s"
					)
		);

		renderPickaxeAbility(
			veinSeeker,
			(int) (guiLeft + xStart + 183),
			(int) (guiTop + yStartTop + 18),
			mouseX,
			mouseY,
			() ->
				Lists.newArrayList(
					"Vein Seeker",
					"",
					"§6Pickaxe Ability: Vein Seeker",
					"§7Points in the direction of the",
					"§7nearest vein and grants §a+§a3§7",
					"§7§6Mining Spread §7for §a14s§7§7.",
					"§8Cooldown: §a60s"
				)
		);

		renderHotmPerk(
			luckofcave,
			(int) (guiLeft + xStart + 207),
			(int) (guiTop + yStartTop + 90),
			mouseX,
			mouseY,
			() ->
				luckofcave != 0 && luckofcave != 45
					? Lists.newArrayList(
						"Luck of the Cave",
						"§7Level " + luckofcave + EnumChatFormatting.DARK_GRAY + "/45",
						"",
						"§7Increases the chance for you to",
						"§7trigger rare occurrences im",
						"§2Dwarven Mines " + EnumChatFormatting.GRAY + "by " + EnumChatFormatting.GREEN + luckofcaveStat + "%§7.",
						"",
						EnumChatFormatting.GRAY + "Cost",
						EnumChatFormatting.DARK_GREEN +
						"" +
						GuiProfileViewer.numberFormat.format((int) Math.pow(luckofcave + 2, 3.07)) +
						" Mithril Powder"
					)
					: Lists.newArrayList(
						"Luck of the Cave",
						"§7Level " + luckofcave + EnumChatFormatting.DARK_GRAY + "/45",
						"",
						"§7Increases the chance for you to",
						"§7trigger rare occurrences im",
						"§2Dwarven Mines " + EnumChatFormatting.GRAY + "by " + EnumChatFormatting.GREEN + luckofcaveStat + "%§7."
					),
			45
		);

		renderHotmPerk(
			dailyPowder,
			(int) (guiLeft + xStart + 255),
			(int) (guiTop + yStartTop + 90),
			mouseX,
			mouseY,
			() ->
				dailyPowder != 0 && dailyPowder != 100
					? Lists.newArrayList(
						"Daily Powder",
						EnumChatFormatting.GRAY + "Level " + dailyPowder + EnumChatFormatting.DARK_GRAY + "/100",
						"",
						EnumChatFormatting.GRAY +
						"Gains " +
						EnumChatFormatting.GREEN +
						dailyPowderStat +
						" Powder" +
						EnumChatFormatting.GRAY +
						" from the",
						EnumChatFormatting.GRAY + "first ore you mine every day.",
						EnumChatFormatting.GRAY + "Works for all Powder types.",
						"",
						EnumChatFormatting.GRAY + "Cost",
						EnumChatFormatting.DARK_GREEN + "" + (200 + ((dailyPowder) * 18)) + " Mithril Powder"
					)
					: Lists.newArrayList(
						"Daily Powder",
						EnumChatFormatting.GRAY + "Level " + dailyPowder + EnumChatFormatting.DARK_GRAY + "/100",
						"",
						EnumChatFormatting.GRAY +
						"Gains " +
						EnumChatFormatting.GREEN +
						dailyPowderStat +
						" Powder" +
						EnumChatFormatting.GRAY +
						" from the",
						EnumChatFormatting.GRAY + "first ore you mine every day.",
						EnumChatFormatting.GRAY + "Works for all Powder types."
					),
			100
		);

		float finalEffMinerStat2 = effMinerStat2;
		renderHotmPerk(
			effMiner,
			(int) (guiLeft + xStart + 255),
			(int) (guiTop + yStartTop + 66),
			mouseX,
			mouseY,
			() ->
				effMiner != 0 && effMiner != 100
					? Lists.newArrayList(
						"Efficient Miner",
						EnumChatFormatting.GRAY + "Level " + effMiner + EnumChatFormatting.DARK_GRAY + "/100",
						"",
						EnumChatFormatting.GRAY + "When mining ores, you have a",
						EnumChatFormatting.GREEN +
						"" +
						effMinerStat +
						"%" +
						EnumChatFormatting.GRAY +
						" chance to mine " +
						EnumChatFormatting.GREEN +
						Math.round(finalEffMinerStat2),
						EnumChatFormatting.GRAY + "adjacent ores.",
						"",
						EnumChatFormatting.GRAY + "Cost",
						EnumChatFormatting.DARK_GREEN +
						"" +
						GuiProfileViewer.numberFormat.format((int) Math.pow(effMiner + 2, 2.6)) +
						" Mithril Powder"
					)
					: Lists.newArrayList(
						"Efficient Miner",
						EnumChatFormatting.GRAY + "Level " + effMiner + EnumChatFormatting.DARK_GRAY + "/100",
						"",
						EnumChatFormatting.GRAY + "When mining ores, you have a",
						EnumChatFormatting.GREEN +
						"" +
						effMinerStat +
						"%" +
						EnumChatFormatting.GRAY +
						" chance to mine " +
						EnumChatFormatting.GREEN +
						Math.round(finalEffMinerStat2),
						EnumChatFormatting.GRAY + "adjacent ores."
					),
			100
		);

		renderHotmPerk(
			potm,
			(int) (guiLeft + xStart + 255),
			(int) (guiTop + yStartTop + 42),
			mouseX,
			mouseY,
			() -> {
				switch (potm) {
					case 0:
						return Lists.newArrayList(
							EnumChatFormatting.RED + "Peak of the Mountain",
							EnumChatFormatting.GRAY + "Level " + potm + EnumChatFormatting.DARK_GRAY + "/5",
							"",
							EnumChatFormatting.GRAY + "Cost",
							EnumChatFormatting.DARK_GREEN + "50000 Mithril Powder"
						);
					case 1:
						return Lists.newArrayList(
							EnumChatFormatting.YELLOW + "Peak of the Mountain",
							EnumChatFormatting.GRAY + "Level " + potm + EnumChatFormatting.DARK_GRAY + "/5",
							"",
							"§7§8+§c1 Pickaxe Ability Level",
							"§7§8+§51 Token of the Mountain",
							"",
							EnumChatFormatting.GRAY + "Cost",
							EnumChatFormatting.DARK_GREEN + "50000 Mithril Powder"
						);
					case 2:
						return Lists.newArrayList(
							EnumChatFormatting.YELLOW + "Peak of the Mountain",
							EnumChatFormatting.GRAY + "Level " + potm + EnumChatFormatting.DARK_GRAY + "/5",
							"",
							"§7§8+§c1 Pickaxe Ability Level",
							"§7§8+§51 Token of the Mountain",
							"§7§8+§a1 Forge Slot",
							"",
							EnumChatFormatting.GRAY + "Cost",
							EnumChatFormatting.DARK_GREEN + "75000 Mithril Powder"
						);
					case 3:
						return Lists.newArrayList(
							EnumChatFormatting.YELLOW + "Peak of the Mountain",
							EnumChatFormatting.GRAY + "Level " + potm + EnumChatFormatting.DARK_GRAY + "/5",
							"",
							"§7§8+§c1 Pickaxe Ability Level",
							"§7§8+§51 Token of the Mountain",
							"§7§8+§a1 Forge Slot",
							"§7§8+§a1 Commission Slot",
							"",
							EnumChatFormatting.GRAY + "Cost",
							EnumChatFormatting.DARK_GREEN + "100000 Mithril Powder"
						);
					case 4:
						return Lists.newArrayList(
							EnumChatFormatting.YELLOW + "Peak of the Mountain",
							EnumChatFormatting.GRAY + "Level " + potm + EnumChatFormatting.DARK_GRAY + "/5",
							"",
							"§7§8+§c1 Pickaxe Ability Level",
							"§7§8+§51 Token of the Mountain",
							"§7§8+§a1 Forge Slot",
							"§7§8+§a1 Commission Slot",
							"§7§8+§21 Mithril Powder §7when",
							"§7mining §fMithril",
							"",
							EnumChatFormatting.GRAY + "Cost",
							EnumChatFormatting.DARK_GREEN + "125000 Mithril Powder"
						);
					case 5:
						return Lists.newArrayList(
							EnumChatFormatting.GREEN + "Peak of the Mountain",
							EnumChatFormatting.GRAY + "Level " + potm + EnumChatFormatting.DARK_GRAY + "/5",
							"",
							"§7§8+§c1 Pickaxe Ability Level",
							"§7§8+§51 Token of the Mountain",
							"§7§8+§a1 Forge Slot",
							"§7§8+§a1 Commission Slot",
							"§7§8+§21 Mithril Powder §7when",
							"§7mining §fMithril",
							"§7§8+§51 Token of the Mountain"
						);
				}
				return null;
			},
			potm > 0 ? new ItemStack(Blocks.redstone_block) : new ItemStack(Blocks.bedrock),
			true // A redstone block or bedrock is being rendered, so standard GUI item lighting needs to be enabled.
		);

		float finalOutput = output;
		renderHotmPerk(
			mole,
			(int) (guiLeft + xStart + 255),
			(int) (guiTop + yStartTop + 18),
			mouseX,
			mouseY,
			() ->
				mole != 0 && mole != 190
					? Lists.newArrayList(
						"Mole",
						EnumChatFormatting.GRAY + "Level " + mole + EnumChatFormatting.DARK_GRAY + "/190",
						"",
						EnumChatFormatting.GRAY + "When mining hard stone, you have",
						EnumChatFormatting.GRAY +
						"a " +
						EnumChatFormatting.GREEN +
						finalOutput +
						"% " +
						EnumChatFormatting.GRAY +
						"chance to mine " +
						EnumChatFormatting.GREEN,
						EnumChatFormatting.GREEN +
						"" +
						Math.round(moleStat) +
						EnumChatFormatting.GRAY +
						" adjacent hard stone block" +
						(moleStat == 1.0 ? "." : "s."),
						"",
						EnumChatFormatting.GRAY + "Cost",
						EnumChatFormatting.LIGHT_PURPLE +
						"" +
						GuiProfileViewer.numberFormat.format((int) Math.pow(mole + 2, 2.2)) +
						" Gemstone Powder"
					)
					: Lists.newArrayList(
						"Mole",
						EnumChatFormatting.GRAY + "Level " + mole + EnumChatFormatting.DARK_GRAY + "/190",
						"",
						EnumChatFormatting.GRAY + "When mining hard stone, you have",
						EnumChatFormatting.GRAY +
						"a " +
						EnumChatFormatting.GREEN +
						finalOutput +
						"% " +
						EnumChatFormatting.GRAY +
						"chance to mine " +
						EnumChatFormatting.GREEN,
						EnumChatFormatting.GREEN +
						"" +
						Math.round(moleStat) +
						EnumChatFormatting.GRAY +
						" adjacent hard stone block" +
						(moleStat == 1.0 ? "." : "s.")
					),
			190
		);

		renderHotmPerk(
			powderBuff,
			(int) (guiLeft + xStart + 255),
			(int) (guiTop + yStartTop - 6),
			mouseX,
			mouseY,
			() ->
				powderBuff != 0 && powderBuff != 50
					? Lists.newArrayList(
						"Powder Buff",
						EnumChatFormatting.GRAY + "Level " + powderBuff + EnumChatFormatting.DARK_GRAY + "/50",
						"",
						EnumChatFormatting.GRAY +
						"Gain " +
						EnumChatFormatting.GREEN +
						powderBuff +
						"% " +
						EnumChatFormatting.GRAY +
						"more Mithril",
						EnumChatFormatting.GRAY + "Powder and Gemstone Powder§7.",
						"",
						EnumChatFormatting.GRAY + "Cost",
						EnumChatFormatting.LIGHT_PURPLE +
						"" +
						GuiProfileViewer.numberFormat.format((int) Math.pow(powderBuff + 2, 3.2)) +
						" Gemstone Powder"
					)
					: Lists.newArrayList(
						"Powder Buff",
						EnumChatFormatting.GRAY + "Level " + powderBuff + EnumChatFormatting.DARK_GRAY + "/50",
						"",
						EnumChatFormatting.GRAY +
						"Gain " +
						EnumChatFormatting.GREEN +
						powderBuff +
						"% " +
						EnumChatFormatting.GRAY +
						"more Mithril",
						EnumChatFormatting.GRAY + "Powder and Gemstone Powder§7."
					),
			50
		);

		renderHotmPerk(
			skyMall,
			(int) (guiLeft + xStart + 183),
			(int) (guiTop + yStartTop + 66),
			mouseX,
			mouseY,
			() ->
				Lists.newArrayList(
					"Sky Mall",
					"§7Every SkyBlock day, you receive",
					"§7a random buff in the §2Dwarven",
					"§2Mines§7.",
					"",
					"§7Possible Buffs",
					"§8 ■ §7Gain §a+100 §6⸕ Mining Speed.",
					"§8 ■ §7Gain §a+50 §6☘ Mining Fortune.",
					"§8 ■ §7Gain §a+15% §7chance to gain",
					"    §7extra Powder while mining.",
					"§8 ■ §7Reduce Pickaxe Ability cooldown",
					"    §7by §a20%",
					"§8 ■ §7§a10x §7chance to find Goblins",
					"    §7while mining.",
					"§8 ■ §7Gain §a5x §9Titanium §7drops."
				),
			new ItemStack(skyMall > 0 ? Items.diamond : Items.coal)
		);

		renderHotmPerk(
			goblinKiller,
			(int) (guiLeft + xStart + 207),
			(int) (guiTop + yStartTop + 42),
			mouseX,
			mouseY,
			() ->
				Lists.newArrayList(
					"Goblin Killer",
					"§7Killing a §6Golden Goblin",
					"§6§7gives §2200 §7extra §2Mithril",
					"§2Powder§7, while killing other",
					"§7Goblins gives some based on",
					"§7their wits."
				),
			new ItemStack(goblinKiller > 0 ? Items.diamond : Items.coal)
		);

		renderHotmPerk(
			seasonMine,
			(int) (guiLeft + xStart + 231),
			(int) (guiTop + yStartTop + 66),
			mouseX,
			mouseY,
			() ->
				seasonMine != 0 && seasonMine != 100
					? Lists.newArrayList(
						"Seasoned Mineman",
						"§7Level " + seasonMine + "§8/100",
						"",
						"§7Grants §3+" + EnumChatFormatting.DARK_AQUA + seasonMineStat + "☯ Mining Wisdom§7.",
						"",
						EnumChatFormatting.GRAY + "Cost",
						EnumChatFormatting.DARK_GREEN +
						"" +
						GuiProfileViewer.numberFormat.format((int) Math.pow(seasonMine + 2, 2.3)) +
						" Mithril Powder"
					)
					: Lists.newArrayList(
						"Seasoned Mineman",
						"§7Level " + seasonMine + "§8/100",
						"",
						"§7Grants §3+" + EnumChatFormatting.DARK_AQUA + seasonMineStat + "☯ Mining Wisdom§7."
					),
			100
		);

		renderHotmPerk(
			madMining,
			(int) (guiLeft + xStart + 207),
			(int) (guiTop + yStartTop + 66),
			mouseX,
			mouseY,
			() -> Lists.newArrayList("Mining Madness", "§7Grants §a+50 §6⸕ Mining Speed", "§7and §6☘ Mining Fortune§7."),
			new ItemStack(madMining > 0 ? Items.diamond : Items.coal)
		);

		renderHotmPerk(
			lonesomeMiner,
			(int) (guiLeft + xStart + 207),
			(int) (guiTop + yStartTop + 18),
			mouseX,
			mouseY,
			() ->
				lonesomeMiner != 0 && lonesomeMiner != 45
					? Lists.newArrayList(
						"Lonesome Miner",
						"§7Level " + lonesomeMiner + EnumChatFormatting.DARK_GRAY + "/45",
						"",
						"§7Increases §c❁ Strength, §9☣ Crit",
						"§9Chance, §9☠ Crit Damage, §a❈",
						"§aDefense, and §c❤ Health",
						"§c§7statistics gain by " + EnumChatFormatting.GREEN + lonesomeMinerStat + "%§7",
						"§7while in the Crystal Hollows.",
						"",
						EnumChatFormatting.GRAY + "Cost",
						EnumChatFormatting.LIGHT_PURPLE +
						"" +
						GuiProfileViewer.numberFormat.format((int) Math.pow(lonesomeMiner + 2, 3.07)) +
						" Gemstone Powder"
					)
					: Lists.newArrayList(
						"Lonesome Miner",
						"§7Level " + lonesomeMiner + EnumChatFormatting.DARK_GRAY + "/45",
						"",
						"§7Increases §c❁ Strength, §9☣ Crit",
						"§9Chance, §9☠ Crit Damage, §a❈",
						"§aDefense, and §c❤ Health",
						"§c§7statistics gain by " + EnumChatFormatting.GREEN + lonesomeMinerStat + "%§7"
					),
			45
		);

		renderHotmPerk(
			professional,
			(int) (guiLeft + xStart + 231),
			(int) (guiTop + yStartTop + 18),
			mouseX,
			mouseY,
			() ->
				professional != 0 && professional != 140
					? Lists.newArrayList(
						"Professional",
						"§7Level " + professional + EnumChatFormatting.DARK_GRAY + "/140",
						"",
						"§7Gain §a+" + professionalStat + "§6 ⸕ Mining",
						"§6Speed§7 when mining Gemstones.",
						"",
						EnumChatFormatting.GRAY + "Cost",
						EnumChatFormatting.LIGHT_PURPLE +
						"" +
						GuiProfileViewer.numberFormat.format((int) Math.pow(professional + 2, 2.3)) +
						" Gemstone Powder"
					)
					: Lists.newArrayList(
						"Professional",
						"§7Level " + professional + EnumChatFormatting.DARK_GRAY + "/140",
						"",
						"§7Gain §a+" + professionalStat + "§6 ⸕ Mining",
						"§6Speed§7 when mining Gemstones."
					),
			140
		);

		renderHotmPerk(
			miningSpeed2,
			(int) (guiLeft + xStart + 207),
			(int) (guiTop + yStartTop - 6),
			mouseX,
			mouseY,
			() ->
				miningSpeed2 != 0 && miningSpeed2 != 50
					? Lists.newArrayList(
						"Mining Speed 2",
						"§7Level " + miningSpeed2 + EnumChatFormatting.DARK_GRAY + "/50",
						"",
						"§7Grants " + EnumChatFormatting.GREEN + "+" + miningSpeed2Stat + EnumChatFormatting.GOLD + " ⸕ Mining",
						"§6Speed§7.",
						"",
						EnumChatFormatting.GRAY + "Cost",
						EnumChatFormatting.LIGHT_PURPLE +
						"" +
						GuiProfileViewer.numberFormat.format(Math.pow(miningSpeed2 + 2, 3)) +
						" Gemstone Powder"
					)
					: Lists.newArrayList(
						"Mining Speed 2",
						"§7Level " + miningSpeed2 + EnumChatFormatting.DARK_GRAY + "/50",
						"",
						"§7Grants " + EnumChatFormatting.GREEN + "+" + miningSpeed2Stat + EnumChatFormatting.GOLD + " ⸕ Mining",
						"§6Speed§7."
					),
			50
		);

		renderHotmPerk(
			quickForge,
			(int) (guiLeft + xStart + 279),
			(int) (guiTop + yStartTop + 114),
			mouseX,
			mouseY,
			() ->
				quickForge != 0 && quickForge != 20
					? Lists.newArrayList(
						"Quick Forge",
						"§7Level " + quickForge + EnumChatFormatting.DARK_GRAY + "/20",
						"",
						"§7Decreases the time it takes to",
						"§7forge by §a" + (quickForgeStat < 20 ? quickForgeStat : 30) + "%§7.",
						"",
						EnumChatFormatting.GRAY + "Cost",
						EnumChatFormatting.DARK_GREEN +
						"" +
						GuiProfileViewer.numberFormat.format((int) Math.pow(quickForge + 2, 4)) +
						" Mithril Powder"
					)
					: Lists.newArrayList(
						"Quick Forge",
						"§7Level " + quickForge + EnumChatFormatting.DARK_GRAY + "/20",
						"",
						"§7Decreases the time it takes to",
						"§7forge by §a" + (quickForgeStat < 20 ? quickForgeStat : 30) + "%§7."
					),
			20
		);

		renderHotmPerk(
			fortunate,
			(int) (guiLeft + xStart + 279),
			(int) (guiTop + yStartTop + 18),
			mouseX,
			mouseY,
			() ->
				fortunate != 0 && fortunate != 20
					? Lists.newArrayList(
						"Fortunate",
						"§7Level " + fortunate + EnumChatFormatting.DARK_GRAY + "/20",
						"",
						"§7Gain " + EnumChatFormatting.GREEN + "+" + fortunateStat + " §6☘ Mining",
						"§6Fortune§7 when mining Gemstone.",
						"",
						EnumChatFormatting.GRAY + "Cost",
						EnumChatFormatting.DARK_GREEN +
						"" +
						GuiProfileViewer.numberFormat.format((int) Math.pow(fortunate + 2, 3.05)) +
						" Mithril Powder"
					)
					: Lists.newArrayList(
						"Fortunate",
						"§7Level " + fortunate + EnumChatFormatting.DARK_GRAY + "/20",
						"",
						"§7Gain " + EnumChatFormatting.GREEN + "+" + fortunateStat + " §6☘ Mining",
						"§6Fortune§7 when mining Gemstone."
					),
			20
		);

		renderHotmPerk(
			greatExplorer,
			(int) (guiLeft + xStart + 303),
			(int) (guiTop + yStartTop + 18),
			mouseX,
			mouseY,
			() ->
				greatExplorer != 0 && greatExplorer != 20
					? Lists.newArrayList(
						"Great Explorer",
						"§7Level " + greatExplorer + EnumChatFormatting.DARK_GRAY + "/20",
						"",
						"§7Grants " + EnumChatFormatting.GREEN + "+" + greatExplorerStat + "% " + EnumChatFormatting.GRAY + "chance to",
						"§7find treasure.",
						"",
						EnumChatFormatting.GRAY + "Cost",
						EnumChatFormatting.LIGHT_PURPLE +
						"" +
						GuiProfileViewer.numberFormat.format((int) Math.pow(greatExplorer + 2, 4)) +
						" Gemstone Powder"
					)
					: Lists.newArrayList(
						"Great Explorer",
						"§7Level " + greatExplorer + EnumChatFormatting.DARK_GRAY + "/20",
						"",
						"§7Grants " + EnumChatFormatting.GREEN + "+" + greatExplorerStat + "% " + EnumChatFormatting.GRAY + "chance to",
						"§7find treasure."
					),
			20
		);

		renderHotmPerk(
			miningFortune2,
			(int) (guiLeft + xStart + 303),
			(int) (guiTop + yStartTop - 6),
			mouseX,
			mouseY,
			() ->
				miningFortune2 != 0 && miningFortune2 != 50
					? Lists.newArrayList(
						"Mining Fortune 2",
						"§7Level " + miningFortune2 + EnumChatFormatting.DARK_GRAY + "/50",
						"",
						"§7Grants §a+§a" + miningFortune2Stat + "§7 §6☘ Mining",
						"§6Fortune§7.",
						"",
						EnumChatFormatting.GRAY + "Cost",
						EnumChatFormatting.LIGHT_PURPLE +
						"" +
						GuiProfileViewer.numberFormat.format((int) Math.pow(miningFortune2 + 2, 3.2)) +
						" Gemstone Powder"
					)
					: Lists.newArrayList(
						"Mining Fortune 2",
						"§7Level " + miningFortune2 + EnumChatFormatting.DARK_GRAY + "/50",
						"",
						"§7Grants §a+§a" + miningFortune2Stat + "§7 §6☘ Mining",
						"§6Fortune§7."
					),
			50
		);

		renderHotmPerk(
			orbit,
			(int) (guiLeft + xStart + 279),
			(int) (guiTop + yStartTop + 66),
			mouseX,
			mouseY,
			() ->
				orbit != 0 && orbit != 80
					? Lists.newArrayList(
						"Orbiter",
						"§7Level " + orbit + EnumChatFormatting.DARK_GRAY + "/80",
						"",
						"§7When mining ores, you have a",
						EnumChatFormatting.GREEN + "" + orbitStat + "%" + EnumChatFormatting.GRAY + " chance to get a random",
						"§7amount of experience orbs.",
						"",
						EnumChatFormatting.GRAY + "Cost",
						EnumChatFormatting.DARK_GREEN + "" + ((orbit + 1) * 70) + " Mithril Powder"
					)
					: Lists.newArrayList(
						"Orbiter",
						"§7Level " + orbit + EnumChatFormatting.DARK_GRAY + "/80",
						"",
						"§7When mining ores, you have a",
						EnumChatFormatting.GREEN + "" + orbitStat + "%" + EnumChatFormatting.GRAY + " chance to get a random",
						"§7amount of experience orbs."
					),
			80
		);

		renderHotmPerk(
			frontLoad,
			(int) (guiLeft + xStart + 303),
			(int) (guiTop + yStartTop + 66),
			mouseX,
			mouseY,
			() ->
				Lists.newArrayList(
					"Front Loaded",
					"§7Grants §a+100 §6⸕ Mining Speed",
					"§7and §6☘ Mining Fortune §7for",
					"§7the first §e2,500 §7ores you",
					"§7mine in a day."
				),
			new ItemStack(frontLoad > 0 ? Items.diamond : Items.coal)
		);

		renderHotmPerk(
			starPowder,
			(int) (guiLeft + xStart + 303),
			(int) (guiTop + yStartTop + 42),
			mouseX,
			mouseY,
			() ->
				Lists.newArrayList(
					"Star Powder",
					"§7Mining Mithril Ore near §5Fallen",
					"§5Crystals §7gives §a+3 §7extra",
					"§7Mithril Powder§7."
				),
			new ItemStack(starPowder > 0 ? Items.diamond : Items.coal)
		);

		renderHotmPerk(
			fortnite,
			(int) (guiLeft + xStart + 327),
			(int) (guiTop + yStartTop + 66),
			mouseX,
			mouseY,
			() ->
				Lists.newArrayList(
					"Precision Mining",
					"§7When mining ore, a particle",
					"§7target appears on the block that",
					"§7increases your §6⸕ Mining Speed",
					"§7by §a30% §7when aiming at it."
				),
			new ItemStack(fortnite > 0 ? Items.diamond : Items.coal)
		);

		renderHotmPerk(
			crystallized,
			(int) (guiLeft + xStart + 303),
			(int) (guiTop + yStartTop + 90),
			mouseX,
			mouseY,
			() ->
				crystallized != 0 && crystallized != 30
					? Lists.newArrayList(
						"Crystallized",
						"§7Level " + crystallized + EnumChatFormatting.DARK_GRAY + "/30",
						"",
						"§7Grants §a+§a" + crystallizedStat + "§7 §6⸕ Mining",
						"§6Speed §7and a §a" + crystallizedStat + "%§7 §7chance",
						"§7to deal §a+1 §7extra damage near",
						"§7§5Fallen Stars§7.",
						"",
						EnumChatFormatting.GRAY + "Cost",
						EnumChatFormatting.DARK_GREEN +
						"" +
						GuiProfileViewer.numberFormat.format((int) Math.pow(crystallized + 2, 2.4)) +
						" Mithril Powder"
					)
					: Lists.newArrayList(
						"Crystallized",
						"§7Level " + crystallized + EnumChatFormatting.DARK_GRAY + "/30",
						"",
						"§7Grants §a+§a" + crystallizedStat + "§7 §6⸕ Mining",
						"§6Speed §7and a §a" + crystallizedStat + "%§7 §7chance",
						"§7to deal §a+1 §7extra damage near",
						"§7§5Fallen Stars§7."
					),
			30
		);

		renderPickaxeAbility(
			pickoblus,
			(int) (guiLeft + xStart + 303),
			(int) (guiTop + yStartTop + 114),
			mouseX,
			mouseY,
			() ->
				Lists.newArrayList(
					"Pickobulus",
					"",
					"§6Pickaxe Ability: Pickobulus",
					"§7Throw your pickaxe to create an",
					"§7explosion on impact, mining all",
					"§7ores within a §a2§7 block",
					"§7radius.",
					"§8Cooldown: §a" + (potm == 0 ? "120s" : "110s")
				)
		);

		renderPickaxeAbility(
			maniacMiner,
			(int) (guiLeft + xStart + 327),
			(int) (guiTop + yStartTop + 18),
			mouseX,
			mouseY,
			() ->
				Lists.newArrayList(
					EnumChatFormatting.RED + "Maniac Miner",
					"",
					"§6Pickaxe Ability: Maniac Miner",
					"§7Spends all your Mana and grants",
					"§7§a+1 §6⸕ Mining Speed §7for",
					"§7every 10 Mana spent, for",
					"§7§a§a15s§7§7.",
					"§8Cooldown: §a59s"
				)
		);
	}

	/**
	 * Renders a standard HOTM perk that can be levelled.
	 */
	private void renderHotmPerk(
		int perkLevel,
		int xPosition,
		int yPosition,
		int mouseX,
		int mouseY,
		Supplier<ArrayList<String>> tooltipSupplier,
		int maxLevel
	) {
		renderHotmPerk(perkLevel, xPosition, yPosition, mouseX, mouseY, tooltipSupplier, false, maxLevel);
	}

	/**
	 * Renders a pickaxe ability that can be unlocked once and not levelled.
	 */
	private void renderPickaxeAbility(
		int perkLevel,
		int xPosition,
		int yPosition,
		int mouseX,
		int mouseY,
		Supplier<ArrayList<String>> tooltipSupplier
	) {
		renderHotmPerk(perkLevel, xPosition, yPosition, mouseX, mouseY, tooltipSupplier, true, -1);
	}

	/**
	 * Renders a HOTM perk. This method is only called from its overloads above.
	 */
	private void renderHotmPerk(
		int perkLevel,
		int xPosition,
		int yPosition,
		int mouseX,
		int mouseY,
		Supplier<ArrayList<String>> tooltipSupplier,
		boolean isPickaxeAbility,
		int maxLevel
	) {
		boolean unlocked = perkLevel > 0;
		GlStateManager.color(1, 1, 1, 1);
		GlStateManager.disableLighting();

		ItemStack itemStack;
		if (isPickaxeAbility) {
			RenderHelper.enableGUIStandardItemLighting(); // GUI standard item lighting must be enabled for items that are rendered as blocks, like emerald blocks.
			itemStack = new ItemStack(unlocked ? Blocks.emerald_block : Blocks.coal_block); // Pickaxe abilities are rendered as blocks
		} else { // Non-pickaxe abilities are rendered as items
			itemStack = new ItemStack(unlocked ? (perkLevel >= maxLevel ? Items.diamond : Items.emerald) : Items.coal);
		}

		ArrayList<String> tooltip = tooltipSupplier.get();
		// Prepend the green, yellow, or red color on the first line of each tooltip depending on if the perk is unlocked
		tooltip.set(
			0,
			(unlocked ? (perkLevel >= maxLevel ? EnumChatFormatting.GREEN : EnumChatFormatting.YELLOW) : EnumChatFormatting.RED) +
			tooltip.get(0)
		);

		NBTTagCompound nbt = new NBTTagCompound(); //Adding NBT Data for Custom Resource Packs
		NBTTagCompound display = new NBTTagCompound();
		display.setString("Name", tooltip.get(0));
		nbt.setTag("display", display);
		itemStack.setTagCompound(nbt);

		Minecraft.getMinecraft().getRenderItem().renderItemIntoGUI(itemStack, xPosition, yPosition);
		GlStateManager.enableLighting();
		if (mouseX >= xPosition && mouseX < xPosition + 16) {
			if (mouseY >= yPosition && mouseY <= yPosition + 16) {
				Utils.drawHoveringText(
					tooltip,
					mouseX,
					mouseY,
					getInstance().width,
					getInstance().height,
					-1,
					Minecraft.getMinecraft().fontRendererObj
				);
			}
		}
	}

	/**
	 * A separate method similar to the one above, but allowing the caller to specify an ItemStack to render.
	 * Used for rendering Peak of the Mountain and perks that are unlocked once and not upgraded.
	 */
	private void renderHotmPerk(
		int perkLevel,
		int xPosition,
		int yPosition,
		int mouseX,
		int mouseY,
		Supplier<ArrayList<String>> tooltipSupplier,
		ItemStack itemStack
	) {
		renderHotmPerk(perkLevel, xPosition, yPosition, mouseX, mouseY, tooltipSupplier, itemStack, false);
	}

	/**
	 * This method renders a HOTM perk using the provided ItemStack.
	 * It is overloaded by the method above, and is only called directly to render Peak of the Mountain.
	 */
	private void renderHotmPerk(
		int perkLevel,
		int xPosition,
		int yPosition,
		int mouseX,
		int mouseY,
		Supplier<ArrayList<String>> tooltipSupplier,
		ItemStack itemStack,
		boolean isRenderingBlock
	) {
		boolean unlocked = perkLevel > 0;
		GlStateManager.color(1, 1, 1, 1);
		GlStateManager.disableLighting();
		if (isRenderingBlock) RenderHelper.enableGUIStandardItemLighting();

		ArrayList<String> tooltip = tooltipSupplier.get();
		// Prepend the green or red color on the first line of each tooltip depending on if the perk is unlocked
		if (!tooltip.get(0).contains("Peak of the Mountain")) tooltip.set(
			0,
			(unlocked ? EnumChatFormatting.GREEN : EnumChatFormatting.RED) + tooltip.get(0)
		); //Peak of the Moutain has three color options, and is set already

		NBTTagCompound nbt = new NBTTagCompound(); //Adding NBT Data for Resource Packs
		NBTTagCompound display = new NBTTagCompound();
		display.setString("Name", tooltip.get(0));
		if (tooltip.get(0).contains("Peak of the Mountain")) display.setString("Lore", tooltip.get(1)); //Set Lore to Level
		nbt.setTag("display", display);
		itemStack.setTagCompound(nbt);

		Minecraft.getMinecraft().getRenderItem().renderItemIntoGUI(itemStack, xPosition, yPosition);
		GlStateManager.enableLighting();
		if (mouseX >= xPosition && mouseX < xPosition + 16) {
			if (mouseY >= yPosition && mouseY <= yPosition + 16) {
				Utils.drawHoveringText(
					tooltip,
					mouseX,
					mouseY,
					getInstance().width,
					getInstance().height,
					-1,
					Minecraft.getMinecraft().fontRendererObj
				);
			}
		}
	}
}
