package io.github.moulberry.notenoughupdates.mixins;

import io.github.moulberry.notenoughupdates.cosmetics.CapeManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.lib.Opcodes;
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
