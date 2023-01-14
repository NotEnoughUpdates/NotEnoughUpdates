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

package io.github.moulberry.notenoughupdates.miscfeatures.inventory

import io.github.moulberry.notenoughupdates.NotEnoughUpdates
import io.github.moulberry.notenoughupdates.core.util.ArrowPagesUtils
import io.github.moulberry.notenoughupdates.mixins.AccessorGuiContainer
import io.github.moulberry.notenoughupdates.util.MuseumUtil
import io.github.moulberry.notenoughupdates.util.MuseumUtil.DonationState.MISSING
import io.github.moulberry.notenoughupdates.util.Utils
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.init.Items
import net.minecraft.inventory.Slot
import net.minecraft.util.EnumChatFormatting
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.event.GuiScreenEvent
import net.minecraftforge.client.event.GuiScreenEvent.BackgroundDrawnEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.input.Mouse
import org.lwjgl.opengl.GL11


object MuseumCheapestItemOverlay {
    data class MuseumItem(
        var name: String,
        var internalNames: List<String>,
        var value: Double,
        var priceRefreshedAt: Long
    )

    private const val ITEMS_PER_PAGE = 8

    private val backgroundResource: ResourceLocation = ResourceLocation("notenoughupdates:dungeon_chest_worth.png")

    val config get() = NotEnoughUpdates.INSTANCE.config.museum

    /**
     * The top left position of the arrows to be drawn, used by [ArrowPagesUtils]
     */
    private var topLeft = intArrayOf(237, 110)
    private var currentPage: Int = 0
    private var previousSlots: List<Slot> = emptyList()
    private var itemsToDonate: MutableList<MuseumItem> = emptyList<MuseumItem>().toMutableList()

    /**
     *category -> was the highest page visited?
     */
    private var checkedPages: HashMap<String, Boolean> = hashMapOf(
        //this page only shows items when you have already donated them -> there is no useful information to gather
        "Special Items" to true,
        "Weapons" to false,
        "Armor Sets" to false,
        "Rarities" to false
    )

    /**
     * Draw the overlay and parse items, if applicable
     */
    @SubscribeEvent
    fun onDrawBackground(event: BackgroundDrawnEvent) {
        if (!shouldRender(event.gui)) return
        val chest = event.gui as GuiChest

        val slots = chest.inventorySlots.inventorySlots
        if (!slots.equals(previousSlots)) {
            checkIfHighestPageWasVisited(slots)
            parseItems(slots)
            updateOutdatedValues()
            sortByValue()
        }
        previousSlots = slots

        val xSize = (event.gui as AccessorGuiContainer).xSize
        val guiLeft = (event.gui as AccessorGuiContainer).guiLeft
        val guiTop = (event.gui as AccessorGuiContainer).guiTop

        drawBackground(guiLeft, xSize, guiTop)
        drawLines(guiLeft, guiTop)
    }

    /**
     * Pass on mouse clicks to [ArrowPagesUtils], if applicable
     */
    @SubscribeEvent
    fun onMouseClick(event: GuiScreenEvent.MouseInputEvent.Pre) {
        if (!shouldRender(event.gui)) return
        if (!Mouse.getEventButtonState()) return
        val guiLeft = (event.gui as AccessorGuiContainer).guiLeft
        val guiTop = (event.gui as AccessorGuiContainer).guiTop
        ArrowPagesUtils.onPageSwitchMouse(
            guiLeft, guiTop, topLeft, currentPage, totalPages()
        ) { pageChange: Int -> currentPage = pageChange }
    }

    /**
     * Sort the collected items by their calculated value
     */
    private fun sortByValue() {
        itemsToDonate.sortBy { it.value }
    }

    /**
     * Update all values that have not been updated for the last minute
     */
    private fun updateOutdatedValues() {
        val time = System.currentTimeMillis()
        itemsToDonate.filter { time - it.priceRefreshedAt >= 60000 }
            .forEach {
                it.value = calculateValue(it.internalNames)
                it.priceRefreshedAt = time
            }
    }

    /**
     * Calculate the value of an item as displayed in the museum, which may consist of multiple pieces
     */
    private fun calculateValue(internalNames: List<String>): Double {
        var totalValue = 0.0
        internalNames.forEach {
            val itemValue: Double =
                when (config.museumCheapestItemOverlayValueSource) {
                    0 -> NotEnoughUpdates.INSTANCE.manager.auctionManager.getBazaarOrBin(it, false)
                    1 -> NotEnoughUpdates.INSTANCE.manager.auctionManager.getCraftCost(it)?.craftCost ?: return@forEach
                    else -> -1.0 //unreachable
                }
            if (itemValue == -1.0 || itemValue == 0.0) {
                totalValue = Double.MAX_VALUE
                return@forEach
            } else {
                totalValue += itemValue
            }
        }
        if (totalValue == 0.0) {
            totalValue = Double.MAX_VALUE
        }

        return totalValue
    }

