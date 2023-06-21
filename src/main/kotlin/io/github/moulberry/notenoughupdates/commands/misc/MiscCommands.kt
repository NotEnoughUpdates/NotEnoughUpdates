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

import com.mojang.brigadier.arguments.StringArgumentType.string
import io.github.moulberry.notenoughupdates.NEUManager
import io.github.moulberry.notenoughupdates.NotEnoughUpdates
import io.github.moulberry.notenoughupdates.autosubscribe.NEUAutoSubscribe
import io.github.moulberry.notenoughupdates.cosmetics.GuiCosmetics
import io.github.moulberry.notenoughupdates.events.RegisterBrigadierCommandEvent
import io.github.moulberry.notenoughupdates.miscgui.CalendarOverlay
import io.github.moulberry.notenoughupdates.miscgui.DynamicLightItemsEditor
import io.github.moulberry.notenoughupdates.miscgui.GuiItemCustomize
import io.github.moulberry.notenoughupdates.miscgui.NeuSearchCalculator
import io.github.moulberry.notenoughupdates.util.Calculator
import io.github.moulberry.notenoughupdates.util.Calculator.CalculatorException
import io.github.moulberry.notenoughupdates.util.MinecraftExecutor
import io.github.moulberry.notenoughupdates.util.PronounDB
import io.github.moulberry.notenoughupdates.util.Utils
import io.github.moulberry.notenoughupdates.util.brigadier.*
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.OpenGlHelper
import net.minecraft.util.ChatComponentText
import net.minecraft.util.EnumChatFormatting.*
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.text.DecimalFormat
import java.util.*
import java.util.concurrent.CompletableFuture

@NEUAutoSubscribe
class MiscCommands {
    @SubscribeEvent
    fun onCommands(event: RegisterBrigadierCommandEvent) {
        event.command("neucalc", "neucalculator") {
            thenArgumentExecute("calculation", RestArgumentType) { calculation ->
                val calculation = this[calculation]
                try {
                    val calculate = Calculator.calculate(calculation, NeuSearchCalculator.PROVIDE_LOWEST_BIN)
                    val formatter = DecimalFormat("#,##0.##")
                    val formatted = formatter.format(calculate)
                    reply("$WHITE$calculation $YELLOW= $GREEN$formatted")
                } catch (e: CalculatorException) {
                    reply(
                        "${RED}Error during calculation: ${e.message}\n${WHITE}${calculation.substring(0, e.offset)}" +
                                "${DARK_RED}${calculation.substring(e.offset, e.length + e.offset)}${GRAY}" +
                                calculation.substring(e.length + e.offset)
                    )
                }
            }.withHelp("Calculate an expression")
            thenExecute {
                reply(
                    "§5It's a calculator.\n" +
                            "§eFor Example §b/neucalc 3m*7k§e.\n" +
                            "§eYou can also use suffixes (k, m, b, t, s)§e.\n" +
                            "§eThe \"s\" suffix acts as 64.\n" +
                            "§eTurn on Sign Calculator in /neu misc to also support this in sign popups and the neu search bar."
                )
            }
        }.withHelp("Display help for NEUs calculator")
        event.command("neucalendar") {
            thenExecute {
                Minecraft.getMinecraft().thePlayer.closeScreen()
                CalendarOverlay.setEnabled(true)
                NotEnoughUpdates.INSTANCE.sendChatMessage("/calendar")
            }
        }.withHelp("Display NEUs custom calendar overlay")
        event.command("neucosmetics") {
            thenExecute {
                if (!OpenGlHelper.isFramebufferEnabled() && NotEnoughUpdates.INSTANCE.config.notifications.doFastRenderNotif) {
                    reply(
                        "${RED}NEU Cosmetics do not work with OptiFine Fast Render. Go to ESC > Options > Video Settings > Performance > Fast Render to disable it."
                    )
                }
                NotEnoughUpdates.INSTANCE.openGui = GuiCosmetics()
            }
        }.withHelp("Equip NEU cosmetics")
        event.command("neucustomize", "neurename") {
            thenExecute {
                val held = Minecraft.getMinecraft().thePlayer.heldItem
                if (held == null) {
                    reply("${RED}You can't customize your hand...")
                    return@thenExecute
                }
                val heldUUID = NEUManager.getUUIDForItem(held)
                if (heldUUID == null) {
                    reply("${RED}This item does not have an UUID, so it cannot be customized.")
                    return@thenExecute
                }

                NotEnoughUpdates.INSTANCE.openGui = GuiItemCustomize(held, heldUUID)
            }
        }.withHelp("Customize your items")
        event.command("neupronouns", "neuliberals") {
            thenArgument("user", string()) {user->
                suggestsList { Minecraft.getMinecraft().theWorld.playerEntities.map { it.name } }
                thenArgumentExecute("platform", string()) { platform ->
                    fetchPronouns(this[platform], this[user])
                }.withHelp("Look up someones pronouns using their username on a platform")
                thenExecute {
                    fetchPronouns("minecraft", this[user])
                }
            }.withHelp("Look up someones pronouns using their minecraft username")
        }
        event.command("neuupdate", "neuupdates", "enoughupdates") {
            thenLiteralExecute("check") {
                NotEnoughUpdates.INSTANCE.autoUpdater.displayUpdateMessageIfOutOfDate()
            }.withHelp("Check for updates")
            thenLiteralExecute("scheduledownload") {
                NotEnoughUpdates.INSTANCE.autoUpdater.scheduleDownload()
            }.withHelp("Queue a new version of NEU to be downloaded")
            thenLiteralExecute("updatemodes") {
                reply("§bTo ensure we do not accidentally corrupt your mod folder, we can only offer support for auto-updates on system with certain capabilities for file deletions (specifically unix systems). You can still manually update your files")
            }.withHelp("Display an explanation why you cannot auto update")
        }
        event.command("neudynamiclights", "neudli", "neudynlights", "neudynamicitems") {
            thenExecute {
                NotEnoughUpdates.INSTANCE.openGui = DynamicLightItemsEditor()
            }
        }.withHelp("Add items to dynamically emit light")
    }

    fun fetchPronouns(platform: String, user: String) {
        val nc = Minecraft.getMinecraft().ingameGUI.chatGUI
        val id = Random().nextInt()
        nc.printChatMessageWithOptionalDeletion(ChatComponentText("§e[NEU] Fetching Pronouns..."), id)

        val pronouns = if ("minecraft" == platform) {
            val c = CompletableFuture<UUID>()
            NotEnoughUpdates.profileViewer.getPlayerUUID(user) { uuidString ->
                if (uuidString == null) {
                    c.completeExceptionally(NullPointerException())
                } else {
                    c.complete(Utils.parseDashlessUUID(uuidString))
                }
            }
            c.thenCompose { minecraftPlayer ->
                PronounDB.getPronounsFor(minecraftPlayer)
            }
        } else {
            PronounDB.getPronounsFor(platform, user)
        }
        pronouns.handleAsync({ pronounChoice, throwable ->
            if (throwable != null || !pronounChoice.isPresent) {
                nc.printChatMessageWithOptionalDeletion(ChatComponentText("§e[NEU] §4Failed to fetch pronouns."), id)
                return@handleAsync null
            }
            val betterPronounChoice = pronounChoice.get()
            nc.printChatMessageWithOptionalDeletion(
                ChatComponentText("§e[NEU] Pronouns for §b$user §eon §b$platform§e:"), id
            )
            betterPronounChoice.render().forEach {
                nc.printChatMessage(ChatComponentText("§e[NEU] §a$it"))
            }
            null
        }, MinecraftExecutor.OffThread)

    }

}
