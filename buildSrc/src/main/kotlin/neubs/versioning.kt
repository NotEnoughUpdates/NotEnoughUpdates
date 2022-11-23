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

package neubs

import org.gradle.api.Project
import java.io.ByteArrayOutputStream

fun Project.setVersionFromEnvironment(baseVersion: String) {
    val buildExtra = mutableListOf<String>()
    val buildVersion = properties["BUILD_VERSION"] as? String
    if (buildVersion != null) buildExtra.add(buildVersion)
    if (System.getenv("CI") == "true") buildExtra.add("ci")

    val stdout = ByteArrayOutputStream()
    val execResult = exec {
        commandLine("git", "describe", "--always", "--first-parent", "--abbrev=7")
        standardOutput = stdout
        isIgnoreExitValue = true
    }
    if (execResult.exitValue == 0) {
        buildExtra.add(String(stdout.toByteArray()).trim())
    }

    val gitDiffStdout = ByteArrayOutputStream()
    val gitDiffResult = exec {
        commandLine("git", "status", "--porcelain")
        standardOutput = gitDiffStdout
        isIgnoreExitValue = true
    }
    if (gitDiffResult.exitValue == 0 && gitDiffStdout.toByteArray().isNotEmpty()) {
        buildExtra.add("dirty")
    }

    version = baseVersion + (if (buildExtra.isEmpty()) "" else buildExtra.joinToString(prefix = "+", separator = "."))

}

