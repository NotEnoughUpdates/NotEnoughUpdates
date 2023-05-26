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

package io.github.moulberry.notenoughupdates.util.hypixelapi

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName
import io.github.moulberry.notenoughupdates.NotEnoughUpdates
import io.github.moulberry.notenoughupdates.util.kotlin.ExtraData
import io.github.moulberry.notenoughupdates.util.kotlin.KSerializable
import io.github.moulberry.notenoughupdates.util.kotlin.KSerializedName

object HypixelItemAPI {
    @KSerializable
    data class ApiResponse(
        val items: List<ApiItem>,
    )

    @KSerializable
    data class ApiItem(
        val id: String,
        val name: String,
        @param:KSerializedName("npc_sell_price")
        val npcSellPrice: Double? = null,
        @param:ExtraData
        val extraData: Map<String, JsonElement>,
    )

    var itemData: Map<String, ApiItem> = mapOf()
        private set

    @JvmStatic
    fun getNPCSellPrice(itemId: String) = itemData[itemId]?.npcSellPrice

    fun loadItemData() {
        NotEnoughUpdates.INSTANCE.manager.apiUtils.newAnonymousHypixelApiRequest("resources/skyblock/items")
            .requestJson(ApiResponse::class.java)
            .thenAccept {
                itemData = it.items.associateBy { it.id }
            }
    }
}
