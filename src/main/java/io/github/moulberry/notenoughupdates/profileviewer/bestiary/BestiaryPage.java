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
import io.github.moulberry.notenoughupdates.core.util.StringUtils;
import io.github.moulberry.notenoughupdates.profileviewer.GuiProfileViewer;
import io.github.moulberry.notenoughupdates.profileviewer.GuiProfileViewerPage;
import io.github.moulberry.notenoughupdates.profileviewer.ProfileViewer;
import io.github.moulberry.notenoughupdates.profileviewer.ProfileViewerUtils;
import io.github.moulberry.notenoughupdates.profileviewer.SkyblockProfiles;
import io.github.moulberry.notenoughupdates.util.Constants;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static io.github.moulberry.notenoughupdates.profileviewer.GuiProfileViewer.pv_elements;

public class BestiaryPage extends GuiProfileViewerPage {

	private static final ResourceLocation BESTIARY_TEXTURE = new ResourceLocation("notenoughupdates:pv_bestiary_tab.png");
	private static final int XCOUNT = 7;
	private static final int YCOUNT = 5;
	private static final float XPADDING = (190 - XCOUNT * 20) / (float) (XCOUNT + 1);
	private static final float YPADDING = (202 - YCOUNT * 20) / (float) (YCOUNT + 1);
	private ItemStack selectedBestiaryLocation = null;
	private List<String> tooltipToDisplay = null;

