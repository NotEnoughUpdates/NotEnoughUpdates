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
import java.math.RoundingMode;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;

public class Calculator {
	public static BigDecimal calculate(String source) throws CalculatorException {
		source = source.toLowerCase(Locale.ROOT);
		return evaluate(shuntingYard(lex(source)));
	}

	///<editor-fold desc="Lexing Time">
	public enum TokenType {
		NUMBER, BINOP, LPAREN, RPAREN, POSTOP
	}

	public static class Token {
		public TokenType type;
		String operatorValue;
		long numericValue;
		int exponent;
		int tokenStart;
		int tokenLength;
	}

	static String binops = "+-*/^x";
	static String postops = "mkbts%";
	static String digits = "0123456789";

	static void readDigitsInto(Token token, String source, boolean decimals) {
		int startIndex = token.tokenStart + token.tokenLength;
		for (int j = 0; j + startIndex < source.length(); j++) {
			char d = source.charAt(j + startIndex);
			int d0 = digits.indexOf(d);
			if (d0 != -1) {
				if (decimals)
					token.exponent--;
				token.numericValue *= 10;
				token.numericValue += d0;
				token.tokenLength += 1;
			} else {
				return;
			}
		}
	}

	public static class CalculatorException extends Exception {
		int offset, length;

		public CalculatorException(String message, int offset, int length) {
			super(message);
			this.offset = offset;
			this.length = length;
		}

		public int getLength() {
			return length;
		}

		public int getOffset() {
			return offset;
		}
	}

	public static List<Token> lex(String source) throws CalculatorException {
		List<Token> tokens = new ArrayList<>();
		for (int i = 0; i < source.length(); ) {
			char c = source.charAt(i);
			if (Character.isWhitespace(c)) {
				i++;
				continue;
			}
			Token token = new Token();
			token.tokenStart = i;
			if (binops.indexOf(c) != -1) {
				token.tokenLength = 1;
				token.type = TokenType.BINOP;
				token.operatorValue = c + "";
			} else if (postops.indexOf(c) != -1) {
				token.tokenLength = 1;
				token.type = TokenType.POSTOP;
				token.operatorValue = c + "";
			} else if (c == ')') {
				token.tokenLength = 1;
				token.type = TokenType.RPAREN;
				token.operatorValue = ")";
			} else if (c == '(') {
				token.tokenLength = 1;
				token.type = TokenType.LPAREN;
				token.operatorValue = "(";
			} else if ('.' == c) {
				token.tokenLength = 1;
				token.type = TokenType.NUMBER;
				readDigitsInto(token, source, true);
				if (token.tokenLength == 1) {
					throw new CalculatorException("Invalid number literal", i, 1);
				}
			} else if (digits.indexOf(c) != -1) {
				token.type = TokenType.NUMBER;
				readDigitsInto(token, source, false);
				if (i + token.tokenLength < source.length()) {
					char p = source.charAt(i + token.tokenLength);
					if ('.' == p) {
						token.tokenLength++;
						readDigitsInto(token, source, true);
					}
				}
			} else {
				throw new CalculatorException("Unknown thing " + c, i, 1);
			}
			tokens.add(token);
			i += token.tokenLength;
		}
		return tokens;
	}
	///</editor-fold>

	///<editor-fold desc="Shunting Time">
	static int getPrecedence(Token token) throws CalculatorException {
		switch (token.operatorValue.intern()) {
			case "+":
			case "-":
				return 0;
			case "*":
			case "/":
			case "x":
				return 1;
			case "^":
				return 2;
		}
		throw new CalculatorException("Unknown operator " + token.operatorValue, token.tokenStart, token.tokenLength);
	}

	public static List<Token> shuntingYard(List<Token> toShunt) throws CalculatorException {
		// IT'S SHUNTING TIME
		// This is an implementation of the shunting yard algorithm

		Deque<Token> op = new ArrayDeque<>();
		List<Token> out = new ArrayList<>();
		boolean nextMultiplyShouldBePower = false;

		for (Token currentlyShunting : toShunt) {
			switch (currentlyShunting.type) {
				case NUMBER:
					out.add(currentlyShunting);
					break;
				case BINOP:
					Token next = toShunt.get(toShunt.indexOf(currentlyShunting) + 1);
					if (currentlyShunting.operatorValue.equals("^")) {
						if (next.numericValue > 999) {
							throw new CalculatorException(next.numericValue + " is too large, pick a power less than 1000", next.tokenStart, next.tokenLength);
						}
					} else if (next != null && currentlyShunting.operatorValue.equals("*") && next.operatorValue != null && next.operatorValue.equals("*")) {
						nextMultiplyShouldBePower = true;
						continue;
					} else if (nextMultiplyShouldBePower && currentlyShunting.operatorValue.equals("*")) {
						currentlyShunting.operatorValue = "^";
					}
					int p = getPrecedence(currentlyShunting);
					while (!op.isEmpty()) {
						Token l = op.peek();
						if (l.type == TokenType.LPAREN)
							break;
						assert (l.type == TokenType.BINOP);
						int pl = getPrecedence(l);
						if (pl >= p) { // Association order
							out.add(op.pop());
						} else {
							break;
						}
					}
					op.push(currentlyShunting);
					break;
				case LPAREN:
					op.push(currentlyShunting);
					break;
				case RPAREN:
					while (1 > 0) {
						if (op.isEmpty())
							throw new CalculatorException(
								"Unbalanced right parenthesis",
								currentlyShunting.tokenStart,
								currentlyShunting.tokenLength
							);
						Token l = op.pop();
						if (l.type == TokenType.LPAREN) {
							break;
						}
						out.add(l);
					}
					break;
				case POSTOP:
					out.add(currentlyShunting);
					break;
			}
		}
		while (!op.isEmpty()) {
			Token l = op.pop();
			if (l.type == TokenType.LPAREN)
				throw new CalculatorException("Unbalanced left parenthesis", l.tokenStart, l.tokenLength);
			out.add(l);
		}
		return out;
	}

