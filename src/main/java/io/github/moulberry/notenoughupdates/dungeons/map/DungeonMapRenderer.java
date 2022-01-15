package io.github.moulberry.notenoughupdates.dungeons.map;

import com.google.gson.JsonObject;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.core.BackgroundBlur;
import io.github.moulberry.notenoughupdates.options.customtypes.NEUDebugFlag;
import io.github.moulberry.notenoughupdates.options.seperateSections.DungeonMapConfig;
import io.github.moulberry.notenoughupdates.util.NEUResourceManager;
import io.github.moulberry.notenoughupdates.util.SpecialColour;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.client.shader.Shader;
import net.minecraft.util.Matrix4f;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static io.github.moulberry.notenoughupdates.util.Utils.ensureFramebufferSize;
import static org.lwjgl.opengl.GL11.GL_MODELVIEW;
import static org.lwjgl.opengl.GL11.GL_PROJECTION;

public class DungeonMapRenderer {

	DungeonMap map;

	public Framebuffer mapFramebuffer1 = null;
	public Framebuffer mapFramebuffer2 = null;
	public Matrix4f projectionMatrix = null;
	public Shader mapShader = null;
	DungeonMapConfig config;
	private final HashMap<Integer, Float> borderRadiusCache = new HashMap<>();

	public DungeonMapRenderer(DungeonMap dungeonMap, DungeonMapConfig config) {
		this.map = dungeonMap;
		this.config = config;
	}

	private static void upload(
		Shader shader,
		Matrix4f projectionMatrix,
		int width,
		int height,
		int scale,
		float radiusSq
	) {
		if (shader == null) return;
		shader.getShaderManager().getShaderUniformOrDefault("ProjMat").set(projectionMatrix);
		shader.getShaderManager().getShaderUniformOrDefault("InSize").set(width * scale, height * scale);
		shader.getShaderManager().getShaderUniformOrDefault("OutSize").set(width, height);
		shader.getShaderManager().getShaderUniformOrDefault("ScreenSize").set((float) width, (float) height);
		shader.getShaderManager().getShaderUniformOrDefault("radiusSq").set(radiusSq);
	}

