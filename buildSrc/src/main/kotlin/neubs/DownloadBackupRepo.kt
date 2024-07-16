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

package neubs

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.net.URL

abstract class DownloadBackupRepo : DefaultTask() {

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @get:Input
    abstract val branch: Property<String>

    @get:Internal
    val repoFile get() = outputDirectory.get().asFile.resolve("assets/notenoughupdates/repo.zip")

    @TaskAction
    fun downloadRepo() {
        val url =
            URL("https://github.com/NotEnoughUpdates/NotEnoughUpdates-REPO/archive/refs/heads/${branch.get()}.zip")
        val file = repoFile
        file.parentFile.mkdirs()
        file.outputStream().use { out ->
            url.openStream().use { inp ->
                inp.copyTo(out)
            }
        }
    }
}
