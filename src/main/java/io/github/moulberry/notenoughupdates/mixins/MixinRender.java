package io.github.moulberry.notenoughupdates.mixins;

import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.dungeons.DungeonBlocks;
import io.github.moulberry.notenoughupdates.util.SpecialColour;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Render.class)
public class MixinRender {
    @Inject(method = "bindEntityTexture", at = @At("HEAD"), cancellable = true)
    public void bindEntityTexture(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (entity instanceof EntityBat && DungeonBlocks.isOverriding()) {
            if (DungeonBlocks.bindModifiedTexture(new ResourceLocation("textures/entity/bat.png"),
                    SpecialColour.specialToChromaRGB(NotEnoughUpdates.INSTANCE.config.dungeons.dungBatColour))) {
                cir.setReturnValue(true);
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST_MIPMAP_LINEAR);
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
            }
        }
    }
}
