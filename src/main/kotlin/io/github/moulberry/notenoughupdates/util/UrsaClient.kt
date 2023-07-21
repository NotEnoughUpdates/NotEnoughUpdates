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

package io.github.moulberry.notenoughupdates.util

import com.google.gson.JsonObject
import io.github.moulberry.notenoughupdates.NotEnoughUpdates
import io.github.moulberry.notenoughupdates.autosubscribe.NEUAutoSubscribe
import io.github.moulberry.notenoughupdates.options.customtypes.NEUDebugFlag
import io.github.moulberry.notenoughupdates.util.kotlin.Coroutines.await
import io.github.moulberry.notenoughupdates.util.kotlin.Coroutines.continueOn
import io.github.moulberry.notenoughupdates.util.kotlin.Coroutines.launchCoroutine
import net.minecraft.client.Minecraft
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import java.time.Duration
import java.time.Instant
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentLinkedQueue

class UrsaClient(val apiUtil: ApiUtil) {
    private data class Token(
        val validUntil: Instant,
        val token: String,
        val obtainedFrom: String,
    ) {
        val isValid get() = Instant.now().plusSeconds(60) < validUntil
    }

    val logger = NEUDebugFlag.API_CACHE

    // Needs synchronized access
    private var token: Token? = null
    private var isPollingForToken = false

    private data class Request<T>(
        val path: String,
        val objectMapping: Class<T>?,
        val consumer: CompletableFuture<T>,
    )

    private val queue = ConcurrentLinkedQueue<Request<*>>()
    private val ursaRoot
        get() = NotEnoughUpdates.INSTANCE.config.apiData.ursaApi.removeSuffix("/").takeIf { it.isNotBlank() }
            ?: "https://ursa.notenoughupdates.org"

    private suspend fun authorizeRequest(usedUrsaRoot: String, connection: ApiUtil.Request, t: Token?) {
        if (t != null && t.obtainedFrom == usedUrsaRoot) {
            logger.log("Authorizing request using token")
            connection.header("x-ursa-token", t.token)
        } else {
            logger.log("Authorizing request using username and serverId")
            val serverId = UUID.randomUUID().toString()
            val session = Minecraft.getMinecraft().session
            val name = session.username
            connection.header("x-ursa-username", name).header("x-ursa-serverid", serverId)
            continueOn(MinecraftExecutor.OffThread)
            Minecraft.getMinecraft().sessionService.joinServer(session.profile, session.token, serverId)
            logger.log("Authorizing request using username and serverId complete")
        }
    }

    private suspend fun saveToken(usedUrsaRoot: String, connection: ApiUtil.Request) {
        logger.log("Attempting to save token")
        val token =
            connection.responseHeaders["x-ursa-token"]?.firstOrNull()
        val validUntil = connection.responseHeaders["x-ursa-expires"]
            ?.firstOrNull()
            ?.toLongOrNull()
            ?.let { Instant.ofEpochMilli(it) } ?: (Instant.now() + Duration.ofMinutes(55))
        continueOn(MinecraftExecutor.OnThread)
        if (token == null) {
            isPollingForToken = false
            logger.log("No token found. Marking as non polling")
        } else {
            this.token = Token(validUntil, token, usedUrsaRoot)
            isPollingForToken = false
            logger.log("Token saving successful")
        }
    }

    private suspend fun <T> performRequest(request: Request<T>, token: Token?) {
        val usedUrsaRoot = ursaRoot
        val apiRequest = apiUtil.request().url("$usedUrsaRoot/${request.path}")
        try {
            logger.log("Ursa Request started")
            authorizeRequest(usedUrsaRoot, apiRequest, token)
            val response =
                if (request.objectMapping == null)
                    (apiRequest.requestString().await() as T)
                else
                    (apiRequest.requestJson(request.objectMapping).await() as T)
            logger.log("Request completed")
            saveToken(usedUrsaRoot, apiRequest)
            request.consumer.complete(response)
        } catch (e: Exception) {
            e.printStackTrace()
            logger.log("Request failed")
            continueOn(MinecraftExecutor.OnThread)
            isPollingForToken = false
            request.consumer.completeExceptionally(e)
        }
    }

    private fun bumpRequests() {
        while (!queue.isEmpty()) {
            if (isPollingForToken) return
            val nextRequest = queue.poll()
            if (nextRequest == null) {
                logger.log("No request to bump found")
                return
            }
            logger.log("Request found")
            var t = token
            if (!(t != null && t.isValid && t.obtainedFrom == ursaRoot)) {
                isPollingForToken = true
                t = null
                if (token != null) {
                    logger.log("Disposing old invalid ursa token.")
                    token = null
                }
                logger.log("No token saved. Marking this request as a token poll request")
            }
            launchCoroutine { performRequest(nextRequest, t) }
        }
    }


    fun clearToken() {
        synchronized(this) {
            token = null
        }
    }

    fun <T> get(path: String, clazz: Class<T>): CompletableFuture<T> {
        val c = CompletableFuture<T>()
        queue.add(Request(path, clazz, c))
        return c
    }


    fun getString(path: String): CompletableFuture<String> {
        val c = CompletableFuture<String>()
        queue.add(Request(path, null, c))
        return c
    }

    fun <T> get(knownRequest: KnownRequest<T>): CompletableFuture<T> {
        return get(knownRequest.path, knownRequest.type)
    }

    data class KnownRequest<T>(val path: String, val type: Class<T>) {
        fun <N> typed(newType: Class<N>) = KnownRequest(path, newType)
        inline fun <reified N> typed() = typed(N::class.java)
    }

    @NEUAutoSubscribe
    object TickHandler {
        @SubscribeEvent
        fun onTick(event: TickEvent) {
            NotEnoughUpdates.INSTANCE.manager.ursaClient.bumpRequests()
        }
    }

    companion object {
        @JvmStatic
        fun profiles(uuid: UUID) = KnownRequest("v1/hypixel/profiles/${uuid}", JsonObject::class.java)

        @JvmStatic
        fun player(uuid: UUID) = KnownRequest("v1/hypixel/player/${uuid}", JsonObject::class.java)

        @JvmStatic
        fun guild(uuid: UUID) = KnownRequest("v1/hypixel/guild/${uuid}", JsonObject::class.java)

        @JvmStatic
        fun bingo(uuid: UUID) = KnownRequest("v1/hypixel/bingo/${uuid}", JsonObject::class.java)

        @JvmStatic
        fun museumForProfile(profileUuid: String) = KnownRequest("v1/hypixel/museum/${profileUuid}", JsonObject::class.java)

        @JvmStatic
        fun status(uuid: UUID) = KnownRequest("v1/hypixel/status/${uuid}", JsonObject::class.java)
    }
}
