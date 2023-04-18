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

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Instant

class SkyBlockTimeTest {
    @Test
    fun someRandomTimestamp() {
        val sbt = SkyBlockTime.fromInstant(Instant.ofEpochSecond(1676236163L))
        assertEquals(SkyBlockTime(260, 10, 7, 17, 15, 38), sbt)
        assertEquals("Early Winter", sbt.monthName)
    }

    @Test
    fun isReversibleEquivalent() {
        val ts = 167623132230L
        val someTimeStamp = Instant.ofEpochSecond(ts)
        assertEquals(someTimeStamp.toEpochMilli(), SkyBlockTime.fromInstant(someTimeStamp).toMillis())
    }

    @Test
    fun monthNames() {
        assertEquals(
            listOf(
                "Early Spring",
                "Spring",
                "Late Spring",
                "Early Summer",
                "Summer",
                "Late Summer",
                "Early Autumn",
                "Autumn",
                "Late Autumn",
                "Early Winter",
                "Winter",
                "Late Winter"
            ), (1..12).map { SkyBlockTime.monthName(it) })
    }

    @Test
    fun monthInverse() {
        (1..12).forEach {
            assertEquals(it, SkyBlockTime.monthNameToInt(SkyBlockTime.monthName(it)))
        }
    }

    @Test
    fun theOriginOfTime() {
        assertEquals(SkyBlockTime(year = 0), SkyBlockTime.fromInstant(Instant.ofEpochMilli(1559829300000L)))
    }


}
