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

package io.github.moulberry.notenoughupdates.util.kotlin

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type


inline fun <reified T : Any> typeToken(): Type {
    return (object : TypeToken<T>() {}).type
}

inline fun <reified T : Any> Gson.fromJson(string: String): T {
    return fromJson(string, typeToken<T>())
}

operator fun JsonObject.set(name: String, value: Number) {
    this.addProperty(name, value)
}

operator fun JsonObject.set(name: String, value: String) {
    this.addProperty(name, value)
}

operator fun JsonObject.set(name: String, value: Boolean) {
    this.addProperty(name, value)
}

operator fun JsonObject.set(name: String, value: Char) {
    this.addProperty(name, value)
}

operator fun JsonObject.set(name: String, value: JsonElement) {
    this.add(name, value)
}
