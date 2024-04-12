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

package io.github.moulberry.notenoughupdates.miscfeatures

import io.github.moulberry.notenoughupdates.NotEnoughUpdates
import io.github.moulberry.notenoughupdates.autosubscribe.NEUAutoSubscribe
import io.github.moulberry.notenoughupdates.core.util.StringUtils
import io.github.moulberry.notenoughupdates.events.SlotClickEvent
import io.github.moulberry.notenoughupdates.events.TabListChangeEvent
import io.github.moulberry.notenoughupdates.miscfeatures.tablisttutorial.TablistAPI
import io.github.moulberry.notenoughupdates.util.ItemUtils
import io.github.moulberry.notenoughupdates.util.Utils
import io.github.moulberry.notenoughupdates.util.kotlin.KSerializable
import io.github.moulberry.notenoughupdates.util.kotlin.useMatcher
import net.minecraftforge.event.entity.player.ItemTooltipEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@NEUAutoSubscribe
object HotmDesires {

    val powderRequirementText = "§.([0-9,]+) .* Powder".toPattern()
    val youArePoorText = "§cYou don't have enough (.*) Powder!".toPattern()

    @KSerializable
    data class Desire(
        val name: String,
        val powderRequirement: Int,
    )

    val desires: MutableMap<String, Desire>?
        get() = NotEnoughUpdates.INSTANCE.config.profileSpecific?.hotmDesires

    @JvmStatic
    fun appendDesireForType(powderType: String): String {
        val desire = desires?.get(powderType) ?: return ""
        return "§7/§c" + StringUtils.formatNumber(desire.powderRequirement)
    }

    val tablistPowderLine = " (.*): ([0-9,]+)".toPattern()

    @SubscribeEvent
    fun onTabListChange(event: TabListChangeEvent) {
        val desireMap = desires ?: return
        if (!isEnabled()) {
            desireMap.clear()
            return
        }
        for (line in TablistAPI.getWidgetLines(TablistAPI.WidgetNames.POWDER)) {
            val (powderKind, powderCount) = tablistPowderLine.useMatcher(StringUtils.cleanColour(line)) {
                val powderKind = group(1)
                val powderCount = group(2).replace(",", "").toInt()
                powderKind to powderCount
            } ?: continue
            val goal = desireMap[powderKind] ?: continue
            if (goal.powderRequirement > powderCount) continue
            desireMap.remove(powderKind)
            Utils.addClickableChatMessage(
                "§e[NEU] You have enough $powderKind powder to upgrade ${goal.name}§e!",
                "/hotm",
                "§eClick to open your Heart of the Mountain to select the next upgrade."
            )
        }
    }

    fun isEnabled() = NotEnoughUpdates.INSTANCE.config.mining.powderTodo

    @SubscribeEvent
    fun onClickHotmItemThatYouCannotUpgrade(event: SlotClickEvent) {
        if (Utils.getOpenChestName() != "Heart of the Mountain" || !isEnabled())
            return
        val name = ItemUtils.getDisplayName(event.slot.stack) ?: return
        val lore = ItemUtils.getLore(event.slot.stack)
        val missingPowderText = lore.lastOrNull() ?: return
        val powderKind = youArePoorText.useMatcher(missingPowderText) {
            group(1)
        } ?: return
        val powderCount =
            lore.firstNotNullOfOrNull { powderRequirementText.useMatcher(it) { group(1).replace(",", "").toInt() } }
                ?: return
        val desireMap = desires ?: return
        if (desireMap[powderKind]?.name != name) {
            desireMap[powderKind] = Desire(name, powderCount)
        } else {
            desireMap.remove(powderKind)
        }
    }


    @SubscribeEvent
    fun onAfterGuiDraw(event: ItemTooltipEvent) {
        if (Utils.getOpenChestName() != "Heart of the Mountain" || !isEnabled())
            return
        val name = ItemUtils.getDisplayName(event.itemStack) ?: return
        if (desires?.values?.any { it.name == name } != true) return
        event.toolTip.add("§e[NEU] Selected this perk as your next goal.")
        event.toolTip.add("§e[NEU] Click again to deselect.")
    }

    @JvmStatic
    fun wantsPowderInfo(): Boolean {
        return desires?.isNotEmpty() == true && isEnabled()
    }

}
