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

package io.github.moulberry.notenoughupdates.profileviewer

import com.google.gson.JsonObject
import io.github.moulberry.notenoughupdates.NotEnoughUpdates
import io.github.moulberry.notenoughupdates.core.util.StringUtils
import io.github.moulberry.notenoughupdates.core.util.render.RenderUtils
import io.github.moulberry.notenoughupdates.util.*
import io.github.moulberry.notenoughupdates.util.hypixelapi.HypixelItemAPI
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.ResourceLocation
import org.lwjgl.input.Mouse
import org.lwjgl.opengl.GL11
import java.util.regex.Matcher
import java.util.regex.Pattern


class SacksPage(pvInstance: GuiProfileViewer) : GuiProfileViewerPage(pvInstance) {
    private val manager get() = NotEnoughUpdates.INSTANCE.manager
    private val pv_sacks = ResourceLocation("notenoughupdates:pv_sacks.png")
    private var sacksJson = Constants.SACKS
    private var tooltipToDisplay = listOf<String>()
    private var currentProfile: SkyblockProfiles.SkyblockProfile? = null

    private var currentSack = "All"

    private var page = 0
    private var maxPage = 0
    private val arrowsHeight = 162

    private val columns = 7
    private val rows = 4
    private val pageSize = columns * rows

    private var guiLeft = GuiProfileViewer.getGuiLeft()
    private var guiTop = GuiProfileViewer.getGuiTop()

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

        var xIndex = 0
        var yIndex = 0

        val sackTypes = sacksJson.getAsJsonObject("sacks")

