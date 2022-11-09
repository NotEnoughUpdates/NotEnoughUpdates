/*
 * Copyright (C) 2022 NotEnoughUpdates contributors
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

package io.github.moulberry.notenoughupdates

import io.github.moulberry.notenoughupdates.util.Utils
import net.minecraft.command.ICommandSender
import net.minecraft.util.EnumChatFormatting.*

/**
 * This is just a placeholder Kotlin file so that the folder exists and I can test stuff.
 * It will be deleted soon.
 *
 * *N.B.:* This Kotlin setup will not fly outside a development environment.
 *
 */
object DidYouKnowYouHaveRights {
    @JvmStatic
    fun stuff(@Suppress("UNUSED_PARAMETER") sender: ICommandSender) {
        Utils.addChatMessage("${BLUE}My favourite part was when ${LIGHT_PURPLE}Moulberry ${BLUE}said \"${GREEN}It's Mouling Time!${BLUE}\" and ${DARK_RED}${BOLD}mouled ${BLUE}all over the place.")
    }
}