	/// </editor-fold>

	///<editor-fold desc="Evaluating Time">

	public static BigDecimal evaluate(List<Token> rpnTokens) throws CalculatorException {
		Deque<BigDecimal> values = new ArrayDeque<>();
		try {
			for (Token command : rpnTokens) {
				switch (command.type) {
					case NUMBER:
						values.push(new BigDecimal(command.numericValue).scaleByPowerOfTen(command.exponent));
						break;
					case BINOP:
						BigDecimal right = values.pop().setScale(2, RoundingMode.HALF_UP);
						BigDecimal left = values.pop().setScale(2, RoundingMode.HALF_UP);
						switch (command.operatorValue.intern()) {
							case "^":
								if (right.compareTo(new BigDecimal(1000)) >= 0) {
									Token rightToken = rpnTokens.get(rpnTokens.indexOf(command) - 1);
									throw new CalculatorException(right + " is too large, pick a power less than 1000", rightToken.tokenStart, rightToken.tokenLength);
								}

								if (right.doubleValue() != right.intValue()) {
									Token rightToken = rpnTokens.get(rpnTokens.indexOf(command) - 1);
									throw new CalculatorException(right + " has a decimal, pick a power that is non-decimal", rightToken.tokenStart, rightToken.tokenLength);
								}
								values.push(left.pow(right.intValue()).setScale(2, RoundingMode.HALF_UP));
								break;
							case "x":
							case "*":
								values.push(left.multiply(right).setScale(2, RoundingMode.HALF_UP));
								break;
							case "/":
								try {
									values.push(left.divide(right, RoundingMode.HALF_UP).setScale(2, RoundingMode.HALF_UP));
								} catch (ArithmeticException e) {
									throw new CalculatorException("Encountered division by 0", command.tokenStart, command.tokenLength);
								}
								break;
							case "+":
								values.push(left.add(right).setScale(2, RoundingMode.HALF_UP));
								break;
							case "-":
								values.push(left.subtract(right).setScale(2, RoundingMode.HALF_UP));
								break;
							default:
								throw new CalculatorException(
									"Unknown operation " + command.operatorValue,
									command.tokenStart,
									command.tokenLength
								);
						}
						break;
					case LPAREN:
					case RPAREN:
						throw new CalculatorException(
							"Did not expect unshunted token in RPN",
							command.tokenStart,
							command.tokenLength
						);
					case POSTOP:
						BigDecimal p = values.pop();
						switch (command.operatorValue.intern()) {
							case "s":
								values.push(p.multiply(new BigDecimal(64)).setScale(2, RoundingMode.HALF_UP));
								break;
							case "k":
								values.push(p.multiply(new BigDecimal(1_000)).setScale(2, RoundingMode.HALF_UP));
								break;
							case "m":
								values.push(p.multiply(new BigDecimal(1_000_000)).setScale(2, RoundingMode.HALF_UP));
								break;
							case "b":
								values.push(p.multiply(new BigDecimal(1_000_000_000)).setScale(2, RoundingMode.HALF_UP));
								break;
							case "t":
								values.push(p.multiply(new BigDecimal("1000000000000")).setScale(2, RoundingMode.HALF_UP));
								break;
							case "%":
								values.push(p.setScale(3, RoundingMode.HALF_UP).divide(new BigDecimal(100), RoundingMode.HALF_UP).setScale(2, RoundingMode.HALF_UP));
								break;
							default:
								throw new CalculatorException(
									"Unknown operation " + command.operatorValue,
									command.tokenStart,
									command.tokenLength
								);
						}
						break;
				}
			}
			BigDecimal peek = values.pop();
			return peek.stripTrailingZeros();
		} catch (NoSuchElementException e) {
			throw new CalculatorException("Unfinished expression", 0, 0);
		}
	}

	///</editor-fold>

}
