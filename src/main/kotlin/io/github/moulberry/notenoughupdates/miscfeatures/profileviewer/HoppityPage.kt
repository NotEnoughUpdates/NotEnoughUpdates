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

package io.github.moulberry.notenoughupdates.miscfeatures.profileviewer

import com.google.gson.JsonObject
import io.github.moulberry.notenoughupdates.NotEnoughUpdates
import io.github.moulberry.notenoughupdates.core.util.StringUtils
import io.github.moulberry.notenoughupdates.profileviewer.GuiProfileViewer
import io.github.moulberry.notenoughupdates.profileviewer.GuiProfileViewerPage
import io.github.moulberry.notenoughupdates.profileviewer.SkyblockProfiles
import io.github.moulberry.notenoughupdates.profileviewer.data.APIDataJson
import io.github.moulberry.notenoughupdates.util.Constants
import io.github.moulberry.notenoughupdates.util.MC
import io.github.moulberry.notenoughupdates.util.Utils
import io.github.moulberry.notenoughupdates.util.roundToDecimals
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11

class HoppityPage(pvInstance: GuiProfileViewer) : GuiProfileViewerPage(pvInstance) {
    private val manager get() = NotEnoughUpdates.INSTANCE.manager
    private val pv_hoppity = ResourceLocation("notenoughupdates:pv_hoppity.png")
    private var hoppityJson = Constants.HOPPITY

    private var guiLeft = GuiProfileViewer.getGuiLeft()
    private var guiTop = GuiProfileViewer.getGuiTop()

    private var currentProfile: SkyblockProfiles.SkyblockProfile? = null

    private val rabbitFamilyInfo = mutableListOf<UpgradeInfo>()
    private val factoryModifiersInfo = mutableListOf<UpgradeInfo>()
    private val otherModifiersInfo = mutableListOf<UpgradeInfo>()

    private var currentChocolate = 0L
    private var prestigeChocolate = 0L
    private var allTimeChocolate = 0L
    private var prestigeLevel = 0
    private var barnCapacity = 20

    // assuming cookie buff as Hypixel won't provide data for this
    private val baseMultiplier = 1.25
    private var rawChocolatePerSecond = 0
    private var multiplier = 0.0
    private var chocolatePerSecond = 0.0
    private var talisman: String? = null
    private var talismanChocolate = 0

    private val rabbitToRarity = mutableMapOf<String, String>()

    private var tooltipToDisplay = listOf<String>()

