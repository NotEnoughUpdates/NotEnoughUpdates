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

data class SkyBlockTime(
    val year: Int = 1,
    val month: Int = 1,
    val day: Int = 1,
    val hour: Int = 0,
    val minute: Int = 0,
    val second: Int = 0,
) {

    fun toRealTime(): Long {
        val skyBlockYear = 124 * 60 * 60.0
        val skyBlockMonth = skyBlockYear / 12
        val skyBlockDay = skyBlockMonth / 31
        val skyBlockHour = skyBlockDay / 24
        val skyBlockMinute = skyBlockHour / 60
        val skyBlockSecond = skyBlockMinute / 60

        var time = 0.0
        time += year * skyBlockYear
        time += (month - 1) * skyBlockMonth
        time += (day - 1) * skyBlockDay
        time += hour * skyBlockHour
        time += minute * skyBlockMinute
        time += second * skyBlockSecond
        time += 1559829300
        return time.toLong() * 1000
    }

    companion object {
        fun now(): SkyBlockTime {
            val skyBlockTimeZero = 1559829300000 // Day 1, Year 1
            var realMillis = (System.currentTimeMillis() - skyBlockTimeZero)

            val skyBlockYear = 124 * 60 * 60 * 1000
            val skyBlockMonth = skyBlockYear / 12
            val skyBlockDay = skyBlockMonth / 31
            val skyBlockHour = skyBlockDay / 24
            val skyBlockMinute = skyBlockHour / 60
            val skyBlockSecond = skyBlockMinute / 60

            fun getUnit(factor: Int): Int {
                val result = realMillis / factor
                realMillis %= factor
                return result.toInt()
            }

            val year = getUnit(skyBlockYear)
            val month = getUnit(skyBlockMonth) + 1
            val day = getUnit(skyBlockDay) + 1
            val hour = getUnit(skyBlockHour)
            val minute = getUnit(skyBlockMinute)
            val second = getUnit(skyBlockSecond)
            return SkyBlockTime(year, month, day, hour, minute, second)
        }

        fun monthName(month: Int): String {
            val prefix = when ((month - 1) % 3 + 1) {
                1 -> "Early "
                2 -> ""
                3 -> "Late "
                else -> "Undefined!"
            }

            val name = when (month / 4 + 1) {
                1 -> "Spring"
                2 -> "Summer"
                3 -> "Autumn"
                4 -> "Winter"
                else -> "lol"
            }

            return prefix + name
        }

        fun daySuffix(day: Int) = when (day % 10) {
            1 -> "st"
            2 -> "nd"
            3 -> "rd"
            else -> "th"
        }
    }
}