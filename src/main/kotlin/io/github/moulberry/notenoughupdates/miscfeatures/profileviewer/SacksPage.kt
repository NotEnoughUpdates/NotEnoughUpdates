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

package io.github.moulberry.notenoughupdates.miscfeatures.profileviewer

import com.google.gson.JsonObject
import io.github.moulberry.notenoughupdates.NotEnoughUpdates
import io.github.moulberry.notenoughupdates.core.util.ArrowPagesUtils
import io.github.moulberry.notenoughupdates.core.util.StringUtils
import io.github.moulberry.notenoughupdates.core.util.render.RenderUtils
import io.github.moulberry.notenoughupdates.profileviewer.GuiProfileViewer
import io.github.moulberry.notenoughupdates.profileviewer.GuiProfileViewerPage
import io.github.moulberry.notenoughupdates.profileviewer.SkyblockProfiles
import io.github.moulberry.notenoughupdates.util.*
import io.github.moulberry.notenoughupdates.util.hypixelapi.HypixelItemAPI
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.ResourceLocation
import org.lwjgl.input.Mouse
import org.lwjgl.opengl.GL11


class SacksPage(pvInstance: GuiProfileViewer) : GuiProfileViewerPage(pvInstance) {
    private val manager get() = NotEnoughUpdates.INSTANCE.manager
    private val pv_sacks = ResourceLocation("notenoughupdates:pv_sacks.png")
    private var sacksJson = Constants.SACKS
    private var tooltipToDisplay = listOf<String>()
    private var currentProfile: SkyblockProfiles.SkyblockProfile? = null

    private var currentSack = "All"

    private var page = 0
    private var maxPage = 0
    private val arrowsHeight = 180
    private val arrowsXPos = 110

    private val columns = 7
    private val rows = 4
    private val pageSize = columns * rows

    private var guiLeft = GuiProfileViewer.getGuiLeft()
    private var guiTop = GuiProfileViewer.getGuiTop()
    private val sackArrayLeft = 168
    private val sackArrayTop = 20
    private val sackGridXSize = 37
    private val sackGridYSize = 41
    private val itemIconSize = 20

    private val sackContents = mutableMapOf<String, SackInfo>()
    private val sackItems = mutableMapOf<String, SackItem>()
    private val playerRunes = mutableListOf<String>()

    private val sackPattern = "^RUNE_(?<name>\\w+)_(?<tier>\\d)\$".toPattern()

