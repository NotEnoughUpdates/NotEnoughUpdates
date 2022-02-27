package io.github.moulberry.notenoughupdates.commands.misc;

import io.github.moulberry.notenoughupdates.collectionlog.GuiCollectionLog;
import io.github.moulberry.notenoughupdates.commands.ScreenCommand;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CollectionLogCommand extends ScreenCommand {

	public CollectionLogCommand() {
		super("neucl", GuiCollectionLog::new);
	}

	@Override
	public List<String> getCommandAliases() {
		return Collections.singletonList("collectionlog");
	}
}
