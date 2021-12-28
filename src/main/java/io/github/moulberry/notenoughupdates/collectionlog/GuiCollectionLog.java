package io.github.moulberry.notenoughupdates.collectionlog;

import io.github.moulberry.notenoughupdates.core.BackgroundBlur;
import io.github.moulberry.notenoughupdates.core.GlScissorStack;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class GuiCollectionLog extends GuiScreen {
    private static final ResourceLocation COLLECTION_LOG_TEX = new ResourceLocation("notenoughupdates:collectionlog.png");

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
        int width = scaledResolution.getScaledWidth();
        int height = scaledResolution.getScaledHeight();

        int colwidth = 307;
        int colheight = 187;

        int left = width / 2 - colwidth / 2;
        int top = height / 2 - colheight / 2;

        BackgroundBlur.renderBlurredBackground(10, width, height, left, top, colwidth, colheight);
        super.drawDefaultBackground();

        Utils.drawStringCentered("\u00a7lCollection Log", fontRendererObj, width / 2, top - 27, true, 0xfff5aa00);

        String[] cats = {"Bosses", "Dragons", "Slayer", "Dungeons"};

        GlStateManager.enableDepth();

        GlStateManager.translate(0, 0, 2);
        for (int i = 0; i < 4; i++) {
            if (i == 0) {
                int offset = i == 0 ? 1 : 2;

                Minecraft.getMinecraft().getTextureManager().bindTexture(COLLECTION_LOG_TEX);
                GlStateManager.color(1, 1, 1, 1);
                Utils.drawTexturedRect(left + i * 71, top - 21, 71, 25,
                        (71 * offset) / 512f, (71 + 71 * offset) / 512f, 211 / 512f, (211 + 25) / 512f, GL11.GL_NEAREST);

                Utils.drawStringCentered(cats[i], fontRendererObj, left + i * 71 + 71 / 2, top - 8, true, 0xfff5aa00);
            }
        }

        GlStateManager.translate(0, 0, -1);
        Minecraft.getMinecraft().getTextureManager().bindTexture(COLLECTION_LOG_TEX);
        GlStateManager.color(1, 1, 1, 1);
        Utils.drawTexturedRect(left, top, colwidth, colheight,
                0, colwidth / 512f, 0, colheight / 512f, GL11.GL_NEAREST);

        GlScissorStack.push(0, top + 3, width, top + colheight - 6, scaledResolution);
        int catIndex = 0;
        for (int h = top + 3; h < top + colheight - 6; h += 24) {
            catIndex += 2;

            Minecraft.getMinecraft().getTextureManager().bindTexture(COLLECTION_LOG_TEX);
            GlStateManager.color(1, 1, 1, 1);
            Utils.drawTexturedRect(left, h, 100, 24,
                    0, 100 / 512f, 187 / 512f, 211 / 512f, GL11.GL_NEAREST);

            fontRendererObj.drawString("Thing " + catIndex, left + 5, h + 2, 0xfff5aa00, true);
            fontRendererObj.drawString("Thing " + (catIndex + 1), left + 5, h + 14, 0xfff5aa00, true);
        }
        GlScissorStack.pop(scaledResolution);

        fontRendererObj.drawString("\u00a7lSuperior Dragon", left + 119, top + 8, 0xfff5aa00, true);
        fontRendererObj.drawString("Obtained: " + EnumChatFormatting.YELLOW + "3/5", left + 122, top + 23, 0xfff5aa00, true);

        String killCountText = "Kills: " + EnumChatFormatting.WHITE + "3";
        //int killCountLen = fontRendererObj.getStringWidth(killCountText);
        fontRendererObj.drawString(killCountText, left + 122, top + 68, 0xfff5aa00, true);

        Minecraft.getMinecraft().getTextureManager().bindTexture(COLLECTION_LOG_TEX);
        GlStateManager.color(1, 1, 1, 1);
        Utils.drawTexturedRect(left + colwidth - 196, top, 196, colheight,
                (512 - 196) / 512f, 1, 0 / 512f, colheight / 512f, GL11.GL_NEAREST);

        GlStateManager.translate(0, 0, -1);

        for (int i = 0; i < 4; i++) {
            if (i != 0) {
                Minecraft.getMinecraft().getTextureManager().bindTexture(COLLECTION_LOG_TEX);
                GlStateManager.color(1, 1, 1, 1);
                Utils.drawTexturedRect(left + i * 71, top - 21, 71, 25,
                        0, 71 / 512f, 211 / 512f, (211 + 25) / 512f, GL11.GL_NEAREST);

                Utils.drawStringCentered(cats[i], fontRendererObj, left + i * 71 + 71 / 2, top - 8, true, 0xfff5aa00);
            }
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
