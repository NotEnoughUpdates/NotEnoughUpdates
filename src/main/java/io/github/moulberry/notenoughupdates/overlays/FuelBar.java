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
import io.github.moulberry.notenoughupdates.core.config.gui.GuiPositionEditor;
import io.github.moulberry.notenoughupdates.util.SBInfo;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.client.config.GuiUtils;
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
			int x = position.getAbsX(scaledResolution, NotEnoughUpdates.INSTANCE.config.mining.drillFuelBarWidth + 2);
			int y = position.getAbsY(scaledResolution, 5);
			x -= NotEnoughUpdates.INSTANCE.config.mining.drillFuelBarWidth / 2 - 1;
			renderBar(x, y + 6, NotEnoughUpdates.INSTANCE.config.mining.drillFuelBarWidth + 2, fuelAmount);

			String str = fuelString.replace("\u00A77", EnumChatFormatting.DARK_GREEN.toString()) +
				EnumChatFormatting.GOLD + String.format(" (%d%%)", (int) (fuelAmount * 100));

			GlStateManager.enableBlend();
			GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA,
				GL11.GL_ONE_MINUS_SRC_ALPHA,
				GL11.GL_ONE,
				GL11.GL_ONE_MINUS_SRC_ALPHA
			);
			GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA,
				GL11.GL_ONE_MINUS_SRC_ALPHA,
				GL11.GL_ONE,
				GL11.GL_ONE_MINUS_SRC_ALPHA
			);

			String clean = Utils.cleanColourNotModifiers(str);
			for (int xO = -2; xO <= 2; xO++) {
				for (int yO = -2; yO <= 2; yO++) {
					if (Math.abs(xO) != Math.abs(yO)) {
						Minecraft.getMinecraft().fontRendererObj.drawString(clean,
							x + 2 + xO / 2f,
							y + yO / 2f,
							new Color(0, 0, 0, 200 / Math.max(Math.abs(xO), Math.abs(yO))).getRGB(),
							false
						);
					}
				}
			}
			Minecraft.getMinecraft().fontRendererObj.drawString(str, x + 2, y, 0xffffff, false);
			Utils.pushGuiScale(0);
			GlStateManager.popMatrix();
		}
	}

	private void renderBar(float x, float y, float xSize, float completed) {
		Minecraft.getMinecraft().getTextureManager().bindTexture(FUEL_BAR);

		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, 0);

		Color c = Color.getHSBColor(148 / 360f * completed - 20 / 360f, 0.9f, 1 - 0.5f * completed);
		GlStateManager.color(c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f, 1);

		Utils.pushGuiScale(NotEnoughUpdates.INSTANCE.config.locationedit.guiScale);

		int w = (int) xSize;
		int w_2 = w / 2;
		int k = (int) Math.min(w, Math.ceil(completed * w));
		Utils.drawTexturedRect(x, y, w_2, 5, 0, w_2 / 181f, 0, 0.5f, GL11.GL_NEAREST);
		Utils.drawTexturedRect(x + w_2, y, w_2, 5, 1 - w_2 / 181f, 1f, 0, 0.5f, GL11.GL_NEAREST);
		if (k > 0) {
			Utils.drawTexturedRect(x, y, Math.min(w_2, k), 5, 0, Math.min(w_2, k) / 181f, 0.5f, 1, GL11.GL_NEAREST);
			if (completed > 0.5f) {
				Utils.drawTexturedRect(x + w_2, y, k - w_2, 5, 1 - w_2 / 181f, 1 + (k - w) / 181f, 0.5f, 1, GL11.GL_NEAREST);
			}
		}

		GlStateManager.popMatrix();
	}
}
