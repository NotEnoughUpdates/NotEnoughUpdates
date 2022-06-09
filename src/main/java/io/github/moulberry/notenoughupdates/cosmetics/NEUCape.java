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

package io.github.moulberry.notenoughupdates.cosmetics;

import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL42;
import org.lwjgl.opengl.GL43;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.TreeMap;

public class NEUCape {
	private int currentFrame = 0;
	private int displayFrame = 0;
	private String capeName;
	public ResourceLocation[] capeTextures = null;

	private long lastFrameUpdate = 0;

	private static final int ANIM_MODE_LOOP = 0;
	private static final int ANIM_MODE_PINGPONG = 1;
	private final int animMode = ANIM_MODE_LOOP;

	private CapeNode[] nodes = null;

	private final Random random = new Random();

	private long eventMillis;
	private float dvdPositionX = 100;
	private float dvdPositionY = 100;
	private float dvdVelocityX = -10;
	private float dvdVelocityY = 10;
	private float eventLength;
	private float eventRandom;

	private static final double vertOffset = 1.4;
	private static final double shoulderLength = 0.24;
	private static final double shoulderWidth = 0.13;

	public static final int HORZ_NODES = 6;
	public static final int VERT_NODES = 22;

	public static float targetDist = 1 / 20f;

	private EntityPlayer currentPlayer;
	private boolean keepCurrentPlayer = false;

	private String shaderName = "cape";

	public NEUCape(String capeName) {
		setCapeTexture(capeName);
	}

	public void setCapeTexture(String capeName) {
		if (this.capeName != null && this.capeName.equalsIgnoreCase(capeName)) return;

		startTime = System.currentTimeMillis();
		boolean defaultBehaviour = true;

		if (NotEnoughUpdates.INSTANCE.config.hidden.disableBrokenCapes) {
			if (capeName.equals("negative")) {
				defaultBehaviour = false;
				this.capeName = "fade";
				this.shaderName = "fade_cape";
			}

		}
		if (defaultBehaviour) {
			this.capeName = capeName;

			if (capeName.equalsIgnoreCase("fade")) {
				shaderName = "fade_cape";
			} else if (capeName.equalsIgnoreCase("space")) {
				shaderName = "space_cape";
			} else if (capeName.equalsIgnoreCase("mcworld")) {
				shaderName = "mcworld_cape";
			} else if (capeName.equalsIgnoreCase("lava") || capeName.equalsIgnoreCase("skyclient")) {
				shaderName = "lava_cape";
			} else if (capeName.equalsIgnoreCase("lightning")) {
				shaderName = "lightning_cape";
			} else if (capeName.equalsIgnoreCase("thebakery")) {
				shaderName = "biscuit_cape";
			} else if (capeName.equalsIgnoreCase("negative")) {
				shaderName = "negative";
			} else if (capeName.equalsIgnoreCase("void")) {
				shaderName = "void";
			} else if (capeName.equalsIgnoreCase("tunnel")) {
				shaderName = "tunnel";
			} else if (capeName.equalsIgnoreCase("planets")) {
				shaderName = "planets";
			} else if (capeName.equalsIgnoreCase("screensaver")) {
				shaderName = "screensaver";
			} else {
				shaderName = "shiny_cape";
			}
		}

		ResourceLocation staticCapeTex = new ResourceLocation("notenoughupdates:capes/" + capeName + ".png");
		capeTextures = new ResourceLocation[1];
		capeTextures[0] = staticCapeTex;
	}

	private void bindTexture() {
		if (capeName.equalsIgnoreCase("negative")) {
			CapeManager.getInstance().updateWorldFramebuffer = true;
			if (CapeManager.getInstance().backgroundFramebuffer != null) {
				CapeManager.getInstance().backgroundFramebuffer.bindFramebufferTexture();
			}
		} else if (capeTextures != null && capeTextures.length > 0) {
			long currentTime = System.currentTimeMillis();
			if (currentTime - lastFrameUpdate > 100) {
				lastFrameUpdate = currentTime / 100 * 100;
				currentFrame++;

				if (animMode == ANIM_MODE_PINGPONG) {
					if (capeTextures.length == 1) {
						currentFrame = displayFrame = 0;
					} else {
						int frameCount = 2 * capeTextures.length - 2;
						currentFrame %= frameCount;
						displayFrame = currentFrame;
						if (currentFrame >= capeTextures.length) {
							displayFrame = frameCount - displayFrame;
						}
					}
				} else if (animMode == ANIM_MODE_LOOP) {
					currentFrame %= capeTextures.length;
					displayFrame = currentFrame;
				}
			}
			Minecraft.getMinecraft().getTextureManager().bindTexture(capeTextures[displayFrame]);
		}
	}

	private CapeNode getNode(int x, int y) {
		return nodes[x + y * HORZ_NODES];
	}

	public void createCapeNodes(EntityPlayer player) {
		nodes = new CapeNode[HORZ_NODES * VERT_NODES];

		float pX = (float) player.posX % 7789;
		float pY = (float) player.posY;
		float pZ = (float) player.posZ % 7789;

		float uMinTop = 48 / 1024f;
		float uMaxTop = 246 / 1024f;
		float uMinBottom = 0 / 1024f;
		float uMaxBottom = 293 / 1024f;

		float vMaxSide = 404 / 1024f;
		float vMaxCenter = 419 / 1024f;

		for (int i = 0; i < VERT_NODES; i++) {
			float uMin = uMinTop + (uMinBottom - uMinTop) * i / (float) (VERT_NODES - 1);
			float uMax = uMaxTop + (uMaxBottom - uMaxTop) * i / (float) (VERT_NODES - 1);

			for (int j = 0; j < HORZ_NODES; j++) {
				float vMin = 0f;
				float centerMult = 1 - Math.abs(j - (HORZ_NODES - 1) / 2f) /
					((HORZ_NODES - 1) / 2f);//0-(horzCapeNodes)  -> 0-1-0
				float vMax = vMaxSide + (vMaxCenter - vMaxSide) * centerMult;

				CapeNode node = new CapeNode(pX, pY, pZ);//pX-1, pY+2-i*targetDist, pZ+(j-(horzCapeNodes-1)/2f)*targetDist*2
				node.texU = uMin + (uMax - uMin) * j / (float) (HORZ_NODES - 1);
				node.texV = vMin + (vMax - vMin) * i / (float) (VERT_NODES - 1);

				node.horzDistMult = 2f + 1f * i / (float) (VERT_NODES - 1);

				if (j == 0 || j == HORZ_NODES - 1) {
					node.horzSideTexU = 406 / 1024f * i / (float) (VERT_NODES - 1);
					if (j == 0) {
						node.horzSideTexVTop = 1 - 20 / 1024f;
					} else {
						node.horzSideTexVTop = 1 - 40 / 1024f;
					}
				}
				if (i == 0) {
					node.vertSideTexU = 198 / 1024f * j / (float) (HORZ_NODES - 1);
					node.vertSideTexVTop = 1 - 60 / 1024f;
				} else if (i == VERT_NODES - 1) {
					node.vertSideTexU = 300 / 1024f * j / (float) (HORZ_NODES - 1);
					node.vertSideTexVTop = 1 - 80 / 1024f;
				}
				nodes[j + i * HORZ_NODES] = node;
			}
		}
		for (int y = 0; y < VERT_NODES; y++) {
			for (int x = 0; x < HORZ_NODES; x++) {
				CapeNode node = nodes[x + y * HORZ_NODES];

				for (Direction dir : Direction.values()) {
					for (int i = 1; i <= 2; i++) {
						Offset offset = new Offset(dir, i);

						int xNeighbor = x + offset.getXOffset();
						int yNeighbor = y + offset.getYOffset();

						if (xNeighbor >= 0 && xNeighbor < HORZ_NODES
							&& yNeighbor >= 0 && yNeighbor < VERT_NODES) {
							CapeNode neighbor = nodes[xNeighbor + yNeighbor * HORZ_NODES];
							node.setNeighbor(offset, neighbor);
						}
					}
				}
			}
		}
	}

