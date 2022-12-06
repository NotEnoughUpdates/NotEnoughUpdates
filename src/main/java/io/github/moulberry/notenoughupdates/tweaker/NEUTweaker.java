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

package io.github.moulberry.notenoughupdates.tweaker;

import io.github.moulberry.notenoughupdates.BuildFlags;
import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.LaunchClassLoader;
import org.spongepowered.asm.launch.MixinTweaker;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NEUTweaker implements ITweaker {

	private List<ITweaker> delegates = new ArrayList<>();

	public NEUTweaker() {
		delegates.add(new MixinTweaker());
		if (BuildFlags.ENABLE_ONECONFIG_COMPAT_LAYER) {
			try {
				Class<?> oneClass = Class.forName("cc.polyfrost.oneconfigwrapper.OneConfigWrapper");
				ITweaker oneInstance = (ITweaker) oneClass.newInstance();
				delegates.add(oneInstance);
			} catch (SecurityException | ClassCastException | ClassNotFoundException |
							 InstantiationException | IllegalAccessException e) {
				System.err.println("Cannot load one config wrapper. Loading without: " + e);
			}
		}
	}

	@Override
	public void acceptOptions(List<String> args, File gameDir, File assetsDir, String profile) {
		for (ITweaker delegate : delegates) {
			delegate.acceptOptions(args, gameDir, assetsDir, profile);
		}
	}

	@Override
	public void injectIntoClassLoader(LaunchClassLoader classLoader) {
		for (ITweaker delegate : delegates) {
			delegate.injectIntoClassLoader(classLoader);
		}
	}

	@Override
	public String getLaunchTarget() {
		String ret = null;
		for (ITweaker delegate : delegates) {
			String launchTarget = delegate.getLaunchTarget();
			if (ret == null)
				ret = launchTarget;
		}
		return ret;
	}

	@Override
	public String[] getLaunchArguments() {
		List<String> args = new ArrayList<>();
		for (ITweaker delegate : delegates) {
			args.addAll(Arrays.asList(delegate.getLaunchArguments()));
		}
		return args.toArray(new String[0]);
	}
}