    override fun drawPage(mouseX: Int, mouseY: Int, partialTicks: Float) {
        guiLeft = GuiProfileViewer.getGuiLeft()
        guiTop = GuiProfileViewer.getGuiTop()

        MC.textureManager.bindTexture(pv_sacks)
        Utils.drawTexturedRect(
            guiLeft.toFloat(),
            guiTop.toFloat(),
            instance.sizeX.toFloat(),
            instance.sizeY.toFloat(),
            GL11.GL_NEAREST
        )

        val newProfile = selectedProfile
        if (newProfile == null) {
            Utils.drawStringCentered("§cMissing Profile Data", guiLeft + 250, guiTop + 101, true, 0)
            return
        }

        if (sacksJson == null) {
            Utils.drawStringCentered("§cMissing Repo Data", guiLeft + 250, guiTop + 101, true, 0)
            return
        }

        if (newProfile != currentProfile) {
            getData()
            currentProfile = selectedProfile
        }

        val currentSackData = sackContents[currentSack] ?: run {
            Utils.drawStringCentered("§cApi Info Missing", guiLeft + 250, guiTop + 101, true, 0)
            return
        }
        // after this point everything in the constants json exists and does not need to be checked again except "item"

        val name = if (currentSack == "All") "§2All Sacks" else "§2$currentSack Sack"
        Utils.renderShadowedString(name, (guiLeft + 78).toFloat(), (guiTop + 74).toFloat(), 105)

        Utils.renderAlignedString(
            "§6Value",
            "§f${StringUtils.formatNumber(currentSackData.sackValue.toLong())}",
            (guiLeft + 27).toFloat(),
            (guiTop + 91).toFloat(),
            102
        )

        Utils.renderAlignedString(
            "§2Items",
            "§f${StringUtils.formatNumber(currentSackData.itemCount)}",
            (guiLeft + 27).toFloat(),
            (guiTop + 108).toFloat(),
            102
        )

        GlStateManager.enableDepth()

        val sackTypes = sacksJson.getAsJsonObject("sacks")

        val startIndex = page * pageSize
        val endIndex = (page + 1) * pageSize
        if (currentSack == "All") {
            for ((index, entrySet) in sackTypes.entrySet().withIndex()) {
                val (sackName, sackData) = entrySet

                if (index < startIndex || index >= endIndex) continue
                val adjustedIndex = index - startIndex

                val xIndex = adjustedIndex % columns
                val yIndex = adjustedIndex / columns
                if (yIndex >= rows) continue

                val data = sackData.asJsonObject

                if (!data.has("item") || !data.get("item").isJsonPrimitive || !data.get("item").asJsonPrimitive.isString) continue
                val sackItemName = data.get("item").asString
                val itemStack = manager.createItem(sackItemName)

                val x = guiLeft + sackArrayLeft + xIndex * sackGridXSize
                val y = guiTop + sackArrayTop + yIndex * sackGridYSize

                MC.textureManager.bindTexture(GuiProfileViewer.pv_elements)
                Utils.drawTexturedRect(
                    (x).toFloat(),
                    (y).toFloat(),
                    20f,
                    20f,
                    0f,
                    20 / 256f,
                    0f,
                    20 / 256f,
                    GL11.GL_NEAREST
                )

                val sackInfo = sackContents[sackName] ?: SackInfo(0, 0.0)
                Utils.drawStringCentered(
                    "§6${StringUtils.shortNumberFormat(sackInfo.sackValue.roundToDecimals(0))}",
                    x + itemIconSize / 2,
                    y - 4,
                    true,
                    0
                )
                Utils.drawStringCentered(
                    "§7${StringUtils.shortNumberFormat(sackInfo.itemCount)}",
                    x + itemIconSize / 2,
                    y + 26,
                    true,
                    0
                )
                GlStateManager.color(0f, 0f, 0f, 0f)

                if (itemStack != null) {
                    Utils.drawItemStack(itemStack, x + 2, y + 2)

                    if (mouseX > x && mouseX < x + itemIconSize) {
                        if (mouseY > y && mouseY < y + itemIconSize) {
                            tooltipToDisplay = createTooltip(
                                "$sackName Sack",
                                sackInfo.sackValue,
                                sackInfo.itemCount,
                                true
                            )
                        }
                    }
                } else {
                    println("$sackItemName missing in neu repo")
                }
            }
        } else {
            val sackData = sackTypes.get(currentSack).asJsonObject

            val sackContents = sackData.getAsJsonArray("contents")
            var sackItemNames = sackContents.map { it.asString }.toList()
            if (currentSack == "Rune") {
                sackItemNames = playerRunes
            }

            for ((index, itemName) in sackItemNames.withIndex()) {
                if (index < startIndex || index >= endIndex) continue
                val adjustedIndex = index - startIndex

                val xIndex = adjustedIndex % columns
                val yIndex = adjustedIndex / columns
                if (yIndex >= rows) continue

                val itemStack = manager.createItem(itemName)

                val x = guiLeft + sackArrayLeft + xIndex * sackGridXSize
                val y = guiTop + sackArrayTop + yIndex * sackGridYSize

                MC.textureManager.bindTexture(GuiProfileViewer.pv_elements)
                Utils.drawTexturedRect(
                    (x).toFloat(),
                    (y).toFloat(),
                    20f,
                    20f,
                    0f,
                    20 / 256f,
                    0f,
                    20 / 256f,
                    GL11.GL_NEAREST
                )

                val itemInfo = sackItems[itemName] ?: SackItem(0, 0.0)
                Utils.drawStringCentered(
                    "§6${StringUtils.shortNumberFormat(itemInfo.value.roundToDecimals(0))}",
                    x + itemIconSize / 2,
                    y - 4,
                    true,
                    0
                )
                Utils.drawStringCentered("§7${StringUtils.shortNumberFormat(itemInfo.amount)}", x + 10, y + 26, true, 0)
                GlStateManager.color(1f, 1f, 1f, 1f)

                if (itemStack != null) {
                    val stackName = itemStack.displayName
                    Utils.drawItemStack(itemStack, x + 2, y + 2)

                    if (mouseX > x && mouseX < x + itemIconSize) {
                        if (mouseY > y && mouseY < y + itemIconSize) {
                            tooltipToDisplay = createTooltip(stackName, itemInfo.value, itemInfo.amount, false)
                        }
                    }
                } else {
                    println("$itemName missing in neu repo")
                }
            }
            val buttonRect = Rectangle(guiLeft + sackArrayLeft, guiTop + arrowsHeight, 80, 15)
            RenderUtils.drawFloatingRectWithAlpha(
                buttonRect.x,
                buttonRect.y,
                buttonRect.width,
                buttonRect.height,
                100,
                true
            )
            Utils.renderShadowedString("§2Back", (guiLeft + sackArrayLeft + 40).toFloat(), (guiTop + arrowsHeight + 3).toFloat(), 79)

            if (Mouse.getEventButtonState() && Utils.isWithinRect(mouseX, mouseY, buttonRect)) {
                currentSack = "All"
                Utils.playPressSound()
                page = 0
                maxPage = getPages(currentSack, sackTypes)
            }
        }

        GlStateManager.color(1f, 1f, 1f, 1f)
        ArrowPagesUtils.onDraw(guiLeft, guiTop, intArrayOf(sackArrayLeft + arrowsXPos, arrowsHeight), page, maxPage + 1)

        if (tooltipToDisplay.isNotEmpty()) {
            tooltipToDisplay = tooltipToDisplay.map { "§7$it" }
            Utils.drawHoveringText(tooltipToDisplay, mouseX, mouseY, instance.width, instance.height, -1)
            tooltipToDisplay = listOf()
        }
    }

