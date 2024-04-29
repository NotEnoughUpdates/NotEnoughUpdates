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

package io.github.moulberry.notenoughupdates.util

import com.google.gson.Gson
import com.google.gson.TypeAdapter
import com.google.gson.TypeAdapterFactory
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import net.minecraft.util.BlockPos

object BlockPosTypeAdapterFactory : TypeAdapterFactory {
    object Adapter : TypeAdapter<BlockPos>() {
        override fun write(out: JsonWriter, value: BlockPos) {
            out.value("${value.x}:${value.y}:${value.z}")
        }

        override fun read(reader: JsonReader): BlockPos {
            val (x, y, z) = reader.nextString().split(":")
            return BlockPos(
                x.toInt(),
                y.toInt(),
                z.toInt(),
            )
        }
    }

    override fun <T : Any?> create(gson: Gson, type: TypeToken<T>): TypeAdapter<T>? {
        if (BlockPos::class.java.isAssignableFrom(type.rawType))
            return Adapter as TypeAdapter<T>
        return null
    }
}
