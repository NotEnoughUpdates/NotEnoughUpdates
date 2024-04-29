/*
 * Copyright (C) 2024 NotEnoughUpdates contributors
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

package io.github.moulberry.notenoughupdates.miscgui

import com.google.gson.*
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import io.github.moulberry.notenoughupdates.util.kotlin.ExtraData
import io.github.moulberry.notenoughupdates.util.kotlin.KSerializable
import io.github.moulberry.notenoughupdates.util.kotlin.fromJson
import moe.nea.lisp.LispAst
import moe.nea.lisp.LispParser

@KSerializable
data class HotmTreeLayoutFile(
    val hotm: HotmTreeLayout,
    val prelude: List<String> = listOf()
)

data class LoreLine(val text: String, val condition: LispAst.Program?)

object LoreLineSerializer : TypeAdapterFactory {
    override fun <T : Any?> create(gson: Gson, type: TypeToken<T>): TypeAdapter<T>? {
        if (type.rawType.isAssignableFrom(LoreLine::class.java)) {
            return object : TypeAdapter<LoreLine>() {
                override fun write(out: JsonWriter?, value: LoreLine?) {
                    TODO("Not yet implemented")
                }

                override fun read(reader: JsonReader): LoreLine {
                    when (reader.peek()) {
                        JsonToken.BEGIN_OBJECT -> {
                            val obj = gson.fromJson<JsonObject>(reader)
                            return LoreLine(obj["text"].asString, LispParser.parse("<json>", obj["onlyIf"].asString))
                        }

                        JsonToken.STRING -> return LoreLine(reader.nextString(), null)
                        else -> throw JsonParseException("Expected object or string when parsing lore line")
                    }
                }

            } as TypeAdapter<T>
        }
        return null
    }
}

object LispProgramSerializer : TypeAdapterFactory {
    override fun <T : Any?> create(gson: Gson, type: TypeToken<T>): TypeAdapter<T>? {
        if (type.rawType.isAssignableFrom(LispAst.Program::class.java)) {
            return object : TypeAdapter<LispAst.Program>() {
                override fun write(out: JsonWriter, value: LispAst.Program) {
                    out.value(value.toSource())
                }

                override fun read(reader: JsonReader): LispAst.Program {
                    return LispParser.parse("<json-source>", reader.nextString())
                }

            } as TypeAdapter<T>
        }
        return null
    }
}

@KSerializable
data class HotmTreeLayout(
    val perks: Map<String, LayoutedHotmPerk>,
    val powders: Map<String, PowderType>,
)

@KSerializable
data class PowderType(
    val costLine: String,
)

@KSerializable
data class LayoutedHotmPerk(
    val name: String,
    val x: Int,
    val y: Int,
    val maxLevel: Int,
    val powder: LispAst.Program,
    val cost: LispAst.Program,
    val lore: List<LoreLine>,
    @ExtraData
    val extras: Map<String, JsonElement>
) {

    val compiledFunctions: Map<String, LispAst.Program> = extras.mapValues {
        LispParser.parse("hotmlayout.json:perk:$name:${it.key}", it.value.asString)
    } + mapOf(
        "cost" to cost
    )
}
