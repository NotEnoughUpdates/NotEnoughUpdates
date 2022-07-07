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

import net.minecraft.client.gui.inventory.GuiEditSign;

public class SignSubmitEvent extends NEUEvent {
	public final GuiEditSign sign;
	public final String[] lines;

	public SignSubmitEvent(GuiEditSign sign, String[] hehe) {
		this.sign = sign;
		this.lines = hehe;
	}

	public GuiEditSign getSign() {
		return sign;
	}

	public String[] getLines() {
		return lines;
	}
}
