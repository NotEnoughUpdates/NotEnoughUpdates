package io.github.moulberry.notenoughupdates.mbgui;

public abstract class MBGuiElement {
	public abstract int getWidth();

	public abstract int getHeight();

	public abstract void recalculate();

	public abstract void mouseClick(float x, float y, int mouseX, int mouseY);

	public abstract void mouseClickOutside();

	public abstract void render(float x, float y);

	//public abstract JsonObject serialize();
}
