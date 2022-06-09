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

package io.github.moulberry.notenoughupdates.infopanes;

import io.github.moulberry.notenoughupdates.NEUManager;
import io.github.moulberry.notenoughupdates.NEUOverlay;
import io.github.moulberry.notenoughupdates.core.util.lerp.LerpingInteger;
import org.lwjgl.input.Mouse;

public abstract class ScrollableInfoPane extends InfoPane {
	private static final int SCROLL_AMOUNT = 50;
	protected LerpingInteger scrollHeight = new LerpingInteger(0);

	public ScrollableInfoPane(NEUOverlay overlay, NEUManager manager) {
		super(overlay, manager);
	}

	public void tick() {
		scrollHeight.tick();
		if (scrollHeight.getValue() < 0) scrollHeight.setValue(0);
	}

	@Override
	public void mouseInput(int width, int height, int mouseX, int mouseY, boolean mouseDown) {
		int dWheel = Mouse.getEventDWheel();

		if (dWheel < 0) {
			scrollHeight.setTarget(scrollHeight.getTarget() + SCROLL_AMOUNT);
			scrollHeight.resetTimer();
		} else if (dWheel > 0) {
			scrollHeight.setTarget(scrollHeight.getTarget() - SCROLL_AMOUNT);
			scrollHeight.resetTimer();
		}
	}
}
