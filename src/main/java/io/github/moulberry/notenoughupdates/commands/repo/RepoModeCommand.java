package io.github.moulberry.notenoughupdates.commands.repo;

import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.commands.ClientCommandBase;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.event.ClickEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;

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
		} else if (args.length >= 2 && args[0].equalsIgnoreCase("setrepourl")) {
			String githubUser = "Moulberry";
			String githubRepo = "NotEnoughUpdates-REPO";
			String githubBranch = "master";
			if (!args[1].equalsIgnoreCase("reset")) {
				githubUser = args[1];
				if (args.length >= 3) {
					githubRepo = args[2];
				}
				if (args.length >= 4) {
					githubBranch = args[3];
				}
			}
			NotEnoughUpdates.INSTANCE.config.hidden.repoURL =
				"https://github.com/" + githubUser + "/" + githubRepo + "/archive/" + githubBranch + ".zip";
			NotEnoughUpdates.INSTANCE.config.hidden.repoCommitsURL =
				"https://api.github.com/repos/" + githubUser + "/" + githubRepo + "/commits/" + githubBranch;
			Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(
				"\u00a75Set NEU repo URL to " + NotEnoughUpdates.INSTANCE.config.hidden.repoURL +
					"\n\u00a75Set NEU repo commits URL to " + NotEnoughUpdates.INSTANCE.config.hidden.repoCommitsURL));
			Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("\u00a75Run <")
				.appendSibling(new ChatComponentText("/neureloadrepo fetch").setChatStyle(new ChatStyle().setChatClickEvent(new ClickEvent(
					ClickEvent.Action.SUGGEST_COMMAND, "/neureloadrepo fetch"))))
				.appendSibling(new ChatComponentText("\u00a75> to redownload your repo.")));

		} else if (args.length == 1 && args[0].equalsIgnoreCase("autoupdate")) {
			NotEnoughUpdates.INSTANCE.config.hidden.autoupdate = !NotEnoughUpdates.INSTANCE.config.hidden.autoupdate;
			Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("\u00a75Toggled repo auto-updating" + (NotEnoughUpdates.INSTANCE.config.hidden.autoupdate ? "on" : "off"));
		} else {
			Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("\u00a7cUsage:" +
				"\n\u00a75/neurepomode <toggle> Toggles on/off dev mode and item editing." +
				"\n\u00a75/neurepomode <setRepoURL> <githubuser> [reponame] [branch] Sets the repo URL for downloading from." +
				"\n\u00a75/neurepomode <autoupdate> Toggles on/off auto-updating the repo"));
		}
	}
}
