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

import com.mojang.brigadier.arguments.StringArgumentType
import io.github.moulberry.notenoughupdates.BuildFlags
import io.github.moulberry.notenoughupdates.NotEnoughUpdates
import io.github.moulberry.notenoughupdates.autosubscribe.NEUAutoSubscribe
import io.github.moulberry.notenoughupdates.core.util.MiscUtils
import io.github.moulberry.notenoughupdates.events.RegisterBrigadierCommandEvent
import io.github.moulberry.notenoughupdates.miscfeatures.FishingHelper
import io.github.moulberry.notenoughupdates.miscfeatures.customblockzones.CustomBiomes
import io.github.moulberry.notenoughupdates.miscfeatures.customblockzones.LocationChangeEvent
import io.github.moulberry.notenoughupdates.miscgui.minionhelper.MinionHelperManager
import io.github.moulberry.notenoughupdates.miscgui.pricegraph.GuiPriceGraph
import io.github.moulberry.notenoughupdates.util.*
import io.github.moulberry.notenoughupdates.util.brigadier.*
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiScreen
import net.minecraft.command.ICommandSender
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.launchwrapper.Launch
import net.minecraft.util.ChatComponentText
import net.minecraft.util.EnumChatFormatting.*
import net.minecraft.util.EnumParticleTypes
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.function.Predicate
import kotlin.math.floor

@NEUAutoSubscribe
class DevTestCommand {
    companion object {
        val DEV_TESTERS: List<String> = mutableListOf(
            "d0e05de7-6067-454d-beae-c6d19d886191",  // moulberry
            "66502b40-6ac1-4d33-950d-3df110297aab",  // lucycoconut
            "a5761ff3-c710-4cab-b4f4-3e7f017a8dbf",  // ironm00n
            "5d5c548a-790c-4fc8-bd8f-d25b04857f44",  // ariyio
            "53924f1a-87e6-4709-8e53-f1c7d13dc239",  // throwpo
            "d3cb85e2-3075-48a1-b213-a9bfb62360c1",  // lrg89
            "0b4d470f-f2fb-4874-9334-1eaef8ba4804",  // dediamondpro
            "ebb28704-ed85-43a6-9e24-2fe9883df9c2",  // lulonaut
            "698e199d-6bd1-4b10-ab0c-52fedd1460dc",  // craftyoldminer
            "8a9f1841-48e9-48ed-b14f-76a124e6c9df",  // eisengolem
            "a7d6b3f1-8425-48e5-8acc-9a38ab9b86f7",  // whalker
            "0ce87d5a-fa5f-4619-ae78-872d9c5e07fe",  // ascynx
            "a049a538-4dd8-43f8-87d5-03f09d48b4dc",  // egirlefe
            "7a9dc802-d401-4d7d-93c0-8dd1bc98c70d",  // efefury
            "bb855349-dfd8-4125-a750-5fc2cf543ad5", // hannibal2
            "eaa5623c-8413-46b7-a74b-2d74a42b2841" // calmwolfs
        )
        val SPECIAL_KICK = "SPECIAL_KICK"

        val DEV_FAIL_STRINGS = arrayOf(
            "No.",
            "I said no.",
            "You aren't allowed to use this.",
            "Are you sure you want to use this? Type 'Yes' in chat.",
            "Are you sure you want to use this? Type 'Yes' in chat.",
            "Lmao you thought",
            "Ok please stop",
            "What do you want from me?",
            "This command almost certainly does nothing useful for you",
            "Ok, this is the last message, after this it will repeat",
            "No.",
            "I said no.",
            "Dammit. I thought that would work. Uhh...",
            "\u00a7dFrom \u00a7c[ADMIN] Minikloon\u00a77: If you use that command again, I'll have to ban you",
            SPECIAL_KICK,
            "Ok, this is actually the last message, use the command again and you'll crash I promise"
        )

        fun isDeveloper(commandSender: ICommandSender): Boolean {
            return DEV_TESTERS.contains((commandSender as? EntityPlayer)?.uniqueID?.toString())
                    || Launch.blackboard.get("fml.deobfuscatedEnvironment") as Boolean

        }
    }

