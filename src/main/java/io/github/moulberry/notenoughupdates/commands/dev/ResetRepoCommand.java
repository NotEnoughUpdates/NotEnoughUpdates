package io.github.moulberry.notenoughupdates.commands.dev;

import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.commands.ClientCommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;

public class ResetRepoCommand extends ClientCommandBase {

	public ResetRepoCommand() {
		super("neuresetrepo");
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) throws CommandException {
		NotEnoughUpdates.INSTANCE.manager
			.userFacingRepositoryReload()
			.thenAccept(strings ->
				strings.forEach(line ->
					sender.addChatMessage(new ChatComponentText("§e[NEU] " + line))));
	}
}
