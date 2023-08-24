/*
 * Copyright (C) 2022-2023 NotEnoughUpdates contributors
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

package io.github.moulberry.notenoughupdates.core.config;

import io.github.moulberry.moulconfig.gui.GuiOptionEditor;
import io.github.moulberry.notenoughupdates.core.util.render.RenderUtils;
import io.github.moulberry.notenoughupdates.core.util.render.TextRenderUtils;
import lombok.var;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public class GuiOptionEditorBlocked extends GuiOptionEditor {
	public static final ResourceLocation blockedTexture = new ResourceLocation(
		"notenoughupdates:textures/gui/config_blocked.png");
	private final GuiOptionEditor base;

	public GuiOptionEditorBlocked(GuiOptionEditor base) {
		super(base.getOption());
		this.base = base;
	}

	@Override
	public void render(int x, int y, int width) {
		// No super. We delegate and overlay ourselves instead.
		base.render(x, y, width);

		var mc = Minecraft.getMinecraft();

		// Depress original option
		Gui.drawRect(x, y, x + width, y + getHeight(), 0x80000000);

		GlStateManager.color(1, 1, 1, 1);
		mc.getTextureManager().bindTexture(blockedTexture);

		float iconWidth = getHeight() * 96F / 64;
		RenderUtils.drawTexturedRect(x, y, iconWidth, getHeight());

		TextRenderUtils.drawStringScaledMaxWidth(
			"This option is currently not available.",
			x + iconWidth,y + getHeight() / 2F - mc.fontRendererObj.FONT_HEIGHT / 2F,
			true, (int) (width - iconWidth), 0xFFFF4444
		);
		GlStateManager.color(1, 1, 1, 1);
	}

	@Override
	public boolean mouseInput(int x, int y, int width, int mouseX, int mouseY) {
		return false;
	}

	@Override
	public boolean keyboardInput() {
		return false;
	}
}
