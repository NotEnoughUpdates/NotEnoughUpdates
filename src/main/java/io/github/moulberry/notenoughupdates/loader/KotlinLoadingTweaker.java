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

import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;

import java.nio.file.Path;
import java.util.Arrays;

/**
 * <h3>TO OTHER MOD AUTHORS THAT WANT TO BUNDLE KOTLIN AND ARE RUNNING INTO CONFLICTS WITH NEU:</h3>
 *
 * <p>
 * NEU allows you to stop it from loading Kotlin by specifying one of two properties in the
 * {@link Launch#blackboard}. This needs to be done before {@link #injectIntoClassLoader}, so do it in your Tweakers
 * constructor. Most likely you will not need to do this, since different Kotlin versions rarely contain breaking changes
 * and most people do not use any of the newer features.
 * </p>
 *
 * <ul>
 *   	<li>
 *      {@code neu.relinquishkotlin.ifbelow}. Set to an int array, like so: {@code [1, 7, 20]}.
 * 			See {@link #BUNDLED_KOTLIN_VERSION}
 *   	</li>
 *  	<li>
 *      {@code neu.relinquishkotlin.always}. If set to {@code true}, will prevent NEU from loading any kotlin.
 *  		Before you use this, consider: are you sure you are always gonna have a more update version of Kotlin than NEU?
 *  		And, even if you are sure, do you really want to block us from updating our Kotlin, after we so graciously allowed
 *  		you to stop us from breaking your mod.
 *  	</li>
 *  	<li>
 *  	 	Additionally, setting the jvm property {@code neu.relinquishkotlin} to 1, will prevent NEU from loading Kotlin.
 *  	</li>
 * </ul>
 *
 * <p>
 * Sadly the Kotlin stdlib cannot be relocated (and we wouldn't want to do that either, because it would prevent us
 * from interoperating with other Kotlin mods in some ways (for example: reflection)), so in order to minimize conflicts,
 * we allow other mods to stop us from loading Kotlin. Usually this would be handled with a common library provider, but
 * since those come bundled with literal Remote Code Execution, Cosmetics, and unskippable Updates that can just not be
 * considered a good thing by any developer with a conscience nowadays, those are not an option for us. Another option
 * would be a dependency resolution algorithm like Jar-in-Jar, but sadly we have to mod on a 7 year old modding platform,
 * so we have to make due with our crude hack.
 * </p>
 */
public class KotlinLoadingTweaker extends JARLoadingTweaker {

	/**
	 * Full version format: [1, 7, 20] (1.7.20)
	 * RC version format: [1, 7, 20, 1] (1.7.20-rc1)
	 */
	public static final int[] BUNDLED_KOTLIN_VERSION = new int[]{1, 8, 21};

	@Override
	protected Path getFilesToLoad() {
		if ("1".equals(System.getProperty("neu.relinquishkotlin"))) {
			System.out.println("NEU is forced to relinquish Kotlin by user configuration.");
			return null;
		}
		if (Launch.blackboard.get("fml.deobfuscatedEnvironment") == Boolean.TRUE) {
			System.out.println("Skipping NEU Kotlin loading in development environment.");
			return null;
		}
		Object relinquishAlways = Launch.blackboard.get("neu.relinquishkotlin.always");
		if (relinquishAlways == Boolean.TRUE) {
			System.err.println(
				"NEU is forced to blanket relinquish loading Kotlin. This is probably a bad judgement call by another developer.");
			return null;
		}
		Object relinquishIfBelow = Launch.blackboard.get("neu.relinquishkotlin.ifbelow");
		if ((relinquishIfBelow instanceof int[])) {
			int[] requiredVersion = (int[]) relinquishIfBelow;
			if (!areWeBundlingAKotlinVersionHigherThan(requiredVersion)) {
				System.err.println(
					"NEU is relinquishing loading Kotlin because a higher version is requested. This may lead to errors if the advertised Kotlin version is not found. (" +
						Arrays.toString(requiredVersion) + " required, " + Arrays.toString(BUNDLED_KOTLIN_VERSION) +
						" available)");
				return null;
			}
		}

		System.out.println("Attempting to load Kotlin from NEU wrapped libraries.");

		return getShadowedElement("/neu-kotlin-libraries-wrapped");
	}

	@Override
	protected String getTestClass() {
		return "kotlin.KotlinVersion";
	}

	public boolean areWeBundlingAKotlinVersionHigherThan(int[] x) {
		for (int i = 0; ; i++) {
			boolean doWeHaveMoreVersionIdsLeft = i < BUNDLED_KOTLIN_VERSION.length;
			boolean doTheyHaveMoreVersionIdsLeft = i < x.length;
			if (doWeHaveMoreVersionIdsLeft && !doTheyHaveMoreVersionIdsLeft) return false;
			if (doTheyHaveMoreVersionIdsLeft && !doWeHaveMoreVersionIdsLeft) return true;
			if (!doTheyHaveMoreVersionIdsLeft) return true;
			if (x[i] > BUNDLED_KOTLIN_VERSION[i]) return false;
		}
	}

	@Override
	public void injectIntoClassLoader(LaunchClassLoader classLoader) {
		performLoading(classLoader);
	}
}
