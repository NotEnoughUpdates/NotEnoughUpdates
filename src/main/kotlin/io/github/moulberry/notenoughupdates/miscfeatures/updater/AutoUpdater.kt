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

package io.github.moulberry.notenoughupdates.miscfeatures.updater

import io.github.moulberry.notenoughupdates.NotEnoughUpdates
import io.github.moulberry.notenoughupdates.autosubscribe.NEUAutoSubscribe
import io.github.moulberry.notenoughupdates.events.RegisterBrigadierCommandEvent
import io.github.moulberry.notenoughupdates.util.brigadier.reply
import io.github.moulberry.notenoughupdates.util.brigadier.thenExecute
import moe.nea.libautoupdate.CurrentVersion
import moe.nea.libautoupdate.UpdateContext
import moe.nea.libautoupdate.UpdateTarget
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.concurrent.CompletableFuture

@NEUAutoSubscribe
object AutoUpdater {
    val updateContext = UpdateContext(
        SigningGithubSource("NotEnoughUpdates", "NotEnoughUpdates"),
        UpdateTarget.deleteAndSaveInTheSameFolder(AutoUpdater::class.java),
        CurrentVersion.ofTag(NotEnoughUpdates.VERSION.substringBefore("+")),
        "notenoughupdates"
    )

    init {
        updateContext.cleanup()
    }

    @SubscribeEvent
    fun testCommand(event: RegisterBrigadierCommandEvent) {
        event.command("testneuupdate") {
            thenExecute {
                updateContext.checkUpdate("pre")
                    .thenCompose {
                        if (it.isUpdateAvailable)
                            it.launchUpdate().thenApply { true }
                        else
                            CompletableFuture.completedFuture(false)
                    }
                    .thenAccept {
                        if (it) {
                            reply("Updated!")
                        } else {
                            reply("No updated :(")
                        }
                    }.handle { t, u ->
                        u?.printStackTrace()
                    }
            }
        }
    }


}
