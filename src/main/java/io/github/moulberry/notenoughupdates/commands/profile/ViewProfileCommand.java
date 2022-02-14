package io.github.moulberry.notenoughupdates.commands.profile;

import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.commands.ClientCommandBase;
import io.github.moulberry.notenoughupdates.profileviewer.GuiProfileViewer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ViewProfileCommand extends ClientCommandBase {

    public static final Consumer<String[]> RUNNABLE = (args) -> {
        if (!OpenGlHelper.isFramebufferEnabled()) {
            Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.RED +
                    "Some parts of the profile viewer do not work with OF Fast Render. Go to ESC > Options > Video Settings > Performance > Fast Render to disable it."));

        }
        if (NotEnoughUpdates.INSTANCE.config.apiKey.apiKey == null || NotEnoughUpdates.INSTANCE.config.apiKey.apiKey.trim().isEmpty()) {
            Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.RED +
                    "Can't view profile, apikey is not set. Run /api new and put the result in settings."));
        } else if (args.length == 0) {
            NotEnoughUpdates.profileViewer.getProfileByName(Minecraft.getMinecraft().thePlayer.getName(), profile -> {
                if (profile == null) {
                    Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.RED +
                            "Invalid player name/api key. Maybe api is down? Try /api new."));
                } else {
                    profile.resetCache();
                    NotEnoughUpdates.INSTANCE.openGui = new GuiProfileViewer(profile);
                }
            });
        } else if (args.length > 1) {
            Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.RED +
                    "Too many arguments. Usage: /neuprofile [name]"));
        } else {
            NotEnoughUpdates.profileViewer.getProfileByName(args[0], profile -> {
                if (profile == null) {
                    Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.RED +
                            "Invalid player name/api key. Maybe api is down? Try /api new."));
                } else {
                    profile.resetCache();
                    NotEnoughUpdates.INSTANCE.openGui = new GuiProfileViewer(profile);
                }
            });
        }
    };

    public ViewProfileCommand() {
        this("neuprofile");
    }


    public ViewProfileCommand(String name) {
        super(name);
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        RUNNABLE.accept(args);
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        if (args.length != 1) return null;

        String lastArg = args[args.length - 1];
        List<String> playerMatches = new ArrayList<>();
        for (EntityPlayer player : Minecraft.getMinecraft().theWorld.playerEntities) {
            String playerName = player.getName();
            if (playerName.toLowerCase().startsWith(lastArg.toLowerCase())) {
                playerMatches.add(playerName);
            }
        }
        return playerMatches;
    }
}
