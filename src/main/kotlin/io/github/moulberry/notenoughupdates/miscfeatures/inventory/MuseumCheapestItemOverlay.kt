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

import io.github.moulberry.notenoughupdates.NotEnoughUpdates
import io.github.moulberry.notenoughupdates.core.util.ArrowPagesUtils
import io.github.moulberry.notenoughupdates.core.util.render.TextRenderUtils
import io.github.moulberry.notenoughupdates.events.ButtonExclusionZoneEvent
import io.github.moulberry.notenoughupdates.mixins.AccessorGuiContainer
import io.github.moulberry.notenoughupdates.options.seperateSections.Museum
import io.github.moulberry.notenoughupdates.util.*
import io.github.moulberry.notenoughupdates.util.MuseumUtil.DonationState.MISSING
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.inventory.Slot
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumChatFormatting
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.event.GuiScreenEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.input.Mouse
import org.lwjgl.opengl.GL11
import kotlin.math.ceil


object MuseumCheapestItemOverlay {

    enum class Category {
        WEAPONS,
        ARMOUR_SETS,
        RARITIES,
        NOT_APPLICABLE; // Either not a valid category or inside the "Special Items" category, which is not useful;

        /**
         * Convert to readable String to be displayed to the user
         */
        override fun toString(): String {
            return when (this) {
                WEAPONS -> "Weapons"
                ARMOUR_SETS -> "Armour Sets"
                RARITIES -> "Rarities"
                NOT_APPLICABLE -> "Everything"
            }
        }
    }

    data class MuseumItem(
        var name: String,
        var internalNames: List<String>,
        var value: Double,
        var priceRefreshedAt: Long,
        var category: Category
    )

    private const val ITEMS_PER_PAGE = 10

    private val backgroundResource: ResourceLocation = ResourceLocation("notenoughupdates:minion_overlay.png")

    val config: Museum get() = NotEnoughUpdates.INSTANCE.config.museum

    /**
     * The top left position of the arrows to be drawn, used by [ArrowPagesUtils]
     */
    private var topLeft = intArrayOf(237, 110)
    private var currentPage: Int = 0
    private var previousSlots: List<Slot> = emptyList()
    private var itemsToDonate: MutableList<MuseumItem> = emptyList<MuseumItem>().toMutableList()
    private var leftButtonRect = Rectangle(0, 0, 0, 0)
    private var rightButtonRect = Rectangle(0, 0, 0, 0)
    private var selectedCategory = Category.NOT_APPLICABLE
    private var totalPages = 0

    /**
     *category -> was the highest page visited?
     */
    private var checkedPages: HashMap<Category, Boolean> = hashMapOf(
        //this page only shows items when you have already donated them -> there is no useful information to gather
        Category.WEAPONS to false,
        Category.ARMOUR_SETS to false,
        Category.RARITIES to false
    )

