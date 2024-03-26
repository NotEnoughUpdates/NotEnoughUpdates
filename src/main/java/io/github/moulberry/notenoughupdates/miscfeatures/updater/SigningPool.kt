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

package io.github.moulberry.notenoughupdates.miscfeatures.updater

import io.github.moulberry.notenoughupdates.util.JarUtil
import java.io.InputStream
import java.security.KeyFactory
import java.security.PublicKey
import java.security.Signature
import java.security.spec.X509EncodedKeySpec


object SigningPool {

    data class PK(
        val name: String,
        val publicKey: PublicKey,
    )

    fun load(): List<PK> {
        val l = JarUtil.access.listFiles("trusted_team_members")
        return l.filter { it.endsWith(".key") }
            .map {
                loadPK(
                    it.substringBeforeLast('.'),
                    JarUtil.access.read("trusted_team_members/$it")
                )
            }
    }

    fun loadPK(name: String, inputStream: InputStream): PK {
        val publicKeyBytes = inputStream.readBytes()
        val x509EncodedKeySpec = X509EncodedKeySpec(publicKeyBytes)
        return PK(name, KeyFactory.getInstance("RSA").generatePublic(x509EncodedKeySpec))
    }

    val keyPool = load().associateBy { it.name }

    fun verifySignature(name: String, inputStream: InputStream, signatureBytes: ByteArray): Boolean {
        val signature = Signature.getInstance("SHA256withRSA")
        signature.initVerify(keyPool[name]?.publicKey ?: return false)

        val b = ByteArray(4096)
        while (true) {
            val read = inputStream.read(b)
            if (read < 0) break
            signature.update(b, 0, read)
        }
        return signature.verify(signatureBytes)
    }
}
