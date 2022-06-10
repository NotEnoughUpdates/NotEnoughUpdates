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

import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Vec3ComparableTest {
	@Test
	void equals_false_when_null() {
		// Arrange
		Vec3Comparable vec3c = new Vec3Comparable(1.0, 2.0, 3.0);

		// Act
		boolean areEqual = vec3c.equals(null);

		// Assert
		assertFalse(areEqual);
	}

	@Test
	void equals_true_when_same_object() {
		// Arrange
		Vec3Comparable vec3c = new Vec3Comparable(1.0, 2.0, 3.0);

		// Act
		boolean areEqual = vec3c.equals(vec3c);

		// Assert
		assertTrue(areEqual);
	}

	@Test
	void equals_true_when_same_value() {
		// Arrange
		Vec3Comparable vec3c1 = new Vec3Comparable(1.0, 2.0, 3.0);
		Vec3Comparable vec3c2 = new Vec3Comparable(1.0, 2.0, 3.0);

		// Act
		boolean areEqual = vec3c1.equals(vec3c2);

		// Assert
		assertTrue(areEqual);
	}

	@Test
	void equals_false_when_vec3_equals() {
		// Arrange
		Vec3Comparable vec3c1 = new Vec3Comparable(1.0, 2.0, 3.0);
		Vec3 vec3c2 = new Vec3(1.0, 2.0, 3.0);

		// Act
		boolean areEqual = vec3c1.equals(vec3c2);

		// Assert
		assertFalse(areEqual);
	}

	@Test
	void equals_false_when_different_object_type() {
		// Arrange
		Vec3Comparable vec3c1 = new Vec3Comparable(1.0, 2.0, 3.0);
		BlockPos blockPos = new BlockPos(1.0, 2.0, 3.0);

		// Act
		boolean areEqual = vec3c1.equals(blockPos);

		// Assert
		assertFalse(areEqual);
	}

	@Test
	void equals_false_when_different_value() {
		// Arrange
		Vec3Comparable vec3c1 = new Vec3Comparable(1.0, 2.0, 3.0);
		Vec3Comparable vec3c2 = new Vec3Comparable(3.0, 2.0, 1.0);

		// Act
		boolean areEqual = vec3c1.equals(vec3c2);

		// Assert
		assertFalse(areEqual);
	}

	@Test
	void hashCode_same_when_same_value() {
		// Arrange
		Vec3Comparable vec3c1 = new Vec3Comparable(1.0, 2.0, 3.0);
		Vec3Comparable vec3c2 = new Vec3Comparable(1.0, 2.0, 3.0);

		// Act
		double vec3c1Hash = vec3c1.hashCode();
		double vec3c2Hash = vec3c2.hashCode();

		// Assert
		assertEquals(vec3c1Hash, vec3c2Hash);
	}

	@Test
	void hashCode_different_when_different_value() {
		// Arrange
		Vec3Comparable vec3c1 = new Vec3Comparable(1.0, 2.0, 3.0);
		Vec3Comparable vec3c2 = new Vec3Comparable(3.0, 2.0, 1.0);

		// Act
		double vec3c1Hash = vec3c1.hashCode();
		double vec3c2Hash = vec3c2.hashCode();

		// Assert
		assertNotEquals(vec3c1Hash, vec3c2Hash);
	}

	@Test
	void compareTo_zero_when_equal() {
		// Arrange
		Vec3Comparable vec3c1 = new Vec3Comparable(1.0, 2.0, 3.0);
		Vec3Comparable vec3c2 = new Vec3Comparable(1.0, 2.0, 3.0);

		// Act
		int result = vec3c1.compareTo(vec3c2);

		// Assert
		assertEquals(0, result);
	}

	@Test
	void compareTo_negative_when_lower() {
		// Arrange
		Vec3Comparable vec3c1 = new Vec3Comparable(0.0, 2.0, 3.0);
		Vec3Comparable vec3c2 = new Vec3Comparable(1.0, 2.0, 3.0);

		// Act
		int result = vec3c1.compareTo(vec3c2);

		// Assert
		assertTrue(result < 0);
	}

	@Test
	void compareTo_positive_when_x_y_or_z_is_higher() {
		// Arrange
		Vec3Comparable vec3c1x = new Vec3Comparable(2.0, 2.0, 3.0);
		Vec3Comparable vec3c2x = new Vec3Comparable(1.0, 2.0, 3.0);
		Vec3Comparable vec3c1y = new Vec3Comparable(1.0, 3.0, 3.0);
		Vec3Comparable vec3c2y = new Vec3Comparable(1.0, 2.0, 3.0);
		Vec3Comparable vec3c1z = new Vec3Comparable(1.0, 2.0, 4.0);
		Vec3Comparable vec3c2z = new Vec3Comparable(1.0, 2.0, 3.0);

		// Act
		int resultX = vec3c1x.compareTo(vec3c2x);
		int resultY = vec3c1y.compareTo(vec3c2y);
		int resultZ = vec3c1z.compareTo(vec3c2z);

		// Assert
		assertTrue(resultX > 0);
		assertTrue(resultY > 0);
		assertTrue(resultZ > 0);
	}

	@Test
	void compareTo_throws_on_null() {
		// Arrange
		Vec3Comparable vec3c = new Vec3Comparable(1.0, 2.0, 3.0);

		// Act & Assert
		Assertions.assertThrows(NullPointerException.class, () -> {
			vec3c.compareTo(null);
		});
	}

	@Test
	void signumEquals_is_true_when_all_signums_match() {
		Vec3Comparable first = new Vec3Comparable(-1.0, 1.0, 0);
		Vec3 second = new Vec3(-1.0, 1.0, 0);
		Assertions.assertTrue(first.signumEquals(second));
	}

	@Test
	void signumEquals_is_false_when_any_signum_differs() {
		Vec3Comparable first = new Vec3Comparable(-1.0, 1.0, 0);
		Vec3 second = new Vec3(-1.0, 1.0, 1.0);
		Vec3 third = new Vec3(-1.0, -1.0, 0);
		Vec3 fourth = new Vec3(1.0, 1.0, 1.0);
		Assertions.assertFalse(first.signumEquals(second));
		Assertions.assertFalse(first.signumEquals(third));
		Assertions.assertFalse(first.signumEquals(fourth));
	}
}
