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
import io.github.moulberry.notenoughupdates.core.util.StringUtils;
import io.github.moulberry.notenoughupdates.util.Constants;
import io.github.moulberry.notenoughupdates.util.Utils;
import io.github.moulberry.notenoughupdates.util.hypixelapi.ProfileCollectionInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static io.github.moulberry.notenoughupdates.profileviewer.GuiProfileViewer.pv_elements;

public class CollectionsPage extends GuiProfileViewerPage {

	private static final ResourceLocation pv_cols = new ResourceLocation("notenoughupdates:pv_cols.png");
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
	private static List<String> tooltipToDisplay = null;
	private static ItemStack selectedCollectionCategory = null;
	private int page = 0;
	private int maxPage = 0;

	public CollectionsPage(GuiProfileViewer instance) {
		super(instance);
	}

	public void drawPage(int mouseX, int mouseY, float partialTicks) {
		int guiLeft = GuiProfileViewer.getGuiLeft();
		int guiTop = GuiProfileViewer.getGuiTop();

		Minecraft.getMinecraft().getTextureManager().bindTexture(pv_cols);
		Utils.drawTexturedRect(guiLeft, guiTop, getInstance().sizeX, getInstance().sizeY, GL11.GL_NEAREST);

		SkyblockProfiles.SkyblockProfile selectedProfile = getSelectedProfile();
		if (selectedProfile == null) {
			return;
		}

		JsonObject resourceCollectionInfo = ProfileViewer.getOrLoadCollectionsResource();
		if (resourceCollectionInfo == null) {
			return;
		}

		ProfileCollectionInfo collectionInfo = selectedProfile.getCollectionInfo();
		if (collectionInfo == null) {
			Utils.drawStringCentered(
				EnumChatFormatting.RED + "Collection API not enabled!",
				guiLeft + 134, guiTop + 101, true, 0
			);
			return;
		}

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

		List<String> collections = ProfileViewer.getCollectionCatToCollectionMap().get(selectedCollectionCategory);
		List<String> minions = ProfileViewer.getCollectionCatToMinionMap().get(selectedCollectionCategory);

		maxPage = Math.max((collections != null ? collections.size() : 0) / 20, (minions != null ? minions.size() : 0) / 20);

		if (maxPage != 0) {
			boolean leftHovered = false;
			boolean rightHovered = false;
			if (Mouse.isButtonDown(0)) {
				if (mouseY > guiTop + 6 && mouseY < guiTop + 22) {
					if (mouseX > guiLeft + 100 - 20 - 12 && mouseX < guiLeft + 100 - 20) {
						leftHovered = true;
					} else if (mouseX > guiLeft + 100 + 20 + 250 && mouseX < guiLeft + 100 + 20 + 12 + 250) {
						rightHovered = true;
					}
				}
			}
			Minecraft.getMinecraft().getTextureManager().bindTexture(GuiProfileViewer.resource_packs);

			if (page > 0) {
				Utils.drawTexturedRect(
					guiLeft + 100 - 15 - 12,
					guiTop + 6,
					12,
					16,
					29 / 256f,
					53 / 256f,
					!leftHovered ? 0 : 32 / 256f,
					!leftHovered ? 32 / 256f : 64 / 256f,
					GL11.GL_NEAREST
				);
			}
			if (page < 1) {
				Utils.drawTexturedRect(
					guiLeft + 100 + 15 + 250,
					guiTop + 6,
					12,
					16,
					5 / 256f,
					29 / 256f,
					!rightHovered ? 0 : 32 / 256f,
					!rightHovered ? 32 / 256f : 64 / 256f,
					GL11.GL_NEAREST
				);
			}
		}

		Utils.drawStringCentered(
			selectedCollectionCategory.getDisplayName() + " Collections",
			guiLeft + 134, guiTop + 14, true, 4210752
		);

		if (collections != null) {
			for (int i = page * 20, j = 0; i < Math.min((page + 1) * 20, collections.size()); i++, j++) {
				String collection = collections.get(i);
				if (collection == null) {
					continue;
				}
				ProfileCollectionInfo.CollectionInfo thisCollection = collectionInfo.getCollections().get(collection);
				if (thisCollection == null) {
					Utils.showOutdatedRepoNotification();
					continue;
				}
				ItemStack collectionItem = ProfileViewer.getCollectionToCollectionDisplayMap().get(collection);
				if (collectionItem == null) {
					continue;
				}
				int xIndex = j % COLLS_XCOUNT;
				int yIndex = j / COLLS_XCOUNT;

				float x = 39 + COLLS_XPADDING + (COLLS_XPADDING + 20) * xIndex;
				float y = 7 + COLLS_YPADDING + (COLLS_YPADDING + 20) * yIndex;

				String tierString;
				int tier = thisCollection.getUnlockedTiers().size();
				if (tier > 20 || tier == 0) {
					tierString = String.valueOf(tier);
				} else {
					tierString = romans[tier - 1];
				}
				BigInteger amount = thisCollection.getTotalCollectionCount();
				BigInteger maxAmount = BigInteger.valueOf(thisCollection.getCollection().getTiers().get(thisCollection.getCollection().getTiers().size() - 1).getAmountRequired());
				Color color = new Color(128, 128, 128, 255);
				int tierStringColour = color.getRGB();
				float completedness = 0;
				if (maxAmount.compareTo(BigInteger.ZERO) > 0) {
					if (amount.compareTo(maxAmount) > 0) {
						completedness = 1;
					} else {
						completedness = amount.floatValue() / maxAmount.floatValue();
					}
				}
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
							"Collected: " + StringUtils.formatNumber(thisCollection.getPersonalCollectionCount())
						);
						tooltipToDisplay.add("Total Collected: " + StringUtils.formatNumber(amount));
					}
				}