    fun mouseClick(mouseX: Int, mouseY: Int, mouseButton: Int): Boolean {
        super.mouseClicked(mouseX, mouseY, mouseButton)

        if (sacksJson == null || sackContents.isEmpty()) {
            return false
        }
        // after this point everything in the constants json exists and does not need to be checked again

        if (currentSack == "All") {
            val sackTypes = sacksJson.getAsJsonObject("sacks")

            val startIndex = page * pageSize
            val endIndex = (page + 1) * pageSize

            for ((index, sackData) in sackTypes.entrySet().withIndex()) {
                val sackName = sackData.key

                if (index < startIndex || index >= endIndex) continue
                val adjustedIndex = index - startIndex

                val xIndex = adjustedIndex % columns
                val yIndex = adjustedIndex / columns
                if (yIndex >= rows) continue

                val x = guiLeft + sackArrayLeft + xIndex * sackGridXSize
                val y = guiTop + sackArrayTop + yIndex * sackGridYSize

                if (mouseX > x && mouseX < x + itemIconSize) {
                    if (mouseY > y && mouseY < y + itemIconSize) {
                        currentSack = sackName
                        Utils.playPressSound()
                        page = 0
                        maxPage = getPages(currentSack, sackTypes)
                        return true
                    }
                }
            }
        }

        ArrowPagesUtils.onPageSwitchMouse(
            guiLeft,
            guiTop,
            intArrayOf(sackArrayLeft + arrowsXPos, arrowsHeight),
            page,
            maxPage + 1
        ) { pageChange -> page = pageChange }

        return false
    }

    private fun createTooltip(name: String, value: Double, amount: Int, isSack: Boolean): List<String> {
        val baseList = mutableListOf(
            "§2$name",
            "Items Stored: §a${StringUtils.formatNumber(amount)}",
            "Total Value: §6${StringUtils.formatNumber(value.toLong())}"
        )
        if (isSack) baseList.add("§eClick for more details")
        return baseList
    }

