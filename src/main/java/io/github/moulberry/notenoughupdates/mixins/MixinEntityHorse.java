package io.github.moulberry.notenoughupdates.mixins;

import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntityHorse.class)
public class MixinEntityHorse {
    @Redirect(method = "updateHorseSlots", at = @At(value = "FIELD", target = "Lnet/minecraft/world/World;isRemote:Z"))
    public boolean onUpdateHorseSlots(World instance) {
        if (instance == null)
            return true;
        return instance.isRemote;
    }
}
