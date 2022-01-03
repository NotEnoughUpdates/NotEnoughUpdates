package io.github.moulberry.notenoughupdates.miscfeatures.CustomBiomeTextures;

import net.minecraft.util.BlockPos;

public class CrystalHollowsTextures implements IslandZoneSubdivider {
    public SpecialBlockZone getSpecialZoneForBlock(String location, BlockPos pos) {
        if (pos.getY() < 65) {
            return SpecialBlockZone.CRYSTAL_HOLLOWS_MAGMA_FIELDS;
        } else if (pos.getX() < 565 && pos.getX() > 461 && pos.getZ() < 566 && pos.getZ() > 460 && pos.getY() > 64) {
            return SpecialBlockZone.CRYSTAL_HOLLOWS_NUCLEUS;
        } else if (pos.getX() < 513 && pos.getZ() < 513 && pos.getY() > 64) {
            return SpecialBlockZone.CRYSTAL_HOLLOWS_JUNGLE;
        } else if (pos.getX() < 513 && pos.getZ() > 512 && pos.getY() > 64) {
            return SpecialBlockZone.CRYSTAL_HOLLOWS_GOBLIN_HIDEOUT;
        } else if (pos.getX() > 512 && pos.getZ() < 513 && pos.getY() > 64) {
            return SpecialBlockZone.CRYSTAL_HOLLOWS_MITHRIL_DEPOSIT;
        } else if (pos.getX() > 512 && pos.getZ() > 512 && pos.getY() > 64) {
            return SpecialBlockZone.CRYSTAL_HOLLOWS_PRECURSOR_REMNANTS;
        }
        return null;
    }
}
