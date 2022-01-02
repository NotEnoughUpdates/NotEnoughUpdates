package io.github.moulberry.notenoughupdates.miscfeatures.CustomBiomeTextures;

import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import net.minecraft.util.BlockPos;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.BiomeGenHell;
import net.minecraft.world.biome.BiomeGenJungle;


public class CrystalHollowsTextures {
    //Biome Prefix: NeuAreaBiomeName
    //Example: NeuCHJungle

    public static final BiomeGenBase crystalHollowsJungle = (new BiomeGenJungle(101, false)).setColor(5470985).setBiomeName("NeuCHJungle").setFillerBlockMetadata(5470985).setTemperatureRainfall(0.95F, 0.9F);

    public static BiomeGenBase retexture(BlockPos pos, String location) {
        if(!NotEnoughUpdates.INSTANCE.config.mining.crystalHollowTextures) return null;
        return crystalHollowsJungle;

    }
}
