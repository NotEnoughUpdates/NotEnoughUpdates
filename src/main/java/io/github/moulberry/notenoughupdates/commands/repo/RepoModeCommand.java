package io.github.moulberry.notenoughupdates.commands.repo;

import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.commands.ClientCommandBase;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;

public class RepoModeCommand extends ClientCommandBase {

	public RepoModeCommand() {
		super("neurepomode");
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) throws CommandException {
		if (args.length == 1 && args[0].equalsIgnoreCase("toggle")) {
			NotEnoughUpdates.INSTANCE.config.hidden.dev = !NotEnoughUpdates.INSTANCE.config.hidden.dev;
			NotEnoughUpdates.INSTANCE.config.hidden.enableItemEditing =
				!NotEnoughUpdates.INSTANCE.config.hidden.enableItemEditing;
			Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("\u00a75Toggled NEU repo dev mode."));
		} else if (args.length == 2 && args[0].equalsIgnoreCase("setrepourl")) {
			if (args[1].equalsIgnoreCase("reset")) {
				NotEnoughUpdates.INSTANCE.config.hidden.repoURL = "https://github.com/Moulberry/NotEnoughUpdates-REPO/archive/master.zip";
				Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(
					"\u00a75You reset the repo URL to " + NotEnoughUpdates.INSTANCE.config.hidden.repoURL));
			} else {
				NotEnoughUpdates.INSTANCE.config.hidden.repoURL = args[1];
				Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(
					"\u00a75You set the repo URL to " + NotEnoughUpdates.INSTANCE.config.hidden.repoURL));
			}

		} else if (args.length == 2 && args[0].equalsIgnoreCase("setcommitsurl")) {
			if (args[1].equalsIgnoreCase("reset")) {
				NotEnoughUpdates.INSTANCE.config.hidden.repoCommitsURL = "https://api.github.com/repos/Moulberry/NotEnoughUpdates-REPO/commits/master";
				Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(
					"\u00a75You reset the commits URL to " + NotEnoughUpdates.INSTANCE.config.hidden.repoCommitsURL));
			} else {
				NotEnoughUpdates.INSTANCE.config.hidden.repoCommitsURL = args[1];
				Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(
					"\u00a75You set the commits URL to " + NotEnoughUpdates.INSTANCE.config.hidden.repoCommitsURL));
			}
		} else {
			Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("\u00a7cUsage:" +
				"\n\u00a75/neurepomode <toggle> Toggles on/off dev mode and item editing." +
				"\n\u00a75/neurepomode <setRepoURL> [reset] Sets the repo URL for downloading from." +
				"\n\u00a75/neurepomode <setCommitsUrl> [reset] Sets the commits URL."));
		}
	}
}
