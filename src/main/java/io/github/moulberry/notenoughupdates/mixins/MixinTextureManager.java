package io.github.moulberry.notenoughupdates.mixins;

import io.github.moulberry.notenoughupdates.miscfeatures.ItemCustomizeManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TextureManager.class)
public class MixinTextureManager {
    @Inject(method = "bindTexture", at = @At("HEAD"), cancellable = true)
    public void bindTexture(ResourceLocation location, CallbackInfo ci) {
        if (ItemCustomizeManager.disableTextureBinding) {
            ci.cancel();
        }
    }
}