    private fun getPages(pageName: String, sackTypes: JsonObject): Int {
        return when (pageName) {
            "All" -> {
                sackTypes.entrySet().size / pageSize
            }

            "Rune" -> {
                playerRunes.size / pageSize
            }

            else -> {
                val sackData = sackTypes.get(currentSack).asJsonObject
                val sackContents = sackData.getAsJsonArray("contents")
                sackContents.size() / pageSize
            }
        }
    }

    private fun getData() {
        sackContents.clear()
        sackItems.clear()
        playerRunes.clear()

        if (!sacksJson.has("sacks") || !sacksJson.get("sacks").isJsonObject) return
        val sackTypes = sacksJson.getAsJsonObject("sacks")
        val selectedProfile = selectedProfile?.profileJson ?: return

        val sacksInfo = Utils.getElementOrDefault(selectedProfile, "inventory.sacks_counts", JsonObject()).asJsonObject

        var totalValue = 0.0
        var totalItems = 0

        for ((sackName, sackData) in sackTypes.entrySet()) {
            if (!sackData.isJsonObject) return
            val data = sackData.asJsonObject
            var sackValue = 0.0
            var sackItemCount = 0

            if (sackName == "Rune") {
                totalItems += getRuneData(sacksInfo)
                continue
            }

            if (!data.has("contents") || !data.get("contents").isJsonArray) return
            val contents = data.getAsJsonArray("contents")
            for (item in contents) {
                if (!item.isJsonPrimitive || !item.asJsonPrimitive.isString) return
                val sackItem = item.asString

                val adjustedName = sackItem.replace("-", ":")
                val itemCount = sacksInfo.getIntOrValue(adjustedName, 0)
                val itemValue = itemCount * getPrice(sackItem)

                if (sackItem !in sackItems) {
                    totalValue += itemValue
                    totalItems += itemCount
                }

                sackItems[sackItem] = SackItem(itemCount, itemValue)
                sackValue += itemValue
                sackItemCount += itemCount
            }
            sackContents[sackName] = SackInfo(sackItemCount, sackValue)
        }

        for ((itemName, _) in sacksInfo.entrySet()) {
            val adjustedName = itemName.replace(":", "-")
            if (adjustedName.contains(Regex("(RUNE|PERFECT_|MUSHROOM_COLLECTION)"))) continue
            if (adjustedName in sackItems) continue
            println("$adjustedName missing from repo sacks file!")
        }

        sackContents["All"] = SackInfo(totalItems, totalValue)
    }

    private fun getPrice(itemName: String): Double {
        val npcPrice = HypixelItemAPI.getNPCSellPrice(itemName) ?: 0.0
        val bazaarInfo = manager.auctionManager.getBazaarInfo(itemName) ?: return npcPrice
        val buyPrice = bazaarInfo.getDoubleOrValue("curr_buy", 0.0)
        val sellPrice = bazaarInfo.getDoubleOrValue("curr_sell", 0.0)
        return maxOf(npcPrice, buyPrice, sellPrice)
    }

    private fun getRuneData(sacksInfo: JsonObject): Int {
        var sackItemCount = 0
        for ((itemName, amount) in sacksInfo.entrySet()) {
            if (!amount.isJsonPrimitive || !amount.asJsonPrimitive.isNumber) continue
            sackPattern.matchMatcher(itemName) {
                val itemAmount = amount.asInt
                val name = group("name")
                val tier = group("tier")
                val neuInternalName = "${name}_RUNE;$tier"
                sackItemCount += itemAmount
                sackItems[neuInternalName] = SackItem(itemAmount, 1.0 * itemAmount)
                playerRunes.add(neuInternalName)
            }
        }
        sackContents["Rune"] = SackInfo(sackItemCount, 1.0 * sackItemCount)
        return sackItemCount
    }

    data class SackInfo(val itemCount: Int, val sackValue: Double)
    data class SackItem(val amount: Int, val value: Double)
}