    override fun drawPage(mouseX: Int, mouseY: Int, partialTicks: Float) {
        guiLeft = GuiProfileViewer.getGuiLeft()
        guiTop = GuiProfileViewer.getGuiTop()

        if (selectedProfile?.APIDataJson?.events?.easter == null) {
            Utils.drawStringCentered("§cMissing Profile Data", guiLeft + 220, guiTop + 101, true, 0)
            return
        }

        if (hoppityJson == null) {
            Utils.drawStringCentered("§cMissing Repo Data", guiLeft + 220, guiTop + 101, true, 0)
            return
        }

        val newProfile = selectedProfile
        if (newProfile != currentProfile) {
            getData()
            currentProfile = selectedProfile
        }

        if (rabbitToRarity.isEmpty()) {
            Utils.drawStringCentered("§cMissing Repo Data", guiLeft + 220, guiTop + 101, true, 0)
            return
        }

        MC.textureManager.bindTexture(pv_hoppity)
        Utils.drawTexturedRect(
            guiLeft.toFloat(),
            guiTop.toFloat(),
            instance.sizeX.toFloat(),
            instance.sizeY.toFloat(),
            GL11.GL_NEAREST
        )

        Utils.renderShadowedString("§eRabbit Family", (guiLeft + 74).toFloat(), (guiTop + 14).toFloat(), 105)
        Utils.renderShadowedString("§eFactory Modifiers", (guiLeft + 74).toFloat(), (guiTop + 76).toFloat(), 105)
        Utils.renderShadowedString("§eOther", (guiLeft + 74).toFloat(), (guiTop + 138).toFloat(), 105)

        Utils.renderShadowedString("§eChocolate Factory", (guiLeft + 214).toFloat(), (guiTop + 14).toFloat(), 105)
        Utils.renderShadowedString("§eStats", (guiLeft + 214).toFloat(), (guiTop + 30).toFloat(), 105)

        Utils.renderShadowedString("§eRabbit Collection", (guiLeft + 356).toFloat(), (guiTop + 14).toFloat(), 105)

        GlStateManager.enableDepth()

        drawAlignedStringWithHover(
            "§eChocolate:",
            "§f${StringUtils.shortNumberFormat(currentChocolate.toDouble())}",
            guiLeft + 160,
            guiTop + 53,
            110,
            mouseX,
            mouseY,
            listOf("§eCurrent Chocolate: §f${StringUtils.formatNumber(currentChocolate)}")
        )
        drawAlignedStringWithHover(
            "§eChocolate Since Prestige:",
            "§f${StringUtils.shortNumberFormat(prestigeChocolate.toDouble())}",
            guiLeft + 160,
            guiTop + 68,
            110,
            mouseX,
            mouseY,
            listOf("§eChocolate Since Prestige: §f${StringUtils.formatNumber(prestigeChocolate)}")
        )
        drawAlignedStringWithHover(
            "§eAll Time:",
            "§f${StringUtils.shortNumberFormat(allTimeChocolate.toDouble())}",
            guiLeft + 160,
            guiTop + 83,
            110,
            mouseX,
            mouseY,
            listOf("§eAll Time Chocolate: §f${StringUtils.formatNumber(allTimeChocolate)}")
        )
        Utils.renderAlignedString(
            "§eFactory Level:",
            "§f$prestigeLevel",
            (guiLeft + 160).toFloat(),
            (guiTop + 98).toFloat(),
            110
        )
        Utils.renderAlignedString(
            "§eBarn Capacity:",
            "§f${RabbitCollectionRarity.TOTAL.uniques}/$barnCapacity",
            (guiLeft + 160).toFloat(),
            (guiTop + 113).toFloat(),
            110
        )
        Utils.renderAlignedString(
            "§eMultiplier:",
            "§f${multiplier.roundToDecimals(3)}",
            (guiLeft + 160).toFloat(),
            (guiTop + 133).toFloat(),
            110
        )
        Utils.renderAlignedString(
            "§eRaw Chocolate/Second:",
            "§f${StringUtils.formatNumber(rawChocolatePerSecond)}",
            (guiLeft + 160).toFloat(),
            (guiTop + 148).toFloat(),
            110
        )
        Utils.renderAlignedString(
            "§eChocolate/Second:",
            "§f${StringUtils.formatNumber(chocolatePerSecond.roundToDecimals(2))}",
            (guiLeft + 160).toFloat(),
            (guiTop + 163).toFloat(),
            110
        )
        Utils.renderAlignedString(
            "§eChocolate/Day:",
            "§f${StringUtils.formatNumber(chocolatePerSecond.roundToDecimals(2) * 86400)}",
            (guiLeft + 160).toFloat(),
            (guiTop + 178).toFloat(),
            110
        )

        rabbitFamilyInfo.displayInfo(22, 34)
        factoryModifiersInfo.displayInfo(31, 96)
        otherModifiersInfo.displayInfo(44, 158)

        drawRabbitStats(mouseX, mouseY)

        if (tooltipToDisplay.isNotEmpty()) {
            tooltipToDisplay = tooltipToDisplay.map { "§7$it" }
            Utils.drawHoveringText(tooltipToDisplay, mouseX, mouseY, instance.width, instance.height, -1)
            tooltipToDisplay = listOf()
        }
    }

    private fun drawAlignedStringWithHover(
        first: String,
        second: String,
        x: Int,
        y: Int,
        length: Int,
        mouseX: Int,
        mouseY: Int,
        hover: List<String>,
    ) {
        Utils.renderAlignedString(first, second, x.toFloat(), y.toFloat(), length)
        if (mouseX in x..(x + length) && mouseY in y..(y + 13)) {
            tooltipToDisplay = hover
        }
    }

    private fun List<UpgradeInfo>.displayInfo(xPos: Int, yPos: Int) {
        var x = guiLeft + xPos
        val y = guiTop + yPos

        this.forEach { upgradeInfo ->
            Utils.drawStringCentered(
                "§7${upgradeInfo.level}§f",
                x + 10,
                y + 26,
                true,
                0
            )

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

            Utils.drawItemStack(upgradeInfo.stack, x + 2, y + 2)

            x += 22
        }
    }

