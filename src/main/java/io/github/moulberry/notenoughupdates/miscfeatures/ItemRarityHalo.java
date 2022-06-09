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

package io.github.moulberry.notenoughupdates.miscfeatures;

import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.util.NEUResourceManager;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.client.shader.Shader;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL45;

import java.awt.*;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;

public class ItemRarityHalo {
	public static Framebuffer itemFramebuffer1 = null;
	public static Framebuffer itemFramebuffer2 = null;
	public static HashMap<ItemStack, Integer> itemHaloTexMap = new HashMap<>();
	public static Matrix4f projectionMatrix = null;

	public static Shader colourShader = null;
	public static Shader blurShaderHorz = null;
	public static Shader blurShaderVert = null;

	private static int oldScaledResolution = 0;

	public static void onItemRender(ItemStack stack, int x, int y) {
		if (x == 0 && y == 0) return;

		if (!OpenGlHelper.isFramebufferEnabled() || !OpenGlHelper.areShadersSupported()) return;
		NotEnoughUpdates neu = NotEnoughUpdates.INSTANCE;
		if (!neu.isOnSkyblock()) return;
		//if(neu.manager.config.itemHighlightOpacity.value <= 1) return;
		if (neu.manager.getInternalNameForItem(stack) == null) return;

		ScaledResolution scaledresolution = new ScaledResolution(Minecraft.getMinecraft());
		int size = 16 * scaledresolution.getScaleFactor();

		if (projectionMatrix == null) {
			projectionMatrix = Utils.createProjectionMatrix(size, size);
		}

		itemFramebuffer1 = checkFramebufferSizes(itemFramebuffer1, size, size);
		itemFramebuffer2 = checkFramebufferSizes(itemFramebuffer2, size, size);

		try {
			if (colourShader == null) {
				colourShader = new Shader(new NEUResourceManager(Minecraft.getMinecraft().getResourceManager()),
					"setrgbtoalpha", itemFramebuffer1, itemFramebuffer2
				);
				upload(colourShader, size, size);
			}

			if (blurShaderHorz == null) {
				blurShaderHorz = new Shader(new NEUResourceManager(Minecraft.getMinecraft().getResourceManager()),
					"blur", itemFramebuffer2, itemFramebuffer1
				);
				blurShaderHorz.getShaderManager().getShaderUniform("BlurDir").set(1, 0);
				blurShaderHorz.getShaderManager().getShaderUniform("Radius").set(5f);
				blurShaderHorz.getShaderManager().getShaderUniform("AlphaMult").set(2f);
				upload(blurShaderHorz, size, size);
			}

			if (blurShaderVert == null) {
				blurShaderVert = new Shader(new NEUResourceManager(Minecraft.getMinecraft().getResourceManager()),
					"blur", itemFramebuffer1, itemFramebuffer2
				);
				blurShaderVert.getShaderManager().getShaderUniform("BlurDir").set(0, 1);
				blurShaderVert.getShaderManager().getShaderUniform("Radius").set(5f);
				blurShaderVert.getShaderManager().getShaderUniform("AlphaMult").set(2f);
				upload(blurShaderVert, size, size);
			}
		} catch (Exception e) {
			return;
		}

		if (oldScaledResolution != scaledresolution.getScaleFactor()) {
			resetItemHaloCache();
			oldScaledResolution = scaledresolution.getScaleFactor();
		}

		int currentBuffer = GL11.glGetInteger(GL30.GL_FRAMEBUFFER_BINDING);
		IntBuffer currentViewport = BufferUtils.createIntBuffer(16);
		GL11.glGetInteger(GL11.GL_VIEWPORT, currentViewport);
		try {
			if (!itemHaloTexMap.containsKey(stack)) {
				int texture1 = TextureUtil.glGenTextures();
				int texture2 = TextureUtil.glGenTextures();

				GlStateManager.bindTexture(texture1);
				GL11.glTexImage2D(
					GL11.GL_TEXTURE_2D,
					0,
					GL11.GL_RGBA8,
					size,
					size,
					0,
					GL11.GL_RGBA,
					GL11.GL_UNSIGNED_BYTE,
					((ByteBuffer) null)
				);
				itemFramebuffer1.bindFramebuffer(false);
				OpenGlHelper.glFramebufferTexture2D(
					OpenGlHelper.GL_FRAMEBUFFER,
					OpenGlHelper.GL_COLOR_ATTACHMENT0,
					3553,
					texture1,
					0
				);

				GlStateManager.bindTexture(texture2);
				GL11.glTexImage2D(
					GL11.GL_TEXTURE_2D,
					0,
					GL11.GL_RGBA8,
					size,
					size,
					0,
					GL11.GL_RGBA,
					GL11.GL_UNSIGNED_BYTE,
					((ByteBuffer) null)
				);
				itemFramebuffer2.bindFramebuffer(false);
				OpenGlHelper.glFramebufferTexture2D(
					OpenGlHelper.GL_FRAMEBUFFER,
					OpenGlHelper.GL_COLOR_ATTACHMENT0,
					3553,
					texture2,
					0
				);

				itemFramebuffer1.framebufferClear();
				itemFramebuffer2.framebufferClear();

				GlStateManager.pushMatrix();
				{
					GlStateManager.matrixMode(5889);
					GlStateManager.loadIdentity();
					GlStateManager.ortho(0.0D, size, size, 0.0D, 1000.0D, 3000.0D);
					GlStateManager.matrixMode(5888);
					GlStateManager.loadIdentity();
					GlStateManager.translate(0.0F, 0.0F, -2000.0F);

					GL11.glScalef(scaledresolution.getScaleFactor(), scaledresolution.getScaleFactor(), 1);

					itemFramebuffer1.bindFramebuffer(true);

					RenderItem itemRender = Minecraft.getMinecraft().getRenderItem();
					RenderHelper.enableGUIStandardItemLighting();
					float zLevel = itemRender.zLevel;
					itemRender.zLevel = -145; //Negates the z-offset of the below method.
					itemRender.renderItemAndEffectIntoGUI(stack, 0, 0);
					itemRender.zLevel = zLevel;
					RenderHelper.disableStandardItemLighting();
				}
				GlStateManager.popMatrix();

				GlStateManager.pushMatrix();
				{
					GL45.glTextureBarrier();
					GL11.glFlush();
					GL11.glFinish();
					executeShader(colourShader);
					//GL45.glTextureBarrier(); GL11.glFlush(); GL11.glFinish();
					//executeShader(blurShaderHorz);
					//GL45.glTextureBarrier(); GL11.glFlush(); GL11.glFinish();
					//executeShader(blurShaderVert);
					//GL45.glTextureBarrier(); GL11.glFlush(); GL11.glFinish();
				}
				GlStateManager.popMatrix();

				GlStateManager.matrixMode(5889);
				GlStateManager.loadIdentity();
				GlStateManager.ortho(
					0.0D,
					scaledresolution.getScaledWidth_double(),
					scaledresolution.getScaledHeight_double(),
					0.0D,
					1000.0D,
					3000.0D
				);
				GlStateManager.matrixMode(5888);

				OpenGlHelper.glBindFramebuffer(OpenGlHelper.GL_FRAMEBUFFER, currentBuffer);
				GlStateManager.viewport(
					currentViewport.get(),
					currentViewport.get(),
					currentViewport.get(),
					currentViewport.get()
				);

				//TextureUtil.deleteTexture(texture1);
				itemHaloTexMap.put(stack, texture2);
			}

			OpenGlHelper.glBindFramebuffer(OpenGlHelper.GL_FRAMEBUFFER, currentBuffer);
			GlStateManager.viewport(
				currentViewport.get(),
				currentViewport.get(),
				currentViewport.get(),
				currentViewport.get()
			);

			GlStateManager.bindTexture(itemHaloTexMap.get(stack));
			Color color = Utils.getPrimaryColour(stack.getDisplayName());
			//GlStateManager.color(color.getRed()/255f, color.getGreen()/255f, color.getBlue()/255f,
			//        NotEnoughUpdates.INSTANCE.manager.config.itemHighlightOpacity.value.floatValue()/255f);
			Utils.drawTexturedRect(x, y, 16, 16,
				0, 1, 1, 0, GL11.GL_NEAREST
			);
			GlStateManager.bindTexture(0);
		} catch (Exception e) {
			e.printStackTrace();
			OpenGlHelper.glBindFramebuffer(OpenGlHelper.GL_FRAMEBUFFER, currentBuffer);
			GlStateManager.viewport(
				currentViewport.get(),
				currentViewport.get(),
				currentViewport.get(),
				currentViewport.get()
			);
		}
	}

