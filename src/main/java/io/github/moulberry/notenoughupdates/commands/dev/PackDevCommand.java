package io.github.moulberry.notenoughupdates.commands.dev;

import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.commands.ClientCommandBase;
import io.github.moulberry.notenoughupdates.core.util.MiscUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

public class PackDevCommand extends ClientCommandBase {

    public PackDevCommand() {
        super("neupackdev");
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 1 && args[0].equalsIgnoreCase("getnpc")) {
            double distSq = 25;
            EntityPlayer closestNPC = null;
            EntityPlayerSP p = Minecraft.getMinecraft().thePlayer;
            for (EntityPlayer player : Minecraft.getMinecraft().theWorld.playerEntities) {
                if (player instanceof AbstractClientPlayer && p != player && player.getUniqueID().version() != 4) {
                    double dSq = player.getDistanceSq(p.posX, p.posY, p.posZ);
                    if (dSq < distSq) {
                        distSq = dSq;
                        closestNPC = player;
                    }
                }
            }

            if (closestNPC == null) {
                Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "No NPCs found within 5 blocks :("));
            } else {
                Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN + "Copied entity texture id to clipboard"));
                MiscUtils.copyToClipboard(((AbstractClientPlayer) closestNPC).getLocationSkin().getResourcePath().replace("skins/", ""));
            }
            return;
        }
        NotEnoughUpdates.INSTANCE.packDevEnabled = !NotEnoughUpdates.INSTANCE.packDevEnabled;
        if (NotEnoughUpdates.INSTANCE.packDevEnabled) {
            Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN + "Enabled pack developer mode."));
        } else {
            Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Disabled pack developer mode."));
        }
    }
}
