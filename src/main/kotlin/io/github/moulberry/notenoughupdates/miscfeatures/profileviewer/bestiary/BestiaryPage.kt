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
package io.github.moulberry.notenoughupdates.miscfeatures.profileviewer.bestiary

import io.github.moulberry.notenoughupdates.core.util.StringUtils
import io.github.moulberry.notenoughupdates.profileviewer.GuiProfileViewer
import io.github.moulberry.notenoughupdates.profileviewer.GuiProfileViewerPage
import io.github.moulberry.notenoughupdates.miscfeatures.profileviewer.bestiary.BestiaryData.calculateTotalBestiaryTiers
import io.github.moulberry.notenoughupdates.miscfeatures.profileviewer.bestiary.BestiaryData.hasMigrated
import io.github.moulberry.notenoughupdates.miscfeatures.profileviewer.bestiary.BestiaryData.parseBestiaryData
import io.github.moulberry.notenoughupdates.util.Constants
import io.github.moulberry.notenoughupdates.util.Utils
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumChatFormatting
import net.minecraft.util.ResourceLocation
import org.lwjgl.input.Mouse
import org.lwjgl.opengl.GL11
import java.awt.Color

/**
 * Individual mob entry in the Bestiary
 */
data class Mob(
    val name: String, val icon: ItemStack, val kills: Double, val deaths: Double, val mobLevelData: MobLevelData
)

/**
 * A Bestiary category as defined in `constants/bestiary.json`
 */
data class Category(
    val id: String,
    val name: String,
    val icon: ItemStack,
    val mobs: List<Mob>,
    val subCategories: List<Category>,
    val familyData: FamilyData
)

/**
 * Level data for one specific mob
 */
data class MobLevelData(
    val level: Int, val maxLevel: Boolean, val progress: Double, val totalProgress: Double, val mobKillData: MobKillData
)

/**
 * Kills data for one specific mob
 */
data class MobKillData(
    val tierKills: Double, val tierReq: Double, val cappedKills: Double, val cap: Double
)

/**
 * Family data for one specific category
 */
data class FamilyData(
    val found: Int, val completed: Int, val total: Int
)

class BestiaryPage(instance: GuiProfileViewer?) : GuiProfileViewerPage(instance) {
    private var selectedCategory = "dynamic"
    private var selectedSubCategory = ""
    private var tooltipToDisplay: MutableList<String> = mutableListOf()
    private var bestiaryLevel = 0.0
    private var computedCategories: MutableList<Category> = mutableListOf()
    private var lastProfileName = ""

    private val bestiaryTexture = ResourceLocation("notenoughupdates:pv_bestiary_tab.png")
    private val mobListXCount = 9
    private val mobListYCount = 5
    private val mobListXPadding = (240 - mobListXCount * 20) / (mobListXCount + 1).toFloat()
    private val mobListYPadding = (202 - mobListYCount * 20) / (mobListYCount + 1).toFloat()

