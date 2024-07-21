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

import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import io.github.moulberry.notenoughupdates.NotEnoughUpdates
import io.github.moulberry.notenoughupdates.profileviewer.GuiProfileViewer
import io.github.moulberry.notenoughupdates.profileviewer.GuiProfileViewerPage
import io.github.moulberry.notenoughupdates.profileviewer.SkyblockProfiles
import io.github.moulberry.notenoughupdates.util.Constants
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
    private var repoData: GardenRepoJson? = null

    val background: ResourceLocation = ResourceLocation("notenoughupdates:profile_viewer/garden/background.png.png")

    override fun drawPage(mouseX: Int, mouseY: Int, partialTicks: Float) {
        guiLeft = GuiProfileViewer.getGuiLeft()
        guiTop = GuiProfileViewer.getGuiTop()
        Minecraft.getMinecraft().textureManager.bindTexture(background)
        Utils.drawTexturedRect(guiLeft.toFloat(), guiTop.toFloat(), 0f, 0f, 0f, 1f, 0f, 1f, GL11.GL_NEAREST)

        if (currentlyFetching) {
            Utils.drawStringCentered("§eLoading Data", guiLeft + 220, guiTop + 101, true, 0)
            return
        }

        val newProfile = selectedProfile
        if (newProfile != currentProfile) {
            getData()
            currentProfile = selectedProfile
            return
        }

        if (repoData == null) {
            Utils.drawStringCentered("§cMissing Repo Data", guiLeft + 220, guiTop + 101, true, 0)
            Utils.showOutdatedRepoNotification("garden.json")
            return
        }

        if (gardenData == null || gardenData?.gardenExperience == 0) {
            Utils.drawStringCentered("§cMissing Profile Data", guiLeft + 220, guiTop + 101, true, 0)
            return
        }

        renderPlots()
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
        Minecraft.getMinecraft().textureManager.bindTexture(GuiProfileViewer.pv_elements)
        Utils.drawTexturedRect(
            (guiLeft + 200).toFloat(),
            (guiTop + 100).toFloat(),
            20f,
            20f,
            0f,
            20 / 256f,
            0f,
            20 / 256f,
            GL11.GL_NEAREST
        )
        Utils.drawItemStack(ItemStack(Blocks.grass), guiLeft + 202, guiTop + 102)
    }
}
