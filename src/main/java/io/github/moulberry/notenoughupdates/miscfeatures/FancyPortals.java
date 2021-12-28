package io.github.moulberry.notenoughupdates.miscfeatures;

import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.play.server.S07PacketRespawn;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.Project;

import java.nio.ByteBuffer;
import java.util.List;

public class FancyPortals {
    private static final ResourceLocation[] RENDERS = new ResourceLocation[6];

    static {
        for (int i = 0; i < 6; i++) {
            RENDERS[i] = new ResourceLocation("notenoughupdates:portal_panoramas/nether/pansc-" + (i + 1) + ".png");
        }
    }

    public static int perspectiveId = -1;

    public static boolean overridePerspective() {
        if (perspectiveId >= 0 && !Keyboard.isKeyDown(Keyboard.KEY_K)) {
            if (perspectiveId == 0) {
                GlStateManager.matrixMode(5889);
                GlStateManager.loadIdentity();
                GlStateManager.ortho(0.0D, 7, 7, 0.0D, -100D, 100D);
                GlStateManager.scale(1, 1, -1);
                GlStateManager.matrixMode(5888);
                GlStateManager.loadIdentity();
                GlStateManager.translate(3.5F, 3.5F, -1.0F);
                GlStateManager.rotate(-90, 1, 0, 0);
            } else if (perspectiveId <= 4) {
                GlStateManager.matrixMode(5889);
                GlStateManager.loadIdentity();
                Project.gluPerspective(90, 1, 0.05F, 160 * MathHelper.SQRT_2);
                GlStateManager.matrixMode(5888);
                GlStateManager.loadIdentity();
                GlStateManager.rotate(perspectiveId * 90, 0, 1, 0);
                GlStateManager.translate(0, -3.5f, 0);
            } else {
                GlStateManager.matrixMode(5889);
                GlStateManager.loadIdentity();
                Project.gluPerspective(90, 1, 0.05F, 160 * MathHelper.SQRT_2);
                GlStateManager.matrixMode(5888);
                GlStateManager.loadIdentity();
                GlStateManager.rotate(-90, 1, 0, 0);
                GlStateManager.translate(0, -3.5f, 0);
            }

            return true;
        }
        return false;
    }

    private static WorldRenderer surfaceWorldRenderer = null;

    private static WorldRenderer getSurfaceWorldRenderer() {
        if (surfaceWorldRenderer != null && !Keyboard.isKeyDown(Keyboard.KEY_O)) {
            return surfaceWorldRenderer;
        }

        surfaceWorldRenderer = createSurfaceWorldRenderer();

        return surfaceWorldRenderer;
    }

    private static void drawPoint(WorldRenderer worldRenderer, int x, int y) {
        float xDist = 1 - Math.abs(x - 50) / 50f;
        float yDist = 1 - Math.abs(y - 50) / 50f;
        float distToEdge = Math.min(xDist, yDist);

        float z = 0.4142f;
        if (distToEdge < 1 / 3.5f) {
            if (y > 50 && yDist < xDist) {
                float circleH = 1.414f - distToEdge * 3.5f * 1.414f;
                z = (float) Math.sqrt(2f - circleH * circleH);
                z *= 0.4142f / 1.4142f;
            } else {
                float circleH = 1 - distToEdge * 3.5f;
                z = (float) Math.sqrt(2f - circleH * circleH) - 1;
            }
        }

        worldRenderer.pos(x * 7 / 100f, y * 7 / 100f, z).tex(x / 100f, y / 100f).endVertex();
    }

    private static WorldRenderer createSurfaceWorldRenderer() {
        WorldRenderer worldRenderer = new WorldRenderer(20 * 100 * 100);
        worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

        for (int x = 0; x < 100; x++) {
            for (int y = 0; y < 100; y++) {
                drawPoint(worldRenderer, x, y);
                drawPoint(worldRenderer, x, y + 1);
                drawPoint(worldRenderer, x + 1, y + 1);
                drawPoint(worldRenderer, x + 1, y);
            }
        }

        return worldRenderer;
    }

    private static long overridingRenderMillis = -1;

    public static void onRespawnPacket(S07PacketRespawn packet) {
        if (true) return;
        if (packet.getDimensionID() != Minecraft.getMinecraft().thePlayer.dimension) {
            overridingRenderMillis = System.currentTimeMillis();
        }
    }

    public static boolean shouldRenderLoadingScreen() {
        return false;
    }

    public static boolean shouldRenderWorldOverlay() {
        if (overridingRenderMillis > 0) {
            if (Minecraft.getMinecraft().theWorld != null && Minecraft.getMinecraft().thePlayer != null) {
                RenderGlobal renderGlobal = Minecraft.getMinecraft().renderGlobal;
                int loaded = 0;
                for (RenderGlobal.ContainerLocalRenderInformation info : renderGlobal.renderInfos) {
                    CompiledChunk compiledchunk = info.renderChunk.compiledChunk;

                    if (compiledchunk != CompiledChunk.DUMMY && !compiledchunk.isEmpty()) {
                        if (++loaded >= 5) {
                            overridingRenderMillis = -1;
                            return false;
                        }
                    }
                }
            }
            if (System.currentTimeMillis() - overridingRenderMillis > 1000) {
                overridingRenderMillis = -1;
                return false;
            }
            return true;
        }
        return false;
    }

