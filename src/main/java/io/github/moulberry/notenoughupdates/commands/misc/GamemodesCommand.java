package io.github.moulberry.notenoughupdates.commands.misc;

import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.commands.ClientCommandBase;
import io.github.moulberry.notenoughupdates.gamemodes.GuiGamemodes;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

public class GamemodesCommand extends ClientCommandBase {

	public GamemodesCommand() {
		super("neugamemodes");
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) throws CommandException {
		boolean upgradeOverride = args.length == 1 && args[0].equals("upgradeOverride");
		NotEnoughUpdates.INSTANCE.openGui = new GuiGamemodes(upgradeOverride);
	}
}
