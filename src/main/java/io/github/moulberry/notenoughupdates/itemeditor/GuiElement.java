package io.github.moulberry.notenoughupdates.itemeditor;

import net.minecraft.client.gui.Gui;

public abstract class GuiElement extends Gui {
    public abstract void render(int x, int y);

    public abstract int getWidth();

    public abstract int getHeight();

    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {}

    public void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {}

    public void otherComponentClick() {}

    public void keyTyped(char typedChar, int keyCode) {}
}
