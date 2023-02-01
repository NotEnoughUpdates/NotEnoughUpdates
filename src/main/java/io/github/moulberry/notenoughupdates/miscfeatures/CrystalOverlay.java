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
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@NEUAutoSubscribe
public class CrystalOverlay {
	private enum CrystalType {
		FARMING_MINION(8, 0xDAA520),
		MINING_MINION(40, 0x6e5a49),
		FORAGING_MINION(12, 0x01a552),
		DESERT(16, 0xfff178),
		FISHING(15, 0x1972a6),
		WART(5, 0x821530),
		WHEAT(6, 0xff9d00);

		CrystalType(int radius, int rgb) {
			this.radius = radius;
			this.rgb = rgb;
		}

		public Set<BlockPos> getCircleOffsets() {
			if (circleOffsets != null) return circleOffsets;
			circleOffsets = new HashSet<>();

			for (int x = -radius; x <= radius; x++) {
				for (int y = -radius; y <= radius; y++) {
					for (int z = -radius; z <= radius; z++) {
						float distSq = (x - 0.5f) * (x - 0.5f) + y * y + (z - 0.5f) * (z - 0.5f);
						if (distSq > (radius - 1) * (radius - 1) && distSq < radius * radius) {
							circleOffsets.add(new BlockPos(x, y, z));
						}
					}
				}
			}

			return circleOffsets;
		}

		public ReverseWorldRenderer getOverlayVBO() {
			if (overlayVBO != null) return overlayVBO;

			EntityPlayerSP p = Minecraft.getMinecraft().thePlayer;
			if (p == null) return null;

			if (!crystals.containsKey(this)) {
				return null;
			}

			//per vertex = 6
			//per size = 4
			//per block = 8
			//total per block = 196

			ReverseWorldRenderer worldRenderer = new ReverseWorldRenderer(196 * getCircleOffsets().size());
			worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

			String col = SpecialColour.special(0, 180, rgb);
			for (BlockPos offset : getCircleOffsets()) {
				BlockPos overlayPos = new BlockPos(offset.getX(), offset.getY(), offset.getZ());

				AxisAlignedBB bb = new AxisAlignedBB(
					overlayPos.getX(),
					overlayPos.getY(),
					overlayPos.getZ(),
					overlayPos.getX() + 1,
					overlayPos.getY() + 1,
					overlayPos.getZ() + 1
				).expand(0.001f * (this.ordinal() + 1), 0.001f * (this.ordinal() + 1), 0.001f * (this.ordinal() + 1));
				uploadFilledBoundingBox(bb, 1f, col, worldRenderer);
			}

			overlayVBO = worldRenderer;
			return overlayVBO;
		}

		ReverseWorldRenderer overlayVBO = null;
		Set<BlockPos> circleOffsets = null;
		int updates = 0;
		int rgb;
		int radius;
	}

	private static double posLastUpdateX;
	private static double posLastUpdateY;
	private static double posLastUpdateZ;

	private static final HashMap<String, CrystalType> skullId = new HashMap<String, CrystalType>() {{
		put("d9c3168a-8654-3dd8-b297-4d3b7e55b95a", CrystalType.FARMING_MINION);
		put("949d100c-aa74-3b09-a642-af5529f808aa", CrystalType.MINING_MINION);
		put("bd79a474-cf07-3f8c-b5a4-98657c33520a", CrystalType.FORAGING_MINION);
		put("2e474ee3-5361-3218-84db-880eb1cface1", CrystalType.FISHING);
	}};

	public static long displayMillis = 0;

	public static long lastMiningUpdate = 0;

	public static HashMap<CrystalType, BlockPos> crystals = new HashMap<>();

