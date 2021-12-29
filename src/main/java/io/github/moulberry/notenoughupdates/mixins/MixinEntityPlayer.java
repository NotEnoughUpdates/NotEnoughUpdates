package io.github.moulberry.notenoughupdates.mixins;

import io.github.moulberry.notenoughupdates.cosmetics.CapeManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EnumPlayerModelParts;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({EntityPlayer.class})
public abstract class MixinEntityPlayer {
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
}
