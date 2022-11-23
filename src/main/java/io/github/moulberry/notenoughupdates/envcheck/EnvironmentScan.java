package io.github.moulberry.notenoughupdates.envcheck;

import javax.swing.*;
import java.lang.reflect.Field;
import java.util.Objects;

public class EnvironmentScan {

		static Class<?> tryGetClass(String name) {
				try {
						return Class.forName(name);
				} catch (ClassNotFoundException e) {
						return null;
				}
		}

		static Object tryGetField(Class<?> clazz, Object inst, String name) {
				if (clazz == null) return null;
				try {
						Field declaredField = clazz.getDeclaredField(name);
						return declaredField.get(inst);
				} catch (NoSuchFieldException | IllegalAccessException ignored) {
				}
				return null;
		}


		static boolean isAtLeast(Object left, int right) {
				if (left instanceof Integer) {
						return (Integer) left >= right;
				}
				return false;
		}

		static boolean shouldCheckOnce = true;

		public static void checkEnvironmentOnce() {
				if (shouldCheckOnce) checkEnvironment();
		}


		static void checkEnvironment() {
				shouldCheckOnce = false;
				checkForgeEnvironment();
		}

		static void checkForgeEnvironment() {
				Class<?> forgeVersion = tryGetClass("net.minecraftforge.common.ForgeVersion");
				if (forgeVersion == null
								|| !Objects.equals(tryGetField(forgeVersion, null, "majorVersion"), 11)
								|| !Objects.equals(tryGetField(forgeVersion, null, "minorVersion"), 15)
								|| !isAtLeast(tryGetField(forgeVersion, null, "revisionVersion"), 1)
								|| !Objects.equals(tryGetField(forgeVersion, null, "mcVersion"), "1.8.9")
				) {

						System.out.printf("Forge Version : %s%nMajor : %s%nMinor : %s%nRevision : %s%nMinecraft : %s%n",
										forgeVersion,
										tryGetField(forgeVersion, null, "majorVersion"),
										tryGetField(forgeVersion, null, "minorVersion"),
										tryGetField(forgeVersion, null, "revisionVersion"),
										tryGetField(forgeVersion, null, "mcVersion")
						);
						missingOrOutdatedForgeError();
				}
		}

		static void missingOrOutdatedForgeError() {
				showErrorMessage(
								"You just launched NotEnoughUpdates with the wrong (or no) modloader installed.",
								"",
								"NotEnoughUpdates only works in Minecraft 1.8.9, with Forge 11.15.1+",
								"Please relaunch NotEnoughUpdates in the correct environment.",
								"If you are using Minecraft 1.8.9 with Forge 11.15.1+ installed, please contact support.",
								"Click OK to launch anyways."
				);
		}


		public static void showErrorMessage(String... messages) {
				String message = String.join("\n", messages);
				JOptionPane.showMessageDialog(
								null, message, "NotEnoughUpdates - Problematic System Configuration", JOptionPane.ERROR_MESSAGE
				);
		}
}