	public void ensureCapeNodesCreated(EntityPlayer player) {
		if (nodes == null) createCapeNodes(player);
	}

	public enum Direction {
		LEFT(-1, 0),
		UP(0, 1),
		RIGHT(1, 0),
		DOWN(0, -1),
		UPLEFT(-1, 1),
		UPRIGHT(1, 1),
		DOWNLEFT(-1, -1),
		DOWNRIGHT(1, -1);

		int xOff;
		int yOff;

		Direction(int xOff, int yOff) {
			this.xOff = xOff;
			this.yOff = yOff;
		}

		public Direction rotateRight90() {
			int wantXOff = -yOff;
			int wantYOff = xOff;
			for (Direction dir : values()) {
				if (dir.xOff == wantXOff && dir.yOff == wantYOff) {
					return dir;
				}
			}
			return this;
		}

		public Direction rotateLeft90() {
			int wantXOff = yOff;
			int wantYOff = -xOff;
			for (Direction dir : values()) {
				if (dir.xOff == wantXOff && dir.yOff == wantYOff) {
					return dir;
				}
			}
			return this;
		}
	}

	public static class Offset {
		Direction direction;
		int steps;

		public Offset(Direction direction, int steps) {
			this.direction = direction;
			this.steps = steps;
		}

		public int getXOffset() {
			return direction.xOff * steps;
		}

		public int getYOffset() {
			return direction.yOff * steps;
		}

		public boolean equals(Object obj) {
			if (obj instanceof Offset) {
				Offset other = (Offset) obj;
				return other.direction == direction && other.steps == steps;
			}
			return false;
		}

		@Override
		public int hashCode() {
			return 13 * direction.ordinal() + 7 * steps;
		}
	}

	private void loadShaderUniforms(ShaderManager shaderManager) {
		String shaderId = "capes/" + shaderName + "/" + shaderName;
		if (shaderName.equalsIgnoreCase("fade_cape") || shaderName.equalsIgnoreCase("planets")) {
			shaderManager.loadData(shaderId, "millis", (int) (System.currentTimeMillis() - startTime));
		} else if (shaderName.equalsIgnoreCase("space_cape")) {
			shaderManager.loadData(shaderId, "millis", (int) (System.currentTimeMillis() - startTime));
			shaderManager.loadData(shaderId, "eventMillis", (int) (System.currentTimeMillis() - eventMillis));
			shaderManager.loadData(shaderId, "eventRand", eventRandom);
		} else if (shaderName.equalsIgnoreCase("mcworld_cape")) {
			shaderManager.loadData(shaderId, "millis", (int) (System.currentTimeMillis() - startTime));
		} else if (shaderName.equalsIgnoreCase("lava_cape")) {
			shaderManager.loadData(shaderId, "millis", (int) (System.currentTimeMillis() - startTime));
		} else if (shaderName.equalsIgnoreCase("tunnel")) {
			shaderManager.loadData(shaderId, "millis", (int) (System.currentTimeMillis() - startTime));
		} else if (shaderName.equalsIgnoreCase("biscuit_cape") || shaderName.equalsIgnoreCase("shiny_cape")) {
			shaderManager.loadData(shaderId, "millis", (int) (System.currentTimeMillis() - startTime));
			shaderManager.loadData(shaderId, "eventMillis", (int) (System.currentTimeMillis() - eventMillis));
		} else if (shaderName.equalsIgnoreCase("negative")) {
			shaderManager.loadData(shaderId, "screensize", new Vector2f(
				Minecraft.getMinecraft().displayWidth,
				Minecraft.getMinecraft().displayHeight
			));
		} else if (shaderName.equalsIgnoreCase("void")) {
			shaderManager.loadData(shaderId, "millis", (int) (System.currentTimeMillis() - startTime));
			shaderManager.loadData(shaderId, "screensize", new Vector2f(
				Minecraft.getMinecraft().displayWidth,
				Minecraft.getMinecraft().displayHeight
			));
		} else if (shaderName.equalsIgnoreCase("screensaver")) {
			shaderManager.loadData(shaderId, "something", (int) ((System.currentTimeMillis() / 4) % 256));
			shaderManager.loadData(shaderId, "dvdPosition", new Vector2f(dvdPositionX, dvdPositionY));
		}
	}

	long lastRender = 0;

	public void onRenderPlayer(RenderPlayerEvent.Post e) {
		EntityPlayer player = e.entityPlayer;

		if (currentPlayer != null && keepCurrentPlayer && currentPlayer != player) return;

		if (player.getActivePotionEffect(Potion.invisibility) != null) return;
		if (player.isSpectator() || player.isInvisible()) return;

		ensureCapeNodesCreated(player);

		Entity viewer = Minecraft.getMinecraft().getRenderViewEntity();
		double viewerX = (viewer.lastTickPosX + (viewer.posX - viewer.lastTickPosX) * e.partialRenderTick) % 7789;
		double viewerY = viewer.lastTickPosY + (viewer.posY - viewer.lastTickPosY) * e.partialRenderTick;
		double viewerZ = (viewer.lastTickPosZ + (viewer.posZ - viewer.lastTickPosZ) * e.partialRenderTick) % 7789;

		GlStateManager.pushMatrix();
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA,
			GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO
		);
		bindTexture();
		GlStateManager.enableTexture2D();
		GlStateManager.enableDepth();
		GlStateManager.disableCull();
		GlStateManager.disableLighting();
		GlStateManager.color(1, 1, 1, 1);

