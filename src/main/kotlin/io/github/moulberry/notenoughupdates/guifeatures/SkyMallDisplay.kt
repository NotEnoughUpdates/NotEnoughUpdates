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

package io.github.moulberry.notenoughupdates.guifeatures

import io.github.moulberry.notenoughupdates.NotEnoughUpdates
import io.github.moulberry.notenoughupdates.autosubscribe.NEUAutoSubscribe
import io.github.moulberry.notenoughupdates.util.SBInfo
import io.github.moulberry.notenoughupdates.util.SkyBlockTime
import io.github.moulberry.notenoughupdates.util.Utils
import net.minecraft.init.Items
import net.minecraft.item.ItemStack
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.time.Duration
import java.time.Instant
import java.util.regex.Pattern

@NEUAutoSubscribe
class SkyMallDisplay {

    private val pattern = Pattern.compile("§r§eNew buff§r§r§r: (.*)§r")

    @SubscribeEvent(receiveCanceled = true)
    fun onChatReceive(event: ClientChatReceivedEvent) {
        if (!NotEnoughUpdates.INSTANCE.hasSkyblockScoreboard()) return
        if (SBInfo.getInstance().getLocation() != "mining_3") return

        val matcher = pattern.matcher(event.message.formattedText)
        if (!matcher.matches()) return

        val message = matcher.group(1) ?: return
        currentPerk = SkyMallPerk.values().find { it.chatMessage == message }

        currentPerk?.let {
            val manager = NotEnoughUpdates.INSTANCE.manager
            displayItem = manager.jsonToStack(manager.itemInformation[it.displayItemId])
        }
    }

    companion object {
        private var displayText = ""
        private var displayItem: ItemStack? = null
        private var lastUpdated = 0L
        private var currentPerk: SkyMallPerk? = null

        fun getDisplayText(): String {
            return if (lastUpdated + 1_000 > System.currentTimeMillis()) {
                displayText
            } else {
                update()
                displayText
            }
        }

        fun getDisplayItem(): ItemStack {
            return displayItem ?: ItemStack(Items.apple)
        }

        private fun update() {
            val nextDayBeginning = SkyBlockTime.now()
                .let { it.copy(day = it.day + 1, hour = 0, minute = 0, second = 0) }
                .toInstant()
            val untilNextDay = Duration.between(Instant.now(), nextDayBeginning)
            displayText = (currentPerk?.displayName ?: "?") + " §a(${
                Utils.prettyTime(untilNextDay.toMillis())
            })"
            lastUpdated = System.currentTimeMillis()
        }
    }

    enum class SkyMallPerk(val displayName: String, val displayItemId: String, val chatMessage: String) {
        PICKAXE_COOLDOWN(
            "20% §6Pickaxe Ability cooldown", "DIAMOND_PICKAXE",
            "§r§fReduce Pickaxe Ability cooldown by §r§a20%§r§f."
        ),
        MORE_POWDER("+15% more §6Powder", "MITHRIL_ORE", "§r§fGain §r§a+15% §r§fmore Powder while mining."),
        MINING_FORTUNE("+50 §6☘ Mining Fortune", "ENCHANTED_RABBIT_FOOT", "§r§fGain §r§a+50 §r§6☘ Mining Fortune§r§f."),
        MINING_SPEED("+100 §6⸕ Mining Speed", "ENCHANTED_FEATHER", "§r§fGain §r§a+100 §r§6⸕ Mining Speed§r§f."),
        MORE_GOBLINS("10x §6Goblin chance", "GOBLIN_HELMET", "§r§f§r§a10x §r§fchance to find Goblins while mining."),
        TITANIUM_DROPS("5x §9Titanium drops", "TITANIUM_ORE", "§r§fGain §r§a5x §r§9Titanium §r§fdrops"),

        // In case hypixel finds some day the missing dot at the end.
        TITANIUM_DROPS_WITH_DOT("5x §9Titanium drops", "TITANIUM_ORE", "§r§fGain §r§a5x §r§9Titanium §r§fdrops."),
        ;
    }
}
