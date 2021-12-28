package io.github.moulberry.notenoughupdates.util;

import org.lwjgl.input.Keyboard;

/**
 * Utility used for positioning GUI elements during development.
 */
public class TexLoc {
    public int x;
    public int y;
    private final int toggleKey;
    private boolean toggled;
    private boolean pressedLastTick;
    private boolean dirPressed;

    public TexLoc(int x, int y, int toggleKey) {
        this.x = x;
        this.y = y;
        this.toggleKey = toggleKey;
    }

    public boolean handleKeyboardInput() {
        int mult = 1;
        if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) mult = 10;
        if (Keyboard.isKeyDown(toggleKey)) {
            if (!pressedLastTick) {
                toggled = !toggled;
            }
            pressedLastTick = true;
        } else {
            pressedLastTick = false;
        }
        if (toggled || toggleKey == 0) {
            if (Keyboard.isKeyDown(Keyboard.KEY_LEFT)) {
                if (!dirPressed) x -= mult;
                dirPressed = true;
            } else if (Keyboard.isKeyDown(Keyboard.KEY_RIGHT)) {
                if (!dirPressed) x += mult;
                dirPressed = true;
            } else if (Keyboard.isKeyDown(Keyboard.KEY_UP)) {
                if (!dirPressed) y -= mult;
                dirPressed = true;
            } else if (Keyboard.isKeyDown(Keyboard.KEY_DOWN)) {
                if (!dirPressed) y += mult;
                dirPressed = true;
            } else {
                dirPressed = false;
                return false;
            }
            System.out.println("X: " + x + " ; Y: " + y);
            return true;
        }
        return false;
    }
}
