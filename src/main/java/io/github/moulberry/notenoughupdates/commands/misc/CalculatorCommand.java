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

package io.github.moulberry.notenoughupdates.commands.misc;

import io.github.moulberry.notenoughupdates.commands.ClientCommandBase;
import io.github.moulberry.notenoughupdates.util.Calculator;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import java.text.DecimalFormat;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class CalculatorCommand extends ClientCommandBase {
	public CalculatorCommand() {
		super("neucalc");
	}

	@Override
	public List<String> getCommandAliases() {
		return Arrays.asList("neucalculator");
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) throws CommandException {
		if ((args.length == 1 && Objects.equals(args[0], "help")) || args.length == 0) {
			sender.addChatMessage(new ChatComponentText(
				"\n§e[NEU] §5It's a calculator.\n" +
					"§eFor Example §b/neucalc 3m*7k§e.\n" +
					"§eYou can also use suffixes (k, m, b, t, s)§e.\n" +
					"§eThe \"s\" suffix acts as 64.\n" +
					"§eTurn on Sign Calculator in /neu misc to also support this in sign popups.\n"));
			return;
		}
		String source = String.join(" ", args);
		try {
			BigDecimal calculate = Calculator.calculate(source);
			DecimalFormat formatter = new DecimalFormat("#,##0.##");
			String format = formatter.format(calculate);
			sender.addChatMessage(new ChatComponentText(
				EnumChatFormatting.YELLOW + "[NEU] " + EnumChatFormatting.WHITE + source + " " + EnumChatFormatting.YELLOW +
					"= " + EnumChatFormatting.GREEN + format
			));
		} catch (Calculator.CalculatorException e) {
			sender.addChatMessage(new ChatComponentText(
				EnumChatFormatting.YELLOW + "[NEU] " + EnumChatFormatting.RED + "Error during calculation: " +
					e.getMessage() + "\n" +
					EnumChatFormatting.WHITE + source.substring(0, e.getOffset()) + EnumChatFormatting.DARK_RED +
					source.substring(e.getOffset(), e.getLength() + e.getOffset()) + EnumChatFormatting.GRAY +
					source.substring(e.getLength() + e.getOffset())
			));
		}
	}
}