	private static Framebuffer checkFramebufferSizes(Framebuffer framebuffer, int width, int height) {
		if (framebuffer == null || framebuffer.framebufferWidth != width || framebuffer.framebufferHeight != height) {
			if (framebuffer == null) {
				framebuffer = new Framebuffer(width, height, true);
			} else {
				framebuffer.createBindFramebuffer(width, height);
			}
			framebuffer.setFramebufferFilter(GL11.GL_NEAREST);
		}
		return framebuffer;
	}

	public static void resetItemHaloCache() {
		ScaledResolution scaledresolution = new ScaledResolution(Minecraft.getMinecraft());
		int size = 16 * scaledresolution.getScaleFactor();

		for (int tex : itemHaloTexMap.values()) {
			TextureUtil.deleteTexture(tex);
		}
		itemHaloTexMap.clear();

		if (NotEnoughUpdates.INSTANCE.isOnSkyblock()) {
			projectionMatrix = Utils.createProjectionMatrix(size, size);
			upload(colourShader, size, size);
			upload(blurShaderHorz, size, size);
			upload(blurShaderVert, size, size);
		}
	}

	private static void upload(Shader shader, int width, int height) {
		if (shader == null) return;
		shader.getShaderManager().getShaderUniformOrDefault("ProjMat").set(projectionMatrix);
		shader.getShaderManager().getShaderUniformOrDefault("InSize").set(width, height);
		shader.getShaderManager().getShaderUniformOrDefault("OutSize").set(width, height);
		shader.getShaderManager().getShaderUniformOrDefault("ScreenSize").set((float) width, (float) height);
	}

