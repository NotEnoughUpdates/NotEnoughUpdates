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

import io.github.moulberry.notenoughupdates.autosubscribe.NEUAutoSubscribe;
import io.github.moulberry.notenoughupdates.util.ReverseWorldRenderer;
import io.github.moulberry.notenoughupdates.util.SpecialColour;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@NEUAutoSubscribe
public class NullzeeSphere {
	public static boolean enabled = false;
	public static float size = 20;
	public static BlockPos centerPos = new BlockPos(0, 0, 0);

	public static ReverseWorldRenderer overlayVBO = null;

	public ReverseWorldRenderer getOverlayVBO() {
		if (overlayVBO != null) return overlayVBO;

		EntityPlayerSP p = Minecraft.getMinecraft().thePlayer;
		if (p == null) return null;

		//per vertex = 6
		//per size = 4
		//per block = 8
		//total per block = 196

		Set<BlockPos> circleOffsets = getCircleOffsets(size);

		ReverseWorldRenderer worldRenderer = new ReverseWorldRenderer(196 * circleOffsets.size());
		worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

		String col = SpecialColour.special(0, 180, 0xffff9900);
		for (BlockPos offset : circleOffsets) {
			BlockPos overlayPos = new BlockPos(offset.getX(), offset.getY(), offset.getZ());

			AxisAlignedBB bb = new AxisAlignedBB(
				overlayPos.getX(),
				overlayPos.getY(),
				overlayPos.getZ(),
				overlayPos.getX() + 1,
				overlayPos.getY() + 1,
				overlayPos.getZ() + 1
			).expand(0.001f, 0.001f, 0.001f);
			uploadFilledBoundingBox(bb, 1f, col, worldRenderer);
		}

		overlayVBO = worldRenderer;
		return overlayVBO;
	}

	public Set<BlockPos> getCircleOffsets(float radius) {
		Set<BlockPos> circleOffsets = new HashSet<>();

		int radiusI = (int) Math.ceil(radius) + 1;
		for (int x = -radiusI; x <= radiusI; x++) {
			for (int y = -radiusI; y <= radiusI; y++) {
				for (int z = -radiusI; z <= radiusI; z++) {
					float distSq = x * x + y * y + z * z;
					if (distSq >= (radius - 0.5) * (radius - 0.5) && distSq <= (radius + 0.5) * (radius + 0.5)) {
						circleOffsets.add(new BlockPos(x, y, z));
					}
				}
			}
		}

		return circleOffsets;
	}

	long lastUpdate = 0;

	private static double posLastUpdateX;
	private static double posLastUpdateY;
	private static double posLastUpdateZ;

	@SubscribeEvent
	public void onTick(TickEvent.ClientTickEvent event) {
		if (!enabled) return;

		EntityPlayerSP p = Minecraft.getMinecraft().thePlayer;
		if (p == null) return;

		if (event.phase == TickEvent.Phase.START) {
			double dX = p.posX - posLastUpdateX;
			double dY = p.posY - posLastUpdateY;
			double dZ = p.posZ - posLastUpdateZ;

			if (dX * dX + dY * dY + dZ * dZ < 1) {
				return;
			}

			posLastUpdateX = p.posX;
			posLastUpdateY = p.posY;
			posLastUpdateZ = p.posZ;

			long currentTime = System.currentTimeMillis();
			if (currentTime - lastUpdate < 250) {
				return;
			}
			lastUpdate = currentTime;

			ReverseWorldRenderer worldRenderer = getOverlayVBO();
			if (worldRenderer != null) {
				worldRenderer.setTranslation(0, 0, 0);
				worldRenderer.sortVertexData(
					(float) p.posX - centerPos.getX(),
					(float) p.posY - centerPos.getY(),
					(float) p.posZ - centerPos.getZ()
				);

			}
		}
	}

