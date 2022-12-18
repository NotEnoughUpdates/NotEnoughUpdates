/*
 * Copyright (C) 2022 NotEnoughUpdates contributors
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
import io.github.moulberry.notenoughupdates.core.config.KeybindHelper
import io.github.moulberry.notenoughupdates.util.Utils
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraftforge.client.event.GuiOpenEvent
import net.minecraftforge.client.event.GuiScreenEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

class WardrobeMouseButtons {

    private var keybinds: ArrayList<Int> = arrayListOf(
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
    fun onGui(event: GuiScreenEvent) {
        if (!NotEnoughUpdates.INSTANCE.config.wardrobeKeybinds.enableWardrobeKeybinds) return
        if (event.gui !is GuiChest) return
        val gui = event.gui as GuiChest
        if (!Utils.getOpenChestName().contains("Wardrobe")) return

        for (i in 0 until keybinds.size) {
            if (KeybindHelper.isKeyDown(keybinds[i])) {
                if (System.currentTimeMillis() - lastClick > 300) {
                    Minecraft.getMinecraft().playerController.windowClick(
                        gui.inventorySlots.windowId,
                        36 + i, 0, 0, Minecraft.getMinecraft().thePlayer
                    )
                }
                lastClick = System.currentTimeMillis()
                break
            }
        }
    }

    @SubscribeEvent
    fun onGuiOpen(event: GuiOpenEvent) {
        if (!NotEnoughUpdates.INSTANCE.config.wardrobeKeybinds.enableWardrobeKeybinds) return
        keybinds = arrayListOf(
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
    }
}
