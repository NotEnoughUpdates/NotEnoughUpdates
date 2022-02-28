package io.github.moulberry.notenoughupdates.commands.dev;

import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.commands.ClientCommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

public class DiagsCommand extends ClientCommandBase {
	public DiagsCommand() {
		super("neudiags");
	}

	private void showUsage(ICommandSender sender) {
		sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED +
			"Usage:"));
		sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED +
			"  /neudiags debugflags [flags] - Sets or shows debug flags"));
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) throws CommandException {
		if (args.length == 0) {
			showUsage(sender);
			return;
		}

		String command = args[0].toLowerCase();
		switch (command) {
			case "debugflags":
				if (args.length > 1) {
					String flags = args[1];
					try {
						NotEnoughUpdates.INSTANCE.config.hidden.debugFlags.setFlags(Integer.decode(flags));
					} catch (NumberFormatException e) {
						sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Can't parse flags: " + flags));
					}
				}

				sender.addChatMessage(new ChatComponentText(EnumChatFormatting.YELLOW + "debugflags: " +
					NotEnoughUpdates.INSTANCE.config.hidden.debugFlags.getFlags()));
				break;
			default:
				showUsage(sender);
				return;
		}
	}
}
