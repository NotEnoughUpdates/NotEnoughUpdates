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

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class CalculatorTest {
	public static void main(String[] args) throws Calculator.CalculatorException {
		Scanner s = new Scanner(System.in);
		while (true) {
			try {
				List<Calculator.Token> lex = Calculator.lex(s.nextLine());
				List<Calculator.Token> shunted = Calculator.shuntingYard(lex);
				for (Calculator.Token rawToken : shunted) {
					System.out.printf(
						"%s(%s)",
						rawToken.type,
						rawToken.operatorValue == null
							? rawToken.numericValue + " * 10 ^ " + rawToken.exponent
							: rawToken.operatorValue
					);
				}
				System.out.println();
				BigDecimal evaluate = Calculator.evaluate(name -> Optional.of(BigDecimal.valueOf(16)), shunted);
				System.out.println("Eval: " + evaluate);
			} catch (Calculator.CalculatorException e) {
				e.printStackTrace();
			}
		}
	}

}
