/*
 * Copyright (C) 2022 Linnea Gräf
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

package io.github.moulberry.notenoughupdates.loader;

import io.github.moulberry.notenoughupdates.envcheck.EnvironmentScan;
import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.LaunchClassLoader;
import org.spongepowered.asm.launch.MixinTweaker;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Tweaker used by NEU to allow delegating to multiple tweakers. The following Tweakers are currently delegated to:
 *
 * <ul>
 * 	<li>{@link KotlinLoadingTweaker} for late loading Kotlin</li>
 * 	<li>{@link MixinTweaker} for loading Mixins</li>
 * 	<li>{@link ModLoadingTweaker} to ensure we are recognized as a forge mod</li>
 * </ul>
 *
 * <p>We also run an environment check, to make sure we are running on the correct Forge and Minecraft version.</p>
 *
 * @see EnvironmentScan
 */
@SuppressWarnings("unused")
public class NEUDelegatingTweaker implements ITweaker {
	static {
		EnvironmentScan.checkEnvironmentOnce();
	}

	List<ITweaker> delegates = new ArrayList<>();

	public NEUDelegatingTweaker() {
		discoverTweakers();
		System.out.println("NEU Delegating Tweaker is loaded with: " + delegates);
	}

	private void discoverTweakers() {
		delegates.add(new MixinTweaker());
		delegates.add(new ModLoadingTweaker());
		delegates.add(new KotlinLoadingTweaker());
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
		String target = null;
		for (ITweaker delegate : delegates) {
			String launchTarget = delegate.getLaunchTarget();
			if (launchTarget != null)
				target = launchTarget;
		}
		return target;
	}

	@Override
	public String[] getLaunchArguments() {
		List<String> launchArguments = new ArrayList<>();
		for (ITweaker delegate : delegates) {
			String[] delegateLaunchArguments = delegate.getLaunchArguments();
			if (delegateLaunchArguments != null)
				launchArguments.addAll(Arrays.asList(delegateLaunchArguments));
		}
		return launchArguments.toArray(new String[0]);
	}

}
