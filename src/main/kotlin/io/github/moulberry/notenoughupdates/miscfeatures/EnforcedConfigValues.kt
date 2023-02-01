/*
 * Copyright (C) 2022 Linnea Gräf
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

package io.github.moulberry.notenoughupdates.miscfeatures

import com.google.gson.JsonElement
import io.github.moulberry.notenoughupdates.NotEnoughUpdates
import io.github.moulberry.notenoughupdates.autosubscribe.NEUAutoSubscribe
import io.github.moulberry.notenoughupdates.events.RepositoryReloadEvent
import io.github.moulberry.notenoughupdates.util.NotificationHandler
import io.github.moulberry.notenoughupdates.util.Shimmy
import io.github.moulberry.notenoughupdates.util.Utils
import io.github.moulberry.notenoughupdates.util.kotlin.fromJson
import net.minecraft.client.Minecraft
import net.minecraftforge.client.event.GuiOpenEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent

@NEUAutoSubscribe
object EnforcedConfigValues {

    class EnforcedValue {
        // lateinit var because we use gson (instead of kotlinx.serialization which can handle data classes)
        lateinit var path: String
        lateinit var value: JsonElement
    }

    class EnforcedValueData {
        var enforcedValues: List<EnforcedValue> = listOf()
        var notificationPSA: List<String>? = null
        var chatPSA: List<String>? = null
        lateinit var affectedVersions: List<Int>
    }


    var enforcedValues: List<EnforcedValueData> = listOf()

    @SubscribeEvent
    fun onRepoReload(event: RepositoryReloadEvent) {
        val fixedValues = event.repositoryRoot.resolve("enforced_values")
        enforcedValues = if (fixedValues.exists()) {
            fixedValues.listFiles()
                .filter {
                    it != null && it.isFile && it.canRead()
                }
                .map {
                    NotEnoughUpdates.INSTANCE.manager.gson.fromJson<EnforcedValueData>(it.readText())
                }.filter {
                    NotEnoughUpdates.VERSION_ID in it.affectedVersions
                }
        } else {
            listOf()
        }
        if (!event.isFirstLoad)
            sendPSAs()
    }

    @SubscribeEvent
    fun onGuiClose(event: GuiOpenEvent) {
        enforceOntoConfig(NotEnoughUpdates.INSTANCE.config ?: return)
    }

    var hasSentPSAsOnce = false

    @SubscribeEvent
    fun onTick(tickEvent: TickEvent.ClientTickEvent) {
        if (hasSentPSAsOnce || Minecraft.getMinecraft().thePlayer == null || !NotEnoughUpdates.INSTANCE.isOnSkyblock) return
        hasSentPSAsOnce = true
        sendPSAs()
        enforceOntoConfig(NotEnoughUpdates.INSTANCE.config ?: return)
    }

    fun sendPSAs() {
        val notification = enforcedValues.flatMap { it.notificationPSA ?: emptyList() }
        if (notification.isNotEmpty()) {
            NotificationHandler.displayNotification(notification, true)
        }
        val chat = enforcedValues.flatMap { it.chatPSA ?: emptyList() }
        if (chat.isNotEmpty()) {
            for (line in chat) {
                Utils.addChatMessage(line)
            }
        }
    }


    fun enforceOntoConfig(config: Any) {
        for (enforcedValue in enforcedValues.flatMap { it.enforcedValues }) {
            val shimmy = Shimmy.makeShimmy(config, enforcedValue.path.split("."))
            if (shimmy == null) {
                println("Could not create shimmy for path ${enforcedValue.path}")
                continue
            }
            val currentValue = shimmy.getJson()
            if (currentValue != enforcedValue.value) {
                println("Resetting ${enforcedValue.path} to ${enforcedValue.value} from $currentValue")
                shimmy.setJson(enforcedValue.value)
            }
        }
    }

    fun isBlockedFromEditing(optionPath: String): Boolean {
        return enforcedValues.flatMap { it.enforcedValues }.any { it.path == optionPath }
    }


}
