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

package neubs

import com.google.gson.*
import org.apache.tools.ant.filters.BaseFilterReader
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.bundling.Zip
import java.io.Reader

class RefmapFilter(reader: Reader) : BaseFilterReader(reader) {

    fun canonicalizeJsonOrder(jsonElement: JsonElement): JsonElement {
        return when (jsonElement) {
            is JsonObject -> JsonObject().also { jsonObject ->
                jsonElement.entrySet().sortedBy { it.key }
                    .forEach { jsonObject.add(it.key, canonicalizeJsonOrder(it.value)) }
            }

            is JsonArray -> JsonArray().also { jsonArray ->
                jsonElement.forEach { jsonArray.add(canonicalizeJsonOrder(it)) }
            }

            else -> jsonElement
        }
    }

    var index = 0

    val remapped by lazy {
        val originalJson = readFully()
        val json = Gson().fromJson(originalJson, JsonObject::class.java)
        val canonicalized = canonicalizeJsonOrder(json)
        GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create()
            .toJson(canonicalized)
    }

    override fun read(): Int {
        if (index in remapped.indices) {
            return remapped[index++].code
        }
        return -1
    }
}

class RefmapFilterPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.afterEvaluate {
            val task = target.tasks.getByPath("jar") as Zip
            task.filesMatching(listOf("mixins.notenoughupdates.refmap.json")) {
                filter(RefmapFilter::class.java)
            }
        }
    }
}