    private fun drawRabbitStats(mouseX: Int, mouseY: Int) {
        val x = guiLeft + 296
        var y = guiTop + 34

        RabbitCollectionRarity.values().forEach { rabbitInfo ->
            Utils.renderAlignedString(
                rabbitInfo.displayName,
                "§f${rabbitInfo.uniques}/${rabbitInfo.maximum}",
                (x + 30).toFloat(),
                (y + 7).toFloat(),
                90
            )

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

            if (mouseX in x..(x + 120) && mouseY in y..(y + 20)) {
                val tooltip = buildList {
                    add("§7${rabbitInfo.displayName} Rabbits")
                    add("")
                    add("§7Unique Rabbits: §a${rabbitInfo.uniques}/${rabbitInfo.maximum}")
                    add("§7Duplicate Rabbits: §a${rabbitInfo.duplicates}")
                    add("§7Total Rabbits Found: §a${rabbitInfo.uniques + rabbitInfo.duplicates}")
                    add("")
                    add("§7Chocolate Per Second: §a${rabbitInfo.chocolatePerSecond}")
                    add("§7Chocolate Multiplier: §a${rabbitInfo.multiplier.roundToDecimals(3)}")
                }
                tooltipToDisplay = tooltip
            }

            Utils.drawItemStack(rabbitInfo.stack, x + 2, y + 2)

            y += 22
        }
    }