	public static void tick() {
		if (!NotEnoughUpdates.INSTANCE.config.itemOverlays.enableCrystalOverlay) return;
		if (Minecraft.getMinecraft().theWorld == null) return;

		EntityPlayerSP p = Minecraft.getMinecraft().thePlayer;
		if (p == null) return;

		long currentTime = System.currentTimeMillis();

		if (NotEnoughUpdates.INSTANCE.config.itemOverlays.alwaysShowCrystal) {
			displayMillis = currentTime;
		} else {
			if (currentTime - displayMillis > 10 * 1000) {
				crystals.clear();
				displayMillis = -1;
			}

			ItemStack held = p.getHeldItem();
			String internal = NotEnoughUpdates.INSTANCE.manager.getInternalNameForItem(held);
			if (internal != null) {
				if (internal.endsWith("_CRYSTAL") && !internal.equals("POWER_CRYSTAL")) {
					displayMillis = currentTime;
				}
			}

			if (displayMillis < 0) {
				return;
			}
		}

		Set<CrystalType> foundTypes = new HashSet<>();
		for (Entity entity : Minecraft.getMinecraft().theWorld.loadedEntityList) {
			if (entity instanceof EntityArmorStand) {
				EntityArmorStand armorStand = (EntityArmorStand) entity;

				if (armorStand.isChild() && armorStand.getEquipmentInSlot(4) != null) {
					ItemStack helmet = armorStand.getEquipmentInSlot(4);

					if (helmet.getItem() == Items.skull && helmet.hasTagCompound()) {
						NBTTagCompound tag = helmet.getTagCompound();
						if (tag.hasKey("SkullOwner", 10)) {
							NBTTagCompound skullOwner = tag.getCompoundTag("SkullOwner");
							if (skullOwner.hasKey("Id", 8)) {
								String id = skullOwner.getString("Id");

								if (skullId.containsKey(id)) {
									CrystalType type = skullId.get(id);
									foundTypes.add(type);
									BlockPos pos = new BlockPos(armorStand.posX, armorStand.posY + 0.5f, armorStand.posZ);

									if (crystals.containsKey(type)) {
										BlockPos old = crystals.get(type);
										if (old.equals(pos)) {
											type.updates = 0;
										} else {
											if (++type.updates >= 3) {
												type.updates = 0;
												crystals.put(type, pos);
											}
										}
									} else {
										crystals.put(type, pos);
									}
								}
							}
						}
					}
				}
			}
		}
		crystals.keySet().retainAll(foundTypes);
	}

	@SubscribeEvent
	public void onTick(TickEvent.ClientTickEvent event) {
		if (!NotEnoughUpdates.INSTANCE.config.itemOverlays.enableCrystalOverlay) return;

		if (displayMillis < 0) {
			return;
		}

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

			for (CrystalType type : crystals.keySet()) {
				if (type == CrystalType.MINING_MINION) {
					long currentTime = System.currentTimeMillis();
					if (currentTime - lastMiningUpdate < 1000) {
						continue;
					}
					lastMiningUpdate = currentTime;
				}

				ReverseWorldRenderer worldRenderer = type.getOverlayVBO();
				if (worldRenderer != null) {
					BlockPos crystal = crystals.get(type);

					worldRenderer.setTranslation(0, 0, 0);
					worldRenderer.sortVertexData(
						(float) p.posX - crystal.getX(),
						(float) p.posY - crystal.getY(),
						(float) p.posZ - crystal.getZ()
					);
                    /*es.submit(() -> worldRenderer.sortVertexData(
                            (float)p.posX-crystal.getX(),
                            (float)p.posY-crystal.getY(),
                            (float)p.posZ-crystal.getZ()));*/

				}
			}
		}
	}

	@SubscribeEvent
	public void onRenderLast(RenderWorldLastEvent event) {
		if (!NotEnoughUpdates.INSTANCE.config.itemOverlays.enableCrystalOverlay) return;

		if (displayMillis < 0) {
			return;
		}

		Entity viewer = Minecraft.getMinecraft().getRenderViewEntity();
		double viewerX = viewer.lastTickPosX + (viewer.posX - viewer.lastTickPosX) * event.partialTicks;
		double viewerY = viewer.lastTickPosY + (viewer.posY - viewer.lastTickPosY) * event.partialTicks;
		double viewerZ = viewer.lastTickPosZ + (viewer.posZ - viewer.lastTickPosZ) * event.partialTicks;

		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
		GlStateManager.disableTexture2D();

		GlStateManager.translate(-viewerX, -viewerY, -viewerZ);

		GL11.glPolygonOffset(5, 5);
		for (CrystalType type : crystals.keySet()) {
			ReverseWorldRenderer worldRenderer = type.getOverlayVBO();
			if (worldRenderer != null && worldRenderer.getVertexCount() > 0) {
				BlockPos crystal = crystals.get(type);
				GlStateManager.translate(crystal.getX(), crystal.getY(), crystal.getZ());

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

				GlStateManager.translate(-crystal.getX(), -crystal.getY(), -crystal.getZ());
			}
		}
		GL11.glPolygonOffset(0, 0);

		GlStateManager.translate(viewerX, viewerY, viewerZ);

		GlStateManager.enableTexture2D();
	}

	@SubscribeEvent
	public void onWorldUnload(WorldEvent.Unload event) {
		crystals.clear();
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
