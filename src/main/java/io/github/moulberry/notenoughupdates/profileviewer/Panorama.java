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

import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.Project;

import java.io.IOException;
import java.util.HashMap;

public class Panorama {

	private static final HashMap<String, ResourceLocation[]> panoramasMap = new HashMap<>();
	private static ResourceLocation backgroundTexture = null;
	private static int lastWidth = 0;
	private static int lastHeight = 0;

	public static synchronized ResourceLocation[] getPanoramasForLocation(String location, String identifier) {
		if (panoramasMap.containsKey(location + identifier)) return panoramasMap.get(location + identifier);
		try {
			ResourceLocation[] panoramasArray = new ResourceLocation[6];
			for (int i = 0; i < 6; i++) {
				panoramasArray[i] =
					new ResourceLocation("notenoughupdates:panoramas/" + location + "_" + identifier + "/panorama_" + i + ".jpg");
				Minecraft.getMinecraft().getResourceManager().getResource(panoramasArray[i]);
			}
			panoramasMap.put(location + identifier, panoramasArray);
			return panoramasArray;
		} catch (IOException e) {
			try {
				ResourceLocation[] panoramasArray = new ResourceLocation[6];
				for (int i = 0; i < 6; i++) {
					panoramasArray[i] = new ResourceLocation("notenoughupdates:panoramas/" + location + "/panorama_" + i + ".jpg");
					Minecraft.getMinecraft().getResourceManager().getResource(panoramasArray[i]);
				}
				panoramasMap.put(location + identifier, panoramasArray);
				return panoramasArray;
			} catch (IOException e2) {
				ResourceLocation[] panoramasArray = new ResourceLocation[6];
				for (int i = 0; i < 6; i++) {
					panoramasArray[i] = new ResourceLocation("notenoughupdates:panoramas/unknown/panorama_" + i + ".jpg");
				}
				panoramasMap.put(location + identifier, panoramasArray);
				return panoramasArray;
			}
		}
	}

	public static void drawPanorama(
		float angle,
		int x,
		int y,
		int width,
		int height,
		float yOffset,
		float zOffset,
		ResourceLocation[] panoramas
	) {
		if (!OpenGlHelper.isFramebufferEnabled()) {
			Minecraft.getMinecraft().getTextureManager().bindTexture(panoramas[0]);

			float aspect = width / (float) height;
			Utils.drawTexturedRect(x, y, width, height, 0.5f - aspect / 2, 0.5f + aspect / 2, 0, 1);

			return;
		}

		Minecraft.getMinecraft().getFramebuffer().unbindFramebuffer();

		ScaledResolution scaledresolution = new ScaledResolution(Minecraft.getMinecraft());

		GL11.glViewport(0, 0, width * scaledresolution.getScaleFactor(), height * scaledresolution.getScaleFactor());

		float fov = 97;

		{
			Tessellator tessellator = Tessellator.getInstance();
			WorldRenderer worldrenderer = tessellator.getWorldRenderer();
			GlStateManager.matrixMode(5889);
			GlStateManager.pushMatrix();
			GlStateManager.loadIdentity();
			Project.gluPerspective(fov, (float) height / width, 0.05F, 10.0F);
			GlStateManager.matrixMode(5888);
			GlStateManager.pushMatrix();
			GlStateManager.loadIdentity();
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			GlStateManager.rotate(180F, 1.0F, 0.0F, 0.0F);
			GlStateManager.rotate(90, 0.0F, 0.0F, 1.0F);
			GlStateManager.rotate(19, 1.0F, 0.0F, 0.0F);
			//GlStateManager.rotate(tl.x, 0.0F, 0.0F, 1.0F);
			GlStateManager.enableBlend();
			GlStateManager.disableAlpha();
			GlStateManager.disableCull();
			GlStateManager.depthMask(false);
			GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

			GlStateManager.pushMatrix();

			GlStateManager.translate(0, yOffset, zOffset);

			GlStateManager.rotate(angle, 0.0F, 1.0F, 0.0F);

			for (int k = 0; k < 6; ++k) {
				GlStateManager.pushMatrix();

				switch (k) {
					case 1:
						GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
						break;
					case 2:
						GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
						break;
					case 3:
						GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F);
						break;
					case 4:
						GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
						break;
					case 5:
						GlStateManager.rotate(-90.0F, 1.0F, 0.0F, 0.0F);
						break;
				}

				Minecraft.getMinecraft().getTextureManager().bindTexture(panoramas[k]);
				float splits = 0.1f;
				for (float x1 = 0; x1 < 1; x1 += splits) {
					for (float y1 = 0; y1 < 1; y1 += splits) {
						worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);

						for (int i = 0; i < 4; i++) {
							float x2 = (i == 0 || i == 3) ? x1 : x1 + splits;
							float y2 = (i >= 2) ? y1 : y1 + splits;

							float xr = x2 * 2 - 1;
							float yr = y2 * 2 - 1;

							float distSq = xr * xr + yr * yr + 1;
							float scale = (float) Math.sqrt(3 / distSq);

							worldrenderer.pos(xr * scale, yr * scale, scale).tex(x2, y2).color(255, 255, 255, 255).endVertex();
						}

						tessellator.draw();
					}
				}

				GlStateManager.popMatrix();
			}

			GlStateManager.popMatrix();
			GlStateManager.colorMask(true, true, true, false);

			worldrenderer.setTranslation(0.0D, 0.0D, 0.0D);
			GlStateManager.colorMask(true, true, true, true);
			GlStateManager.matrixMode(5889);
			GlStateManager.popMatrix();
			GlStateManager.matrixMode(5888);
			GlStateManager.popMatrix();
			GlStateManager.depthMask(true);
			GlStateManager.enableCull();
			GlStateManager.enableDepth();
		}

		if (
			backgroundTexture == null ||
			lastWidth != width * scaledresolution.getScaleFactor() ||
			lastHeight != height * scaledresolution.getScaleFactor()
		) {
			DynamicTexture viewportTexture = new DynamicTexture(
				width * scaledresolution.getScaleFactor(),
				height * scaledresolution.getScaleFactor()
			);
			backgroundTexture = Minecraft.getMinecraft().getTextureManager().getDynamicTextureLocation("background", viewportTexture);
			lastWidth = width * scaledresolution.getScaleFactor();
			lastHeight = height * scaledresolution.getScaleFactor();
		}

		Minecraft.getMinecraft().getTextureManager().bindTexture(backgroundTexture);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		GL11.glCopyTexSubImage2D(
			GL11.GL_TEXTURE_2D,
			0,
			0,
			0,
			0,
			0,
			width * scaledresolution.getScaleFactor(),
			height * scaledresolution.getScaleFactor()
		);

		Minecraft.getMinecraft().getFramebuffer().bindFramebuffer(true);
		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer worldrenderer = tessellator.getWorldRenderer();
		worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
		worldrenderer.pos(x, y + height, 0).tex(0, 1).endVertex();
		worldrenderer.pos(x + width, y + height, 0).tex(0, 0).endVertex();
		worldrenderer.pos(x + width, y, 0).tex(1, 0).endVertex();
		worldrenderer.pos(x, y, 0).tex(1, 1).endVertex();
		tessellator.draw();
	}
}
