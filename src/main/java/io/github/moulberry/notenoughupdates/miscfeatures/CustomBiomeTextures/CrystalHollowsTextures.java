package io.github.moulberry.notenoughupdates.miscfeatures.CustomBiomeTextures;

import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import net.minecraft.util.BlockPos;
import net.minecraft.world.biome.*;


public class CrystalHollowsTextures {
    //Biome Prefix: NeuAreaBiomeName
    //Example: NeuCHJungle

    public static final BiomeGenBase crystalHollowsJungle = (new BiomeGenJungle(101, true)).setColor(5470985).setBiomeName("NeuCrystalHollowsJungle").setFillerBlockMetadata(5470985).setTemperatureRainfall(0.95F, 0.9F);
    public static final BiomeGenBase crystalHollowsMagmaFields = (new BiomeGenHell(102)).setColor(16711680).setBiomeName("NeuCrystalHollowsMagmaFields").setDisableRain().setTemperatureRainfall(2.0F, 0.0F);
    public static final BiomeGenBase crystalHollowsGoblinHoldout = (new BiomeGenMesa(103, false, false)).setColor(13274213).setBiomeName("NeuCrystalHollowsGoblinHoldout");
    public static final BiomeGenBase crystalHollowsPrecursorRemnants = (new BiomeGenMesa(104, false, true)).setColor(11573093).setBiomeName("NeuCrystalHollowsPrecursorRemnants");
    public static final BiomeGenBase crystalHollowsMithrilDeposit = (new BiomeGenSnow(105, false)).setColor(16777215).setBiomeName("NeuCrystalHollowsMithrilDeposits");
    public static final BiomeGenBase crystalHollowsCrystalNucleus = (new BiomeGenJungle(106, true)).setColor(5470985).setBiomeName("NeuCrystalHollowsCrystalNucleus").setFillerBlockMetadata(5470985).setTemperatureRainfall(0.95F, 0.9F);

    public static BiomeGenBase retexture(BlockPos pos, String location) {
        if(!NotEnoughUpdates.INSTANCE.config.mining.crystalHollowTextures) return null;
        if (pos.getY() < 65) {
            return crystalHollowsMagmaFields;
        } else if (pos.getX() < 565 && pos.getX() > 461 && pos.getZ() < 566 && pos.getZ() > 460 && pos.getY() > 64) {
            return crystalHollowsCrystalNucleus;
        } else if (pos.getX() < 513 && pos.getZ() < 513 && pos.getY() > 64) {
            return crystalHollowsJungle;
        } else if (pos.getX() < 513 && pos.getZ() > 512 && pos.getY() > 64) {
            return crystalHollowsGoblinHoldout;
        } else if (pos.getX() > 512 && pos.getZ() < 513 && pos.getY() > 64) {
            return crystalHollowsMithrilDeposit;
        } else if (pos.getX() > 512 && pos.getZ() > 512 && pos.getY() > 64) {
            return crystalHollowsPrecursorRemnants;
        }
            return null;

    }
}
