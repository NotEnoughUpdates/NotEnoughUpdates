package io.github.moulberry.notenoughupdates.itemeditor;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

public class GuiElementText extends GuiElement {
	protected String text;
	private final int colour;

	public GuiElementText(String text, int colour) {
		this.text = text;
		this.colour = colour;
	}

	@Override
	public int getHeight() {
		return 18;
	}

	@Override
	public int getWidth() {
		FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
		return fr.getStringWidth(text);
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	@Override
	public void render(int x, int y) {
		FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;

		fr.drawString(text, x, y + 6, colour);
	}
}
