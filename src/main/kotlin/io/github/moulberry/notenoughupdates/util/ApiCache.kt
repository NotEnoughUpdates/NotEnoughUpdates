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

import io.github.moulberry.notenoughupdates.NotEnoughUpdates
import io.github.moulberry.notenoughupdates.options.customtypes.NEUDebugFlag
import io.github.moulberry.notenoughupdates.util.ApiUtil.Request
import io.github.moulberry.notenoughupdates.util.kotlin.supplyImmediate
import org.apache.http.NameValuePair
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.function.Supplier
import kotlin.io.path.deleteIfExists
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource
import kotlin.time.toKotlinDuration

@OptIn(ExperimentalTime::class)

object ApiCache {
    data class CacheKey(
        val baseUrl: String,
        val requestParameters: List<NameValuePair>,
        val shouldGunzip: Boolean,
    )

    data class CacheResult internal constructor(
        var cacheState: CacheState,
        val firedAt: TimeSource.Monotonic.ValueTimeMark,
    ) {
        constructor(future: CompletableFuture<String>, firedAt: TimeSource.Monotonic.ValueTimeMark) : this(
            CacheState.WaitingForFuture(future),
            firedAt
        ) {
            future.thenAccept { text ->
                synchronized(this) {
                    val f = Files.createTempFile(cacheBaseDir, "api-cache", ".bin")
                    log("Writing cache to disk: $f")
                    f.toFile().deleteOnExit()
                    f.writeText(text)
                    cacheState = CacheState.FileCached(f)
                }
            }
        }

        sealed interface CacheState {
            object Disposed : CacheState
            data class WaitingForFuture(val future: CompletableFuture<String>) : CacheState
            data class FileCached(val file: Path) : CacheState
        }

        val isAvailable get() = cacheState is CacheState.FileCached

        fun getCachedFuture(): CompletableFuture<String> {
            synchronized(this) {
                return when (val cs = cacheState) {
                    CacheState.Disposed -> supplyImmediate {
                        throw IllegalStateException("Attempting to read from a disposed future. Most likely caused by non synchronized access to ApiCache.cachedRequests")
                    }

                    is CacheState.FileCached -> supplyImmediate {
                        cs.file.readText()
                    }

                    is CacheState.WaitingForFuture -> cs.future
                }
            }
        }

        /**
         * Should be called when removing / replacing a request from [cachedRequests].
         * Should only be called while holding a lock on [ApiCache].
         * This deletes the disk cache and smashes the internal state for it to be GCd.
         * After calling this method no other method may be called on this object.
         */
        internal fun dispose() {
            synchronized(this) {
                val file = (cacheState as? CacheState.FileCached)?.file
                log("Disposing cache for $file")
                cacheState = CacheState.Disposed
                file?.deleteIfExists()
            }
        }
    }

    private val cacheBaseDir by lazy {
        val d = Files.createTempDirectory("neu-cache")
        d.toFile().deleteOnExit()
        d
    }
    private val cachedRequests = mutableMapOf<CacheKey, CacheResult>()
    val histogramTotalRequests: MutableMap<String, Int> = mutableMapOf()
    val histogramNonCachedRequests: MutableMap<String, Int> = mutableMapOf()

    private val timeout = 10.seconds
    private val globalMaxCacheAge = 1.hours

    private fun log(message: String) {
        NEUDebugFlag.API_CACHE.log(message)
    }

    private fun traceApiRequest(
        request: Request,
        failReason: String?,
    ) {
        if (!NotEnoughUpdates.INSTANCE.config.hidden.dev) return
        val callingClass = Thread.currentThread().stackTrace
            .find {
                !it.className.startsWith("java.") &&
                        !it.className.startsWith("kotlin.") &&
                        it.className != ApiCache::class.java.name &&
                        it.className != ApiUtil::class.java.name &&
                        it.className != Request::class.java.name
            }
        val callingClassText = callingClass?.let {
            "${it.className}.${it.methodName} (${it.fileName}:${it.lineNumber})"
        } ?: "no calling class found"
        callingClass?.className?.let {
            histogramTotalRequests[it] = (histogramTotalRequests[it] ?: 0) + 1
            if (failReason != null)
                histogramNonCachedRequests[it] = (histogramNonCachedRequests[it] ?: 0) + 1
        }
        if (failReason != null) {
            log("Executing api request for url ${request.baseUrl} by $callingClassText: $failReason")
        } else {
            log("Cache hit for api request for url ${request.baseUrl} by $callingClassText.")
        }
    }

    fun clear() {
        synchronized(this) {
            cachedRequests.clear()
        }
    }

    private fun evictCache() {
        synchronized(this) {
            val it = cachedRequests.iterator()
            while (it.hasNext()) {
                val next = it.next()
                if (next.value.firedAt.elapsedNow() >= globalMaxCacheAge) {
                    next.value.dispose()
                    it.remove()
                }
            }
        }
    }

    fun cacheRequest(
        request: Request,
        cacheKey: CacheKey?,
        futureSupplier: Supplier<CompletableFuture<String>>,
        maxAge: Duration?
    ): CompletableFuture<String> {
        evictCache()
        if (cacheKey == null) {
            traceApiRequest(request, "uncacheable request (probably POST)")
            return futureSupplier.get()
        }
        if (maxAge == null) {
            traceApiRequest(request, "manually specified as uncacheable")
            return futureSupplier.get()
        }
        fun recache(): CompletableFuture<String> {
            return futureSupplier.get().also {
                cachedRequests[cacheKey]?.dispose() // Safe to dispose like this because this function is always called in a synchronized block
                cachedRequests[cacheKey] = CacheResult(it, TimeSource.Monotonic.markNow())
            }
        }
        synchronized(this) {
            val cachedRequest = cachedRequests[cacheKey]
            if (cachedRequest == null) {
                traceApiRequest(request, "no cache found")
                return recache()
            }

            return if (cachedRequest.isAvailable) {
                if (cachedRequest.firedAt.elapsedNow() > maxAge.toKotlinDuration()) {
                    traceApiRequest(request, "outdated cache")
                    recache()
                } else {
                    // Using local cached request
                    traceApiRequest(request, null)
                    cachedRequest.getCachedFuture()
                }
            } else {
                if (cachedRequest.firedAt.elapsedNow() > timeout) {
                    traceApiRequest(request, "suspiciously slow api response")
                    recache()
                } else {
                    // Joining ongoing request
                    traceApiRequest(request, null)
                    cachedRequest.getCachedFuture()
                }
            }
        }
    }

}
