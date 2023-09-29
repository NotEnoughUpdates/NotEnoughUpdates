/*
 * Copyright (C) 2022-2023 NotEnoughUpdates contributors
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
import io.github.moulberry.notenoughupdates.core.config.KeybindHelper
import io.github.moulberry.notenoughupdates.util.Utils
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.inventory.ContainerChest
import net.minecraft.inventory.Slot
import net.minecraft.item.ItemStack
import net.minecraftforge.client.event.GuiScreenEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@NEUAutoSubscribe
class WardrobeMouseButtons {

    private val keybinds: List<Int> get() = listOf(
        NotEnoughUpdates.INSTANCE.config.wardrobeKeybinds.wardrobeSlot1,
        NotEnoughUpdates.INSTANCE.config.wardrobeKeybinds.wardrobeSlot2,
        NotEnoughUpdates.INSTANCE.config.wardrobeKeybinds.wardrobeSlot3,
        NotEnoughUpdates.INSTANCE.config.wardrobeKeybinds.wardrobeSlot4,
        NotEnoughUpdates.INSTANCE.config.wardrobeKeybinds.wardrobeSlot5,
        NotEnoughUpdates.INSTANCE.config.wardrobeKeybinds.wardrobeSlot6,
        NotEnoughUpdates.INSTANCE.config.wardrobeKeybinds.wardrobeSlot7,
        NotEnoughUpdates.INSTANCE.config.wardrobeKeybinds.wardrobeSlot8,
        NotEnoughUpdates.INSTANCE.config.wardrobeKeybinds.wardrobeSlot9,
        NotEnoughUpdates.INSTANCE.config.wardrobeKeybinds.wardrobePageUnequip,
        NotEnoughUpdates.INSTANCE.config.wardrobeKeybinds.wardrobePagePrevious,
        NotEnoughUpdates.INSTANCE.config.wardrobeKeybinds.wardrobePageNext,
    )
    private var lastClick = -1L

    @SubscribeEvent
    fun onGuiKeyboardInput(event: GuiScreenEvent.KeyboardInputEvent.Pre) {
        checkKeybinds(event)
    }

    @SubscribeEvent
    fun onGuiMouseInput(event: GuiScreenEvent.MouseInputEvent.Pre) {
        checkKeybinds(event)
    }

    @Suppress("InvalidSubscribeEvent")
    private fun checkKeybinds(event: GuiScreenEvent) {
        if (!NotEnoughUpdates.INSTANCE.config.wardrobeKeybinds.enableWardrobeKeybinds || !NotEnoughUpdates.INSTANCE.hasSkyblockScoreboard()) return
        val gui = event.gui as? GuiChest ?: return
        if (!Utils.getOpenChestName().contains("Wardrobe")) return
        val chestName = Utils.getOpenChestName()
        val chestNameRegex = "Wardrobe (\\((?<current>[0-9]+)\\/(?<total>[0-9]+)\\))".toRegex()
        val chestNameMatch = chestNameRegex.matchEntire(chestName)
        if (chestNameMatch == null) return
        val totalPages = chestNameMatch.groups["total"]!!.value.toInt()
        val currentPage = chestNameMatch.groups["current"]!!.value.toInt()
        val guiChes = event.gui as GuiChest
        val container = guiChes.inventorySlots as ContainerChest
        var slotNum = 0

        if ((currentPage != totalPages) && KeybindHelper.isKeyDown(NotEnoughUpdates.INSTANCE.config.wardrobeKeybinds.wardrobePageNext)) {
            slotNum = 53
        } else if ((currentPage != 1) && KeybindHelper.isKeyDown(NotEnoughUpdates.INSTANCE.config.wardrobeKeybinds.wardrobePagePrevious)) {
            slotNum = 45
        } else if (KeybindHelper.isKeyDown(NotEnoughUpdates.INSTANCE.config.wardrobeKeybinds.wardrobePageUnequip)) {
            var notEquipped = 0
            for (j in 36..44) {
                val slotName = container.getSlot(j).getStack().getDisplayName()
                if (slotName.contains("Equipped")) {
                    slotNum = j
                }
                else {
                    notEquipped++
                }
            }
            if (notEquipped == 9) return
        } else {
            for (i in keybinds.indices) {
                if (KeybindHelper.isKeyDown(keybinds[i])) {
                    if (System.currentTimeMillis() - lastClick > 300) {
                            slotNum = (36 + i)
                    }
                }
            }
        }

        val thatItemStack = container.getSlot(slotNum).getStack() ?: return
        if (thatItemStack.getDisplayName().isEmpty()) return
        if (slotNum < 36 || ((slotNum > 45) && (slotNum != 53))) return
        Utils.sendLeftMouseClick(gui.inventorySlots.windowId, slotNum)
        lastClick = System.currentTimeMillis()
        event.isCanceled = true
    }

}