    /**
     * Draw the lines containing the displayname and value over the background
     */
    private fun drawLines(guiLeft: Int, guiTop: Int) {
        val lines = buildLines()
        lines.forEachIndexed { index, line ->
            if (index == ITEMS_PER_PAGE && !visitedAllPages()) {
                Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow(
                    "${EnumChatFormatting.RED}Visit all pages for accurate info!",
                    (guiLeft + 185).toFloat(),
                    (guiTop + 85).toFloat(),
                    0
                )
            } else {
                Utils.renderAlignedString(
                    "${EnumChatFormatting.RESET}${line.name}",
                    if (line.value == Double.MAX_VALUE) "${EnumChatFormatting.RED}Unknown" else "${EnumChatFormatting.AQUA}${
                        Utils.shortNumberFormat(
                            line.value,
                            0
                        )
                    }",
                    (guiLeft + 187).toFloat(),
                    (guiTop + 5 + (index * 10)).toFloat(),
                    160
                )
            }
        }

        ArrowPagesUtils.onDraw(guiLeft, guiTop, topLeft, currentPage, totalPages())
        return
    }

    /**
     * Create the list of [MuseumItem]s that should be displayed on the current page
     */
    private fun buildLines(): List<MuseumItem> {
        val list = emptyList<MuseumItem>().toMutableList()
        for (i in (if (currentPage == 0) ITEMS_PER_PAGE else ITEMS_PER_PAGE + 1) * (currentPage)..(if (currentPage == 0) ITEMS_PER_PAGE else ITEMS_PER_PAGE + 1) * currentPage + ITEMS_PER_PAGE) {
            if (i >= itemsToDonate.size) {
                break
            }
            list.add(itemsToDonate[i])
        }
        return list
    }

    /**
     * Parse the not already donated items present in the currently open Museum page
     */
    private fun parseItems(slots: List<Slot>) {
        // Iterate upper chest with 56 slots
        val time = System.currentTimeMillis()
        val armor = Utils.getOpenChestName().endsWith("Armor Sets")
        for (i in 0..53) {
            val stack = slots[i].stack ?: continue
            val parsedItems = MuseumUtil.findMuseumItem(stack, armor) ?: continue
            when (parsedItems.state) {
                MISSING ->
                    if (itemsToDonate.none { it.internalNames == parsedItems.skyblockItemIds })
                        itemsToDonate.add(
                            MuseumItem(
                                stack.displayName,
                                parsedItems.skyblockItemIds,
                                calculateValue(parsedItems.skyblockItemIds),
                                time
                            )
                        )

                else -> itemsToDonate.retainAll { it.internalNames != parsedItems.skyblockItemIds }
            }
        }
    }

    /**
     * Check if the highest page for the current category is currently open and update [checkedPages] accordingly
     */
    private fun checkIfHighestPageWasVisited(slots: List<Slot>) {
        val category = getCategory()
        val nextPageSlot = slots[53]
        // If the "Next Page" arrow is missing, we are at the highest page
        if ((nextPageSlot.stack ?: return).item != Items.arrow) {
            checkedPages[category] = true
        }
    }

    /**
     * Draw the background texture to the right side of the open Museum Page
     */
    private fun drawBackground(guiLeft: Int, xSize: Int, guiTop: Int) {
        Minecraft.getMinecraft().textureManager.bindTexture(backgroundResource)
        GL11.glColor4f(1F, 1F, 1F, 1F)
        GlStateManager.disableLighting()
        Utils.drawTexturedRect(
            guiLeft.toFloat() + xSize + 4,
            guiTop.toFloat(),
            180F,
            101F,
            0F,
            180 / 256F,
            0F,
            101 / 256F,
            GL11.GL_NEAREST
        )
    }

    /**
     * Determine if the overlay should be active based on the config option and the currently open GuiChest, if applicable
     */
    private fun shouldRender(gui: GuiScreen): Boolean =
        config.museumCheapestItemOverlay && gui is GuiChest && Utils.getOpenChestName()
            .startsWith("Museum ➜")

    /**
     * Determine the name of the currently open Museum Category
     */
    private fun getCategory(): String = Utils.getOpenChestName().substring(9, Utils.getOpenChestName().length)

    /**
     * Determine if all useful pages have been visited
     */
    private fun visitedAllPages(): Boolean = !checkedPages.containsValue(false)

    /**
     * Calculate the total amount of pages the overlay should have
     */
    private fun totalPages(): Int = when (itemsToDonate.size % ITEMS_PER_PAGE) {
        0 -> itemsToDonate.size / ITEMS_PER_PAGE
        else -> (itemsToDonate.size / ITEMS_PER_PAGE) + 1
    }
}
