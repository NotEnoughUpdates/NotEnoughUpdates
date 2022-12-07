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

package io.github.moulberry.notenoughupdates.overlays;

import io.github.moulberry.notenoughupdates.core.config.Position;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import org.lwjgl.input.Keyboard;

import java.util.List;
import java.util.function.Supplier;

public abstract class TextTabOverlay extends TextOverlay {
	public TextTabOverlay(
		Position position,
		Supplier<List<String>> dummyStrings,
		Supplier<TextOverlayStyle> styleSupplier
	) {
		super(position, dummyStrings, styleSupplier);
	}

	protected boolean lastTabState = false;
	private boolean shouldUpdateOverlay = true;

	@Override
	public void tick() {
		if (shouldUpdateOverlay) {
			update();
		}
	}

	public void realTick() {
		shouldUpdateOverlay = shouldUpdate();
		if (!(Minecraft.getMinecraft().currentScreen instanceof GuiChat)) {
			int keycode = Minecraft.getMinecraft().gameSettings.keyBindPlayerList.getKeyCode();
			boolean currentTabState;
			if (keycode > 0) {
				currentTabState = Keyboard.isKeyDown(keycode);
			} else {
				currentTabState = false;
			}
			if (lastTabState != currentTabState) {
				lastTabState = currentTabState;
			}
		} else lastTabState = false; // disallow showing overlays that use tab while having chat open
		if (shouldUpdateOverlay) {
				update();
		}
	}

	private boolean shouldUpdate() {
		//prevent rendering when tab completing in ah search overlay
		if (AuctionSearchOverlay.shouldReplace()) {
			return false;
		}

		return true;
	}
}
