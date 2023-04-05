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

package io.github.moulberry.notenoughupdates.miscgui.pricegraph

import io.github.moulberry.notenoughupdates.NotEnoughUpdates
import io.github.moulberry.notenoughupdates.util.SpecialColour
import io.github.moulberry.notenoughupdates.util.Utils
import io.github.moulberry.notenoughupdates.util.roundToDecimals
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumChatFormatting
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalUnit
import java.util.*
import java.util.concurrent.CompletableFuture
import kotlin.math.abs


private const val X_SIZE = 364
private const val Y_SIZE = 215
private val dateFormat = SimpleDateFormat("'§b'd MMMMM yyyy '§eat§b' HH:mm")
private val numberFormat = NumberFormat.getInstance()

class GuiPriceGraph(itemId: String) : GuiScreen() {
    private val TEXTURE: ResourceLocation = when (NotEnoughUpdates.INSTANCE.config.ahGraph.graphStyle) {
        1 -> ResourceLocation("notenoughupdates:price_graph_gui/price_information_gui_dark.png")
        2 -> ResourceLocation("notenoughupdates:price_graph_gui/price_information_gui_phqdark.png")
        3 -> ResourceLocation("notenoughupdates:price_graph_gui/price_information_gui_fsr.png")
        else -> ResourceLocation("notenoughupdates:price_graph_gui/price_information_gui.png")
    }
    private val dataProvider: DataProvider = ServerDataProvider
    private val rawData: CompletableFuture<Map<Instant, PriceObject>?> = dataProvider.loadData(itemId)
    private var data: Map<Instant, PriceObject> = mapOf()
    private var processedData = false
    private var firstTime: Instant = Instant.now()
    private var lastTime: Instant = Instant.now()
    private var lowestPrice: Double = 0.0
    private var highestPrice: Double = 1.0
    private var guiLeft = 0
    private var guiTop = 0

    /**
     * 0 = hour
     * 1 = day
     * 2 = week
     * 3 = all
     * 4 = custom
     */
    private var mode = NotEnoughUpdates.INSTANCE.config.ahGraph.defaultMode

    private var itemName: String? = null
    private var itemStack: ItemStack? = null

    private var customSelecting = false
    private var customSelectionStart = 0
    private var customSelectionEnd = 0

