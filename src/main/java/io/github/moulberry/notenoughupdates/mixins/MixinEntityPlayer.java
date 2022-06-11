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

import io.github.moulberry.notenoughupdates.cosmetics.CapeManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({EntityPlayer.class})
public abstract class MixinEntityPlayer {
	@Shadow
	public abstract boolean interactWith(Entity par1);

	@Inject(method = "isWearing", at = @At("HEAD"), cancellable = true)
	public void isWearing(EnumPlayerModelParts part, CallbackInfoReturnable<Boolean> cir) {
		if (part == EnumPlayerModelParts.CAPE) {
			EntityPlayer $this = (EntityPlayer) (Object) this;
			String uuid = $this.getUniqueID().toString().replace("-", "");
			String cape = CapeManager.getInstance().getCape(uuid);
			if (cape != null && !cape.equalsIgnoreCase("null")) {
				cir.setReturnValue(false);
			}
		}
	}

	@Redirect(method = "<init>", at = @At(value = "FIELD", target = "Lnet/minecraft/world/World;isRemote:Z", opcode = Opcodes.GETFIELD))
	public boolean onIsRemote(World instance) {
		if (instance == null) return true;
		return instance.isRemote;
	}

	@Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getSpawnPoint()Lnet/minecraft/util/BlockPos;"))
	public BlockPos onGetSpawnPoint(World instance) {
		if (instance == null)
			return new BlockPos(0, 0, 0);
		return instance.getSpawnPoint();
	}

	@Inject(method = "getWorldScoreboard", at = @At("HEAD"), cancellable = true)
	public void onGetWorldScoreboard(CallbackInfoReturnable<Scoreboard> cir) {
		if (((EntityPlayer) (Object) this).worldObj == null) {
			cir.setReturnValue(null);
		}
	}

	@Redirect(method = "getTeam", at = @At(value = "INVOKE", target = "Lnet/minecraft/scoreboard/Scoreboard;getPlayersTeam(Ljava/lang/String;)Lnet/minecraft/scoreboard/ScorePlayerTeam;"))
	public ScorePlayerTeam onGetTeam(Scoreboard instance, String p_getPlayersTeam_1_) {
		if (instance == null) {
			return null;
		}
		return instance.getPlayersTeam(p_getPlayersTeam_1_);
	}

}