	public void render(int centerX, int centerY, DungeonMapStaticParser data, DungeonMapPlayers players) {
		boolean useFb = config.dmCompat <= 1 && OpenGlHelper.isFramebufferEnabled();
		boolean useShd = config.dmCompat <= 0 && OpenGlHelper.areShadersSupported();

		ScaledResolution scaledResolution = Utils.pushGuiScale(2);

		int borderSizeOption = Math.round(config.dmBorderSize);

		int backgroundSize;
		if (config.dmBorderStyle <= 1) {
			backgroundSize = 80 + Math.round(40 * config.dmBorderSize);
		} else {
			backgroundSize = borderSizeOption == 0 ? 90 : borderSizeOption == 1 ? 120 : borderSizeOption == 2 ? 160 : 240;
		}
		int backgroundCenter = backgroundSize / 2;
		int scaleFactor = 8;

		float mapSize = backgroundSize * config.dmRoomSize;
		float pixelToMapScale = mapSize / 128F;
		float renderRoomSize = data.roomSize * pixelToMapScale;
		float renderConnSize = data.connectorSize * pixelToMapScale;

		projectionMatrix = Utils.createProjectionMatrix(backgroundSize * scaleFactor, backgroundSize * scaleFactor);
		mapFramebuffer1 = ensureFramebufferSize(
			mapFramebuffer1,
			backgroundSize * scaleFactor,
			backgroundSize * scaleFactor
		);
		mapFramebuffer2 = ensureFramebufferSize(
			mapFramebuffer2,
			backgroundSize * scaleFactor,
			backgroundSize * scaleFactor
		);
		mapFramebuffer1.framebufferColor[1] = 0;
		mapFramebuffer1.framebufferColor[2] = 0;

		try {
			if (mapShader == null) {
				mapShader = new Shader(new NEUResourceManager(Minecraft.getMinecraft().getResourceManager()),
					"dungeonmap", mapFramebuffer1, mapFramebuffer2
				);
			}
		} catch (Exception e) {
			e.printStackTrace();
			Utils.pushGuiScale(-1);
			return;
		}

		int backgroundColour = SpecialColour.specialToChromaRGB(config.dmBackgroundColour);

		mapFramebuffer1.framebufferColor[0] = ((backgroundColour >> 16) & 0xFF) / 255f;
		mapFramebuffer1.framebufferColor[1] = ((backgroundColour >> 8) & 0xFF) / 255f;
		mapFramebuffer1.framebufferColor[2] = (backgroundColour & 0xFF) / 255f;
		mapFramebuffer2.framebufferColor[0] = ((backgroundColour >> 16) & 0xFF) / 255f;
		mapFramebuffer2.framebufferColor[1] = ((backgroundColour >> 8) & 0xFF) / 255f;
		mapFramebuffer2.framebufferColor[2] = (backgroundColour & 0xFF) / 255f;

		try {
			if (useFb) {
				mapFramebuffer1.framebufferClear();
				mapFramebuffer2.framebufferClear();
			}

			GlStateManager.pushMatrix();
			{
				if (useFb) {
					GlStateManager.matrixMode(GL_PROJECTION);
					GlStateManager.loadIdentity();
					GlStateManager.ortho(
						0.0D,
						backgroundSize * scaleFactor,
						backgroundSize * scaleFactor,
						0.0D,
						1000.0D,
						3000.0D
					);
					GlStateManager.matrixMode(GL_MODELVIEW);
					GlStateManager.loadIdentity();
					GlStateManager.translate(0.0F, 0.0F, -2000.0F);

					GlStateManager.scale(scaleFactor, scaleFactor, 1);
					mapFramebuffer1.bindFramebuffer(true);

					GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
					GlStateManager.disableBlend();
				} else {
					GL11.glEnable(GL11.GL_SCISSOR_TEST);
					GL11.glScissor(
						(centerX - backgroundSize / 2) * 2,
						Minecraft.getMinecraft().displayHeight - (centerY + backgroundSize / 2) * 2,
						backgroundSize * 2,
						backgroundSize * 2
					);

					GlStateManager.translate(centerX - backgroundSize / 2F, centerY - backgroundSize / 2F, 100);
				}

				if (config.dmBackgroundBlur > 0.1 && config.dmBackgroundBlur < 100 && config.dmEnable) {
					GlStateManager.translate(-centerX + backgroundSize / 2F, -centerY + backgroundSize / 2F, 0);
					BackgroundBlur.renderBlurredBackground(config.dmBackgroundBlur,
						scaledResolution.getScaledWidth(), scaledResolution.getScaledHeight(),
						centerX - backgroundSize / 2, centerY - backgroundSize / 2, backgroundSize, backgroundSize
					);
					BackgroundBlur.markDirty();
					GlStateManager.translate(centerX - backgroundSize / 2F, centerY - backgroundSize / 2F, 0);
				}

				GlStateManager.translate(backgroundCenter, backgroundCenter, 10);

				if (!useFb || config.dmBackgroundBlur > 0.1 && config.dmBackgroundBlur < 100) {
					GlStateManager.enableBlend();
					GL14.glBlendFuncSeparate(
						GL11.GL_SRC_ALPHA,
						GL11.GL_ONE_MINUS_SRC_ALPHA,
						GL11.GL_ONE,
						GL11.GL_ONE_MINUS_SRC_ALPHA
					);
				}
				Utils.drawRectNoBlend(
					-backgroundCenter,
					-backgroundCenter,
					backgroundCenter,
					backgroundCenter,
					backgroundColour
				);

				float rotation = players.getMainPlayerRotation() + 180;

				if (!config.dmRotatePlayer)
					rotation = 0F;
				GlStateManager.rotate(-rotation, 0, 0, 1);

				if (config.dmCenterPlayer) {
					float x = players.getMainPlayerX() * pixelToMapScale;
					float y = players.getMainPlayerZ() * pixelToMapScale;

					GlStateManager.translate(-x, -y, 0);
				} else {
					GlStateManager.translate(-mapSize / 2F, -mapSize / 2F, 0);
				}

				for (Map.Entry<RoomOffset, Room> entry : data.roomMap.entrySet()) {
					Room room = entry.getValue();

					GlStateManager.pushMatrix();
					GlStateManager.translate(room.posX * pixelToMapScale, room.posY * pixelToMapScale, 0);

					room.render(renderRoomSize, renderConnSize);

					GlStateManager.popMatrix();
				}

				GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

				for (Map.Entry<RoomOffset, Room> entry : data.roomMap.entrySet()) {
					Room room = entry.getValue();

					GlStateManager.pushMatrix();

					GlStateManager.translate(room.posX * pixelToMapScale, room.posY * pixelToMapScale, 0);
					room.renderCheckmark(renderRoomSize, renderConnSize, rotation);

					GlStateManager.popMatrix();
				}

				Tessellator tessellator = Tessellator.getInstance();
				WorldRenderer worldrenderer = tessellator.getWorldRenderer();
				int k = 0;
				for (Map.Entry<String, DungeonMapPlayers.PlayerMarker> entry : players.getRunnerPositions().entrySet()) {
					renderPlayer(
						players,
						pixelToMapScale,
						renderRoomSize,
						tessellator,
						worldrenderer,
						k--,
						entry.getKey(),
						entry.getValue()
					);
				}
				for (DungeonMapPlayers.PlayerMarker orphanedMarker : players.getOrphanedMarkers().values()) {
					renderPlayer(
						players,
						pixelToMapScale,
						renderRoomSize,
						tessellator,
						worldrenderer,
						k--,
						null,
						orphanedMarker
					);
				}
				if (NEUDebugFlag.MAP.isSet())
					players.getAllMapPositions().forEach((label, playerMarker) -> {
						float x = playerMarker.x * pixelToMapScale;
						float y = playerMarker.z * pixelToMapScale;
						Utils.drawStringF(label, Minecraft.getMinecraft().fontRendererObj, x, y, true, 0xffd3d3d3);
					});

				if (useFb) {
					GlStateManager.enableBlend();
					GL14.glBlendFuncSeparate(
						GL11.GL_SRC_ALPHA,
						GL11.GL_ONE_MINUS_SRC_ALPHA,
						GL11.GL_ONE,
						GL11.GL_ONE_MINUS_SRC_ALPHA
					);
				} else {
					GL11.glDisable(GL11.GL_SCISSOR_TEST);
				}
			}
			GlStateManager.popMatrix();

			if (useFb) {
				Framebuffer renderFromBuffer = mapFramebuffer1;
				if (useShd) {
					GlStateManager.pushMatrix();
					{
						try {
							upload(mapShader, projectionMatrix, backgroundSize, backgroundSize, scaleFactor, getBorderRadius());
							mapShader.setProjectionMatrix(projectionMatrix);
							mapShader.loadShader(0);
							renderFromBuffer = mapFramebuffer2;
						} catch (Exception ignored) {
						}
					}
					GlStateManager.popMatrix();
				}

				Minecraft.getMinecraft().getFramebuffer().bindFramebuffer(true);

				Utils.pushGuiScale(2);

				GlStateManager.translate(centerX, centerY, 100);

				renderFromBuffer.bindFramebufferTexture();
				Utils.drawTexturedRect(-backgroundSize / 2F, -backgroundSize / 2F, backgroundSize, backgroundSize,
					0, 1, 1, 0, GL11.GL_NEAREST
				);
				GlStateManager.bindTexture(0);

				GlStateManager.translate(-centerX, -centerY, -100);

				Utils.pushGuiScale(-1);
			}

			GlStateManager.translate(centerX, centerY, 100);

			if (config.dmChromaBorder) {
				int colour = SpecialColour.specialToChromaRGB(config.dmBorderColour);

				Gui.drawRect(-backgroundCenter - 2, -backgroundCenter - 2, -backgroundCenter, -backgroundCenter,
					colour
				); //topleft
				Gui.drawRect(-backgroundCenter - 2, backgroundCenter + 2, -backgroundCenter, backgroundCenter,
					SpecialColour.rotateHue(colour, -180)
				); //bottomleft
				Gui.drawRect(backgroundCenter, -backgroundCenter - 2, backgroundCenter + 2, backgroundCenter,
					SpecialColour.rotateHue(colour, -180)
				); //topright
				Gui.drawRect(backgroundCenter, backgroundCenter, backgroundCenter + 2, backgroundCenter + 2,
					colour
				); //bottomright

				for (int i = 0; i < 20; i++) {
					int start1 = SpecialColour.rotateHue(colour, -9 * i);
					int start2 = SpecialColour.rotateHue(colour, -9 * i - 9);
					int end1 = SpecialColour.rotateHue(colour, -180 - 9 * i);
					int end2 = SpecialColour.rotateHue(colour, -180 - 9 * i - 9);

					Utils.drawGradientRect(
						-backgroundCenter - 2,
						-backgroundCenter + (int) (backgroundSize * (i / 20f)),
						-backgroundCenter,
						-backgroundCenter + (int) (backgroundSize * ((i + 1) / 20f)),
						start1,
						start2
					); //left
					Utils.drawGradientRect(
						backgroundCenter,
						-backgroundCenter + (int) (backgroundSize * (i / 20f)),
						backgroundCenter + 2,
						-backgroundCenter + (int) (backgroundSize * ((i + 1) / 20f)),
						end1,
						end2
					); //right
					Utils.drawGradientRectHorz(-backgroundCenter + (int) (backgroundSize * (i / 20f)), -backgroundCenter - 2,
						-backgroundCenter + (int) (backgroundSize * ((i + 1) / 20f)), -backgroundCenter, start1, start2
					); //top
					Utils.drawGradientRectHorz(-backgroundCenter + (int) (backgroundSize * (i / 20f)),
						backgroundCenter, -backgroundCenter + (int) (backgroundSize * ((i + 1) / 20f)), backgroundCenter + 2,
						end1, end2
					); //bottom
				}

			} else {
				Gui.drawRect(-backgroundCenter - 2, -backgroundCenter, -backgroundCenter, backgroundCenter,
					SpecialColour.specialToChromaRGB(config.dmBorderColour)
				); //left
				Gui.drawRect(backgroundCenter, -backgroundCenter, backgroundCenter + 2, backgroundCenter,
					SpecialColour.specialToChromaRGB(config.dmBorderColour)
				); //right
				Gui.drawRect(-backgroundCenter - 2, -backgroundCenter - 2, backgroundCenter + 2, -backgroundCenter,
					SpecialColour.specialToChromaRGB(config.dmBorderColour)
				); //top
				Gui.drawRect(-backgroundCenter - 2, backgroundCenter, backgroundCenter + 2, backgroundCenter + 2,
					SpecialColour.specialToChromaRGB(config.dmBorderColour)
				); //bottom
			}

			String sizeId = borderSizeOption == 0 ? "small" : borderSizeOption == 2 ? "large" : "medium";

			ResourceLocation rl = new ResourceLocation("notenoughupdates:dungeon_map/borders/" + sizeId + "/" +
				config.dmBorderStyle + ".png");
			if (Minecraft.getMinecraft().getTextureManager().getTexture(rl) != null) {
				Minecraft.getMinecraft().getTextureManager().bindTexture(rl);
				GlStateManager.color(1, 1, 1, 1);

				int size = borderSizeOption == 0 ? 165 : borderSizeOption == 1 ? 220 : borderSizeOption == 2 ? 300 : 440;
				Utils.drawTexturedRect(-size / 2, -size / 2, size, size, GL11.GL_NEAREST);
			}

			GlStateManager.translate(-centerX, -centerY, -100);
		} catch (Exception e) {
			e.printStackTrace();
			Minecraft.getMinecraft().getFramebuffer().bindFramebuffer(true);
			Minecraft.getMinecraft().entityRenderer.setupOverlayRendering();
		}

		Utils.pushGuiScale(-1);

		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
		GlStateManager.enableDepth();
		GlStateManager.disableLighting();
	}

