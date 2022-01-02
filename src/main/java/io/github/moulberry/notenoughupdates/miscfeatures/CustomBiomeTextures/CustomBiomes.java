package io.github.moulberry.notenoughupdates.miscfeatures.CustomBiomeTextures;

import io.github.moulberry.notenoughupdates.util.SBInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.biome.BiomeGenBase;

import net.minecraftforge.event.entity.player.ArrowNockEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class CustomBiomes {

    public static final CustomBiomes INSTANCE = new CustomBiomes();

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
    public void LocationChangeEvent(LocationChangeEvent event){
        String location = event.newLocation;
        WorldClient world = Minecraft.getMinecraft().theWorld;
        if (world == null) return;


        if (location == null) return;
        switch (location.intern()){
            case "crystal_hollows":
            case "mining_3":
                //if has custom biome, do chunk update or something
                EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
                if(player == null) return;


                world.markBlocksDirtyVertical((int)player.posX, (int)player.posX, (int)player.posZ, (int)player.posZ);


        }
    }



}
