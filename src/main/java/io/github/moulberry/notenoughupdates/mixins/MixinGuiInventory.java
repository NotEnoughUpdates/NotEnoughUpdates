package io.github.moulberry.notenoughupdates.mixins;

import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.listener.RenderListener;
import net.minecraft.client.gui.inventory.GuiInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiInventory.class)
public class MixinGuiInventory {
    @Inject(method = "drawGuiContainerForegroundLayer", at = @At("HEAD"), cancellable = true)
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY, CallbackInfo ci) {
        if (NotEnoughUpdates.INSTANCE.config.inventoryButtons.hideCrafting ||
                RenderListener.disableCraftingText) {
            ci.cancel();
        }
    }
}
