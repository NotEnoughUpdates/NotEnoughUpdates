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

package io.github.moulberry.notenoughupdates.mixins;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(GuiContainer.class)
public interface AccessorGuiContainer {

	@Invoker("getSlotAtPosition")
	Slot doGetSlotAtPosition(int x, int y);

	@Invoker("drawSlot")
	void doDrawSlot(Slot slot);

	@Invoker("isMouseOverSlot")
	boolean doIsMouseOverSlot(Slot slot, int x, int y);

	@Accessor("guiLeft")
	int getGuiLeft();

	@Accessor("guiTop")
	int getGuiTop();

	@Accessor("xSize")
	int getXSize();

	@Accessor("ySize")
	int getYSize();

}
