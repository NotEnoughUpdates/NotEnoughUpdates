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
import net.minecraft.client.gui.inventory.GuiChest
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

        for (i in keybinds.indices) {
            if (KeybindHelper.isKeyDown(keybinds[i])) {
                if (System.currentTimeMillis() - lastClick > 300) {
                    Utils.sendLeftMouseClick(gui.inventorySlots.windowId, 36 + i)
                    lastClick = System.currentTimeMillis()
                    event.isCanceled = true
                }
                break
            }
        }
    }

}
