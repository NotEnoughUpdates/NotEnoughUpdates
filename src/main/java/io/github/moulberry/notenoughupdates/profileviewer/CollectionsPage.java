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

import com.google.gson.JsonObject;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.util.Constants;
import io.github.moulberry.notenoughupdates.util.Utils;
import java.awt.*;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

public class CollectionsPage extends GuiProfileViewerPage {

	public static final ResourceLocation pv_cols = new ResourceLocation("notenoughupdates:pv_cols.png");
	public static final ResourceLocation pv_elements = new ResourceLocation("notenoughupdates:pv_elements.png");
	private static final int COLLS_XCOUNT = 5;
	private static final int COLLS_YCOUNT = 4;
	private static final float COLLS_XPADDING = (190 - COLLS_XCOUNT * 20) / (float) (COLLS_XCOUNT + 1);
	private static final float COLLS_YPADDING = (202 - COLLS_YCOUNT * 20) / (float) (COLLS_YCOUNT + 1);
	private static final String[] romans = new String[] {
		"I",
		"II",
		"III",
		"IV",
		"V",
		"VI",
		"VII",
		"VIII",
		"IX",
		"X",
		"XI",
		"XII",
		"XIII",
		"XIV",
		"XV",
		"XVI",
		"XVII",
		"XIX",
		"XX",
	};
	private static final NumberFormat numberFormat = NumberFormat.getInstance(Locale.US);
	private static List<String> tooltipToDisplay = null;
	private static ItemStack selectedCollectionCategory = null;

	public CollectionsPage(GuiProfileViewer instance) {
		super(instance);
	}

