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

package io.github.moulberry.notenoughupdates.util.brigadier

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.ParseResults
import com.mojang.brigadier.arguments.StringArgumentType.string
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.mojang.brigadier.suggestion.Suggestions
import io.github.moulberry.notenoughupdates.BuildFlags
import io.github.moulberry.notenoughupdates.NotEnoughUpdates
import io.github.moulberry.notenoughupdates.autosubscribe.NEUAutoSubscribe
import io.github.moulberry.notenoughupdates.commands.dev.DevTestCommand.DEV_TESTERS
import io.github.moulberry.notenoughupdates.core.config.GuiPositionEditor
import io.github.moulberry.notenoughupdates.core.util.MiscUtils
import io.github.moulberry.notenoughupdates.miscfeatures.FishingHelper
import io.github.moulberry.notenoughupdates.miscfeatures.customblockzones.CustomBiomes
import io.github.moulberry.notenoughupdates.miscfeatures.customblockzones.LocationChangeEvent
import io.github.moulberry.notenoughupdates.miscgui.GuiPriceGraph
import io.github.moulberry.notenoughupdates.miscgui.minionhelper.MinionHelperManager
import io.github.moulberry.notenoughupdates.util.LRUCache
import io.github.moulberry.notenoughupdates.util.PronounDB
import io.github.moulberry.notenoughupdates.util.SBInfo
import io.github.moulberry.notenoughupdates.util.TabListUtils
import io.github.moulberry.notenoughupdates.util.brigadier.EnumArgumentType.Companion.enum
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiScreen
import net.minecraft.command.CommandBase
import net.minecraft.command.ICommandSender
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.launchwrapper.Launch
import net.minecraft.util.BlockPos
import net.minecraft.util.ChatComponentText
import net.minecraft.util.EnumChatFormatting
import net.minecraft.util.EnumChatFormatting.GOLD
import net.minecraft.util.EnumChatFormatting.RED
import net.minecraft.util.EnumParticleTypes
import net.minecraftforge.client.ClientCommandHandler
import net.minecraftforge.common.MinecraftForge
import java.util.concurrent.CompletableFuture

@NEUAutoSubscribe
object Rome {
    val dispatcher = CommandDispatcher<DefaultSource>()
    private val parseText =
        LRUCache.memoize<Pair<ICommandSender, String>, ParseResults<DefaultSource>>({ (sender, text) ->
            dispatcher.parse(text, sender)
        }, 1)

