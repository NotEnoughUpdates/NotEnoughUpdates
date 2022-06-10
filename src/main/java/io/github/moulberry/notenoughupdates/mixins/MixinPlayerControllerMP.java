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

import io.github.moulberry.notenoughupdates.events.OnBlockBreakSoundEffect;
import io.github.moulberry.notenoughupdates.miscfeatures.ItemCooldowns;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerControllerMP.class)
public class MixinPlayerControllerMP {
	@Inject(method = "clickBlock", at = @At("HEAD"), cancellable = true)
	public void clickBlock(BlockPos loc, EnumFacing face, CallbackInfoReturnable<Boolean> cir) {
		ItemCooldowns.blockClicked(loc);
        /*if(MiningStuff.blockClicked(loc)) {
            cir.setReturnValue(false);
            ((PlayerControllerMP)(Object)this).resetBlockRemoving();
        }*/
	}

	@Redirect(method = "onPlayerDamageBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/audio/SoundHandler;playSound(Lnet/minecraft/client/audio/ISound;)V"))
	public void onPlayerDamageBlock(
		SoundHandler instance,
		ISound p_playSound_1_,
		BlockPos p_onPlayerDamageBlock_1_,
		EnumFacing p_onPlayerDamageBlock_2_
	) {
		OnBlockBreakSoundEffect onBlockBreakSoundEffect = new OnBlockBreakSoundEffect(
			p_playSound_1_,
			p_onPlayerDamageBlock_1_,
			Minecraft.getMinecraft().theWorld.getBlockState(p_onPlayerDamageBlock_1_)
		);
		if (!onBlockBreakSoundEffect.post()) {
			instance.playSound(onBlockBreakSoundEffect.getSound());
		}
	}

}
