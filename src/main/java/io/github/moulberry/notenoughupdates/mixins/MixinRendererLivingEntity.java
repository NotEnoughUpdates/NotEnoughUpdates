package io.github.moulberry.notenoughupdates.mixins;

import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.miscfeatures.DamageCommas;
import io.github.moulberry.notenoughupdates.overlays.BonemerangOverlay;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.util.IChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RendererLivingEntity.class)
public abstract class MixinRendererLivingEntity<T extends EntityLivingBase> {
    @Redirect(method = "renderName", at = @At(value = "INVOKE", target =
            "Lnet/minecraft/entity/EntityLivingBase;getDisplayName()Lnet/minecraft/util/IChatComponent;"))
    public IChatComponent renderName_getDisplayName(EntityLivingBase entity) {
        if (entity instanceof EntityArmorStand) {
            return DamageCommas.replaceName(entity);
        } else {
            return entity.getDisplayName();
        }
    }

    @Inject(method = "getColorMultiplier", at = @At("HEAD"), cancellable = true)
    public void getColorMultiplier(T entitylivingbaseIn, float lightBrightness,
                                   float partialTickTime, CallbackInfoReturnable<Integer> cir) {
        if (BonemerangOverlay.INSTANCE.bonemeragedEntities.contains(entitylivingbaseIn) && NotEnoughUpdates.INSTANCE.config.itemOverlays.highlightTargeted) {
            cir.setReturnValue(0x80ff9500);
        }
    }
}