    override fun drawPage(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val guiLeft = GuiProfileViewer.getGuiLeft()
        val guiTop = GuiProfileViewer.getGuiTop()

        val selectedProfile = GuiProfileViewer.getSelectedProfile() ?: return
        val profileInfo = selectedProfile.profileJson
        val profileName = GuiProfileViewer.getProfileName()

        if (!hasMigrated(profileInfo) || Constants.BESTIARY == null) {
            Utils.drawStringCentered(
                "${EnumChatFormatting.RED}No valid bestiary data!",
                guiLeft + 431 / 2f,
                (guiTop + 101).toFloat(),
                true,
                0
            )
            lastProfileName = profileName
            return
        }
        // Do the initial parsing only once or on profile switch
        if (computedCategories.isEmpty() || lastProfileName != profileName) {
            computedCategories = parseBestiaryData(profileInfo)
            bestiaryLevel = calculateTotalBestiaryTiers(computedCategories).toDouble()
        }

        if (computedCategories.isEmpty() || Constants.BESTIARY == null) {
            Utils.drawStringCentered(
                "${EnumChatFormatting.RED}No valid bestiary data!",
                guiLeft + 431 / 2f,
                (guiTop + 101).toFloat(),
                true,
                0
            )
            lastProfileName = profileName
            return
        }
        lastProfileName = profileName

        val bestiarySize = computedCategories.size
        val bestiaryXSize = (350f / (bestiarySize - 1 + 0.0000001f)).toInt()


        // Render the category list
        for ((categoryXIndex, category) in computedCategories.withIndex()) {
            Minecraft.getMinecraft().textureManager.bindTexture(GuiProfileViewer.pv_elements)

            if (mouseX > guiLeft + 30 + bestiaryXSize * categoryXIndex &&
                mouseX < guiLeft + 30 + bestiaryXSize * categoryXIndex + 20
                && mouseY > guiTop + 10 && mouseY < guiTop + 10 + 20
            ) {
                tooltipToDisplay.add(EnumChatFormatting.GRAY.toString() + category.name)
                if (Mouse.getEventButtonState() && selectedCategory != category.id) {
                    selectedCategory = category.id
                    Utils.playPressSound()
                }
            }


            if (category.id == selectedCategory) {
                Utils.drawTexturedRect(
                    (guiLeft + 30 + bestiaryXSize * categoryXIndex).toFloat(),
                    (guiTop + 10).toFloat(),
                    20f,
                    20f,
                    20 / 256f,
                    0f,
                    20 / 256f,
                    0f,
                    GL11.GL_NEAREST
                )
            } else {
                Utils.drawTexturedRect(
                    (guiLeft + 30 + bestiaryXSize * categoryXIndex).toFloat(),
                    (guiTop + 10).toFloat(),
                    20f,
                    20f,
                    0f,
                    20 / 256f,
                    0f,
                    20 / 256f,
                    GL11.GL_NEAREST
                )
            }
            Utils.drawItemStack(category.icon, guiLeft + 32 + bestiaryXSize * categoryXIndex, guiTop + 12)
        }

        val scaledResolution = ScaledResolution(Minecraft.getMinecraft())
        val width = scaledResolution.scaledWidth
        val height = scaledResolution.scaledHeight

        Minecraft.getMinecraft().textureManager.bindTexture(bestiaryTexture)
        Utils.drawTexturedRect(guiLeft.toFloat(), guiTop.toFloat(), 431f, 202f, GL11.GL_NEAREST)
        GlStateManager.color(1f, 1f, 1f, 1f)
        val color = Color(128, 128, 128, 255)
        Utils.renderAlignedString(
            EnumChatFormatting.RED.toString() + "Milestone: ",
            "${EnumChatFormatting.GRAY}${(bestiaryLevel / 10)}",
            (guiLeft + 280).toFloat(),
            (guiTop + 50).toFloat(),
            110
        )

        // Render the subcategories in the bottom right corner, if applicable
        val selectedCategory = computedCategories.first { it.id == selectedCategory }
        if (selectedCategory.subCategories.isNotEmpty()) {
            if (selectedSubCategory == "") {
                selectedSubCategory = selectedCategory.subCategories.first().id
            }

            Utils.renderShadowedString(
                "${EnumChatFormatting.RED}Subcategories", (guiLeft + 317).toFloat(), (guiTop + 165).toFloat(), 1000
            )
            GlStateManager.color(1f, 1f, 1f, 1f)

            val xStart = (guiLeft + 280).toFloat()
            val y = (guiTop + 175).toFloat()
            for ((i, subCategory) in selectedCategory.subCategories.withIndex()) {
                Minecraft.getMinecraft().textureManager.bindTexture(GuiProfileViewer.pv_elements)

                if (subCategory.id == selectedSubCategory) {
                    Utils.drawTexturedRect(
                        (xStart + 24 * i.toFloat()),
                        y,
                        20f,
                        20f,
                        20 / 256f,
                        0f,
                        20 / 256f,
                        0f,
                        GL11.GL_NEAREST
                    )
                } else {
                    Utils.drawTexturedRect(
                        (xStart + 24 * i.toFloat()),
                        y,
                        20f,
                        20f,
                        0f,
                        20 / 256f,
                        0f,
                        20 / 256f,
                        GL11.GL_NEAREST
                    )
                }
                Utils.drawItemStack(subCategory.icon, (xStart + 24 * i + 2).toInt(), y.toInt() + 2)
                if (mouseX > xStart + 24 * i
                    && mouseX < xStart + 24 * (i + 1)
                    && mouseY > y
                    && mouseY < y + 16
                ) {
                    tooltipToDisplay.add(subCategory.name)
                    if (Mouse.getEventButtonState() && selectedSubCategory != subCategory.id) {
                        selectedSubCategory = subCategory.id
                        Utils.playPressSound()
                    }
                }
            }
        } else {
            selectedSubCategory = ""
        }

        // Render family information
        var catData = selectedCategory.familyData
        Utils.renderAlignedString(
            EnumChatFormatting.RED.toString() + "Families Found:",
            (if (catData.found == catData.total) "§6" else "§7") + "${catData.found}/${catData.total}",
            guiLeft + 280F,
            guiTop + 70F,
            110
        )
        if (catData.found == catData.total) {
            instance.renderGoldBar(guiLeft + 280F, guiTop + 80F, 112F)
        } else {
            instance.renderBar(guiLeft + 280F, guiTop + 80F, 112F, catData.found / catData.total.toFloat())
        }

        Utils.renderAlignedString(
            EnumChatFormatting.RED.toString() + "Families Completed:",
            (if (catData.completed == catData.total) "§6" else "§7") + "${catData.completed}/${catData.total}",
            guiLeft + 280F,
            guiTop + 90F,
            110
        )
        if (catData.completed == catData.total) {
            instance.renderGoldBar(guiLeft + 280F, guiTop + 100F, 112F)
        } else {
            instance.renderBar(guiLeft + 280F, guiTop + 100F, 112F, catData.completed / catData.total.toFloat())
        }

        // Render subcategory family information, if possible
        if (selectedSubCategory != "") {
            catData = selectedCategory.subCategories.find { it.id == selectedSubCategory }!!.familyData
            Utils.renderAlignedString(
                EnumChatFormatting.RED.toString() + "Families Found:",
                (if (catData.found == catData.total) "§6" else "§7") + "${catData.found}/${catData.total}",
                guiLeft + 280F,
                guiTop + 120F,
                110
            )
            if (catData.found == catData.total) {
                instance.renderGoldBar(guiLeft + 280F, guiTop + 130F, 112F)
            } else {
                instance.renderBar(guiLeft + 280F, guiTop + 130F, 112F, catData.found / catData.total.toFloat())
            }

            Utils.renderAlignedString(
                EnumChatFormatting.RED.toString() + "Families Completed:",
                (if (catData.completed == catData.total) "§6" else "§7") + "${catData.completed}/${catData.total}",
                guiLeft + 280F,
                guiTop + 140F,
                110
            )
            if (catData.completed == catData.total) {
                instance.renderGoldBar(guiLeft + 280F, guiTop + 150F, 112F)
            } else {
                instance.renderBar(guiLeft + 280F, guiTop + 150F, 112F, catData.completed / catData.total.toFloat())
            }
        }

        // Determine which mobs should be displayed
        val mobs = if (selectedSubCategory != "") {
            selectedCategory.subCategories.first { it.id == selectedSubCategory }.mobs
        } else {
            selectedCategory.mobs
        }

        // Render the mob list
        for ((i, mob) in mobs.withIndex()) {
            val stack = mob.icon
            val xIndex = i % mobListXCount
            val yIndex = i / mobListXCount
            val x = 23 + mobListXPadding + (mobListXPadding + 20) * xIndex
            val y = 30 + mobListYPadding + (mobListYPadding + 20) * yIndex

            GlStateManager.disableLighting()
            RenderHelper.enableGUIStandardItemLighting()
            GlStateManager.color(1f, 1f, 1f, 1f)
            Minecraft.getMinecraft().textureManager.bindTexture(GuiProfileViewer.pv_elements)
            Utils.drawTexturedRect(
                guiLeft + x,
                guiTop + y,
                20f,
                20f,
                0f,
                20 / 256f,
                0f,
                20f / 256f,
                GL11.GL_NEAREST
            )
            Utils.drawItemStack(stack, guiLeft + x.toInt() + 2, guiTop + y.toInt() + 2)
            val kills = mob.kills
            val deaths = mob.deaths

            if (mouseX > guiLeft + x.toInt() + 2 && mouseX < guiLeft + x.toInt() + 18) {
                if (mouseY > guiTop + y.toInt() + 2 && mouseY < guiTop + y.toInt() + 18) {
                    tooltipToDisplay = ArrayList()
                    tooltipToDisplay.add(
                        "${mob.name} ${mob.mobLevelData.level}"
                    )
                    tooltipToDisplay.add(
                        EnumChatFormatting.GRAY.toString() + "Kills: " + EnumChatFormatting.GREEN +
                                StringUtils.formatNumber(kills)
                    )
                    tooltipToDisplay.add(
                        EnumChatFormatting.GRAY.toString() + "Deaths: " + EnumChatFormatting.GREEN +
                                StringUtils.formatNumber(deaths)
                    )
                    tooltipToDisplay.add("")

                    if (!mob.mobLevelData.maxLevel) {
                        tooltipToDisplay.add(
                            EnumChatFormatting.GRAY.toString() + "Progress to Tier ${mob.mobLevelData.level + 1}: " +
                                    EnumChatFormatting.AQUA + "${mob.mobLevelData.progress}%"
                        )

                        var bar = "§3§l§m"
                        for (j in 1..14) {
                            var col = ""
                            if (mob.mobLevelData.progress < j * (100 / 14)) col = "§f§l§m"
                            bar += "$col "
                        }
                        tooltipToDisplay.add(
                            "${bar}§r§b ${StringUtils.formatNumber(mob.mobLevelData.mobKillData.tierKills)}/${
                                StringUtils.formatNumber(
                                    mob.mobLevelData.mobKillData.tierReq
                                )
                            }"
                        )
                        tooltipToDisplay.add("")
                    }

                    tooltipToDisplay.add(
                        EnumChatFormatting.GRAY.toString() + "Overall Progress: " + EnumChatFormatting.AQUA + "${mob.mobLevelData.totalProgress}%" +
                                if (mob.mobLevelData.maxLevel) " §7(§c§lMAX!§r§7)" else ""
                    )
                    var bar = "§3§l§m"
                    for (j in 1..14) {
                        var col = ""
                        if (mob.mobLevelData.totalProgress < j * (100 / 14)) col = "§f§l§m"
                        bar += "$col "
                    }
                    tooltipToDisplay.add(
                        "${bar}§r§b ${StringUtils.formatNumber(mob.mobLevelData.mobKillData.cappedKills)}/${
                            StringUtils.formatNumber(
                                mob.mobLevelData.mobKillData.cap
                            )
                        }"
                    )
                }
            }
            GlStateManager.color(1f, 1f, 1f, 1f)
            Utils.drawStringCentered(
                if (mob.mobLevelData.maxLevel) {
                    "${EnumChatFormatting.GOLD}${mob.mobLevelData.level}"
                } else {
                    mob.mobLevelData.level.toString()
                }, guiLeft + x + 10, guiTop + y + 26, true, color.rgb
            )
        }

        // Render the accumulated tooltip, if applicable
        if (tooltipToDisplay.isNotEmpty()) {
            val grayTooltip: MutableList<String> = ArrayList(tooltipToDisplay.size)
            for (line in tooltipToDisplay) {
                grayTooltip.add(EnumChatFormatting.GRAY.toString() + line)
            }
            Utils.drawHoveringText(grayTooltip, mouseX, mouseY, width, height, -1)
            tooltipToDisplay.clear()
        }
    }
}