    var devFailIndex = 0
    fun canPlayerExecute(commandSender: ICommandSender): Boolean {
        return isDeveloper(commandSender)
    }

    @SubscribeEvent
    fun onCommands(event: RegisterBrigadierCommandEvent) {
        val hook = event.command("neudevtest") {
            requires {
                canPlayerExecute(it)
            }
            thenLiteral("joinServer") {
                thenArgument("serverId", RestArgumentType) { serverId ->
                    thenExecute {
                        try {
                            MC.sessionService.joinServer(
                                MC.session.profile,
                                MC.session.token,
                                get(serverId)
                            )
                            reply("Joined server ${get(serverId)}")
                        } catch (e: Exception) {
                            e.printStackTrace()
                            reply("Failed to join server")
                        }
                    }
                }.withHelp("Send a joinServer request to mojang (to test authentication with the cape server)")
            }
            thenLiteral("testsearch") {
                thenArgument("name", RestArgumentType) { arg ->
                    thenExecute {
                        reply("Resolved ID: ${ItemResolutionQuery.findInternalNameByDisplayName(get(arg), true)}")
                    }
                }.withHelp("Search for an item id by name")
            }
            thenLiteralExecute("garden") {
                val player = Minecraft.getMinecraft().thePlayer
                reply("Is in Garden: ${SBInfo.getInstance().getLocation() == "garden"}")
                val pp = player.position
                reply("Plot X: ${floor((pp.getX() + 48) / 96F)}")
                reply("Plot Z: ${floor((pp.getZ() + 48) / 96F)}")
            }.withHelp("Show diagnostics information about the garden")
            thenLiteralExecute("profileinfo") {
                val currentProfile = SBInfo.getInstance().currentProfile
                val gamemode = SBInfo.getInstance().getGamemodeForProfile(currentProfile)
                reply("${GOLD}You are on Profile $currentProfile with the mode $gamemode")
            }.withHelp("Display information about your current profile")
            thenLiteralExecute("buildflags") {
                reply("BuildFlags: \n" +
                        BuildFlags.getAllFlags().entries
                            .joinToString(("\n")) { (key, value) -> " + $key - $value" })
            }.withHelp("List the flags with which NEU was built")
            thenLiteral("exteditor") {
                thenArgument("editor", StringArgumentType.string()) { newEditor ->
                    thenExecute {
                        NotEnoughUpdates.INSTANCE.config.hidden.externalEditor = this[newEditor]
                        reply("You changed your external editor to: §Z${this[newEditor]}")
                    }
                }.withHelp("Change the editor used to edit repo files")
                thenExecute {
                    reply("Your external editor is: §Z${NotEnoughUpdates.INSTANCE.config.hidden.externalEditor}")
                }
            }.withHelp("See your current external editor for repo files")
            thenLiteral("pricetest") {
                thenArgument("item", StringArgumentType.string()) { item ->
                    thenExecute {
                        NotEnoughUpdates.INSTANCE.openGui =
                            GuiPriceGraph(this[item])
                    }
                }.withHelp("Display the price graph for an item by id")
                thenExecute {
                    NotEnoughUpdates.INSTANCE.manager.auctionManager.updateBazaar()
                }
            }.withHelp("Update the price data from the bazaar")
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
            }.withHelp("Display information about the special block zone at your cursor (Custom Texture Regions)")
            thenLiteral("pt") {
                thenArgument("particle", EnumArgumentType.enum<EnumParticleTypes>()) { particle ->
                    thenExecute {
                        FishingHelper.type = this[particle]
                        reply("Fishing particles set to ${FishingHelper.type}")
                    }
                }
            }
            thenLiteral("callUrsa") {
                thenArgument("path", RestArgumentType) { path ->
                    thenExecute {
                        NotEnoughUpdates.INSTANCE.manager.ursaClient.getString(this[path])
                            .thenAccept {
                                reply(it.toString())
                            }
                    }
                }.withHelp("Send an authenticated request to the current ursa server")
            }
            thenLiteralExecute("dev") {
                NotEnoughUpdates.INSTANCE.config.hidden.dev = !NotEnoughUpdates.INSTANCE.config.hidden.dev
                reply("Dev mode " + if (NotEnoughUpdates.INSTANCE.config.hidden.dev) "§aenabled" else "§cdisabled")
            }.withHelp("Toggle developer mode")
            thenLiteralExecute("saveconfig") {
                NotEnoughUpdates.INSTANCE.saveConfig()
                reply("Config saved")
            }.withHelp("Force sync the config to disk")
            thenLiteralExecute("clearapicache") {
                ApiCache.clear()
                NotEnoughUpdates.INSTANCE.manager.ursaClient.clearToken()
                reply("Cleared API cache and reset ursa token")
            }.withHelp("Clear the API cache")
            thenLiteralExecute("searchmode") {
                NotEnoughUpdates.INSTANCE.config.hidden.firstTimeSearchFocus = true
                reply(AQUA.toString() + "I would never search")
            }.withHelp("Reset your search data to redisplay the search tutorial")
            thenLiteralExecute("bluehair") {
                PronounDB.test()
            }.withHelp("Test the pronoundb integration")
            thenLiteral("opengui") {
                thenArgumentExecute("class", StringArgumentType.string()) { className ->
                    try {
                        NotEnoughUpdates.INSTANCE.openGui =
                            Class.forName(this[className]).newInstance() as GuiScreen
                        reply("Opening gui: " + NotEnoughUpdates.INSTANCE.openGui)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        reply("Failed to open this GUI.")
                    }
                }.withHelp("Open a gui by class name")
            }
            thenLiteralExecute("center") {
                val x = floor(Minecraft.getMinecraft().thePlayer.posX) + 0.5f
                val z = floor(Minecraft.getMinecraft().thePlayer.posZ) + 0.5f
                Minecraft.getMinecraft().thePlayer.setPosition(x, Minecraft.getMinecraft().thePlayer.posY, z)
                reply("Literal hacks")
            }.withHelp("Center yourself on the block you are currently standing (like using AOTE)")
            thenLiteral("minion") {
                thenArgumentExecute("args", RestArgumentType) { arg ->
                    MinionHelperManager.getInstance().handleCommand(arrayOf("minion") + this[arg].split(" "))
                }.withHelp("Minion related commands. Not yet integrated in brigadier")
            }
            thenLiteralExecute("copytablist") {
                val tabList = TabListUtils.getTabList().joinToString("\n", postfix = "\n")
                MiscUtils.copyToClipboard(tabList)
                reply("Copied tablist to clipboard!")
            }.withHelp("Copy the tab list")
            thenLiteral("useragent") {
                thenArgumentExecute("newuseragent", RestArgumentType) { userAgent ->
                    reply("Setting your user agent to ${this[userAgent]}")
                    NotEnoughUpdates.INSTANCE.config.hidden.customUserAgent = this[userAgent]
                }.withHelp("Set a custom user agent for all HTTP requests")
                thenExecute {
                    reply("Resetting your user agent.")
                    NotEnoughUpdates.INSTANCE.config.hidden.customUserAgent = null
                }
            }.withHelp("Reset the custom user agent")
        }
        hook.beforeCommand = Predicate {
            if (!canPlayerExecute(it.context.source)) {
                if (devFailIndex !in DEV_FAIL_STRINGS.indices) {
                    throw object : Error("L") {
                        @Override
                        fun printStackTrace() {
                            throw Error("L")
                        }
                    }
                }
                val text = DEV_FAIL_STRINGS[devFailIndex++]
                if (text == SPECIAL_KICK) {
                    val component = ChatComponentText("\u00a7cYou are permanently banned from this server!")
                    component.appendText("\n")
                    component.appendText("\n\u00a77Reason: \u00a7rI told you not to run the command - Moulberry")
                    component.appendText("\n\u00a77Find out more: \u00a7b\u00a7nhttps://www.hypixel.net/appeal")
                    component.appendText("\n")
                    component.appendText("\n\u00a77Ban ID: \u00a7r#49871982")
                    component.appendText("\n\u00a77Sharing your Ban ID may affect the processing of your appeal!")
                    Minecraft.getMinecraft().netHandler.networkManager.closeChannel(component)
                } else {
                    it.context.source.addChatMessage(ChatComponentText("$RED$text"))
                }
                false
            } else {
                true
            }
        }
    }

}
