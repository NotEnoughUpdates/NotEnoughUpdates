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
import io.github.moulberry.notenoughupdates.util.Utils
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.GuiTextField
import net.minecraft.client.renderer.Tessellator
import net.minecraft.init.Items
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.client.GuiScrollingList

class ItemSearchGui(initialText: String, val onSelect: (String?) -> Unit) : GuiScreen() {
    val textField = GuiTextField(
        0, Minecraft.getMinecraft().fontRendererObj,
        -1,
        5,
        200,
        20
    )
    var lastFilter = ""

    class ItemScrollingList(
        screenWidth: Int,
        screenHeight: Int,
        val callback: (ItemStack) -> Unit
    ) : GuiScrollingList(
        Minecraft.getMinecraft(),
        200,
        screenHeight - 30,
        30,
        screenHeight,
        screenWidth / 2 - 100,
        20,
        screenWidth,
        screenHeight
    ) {
        var items: List<ItemStack> = listOf()
            private set
        var selected: Int? = null

        fun setItems(newItems: List<ItemStack>) {
            this.items = newItems
            selected = null
        }

        override fun getSize(): Int {
            return items.size
        }

        override fun elementClicked(i: Int, doubleClick: Boolean) {
            if (doubleClick)
                callback(items[i])
            selected = i
        }

        override fun isSelected(i: Int): Boolean {
            return selected == i
        }

        override fun drawBackground() {

        }

        override fun drawSlot(i: Int, right: Int, top: Int, height: Int, tessellator: Tessellator?) {
            val item = items[i]
            Utils.drawItemStack(item, this.left + 1, top + 1)
            Utils.drawStringF(item.displayName, this.left + 18F, top + 1F, true, 0xFF00FF00.toInt())
        }
    }

    lateinit var items: ItemScrollingList


    init {
        textField.text = initialText
    }

    override fun initGui() {
        super.initGui()
        textField.xPosition = width / 2 - 100
        textField.setCanLoseFocus(false)
        textField.isFocused = true
        items = ItemScrollingList(width, height) {
            val name = NotEnoughUpdates.INSTANCE.manager.createItemResolutionQuery()
                .withItemStack(it)
                .resolveInternalName() ?: return@ItemScrollingList
            onSelect(name)
        }
        updateItems()
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        super.drawScreen(mouseX, mouseY, partialTicks)
        drawDefaultBackground()
        textField.drawTextBox()
        items.drawScreen(mouseX, mouseY, partialTicks)
    }

    override fun onGuiClosed() {
        onSelect(null)
    }
    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        super.mouseClicked(mouseX, mouseY, mouseButton)
        textField.mouseClicked(mouseX, mouseY, mouseButton)
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        super.keyTyped(typedChar, keyCode)
        textField.textboxKeyTyped(typedChar, keyCode)
    }

    fun updateItems() {
        lastFilter = textField.text

        val candidates =
            NotEnoughUpdates.INSTANCE.manager.search(textField.text)
                .map {
                    NotEnoughUpdates.INSTANCE.manager.createItemResolutionQuery()
                        .withKnownInternalName(it)
                        .resolveToItemStack() ?: ItemStack(
                        Items.painting, 1, 10
                    )
                }
        items.setItems(candidates)
    }

    override fun updateScreen() {
        super.updateScreen()
        textField.updateCursorCounter()
        if (textField.text != lastFilter) updateItems()
    }
}
