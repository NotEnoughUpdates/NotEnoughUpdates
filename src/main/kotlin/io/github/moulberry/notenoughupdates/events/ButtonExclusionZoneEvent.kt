/*
 * Copyright (C) 2023 Linnea Gräf
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

package io.github.moulberry.notenoughupdates.events

import io.github.moulberry.notenoughupdates.events.ButtonExclusionZoneEvent.PushDirection.*
import io.github.moulberry.notenoughupdates.util.Rectangle
import net.minecraft.client.gui.GuiScreen
import java.util.*

class ButtonExclusionZoneEvent(
    val gui: GuiScreen,
    val guiBaseRect: Rectangle,
) : NEUEvent() {
    enum class PushDirection {
        TOWARDS_RIGHT,
        TOWARDS_LEFT,
        TOWARDS_TOP,
        TOWARDS_BOTTOM,
    }

    data class ExclusionZone(
        val area: Rectangle,
        val pushDirection: PushDirection,
    )

    val occupiedRects = mutableListOf<ExclusionZone>()
    fun blockArea(area: Rectangle, direction: PushDirection) {
        occupiedRects.add(ExclusionZone(area, direction))
    }

    @JvmOverloads
    fun findButtonPosition(button: Rectangle, margin: Int = 0): Rectangle {
        val processedAreas = IdentityHashMap<ExclusionZone, Unit>()

        var buttonPosition = button
        while (true) {
            val overlappingExclusionZone =
                occupiedRects.find { it !in processedAreas && it.area.intersects(buttonPosition) } ?: break
            buttonPosition = when (overlappingExclusionZone.pushDirection) {
                TOWARDS_RIGHT -> buttonPosition.copy(x = overlappingExclusionZone.area.right + margin)
                TOWARDS_LEFT -> buttonPosition.copy(x = overlappingExclusionZone.area.left - buttonPosition.width - margin)
                TOWARDS_TOP -> buttonPosition.copy(y = overlappingExclusionZone.area.top - buttonPosition.height - margin)
                TOWARDS_BOTTOM -> buttonPosition.copy(y = overlappingExclusionZone.area.bottom + margin)
            }
            processedAreas[overlappingExclusionZone] = Unit
        }

        return buttonPosition
    }


}
