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

package io.github.moulberry.notenoughupdates.profileviewer;

import javax.annotation.Nullable;
import java.io.IOException;

public abstract class GuiProfileViewerPage {

	private final GuiProfileViewer instance;

	public GuiProfileViewerPage(GuiProfileViewer instance) {
		this.instance = instance;
	}

	/**
	 * @return Instance of the current {@link GuiProfileViewer}
	 */
	public GuiProfileViewer getInstance() {
		return instance;
	}

	/**
	 * @see GuiProfileViewer#getSelectedProfile()
	 */
	public @Nullable SkyblockProfiles.SkyblockProfile getSelectedProfile() {
		return GuiProfileViewer.getSelectedProfile();
	}

	public abstract void drawPage(int mouseX, int mouseY, float partialTicks);

	/**
	 * @return Whether to return in calling method
	 */
	public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		return false;
	}

	public void mouseReleased(int mouseX, int mouseY, int mouseButton) {}

	public void keyTyped(char typedChar, int keyCode) throws IOException {}

	public void resetCache() {}
}