    init {
        if (NotEnoughUpdates.INSTANCE.manager.itemInformation.containsKey(itemId)) {
            val itemInfo = NotEnoughUpdates.INSTANCE.manager.itemInformation[itemId]
            itemName = itemInfo!!["displayname"].asString
            itemStack = NotEnoughUpdates.INSTANCE.manager.jsonToStack(itemInfo)
        }
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        guiLeft = (width - X_SIZE) / 2
        guiTop = (height - Y_SIZE) / 2

        if (customSelecting) customSelectionEnd =
            if (mouseX < guiLeft + 17) guiLeft + 17 else mouseX.coerceAtMost(guiLeft + 315)

        Minecraft.getMinecraft().textureManager.bindTexture(TEXTURE)
        GlStateManager.color(1f, 1f, 1f, 1f)
        Utils.drawTexturedRect( // Draw main background
            guiLeft.toFloat(), guiTop.toFloat(), X_SIZE.toFloat(), Y_SIZE.toFloat(),
            0f, X_SIZE / 512f, 0f, Y_SIZE / 512f, GL11.GL_NEAREST
        )
        for (i in 0..3) Utils.drawTexturedRect( // Draw buttons
            (guiLeft + 245 + 18 * i).toFloat(), (guiTop + 17).toFloat(), 16f, 16f,
            (0f + 16f * i) / 512f, (16 + 16f * i) / 512f,
            (if (mode == i) 215 else 231) / 512f,
            (if (mode == i) 231 else 247) / 512f, GL11.GL_NEAREST
        )

        if (itemName != null && itemStack != null) { // Draw item name and icon
            Utils.drawItemStack(itemStack, guiLeft + 16, guiTop + 11)
            Utils.drawStringScaledMax(
                itemName, (guiLeft + 35).toFloat(), (guiTop + 13).toFloat(), false,
                0xffffff, 1.77f, 208
            )
        }

        if (!rawData.isDone) {
            Utils.drawStringCentered( // Loading text
                "Loading...",
                (guiLeft + 166).toFloat(), (guiTop + 116).toFloat(),
                false, -0x100
            )
        } else if (rawData.get() == null || rawData.get()!!.size < 2 || data.isEmpty() && processedData) {
            Utils.drawStringCentered( // Error text
                "No data found.",
                (guiLeft + 166).toFloat(),
                (guiTop + 116).toFloat(),
                false,
                -0x10000
            )
        } else if (data.isEmpty()) {
            processData()
        } else {
            // Draw Graph
            val buyColor = SpecialColour.specialToChromaRGB(NotEnoughUpdates.INSTANCE.config.ahGraph.graphColor)
            val sellColor = SpecialColour.specialToChromaRGB(NotEnoughUpdates.INSTANCE.config.ahGraph.graphColor2)

            var prevX: Double? = null
            var prevY: Double? = null
            val sellPoints = mutableMapOf<Double, Double>()
            if (data.values.first().sellPrice != null) {
                drawGradient(sellColor)
                for (point in data) {
                    if (point.value.sellPrice == null) continue
                    val x = getX(point.key)
                    val y = getY(point.value.sellPrice!!)
                    sellPoints[x] = y

                    if (prevX != null && prevY != null) drawCoveringQuad(x, y, prevX, prevY)

                    prevX = x
                    prevY = y
                }
            }

            drawGradient(buyColor)

            prevX = null
            prevY = null
            var prevSellY: Double? = null

            var closestPoint = data.entries.first()
            var closestDistance = Double.MAX_VALUE

            for (point in data) {
                val x = getX(point.key)
                val y = getY(point.value.buyPrice)

                if (prevX != null && prevY != null) {
                    drawCoveringQuad(x, y, prevX, prevY)
                    Utils.drawLine(
                        prevX.toFloat(), prevY.toFloat() + 0.5f,
                        x.toFloat(), y.toFloat() + 0.5f,
                        2, buyColor
                    )
                }
                if (sellPoints.isNotEmpty()) {
                    val sellY = sellPoints[x]
                    if (sellY != null && prevSellY != null && prevX != null) Utils.drawLine(
                        prevX.toFloat(), prevSellY.toFloat() + 0.5f,
                        x.toFloat(), sellY.toFloat() + 0.5f,
                        2, sellColor
                    )
                    if (sellY != null) prevSellY = sellY
                }

                val distance = abs(mouseX - x)
                if (closestDistance > distance) {
                    closestPoint = point
                    closestDistance = distance
                }

                prevX = x
                prevY = y
            }

            // Draw axis
            for (i in 0..6) { // Y-axis with price
                val price = map(i.toDouble(), 0.0, 6.0, highestPrice, lowestPrice).toLong()
                Utils.drawStringF(
                    formatPrice(price), guiLeft + 320f,
                    map(i.toDouble(), 0.0, 6.0, guiTop + 35.0, guiTop + 198.0).toFloat()
                            - Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT / 2f, false, 0x8b8b8b
                )
            }
            // X-axis with hour or date
            val showDays = lastTime.epochSecond - firstTime.epochSecond > 86400
            val amountOfTime = (lastTime.epochSecond - firstTime.epochSecond) / (if (showDays) 86400.0 else 3600.0)
            val pixelsPerTime = 298.0 / amountOfTime
            var time = firstTime.plusSeconds(
                if (showDays) (24 - Date.from(firstTime).hours) * 3600L
                else (60 - Date.from(firstTime).minutes) * 60L
            )
            var xPos = getX(time)
            var lastX = -100.0
            while (xPos < guiLeft + 315) {
                if (abs(xPos - lastX) > 30) {
                    Utils.drawStringCentered(
                        Date.from(time).let { if (showDays) it.date else it.hours }.toString(),
                        xPos.toFloat(), (guiTop + 206).toFloat(),
                        false, 0x8b8b8b
                    )
                    lastX = xPos
                }
                time = time.plusSeconds(if (showDays) 86400L else 3600L)
                xPos += pixelsPerTime
            }

            if (
                mouseX >= guiLeft + 17 && mouseX <= guiLeft + 315 &&
                mouseY >= guiTop + 35 && mouseY <= guiTop + 198 && !customSelecting
            ) {
                // Draw tooltip with price info
                val text = ArrayList<String>()
                text.add(dateFormat.format(Date.from(closestPoint.key)))
                if (closestPoint.value.sellPrice == null) {
                    text.add(
                        "${EnumChatFormatting.YELLOW}${EnumChatFormatting.BOLD}Lowest BIN: ${EnumChatFormatting.GOLD}" +
                                "${EnumChatFormatting.BOLD}${numberFormat.format(closestPoint.value.buyPrice)}"
                    )
                } else {
                    text.add(
                        "${EnumChatFormatting.YELLOW}${EnumChatFormatting.BOLD}Bazaar Insta-Buy: ${EnumChatFormatting.GOLD}" +
                                "${EnumChatFormatting.BOLD}${numberFormat.format(closestPoint.value.buyPrice)}"
                    )
                    text.add(
                        "${EnumChatFormatting.YELLOW}${EnumChatFormatting.BOLD}Bazaar Insta-Sell: ${EnumChatFormatting.GOLD}" +
                                "${EnumChatFormatting.BOLD}${numberFormat.format(closestPoint.value.sellPrice)}"
                    )
                }
                val x = getX(closestPoint.key)
                val y = getY(closestPoint.value.buyPrice)
                Utils.drawLine(
                    x.toFloat(), (guiTop + 35).toFloat(),
                    x.toFloat(), (guiTop + 198).toFloat(),
                    2, 0x4D8b8b8b
                )
                Minecraft.getMinecraft().textureManager.bindTexture(TEXTURE)
                GlStateManager.color(1f, 1f, 1f, 1f)
                Utils.drawTexturedRect(
                    x.toFloat() - 2.5f, y.toFloat() - 2.5f, 5f, 5f,
                    0f, 5 / 512f, 247 / 512f, 252 / 512f, GL11.GL_NEAREST
                )
                if (closestPoint.value.sellPrice != null) Utils.drawTexturedRect(
                    x.toFloat() - 2.5f, getY(closestPoint.value.sellPrice!!).toFloat() - 2.5f, 5f, 5f,
                    0f, 5 / 512f, 247 / 512f, 252 / 512f, GL11.GL_NEAREST
                )

                drawHoveringText(text, x.toInt(), y.toInt())
            }

            if (customSelecting) {
                Utils.drawDottedLine(
                    customSelectionStart.toFloat(), guiTop + 36f,
                    customSelectionStart.toFloat(), guiTop + 197f,
                    2, 10, -0x39393a
                )
                Utils.drawDottedLine(
                    customSelectionEnd.toFloat(), guiTop + 36f,
                    customSelectionEnd.toFloat(), guiTop + 197f,
                    2, 10, -0x39393a
                )
                Utils.drawDottedLine(
                    customSelectionStart.toFloat(), guiTop + 36f,
                    customSelectionEnd.toFloat(), guiTop + 36f,
                    2, 10, -0x39393a
                )
                Utils.drawDottedLine(
                    customSelectionStart.toFloat(), guiTop + 197f,
                    customSelectionEnd.toFloat(), guiTop + 197f,
                    2, 10, -0x39393a
                )
            }
        }

        // Draw item tooltips
        if (mouseY >= guiTop + 17 && mouseY <= guiTop + 35 && mouseX >= guiLeft + 244 && mouseX <= guiLeft + 316) {
            val index = (mouseX - guiLeft - 245) / 18
            drawRect(
                guiLeft + 245 + 18 * index,
                guiTop + 17, guiLeft + 261 + 18 * index,
                guiTop + 33, -0x7f000001
            )
            drawHoveringText(
                listOf(
                    when (index) {
                        0 -> "Show 1 Hour"
                        1 -> "Show 1 Day"
                        2 -> "Show 1 Week"
                        else -> "Show All"
                    }
                ), mouseX, mouseY
            )
        }
    }

