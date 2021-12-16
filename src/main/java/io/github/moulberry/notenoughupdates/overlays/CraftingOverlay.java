package io.github.moulberry.notenoughupdates.overlays;

import com.google.gson.JsonObject;
import io.github.moulberry.notenoughupdates.NEUManager;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.util.List;

public class CraftingOverlay {
    private static ItemStack[] items = new ItemStack[9];
    private static final NEUManager manager = NotEnoughUpdates.INSTANCE.manager;
    public static boolean shouldRender = false;
    private static String text = null;

    public static void render() {
        if (shouldRender) {
            ContainerChest container = (ContainerChest) Minecraft.getMinecraft().thePlayer.openContainer;
            GuiChest gc = (GuiChest) Minecraft.getMinecraft().currentScreen;
            FontRenderer ft = Minecraft.getMinecraft().fontRendererObj;
            int width = Utils.peekGuiScale().getScaledWidth();
            int height = Utils.peekGuiScale().getScaledHeight();
            int mouseX = Mouse.getX() * width / Minecraft.getMinecraft().displayWidth;
            int mouseY = height - Mouse.getY() * height / Minecraft.getMinecraft().displayHeight - 1;
            List<String> tooltip = null;
            for (int i = 0; i < 9; i++) {
                if (items[i] != null) {
                    int slotIndex = (int) (10 + 9 * Math.floor(i / 3f) + (i % 3));
                    Slot slot = container.inventorySlots.get(slotIndex);
                    int x = slot.xDisplayPosition + gc.guiLeft;
                    int y = slot.yDisplayPosition + gc.guiTop;
                    if (!slot.getHasStack() || !manager.getInternalNameForItem(items[i]).equals(manager.getInternalNameForItem(slot.getStack())) ||
                            slot.getStack().stackSize < items[i].stackSize)
                        Gui.drawRect(x, y, x + 16, y + 16, 0x64ff0000);
                    if (!slot.getHasStack())
                        Utils.drawItemStack(items[i], x, y);
                    if (!slot.getHasStack() && mouseX >= x && mouseX < x + 16 && mouseY >= y && mouseY < y + 16)
                        tooltip = items[i].getTooltip(Minecraft.getMinecraft().thePlayer, false);
                }
            }
            if (text != null)
                ft.drawStringWithShadow(text,
                        Utils.peekGuiScale().getScaledWidth() / 2f - ft.getStringWidth(text) / 2f,
                        gc.guiTop - 15f, 0x808080);
            if (tooltip != null)
                Utils.drawHoveringText(tooltip, mouseX, mouseY, width, height, -1, ft);
        }
    }

    public static void keyInput() {
        if (!Keyboard.getEventKeyState() || Keyboard.getEventKey() != Keyboard.KEY_U && Keyboard.getEventKey() != Keyboard.KEY_R)
            return;
        int width = Utils.peekGuiScale().getScaledWidth();
        int height = Utils.peekGuiScale().getScaledHeight();
        int mouseX = Mouse.getX() * width / Minecraft.getMinecraft().displayWidth;
        int mouseY = height - Mouse.getY() * height / Minecraft.getMinecraft().displayHeight - 1;
        ContainerChest container = (ContainerChest) Minecraft.getMinecraft().thePlayer.openContainer;
        GuiChest gc = (GuiChest) Minecraft.getMinecraft().currentScreen;
        for (int i = 0; i < 9; i++) {
            if (items[i] != null) {
                int slotIndex = (int) (10 + 9 * Math.floor(i / 3f) + (i % 3));
                Slot slot = container.inventorySlots.get(slotIndex);
                int x = slot.xDisplayPosition + gc.guiLeft;
                int y = slot.yDisplayPosition + gc.guiTop;
                if (mouseX >= x && mouseX < x + 16 && mouseY >= y && mouseY < y + 16) {
                    if (!slot.getHasStack()) {
                        String internalName = manager.getInternalNameForItem(items[i]);
                        if (Keyboard.getEventKey() == Keyboard.KEY_U && internalName != null) {
                            manager.displayGuiItemUsages(internalName);
                        } else if (Keyboard.getEventKey() == Keyboard.KEY_R && internalName != null && manager.getItemInformation().containsKey(internalName)) {
                            JsonObject item = manager.getItemInformation().get(internalName);
                            manager.showRecipe(item);
                        }
                    }
                    break;
                }
            }
        }
    }
}
