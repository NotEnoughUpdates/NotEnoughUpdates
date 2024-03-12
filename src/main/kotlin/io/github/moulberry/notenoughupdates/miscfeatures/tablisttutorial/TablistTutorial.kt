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

package io.github.moulberry.notenoughupdates.miscfeatures.tablisttutorial

import com.mojang.brigadier.arguments.StringArgumentType.string
import io.github.moulberry.notenoughupdates.NotEnoughUpdates
import io.github.moulberry.notenoughupdates.autosubscribe.NEUAutoSubscribe
import io.github.moulberry.notenoughupdates.core.util.StringUtils
import io.github.moulberry.notenoughupdates.core.util.render.RenderUtils
import io.github.moulberry.notenoughupdates.events.RegisterBrigadierCommandEvent
import io.github.moulberry.notenoughupdates.mixins.AccessorGlStateManager
import io.github.moulberry.notenoughupdates.mixins.AccessorGuiContainer
import io.github.moulberry.notenoughupdates.util.ItemUtils
import io.github.moulberry.notenoughupdates.util.StateManagerUtils
import io.github.moulberry.notenoughupdates.util.brigadier.thenArgument
import io.github.moulberry.notenoughupdates.util.brigadier.thenExecute
import io.github.moulberry.notenoughupdates.util.brigadier.withHelp
import io.github.moulberry.notenoughupdates.util.stripControlCodes
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.inventory.ContainerChest
import net.minecraft.inventory.Slot
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.event.GuiScreenEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@NEUAutoSubscribe
object TablistTutorial {
    data class TabListWidget(
        val regionName: String,
        val widgetName: TablistAPI.WidgetNames,
    )

    private object Arrow {
        val imageLocation = ResourceLocation("notenoughupdates:textures/gui/tablisttutorial/arrow.png")
        val tipXOffset = 16
        val tipYOffset = 64
        val labelXOffset = 58
        val labelYOffset = 19
        val textureSize = 64
        val textScale = 2f

        fun drawBigRedArrow(x: Int, y: Int, label: String) {
            val imgX = x - Arrow.tipXOffset
            val imgY = y - Arrow.tipYOffset
            val textX = imgX + Arrow.labelXOffset
            val textY = imgY + Arrow.labelYOffset

            GlStateManager.pushMatrix()
            GlStateManager.translate(0f, 0f, 300f)
            GlStateManager.color(1f, 1f, 1f, 1f)
            Minecraft.getMinecraft().textureManager.bindTexture(imageLocation)
            RenderUtils.drawTexturedRect(imgX.toFloat(), imgY.toFloat(), textureSize.toFloat(), textureSize.toFloat())
            GlStateManager.translate(textX.toFloat(), textY.toFloat(), 0F)
            GlStateManager.scale(textScale, textScale, 1F)
            val fr = Minecraft.getMinecraft().fontRendererObj
            fr.drawString(label, 0, -fr.FONT_HEIGHT / 2, -1)
            GlStateManager.popMatrix()
        }

        fun drawBigRedArrow(gui: GuiContainer, slot: Slot, label: String) {
            gui as AccessorGuiContainer
            drawBigRedArrow(gui.guiLeft + slot.xDisplayPosition + 9, gui.guiTop + slot.yDisplayPosition, label)
        }
    }

    var activeTask: TabListWidget? = null

    @SubscribeEvent
    fun onGuiPostRender(event: GuiScreenEvent.DrawScreenEvent.Post) {
        if (activeTask == null) {
            activeTask = TablistTaskQueue.getNextQueueItem()
        }
        val task = activeTask ?: return

        val gui = event.gui as? GuiChest ?: return
        val chestInventory = gui.inventorySlots as ContainerChest

        val name = chestInventory.lowerChestInventory.displayName.unformattedText

        StateManagerUtils.withSavedState(AccessorGlStateManager.getLightingState()) {
            GlStateManager.disableLighting()
            if (name == "Tablist Widgets") {
                drawSelectAreaArrow(gui, chestInventory, task)
            }
            val regionName = getRegionName(name)
            if (regionName == task.regionName) {
                drawEnableEffect(gui, chestInventory, task)
            } else if (regionName != null) {
                val backSlot = chestInventory.inventorySlots.getOrNull(5 * 9 + 3)
                if (backSlot != null) {
                    Arrow.drawBigRedArrow(gui, backSlot, "Go back!")
                }
            }
        }
    }

    data class WidgetStatus(
        val widgetName: String,
        val enabled: Boolean,
        val slot: Slot,
    )

    fun findWidgets(chestInventory: ContainerChest): List<WidgetStatus> {
        return chestInventory.inventorySlots.mapNotNull {
            val name = ItemUtils.getDisplayName(it.stack)?.let(StringUtils::cleanColour) ?: return@mapNotNull null
            if (!name.endsWith(" Widget")) {
                return@mapNotNull null
            }
            val disabled = name.startsWith("✖")
            val enabled = name.startsWith("✔")
            if (!(enabled || disabled)) {
                return@mapNotNull null
            }
            return@mapNotNull WidgetStatus(name.drop(1).removeSuffix(" Widget").trim(), enabled, it)
        }
    }

    private fun drawEnableEffect(gui: GuiChest, chestInventory: ContainerChest, task: TabListWidget) {
        val widgets = findWidgets(chestInventory)
        val widget = widgets.find { it.widgetName == task.widgetName.toString() }
        if (widget == null) return
        if (widget.enabled) {
            drawPriorityClick(gui, chestInventory, widget)
            return
        }
        Arrow.drawBigRedArrow(gui, widget.slot, "Click here!")
    }

