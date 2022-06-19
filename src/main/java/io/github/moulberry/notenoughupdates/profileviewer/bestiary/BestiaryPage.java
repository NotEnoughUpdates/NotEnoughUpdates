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

package io.github.moulberry.notenoughupdates.profileviewer.bestiary;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.github.moulberry.notenoughupdates.profileviewer.GuiProfileViewer;
import io.github.moulberry.notenoughupdates.profileviewer.ProfileViewer;
import io.github.moulberry.notenoughupdates.util.Constants;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class BestiaryPage {

	private static int guiLeft;
	private static int guiTop;
	private static final ResourceLocation BESTIARY_TEXTURE = new ResourceLocation(
		"notenoughupdates:pv_bestiary_tab.png");
	public static final ResourceLocation pv_elements = new ResourceLocation("notenoughupdates:pv_elements.png");
	private static ItemStack selectedBestiaryLocation = null;
	private static final String[] romans = new String[]{
		"I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X", "XI",
		"XII", "XIII", "XIV", "XV", "XVI", "XVII", "XIX", "XX"
	};
	private static List<String> tooltipToDisplay = null;
	private static final NumberFormat numberFormat = NumberFormat.getInstance(Locale.US);
	private static final int COLLS_XCOUNT = 7;
	private static final int COLLS_YCOUNT = 4;
	private static final float COLLS_XPADDING = (190 - COLLS_XCOUNT * 20) / (float) (COLLS_XCOUNT + 1);
	private static final float COLLS_YPADDING = (202 - COLLS_YCOUNT * 20) / (float) (COLLS_YCOUNT + 1);
	public static void renderPage(int mouseX, int mouseY, int width, int height) {
		guiLeft = GuiProfileViewer.getGuiLeft();
		guiTop = GuiProfileViewer.getGuiTop();
		JsonObject profileInfo = GuiProfileViewer.getProfile().getProfileInformation(GuiProfileViewer.getProfileId());

		int bestiarySize = BestiaryData.getBestiaryLocations().size();
		int bestiaryXSize = (int) (350f / (bestiarySize - 1 + 0.0000001f));

		{
			int yIndex = 0;
			for (ItemStack stack : BestiaryData.getBestiaryLocations().keySet()) {
				Minecraft.getMinecraft().getTextureManager().bindTexture(pv_elements);
				if (stack == selectedBestiaryLocation) {
					Utils.drawTexturedRect(guiLeft + 30 + bestiaryXSize * yIndex, guiTop + 10, 20, 20,
						20 / 256f, 0, 20 / 256f, 0, GL11.GL_NEAREST
					);
					Utils.drawItemStack(
						stack,
						guiLeft + 32 + bestiaryXSize * yIndex,
						guiTop + 12
					);
				} else {
					Utils.drawTexturedRect(guiLeft + 30 + bestiaryXSize * yIndex, guiTop + 10, 20, 20,
						0, 20 / 256f, 0, 20 / 256f, GL11.GL_NEAREST
					);
					Utils.drawItemStack(stack, guiLeft + 32 + bestiaryXSize * yIndex, guiTop + 12);
				}
				yIndex++;
			}
		}
		ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
		width = scaledResolution.getScaledWidth();
		height = scaledResolution.getScaledHeight();

		Minecraft.getMinecraft().getTextureManager().bindTexture(BESTIARY_TEXTURE);
		Utils.drawTexturedRect(guiLeft, guiTop, 431, 202, GL11.GL_NEAREST);

		GlStateManager.color(1, 1, 1, 1);
		GlStateManager.disableLighting();
		RenderHelper.enableGUIStandardItemLighting();

		List<String> mobs = BestiaryData.getBestiaryLocations().get(selectedBestiaryLocation);
		if (mobs != null) {
			for (int i = 0; i < mobs.size(); i++) {

				String mob = mobs.get(i);
				if (mob != null) {
					ItemStack mobItem = BestiaryData.getBestiaryMobs().get(mob);
					if (mobItem != null) {
						int xIndex = i % COLLS_XCOUNT;
						int yIndex = i / COLLS_XCOUNT;

						float x = 23 + COLLS_XPADDING + (COLLS_XPADDING + 20) * xIndex;
						float y = 20 + COLLS_YPADDING + (COLLS_YPADDING + 20) * yIndex;

						Color color = new Color(128, 128, 128, 255);
						float completedness = 0;

						GlStateManager.color(1, 1, 1, 1);
						Minecraft.getMinecraft().getTextureManager().bindTexture(pv_elements);
						Utils.drawTexturedRect(guiLeft + x, guiTop + y, 20, 20 * (1 - completedness),
							0, 20 / 256f, 0, 20 * (1 - completedness) / 256f, GL11.GL_NEAREST
						);
						GlStateManager.color(1, 185 / 255f, 0, 1);
						Minecraft.getMinecraft().getTextureManager().bindTexture(pv_elements);
						Utils.drawTexturedRect(guiLeft + x, guiTop + y + 20 * (1 - completedness), 20, 20 * (completedness),
							0, 20 / 256f, 20 * (1 - completedness) / 256f, 20 / 256f, GL11.GL_NEAREST
						);
						Utils.drawItemStack(mobItem, guiLeft + (int) x + 2, guiTop + (int) y + 2);
						float kills = Utils.getElementAsFloat(Utils.getElement(profileInfo, "bestiary.kills_" + mob), 0);
						float deaths = Utils.getElementAsFloat(Utils.getElement(profileInfo, "bestiary.deaths_" + mob), 0);

						if (mouseX > guiLeft + (int) x + 2 && mouseX < guiLeft + (int) x + 18) {
							if (mouseY > guiTop + (int) y + 2 && mouseY < guiTop + (int) y + 18) {
								String type;
								String tierString = "I";
								if (BestiaryData.getMobType().get(mob) != null) {
									type = BestiaryData.getMobType().get(mob);
								} else {
									type = "MOB";
								}

								JsonObject leveling = Constants.LEVELING;
								JsonArray levelingArray = Utils.getElement(leveling, "bestiary." + type).getAsJsonArray();
								int levelCap = Utils.getElementAsInt(Utils.getElement(leveling, "bestiary.caps." + type), 0);
								ProfileViewer.Level level = ProfileViewer.getLevel(levelingArray, kills, levelCap, false);


								tooltipToDisplay = new ArrayList<>();
								tooltipToDisplay.add(mobItem.getDisplayName() + " " + (int) Math.floor(level.level));
								tooltipToDisplay.add("Kills: " + numberFormat.format(kills));
								tooltipToDisplay.add("Deaths: " + numberFormat.format(deaths));
								//tooltipToDisplay.add("Total Collected: " + numberFormat.format(amount));
							}
						}

						GlStateManager.color(1, 1, 1, 1);
//						if (tier >= 0) {
//							Utils.drawStringCentered(tierString, Minecraft.getMinecraft().fontRendererObj,
//								guiLeft + x + 10, guiTop + y - 4, true,
//								tierStringColour
//							);
//						}

						Utils.drawStringCentered(GuiProfileViewer.shortNumberFormat(kills, 0) + "", Minecraft.getMinecraft().fontRendererObj,
							guiLeft + x + 10, guiTop + y + 26, true,
							color.getRGB()
						);
					}
				}
			}
		}
		if (tooltipToDisplay != null) {
			List<String> grayTooltip = new ArrayList<>(tooltipToDisplay.size());
			for (String line : tooltipToDisplay) {
				grayTooltip.add(EnumChatFormatting.GRAY + line);
			}
			Utils.drawHoveringText(grayTooltip, mouseX, mouseY, width, height, -1, Minecraft.getMinecraft().fontRendererObj);
			tooltipToDisplay = null;
		}
	}

	public static void mouseReleased(int mouseX, int mouseY, int mouseButton) {
		int bestiarySize = BestiaryData.getBestiaryLocations().size();
		int bestiaryYSize = (int) (350f / (bestiarySize - 1 + 0.0000001f));
		int yIndex = 0;
		for (ItemStack stack : BestiaryData.getBestiaryLocations().keySet()) {
			if (mouseX > guiLeft + 30 + bestiaryYSize * yIndex &&
				mouseX < guiLeft + 30 + bestiaryYSize * yIndex + 20) {
				if (mouseY > guiTop + 10 &&	mouseY < guiTop + 10 + 20) {
					selectedBestiaryLocation = stack;
					Utils.playPressSound();
					return;
				}
			}
			yIndex++;
		}
	}
}
