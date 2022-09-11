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

package io.github.moulberry.notenoughupdates.events;

import net.minecraft.client.gui.inventory.GuiContainer;

public class GuiInventoryBackgroundDrawnEvent extends NEUEvent {
	private final GuiContainer container;
	private final float partialTicks;

	public GuiInventoryBackgroundDrawnEvent(GuiContainer container, float partialTicks) {
		this.container = container;
		this.partialTicks = partialTicks;
	}

	public GuiContainer getContainer() {
		return container;
	}

	public float getPartialTicks() {
		return partialTicks;
	}
}
