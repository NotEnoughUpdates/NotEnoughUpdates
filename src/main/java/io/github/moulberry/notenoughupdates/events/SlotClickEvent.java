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
import net.minecraft.inventory.Slot;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import org.jetbrains.annotations.NotNull;

@Cancelable
public class SlotClickEvent extends NEUEvent {
	public final @NotNull GuiContainer guiContainer;
	public final @NotNull Slot slot;
	public final int slotId;
	public int clickedButton;
	/**
	 * Click types (along with the default keybind):
	 *
	 * <ul>
	 * 	 <li>0 : mouse click (either LMB or RMB)</li>
	 * 	 <li>1 : Shift mouse click</li>
	 * 	 <li>2 : hotbar keybind (0-9) -> see clickedButton</li>
	 * 	 <li>3 : pick block (middle mouse button)</li>
	 * 	 <li>4 : drop block (Q)</li>
	 * </ul>
	 */
	public int clickType;
	public boolean usePickblockInstead = false;

	public SlotClickEvent(GuiContainer guiContainer, Slot slot, int slotId, int clickedButton, int clickType) {
		this.guiContainer = guiContainer;
		this.slot = slot;
		this.slotId = slotId;
		this.clickedButton = clickedButton;
		this.clickType = clickType;
	}

	public void usePickblockInstead() {
		usePickblockInstead = true;
	}

	@Override
	public void setCanceled(boolean cancel) {
		super.setCanceled(cancel);
	}
}
