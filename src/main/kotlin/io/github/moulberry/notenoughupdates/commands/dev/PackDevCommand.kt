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

import com.mojang.brigadier.arguments.DoubleArgumentType.doubleArg
import com.mojang.brigadier.builder.ArgumentBuilder
import io.github.moulberry.notenoughupdates.NotEnoughUpdates
import io.github.moulberry.notenoughupdates.autosubscribe.NEUAutoSubscribe
import io.github.moulberry.notenoughupdates.core.util.MiscUtils
import io.github.moulberry.notenoughupdates.events.RegisterBrigadierCommandEvent
import io.github.moulberry.notenoughupdates.util.brigadier.*
import net.minecraft.client.Minecraft
import net.minecraft.client.entity.AbstractClientPlayer
import net.minecraft.command.ICommandSender
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLiving
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumChatFormatting
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@NEUAutoSubscribe
class PackDevCommand {

    fun <T : EntityLivingBase, U : ArgumentBuilder<ICommandSender, U>> U.npcListCommand(
        name: String,
        singleCommand: String,
        multipleCommand: String,
        clazz: Class<T>,
        provider: () -> List<Entity>
    ) {
        fun getEntities(distance: Double): List<T> {
            val distanceSquared = distance * distance
            val thePlayer = Minecraft.getMinecraft().thePlayer
            return provider()
                .asSequence()
                .filterIsInstance(clazz)
                .filter { it != thePlayer }
                .filter { it.getDistanceSqToEntity(thePlayer) < distanceSquared }
                .toList()
        }

        thenLiteral(singleCommand) {
            thenArgumentExecute("distance", doubleArg(0.0)) { dist ->
                val dist = this[dist]
                val entity = getEntities(dist).minByOrNull { it.getDistanceSqToEntity(Minecraft.getMinecraft().thePlayer) }
                if (entity == null) {
                    reply("No $name found within $dist blocks")
                    return@thenArgumentExecute
                }
                MiscUtils.copyToClipboard(StringBuilder().also { it.appendEntityData(entity) }.toString().trim())
                reply("Copied data to clipboard")
            }.withHelp("Find the nearest $name and copy data about them to your clipboard")
        }
        thenLiteral(multipleCommand) {
            thenArgumentExecute("distance", doubleArg(0.0)) { dist ->
                val dist = this[dist]
                val entity = getEntities(dist)
                val sb = StringBuilder()
                reply("Found ${entity.size} ${name}s")
                if (entity.isNotEmpty()) {
                    entity.forEach {
                        sb.appendEntityData(it)
                    }
                    MiscUtils.copyToClipboard(sb.toString().trim())

                    reply("Copied data to clipboard")
                }
            }.withHelp("Find all $name within range and copy data about them to your clipboard")
        }
    }

    fun StringBuilder.appendEntityData(entity: EntityLivingBase) {
        if (entity is EntityPlayer) {
            append("Player UUID: ")
            appendLine(entity.uniqueID)
            if (entity is AbstractClientPlayer) {
                append("Entity Texture Id: ")
                appendLine(entity.locationSkin.resourcePath?.replace("skins/", ""))
            }
        }
        append("Custom Name Tag: ")
        appendLine(entity.customNameTag ?: "null")
        append("Mob: ")
        appendLine(entity.name)
        append("Entity Id: ")
        appendLine(entity.entityId)

        appendItemData("Item", entity.heldItem)

        for ((slot, name) in listOf("Boots", "Leggings", "Chestplate", "Helmet").withIndex()) {
            val armorPiece = entity.getCurrentArmor(slot)
            appendItemData(name, armorPiece)
        }
        appendLine()
        appendLine()
    }

    fun StringBuilder.appendItemData(name: String, item: ItemStack?) {
        append("$name: ")
        if (item != null) {
            appendLine(item)
            append("$name Display Name")
            appendLine(item.displayName)
            append("$name Tag Compound: ")
            val compound = item.tagCompound
            if (compound == null) {
                appendLine("null")
            } else {
                appendLine(compound)
                append("$name Tag Compound Extra Attributes")
                appendLine(compound.getTag("ExtraAttributes"))
            }
        } else {
            appendLine("null")
        }

    }


    @SubscribeEvent
    fun onCommands(event: RegisterBrigadierCommandEvent) {
        event.command("neupackdev") {
            npcListCommand("Player", "getplayer", "getplayers", AbstractClientPlayer::class.java) {
                Minecraft.getMinecraft().theWorld.playerEntities
            }
            npcListCommand("NPC", "getnpc", "getnpcs", AbstractClientPlayer::class.java) {
                Minecraft.getMinecraft().theWorld.playerEntities.filter { it.uniqueID?.version() != 4 }
            }
            npcListCommand("mob", "getmob", "getmobs", EntityLiving::class.java) {
                Minecraft.getMinecraft().theWorld.loadedEntityList
            }
            npcListCommand("armor stand", "getarmorstand", "getarmorstands", EntityArmorStand::class.java) {
                Minecraft.getMinecraft().theWorld.loadedEntityList
            }
            thenExecute {
                NotEnoughUpdates.INSTANCE.packDevEnabled = !NotEnoughUpdates.INSTANCE.packDevEnabled
                if (NotEnoughUpdates.INSTANCE.packDevEnabled) {
                    reply("${EnumChatFormatting.GREEN}Enabled pack developer mode.")
                } else {
                    reply("${EnumChatFormatting.RED}Disabled pack developer mode.")
                }
            }
        }.withHelp("Toggle pack developer mode")
    }
}