    private fun getData() {
        val data = selectedProfile?.APIDataJson ?: return

        if (data.events?.easter == null) return

        val easterData = data.events?.easter ?: return

        rabbitToRarity.clear()
        RabbitCollectionRarity.resetData()

        val hoppityData = hoppityJson.getAsJsonObject("hoppity") ?: return
        val rabbitRarities = hoppityData.getAsJsonObject("rarities") ?: return
        val specialRabbits = hoppityData.getAsJsonObject("special") ?: return
        val prestigeMultipliers = hoppityData.getAsJsonObject("prestigeMultipliers") ?: return
        val talismanChocolateData = hoppityData.getAsJsonObject("talisman") ?: return

        val foundMythicRabbits = mutableSetOf<String>()

        getTalismanTier(talismanChocolateData)

        for (rarity in rabbitRarities.entrySet()) {
            val rarityName = rarity.key
            val rarityInfo = rarity.value.asJsonObject
            val rabbits = rarityInfo.getAsJsonArray("rabbits")
            for (rabbit in rabbits) {
                rabbitToRarity[rabbit.asString] = rarityName
            }
            val rabbitRarity = RabbitCollectionRarity.fromApiName(rarityName) ?: continue
            rabbitRarity.maximum = rabbits.size()
        }

        val rabbits = easterData.rabbits ?: JsonObject()

        for ((rabbitName, rabbitInfo) in rabbits.entrySet()) {
            if (rabbitInfo.isJsonObject) continue
            val rabbitRarity = rabbitToRarity[rabbitName]?.let { RabbitCollectionRarity.fromApiName(it) } ?: continue
            rabbitRarity.addAmount(rabbitInfo.asInt)
            if (rabbitRarity == RabbitCollectionRarity.MYTHIC) {
                foundMythicRabbits.add(rabbitName)
            }
        }

        for (rarity in rabbitRarities.entrySet()) {
            val rabbitRarity = RabbitCollectionRarity.fromApiName(rarity.key) ?: continue
            val rarityInfo = rarity.value.asJsonObject
            val cps = rarityInfo.get("chocolate").asInt
            val multiplier = rarityInfo.get("multiplier").asDouble
            rabbitRarity.setChocolateData(cps, multiplier)
        }

        for (mythic in foundMythicRabbits) {
            val specialRabbit = specialRabbits.getAsJsonObject(mythic)
            val cps = specialRabbit.get("chocolate").asInt
            val multiplier = specialRabbit.get("multiplier").asDouble
            RabbitCollectionRarity.MYTHIC.chocolatePerSecond += cps
            RabbitCollectionRarity.MYTHIC.multiplier += multiplier
        }

        val totalRabbit = RabbitCollectionRarity.TOTAL
        totalRabbit.uniques = RabbitCollectionRarity.values().sumOf { it.uniques }
        totalRabbit.duplicates = RabbitCollectionRarity.values().sumOf { it.duplicates }
        totalRabbit.chocolatePerSecond = RabbitCollectionRarity.values().sumOf { it.chocolatePerSecond }
        totalRabbit.multiplier = RabbitCollectionRarity.values().sumOf { it.multiplier }
        totalRabbit.maximum = RabbitCollectionRarity.values().sumOf { it.maximum }

        rabbitFamilyInfo.clear()
        factoryModifiersInfo.clear()
        otherModifiersInfo.clear()

        val employeesData = easterData.employees ?: APIDataJson.Events.EasterEventData.EmployeeData()
        val timeTowerInfo = easterData.time_tower ?: APIDataJson.Events.EasterEventData.TimeTowerData()

        val coachLevel = easterData.chocolate_multiplier_upgrades
        val barnLevel = easterData.rabbit_barn_capacity_level
        barnCapacity = barnLevel * 2 + 18

        prestigeLevel = easterData.chocolate_level
        var timeTowerLevel = timeTowerInfo.level
        if (prestigeLevel > 1) timeTowerLevel = timeTowerLevel.coerceAtLeast(1)

        rabbitFamilyInfo.add(UpgradeInfo(rabbitBro, employeesData.rabbit_bro, 1))
        rabbitFamilyInfo.add(UpgradeInfo(rabbitCousin, employeesData.rabbit_cousin, 2))
        rabbitFamilyInfo.add(UpgradeInfo(rabbitSis, employeesData.rabbit_sis, 3))
        rabbitFamilyInfo.add(UpgradeInfo(rabbitDaddy, employeesData.rabbit_father, 4))
        rabbitFamilyInfo.add(UpgradeInfo(rabbitGranny, employeesData.rabbit_grandma, 5))

        factoryModifiersInfo.add(UpgradeInfo(handBaked, easterData.click_upgrades + 1))
        factoryModifiersInfo.add(UpgradeInfo(timeTower, timeTowerLevel))
        factoryModifiersInfo.add(UpgradeInfo(rabbitShrine, easterData.rabbit_rarity_upgrades))
        factoryModifiersInfo.add(UpgradeInfo(coachJackrabbit, coachLevel))

        otherModifiersInfo.add(UpgradeInfo(prestigeItem, prestigeLevel))
        otherModifiersInfo.add(UpgradeInfo(rabbitBarn, barnLevel))

        val shownTalismanItem = talisman?.let { manager.createItem(it) } ?: talismanItem
        otherModifiersInfo.add(UpgradeInfo(shownTalismanItem, talismanChocolate / 10))

        currentChocolate = easterData.chocolate
        prestigeChocolate = easterData.chocolate_since_prestige
        allTimeChocolate = easterData.total_chocolate

        val prestigeMultiplier = prestigeMultipliers.get(prestigeLevel.toString()).asDouble
        val coachMultiplier = 0.01 * coachLevel
        val rabbitMultiplier = RabbitCollectionRarity.TOTAL.multiplier
        multiplier = baseMultiplier + prestigeMultiplier + coachMultiplier + rabbitMultiplier

        val rabbitChocolate = RabbitCollectionRarity.TOTAL.chocolatePerSecond
        val employeeChocolate = rabbitFamilyInfo.sumOf { it.extraCps * it.level }
        rawChocolatePerSecond = rabbitChocolate + employeeChocolate + talismanChocolate

        chocolatePerSecond = rawChocolatePerSecond * multiplier
    }

    private fun getTalismanTier(talismanChocolateData: JsonObject) {
        talisman = null
        var bestTalisman: String? = null
        var bestTalismanCps = 0

        val playerItems = GuiProfileViewer.getSelectedProfile()?.inventoryInfo ?: return
        val talismanInventory = playerItems["talisman_bag"] ?: return
        val playerInventory = playerItems["inv_contents"] ?: return

        for (item in talismanInventory) {
            if (item.isJsonNull) continue
            val internalName = item.asJsonObject.get("internalname").asString
            if (talismanChocolateData.has(internalName)) {
                val cps = talismanChocolateData.get(internalName).asInt
                if (cps > bestTalismanCps) {
                    bestTalisman = internalName
                    bestTalismanCps = cps
                }
            }
        }

        for (item in playerInventory) {
            if (item.isJsonNull) continue
            val internalName = item.asJsonObject.get("internalname").asString
            if (talismanChocolateData.has(internalName)) {
                val cps = talismanChocolateData.get(internalName).asInt
                if (cps > bestTalismanCps) {
                    bestTalisman = internalName
                    bestTalismanCps = cps
                }
            }
        }
        talisman = bestTalisman
        talismanChocolate = bestTalismanCps
    }

