package io.github.moulberry.notenoughupdates.mixins;

import io.github.moulberry.notenoughupdates.miscfeatures.CustomSkulls;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemCameraTransforms.class)
public class MixinItemCameraTransforms {
	@Inject(method = "applyTransform", at = @At("HEAD"))
	public void applyTransform(ItemCameraTransforms.TransformType type, CallbackInfo ci) {
		CustomSkulls.mostRecentTransformType = type;
	}
}