    /*{
        id: "minecraft:skull",
        Count: 1b,
        tag: {
            overrideMeta: 1b,
            HideFlags: 254,
            SkullOwner: {
                Id: "474a0574-24e5-4949-bf61-f8d06120815e",
                hypixelPopulated: 1b,
                Properties: {
                    textures: [{
                        Value: "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzljODg4MWU0MjkxNWE5ZDI5YmI2MWExNmZiMjZkMDU5OTEzMjA0ZDI2NWRmNWI0MzliM2Q3OTJhY2Q1NiJ9fX0="
                    }]
                },
                Name: "§474a0574-24e5-4949-bf61-f8d06120815e"
            },
            display: {
                Lore: ["§7§3⬛               §3§lInfo", "§7§8⬛§f§b§lArea: §7Private Island", "§7§8⬛§f Server: §8mini4C", "§7§8⬛§f Gems: §a0", "§7§8⬛§f Crystals: §d3", "§7§8⬛§f", "§7§8⬛§f§b§lMinions§f: 27§7/§r28", "§7§8⬛§f 7x Ice VII §7[§aACTIVE§7]", "§7§8⬛§f 1x Acacia XI §7[§aACTIVE§7]", "§7§8⬛§f 1x Cave Spider XI §7[§aACTIVE§7]", "§7§8⬛§f 1x Cow XI §7[§aACTIVE§7]", "§7§8⬛§f 1x Creeper X §7[§aACTIVE§7]", "§7§8⬛§f 1x Fishing X §7[§aACTIVE§7]", "§7§8⬛§f 1x Ice XI §7[§aACTIVE§7]", "§7§8⬛§f 1x Iron XI §7[§aACTIVE§7]", "§7§8⬛§f 1x Jungle XI §7[§aACTIVE§7]", "§7§8⬛§f 1x Melon XI §7[§aACTIVE§7]", "§7§8⬛§f 1x Obsidian XI §7[§aACTIVE§7]", "§7§8⬛§f 1x Pig XI §7[§aACTIVE§7]", "§7§8⬛§f ... and 10 more."],
                Name: "§aPrivate Island Widgets Preview"
            },
            AttributeModifiers: []
        },
        Damage: 3s
    }*/
    fun drawPriorityClick(gui: GuiChest, chestInventory: ContainerChest, widget: WidgetStatus) {
        val prioritySlot = chestInventory.inventorySlots.getOrNull(13) ?: return
        val leftSide = chestInventory.inventory.getOrNull(3).let(ItemUtils::getLore)
        val middle = chestInventory.inventory.getOrNull(4).let(ItemUtils::getLore)
        val rightSide = chestInventory.inventory.getOrNull(5).let(ItemUtils::getLore)

        val priorityList = chestInventory.inventory.getOrNull(13).let(ItemUtils::getLore)
        val allTabListEntries = leftSide + middle + rightSide
        val regex = activeTask?.widgetName?.regex ?: Regex.fromLiteral("${widget.widgetName}:")
        if (allTabListEntries.any { it.replace("⬛", "").stripControlCodes().matches(regex) }) {
            activeTask = TablistTaskQueue.getNextQueueItem()
//            Utils.addChatMessage("Success! You enabled ${widget.widgetName}!")
            return
        }
        val editingIndex = priorityList.indexOfFirst { it.contains("EDITING") }
        val widgetPriorityIndex = priorityList.indexOfFirst { it.contains("${widget.widgetName} Widget") }
        if (editingIndex < 0 || widgetPriorityIndex < 0) {
            return
        }
        if (editingIndex == widgetPriorityIndex) {
            Arrow.drawBigRedArrow(gui, prioritySlot, "Shift Right Click")
        } else if (editingIndex < widgetPriorityIndex) {
            Arrow.drawBigRedArrow(gui, prioritySlot, "Left Click")
        } else {
            Arrow.drawBigRedArrow(gui, prioritySlot, "Right Click")
        }
    }

    fun getRegionName(label: String): String? {
        if (!label.startsWith("Widgets")) return null
        return label.removePrefix("Widgets ").removePrefix("in ").removePrefix("on ")
            .removePrefix("the ")
    }

    private fun drawSelectAreaArrow(gui: GuiChest, inventory: ContainerChest, task: TabListWidget) {
        val regionSlot = inventory.inventorySlots.find {
            val name = ItemUtils.getDisplayName(it.stack)?.let(StringUtils::cleanColour) ?: ""
            getRegionName(name) == task.regionName
        } ?: return
        Arrow.drawBigRedArrow(gui, regionSlot, "§cClick here!")
    }

    @SubscribeEvent
    fun onCommands(event: RegisterBrigadierCommandEvent) {
        event.command("neutesttablisttutorial") {
            thenArgument("region", string()) { region ->
                thenArgument("widget", string()) { widget ->
                    thenExecute {
                        TablistTaskQueue.addToQueue(
                            TabListWidget(
                                "Dwarven Mines",
                                TablistAPI.WidgetNames.COMMISSIONS,
                            )
                        )
                        NotEnoughUpdates.INSTANCE.sendChatMessage("/tab")
                    }
                }.withHelp("Test command for showing a tab list tutorial")
            }
        }

        event.command("neutesttablistapi") {
            thenExecute {
                TablistAPI.getWidgetLines(TabListWidget("Dwarven Mines", TablistAPI.WidgetNames.CRYSTALS))
                    .forEach { println(it) }
                println("SEP")
                TablistAPI.getWidgetLines(TabListWidget("Dwarven Mines", TablistAPI.WidgetNames.FORGE))
                    .forEach { println(it) }
            }
        }
    }
}
