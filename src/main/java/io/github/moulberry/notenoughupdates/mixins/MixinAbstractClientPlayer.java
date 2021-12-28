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
