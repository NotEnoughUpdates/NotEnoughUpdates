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

package io.github.moulberry.notenoughupdates.util

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject


fun Iterable<JsonElement>.toJsonArray(): JsonArray = JsonArray().also {
    for (jsonElement in this) {
        it.add(jsonElement)
    }
}

fun JsonObject.getIntOrValue(key: String, alternative: Int): Int {
    return if (has(key) && get(key).isJsonPrimitive && get(key).asJsonPrimitive.isNumber) {
        get(key).asInt
    } else {
        alternative
    }
}

fun JsonObject.getDoubleOrValue(key: String, alternative: Double): Double {
    return if (has(key) && get(key).isJsonPrimitive && get(key).asJsonPrimitive.isNumber) {
        get(key).asDouble
    } else {
        alternative
    }
}

fun JsonObject.getLongOrValue(key: String, alternative: Long): Long {
    return if (has(key) && get(key).isJsonPrimitive && get(key).asJsonPrimitive.isNumber) {
        get(key).asLong
    } else {
        alternative
    }
}
