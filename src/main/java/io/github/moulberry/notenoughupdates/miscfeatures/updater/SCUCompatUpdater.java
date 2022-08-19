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
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.List;

/*
 * Legal considerations: Skyblock Client Updater is licensed under the GNU AGPL v3.0 or later (with modifications).
 * https://github.com/My-Name-Is-Jeff/SkyblockClient-Updater/blob/main/LICENSE
 *
 * However, even tho the AGPL License does not allow conveying covered work in combination with LGPL licensed code
 * (such as our own), we do not perceive ourselves as conveying neither an unmodified version of Skyblock Client Updater
 * nor a work based on Skyblock Client Updater (modified work) since our work is usable and functional in its entirety
 * without presence of Skyblock Client Updater and is not to be distributed along a copy of Skyblock Client Updater
 * unless that combined work is licensed with respect of both the LGPL and the AGPL, therefore is not adapting any part
 * of Skyblock Client Updater unless already part of a whole distribution.
 *
 * In case the Copyright owner (Lily aka My-Name-Is-Jeff on Github) disagrees, we are willing to take down this module
 * (or only convey this component of our work under a pure GPL license) with or without them providing legal grounds
 * for this request. However, due to them not being able to be reached for comment, we will include this
 * component for the time being.
 * */
public class SCUCompatUpdater extends UpdateLoader {

	public static final boolean IS_ENABLED = false;

	private SCUCompatUpdater(AutoUpdater updater, URL url) {
		super(updater, url);
	}

	@Override
	public void greet() {
		updater.logProgress("Skyblock Client Updater compatibility layer loaded.");
	}

	@Override
	public void deleteFiles(List<File> toDelete) {
		try {
			for (File f : toDelete)
				ReflectionHolder.deleteFileOnShutdownHandle.invoke(ReflectionHolder.updateCheckerInstance, f, "");
		} catch (Throwable e) {
			e.printStackTrace();
			updater.logProgress("Invoking SCU failed. Check the log for more info.");
			state = State.FAILED;
		}
	}

	static class ReflectionHolder {
		static boolean isSCUFullyPresent = false;
		static Class<?> updateChecker;
		static Object updateCheckerInstance;
		static Method deleteFileOnShutdown;
		static MethodHandle deleteFileOnShutdownHandle;

		static {
			try {
				updateChecker = Class.forName("mynameisjeff.skyblockclientupdater.utils.UpdateChecker");
				Field instanceField = updateChecker.getDeclaredField("INSTANCE");
				instanceField.setAccessible(true);
				updateCheckerInstance = instanceField.get(null);
				deleteFileOnShutdown = updateChecker.getDeclaredMethod("deleteFileOnShutdown", File.class, String.class);
				deleteFileOnShutdownHandle = MethodHandles.publicLookup().unreflect(deleteFileOnShutdown);
				isSCUFullyPresent = true;
			} catch (NoSuchFieldException | ClassNotFoundException | IllegalAccessException | NoSuchMethodException e) {
				e.printStackTrace();
			}
		}
	}

	public static UpdateLoader tryCreate(AutoUpdater updater, URL url) {
		if (!ReflectionHolder.isSCUFullyPresent) {
			updater.logProgress("§cFound Skyclient Updater Mod, however our hooks did not function properly.");
			return null;
		}
		return new SCUCompatUpdater(updater, url);
	}
}
