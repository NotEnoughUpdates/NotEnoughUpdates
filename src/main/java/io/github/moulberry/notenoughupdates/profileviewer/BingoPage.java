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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class BingoPage extends GuiProfileViewerPage {

	private static final ResourceLocation BINGO_GUI_TEXTURE = new ResourceLocation("notenoughupdates:pv_bingo_tab.png");
	private long lastResourceRequest;
	private List<JsonObject> bingoGoals = null;
	private int currentEventId;

	public BingoPage(GuiProfileViewer instance) {
		super(instance);
	}

	@Override
	public void drawPage(int mouseX, int mouseY, float partialTicks) {
		loadBingoResources();

		ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
		int width = scaledResolution.getScaledWidth();
		int height = scaledResolution.getScaledHeight();

		int guiLeft = GuiProfileViewer.getGuiLeft();
		int guiTop = GuiProfileViewer.getGuiTop();

		JsonObject bingoInfo = GuiProfileViewer.getProfile().getBingoInformation();
		// Check if the player has created a bingo profile for the current event
		if (bingoInfo == null) {
			showMissingDataMessage(guiLeft, guiTop);
			return;
		}

		// Bingo goals data not loaded yet
		if (bingoGoals == null) {
			return;
		}

		JsonArray events = bingoInfo.get("events").getAsJsonArray();
		JsonObject lastEvent = events.get(events.size() - 1).getAsJsonObject();
		int lastParticipatedId = lastEvent.get("key").getAsInt();
		if (currentEventId != lastParticipatedId) {
			showMissingDataMessage(guiLeft, guiTop);
			return;
		}

		List<String> completedGoals = jsonArrayToStringList(lastEvent.get("completed_goals").getAsJsonArray());
		Minecraft.getMinecraft().getTextureManager().bindTexture(BINGO_GUI_TEXTURE);
		Utils.drawTexturedRect(guiLeft, guiTop, 431, 202, GL11.GL_NEAREST);

		GlStateManager.color(1, 1, 1, 1);
		GlStateManager.disableLighting();

		int row = 0;
		int col = 0;
		int initialY = guiTop + 46;
		int initialX = guiLeft + 231;
		int xAdjustment = 0;
		int yAdjustment = 0;
		for (JsonObject bingoGoal : bingoGoals) {
			boolean dye = false;
			boolean completed = false;
			boolean communityGoal = false;
			Item material;
			if (bingoGoal.has("tiers")) {
				if (isCommunityGoalFinished(bingoGoal)) {
					material = Item.getItemFromBlock(Blocks.emerald_block);
				} else {
					material = Item.getItemFromBlock(Blocks.iron_block);
				}
				RenderHelper.enableGUIStandardItemLighting();
				completed = true;
				communityGoal = true;
				yAdjustment = -1;
				xAdjustment = -1;
			} else {
				if (completedGoals.contains(bingoGoal.get("id").getAsString())) {
					material = Items.dye;
					xAdjustment = -1;
					dye = true;
					completed = true;
				} else {
					material = Items.paper;
				}
			}

			ItemStack itemStack = new ItemStack(material);
			if (dye) {
				itemStack.setItemDamage(10);
			}
			int x = col == 0 ? initialX + xAdjustment : initialX + (24 * col) + xAdjustment;
			int y = row == 0 ? initialY + yAdjustment : initialY + (24 * row) + yAdjustment;

			Minecraft.getMinecraft().getRenderItem().renderItemIntoGUI(itemStack, x, y);
			y = communityGoal ? y - 1 : y;
			if (mouseX >= x && mouseX < x + 24) {
				if (mouseY >= y && mouseY <= y + 24) {
					Utils.drawHoveringText(getTooltip(bingoGoal, completed, communityGoal), mouseX, mouseY, width, height, -1);
				}
			}
			col++;
			if (col == 5) {
				col = 0;
				row++;
			}
		}

		String totalPointsString =
			EnumChatFormatting.AQUA + "Collected Points: " + EnumChatFormatting.WHITE + lastEvent.get("points").getAsInt();
		int totalGoals = completedGoals.size();
		String personalGoalsString;
		if (totalGoals == 20) {
			personalGoalsString = EnumChatFormatting.AQUA + "Personal Goals: " + EnumChatFormatting.GOLD + "20/20";
		} else {
			personalGoalsString =
				EnumChatFormatting.AQUA +
					"Personal Goals: " +
					EnumChatFormatting.WHITE +
					completedGoals.size() +
					EnumChatFormatting.GOLD +
					"/" +
					EnumChatFormatting.WHITE +
					20;
		}
		Utils.drawStringF(totalPointsString, guiLeft + 22, guiTop + 19, true, 0);
		Utils.drawStringF(personalGoalsString, guiLeft + 22, guiTop + 31, true, 0);

		GlStateManager.enableLighting();
	}

	private boolean isCommunityGoalFinished(JsonObject goal) {
		JsonArray tiers = goal.get("tiers").getAsJsonArray();
		int totalTiers = tiers.size();
		long progress = goal.get("progress").getAsLong();
		int finalTier = 0;
		for (JsonElement tier : tiers) {
			long currentTier = tier.getAsLong();
			if (progress < currentTier) {
				break;
			}
			finalTier++;
		}
		return finalTier == totalTiers;
	}

	private String generateProgressIndicator(double progress, double goal) {
		int totalFields = 20;
		int filled;
		double percentage = progress / goal * 100;
		if (percentage >= 100) {
			filled = 20;
		} else {
			filled = (int) Math.round((percentage / 100) * 20);
		}
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(EnumChatFormatting.DARK_GREEN);
		for (int i = 0; i < totalFields; i++) {
			stringBuilder.append("-");
			if (i > filled) {
				stringBuilder.append(EnumChatFormatting.GRAY);
			}
		}

		return stringBuilder.toString();
	}

	private List<String> getTooltip(JsonObject goal, boolean completed, boolean communityGoal) {
		List<String> tooltip = new ArrayList<>();
		if (communityGoal) {
			//get current tier
			JsonArray tiers = goal.get("tiers").getAsJsonArray();
			int totalTiers = tiers.size();
			double progress = goal.get("progress").getAsLong();
			int finalTier = 0;
			for (JsonElement tier : tiers) {
				double currentTier = tier.getAsLong();
				if (progress < currentTier) {
					break;
				}
				finalTier++;
			}
			double nextTier = finalTier < totalTiers ? tiers.get(totalTiers - 1).getAsLong() : tiers
				.get(finalTier - 1)
				.getAsLong();
			int progressToNextTier = (int) Math.round(progress / nextTier * 100);
			if (progressToNextTier > 100) progressToNextTier = 100;
			String progressBar = generateProgressIndicator(progress, nextTier);
			String name = goal.get("name").getAsString();
			int nextTierNum = finalTier < totalTiers ? finalTier + 1 : totalTiers;

			String nextTierString = Utils.shortNumberFormat(nextTier, 0);
			String progressString = Utils.shortNumberFormat(progress, 0);
			tooltip.add(EnumChatFormatting.GREEN + name + " " + finalTier);
			tooltip.add(EnumChatFormatting.DARK_GRAY + "Community Goal");
			tooltip.add("");
			tooltip.add(
				EnumChatFormatting.GRAY +
					"Progress to " +
					name +
					" " +
					nextTierNum +
					": " +
					EnumChatFormatting.YELLOW +
					progressToNextTier +
					EnumChatFormatting.GOLD +
					"%"
			);
			tooltip.add(
				progressBar +
					EnumChatFormatting.YELLOW +
					" " +
					progressString +
					EnumChatFormatting.GOLD +
					"/" +
					EnumChatFormatting.YELLOW +
					nextTierString
			);
			tooltip.add("");
			tooltip.add(EnumChatFormatting.DARK_GRAY.toString() + EnumChatFormatting.ITALIC + "Community Goals are");
			tooltip.add(
				EnumChatFormatting.DARK_GRAY.toString() + EnumChatFormatting.ITALIC + "collaborative - anyone with a");
			tooltip.add(
				EnumChatFormatting.DARK_GRAY.toString() + EnumChatFormatting.ITALIC + "Bingo profile can help to reach");
			tooltip.add(EnumChatFormatting.DARK_GRAY.toString() + EnumChatFormatting.ITALIC + "the goal!");
			tooltip.add("");
			tooltip.add(EnumChatFormatting.DARK_GRAY.toString() + EnumChatFormatting.ITALIC + "The more you contribute");
			tooltip.add(
				EnumChatFormatting.DARK_GRAY.toString() + EnumChatFormatting.ITALIC + "towards the goal, the more you");
			tooltip.add(EnumChatFormatting.DARK_GRAY.toString() + EnumChatFormatting.ITALIC + "will be rewarded");

			if (finalTier == totalTiers) {
				tooltip.add("");
				tooltip.add(EnumChatFormatting.GREEN + "GOAL REACHED");
			}
		} else {
			tooltip.add(EnumChatFormatting.GREEN + goal.get("name").getAsString());
			tooltip.add(EnumChatFormatting.DARK_GRAY + "Personal Goal");
			tooltip.add("");
			tooltip.add(goal.get("lore").getAsString());
			tooltip.add("");
			tooltip.add(EnumChatFormatting.GRAY + "Reward");
			tooltip.add(EnumChatFormatting.GOLD + "1 Bingo Point");
			if (completed) {
				tooltip.add("");
				tooltip.add(EnumChatFormatting.GREEN + "GOAL REACHED");
			} else {
				tooltip.add("");
				tooltip.add(EnumChatFormatting.RED + "You have not reached this goal!");
			}
		}
		return tooltip;
	}

	private void showMissingDataMessage(int guiLeft, int guiTop) {
		String message = EnumChatFormatting.RED + "No Bingo data for current event!";
		Utils.drawStringCentered(message, guiLeft + 431 / 2f, guiTop + 101, true, 0);
	}

	private List<String> jsonArrayToStringList(JsonArray completedGoals) {
		List<String> list = new ArrayList<>();
		for (JsonElement completedGoal : completedGoals) {
			list.add(completedGoal.getAsString());
		}
		return list;
	}

	private List<JsonObject> jsonArrayToJsonObjectList(JsonArray goals) {
		List<JsonObject> list = new ArrayList<>();
		for (JsonElement goal : goals) {
			list.add(goal.getAsJsonObject());
		}

		return list;
	}

	private void loadBingoResources() {
		long currentTime = System.currentTimeMillis();

		//renew every 2 minutes
		if (currentTime - lastResourceRequest < 120 * 1000 && bingoGoals != null) return;
		lastResourceRequest = currentTime;

		NotEnoughUpdates.INSTANCE.manager.apiUtils
			.newAnonymousHypixelApiRequest("resources/skyblock/bingo")
			.requestJson()
			.thenAccept(jsonObject -> {
				if (jsonObject.has("success") && jsonObject.get("success").getAsBoolean()) {
					bingoGoals = jsonArrayToJsonObjectList(jsonObject.get("goals").getAsJsonArray());
					currentEventId = jsonObject.get("id").getAsInt();
				}
			});
	}
}
