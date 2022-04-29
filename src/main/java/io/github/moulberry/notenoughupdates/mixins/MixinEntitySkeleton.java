package io.github.moulberry.notenoughupdates.mixins;

import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntitySkeleton.class)
public class MixinEntitySkeleton {
    @Redirect(method = "setCurrentItemOrArmor", at = @At(value = "FIELD", target = "Lnet/minecraft/world/World;isRemote:Z"))
    public boolean onSetCurrentItemOrArmor(World instance) {
        if (instance == null)
            return true;
        return instance.isRemote;
    }
}