    init {
        dispatcher.register(literal("neudevtest2") {
            requires {
                DEV_TESTERS.contains((it as? EntityPlayer)?.uniqueID?.toString())
                        || Launch.blackboard.get("fml.deobfuscatedEnvironment") as Boolean
            }
            thenLiteralExecute("profileinfo") {
                val currentProfile = SBInfo.getInstance().currentProfile
                val gamemode = SBInfo.getInstance().getGamemodeForProfile(currentProfile)
                reply("${GOLD}You are on Profile $currentProfile with the mode $gamemode")
            }
            thenLiteralExecute("buildflags") {
                reply("BuildFlags: \n" +
                        BuildFlags.getAllFlags().entries
                            .joinToString(("\n")) { (key, value) -> " + $key - $value" })
            }
            thenLiteral("exteditor") {
                thenExecute {
                    reply("Your external editor is: §Z${NotEnoughUpdates.INSTANCE.config.hidden.externalEditor}")
                }
                thenArgument("editor", string()) { newEditor ->
                    thenExecute {
                        NotEnoughUpdates.INSTANCE.config.hidden.externalEditor = this[newEditor]
                        reply("You changed your external editor to: §Z${this[newEditor]}")
                    }
                }
            }
            thenLiteral("pricetest") {
                thenExecute {
                    NotEnoughUpdates.INSTANCE.manager.auctionManager.updateBazaar()
                }
                thenArgument("item", string()) { item ->
                    thenExecute {
                        NotEnoughUpdates.INSTANCE.openGui = GuiPriceGraph(this[item])
                    }
                }
            }
            thenLiteralExecute("zone") {
                val target = Minecraft.getMinecraft().objectMouseOver.blockPos
                    ?: Minecraft.getMinecraft().thePlayer.position
                val zone = CustomBiomes.INSTANCE.getSpecialZone(target)
                listOf(
                    ChatComponentText("Showing Zone Info for: $target"),
                    ChatComponentText("Zone: " + (zone?.name ?: "null")),
                    ChatComponentText("Location: " + SBInfo.getInstance().getLocation()),
                    ChatComponentText("Biome: " + CustomBiomes.INSTANCE.getCustomBiome(target))
                ).forEach { component ->
                    reply(component)
                }
                MinecraftForge.EVENT_BUS.post(
                    LocationChangeEvent(
                        SBInfo.getInstance().getLocation(), SBInfo
                            .getInstance()
                            .getLocation()
                    )
                )
            }
            thenLiteralExecute("positiontest") {
                NotEnoughUpdates.INSTANCE.openGui = GuiPositionEditor()
            }
            thenLiteral("pt") {
                thenArgument("particle", enum<EnumParticleTypes>()) { particle ->
                    thenExecute {
                        FishingHelper.type = this[particle]
                        reply("Fishing particles set to ${FishingHelper.type}")
                    }
                }
            }
            thenLiteralExecute("dev") {
                NotEnoughUpdates.INSTANCE.config.hidden.dev = !NotEnoughUpdates.INSTANCE.config.hidden.dev
                reply("§e[NEU] Dev mode " + if (NotEnoughUpdates.INSTANCE.config.hidden.dev) "§aenabled" else "§cdisabled")
            }
            thenLiteralExecute("saveconfig") {
                NotEnoughUpdates.INSTANCE.saveConfig()
                reply("Config saved")
            }
            thenLiteralExecute("searchmode") {
                NotEnoughUpdates.INSTANCE.config.hidden.firstTimeSearchFocus = true
                reply(EnumChatFormatting.AQUA.toString() + "I would never search")
            }
            thenLiteralExecute("bluehair") {
                PronounDB.test()
            }
            thenLiteral("opengui") {
                thenArgument("class", string()) { className ->
                    thenExecute {
                        try {
                            NotEnoughUpdates.INSTANCE.openGui =
                                Class.forName(this[className]).newInstance() as GuiScreen
                            reply("Opening gui: " + NotEnoughUpdates.INSTANCE.openGui)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            reply("Failed to open this GUI.")
                        }
                    }
                }
            }
            thenLiteralExecute("center") {
                val x = Math.floor(Minecraft.getMinecraft().thePlayer.posX) + 0.5f
                val z = Math.floor(Minecraft.getMinecraft().thePlayer.posZ) + 0.5f
                Minecraft.getMinecraft().thePlayer.setPosition(x, Minecraft.getMinecraft().thePlayer.posY, z)
                reply("Literal hacks")
            }
            thenLiteral("minion") {
                thenArgument("args", RestArgumentType) { arg ->
                    thenExecute {
                        MinionHelperManager.getInstance().handleCommand(arrayOf("minion") + this[arg].split(" "))
                    }
                }
            }
            thenLiteralExecute("copytablist") {
                val tabList = TabListUtils.getTabList().joinToString("\n", postfix = "\n")
                MiscUtils.copyToClipboard(tabList)
                reply("Copied tablist to clipboard!")
            }
        })
        updateHooks()
    }

    fun updateHooks() = registerHooks(ClientCommandHandler.instance)

    fun registerHooks(handler: ClientCommandHandler) {
        dispatcher.root.children.forEach { commandNode ->
            if (commandNode.name in handler.commands) return@forEach
            handler.registerCommand(object : CommandBase() {
                override fun getCommandName(): String {
                    return commandNode.name
                }

                override fun getCommandUsage(sender: ICommandSender): String {
                    return dispatcher.getAllUsage(commandNode, sender, true).joinToString("\n")
                }

                fun getText(args: Array<out String>) = "${commandNode.name} ${args.joinToString(" ")}"

                override fun processCommand(sender: ICommandSender, args: Array<out String>) {
                    val results = parseText.apply(sender to getText(args).trim())
                    try {
                        dispatcher.execute(results)
                    } catch (syntax: CommandSyntaxException) {
                        sender.addChatMessage(ChatComponentText("${RED}${syntax.message}"))
                    }
                }

                var lastCompletionText: String? = null
                var lastCompletion: CompletableFuture<Suggestions>? = null

                override fun addTabCompletionOptions(
                    sender: ICommandSender,
                    args: Array<out String>,
                    pos: BlockPos
                ): List<String> {
                    val originalText = getText(args)
                    var lc: CompletableFuture<Suggestions>? = null
                    if (lastCompletionText == originalText) {
                        lc = lastCompletion
                    }
                    if (lc == null) {
                        lastCompletion?.cancel(true)
                        val results = parseText.apply(sender to originalText)
                        lc = dispatcher.getCompletionSuggestions(results)
                    }
                    lastCompletion = lc
                    lastCompletionText = originalText
                    val suggestions = lastCompletion?.getNow(null) ?: return emptyList()
                    return suggestions.list.map { it.text }
                }

                override fun canCommandSenderUseCommand(sender: ICommandSender): Boolean {
                    return true
                }
            })
        }
    }
}
