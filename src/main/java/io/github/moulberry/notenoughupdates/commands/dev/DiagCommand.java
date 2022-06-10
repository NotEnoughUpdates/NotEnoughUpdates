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

package io.github.moulberry.notenoughupdates.commands.dev;

import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.commands.ClientCommandBase;
import io.github.moulberry.notenoughupdates.miscfeatures.CrystalMetalDetectorSolver;
import io.github.moulberry.notenoughupdates.miscfeatures.CrystalWishingCompassSolver;
import io.github.moulberry.notenoughupdates.options.customtypes.NEUDebugFlag;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

public class DiagCommand extends ClientCommandBase {
	public DiagCommand() {
		super("neudiag");
	}

	private static final String USAGE_TEXT = EnumChatFormatting.WHITE +
		"Usage: /neudiag <metal | wishing | debug>\n\n" +
		"/neudiag metal          Metal Detector Solver diagnostics\n" +
		"  <no sub-command>           Show current solution diags\n" +
		"  center=<off | on>          Disable / enable using center\n" +
		"/neudiag wishing        Wishing Compass Solver diagnostics\n" +
		"/neudiag debug\n" +
		"  <no sub-command>           Show current flags\n" +
		"  <enable | disable> <flag>  Enable/disable flag\n";

	private void showUsage(ICommandSender sender) {
		sender.addChatMessage(new ChatComponentText(USAGE_TEXT));
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) throws CommandException {
		if (args.length == 0) {
			showUsage(sender);
			return;
		}

		String command = args[0].toLowerCase();
		switch (command) {
			case "metal":
				if (args.length == 1) {
					CrystalMetalDetectorSolver.logDiagnosticData(true);
					return;
				}

				String subCommand = args[1].toLowerCase();
				if (subCommand.equals("center=off")) {
					CrystalMetalDetectorSolver.setDebugDoNotUseCenter(true);
					sender.addChatMessage(new ChatComponentText(EnumChatFormatting.YELLOW +
						"Center coordinates-based solutions disabled"));
				} else if (subCommand.equals("center=on")) {
					CrystalMetalDetectorSolver.setDebugDoNotUseCenter(false);
					sender.addChatMessage(new ChatComponentText(EnumChatFormatting.YELLOW +
						"Center coordinates-based solutions enabled"));
				} else {
					showUsage(sender);
					return;
				}

				break;
			case "wishing":
				CrystalWishingCompassSolver.getInstance().logDiagnosticData(true);
				break;
			case "debug":
				if (args.length > 1) {
					boolean enablingFlag = true;
					String action = args[1];
					switch (action) {
						case "disable":
							enablingFlag = false;
							// falls through
						case "enable":
							if (args.length != 3) {
								sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED +
									"You must specify a flag:\n" +
									NEUDebugFlag.FLAG_LIST));
								return;
							}

							String flagName = args[2].toUpperCase();
							try {
								NEUDebugFlag debugFlag = NEUDebugFlag.valueOf(flagName);
								if (enablingFlag) {
									NotEnoughUpdates.INSTANCE.config.hidden.debugFlags.add(debugFlag);
								} else {
									NotEnoughUpdates.INSTANCE.config.hidden.debugFlags.remove(debugFlag);
								}
							} catch (IllegalArgumentException e) {
								sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED +
									flagName + " is invalid. Valid flags are:\n" +
									NEUDebugFlag.FLAG_LIST));
								return;
							}
							break;
						default:
							showUsage(sender);
							return;
					}
				}

				sender.addChatMessage(new ChatComponentText(EnumChatFormatting.YELLOW + "Effective debug flags: " +
					NotEnoughUpdates.INSTANCE.config.hidden.debugFlags.toString()));
				break;
			default:
				showUsage(sender);
				return;
		}
	}
}
