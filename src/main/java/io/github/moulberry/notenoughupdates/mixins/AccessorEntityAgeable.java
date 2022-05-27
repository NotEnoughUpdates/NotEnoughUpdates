package io.github.moulberry.notenoughupdates.mixins;

import net.minecraft.entity.EntityAgeable;
import net.minecraft.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(EntityAgeable.class)
public interface AccessorEntityAgeable {
		@Accessor(value = "growingAge")
		void setGrowingAgeDirect(int newValue);

}
