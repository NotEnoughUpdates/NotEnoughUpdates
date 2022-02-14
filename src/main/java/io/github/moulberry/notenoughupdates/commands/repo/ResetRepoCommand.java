package io.github.moulberry.notenoughupdates.commands.repo;

import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.commands.ClientCommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

public class ResetRepoCommand extends ClientCommandBase {

    public ResetRepoCommand() {
        super("neuresetrepo");
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        NotEnoughUpdates.INSTANCE.manager.resetRepo();
    }
}
