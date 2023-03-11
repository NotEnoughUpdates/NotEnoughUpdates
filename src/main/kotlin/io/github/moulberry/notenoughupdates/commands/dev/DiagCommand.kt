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

import com.mojang.brigadier.arguments.BoolArgumentType.bool
import io.github.moulberry.notenoughupdates.NotEnoughUpdates
import io.github.moulberry.notenoughupdates.autosubscribe.NEUAutoSubscribe
import io.github.moulberry.notenoughupdates.events.RegisterBrigadierCommandEvent
import io.github.moulberry.notenoughupdates.miscfeatures.CrystalMetalDetectorSolver
import io.github.moulberry.notenoughupdates.miscfeatures.CrystalWishingCompassSolver
import io.github.moulberry.notenoughupdates.options.customtypes.NEUDebugFlag
import io.github.moulberry.notenoughupdates.util.brigadier.*
import io.github.moulberry.notenoughupdates.util.brigadier.EnumArgumentType.Companion.enum
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

// Why is this not merged into /neudevtest
@NEUAutoSubscribe
class DiagCommand {
    @SubscribeEvent
    fun onCommands(event: RegisterBrigadierCommandEvent) {
        event.command("neudiag") {
            thenLiteral("metal") {
                thenLiteral("center") {
                    thenArgumentExecute("usecenter", bool()) { useCenter ->
                        CrystalMetalDetectorSolver.setDebugDoNotUseCenter(this[useCenter])
                        reply("Center coordinates-based solutions ${if (this[useCenter]) "enabled" else "disabled"}")
                    }.withHelp("Toggle coordinate based solutions")
                }
                thenExecute {
                    CrystalMetalDetectorSolver.logDiagnosticData(true)
                    reply("Enabled metal detector diagnostic logging.")
                }
            }.withHelp("Enable metal detector diagnostics")
            thenLiteralExecute("wishing") {
                CrystalWishingCompassSolver.getInstance().logDiagnosticData(true)
                reply("Enabled wishing compass diagnostic logging")
            }.withHelp("Enable wishing compass diagnostic logging")
            thenLiteral("debug") {
                thenLiteralExecute("list") {
                    reply("Here are all flags:\n${NEUDebugFlag.getFlagList()}")
                }.withHelp("List all debug diagnostic logging flags")
                thenLiteral("setflag") {
                    thenArgument("flag", enum<NEUDebugFlag>()) { flag ->
                        thenArgumentExecute("enable", bool()) { enable ->
                            val debugFlags = NotEnoughUpdates.INSTANCE.config.hidden.debugFlags
                            if (this[enable]) {
                                debugFlags.add(this[flag])
                            } else {
                                debugFlags.remove(this[flag])
                            }
                            reply("${if(this[enable]) "Enabled" else "Disabled"} the flag ${this[flag]}.")
                        }.withHelp("Enable or disable a diagnostic logging stream")
                    }
                }
                thenExecute {
                    reply("Effective debug flags: \n${NEUDebugFlag.getEnabledFlags()}")
                }
            }.withHelp("Log diagnostic data.")
        }
    }
}
