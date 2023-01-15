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

package nontests

import net.minecraft.nbt.NBTTagCompound
import kotlin.random.Random
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime


fun main() {
    val simple = NBTTagCompound().apply {
        setTag("ExtraAttributes", NBTTagCompound().apply {
            setString("uuid", "08248f07-41c1-4353-8e37-5225939cb559")
        })
    }
    val complex = simple.copy() as NBTTagCompound
    for (i in 0..100) {
        complex.setInteger("tag$i", Random.nextInt())
    }
    testPerf(simple, "Simple")
    testPerf(complex, "Complex")

}

@OptIn(ExperimentalTime::class)
fun testPerf(tag: NBTTagCompound, name: String) {
    val hashCodeTime = measureTime {
        for (i in 0..100000) {
            tag.hashCode()
        }
    }
    val accessTime = measureTime {
        for (i in 0..100000) {
            tag.getCompoundTag("ExtraAttributes").getString("uuid")
        }
    }
    println("$name - hashCode: $hashCodeTime")
    println("$name - accesTime: $accessTime")
}




