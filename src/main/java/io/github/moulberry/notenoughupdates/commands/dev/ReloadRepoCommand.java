package io.github.moulberry.notenoughupdates.commands.dev;

import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.commands.ClientCommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;

public class ReloadRepoCommand extends ClientCommandBase {

	public ReloadRepoCommand() {
		super("neureloadrepo");
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) throws CommandException {
		NotEnoughUpdates.INSTANCE.manager.reloadRepository();
		sender.addChatMessage(new ChatComponentText("§e[NEU] Reloaded repository."));
	}
}
