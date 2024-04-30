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
import io.github.moulberry.notenoughupdates.options.NEUConfig
import io.github.moulberry.notenoughupdates.util.Utils
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.init.Items
import net.minecraftforge.client.event.GuiScreenEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.input.Keyboard

@NEUAutoSubscribe
class BazaarAHCtrlF {

    companion object {
        var disableOtherCtrlF = false
    }

    val config: NEUConfig = NotEnoughUpdates.INSTANCE.config


    @SubscribeEvent(priority = EventPriority.HIGH)
    fun onGuiScreenKeyboard(event: GuiScreenEvent.KeyboardInputEvent.Pre) {
        if (event.gui !is GuiChest) return
        val chestName = Utils.getOpenChestName()
        val inBZ = inBZ(chestName)
        val inAH = inAH(chestName)
        if (!inBZ && !inAH) return
        val openSlots = Minecraft.getMinecraft().thePlayer?.openContainer?.inventorySlots ?: return

        var slotId = 0;
        if (inBZ) {
            slotId = 45
        } else if (inAH) {
            slotId = 48
        }

        val gui = event.gui as GuiChest
        val signStack = openSlots[slotId]?.stack ?: return
        if (signStack.item == Items.sign && signStack.displayName == "§aSearch") {
            if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) && Keyboard.isKeyDown(Keyboard.KEY_F)) {
                Utils.sendMiddleMouseClick(gui.inventorySlots.windowId, slotId)
                if (config.toolbar.searchBar && config.toolbar.ctrlF) disableOtherCtrlF = true
            }
        }
    }

    private fun inAH(chestName: String): Boolean {
        if (!config.ahTweaks.ctrlFSearch) return false
        return (chestName.startsWith("Auctions"))
    }

    private fun inBZ(chestName: String): Boolean {
        if (!config.bazaarTweaks.ctrlFSearch) return false
        return (chestName.startsWith("Bazaar"))
    }
}
