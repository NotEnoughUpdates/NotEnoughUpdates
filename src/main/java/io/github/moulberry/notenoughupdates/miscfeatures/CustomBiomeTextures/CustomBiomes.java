package io.github.moulberry.notenoughupdates.miscfeatures.CustomBiomeTextures;

import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.events.OnBlockBreakSoundEffect;
import io.github.moulberry.notenoughupdates.util.SBInfo;
import net.minecraft.block.BlockColored;
import net.minecraft.block.BlockStone;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.BlockPos;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;

public class CustomBiomes {

    public static final CustomBiomes INSTANCE = new CustomBiomes();

    private Map<String, IslandZoneSubdivider> subdividers = new HashMap<>();

    private CustomBiomes() {
        subdividers.put("crystal_hollows", new CrystalHollowsTextures());
        subdividers.put("mining_3", new DwarvenMinesTextures());
    }

    //Biome Prefix: NeuAreaBiomeName
    //Example: NeuCHJungle

    public BiomeGenBase getCustomBiome(BlockPos pos) {
        SpecialBlockZone specialZone = getSpecialZone(pos);
        if (specialZone != null) {
            if ((specialZone.isDwarvenMines() && NotEnoughUpdates.INSTANCE.config.mining.dwarvenTextures)
                    || (specialZone.isCrystalHollows() && NotEnoughUpdates.INSTANCE.config.mining.crystalHollowTextures))
                return specialZone.getCustomBiome();
        }
        return null;
    }

    /**
     * Finds the special zone for the give block position
     * <p>
     * Returns null on error
     */
    public SpecialBlockZone getSpecialZone(BlockPos pos) {
        if (Minecraft.getMinecraft().theWorld == null) return null;
        String location = SBInfo.getInstance().getLocation();
        IslandZoneSubdivider subdivider = subdividers.get(location);
        if (subdivider == null) return SpecialBlockZone.NON_SPECIAL_ZONE;
        return subdivider.getSpecialZoneForBlock(location, pos);
    }


    @SubscribeEvent
    public void onBreakSound(OnBlockBreakSoundEffect event) {
        SpecialBlockZone specialZone = getSpecialZone(event.getPosition());
        boolean hasMithrilSounds = NotEnoughUpdates.INSTANCE.config.mining.mithrilSounds;
        boolean hasCrystalSounds = NotEnoughUpdates.INSTANCE.config.mining.gemstoneSounds;
        if (specialZone != null) {
            if (specialZone.hasMithril() && isMithril(event.getBlock()) && hasMithrilSounds) {
                event.setSound(CustomBlockSounds.replaceSoundEvent(event.getSound(), CustomBlockSounds.mithrilBreak));
            }
            if (specialZone.hasTitanium() && isTitanium(event.getBlock()) && hasMithrilSounds) {
                event.setSound(CustomBlockSounds.replaceSoundEvent(event.getSound(), CustomBlockSounds.titaniumBreak));
            }
            if (specialZone.hasGemstones() && isGemstone(event.getBlock()) && hasCrystalSounds) {
                event.setSound(CustomBlockSounds.replaceSoundEvent(event.getSound(), CustomBlockSounds.gemstoneBreak));
            }
        }
    }

    public static boolean isTitanium(IBlockState state) {
        return state.getBlock() == Blocks.stone && state.getValue(BlockStone.VARIANT) == BlockStone.EnumType.DIORITE_SMOOTH;
    }

    public static boolean isMithril(IBlockState state) {
        return (state.getBlock() == Blocks.stained_hardened_clay && state.getValue(BlockColored.COLOR) == EnumDyeColor.CYAN)
                || (state.getBlock() == Blocks.wool && state.getValue(BlockColored.COLOR) == EnumDyeColor.GRAY)
                || state.getBlock() == Blocks.prismarine
                || state.getBlock() == Blocks.bedrock;
    }

    public static boolean isGemstone(IBlockState state) {
        return state.getBlock() == Blocks.stained_glass || state.getBlock() == Blocks.stained_glass_pane;
    }

    @SubscribeEvent
    public void onLocationChange(LocationChangeEvent event) {
        WorldClient world = Minecraft.getMinecraft().theWorld;
        String location = event.newLocation;
        if (world == null) return;
        if (location == null) return;
        switch (location.intern()) {
            case "crystal_hollows":
            case "mining_3":
                //if has custom biome, do chunk update or something
                EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
                if (player == null) return;

                world.markBlocksDirtyVertical((int) player.posX, (int) player.posX, (int) player.posZ, (int) player.posZ);
        }
    }


}
