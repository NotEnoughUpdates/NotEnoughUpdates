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

package io.github.moulberry.notenoughupdates.miscfeatures.profileviewer.export

import com.google.auto.service.AutoService
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import io.github.moulberry.notenoughupdates.core.util.StringUtils
import io.github.moulberry.notenoughupdates.recipes.generators.RepoExporter
import io.github.moulberry.notenoughupdates.recipes.generators.RepoExportingContext
import io.github.moulberry.notenoughupdates.util.ItemUtils
import io.github.moulberry.notenoughupdates.util.JsonUtils
import io.github.moulberry.notenoughupdates.util.copyToClipboard
import io.github.moulberry.notenoughupdates.util.kotlin.useMatcher
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.entity.player.InventoryPlayer
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.item.Item

@AutoService(RepoExporter::class)
class HotmExporter : RepoExporter {
    val tierRegex = "Tier (\\d+)".toPattern()
    val levelRegex = "Level .*/(\\d+)".toPattern()

    fun String.clean() = StringUtils.cleanColour(this)

    override suspend fun export(context: RepoExportingContext) {
        val gui = context.gui as GuiChest
        val jsonObject = JsonObject()
        for (inventorySlot in gui.inventorySlots.inventorySlots) {
            if (inventorySlot.inventory is InventoryPlayer) continue
            val stack = inventorySlot.stack ?: continue
            val isNormalPerk = stack.item in setOf(Items.diamond, Items.coal, Items.emerald, Items.redstone)
            val isAbilityPerk = stack.item in setOf(
                Item.getItemFromBlock(Blocks.coal_block),
                Item.getItemFromBlock(Blocks.emerald_block)
            )
            if (!(isAbilityPerk || isNormalPerk)) continue
            val itemJsonObject = JsonObject()
            itemJsonObject.addProperty("name", stack.displayName)
            val lore = ItemUtils.getLore(stack)
            itemJsonObject.addProperty("x", inventorySlot.slotNumber % 9 - 1)
            val tierSlot = inventorySlot.slotNumber - inventorySlot.slotNumber % 9
            val yItem = inventorySlot.inventory.getStackInSlot(tierSlot) ?: error("No y item stack at $tierSlot")
            itemJsonObject.addProperty(
                "y",
                10 - (tierRegex.useMatcher(yItem.displayName.clean()) { group(1).toInt() }
                    ?: error("No tier number on y item stack at $tierSlot"))
            )
            val maxLevel = lore.firstNotNullOfOrNull {
                levelRegex.useMatcher(it.clean()) { group(1).toInt() }
            }
            if (maxLevel != null)
                itemJsonObject.addProperty("maxLevel", maxLevel)
            itemJsonObject.addProperty("powder", "UNKNOWN")
            itemJsonObject.addProperty("item", if (isNormalPerk) "(npi level0 maxLevel)" else "(api level0)")
            itemJsonObject.addProperty("cost", "UNKNOWN")
            itemJsonObject.addProperty("stat", "UNKNOWN")
            itemJsonObject.add(
                "lore",
                JsonUtils.transformListToJsonArray(
                    lore.dropWhile { it.clean().startsWith("Level") || it.clean().isBlank() }
                        .takeWhile { !it.clean().startsWith("Cost") }
                        .let { intermediary ->
                            if (intermediary.any { it.contains("=====[ ") })
                                intermediary.dropLastWhile { it.clean() != "=====[ UPGRADE ]=====" }
                                    .dropLast(1)
                            else intermediary
                        }
                        .dropLastWhile { it.clean().isBlank() }
                ) { JsonPrimitive(it) }
            )
            jsonObject.add(stack.displayName.clean().lowercase().replace(" ", "_"), itemJsonObject)
        }
        jsonObject.toString().copyToClipboard()
    }

    override fun canExport(gui: GuiScreen): Boolean {
        return gui is GuiChest && gui.inventorySlots.inventorySlots.getOrNull(49)
            ?.stack?.displayName?.let(StringUtils::cleanColour) == "Heart of the Mountain"
    }

    override val name: String
        get() = "HOTM Perks"
}