    private fun getX(time: Instant) = map(
        time.epochSecond.toDouble(),
        firstTime.epochSecond.toDouble(),
        lastTime.epochSecond.toDouble(),
        guiLeft + 17.0,
        guiLeft + 315.0
    )

    private fun getY(price: Double) = map(
        price,
        highestPrice + 1,
        lowestPrice - 1,
        guiTop + 45.0,
        guiTop + 188.0
    )


    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        super.mouseClicked(mouseX, mouseY, mouseButton)
        if (mouseY >= guiTop + 17 && mouseY <= guiTop + 35 && mouseX >= guiLeft + 244 && mouseX <= guiLeft + 316) {
            selectMode((mouseX - guiLeft - 245) / 18)
            Utils.playPressSound()
        } else if (mouseY >= guiTop + 35 && mouseY <= guiTop + 198 && mouseX >= guiLeft + 17 && mouseX <= guiLeft + 315) {
            customSelecting = true
            customSelectionStart = mouseX
            customSelectionEnd = mouseX
        }
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        super.mouseReleased(mouseX, mouseY, state)
        if (customSelecting) {
            customSelecting = false
            customSelectionEnd =
                if (mouseX < guiLeft + 17) guiLeft + 17 else mouseX.coerceAtMost(guiLeft + 315)
            if (customSelectionStart > customSelectionEnd) {
                val temp = customSelectionStart
                customSelectionStart = customSelectionEnd
                customSelectionEnd = temp
            }
            if (customSelectionStart - customSelectionEnd == 0) return
            selectMode(4)
        }
    }

    private fun processData() {
        processedData = true
        // Filter based on time
        val now = Instant.now()
        val startTime = when (mode) {
            0 -> now.minus(Duration.ofHours(1))
            1 -> now.minus(Duration.ofDays(1))
            2 -> now.minus(Duration.ofDays(7))
            3 -> null
            4 -> Instant.ofEpochSecond(
                map(
                    customSelectionStart.toDouble(),
                    guiLeft + 17.0,
                    guiLeft + 315.0,
                    firstTime.epochSecond.toDouble(),
                    lastTime.epochSecond.toDouble()
                ).toLong()
            )

            else -> error("$mode is not a valid mode!")
        }
        val endTime = when (mode) {
            4 -> Instant.ofEpochSecond(
                map(
                    customSelectionEnd.toDouble(),
                    guiLeft + 17.0,
                    guiLeft + 315.0,
                    firstTime.epochSecond.toDouble(),
                    lastTime.epochSecond.toDouble()
                ).toLong()
            )

            else -> null
        }
        val cutData = rawData.get()!!.filter {
            (startTime == null || it.key >= startTime) && (endTime == null || it.key <= endTime)
        }
        if (cutData.isEmpty()) return

        // Smooth data
        val zones = NotEnoughUpdates.INSTANCE.config.ahGraph.graphZones
        val first = cutData.minOf { it.key }
        val last = cutData.maxOf { it.key }
        val trimmedData = mutableMapOf<Instant, PriceObject>()
        for (i in 0..zones) {
            val zoneStart = Instant.ofEpochSecond(
                map(
                    i.toDouble(), 0.0, zones.toDouble(),
                    first.epochSecond.toDouble(), last.epochSecond.toDouble()
                ).toLong()
            )
            val zoneEnd = Instant.ofEpochSecond(
                map(
                    i + 1.0, 0.0, zones.toDouble(),
                    first.epochSecond.toDouble(), last.epochSecond.toDouble()
                ).toLong()
            )
            val dataInZone = cutData.filter { it.key >= zoneStart && it.key < zoneEnd }
            if (dataInZone.isEmpty()) {
                continue
            } else {
                val averageTime = Instant.ofEpochSecond(dataInZone.keys.sumOf { it.epochSecond } / dataInZone.size)
                val averageBuyPrice = (dataInZone.values.sumOf { it.buyPrice } / dataInZone.size).roundToDecimals(1)
                val averageSellPrice = if (dataInZone.values.first().sellPrice == null) null
                else (dataInZone.values.sumOf { it.sellPrice ?: 0.0 } / dataInZone.size).roundToDecimals(1)
                trimmedData[averageTime] = PriceObject(averageBuyPrice, averageSellPrice)
            }
        }
        data = trimmedData
        if (data.isEmpty()) return

        // Populate variables required for graphs
        firstTime = data.minOf { it.key }
        lastTime = data.maxOf { it.key }
        lowestPrice = data.minOf {
            if (it.value.sellPrice != null) it.value.buyPrice.coerceAtMost(it.value.sellPrice!!)
            else it.value.buyPrice
        }
        highestPrice = data.maxOf {
            if (it.value.sellPrice != null) it.value.buyPrice.coerceAtLeast(it.value.sellPrice!!)
            else it.value.buyPrice
        }
    }

    private fun selectMode(mode: Int) {
        this.mode = mode
        data = mapOf()
        processedData = false
    }

    private fun drawGradient(color: Int) {
        Utils.drawGradientRect(
            0,
            guiLeft + 17,
            guiTop + 35,
            guiLeft + 315,
            guiTop + 198,
            changeAlpha(color, 120),
            changeAlpha(color, 10)
        )
    }

    private fun drawCoveringQuad(x: Double, y: Double, prevX: Double, prevY: Double) {
        Minecraft.getMinecraft().textureManager.bindTexture(TEXTURE)
        GlStateManager.color(1f, 1f, 1f, 1f)
        Utils.drawTexturedQuad(
            prevX.toFloat(), prevY.toFloat(),
            x.toFloat(), y.toFloat(),
            x.toFloat(), guiTop + 35f,
            prevX.toFloat(), guiTop + 35f,
            18 / 512f, 19 / 512f,
            36 / 512f, 37 / 512f,
            GL11.GL_NEAREST
        )
    }

    private fun formatPrice(price: Long): String {
        val df = DecimalFormat("#.00")
        if (price >= 1000000000) {
            return df.format((price / 1000000000f).toDouble()) + "B"
        } else if (price >= 1000000) {
            return df.format((price / 1000000f).toDouble()) + "M"
        } else if (price >= 1000) {
            return df.format((price / 1000f).toDouble()) + "K"
        }
        return price.toString()
    }

    private fun changeAlpha(origColor: Int, alpha: Int): Int {
        val color = origColor and 0x00ffffff //drop the previous alpha value
        return alpha shl 24 or color //add the one the user inputted
    }

    private fun map(x: Double, inMin: Double, inMax: Double, outMin: Double, outMax: Double): Double {
        return (x - inMin) * (outMax - outMin) / (inMax - inMin) + outMin
    }
}
