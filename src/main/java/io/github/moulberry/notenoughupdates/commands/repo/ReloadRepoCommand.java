package io.github.moulberry.notenoughupdates.commands.repo;

import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.commands.ClientCommandBase;
import io.github.moulberry.notenoughupdates.options.NEUConfig;
import io.github.moulberry.notenoughupdates.util.Constants;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class ReloadRepoCommand extends ClientCommandBase {

	public ReloadRepoCommand() {
		super("neureloadrepo");
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) throws CommandException {
		NotEnoughUpdates.INSTANCE.manager.reloadRepository();

		NotEnoughUpdates.INSTANCE.newConfigFile();
		if (NotEnoughUpdates.INSTANCE.getConfigFile().exists()) {
			try (
				BufferedReader reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(NotEnoughUpdates.INSTANCE.getConfigFile()),
					StandardCharsets.UTF_8
				))
			) {
				NotEnoughUpdates.INSTANCE.config = NotEnoughUpdates.INSTANCE.manager.gson.fromJson(reader, NEUConfig.class);
			} catch (Exception ignored) {
			}
		}
	}
}
