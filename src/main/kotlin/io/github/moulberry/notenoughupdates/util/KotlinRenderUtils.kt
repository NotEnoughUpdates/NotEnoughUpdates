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

package io.github.moulberry.notenoughupdates.util

import net.minecraft.client.renderer.GlStateManager
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11

object KotlinRenderUtils {

    @JvmStatic
    fun renderItemStackButton(button: Rectangle, item: ItemStack, texture: ResourceLocation) {
        renderItemStackButton(button.x, button.y, item, texture)
    }

    @JvmStatic
    fun renderItemStackButton(x: Int, y: Int, item: ItemStack, texture: ResourceLocation) {
        MC.textureManager.bindTexture(texture)
        GlStateManager.color(1f, 1f, 1f, 1f)
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

        Utils.drawItemStack(
            item,
            x + 2,
            y + 2
        )
    }
}
