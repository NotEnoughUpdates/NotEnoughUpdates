package io.github.moulberry.notenoughupdates.mixins;

import io.github.moulberry.notenoughupdates.miscfeatures.FancyPortals;
import net.minecraft.client.LoadingScreenRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LoadingScreenRenderer.class)
public class MixinLoadingScreenRenderer {
	@Inject(method = "setLoadingProgress", at = @At(value = "HEAD"), cancellable = true)
	public void setLoadingProgress(int progress, CallbackInfo ci) {
		if (progress < 0 && !FancyPortals.shouldRenderLoadingScreen()) {
			ci.cancel();
		}
	}
}
