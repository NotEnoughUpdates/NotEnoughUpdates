package io.github.moulberry.notenoughupdates.mbgui;

import org.lwjgl.util.vector.Vector2f;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public abstract class MBGuiGroup extends MBGuiElement {
    public int width;
    public int height;
    protected HashMap<MBGuiElement, Vector2f> childrenPosition = new HashMap<>();

    public MBGuiGroup() {}

    public abstract Collection<MBGuiElement> getChildren();

    public Map<MBGuiElement, Vector2f> getChildrenPosition() {
        return Collections.unmodifiableMap(childrenPosition);
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public void mouseClick(float x, float y, int mouseX, int mouseY) {
        Map<MBGuiElement, Vector2f> childrenPos = getChildrenPosition();

        for (MBGuiElement child : getChildren()) {
            Vector2f childPos = childrenPos.get(child);
            if (mouseX > x + childPos.x && mouseX < x + childPos.x + child.getWidth()) {
                if (mouseY > y + childPos.y && mouseY < y + childPos.y + child.getHeight()) {
                    child.mouseClick(x + childPos.x, y + childPos.y, mouseX, mouseY);
                }
            }
        }
    }

    @Override
    public void mouseClickOutside() {
        for (MBGuiElement child : getChildren()) {
            child.mouseClickOutside();
        }
    }

    @Override
    public void render(float x, float y) {
        Map<MBGuiElement, Vector2f> childrenPos = getChildrenPosition();

        for (MBGuiElement child : getChildren()) {
            Vector2f childPos = childrenPos.get(child);
            child.render(x + childPos.x, y + childPos.y);
        }
    }
}