	@SubscribeEvent
	public void onRenderLast(RenderWorldLastEvent event) {
		if (!enabled) return;

		Entity viewer = Minecraft.getMinecraft().getRenderViewEntity();
		double viewerX = viewer.lastTickPosX + (viewer.posX - viewer.lastTickPosX) * event.partialTicks;
		double viewerY = viewer.lastTickPosY + (viewer.posY - viewer.lastTickPosY) * event.partialTicks;
		double viewerZ = viewer.lastTickPosZ + (viewer.posZ - viewer.lastTickPosZ) * event.partialTicks;

		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
		GlStateManager.disableTexture2D();

		GlStateManager.translate(-viewerX, -viewerY, -viewerZ);

		GL11.glPolygonOffset(5, 5);
		ReverseWorldRenderer worldRenderer = getOverlayVBO();
		if (worldRenderer != null && worldRenderer.getVertexCount() > 0) {
			GlStateManager.translate(centerPos.getX(), centerPos.getY(), centerPos.getZ());

			VertexFormat vertexformat = worldRenderer.getVertexFormat();
			int stride = vertexformat.getNextOffset();
			ByteBuffer bytebuffer = worldRenderer.getByteBuffer();
			List<VertexFormatElement> list = vertexformat.getElements();

			for (int index = 0; index < list.size(); index++) {
				VertexFormatElement vertexformatelement = list.get(index);
				vertexformatelement.getUsage().preDraw(vertexformat, index, stride, bytebuffer);
			}

			GL11.glDrawArrays(worldRenderer.getDrawMode(), 0, worldRenderer.getVertexCount());

			for (int index = 0; index < list.size(); index++) {
				VertexFormatElement vertexformatelement = list.get(index);
				vertexformatelement.getUsage().postDraw(vertexformat, index, stride, bytebuffer);
			}

			GlStateManager.translate(-centerPos.getX(), -centerPos.getY(), -centerPos.getZ());
		}
		GL11.glPolygonOffset(0, 0);

		GlStateManager.translate(viewerX, viewerY, viewerZ);

		GlStateManager.enableTexture2D();
	}

