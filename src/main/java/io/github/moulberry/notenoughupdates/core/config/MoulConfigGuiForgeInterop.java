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

package io.github.moulberry.notenoughupdates.core.config;

import io.github.moulberry.moulconfig.gui.GuiScreenElementWrapper;
import io.github.moulberry.notenoughupdates.commands.help.SettingsCommand;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.IModGuiFactory;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.Set;

public class MoulConfigGuiForgeInterop implements IModGuiFactory {
	@Override
	public void initialize(Minecraft minecraft) {}

	@Override
	public Class<? extends GuiScreen> mainConfigGuiClass() {
		return WrappedMoulConfig.class;
	}

	@Override
	public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {
		return null;
	}

	@Override
	public RuntimeOptionGuiHandler getHandlerFor(RuntimeOptionCategoryElement runtimeOptionCategoryElement) {
		return null;
	}

	public static class WrappedMoulConfig extends GuiScreenElementWrapper {

		private final GuiScreen parent;

		public WrappedMoulConfig(GuiScreen parent) {
			super(SettingsCommand.INSTANCE.createConfigElement(""));
			this.parent = parent;
		}

		@Override
		public void handleKeyboardInput() throws IOException {
			if (Keyboard.getEventKeyState() && Keyboard.getEventKey() == Keyboard.KEY_ESCAPE) {
				Minecraft.getMinecraft().displayGuiScreen(parent);
				return;
			}
			super.handleKeyboardInput();
		}
	}
}
