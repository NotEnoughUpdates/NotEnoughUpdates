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
import io.github.moulberry.notenoughupdates.options.customtypes.NEUDebugFlag
import io.github.moulberry.notenoughupdates.util.kotlin.Coroutines.await
import io.github.moulberry.notenoughupdates.util.kotlin.Coroutines.continueOn
import io.github.moulberry.notenoughupdates.util.kotlin.Coroutines.launchCoroutine
import io.github.moulberry.notenoughupdates.util.kotlin.Coroutines.launchCoroutineOnCurrentThread
import net.minecraft.client.Minecraft
import java.time.Duration
import java.time.Instant
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentLinkedQueue

class UrsaClient(val apiUtil: ApiUtil) {
    private data class Token(
        val obtainedAt: Instant,
        val token: String,
    ) {
        val isValid get() = Duration.between(obtainedAt, Instant.now()).seconds < 3500
    }

    val logger = NEUDebugFlag.API_CACHE
    private var token: Token? = null
    private var isPollingForToken = false

    private data class Request<T>(
        val path: String,
        val objectMapping: Class<T>,
        val consumer: CompletableFuture<T>,
    )

    private val queue = ConcurrentLinkedQueue<Request<*>>()
    val ursaRoot = "http://localhost:3000"

    private suspend fun authorizeRequest(connection: ApiUtil.Request) {
        val t = token
        if (t != null && t.isValid) {
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

    private fun saveToken(connection: ApiUtil.Request) {
        logger.log("Attempting to save token")
        val token = connection.responseHeaders["x-ursa-token"]?.firstOrNull()
        if (token == null) {
            isPollingForToken = false
            logger.log("No token found. Marking as non polling")
            return
        }
        synchronized(this) {
            this.token = Token(Instant.now(), token)
            isPollingForToken = false
            logger.log("Token saving successful")
        }
        launchCoroutineOnCurrentThread {
            continueOn(MinecraftExecutor.OnThread)
            bumpRequests()
        }
    }

    private suspend fun <T> performRequest(request: Request<T>) {
        val apiRequest = apiUtil.request().url("$ursaRoot/${request.path}")
        try {
            logger.log("Ursa Request started")
            authorizeRequest(apiRequest)
            val response = apiRequest.requestJson(request.objectMapping).await()
            logger.log("Request completed")
            saveToken(apiRequest)
            request.consumer.complete(response)
        } catch (e: Exception) {
            logger.log("Request failed")
            isPollingForToken = false
            request.consumer.completeExceptionally(e)
        }
    }

    private fun bumpRequests() {
        logger.log("Bumping ursa requests")
        if (isPollingForToken) return
        logger.log("Not currently polling for token")
        val nextRequest = queue.poll()
        if (nextRequest == null) {
            logger.log("No request to bump found")
            return
        }
        logger.log("Request found")
        synchronized(this) {
            if (token?.isValid != true) {
                isPollingForToken = true
                logger.log("No token saved. Marking this request as a token poll request")
            }
        }
        launchCoroutine { performRequest(nextRequest) }
        bumpRequests()
    }

    fun clearToken() {
        synchronized(this) {
            token = null
        }
    }

    fun <T> get(path: String, clazz: Class<T>): CompletableFuture<T> {
        val c = CompletableFuture<T>()
        queue.add(Request(path, clazz, c))
        bumpRequests()
        return c
    }

    fun <T> get(knownRequest: KnownRequest<T>): CompletableFuture<T> {
        return get(knownRequest.path, knownRequest.type)
    }

    data class KnownRequest<T>(val path: String, val type: Class<T>) {
        fun <N> typed(newType: Class<N>) = KnownRequest(path, newType)
        inline fun <reified N> typed() = typed(N::class.java)
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
        fun status(uuid: UUID) = KnownRequest("v1/hypixel/status/${uuid}", JsonObject::class.java)
    }
}
