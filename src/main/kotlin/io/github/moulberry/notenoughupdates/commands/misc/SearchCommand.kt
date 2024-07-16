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

package io.github.moulberry.notenoughupdates.commands.misc

import com.mojang.brigadier.arguments.StringArgumentType
import io.github.moulberry.notenoughupdates.NotEnoughUpdates
import io.github.moulberry.notenoughupdates.autosubscribe.NEUAutoSubscribe
import io.github.moulberry.notenoughupdates.events.RegisterBrigadierCommandEvent
import io.github.moulberry.notenoughupdates.overlays.AuctionSearchOverlay
import io.github.moulberry.notenoughupdates.overlays.BazaarSearchOverlay
import io.github.moulberry.notenoughupdates.overlays.RecipeSearchOverlay
import io.github.moulberry.notenoughupdates.util.brigadier.RestArgumentType
import io.github.moulberry.notenoughupdates.util.brigadier.get
import io.github.moulberry.notenoughupdates.util.brigadier.thenArgumentExecute
import io.github.moulberry.notenoughupdates.util.brigadier.thenExecute
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@NEUAutoSubscribe
class SearchCommand {
    @SubscribeEvent
    fun onCommands(event: RegisterBrigadierCommandEvent) {
        event.command("bzs") {
            thenArgumentExecute("search", StringArgumentType.string()) { search ->
                NotEnoughUpdates.INSTANCE.sendChatMessage("/bz ${this[search]}")
            }
            thenExecute {  ->
                NotEnoughUpdates.INSTANCE.openGui = BazaarSearchOverlay()
            }
        }
        event.command("ahs") {
            thenArgumentExecute("search", StringArgumentType.string()) { search ->
                NotEnoughUpdates.INSTANCE.sendChatMessage("/ahs ${this[search]}")
            }
            thenExecute {  ->
                NotEnoughUpdates.INSTANCE.openGui = AuctionSearchOverlay()
            }
        }
        event.command("ah") {
            thenArgumentExecute("search", RestArgumentType) { search ->
                val searchString = this[search]
                if (!NotEnoughUpdates.INSTANCE.config.ahTweaks.convertSearchCommand) {
                    NotEnoughUpdates.INSTANCE.sendChatMessage("/ah $searchString")
                    return@thenArgumentExecute
                }
                val split = searchString.split(" ")
                if (split.size > 1) {
                    NotEnoughUpdates.INSTANCE.sendChatMessage("/ahs $searchString")
                } else {
                    NotEnoughUpdates.INSTANCE.sendChatMessage("/ah $searchString")
                }
            }
            thenExecute {
                NotEnoughUpdates.INSTANCE.sendChatMessage("/ah")
            }
        }
        event.command("recipe") {
            thenArgumentExecute("search", StringArgumentType.string()) { search ->
                NotEnoughUpdates.INSTANCE.sendChatMessage("/recipe ${this[search]}")
            }
            thenExecute {  ->
                NotEnoughUpdates.INSTANCE.openGui = RecipeSearchOverlay()
            }
        }
    }
}
