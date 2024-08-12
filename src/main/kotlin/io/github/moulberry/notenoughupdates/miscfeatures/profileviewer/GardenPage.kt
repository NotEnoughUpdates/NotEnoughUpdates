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
import io.github.moulberry.notenoughupdates.profileviewer.data.APIDataJson
import io.github.moulberry.notenoughupdates.util.Constants
import io.github.moulberry.notenoughupdates.util.MC
import io.github.moulberry.notenoughupdates.util.UrsaClient
import io.github.moulberry.notenoughupdates.util.Utils
import io.github.moulberry.notenoughupdates.util.kotlin.Coroutines
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
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
    private var eliteData: EliteWeightJson? = null
    private var currentlyFetching = false
    private lateinit var repoData: GardenRepoJson
    private var apiData: APIDataJson? = null

    private var mouseX: Int = 0
    private var mouseY: Int = 0

    private val visitorRarityToVisits: MutableMap<VisitorRarity, Int> = mutableMapOf()
    private val visitorRarityToCompleted: MutableMap<VisitorRarity, Int> = mutableMapOf()

    val background: ResourceLocation = ResourceLocation("notenoughupdates:profile_viewer/garden/background.png")

    companion object {
        private val cropTypeAdapter = object : TypeAdapter<CropType>() {
            override fun write(writer: JsonWriter, value: CropType) {
                writer.value(value.name)
            }

            override fun read(reader: JsonReader): CropType? {
                return CropType.fromApiName(reader.nextString())
            }
        }

        val gson: Gson =
            GsonBuilder().setPrettyPrinting().registerTypeAdapter(CropType::class.java, cropTypeAdapter.nullSafe())
                .create()
    }

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
        val selectedProfile = GuiProfileViewer.getSelectedProfile() ?: return
        apiData = selectedProfile.APIDataJson ?: return

        MC.textureManager.bindTexture(background)
        Utils.drawTexturedRect(
            guiLeft.toFloat(),
            guiTop.toFloat(),
            instance.sizeX.toFloat(),
            instance.sizeY.toFloat(),
            GL11.GL_NEAREST
        )

        renderPlots()
        renderGardenLevel()
        renderFarmingWeight()
        renderCropUpgrades()
        renderCropMilestones()
        renderVisitorStats()
        renderCompost()
    }

    private fun getData() {
        currentlyFetching = true
        val profileId = selectedProfile?.outerProfileJson?.get("profile_id")?.asString?.replace("-", "")
        Coroutines.launchCoroutine {
            gardenData = loadGardenData(profileId)
            getVisitorData()
            currentlyFetching = false
        }
        Coroutines.launchCoroutine {
            eliteData = loadFarmingWeight(GuiProfileViewer.getProfile()?.uuid, profileId)
        }
    }

    private fun loadGardenData(profileId: String?): GardenData? {
        profileId ?: return null
        val data = manager.ursaClient.get(UrsaClient.gardenForProfile(profileId)).get()
        repoData = gson.fromJson(Constants.GARDEN, GardenRepoJson::class.java)
        return gson.fromJson(data, GardenDataJson::class.java).garden
    }

    private fun getVisitorData() {
        for ((visitor, amount) in gardenData?.commissionData?.visits ?: return) {
            val rarity = repoData.visitors[visitor]
            if (rarity == null) {
                println("Unknown visitor: $visitor")
                continue
            }
            visitorRarityToVisits[rarity] = visitorRarityToVisits.getOrDefault(rarity, 0) + amount
            visitorRarityToVisits[VisitorRarity.TOTAL] =
                visitorRarityToVisits.getOrDefault(VisitorRarity.TOTAL, 0) + amount
        }
        for ((visitor, amount) in gardenData?.commissionData?.completed ?: return) {
            val rarity = repoData.visitors[visitor] ?: continue
            visitorRarityToCompleted[rarity] = visitorRarityToCompleted.getOrDefault(rarity, 0) + amount
            visitorRarityToCompleted[VisitorRarity.TOTAL] =
                visitorRarityToCompleted.getOrDefault(VisitorRarity.TOTAL, 0) + amount
        }
    }

    private fun loadFarmingWeight(uuid: String?, profileId: String?): EliteWeightJson? {
        uuid ?: return null
        profileId ?: return null
        val data = manager.apiUtils.request()
            .url("https://api.elitebot.dev/weight/$uuid/$profileId")
            .requestJson().get()
        return gson.fromJson(data, EliteWeightJson::class.java)
    }

    private fun renderPlots() {
        val top = guiTop + 79
        val left = guiLeft + 192
        GlStateManager.color(1f, 1f, 1f, 1f)
        for (value in repoData.plots) {
            Minecraft.getMinecraft().textureManager.bindTexture(GuiProfileViewer.pv_elements)
            val x = left + value.value.x * 22
            val y = top + value.value.y * 22
            Utils.drawTexturedRect(
                x.toFloat(),
                y.toFloat(),
                20f,
                20f,
                0f,
                20 / 256f,
                0f,
                20 / 256f,
                GL11.GL_NEAREST
            )
            if (gardenData?.unlockedPlotIds?.contains(value.key) != true) {
                Utils.drawItemStack(ItemStack(Blocks.barrier), x + 2, y + 2)
                if (mouseX >= x && mouseX <= x + 20 && mouseY >= y && mouseY <= y + 20) {
                    instance.tooltipToDisplay = listOf("§cLocked " + value.value.name)
                }
                continue
            }
            Utils.drawItemStack(ItemStack(Blocks.grass), x + 2, y + 2)
            if (mouseX >= x && mouseX <= x + 20 && mouseY >= y && mouseY <= y + 20) {
                instance.tooltipToDisplay = listOf(value.value.name)
            }
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
        val x = left + 2 * 22 + 2
        val y = top + 2 * 22 + 2
        var error = true
        repoData.barn[gardenData?.selectedBarnSkin]?.let {
            val itemStack = NotEnoughUpdates.INSTANCE.manager.createItemResolutionQuery().withKnownInternalName(it.item)
                .resolveToItemStack()
            Utils.drawItemStack(itemStack, x, y)
            if (mouseX >= x && mouseX <= x + 20 && mouseY >= y && mouseY <= y + 20) {
                instance.tooltipToDisplay =
                    listOf("§7Barn Skin: ${it.name}")
            }
            error = false
        }
        if (error) {
            Utils.drawItemStack(ItemStack(Blocks.barrier), x, y)
            if (mouseX >= x && mouseX <= x + 20 && mouseY >= y && mouseY <= y + 20) {
                instance.tooltipToDisplay = listOf(
                    "§cUnknown barn Skin: ${gardenData?.selectedBarnSkin}",
                    "§cIf you expected it to be there please send a message in",
                    "§c§l#neu-support §r§con §ldiscord.gg/moulberry"
                )
            }
        }

    }

    private fun renderCropUpgrades() {
        val startHeight = guiTop + 105
        var yPos = startHeight
        var xPos = guiLeft + 26

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
            Utils.renderAlignedString(
                "§e${crop.displayName}",
                "§f$upgradeLevel",
                (xPos + 20).toFloat(),
                (yPos + 5).toFloat(),
                50
            )

            if (mouseX >= xPos + 20 && mouseX <= xPos + 70 && mouseY >= yPos && mouseY <= yPos + 20) {
                val tooltip = ArrayList<String>()
                tooltip.add("§a${crop.displayName}")
                tooltip.add("")
                if (repoData.cropUpgrades.size == upgradeLevel) {
                    tooltip.add("§7Current Tier: §a$upgradeLevel§7/§a${repoData.cropUpgrades.size}")
                } else {
                    tooltip.add("§7Current Tier: §e$upgradeLevel§7/§a${repoData.cropUpgrades.size}")
                }
                tooltip.add("§7${crop.displayName} Fortune: §6+${upgradeLevel*5}☘")
                tooltip.add("")
                if (repoData.cropUpgrades.size == upgradeLevel) {
                    tooltip.add("§6Maxed")
                } else {
                    tooltip.add("§7Cost:")
                    tooltip.add("§c${repoData.cropUpgrades[upgradeLevel]} §7Copper to Upgrade")
                    val totalCopper = repoData.cropUpgrades.sum()
                    val sum = totalCopper - repoData.cropUpgrades.subList(0, upgradeLevel).sum()
                    tooltip.add("§c$sum §7Copper to Max")
                }
                instance.tooltipToDisplay = tooltip
            }
        }
    }

    private fun renderCropMilestones() {
        val startHeight = guiTop + 10
        var yPos = startHeight
        var xPos = guiLeft + 26

        Utils.renderShadowedString("§eCrop Milestones", xPos + 70, yPos + 5, 105)

        for ((index, crop) in CropType.values().withIndex()) {
            if (index == 5) {
                yPos = startHeight
                xPos += 70
            }
            yPos += 14

            val itemStack = manager.createItem(crop.itemId)
            Utils.drawItemStack(itemStack, xPos + 2, yPos)
            val levelsInfo = repoData.cropMilestones[crop] ?: continue
            val currentCollection = gardenData?.resourcesCollected?.get(crop) ?: 0
            val levelInfo = getLevel(levelsInfo, currentCollection)
            val collectionLevel = levelInfo.level.toInt()
            val formattedAmount = StringUtils.formatNumber(currentCollection.toDouble())
            var nextLevel = 0
            var nextLevelString = "§6MAXED"
            var maxLevel = 0
            var maxLevelString = ""
            var formattedPercentage = "100.00"
            var formattedMaxLevelPercentage = "100.00"
            var aboveMaxMilestoneNumber: String
            var lastCropMilestoneAmountRequired = 0

            for (i in 0..45) {
                maxLevel += levelsInfo[i]
                if (i < collectionLevel + 1) nextLevel += levelsInfo[i]
                if (i == 45) lastCropMilestoneAmountRequired = levelsInfo[i]
            }
            if (!levelInfo.maxed) {
                maxLevelString = StringUtils.formatNumber(maxLevel)
                val remainingForNext = levelsInfo[collectionLevel] - (nextLevel - currentCollection)
                val formattedRemainingForNext = StringUtils.formatNumber(remainingForNext.toDouble())
                nextLevelString =
                    "§e$formattedRemainingForNext§6/§e${StringUtils.formatNumber(levelsInfo[collectionLevel].toDouble())}"

                val percentage = (remainingForNext / levelsInfo[collectionLevel].toDouble()) * 100
                formattedPercentage = String.format("%.2f", percentage)

                val maxLevelPercentage = (currentCollection.toFloat() / maxLevel.toFloat()) * 100
                formattedMaxLevelPercentage = String.format("%.2f", maxLevelPercentage)
            }
            val tooltip = ArrayList<String>()
            tooltip.add("§a${crop.displayName} $collectionLevel")
            tooltip.add("§7Total: §a$formattedAmount")
            tooltip.add("")
            if (!levelInfo.maxed) {
                tooltip.add("Progress to Tier " + (levelInfo.level.toInt() + 1) + ": §e$formattedPercentage%")
                tooltip.add(nextLevelString)
                tooltip.add("")
                tooltip.add("Progress to Tier 46: §e$formattedMaxLevelPercentage%")
                tooltip.add("§e$formattedAmount§6/§e$maxLevelString")
            } else {
                val aboveMaxMilestone =
                    (currentCollection.toDouble() - maxLevel.toFloat()) + lastCropMilestoneAmountRequired
                aboveMaxMilestoneNumber = StringUtils.formatNumber(aboveMaxMilestone)
                tooltip.add("§7Overflow: §6$aboveMaxMilestoneNumber")
                tooltip.add("")
                tooltip.add("§6Max tier reached!")
            }
            drawAlignedStringWithHover(
                "§e${crop.displayName}",
                "§f$collectionLevel",
                xPos + 20,
                yPos + 5,
                50,
                tooltip
            )
        }
    }

    private fun renderVisitorStats() {
        val xPos = guiLeft + 322
        var yPos = guiTop + 17

        Utils.renderShadowedString("§eVisitors", xPos + 40, yPos - 2, 80)

        // todo progress bar!
        Utils.renderAlignedString(
            "§eUnique Visitors",
            "§f${gardenData?.commissionData?.uniqueNpcsServed ?: 0}/${repoData.visitors.size}",
            xPos.toFloat(),
            (yPos + 10).toFloat(),
            80
        )
        yPos += 20

        for (rarity in VisitorRarity.values()) {
            val formattedVisits = StringUtils.formatNumber(visitorRarityToVisits.getOrDefault(rarity, 0))
            val formattedCompleted = StringUtils.formatNumber(visitorRarityToCompleted.getOrDefault(rarity, 0))
            val tooltip = listOf(
                "§7Visits: §f$formattedVisits",
                "§7Completed: §f$formattedCompleted",
            )
            val rarityStats = "§f$formattedCompleted/$formattedVisits"
            drawAlignedStringWithHover(rarity.displayName, rarityStats, xPos, yPos, 80, tooltip)
            yPos += 12
        }
    }

    private fun renderGardenLevel() {
        val top = guiTop + 20
        val left = guiLeft + 190
        val level = getLevel(repoData.gardenExperience, gardenData?.gardenExperience?.toLong())
        if (level.maxed) {
            instance.renderGoldBar((left).toFloat() + 16, (top + 10).toFloat(), 80f)
        } else {
            instance.renderBar(left.toFloat() + 16, (top + 10).toFloat(), 80f, level.level % 1)
        }

        val maxXp = level.maxXpForLevel.toInt()
        val totalXpS = StringUtils.formatNumber(level.totalXp.toLong())
        val gardenTooltip = ArrayList<String>()
        gardenTooltip.add("§2Garden")
        if (level.maxed) {
            gardenTooltip.add("§7Progress: §6MAXED!")
        } else {
            gardenTooltip.add(
                "§7Progress: §5" +
                        StringUtils.shortNumberFormat(Math.round((level.level % 1) * maxXp)) +
                        "/" +
                        StringUtils.shortNumberFormat(maxXp)
            )
        }
        gardenTooltip.add(
            "§7Total XP: §5${totalXpS}§8 (" +
                    StringUtils.formatToTenths(instance.getPercentage("garden", level)) +
                    "% to ${level.maxLevel})"
        )
        drawAlignedStringWithHover("§2Garden", "§f${level.level.toInt()}", left + 36, top, 60, gardenTooltip)
        Utils.drawItemStack(ItemStack(Blocks.grass), left + 16, top - 6)

        val copper = apiData?.garden_player_data?.copper ?: 0
        Utils.renderAlignedString(
            "§cCopper",
            "§f" + StringUtils.formatNumber(copper),
            (left + 16).toFloat(),
            (top + 20).toFloat(),
            80
        )
    }

    private fun renderFarmingWeight() {
        val top = guiTop + 51
        val left = guiLeft + 190

        if (eliteData == null) {
            drawAlignedStringWithHover(
                "§eFarming Weight",
                "§eLoading...",
                left + 16,
                top,
                95,
                listOf("§eLoading...", "§eTry again soon!")
            )
            return
        }

        val totalWeight = eliteData?.totalWeight ?: 0.0
        val bonusWeight = eliteData?.bonusWeight?.values?.sum() ?: 0.0

        val tooltip = buildList{
            add("§7Total Weight: §f${StringUtils.formatNumber(totalWeight)}")
            add("§7Bonus Weight: §f${StringUtils.formatNumber(bonusWeight)}")

            for (crop in CropType.values()) {
                val cropWeight = eliteData?.cropWeight?.get(crop) ?: 0.0
                add("§7${crop.displayName}: §f${StringUtils.formatNumber(cropWeight)}")
            }
        }

        drawAlignedStringWithHover(
            "§eFarming Weight",
            "§f${StringUtils.formatNumber(totalWeight.toInt())}",
            left + 11,
            top,
            90,
            tooltip
        )
    }

    private fun renderCompost() {
        val xPos = guiLeft + 322
        var yPos = guiTop + 122

        Utils.renderShadowedString("§eCompost Upgrades", xPos + 40, yPos - 2, 80)
        yPos += 12

        val (speed, multiDrop, fuelCap, organicMatterCap, costReduction) = gardenData?.composterData?.upgrades ?: return
        for (i in 0..4) {
            val upgradeName = when (i) {
                0 -> "§aSpeed"
                1 -> "§aMulti Drop"
                2 -> "§aFuel Cap"
                3 -> "§aOrganic Matter Cap"
                4 -> "§aCost Reduction"
                else -> 0
            }
            val upgradeAmount = when (i) {
                0 -> speed
                1 -> multiDrop
                2 -> fuelCap
                3 -> organicMatterCap
                4 -> costReduction
                else -> 0
            }
            val repoName = when (i) {
                0 -> "speed"
                1 -> "multi_drop"
                2 -> "fuel_cap"
                3 -> "organic_matter_cap"
                4 -> "cost_reduction"
                else -> 0
            }
            val tooltip = ArrayList<String>()
            val upgradeValues = repoData.composterUpgrades[repoName]?.get(upgradeAmount + 1)
            val upgradeValuesCurrent = repoData.composterUpgrades[repoName]?.get(upgradeAmount)?.upgrade ?: 0
            val upgradeValuesCurrentSt = StringUtils.formatNumber(upgradeValuesCurrent)
            tooltip.add("$upgradeName $upgradeAmount")
            if (upgradeValues != null) {
                repoData.composterTooltips[repoName]?.replace("{}", "$upgradeValuesCurrentSt -> ${StringUtils.formatNumber(upgradeValues.upgrade)}")
                    ?.let { tooltip.add(it) }
                tooltip.add("")
                tooltip.add("§7Cost:")
                for (item in upgradeValues.items) {
                    val itemStack = manager.createItem(item.key.uppercase())
                    if (itemStack == null) {
                        println("Item not found: ${item.key}")
                        tooltip.add("§cUnknown Item: ${item.key}")
                    } else {
                        tooltip.add("§7${item.value}x ${itemStack.displayName}")
                    }
                }
                tooltip.add("§7${upgradeValues.copper} §cCopper")
            } else {
                repoData.composterTooltips[repoName]?.replace("{}", upgradeValuesCurrentSt)
                    ?.let { tooltip.add(it) }
                tooltip.add("§6Maxed")
            }

            drawAlignedStringWithHover("§e$upgradeName", "§f$upgradeAmount", xPos, yPos, 80, tooltip)
            yPos += 12
        }
    }

    private fun getLevel(experienceList: List<Int>, currentExp: Long?): Level {
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
        hover: List<String>,
    ) {
        Utils.renderAlignedString(first, second, x.toFloat(), y.toFloat(), length)
        if (mouseX in x..(x + length) && mouseY in y..(y + 13)) {
            instance.tooltipToDisplay = hover
        }
    }
}
