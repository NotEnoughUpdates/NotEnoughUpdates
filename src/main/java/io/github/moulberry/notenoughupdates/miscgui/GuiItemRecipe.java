package io.github.moulberry.notenoughupdates.miscgui;

import com.google.common.collect.ImmutableList;
import io.github.moulberry.notenoughupdates.NEUManager;
import io.github.moulberry.notenoughupdates.recipes.NeuRecipe;
import io.github.moulberry.notenoughupdates.recipes.RecipeSlot;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GuiItemRecipe extends GuiScreen {
    public static final ResourceLocation resourcePacksTexture = new ResourceLocation("textures/gui/resource_packs.png");

    public static final int SLOT_SIZE = 16;
    public static final int SLOT_SPACING = SLOT_SIZE + 2;
    public static final int BUTTON_WIDTH = 7;
    public static final int BUTTON_HEIGHT = 11;
    public static final int BUTTON_POSITION_Y = 63;
    public static final int BUTTON_POSITION_LEFT_X = 110;
    public static final int BUTTON_POSITION_RIGHT_X = 147;
    public static final int PAGE_STRING_X = 132;
    public static final int PAGE_STRING_Y = 69;
    public static final int TITLE_X = 28;
    public static final int TITLE_Y = 6;
    public static final int HOTBAR_SLOT_X = 8;
    public static final int HOTBAR_SLOT_Y = 142;
    public static final int PLAYER_INVENTORY_X = 8;
    public static final int PLAYER_INVENTORY_Y = 84;

    private int currentIndex = 0;

    private final String title;
    private final List<NeuRecipe> craftingRecipes;
    private final NEUManager manager;

    public int guiLeft = 0;
    public int guiTop = 0;
    public int xSize = 176;
    public int ySize = 166;

    public GuiItemRecipe(String title, List<NeuRecipe> craftingRecipes, NEUManager manager) {
        this.craftingRecipes = craftingRecipes;
        this.manager = manager;
        this.title = title;
    }

    public NeuRecipe getCurrentRecipe() {
        currentIndex = MathHelper.clamp_int(currentIndex, 0, craftingRecipes.size());
        return craftingRecipes.get(currentIndex);
    }

    public boolean isWithinRect(int x, int y, int topLeftX, int topLeftY, int width, int height) {
        return topLeftX <= x && x <= topLeftX + width
                && topLeftY <= y && y <= topLeftY + height;
    }

    private ImmutableList<RecipeSlot> getAllRenderedSlots() {
        return ImmutableList.<RecipeSlot>builder()
                .addAll(getPlayerInventory())
                .addAll(getCurrentRecipe().getSlots()).build();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        FontRenderer fontRendererObj = Minecraft.getMinecraft().fontRendererObj;

        this.guiLeft = (width - this.xSize) / 2;
        this.guiTop = (height - this.ySize) / 2;

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        NeuRecipe currentRecipe = getCurrentRecipe();

        Minecraft.getMinecraft().getTextureManager().bindTexture(currentRecipe.getBackground());
        this.drawTexturedModalRect(guiLeft, guiTop, 0, 0, this.xSize, this.ySize);

        currentRecipe.drawExtraBackground(this, mouseX, mouseY);

        List<RecipeSlot> slots = getAllRenderedSlots();
        for (RecipeSlot slot : slots) {
            Utils.drawItemStack(slot.getItemStack(), slot.getX(this), slot.getY(this));
        }

        if (craftingRecipes.size() > 1) drawArrows(mouseX, mouseY);

        Utils.drawStringScaledMaxWidth(title, fontRendererObj, guiLeft + TITLE_X, guiTop + TITLE_Y, false, xSize - 38, 0x404040);

        currentRecipe.drawExtraInfo(this, mouseX, mouseY);

        for (RecipeSlot slot : slots) {
            if (isWithinRect(mouseX, mouseY, slot.getX(this), slot.getY(this), SLOT_SIZE, SLOT_SIZE)) {
                if (slot.getItemStack() == null) continue;
                Utils.drawHoveringText(slot.getItemStack().getTooltip(Minecraft.getMinecraft().thePlayer, false), mouseX, mouseY, width, height, -1, fontRendererObj);
            }
        }
        currentRecipe.drawHoverInformation(this, mouseX, mouseY);
    }

    private void drawArrows(int mouseX, int mouseY) {
        boolean leftSelected = isWithinRect(mouseX - guiLeft, mouseY - guiTop, BUTTON_POSITION_LEFT_X, BUTTON_POSITION_Y, BUTTON_WIDTH, BUTTON_HEIGHT);
        boolean rightSelected = isWithinRect(mouseX - guiLeft, mouseY - guiTop, BUTTON_POSITION_RIGHT_X, BUTTON_POSITION_Y, BUTTON_WIDTH, BUTTON_HEIGHT);

        Minecraft.getMinecraft().getTextureManager().bindTexture(resourcePacksTexture);

        Utils.drawTexturedRect(guiLeft + BUTTON_POSITION_LEFT_X, guiTop + BUTTON_POSITION_Y, BUTTON_WIDTH, BUTTON_HEIGHT,
                34 / 256f, 48 / 256f,
                leftSelected ? 37 / 256f : 5 / 256f, leftSelected ? 59 / 256f : 27 / 256f
        );
        Utils.drawTexturedRect(guiLeft + BUTTON_POSITION_RIGHT_X, guiTop + BUTTON_POSITION_Y, BUTTON_WIDTH, BUTTON_HEIGHT,
                10 / 256f, 24 / 256f,
                rightSelected ? 37 / 256f : 5 / 256f, rightSelected ? 59 / 256f : 27 / 256f
        );
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

        String selectedPage = (currentIndex + 1) + "/" + craftingRecipes.size();

        Utils.drawStringCenteredScaledMaxWidth(selectedPage, fontRendererObj,
                guiLeft + PAGE_STRING_X, guiTop + PAGE_STRING_Y, false, 24, Color.BLACK.getRGB());
    }

    public List<RecipeSlot> getPlayerInventory() {
        List<RecipeSlot> slots = new ArrayList<>();
        ItemStack[] inventory = Minecraft.getMinecraft().thePlayer.inventory.mainInventory;
        int hotbarSize = InventoryPlayer.getHotbarSize();
        for (int i = 0; i < inventory.length; i++) {
            ItemStack item = inventory[i];
            if (item == null || item.stackSize == 0) continue;
            int row = i / hotbarSize;
            int col = i % hotbarSize;
            if (row == 0)
                slots.add(new RecipeSlot(HOTBAR_SLOT_X + i * SLOT_SPACING, HOTBAR_SLOT_Y, item));
            else
                slots.add(new RecipeSlot(PLAYER_INVENTORY_X + col * SLOT_SPACING, PLAYER_INVENTORY_Y + (row - 1) * SLOT_SPACING, item));
        }
        return slots;
    }

    @Override
    public void handleKeyboardInput() throws IOException {
        super.handleKeyboardInput();

        ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
        int width = scaledResolution.getScaledWidth();
        int height = scaledResolution.getScaledHeight();
        int mouseX = Mouse.getX() * width / Minecraft.getMinecraft().displayWidth;
        int mouseY = height - Mouse.getY() * height / Minecraft.getMinecraft().displayHeight - 1;
        int keyPressed = Keyboard.getEventKey() == 0 ? Keyboard.getEventCharacter()+256 : Keyboard.getEventKey();

        for (RecipeSlot slot : getAllRenderedSlots()) {
            if (isWithinRect(mouseX, mouseY, slot.getX(this), slot.getY(this), SLOT_SIZE, SLOT_SIZE)) {
                ItemStack itemStack = slot.getItemStack();
                if (keyPressed == manager.keybindViewRecipe.getKeyCode()) { // TODO: rework this so it doesnt skip recipe chains
                    manager.displayGuiItemRecipe(manager.getInternalNameForItem(itemStack), "");
                } else if (keyPressed == manager.keybindViewUsages.getKeyCode()) {
                    manager.displayGuiItemUsages(manager.getInternalNameForItem(itemStack));
                }
            }
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        if (isWithinRect(mouseX - guiLeft, mouseY - guiTop, BUTTON_POSITION_LEFT_X, BUTTON_POSITION_Y, BUTTON_WIDTH, BUTTON_HEIGHT)) {
            currentIndex = currentIndex == 0 ? 0 : currentIndex - 1;
            Utils.playPressSound();
            return;
        }

        if (isWithinRect(mouseX - guiLeft, mouseY - guiTop, BUTTON_POSITION_RIGHT_X, BUTTON_POSITION_Y, BUTTON_WIDTH, BUTTON_HEIGHT)) {
            currentIndex = currentIndex == craftingRecipes.size() - 1 ? currentIndex : currentIndex + 1;
            Utils.playPressSound();
            return;
        }

        for (RecipeSlot slot : getAllRenderedSlots()) {
            if (isWithinRect(mouseX, mouseY, slot.getX(this), slot.getY(this), SLOT_SIZE, SLOT_SIZE)) {
                ItemStack itemStack = slot.getItemStack();
                if (mouseButton == 0) {
                    manager.displayGuiItemRecipe(manager.getInternalNameForItem(itemStack), "");
                } else if (mouseButton == 1) {
                    manager.displayGuiItemUsages(manager.getInternalNameForItem(itemStack));
                }
            }
        }
    }
}
