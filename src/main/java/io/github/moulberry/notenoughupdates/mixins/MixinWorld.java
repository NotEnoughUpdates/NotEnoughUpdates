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
import io.github.moulberry.notenoughupdates.miscfeatures.CustomItemEffects;
import io.github.moulberry.notenoughupdates.miscfeatures.customblockzones.CustomBiomes;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(World.class)
public class MixinWorld {
	@Inject(method = "spawnParticle(IZDDDDDD[I)V", at = @At("HEAD"), cancellable = true)
	public void spawnParticle(
		int particleID, boolean p_175720_2_, double xCood, double yCoord, double zCoord,
		double xOffset, double yOffset, double zOffset, int[] p_175720_15_, CallbackInfo ci
	) {
		if (NotEnoughUpdates.INSTANCE.config.itemOverlays.disableHyperionParticles &&
			System.currentTimeMillis() - CustomItemEffects.INSTANCE.lastUsedHyperion < 500) {
			if (particleID == 1) {
				ci.cancel();
			}
		}
	}

	@Inject(method = "getBiomeGenForCoords", at = @At("HEAD"), cancellable = true)
	public void getBiomeGenForCoords(BlockPos pos, CallbackInfoReturnable<BiomeGenBase> cir) {
		BiomeGenBase customBiome = CustomBiomes.INSTANCE.getCustomBiome(pos);
		if (customBiome != null) {
			cir.setReturnValue(customBiome);
		}

	}
}
