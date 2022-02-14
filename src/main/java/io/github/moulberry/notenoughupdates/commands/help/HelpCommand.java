package io.github.moulberry.notenoughupdates.commands.help;

import com.google.common.collect.Lists;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.commands.ClientCommandBase;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;

import java.util.ArrayList;

public class HelpCommand extends ClientCommandBase {

    public HelpCommand() {
        super("neuhelp");
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        ArrayList<String> neuHelpMessages = Lists.newArrayList(
                "\u00a75\u00a7lNotEnoughUpdates commands",
                "\u00a76/neu \u00a77- Opens the main neu GUI.",
                "\u00a76/pv \u00a7b?{name} \u00a72\u2D35 \u00a7r\u00a77- Opens the profile viewer",
                "\u00a76/neusouls {on/off/clear/unclear} \u00a7r\u00a77- Shows waypoints to fairy souls.",
                "\u00a76/neubuttons \u00a7r\u00a77- Opens a GUI which allows you to customize inventory buttons.",
                "\u00a76/neuec \u00a7r\u00a77- Opens the enchant colour GUI.",

                "\u00a76/join {floor} \u00a7r\u00a77- Short Command to join a Dungeon. \u00a7lNeed a Party of 5 People\u00a7r\u00a77 {4/f7/m5}.",
                "\u00a76/neucosmetics \u00a7r\u00a77- Opens the cosmetic GUI.",
                "\u00a76/neurename \u00a7r\u00a77- Opens the NEU Item Customizer.",
                "\u00a76/cata \u00a7b?{name} \u00a72\u2D35 \u00a7r\u00a77- Opens the profile viewer's catacombs page.",
                "\u00a76/neulinks \u00a7r\u00a77- Shows links to neu/moulberry.",
                "\u00a76/neuoverlay \u00a7r\u00a77- Opens GUI Editor for quickcommands and searchbar.",
                "\u00a76/neuah \u00a7r\u00a77- Opens neu's custom ah GUI.",
                "\u00a76/neumap \u00a7r\u00a77- Opens the dungeon map GUI.",
                "\u00a76/neucalendar \u00a7r\u00a77- Opens neu's custom calendar GUI.",
                "",
                "\u00a76\u00a7lOld commands:",
                "\u00a76/peek \u00a7b?{user} \u00a72\u2D35 \u00a7r\u00a77- Shows quickly stats for a user.",
                "",
                "\u00a76\u00a7lDebug commands:",
                "\u00a76/neustats \u00a7r\u00a77- Copies helpful info to the clipboard.",
                "\u00a76/neustats modlist \u00a7r\u00a77- Copies modlist info to clipboard.",
                "\u00a76/neuresetrepo \u00a7r\u00a77- Deletes all repo files.",
                "\u00a76/neureloadrepo \u00a7r\u00a77- Debug command with repo.",
                "",
                "\u00a76\u00a7lDev commands:",
                "\u00a76/neupackdev \u00a7r\u00a77- pack creator command - getnpc");
        for (String neuHelpMessage : neuHelpMessages) {
            Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(neuHelpMessage));
        }
        if (NotEnoughUpdates.INSTANCE.config.hidden.dev) {
            ArrayList<String> neuDevHelpMessages = Lists.newArrayList(
                    "\u00a76/neudevtest \u00a7r\u00a77- dev test command",
                    "\u00a76/neuzeephere \u00a7r\u00a77- sphere",
                    "\u00a76/neudungeonwintest \u00a7r\u00a77- displays the dungeon win screen");

            for (String neuDevHelpMessage : neuDevHelpMessages) {
                Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(neuDevHelpMessage));
            }
        }
        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(""));
        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("\u00a77Commands marked with a \u00a72\"\u2D35\"\u00a77 require are api key. You can set your api key via \"/api new\" or by manually putting it in the api field in \"/neu\""));
        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(""));
        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("\u00a77Arguments marked with a \u00a7b\"?\"\u00a77 are optional."));
        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(""));
        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("\u00a76\u00a7lScroll up to see everything"));
    }
}