	public static void uploadFilledBoundingBox(
		AxisAlignedBB p_181561_0_,
		float alpha,
		String special,
		ReverseWorldRenderer worldrenderer
	) {
		Color c = new Color(SpecialColour.specialToChromaRGB(special), true);

		//vertical
		worldrenderer
			.pos(p_181561_0_.minX, p_181561_0_.minY, p_181561_0_.minZ)
			.color(c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f, c.getAlpha() / 255f * alpha)
			.endVertex();
		worldrenderer
			.pos(p_181561_0_.maxX, p_181561_0_.minY, p_181561_0_.minZ)
			.color(c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f, c.getAlpha() / 255f * alpha)
			.endVertex();
		worldrenderer
			.pos(p_181561_0_.maxX, p_181561_0_.minY, p_181561_0_.maxZ)
			.color(c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f, c.getAlpha() / 255f * alpha)
			.endVertex();
		worldrenderer
			.pos(p_181561_0_.minX, p_181561_0_.minY, p_181561_0_.maxZ)
			.color(c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f, c.getAlpha() / 255f * alpha)
			.endVertex();
		worldrenderer
			.pos(p_181561_0_.minX, p_181561_0_.maxY, p_181561_0_.maxZ)
			.color(c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f, c.getAlpha() / 255f * alpha)
			.endVertex();
		worldrenderer
			.pos(p_181561_0_.maxX, p_181561_0_.maxY, p_181561_0_.maxZ)
			.color(c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f, c.getAlpha() / 255f * alpha)
			.endVertex();
		worldrenderer
			.pos(p_181561_0_.maxX, p_181561_0_.maxY, p_181561_0_.minZ)
			.color(c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f, c.getAlpha() / 255f * alpha)
			.endVertex();
		worldrenderer
			.pos(p_181561_0_.minX, p_181561_0_.maxY, p_181561_0_.minZ)
			.color(c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f, c.getAlpha() / 255f * alpha)
			.endVertex();

		//x
		worldrenderer.pos(p_181561_0_.minX, p_181561_0_.minY, p_181561_0_.maxZ)
								 .color(
									 c.getRed() / 255f * 0.8f,
									 c.getGreen() / 255f * 0.8f,
									 c.getBlue() / 255f * 0.8f,
									 c.getAlpha() / 255f * alpha
								 ).endVertex();
		worldrenderer.pos(p_181561_0_.minX, p_181561_0_.maxY, p_181561_0_.maxZ)
								 .color(
									 c.getRed() / 255f * 0.8f,
									 c.getGreen() / 255f * 0.8f,
									 c.getBlue() / 255f * 0.8f,
									 c.getAlpha() / 255f * alpha
								 ).endVertex();
		worldrenderer.pos(p_181561_0_.minX, p_181561_0_.maxY, p_181561_0_.minZ)
								 .color(
									 c.getRed() / 255f * 0.8f,
									 c.getGreen() / 255f * 0.8f,
									 c.getBlue() / 255f * 0.8f,
									 c.getAlpha() / 255f * alpha
								 ).endVertex();
		worldrenderer.pos(p_181561_0_.minX, p_181561_0_.minY, p_181561_0_.minZ)
								 .color(
									 c.getRed() / 255f * 0.8f,
									 c.getGreen() / 255f * 0.8f,
									 c.getBlue() / 255f * 0.8f,
									 c.getAlpha() / 255f * alpha
								 ).endVertex();
		worldrenderer.pos(p_181561_0_.maxX, p_181561_0_.minY, p_181561_0_.minZ)
								 .color(
									 c.getRed() / 255f * 0.8f,
									 c.getGreen() / 255f * 0.8f,
									 c.getBlue() / 255f * 0.8f,
									 c.getAlpha() / 255f * alpha
								 ).endVertex();
		worldrenderer.pos(p_181561_0_.maxX, p_181561_0_.maxY, p_181561_0_.minZ)
								 .color(
									 c.getRed() / 255f * 0.8f,
									 c.getGreen() / 255f * 0.8f,
									 c.getBlue() / 255f * 0.8f,
									 c.getAlpha() / 255f * alpha
								 ).endVertex();
		worldrenderer.pos(p_181561_0_.maxX, p_181561_0_.maxY, p_181561_0_.maxZ)
								 .color(
									 c.getRed() / 255f * 0.8f,
									 c.getGreen() / 255f * 0.8f,
									 c.getBlue() / 255f * 0.8f,
									 c.getAlpha() / 255f * alpha
								 ).endVertex();
		worldrenderer.pos(p_181561_0_.maxX, p_181561_0_.minY, p_181561_0_.maxZ)
								 .color(
									 c.getRed() / 255f * 0.8f,
									 c.getGreen() / 255f * 0.8f,
									 c.getBlue() / 255f * 0.8f,
									 c.getAlpha() / 255f * alpha
								 ).endVertex();

		//z
		worldrenderer.pos(p_181561_0_.minX, p_181561_0_.maxY, p_181561_0_.minZ)
								 .color(
									 c.getRed() / 255f * 0.9f,
									 c.getGreen() / 255f * 0.9f,
									 c.getBlue() / 255f * 0.9f,
									 c.getAlpha() / 255f * alpha
								 ).endVertex();
		worldrenderer.pos(p_181561_0_.maxX, p_181561_0_.maxY, p_181561_0_.minZ)
								 .color(
									 c.getRed() / 255f * 0.9f,
									 c.getGreen() / 255f * 0.9f,
									 c.getBlue() / 255f * 0.9f,
									 c.getAlpha() / 255f * alpha
								 ).endVertex();
		worldrenderer.pos(p_181561_0_.maxX, p_181561_0_.minY, p_181561_0_.minZ)
								 .color(
									 c.getRed() / 255f * 0.9f,
									 c.getGreen() / 255f * 0.9f,
									 c.getBlue() / 255f * 0.9f,
									 c.getAlpha() / 255f * alpha
								 ).endVertex();
		worldrenderer.pos(p_181561_0_.minX, p_181561_0_.minY, p_181561_0_.minZ)
								 .color(
									 c.getRed() / 255f * 0.9f,
									 c.getGreen() / 255f * 0.9f,
									 c.getBlue() / 255f * 0.9f,
									 c.getAlpha() / 255f * alpha
								 ).endVertex();
		worldrenderer.pos(p_181561_0_.minX, p_181561_0_.minY, p_181561_0_.maxZ)
								 .color(
									 c.getRed() / 255f * 0.9f,
									 c.getGreen() / 255f * 0.9f,
									 c.getBlue() / 255f * 0.9f,
									 c.getAlpha() / 255f * alpha
								 ).endVertex();
		worldrenderer.pos(p_181561_0_.maxX, p_181561_0_.minY, p_181561_0_.maxZ)
								 .color(
									 c.getRed() / 255f * 0.9f,
									 c.getGreen() / 255f * 0.9f,
									 c.getBlue() / 255f * 0.9f,
									 c.getAlpha() / 255f * alpha
								 ).endVertex();
		worldrenderer.pos(p_181561_0_.maxX, p_181561_0_.maxY, p_181561_0_.maxZ)
								 .color(
									 c.getRed() / 255f * 0.9f,
									 c.getGreen() / 255f * 0.9f,
									 c.getBlue() / 255f * 0.9f,
									 c.getAlpha() / 255f * alpha
								 ).endVertex();
		worldrenderer.pos(p_181561_0_.minX, p_181561_0_.maxY, p_181561_0_.maxZ)
								 .color(
									 c.getRed() / 255f * 0.9f,
									 c.getGreen() / 255f * 0.9f,
									 c.getBlue() / 255f * 0.9f,
									 c.getAlpha() / 255f * alpha
								 ).endVertex();
	}
}
