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

import com.google.gson.JsonObject
import com.mojang.brigadier.arguments.StringArgumentType.string
import io.github.moulberry.notenoughupdates.NotEnoughUpdates
import io.github.moulberry.notenoughupdates.autosubscribe.NEUAutoSubscribe
import io.github.moulberry.notenoughupdates.dungeons.GuiDungeonMapEditor
import io.github.moulberry.notenoughupdates.events.RegisterBrigadierCommandEvent
import io.github.moulberry.notenoughupdates.util.brigadier.*
import net.minecraft.block.material.MapColor
import net.minecraft.client.Minecraft
import net.minecraft.item.ItemMap
import net.minecraft.util.EnumChatFormatting.GREEN
import net.minecraft.util.EnumChatFormatting.RED
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color

@NEUAutoSubscribe
class DungeonCommands {
    @SubscribeEvent
    fun onCommands(event: RegisterBrigadierCommandEvent) {
        event.command("dh") {
            thenExecute {
                Minecraft.getMinecraft().thePlayer.sendChatMessage("/warp dungeon_hub")
            }
        }.withHelp("Warps to the dungeon hub")
        event.command("dn") {
            thenExecute {
                Minecraft.getMinecraft().thePlayer.sendChatMessage("/warp dungeon_hub")
                reply("Warping to...")
                reply("Deez nuts lmao")
            }
        }.withHelp("Warps to the dungeon nuts")
        event.command("neumap") {
            thenLiteral("reset") {
                thenExecute {
                    NotEnoughUpdates.INSTANCE.colourMap = null
                    reply("Reset color map")
                }
                requiresDev()
            }.withHelp("Reset the colour map")
            thenLiteral("save") {
                thenArgument("filename", string()) { fileName ->
                    requiresDev()
                    thenExecute {
                        val stack = Minecraft.getMinecraft().thePlayer.heldItem
                        if (stack == null || stack.item !is ItemMap) {
                            reply("Please hold a map item")
                            return@thenExecute
                        }
                        val map = stack.item as ItemMap
                        val mapData = map.getMapData(stack, Minecraft.getMinecraft().theWorld)
                        if (mapData == null) {
                            reply("Could not grab map data (empty map)")
                            return@thenExecute
                        }
                        val json = JsonObject()
                        for (i in 0 until (128 * 128)) {
                            val x = i % 128
                            val y = i / 128
                            val j = mapData.colors[i].toInt() and 255
                            val c = if (j / 4 == 0) {
                                Color((i + i / 128 and 1) * 8 + 16 shl 24, true)
                            } else {
                                Color(MapColor.mapColorArray[j / 4].getMapColor(j and 3), true)
                            }
                            json.addProperty("$x:$y", c.rgb)
                        }
                        try {
                            NotEnoughUpdates.INSTANCE.manager.configLocation.resolve("maps").mkdirs()
                            NotEnoughUpdates.INSTANCE.manager.writeJson(
                                json,
                                NotEnoughUpdates.INSTANCE.manager.configLocation.resolve("maps/${this[fileName]}.json")
                            )
                            reply("${GREEN}Saved to file.")
                        } catch (e: Exception) {
                            e.printStackTrace()
                            reply("${RED}Failed to save.")
                        }
                    }
                }.withHelp("Save a colour map from an item")
            }
            thenLiteral("load") {
                thenArgument("filename", string()) { fileName ->
                    requiresDev()
                    thenExecute {
                        val json = NotEnoughUpdates.INSTANCE.manager.getJsonFromFile(
                            NotEnoughUpdates.INSTANCE.manager.configLocation.resolve(
                                "maps/${this[fileName]}.json"
                            )
                        )
                        NotEnoughUpdates.INSTANCE.colourMap = (0 until 128).map { x ->
                            (0 until 128).map { y ->
                                val key = "$x:$y"
                                json[key]?.asInt?.let { Color(it, true) } ?: Color(0, 0, 0, 0)
                            }.toTypedArray()
                        }.toTypedArray()
                        for (x in 0..127) {
                            for (y in 0..127) {
                                NotEnoughUpdates.INSTANCE.colourMap[x][y] = Color(0, 0, 0, 0)
                            }
                        }
                        reply("Loaded colour map from file")
                    }
                }.withHelp("Load a colour map from a file")
            }
            thenExecute {
                NotEnoughUpdates.INSTANCE.openGui = GuiDungeonMapEditor(null)
            }
        }.withHelp("Open the dungeon map editor")
    }
}
