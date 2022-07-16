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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import io.github.moulberry.notenoughupdates.util.NetUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

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

	public abstract void launchUpdate(File file);
}
