package io.github.moulberry.notenoughupdates.commands.dungeon;

import io.github.moulberry.notenoughupdates.commands.ClientCommandBase;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

public class DhCommand extends ClientCommandBase {

	public DhCommand() {
		super("dh");
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) throws CommandException {
		Minecraft.getMinecraft().thePlayer.sendChatMessage("/warp dungeon_hub");
	}
}
