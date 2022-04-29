package io.github.moulberry.notenoughupdates.commands.dev;

import io.github.moulberry.notenoughupdates.commands.ClientCommandBase;
import io.github.moulberry.notenoughupdates.dungeons.DungeonWin;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ResourceLocation;

public class DungeonWinTestCommand extends ClientCommandBase {

	public DungeonWinTestCommand() {
		super("neudungeonwintest");
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) throws CommandException {
		if (args.length > 0) {
			DungeonWin.TEAM_SCORE = new ResourceLocation("notenoughupdates:dungeon_win/" + args[0].toLowerCase() + ".png");
		}

		DungeonWin.displayWin();
	}
}
