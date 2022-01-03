package io.github.moulberry.notenoughupdates.miscfeatures.CustomBiomeTextures;

import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import net.minecraft.util.BlockPos;
import net.minecraft.world.biome.*;


public class CrystalHollowsTextures {
    //Biome Prefix: NeuAreaBiomeName
    //Example: NeuCHJungle


    public static BiomeGenBase retexture(BlockPos pos, String location) {
        if(!NotEnoughUpdates.INSTANCE.config.mining.crystalHollowTextures) return null;
        if (pos.getY() < 65) {
            return NotEnoughUpdates.crystalHollowsMagmaFields;
        } else if (pos.getX() < 565 && pos.getX() > 461 && pos.getZ() < 566 && pos.getZ() > 460 && pos.getY() > 64) {
            return NotEnoughUpdates.crystalHollowsCrystalNucleus;
        } else if (pos.getX() < 513 && pos.getZ() < 513 && pos.getY() > 64) {
            return NotEnoughUpdates.crystalHollowsJungle;
        } else if (pos.getX() < 513 && pos.getZ() > 512 && pos.getY() > 64) {
            return NotEnoughUpdates.crystalHollowsGoblinHoldout;
        } else if (pos.getX() > 512 && pos.getZ() < 513 && pos.getY() > 64) {
            return NotEnoughUpdates.crystalHollowsMithrilDeposit;
        } else if (pos.getX() > 512 && pos.getZ() > 512 && pos.getY() > 64) {
            return NotEnoughUpdates.crystalHollowsPrecursorRemnants;
        }
            return null;

    }
}