				GlStateManager.color(1, 1, 1, 1);
				Utils.drawStringCentered(tierString, guiLeft + x + 10, guiTop + y - 4, true, tierStringColour);

				Utils.drawStringCentered(
					StringUtils.shortNumberFormat(amount), guiLeft + x + 10, guiTop + y + 26, true, color.getRGB());
			}
		}

		Utils.drawStringCentered(
			selectedCollectionCategory.getDisplayName() + " Minions", guiLeft + 326, guiTop + 14, true, 4210752);

		if (minions != null) {
			for (int i = page * 20, j = 0; i < Math.min((page + 1) * 20, minions.size()); i++, j++) {
				String minion = minions.get(i);
				if (minion == null) {
					continue;
				}
				JsonObject misc = Constants.MISC;
				float MAX_MINION_TIER = Utils.getElementAsFloat(Utils.getElement(misc, "minions." + minion + "_GENERATOR"), 11);

				int tier = collectionInfo.getCraftedGenerators().getOrDefault(minion, 0);
				JsonObject minionJson;
				if (tier == 0) {
					minionJson = NotEnoughUpdates.INSTANCE.manager.getItemInformation().get(minion + "_GENERATOR_1");
				} else {
					minionJson = NotEnoughUpdates.INSTANCE.manager.getItemInformation().get(minion + "_GENERATOR_" + tier);
				}

				if (minionJson == null) {
					continue;
				}
				int xIndex = j % COLLS_XCOUNT;
				int yIndex = j / COLLS_XCOUNT;

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
					Utils.drawStringCentered(tierString, guiLeft + x + 10, guiTop + y - 4, true, tierStringColour);
				}
			}
		}

		if (tooltipToDisplay != null) {
			List<String> grayTooltip = new ArrayList<>(tooltipToDisplay.size());
			for (String line : tooltipToDisplay) {
				grayTooltip.add(EnumChatFormatting.GRAY + line);
			}
			Utils.drawHoveringText(grayTooltip, mouseX, mouseY, getInstance().width, getInstance().height, -1);
			tooltipToDisplay = null;
		}
	}

	@Override
	public void keyTyped(char typedChar, int keyCode) throws IOException {
		ItemStack stack = null;
		Iterator<ItemStack> items = ProfileViewer.getCollectionCatToCollectionMap().keySet().iterator();
		switch (keyCode) {
			case Keyboard.KEY_6:
			case Keyboard.KEY_NUMPAD6:
				items.next();
			case Keyboard.KEY_5:
			case Keyboard.KEY_NUMPAD5:
				items.next();
			case Keyboard.KEY_4:
			case Keyboard.KEY_NUMPAD4:
				items.next();
			case Keyboard.KEY_3:
			case Keyboard.KEY_NUMPAD3:
				items.next();
			case Keyboard.KEY_2:
			case Keyboard.KEY_NUMPAD2:
				items.next();
			case Keyboard.KEY_1:
			case Keyboard.KEY_NUMPAD1:
				stack = items.next();
		}
		if (stack != null) {
			selectedCollectionCategory = stack;
			page = 0;
		}
		Utils.playPressSound();
	}

	@Override
	public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
		int guiLeft = GuiProfileViewer.getGuiLeft();
		int guiTop = GuiProfileViewer.getGuiTop();

		if (maxPage != 0) {
			if (mouseY > guiTop + 6 && mouseY < guiTop + 22) {
				if (mouseX > guiLeft + 100 - 15 - 12 && mouseX < guiLeft + 100 - 20) {
					if (page > 0) {
						page--;
						return;
					}
				} else if (mouseX > guiLeft + 100 + 15 + 250 && mouseX < guiLeft + 100 + 20 + 12 + 250) {
					if (page < 1) {
						page++;
						return;
					}
				}
			}
		}

		int collectionCatSize = ProfileViewer.getCollectionCatToCollectionMap().size();
		int collectionCatYSize = (int) (162f / (collectionCatSize - 1 + 0.0000001f));
		int yIndex = 0;
		for (ItemStack stack : ProfileViewer.getCollectionCatToCollectionMap().keySet()) {
			if (mouseX > guiLeft + 7 && mouseX < guiLeft + 7 + 20) {
				if (mouseY > guiTop + 10 + collectionCatYSize * yIndex && mouseY < guiTop + 10 + collectionCatYSize * yIndex + 20) {
					selectedCollectionCategory = stack;
					page = 0;
					Utils.playPressSound();
					return;
				}
			}
			yIndex++;
		}
	}
}
