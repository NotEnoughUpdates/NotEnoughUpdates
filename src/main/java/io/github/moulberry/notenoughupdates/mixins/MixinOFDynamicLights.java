/*
 * Copyright (C) 2023 NotEnoughUpdates contributors
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
import io.github.moulberry.notenoughupdates.miscgui.DynamicLightItemsEditor;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "net.optifine.DynamicLights", remap = false)
public class MixinOFDynamicLights {

	@Inject(method = "getLightLevel(Lnet/minecraft/item/ItemStack;)I", at = @At("TAIL"))
	private static int getLightLevel(ItemStack itemStack, CallbackInfoReturnable<Integer> cir) {
		String internalName = DynamicLightItemsEditor.Companion.resolveInternalName(itemStack);
		if (internalName == null) return 0;

		if (NotEnoughUpdates.INSTANCE.config.hidden.dynamicLightItems.contains(internalName)) {
			return 15;
		}
		return 0;
	}

}
