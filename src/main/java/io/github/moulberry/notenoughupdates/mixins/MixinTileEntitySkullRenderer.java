package io.github.moulberry.notenoughupdates.mixins;

import com.mojang.authlib.GameProfile;
import io.github.moulberry.notenoughupdates.miscfeatures.CustomSkulls;
import net.minecraft.client.renderer.tileentity.TileEntitySkullRenderer;
import net.minecraft.util.EnumFacing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TileEntitySkullRenderer.class)
public class MixinTileEntitySkullRenderer {
	@Inject(method = "renderSkull", at = @At("HEAD"), cancellable = true)
	public void renderSkull(
		float xOffset, float yOffset, float zOffset, EnumFacing placedDirection,
		float rotationDeg, int skullType, GameProfile skullOwner, int damage, CallbackInfo ci
	) {
		if (CustomSkulls.getInstance().renderSkull(
			xOffset,
			yOffset,
			zOffset,
			placedDirection,
			rotationDeg,
			skullType,
			skullOwner,
			damage
		)) {
			ci.cancel();
		}
	}
}
