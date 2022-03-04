package io.github.moulberry.notenoughupdates.commands.misc;

import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.commands.ClientCommandBase;
import io.github.moulberry.notenoughupdates.miscgui.CalendarOverlay;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

public class CalendarCommand extends ClientCommandBase {

	public CalendarCommand() {
		super("neucalendar");
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) throws CommandException {
		Minecraft.getMinecraft().thePlayer.closeScreen();
		CalendarOverlay.setEnabled(true);
		NotEnoughUpdates.INSTANCE.sendChatMessage("/calendar");
	}
}
