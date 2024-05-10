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

package io.github.moulberry.notenoughupdates.profileviewer;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.core.util.StringUtils;
import io.github.moulberry.notenoughupdates.core.util.render.RenderUtils;
import io.github.moulberry.notenoughupdates.miscfeatures.profileviewer.HoppityPage;
import io.github.moulberry.notenoughupdates.profileviewer.data.APIDataJson;
import io.github.moulberry.notenoughupdates.profileviewer.weight.weight.Weight;
import io.github.moulberry.notenoughupdates.util.Constants;
import io.github.moulberry.notenoughupdates.util.Rectangle;
import io.github.moulberry.notenoughupdates.util.Utils;
import lombok.var;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.text.WordUtils;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class ExtraPage extends GuiProfileViewerPage {

	private static final ResourceLocation pv_extra = new ResourceLocation("notenoughupdates:pv_extra.png");
	private static final List<String> skills = Arrays.asList(
		"taming",
		"mining",
		"foraging",
		"enchanting",
		"farming",
		"combat",
		"fishing",
		"alchemy",
		"carpentry"
	); // TODO: why is this hardcoded
	private TreeMap<Integer, Set<String>> topKills = null;
	private TreeMap<Integer, Set<String>> topDeaths = null;
	private int deathScroll = 0;
	private int killScroll = 0;
	private boolean clickedLoadGuildInfoButton = false;

	private boolean onHoppityPage;
	private final HoppityPage hoppityPage;

	public static final ItemStack hoppitySkull = Utils.createSkull(
		"calmwolfs",
		"d7ac85e6-bd40-359e-a2c5-86082959309e",
		"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvO" +
			"WE4MTUzOThlN2RhODliMWJjMDhmNjQ2Y2FmYzhlN2I4MTNkYTBiZTBlZWMwY2NlNmQzZWZmNTIwNzgwMTAyNiJ9fX0="
	);

	private static final LinkedHashMap<String, ItemStack> pageModeIcon = new LinkedHashMap<String, ItemStack>() {
		{
			put(
				"stats",
				Utils.editItemStackInfo(
					new ItemStack(Items.book),
					EnumChatFormatting.GRAY + "Stats",
					true
				)
			);
			put(
				"hoppity",
				Utils.editItemStackInfo(
					hoppitySkull,
					EnumChatFormatting.GRAY + "Hoppity",
					true
				)
			);
		}
	};

	public ExtraPage(GuiProfileViewer instance) {
		super(instance);
		this.hoppityPage = new HoppityPage(instance);
		getInstance().killDeathSearchTextField.setSize(80, 12);
	}

	@Override
	public void keyTyped(char typedChar, int keyCode) throws IOException {
		super.keyTyped(typedChar, keyCode);
		if (getInstance().killDeathSearchTextField.getFocus()) {
			getInstance().killDeathSearchTextField.keyTyped(typedChar, keyCode);
			killScroll = 0;
			deathScroll = 0;
		}
	}

	@Override
	public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		int guiLeft = GuiProfileViewer.getGuiLeft();
		int guiTop = GuiProfileViewer.getGuiTop();
		super.mouseClicked(mouseX, mouseY, mouseButton);

		// Dimensions: X: guiLeft + xStart + xOffset * 3, Y: guiTop + yStartBottom + 77, Width: 80, Height: 12
		if (mouseX >= GuiProfileViewer.getGuiLeft() + 22 + 103 * 3 &&
			mouseX <= GuiProfileViewer.getGuiLeft() + 22 + 103 * 3 + 80 &&
			mouseY >= GuiProfileViewer.getGuiTop() + 105 + 77 && mouseY <= GuiProfileViewer.getGuiTop() + 105 + 77 + 12) {
			getInstance().killDeathSearchTextField.mouseClicked(mouseX, mouseY, mouseButton);
			getInstance().playerNameTextField.otherComponentClick();
			return true;
		}

		getInstance().killDeathSearchTextField.otherComponentClick();

		int i = ProfileViewerUtils.onSlotToChangePage(mouseX, mouseY, guiLeft, guiTop);
		switch (i) {
			case 1:
				onHoppityPage = false;
				break;
			case 2:
				onHoppityPage = true;
				break;

			default:
				break;
		}

		return false;
	}

	public void drawEssence(
		JsonObject profileInfo,
		float xStart,
		float yStartTop,
		float xOffset,
		float yOffset,
		float mouseX,
		float mouseY
	) {
		if (Constants.PARENTS == null || !Constants.PARENTS.has("ESSENCE_WITHER")) {
			Utils.showOutdatedRepoNotification("parents.json or missing ESSENCE_WITHER");
			return;
		}

		int guiLeft = GuiProfileViewer.getGuiLeft();
		int guiTop = GuiProfileViewer.getGuiTop();
		yStartTop += 77;

		JsonArray essenceArray = new JsonArray();
		essenceArray.addAll(Constants.PARENTS.get("ESSENCE_WITHER").getAsJsonArray());
		//add wither essence since it's not part of the parents array
		essenceArray.add(new JsonPrimitive("ESSENCE_WITHER"));

		for (int i = 0; i < essenceArray.size(); i++) {
			JsonElement jsonElement = essenceArray.get(i);
			String essenceName = jsonElement.getAsString();

			TreeMap<String, JsonObject> itemInformation = NotEnoughUpdates.INSTANCE.manager.getItemInformation();
			if (!itemInformation.containsKey(essenceName)) {
				Utils.showOutdatedRepoNotification(essenceName);
				return;
			}
			String displayName = itemInformation.get(essenceName).getAsJsonObject().get("displayname").getAsString();
			int essenceNumber = Utils.getElementAsInt(Utils.getElement(
				profileInfo,
				"currencies.essence." + essenceName.replace("ESSENCE_", "") + ".current"
			), 0);

			Utils.renderAlignedString(
				EnumChatFormatting.GOLD + displayName,
				EnumChatFormatting.WHITE + StringUtils.shortNumberFormat(essenceNumber, 0),
				guiLeft + xStart + xOffset,
				guiTop + yStartTop + (yOffset - 1) * i,
				76
			);

			if (Constants.ESSENCESHOPS == null) return;
			JsonObject essenceShops = Constants.ESSENCESHOPS;
			if (mouseX >= guiLeft + xStart + xOffset && mouseX <= guiLeft + xStart + xOffset + 76 &&
				mouseY >= guiTop + yStartTop + (yOffset - 1) * i &&
				mouseY <= guiTop + yStartTop + (yOffset - 1) * i + 10) {
				getInstance().tooltipToDisplay = new ArrayList<>();
				if (essenceShops.get(essenceName) == null) continue;

				for (Map.Entry<String, JsonElement> entry : essenceShops.get(essenceName).getAsJsonObject().entrySet()) {
					int perkTier = Utils.getElementAsInt(Utils.getElement(profileInfo, "player_data.perks." + entry.getKey()), 0);
					int max = entry.getValue().getAsJsonObject().get("costs").getAsJsonArray().size();
					EnumChatFormatting formatting = perkTier == max ? EnumChatFormatting.GREEN : EnumChatFormatting.AQUA;
					String name = entry.getValue().getAsJsonObject().get("name").getAsString();
					getInstance().tooltipToDisplay.add(EnumChatFormatting.GOLD + name + ": " + formatting + perkTier + "/" + max);
				}
			}
		}
	}

	@Override
	public void drawPage(int mouseX, int mouseY, float partialTicks) {
		int guiLeft = GuiProfileViewer.getGuiLeft();
		int guiTop = GuiProfileViewer.getGuiTop();

		drawSideButtons();

		if (onHoppityPage) {
			hoppityPage.drawPage(mouseX, mouseY, partialTicks);
			return;
		}

		Minecraft.getMinecraft().getTextureManager().bindTexture(pv_extra);
		Utils.drawTexturedRect(guiLeft, guiTop, getInstance().sizeX, getInstance().sizeY, GL11.GL_NEAREST);

		SkyblockProfiles.SkyblockProfile selectedProfile = getSelectedProfile();
		if (selectedProfile == null) {
			return;
		}

		JsonObject profileInfo = selectedProfile.getProfileJson();
		Map<String, ProfileViewer.Level> skyblockInfo = selectedProfile.getLevelingInfo();
		APIDataJson data = selectedProfile.getAPIDataJson();
		if (data == null) {
			return;
		}
		var player_stats = data.player_stats;
		var auctions = player_stats.auctions;

		float xStart = 22;
		float xOffset = 103;
		float yStartTop = 27;
		float yStartBottom = 105;
		float yOffset = 10;

		float bankBalance = Utils.getElementAsFloat(Utils.getElement(
			selectedProfile.getOuterProfileJson(),
			"banking.balance"
		), 0);
		float purseBalance = data.currencies.coin_purse;

		Utils.renderAlignedString(
			EnumChatFormatting.GOLD + "Bank Balance",
			EnumChatFormatting.WHITE + StringUtils.shortNumberFormat(bankBalance),
			guiLeft + xStart,
			guiTop + yStartTop,
			76
		);
		Utils.renderAlignedString(
			EnumChatFormatting.GOLD + "Purse",
			EnumChatFormatting.WHITE + StringUtils.shortNumberFormat(purseBalance),
			guiLeft + xStart,
			guiTop + yStartTop + yOffset,
			76
		);

		{
			String first_join = getTimeSinceString(profileInfo, "profile.first_join");
			if (first_join != null) {
				Utils.renderAlignedString(
					EnumChatFormatting.AQUA + "Joined",
					EnumChatFormatting.WHITE + first_join,
					guiLeft + xStart,
					guiTop + yStartTop + yOffset * 2,
					76
				);
			}
		}

		JsonObject guildInfo;
		if (GuiProfileViewer.getProfile().isPlayerInGuild()) {
			guildInfo =
				clickedLoadGuildInfoButton ? GuiProfileViewer.getProfile().getOrLoadGuildInformation(null) : null;
		} else {
			guildInfo = new JsonObject();
			guildInfo.add("name", new JsonPrimitive("N/A"));
		}

		boolean shouldRenderGuild = guildInfo != null && guildInfo.has("name");

		// Render the info when the button has been clicked
		if (shouldRenderGuild) {
			Utils.renderAlignedString(
				EnumChatFormatting.AQUA + "Guild",
				EnumChatFormatting.WHITE + guildInfo.get("name").getAsString(),
				guiLeft + xStart,
				guiTop + yStartTop + yOffset * 3,
				76
			);
		} else {
			// Render a button to click to load the guild info
			Rectangle buttonRect = new Rectangle(
				(int) (guiLeft + xStart - 1),
				(int) (guiTop + yStartTop + yOffset * 3),
				78,
				12
			);

			RenderUtils.drawFloatingRectWithAlpha(buttonRect.getX(), buttonRect.getY(), buttonRect.getWidth(),
				buttonRect.getHeight(), 100, true
			);
			Utils.renderShadowedString(
				clickedLoadGuildInfoButton
					? EnumChatFormatting.AQUA + "Loading..."
					: EnumChatFormatting.WHITE + "Load Guild Info",
				guiLeft + xStart + 38,
				guiTop + yStartTop + yOffset * 3 + 2,
				70
			);

			if (Mouse.getEventButtonState() && Utils.isWithinRect(mouseX, mouseY, buttonRect)) {
				clickedLoadGuildInfoButton = true;
			}
		}

		GuiProfileViewer.pronouns.peekValue().flatMap(it -> it).ifPresent(choice -> Utils.renderAlignedString(
			EnumChatFormatting.GREEN + "Pronouns",
			EnumChatFormatting.WHITE + String.join(" / ", choice.render()),
			guiLeft + xStart,
			guiTop + yStartTop + yOffset * 4 + (shouldRenderGuild ? 0 : 5),
			76
		));

		int fairySouls = data.fairy_soul.total_collected;

		int fairySoulMax = 227;
		if (Constants.FAIRYSOULS != null && Constants.FAIRYSOULS.has("Max Souls")) {
			fairySoulMax = Constants.FAIRYSOULS.get("Max Souls").getAsInt();
		}
		Utils.renderAlignedString(
			EnumChatFormatting.LIGHT_PURPLE + "Fairy Souls",
			EnumChatFormatting.WHITE.toString() + fairySouls + "/" + fairySoulMax,
			guiLeft + xStart,
			guiTop + yStartBottom,
			76
		);

		if (skyblockInfo != null) {
			float totalSkillLVL = 0;
			float totalTrueSkillLVL = 0;
			float totalSlayerLVL = 0;
			float totalSkillCount = 0;
			float totalSlayerCount = 0;
			float totalSlayerXP = 0;

			for (Map.Entry<String, ProfileViewer.Level> entry : skyblockInfo.entrySet()) {
				if (skills.contains(entry.getKey())) {
					totalSkillLVL += entry.getValue().level;
					totalTrueSkillLVL += Math.floor(entry.getValue().level);
					totalSkillCount++;
				} else if (Weight.SLAYER_NAMES.contains(entry.getKey())) {
					totalSlayerLVL += entry.getValue().level;
					totalSlayerCount++;
					totalSlayerXP += entry.getValue().totalXp;
				}
			}

			float avgSkillLVL = totalSkillLVL / totalSkillCount;
			float avgTrueSkillLVL = totalTrueSkillLVL / totalSkillCount;
			float avgSlayerLVL = totalSlayerLVL / totalSlayerCount;

			Utils.renderAlignedString(
				EnumChatFormatting.RED + "AVG Skill LVL",
				selectedProfile.skillsApiEnabled() ?
					EnumChatFormatting.WHITE.toString() + Math.floor(avgSkillLVL * 10) / 10 :
					EnumChatFormatting.RED + "API OFF!",
				guiLeft + xStart,
				guiTop + yStartBottom + yOffset,
				76
			);

			Utils.renderAlignedString(
				EnumChatFormatting.RED + "True AVG Skill LVL",
				selectedProfile.skillsApiEnabled() ?
					EnumChatFormatting.WHITE.toString() + Math.floor(avgTrueSkillLVL * 10) / 10 :
					EnumChatFormatting.RED + "API OFF!",
				guiLeft + xStart,
				guiTop + yStartBottom + yOffset * 2,
				76
			);

			Utils.renderAlignedString(
				EnumChatFormatting.RED + "AVG Slayer LVL",
				EnumChatFormatting.WHITE.toString() + Math.floor(avgSlayerLVL * 10) / 10,
				guiLeft + xStart,
				guiTop + yStartBottom + yOffset * 3,
				76
			);

			Utils.renderAlignedString(
				EnumChatFormatting.RED + "Total Slayer XP",
				EnumChatFormatting.WHITE + StringUtils.shortNumberFormat(totalSlayerXP),
				guiLeft + xStart,
				guiTop + yStartBottom + yOffset * 4,
				76
			);
		}

		float auctions_bids = auctions.bids;
		float auctions_highest_bid = auctions.highest_bid;
		float auctions_won = auctions.won;
		float auctions_created = auctions.created;
		float auctions_gold_spent = auctions.gold_spent;
		float auctions_gold_earned = auctions.gold_earned;

		Utils.renderAlignedString(
			EnumChatFormatting.DARK_PURPLE + "Auction Bids",
			EnumChatFormatting.WHITE.toString() + (int) auctions_bids,
			guiLeft + xStart + xOffset,
			guiTop + yStartTop,
			76
		);
		Utils.renderAlignedString(
			EnumChatFormatting.DARK_PURPLE + "Highest Bid",
			EnumChatFormatting.WHITE + StringUtils.shortNumberFormat(auctions_highest_bid),
			guiLeft + xStart + xOffset,
			guiTop + yStartTop + yOffset,
			76
		);
		Utils.renderAlignedString(
			EnumChatFormatting.DARK_PURPLE + "Auctions Won",
			EnumChatFormatting.WHITE.toString() + (int) auctions_won,
			guiLeft + xStart + xOffset,
			guiTop + yStartTop + yOffset * 2,
			76
		);
		Utils.renderAlignedString(
			EnumChatFormatting.DARK_PURPLE + "Auctions Created",
			EnumChatFormatting.WHITE.toString() + (int) auctions_created,
			guiLeft + xStart + xOffset,
			guiTop + yStartTop + yOffset * 3,
			76
		);
		Utils.renderAlignedString(
			EnumChatFormatting.DARK_PURPLE + "Gold Spent",
			EnumChatFormatting.WHITE + StringUtils.shortNumberFormat(auctions_gold_spent),
			guiLeft + xStart + xOffset,
			guiTop + yStartTop + yOffset * 4,
			76
		);
		Utils.renderAlignedString(
			EnumChatFormatting.DARK_PURPLE + "Gold Earned",
			EnumChatFormatting.WHITE + StringUtils.shortNumberFormat(auctions_gold_earned),
			guiLeft + xStart + xOffset,
			guiTop + yStartTop + yOffset * 5,
			76
		);

		float pet_milestone_ores_mined = player_stats.pets.milestone.ores_mined;
		float pet_milestone_sea_creatures_killed = player_stats.pets.milestone.sea_creatures_killed;

		float items_fished = player_stats.items_fished.total;
		float items_fished_treasure = player_stats.items_fished.treasure;
		float items_fished_large_treasure = player_stats.items_fished.large_treasure;

		Utils.renderAlignedString(
			EnumChatFormatting.GREEN + "Ores Mined",
			EnumChatFormatting.WHITE.toString() + (int) pet_milestone_ores_mined,
			guiLeft + xStart + xOffset * 2,
			guiTop + yStartTop,
			76
		);
		Utils.renderAlignedString(
			EnumChatFormatting.GREEN + "Sea Creatures Killed",
			EnumChatFormatting.WHITE.toString() + (int) pet_milestone_sea_creatures_killed,
			guiLeft + xStart + xOffset * 2,
			guiTop + yStartTop + yOffset,
			76
		);

		Utils.renderAlignedString(
			EnumChatFormatting.GREEN + "Items Fished",
			EnumChatFormatting.WHITE.toString() + (int) items_fished,
			guiLeft + xStart + xOffset * 2,
			guiTop + yStartTop + yOffset * 3,
			76
		);
		Utils.renderAlignedString(
			EnumChatFormatting.GREEN + "Treasures Fished",
			EnumChatFormatting.WHITE.toString() + (int) items_fished_treasure,
			guiLeft + xStart + xOffset * 2,
			guiTop + yStartTop + yOffset * 4,
			76
		);
		Utils.renderAlignedString(
			EnumChatFormatting.GREEN + "Large Treasures",
			EnumChatFormatting.WHITE.toString() + (int) items_fished_large_treasure,
			guiLeft + xStart + xOffset * 2,
			guiTop + yStartTop + yOffset * 5,
			76
		);

		drawEssence(selectedProfile.getProfileJson(), xStart, yStartTop, xOffset, yOffset, mouseX, mouseY);

		if (topKills == null) {
			topKills = new TreeMap<>();
			JsonObject stats = Utils
				.getElementOrDefault(profileInfo, "player_stats.kills", new JsonObject())
				.getAsJsonObject();
			for (Map.Entry<String, JsonElement> entry : stats.entrySet()) {
				if (entry.getValue().isJsonPrimitive()) {
					JsonPrimitive prim = (JsonPrimitive) entry.getValue();
					if (prim.isNumber()) {
						String name = WordUtils.capitalizeFully(entry.getKey().replace("_", " "));
						Set<String> kills = topKills.computeIfAbsent(prim.getAsInt(), k -> new HashSet<>());
						kills.add(name);
					}
				}
			}
		}
		if (topDeaths == null) {
			topDeaths = new TreeMap<>();
			JsonObject stats = Utils
				.getElementOrDefault(profileInfo, "player_stats.deaths", new JsonObject())
				.getAsJsonObject();
			for (Map.Entry<String, JsonElement> entry : stats.entrySet()) {
				if (entry.getValue().isJsonPrimitive()) {
					JsonPrimitive prim = (JsonPrimitive) entry.getValue();
					if (prim.isNumber()) {
						String name = WordUtils.capitalizeFully(entry.getKey().replace("_", " "));
						Set<String> deaths = topDeaths.computeIfAbsent(prim.getAsInt(), k -> new HashSet<>());
						deaths.add(name);
					}
				}
			}
		}

		getInstance().killDeathSearchTextField.render(
			(int) (guiLeft + xStart + xOffset * 3),
			(int) (guiTop + yStartBottom + 77)
		);

		float killDeathX = guiLeft + xStart + xOffset * 3;

		int index = 0;
		int skipCount = 0;
		int renderedKills = 0;
		for (int killCount : topKills.descendingKeySet()) {
			Set<String> kills = topKills.get(killCount);
			for (String killType : kills) {
				boolean isSearch =
					getInstance().killDeathSearchTextField.getText().isEmpty() || killType.toLowerCase(Locale.ROOT).contains(
						getInstance().killDeathSearchTextField.getText().toLowerCase(Locale.ROOT));
				float killY = guiTop + yStartTop + yOffset * ((index - skipCount) - killScroll);
				if (!isSearch) skipCount++;
				if (isSearch && killY + 6 < guiTop + yStartTop + 65 && killY >= guiTop + yStartTop) {
					renderedKills++;
					Utils.renderAlignedString(
						EnumChatFormatting.YELLOW + "K: " + killType,
						EnumChatFormatting.WHITE.toString() + killCount,
						killDeathX,
						killY,
						76
					);
				}

				index++;
			}
		}

		index = 0;
		skipCount = 0;
		int renderedDeaths = 0;
		for (int deathCount : topDeaths.descendingKeySet()) {
			Set<String> deaths = topDeaths.get(deathCount);
			for (String deathType : deaths) {
				boolean isSearch =
					getInstance().killDeathSearchTextField.getText().isEmpty() || deathType.toLowerCase(Locale.ROOT).contains(
						getInstance().killDeathSearchTextField.getText().toLowerCase(Locale.ROOT));
				float deathY = guiTop + yStartBottom + yOffset * ((index - skipCount) - deathScroll);
				if (!isSearch) skipCount++;
				if (isSearch && deathY + 6 < guiTop + yStartBottom + 65 && deathY >= guiTop + yStartBottom) {
					renderedDeaths++;
					Utils.renderAlignedString(
						EnumChatFormatting.YELLOW + "D: " + deathType,
						EnumChatFormatting.WHITE.toString() + deathCount,
						killDeathX,
						deathY,
						76
					);
				}
				index++;
			}
		}

		int mouseDWheel = Mouse.getDWheel();
		if (mouseX >= killDeathX && mouseX <= killDeathX + 76) {
			if (mouseY >= guiTop + yStartTop && mouseY <= guiTop + yStartTop + 65) {
				if (mouseDWheel > 0) {
					killScroll -= 1;
				} else if (mouseDWheel < 0) {
					killScroll += 1;
				}

				if (killScroll < 0) {
					killScroll = 0;
				}
			} else if (mouseY >= guiTop + yStartBottom && mouseY <= guiTop + yStartBottom + 65) {
				if (mouseDWheel > 0) {
					deathScroll -= 1;
				} else if (mouseDWheel < 0) {
					deathScroll += 1;
				}
				if (deathScroll < 0) {
					deathScroll = 0;
				}

			}
		}

		if (killScroll > renderedDeaths) {
			killScroll = renderedDeaths;
		}
		if (deathScroll > renderedKills) {
			deathScroll = renderedKills;
		}
	}

	private String getTimeSinceString(JsonObject profileInfo, String path) {
		JsonElement lastSaveElement = Utils.getElement(profileInfo, path);

		if (lastSaveElement != null && lastSaveElement.isJsonPrimitive()) {
			return Utils.timeSinceMillisecond(lastSaveElement.getAsLong());
		}
		return null;
	}

	@Override
	public void resetCache() {
		topDeaths = null;
		topKills = null;
	}

	private void drawSideButtons() {
		GlStateManager.enableDepth();
		GlStateManager.translate(0, 0, 5);
		if (onHoppityPage) {
			Utils.drawPvSideButton(1, pageModeIcon.get("hoppity"), true, getInstance());
		} else {
			Utils.drawPvSideButton(0, pageModeIcon.get("stats"), true, getInstance());
		}
		GlStateManager.translate(0, 0, -3);

		GlStateManager.translate(0, 0, -2);
		if (!onHoppityPage) {
			Utils.drawPvSideButton(1, pageModeIcon.get("hoppity"), false, getInstance());
		} else {
			Utils.drawPvSideButton(0, pageModeIcon.get("stats"), false, getInstance());
		}
		GlStateManager.disableDepth();
	}
}
