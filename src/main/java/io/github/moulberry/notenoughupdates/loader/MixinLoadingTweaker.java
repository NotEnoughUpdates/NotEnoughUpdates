/*
 * Copyright (C) 2024 NotEnoughUpdates contributors
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

import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;

import java.nio.file.Path;

public class MixinLoadingTweaker extends JARLoadingTweaker {

	@Override
	protected Path getFilesToLoad() {
		return getShadowedElement("/neu-mixin-libraries-wrapped");
	}

	@Override
	protected String getTestClass() {
		return "org.spongepowered.asm.launch.MixinBootstrap";
	}

	@Override
	public void injectIntoClassLoader(LaunchClassLoader classLoader) {
		// Explicitly don't inject in here. Instead use the constructor.
	}

	public MixinLoadingTweaker() {
		performLoading(Launch.classLoader);
	}
}
