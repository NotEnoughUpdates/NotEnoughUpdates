package io.github.moulberry.notenoughupdates.mixins;

import net.minecraft.entity.item.EntityArmorStand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(EntityArmorStand.class)
public interface AccessorEntityArmorStand {
    @Invoker(value = "setSmall")
    void setSmallDirect(boolean isSmall);

}
