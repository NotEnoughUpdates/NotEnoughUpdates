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

package io.github.moulberry.notenoughupdates.miscgui.pricegraph

import com.google.gson.GsonBuilder
import io.github.moulberry.notenoughupdates.NotEnoughUpdates
import java.time.Instant
import java.util.concurrent.CompletableFuture

object ServerGraphDataProvider : GraphDataProvider {
    private val gson = GsonBuilder().create()

    override fun loadData(itemId: String): CompletableFuture<Map<Instant, PriceObject>?> {
        return CompletableFuture.supplyAsync {
            val request = NotEnoughUpdates.INSTANCE.manager.apiUtils.request()
                .url("https://${NotEnoughUpdates.INSTANCE.config.ahGraph.serverUrl}")
                .queryArgument("item", itemId).requestJson().get()?.asJsonObject ?: return@supplyAsync null

            val response = mutableMapOf<Instant, PriceObject>()
            for (element in request.entrySet()) response[Instant.parse(element.key)] =
                gson.fromJson(element.value, PriceObject::class.java)
            return@supplyAsync response
        }
    }
}
