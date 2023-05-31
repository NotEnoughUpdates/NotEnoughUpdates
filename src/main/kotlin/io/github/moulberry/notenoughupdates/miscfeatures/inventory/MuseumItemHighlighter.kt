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

package io.github.moulberry.notenoughupdates.miscfeatures.inventory

import io.github.moulberry.notenoughupdates.NotEnoughUpdates
import io.github.moulberry.notenoughupdates.autosubscribe.NEUAutoSubscribe
import io.github.moulberry.notenoughupdates.core.ChromaColour
import io.github.moulberry.notenoughupdates.core.util.StringUtils
import io.github.moulberry.notenoughupdates.events.GuiContainerBackgroundDrawnEvent
import io.github.moulberry.notenoughupdates.events.ReplaceItemEvent
import io.github.moulberry.notenoughupdates.events.RepositoryReloadEvent
import io.github.moulberry.notenoughupdates.util.ItemUtils
import io.github.moulberry.notenoughupdates.util.LRUCache
import io.github.moulberry.notenoughupdates.util.MuseumUtil
import io.github.moulberry.notenoughupdates.util.MuseumUtil.isMuseumInventory
import net.minecraft.client.gui.Gui
import net.minecraft.init.Items
import net.minecraft.inventory.ContainerChest
import net.minecraft.item.EnumDyeColor
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@NEUAutoSubscribe
object MuseumItemHighlighter {

    private val manager get() = NotEnoughUpdates.INSTANCE.manager
    private val config get() = NotEnoughUpdates.INSTANCE.config.museum

    private fun getHighlightColor() = ChromaColour.specialToChromaRGB(config.museumItemColor)


    private val findRawItemForName = LRUCache.memoize(::findRawItemForName0, 4 * 7 * 2)

    @SubscribeEvent
    fun onRepositoryReload(event: RepositoryReloadEvent) {
        findRawItemForName.clearCache()
    }

    private fun findRawItemForName0(arg: Pair<String, Boolean>): ItemStack? {
        val (name, armor) = arg
        return MuseumUtil.findItemsByName(name, armor).firstOrNull()?.let { manager.createItem(it) }
    }


    @SubscribeEvent
    fun onItemOverride(event: ReplaceItemEvent) {
        if (!config.museumItemShow) return
        if (!isMuseumInventory(event.inventory)) return
        val original = event.original ?: return
        if (!isCompletedRetrievedItem(original)) return
        val armor = StringUtils.cleanColour(event.inventory.displayName.unformattedText).endsWith("Armor Sets")
        val rawItem = findRawItemForName.apply(original.displayName to armor) ?: return
        val hydratedItem = hydrateMuseumItem(rawItem, original)
        event.replaceWith(hydratedItem)
    }

    fun isCompletedRetrievedItem(itemStack: ItemStack): Boolean {
        return itemStack.hasDisplayName() && itemStack.item == Items.dye && EnumDyeColor.byDyeDamage(itemStack.itemDamage) == EnumDyeColor.LIME
    }

    @SubscribeEvent
    fun onBackgroundDrawn(event: GuiContainerBackgroundDrawnEvent) {
        val egui = event.container ?: return
        val chest = egui.inventorySlots as? ContainerChest ?: return
        if (!config.museumItemShow) return
        if (!isMuseumInventory(chest.lowerChestInventory)) return
        val fixedHighlightColor = getHighlightColor()
        for (slot in chest.inventorySlots) {
            if (slot == null || slot.stack == null) continue
            if (isHydratedMuseumItem(slot.stack) || isCompletedRetrievedItem(slot.stack)) {
                val left = slot.xDisplayPosition
                val top = slot.yDisplayPosition
                Gui.drawRect(
                    left, top,
                    left + 16, top + 16,
                    fixedHighlightColor
                )
            }
        }
    }

    fun hydrateMuseumItem(rawItem: ItemStack, original: ItemStack) = rawItem.copy().apply {
        setStackDisplayName(original.displayName)
        val originalLore = ItemUtils.getLore(original).toMutableList()
        ItemUtils.setLore(this, originalLore)
        val data = ItemUtils.getOrCreateTag(this)
        val extraAttributes = data.getCompoundTag("ExtraAttributes")
        extraAttributes.setByte("donated_museum", 1)
        data.setTag("ExtraAttributes", extraAttributes)
        data.setBoolean(MUSEUM_HYDRATED_ITEM_TAG, true)
    }

    fun isHydratedMuseumItem(stack: ItemStack): Boolean {
        return ItemUtils.getOrCreateTag(stack).getBoolean(MUSEUM_HYDRATED_ITEM_TAG)
    }

    const val MUSEUM_HYDRATED_ITEM_TAG = "NEU_HYDRATED_MUSEUM_ITEM"

}
