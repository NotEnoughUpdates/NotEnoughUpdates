package io.github.moulberry.notenoughupdates.commands;

import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

public class ScreenCommand extends ClientCommandBase {

	private final ScreenOpener opener;

	protected ScreenCommand(String name, ScreenOpener opener) {
		super(name);
		this.opener = opener;
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) throws CommandException {
		NotEnoughUpdates.INSTANCE.openGui = opener.open();
	}

	@FunctionalInterface
	public interface ScreenOpener {
		GuiScreen open();
	}
}
