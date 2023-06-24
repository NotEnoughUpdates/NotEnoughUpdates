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

package io.github.moulberry.notenoughupdates.recipes.generators

import com.google.auto.service.AutoService
import io.github.moulberry.notenoughupdates.options.customtypes.NEUDebugFlag
import io.github.moulberry.notenoughupdates.recipes.Ingredient
import io.github.moulberry.notenoughupdates.recipes.ItemShopRecipe
import io.github.moulberry.notenoughupdates.util.ItemUtils
import io.github.moulberry.notenoughupdates.util.JsonUtils
import io.github.moulberry.notenoughupdates.util.SBInfo
import io.github.moulberry.notenoughupdates.util.Utils
import io.github.moulberry.notenoughupdates.util.kotlin.set
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.inventory.ContainerChest
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.StringUtils

@AutoService(RepoExporter::class)
class ItemShopExporter : RepoExporter {
    override suspend fun export(context: RepoExportingContext) {
        val chest = context.gui as GuiChest
        val container = chest.inventorySlots as ContainerChest
        val inventory = container.lowerChestInventory
        val displayName = inventory.displayName.unformattedText
        val npcInternalName = displayName.uppercase().replace(" ", "_") + "_NPC"
        val file = context.manager.repoLocation.resolve("items/$npcInternalName.json")
        val itemDisplayName = "§9$displayName (NPC)"
        val baseNPCJson = context.manager.createItemJson(
            npcInternalName,
            "minecraft:skull",
            itemDisplayName,
            arrayOf(""),
            null,
            "WIKI_URL",
            arrayOf("TODO"),
            "viewrecipe",
            3,
            NBTTagCompound()
        )
        baseNPCJson["x"] = context.mc.thePlayer.posX.toInt()
        baseNPCJson["y"] = context.mc.thePlayer.posY.toInt()
        baseNPCJson["z"] = context.mc.thePlayer.posZ.toInt()
        baseNPCJson["island"] = SBInfo.getInstance().getLocation() ?: "none"

        val recipes = mutableListOf<ItemShopRecipe>()
        for (slotNum in 0 until inventory.sizeInventory) {
            val slot = inventory.getStackInSlot(slotNum) ?: continue
            if (slot.displayName.isNullOrBlank()) continue

            recipes.add(findRecipe(context, slot) ?: continue)
        }
        baseNPCJson["recipes"] = JsonUtils.transformListToJsonArray(recipes, ItemShopRecipe::serialize)
        context.writeFile(file, baseNPCJson)
    }

    val coinRegex = "^([0-9,]+) Coins$".toRegex()
    val itemRegex = "^(?<name>.*?)(?: x(?<amount>[0-9,]+))?$".toRegex()
    suspend fun findRecipe(
        context: RepoExportingContext,
        stack: ItemStack,
    ): ItemShopRecipe? {
        val resultName =
            context.manager.createItemResolutionQuery().withItemStack(stack).resolveInternalName() ?: return null

        var inCost = false
        val costList = mutableListOf<Ingredient>()
        for (loreLine in ItemUtils.getLore(stack)) {
            var loreLine = loreLine
            if (loreLine == "§7Cost") {
                inCost = true
                continue
            } else if (loreLine == "§eClick to trade!") {
                inCost = false
                continue
            } else if (loreLine.startsWith("§7Cost: ")) {
                loreLine = loreLine.substringAfter("§7Cost: ")
                inCost = true
            } else if (!inCost) continue
            val cleanLine = StringUtils.stripControlCodes(loreLine)
            if (cleanLine.isBlank()) continue
            val ingredient = findIngredientForLoreLine(context, cleanLine)
            if (ingredient == null) {
                Utils.addChatMessage("Warning: Could not find ingredient for $loreLine")
            } else {
                costList.add(ingredient)
            }
        }
        return ItemShopRecipe(
            null, costList, Ingredient(context.manager, resultName, stack.stackSize.toDouble()), null
        )
    }

    suspend fun findIngredientForLoreLine(
        context: RepoExportingContext,
        loreLine: String,
    ): Ingredient? {
        val coinMatch = coinRegex.matchEntire(loreLine)
        if (coinMatch != null) {
            val amount = coinMatch.groupValues[1].replace(",", "").toInt()
            return Ingredient.coinIngredient(context.manager, amount)
        }
        val itemMatch = itemRegex.matchEntire(loreLine)
        if (itemMatch != null) {
            val label = itemMatch.groups["name"]!!.value
            val amount = itemMatch.groups["amount"]?.value?.replace(",", "")?.toDouble() ?: 1.0
            if (context.manager.itemInformation.containsKey(label.uppercase())) {
                return Ingredient(context.manager, label, amount)
            }
            return Ingredient(context.manager, context.findItemByName(label), amount)
        }
        return null
    }

    override fun canExport(gui: GuiScreen): Boolean {
        if (NEUDebugFlag.ALWAYS_EXPORT_SHOPS.isSet) return true
        if (gui !is GuiChest) return false
        val buyBackSlot = 4 + 9 * 5
        val stacks = gui.inventorySlots.inventory
        if (buyBackSlot !in stacks.indices) return false
        val buyBackStack = stacks[buyBackSlot] ?: return false
        return Utils.cleanColour(buyBackStack.displayName) == "Sell Item" ||
                ItemUtils.getLore(buyBackStack).any { Utils.cleanColour(it) == "Click to buyback!" }
    }

    override val name: String
        get() = "Item Shop"
}
