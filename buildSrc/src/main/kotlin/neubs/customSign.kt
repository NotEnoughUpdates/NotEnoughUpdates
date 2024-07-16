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
import org.gradle.api.tasks.TaskAction
import java.security.KeyFactory
import java.security.Signature
import java.security.spec.PKCS8EncodedKeySpec

abstract class CustomSignTask : DefaultTask() {

    @TaskAction
    fun run() {
        println("Hash to sign: ")
        val hash = readLine()!!.trim().toUpperCase()
        require(hash.matches("[A-F0-9]{64}".toRegex())) { "Please provide a valid sha256 hash" }
        val secrets = project.file("secrets").listFiles()?.toList()
            ?.filter { !it.name.startsWith(".") } ?: emptyList()

        if (secrets.isEmpty()) error("Could not find any secret files.")
        secrets.forEach { require(it.name.endsWith(".der")) { "Invalid secret file ${it.name}" } }
        project.file("build/signatures").mkdirs()
        for (secret in secrets) {
            val keySpec = PKCS8EncodedKeySpec(secret.readBytes())
            val signature = Signature.getInstance("SHA256withRSA")
            signature.initSign(KeyFactory.getInstance("RSA").generatePrivate(keySpec))
            signature.update(hash.encodeToByteArray())
            val file = project.file("build/signatures/_${secret.nameWithoutExtension}.asc")
            file.writeBytes(signature.sign())
            println("Generated signature at ${file.absolutePath}")
        }
    }

    init {
        outputs.upToDateWhen { false }
    }
}
