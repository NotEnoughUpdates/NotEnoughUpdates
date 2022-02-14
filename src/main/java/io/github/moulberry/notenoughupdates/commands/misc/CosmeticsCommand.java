package io.github.moulberry.notenoughupdates.commands.misc;

import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.commands.ClientCommandBase;
import io.github.moulberry.notenoughupdates.cosmetics.GuiCosmetics;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

public class CosmeticsCommand extends ClientCommandBase {

    public CosmeticsCommand() {
        super("neucosmetics");
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        if (!OpenGlHelper.isFramebufferEnabled()) {
            Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.RED +
                    "NEU cosmetics do not work with OF Fast Render. Go to ESC > Options > Video Settings > Performance > Fast Render to disable it."));

        }

        NotEnoughUpdates.INSTANCE.openGui = new GuiCosmetics();
    }
}
