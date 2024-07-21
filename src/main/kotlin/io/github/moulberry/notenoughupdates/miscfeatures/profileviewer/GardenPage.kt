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

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import io.github.moulberry.notenoughupdates.NotEnoughUpdates
import io.github.moulberry.notenoughupdates.core.util.StringUtils
import io.github.moulberry.notenoughupdates.profileviewer.GuiProfileViewer
import io.github.moulberry.notenoughupdates.profileviewer.GuiProfileViewerPage
import io.github.moulberry.notenoughupdates.profileviewer.ProfileViewer.Level
import io.github.moulberry.notenoughupdates.profileviewer.ProfileViewerUtils
import io.github.moulberry.notenoughupdates.profileviewer.SkyblockProfiles
import io.github.moulberry.notenoughupdates.util.Constants
import io.github.moulberry.notenoughupdates.util.MC
import io.github.moulberry.notenoughupdates.util.UrsaClient
import io.github.moulberry.notenoughupdates.util.Utils
import io.github.moulberry.notenoughupdates.util.kotlin.Coroutines
import net.minecraft.client.Minecraft
import net.minecraft.init.Blocks
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11

class GardenPage(pvInstance: GuiProfileViewer) : GuiProfileViewerPage(pvInstance) {
    private val manager get() = NotEnoughUpdates.INSTANCE.manager

    private var guiLeft = GuiProfileViewer.getGuiLeft()
    private var guiTop = GuiProfileViewer.getGuiTop()

    private var currentProfile: SkyblockProfiles.SkyblockProfile? = null
    private var gardenData: GardenData? = null
    private var currentlyFetching = false
    private lateinit var repoData: GardenRepoJson

    private var tooltipToDisplay = listOf<String>()

    private var mouseX: Int = 0
    private var mouseY: Int = 0

    val background: ResourceLocation = ResourceLocation("notenoughupdates:profile_viewer/garden/background.png")

    override fun drawPage(mouseX: Int, mouseY: Int, partialTicks: Float) {
        guiLeft = GuiProfileViewer.getGuiLeft()
        guiTop = GuiProfileViewer.getGuiTop()

        this.mouseX = mouseX
        this.mouseY = mouseY

        if (currentlyFetching) {
            Utils.drawStringCentered("§eLoading Data", guiLeft + 220, guiTop + 101, true, 0)
            return
        }

        if (Constants.GARDEN == null) {
            Utils.drawStringCentered("§cMissing Repo Data", guiLeft + 220, guiTop + 101, true, 0)
            Utils.showOutdatedRepoNotification("garden.json")
            return
        }

        val newProfile = selectedProfile
        if (newProfile != currentProfile) {
            getData()
            currentProfile = selectedProfile
            return
        }

        if (gardenData == null || gardenData?.gardenExperience == 0) {
            Utils.drawStringCentered("§cMissing Profile Data", guiLeft + 220, guiTop + 101, true, 0)
            return
        }

        MC.textureManager.bindTexture(background)
        Utils.drawTexturedRect(guiLeft.toFloat(), guiTop.toFloat(), instance.sizeX.toFloat(), instance.sizeY.toFloat(), GL11.GL_NEAREST)

        renderPlots()
        renderGardenLevel()
        renderCropUpgrades()
        renderCropMilestones()

        if (tooltipToDisplay.isNotEmpty()) {
            tooltipToDisplay = tooltipToDisplay.map { "§7$it" }
            Utils.drawHoveringText(tooltipToDisplay, mouseX, mouseY, instance.width, instance.height, -1)
            tooltipToDisplay = listOf()
        }
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int): Boolean {

        return false
    }

    private fun getData() {
        currentlyFetching = true
        val profileId = selectedProfile?.outerProfileJson?.get("profile_id")?.asString?.replace("-", "")
        Coroutines.launchCoroutine {
            gardenData = loadGardenData(profileId)
            currentlyFetching = false
        }
    }

    private fun loadGardenData(profileId: String?): GardenData? {
        profileId ?: return null
        val data = manager.ursaClient.get(UrsaClient.gardenForProfile(profileId)).get()

        val gson = GsonBuilder().setPrettyPrinting().registerTypeAdapter(CropType::class.java, cropTypeAdapter.nullSafe()).create()
        repoData = gson.fromJson(Constants.GARDEN, GardenRepoJson::class.java)
        return gson.fromJson(data, GardenDataJson::class.java).garden
    }

