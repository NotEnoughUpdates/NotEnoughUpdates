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

package io.github.moulberry.notenoughupdates.api

import io.github.moulberry.notenoughupdates.NotEnoughUpdates
import io.github.moulberry.notenoughupdates.autosubscribe.NEUAutoSubscribe
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import java.util.concurrent.CompletableFuture

@NEUAutoSubscribe
object HypixelAPICache {
    private const val cacheTime = 60_000 * 5

    private val userProfilesCache = mutableMapOf<String, CompletableFuture<String>>()
    private val LOGGER = LogManager.getLogger("NotEnoughUpdates API Cache")

    private var lastResetTime = 0L
    private var dirty = false

    fun getCacheFor(link: String): CompletableFuture<String>? {
        if (!shouldCache(link)) return null

        log("getCacheFor $link")
        printStackTrace()
        log("")

        if (dirty || System.currentTimeMillis() > lastResetTime + cacheTime) {
            resetCache()
        }
        val result = userProfilesCache.getOrDefault(link, null)
        if (result == null) {
            log("No cache found, make a new request!")
            println()
        } else {
            log("Using cache.")
        }
        return result
    }

    fun addToCache(link: String, data: CompletableFuture<String>) {
        if (shouldCache(link)) {
            log("addToCache $link")
            userProfilesCache[link] = data
        }
    }

    private fun shouldCache(link: String) =
        link.startsWith("https://api.hypixel.net") && !link.contains("skyblock/bazaar")

    private fun resetCache() {
        dirty = false
        userProfilesCache.clear()
        lastResetTime = System.currentTimeMillis()
        log("resetCache")
    }

    @SubscribeEvent
    fun onWorldChange(event: WorldEvent.Load) {
        dirty = true
    }

    private fun printStackTrace() {
        for (element in Thread.currentThread().stackTrace) {
            val className = element.className
            if (!className.startsWith("io.github.moulberry.notenoughupdates.")) continue
            if (className.endsWith(this::class.java.simpleName)) continue
            if (className.endsWith("ApiUtil\$Request")) continue

            log("$element")
        }
    }

    private fun log(text: String) {
        if (NotEnoughUpdates.INSTANCE.config.hidden.logApiCalls) {
            LOGGER.log(Level.INFO, text)
        }
    }
}
