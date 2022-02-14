package io.github.moulberry.notenoughupdates.commands.profile;

import io.github.moulberry.notenoughupdates.profileviewer.GuiProfileViewer;
import net.minecraft.command.ICommandSender;

public class CataCommand extends ViewProfileCommand {

    public CataCommand() {
        super("cata");
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        GuiProfileViewer.currentPage = GuiProfileViewer.ProfileViewerPage.DUNG;
        super.processCommand(sender, args);
    }
}
