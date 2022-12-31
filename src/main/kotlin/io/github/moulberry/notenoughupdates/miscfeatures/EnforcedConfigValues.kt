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
import io.github.moulberry.notenoughupdates.events.RepositoryReloadEvent
import io.github.moulberry.notenoughupdates.util.NotificationHandler
import io.github.moulberry.notenoughupdates.util.Shimmy
import io.github.moulberry.notenoughupdates.util.Utils
import io.github.moulberry.notenoughupdates.util.kotlin.fromJson
import net.minecraft.client.Minecraft
import net.minecraftforge.client.event.GuiOpenEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent

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
    }


    var enforcedValues = EnforcedValueData()

    @SubscribeEvent
    fun onRepoReload(event: RepositoryReloadEvent) {
        val fixedValues = event.repositoryRoot.resolve("constants/enforced_values.json")
        enforcedValues = if (fixedValues.exists()) {
            NotEnoughUpdates.INSTANCE.manager.gson.fromJson(fixedValues.readText())
        } else {
            EnforcedValueData()
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
        if (hasSentPSAsOnce || Minecraft.getMinecraft().thePlayer == null) return
        hasSentPSAsOnce = true
        sendPSAs()
        enforceOntoConfig(NotEnoughUpdates.INSTANCE.config ?: return)
    }

    fun sendPSAs() {
        val notification = enforcedValues.notificationPSA
        if (notification != null) {
            NotificationHandler.displayNotification(notification, true)
        }
        val chat = enforcedValues.chatPSA
        if (chat != null) {
            for (line in chat)
                Utils.addChatMessage(line)
        }
    }


    fun enforceOntoConfig(config: Any) {
        for (enforcedValue in enforcedValues.enforcedValues) {
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
        return enforcedValues.enforcedValues.any { it.path == optionPath }
    }


}
