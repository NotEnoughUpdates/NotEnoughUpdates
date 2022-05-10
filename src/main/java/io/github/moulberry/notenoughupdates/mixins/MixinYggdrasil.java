package io.github.moulberry.notenoughupdates.mixins;

import com.mojang.authlib.yggdrasil.YggdrasilMinecraftSessionService;
import io.github.moulberry.notenoughupdates.miscfeatures.ratprotection.RatProtection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(
	value = YggdrasilMinecraftSessionService.class,
	priority = Integer.MAX_VALUE,
	remap = false
)
public class MixinYggdrasil {
	@ModifyVariable(
		method = "joinServer",
		at = @At("HEAD"),
		ordinal = 0,
		argsOnly = true
	)
	private String onJoinServer(String value) {
		return RatProtection.changedToken ? RatProtection.changed : value;
	}
}
