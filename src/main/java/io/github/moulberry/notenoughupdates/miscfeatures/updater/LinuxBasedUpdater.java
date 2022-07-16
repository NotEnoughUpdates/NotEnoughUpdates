/*
 * Copyright (C) 2022 NotEnoughUpdates contributors
 *
 * This file is part of NotEnoughUpdates.
 *
 * NotEnoughUpdates is free software: you can redistribute it
 * and/or modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * NotEnoughUpdates is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with NotEnoughUpdates. If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.moulberry.notenoughupdates.miscfeatures.updater;

import com.google.common.io.Files;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

/* Based on what? */
class LinuxBasedUpdater extends UpdateLoader {

	LinuxBasedUpdater(AutoUpdater updater, URL url) {
		super(updater, url);
	}

	@Override
	public void greet() {
		updater.logProgress(
			"Welcome Aristocrat! Your superior linux system configuration is supported for NEU auto updates.");
	}

	@Override
	public void launchUpdate(File file) {
		if (state != State.DOWNLOAD_FINISHED) {
			updater.logProgress("§cUpdate is invalid state " + state + " to start update.");
			state = State.FAILED;
			return;
		}
		File mcDataDir = new File(Minecraft.getMinecraft().mcDataDir, "mods");
		if (!mcDataDir.exists() || !mcDataDir.isDirectory() || !mcDataDir.canRead()) {
			updater.logProgress("§cCould not find mods folder. Searched: " + mcDataDir);
			state = State.FAILED;
			return;
		}
		ArrayList<File> toDelete = new ArrayList<>();
		for (File sus : mcDataDir.listFiles()) {
			if (sus.getName().endsWith(".jar")) {
				if (updater.isNeuJar(sus)) {
					updater.logProgress("Found old NEU file: " + sus + ". Deleting later.");
					toDelete.add(sus);
				}
			}
		}
		File dest = new File(mcDataDir, file.getName());
		try {
			Files.copy(file, dest);
		} catch (IOException e) {
			e.printStackTrace();
			updater.logProgress(
				"§cFailed to copy release JAR. Not making any changes to your mod folder. Consult your logs for more info.");
			state = State.FAILED;
		}
		for (File toDel : toDelete) {
			if (!toDel.delete()) {
				updater.logProgress("§cCould not delete old version of NEU: " + toDel + ". Please manually delete file.");
				state = State.FAILED;
			}
		}
		if (state != State.FAILED)
			state = State.INSTALLED;
		updater.logProgress("Update successful. Thank you for your time.");
	}
}
