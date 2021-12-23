package io.github.moulberry.notenoughupdates.mixins;

import io.github.moulberry.notenoughupdates.core.ChromaColour;
import io.github.moulberry.notenoughupdates.miscfeatures.ItemCustomizeManager;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.layers.LayerArmorBase;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LayerArmorBase.class)
public abstract class MixinLayerArmorBase<T extends ModelBase> {
    private static String customEnchGlint = null;

    @Redirect(method = "renderLayer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/item/ItemStack;hasEffect()Z"
            )
    )
    public boolean renderItem_hasEffect(ItemStack stack) {
        ItemCustomizeManager.ItemData data = ItemCustomizeManager.getDataForItem(stack);
        if (data != null) {
            customEnchGlint = data.customGlintColour;
            if (data.overrideEnchantGlint) {
                return data.enchantGlintValue;
            }
        } else {
            customEnchGlint = null;
        }

        return stack.hasEffect();
    }

    @Inject(method = "renderGlint", at = @At("HEAD"), cancellable = true)
    public void renderGlint(EntityLivingBase entitylivingbaseIn, T modelbaseIn, float p_177183_3_, float p_177183_4_,
                            float partialTicks, float p_177183_6_, float p_177183_7_, float p_177183_8_, float scale, CallbackInfo ci) {
        float existed = (float) entitylivingbaseIn.ticksExisted + partialTicks;
        if (ItemCustomizeManager.render3DGlint(customEnchGlint, existed, () ->
                modelbaseIn.render(entitylivingbaseIn, p_177183_3_, p_177183_4_, p_177183_6_, p_177183_7_, p_177183_8_, scale))) {
            ci.cancel();
        }
    }

    @Redirect(method = "renderLayer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/item/ItemArmor;getColor(Lnet/minecraft/item/ItemStack;)I"
            )
    )
    public int renderItem_getColor(ItemArmor item, ItemStack stack) {
        ItemCustomizeManager.ItemData data = ItemCustomizeManager.getDataForItem(stack);
        if (data != null && data.customLeatherColour != null) {
            return ChromaColour.specialToChromaRGB(data.customLeatherColour);
        }

        return item.getColor(stack);
    }
}
