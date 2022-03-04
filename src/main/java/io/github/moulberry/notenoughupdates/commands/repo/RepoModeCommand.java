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
		NotEnoughUpdates.INSTANCE.config.hidden.dev = !NotEnoughUpdates.INSTANCE.config.hidden.dev;
		NotEnoughUpdates.INSTANCE.config.hidden.enableItemEditing =
			!NotEnoughUpdates.INSTANCE.config.hidden.enableItemEditing;
		Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("\u00a75Toggled NEU repo dev mode."));
	}
}
