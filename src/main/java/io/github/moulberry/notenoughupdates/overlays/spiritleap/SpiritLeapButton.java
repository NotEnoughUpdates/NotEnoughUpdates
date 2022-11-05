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

package io.github.moulberry.notenoughupdates.overlays.spiritleap;

import com.mojang.authlib.GameProfile;
import io.github.moulberry.notenoughupdates.mixins.AccessorGuiContainer;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.util.UUID;

public class SpiritLeapButton {
	private final String dungeonClass;
	private final String ign;

	public SpiritLeapButton(String dungeonClass, String ign) {
		this.dungeonClass = dungeonClass;
		this.ign = ign;

	}

	public String getDungeonClass() {
		return dungeonClass;
	}

	public String getIgn() {
		return ign;
	}

	public int getHeight() {
		return 55;
	}

	public int getWidth() {
		return 123;
	}

	public void render(int x, int y, ResourceLocation location, boolean dummy) {
		Minecraft
			.getMinecraft()
			.getTextureManager()
			.bindTexture(new ResourceLocation("notenoughupdates:auction_profit.png"));

		GL11.glColor4f(1, 1, 1, 1);
		GlStateManager.disableLighting();

		if (!dummy) {
			ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
			int width = scaledResolution.getScaledWidth();
			int height = scaledResolution.getScaledHeight();
			System.out.println(width + x);
			Utils.drawTexturedRect(
				x > 0 ? x : width + x,
				y > 0 ? y : height - y,
				180,
				101,
				0,
				180 / 256f,
				0,
				101 / 256f,
				GL11.GL_NEAREST
			);
			Utils.drawStringScaled("§a[" + dungeonClass + "] §f" + ign, Minecraft.getMinecraft().fontRendererObj,
				x > 0 ? x + 60 : (width + x) + 20,
				y > 0 ? y+4: (height - y),
				false, 0, 2
			);
		} else {
			Utils.drawTexturedRect(x, y, 180, 101, 0, 180 / 256f, 0, 101 / 256f, GL11.GL_NEAREST);
			Utils.drawStringScaled("§a[" + dungeonClass + "] §f" + ign, Minecraft.getMinecraft().fontRendererObj,
				x + 60, y + 4, true, 0, 2
			);
		}
		if (location == null) return;
		// fix head loc too when fixing all other stuffs
		Minecraft.getMinecraft().getTextureManager().bindTexture(location);
		drawScaledCustomSizeModalRect(x + 5, y + 4, 8.0F, 8, 8, 8, 32, 32, 64.0F, 64.0F);
	}

	// thank you i love you fellow sba contributo

	/**
	 * Adapted from SkyblockAddons under MIT license
	 *
	 * @link https://github.com/BiscuitDevelopment/SkyblockAddons/blob/master/LICENSE
	 * @author BiscuitDevelopment
	 */
	public static void drawScaledCustomSizeModalRect(
		float x,
		float y,
		float u,
		float v,
		float uWidth,
		float vHeight,
		float width,
		float height,
		float tileWidth,
		float tileHeight
	) {
		float f = 1.0F / tileWidth;
		float f1 = 1.0F / tileHeight;
		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer worldrenderer = tessellator.getWorldRenderer();
		worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
		worldrenderer.pos(x, y + height, 0.0D).tex(u * f, (v + vHeight) * f1).endVertex();
		worldrenderer.pos(x + width, y + height, 0.0D).tex((u + uWidth) * f, (v + vHeight) * f1).endVertex();
		worldrenderer.pos(x + width, y, 0.0D).tex((u + uWidth) * f, v * f1).endVertex();
		worldrenderer.pos(x, y, 0.0D).tex(u * f, v * f1).endVertex();
		tessellator.draw();
	}
}
