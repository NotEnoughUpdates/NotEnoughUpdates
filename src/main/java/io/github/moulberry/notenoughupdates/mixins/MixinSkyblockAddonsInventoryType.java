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
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.regex.Pattern;

@Pseudo
@Mixin(targets = "codes.biscuit.skyblockaddons.core.InventoryType", remap = false)
public abstract class MixinSkyblockAddonsInventoryType {

	@Shadow
	@Final
	private Pattern inventoryPattern;

	@Inject(method = "getInventoryPattern", at = @At("HEAD"), cancellable = true)
	private void modifyStorage(CallbackInfoReturnable<Pattern> cir) {
		if (!NotEnoughUpdates.INSTANCE.config.storageGUI.fixSBABackpack) return;
		if (inventoryPattern.toString().equals("(?<type>[a-zA-Z]+) Backpack ?✦? \\((?<page>\\d+)/\\d+\\)")) {
			cir.setReturnValue(Pattern.compile("(?<type>[a-zA-Z]+) Backpack ?✦? \\(Slot #(?<page>\\d+)\\)"));
		}
	}

}
