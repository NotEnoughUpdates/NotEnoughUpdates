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

import moe.nea.libautoupdate.GithubReleaseUpdateSource
import moe.nea.libautoupdate.UpdateData

class SigningGithubSource(username: String, repo: String) :
    GithubReleaseUpdateSource(username, repo) {

    val hashRegex = "sha256sum: `(?<hash>[a-fA-F0-9]{64})`".toPattern()
    override fun findAsset(release: GithubRelease): UpdateData? {
        val asset = super.findAsset(release) ?: return null
        val match = release.body.lines()
            .firstNotNullOfOrNull { line -> hashRegex.matcher(line).takeIf { it.matches() } }
            ?: return null
        // Inject our custom sha256sum
        return SignedGithubUpdateData(asset.versionName, asset.versionNumber, match.group("hash"), asset.download,
            release.assets.filter { it.name.endsWith(".asc") })
    }

}
