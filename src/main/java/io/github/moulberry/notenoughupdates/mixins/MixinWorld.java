package io.github.moulberry.notenoughupdates.mixins;

import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.miscfeatures.CustomItemEffects;
import io.github.moulberry.notenoughupdates.miscfeatures.DwarvenMinesTextures;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(World.class)
public class MixinWorld {
    @Inject(method = "spawnParticle(IZDDDDDD[I)V", at = @At("HEAD"), cancellable = true)
    public void spawnParticle(int particleID, boolean p_175720_2_, double xCood, double yCoord, double zCoord,
                              double xOffset, double yOffset, double zOffset, int[] p_175720_15_, CallbackInfo ci) {
        if (NotEnoughUpdates.INSTANCE.config.itemOverlays.disableHyperionParticles &&
                System.currentTimeMillis() - CustomItemEffects.INSTANCE.lastUsedHyperion < 500) {
            if (particleID == 1) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "getBiomeGenForCoords", at = @At("HEAD"), cancellable = true)
    public void getBiomeGenForCoords(BlockPos pos, CallbackInfoReturnable<BiomeGenBase> cir) {
        int retexture = DwarvenMinesTextures.retexture(pos);
        if (retexture == 1) {
            cir.setReturnValue(BiomeGenBase.extremeHillsPlus);
        } else if (retexture == 2) {
            cir.setReturnValue(BiomeGenBase.extremeHillsEdge);
        } else if (retexture == 3) {
            cir.setReturnValue(BiomeGenBase.coldBeach);
        }
    }
}
