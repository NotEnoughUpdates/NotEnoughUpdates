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

import io.github.moulberry.notenoughupdates.NEUManager
import io.github.moulberry.notenoughupdates.NotEnoughUpdates
import io.github.moulberry.notenoughupdates.core.util.StringUtils
import net.minecraft.inventory.IInventory
import net.minecraft.item.EnumDyeColor
import net.minecraft.item.ItemDye
import net.minecraft.item.ItemStack

object MuseumUtil {

    data class MuseumItem(
        /**
         * A potentially non-exhaustive list of item ids that are required for this museum donation.
         */
        val skyblockItemIds: List<String>,
        val state: DonationState,
    )

    enum class DonationState {
        /**
         * Donated armor only shows one piece, so we use that for id resolution, which might result in incomplete
         * results (hence the separate state). This still means that the entire set is donated, but it is guaranteed to
         * be only a partial result. Other values of this enum do not guarantee a full result, but at least they do not
         * guarantee a partial one.
         */
        DONATED_PRESENT_PARTIAL,
        DONATED_PRESENT,
        DONATED_VACANT,
        MISSING,
    }

    fun findMuseumItem(stack: ItemStack, isOnArmorPage: Boolean): MuseumItem? {
        val item = stack.item ?: return null
        val items by lazy { findItemsByName(stack.displayName, isOnArmorPage)}
        if (item is ItemDye) {
            val dyeColor = EnumDyeColor.byDyeDamage(stack.itemDamage)
            if (dyeColor == EnumDyeColor.LIME) {
                // Item is donated, but not present in the museum
                return MuseumItem(items, DonationState.DONATED_VACANT)
            } else if (dyeColor == EnumDyeColor.GRAY) {
                // Item is not donated
                return MuseumItem(items, DonationState.MISSING)
            }
            // Otherwise unknown item, try to analyze as normal item.
        }
        val skyblockId = NotEnoughUpdates.INSTANCE.manager.createItemResolutionQuery().withItemStack(stack)
            .resolveInternalName()
        if (skyblockId != null) {
            return MuseumItem(
                listOf(skyblockId),
                if (isOnArmorPage) DonationState.DONATED_PRESENT_PARTIAL else DonationState.DONATED_PRESENT
            )
        }
        return MuseumItem(
            items,
            DonationState.DONATED_PRESENT
        )
    }

    fun findItemsByName(displayName: String, armor: Boolean): List<String> {
        return (if (armor)
            findMuseumArmorSetByName(displayName)
        else
            listOf(findMuseumItemByName(displayName))).filterNotNull()

    }

    fun findMuseumItemByName(displayName: String): String? =
        ItemResolutionQuery.findInternalNameByDisplayName(displayName, true)


    fun findMuseumArmorSetByName(displayName: String): List<String?> {
        val armorSlots = arrayOf(
            "HELMET",
            "LEGGINGS",
            "CHESTPLATE",
            "BOOTS"
        )
        val monochromeName = NEUManager.cleanForTitleMapSearch(displayName)
        val results = ItemResolutionQuery.findInternalNameCandidatesForDisplayName(displayName)
            .asSequence()
            .filter {
                val item = NotEnoughUpdates.INSTANCE.manager.createItem(it)
                val name = NEUManager.cleanForTitleMapSearch(item.displayName)
                monochromeName.replace("armor", "") in name
            }
            .toSet()
        return armorSlots.map { armorSlot ->
            results.singleOrNull { armorSlot in it }
        }
    }

    fun isMuseumInventory(inventory: IInventory): Boolean {
        return StringUtils.cleanColour(inventory.displayName.unformattedText).startsWith("Museum ➜")
    }
}
