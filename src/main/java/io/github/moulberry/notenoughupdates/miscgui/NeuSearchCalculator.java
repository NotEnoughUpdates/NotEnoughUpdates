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

package io.github.moulberry.notenoughupdates.miscgui;

import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.util.Calculator;

import java.math.BigDecimal;
import java.text.DecimalFormat;

public class NeuSearchCalculator {

	static String lastInput = "";
	static String lastResult = null;

	public static String format(String text) {
		String calculate = calculateInSearchBar(text);
		return text + (calculate != null ? " §e= §a" + calculate : "");
	}

	private static String calculateInSearchBar(String input) {
		int calculationMode = NotEnoughUpdates.INSTANCE.config.misc.calculationMode;
		if (input.isEmpty() || calculationMode == 0 || (calculationMode == 1 && !input.startsWith("!"))) return null;
		input = calculationMode == 1 ? input.substring(1) : input;

		if (!lastInput.equals(input)) {
			lastInput = input;
			try {
				BigDecimal calculate = Calculator.calculate(input);
				lastResult = new DecimalFormat("#,##0.##").format(calculate);
			} catch (Calculator.CalculatorException ignored) {
				lastResult = null;
			}
		}

		return lastResult;
	}
}
