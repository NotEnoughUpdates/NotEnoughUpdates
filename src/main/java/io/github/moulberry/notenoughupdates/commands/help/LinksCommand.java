package io.github.moulberry.notenoughupdates.commands.help;

import com.google.gson.JsonObject;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.commands.ClientCommandBase;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;

import java.io.File;

public class LinksCommand extends ClientCommandBase {

    public LinksCommand() {
        super("neulinks");
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        File repo = NotEnoughUpdates.INSTANCE.manager.repoLocation;
        if (repo.exists()) {
            File updateJson = new File(repo, "update.json");
            try {
                JsonObject update = NotEnoughUpdates.INSTANCE.manager.getJsonFromFile(updateJson);

                Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(""));
                NotEnoughUpdates.INSTANCE.displayLinks(update);
                Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(""));
            } catch (Exception ignored) {}
        }
    }
}
