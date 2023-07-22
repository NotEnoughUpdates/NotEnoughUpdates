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

package io.github.moulberry.notenoughupdates.core.util.render;

import io.github.moulberry.notenoughupdates.core.BackgroundBlur;
import io.github.moulberry.notenoughupdates.miscfeatures.CustomItemEffects;
import io.github.moulberry.notenoughupdates.util.SpecialColour;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.Slot;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraft.util.Vec3i;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.util.vector.Vector3f;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RenderUtils {
	public static void drawFloatingRectDark(int x, int y, int width, int height) {
		drawFloatingRectDark(x, y, width, height, true);
	}

	public static void drawFloatingRectDark(int x, int y, int width, int height, boolean shadow) {
		int alpha = 0xf0000000;

		if (OpenGlHelper.isFramebufferEnabled()) {
			ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
			BackgroundBlur.renderBlurredBackground(15, scaledResolution.getScaledWidth(),
				scaledResolution.getScaledHeight(), x, y, width, height, true
			);
		} else {
			alpha = 0xff000000;
		}

		int main = alpha | 0x202026;
		int light = 0xff303036;
		int dark = 0xff101016;
		Gui.drawRect(x, y, x + 1, y + height, light); //Left
		Gui.drawRect(x + 1, y, x + width, y + 1, light); //Top
		Gui.drawRect(x + width - 1, y + 1, x + width, y + height, dark); //Right
		Gui.drawRect(x + 1, y + height - 1, x + width - 1, y + height, dark); //Bottom
		Gui.drawRect(x + 1, y + 1, x + width - 1, y + height - 1, main); //Middle
		if (shadow) {
			Gui.drawRect(x + width, y + 2, x + width + 2, y + height + 2, 0x70000000); //Right shadow
			Gui.drawRect(x + 2, y + height, x + width, y + height + 2, 0x70000000); //Bottom shadow
		}
	}

	public static void drawFloatingRect(int x, int y, int width, int height) {
		drawFloatingRectWithAlpha(x, y, width, height, 0xFF, true);
	}

	public static void drawFloatingRectWithAlpha(int x, int y, int width, int height, int alpha, boolean shadow) {
		int main = (alpha << 24) | 0xc0c0c0;
		int light = (alpha << 24) | 0xf0f0f0;
		int dark = (alpha << 24) | 0x909090;
		Gui.drawRect(x, y, x + 1, y + height, light); //Left
		Gui.drawRect(x + 1, y, x + width, y + 1, light); //Top
		Gui.drawRect(x + width - 1, y + 1, x + width, y + height, dark); //Right
		Gui.drawRect(x + 1, y + height - 1, x + width - 1, y + height, dark); //Bottom
		Gui.drawRect(x + 1, y + 1, x + width - 1, y + height - 1, main); //Middle
		if (shadow) {
			Gui.drawRect(x + width, y + 2, x + width + 2, y + height + 2, (alpha * 3 / 5) << 24); //Right shadow
			Gui.drawRect(x + 2, y + height, x + width, y + height + 2, (alpha * 3 / 5) << 24); //Bottom shadow
		}
	}

	public static void drawTexturedRect(float x, float y, float width, float height) {
		drawTexturedRect(x, y, width, height, 0, 1, 0, 1);
	}

	public static void drawTexturedRect(float x, float y, float width, float height, int filter) {
		drawTexturedRect(x, y, width, height, 0, 1, 0, 1, filter);
	}

	public static void drawTexturedRect(
		float x,
		float y,
		float width,
		float height,
		float uMin,
		float uMax,
		float vMin,
		float vMax
	) {
		drawTexturedRect(x, y, width, height, uMin, uMax, vMin, vMax, GL11.GL_NEAREST);
	}

	public static void drawTexturedRect(
		float x,
		float y,
		float width,
		float height,
		float uMin,
		float uMax,
		float vMin,
		float vMax,
		int filter
	) {
		GlStateManager.enableBlend();
		GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);

		drawTexturedRectNoBlend(x, y, width, height, uMin, uMax, vMin, vMax, filter);

		GlStateManager.disableBlend();
	}

	public static void drawTexturedRectNoBlend(
		float x,
		float y,
		float width,
		float height,
		float uMin,
		float uMax,
		float vMin,
		float vMax,
		int filter
	) {
		GlStateManager.enableTexture2D();

		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, filter);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, filter);

		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer worldrenderer = tessellator.getWorldRenderer();
		worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
		worldrenderer
			.pos(x, y + height, 0.0D)
			.tex(uMin, vMax).endVertex();
		worldrenderer
			.pos(x + width, y + height, 0.0D)
			.tex(uMax, vMax).endVertex();
		worldrenderer
			.pos(x + width, y, 0.0D)
			.tex(uMax, vMin).endVertex();
		worldrenderer
			.pos(x, y, 0.0D)
			.tex(uMin, vMin).endVertex();
		tessellator.draw();

		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
	}

	public static void drawGradientRect(
		int zLevel,
		int left,
		int top,
		int right,
		int bottom,
		int startColor,
		int endColor
	) {
		float startAlpha = (float) (startColor >> 24 & 255) / 255.0F;
		float startRed = (float) (startColor >> 16 & 255) / 255.0F;
		float startGreen = (float) (startColor >> 8 & 255) / 255.0F;
		float startBlue = (float) (startColor & 255) / 255.0F;
		float endAlpha = (float) (endColor >> 24 & 255) / 255.0F;
		float endRed = (float) (endColor >> 16 & 255) / 255.0F;
		float endGreen = (float) (endColor >> 8 & 255) / 255.0F;
		float endBlue = (float) (endColor & 255) / 255.0F;

		GlStateManager.disableTexture2D();
		GlStateManager.enableBlend();
		GlStateManager.disableAlpha();
		GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
		GlStateManager.shadeModel(7425);

		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer worldrenderer = tessellator.getWorldRenderer();
		worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
		worldrenderer.pos(right, top, zLevel).color(startRed, startGreen, startBlue, startAlpha).endVertex();
		worldrenderer.pos(left, top, zLevel).color(startRed, startGreen, startBlue, startAlpha).endVertex();
		worldrenderer.pos(left, bottom, zLevel).color(endRed, endGreen, endBlue, endAlpha).endVertex();
		worldrenderer.pos(right, bottom, zLevel).color(endRed, endGreen, endBlue, endAlpha).endVertex();
		tessellator.draw();

		GlStateManager.shadeModel(7424);
		GlStateManager.disableBlend();
		GlStateManager.enableAlpha();
		GlStateManager.enableTexture2D();
	}

	private static final ResourceLocation beaconBeam = new ResourceLocation("textures/entity/beacon_beam.png");

	private static void renderBeaconBeam(
		double x, double y, double z, int rgb, float alphaMult,
		float partialTicks, Boolean disableDepth
	) {
		int height = 300;
		int bottomOffset = 0;
		int topOffset = bottomOffset + height;

		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer worldrenderer = tessellator.getWorldRenderer();

		if (disableDepth) {
			GlStateManager.disableDepth();
		}

		Minecraft.getMinecraft().getTextureManager().bindTexture(beaconBeam);
		GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
		GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
		GlStateManager.disableLighting();
		GlStateManager.enableCull();
		GlStateManager.enableTexture2D();
		GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE, GL11.GL_ZERO);
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);

		double time = Minecraft.getMinecraft().theWorld.getTotalWorldTime() + (double) partialTicks;
		double d1 = MathHelper.func_181162_h(-time * 0.2D - (double) MathHelper.floor_double(-time * 0.1D));

		float r = ((rgb >> 16) & 0xFF) / 255f;
		float g = ((rgb >> 8) & 0xFF) / 255f;
		float b = (rgb & 0xFF) / 255f;
		double d2 = time * 0.025D * -1.5D;
		double d4 = 0.5D + Math.cos(d2 + 2.356194490192345D) * 0.2D;
		double d5 = 0.5D + Math.sin(d2 + 2.356194490192345D) * 0.2D;
		double d6 = 0.5D + Math.cos(d2 + (Math.PI / 4D)) * 0.2D;
		double d7 = 0.5D + Math.sin(d2 + (Math.PI / 4D)) * 0.2D;
		double d8 = 0.5D + Math.cos(d2 + 3.9269908169872414D) * 0.2D;
		double d9 = 0.5D + Math.sin(d2 + 3.9269908169872414D) * 0.2D;
		double d10 = 0.5D + Math.cos(d2 + 5.497787143782138D) * 0.2D;
		double d11 = 0.5D + Math.sin(d2 + 5.497787143782138D) * 0.2D;
		double d14 = -1.0D + d1;
		double d15 = (double) (height) * 2.5D + d14;
		worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
		worldrenderer.pos(x + d4, y + topOffset, z + d5).tex(1.0D, d15).color(r, g, b, 1.0F * alphaMult).endVertex();
		worldrenderer.pos(x + d4, y + bottomOffset, z + d5).tex(1.0D, d14).color(r, g, b, 1.0F).endVertex();
		worldrenderer.pos(x + d6, y + bottomOffset, z + d7).tex(0.0D, d14).color(r, g, b, 1.0F).endVertex();
		worldrenderer.pos(x + d6, y + topOffset, z + d7).tex(0.0D, d15).color(r, g, b, 1.0F * alphaMult).endVertex();
		worldrenderer.pos(x + d10, y + topOffset, z + d11).tex(1.0D, d15).color(r, g, b, 1.0F * alphaMult).endVertex();
		worldrenderer.pos(x + d10, y + bottomOffset, z + d11).tex(1.0D, d14).color(r, g, b, 1.0F).endVertex();
		worldrenderer.pos(x + d8, y + bottomOffset, z + d9).tex(0.0D, d14).color(r, g, b, 1.0F).endVertex();
		worldrenderer.pos(x + d8, y + topOffset, z + d9).tex(0.0D, d15).color(r, g, b, 1.0F * alphaMult).endVertex();
		worldrenderer.pos(x + d6, y + topOffset, z + d7).tex(1.0D, d15).color(r, g, b, 1.0F * alphaMult).endVertex();
		worldrenderer.pos(x + d6, y + bottomOffset, z + d7).tex(1.0D, d14).color(r, g, b, 1.0F).endVertex();
		worldrenderer.pos(x + d10, y + bottomOffset, z + d11).tex(0.0D, d14).color(r, g, b, 1.0F).endVertex();
		worldrenderer.pos(x + d10, y + topOffset, z + d11).tex(0.0D, d15).color(r, g, b, 1.0F * alphaMult).endVertex();
		worldrenderer.pos(x + d8, y + topOffset, z + d9).tex(1.0D, d15).color(r, g, b, 1.0F * alphaMult).endVertex();
		worldrenderer.pos(x + d8, y + bottomOffset, z + d9).tex(1.0D, d14).color(r, g, b, 1.0F).endVertex();
		worldrenderer.pos(x + d4, y + bottomOffset, z + d5).tex(0.0D, d14).color(r, g, b, 1.0F).endVertex();
		worldrenderer.pos(x + d4, y + topOffset, z + d5).tex(0.0D, d15).color(r, g, b, 1.0F * alphaMult).endVertex();
		tessellator.draw();

		GlStateManager.disableCull();
		double d12 = -1.0D + d1;
		double d13 = height + d12;

		worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
		worldrenderer.pos(x + 0.2D, y + topOffset, z + 0.2D).tex(1.0D, d13).color(r, g, b, 0.25F * alphaMult).endVertex();
		worldrenderer.pos(x + 0.2D, y + bottomOffset, z + 0.2D).tex(1.0D, d12).color(r, g, b, 0.25F).endVertex();
		worldrenderer.pos(x + 0.8D, y + bottomOffset, z + 0.2D).tex(0.0D, d12).color(r, g, b, 0.25F).endVertex();
		worldrenderer.pos(x + 0.8D, y + topOffset, z + 0.2D).tex(0.0D, d13).color(r, g, b, 0.25F * alphaMult).endVertex();
		worldrenderer.pos(x + 0.8D, y + topOffset, z + 0.8D).tex(1.0D, d13).color(r, g, b, 0.25F * alphaMult).endVertex();
		worldrenderer.pos(x + 0.8D, y + bottomOffset, z + 0.8D).tex(1.0D, d12).color(r, g, b, 0.25F).endVertex();
		worldrenderer.pos(x + 0.2D, y + bottomOffset, z + 0.8D).tex(0.0D, d12).color(r, g, b, 0.25F).endVertex();
		worldrenderer.pos(x + 0.2D, y + topOffset, z + 0.8D).tex(0.0D, d13).color(r, g, b, 0.25F * alphaMult).endVertex();
		worldrenderer.pos(x + 0.8D, y + topOffset, z + 0.2D).tex(1.0D, d13).color(r, g, b, 0.25F * alphaMult).endVertex();
		worldrenderer.pos(x + 0.8D, y + bottomOffset, z + 0.2D).tex(1.0D, d12).color(r, g, b, 0.25F).endVertex();
		worldrenderer.pos(x + 0.8D, y + bottomOffset, z + 0.8D).tex(0.0D, d12).color(r, g, b, 0.25F).endVertex();
		worldrenderer.pos(x + 0.8D, y + topOffset, z + 0.8D).tex(0.0D, d13).color(r, g, b, 0.25F * alphaMult).endVertex();
		worldrenderer.pos(x + 0.2D, y + topOffset, z + 0.8D).tex(1.0D, d13).color(r, g, b, 0.25F * alphaMult).endVertex();
		worldrenderer.pos(x + 0.2D, y + bottomOffset, z + 0.8D).tex(1.0D, d12).color(r, g, b, 0.25F).endVertex();
		worldrenderer.pos(x + 0.2D, y + bottomOffset, z + 0.2D).tex(0.0D, d12).color(r, g, b, 0.25F).endVertex();
		worldrenderer.pos(x + 0.2D, y + topOffset, z + 0.2D).tex(0.0D, d13).color(r, g, b, 0.25F * alphaMult).endVertex();
		tessellator.draw();

		GlStateManager.disableLighting();
		GlStateManager.enableTexture2D();
		if (disableDepth) {
			GlStateManager.enableDepth();
		}
	}

	public static void renderBoundingBox(
		BlockPos worldPos,
		int rgb,
		float partialTicks
	) {
		Vector3f interpolatedPlayerPosition = getInterpolatedPlayerPosition(partialTicks);
		renderBoundingBoxInViewSpace(
			worldPos.getX() - interpolatedPlayerPosition.x,
			worldPos.getY() - interpolatedPlayerPosition.y,
			worldPos.getZ() - interpolatedPlayerPosition.z,
			rgb
		);
	}

	private static void renderBoundingBoxInViewSpace(double x, double y, double z, int rgb) {
		AxisAlignedBB bb = new AxisAlignedBB(x, y, z, x + 1, y + 1, z + 1);

		GlStateManager.disableDepth();
		GlStateManager.disableCull();
		GlStateManager.disableTexture2D();
		CustomItemEffects.drawFilledBoundingBox(bb, 1f, SpecialColour.special(0, (rgb >> 24) & 0xFF, rgb));
		GlStateManager.enableTexture2D();
		GlStateManager.enableCull();
		GlStateManager.enableDepth();
	}

	public static void renderBeaconBeam(BlockPos block, int rgb, float alphaMult, float partialTicks) {
		double viewerX;
		double viewerY;
		double viewerZ;

		Vector3f aoteInterpPos = CustomItemEffects.INSTANCE.getCurrentPosition();
		if (aoteInterpPos != null) {
			viewerX = aoteInterpPos.x;
			viewerY = aoteInterpPos.y;
			viewerZ = aoteInterpPos.z;
		} else {
			Entity viewer = Minecraft.getMinecraft().getRenderViewEntity();
			viewerX = viewer.lastTickPosX + (viewer.posX - viewer.lastTickPosX) * partialTicks;
			viewerY = viewer.lastTickPosY + (viewer.posY - viewer.lastTickPosY) * partialTicks;
			viewerZ = viewer.lastTickPosZ + (viewer.posZ - viewer.lastTickPosZ) * partialTicks;
		}

		double x = block.getX() - viewerX;
		double y = block.getY() - viewerY;
		double z = block.getZ() - viewerZ;

		double distSq = x * x + y * y + z * z;

		RenderUtils.renderBeaconBeam(x, y, z, rgb, 1.0f, partialTicks, distSq > 10 * 10);
	}

	public static Vector3f getInterpolatedPlayerPosition(float partialTicks) {

		Vector3f aoteInterpPos = CustomItemEffects.INSTANCE.getCurrentPosition();
		if (aoteInterpPos != null) {
			return new Vector3f(aoteInterpPos);
		} else {
			Entity viewer = Minecraft.getMinecraft().getRenderViewEntity();
			Vector3f lastPos = new Vector3f(
				(float) viewer.lastTickPosX,
				(float) viewer.lastTickPosY,
				(float) viewer.lastTickPosZ
			);
			Vector3f currentPos = new Vector3f(
				(float) viewer.posX,
				(float) viewer.posY,
				(float) viewer.posZ
			);
			Vector3f movement = Vector3f.sub(currentPos, lastPos, currentPos);
			movement.scale(partialTicks);
			return Vector3f.add(lastPos, movement, lastPos);
		}
	}

	public static void renderBeaconBeamOrBoundingBox(BlockPos block, int rgb, float alphaMult, float partialTicks) {

		Vector3f interpolatedPlayerPosition = getInterpolatedPlayerPosition(partialTicks);
		double x = block.getX() - interpolatedPlayerPosition.x;
		double y = block.getY() - interpolatedPlayerPosition.y;
		double z = block.getZ() - interpolatedPlayerPosition.z;

		double distSq = x * x + y * y + z * z;

		if (distSq > 10 * 10) {
			RenderUtils.renderBeaconBeam(x, y, z, rgb, 1.0f, partialTicks, true);
		} else {
			RenderUtils.renderBoundingBoxInViewSpace(x, y, z, rgb);
		}
	}

	public static void renderWayPoint(String str, Vec3i loc, float partialTicks) {
		renderWayPoint(str, new Vector3f(loc.getX(), loc.getY(), loc.getZ()), partialTicks);
	}

	public static void renderWayPoint(List<String> str, Vec3i loc, float partialTicks) {
		renderWayPoint(str, new Vector3f(loc.getX(), loc.getY(), loc.getZ()), partialTicks, false);
	}

	public static void renderWayPoint(String str, Vector3f loc, float partialTicks) {
		renderWayPoint(Arrays.asList(str), loc, partialTicks, false);
	}

	public static void renderWayPoint(Vec3i loc, float partialTicks) {
		renderWayPoint(Arrays.asList(""), new Vector3f(loc.getX(), loc.getY(), loc.getZ()), partialTicks, true);
	}

	public static void drawFilledQuadWithTexture(Vec3 p1, Vec3 p2, Vec3 p3, Vec3 p4, float alpha, ResourceLocation texture) {
		GlStateManager.pushMatrix();
		Entity v = Minecraft.getMinecraft().getRenderViewEntity();
		double vX = v.lastTickPosX + (v.posX - v.lastTickPosX);
		double vY = v.lastTickPosY + (v.posY - v.lastTickPosY);
		double vZ = v.lastTickPosZ + (v.posZ - v.lastTickPosZ);

		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer worldrenderer = tessellator.getWorldRenderer();

		GlStateManager.enableTexture2D();
		GlStateManager.enableBlend();
		GlStateManager.disableCull();
		GlStateManager.color(1.0f, 1.0f, 1.0f, alpha);
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		worldrenderer.pos(p1.xCoord-vX, p1.yCoord-vY, p1.zCoord-vZ).tex(0, 0).endVertex(); //Top Left
		worldrenderer.pos(p2.xCoord-vX, p2.yCoord-vY, p2.zCoord-vZ).tex(1, 0).endVertex(); //Top Right
		worldrenderer.pos(p3.xCoord-vX, p3.yCoord-vY, p3.zCoord-vZ).tex(1, 1).endVertex(); //Bottom Right
		worldrenderer.pos(p4.xCoord-vX, p4.yCoord-vY, p4.zCoord-vZ).tex(0, 1).endVertex(); //Bottom Left
		tessellator.draw();
		GlStateManager.enableCull();
		GlStateManager.popMatrix();
	}

	public static void renderWayPoint(List<String> lines, Vector3f loc, float partialTicks, boolean onlyShowDistance) {
		GlStateManager.alphaFunc(516, 0.1F);

		GlStateManager.pushMatrix();

		Entity viewer = Minecraft.getMinecraft().getRenderViewEntity();
		double viewerX = viewer.lastTickPosX + (viewer.posX - viewer.lastTickPosX) * partialTicks;
		double viewerY = viewer.lastTickPosY + (viewer.posY - viewer.lastTickPosY) * partialTicks;
		double viewerZ = viewer.lastTickPosZ + (viewer.posZ - viewer.lastTickPosZ) * partialTicks;

		double x = loc.x - viewerX + 0.5f;
		double y = loc.y - viewerY - viewer.getEyeHeight();
		double z = loc.z - viewerZ + 0.5f;

		double distSq = x * x + y * y + z * z;
		double dist = Math.sqrt(distSq);
		if (distSq > 144) {
			x *= 12 / dist;
			y *= 12 / dist;
			z *= 12 / dist;
		}
		GlStateManager.translate(x, y, z);
		GlStateManager.translate(0, viewer.getEyeHeight(), 0);

		lines = onlyShowDistance ? new ArrayList<>() : new ArrayList<>(lines);
		lines.add(EnumChatFormatting.YELLOW.toString() + Math.round(dist) + "m");
		renderNametag(lines);

		GlStateManager.popMatrix();

		GlStateManager.disableLighting();
	}

	public static void renderNametag(String str) {
		renderNametag(Arrays.asList(str));
	}

	public static void renderNametag(List<String> lines) {
		FontRenderer fontrenderer = Minecraft.getMinecraft().fontRendererObj;
		float f = 1.6F;
		float f1 = 0.016666668F * f;
		GlStateManager.pushMatrix();
		GL11.glNormal3f(0.0F, 1.0F, 0.0F);
		GlStateManager.rotate(-Minecraft.getMinecraft().getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
		GlStateManager.rotate(Minecraft.getMinecraft().getRenderManager().playerViewX, 1.0F, 0.0F, 0.0F);
		GlStateManager.scale(-f1, -f1, f1);
		GlStateManager.disableLighting();
		GlStateManager.depthMask(false);
		GlStateManager.disableDepth();
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer worldrenderer = tessellator.getWorldRenderer();
		int i = 0;

		for (String str : lines) {
			int j = fontrenderer.getStringWidth(str) / 2;

			GlStateManager.disableTexture2D();
			worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
			worldrenderer.pos(-j - 1, -1 + i, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
			worldrenderer.pos(-j - 1, 8 + i, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
			worldrenderer.pos(j + 1, 8 + i, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
			worldrenderer.pos(j + 1, -1 + i, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
			tessellator.draw();
			GlStateManager.enableTexture2D();
			fontrenderer.drawString(str, -fontrenderer.getStringWidth(str) / 2, i, 553648127);
			GlStateManager.depthMask(true);

			fontrenderer.drawString(str, -fontrenderer.getStringWidth(str) / 2, i, -1);
			GlStateManager.translate(0, 10f, 0);
		}
		GlStateManager.enableDepth();
		GlStateManager.enableBlend();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.popMatrix();
	}

	public static void highlightSlot(Slot slot, Color color) {
		boolean lightingState = GL11.glIsEnabled(GL11.GL_LIGHTING);

		GlStateManager.disableLighting();
		GlStateManager.color(1f, 1f, 1f, 1f);

		GlStateManager.pushMatrix();
		GlStateManager.translate(0f, 0f, 110 + Minecraft.getMinecraft().getRenderItem().zLevel);
		Gui.drawRect(
			slot.xDisplayPosition,
			slot.yDisplayPosition,
			slot.xDisplayPosition + 16,
			slot.yDisplayPosition + 16,
			color.getRGB()
		);
		GlStateManager.popMatrix();

		if (lightingState) GlStateManager.enableLighting();
	}
}