        val startIndex = page * pageSize
        val endIndex = (page + 1) * pageSize
        var itemCount = -1
        if (currentSack == "All") {
            for ((sackName, sackData) in sackTypes.entrySet()) {
                itemCount++
                if (itemCount < startIndex || itemCount >= endIndex) continue

                if (yIndex == rows) continue
                val data = sackData.asJsonObject

                if (!data.has("item") || !data.get("item").isJsonPrimitive || !data.get("item").asJsonPrimitive.isString) continue
                val sackItemName = data.get("item").asString
                val itemStack = manager.createItem(sackItemName)

                val x = guiLeft + 168 + xIndex * 37
                val y = guiTop + 20 + yIndex * 41

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
                    x + 10,
                    y - 4,
                    true,
                    0
                )
                Utils.drawStringCentered(
                    "§2${StringUtils.shortNumberFormat(sackInfo.itemCount)}",
                    x + 10,
                    y + 26,
                    true,
                    0
                )
                GlStateManager.color(1f, 1f, 1f, 1f)

                if (itemStack != null) {
                    Utils.drawItemStack(itemStack, x + 2, y + 2)

                    if (mouseX > x && mouseX < x + 20) {
                        if (mouseY > y && mouseY < y + 20) {
                            tooltipToDisplay = createTooltip("$sackName Sack", sackInfo.sackValue, sackInfo.itemCount)
                        }
                    }
                } else {
                    println("$sackItemName missing in neu repo")
                }

                xIndex++
                if (xIndex == columns) {
                    xIndex = 0
                    yIndex++
                }
            }
        } else {
            val sackData = sackTypes.get(currentSack).asJsonObject

            val sackContents = sackData.getAsJsonArray("contents")
            var sackItemNames = sackContents.map { it.asString }.toList()
            if (currentSack == "Rune") {
                sackItemNames = playerRunes
            }

            for (itemName in sackItemNames) {
                itemCount++
                if (itemCount < startIndex || itemCount >= endIndex) continue

                if (yIndex == rows) continue
                val itemStack = manager.createItem(itemName)

                val x = guiLeft + 168 + xIndex * 37
                val y = guiTop + 20 + yIndex * 41

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
                    x + 10,
                    y - 4,
                    true,
                    0
                )
                Utils.drawStringCentered("§2${StringUtils.shortNumberFormat(itemInfo.amount)}", x + 10, y + 26, true, 0)
                GlStateManager.color(1f, 1f, 1f, 1f)

                if (itemStack != null) {
                    val stackName = itemStack.displayName
                    Utils.drawItemStack(itemStack, x + 2, y + 2)

                    if (mouseX > x && mouseX < x + 20) {
                        if (mouseY > y && mouseY < y + 20) {
                            tooltipToDisplay = createTooltip(stackName, itemInfo.value, itemInfo.amount)
                        }
                    }
                } else {
                    println("$itemName missing in neu repo")
                }

                xIndex++
                if (xIndex == columns) {
                    xIndex = 0
                    yIndex++
                }
            }
            val buttonRect = Rectangle(guiLeft + 250, guiTop + 180, 80, 15)
            RenderUtils.drawFloatingRectWithAlpha(
                buttonRect.x,
                buttonRect.y,
                buttonRect.width,
                buttonRect.height,
                100,
                true
            )
            Utils.renderShadowedString("§2Back", (guiLeft + 290).toFloat(), (guiTop + 183).toFloat(), 79)

            if (Mouse.getEventButtonState() && Utils.isWithinRect(mouseX, mouseY, buttonRect)) {
                currentSack = "All"
                Utils.playPressSound()
                page = 0
                maxPage = getPages(currentSack, sackTypes)
            }
        }

        GlStateManager.color(1f, 1f, 1f, 1f)
        MC.textureManager.bindTexture(GuiProfileViewer.resource_packs)

        if (page > 0) {
            Utils.drawTexturedRect(
                (guiLeft + 290 - 20).toFloat(),
                (guiTop + arrowsHeight).toFloat(),
                12f,
                16f,
                29 / 256f,
                53 / 256f,
                0f,
                32 / 256f,
                GL11.GL_NEAREST
            )
        }
        if (page < maxPage) {
            Utils.drawTexturedRect(
                (guiLeft + 290 + 8).toFloat(),
                (guiTop + arrowsHeight).toFloat(),
                12f,
                16f,
                5 / 256f,
                29 / 256f,
                0f,
                32 / 256f,
                GL11.GL_NEAREST
            )
        }


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
            var xIndex = 0
            var yIndex = 0

            val startIndex = page * pageSize
            val endIndex = (page + 1) * pageSize
            var itemCount = -1

            for ((sackName, _) in sackTypes.entrySet()) {
                itemCount++
                if (itemCount < startIndex || itemCount >= endIndex) continue

                if (yIndex == rows) continue
                val x = guiLeft + 168 + xIndex * 37
                val y = guiTop + 20 + yIndex * 41

                if (mouseX > x && mouseX < x + 20) {
                    if (mouseY > y && mouseY < y + 20) {
                        currentSack = sackName
                        Utils.playPressSound()
                        page = 0
                        maxPage = getPages(currentSack, sackTypes)
                        return true
                    }
                }

                xIndex++
                if (xIndex == columns) {
                    xIndex = 0
                    yIndex++
                }
            }
        }

        if (Mouse.isButtonDown(0)) {
            if (mouseY > guiTop + arrowsHeight && mouseY < guiTop + arrowsHeight + 16) {
                if (mouseX > guiLeft + 290 - 20 && mouseX < guiLeft + 290 + 20) {
                    if (mouseX > guiLeft + 290) {
                        if (page < maxPage) {
                            page++
                            Utils.playPressSound()
                            return true
                        }
                    } else {
                        if (page > 0) {
                            page--
                            Utils.playPressSound()
                            return true
                        }
                    }
                }
            }
        }

        return false
    }

    private fun createTooltip(name: String, value: Double, amount: Int): List<String> {
        return listOf(
            "§2$name",
            "Items Stored: ${StringUtils.formatNumber(amount)}",
            "Total Value: ${StringUtils.formatNumber(value.toLong())}"
        )
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

        if (!selectedProfile.has("sacks_counts") || !selectedProfile.get("sacks_counts").isJsonObject) return
        val sacksInfo = selectedProfile.get("sacks_counts").asJsonObject

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
                val itemCount = sacksInfo.getInt(adjustedName)
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
        val buyPrice = bazaarInfo.getDouble("curr_buy")
        val sellPrice = bazaarInfo.getDouble("curr_sell")
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
                sackItems[neuInternalName] = SackItem(itemAmount, 0.0)
                playerRunes.add(neuInternalName)
            }
        }
        sackContents["Rune"] = SackInfo(sackItemCount, 0.0)
        return sackItemCount
    }

    private fun JsonObject.getInt(key: String): Int {
        return if (has(key) && get(key).isJsonPrimitive && get(key).asJsonPrimitive.isNumber) {
            get(key).asInt
        } else 0
    }

    private fun JsonObject.getDouble(key: String): Double {
        return if (has(key) && get(key).isJsonPrimitive && get(key).asJsonPrimitive.isNumber) {
            get(key).asDouble
        } else 0.0
    }

    private inline fun <T> Pattern.matchMatcher(text: String, consumer: Matcher.() -> T) =
        matcher(text).let { if (it.matches()) consumer(it) else null }

    data class SackInfo(val itemCount: Int, val sackValue: Double)
    data class SackItem(val amount: Int, val value: Double)
}
