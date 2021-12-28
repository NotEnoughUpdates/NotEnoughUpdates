package io.github.moulberry.notenoughupdates.mbgui;

import org.lwjgl.util.vector.Vector2f;

import java.util.Collection;
import java.util.List;

public abstract class MBGuiGroupAligned extends MBGuiGroup {
    //Serialized
    private final List<MBGuiElement> children;
    private final boolean vertical;

    public MBGuiGroupAligned(List<MBGuiElement> children, boolean vertical) {
        this.children = children;
        this.vertical = vertical;
        recalculate();
    }

    public abstract int getPadding();

    public Collection<MBGuiElement> getChildren() {
        return children;
    }

    public void recalculate() {
        for (MBGuiElement child : children) {
            child.recalculate();
        }

        if (vertical) {
            height = 0;
            for (int i = 0; i < children.size(); i++) {
                MBGuiElement child = children.get(i);
                childrenPosition.put(child, new Vector2f(0, height));
                height += child.getHeight();
                if (i != children.size() - 1) height += getPadding();
            }

            width = 0;
            for (MBGuiElement child : children) {
                int childWidth = child.getWidth();
                if (childWidth > width) {
                    width = childWidth;
                }
            }
        } else {
            width = 0;
            for (int i = 0; i < children.size(); i++) {
                MBGuiElement child = children.get(i);
                childrenPosition.put(child, new Vector2f(width, 0));
                width += child.getWidth();
                if (i != children.size() - 1) width += getPadding();
            }

            height = 0;
            for (MBGuiElement child : children) {
                int childHeight = child.getHeight();
                if (childHeight > height) {
                    height = childHeight;
                }
            }
        }
    }
}
