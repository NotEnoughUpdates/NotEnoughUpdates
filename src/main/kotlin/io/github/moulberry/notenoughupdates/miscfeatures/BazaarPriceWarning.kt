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

import io.github.moulberry.notenoughupdates.autosubscribe.NEUAutoSubscribe
import io.github.moulberry.notenoughupdates.events.SlotClickEvent
import io.github.moulberry.notenoughupdates.util.ItemUtils
import io.github.moulberry.notenoughupdates.util.Utils
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.inventory.Slot
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@NEUAutoSubscribe
class BazaarPriceWarning : WarningPopUp() {
    override fun shouldShow(): Boolean {
        val openSlots = Minecraft.getMinecraft().thePlayer?.openContainer?.inventorySlots ?: return false
        return super.shouldShow() && openSlots.contains(clickedSlot ?: return false)
    }

    var clickedSlot: Slot? = null
    val priceRegx = "§7Price: §6([0-9.,]+) coins".toPattern()
    var price = 0.0

    @SubscribeEvent
    fun onClick(event: SlotClickEvent) {
        if (!Utils.getOpenChestName().contains("Instant Buy"))
            return
        if (shouldShow()) return
        val stack = event.slot.stack ?: return
        val lore = ItemUtils.getLore(stack)
        if (lore.lastOrNull() != "§7§eClick to buy now!") return
        val priceMatch = lore.firstNotNullOfOrNull { priceRegx.matcher(it).takeIf { it.matches() } } ?: return
        val price = priceMatch.group(1).replace(",", "").toDouble()

        if (price <= 1000) // TODO: option
            return

        this.price = price
        clickedSlot = event.slot
        show()
        event.cancel()
    }

    fun getLore(): List<String> {
        return clickedSlot?.stack?.let(ItemUtils::getLore) ?: listOf()
    }

    override fun getItemName(): String {
        return getLore().firstOrNull() ?: "<unknown>"
    }

    override fun getWarningLines(): List<String> {
        return listOf("will cost you §6${price}§r coins")
    }

    override fun getWarningPopup(): List<String> {
        val displayName = clickedSlot?.stack?.let(ItemUtils::getDisplayName)
        val tooltip = getLore().toMutableList()
        if (displayName != null)
            tooltip.add(0, displayName)
        return tooltip
    }

    override fun confirmClick() {
        val chest = Minecraft.getMinecraft().currentScreen as GuiChest
        Minecraft.getMinecraft().playerController.windowClick(
            chest.inventorySlots.windowId,
            clickedSlot?.slotNumber ?: return, 0, 0, Minecraft.getMinecraft().thePlayer
        )
    }
}
