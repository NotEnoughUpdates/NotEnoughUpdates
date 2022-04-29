package io.github.moulberry.notenoughupdates.commands.dungeon;

import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.commands.ClientCommandBase;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import org.apache.commons.lang3.StringUtils;

public class JoinDungeonCommand extends ClientCommandBase {

	public JoinDungeonCommand() {
		super("join");
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) throws CommandException {
		if (!NotEnoughUpdates.INSTANCE.hasSkyblockScoreboard()) {
			Minecraft.getMinecraft().thePlayer.sendChatMessage("/join " + StringUtils.join(args, " "));
		} else {
			if (args.length != 1) {
				Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(
					EnumChatFormatting.RED + "Example Usage: /join f7, /join m6 or /join 7"));
			} else {
				String cataPrefix = "catacombs";
				if (args[0].startsWith("m")) {
					cataPrefix = "master_catacombs";
				}
				String cmd = "/joindungeon " + cataPrefix + " " + args[0].charAt(args[0].length() - 1);
				Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(
					EnumChatFormatting.YELLOW + "Running command: " + cmd));
				Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(
					EnumChatFormatting.YELLOW +
						"The dungeon should start soon. If it doesn't, make sure you have a party of 5 people"));
				Minecraft.getMinecraft().thePlayer.sendChatMessage(cmd);
			}
		}
	}
}
