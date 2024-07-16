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

import io.github.moulberry.notenoughupdates.NotEnoughUpdates
import java.io.File
import java.io.InputStream
import java.net.URL
import java.util.jar.JarFile
import kotlin.io.path.toPath

object JarUtil {

    interface Access {
        fun listFiles(path: String): List<String>
        fun read(path: String): InputStream
    }

    val access = NotEnoughUpdates::class.java.protectionDomain.codeSource.location.let {
        if (it.protocol.equals("jar")) {
            val jarFilePath = URL(it.toString().split("!")[0].substring(4))
                .toURI().toPath().toFile()
            val jarFile = JarFile(jarFilePath)
            object : Access {
                override fun listFiles(path: String): List<String> {
                    return jarFile.entries().toList().filter { it.name.startsWith(path + "/") }
                        .filter { it.name.indexOf('/', path.length + 1) < 0 }
                        .map { it.name.substringAfterLast('/') }
                }

                override fun read(path: String): InputStream {
                    return jarFile.getInputStream(jarFile.getJarEntry(path))
                }
            }
        } else {
            val baseFilePath = it.toString().replace("\\", "/")
                .replace(NotEnoughUpdates::class.java.getCanonicalName().replace(".", "/") + ".class", "")
            val baseFile = URL(baseFilePath).toURI().toPath().toFile()
            object : Access {
                override fun listFiles(path: String): List<String> {
                    return baseFile.resolve(path).listFiles()?.map { it.name } ?: listOf()
                }

                override fun read(path: String): InputStream {
                    return baseFile.resolve(path).inputStream()
                }
            }
        }
    }

}
