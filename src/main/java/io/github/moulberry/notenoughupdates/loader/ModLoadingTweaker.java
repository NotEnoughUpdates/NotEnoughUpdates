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

import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraftforge.fml.relauncher.CoreModManager;
import org.spongepowered.asm.launch.MixinBootstrap;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

/**
 * The mod loading tweaker makes sure that we are recognized as a Forge Mod, despite having a Tweaker.
 * We also add ourselves as a mixin container for integration with other mixin loaders.
 */
public class ModLoadingTweaker implements ITweaker {
	@Override
	public void acceptOptions(List<String> args, File gameDir, File assetsDir, String profile) {
		URL location = ModLoadingTweaker.class.getProtectionDomain().getCodeSource().getLocation();
		if (location == null) return;
		if (!"file".equals(location.getProtocol())) return;
		try {
			MixinBootstrap.getPlatform().addContainer(location.toURI());
			String file = new File(location.toURI()).getName();
			CoreModManager.getIgnoredMods().remove(file);
			CoreModManager.getReparseableCoremods().add(file);
		} catch (URISyntaxException e) {
			System.err.println("NEU could not re-add itself as mod.");
			e.printStackTrace();
		}
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
