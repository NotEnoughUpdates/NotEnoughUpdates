package io.github.moulberry.notenoughupdates.miscfeatures.CustomBiomeTextures;

import net.minecraft.util.BlockPos;

public interface IslandZoneSubdivider {
    SpecialBlockZone getSpecialZoneForBlock(String location, BlockPos position);
}
