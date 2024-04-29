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

import io.github.moulberry.notenoughupdates.NotEnoughUpdates
import io.github.moulberry.notenoughupdates.autosubscribe.NEUAutoSubscribe
import io.github.moulberry.notenoughupdates.events.RegisterBrigadierCommandEvent
import io.github.moulberry.notenoughupdates.util.ApiUtil
import io.github.moulberry.notenoughupdates.util.MinecraftExecutor
import io.github.moulberry.notenoughupdates.util.brigadier.thenExecute
import moe.nea.libautoupdate.*
import net.minecraft.client.Minecraft
import net.minecraft.event.ClickEvent
import net.minecraft.util.ChatComponentText
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.apache.logging.log4j.LogManager
import java.util.concurrent.CompletableFuture
import javax.net.ssl.HttpsURLConnection

@NEUAutoSubscribe
object AutoUpdater {
    val updateContext = UpdateContext(
        SigningGithubSource("NotEnoughUpdates", "NotEnoughUpdates"),
        UpdateTarget.deleteAndSaveInTheSameFolder(AutoUpdater::class.java),
        CurrentVersion.ofTag(NotEnoughUpdates.VERSION.substringBefore("+")),
        "notenoughupdates"
    )

    init {
        UpdateUtils.patchConnection {
            if (it is HttpsURLConnection) {
                ApiUtil.patchHttpsRequest(it)
            }
        }
    }

    val logger = LogManager.getLogger("NEUUpdater")
    private var activePromise: CompletableFuture<*>? = null
        set(value) {
            field?.cancel(true)
            field = value
        }


    var updateState: UpdateState = UpdateState.NONE
        private set

    var potentialUpdate: PotentialUpdate? = null

    enum class UpdateState {
        AVAILABLE,
        QUEUED,
        DOWNLOADED,
        NONE
    }

    fun reset() {
        updateState = UpdateState.NONE
        activePromise = null
        potentialUpdate = null
        logger.info("Reset update state")
    }

    fun checkUpdate() {
        if (updateState != UpdateState.NONE) {
            logger.error("Trying to perform update check while another update is already in progress")
            return
        }
        logger.info("Starting update check")
        val updateStream = config.updateStream.get()
        activePromise = updateContext.checkUpdate(updateStream.stream)
            .thenApplyAsync({
                if (it.isUpdateAvailable)
                    (it.update as? SignedGithubUpdateData)?.verifyAnySignature()
                it
            }, MinecraftExecutor.OffThread)
            .thenAcceptAsync({
                logger.info("Update check completed")
                if (updateState != UpdateState.NONE) {
                    logger.warn("This appears to be the second update check. Ignoring this one")
                    return@thenAcceptAsync
                }
                potentialUpdate = it
                if (it.isUpdateAvailable) {
                    if ((it.update as? SignedGithubUpdateData)?.verifyAnySignature() != true) {
                        logger.error("Found unsigned github update: ${it.update}")
                        return@thenAcceptAsync
                    }
                    updateState = UpdateState.AVAILABLE
                    Minecraft.getMinecraft().thePlayer?.addChatMessage(ChatComponentText("§e[NEU] §aNEU found a new update: ${it.update.versionName}. Click here to automatically install this update.").apply {
                        this.chatStyle = this.chatStyle.setChatClickEvent(
                            ClickEvent(
                                ClickEvent.Action.RUN_COMMAND,
                                "/neuinternalupdatenow"
                            )
                        )
                    })
                }
            }, MinecraftExecutor.OnThread)
    }

    fun queueUpdate() {
        if (updateState != UpdateState.AVAILABLE) {
            logger.error("Trying to enqueue an update while another one is already downloaded or none is present")
        }
        updateState = UpdateState.QUEUED
        activePromise = CompletableFuture.supplyAsync {
            logger.info("Update download started")
            potentialUpdate!!.prepareUpdate()
        }.thenAcceptAsync({
            logger.info("Update download completed, setting exit hook")
            updateState = UpdateState.DOWNLOADED
            potentialUpdate!!.executeUpdate()
        }, MinecraftExecutor.OnThread)
    }

    init {
        updateContext.cleanup()
        config.updateStream.whenChanged { _, _ ->
            reset()
        }
    }

    fun getCurrentVersion(): String {
        return NotEnoughUpdates.VERSION
    }


    val config get() = NotEnoughUpdates.INSTANCE.config.about

    @SubscribeEvent
    fun onPlayerAvailableOnce(event: TickEvent.ClientTickEvent) {
        val p = Minecraft.getMinecraft().thePlayer ?: return
        MinecraftForge.EVENT_BUS.unregister(this)
        if (config.autoUpdates)
            checkUpdate()
    }


    @SubscribeEvent
    fun testCommand(event: RegisterBrigadierCommandEvent) {
        event.command("neuinternalupdatenow") {
            thenExecute {
                queueUpdate()
            }
        }
    }

    fun getNextVersion(): String? {
        return potentialUpdate?.update?.versionName
    }


}
