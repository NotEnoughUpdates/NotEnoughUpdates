package io.github.moulberry.notenoughupdates.mixins;

import io.github.moulberry.notenoughupdates.miscfeatures.EnchantingSolvers;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Container.class)
public class MixinContainer {
	@Inject(method = "putStacksInSlots", at = @At("RETURN"))
	public void putStacksInSlots(ItemStack[] stacks, CallbackInfo ci) {
		EnchantingSolvers.processInventoryContents(false);
	}
}