    private val rabbitBro: ItemStack = Utils.createSkull(
        "ThatGravyBoat",
        "6adb1fab-c55c-31b1-a575-052b11f3a9c9",
        "ewogICJ0aW1lc3RhbXAiIDogMTcxMjU5NDI0NjM2MywKICAicHJvZmlsZUlkIiA6ICJjZjc4YzFkZjE3ZTI0Y2Q5YTIxYmU4NWQ0NDk5ZWE4ZiIsCiAgInByb2ZpbGVOYW1lIiA6ICJNYXR0c0FybW9yU3RhbmRzIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzI4NzkzNGJkZDlkZjI3MDViMjUxYmI5OTdlMDI5YjE4YzFlOTRkZjEyOTkyYjgxMDdlNzQ0OTdiMjA1Y2E3ZTgiCiAgICB9CiAgfQp9"
    )
    private val rabbitCousin: ItemStack = Utils.createSkull(
        "ThatGravyBoat",
        "3c2e46e4-0bd8-3e75-ac59-c6fe48ce2155",
        "ewogICJ0aW1lc3RhbXAiIDogMTcxMjU5NDI2ODkxNCwKICAicHJvZmlsZUlkIiA6ICJlMjc5NjliODYyNWY0NDg1YjkyNmM5NTBhMDljMWMwMSIsCiAgInByb2ZpbGVOYW1lIiA6ICJLRVZJTktFTE9LRSIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9hOTgyODI1YzAxYjY1OGYzNDhhMDk5YjQ1NzkwMjlhMTgwZDJlNDE1MTgzOTUxYjJlNmU1ZTI3MjU3ZGY0MjU0IgogICAgfQogIH0KfQ=="
    )
    private val rabbitSis: ItemStack = Utils.createSkull(
        "ThatGravyBoat",
        "8b08b9b4-28da-33fe-971b-2c15c6526d80",
        "ewogICJ0aW1lc3RhbXAiIDogMTcxMjg0NzA5MzAxMSwKICAicHJvZmlsZUlkIiA6ICIyMWNjMzkxZmNkMjc0NzY5OTg5Y2M3M2VjYWRiNTE3YiIsCiAgInByb2ZpbGVOYW1lIiA6ICJHT1NUTFk5NyIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9mZDA3NmUwZTNkNDA3MmQwZmZmZWUwYTg3YTVkNzI2ZmMzNGIyYmNlYzM4YzI2NGZiOWI2Nzg3MWE4ZWFkNjMzIgogICAgfQogIH0KfQ=="
    )
    private val rabbitDaddy: ItemStack = Utils.createSkull(
        "ThatGravyBoat",
        "794aa517-c2a6-3762-8791-187665c4eda0",
        "ewogICJ0aW1lc3RhbXAiIDogMTcxMjg0NzA0NzAwNSwKICAicHJvZmlsZUlkIiA6ICIzOThiZGM3NWVhYzQ0ZjMzYWEyMDBiMTYyNTRmMDhlOSIsCiAgInByb2ZpbGVOYW1lIiA6ICJJa2h3YW4wNTEwIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzU3Y2FiMGMzNGQ3ZGRjZjcyZGI1NmZmMzZmMjg4M2Y1NTRjZmY3NmViNWQzYjNlMDU2MjMzODAzNmM5NzYwNDMiCiAgICB9CiAgfQp9"
    )
    private val rabbitGranny: ItemStack = Utils.createSkull(
        "ThatGravyBoat",
        "d6c31145-355d-3807-868c-a7e26e11fc59",
        "ewogICJ0aW1lc3RhbXAiIDogMTcxMjU5NDIyNDA2NCwKICAicHJvZmlsZUlkIiA6ICI2OGVmMmM5NTc5NjM0MjE4YjYwNTM5YWVlOTU3NWJiNSIsCiAgInByb2ZpbGVOYW1lIiA6ICJUaGVNdWx0aUFjb3VudCIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9kNmViMmQ4NWVlOGUzYWYxYzJlYzkzNGJlYjcwYTM5YzVlNzY2YjIzYmRhYjYzMjEwYmQyYWFjZDczY2JiZmM4IgogICAgfQogIH0KfQ=="
    )

