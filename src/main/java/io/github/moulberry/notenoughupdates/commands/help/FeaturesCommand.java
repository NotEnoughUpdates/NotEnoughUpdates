package io.github.moulberry.notenoughupdates.commands.help;

import io.github.moulberry.notenoughupdates.commands.ClientCommandBase;
import io.github.moulberry.notenoughupdates.util.Constants;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.event.ClickEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class FeaturesCommand extends ClientCommandBase {
    public FeaturesCommand() {
        super("neufeatures");
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(""));
        if (Constants.MISC == null || !Constants.MISC.has("featureslist")) {
            Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("" + EnumChatFormatting.DARK_RED + EnumChatFormatting.BOLD + "WARNING: " + EnumChatFormatting.RESET + EnumChatFormatting.RED + "Could not load URL from repo."));
            Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("" + EnumChatFormatting.RED + "Please run " + EnumChatFormatting.BOLD + "/neuresetrepo" + EnumChatFormatting.RESET + EnumChatFormatting.RED + " and " + EnumChatFormatting.BOLD + "restart your game" + EnumChatFormatting.RESET + EnumChatFormatting.RED + " in order to fix. " + EnumChatFormatting.DARK_RED + EnumChatFormatting.BOLD + "If that doesn't fix it" + EnumChatFormatting.RESET + EnumChatFormatting.RED + ", please join discord.gg/moulberry and post in #neu-support"));
            Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(""));
            return;
        }
        String url = Constants.MISC.get("featureslist").getAsString();

        Desktop desk = Desktop.getDesktop();
        try {
            desk.browse(new URI(url));
            Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.DARK_PURPLE + "" + EnumChatFormatting.BOLD + "NEU" + EnumChatFormatting.RESET + EnumChatFormatting.GOLD + "> Opening Feature List in browser."));
        } catch (URISyntaxException | IOException ignored) {

            ChatComponentText clickTextFeatures = new ChatComponentText(
                    EnumChatFormatting.DARK_PURPLE + "" + EnumChatFormatting.BOLD + "NEU" + EnumChatFormatting.RESET + EnumChatFormatting.GOLD + "> Click here to open the Feature List in your browser.");
            clickTextFeatures.setChatStyle(Utils.createClickStyle(ClickEvent.Action.OPEN_URL, url));
            Minecraft.getMinecraft().thePlayer.addChatMessage(clickTextFeatures);

        }
        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(""));
    }
}