	private static void executeShader(Shader shader) {
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.disableBlend();
		GlStateManager.disableDepth();
		GlStateManager.disableAlpha();
		GlStateManager.disableFog();
		GlStateManager.disableLighting();
		GlStateManager.disableColorMaterial();
		GlStateManager.enableTexture2D();
		GlStateManager.bindTexture(0);

		float f = (float) shader.framebufferOut.framebufferTextureWidth;
		float f1 = (float) shader.framebufferOut.framebufferTextureHeight;
		GlStateManager.viewport(0, 0, (int) f, (int) f1);

		shader.getShaderManager().useShader();
		shader.getShaderManager().addSamplerTexture("DiffuseSampler", shader.framebufferIn);

		shader.framebufferOut.framebufferClear();
		shader.framebufferOut.bindFramebuffer(false);

		GlStateManager.depthMask(false);

		GlStateManager.enableAlpha();
		GlStateManager.enableBlend();
		GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);

		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer worldrenderer = tessellator.getWorldRenderer();
		worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
		worldrenderer.pos(0.0D, f1, 500.0D).color(255, 255, 255, 255).endVertex();
		worldrenderer.pos(f, f1, 500.0D).color(255, 255, 255, 255).endVertex();
		worldrenderer.pos(f, 0.0D, 500.0D).color(255, 255, 255, 255).endVertex();
		worldrenderer.pos(0.0D, 0.0D, 500.0D).color(255, 255, 255, 255).endVertex();
		tessellator.draw();

		GlStateManager.depthMask(true);

		shader.getShaderManager().endShader();

		shader.framebufferOut.unbindFramebuffer();
		shader.framebufferIn.unbindFramebufferTexture();
	}
}
