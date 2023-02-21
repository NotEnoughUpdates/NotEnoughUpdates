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

import io.github.moulberry.notenoughupdates.NotEnoughUpdates
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.nio.charset.StandardCharsets

object ErrorUtil {
    @JvmStatic
    fun printCensoredStackTrace(e: Throwable, toCensor: List<String>): String {
        val baos = ByteArrayOutputStream()
        e.printStackTrace(PrintStream(baos, true, StandardCharsets.UTF_8.name()))
        var string = String(baos.toByteArray(), StandardCharsets.UTF_8)
        toCensor.forEach {
            string = string.replace(it, "*".repeat(it.length))
        }
        return string
    }

    @JvmStatic
    fun printStackTraceWithoutApiKey(e: Throwable): String {
        return printCensoredStackTrace(e, listOf(NotEnoughUpdates.INSTANCE.config.apiData.apiKey))
    }
}
