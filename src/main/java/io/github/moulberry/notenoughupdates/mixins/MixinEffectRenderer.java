package io.github.moulberry.notenoughupdates.mixins;

import io.github.moulberry.notenoughupdates.miscfeatures.CustomItemEffects;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.util.vector.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EffectRenderer.class)
public class MixinEffectRenderer {
	@Redirect(method = "renderParticles", at = @At(
		value = "INVOKE",
		target = "Lnet/minecraft/client/renderer/GlStateManager;enableBlend()V")
	)
	public void renderParticles_enableBlend() {
		GlStateManager.enableBlend();

		Vector3f currentPosition = CustomItemEffects.INSTANCE.getCurrentPosition();
		if (currentPosition != null) {
			EntityFX.interpPosX = currentPosition.x;
			EntityFX.interpPosY = currentPosition.y;
			EntityFX.interpPosZ = currentPosition.z;
		}
	}
}
