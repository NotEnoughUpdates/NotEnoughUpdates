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

package io.github.moulberry.notenoughupdates.util;

import net.minecraft.item.ItemStack;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

public class SkytilsCompat {
	// Defer static initialization
	private static class Holder {
		// Skytils is present in some capacity
		static boolean isSkytilsPresent = false;
		// All classes successfully loaded
		static boolean isSkytilsFullyPresent = false;

		static Class<?> skytilsClass = null;
		static Method renderRarityMethod = null;
		static Class<?> renderUtilClass = null;

		static Object skytilsCompanionObject = null;

		static Class<?> skytilsConfigClass = null;

		static Object skytilsConfigObject = null;
		static Method skytilsGetShowItemRarity = null;

		static {
			Exception exception = null;
			for (String packageStart : Arrays.asList("gg.skytils", "skytils")) {
				isSkytilsPresent = false;
				try {
					skytilsClass = Class.forName(packageStart + ".skytilsmod.Skytils");
					isSkytilsPresent = true;
				} catch (ClassNotFoundException ignored) {
				}

				if (isSkytilsPresent) {
					try {
						Class<?> skytilsCompanionClass = Class.forName(packageStart + ".skytilsmod.Skytils$Companion");
						skytilsConfigClass = Class.forName(packageStart + ".skytilsmod.core.Config");
						Field skytilsCompanionField = skytilsClass.getField("Companion");
						skytilsCompanionObject = skytilsCompanionField.get(null);
						Method skytilsGetConfigMethod = skytilsCompanionClass.getMethod("getConfig");
						skytilsConfigObject = skytilsGetConfigMethod.invoke(skytilsCompanionObject);
						skytilsGetShowItemRarity = skytilsConfigClass.getMethod("getShowItemRarity");
						renderUtilClass = Class.forName(packageStart + ".skytilsmod.utils.RenderUtil");
						renderRarityMethod = renderUtilClass.getDeclaredMethod(
							"renderRarity",
							ItemStack.class,
							Integer.TYPE,
							Integer.TYPE
						);
						isSkytilsFullyPresent = true;
						break;
					} catch (ClassNotFoundException | NoSuchMethodException | NoSuchFieldException | IllegalAccessException |
						InvocationTargetException e) {
						exception = e;
					}
				}
			}

			if (!isSkytilsFullyPresent) {
				if (exception != null) {
					System.err.println("Failed to get Skytils class even tho Skytils mod is present. This is (probably) a NEU bug");
					exception.printStackTrace();
				}
			}
		}
	}

	public static boolean isSkytilsFullyLoaded() {
		return Holder.isSkytilsFullyPresent;
	}

	public static boolean isSkytilsPresent() {
		return Holder.isSkytilsPresent;
	}

	public static void renderSkytilsRarity(ItemStack stack, int x, int y) {
		renderSkytilsRarity(stack, x, y, false);
	}

	public static void renderSkytilsRarity(ItemStack stack, int x, int y, boolean force) {
		if (Holder.isSkytilsFullyPresent) {
			try {
				if (force || (boolean) Holder.skytilsGetShowItemRarity.invoke(Holder.skytilsConfigObject))
					Holder.renderRarityMethod.invoke(null, stack, x, y);
			} catch (IllegalAccessException | InvocationTargetException e) {
				throw new RuntimeException(e);
			}
		}
	}

}