    private val handBaked = ItemStack(Items.cookie)
    private val timeTower = ItemStack(Items.clock)
    private val rabbitShrine = ItemStack(Items.rabbit_foot)
    private val coachJackrabbit: ItemStack = Utils.createSkull(
        "ThatGravyBoat",
        "4793d1f6-8b13-3e09-857b-cfb47dea24d3",
        "ewogICJ0aW1lc3RhbXAiIDogMTcxMzAyMjkyOTYwNCwKICAicHJvZmlsZUlkIiA6ICI2NGY0MGFiNzFmM2E0NGZiYjg0N2I5ZWFhOWZjNDRlNSIsCiAgInByb2ZpbGVOYW1lIiA6ICJvZGF2aWRjZXNhciIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9iYzBjYzY3ZTc5YzIyOGU1NDFlNjhhZWIxZDgxZWQ3YWY1MTE2NjYyMmFkNGRiOTQxN2Q3YTI5ZDFiODlhZjk1IgogICAgfQogIH0KfQ=="
    )

    private val prestigeItem = ItemStack(Blocks.dropper)
    private val rabbitBarn = ItemStack(Blocks.oak_fence)
    private val talismanItem = ItemStack(Items.dye, 1, 8)

    data class UpgradeInfo(
        val stack: ItemStack,
        val level: Int,
        val extraCps: Int = 0,
    )

