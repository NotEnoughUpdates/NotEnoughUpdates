package io.github.moulberry.notenoughupdates.mixins;

import io.github.moulberry.notenoughupdates.miscgui.InventoryStorageSelector;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ItemRenderer.class)
public abstract class MixinItemRenderer {
    @Redirect(method = "updateEquippedItem", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/player/InventoryPlayer;getCurrentItem()Lnet/minecraft/item/ItemStack;"
    ))
    public ItemStack modifyStackToRender(InventoryPlayer player) {
        if (InventoryStorageSelector.getInstance().isSlotSelected()) {
            return InventoryStorageSelector.getInstance().getHeldItemOverride();
        }
        return player.getCurrentItem();
    }
}
