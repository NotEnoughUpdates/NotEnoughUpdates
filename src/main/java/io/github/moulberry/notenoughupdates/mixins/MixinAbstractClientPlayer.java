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

import io.github.moulberry.notenoughupdates.miscfeatures.NPCRetexturing;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayer.class)
public class MixinAbstractClientPlayer {
	@Inject(method = "hasSkin", at = @At("HEAD"), cancellable = true)
	public void hasSkin(CallbackInfoReturnable<Boolean> cir) {
		AbstractClientPlayer $this = (AbstractClientPlayer) (Object) this;
		if (NPCRetexturing.getInstance().getSkin($this) != null) {
			cir.setReturnValue(true);
		}
	}

	@Inject(method = "getLocationSkin()Lnet/minecraft/util/ResourceLocation;", at = @At("HEAD"), cancellable = true)
	public void getLocationSkin(CallbackInfoReturnable<ResourceLocation> cir) {
		AbstractClientPlayer $this = (AbstractClientPlayer) (Object) this;
		NPCRetexturing.Skin skin = NPCRetexturing.getInstance().getSkin($this);
		if (skin != null) {
			cir.setReturnValue(skin.skinLocation);
		}
	}

	@Inject(method = "getSkinType", at = @At("HEAD"), cancellable = true)
	public void getSkinType(CallbackInfoReturnable<String> cir) {
		AbstractClientPlayer $this = (AbstractClientPlayer) (Object) this;
		NPCRetexturing.Skin skin = NPCRetexturing.getInstance().getSkin($this);
		if (skin != null) {
			cir.setReturnValue(skin.skinny ? "slim" : "default");
		}
	}
}
