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

import com.mojang.authlib.GameProfile;
import io.github.moulberry.notenoughupdates.miscfeatures.CustomSkulls;
import net.minecraft.client.renderer.tileentity.TileEntitySkullRenderer;
import net.minecraft.util.EnumFacing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TileEntitySkullRenderer.class)
public class MixinTileEntitySkullRenderer {
	@Inject(method = "renderSkull", at = @At("HEAD"), cancellable = true)
	public void renderSkull(
		float xOffset, float yOffset, float zOffset, EnumFacing placedDirection,
		float rotationDeg, int skullType, GameProfile skullOwner, int damage, CallbackInfo ci
	) {
		if (CustomSkulls.getInstance().renderSkull(
			xOffset,
			yOffset,
			zOffset,
			placedDirection,
			rotationDeg,
			skullType,
			skullOwner,
			damage
		)) {
			ci.cancel();
		}
	}
}
