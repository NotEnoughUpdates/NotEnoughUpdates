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

package io.github.moulberry.notenoughupdates.miscfeatures.inventory

import com.google.gson.Gson
import io.github.moulberry.notenoughupdates.NotEnoughUpdates
import io.github.moulberry.notenoughupdates.autosubscribe.NEUAutoSubscribe
import io.github.moulberry.notenoughupdates.events.GuiContainerBackgroundDrawnEvent
import io.github.moulberry.notenoughupdates.util.MuseumUtil
import io.github.moulberry.notenoughupdates.util.SBInfo
import io.github.moulberry.notenoughupdates.util.Utils
import io.github.moulberry.notenoughupdates.util.kotlin.KSerializable
import io.github.moulberry.notenoughupdates.util.stripControlCodes
import net.minecraft.inventory.ContainerChest
import net.minecraft.util.EnumChatFormatting
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.io.File

@NEUAutoSubscribe
object MuseumTooltipManager {

    @KSerializable
    data class MuseumData(
        val profiles: MutableMap<String, ProfileSpecificMuseumData> = mutableMapOf()
    )

    @KSerializable
    data class ProfileSpecificMuseumData(
        val donatedItems: MutableSet<String>,
        var visitedOnce: Boolean
    )

    private val loadedMuseumDataDelegate = lazy {
        var data = MuseumData()
        if (file.exists()) {
            try {
                val content = file.readText()
                val loadedData = Gson().fromJson(content, MuseumData::class.java)
                if (loadedData != null) {
                    data = loadedData
                } else {
                    Utils.addChatMessage("${EnumChatFormatting.RED}${EnumChatFormatting.BOLD}[NEU] Error while reading existing museum data, resetting.")
                }
            } catch (ignored: Exception) {
                Utils.addChatMessage("${EnumChatFormatting.RED}${EnumChatFormatting.BOLD}[NEU] Error while reading existing museum data, resetting.")
            }
        } else {
            file.createNewFile()
            file.writeText(Gson().toJson(data))
        }
        data
    }
    private val loadedMuseumData by loadedMuseumDataDelegate

    private val file = File(NotEnoughUpdates.INSTANCE.neuDir, "donated_museum_items.json")

    private val donatedStates = listOf(
        MuseumUtil.DonationState.DONATED_PRESENT,
        MuseumUtil.DonationState.DONATED_VACANT,
        MuseumUtil.DonationState.DONATED_PRESENT_PARTIAL
    )

    private fun addItemToDonatedList(itemsToAdd: List<String>) {
        val profile = SBInfo.getInstance().currentProfile ?: return

        for (internalName in itemsToAdd) {
            loadedMuseumData.profiles.computeIfAbsent(profile) { ProfileSpecificMuseumData(mutableSetOf(), false) }

            val profileData = loadedMuseumData.profiles[profile]!!
            profileData.donatedItems.add(internalName)
            profileData.visitedOnce = true
        }
    }

    /***
     * Check if the given item has been donated to the museum on the current profile
     *
     * This will only work if the player has visited the museum before
     */
    fun isItemDonated(item: String): Boolean {
        val profile = SBInfo.getInstance().currentProfile ?: return false

        val profileData = loadedMuseumData.profiles[profile] ?: return false
        return profileData.donatedItems.contains(item)

    }

    /***
     * Check if the player has visited the museum at least once on this profile
     */
    fun hasPlayerVisitedMuseum(): Boolean {
        val profile = SBInfo.getInstance().currentProfile ?: return false
        if (SBInfo.getInstance().stranded || SBInfo.getInstance().bingo) {
            return true
        }

        val profileData = loadedMuseumData.profiles[profile] ?: return false
        return profileData.visitedOnce
    }

    @SubscribeEvent
    fun onBackgroundDrawn(event: GuiContainerBackgroundDrawnEvent) {
        val gui = event.container ?: return
        val chest = gui.inventorySlots as? ContainerChest ?: return
        if (!MuseumUtil.isMuseumInventory(chest.lowerChestInventory)) return

        val armor = Utils.getOpenChestName().stripControlCodes().endsWith("Armor Sets")
        val slots = chest.inventorySlots

        for (i in 0..53) {
            val slot = slots[i]
            if (slot == null || slot.stack == null) continue
            val item = MuseumUtil.findMuseumItem(slot.stack, armor) ?: continue
            if (donatedStates.contains(item.state)) {
                addItemToDonatedList(item.skyblockItemIds)
            }
        }
    }

    @SubscribeEvent
    fun onWorldUnload(@Suppress("UNUSED_PARAMETER") event: WorldEvent.Unload) {
        // Only save when the Museum has actually been opened
        if (loadedMuseumDataDelegate.isInitialized()) {
            if (!file.exists()) {
                file.createNewFile()
            }
            file.writeText(Gson().toJson(loadedMuseumData))
        }
    }
}
