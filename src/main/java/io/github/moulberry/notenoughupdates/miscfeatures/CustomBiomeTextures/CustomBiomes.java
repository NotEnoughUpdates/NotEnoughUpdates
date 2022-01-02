package io.github.moulberry.notenoughupdates.miscfeatures.CustomBiomeTextures;

import io.github.moulberry.notenoughupdates.util.SBInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.util.BlockPos;
import net.minecraft.world.biome.BiomeGenBase;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class CustomBiomes {

    //Biome Prefix: NeuAreaBiomeName
    //Example: NeuCHJungle

    public static BiomeGenBase getCustomBiome(BlockPos pos){
        if (Minecraft.getMinecraft().theWorld == null) return null;
        String location = SBInfo.getInstance().getLocation();

        if (location == null) return null;
        switch (location.intern()){
            case "crystal_hollows":
                return CrystalHollowsTextures.retexture(pos, location);
            case "mining_3":
                return DwarvenMinesTextures.retexture(pos, location);
            default:
                return null;
        }
    }

    @SubscribeEvent
    public static void ChangeLocationEvent(ChangeLocationEvent event){
        if (Minecraft.getMinecraft().theWorld == null) return;
        String location = event.newLocation;

        if (location == null) return;
        switch (location.intern()){
            case "crystal_hollows":
            case "mining_3":
                //if has custom biome, do chunk update or something
        }
    }

}
