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
import io.github.moulberry.notenoughupdates.autosubscribe.NEUAutoSubscribe
import io.github.moulberry.notenoughupdates.core.util.StringUtils
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.inventory.ContainerChest
import net.minecraftforge.client.event.GuiOpenEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import java.util.regex.Pattern

@NEUAutoSubscribe
class HotmInformation {
    private var ticksTillReload = 0
    private val pattern = Pattern.compile("§[7b]Level (\\d*)(?:§8/.*)?")

    @SubscribeEvent
    fun onGuiOpen(event: GuiOpenEvent) {
        val gui = event.gui
        if (gui !is GuiChest) return

        val containerName = (gui.inventorySlots as ContainerChest).lowerChestInventory.displayName.unformattedText
        if (containerName == "Heart of the Mountain") {
            ticksTillReload = 5
        }
    }

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (event.phase != TickEvent.Phase.START) return
        if (ticksTillReload == 0) return
        ticksTillReload--
        if (ticksTillReload == 0) {
            loadDataFromInventory()
        }
    }

    private fun loadDataFromInventory() {
        val profileSpecific = NotEnoughUpdates.INSTANCE.config.profileSpecific ?: return

        for (slot in Minecraft.getMinecraft().thePlayer.openContainer.inventorySlots) {
            val stack = slot.stack ?: continue
            val displayName = stack.displayName
            val lore = ItemUtils.getLore(stack)
            if (!lore.any { it.contains("Right click to") }) continue

            val perkName = StringUtils.cleanColour(displayName)
            profileSpecific.hotmTree[perkName] = getLevel(lore[0])
        }
    }

    private fun getLevel(string: String): Int {
        val matcher = pattern.matcher(string)
        val level = if (matcher.matches()) matcher.group(1).toInt() else 1

        val withBlueCheeseGoblinOmelette = string.contains("§b")
        val isNotMaxed = string.contains("§8/")
        return if (withBlueCheeseGoblinOmelette && (isNotMaxed || level > 1)) level - 1 else level
    }

    companion object {
        private val QUICK_FORGE_MULTIPLIERS = intArrayOf(
            985,
            970,
            955,
            940,
            925,
            910,
            895,
            880,
            865,
            850,
            845,
            840,
            835,
            830,
            825,
            820,
            815,
            810,
            805,
            700
        )

        /*
         * 1000 = 100% of the time left
         *  700 = 70% of the time left
         * */
        @JvmStatic
        fun getQuickForgeMultiplier(level: Int): Int {
            if (level <= 0) return 1000
            return if (level > 20) -1 else QUICK_FORGE_MULTIPLIERS[level - 1]
        }
    }
}