	public BestiaryPage(GuiProfileViewer instance) {
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

		int bestiarySize = BestiaryData.getBestiaryLocations().size();
		int bestiaryXSize = (int) (350f / (bestiarySize - 1 + 0.0000001f));

		{
			int yIndex = 0;
			for (ItemStack stack : BestiaryData.getBestiaryLocations().keySet()) {
				Minecraft.getMinecraft().getTextureManager().bindTexture(pv_elements);
				if (mouseX > guiLeft + 30 + bestiaryXSize * yIndex && mouseX < guiLeft + 30 + bestiaryXSize * yIndex + 20) {
					if (mouseY > guiTop + 10 && mouseY < guiTop + 10 + 20) {
						tooltipToDisplay = stack.getTooltip(Minecraft.getMinecraft().thePlayer, false);
					}
				}
				if (stack == selectedBestiaryLocation) {
					Utils.drawTexturedRect(
						guiLeft + 30 + bestiaryXSize * yIndex,
						guiTop + 10,
						20,
						20,
						20 / 256f,
						0,
						20 / 256f,
						0,
						GL11.GL_NEAREST
					);
					Utils.drawItemStack(stack, guiLeft + 32 + bestiaryXSize * yIndex, guiTop + 12);
				} else {
					Utils.drawTexturedRect(
						guiLeft + 30 + bestiaryXSize * yIndex,
						guiTop + 10,
						20,
						20,
						0,
						20 / 256f,
						0,
						20 / 256f,
						GL11.GL_NEAREST
					);
					Utils.drawItemStack(stack, guiLeft + 32 + bestiaryXSize * yIndex, guiTop + 12);
				}
				yIndex++;
			}
		}
		ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
		int width = scaledResolution.getScaledWidth();
		int height = scaledResolution.getScaledHeight();

		Minecraft.getMinecraft().getTextureManager().bindTexture(BESTIARY_TEXTURE);
		Utils.drawTexturedRect(guiLeft, guiTop, 431, 202, GL11.GL_NEAREST);

		GlStateManager.color(1, 1, 1, 1);
		Color color = new Color(128, 128, 128, 255);
		Utils.renderAlignedString(
			EnumChatFormatting.RED + "Bestiary Level: ",
			EnumChatFormatting.GRAY + "" + (float) getSelectedProfile().getBestiaryLevel() / 10,
			guiLeft + 220,
			guiTop + 50,
			110
		);

		GlStateManager.disableLighting();
		RenderHelper.enableGUIStandardItemLighting();
		List<String> mobs = BestiaryData.getBestiaryLocations().get(selectedBestiaryLocation);
		if (mobs != null) {
			for (int i = 0; i < mobs.size(); i++) {
				String mob = mobs.get(i);
				if (mob != null) {
					ItemStack mobItem = BestiaryData.getBestiaryMobs().get(mob);
					if (mobItem != null) {
						int xIndex = i % XCOUNT;
						int yIndex = i / XCOUNT;

						float x = 23 + XPADDING + (XPADDING + 20) * xIndex;
						float y = 30 + YPADDING + (YPADDING + 20) * yIndex;

						float completedness = 0;

						GlStateManager.color(1, 1, 1, 1);
						Minecraft.getMinecraft().getTextureManager().bindTexture(pv_elements);
						Utils.drawTexturedRect(
							guiLeft + x,
							guiTop + y,
							20,
							20 * (1 - completedness),
							0,
							20 / 256f,
							0,
							20 * (1 - completedness) / 256f,
							GL11.GL_NEAREST
						);
						//GlStateManager.color(1, 185 / 255f, 0, 1);
						Minecraft.getMinecraft().getTextureManager().bindTexture(pv_elements);
						Utils.drawTexturedRect(
							guiLeft + x,
							guiTop + y + 20 * (1 - completedness),
							20,
							20 * (completedness),
							0,
							20 / 256f,
							20 * (1 - completedness) / 256f,
							20 / 256f,
							GL11.GL_NEAREST
						);
						Utils.drawItemStack(mobItem, guiLeft + (int) x + 2, guiTop + (int) y + 2);
						float kills = Utils.getElementAsFloat(Utils.getElement(profileInfo, "bestiary.kills_" + mob), 0);
						float deaths = Utils.getElementAsFloat(Utils.getElement(profileInfo, "bestiary.deaths_" + mob), 0);

						String type;
						if (BestiaryData.getMobType().get(mob) != null) {
							type = BestiaryData.getMobType().get(mob);
						} else {
							type = "MOB";
						}
						JsonObject leveling = Constants.LEVELING;
						ProfileViewer.Level level = null;
						if (leveling != null && Utils.getElement(leveling, "bestiary." + type) != null) {
							JsonArray levelingArray = Utils.getElement(leveling, "bestiary." + type).getAsJsonArray();
							int levelCap = Utils.getElementAsInt(Utils.getElement(leveling, "bestiary.caps." + type), 0);
							level = ProfileViewerUtils.getLevel(levelingArray, kills, levelCap, false);
						} else {
							Utils.showOutdatedRepoNotification();
						}

						float levelNum = -1;
						if (level != null) {
							levelNum = level.level;
						}
						if (mouseX > guiLeft + (int) x + 2 && mouseX < guiLeft + (int) x + 18) {
							if (mouseY > guiTop + (int) y + 2 && mouseY < guiTop + (int) y + 18) {
								tooltipToDisplay = new ArrayList<>();
								tooltipToDisplay.add(
									mobItem.getDisplayName() + " " + ((levelNum == -1) ? "?" : (int) Math.floor(levelNum))
								);
								tooltipToDisplay.add(
									EnumChatFormatting.GRAY + "Kills: " + EnumChatFormatting.GREEN + StringUtils.formatNumber(kills)
								);
								tooltipToDisplay.add(
									EnumChatFormatting.GRAY + "Deaths: " + EnumChatFormatting.GREEN + StringUtils.formatNumber(deaths)
								);
								if (level != null) {
									String progressStr;
									if (level.maxed) {
										progressStr = EnumChatFormatting.GOLD + "MAXED!";
									} else {
										progressStr = EnumChatFormatting.AQUA +
											StringUtils.shortNumberFormat(Math.round((levelNum % 1) * level.maxXpForLevel)) +
											"/" +
											StringUtils.shortNumberFormat(level.maxXpForLevel);
									}
									tooltipToDisplay.add(EnumChatFormatting.GRAY + "Progress: " + progressStr);
								}
							}
						}

						GlStateManager.color(1, 1, 1, 1);
						//						if (tier >= 0) {
						//							Utils.drawStringCentered(tierString,
						//								guiLeft + x + 10, guiTop + y - 4, true,
						//								tierStringColour
						//							);
						//						}
						Utils.drawStringCentered(
							(int) Math.floor(levelNum) + "",
							guiLeft + x + 10, guiTop + y + 26, true, color.getRGB()
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
			Utils.drawHoveringText(grayTooltip, mouseX, mouseY, width, height, -1);
			tooltipToDisplay = null;
		}
	}

	@Override
	public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
		int guiLeft = GuiProfileViewer.getGuiLeft();
		int guiTop = GuiProfileViewer.getGuiTop();

		int bestiarySize = BestiaryData.getBestiaryLocations().size();
		int bestiaryYSize = (int) (350f / (bestiarySize - 1 + 0.0000001f));
		int yIndex = 0;
		for (ItemStack stack : BestiaryData.getBestiaryLocations().keySet()) {
			if (mouseX > guiLeft + 30 + bestiaryYSize * yIndex && mouseX < guiLeft + 30 + bestiaryYSize * yIndex + 20) {
				if (mouseY > guiTop + 10 && mouseY < guiTop + 10 + 20) {
					selectedBestiaryLocation = stack;
					Utils.playPressSound();
					return;
				}
			}
			yIndex++;
		}
	}
}
