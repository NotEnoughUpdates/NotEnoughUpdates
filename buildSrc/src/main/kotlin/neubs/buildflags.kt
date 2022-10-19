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

package neubs

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.the
import java.nio.charset.StandardCharsets
import java.util.*

const val NEU_BUILDFLAGS_PREFIX = "neu.buildflags."

class NEUBuildFlags : Plugin<Project> {
    override fun apply(target: Project) {
        val props =
            target.properties.filterKeys { it.startsWith(NEU_BUILDFLAGS_PREFIX) }.mapValues { it.value as String }
        target.extensions.add("buildflags", Extension(props))
        target.tasks.create("generateBuildFlags") {
            outputs.upToDateWhen { false }
            val t = target.layout.buildDirectory.file("buildflags.properties")
            outputs.file(t)
            doLast {
                val p = Properties()
                p.putAll(props)
                t.get().asFile.writer(StandardCharsets.UTF_8).use {
                    p.store(it, "Store build time configuration for NEU")
                }
            }

        }
    }

    class Extension(val props: Map<String, String>) {
        fun bool(name: String) = props["$NEU_BUILDFLAGS_PREFIX$name"] == "true"
    }
}

val Project.buildFlags: NEUBuildFlags.Extension
    get() = the<NEUBuildFlags.Extension>()
