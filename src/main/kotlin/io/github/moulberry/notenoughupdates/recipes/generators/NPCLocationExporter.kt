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

package io.github.moulberry.notenoughupdates.recipes.generators

import io.github.moulberry.notenoughupdates.NotEnoughUpdates
import io.github.moulberry.notenoughupdates.autosubscribe.NEUAutoSubscribe
import io.github.moulberry.notenoughupdates.core.util.StringUtils
import io.github.moulberry.notenoughupdates.core.util.render.RenderUtils
import io.github.moulberry.notenoughupdates.itemeditor.GuiElementTextField
import io.github.moulberry.notenoughupdates.util.ItemUtils
import io.github.moulberry.notenoughupdates.util.SBInfo
import io.github.moulberry.notenoughupdates.util.Utils
import io.github.moulberry.notenoughupdates.util.kotlin.set
import net.minecraft.client.Minecraft
import net.minecraft.client.entity.AbstractClientPlayer
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.BlockPos
import net.minecraftforge.client.event.MouseEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.input.Keyboard
import java.util.*

@NEUAutoSubscribe
class NPCLocationExporter {
    class NPCNamePrompt(val uuid: UUID, val position: BlockPos, val island: String, val skinId: String) : GuiScreen() {
        val nameField = GuiElementTextField("§9Unknown (NPC)", GuiElementTextField.COLOUR).also {
            it.setSize(100, 20)
        }
        var name
            get() = nameField.text
            set(value) {
                nameField.text = value
            }
        val id
            get() = StringUtils.cleanColour(name.replace(" ", "_"))
                .uppercase()
                .replace("[^A-Z_0-9]".toRegex(), "")
        val itemStack
            get() = ItemUtils.createSkullItemStack(
                name,
                uuid.toString(),
                "https://textures.minecraft.net/texture/$skinId"
            )
        val top get() = height / 2 - 50
        val left get() = width / 2 - 100

        override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
            super.drawScreen(mouseX, mouseY, partialTicks)
            drawDefaultBackground()
            RenderUtils.drawFloatingRect(left, top, 200, 100)
            nameField.render(left + 48 + 20, top + 20)
            GlStateManager.pushMatrix()
            GlStateManager.translate((left + 5).toDouble(), (top + 5).toDouble(), 0.0)
            GlStateManager.scale(3.0, 3.0, 1.0)
            GlStateManager.translate(8F, 8F, 0F)

            GlStateManager.rotate(((System.currentTimeMillis() / 5000.0) % 1 * 360).toFloat(), 0F, 0F, 1F)
            Utils.drawItemStack(itemStack, -8, -8, false)
            GlStateManager.popMatrix()
        }

        fun save() {
            val itemStack = this.itemStack
            ItemUtils.getExtraAttributes(itemStack).setString("id", id)
            val json = NotEnoughUpdates.INSTANCE.manager.getJsonForItem(itemStack)
            json["internalname"] = id
            json["clickcommand"] = ""
            json["modver"] = NotEnoughUpdates.VERSION
            json["x"] = position.x
            json["y"] = position.y
            json["z"] = position.z
            json["island"] = island
            NotEnoughUpdates.INSTANCE.manager.writeJsonDefaultDir(json, "$id.json")
            Utils.addChatMessage("Saved to file")
            Minecraft.getMinecraft().displayGuiScreen(null)
        }

        override fun keyTyped(typedChar: Char, keyCode: Int) {
            super.keyTyped(typedChar, keyCode)
            if (keyCode == Keyboard.KEY_RETURN) {
                save()
            }
            nameField.keyTyped(typedChar, keyCode)
        }

        override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
            super.mouseClicked(mouseX, mouseY, mouseButton)
            val mouseX = Utils.getMouseX()
            val mouseY = Utils.getMouseY()
            if (mouseX - left - 48 - 20 in 0..nameField.width &&
                mouseY - top - 20 in 0..nameField.height
            ) {
                nameField.mouseClicked(mouseX, mouseY, mouseButton)
            } else {
                nameField.otherComponentClick()
            }
        }
    }

    @SubscribeEvent
    fun onMouseClick(event: MouseEvent) {
        if (event.buttonstate || event.button != 2 || !NotEnoughUpdates.INSTANCE.config.apiData.repositoryEditing) return
        val pointedEntity = Minecraft.getMinecraft().pointedEntity
        if (pointedEntity == null) {
            Utils.addChatMessage("Could not find entity under cursor")
            return
        }
        if (pointedEntity !is AbstractClientPlayer) {
            Utils.addChatMessage("Entity under cursor is not a player")
            return
        }
        val uuid = pointedEntity.uniqueID
        val position = pointedEntity.position
        val island = SBInfo.getInstance().getLocation()
        val skin = pointedEntity.locationSkin.resourcePath?.replace("skins/", "")
        if (skin == null) {
            Utils.addChatMessage("Could not load skin")
            return
        }
        Minecraft.getMinecraft().displayGuiScreen(NPCNamePrompt(uuid, position, island, skin))
    }
}