		if (shaderName.equals("mcworld_cape")) {
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		} else {
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
		}

		GL11.glTranslatef(-(float) viewerX, -(float) viewerY, -(float) viewerZ);

		ShaderManager shaderManager = ShaderManager.getInstance();
		shaderManager.loadShader("capes/" + shaderName + "/" + shaderName);
		loadShaderUniforms(shaderManager);

		renderCape(player, e.partialRenderTick);

		GL11.glTranslatef((float) viewerX, (float) viewerY, (float) viewerZ);

		GL20.glUseProgram(0);

		GlStateManager.enableCull();
		GlStateManager.enableTexture2D();
		GlStateManager.disableBlend();
		GlStateManager.enableDepth();
		GlStateManager.enableLighting();
		GlStateManager.popMatrix();

		lastRender = System.currentTimeMillis();
	}

	public void onTick(TickEvent.ClientTickEvent event, EntityPlayer player) {
		if (player == null) return;
		if (Minecraft.getMinecraft().isGamePaused()) return;

		if (System.currentTimeMillis() - lastRender < 500) {
			if (currentPlayer == null || !keepCurrentPlayer) {
				keepCurrentPlayer = true;
				currentPlayer = player;
			} else if (currentPlayer != player) {
				return;
			}

			ensureCapeNodesCreated(player);

			for (int y = 0; y < VERT_NODES; y++) {
				for (int x = 0; x < HORZ_NODES; x++) {
					CapeNode node = nodes[x + y * HORZ_NODES];
					node.lastPosition.x = node.position.x;
					node.lastPosition.y = node.position.y;
					node.lastPosition.z = node.position.z;
				}
			}
			updateCape(player);
		} else {
			keepCurrentPlayer = false;
		}
	}

	private static double interpolateRotation(float a, float b, float amount) {
		double f;

		for (f = b - a; f < -180.0F; f += 360.0F) {
		}

		while (f >= 180.0F) {
			f -= 360.0F;
		}

		return a + amount * f;
	}

	private double getPlayerRenderAngle(EntityPlayer player, float partialRenderTick) {
		double angle = interpolateRotation(player.prevRenderYawOffset, player.renderYawOffset, partialRenderTick);

		if (player.isRiding() && player.ridingEntity instanceof EntityLivingBase && player.ridingEntity.shouldRiderSit()) {

			EntityLivingBase entitylivingbase = (EntityLivingBase) player.ridingEntity;
			double head = interpolateRotation(player.prevRotationYawHead, player.rotationYawHead, partialRenderTick);
			angle = interpolateRotation(
				entitylivingbase.prevRenderYawOffset,
				entitylivingbase.renderYawOffset,
				partialRenderTick
			);
			double wrapped = MathHelper.wrapAngleTo180_double(head - angle);

			if (wrapped < -85.0F) {
				wrapped = -85.0F;
			}

			if (wrapped >= 85.0F) {
				wrapped = 85.0F;
			}

			angle = head - wrapped;

			if (wrapped * wrapped > 2500.0F) {
				angle += wrapped * 0.2F;
			}
		}

		return Math.toRadians(angle);
	}

	private Vector3f updateFixedCapeNodes(EntityPlayer player) {
		double pX = player.posX % 7789;//player.lastTickPosX + (player.posX - player.lastTickPosX) * partialRenderTick;
		double pY = player.posY;//player.lastTickPosY + (player.posY - player.lastTickPosY) * partialRenderTick;
		double pZ = player.posZ % 7789;//player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialRenderTick;
		double angle = getPlayerRenderAngle(player, 0);

		double vertOffset2 =
			vertOffset + (player.isSneaking() ? -0.22f : 0) + (player.getCurrentArmor(2) != null ? 0.06f : 0);
		double shoulderWidth2 = shoulderWidth + (player.getCurrentArmor(2) != null ? 0.08f : 0);

		float xOff = (float) (Math.cos(angle) * shoulderLength);
		float zOff = (float) (Math.sin(angle) * shoulderLength);

		float totalDX = 0;
		float totalDY = 0;
		float totalDZ = 0;
		int totalMovements = 0;

		for (int i = 0; i < HORZ_NODES; i++) {
			float mult = 1 - 2f * i / (HORZ_NODES - 1); //1 -> -1
			float widthMult = 1.25f - (1.414f * i / (HORZ_NODES - 1) - 0.707f) * (1.414f * i / (HORZ_NODES - 1) - 0.707f);
			CapeNode node = nodes[i];
			float x = (float) pX + (float) (xOff * mult - widthMult * Math.cos(angle + Math.PI / 2) * shoulderWidth2);
			float y = (float) pY + (float) (vertOffset2);
			float z = (float) pZ + (float) (zOff * mult - widthMult * Math.sin(angle + Math.PI / 2) * shoulderWidth2);
			totalDX += x - node.position.x;
			totalDY += y - node.position.y;
			totalDZ += z - node.position.z;
			totalMovements++;
			node.position.x = x;
			node.position.y = y;
			node.position.z = z;
			node.fixed = true;
		}

		float avgDX = totalDX / totalMovements;
		float avgDY = totalDY / totalMovements;
		float avgDZ = totalDZ / totalMovements;

		return new Vector3f(avgDX, avgDY, avgDZ);
	}

	private void updateFixedCapeNodesPartial(EntityPlayer player, float partialRenderTick) {
		double pX = (player.lastTickPosX + (player.posX - player.lastTickPosX) * partialRenderTick) % 7789;
		double pY = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialRenderTick;
		double pZ = (player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialRenderTick) % 7789;
		double angle = getPlayerRenderAngle(player, partialRenderTick);

		double vertOffset2 =
			vertOffset + (player.isSneaking() ? -0.22f : 0) + (player.getCurrentArmor(2) != null ? 0.06f : 0);
		double shoulderWidth2 = shoulderWidth + (player.getCurrentArmor(2) != null ? 0.08f : 0);

		float xOff = (float) (Math.cos(angle) * shoulderLength);
		float zOff = (float) (Math.sin(angle) * shoulderLength);

		for (int i = 0; i < HORZ_NODES; i++) {
			float mult = 1 - 2f * i / (HORZ_NODES - 1); //1 -> -1
			float widthMult = 1.25f - (1.414f * i / (HORZ_NODES - 1) - 0.707f) * (1.414f * i / (HORZ_NODES - 1) - 0.707f);
			CapeNode node = nodes[i];
			node.renderPosition.x = (float) pX + (float) (xOff * mult - widthMult * Math.cos(angle + Math.PI / 2) *
				shoulderWidth2);
			node.renderPosition.y = (float) pY + (float) (vertOffset2);
			node.renderPosition.z = (float) pZ + (float) (zOff * mult - widthMult * Math.sin(angle + Math.PI / 2) *
				shoulderWidth2);
			node.fixed = true;
		}
	}

	private double deltaAngleAccum;
	private double oldPlayerAngle;
	private int crouchTicks = 0;
	long startTime = 0;

	public float deltaYComparedToLine(float x0, float y0, float x1, float y1) {
		float m = (y1 - y0) / (x1 - x0);
		float b = y0 - m * x0;
		float lineAtX = dvdPositionX * m + b;
		return dvdPositionY - lineAtX;
	}

	public float projectOntoLine(float x0, float y0, float x1, float y1, float x) {
		float m = (y1 - y0) / (x1 - x0);
		float b = y0 - m * x0;
		return x * m + b;
	}

	private void updateCape(EntityPlayer player) {
		Vector3f capeTranslation = updateFixedCapeNodes(player);

		if (shaderName.equals("space_cape")) {
			long currentTime = System.currentTimeMillis();
			if (currentTime - startTime > eventMillis - startTime + eventLength) {
				eventMillis = currentTime;
				eventLength = random.nextFloat() * 2000 + 4000;
				eventRandom = random.nextFloat();
			}
		} else if (shaderName.equals("biscuit_cape") || shaderName.equals("shiny_cape")) {
			long currentTime = System.currentTimeMillis();
			if (currentTime - startTime > eventMillis - startTime + eventLength) {
				eventMillis = currentTime;
				eventLength = random.nextFloat() * 3000 + 3000;
			}
		} else if (shaderName.equals("screensaver")) {
			dvdPositionX += dvdVelocityX;
			dvdPositionY += dvdVelocityY;
			float diskSizeX = 162 / 2F, diskSizeY = 78 / 2F;
			// Left line
			if (deltaYComparedToLine(0, 404, 47, 0) < 0) {
				dvdVelocityX = 10;
				dvdPositionX = projectOntoLine(404, 0, 0, 47, dvdPositionY);
			}
			// Bottom line
			if (deltaYComparedToLine(0, 404 - diskSizeY, 292, 404 - diskSizeY) > 0) {
				dvdVelocityY = -10;
				dvdPositionY = 404 - diskSizeY;
			}
			// Top line
			if (deltaYComparedToLine(47, 0, 246, 0) < 0) {
				dvdVelocityY = 10;
				dvdPositionY = 0;
			}
			// Right line
			if (deltaYComparedToLine(246 - diskSizeX, 0, 293 - diskSizeX, 404) < 0) {
				dvdVelocityX = -10;
				dvdPositionX = projectOntoLine(0, 246 - diskSizeX, 404, 293 - diskSizeX, dvdPositionY);
			}
		}

		double playerAngle = getPlayerRenderAngle(player, 0);
		double deltaAngle = playerAngle - oldPlayerAngle;
		if (deltaAngle > Math.PI) {
			deltaAngle = 2 * Math.PI - deltaAngle;
		}
		if (deltaAngle < -Math.PI) {
			deltaAngle = 2 * Math.PI + deltaAngle;
		}
		deltaAngleAccum *= 0.5f;
		deltaAngleAccum += deltaAngle;

		float dX = (float) Math.cos(playerAngle + Math.PI / 2f);
		float dZ = (float) Math.sin(playerAngle + Math.PI / 2f);

		float factor = (float) (deltaAngleAccum * deltaAngleAccum);

		float capeTransLength = capeTranslation.length();

		float capeTranslationFactor = 0f;
		if (capeTransLength > 0.5f) {
			capeTranslationFactor = (capeTransLength - 0.5f) / capeTransLength;
		}
		Vector3f lookDir = new Vector3f(dX, 0, dZ);
		Vector3f lookDirNorm = lookDir.normalise(null);
		float dot = Vector3f.dot(capeTranslation, lookDirNorm);
		if (dot < 0) { //Moving backwards
			for (int y = 0; y < VERT_NODES; y++) {
				for (int x = 0; x < HORZ_NODES; x++) {
					CapeNode node = nodes[x + y * HORZ_NODES];
					if (!node.fixed) {
						node.position.x += lookDirNorm.x * dot;
						node.position.y += lookDirNorm.y * dot;
						node.position.z += lookDirNorm.z * dot;
					}
				}
			}
			//Apply small backwards force
			factor = 0.05f;
		}

		if (factor > 0) {
			for (int y = 0; y < VERT_NODES; y++) {
				for (int x = 0; x < HORZ_NODES; x++) {
					nodes[x + y * HORZ_NODES].applyForce(-dX * factor, 0, -dZ * factor);
				}
			}
		}

		if (capeTranslationFactor > 0f) {
			float capeDX = capeTranslation.x * capeTranslationFactor;
			float capeDY = capeTranslation.y * capeTranslationFactor;
			float capeDZ = capeTranslation.z * capeTranslationFactor;

			for (int y = 0; y < VERT_NODES; y++) {
				for (int x = 0; x < HORZ_NODES; x++) {
					CapeNode node = nodes[x + y * HORZ_NODES];
					if (!node.fixed) {
						node.position.x += capeDX;
						node.position.y += capeDY;
						node.position.z += capeDZ;
					}
				}
			}
		}

		//Wind
		float currTime = (System.currentTimeMillis() - startTime) / 1000f;
		float windRandom = Math.abs((float) (0.5f * Math.sin(0.22f * currTime) + Math.sin(0.44f * currTime) * Math.sin(
			0.47f * currTime)));
		double windDir = playerAngle + Math.PI / 4f * Math.sin(0.2f * currTime);

		float windDX = (float) Math.cos(windDir + Math.PI / 2f);
		float windDZ = (float) Math.sin(windDir + Math.PI / 2f);
		for (int y = 0; y < VERT_NODES; y++) {
			for (int x = 0; x < HORZ_NODES; x++) {
				nodes[x + y * HORZ_NODES].applyForce(-windDX * windRandom * 0.01f, 0, -windDZ * windRandom * 0.01f);
			}
		}

		if (player.isSneaking()) {
			crouchTicks++;
			float mult = 0.5f;
			if (crouchTicks < 5) {
				mult = 2f;
			}
			for (int y = 0; y < 8; y++) {
				for (int x = 0; x < HORZ_NODES; x++) {
					nodes[x + y * HORZ_NODES].applyForce(-dX * mult, 0, -dZ * mult);
				}
			}
		} else {
			crouchTicks = 0;
		}

		Vector3f avgPosition = avgFixedPosition();
		for (int y = 0; y < VERT_NODES; y++) {
			for (int x = 0; x < HORZ_NODES; x++) {
				CapeNode node = nodes[x + y * HORZ_NODES];

				Vector3f delta = Vector3f.sub(node.position, avgPosition, null);

				if (delta.lengthSquared() > 5 * 5) {
					Vector3f norm = delta.normalise(null);
					node.position = Vector3f.add(avgPosition, norm, null);
				}
			}
		}

		oldPlayerAngle = playerAngle;

		for (int y = 0; y < VERT_NODES; y++) {
			for (int x = 0; x < HORZ_NODES; x++) {
				nodes[x + y * HORZ_NODES].update();
			}
		}
		int updates = 50;
		for (int i = 0; i < updates; i++) {
			for (int y = 0; y < VERT_NODES; y++) {
				for (int x = 0; x < HORZ_NODES; x++) {
					nodes[x + y * HORZ_NODES].resolveAll(2 + 1f * y / VERT_NODES, false);
				}
			}
		}
	}

	private int ssbo = -1;

	private void generateSSBO() {
		ssbo = GL15.glGenBuffers();
		loadSBBO();
	}

	private void loadSBBO() {
		FloatBuffer buff = BufferUtils.createFloatBuffer(CapeNode.FLOAT_NUM * HORZ_NODES * VERT_NODES);
		for (int y = 0; y < VERT_NODES; y++) {
			for (int x = 0; x < HORZ_NODES; x++) {
				nodes[x + y * HORZ_NODES].loadIntoBuffer(buff);
			}
		}
		buff.flip();

		GL15.glBindBuffer(GL43.GL_SHADER_STORAGE_BUFFER, ssbo);
		GL15.glBufferData(GL43.GL_SHADER_STORAGE_BUFFER, buff, GL15.GL_DYNAMIC_DRAW);
		GL15.glBindBuffer(GL43.GL_SHADER_STORAGE_BUFFER, 0);
	}

	private void resolveAllCompute() {
		if (ssbo == -1) {
			generateSSBO();
		}
		loadSBBO();

		int program = ShaderManager.getInstance().getShader("node");

		int block_index = GL43.glGetProgramResourceIndex(program, GL43.GL_SHADER_STORAGE_BLOCK, "nodes_buffer");
		int ssbo_binding_point_index = 0;
		GL43.glShaderStorageBlockBinding(program, block_index, ssbo_binding_point_index);
		int binding_point_index = 0;
		GL30.glBindBufferBase(GL43.GL_SHADER_STORAGE_BUFFER, binding_point_index, ssbo);

		GL20.glUseProgram(program);

		for (int i = 0; i < 30; i++) {
			GL43.glDispatchCompute(VERT_NODES, 1, 1);
			GL42.glMemoryBarrier(GL43.GL_SHADER_STORAGE_BARRIER_BIT);
		}

		GL20.glUseProgram(0);

		FloatBuffer buff = BufferUtils.createFloatBuffer(CapeNode.FLOAT_NUM * HORZ_NODES * VERT_NODES);

		GL15.glBindBuffer(GL43.GL_SHADER_STORAGE_BUFFER, ssbo);
		GL15.glGetBufferSubData(GL43.GL_SHADER_STORAGE_BUFFER, 0, buff);
		GL15.glBindBuffer(GL43.GL_SHADER_STORAGE_BUFFER, 0);

		for (int y = 0; y < VERT_NODES; y++) {
			for (int x = 0; x < HORZ_NODES; x++) {
				nodes[x + y * HORZ_NODES].readFromBuffer(buff);
			}
		}
	}

	private Vector3f avgRenderPosition() {
		Vector3f accum = new Vector3f();
		int num = 0;
		for (int y = 0; y < VERT_NODES; y++) {
			for (int x = 0; x < HORZ_NODES; x++) {
				CapeNode node = nodes[x + y * HORZ_NODES];
				Vector3f.add(accum, node.renderPosition, accum);
				num++;
			}
		}
		if (num != 0) {
			accum.scale(1f / num);
		}
		return accum;
	}

	private Vector3f avgNormal() {
		Vector3f accum = new Vector3f();
		int num = 0;
		for (int y = 0; y < VERT_NODES; y++) {
			for (int x = 0; x < HORZ_NODES; x++) {
				CapeNode node = nodes[x + y * HORZ_NODES];
				Vector3f.add(accum, node.normal(), accum);
				num++;
			}
		}
		if (num != 0) {
			accum.scale(1f / num);
		}
		return accum;
	}

	private Vector3f avgFixedRenderPosition() {
		Vector3f accum = new Vector3f();
		int numFixed = 0;
		for (int y = 0; y < VERT_NODES; y++) {
			for (int x = 0; x < HORZ_NODES; x++) {
				CapeNode node = nodes[x + y * HORZ_NODES];
				if (node.fixed) {
					Vector3f.add(accum, node.renderPosition, accum);
					numFixed++;
				}
			}
		}
		if (numFixed != 0) {
			accum.scale(1f / numFixed);
		}
		return accum;
	}

	private Vector3f avgFixedPosition() {
		Vector3f accum = new Vector3f();
		int numFixed = 0;
		for (int y = 0; y < VERT_NODES; y++) {
			for (int x = 0; x < HORZ_NODES; x++) {
				CapeNode node = nodes[x + y * HORZ_NODES];
				if (node.fixed) {
					Vector3f.add(accum, node.position, accum);
					numFixed++;
				}
			}
		}
		if (numFixed != 0) {
			accum.scale(1f / numFixed);
		}
		return accum;
	}

	private void renderBackAndDoFrontStencil() {
		for (int y = 0; y < VERT_NODES; y++) {
			for (int x = 0; x < HORZ_NODES; x++) {
				nodes[x + y * HORZ_NODES].renderNode(CapeNode.DRAW_MASK_BACK | CapeNode.DRAW_MASK_SIDES);
			}
		}

		if (!Minecraft.getMinecraft().getFramebuffer().isStencilEnabled())
			Minecraft.getMinecraft().getFramebuffer().enableStencil();

		GL11.glEnable(GL11.GL_STENCIL_TEST);
		GL11.glStencilFunc(GL11.GL_ALWAYS, 1, 0xFF);
		GL11.glStencilOp(GL11.GL_ZERO, GL11.GL_ZERO, GL11.GL_REPLACE);
		GL11.glStencilMask(0xFF);
		GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);
		GlStateManager.enableDepth();

		GL11.glColorMask(false, false, false, false);
		for (int y = 0; y < VERT_NODES; y++) {
			for (int x = 0; x < HORZ_NODES; x++) {
				nodes[x + y * HORZ_NODES].renderNode(CapeNode.DRAW_MASK_FRONT);
			}
		}
		GL11.glColorMask(true, true, true, true);

		// Only pass stencil test if equal to 1
		GL11.glStencilMask(0x00);
		GL11.glStencilFunc(GL11.GL_EQUAL, 1, 0xFF);
	}

	private Vector3f getPoint(Vector3f point, Vector3f... vectors) {
		Vector3f res = new Vector3f(point);
		for (Vector3f vec : vectors) Vector3f.add(res, vec, res);
		return res;
	}

	private static void renderVBO(WorldRenderer worldRenderer) {
		if (worldRenderer != null && worldRenderer.getVertexCount() > 0) {
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
		}
	}

	private static WorldRenderer sphereVBO = null;

	private void renderNodes() {
		if (capeName.equalsIgnoreCase("planets")) {
			renderBackAndDoFrontStencil();

			Vector3f pointNorm = avgNormal();
			Vector3f capeAvgPos = avgRenderPosition();

			pointNorm.scale(0.5f / pointNorm.length());
			pointNorm.scale(1 - pointNorm.y / 1.3f);
			Vector3f point = Vector3f.sub(capeAvgPos, pointNorm, null);

			if (sphereVBO == null || Keyboard.isKeyDown(Keyboard.KEY_K)) {
				if (sphereVBO != null) sphereVBO.reset();

				int arcSegments = 24;
				int rotationSegments = 24;
				double arcAngleDelta = Math.PI / (arcSegments - 1);

				float xScale = 0.95f;

				double diameterUnitArcLen = 0;

				double arcAngle = 0;
				for (int i = 0; i < arcSegments; i++) {
					diameterUnitArcLen += Math.sin(arcAngle);
					arcAngle += arcAngleDelta;
				}
				double arcLength = 2f / diameterUnitArcLen;

				List<List<Vector3f>> arcs = new ArrayList<>();
				for (int angleI = 0; angleI < rotationSegments; angleI++) {
					double angle = Math.PI * 2 * angleI / rotationSegments;

					List<Vector3f> arc = new ArrayList<>();

					Vector3f arcPos = new Vector3f(0, 0, -1);

					arc.add(arcPos);

					arcAngle = 0;
					for (int segmentI = 0; segmentI < arcSegments; segmentI++) {

						double deltaZ = Math.sin(arcAngle) * arcLength;
						double deltaY = Math.cos(arcAngle) * Math.cos(angle) * arcLength;
						double deltaX = Math.cos(arcAngle) * Math.sin(angle) * arcLength * xScale;

						arcPos = new Vector3f(arcPos);
						arcPos.z += deltaZ;
						arcPos.y += deltaY;
						arcPos.x += deltaX;
						arcPos.normalise();
						arc.add(arcPos);

						arcAngle += arcAngleDelta;
					}

					arcs.add(arc);
				}

				sphereVBO = new WorldRenderer(8 * 4 * rotationSegments * arcSegments);
				sphereVBO.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_NORMAL);

				double maxXYRad = 0;
				for (int angleI = 0; angleI < rotationSegments; angleI++) {
					for (int segmentI = 0; segmentI <= arcSegments; segmentI++) {
						List<Vector3f> thisArc = arcs.get(angleI);
						Vector3f point1 = thisArc.get(segmentI);
						double rad = Math.sqrt(point1.x * point1.x + point1.y * point1.y);
						maxXYRad = Math.max(maxXYRad, rad);
					}
				}

				for (int angleI = 0; angleI < rotationSegments; angleI++) {

					int nextAngleI = angleI + 1;
					if (angleI == rotationSegments - 1) {
						nextAngleI = 0;
					}

					float v = 0.5f * (angleI) / (rotationSegments);
					float v2 = 0.5f * (angleI + 1) / (rotationSegments);
					//if(v2 == 0) v2 = 0.5f;

					List<Vector3f> thisArc = arcs.get(angleI);
					List<Vector3f> nextArc = arcs.get(nextAngleI);

					for (int segmentI = 1; segmentI <= arcSegments; segmentI++) {
						Vector3f point1 = thisArc.get(segmentI);
						Vector3f point2 = thisArc.get(segmentI - 1);
						Vector3f point3 = nextArc.get(segmentI - 1);
						Vector3f point4 = nextArc.get(segmentI);

						double u1 = 0.5f * segmentI / arcSegments;
						double u2 = 0.5f * (segmentI - 1) / arcSegments;

						sphereVBO.pos(point4.x, point4.y, point4.z)
										 .tex(u1, v2).normal(-point4.x, -point4.y, -point4.z).endVertex();
						sphereVBO.pos(point3.x, point3.y, point3.z)
										 .tex(u2, v2).normal(-point3.x, -point3.y, -point3.z).endVertex();
						sphereVBO.pos(point2.x, point2.y, point2.z)
										 .tex(u2, v).normal(-point2.x, -point2.y, -point2.z).endVertex();
						sphereVBO.pos(point1.x, point1.y, point1.z)
										 .tex(u1, v).normal(-point1.x, -point1.y, -point1.z).endVertex();
					}
				}
			}

			String shaderId = "capes/" + shaderName + "/" + shaderName;
			double mercuryAngle = Math.PI * 2 * ((System.currentTimeMillis() - startTime) / 10000f % 1);
			double mercuryX = Math.sin(mercuryAngle) * 0.3;
			double mercuryZ = Math.cos(mercuryAngle) * 0.3;

			double earthAngle = Math.PI * 2 * ((System.currentTimeMillis() - startTime) / 30000f % 1);
			double earthSlant = Math.PI * 0.1;
			double earthX = Math.sin(earthAngle) * Math.cos(earthSlant) * 0.6;
			double earthY = Math.sin(earthAngle) * Math.sin(earthSlant) * 0.6;
			double earthZ = Math.cos(earthAngle) * Math.cos(earthSlant) * 0.6;

			float sunDist = Vector3f.sub(point, capeAvgPos, null).lengthSquared();
			float mercuryDist = Vector3f.sub(new Vector3f(point.x + (float) mercuryX, point.y, point.z + (float) mercuryZ),
				capeAvgPos, null
			).lengthSquared();
			float earthDist = Vector3f.sub(new Vector3f(
					point.x + (float) earthX,
					point.y + (float) earthY,
					point.z + (float) earthZ
				),
				capeAvgPos, null
			).lengthSquared();

			double jupiterAngle = Math.PI * 2 * ((System.currentTimeMillis() - startTime) / 200000f % 1);
			double jupiterSlant = Math.PI * -0.08;
			double jupiterX = Math.sin(jupiterAngle) * Math.cos(jupiterSlant) * 1.5;
			double jupiterY = Math.sin(jupiterAngle) * Math.sin(jupiterSlant) * 1.5;
			double jupiterZ = Math.cos(jupiterAngle) * Math.cos(jupiterSlant) * 1.5;
			float jupiterDist = Vector3f.sub(new Vector3f(
					point.x + (float) jupiterX,
					point.y + (float) jupiterY,
					point.z + (float) jupiterZ
				),
				capeAvgPos, null
			).lengthSquared();

			double neptuneX = -Math.sin(earthAngle) * Math.cos(earthSlant);
			double neptuneY = -Math.sin(earthAngle) * Math.sin(earthSlant);
			double neptuneZ = -Math.cos(earthAngle) * Math.cos(earthSlant);

			float neptuneDist = Vector3f.sub(new Vector3f(
					point.x + (float) neptuneX,
					point.y + (float) neptuneY,
					point.z + (float) neptuneZ
				),
				capeAvgPos, null
			).lengthSquared();

			TreeMap<Float, Integer> orbitals = new TreeMap<>();
			orbitals.put(sunDist, 0);
			orbitals.put(earthDist, 1);
			orbitals.put(mercuryDist, 2);

			double delta = Minecraft.getMinecraft().getRenderViewEntity().getRotationYawHead() % 360;
			while (delta < 0) delta += 360;

			double jupDelta = (delta + Math.toDegrees(jupiterAngle)) % 360;
			while (jupDelta < 0) jupDelta += 360;
			if (jupDelta > 250 || jupDelta < 110) orbitals.put(jupiterDist, 3);

			double nepDelta = (delta + Math.toDegrees(-earthAngle)) % 360;
			while (nepDelta < 0) nepDelta += 360;
			if (nepDelta > 250 || nepDelta < 110) orbitals.put(neptuneDist, 4);

			GlStateManager.disableDepth();
			GlStateManager.enableCull();

			for (int planetId : orbitals.descendingMap().values()) {
				GlStateManager.pushMatrix();
				switch (planetId) {
					case 0: {
						GlStateManager.translate(point.x, point.y, point.z);
						GlStateManager.scale(0.2f, 0.2f, 0.2f);
						break;
					}
					case 1: {
						Vector3f sunVec = new Vector3f((float) earthX, (float) earthY, (float) earthZ);
						ShaderManager.getInstance().loadData(shaderId, "sunVec", sunVec);
						GlStateManager.translate(point.x + earthX, point.y + earthY, point.z + earthZ);
						GlStateManager.scale(0.1f, 0.1f, 0.1f);
						break;
					}
					case 2: {
						Vector3f sunVec = new Vector3f((float) mercuryX, 0, (float) mercuryZ);
						ShaderManager.getInstance().loadData(shaderId, "sunVec", sunVec);
						GlStateManager.translate(point.x + mercuryX, point.y, point.z + mercuryZ);
						GlStateManager.scale(0.05f, 0.05f, 0.05f);
						break;
					}
					case 3: {
						Vector3f sunVec = new Vector3f((float) jupiterX, (float) jupiterY, (float) jupiterZ);
						ShaderManager.getInstance().loadData(shaderId, "sunVec", sunVec);
						GlStateManager.translate(point.x + jupiterX, point.y + jupiterY, point.z + jupiterZ);
						GlStateManager.scale(0.3f, 0.3f, 0.3f);
						break;
					}
					case 4: {
						Vector3f sunVec = new Vector3f((float) neptuneX, (float) neptuneY, (float) neptuneZ);
						ShaderManager.getInstance().loadData(shaderId, "sunVec", sunVec);
						GlStateManager.translate(point.x + neptuneX, point.y + neptuneY, point.z + neptuneZ);
						GlStateManager.scale(0.15f, 0.15f, 0.15f);
						break;
					}
				}
				ShaderManager.getInstance().loadData(shaderId, "planetType", planetId);
				renderVBO(sphereVBO);
				GlStateManager.popMatrix();
			}

			GlStateManager.disableCull();
			GlStateManager.enableDepth();

			GL11.glDisable(GL11.GL_STENCIL_TEST);
		} else if (capeName.equalsIgnoreCase("parallax")) {
			renderBackAndDoFrontStencil();

			Vector3f pointNorm = avgNormal();
			pointNorm.scale(-0.2f / pointNorm.length());
			Vector3f negPointNorm = new Vector3f(pointNorm);
			negPointNorm.scale(-1);
			//pointNorm.scale(1 - pointNorm.y/1.3f);
			Vector3f point = Vector3f.add(avgRenderPosition(), pointNorm, null);
			Vector3f fixedPoint = Vector3f.add(avgFixedRenderPosition(), pointNorm, null);

			Vector3f up = Vector3f.sub(fixedPoint, point, null);
			float halfUp = up.length();

			Vector3f down = new Vector3f(up);
			down.scale(-1);

			Vector3f left = Vector3f.cross(up, pointNorm, null);
			left.scale(halfUp * 522f / 341f / left.length());
			Vector3f right = new Vector3f(left);
			right.scale(-1);

			Vector3f point1 = getPoint(point, left);
			Vector3f point2 = getPoint(point, left, down, down);
			Vector3f point3 = getPoint(point, right, down, down);
			Vector3f point4 = getPoint(point, right);

			Vector3f point2Edge = getPoint(point2, negPointNorm, negPointNorm);
			Vector3f point3Edge = getPoint(point3, negPointNorm, negPointNorm);

			GlStateManager.disableDepth();
			GlStateManager.disableCull();

			GlStateManager.color(1, 1, 1, 1);

			Tessellator tessellator = Tessellator.getInstance();
			WorldRenderer worldrenderer = tessellator.getWorldRenderer();
			worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

			worldrenderer.pos(point1.x, point1.y, point1.z)
									 .tex(0, 943 / 1024f).endVertex();
			worldrenderer.pos(point2.x, point2.y, point2.z)
									 .tex(280 / 1024f, 943 / 1024f).endVertex();
			worldrenderer.pos(point3.x, point3.y, point3.z)
									 .tex(280 / 1024f, 421 / 1024f).endVertex();
			worldrenderer.pos(point4.x, point4.y, point4.z)
									 .tex(0, 421 / 1024f).endVertex();

			tessellator.draw();

			worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

			worldrenderer.pos(point2.x, point2.y, point2.z)
									 .tex(280 / 1024f, 943 / 1024f).endVertex();
			worldrenderer.pos(point2Edge.x, point2Edge.y, point2Edge.z)
									 .tex(341 / 1024f, 943 / 1024f).endVertex();
			worldrenderer.pos(point3Edge.x, point3Edge.y, point3Edge.z)
									 .tex(341 / 1024f, 421 / 1024f).endVertex();
			worldrenderer.pos(point3.x, point3.y, point3.z)
									 .tex(280 / 1024f, 421 / 1024f).endVertex();

			tessellator.draw();

			GlStateManager.disableCull();
			GlStateManager.enableDepth();

			GL11.glDisable(GL11.GL_STENCIL_TEST);
		} else if (capeName.equalsIgnoreCase("tunnel")) {
			renderBackAndDoFrontStencil();

			Vector3f pointNorm = avgNormal();

			pointNorm.scale(0.7f / pointNorm.length());
			pointNorm.scale(1 - pointNorm.y / 1.3f);
			Vector3f point = Vector3f.sub(avgRenderPosition(), pointNorm, null);

			List<CapeNode> edgeNodes = new ArrayList<>();
			List<Vector2f> edgeCoords = new ArrayList<>();

			//Left edge
			for (int y = 0; y < VERT_NODES; y++) {
				edgeNodes.add(nodes[y * HORZ_NODES]);
				edgeCoords.add(new Vector2f(0, (float) y / (VERT_NODES - 1)));
			}
			edgeNodes.add(null);
			edgeCoords.add(null);
			//Bottom edge
			int bottomIndex = VERT_NODES - 1;
			int botSize = HORZ_NODES;
			for (int x = 0; x < botSize; x++) {
				edgeNodes.add(getNode(x, bottomIndex));
				edgeCoords.add(new Vector2f((float) x / (botSize - 1), 1));
			}
			edgeNodes.add(null);
			edgeCoords.add(null);
			//Right edge
			for (int y = VERT_NODES - 1; y >= 0; y--) {
				edgeNodes.add(getNode(HORZ_NODES - 1, y));
				edgeCoords.add(new Vector2f(1, (float) y / VERT_NODES));
			}
			edgeNodes.add(null);
			edgeCoords.add(null);
			//Top edge
			int topSize = HORZ_NODES;
			for (int x = topSize - 1; x >= 0; x--) {
				edgeNodes.add(getNode(x, 0));
				edgeCoords.add(new Vector2f((float) x / (topSize - 1), 0));
			}

			GlStateManager.disableDepth();
			GlStateManager.enableCull();
			CapeNode last = null;
			for (int i = 0; i < edgeNodes.size(); i++) {
				CapeNode node = edgeNodes.get(i);
				if (last != null && node != null) {
					Vector2f lastCoord = edgeCoords.get(i - 1);
					Vector2f coord = edgeCoords.get(i);

					Tessellator tessellator = Tessellator.getInstance();
					WorldRenderer worldrenderer = tessellator.getWorldRenderer();
					worldrenderer.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_TEX_NORMAL);

					Vector3f lastNodeNorm = last.normal();
					worldrenderer.pos(
												 last.renderPosition.x + lastNodeNorm.x * 0.05f,
												 last.renderPosition.y + lastNodeNorm.y * 0.05f,
												 last.renderPosition.z + lastNodeNorm.z * 0.05f
											 )
											 .tex(lastCoord.x * 300f / 1024f, lastCoord.y * 420f / 1024f)
											 .normal(-lastNodeNorm.x, -lastNodeNorm.y, -lastNodeNorm.z).endVertex();

					Vector3f nodeNorm = node.normal();
					worldrenderer.pos(
												 node.renderPosition.x + nodeNorm.x * 0.05f,
												 node.renderPosition.y + nodeNorm.y * 0.05f,
												 node.renderPosition.z + nodeNorm.z * 0.05f
											 )
											 .tex(coord.x * 300f / 1024f, coord.y * 420f / 1024f)
											 .normal(-nodeNorm.x, -nodeNorm.y, -nodeNorm.z).endVertex();

					worldrenderer.pos(point.x, point.y, point.z)
											 .tex(150f / 1024f, 210f / 1024f)
											 .normal(-pointNorm.x, -pointNorm.y, -pointNorm.z).endVertex();

					tessellator.draw();
				}
				last = node;
			}
			GlStateManager.disableCull();
			GlStateManager.enableDepth();

			GL11.glDisable(GL11.GL_STENCIL_TEST);
		} else {
			for (int y = 0; y < VERT_NODES; y++) {
				for (int x = 0; x < HORZ_NODES; x++) {
					nodes[x + y * HORZ_NODES].renderNode();
				}
			}
		}
	}

	private void renderCape(EntityPlayer player, float partialRenderTick) {
		ensureCapeNodesCreated(player);

		Vector3f avgPositionFixedBefore = avgFixedRenderPosition();
		updateFixedCapeNodesPartial(player, partialRenderTick);
		Vector3f avgPositionFixed = avgFixedRenderPosition();
		Vector3f delta = Vector3f.sub(avgPositionFixed, avgPositionFixedBefore, null);

		if (delta.lengthSquared() > 9) {
			updateFixedCapeNodes(player);

			for (int y = 0; y < VERT_NODES; y++) {
				for (int x = 0; x < HORZ_NODES; x++) {
					CapeNode node = nodes[x + y * HORZ_NODES];
					if (!node.fixed) {
						Vector3f.add(node.renderPosition, delta, node.renderPosition);
						node.position.set(node.renderPosition);
						node.lastPosition.set(node.renderPosition);
					} else {
						node.lastPosition.set(node.position);
					}
				}
			}

			renderNodes();
			return;
		}

		for (int y = 0; y < VERT_NODES; y++) {
			for (int x = 0; x < HORZ_NODES; x++) {
				CapeNode node = nodes[x + y * HORZ_NODES];

				node.resetNormal();

				if (node.fixed) continue;

				Vector3f newPosition = new Vector3f();
				newPosition.x = node.lastPosition.x + (node.position.x - node.lastPosition.x) * partialRenderTick;
				newPosition.y = node.lastPosition.y + (node.position.y - node.lastPosition.y) * partialRenderTick;
				newPosition.z = node.lastPosition.z + (node.position.z - node.lastPosition.z) * partialRenderTick;

				int length = node.oldRenderPosition.length;

				int fps = Minecraft.getDebugFPS();
				if (fps < 50) {
					length = 2;
				} else if (fps < 100) {
					length = 2 + (int) ((fps - 50) / 50f * 3);
				}

				if (node.oldRenderPosition[length - 1] == null) {
					Arrays.fill(node.oldRenderPosition, Vector3f.sub(newPosition, avgPositionFixed, null));
					node.renderPosition = newPosition;
				} else {
					Vector3f accum = new Vector3f();
					for (int i = 0; i < length; i++) {
						Vector3f.add(accum, node.oldRenderPosition[i], accum);
						Vector3f.add(accum, avgPositionFixed, accum);
					}
					accum.scale(1 / (float) (length));

					float blendFactor = 0.5f + 0.3f * y / (float) (VERT_NODES - 1); //0.5/0.5 -> 0.8/0.2 //0-1
					accum.scale(blendFactor);
					newPosition.scale(1 - blendFactor);
					Vector3f.add(accum, newPosition, accum);
					node.renderPosition = accum;
				}

				if (!Minecraft.getMinecraft().isGamePaused()) {
					for (int i = node.oldRenderPosition.length - 1; i >= 0; i--) {
						if (i > 0) {
							node.oldRenderPosition[i] = node.oldRenderPosition[i - 1];
						} else {
							node.oldRenderPosition[i] = Vector3f.sub(node.renderPosition, avgPositionFixed, null);
						}
					}
				}
			}
		}
		renderNodes();
	}
}
