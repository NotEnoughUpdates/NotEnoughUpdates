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

package io.github.moulberry.notenoughupdates.core.util;

import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class StringUtils {
	private final static DecimalFormat TENTHS_DECIMAL_FORMAT = new DecimalFormat("#.#");
	public static final NumberFormat NUMBER_FORMAT = NumberFormat.getInstance(Locale.US);

	public static String cleanColour(String in) {
		return in.replaceAll("(?i)\\u00A7.", "");
	}

	public static String cleanColourNotModifiers(String in) {
		return in.replaceAll("(?i)\\u00A7[0-9a-f]", "\u00A7r");
	}

	public static String substringBetween(String str, String open, String close) {
		return org.apache.commons.lang3.StringUtils.substringBetween(str, open, close);
	}

	public static int cleanAndParseInt(String str) {
		str = cleanColour(str);
		str = str.replace(",", "");
		return Integer.parseInt(str);
	}

	public static String shortNumberFormat(double n) {
		return shortNumberFormat(n, 0);
	}

	private static final char[] sizeSuffix = new char[]{'k', 'm', 'b', 't'};

	public static String shortNumberFormat(BigInteger bigInteger) {
		BigInteger THOUSAND = BigInteger.valueOf(1000);
		int i = -1;
		while (bigInteger.compareTo(THOUSAND) > 0 && i < sizeSuffix.length) {
			bigInteger = bigInteger.divide(THOUSAND);
			i++;
		}
		return bigInteger.toString() + (i == -1 ? "" : sizeSuffix[i]);
	}

	public static String shortNumberFormat(double n, int iteration) {
		if (n < 0) return "-" + shortNumberFormat(-n, iteration);
		if (n < 1000) {
			if (n % 1 == 0) {
				return Integer.toString((int) n);
			} else {
				return String.format("%.2f", n);
			}
		}

		double d = ((long) n / 100) / 10.0;
		boolean isRound = (d * 10) % 10 == 0;
		return d < 1000 ? (isRound || d > 9.99 ? (int) d * 10 / 10 : d + "") + "" + sizeSuffix[iteration] : shortNumberFormat(d, iteration + 1);
	}

	public static String removeLastWord(String string, String splitString) {
		try {
			String[] split = string.split(splitString);
			String rawTier = split[split.length - 1];
			return string.substring(0, string.length() - rawTier.length() - 1);
		} catch (StringIndexOutOfBoundsException e) {
			throw new RuntimeException("removeLastWord: '" + string + "'", e);
		}
	}

	public static String firstUpperLetter(String text) {
		if (text.isEmpty()) return text;
		String firstLetter = ("" + text.charAt(0)).toUpperCase();
		return firstLetter + text.substring(1);
	}

	public static boolean isNumeric(String string) {
		if (string == null || string.isEmpty()) {
			return false;
		}

		for (char c : string.toCharArray()) {
			if (!Character.isDigit(c)) {
				return false;
			}
		}
		return true;
	}

	public static String formatToTenths(Number num) {
		return TENTHS_DECIMAL_FORMAT.format(num);
	}

	public static String formatNumber(Number num) {
		return NUMBER_FORMAT.format(num);
	}
}