	private void renderPlayer(
		DungeonMapPlayers players,
		float pixelToMapScale,
		float renderRoomSize,
		Tessellator tessellator,
		WorldRenderer worldrenderer,
		int k,
		String name,
		DungeonMapPlayers.PlayerMarker player
	) {

		float x = player.x * pixelToMapScale;
		float y = player.z * pixelToMapScale;
		float angle = player.angle;

		boolean isMainPlayer = name != null && name.equals(Minecraft.getMinecraft().thePlayer.getName());
		float minU = isMainPlayer ? 1 / 4F : 3 / 4f;
		float minV = 0;

		float maxU = minU + 1 / 4f;
		float maxV = minV + 1 / 4f;

		boolean blackBorder = false;
		boolean headLayer = false;
		int pixelWidth = 8;
		int pixelHeight = 8;
		if (renderRoomSize >= 24) {
			pixelWidth = pixelHeight = 12;
		}
		GlStateManager.color(1, 1, 1, 1);
		if (config.dmPlayerHeads > 0 && (!isMainPlayer || NotEnoughUpdates.INSTANCE.config.dungeons.showOwnHeadAsMarker) &&
			name != null) {
			Minecraft.getMinecraft().getTextureManager().bindTexture(players.getSkin(name));

			minU = 8 / 64f;
			minV = 8 / 64f;
			maxU = 16 / 64f;
			maxV = 16 / 64f;

			headLayer = true;
			if (config.dmPlayerHeads >= 2) {
				blackBorder = true;
			}
		} else {
			Minecraft.getMinecraft().getTextureManager().bindTexture(DungeonResources.MAP_ICONS);
		}

		GlStateManager.pushMatrix();

		GlStateManager.disableDepth();
		GlStateManager.enableBlend();
		GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);

