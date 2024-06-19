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
package io.github.moulberry.notenoughupdates.profileviewer.persistent

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import io.github.moulberry.notenoughupdates.NotEnoughUpdates
import io.github.moulberry.notenoughupdates.autosubscribe.NEUAutoSubscribe
import io.github.moulberry.notenoughupdates.core.config.ConfigUtil
import io.github.moulberry.notenoughupdates.events.RegisterBrigadierCommandEvent
import io.github.moulberry.notenoughupdates.util.brigadier.thenExecute
import io.github.moulberry.notenoughupdates.util.kotlin.set
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.io.File

@NEUAutoSubscribe
object RecentPVSearches {
    var recentSearches: ArrayList<RecentSearch> = ArrayList()

    private fun getFile() = File(NotEnoughUpdates.INSTANCE.neuDir, "recent_pv_searches.json")

    fun loadRecentSearches() {
        if (!getFile().exists()) {
            return
        }
        val listType = object : TypeToken<ArrayList<JsonObject>?>() {}.type

        val rawSearches: ArrayList<JsonObject> =
            ConfigUtil.loadConfigWithTypeToken(listType, getFile(), Gson()) ?: return

        recentSearches.clear()
        for (recentSearch in rawSearches) {
            val name = recentSearch["playername"].asString ?: continue
            val stackObject = recentSearch["itemstack"].asJsonObject ?: continue
            val stack = NotEnoughUpdates.INSTANCE.manager.jsonToStack(stackObject, false, false)
            recentSearches.add(RecentSearch(name, stack))
        }
    }

    fun saveRecentSearches() {
        val rawSearches: ArrayList<JsonObject> = ArrayList()
        for (recentSearch in recentSearches) {
            // If the actual skull was never resolved, don't store it
            if (recentSearch.playerHead == null) {
                continue
            }
            val jsonObject = JsonObject()
            jsonObject["playername"] = recentSearch.playername
            val stackObject = NotEnoughUpdates.INSTANCE.manager.getJsonForItem(recentSearch.playerHead)
            stackObject["internalname"] = "SKULL_ITEM"
            jsonObject["itemstack"] = stackObject
            rawSearches.add(jsonObject)
        }

        ConfigUtil.saveConfig(rawSearches, getFile(), Gson())
    }

    @SubscribeEvent
    fun onCommands(event: RegisterBrigadierCommandEvent) {
        event.command("neutestrecentsearchsaving") {
            thenExecute {
                saveRecentSearches()
            }
        }

        event.command("neutestrecentsearchreading") {
            thenExecute {
                loadRecentSearches()
            }
        }
    }

    data class RecentSearch(val playername: String, val playerHead: ItemStack?)
}
