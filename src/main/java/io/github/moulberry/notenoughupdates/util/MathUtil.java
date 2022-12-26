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

public class MathUtil {
	public static boolean isDecimalPartApproximately(double fullValue, double expectedDecimalPart) {
		return Math.abs(Math.abs(fullValue % 1) - expectedDecimalPart) < 0.01;
	}
	public static boolean basicallyEqual(double num1, double num2, double dist) {
		return Math.abs(num1 - num2) < dist;
	}
	public static boolean basicallyEqual(double num1, double num2) {
		return Math.abs(num1 - num2) < 0.01;
	}
}
