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

import com.google.common.reflect.TypeToken
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import io.github.moulberry.notenoughupdates.NotEnoughUpdates
import java.io.*
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicBoolean
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

object LocalGraphDataProvider : GraphDataProvider {
    private val priceDir = File("config/notenoughupdates/prices")
    private val GSON = GsonBuilder().create()
    private val format = SimpleDateFormat("dd-MM-yyyy")
    private val config = NotEnoughUpdates.INSTANCE.config
    private val fileLocked = AtomicBoolean(false)

    override fun loadData(itemId: String): CompletableFuture<Map<Instant, PriceObject>?> {
        return CompletableFuture.supplyAsync {
            if (!priceDir.exists() || !priceDir.isDirectory) return@supplyAsync null
            if (fileLocked.get()) while (fileLocked.get()) { // Wait for file to become unlocked
                Thread.sleep(100)
            }
            val response = mutableMapOf<Instant, PriceObject>()
            val futures = mutableListOf<CompletableFuture<Map<Instant, PriceObject>?>>()
            for (file in priceDir.listFiles { file -> file.extension == "gz" }!!) {
                futures.add(CompletableFuture.supplyAsync {
                    val data = load(file)?.get(itemId) ?: return@supplyAsync null
                    (if (data.isBz()) data.bz?.map {
                        Instant.ofEpochSecond(it.key) to PriceObject(it.value.b, it.value.s)
                    } else data.ah?.map {
                        Instant.ofEpochSecond(it.key) to PriceObject(it.value.toDouble(), null)
                    })?.toMap()
                })
            }

            for (future in futures) {
                val result = future.get()
                if (result != null) response.putAll(result)
            }

            return@supplyAsync response
        }
    }

    private fun load(file: File): MutableMap<String, ItemData>? {
        val type = object : TypeToken<MutableMap<String, ItemData>?>() {}.type
        if (file.exists()) {
            try {
                BufferedReader(
                    InputStreamReader(
                        GZIPInputStream(FileInputStream(file)),
                        StandardCharsets.UTF_8
                    )
                ).use { reader ->
                    return GSON.fromJson(
                        reader,
                        type
                    )
                }
            } catch (e: Exception) {
                println("Deleting " + file.name + " because it is probably corrupted.")
                file.delete()
            }
        }
        return null
    }

    fun savePrices(items: JsonObject, bazaar: Boolean) {
        try {
            if (!priceDir.exists() && !priceDir.mkdir()) return
            val files = priceDir.listFiles()
            val dataRetentionTime =
                System.currentTimeMillis() - config.ahGraph.dataRetention * 86400000L
            files?.filter { it.extension == "gz" && it.lastModified() < dataRetentionTime }?.forEach { it.delete() }

            if (!config.ahGraph.graphEnabled || config.ahGraph.dataSource != 1) return
            if (fileLocked.get()) while (fileLocked.get()) { // Wait for file to become unlocked
                Thread.sleep(100)
            }
            fileLocked.set(true)

            val date = Date()
            val epochSecond = date.toInstant().epochSecond
            val file = File(priceDir, "prices_" + format.format(date) + ".gz")
            var prices = load(file) ?: mutableMapOf()
            if (file.exists()) {
                val tempPrices = load(file)
                if (tempPrices != null) prices = tempPrices
            }
            for (item in items.entrySet()) {
                addOrUpdateItemPriceInfo(item, prices, epochSecond, bazaar)
            }
            file.createNewFile()
            BufferedWriter(
                OutputStreamWriter(
                    GZIPOutputStream(FileOutputStream(file)),
                    StandardCharsets.UTF_8
                )
            ).use { writer -> writer.write(GSON.toJson(prices)) }
            fileLocked.set(false)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            fileLocked.set(false)
        }
    }

    private fun addOrUpdateItemPriceInfo(
        item: Map.Entry<String, JsonElement>,
        prices: MutableMap<String, ItemData>,
        timestamp: Long,
        bazaar: Boolean
    ) {
        val itemName = item.key
        var existingItemData: ItemData? = null
        if (prices.containsKey(itemName)) {
            existingItemData = prices[itemName]
        }

        // Handle transitions from ah to bz (the other direction typically doesn't happen)
        if (existingItemData != null) {
            if (existingItemData.isBz() && !bazaar) {
                return
            }
            if (!existingItemData.isBz() && bazaar) {
                prices.remove(itemName)
                existingItemData = null
            }
        }
        if (bazaar) {
            if (!item.value.asJsonObject.has("curr_buy") ||
                !item.value.asJsonObject.has("curr_sell")
            ) {
                return
            }
            val bzData = BzData(
                item.value.asJsonObject["curr_buy"].asDouble,
                item.value.asJsonObject["curr_sell"].asDouble
            )
            if (existingItemData != null) {
                existingItemData.bz?.set(timestamp, bzData)
            } else {
                val mapData = mutableMapOf(timestamp to bzData)
                prices[item.key] = ItemData(bz = mapData)
            }
        } else {
            if (existingItemData != null) {
                prices[item.key]!!.ah?.set(timestamp, item.value.asBigDecimal.toLong())
            } else {
                val mapData = mutableMapOf(timestamp to item.value.asLong)
                prices[item.key] = ItemData(ah = mapData)
            }
        }
    }
}


private data class ItemData(val ah: MutableMap<Long, Long>? = null, val bz: MutableMap<Long, BzData>? = null) {

    fun get(): MutableMap<Long, *>? = if (!isBz()) ah else bz

    fun isBz() = !bz.isNullOrEmpty()
}

private class BzData(val b: Double, val s: Double)

