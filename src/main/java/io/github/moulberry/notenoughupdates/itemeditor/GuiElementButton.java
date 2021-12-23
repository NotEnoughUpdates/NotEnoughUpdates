package io.github.moulberry.notenoughupdates.itemeditor;

import java.awt.*;

public class GuiElementButton extends GuiElementText {
    private final Runnable callback;

    public GuiElementButton(String text, int colour, Runnable callback) {
        super(text, colour);
        this.callback = callback;
    }

    @Override
    public int getHeight() {
        return super.getHeight() + 5;
    }

    @Override
    public int getWidth() {
        return super.getWidth() + 10;
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        callback.run();
    }

    @Override
    public void render(int x, int y) {
        drawRect(x, y, x + getWidth(), y + super.getHeight(), Color.WHITE.getRGB());
        drawRect(x + 1, y + 1, x + getWidth() - 1, y + super.getHeight() - 1, Color.BLACK.getRGB());
        super.render(x + 5, y - 1);
    }
}