    companion object {
        private val totalRabbit: ItemStack = Utils.createSkull(
            "CalMWolfs",
            "c67dc557-0d47-38a4-a2d4-4e776001ed82",
            "ewogICJ0aW1lc3RhbXAiIDogMTcxMTYzNDM5MTg3OCwKICAicHJvZmlsZUlkIiA6ICIxNmQ4NjI4NzYzMWY0NDY2OGQ0NDM2ZTJlY2IwNTllNSIsCiAgInByb2ZpbGVOYW1lIiA6ICJSZXphVG91cm5leSIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9iNzllN2YzMzQxYjY3MmQ5ZGU2NTY0Y2JhY2EwNTJhNmE3MjNlYTQ2NmEyZTY2YWYzNWJhMWJhODU1ZjBkNjkyIgogICAgfQogIH0KfQ=="
        )
        private val commonRabbit: ItemStack = Utils.createSkull(
            "CalMWolfs",
            "12cfcf5a-aaf8-3a88-ab5a-bacb8557f002",
            "ewogICJ0aW1lc3RhbXAiIDogMTcxMTYzNTExNDE5OCwKICAicHJvZmlsZUlkIiA6ICI5ZDE1OGM1YjNiN2U0ZGNlOWU0OTA5MTdjNmJlYmM5MSIsCiAgInByb2ZpbGVOYW1lIiA6ICJTbm9uX1NTIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzE1ZDBmMGMzNjhlNTRkMjBlM2M1ZTU1MGEwNGE0NjlkMDE2MWIxZmVjZjI2YzhlNTE4MzE4YzA5ZTExMzRmNmIiCiAgICB9CiAgfQp9"
        )
        private val uncommonRabbit: ItemStack = Utils.createSkull(
            "CalMWolfs",
            "60b1f785-0d68-342c-8047-9f13a235e68a",
            "ewogICJ0aW1lc3RhbXAiIDogMTcxMTYzNTM2MzM0NCwKICAicHJvZmlsZUlkIiA6ICI0MzFhMmRlYTQ4YTE0NTMxYjEyZDU5MzY0NDUxNmIyNSIsCiAgInByb2ZpbGVOYW1lIiA6ICJpQ2FwdGFpbk5lbW8iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjgxMDdjNTIwMWVhMjFiYWE4OTU1MTc1MTBiMDA3ZjVmNjE1ZTNjNjYxNWRmNjk2YjkwNmFiOThlNmY5ZjA2IgogICAgfQogIH0KfQ"
        )
        private val rareRabbit: ItemStack = Utils.createSkull(
            "CalMWolfs",
            "68beea4d-1f24-341f-910c-bca2155f1070",
            "ewogICJ0aW1lc3RhbXAiIDogMTcxMTYzNTI0MDkwNCwKICAicHJvZmlsZUlkIiA6ICJmY2ZhYTg0MzA0YjE0NDUxOThkNWYxNzQ3ZjI0Y2Q5MCIsCiAgInByb2ZpbGVOYW1lIiA6ICJMYXJzVGhlV29sZiIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS80NTZhMjA5ODAzZTFlOGViOTQxMTc3ZTJjYzhhMDFiY2VhODg0ZDk0ZGM3N2MzOGUyMmY1Y2QxYTg2MmY4OWNhIgogICAgfQogIH0KfQ"
        )
        private val epicRabbit: ItemStack = Utils.createSkull(
            "CalMWolfs",
            "217a75ba-42a7-3cf9-b366-35c07316942b",
            "ewogICJ0aW1lc3RhbXAiIDogMTcxMTYzNDg4OTY5NywKICAicHJvZmlsZUlkIiA6ICIzYmFlMTVhMWU0Zjg0ZTc5OWE3N2QwZDBhZTNlZDc5NiIsCiAgInByb2ZpbGVOYW1lIiA6ICJiYXlyb25fZ2FtZXJfMjU0IiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzMzNmYwYTUxY2ZiMTBiODE5ZGUxZmNkZjM0NzBmM2QzMzZkYjI2MWQxZmZiYTk0M2E3ODU2NTQwODA5ZGI0ZWUiCiAgICB9CiAgfQp9"
        )
        private val legendaryRabbit: ItemStack = Utils.createSkull(
            "CalMWolfs",
            "447a59da-2eff-3b06-8487-6af08e798c81",
            "ewogICJ0aW1lc3RhbXAiIDogMTcxMTYzNDc1NTM1NCwKICAicHJvZmlsZUlkIiA6ICI5YzM5OTdhMjVjNWY0NmY0OWZlMWFhY2RlZjRiMmMwNSIsCiAgInByb2ZpbGVOYW1lIiA6ICJLaWxsZXJmcmVkZHk4OTQiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGUxYzE3MGI0ZjZjMzc2MTRlZTk2MDk2MDE2NDg1NWFiNzQyNmNlZmI0NDA5N2Y3OTU3ZmEzMGE2N2I5MzVlZiIKICAgIH0KICB9Cn0"
        )
    }

    // todo once someone has a mythic rabbit add it here
    private enum class RabbitCollectionRarity(
        val apiName: String,
        colourCode: String,
        val stack: ItemStack,
        var uniques: Int,
        var duplicates: Int,
        var chocolatePerSecond: Int = 0,
        var multiplier: Double = 0.0,
        var maximum: Int = 0
    ) {
        TOTAL("Total", "§c", totalRabbit, 0, 0),
        COMMON("Common", "§f", commonRabbit, 0, 0),
        UNCOMMON("Uncommon", "§a", uncommonRabbit, 0, 0),
        RARE("Rare", "§9", rareRabbit, 0, 0),
        EPIC("Epic", "§5", epicRabbit, 0, 0),
        LEGENDARY("Legendary", "§6", legendaryRabbit, 0, 0),
        MYTHIC("Mythic", "§d", totalRabbit, 0, 0),
        ;

        val displayName = "$colourCode$apiName"

        fun addAmount(amount: Int) {
            if (amount == 0) return
            this.uniques += 1
            this.duplicates += amount - 1
        }

        fun setChocolateData(cps: Int, multiplier: Double) {
            this.chocolatePerSecond = cps * uniques
            this.multiplier = multiplier * uniques
        }

        companion object {
            fun fromApiName(apiName: String): RabbitCollectionRarity? {
                return values().firstOrNull { it.apiName.lowercase() == apiName }
            }

            fun resetData() {
                values().forEach {
                    it.uniques = 0
                    it.duplicates = 0
                    it.chocolatePerSecond = 0
                    it.multiplier = 0.0
                    it.maximum = 0
                }
            }
        }
    }
}
