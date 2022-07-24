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

import io.github.moulberry.notenoughupdates.util.NetUtils;
import net.minecraft.client.Minecraft;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

abstract class UpdateLoader {

	enum State {
		NOTHING, DOWNLOAD_STARTED, DOWNLOAD_FINISHED, INSTALLED, FAILED
	}

	URL url;
	AutoUpdater updater;

	State state = State.NOTHING;

	UpdateLoader(AutoUpdater updater, URL url) {
		this.url = url;
		this.updater = updater;
	}

	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}

	public URL getUrl() {
		return url;
	}

	public void scheduleDownload() {
		state = State.DOWNLOAD_STARTED;
		try {
			NetUtils.downloadAsync(url, File.createTempFile("NotEnoughUpdates-update", ".jar"))
							.handle(
								(f, exc) -> {
									if (exc != null) {
										state = State.FAILED;
										updater.logProgress("§cError while downloading. Check your logs for more info.");
										exc.printStackTrace();
										return null;
									}
									state = State.DOWNLOAD_FINISHED;

									updater.logProgress("Download completed. Trying to install");
									launchUpdate(f);
									return null;
								});
		} catch (IOException e) {
			state = State.FAILED;
			updater.logProgress("§cError while creating download. Check your logs for more info.");
			e.printStackTrace();
		}
	}

	public abstract void greet();

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
		File[] modFiles = mcDataDir.listFiles();
		if (modFiles == null) {
			updater.logProgress("§cCould not list minecraft mod folder (" + mcDataDir + ")");
			state = State.FAILED;
			return;
		}
		for (File sus : modFiles) {
			if (sus.getName().endsWith(".jar")) {
				if (updater.isNeuJar(sus)) {
					updater.logProgress("Found old NEU file: " + sus + ". Deleting later.");
					toDelete.add(sus);
				}
			}
		}
		File dest = new File(mcDataDir, file.getName());
		try (
			InputStream i = Files.newInputStream(file.toPath());
			OutputStream o = Files.newOutputStream(dest.toPath());
		) {
			IOUtils.copyLarge(i, o);
		} catch (IOException e) {
			e.printStackTrace();
			updater.logProgress(
				"§cFailed to copy release JAR. Not making any changes to your mod folder. Consult your logs for more info.");
			state = State.FAILED;
		}
		deleteFiles(toDelete);
		if (state != State.FAILED) {
			state = State.INSTALLED;
			updater.logProgress("Update successful. Thank you for your time.");
			return;
		}
		updater.logProgress("§cFailure to delete some files. Please delte the old NEU version manually from your mods folder.");
	}

	public abstract void deleteFiles(List<File> toDelete);

}
