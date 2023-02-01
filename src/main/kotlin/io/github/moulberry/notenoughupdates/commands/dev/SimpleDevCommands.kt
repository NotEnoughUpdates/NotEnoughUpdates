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

package io.github.moulberry.notenoughupdates.commands.dev

import com.mojang.brigadier.arguments.StringArgumentType.string
import io.github.moulberry.notenoughupdates.NotEnoughUpdates
import io.github.moulberry.notenoughupdates.autosubscribe.NEUAutoSubscribe
import io.github.moulberry.notenoughupdates.dungeons.DungeonWin
import io.github.moulberry.notenoughupdates.events.RegisterBrigadierCommandEvent
import io.github.moulberry.notenoughupdates.util.brigadier.get
import io.github.moulberry.notenoughupdates.util.brigadier.reply
import io.github.moulberry.notenoughupdates.util.brigadier.thenArgumentExecute
import io.github.moulberry.notenoughupdates.util.brigadier.thenExecute
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@NEUAutoSubscribe
class SimpleDevCommands {
    @SubscribeEvent
    fun onCommands(event: RegisterBrigadierCommandEvent) {
        event.command("neureloadrepo") {
            thenExecute {
                NotEnoughUpdates.INSTANCE.manager.reloadRepository()
                reply("§e[NEU] Reloaded repository.")
            }
        }
        event.command("neudungeonwintest") {
            thenExecute {
                DungeonWin.displayWin()
            }
            thenArgumentExecute("file", string()) { file ->
                DungeonWin.TEAM_SCORE = ResourceLocation("notenoughupdates:dungeon_win/${this[file].lowercase()}.png")
                reply("Changed the dungeon win display")
            }
        }
    }
}
