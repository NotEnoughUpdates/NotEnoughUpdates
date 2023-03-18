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

import io.github.moulberry.notenoughupdates.BuildFlags;
import io.github.moulberry.notenoughupdates.envcheck.EnvironmentScan;
import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;
import org.spongepowered.asm.launch.MixinTweaker;

import java.io.File;
import java.util.ArrayList;
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

	List<String> delegates = new ArrayList<>();

	public NEUDelegatingTweaker() {
		discoverTweakers();
		System.out.println("NEU Delegating Tweaker is loaded with: " + delegates);
	}

	private void discoverTweakers() {
		if (BuildFlags.ENABLE_ONECONFIG_COMPAT_LAYER) {
			delegates.add("cc.polyfrost.oneconfig.loader.stage0.LaunchWrapperTweaker");
		}
		delegates.add(MixinTweaker.class.getName());
		delegates.add(ModLoadingTweaker.class.getName());
		delegates.add(KotlinLoadingTweaker.class.getName());
	}

	@Override
	public void acceptOptions(List<String> args, File gameDir, File assetsDir, String profile) {
		List<String> tweakClasses = (List<String>) Launch.blackboard.get("TweakClasses");
		tweakClasses.addAll(delegates);
	}

	@Override
	public void injectIntoClassLoader(LaunchClassLoader classLoader) {
	}

	@Override
	public String getLaunchTarget() {
		return null;
	}

	@Override
	public String[] getLaunchArguments() {
		return new String[0];
	}

}