    public static void onUpdateCameraAndRender(float partialTicks, long nanoTime) {
        if (overridingRenderMillis > 0) {
            if (Minecraft.getMinecraft().theWorld != null && Minecraft.getMinecraft().thePlayer != null) {
                Minecraft.getMinecraft().thePlayer.timeInPortal = 0.3f;
                Minecraft.getMinecraft().thePlayer.prevTimeInPortal = 0.3f;
            }

            GlStateManager.rotate(90, 0, 1, 0);
            renderWorld();

            Minecraft.getMinecraft().ingameGUI.renderGameOverlay(partialTicks);
        }
    }

    @SubscribeEvent
    public void onRenderEntityYeeter(RenderLivingEvent.Pre<EntityLivingBase> event) {
        /*if(!Keyboard.isKeyDown(Keyboard.KEY_G)) return;
        event.setCanceled(true);
        if(event.entity instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) event.entity;
            if(player.getUniqueID().version() == 4) {
                event.setCanceled(true);
            }
        }*/
    }

    private static void renderWorld() {
        for (int i = 5; i >= 0; i--) {
            GlStateManager.pushMatrix();

            GlStateManager.disableDepth();
            GlStateManager.disableLighting();

            GlStateManager.rotate(180, 0, 0, 1);
            GlStateManager.rotate(-90, 0, 1, 0);

            if (i != 0) GlStateManager.translate(0, -3.49, 0);

            switch (i) {
                case 1:
                    GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
                    break;
                case 2:
                    GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
                    break;
                case 3:
                    GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F);
                    break;
                case 5:
                    GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
                    break;
                case 0:
                    GlStateManager.rotate(-90.0F, 1.0F, 0.0F, 0.0F);
                    break;
            }

            Minecraft.getMinecraft().getTextureManager().bindTexture(RENDERS[i]);
            GlStateManager.color(1, 1, 1, 1);
            if (i != 0) GlStateManager.translate(0, 0, 3.49);

            if (i != 0) {
                GlStateManager.translate(-3.5f, -3.5f, 0);
                WorldRenderer worldRenderer = getSurfaceWorldRenderer();
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
            } else {
                Utils.drawTexturedRect(-3.5f, -3.5f, 7, 7, i == 0 ? GL11.GL_NEAREST : GL11.GL_LINEAR);
            }

            GlStateManager.enableDepth();

            GlStateManager.popMatrix();
        }
    }

    @SubscribeEvent
    public void onRenderLast(RenderWorldLastEvent event) {
        if (true) return;
        if (!Minecraft.getMinecraft().getFramebuffer().isStencilEnabled())
            Minecraft.getMinecraft().getFramebuffer().enableStencil();

        GL11.glEnable(GL11.GL_STENCIL_TEST);
        GL11.glStencilFunc(GL11.GL_ALWAYS, 1, 0xFF);
        GL11.glStencilOp(GL11.GL_ZERO, GL11.GL_ZERO, GL11.GL_REPLACE);
        GL11.glStencilMask(0xFF);
        GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);
        GlStateManager.enableDepth();
        GlStateManager.enableCull();
        GlStateManager.cullFace(GL11.GL_BACK);

        GL11.glColorMask(false, false, false, false);

        Entity viewer = Minecraft.getMinecraft().getRenderViewEntity();
        double viewerX = viewer.lastTickPosX + (viewer.posX - viewer.lastTickPosX) * event.partialTicks;
        double viewerY = viewer.lastTickPosY + (viewer.posY - viewer.lastTickPosY) * event.partialTicks;
        double viewerZ = viewer.lastTickPosZ + (viewer.posZ - viewer.lastTickPosZ) * event.partialTicks;
        GlStateManager.pushMatrix();

        GlStateManager.translate(-viewerX + 12 + 5 / 16f, -viewerY + 100, -viewerZ + 39);
        GlStateManager.rotate(90, 0, 1, 0);
        Gui.drawRect(0, 5, 3, 0, 0xffffffff);
        GlStateManager.rotate(180, 0, 1, 0);
        GlStateManager.translate(-3, 0, -6 / 16f);
        Gui.drawRect(0, 5, 3, 0, 0xffffffff);

        GlStateManager.popMatrix();

        GL11.glColorMask(true, true, true, true);

        // Only pass stencil test if equal to 1
        GL11.glStencilMask(0x00);
        GL11.glStencilFunc(GL11.GL_EQUAL, 1, 0xFF);

        GlStateManager.translate(-viewerX + 12, -viewerY + 100, -viewerZ + 37.5f);

        renderWorld();

        GL11.glDisable(GL11.GL_STENCIL_TEST);
        GlStateManager.enableCull();
    }
}
