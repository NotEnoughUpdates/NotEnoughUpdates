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

package io.github.moulberry.notenoughupdates.util.kotlin

import io.github.moulberry.notenoughupdates.autosubscribe.NEUAutoSubscribe
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor
import java.util.concurrent.ForkJoinPool
import kotlin.coroutines.*

@NEUAutoSubscribe
object Coroutines {
    fun <T> launchCoroutineOnCurrentThread(block: suspend () -> T): CompletableFuture<T> {
        val future = CompletableFuture<T>()
        val coroutine = block.createCoroutine(object : Continuation<T> {
            override val context: CoroutineContext
                get() = EmptyCoroutineContext

            override fun resumeWith(result: Result<T>) {
                try {
                    future.complete(result.getOrThrow())
                } catch (ex: Throwable) {
                    future.completeExceptionally(ex)
                }
            }
        })
        coroutine.resume(Unit)
        return future
    }

    suspend fun continueOn(ex: Executor) {
        suspendCoroutine {
            ex.execute {
                it.resume(Unit)
            }
        }
    }

    fun <T> launchCoroutine(block: suspend () -> T): CompletableFuture<T> {
        return launchCoroutineOnCurrentThread {
            continueOn(ForkJoinPool.commonPool())
            block()
        }
    }

    private data class DelayedTask(val contination: () -> Unit, var tickDelay: Int)

    private val tasks = mutableListOf<DelayedTask>()

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (event.phase == TickEvent.Phase.END) {
            val toRun = mutableListOf<DelayedTask>()
            synchronized(tasks) {
                tasks.removeIf {
                    it.tickDelay--
                    if (it.tickDelay <= 0) {
                        toRun.add(it)
                        return@removeIf true
                    }
                    false
                }
            }
            toRun.forEach { it.contination() }
        }
    }


    suspend fun <T> CompletableFuture<T>.await(): T {
        return suspendCoroutine { cont ->
            handle { res, ex ->
                if (ex != null)
                    cont.resumeWithException(ex)
                else
                    cont.resume(res)
            }
        }
    }

    suspend fun waitTicks(tickCount: Int) {
        suspendCoroutine {
            synchronized(tasks) {
                tasks.add(DelayedTask({ it.resume(Unit) }, tickCount))
            }
        }
    }


}
