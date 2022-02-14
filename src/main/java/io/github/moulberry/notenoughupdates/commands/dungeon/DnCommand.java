package io.github.moulberry.notenoughupdates.commands.dungeon;

import io.github.moulberry.notenoughupdates.commands.ClientCommandBase;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

public class DnCommand extends ClientCommandBase {

    public DnCommand() {
        super("dn");
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        Minecraft.getMinecraft().thePlayer.sendChatMessage("/warp dungeon_hub");
        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.AQUA + "Warping to:" + EnumChatFormatting.YELLOW + " Deez Nuts lmao"));
    }
}
