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

package io.github.moulberry.notenoughupdates.overlays;

import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.autosubscribe.NEUAutoSubscribe;
import io.github.moulberry.notenoughupdates.core.config.Position;
import io.github.moulberry.notenoughupdates.core.config.GuiPositionEditor;
import io.github.moulberry.notenoughupdates.util.SBInfo;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import java.awt.*;

@NEUAutoSubscribe
public class FuelBar {
	public static final ResourceLocation FUEL_BAR = new ResourceLocation("notenoughupdates:fuel_bar.png");

	private float fuelAmount = -1;
	private String fuelString = "";

	@SubscribeEvent
	public void onTick(TickEvent.ClientTickEvent event) {
		fuelAmount = -1;

		if (SBInfo.getInstance().getLocation() == null) return;
		if (!(SBInfo.getInstance().getLocation().startsWith("mining_") || SBInfo.getInstance().getLocation().equals(
			"crystal_hollows")))
			return;

		if (Minecraft.getMinecraft().thePlayer == null) return;
		if (!NotEnoughUpdates.INSTANCE.config.mining.drillFuelBar) return;

		ItemStack held = Minecraft.getMinecraft().thePlayer.getHeldItem();
		if (held != null) {
			String internalname = NotEnoughUpdates.INSTANCE.manager.getInternalNameForItem(held);
			if (internalname != null && (internalname.contains("_DRILL_") || internalname.equals("DIVAN_DRILL"))) {
				String[] lore = NotEnoughUpdates.INSTANCE.manager.getLoreFromNBT(held.getTagCompound());
				for (String line : lore) {
					try {
						if (line.startsWith("\u00A77Fuel: ")) {
							String[] split = Utils.cleanColour(line).split("/");
							if (split.length == 2) {
								String fuelS = split[0].split(" ")[1];
								int fuel = Integer.parseInt(fuelS.replace(",", "").trim());

								String maxFuelS = split[1].trim();
								int mult = 1;
								if (maxFuelS.endsWith("k")) {
									mult = 1000;
								}
								int maxFuel = Integer.parseInt(maxFuelS.replace("k", "").trim()) * mult;
								fuelAmount = fuel / (float) maxFuel;
								if (fuelAmount > 1) {
									fuelAmount = 1;
								}
								fuelString = line;

								break;
							}
						}
					} catch (Exception ignored) {
					}
				}
			}
		}
	}

	@SubscribeEvent
	public void onRenderScreen(RenderGameOverlayEvent.Post event) {
		if (!GuiPositionEditor.renderDrill) {
			if (fuelAmount < 0) return;
			if (!NotEnoughUpdates.INSTANCE.config.mining.drillFuelBar) return;
		} else {
			fuelAmount = .3f;
		}
		if (event.type == RenderGameOverlayEvent.ElementType.ALL) {
			GlStateManager.pushMatrix();
			ScaledResolution scaledResolution = Utils.pushGuiScale(NotEnoughUpdates.INSTANCE.config.locationedit.guiScale);

			Position position = NotEnoughUpdates.INSTANCE.config.mining.drillFuelBarPosition;
			int x = position.getAbsX(scaledResolution, NotEnoughUpdates.INSTANCE.config.mining.drillFuelBarWidth);
			int y = position.getAbsY(scaledResolution, 12);
			x -= NotEnoughUpdates.INSTANCE.config.mining.drillFuelBarWidth / 2;
			renderBar(x, y + 4, NotEnoughUpdates.INSTANCE.config.mining.drillFuelBarWidth, fuelAmount);

			String str = fuelString.replace("\u00A77", EnumChatFormatting.DARK_GREEN.toString()) +
				EnumChatFormatting.GOLD + String.format(" (%d%%)", (int) (fuelAmount * 100));

			GlStateManager.enableBlend();
			GL14.glBlendFuncSeparate(
				GL11.GL_SRC_ALPHA,
				GL11.GL_ONE_MINUS_SRC_ALPHA,
				GL11.GL_ONE,
				GL11.GL_ONE_MINUS_SRC_ALPHA
			);
			GlStateManager.tryBlendFuncSeparate(
				GL11.GL_SRC_ALPHA,
				GL11.GL_ONE_MINUS_SRC_ALPHA,
				GL11.GL_ONE,
				GL11.GL_ONE_MINUS_SRC_ALPHA
			);

			String clean = Utils.cleanColourNotModifiers(str);
			for (int xO = -2; xO <= 2; xO++) {
				for (int yO = -2; yO <= 2; yO++) {
					if (Math.abs(xO) != Math.abs(yO)) {
						Minecraft.getMinecraft().fontRendererObj.drawString(clean,
							x + 2 + xO / 2f, y + yO / 2f,
							new Color(0, 0, 0, 200 / Math.max(Math.abs(xO), Math.abs(yO))).getRGB(), false
						);
					}
				}
			}
			Minecraft.getMinecraft().fontRendererObj.drawString(str,
				x + 2, y, 0xffffff, false
			);
			Utils.pushGuiScale(0);
			GlStateManager.popMatrix();
		}
	}

	private void renderBarCap(float x, float y, boolean left, float rCapSize, float completion) {
		float size = left ? 10 : rCapSize;
		int startTexX = left ? 0 : (170 + 11 - (int) Math.ceil(rCapSize));

		if (completion < 1) {
			Utils.drawTexturedRect(x, y, size, 5,
				startTexX / 181f, 1, 0 / 10f, 5 / 10f, GL11.GL_NEAREST
			);
		}
		if (completion > 0) {
			Utils.drawTexturedRect(x, y, size * completion, 5,
				startTexX / 181f, (startTexX + size * completion) / 181f, 5 / 10f, 10 / 10f, GL11.GL_NEAREST
			);
		}
	}

	private void renderBarNotch(float x, float y, int id, float completion) {
		id = id % 16;

		int startTexX = 10 + id * 10;

		if (completion < 1) {
			Utils.drawTexturedRect(x, y, 10, 5,
				startTexX / 181f, (startTexX + 10) / 181f, 0 / 10f, 5 / 10f, GL11.GL_NEAREST
			);
		}
		if (completion > 0) {
			Utils.drawTexturedRect(x, y, 10 * completion, 5,
				startTexX / 181f, (startTexX + 10 * completion) / 181f, 5 / 10f, 10 / 10f, GL11.GL_NEAREST
			);
		}

	}

	private void renderBar(float x, float y, float xSize, float completed) {
		Minecraft.getMinecraft().getTextureManager().bindTexture(FUEL_BAR);

		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, 0);
		xSize = xSize / 1.5f;
		GlStateManager.scale(1.5f, 1.5f, 1);

		Color c = Color.getHSBColor(148 / 360f * completed - 20 / 360f, 0.9f, 1 - 0.5f * completed);
		GlStateManager.color(c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f, 1);

		float offsetCompleteX = xSize * completed;
		for (int xOffset = 0; xOffset < xSize; xOffset += 10) {
			float notchCompl = 1;
			if (xOffset > offsetCompleteX) {
				notchCompl = 0;
			} else if (xOffset + 10 > offsetCompleteX) {
				notchCompl = (offsetCompleteX - xOffset) / 10f;
			}
			if (xOffset == 0) {
				renderBarCap(0, 0, true, 0, notchCompl);
			} else if (xOffset + 11 > xSize) {
				renderBarCap(xOffset, 0, false, xSize - xOffset, notchCompl);
			} else {
				renderBarNotch(xOffset, 0, xOffset / 10, notchCompl);
			}
		}

		GlStateManager.popMatrix();
	}
}
