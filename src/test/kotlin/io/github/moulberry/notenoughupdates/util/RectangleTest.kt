/*
 * Copyright (C) 2023 NotEnoughUpdates contributors
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

package io.github.moulberry.notenoughupdates.util

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class RectangleTest {

    @Test
    fun testNoNegativeSizes() {
        assertThrows<IllegalArgumentException> {
            Rectangle(0, 0, -1, 0)
        }
        assertThrows<IllegalArgumentException> {
            Rectangle(0, 0, 0, -1)
        }
    }

    @Test
    fun testOverlaps() {
        val topLeft = Rectangle(0, 0, 10, 10)
        assertTrue(topLeft.intersects(topLeft))
        assertTrue(topLeft.intersects(Rectangle(9, 2, 1, 1)))
        assertTrue(topLeft.intersects(Rectangle(-2, -2, 4, 4)))
        assertTrue(topLeft.intersects(Rectangle(4, 4, 1, 1)))
        assertFalse(topLeft.intersects(Rectangle(-2,-2, 1,1)))
        assertFalse(topLeft.intersects(Rectangle(-2,-2, 2,2)))
    }
}
