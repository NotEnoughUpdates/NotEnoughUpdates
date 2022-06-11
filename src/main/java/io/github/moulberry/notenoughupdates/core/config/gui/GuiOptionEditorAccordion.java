/*
 * Copyright (C) 2022 NotEnoughUpdates contributors
 *
 * This file is part of NotEnoughUpdates.
 *
 * NotEnoughUpdates is free software: you can redistribute it
 * and/or modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * NotEnoughUpdates is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with NotEnoughUpdates. If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.moulberry.notenoughupdates.core.config.gui;

import io.github.moulberry.notenoughupdates.core.config.struct.ConfigProcessor;
import io.github.moulberry.notenoughupdates.core.util.render.RenderUtils;
import io.github.moulberry.notenoughupdates.core.util.render.TextRenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

public class GuiOptionEditorAccordion extends GuiOptionEditor {
	private final int accordionId;
	private boolean accordionToggled;

	public GuiOptionEditorAccordion(ConfigProcessor.ProcessedOption option, int accordionId) {
		super(option);
		this.accordionToggled = (boolean) option.get();
		this.accordionId = accordionId;
	}

	@Override
	public int getHeight() {
		return 20;
	}

	public int getAccordionId() {
		return accordionId;
	}

	public boolean getToggled() {
		return accordionToggled;
	}

	@Override
	public void render(int x, int y, int width) {
		int height = getHeight();
		RenderUtils.drawFloatingRectDark(x, y, width, height, true);

		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer worldrenderer = tessellator.getWorldRenderer();
		GlStateManager.enableBlend();
		GlStateManager.disableTexture2D();
		GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
		GlStateManager.color(1, 1, 1, 1);
		worldrenderer.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION);
		if (accordionToggled) {
			worldrenderer.pos((double) x + 6, (double) y + 6, 0.0D).endVertex();
			worldrenderer.pos((double) x + 9.75f, (double) y + 13.5f, 0.0D).endVertex();
			worldrenderer.pos((double) x + 13.5f, (double) y + 6, 0.0D).endVertex();
		} else {
			worldrenderer.pos((double) x + 6, (double) y + 13.5f, 0.0D).endVertex();
			worldrenderer.pos((double) x + 13.5f, (double) y + 9.75f, 0.0D).endVertex();
			worldrenderer.pos((double) x + 6, (double) y + 6, 0.0D).endVertex();
		}
		tessellator.draw();
		GlStateManager.enableTexture2D();
		GlStateManager.disableBlend();

		TextRenderUtils.drawStringScaledMaxWidth(option.name, Minecraft.getMinecraft().fontRendererObj,
			x + 18, y + 6, false, width - 10, 0xc0c0c0
		);
	}

	@Override
	public boolean mouseInput(int x, int y, int width, int mouseX, int mouseY) {
		if (Mouse.getEventButtonState() && mouseX > x && mouseX < x + width &&
			mouseY > y && mouseY < y + getHeight()) {
			accordionToggled = !accordionToggled;
			return true;
		}

		return false;
	}

	@Override
	public boolean keyboardInput() {
		return false;
	}
}
