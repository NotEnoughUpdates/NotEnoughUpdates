package io.github.moulberry.notenoughupdates.infopanes;

import io.github.moulberry.notenoughupdates.NEUManager;
import io.github.moulberry.notenoughupdates.NEUOverlay;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;

import java.awt.*;

public class TextInfoPane extends ScrollableInfoPane {
    protected String title;
    protected String text;

    public TextInfoPane(NEUOverlay overlay, NEUManager manager, String title, String text) {
        super(overlay, manager);
        this.title = title;
        this.text = text;
    }

    public void render(int width, int height, Color bg, Color fg, ScaledResolution scaledresolution, int mouseX, int mouseY) {
        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;

        int paneWidth = (int) (width / 3 * overlay.getWidthMult());
        int rightSide = (int) (width * overlay.getInfoPaneOffsetFactor());
        int leftSide = rightSide - paneWidth;

        int titleLen = fr.getStringWidth(title);
        int yScroll = -scrollHeight.getValue();
        fr.drawString(title, (leftSide + rightSide - titleLen) / 2, yScroll + overlay.getBoxPadding() + 5,
                Color.WHITE.getRGB());

        int yOff = 20;
        for (String line : text.split("\n")) {
            yOff += Utils.renderStringTrimWidth(line, fr, false, leftSide + overlay.getBoxPadding() + 5,
                    yScroll + overlay.getBoxPadding() + 10 + yOff,
                    width * 1 / 3 - overlay.getBoxPadding() * 2 - 10, Color.WHITE.getRGB(), -1);
            yOff += 16;
        }

        int top = overlay.getBoxPadding() - 5;
        int totalBoxHeight = yOff + 14;
        int bottom = Math.max(top + totalBoxHeight, height - overlay.getBoxPadding() + 5);

        if (scrollHeight.getValue() > top + totalBoxHeight - (height - overlay.getBoxPadding() + 5)) {
            scrollHeight.setValue(top + totalBoxHeight - (height - overlay.getBoxPadding() + 5));
        }
        drawRect(leftSide + overlay.getBoxPadding() - 5, yScroll + overlay.getBoxPadding() - 5,
                rightSide - overlay.getBoxPadding() + 5, yScroll + bottom, bg.getRGB());
    }

    public boolean keyboardInput() {
        return false;
    }

    public void mouseInput(int width, int height, int mouseX, int mouseY, boolean mouseDown) {
        super.mouseInput(width, height, mouseX, mouseY, mouseDown);
    }
}
