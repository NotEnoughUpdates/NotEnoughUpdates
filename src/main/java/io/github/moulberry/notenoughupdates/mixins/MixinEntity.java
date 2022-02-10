package io.github.moulberry.notenoughupdates.mixins;

import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class MixinEntity {
    // Fix NPE in vanilla code, that we need to work for VillagerTradeRecipe
    @Inject(method = "getBrightnessForRender", at = @At("HEAD"), cancellable = true)
    public void onGetBrightnessForRender(float p_getBrightnessForRender_1_, CallbackInfoReturnable<Integer> cir) {
        if (((Entity) (Object) this).worldObj == null)
            cir.setReturnValue(-1);
    }

    // Fix NPE in vanilla code, that we need to work for VillagerTradeRecipe
    @Inject(method = "getBrightness", at = @At("HEAD"), cancellable = true)
    public void onGetBrightness(float p_getBrightness_1_, CallbackInfoReturnable<Float> cir) {
        if (((Entity) (Object) this).worldObj == null)
            cir.setReturnValue(1.0F);
    }
}
