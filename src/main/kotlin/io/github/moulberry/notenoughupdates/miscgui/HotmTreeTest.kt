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

package io.github.moulberry.notenoughupdates.miscgui

import com.google.gson.GsonBuilder
import io.github.moulberry.notenoughupdates.NotEnoughUpdates
import io.github.moulberry.notenoughupdates.autosubscribe.NEUAutoSubscribe
import io.github.moulberry.notenoughupdates.events.RegisterBrigadierCommandEvent
import io.github.moulberry.notenoughupdates.util.brigadier.thenExecute
import io.github.moulberry.notenoughupdates.util.kotlin.KotlinTypeAdapterFactory
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@NEUAutoSubscribe
class HotmTreeTest {
    val gson = GsonBuilder()
        .registerTypeAdapterFactory(KotlinTypeAdapterFactory)
        .registerTypeAdapterFactory(LoreLineSerializer)
        .registerTypeAdapterFactory(LispProgramSerializer)
        .create()

    @SubscribeEvent
    fun onCommands(event: RegisterBrigadierCommandEvent) {
        event.command("neutesthotmtree") {
            thenExecute {
                val hotmLayoutFile = NotEnoughUpdates.INSTANCE.manager.repoLocation
                    .resolve("constants/hotmlayout.json")
                val hotmLayout = gson.fromJson(hotmLayoutFile.readText(), HotmTreeLayoutFile::class.java)
                NotEnoughUpdates.INSTANCE.openGui = HotmTreeScreen(hotmLayout.hotm, hotmLayout.prelude)
            }
        }
    }
}
