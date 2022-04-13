package io.github.moulberry.notenoughupdates.util;

import net.minecraft.item.ItemStack;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.cert.TrustAnchor;

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
			try {
				skytilsClass = Class.forName("skytils.skytilsmod.Skytils");
				isSkytilsPresent = true;
			} catch (ClassNotFoundException ignored) {
			}
			try {
				Class<?> skytilsCompanionClass = Class.forName("skytils.skytilsmod.Skytils$Companion");
				skytilsConfigClass = Class.forName("skytils.skytilsmod.core.Config");
				Field skytilsCompanionField = skytilsClass.getField("Companion");
				skytilsCompanionObject = skytilsCompanionField.get(null);
				Method skytilsGetConfigMethod = skytilsCompanionClass.getMethod("getConfig");
				skytilsConfigObject = skytilsGetConfigMethod.invoke(skytilsCompanionObject);
				skytilsGetShowItemRarity = skytilsConfigClass.getMethod("getShowItemRarity");
				renderUtilClass = Class.forName("skytils.skytilsmod.utils.RenderUtil");
				renderRarityMethod = renderUtilClass.getDeclaredMethod(
					"renderRarity",
					ItemStack.class,
					Integer.TYPE,
					Integer.TYPE
				);
				isSkytilsFullyPresent = true;
			} catch (ClassNotFoundException | NoSuchMethodException | NoSuchFieldException | IllegalAccessException |
							 InvocationTargetException e) {
				System.err.println("Failed to get Skytils class even tho Skytils mod is present. This is (probably) a NEU bug");
				e.printStackTrace();
			}
		}

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
