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

import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.events.GuiInventoryBackgroundDrawnEvent;
import io.github.moulberry.notenoughupdates.listener.RenderListener;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiInventory.class)
public class MixinGuiInventory {
	@Inject(method = "drawGuiContainerForegroundLayer", at = @At("HEAD"), cancellable = true)
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY, CallbackInfo ci) {
		if (NotEnoughUpdates.INSTANCE.config.inventoryButtons.hideCrafting ||
			RenderListener.disableCraftingText) {
			ci.cancel();
		}
	}

	@Inject(method = "drawGuiContainerBackgroundLayer", at = @At("TAIL"))
	public void onDrawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY, CallbackInfo ci) {
		new GuiInventoryBackgroundDrawnEvent((GuiContainer) (Object) this, partialTicks).post();
	}
}
