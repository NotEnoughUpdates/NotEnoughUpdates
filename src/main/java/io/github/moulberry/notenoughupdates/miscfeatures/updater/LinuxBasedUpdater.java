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

import java.io.File;
import java.net.URL;
import java.util.List;

class LinuxBasedUpdater /* Based on what? */ extends UpdateLoader {

	LinuxBasedUpdater(AutoUpdater updater, URL url) {
		super(updater, url);
	}

	@Override
	public void greet() {
		updater.logProgress(
			"Welcome Aristocrat! Your superior linux system configuration is supported for NEU auto updates.");
	}

	@Override
	public void deleteFiles(List<File> toDelete) {
		for (File toDel : toDelete) {
			if (!toDel.delete()) {
				updater.logProgress("§cCould not delete old version of NEU: " + toDel + ". Please manually delete file.");
				state = State.FAILED;
			}
		}
	}
}
