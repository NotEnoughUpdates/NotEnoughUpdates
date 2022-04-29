package io.github.moulberry.notenoughupdates.commands.profile;

import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import net.minecraft.client.Minecraft;
import net.minecraft.command.ICommandSender;
import org.apache.commons.lang3.StringUtils;

public class PvCommand extends ViewProfileCommand {

	public PvCommand() {
		super("pv");
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) {
		if (!NotEnoughUpdates.INSTANCE.isOnSkyblock()) {
			Minecraft.getMinecraft().thePlayer.sendChatMessage("/pv " + StringUtils.join(args, " "));
		} else {
			super.processCommand(sender, args);
		}
	}
}
