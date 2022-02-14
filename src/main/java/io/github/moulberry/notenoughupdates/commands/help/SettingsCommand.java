package io.github.moulberry.notenoughupdates.commands.help;

import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.commands.ClientCommandBase;
import io.github.moulberry.notenoughupdates.core.GuiScreenElementWrapper;
import io.github.moulberry.notenoughupdates.options.NEUConfigEditor;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;

public class SettingsCommand extends ClientCommandBase {

    public SettingsCommand() {
        super("neu");
    }

    @Override
    public List<String> getCommandAliases() {
        return Arrays.asList("neusettings", "neuconfig");
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        if (args.length > 0) {
            NotEnoughUpdates.INSTANCE.openGui = new GuiScreenElementWrapper(new NEUConfigEditor(NotEnoughUpdates.INSTANCE.config, StringUtils.join(args, " ")));
        } else {
            NotEnoughUpdates.INSTANCE.openGui = new GuiScreenElementWrapper(NEUConfigEditor.editor);
        }
    }
}
