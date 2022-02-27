package io.github.moulberry.notenoughupdates.mixins;

import io.github.moulberry.notenoughupdates.miscfeatures.FishingHelper;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldClient.class)
public class MixinWorldClient {
	@Inject(method = "addEntityToWorld", at = @At("HEAD"))
	public void addEntityToWorld(int entityID, Entity entityToSpawn, CallbackInfo ci) {
		FishingHelper.getInstance().addEntity(entityID, entityToSpawn);
	}

	@Inject(method = "removeEntityFromWorld", at = @At("RETURN"))
	public void removeEntityFromWorld(int entityID, CallbackInfoReturnable<Entity> cir) {
		FishingHelper.getInstance().removeEntity(entityID);
	}
}
