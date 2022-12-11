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
import io.github.moulberry.notenoughupdates.miscfeatures.WitherCloakChanger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.layers.LayerCreeperCharge;
import net.minecraft.entity.monster.EntityCreeper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LayerCreeperCharge.class)
public abstract class MixinEntityChargedCreeper {

	@Inject(method = "doRenderLayer(Lnet/minecraft/entity/monster/EntityCreeper;FFFFFFF)V", at = @At("HEAD"), cancellable = true)
	public void cancelChargedCreeperLayer(EntityCreeper creeper, float f, float g, float partialTicks, float h, float i, float j, float scale, CallbackInfo ci) {
		//Wither Cloak Creepers: Is toggled on, Are invisible, 20 max health, usually less than 7.5M from the player, only existent when active, and only in sb obviously
		boolean isWitherCloak =
			NotEnoughUpdates.INSTANCE.config.itemOverlays.customWitherCloakToggle && creeper.isInvisible() &&
				creeper.getMaxHealth() == 20.0f && creeper.getDistanceToEntity(Minecraft.getMinecraft().thePlayer) < 7.5f &&
				(WitherCloakChanger.isCloakActive || System.currentTimeMillis() - WitherCloakChanger.lastDeactivate < 300) &&
				NotEnoughUpdates.INSTANCE.isOnSkyblock();
		if (isWitherCloak) {
			WitherCloakChanger.lastCreeperRender = System.currentTimeMillis();
			if (ci.isCancellable()) {
				ci.cancel();
			}
		}
	}
}
