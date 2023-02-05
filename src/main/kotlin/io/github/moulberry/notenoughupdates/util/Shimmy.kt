/*
 * Copyright (C) 2022 Linnea Gräf
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

import com.google.gson.Gson
import com.google.gson.JsonElement
import java.lang.reflect.Field

class Shimmy private constructor(
    val source: Any,
    val field: Field,
) {
    companion object {
        val gson = Gson()
        private fun shimmy(source: Any?, fieldName: String): Any? {
            if (source == null) return null
            return try {
                val declaredField = source.javaClass.getDeclaredField(fieldName)
                declaredField.isAccessible = true
                declaredField.get(source)
            } catch (e: NoSuchFieldException) {
                null
            }
        }

        @JvmStatic
        fun makeShimmy(source: Any?, path: List<String>): Shimmy? {
            if (path.isEmpty())
                return null
            var source = source
            for (part in path.dropLast(1)) {
                source = shimmy(source, part)
            }
            if (source == null) return null
            val lastName = path.last()
            return try {
                val field = source.javaClass.getDeclaredField(lastName)
                field.isAccessible = true
                Shimmy(
                    source,
                    field,
                )
            } catch (e: NoSuchFieldException) {
                null
            }
        }

    }

    val clazz: Class<*> = field.type
    fun get(): Any? {
        return field.get(source)
    }

    fun set(value: Any?) {
        field.set(source, value)
    }

    fun getJson(): JsonElement {
        return gson.toJsonTree(get())
    }

    fun setJson(element: JsonElement) {
        set(gson.fromJson(element, clazz))
    }


}
