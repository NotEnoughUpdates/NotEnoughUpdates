package io.github.moulberry.notenoughupdates.commands.dev;

import io.github.moulberry.notenoughupdates.commands.ClientCommandBase;
import io.github.moulberry.notenoughupdates.miscfeatures.NullzeeSphere;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

public class NullzeeSphereCommand extends ClientCommandBase {

    public NullzeeSphereCommand() {
        super("neuzeesphere");
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        if (args.length != 1) {
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Usage: /neuzeesphere [on/off] or /neuzeesphere (radius) or /neuzeesphere setCenter"));
            return;
        }
        if (args[0].equalsIgnoreCase("on")) {
            NullzeeSphere.enabled = true;
        } else if (args[0].equalsIgnoreCase("off")) {
            NullzeeSphere.enabled = false;
        } else if (args[0].equalsIgnoreCase("setCenter")) {
            EntityPlayerSP p = ((EntityPlayerSP) sender);
            NullzeeSphere.centerPos = new BlockPos(p.posX, p.posY, p.posZ);
            NullzeeSphere.overlayVBO = null;
        } else {
            try {
                NullzeeSphere.size = Float.parseFloat(args[0]);
                NullzeeSphere.overlayVBO = null;
            } catch (Exception e) {
                sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Can't parse radius: " + args[0]));
            }
        }
    }
}
