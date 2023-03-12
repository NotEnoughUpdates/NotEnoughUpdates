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

package io.github.moulberry.notenoughupdates.miscgui

import io.github.moulberry.notenoughupdates.NotEnoughUpdates
import io.github.moulberry.notenoughupdates.util.Utils
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fml.common.registry.GameRegistry
import org.lwjgl.opengl.GL11
import java.awt.Color
import kotlin.math.ceil

class DynamicLightItemsEditor() : GuiScreen() {

    val background = ResourceLocation("notenoughupdates:dynamic_light_items_editor.png")
    val enabledButton = ResourceLocation("notenoughupdates:enabled_button.png")
    val disabledButton = ResourceLocation("notenoughupdates:disabled_button.png")
    val chestGui = ResourceLocation("textures/gui/container/generic_54.png")
    val widgets = ResourceLocation("textures/gui/widgets.png")
    val help = ResourceLocation("notenoughupdates:help.png")

    var xSize = 217
    var ySize = 88
    var guiLeft = 0
    var guiTop = 0

    var stackToRender: String? = null
    var itemSelected: String? = null

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        drawDefaultBackground()

        val numOfItems = NotEnoughUpdates.INSTANCE.config.hidden.dynamicLightItems.size
        val numOfRows = if (didApplyMixin) ceil(numOfItems / 9f).toInt() else 0
        ySize = 70 + 18 * numOfRows
        guiLeft = (width - xSize) / 2
        guiTop = (height - ySize) / 2

        // Top and bottom half of gui
        Minecraft.getMinecraft().textureManager.bindTexture(background)
        Utils.drawTexturedRect(guiLeft.toFloat(), guiTop.toFloat(), xSize.toFloat(), 24F,
            0F, 1F, 0F, 24 / 88f, GL11.GL_NEAREST)
        Utils.drawTexturedRect(guiLeft.toFloat(), (guiTop + ySize - 46).toFloat(), xSize.toFloat(), 46F,
            0F, 1F, 42 / 88f, 1F, GL11.GL_NEAREST)

        fontRendererObj.drawString("Dynamic Light Items Editor", guiLeft + 10, guiTop + 7, 4210752)

        GlStateManager.color(1f, 1f, 1f, 1f)
        Minecraft.getMinecraft().textureManager.bindTexture(help)
        Utils.drawTexturedRect((guiLeft + xSize + 3).toFloat(), guiTop.toFloat(), 16F, 16F, GL11.GL_NEAREST)
        if (mouseX >= guiLeft + xSize + 3 &&
            mouseX <= guiLeft + xSize + 19 &&
            mouseY >= guiTop &&
            mouseY <= guiTop + 16) {
            val tooltip = listOf(
                "§bDynamic Light Item Editor",
                "§eWhat is this?",
                "§eNEU makes use of OptiFine's feature of certain items",
                "§eemitting dynamic light. By default OptiFine only implements",
                "§ethis feature for a select few minecraft items.",
                "",
                "§eThis editor however, allows you to add specific skyblock",
                "§eitems that will emit dynamic light when held. Simply hold the",
                "§eitem you wish to add, then open this menu again and click",
                "§e'Add Held Item', now if you have OptiFine installed and the",
                "§edynamic lights option enabled, the added items will emit light!",
                "",
                "§eTo remove an item, click the item in this menu and click",
                "§ethe 'Remove Item' button in the bottom right.",
            )
            Utils.drawHoveringText(tooltip, mouseX, mouseY, width, height, -1)
        }

        if (!didApplyMixin) {
            fontRendererObj.drawString("Could not find OptiFine!", guiLeft + 50, guiTop + 22, Color.RED.rgb)
            fontRendererObj.drawString("Go to #neu-support in", guiLeft + 50, guiTop + 32, Color.RED.rgb)
            fontRendererObj.drawString("the discord for help", guiLeft + 52, guiTop + 42, Color.RED.rgb)
            return
        }

        // Buttons
        GlStateManager.color(1f, 1f, 1f, 1f)
        Minecraft.getMinecraft().textureManager.bindTexture(enabledButton)
        Utils.drawTexturedRect(guiLeft.toFloat() + 15, (guiTop + ySize - 32).toFloat(), 88F, 20F,
            0F, 1F, 0F, 1F, GL11.GL_NEAREST)

        if (itemSelected != null) {
            Minecraft.getMinecraft().textureManager.bindTexture(enabledButton)
        } else {
            Minecraft.getMinecraft().textureManager.bindTexture(disabledButton)
        }
        Utils.drawTexturedRect(guiLeft.toFloat() + 114, (guiTop + ySize - 32).toFloat(), 88F, 20F,
            0F, 1F, 0F, 1F, GL11.GL_NEAREST)

        fontRendererObj.drawString("Add Held Item", guiLeft + 27, guiTop + ySize - 26, 4210752)
        fontRendererObj.drawString("Remove Item", guiLeft + 130, guiTop + ySize - 26, 4210752)

        GlStateManager.color(1f, 1f, 1f, 1f)