    /**
     * Draw the overlay and parse items, if applicable
     */
    @SubscribeEvent
    fun onDrawBackground(event: GuiScreenEvent.BackgroundDrawnEvent) {
        if (!shouldRender(event.gui)) return
        val chest = event.gui as GuiChest

        val slots = chest.inventorySlots.inventorySlots
        //check if there is any info to gather only when a category is currently open
        if (!slots.equals(previousSlots) && Utils.getOpenChestName().startsWith("Museum ➜")) {
            checkIfHighestPageWasVisited(slots)
            parseItems(slots)
            updateOutdatedValues()
        }
        previousSlots = slots

        val xSize = (event.gui as AccessorGuiContainer).xSize
        val guiLeft = (event.gui as AccessorGuiContainer).guiLeft
        val guiTop = (event.gui as AccessorGuiContainer).guiTop

        drawBackground(guiLeft, xSize, guiTop)
        drawLines(guiLeft, guiTop)
        drawButtons(guiLeft, xSize, guiTop)
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
            guiLeft, guiTop, topLeft, currentPage, totalPages
        ) { pageChange: Int -> currentPage = pageChange }
    }

    @SubscribeEvent
    fun onButtonExclusionZones(event: ButtonExclusionZoneEvent) {
        if (shouldRender(event.gui)) {
            event.blockArea(
                Rectangle(
                    event.guiBaseRect.right,
                    event.guiBaseRect.top,
                    175, 130
                ), ButtonExclusionZoneEvent.PushDirection.TOWARDS_RIGHT
            )
        }
    }

    @SubscribeEvent
    fun onMouseInput(event: GuiScreenEvent.MouseInputEvent.Pre) {
        if (!shouldRender(event.gui)) return
        val mouseX = Utils.getMouseX()
        val mouseY = Utils.getMouseY()
        if (Mouse.getEventButtonState() && leftButtonRect.contains(mouseX, mouseY)) {
            config.museumCheapestItemOverlayValueSource = 1 - config.museumCheapestItemOverlayValueSource
            updateAllValues()
        } else if (Mouse.getEventButtonState() && rightButtonRect.contains(mouseX, mouseY)) {
            advanceSelectedCategory()
        }
    }

    /**
     * Move the selected category one index forward, or back to the start when already at the end
     */
    private fun advanceSelectedCategory() {
        val nextValueIndex = (selectedCategory.ordinal + 1) % 4
        selectedCategory = enumValues<Category>()[nextValueIndex]
    }

    /**
     * Draw the two clickable buttons on the bottom right and display a tooltip if needed
     */
    private fun drawButtons(guiLeft: Int, xSize: Int, guiTop: Int) {
        RenderHelper.enableGUIStandardItemLighting()
        val useBIN = config.museumCheapestItemOverlayValueSource == 0
        val mouseX = Utils.getMouseX()
        val mouseY = Utils.getMouseY()
        val scaledResolution = ScaledResolution(Minecraft.getMinecraft())
        val width = scaledResolution.scaledWidth
        val height = scaledResolution.scaledHeight

        // Left button
        val leftItemStack = if (useBIN) {
            ItemUtils.getCoinItemStack(100000.0)
        } else {
            ItemStack(Blocks.crafting_table)
        }
        leftButtonRect = Rectangle(
            guiLeft + xSize + 131,
            guiTop + 106,
            16,
            16
        )
        Minecraft.getMinecraft().renderItem.renderItemIntoGUI(
            leftItemStack,
            leftButtonRect.x,
            leftButtonRect.y
        )

        if (leftButtonRect.contains(mouseX, mouseY)) {
            val tooltip = if (useBIN) {
                listOf(
                    "${EnumChatFormatting.GREEN}Using ${EnumChatFormatting.BLUE}lowest BIN ${EnumChatFormatting.GREEN}as price source!",
                    "",
                    "${EnumChatFormatting.YELLOW}Click to switch to craft cost!"
                )
            } else {
                listOf(
                    "${EnumChatFormatting.GREEN}Using ${EnumChatFormatting.AQUA}craft cost ${EnumChatFormatting.GREEN}as price source!",
                    "",
                    "${EnumChatFormatting.YELLOW}Click to switch to lowest BIN!"
                )
            }
            Utils.drawHoveringText(
                tooltip,
                mouseX,
                mouseY,
                width,
                height,
                -1,
                Minecraft.getMinecraft().fontRendererObj
            )
        }

        // Right button
        val rightItemStack = when (selectedCategory) {
            Category.WEAPONS -> ItemStack(Items.diamond_sword)
            Category.ARMOUR_SETS -> ItemStack(Items.diamond_chestplate)
            Category.RARITIES -> ItemStack(Items.emerald)
            Category.NOT_APPLICABLE -> ItemStack(Items.filled_map)
        }
        rightButtonRect = Rectangle(
            guiLeft + xSize + 150,
            guiTop + 106,
            16,
            16
        )
        Minecraft.getMinecraft().renderItem.renderItemIntoGUI(
            rightItemStack,
            rightButtonRect.x,
            rightButtonRect.y
        )
        if (rightButtonRect.contains(mouseX, mouseY)) {
            val tooltip = mutableListOf(
                "${EnumChatFormatting.GREEN}Category Filter",
                "",
            )
            for (category in Category.values()) {
                tooltip.add(
                    if (category == selectedCategory) {
                        "${EnumChatFormatting.BLUE}>$category"
                    } else {
                        category.toString()
                    }
                )
            }

            tooltip.add("")
            tooltip.add("${EnumChatFormatting.YELLOW}Click to advance!")
            Utils.drawHoveringText(
                tooltip,
                mouseX,
                mouseY,
                width,
                height,
                -1,
                Minecraft.getMinecraft().fontRendererObj
            )
        }
        RenderHelper.disableStandardItemLighting()
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
     * Update all values regardless of the time of the last update
     */
    private fun updateAllValues() {
        val time = System.currentTimeMillis()
        itemsToDonate.forEach {
            it.value = calculateValue(it.internalNames)
            it.priceRefreshedAt = time
        }
        sortByValue()
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
        val mouseX = Utils.getMouseX()
        val mouseY = Utils.getMouseY()
        val scaledResolution = ScaledResolution(Minecraft.getMinecraft())
        val width = scaledResolution.scaledWidth
        val height = scaledResolution.scaledHeight

        val applicableItems = if (selectedCategory == Category.NOT_APPLICABLE) {
            itemsToDonate
        } else {
            itemsToDonate.toList().filter { it.category == selectedCategory }
        }
        val lines = buildLines(applicableItems)
        totalPages = ceil(applicableItems.size.toFloat() / ITEMS_PER_PAGE.toFloat()).toInt()

        lines.forEachIndexed { index, line ->
            if (!visitedAllPages() && (index == ITEMS_PER_PAGE || index == lines.size - 1)) {
                TextRenderUtils.drawStringScaledMaxWidth(
                    "${EnumChatFormatting.RED}Visit all pages for accurate info!",
                    Minecraft.getMinecraft().fontRendererObj,
                    (guiLeft + 185).toFloat(),
                    (guiTop + 95).toFloat(),
                    true,
                    155,
                    0
                )
                return@forEachIndexed
            } else {
                val x = (guiLeft + 187).toFloat()
                val y = (guiTop + 5 + (index * 10)).toFloat()
                Utils.renderAlignedString(
                    line.name,
                    if (line.value == Double.MAX_VALUE) "${EnumChatFormatting.RED}Unknown ${if (config.museumCheapestItemOverlayValueSource == 0) "BIN" else "Craft Cost"}" else "${EnumChatFormatting.AQUA}${
                        Utils.shortNumberFormat(
                            line.value,
                            0
                        )
                    }",
                    x,
                    y,
                    156
                )

                if (Utils.isWithinRect(mouseX, mouseY, x.toInt(), y.toInt(), 170, 10)) {
                    val tooltip = mutableListOf(line.name, "")
                    //armor set
                    if (line.internalNames.size > 1) {
                        tooltip.add("${EnumChatFormatting.AQUA}Consists of:")
                        line.internalNames.forEach {
                            val displayname =
                                NotEnoughUpdates.INSTANCE.manager.createItemResolutionQuery().withKnownInternalName(it)
                                    .resolveToItemListJson()
                                    ?.get("displayname")?.asString ?: "ERROR"
                            val value = calculateValue(listOf(it))

                            // Creates:"  - displayname (price)" OR "  - displayname (No BIN found!)"
                            tooltip.add(
                                "  ${EnumChatFormatting.DARK_GRAY}-${EnumChatFormatting.RESET} $displayname${EnumChatFormatting.DARK_GRAY} (${EnumChatFormatting.GOLD}${
                                    if (value == Double.MAX_VALUE) {
                                        "${EnumChatFormatting.RED}No BIN found!"
                                    } else {
                                        Utils.shortNumberFormat(
                                            value,
                                            0
                                        )
                                    }
                                }${EnumChatFormatting.DARK_GRAY})"
                            )
                        }
                        tooltip.add("")
                    }

                    if (NotEnoughUpdates.INSTANCE.manager.getRecipesFor(line.internalNames[0]).isNotEmpty()) {
                        tooltip.add("${EnumChatFormatting.YELLOW}${EnumChatFormatting.BOLD}Click to open recipe!")
                    } else {
                        tooltip.add("${EnumChatFormatting.RED}${EnumChatFormatting.BOLD}No recipe available!")
                    }

                    if (Mouse.getEventButtonState()) {
                        //TODO? this only opens the recipe for one of the armor pieces
                        NotEnoughUpdates.INSTANCE.manager.showRecipe(line.internalNames[0])
                    }

                    Utils.drawHoveringText(
                        tooltip,
                        mouseX,
                        mouseY,
                        width,
                        height,
                        -1,
                        Minecraft.getMinecraft().fontRendererObj
                    )
                }
            }
        }

        //no page has been visited yet
        if (lines.isEmpty()) {
            TextRenderUtils.drawStringScaledMaxWidth(
                "${EnumChatFormatting.RED}No items matching filter!",
                Minecraft.getMinecraft().fontRendererObj,
                (guiLeft + 200).toFloat(),
                (guiTop + 128 / 2).toFloat(),
                true,
                155,
                0
            )
        }

        ArrowPagesUtils.onDraw(guiLeft, guiTop, topLeft, currentPage, totalPages)
        return
    }

    /**
     * Create the list of [MuseumItem]s that should be displayed on the current page
     */
    private fun buildLines(applicableItems: List<MuseumItem>): List<MuseumItem> {
        val list = emptyList<MuseumItem>().toMutableList()

        for (i in (ITEMS_PER_PAGE * currentPage) until ((ITEMS_PER_PAGE * currentPage) + ITEMS_PER_PAGE)) {
            if (i >= applicableItems.size) {
                break
            }

            list.add(applicableItems[i])
        }
        return list
    }

    /**
     * Parse the not already donated items present in the currently open Museum page
     */
    private fun parseItems(slots: List<Slot>) {
        Thread {
            val time = System.currentTimeMillis()
            val category = getCategory()
            if (category == Category.NOT_APPLICABLE) {
                return@Thread
            }
            val armor = category == Category.ARMOUR_SETS
            for (i in 0..53) {
                val stack = slots[i].stack ?: continue
                val parsedItems = MuseumUtil.findMuseumItem(stack, armor) ?: continue
                when (parsedItems.state) {
                    MISSING -> {
                        val displayName = if (armor) {
                            // Use the provided displayname for armor sets but change the color to blue (from red)
                            "${EnumChatFormatting.BLUE}${stack.displayName.stripControlCodes()}"
                        } else {
                            // Find out the real displayname and use it for normal items, if possible
                            NotEnoughUpdates.INSTANCE.manager.createItemResolutionQuery()
                                .withKnownInternalName(parsedItems.skyblockItemIds.first())
                                .resolveToItemListJson()
                                ?.get("displayname")?.asString ?: "${EnumChatFormatting.RED}ERROR"
                        }

                        //if the list does not already contain it, insert this MuseumItem
                        if (itemsToDonate.none { it.internalNames == parsedItems.skyblockItemIds }) {
                            itemsToDonate.add(
                                MuseumItem(
                                    displayName,
                                    parsedItems.skyblockItemIds,
                                    calculateValue(parsedItems.skyblockItemIds),
                                    time,
                                    category
                                )
                            )
                        }
                    }

                    else -> itemsToDonate.retainAll { it.internalNames != parsedItems.skyblockItemIds }
                }
            }
            sortByValue()
        }.start()
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
            (guiLeft + xSize + 4).toFloat(),
            guiTop.toFloat(),
            168f,
            128f,
            0f,
            1f,
            0f,
            1f,
            GL11.GL_NEAREST
        )
    }

    /**
     * Determine if the overlay should be active based on the config option and the currently open GuiChest, if applicable
     */
    private fun shouldRender(gui: GuiScreen): Boolean =
        config.museumCheapestItemOverlay && NotEnoughUpdates.INSTANCE.hasSkyblockScoreboard() && (gui is GuiChest && Utils.getOpenChestName()
            .startsWith("Museum ➜") || Utils.getOpenChestName() == "Your Museum")

    /**
     * Determine the currently open Museum Category
     */
    private fun getCategory(): Category =
        when (Utils.getOpenChestName().substring(9, Utils.getOpenChestName().length)) {
            "Weapons" -> Category.WEAPONS
            "Armor Sets" -> Category.ARMOUR_SETS
            "Rarities" -> Category.RARITIES
            else -> Category.NOT_APPLICABLE
        }

    /**
     * Determine if all useful pages have been visited
     */
    private fun visitedAllPages(): Boolean = !checkedPages.containsValue(false)
}
