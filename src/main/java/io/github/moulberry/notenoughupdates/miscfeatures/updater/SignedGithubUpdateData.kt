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

import com.google.gson.JsonElement
import moe.nea.libautoupdate.GithubReleaseUpdateSource.GithubRelease
import moe.nea.libautoupdate.UpdateData
import java.io.ByteArrayInputStream
import java.net.URL

class SignedGithubUpdateData(
    versionName: String,
    versionNumber: JsonElement,
    sha256: String,
    download: String,
    val signatures: List<GithubRelease.Download>
) : UpdateData(
    versionName,
    versionNumber,
    sha256,
    download
) {
    override fun toString(): String {
        return "${super.toString()} + Signatures(signatures = ${signatures.map { it.name }}})"
    }

    fun verifyAnySignature(): Boolean {
        val signatories = validSignatories
        for (signatory in signatories) {
            println("Accepted signature from ${signatory.name}")
        }
        return signatories.size >= 2
    }

    val validSignatories by lazy {
        findValidSignatories()
    }


    private fun findValidSignatories(): List<GithubRelease.Download> {
        val signatures = signatures
        return signatures.filter { verifySignature(it) }
    }

    private fun verifySignature(signatureDownload: GithubRelease.Download): Boolean {
        val name = signatureDownload.name.substringBeforeLast('.').substringAfterLast("_")
        val signatureBytes = URL(signatureDownload.browserDownloadUrl).openStream().readBytes()
        val hashBytes = ByteArrayInputStream(sha256.uppercase().encodeToByteArray())
        return SigningPool.verifySignature(name, hashBytes, signatureBytes)
    }

}
