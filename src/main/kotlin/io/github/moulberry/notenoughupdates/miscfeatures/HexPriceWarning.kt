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

package io.github.moulberry.notenoughupdates.miscfeatures

import io.github.moulberry.notenoughupdates.NotEnoughUpdates
import io.github.moulberry.notenoughupdates.autosubscribe.NEUAutoSubscribe
import io.github.moulberry.notenoughupdates.core.util.render.RenderUtils
import io.github.moulberry.notenoughupdates.core.util.render.TextRenderUtils
import io.github.moulberry.notenoughupdates.events.IsSlotBeingHoveredEvent
import io.github.moulberry.notenoughupdates.events.SlotClickEvent
import io.github.moulberry.notenoughupdates.util.ItemUtils
import io.github.moulberry.notenoughupdates.util.ScreenReplacer
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.EnumChatFormatting
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse

/*
    We are gathered here today to mourn the death of 26m coins that Alea spent on a Cleave 6 book, while trying to take
    off Cleave 5.
 */
@NEUAutoSubscribe
object HexPriceWarning : ScreenReplacer() {

    fun shouldCheck(): Boolean {
        return getLimit() > 0
    }

    fun getLimit(): Double {
        return NotEnoughUpdates.INSTANCE.config.enchantingSolvers.hexOverpayWarning;
    }

    var hasWarning = false
    var lastClickedSlot = 0
    var cost = ""
    var upgradeName = ""
    override fun shouldShow(): Boolean {
        return shouldCheck() && hasWarning
    }

    override fun render() {
        val scaledResolution = ScaledResolution(Minecraft.getMinecraft())
        val width = scaledResolution.scaledWidth
        val height = scaledResolution.scaledHeight

        GlStateManager.disableLighting()

        GlStateManager.pushMatrix()
        GlStateManager.translate(0f, 0f, 500f)

        drawRect(0, 0, width, height, -0x80000000)

        RenderUtils.drawFloatingRectDark(width / 2 - 90, height / 2 - 45, 180, 90)

        val neuLength = Minecraft.getMinecraft().fontRendererObj.getStringWidth("\u00a7lNEU")
        Minecraft.getMinecraft().fontRendererObj.drawString(
            "\u00a7lNEU",
            width / 2 + 90 - neuLength - 3,
            height / 2 - 45 + 4,
            -0x1000000
        )

        TextRenderUtils.drawStringCenteredScaledMaxWidth(
            "Are you SURE?",
            (width / 2).toFloat(), (height / 2 - 45 + 10).toFloat(), false, 170, -0xbfc0
        )


        val sellLine = "\u00a77[ §r$upgradeName\u00a77 ]"

        TextRenderUtils.drawStringCenteredScaledMaxWidth(
            sellLine,
            (width / 2).toFloat(), (height / 2 - 45 + 25).toFloat(), false, 170, -0x1
        )
        TextRenderUtils.drawStringCenteredScaledMaxWidth(
            "will cost you §6$cost§r coins",
            (width / 2).toFloat(), (height / 2 - 45 + 34).toFloat(), false, 170, -0x5f5f60
        )


        RenderUtils.drawFloatingRectDark(width / 2 - 43, height / 2 + 23, 40, 16, false)
        RenderUtils.drawFloatingRectDark(width / 2 + 3, height / 2 + 23, 40, 16, false)

        TextRenderUtils.drawStringCenteredScaledMaxWidth(
            EnumChatFormatting.GREEN.toString() + "[Y]es",
            (width / 2 - 23).toFloat(), (height / 2 + 31).toFloat(), true, 36, -0xff0100
        )
        TextRenderUtils.drawStringCenteredScaledMaxWidth(
            EnumChatFormatting.RED.toString() + "[N]o",
            (width / 2 + 23).toFloat(), (height / 2 + 31).toFloat(), true, 36, -0x10000
        )

        GlStateManager.popMatrix()
    }

    fun confirmClick() {
        val chest = Minecraft.getMinecraft().currentScreen as GuiChest
        Minecraft.getMinecraft().playerController.windowClick(
            chest.inventorySlots.windowId,
            lastClickedSlot, 0, 0, Minecraft.getMinecraft().thePlayer
        )
        hasWarning = false
    }


    override fun mouseInput(mouseX: Int, mouseY: Int): Boolean {
        val scaledResolution = ScaledResolution(Minecraft.getMinecraft())
        val width = scaledResolution.scaledWidth
        val height = scaledResolution.scaledHeight

        if (Mouse.getEventButtonState()) {
            if (mouseY >= height / 2 + 23 && mouseY <= height / 2 + 23 + 16) {
                confirmClick()
            }

            if (mouseX < width / 2 - 90 || mouseX > width / 2 + 90 || mouseY < height / 2 - 45 || mouseY > height / 2 + 45) {
                hasWarning = false
            }
        }
        return false
    }

    override fun keyboardInput(): Boolean {
        if (!Keyboard.getEventKeyState()) {
            if (Keyboard.getEventKey() == Keyboard.KEY_Y || Keyboard.getEventKey() == Keyboard.KEY_RETURN) {
                confirmClick()
            }
            hasWarning = false
            return true
        }

        return false

    }

    @SubscribeEvent
    fun onHover(event: IsSlotBeingHoveredEvent) {
        if (shouldShow()) {
            event.prevent()
        }
    }


    @SubscribeEvent
    fun onClick(event: SlotClickEvent) {
        if (!shouldCheck()) return
        if (hasWarning) return
        val stack = event.slot.stack ?: return
        val lore = ItemUtils.getLore(stack)
        val bazaarPriceLine = lore.indexOf("§7Bazaar Price")
        if (bazaarPriceLine >= 0 &&
            (bazaarPriceLine + 1) in lore.indices
        ) {
            val priceLine = lore[bazaarPriceLine + 1]
            val priceMatcher = coins.matcher(priceLine)
            if (!priceMatcher.matches()) return
            val price = priceMatcher.group(1).replace(",", "").toDouble()
            if (price >= getLimit()) {
                hasWarning = true
                lastClickedSlot = event.slotId
                cost = priceMatcher.group(1)
                upgradeName = ItemUtils.getDisplayName(stack.tagCompound)
                event.cancel()
            }
        }
    }

    val coins = "§6([,.0-9]+) Coins".toPattern()
}
