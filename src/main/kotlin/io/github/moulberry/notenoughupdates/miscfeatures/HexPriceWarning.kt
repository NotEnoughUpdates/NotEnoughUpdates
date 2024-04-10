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
import io.github.moulberry.notenoughupdates.events.SlotClickEvent
import io.github.moulberry.notenoughupdates.util.ItemUtils
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

/*
    We are gathered here today to mourn the death of 26m coins that Alea spent on a Cleave 6 book, while trying to take
    off Cleave 5.
 */
@NEUAutoSubscribe
object HexPriceWarning : WarningPopUp() {

    fun shouldCheck(): Boolean {
        return getLimit() >= 1
    }

    fun getLimit(): Double {
        return NotEnoughUpdates.INSTANCE.config.enchantingSolvers.hexOverpayWarning;
    }

    var lastClickedSlot = 0
    var cost = ""
    var upgradeName = ""
    override fun shouldShow(): Boolean {
        return shouldCheck() && super.shouldShow()
    }

    override fun getItemName(): String {
        return upgradeName
    }

    override fun getWarningLines(): List<String> {
        return listOf("will cost you §6$cost§r coins")
    }

    override fun confirmClick() {
        val chest = Minecraft.getMinecraft().currentScreen as GuiChest
        Minecraft.getMinecraft().playerController.windowClick(
            chest.inventorySlots.windowId,
            lastClickedSlot, 0, 0, Minecraft.getMinecraft().thePlayer
        )
    }

    @SubscribeEvent
    fun onClick(event: SlotClickEvent) {
        if (!shouldCheck()) return
        if (isShowing) return
        val stack = event.slot.stack ?: return
        val lore = ItemUtils.getLore(stack)
        val bazaarPriceLine = lore.indexOf("§7Bazaar Price")
        if (bazaarPriceLine >= 0 &&
            (bazaarPriceLine + 1) in lore.indices
        ) {
            val priceLine = lore[bazaarPriceLine + 1]
            val priceMatcher = coins.matcher(priceLine)
            if (!priceMatcher.matches()) return
            val price = priceMatcher.group(1).replace(",", "").toDouble()
            if (price >= getLimit()) {
                lastClickedSlot = event.slotId
                cost = priceMatcher.group(1)
                upgradeName = ItemUtils.getDisplayName(stack.tagCompound) ?: "<unnamed upgrade>"
                show()
                event.cancel()
            }
        }
    }

    val coins = "§6([,.0-9]+) Coins".toPattern()
}