	public void drawPage(int mouseX, int mouseY, float partialTicks) {
		int guiLeft = GuiProfileViewer.getGuiLeft();
		int guiTop = GuiProfileViewer.getGuiTop();

		Minecraft.getMinecraft().getTextureManager().bindTexture(pv_cols);
		Utils.drawTexturedRect(guiLeft, guiTop, getInstance().sizeX, getInstance().sizeY, GL11.GL_NEAREST);

		JsonObject collectionInfo = GuiProfileViewer.getProfile().getCollectionInfo(GuiProfileViewer.getProfileId());
		if (collectionInfo == null) {
			Utils.drawStringCentered(
				EnumChatFormatting.RED + "Collection API not enabled!",
				Minecraft.getMinecraft().fontRendererObj,
				guiLeft + 134,
				guiTop + 101,
				true,
				0
			);
			return;
		}
		JsonObject resourceCollectionInfo = ProfileViewer.getResourceCollectionInformation();
		if (resourceCollectionInfo == null) return;

		int collectionCatSize = ProfileViewer.getCollectionCatToCollectionMap().size();
		int collectionCatYSize = (int) (162f / (collectionCatSize - 1 + 0.0000001f));
		{
			int yIndex = 0;
			for (ItemStack stack : ProfileViewer.getCollectionCatToCollectionMap().keySet()) {
				if (selectedCollectionCategory == null) selectedCollectionCategory = stack;
				Minecraft.getMinecraft().getTextureManager().bindTexture(pv_elements);
				if (stack == selectedCollectionCategory) {
					Utils.drawTexturedRect(
						guiLeft + 7,
						guiTop + 10 + collectionCatYSize * yIndex,
						20,
						20,
						20 / 256f,
						0,
						20 / 256f,
						0,
						GL11.GL_NEAREST
					);
					Utils.drawItemStackWithText(stack, guiLeft + 10, guiTop + 13 + collectionCatYSize * yIndex, "" + (yIndex + 1));
				} else {
					Utils.drawTexturedRect(
						guiLeft + 7,
						guiTop + 10 + collectionCatYSize * yIndex,
						20,
						20,
						0,
						20 / 256f,
						0,
						20 / 256f,
						GL11.GL_NEAREST
					);
					Utils.drawItemStackWithText(stack, guiLeft + 9, guiTop + 12 + collectionCatYSize * yIndex, "" + (yIndex + 1));
				}
				yIndex++;
			}
		}

		Utils.drawStringCentered(
			selectedCollectionCategory.getDisplayName() + " Collections",
			Minecraft.getMinecraft().fontRendererObj,
			guiLeft + 134,
			guiTop + 14,
			true,
			4210752
		);

		JsonObject minionTiers = collectionInfo.get("minion_tiers").getAsJsonObject();
		JsonObject collectionTiers = collectionInfo.get("collection_tiers").getAsJsonObject();
		JsonObject maxAmounts = collectionInfo.get("max_amounts").getAsJsonObject();
		JsonObject totalAmounts = collectionInfo.get("total_amounts").getAsJsonObject();
		JsonObject personalAmounts = collectionInfo.get("personal_amounts").getAsJsonObject();

		List<String> collections = ProfileViewer.getCollectionCatToCollectionMap().get(selectedCollectionCategory);
		if (collections != null) {
			for (int i = 0; i < collections.size(); i++) {
				String collection = collections.get(i);
				if (collection != null) {
					ItemStack collectionItem = ProfileViewer.getCollectionToCollectionDisplayMap().get(collection);
					if (collectionItem != null) {
						int xIndex = i % COLLS_XCOUNT;
						int yIndex = i / COLLS_XCOUNT;

						float x = 39 + COLLS_XPADDING + (COLLS_XPADDING + 20) * xIndex;
						float y = 7 + COLLS_YPADDING + (COLLS_YPADDING + 20) * yIndex;

						String tierString;
						int tier = (int) Utils.getElementAsFloat(collectionTiers.get(collection), 0);
						if (tier > 20 || tier < 0) {
							tierString = String.valueOf(tier);
						} else {
							tierString = romans[tier];
						}
						float amount = Utils.getElementAsFloat(totalAmounts.get(collection), 0);
						float maxAmount = Utils.getElementAsFloat(maxAmounts.get(collection), 0);
						Color color = new Color(128, 128, 128, 255);
						int tierStringColour = color.getRGB();
						float completedness = 0;
						if (maxAmount > 0) {
							completedness = amount / maxAmount;
						}
						completedness = Math.min(1, completedness);
						if (maxAmounts.has(collection) && completedness >= 1) {
							tierStringColour = new Color(255, 215, 0).getRGB();
						}

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
						GlStateManager.color(1, 185 / 255f, 0, 1);
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
						Utils.drawItemStack(collectionItem, guiLeft + (int) x + 2, guiTop + (int) y + 2);

						if (mouseX > guiLeft + (int) x + 2 && mouseX < guiLeft + (int) x + 18) {
							if (mouseY > guiTop + (int) y + 2 && mouseY < guiTop + (int) y + 18) {
								tooltipToDisplay = new ArrayList<>();
								tooltipToDisplay.add(
									collectionItem.getDisplayName() +
									" " +
									(completedness >= 1 ? EnumChatFormatting.GOLD : EnumChatFormatting.GRAY) +
									tierString
								);
								tooltipToDisplay.add(
									"Collected: " + numberFormat.format(Utils.getElementAsFloat(personalAmounts.get(collection), 0))
								);
								tooltipToDisplay.add("Total Collected: " + numberFormat.format(amount));
							}
						}

						GlStateManager.color(1, 1, 1, 1);
						if (tier >= 0) {
							Utils.drawStringCentered(
								tierString,
								Minecraft.getMinecraft().fontRendererObj,
								guiLeft + x + 10,
								guiTop + y - 4,
								true,
								tierStringColour
							);
						}

						Utils.drawStringCentered(
							GuiProfileViewer.shortNumberFormat(amount, 0) + "",
							Minecraft.getMinecraft().fontRendererObj,
							guiLeft + x + 10,
							guiTop + y + 26,
							true,
							color.getRGB()
						);
					}
				}
			}
		}

		Utils.drawStringCentered(
			selectedCollectionCategory.getDisplayName() + " Minions",
			Minecraft.getMinecraft().fontRendererObj,
			guiLeft + 326,
			guiTop + 14,
			true,
			4210752
		);

		List<String> minions = ProfileViewer.getCollectionCatToMinionMap().get(selectedCollectionCategory);
		if (minions != null) {
			for (int i = 0; i < minions.size(); i++) {
				String minion = minions.get(i);
				if (minion != null) {
					JsonObject misc = Constants.MISC;
					float MAX_MINION_TIER = Utils.getElementAsFloat(Utils.getElement(misc, "minions." + minion + "_GENERATOR"), 11);

					int tier = (int) Utils.getElementAsFloat(minionTiers.get(minion), 0);
					JsonObject minionJson;
					if (tier == 0) {
						minionJson = NotEnoughUpdates.INSTANCE.manager.getItemInformation().get(minion + "_GENERATOR_1");
					} else {
						minionJson = NotEnoughUpdates.INSTANCE.manager.getItemInformation().get(minion + "_GENERATOR_" + tier);
					}

					if (minionJson != null) {
						int xIndex = i % COLLS_XCOUNT;
						int yIndex = i / COLLS_XCOUNT;

						float x = 231 + COLLS_XPADDING + (COLLS_XPADDING + 20) * xIndex;
						float y = 7 + COLLS_YPADDING + (COLLS_YPADDING + 20) * yIndex;

						String tierString;

						if (tier - 1 >= romans.length || tier - 1 < 0) {
							tierString = String.valueOf(tier);
						} else {
							tierString = romans[tier - 1];
						}

						Color color = new Color(128, 128, 128, 255);
						int tierStringColour = color.getRGB();
						float completedness = tier / MAX_MINION_TIER;

						completedness = Math.min(1, completedness);
						if (completedness >= 1) {
							tierStringColour = new Color(255, 215, 0).getRGB();
						}

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
						GlStateManager.color(1, 185 / 255f, 0, 1);
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

						Utils.drawItemStack(
							NotEnoughUpdates.INSTANCE.manager.jsonToStack(minionJson),
							guiLeft + (int) x + 2,
							guiTop + (int) y + 2
						);

						if (mouseX > guiLeft + (int) x + 2 && mouseX < guiLeft + (int) x + 18) {
							if (mouseY > guiTop + (int) y + 2 && mouseY < guiTop + (int) y + 18) {
								tooltipToDisplay =
									NotEnoughUpdates.INSTANCE.manager
										.jsonToStack(minionJson)
										.getTooltip(Minecraft.getMinecraft().thePlayer, false);
							}
						}

						GlStateManager.color(1, 1, 1, 1);
						if (tier >= 0) {
							Utils.drawStringCentered(
								tierString,
								Minecraft.getMinecraft().fontRendererObj,
								guiLeft + x + 10,
								guiTop + y - 4,
								true,
								tierStringColour
							);
						}
					}
				}
			}
		}

		if (tooltipToDisplay != null) {
			List<String> grayTooltip = new ArrayList<>(tooltipToDisplay.size());
			for (String line : tooltipToDisplay) {
				grayTooltip.add(EnumChatFormatting.GRAY + line);
			}
			Utils.drawHoveringText(
				grayTooltip,
				mouseX,
				mouseY,
				getInstance().width,
				getInstance().height,
				-1,
				Minecraft.getMinecraft().fontRendererObj
			);
			tooltipToDisplay = null;
		}
	}

