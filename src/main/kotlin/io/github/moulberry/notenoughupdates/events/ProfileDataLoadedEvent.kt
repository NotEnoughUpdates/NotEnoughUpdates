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
package io.github.moulberry.notenoughupdates.events

import com.google.gson.JsonObject

//TODO extend the usage of this event (accessory bag and storage data)
class ProfileDataLoadedEvent(val uuid: String, val data: JsonObject?) : NEUEvent() {
    val profileInfo: JsonObject? by lazy { readProfileInfo() }

    private fun readProfileInfo(): JsonObject? {
        if (data == null) return null

        val skyblockProfiles = data["profiles"].asJsonArray
        for (profileEle in skyblockProfiles) {
            val profile = profileEle.asJsonObject
            if (!profile.has("members")) continue
            val members = profile["members"].asJsonObject
            if (!members.has(uuid)) continue
            if (profile.has("selected") && profile["selected"].asBoolean) {
                return members[uuid].asJsonObject
            }
        }
        return null
    }
}
