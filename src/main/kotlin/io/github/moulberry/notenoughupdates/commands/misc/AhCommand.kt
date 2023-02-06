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

package io.github.moulberry.notenoughupdates.commands.misc

import io.github.moulberry.notenoughupdates.NotEnoughUpdates
import io.github.moulberry.notenoughupdates.auction.CustomAHGui
import io.github.moulberry.notenoughupdates.autosubscribe.NEUAutoSubscribe
import io.github.moulberry.notenoughupdates.events.RegisterBrigadierCommandEvent
import io.github.moulberry.notenoughupdates.util.Utils
import io.github.moulberry.notenoughupdates.util.brigadier.RestArgumentType
import io.github.moulberry.notenoughupdates.util.brigadier.get
import io.github.moulberry.notenoughupdates.util.brigadier.reply
import io.github.moulberry.notenoughupdates.util.brigadier.thenArgumentExecute
import net.minecraft.util.EnumChatFormatting.RED
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.function.Predicate

@NEUAutoSubscribe
class AhCommand {
    @SubscribeEvent
    fun onCommands(event: RegisterBrigadierCommandEvent) {
        val hook = event.command("neuah") {

            thenArgumentExecute("search", RestArgumentType) { search ->
                if (NotEnoughUpdates.INSTANCE.config.apiData.apiKey == null ||
                    NotEnoughUpdates.INSTANCE.config.apiData.apiKey.isBlank()
                ) {
                    reply("${RED}Can't open NEU AH: an api key is not set. Run /api new and put the result in settings.")
                    return@thenArgumentExecute
                }
                NotEnoughUpdates.INSTANCE.openGui = CustomAHGui()
                NotEnoughUpdates.INSTANCE.manager.auctionManager.customAH.lastOpen = System.currentTimeMillis()
                NotEnoughUpdates.INSTANCE.manager.auctionManager.customAH.clearSearch()
                NotEnoughUpdates.INSTANCE.manager.auctionManager.customAH.updateSearch()

                val search = this[search]

                NotEnoughUpdates.INSTANCE.manager.auctionManager.customAH.setSearch(
                    if (search.isBlank() && NotEnoughUpdates.INSTANCE.config.neuAuctionHouse.saveLastSearch)
                        null else search
                )
            }
        }
        hook.beforeCommand = Predicate {
            if (!NotEnoughUpdates.INSTANCE.hasSkyblockScoreboard())
                Utils.addChatMessage("${RED}You must be on SkyBlock to use this feature.")
            NotEnoughUpdates.INSTANCE.hasSkyblockScoreboard()
        }
    }
}
