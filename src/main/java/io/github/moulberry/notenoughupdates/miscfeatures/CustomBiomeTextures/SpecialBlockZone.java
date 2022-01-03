package io.github.moulberry.notenoughupdates.miscfeatures.CustomBiomeTextures;

import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import net.minecraft.world.biome.BiomeGenBase;

public enum SpecialBlockZone {
    DWARVEN_MINES_NON_MITHRIL(BiomeGenBase.extremeHillsPlus, false, false, false),
    DWARVEN_MINES_MITHRIL(BiomeGenBase.extremeHillsEdge, true, true, false),
    CRYSTAL_HOLLOWS_MAGMA_FIELDS(NotEnoughUpdates.crystalHollowsMagmaFields, true, false, true),
    CRYSTAL_HOLLOWS_NUCLEUS(NotEnoughUpdates.crystalHollowsCrystalNucleus, true, false, true),
    CRYSTAL_HOLLOWS_JUNGLE(NotEnoughUpdates.crystalHollowsJungle, true, false, true),
    CRYSTAL_HOLLOWS_GOBLIN_HIDEOUT(NotEnoughUpdates.crystalHollowsGoblinHoldout, true, false, true),
    CRYSTAL_HOLLOWS_MITHRIL_DEPOSIT(NotEnoughUpdates.crystalHollowsMithrilDeposit, true, false, true),
    CRYSTAL_HOLLOWS_PRECURSOR_REMNANTS(NotEnoughUpdates.crystalHollowsPrecursorRemnants, true, false, true),
    NON_SPECIAL_ZONE(null, false, false, false);

    private final BiomeGenBase customBiome;
    private final boolean hasMithril;
    private final boolean hasTitanium;
    private final boolean hasGemstones;

    SpecialBlockZone(BiomeGenBase customBiome, boolean hasMithril, boolean hasTitanium, boolean hasGemstones) {
        this.customBiome = customBiome;
        this.hasMithril = hasMithril;
        this.hasTitanium = hasTitanium;
        this.hasGemstones = hasGemstones;
    }

    public BiomeGenBase getCustomBiome() {
        return customBiome;
    }

    public boolean hasMithril() {
        return hasMithril;
    }

    public boolean hasTitanium() {
        return hasTitanium;
    }

    public boolean hasGemstones() {
        return hasGemstones;
    }
}