        // Add in some part of the gui for every row
        Minecraft.getMinecraft().textureManager.bindTexture(background)
        for (i in 0 until numOfRows) {
            Utils.drawTexturedRect(guiLeft.toFloat(), ((guiTop + 24) + (i * 18)).toFloat(), xSize.toFloat(), 18f,
                0f, 1f, 24 / 88f, 42 / 88f, GL11.GL_NEAREST)
        }

        var hoveredItem: String? = null
        var selectedPosition: Pair<Int, Int> = Pair(-999, -999)

        // Draw a slot for each item and the ItemStack
        for ((index, item) in NotEnoughUpdates.INSTANCE.config.hidden.dynamicLightItems.withIndex()) {
            val i = index % 9
            val j = index / 9
            GlStateManager.color(1f, 1f, 1f, 1f)

            Minecraft.getMinecraft().textureManager.bindTexture(chestGui)
            drawTexturedModalRect(guiLeft + 27 + i % 9 * 18, guiTop + 24 + j * 18, 7, 17, 18, 18)

            val itemStack = resolveItemStack(item) ?: return
            Utils.drawItemStack(itemStack, guiLeft + 28 + i % 9 * 18, guiTop + 25 + j * 18)

            if (mouseX >= guiLeft + 27 + i % 9 * 18 && mouseX <= guiLeft + 45 + i % 9 * 18) {
                if (mouseY >= guiTop + 24 + j * 18 && mouseY <= guiTop + 42 + j * 18) {
                    hoveredItem = item
                    val tooltip = itemStack.getTooltip(Minecraft.getMinecraft().thePlayer, false)
                    Utils.drawHoveringText(tooltip, mouseX, mouseY, width, height, -1)
                }
            }

            if (itemSelected != null && itemSelected.equals(item)) {
                // Save the position, so when we render the selected box its renders on top of everything
                selectedPosition = Pair(guiLeft + 24 + i % 9 * 18, guiTop + 21 + j * 18)
            }
        }

        stackToRender = hoveredItem

        GlStateManager.color(1f, 1f, 1f, 1f)
        Minecraft.getMinecraft().textureManager.bindTexture(widgets)
        drawTexturedModalRect(selectedPosition.first, selectedPosition.second, 0, 22, 24, 24)

        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (didApplyMixin) {
            // Add Held Item button
            if (mouseX >= guiLeft + 15 &&
                mouseX <= guiLeft + 103 &&
                mouseY >= (guiTop + ySize - 32) &&
                mouseY <= (guiTop + ySize - 12)) {

                val heldItem = Minecraft.getMinecraft().thePlayer.heldItem

                if (heldItem == null) {
                    Utils.addChatMessage("§c[NEU] You can't add your hand to the list of dynamic light items.")
                    return
                }

                val internalName = resolveInternalName(heldItem)
                if (internalName == null) {
                    Utils.addChatMessage("§c[NEU] Couldn't resolve an internal name for this item!")
                    return
                }
                NotEnoughUpdates.INSTANCE.config.hidden.dynamicLightItems.add(internalName)
            }

            // Remove Item button
            if (mouseX >= guiLeft + 114 &&
                mouseX <= guiLeft + 202 &&
                mouseY >= guiTop + ySize - 32 &&
                mouseY <= guiTop + ySize - 12 &&
                itemSelected != null) {
                NotEnoughUpdates.INSTANCE.config.hidden.dynamicLightItems.remove(itemSelected)
                itemSelected = null
            }

            if (stackToRender != null) {
                itemSelected = stackToRender
            }
        }

        super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    companion object {
        @JvmStatic
        var didApplyMixin = false

        fun resolveItemStack(internalName: String): ItemStack? {
            var itemStack = NotEnoughUpdates.INSTANCE.manager
                .createItemResolutionQuery()
                .withKnownInternalName(internalName)
                .resolveToItemStack()
            if (itemStack == null) {
                // Try resolve the item stack through forge
                itemStack = GameRegistry.makeItemStack(internalName, 0, 1, null)
                if (itemStack == null) {
                    Utils.addChatMessage("§c[NEU] Couldn't resolve the ItemStack for $internalName")
                    return null
                }
            }

            return itemStack
        }

        @JvmStatic
        fun resolveInternalName(itemStack: ItemStack): String? {
            var internalName =
                NotEnoughUpdates.INSTANCE.manager.createItemResolutionQuery().withItemStack(itemStack).resolveInternalName()
            if (internalName == null) {
                // If resolving internal name failed, the item may be a minecraft item
                internalName = itemStack.item.registryName
                if (internalName == null) {
                    // Check if minecraft searching also fails
                    // Leave error handling for caller since this method is also called in MixinOFDynamicLights which
                    // is run every tick, and we don't want to flood the chat
                    return null
                }
            }

            return internalName
        }

        @JvmStatic
        fun findDynamicLightItems(itemStack: ItemStack): Int {
            val internalName: String = resolveInternalName(itemStack) ?: return 0
            if (NotEnoughUpdates.INSTANCE.config.hidden.dynamicLightItems.contains(internalName)) {
                return 15
            }
            return 0
        }
    }
}
