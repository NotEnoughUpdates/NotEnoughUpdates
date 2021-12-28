package io.github.moulberry.notenoughupdates.miscgui;

import com.google.gson.JsonObject;
import io.github.moulberry.notenoughupdates.NEUManager;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.io.IOException;
import java.util.List;

public class GuiItemRecipe extends GuiScreen {
    private static final ResourceLocation resourcePacksTexture = new ResourceLocation("textures/gui/resource_packs.png");
    private static final ResourceLocation craftingTableGuiTextures = new ResourceLocation("textures/gui/container/crafting_table.png");

    private final List<ItemStack[]> craftMatrices;
    private final List<JsonObject> results;
    private int currentIndex = 0;

    private final String title;
    private final NEUManager manager;

    public int guiLeft = 0;
    public int guiTop = 0;
    public int xSize = 176;
    public int ySize = 166;

    public GuiItemRecipe(String title, List<ItemStack[]> craftMatrices, List<JsonObject> results, NEUManager manager) {
        this.craftMatrices = craftMatrices;
        this.results = results;
        this.manager = manager;
        this.title = title;
    }

    private String getCraftText() {
        if(results.get(currentIndex).has("crafttext")) {
            return results.get(currentIndex).get("crafttext").getAsString();
        } else {
            return "";
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();

        if(currentIndex < 0) {
            currentIndex = 0;
        } else if(currentIndex >= craftMatrices.size()) {
            currentIndex = craftMatrices.size()-1;
        }

        FontRenderer fontRendererObj = Minecraft.getMinecraft().fontRendererObj;

        this.guiLeft = (width - this.xSize) / 2;
        this.guiTop = (height - this.ySize) / 2;

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        Minecraft.getMinecraft().getTextureManager().bindTexture(craftingTableGuiTextures);
        this.drawTexturedModalRect(guiLeft, guiTop, 0, 0, this.xSize, this.ySize);

        List<String> tooltipToRender = null;
        for(int index=0; index <= 45; index++) {
            Vector2f pos = getPositionForIndex(index);
            Utils.drawItemStack(getStackForIndex(index), (int)pos.x, (int)pos.y);

            if(mouseX > pos.x && mouseX < pos.x+16) {
                if(mouseY > pos.y && mouseY < pos.y+16) {
                    ItemStack stack = getStackForIndex(index);
                    if(stack != null) {
                        tooltipToRender = stack.getTooltip(Minecraft.getMinecraft().thePlayer, false);
                    }
                }
            }
        }

        if(craftMatrices.size() > 1) {
            int guiX = mouseX - guiLeft;
            int guiY = mouseY - guiTop;

            int buttonWidth = 7;
            int buttonHeight = 11;

            boolean leftSelected = false;
            boolean rightSelected = false;

            if(guiY > + 63 && guiY < + 63 + buttonHeight) {
                if(guiX > + 110 && guiX < 110 + buttonWidth) {
                    leftSelected = true;
                } else if(guiX > 147 && guiX < 147 + buttonWidth) {
                    rightSelected = true;
                }
            }

            Minecraft.getMinecraft().getTextureManager().bindTexture(resourcePacksTexture);
            //Left arrow
            Utils.drawTexturedRect(guiLeft+110, guiTop+63, 7, 11, 34/256f, 48/256f,
                    5/256f + (leftSelected ? 32/256f : 0), 27/256f + (leftSelected ? 32/256f : 0));
            //Right arrow
            Utils.drawTexturedRect(guiLeft+147, guiTop+63, 7, 11, 10/256f, 24/256f,
                    5/256f + (rightSelected ? 32/256f : 0), 27/256f + (rightSelected ? 32/256f : 0));
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

            String str = (currentIndex+1)+"/"+craftMatrices.size();
            Utils.drawStringCenteredScaledMaxWidth(str, fontRendererObj, guiLeft+132, guiTop+69,
                    false, 24, Color.BLACK.getRGB());
        }

        Utils.drawStringCenteredScaledMaxWidth(getCraftText(), fontRendererObj, guiLeft+132, guiTop+25,
                false, 75, 4210752);

        Utils.drawStringScaledMaxWidth(title, fontRendererObj, guiLeft+28, guiTop+6, title.contains("\u00a7"), xSize-38, 4210752);

        if(tooltipToRender != null) {
            Utils.drawHoveringText(tooltipToRender, mouseX, mouseY, width, height, -1, fontRendererObj);
        }
    }

    public ItemStack getStackForIndex(int index) {
        if(index == 0) {
            return manager.jsonToStack(results.get(currentIndex));
        } else if(index >= 1 && index <= 9) {
            return craftMatrices.get(currentIndex)[index-1];
        } else {
            return Minecraft.getMinecraft().thePlayer.inventory.getStackInSlot(index-10);
        }
    }

    public Vector2f getPositionForIndex(int index) {
        //0 = result
        //1-9 = craft matrix
        //10-18 = hotbar
        //19-45 = player inv

        if(index == 0) {
            return new Vector2f(guiLeft+124, guiTop+35);
        } else if(index >= 1 && index <= 9) {
            index -= 1;
            int x = index % 3;
            int y = index / 3;
            return new Vector2f(guiLeft+30 + x*18, guiTop+17 + y * 18);
        } else if(index >= 10 && index <= 18) {
            index -= 10;
            return new Vector2f(guiLeft+8 + index*18, guiTop+142);
        } else if(index >= 19 && index <= 45) {
            index -= 19;
            int x = index % 9;
            int y = index / 9;
            return new Vector2f(guiLeft+8 + x*18, guiTop+84 + y*18);
        }
        return null;
    }

    @Override
    public void handleKeyboardInput() throws IOException {
        super.handleKeyboardInput();

        if(!Keyboard.getEventKeyState()) return;

        ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
        int width = scaledResolution.getScaledWidth();
        int height = scaledResolution.getScaledHeight();
        int mouseX = Mouse.getX() * width / Minecraft.getMinecraft().displayWidth;
        int mouseY = height - Mouse.getY() * height / Minecraft.getMinecraft().displayHeight - 1;

        int keyPressed = Keyboard.getEventKey() == 0 ? Keyboard.getEventCharacter()+256 : Keyboard.getEventKey();

        for(int index=0; index <= 45; index++) {
            Vector2f pos = getPositionForIndex(index);
            if(mouseX > pos.x && mouseX < pos.x+16) {
                if(mouseY > pos.y && mouseY < pos.y+16) {
                    ItemStack stack = getStackForIndex(index);
                    if(stack != null) {
                        if(keyPressed == manager.keybindViewRecipe.getKeyCode()) {
                            manager.displayGuiItemRecipe(manager.getInternalNameForItem(stack), "");
                        } else if(keyPressed == manager.keybindViewUsages.getKeyCode()) {
                            manager.displayGuiItemUsages(manager.getInternalNameForItem(stack));
                        }
                    }
                    return;
                }
            }
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        int guiX = mouseX - guiLeft;
        int guiY = mouseY - guiTop;

        int buttonWidth = 7;
        int buttonHeight = 11;

        if(guiY > + 63 && guiY < + 63 + buttonHeight) {
            if(guiX > + 110 && guiX < 110 + buttonWidth) {
                currentIndex--;
                Utils.playPressSound();
                return;
            } else if(guiX > 147 && guiX < 147 + buttonWidth) {
                currentIndex++;
                Utils.playPressSound();
                return;
            }
        }

        for(int index=0; index <= 45; index++) {
            Vector2f pos = getPositionForIndex(index);
            if(mouseX > pos.x && mouseX < pos.x+16) {
                if(mouseY > pos.y && mouseY < pos.y+16) {
                    ItemStack stack = getStackForIndex(index);
                    if(stack != null) {
                        if(mouseButton == 0) {
                            manager.displayGuiItemRecipe(manager.getInternalNameForItem(stack), "");
                        } else if(mouseButton == 1) {
                            manager.displayGuiItemUsages(manager.getInternalNameForItem(stack));
                        }
                    }
                    return;
                }
            }
        }
    }
}