    private val cropTypeAdapter = object : TypeAdapter<CropType>() {
        override fun write(writer: JsonWriter, value: CropType) {
            writer.value(value.name)
        }

        override fun read(reader: JsonReader): CropType? {
            return CropType.fromApiName(reader.nextString())
        }
    }

    private fun renderPlots() {
        val top = guiTop + 80
        val left = guiLeft + 160
        for (value in repoData.plots) {
            val contains = gardenData?.unlockedPlotIds?.contains(value.key)
            contains ?: continue
            if (!contains) continue
            Minecraft.getMinecraft().textureManager.bindTexture(GuiProfileViewer.pv_elements)
            Utils.drawTexturedRect(
                (left + value.value.x * 22).toFloat(),
                (top + value.value.y * 22).toFloat(),
                20f,
                20f,
                0f,
                20 / 256f,
                0f,
                20 / 256f,
                GL11.GL_NEAREST
            )
            Utils.drawItemStack(ItemStack(Blocks.grass), left + value.value.x * 22 + 2, top + value.value.y * 22 + 2)
        }

        Minecraft.getMinecraft().textureManager.bindTexture(GuiProfileViewer.pv_elements)
        Utils.drawTexturedRect(
            (left + 2 * 22).toFloat(),
            (top + 2 * 22).toFloat(),
            20f,
            20f,
            0f,
            20 / 256f,
            0f,
            20 / 256f,
            GL11.GL_NEAREST
        )
        repoData.barn[gardenData?.selectedBarnSkin]?.let {
            val itemStack =
                NotEnoughUpdates.INSTANCE.manager.createItemResolutionQuery().withKnownInternalName(it)
                    .resolveToItemStack()
            Utils.drawItemStack(itemStack, left + 2 * 22 + 2, top + 2 * 22 + 2)
        } ?: Utils.drawItemStack(ItemStack(Blocks.grass), left + 2 * 22 + 2, top + 2 * 22 + 2)
    }

    private fun renderCropUpgrades() {
        val startHeight = guiTop + 100
        var yPos = startHeight
        var xPos = guiLeft + 6

        Utils.renderShadowedString("§eCrop Upgrades", xPos + 70, yPos + 5, 105)

        for ((index, crop) in CropType.values().withIndex()) {
            if (index == 5) {
                yPos = startHeight
                xPos += 70
            }
            yPos += 14

            val upgradeLevel = gardenData?.cropUpgradeLevels?.get(crop) ?: 0

            val itemStack = manager.createItem(crop.itemId)
            Utils.drawItemStack(itemStack, xPos + 2, yPos)
            Utils.renderAlignedString("§e${crop.displayName}", "§f$upgradeLevel", (xPos + 20).toFloat(), (yPos + 5).toFloat(), 50)
        }
    }

    private fun renderCropMilestones() {
        val startHeight = guiTop + 10
        var yPos = startHeight
        var xPos = guiLeft + 6

        Utils.renderShadowedString("§eCrop Milestones", xPos + 70, yPos + 5, 105)

        for ((index, crop) in CropType.values().withIndex()) {
            if (index == 5) {
                yPos = startHeight
                xPos += 70
            }
            yPos += 14

            val levelsInfo = repoData.cropMilestones[crop] ?: continue
            val currentCollection = gardenData?.resourcesCollected?.get(crop) ?: 0
            val levelInfo = getLevel(levelsInfo, currentCollection)
            val collectionLevel = levelInfo.level.toInt()
            val formattedAmount = StringUtils.formatNumber(currentCollection.toDouble())
            drawAlignedStringWithHover(
                "§e${crop.displayName}",
                "§f$collectionLevel",
                xPos + 20,
                yPos + 5,
                50,
                mouseX,
                mouseY,
                listOf("§7Farmed: §f$formattedAmount")
            )
        }
    }

    private fun renderGardenLevel() {
        val top = guiTop + 20
        val left = guiLeft + 160
        val level = getLevel(repoData.gardenExperience, gardenData?.gardenExperience?.toLong())
        instance.renderBar(top.toFloat(), (left + 6).toFloat(), 80f, level.level % 1)
    }

    private fun getLevel(experienceList: List<Int>, currentExp: Long?): Level {
        val gson = Gson()
        val array = JsonArray()
        experienceList.forEach { array.add(gson.toJsonTree(it)) }
        return ProfileViewerUtils.getLevel(array, (currentExp ?: 0).toFloat(), experienceList.size, false)
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
}
