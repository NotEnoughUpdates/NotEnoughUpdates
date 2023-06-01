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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class CrimsonIslePage extends GuiProfileViewerPage {

	private static final ResourceLocation CRIMSON_ISLE =
		new ResourceLocation("notenoughupdates:pv_crimson_isle_page.png");

	private static final DateFormat dateFormat = new SimpleDateFormat("EEE d MMM yyyy");
	private static final DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
	private static final String[] dojoGrades = {"F", "D", "C", "B", "A", "S"};

	private static final ItemStack[] KUUDRA_KEYS = {
		NotEnoughUpdates.INSTANCE.manager.createItem("KUUDRA_TIER_KEY"),
		NotEnoughUpdates.INSTANCE.manager.createItem("KUUDRA_HOT_TIER_KEY"),
		NotEnoughUpdates.INSTANCE.manager.createItem("KUUDRA_BURNING_TIER_KEY"),
		NotEnoughUpdates.INSTANCE.manager.createItem("KUUDRA_FIERY_TIER_KEY"),
		NotEnoughUpdates.INSTANCE.manager.createItem("KUUDRA_INFERNAL_TIER_KEY"),
	};

	private static final String[] KUUDRA_TIERS_NAME = {"Basic", "Hot", "Burning", "Fiery", "Infernal"};

	// This is different to the one above as these refer to the names of the tiers in the API
	public static final String[] KUUDRA_TIERS = {"none", "hot", "burning", "fiery", "infernal"};

	public static final LinkedHashMap<String, String> apiDojoTestNames = new LinkedHashMap<String, String>() {{
		put("mob_kb", EnumChatFormatting.GOLD + "Test of Force");
		put("wall_jump", EnumChatFormatting.LIGHT_PURPLE + "Test of Stamina");
		put("archer", EnumChatFormatting.YELLOW + "Test of Mastery");
		put("sword_swap", EnumChatFormatting.RED + "Test of Discipline");
		put("snake", EnumChatFormatting.GREEN + "Test of Swiftness");
		put("lock_head", EnumChatFormatting.BLUE + "Test of Control");
		put("fireball", EnumChatFormatting.GOLD + "Test of Tenacity");
	}};

	public static final LinkedHashMap<Integer, String> dojoPointsToRank = new LinkedHashMap<Integer, String>() {{
		put(0, EnumChatFormatting.GRAY + "None");
		put(1000, EnumChatFormatting.YELLOW + "Yellow");
		put(2000, EnumChatFormatting.GREEN + "Green");
		put(4000, EnumChatFormatting.BLUE + "Blue");
		put(6000, EnumChatFormatting.GOLD + "Brown");
		put(7000, EnumChatFormatting.DARK_GRAY + "Black");
	}};

	private static final HashMap<String, String> factions = new HashMap<String, String>() {{
		put("mages", EnumChatFormatting.DARK_PURPLE + "Mages");
		put("barbarians", EnumChatFormatting.RED + "Barbarians");
		put("N/A", EnumChatFormatting.GRAY + "N/A");
	}};

	private static final LinkedHashMap<Integer, String> factionThresholds = new LinkedHashMap<Integer, String>() {{
		put(-3000, "Hostile");
		put(-1000, "Unfriendly");
		put(0, "Neutral");
		put(1000, "Friendly");
		put(3000, "Trusted");
		put(6000, "Honored");
		put(12000, "Hero");
	}};

	public CrimsonIslePage(GuiProfileViewer instance) {
		super(instance);
	}

	@Override
	public void drawPage(int mouseX, int mouseY, float partialTicks) {
		int guiLeft = GuiProfileViewer.getGuiLeft();
		int guiTop = GuiProfileViewer.getGuiTop();

		SkyblockProfiles.SkyblockProfile selectedProfile = getSelectedProfile();
		if (selectedProfile == null) {
			return;
		}

		JsonObject profileInfo = selectedProfile.getProfileJson();
		if (profileInfo == null || !profileInfo.has("nether_island_player_data")) {
			Utils.drawStringCentered(
				EnumChatFormatting.RED + "No data found for the Crimson Isles",
				guiLeft + 431 / 2f, guiTop + 101, true, 0
			);
			return;
		}

		Minecraft.getMinecraft().getTextureManager().bindTexture(CRIMSON_ISLE);
		Utils.drawTexturedRect(guiLeft, guiTop, 431, 202, GL11.GL_NEAREST);

		JsonObject netherIslandPlayerData = profileInfo.getAsJsonObject("nether_island_player_data");

		// Dojo stats
		drawDojoStats(netherIslandPlayerData, guiLeft, guiTop);

		// Kuudra stats
		drawKuudraStats(netherIslandPlayerData, guiLeft, guiTop, mouseX, mouseY);

		// Last matriarch attempt
		drawLastMatriarchAttempt(netherIslandPlayerData, guiLeft, guiTop);

		// Faction reputation
		drawFactionReputation(netherIslandPlayerData, guiLeft, guiTop);
	}

	public void drawKuudraStats(JsonObject data, int guiLeft, int guiTop, int mouseX, int mouseY) {
		Utils.drawStringCentered(EnumChatFormatting.RED + "Kuudra Stats", guiLeft + (431 * 0.18f), guiTop + 14, true, 0);

		JsonObject kuudraCompletedTiers = data.getAsJsonObject("kuudra_completed_tiers");

		RenderHelper.enableGUIStandardItemLighting();

		for (int i = 0; i < 5; i++) {
			// Checking the player has completions for each tier
			// and get the number of completions if they do
			int completions =
				kuudraCompletedTiers.has(KUUDRA_TIERS[i]) ? kuudraCompletedTiers.get(KUUDRA_TIERS[i]).getAsInt() : 0;

			// Get the highest wave for this tier of kuudra if they have completed a run
			// since infernal kuudra was released
			int highestWaveCompleted = kuudraCompletedTiers.has("highest_wave_" + KUUDRA_TIERS[i]) ?
				kuudraCompletedTiers.get("highest_wave_" + KUUDRA_TIERS[i]).getAsInt() : 0;

			Minecraft.getMinecraft().getRenderItem().renderItemIntoGUI(
				KUUDRA_KEYS[i],
				guiLeft + 8,
				guiTop + 30 + (i * 30)
			);

			Utils.renderAlignedString(
				EnumChatFormatting.RED + KUUDRA_TIERS_NAME[i] + ": ",
				EnumChatFormatting.WHITE + String.valueOf(completions),
				guiLeft + 23,
				guiTop + 30 + (i * 30),
				110
			);

			Utils.renderAlignedString(
				EnumChatFormatting.RED + "Highest Wave: ",
				EnumChatFormatting.WHITE + (highestWaveCompleted != 0 ? String.valueOf(highestWaveCompleted) : "N/A"),
				guiLeft + 23,
				guiTop + 42 + (i * 30),
				110
			);

			if (highestWaveCompleted == 0) {
				if (mouseX > guiLeft + 23 && mouseX < guiLeft + 133 && mouseY < guiTop + 50 + (i * 30) &&
					mouseY > guiTop + 42 + (i * 30)) {
					getInstance().tooltipToDisplay = new ArrayList<>();
					getInstance().tooltipToDisplay.add(EnumChatFormatting.RED + "N/A will only show for highest wave");
					getInstance().tooltipToDisplay.add(EnumChatFormatting.RED + "if you have not completed a run for");
					getInstance().tooltipToDisplay.add(EnumChatFormatting.RED + "this tier since Infernal tier was released.");
				}
			}
		}

		Utils.renderAlignedString(
			EnumChatFormatting.RED + "Total runs: ",
			EnumChatFormatting.WHITE + String.valueOf(getTotalKuudraRuns(kuudraCompletedTiers)),
			guiLeft + 23,
			guiTop + 30 + (5 * 30),
			110
		);
	}

	public int getTotalKuudraRuns(JsonObject completedRuns) {
		int totalRuns = 0;
		for (Map.Entry<String, JsonElement> runs : completedRuns.entrySet()) {
			if (runs.getKey().startsWith("highest_wave")) continue;
			totalRuns += runs.getValue().getAsInt();
		}
		return totalRuns;
	}

	public void drawDojoStats(JsonObject data, int guiLeft, int guiTop) {
		Utils.drawStringCentered(EnumChatFormatting.YELLOW + "Dojo Stats", guiLeft + (431 * 0.49f), guiTop + 14, true, 0);

		JsonObject dojoStats = data.getAsJsonObject("dojo");

		int totalPoints = 0;
		int idx = 0;
		for (Map.Entry<String, String> dojoTest : apiDojoTestNames.entrySet()) {
			int curPoints = dojoStats.get("dojo_points_" + dojoTest.getKey()).getAsInt();
			totalPoints += curPoints;
			Utils.renderAlignedString(
				dojoTest.getValue() + ": ",
				EnumChatFormatting.WHITE + "" + curPoints + " (" +
					dojoGrades[(curPoints / 200) >= 6 ? 5 : (curPoints / 200)] + ")",
				guiLeft + (431 * 0.49f) - 65,
				guiTop + 30 + (idx * 12),
				130
			);
			idx ++;
		}

		Utils.renderAlignedString(
			EnumChatFormatting.GRAY + "Points: ",
			EnumChatFormatting.GOLD + String.valueOf(totalPoints),
			guiLeft + (431 * 0.49f) - 65,
			guiTop + 40 + (apiDojoTestNames.size() * 12),
			130
		);

		Utils.renderAlignedString(
			EnumChatFormatting.GRAY + "Rank: ",
			getRank(totalPoints),
			guiLeft + (431 * 0.49f) - 65,
			guiTop + 52 + (apiDojoTestNames.size() * 12),
			130
		);

		Utils.renderAlignedString(
			EnumChatFormatting.GRAY + "Points to next: ",
			getPointsToNextRank(totalPoints) == 0
				? EnumChatFormatting.GOLD + "MAXED!"
				: String.valueOf(getPointsToNextRank(totalPoints)),
			guiLeft + (431 * 0.49f) - 65,
			guiTop + 64 + (apiDojoTestNames.size() * 12),
			130
		);
	}

	public static String getRank(int points) {
		int lastRank = 0;
		for (Map.Entry<Integer, String> rank : dojoPointsToRank.entrySet()) {
			if (points < rank.getKey()) {
				return dojoPointsToRank.get(lastRank);
			}
			lastRank = rank.getKey();
		}
		return dojoPointsToRank.get(lastRank);
	}

	public int getPointsToNextRank(int pointsTotal) {
		int pointsToNextRank = 0;
		for (Map.Entry<Integer, String> dojoRank : dojoPointsToRank.entrySet()) {
			if (dojoRank.getKey() > pointsTotal) {
				pointsToNextRank = dojoRank.getKey();
				break;
			}
		}
		return pointsToNextRank == 0 ? 0 : pointsToNextRank - pointsTotal;
	}

	public void drawLastMatriarchAttempt(JsonObject data, int guiLeft, int guiTop) {
		Utils.drawStringCentered(
			EnumChatFormatting.GOLD + "Last Matriarch Attempt",
			guiLeft + (431 * 0.82f), guiTop + 104, true, 0
		);

		JsonObject lastMatriarchAttempt = data.getAsJsonObject("matriarch");

		if (!lastMatriarchAttempt.entrySet().isEmpty()) {
			Utils.renderAlignedString(
				EnumChatFormatting.GOLD + "Heavy Pearls Acquired: ",
				EnumChatFormatting.WHITE + lastMatriarchAttempt.get("pearls_collected").getAsString(),
				guiLeft + 290,
				guiTop + 119,
				130
			);

			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(lastMatriarchAttempt.get("last_attempt").getAsLong());
			String date = dateFormat.format(calendar.getTime());
			String time = timeFormat.format(calendar.getTime());

			Utils.renderAlignedString(
				EnumChatFormatting.GOLD + "Last Attempt: ",
				EnumChatFormatting.WHITE + date,
				guiLeft + 290,
				guiTop + 131,
				130
			);

			Utils.renderAlignedString(
				EnumChatFormatting.GOLD + " ",
				EnumChatFormatting.WHITE + time,
				guiLeft + 290,
				guiTop + 143,
				130
			);
			return;
		}

		Utils.renderAlignedString(
			EnumChatFormatting.RED + "No attempts found!",
			" ",
			guiLeft + 290,
			guiTop + 119,
			130
		);
	}

	public void drawFactionReputation(JsonObject data, int guiLeft, int guiTop) {
		Utils.drawStringCentered(
			EnumChatFormatting.DARK_PURPLE + "Faction Reputation",
			guiLeft + (431 * 0.82f), guiTop + 14, true, 0
		);

		String selectedFaction = data.has("selected_faction") ? data.get("selected_faction").getAsString() : "N/A";

		Utils.renderAlignedString(
			EnumChatFormatting.GREEN + "Faction: ",
			factions.get(selectedFaction),
			guiLeft + 290,
			guiTop + 30,
			130
		);

		int mageReputation = data.has("mages_reputation") ? data.get("mages_reputation").getAsInt() : 0;
		int barbarianReputation = data.has("barbarians_reputation") ? data.get("barbarians_reputation").getAsInt() : 0;

		// Mage reputation
		int mageReputationThreshold = 0;
		for (Map.Entry<Integer, String> factionThreshold : factionThresholds.entrySet()) {
			if (mageReputation >= factionThreshold.getKey()) {
				mageReputationThreshold = factionThreshold.getKey();
			}
		}

		Utils.renderAlignedString(
			EnumChatFormatting.DARK_PURPLE + "Mage Reputation: ",
			EnumChatFormatting.WHITE + String.valueOf(mageReputation),
			guiLeft + 290,
			guiTop + 42 + (selectedFaction.equals("mages") || selectedFaction.equals("N/A") ? 0 : 24),
			130
		);

		Utils.renderAlignedString(
			EnumChatFormatting.DARK_PURPLE + "Title: ",
			EnumChatFormatting.WHITE + factionThresholds.get(mageReputationThreshold),
			guiLeft + 290,
			guiTop + 54 + (selectedFaction.equals("mages") || selectedFaction.equals("N/A") ? 0 : 24),
			130
		);

		// Barbarian reputation
		int barbarianReputationThreshold = 0;
		for (Map.Entry<Integer, String> factionThreshold : factionThresholds.entrySet()) {
			if (barbarianReputation >= factionThreshold.getKey()) {
				barbarianReputationThreshold = factionThreshold.getKey();
			}
		}

		Utils.renderAlignedString(
			EnumChatFormatting.RED + "Barbarian Reputation: ",
			EnumChatFormatting.WHITE + String.valueOf(barbarianReputation),
			guiLeft + 290,
			guiTop + 42 + (selectedFaction.equals("barbarians") ? 0 : 24),
			130
		);

		Utils.renderAlignedString(
			EnumChatFormatting.RED + "Title: ",
			EnumChatFormatting.WHITE + factionThresholds.get(barbarianReputationThreshold),
			guiLeft + 290,
			guiTop + 54 + (selectedFaction.equals("barbarians") ? 0 : 24),
			130
		);
	}
}
