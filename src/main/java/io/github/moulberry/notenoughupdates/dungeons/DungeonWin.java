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

package io.github.moulberry.notenoughupdates.dungeons;

import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DungeonWin {
	private static class Confetti {
		private float x;
		private float y;
		private float xLast;
		private float yLast;
		private int life = 0;
		private float xVel;
		private float yVel;
		private final int id;

		public Confetti(float x, float y, float xVel, float yVel) {
			this.x = x;
			this.xLast = x;
			this.y = y;
			this.yLast = y;
			this.xVel = xVel;
			this.yVel = yVel;
			this.id = rand.nextInt(16);
			this.life = 20 + rand.nextInt(10);
		}
	}

	public static ResourceLocation CONFETTI = new ResourceLocation("notenoughupdates:dungeon_win/confetti.png");
	public static ResourceLocation SPLUS = new ResourceLocation("notenoughupdates:dungeon_win/splus.png");
	public static ResourceLocation S = new ResourceLocation("notenoughupdates:dungeon_win/s.png");
	public static ResourceLocation A = new ResourceLocation("notenoughupdates:dungeon_win/a.png");
	public static ResourceLocation B = new ResourceLocation("notenoughupdates:dungeon_win/b.png");
	public static ResourceLocation C = new ResourceLocation("notenoughupdates:dungeon_win/c.png");
	public static ResourceLocation D = new ResourceLocation("notenoughupdates:dungeon_win/d.png");
	public static ResourceLocation TEAM_SCORE = SPLUS;

	private static final int SCALE_FACTOR = 3;
	private static final int WIDTH = 32 * SCALE_FACTOR;
	private static final int HEIGHT = 16 * SCALE_FACTOR;

	private static boolean hideChat = false;
	private static long lastDungeonFinish = 0;
	private static final Pattern TEAM_SCORE_REGEX = Pattern.compile("Team Score: [0-9]+ \\((S\\+|S|A|B|C|D)\\)");

	private static final ScheduledExecutorService SES = Executors.newScheduledThreadPool(1);

	public static Random rand = new Random();
	public static List<Confetti> confetti = new ArrayList<>();
	public static List<String> text = new ArrayList<>();
	public static long startTime = 0;

	private static boolean seenDungeonWinOverlayThisRun = false;

	static {
		for (int i = 0; i < 10; i++) {
			text.add("{PLACEHOLDER DUNGEON STAT #" + i + "}");
		}
	}

	public static void displayWin() {
		if (NotEnoughUpdates.INSTANCE.config.dungeons.dungeonWinMillis < 100 ||
			!NotEnoughUpdates.INSTANCE.config.dungeons.enableDungeonWin)
			return;
		startTime = System.currentTimeMillis();
		confetti.clear();
	}

	public static void tick() {
		if (NotEnoughUpdates.INSTANCE.config.dungeons.dungeonWinMillis < 100 ||
			!NotEnoughUpdates.INSTANCE.config.dungeons.enableDungeonWin)
			return;
		if (System.currentTimeMillis() - startTime > 5000) return;
		int deltaTime = (int) (System.currentTimeMillis() - startTime);

		if (deltaTime < 1000) {
			ScaledResolution sr = Utils.pushGuiScale(2);
			int cap = 0;
			switch (TEAM_SCORE.getResourcePath()) {
				case "dungeon_win/splus.png":
					cap = 200;
					break;
				case "dungeon_win/s.png":
					cap = 100;
					break;
				case "dungeon_win/a.png":
					cap = 50;
					break;
			}
			int maxConfetti = Math.min(cap, deltaTime / 5);
			while (confetti.size() < maxConfetti) {
				int y;
				if (deltaTime < 500) {
					y = sr.getScaledHeight() / 2 - (int) (Math.sin(deltaTime / 1000f * Math.PI) * sr.getScaledHeight() / 9);
				} else {
					y = sr.getScaledHeight() / 6 + (int) (Math.sin(deltaTime / 1000f * Math.PI) * sr.getScaledHeight() * 4 / 18);
				}
				int xOffset = -WIDTH / 2 + rand.nextInt(WIDTH);
				int x = sr.getScaledWidth() / 2 + xOffset;

				int xVel = xOffset / 2;
				int yVel = -25 - rand.nextInt(10) + Math.abs(xVel) / 2;

				confetti.add(new Confetti(x, y, xVel, yVel));
			}
		} else {
			Set<Confetti> toRemove = new HashSet<>();
			for (Confetti c : confetti) {
				if (c.life <= 0) {
					toRemove.add(c);
				}
			}
			try {
				confetti.removeAll(toRemove);
			} catch (ConcurrentModificationException ignored) {
			}
		}

		Utils.pushGuiScale(-1);
		for (Confetti c : confetti) {
			c.yVel += 1;
			c.xVel /= 1.1f;
			c.yVel /= 1.1f;
			c.xLast = c.x;
			c.yLast = c.y;
			c.x += c.xVel;
			c.y += c.yVel;
			c.life--;
		}
	}

	public static void onChatMessage(ClientChatReceivedEvent e) {
		if (e.type == 2) return;

		if (NotEnoughUpdates.INSTANCE.config.dungeons.dungeonWinMillis < 100 ||
			!NotEnoughUpdates.INSTANCE.config.dungeons.enableDungeonWin)
			return;

		long currentTime = System.currentTimeMillis();
		String unformatted = Utils.cleanColour(e.message.getUnformattedText());

		//Added two more Resets, can't do Reset+Reset+Reset cause idk?
		//hypixel please don't randomly add more

		if (e.message.getFormattedText().startsWith(
			EnumChatFormatting.RESET + "" + EnumChatFormatting.RESET + "" + EnumChatFormatting.RESET + "   ")) {
			if (currentTime - lastDungeonFinish > 30000) {
				Matcher matcher = TEAM_SCORE_REGEX.matcher(unformatted);
				if (matcher.find()) {
					lastDungeonFinish = currentTime;
					String score = matcher.group(1);
					switch (score.toUpperCase()) {
						case "S+":
							TEAM_SCORE = SPLUS;
							break;
						case "S":
							TEAM_SCORE = S;
							break;
						case "A":
							TEAM_SCORE = A;
							break;
						case "B":
							TEAM_SCORE = B;
							break;
						case "C":
							TEAM_SCORE = C;
							break;
						default:
							TEAM_SCORE = D;
							break;
					}

					SES.schedule(() -> NotEnoughUpdates.INSTANCE.sendChatMessage("/showextrastats"), 100L, TimeUnit.MILLISECONDS);
					seenDungeonWinOverlayThisRun = false;
				}
			}
		}
		if (currentTime - lastDungeonFinish > 100 && currentTime - lastDungeonFinish < 10000) {
			if (hideChat) {
				if (text.size() > 50) text.clear();

				if (unformatted.contains("\u25AC")) {
					e.setCanceled(true);
					hideChat = false;
					displayWin();
					seenDungeonWinOverlayThisRun = true;
				} else {
					if (unformatted.trim().length() > 0 && !seenDungeonWinOverlayThisRun) {
						if (unformatted.contains("The Catacombs") || unformatted.contains("Master Mode Catacombs") ||
							unformatted.contains("Team Score") || unformatted.contains("Defeated") || unformatted.contains(
							"Total Damage")
							|| unformatted.contains("Ally Healing") || unformatted.contains("Enemies Killed") || unformatted.contains(
							"Deaths") || unformatted.contains("Secrets Found")) {
							e.setCanceled(true);
							text.add(e.message.getFormattedText().substring(6).trim());
						} else if (unformatted.trim().length() > 6) {
							System.out.println(
								"These messages would of showed on neu dungeon overlay but didnt, They are either bugged or i missed them: \"" +
									e.message.getFormattedText().substring(6).trim() + "\"");
						}
					} else {
						e.setCanceled(true);
					}
				}
			} else {
				if (unformatted.contains("\u25AC") && !seenDungeonWinOverlayThisRun) {
					hideChat = true;
					text.clear();
					e.setCanceled(true);
				}
			}

		}
	}

	public static void render(float partialTicks) {
		if (NotEnoughUpdates.INSTANCE.config.dungeons.dungeonWinMillis < 100 ||
			!NotEnoughUpdates.INSTANCE.config.dungeons.enableDungeonWin)
			return;
		int maxTime = Math.min(30000, NotEnoughUpdates.INSTANCE.config.dungeons.dungeonWinMillis);
		if (System.currentTimeMillis() - startTime > maxTime) return;
		int deltaTime = (int) (System.currentTimeMillis() - startTime);

		float alpha = Math.max(0, Math.min(1, 1 - (deltaTime - maxTime + 150) / 150f));

		ScaledResolution sr = Utils.pushGuiScale(2);

		if (deltaTime > 600) {
			float bottom;
			if (deltaTime < 1000) {
				bottom = sr.getScaledHeight() / 6f + (float) Math.sin(deltaTime / 1000f * Math.PI) * sr.getScaledHeight() * 4 /
					18 + HEIGHT / 2;
			} else {
				bottom = sr.getScaledHeight() / 6f + HEIGHT / 2;
			}
			for (int i = 0; i < text.size(); i++) {
				String line = text.get(i);
				float textCenterY = sr.getScaledHeight() / 6f + HEIGHT / 2 + 7 + i * 10;
				if (textCenterY > bottom) {
					int textAlpha = (int) (alpha * (deltaTime > 1000 ? 255 : Math.min(255, (textCenterY - bottom) / 30f * 255)));
					GlStateManager.enableBlend();

					if (textAlpha > 150) {
						for (int xOff = -2; xOff <= 2; xOff++) {
							for (int yOff = -2; yOff <= 2; yOff++) {
								if (Math.abs(xOff) != Math.abs(yOff)) {
									Utils.drawStringCentered(
										Utils.cleanColourNotModifiers(line),
										sr.getScaledWidth() / 2 + xOff / 2f,
										textCenterY + yOff / 2f,
										false,
										((textAlpha / Math.max(Math.abs(xOff), Math.abs(yOff))) << 24)
									);
								}
							}
						}
					}

					Utils.drawStringCentered(line, sr.getScaledWidth() / 2, textCenterY, false, (textAlpha << 24) | 0x00FFFFFF);
				}
			}
		}

		for (Confetti c : confetti) {
			Minecraft.getMinecraft().getTextureManager().bindTexture(CONFETTI);
			GlStateManager.color(1, 1, 1, 1);
			if (c.life >= 15) {
				GlStateManager.color(1, 1, 1, Math.min(1, c.life / 4f));
				Utils.drawTexturedRect(
					c.xLast + (c.x - c.xLast) * partialTicks - 4,
					c.yLast + (c.y - c.yLast) * partialTicks - 4,
					8,
					8,
					(c.id % 4) / 4f,
					(c.id % 4 + 1) / 4f,
					(c.id / 4) / 4f,
					(c.id / 4 + 1) / 4f,
					GL11.GL_NEAREST
				);
			}
		}

		Minecraft.getMinecraft().getTextureManager().bindTexture(TEAM_SCORE);
		GlStateManager.color(1, 1, 1, alpha);

		GlStateManager.pushMatrix();
		if (deltaTime < 1600) {
			GlStateManager.translate(sr.getScaledWidth() / 2, 0, 0);
			if (deltaTime < 500) {
				GlStateManager.translate(
					0,
					sr.getScaledHeight() / 2f - Math.sin(deltaTime / 1000f * Math.PI) * sr.getScaledHeight() / 9,
					0
				);
			} else if (deltaTime < 1000) {
				GlStateManager.translate(
					0,
					sr.getScaledHeight() / 6f + Math.sin(deltaTime / 1000f * Math.PI) * sr.getScaledHeight() * 4 / 18,
					0
				);
			} else {
				GlStateManager.translate(0, sr.getScaledHeight() / 6f, 0);
			}
			if (deltaTime < 200) {
				float scale = deltaTime / 200f;
				GlStateManager.scale(scale, scale, 1);
			} else if (deltaTime < 1000) {
				float scale = 1 + (float) Math.sin((deltaTime - 200) / 800f * Math.PI) * 0.8f;
				GlStateManager.scale(scale, scale, 1);
			} else if (deltaTime < 1100) {
				float scale = 1 + (float) Math.sin((deltaTime - 1000) / 100f * Math.PI) * 0.15f;
				GlStateManager.scale(scale, scale, 1);
			}

			if (deltaTime < 600) {
				GlStateManager.rotate(180 + deltaTime / 600f * 180, 0, 1, 0);
				GlStateManager.rotate(180 - deltaTime / 600f * 180, 1, 0, 0);
				GlStateManager.rotate(-180 - deltaTime / 600f * 165, 0, 0, 1);
			} else if (deltaTime < 1000) {
				GlStateManager.rotate(15 - (deltaTime - 600) / 400f * 11, 0, 0, 1);
			} else {
				float logFac = 1 - (float) Math.log((deltaTime - 1000) / 600f * 1.7f + 1);
				logFac = logFac * logFac;

				GlStateManager.rotate(4f * logFac, 0, 0, 1);
				float x = (deltaTime - 1000) / 300f;
				GlStateManager.rotate((float) (40 * (1 - Math.log(x * 0.85f + 1)) * Math.sin(10 * x * x)), 0, 1, 0);
			}
		} else {
			GlStateManager.translate(sr.getScaledWidth() / 2, sr.getScaledHeight() / 6f, 0);
		}

		GlStateManager.disableCull();

		Utils.drawTexturedRect(-WIDTH / 2, -HEIGHT / 2, WIDTH, HEIGHT, GL11.GL_NEAREST);
		GlStateManager.translate(0, 0, -SCALE_FACTOR * 2);
		Utils.drawTexturedRect(-WIDTH / 2, -HEIGHT / 2, WIDTH, HEIGHT, GL11.GL_NEAREST);
		GlStateManager.translate(0, 0, SCALE_FACTOR * 2);

		if (deltaTime < 1600) {
			float epsilon = 0.01f;
			for (int xIndex = 0; xIndex < 32; xIndex++) {
				for (int yIndex = 0; yIndex < 16; yIndex++) {
					float uMin = xIndex / 32f;
					float uMax = (xIndex + 1) / 32f;
					float vMin = yIndex / 16f;
					float vMax = (yIndex + 1) / 16f;

					int x = -WIDTH / 2 + xIndex * SCALE_FACTOR;
					int y = -HEIGHT / 2 + yIndex * SCALE_FACTOR;

					GlStateManager.enableTexture2D();
					GlStateManager.enableBlend();
					GL14.glBlendFuncSeparate(
						GL11.GL_SRC_ALPHA,
						GL11.GL_ONE_MINUS_SRC_ALPHA,
						GL11.GL_ONE,
						GL11.GL_ONE_MINUS_SRC_ALPHA
					);

					GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
					GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);

					Tessellator tessellator = Tessellator.getInstance();
					WorldRenderer worldrenderer = tessellator.getWorldRenderer();
					//Left
					worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
					worldrenderer
						.pos(x + epsilon, y + SCALE_FACTOR, 0.0D + epsilon)
						.tex(uMin, vMax).endVertex();
					worldrenderer
						.pos(x + epsilon, y, 0.0D + epsilon)
						.tex(uMax, vMax).endVertex();
					worldrenderer
						.pos(x + epsilon, y, -SCALE_FACTOR * 2 - epsilon)
						.tex(uMax, vMin).endVertex();
					worldrenderer
						.pos(x + epsilon, y + SCALE_FACTOR, -SCALE_FACTOR * 2 - epsilon)
						.tex(uMin, vMin).endVertex();
					tessellator.draw();
					//Right
					worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
					worldrenderer
						.pos(x + SCALE_FACTOR - epsilon, y + SCALE_FACTOR, 0.0D + epsilon)
						.tex(uMin, vMax).endVertex();
					worldrenderer
						.pos(x + SCALE_FACTOR - epsilon, y, 0.0D + epsilon)
						.tex(uMax, vMax).endVertex();
					worldrenderer
						.pos(x + SCALE_FACTOR - epsilon, y, -SCALE_FACTOR * 2 - epsilon)
						.tex(uMax, vMin).endVertex();
					worldrenderer
						.pos(x + SCALE_FACTOR - epsilon, y + SCALE_FACTOR, -SCALE_FACTOR * 2 - epsilon)
						.tex(uMin, vMin).endVertex();
					tessellator.draw();
					//Top
					worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
					worldrenderer
						.pos(x + SCALE_FACTOR, y + epsilon, 0.0D + epsilon)
						.tex(uMin, vMax).endVertex();
					worldrenderer
						.pos(x, y + epsilon, 0.0D + epsilon)
						.tex(uMax, vMax).endVertex();
					worldrenderer
						.pos(x, y + epsilon, -SCALE_FACTOR * 2 - epsilon)
						.tex(uMax, vMin).endVertex();
					worldrenderer
						.pos(x + SCALE_FACTOR, y + epsilon, -SCALE_FACTOR * 2 - epsilon)
						.tex(uMin, vMin).endVertex();
					tessellator.draw();
					//Top
					worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
					worldrenderer
						.pos(x + SCALE_FACTOR, y + SCALE_FACTOR - epsilon, 0.0D + epsilon)
						.tex(uMin, vMax).endVertex();
					worldrenderer
						.pos(x, y + SCALE_FACTOR - epsilon, 0.0D + epsilon)
						.tex(uMax, vMax).endVertex();
					worldrenderer
						.pos(x, y + SCALE_FACTOR - epsilon, -SCALE_FACTOR * 2 - epsilon)
						.tex(uMax, vMin).endVertex();
					worldrenderer
						.pos(x + SCALE_FACTOR, y + SCALE_FACTOR - epsilon, -SCALE_FACTOR * 2 - epsilon)
						.tex(uMin, vMin).endVertex();
					tessellator.draw();

					GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
					GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);

					GlStateManager.disableBlend();
				}
			}
		}

		GlStateManager.popMatrix();

		for (Confetti c : confetti) {
			Minecraft.getMinecraft().getTextureManager().bindTexture(CONFETTI);
			GlStateManager.color(1, 1, 1, 1);
			if (c.life > 0 && c.life < 15) {
				GlStateManager.color(1, 1, 1, Math.min(1, c.life / 4f));
				Utils.drawTexturedRect(
					c.xLast + (c.x - c.xLast) * partialTicks - 4,
					c.yLast + (c.y - c.yLast) * partialTicks - 4,
					8,
					8,
					(c.id % 4) / 4f,
					(c.id % 4 + 1) / 4f,
					(c.id / 4) / 4f,
					(c.id / 4 + 1) / 4f,
					GL11.GL_NEAREST
				);
			}
		}

		Utils.pushGuiScale(-1);

		GlStateManager.enableBlend();
	}
}
