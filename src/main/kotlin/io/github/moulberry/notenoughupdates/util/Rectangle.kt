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

/**
 * An axis aligned rectangle in the following coordinate space:
 *
 *  * The top direction is towards y=-INF
 *  * The bottom direction is towards y=+INF
 *  * The right direction is towards x=+INF
 *  * The left direction is towards x=-INF
 */
data class Rectangle(
    val x: Int, val y: Int,
    val width: Int, val height: Int,
) {
    /**
     * The left edge of this rectangle (Low X)
     */
    val left get() = x

    /**
     * The right edge of this rectangle (High X)
     */
    val right get() = x + width

    /**
     * The top edge of this rectangle (Low X)
     */
    val top get() = y

    /**
     * The bottom edge of this rectangle (High X)
     */
    val bottom get() = y + height

    init {
        require(width >= 0)
        require(height >= 0)
    }

    /**
     * Check for intersections between two rectangles. Two rectangles with perfectly aligned edges do *not* count as
     * intersecting.
     */
    fun intersects(other: Rectangle): Boolean {
        val intersectsX = !(right <= other.left || left >= other.right)
        val intersectsY = !(top >= other.bottom || bottom <= other.top)
        return intersectsX && intersectsY
    }

    /**
     * Check if this rectangle contains the given coordinate
     */
    fun contains(x1: Int, y1: Int) :Boolean{
        return left <= x1 && x1 < left + width && top <= y1 && y1 < top + height
    }
}