		GlStateManager.translate(x, y, -0.02F);
		GlStateManager.scale(config.dmIconScale, config.dmIconScale, 1);
		GlStateManager.rotate(angle, 0.0F, 0.0F, 1.0F);

		if (blackBorder) {
			Gui.drawRect(-pixelWidth / 2 - 1, -pixelHeight / 2 - 1, pixelWidth / 2 + 1, pixelHeight / 2 + 1, 0xff111111);
			GlStateManager.color(1, 1, 1, 1);
		}

		worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
		worldrenderer.pos(-pixelWidth / 2f, pixelHeight / 2f, 30 + ((float) k * -0.005F)).tex(minU, minV).endVertex();
		worldrenderer.pos(pixelWidth / 2f, pixelHeight / 2f, 30 + ((float) k * -0.005F)).tex(maxU, minV).endVertex();
		worldrenderer.pos(pixelWidth / 2f, -pixelHeight / 2f, 30 + ((float) k * -0.005F)).tex(maxU, maxV).endVertex();
		worldrenderer.pos(-pixelWidth / 2f, -pixelHeight / 2f, 30 + ((float) k * -0.005F)).tex(minU, maxV).endVertex();
		tessellator.draw();

		if (headLayer) {
			worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
			worldrenderer
				.pos(-pixelWidth / 2f, pixelHeight / 2f, 30 + ((float) k * -0.005F) + 0.001f)
				.tex(minU + 0.5f, minV)
				.endVertex();
			worldrenderer
				.pos(pixelWidth / 2f, pixelHeight / 2f, 30 + ((float) k * -0.005F) + 0.001f)
				.tex(maxU + 0.5f, minV)
				.endVertex();
			worldrenderer
				.pos(pixelWidth / 2f, -pixelHeight / 2f, 30 + ((float) k * -0.005F) + 0.001f)
				.tex(maxU + 0.5f, maxV)
				.endVertex();
			worldrenderer
				.pos(-pixelWidth / 2f, -pixelHeight / 2f, 30 + ((float) k * -0.005F) + 0.001f)
				.tex(minU + 0.5f, maxV)
				.endVertex();
			tessellator.draw();
		}
		GlStateManager.popMatrix();
	}

	public float getBorderRadius() {
		int borderSizeOption = Math.round(config.dmBorderSize);
		String sizeId = borderSizeOption == 0 ? "small" : borderSizeOption == 2 ? "large" : "medium";

		int style = config.dmBorderStyle;
		if (borderRadiusCache.containsKey(style)) {
			return borderRadiusCache.get(style);
		}

		try (
			BufferedReader reader = new BufferedReader(new InputStreamReader(Minecraft
				.getMinecraft()
				.getResourceManager()
				.getResource(
					new ResourceLocation("notenoughupdates:dungeon_map/borders/" + sizeId + "/" + style + ".json"))
				.getInputStream(), StandardCharsets.UTF_8))
		) {
			JsonObject json = NotEnoughUpdates.INSTANCE.manager.gson.fromJson(reader, JsonObject.class);
			float radiusSq = json.get("radiusSq").getAsFloat();

			borderRadiusCache.put(style, radiusSq);
			return radiusSq;
		} catch (Exception ignored) {
		}

		borderRadiusCache.put(style, 1f);
		return 1f;
	}
}