	@Override
	public void keyTyped(char typedChar, int keyCode) throws IOException {
		ItemStack stack = null;
		Iterator<ItemStack> items = ProfileViewer.getCollectionCatToCollectionMap().keySet().iterator();
		switch (keyCode) {
			case Keyboard.KEY_5:
			case Keyboard.KEY_NUMPAD5:
				stack = items.next();
			case Keyboard.KEY_4:
			case Keyboard.KEY_NUMPAD4:
				stack = items.next();
			case Keyboard.KEY_3:
			case Keyboard.KEY_NUMPAD3:
				stack = items.next();
			case Keyboard.KEY_2:
			case Keyboard.KEY_NUMPAD2:
				stack = items.next();
			case Keyboard.KEY_1:
			case Keyboard.KEY_NUMPAD1:
				stack = items.next();
		}
		if (stack != null) {
			selectedCollectionCategory = stack;
		}
		Utils.playPressSound();
	}

	@Override
	public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
		int guiLeft = GuiProfileViewer.getGuiLeft();
		int guiTop = GuiProfileViewer.getGuiTop();

		int collectionCatSize = ProfileViewer.getCollectionCatToCollectionMap().size();
		int collectionCatYSize = (int) (162f / (collectionCatSize - 1 + 0.0000001f));
		int yIndex = 0;
		for (ItemStack stack : ProfileViewer.getCollectionCatToCollectionMap().keySet()) {
			if (mouseX > guiLeft + 7 && mouseX < guiLeft + 7 + 20) {
				if (mouseY > guiTop + 10 + collectionCatYSize * yIndex && mouseY < guiTop + 10 + collectionCatYSize * yIndex + 20) {
					selectedCollectionCategory = stack;
					Utils.playPressSound();
					return;
				}
			}
			yIndex++;
		}
	}
}
