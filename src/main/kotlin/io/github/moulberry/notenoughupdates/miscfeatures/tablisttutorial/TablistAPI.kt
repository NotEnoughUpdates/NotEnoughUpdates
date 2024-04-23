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

import io.github.moulberry.notenoughupdates.autosubscribe.NEUAutoSubscribe
import io.github.moulberry.notenoughupdates.util.TabListUtils
import io.github.moulberry.notenoughupdates.util.stripControlCodes
import net.minecraft.client.Minecraft
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.*

@NEUAutoSubscribe
object TablistAPI {

    private var lastWorldSwitch = 0L
    var lastWidgetEnabled: WidgetNames? = null

    @SubscribeEvent
    fun onWorldSwitch(event: EntityJoinWorldEvent) {
        if (event.entity == Minecraft.getMinecraft().thePlayer) {
            lastWorldSwitch = System.nanoTime()
        }
    }

    @JvmStatic
    fun getWidgetLinesInRegion(
        widget: TablistTutorial.TabListWidget,
        addToQueue: Boolean,
        showNotification: Boolean
    ): List<String> {
        val regex = widget.widgetName.regex ?: Regex.fromLiteral("${widget.widgetName}:")
        val list = mutableListOf<String>()
        // If not a single reset is present, the tab list hasn't been initialized yet
        var sawReset = false

        for (entry in TabListUtils.getTabList()) {
            if (entry.contains("§r")) {
                sawReset = true
            }

            if (list.isNotEmpty()) {
                // Empty line, the widget ends here.
                if (entry == "§r") {
                    break
                }

                // New tab column, ignore it and continue with the same widget
                if (entry == "§r               §r§3§lInfo§r") {
                    continue
                }

                list.add(entry)
            } else if (entry.stripControlCodes().matches(regex)) {
                list.add(entry)
            }
        }
        if (addToQueue &&
            list.isEmpty() &&
            sawReset &&
            (System.nanoTime() - lastWorldSwitch > 10_000_000L) &&
            widget.widgetName != lastWidgetEnabled
        ) {
            TablistTaskQueue.addToQueue(widget, showNotification)
        }
        return list
    }

    /**
     * Attempt to get the lines for this widget.
     * Otherwise, add the widget to the tablist queue and show a notification to the user
     */
    @JvmStatic
    fun getWidgetLines(widgetName: WidgetNames): List<String> {
        return getWidgetLinesInRegion(
            TablistTutorial.TabListWidget("CURRENT_REGION", widgetName),
            addToQueue = true,
            showNotification = true
        )
    }

    /**
     * Attempt to get the lines for this widget.
     * Otherwise, add the widget to the tablist queue without showing a notification to the user.
     *
     * Consider using this if there is a more optimal way of informing the user.
     */
    @JvmStatic
    fun getWidgetLinesWithoutNotification(widgetName: WidgetNames): List<String> {
        return getWidgetLinesInRegion(
            TablistTutorial.TabListWidget("CURRENT_REGION", widgetName),
            addToQueue = true,
            showNotification = false
        )
    }

    /**
     * Attempt to get the lines for this widget.
     * Otherwise, do nothing.
     *
     * Consider using this if the result is not important, but simply a nice to have.
     */
    @JvmStatic
    fun getOptionalWidgetLines(widgetName: WidgetNames): List<String> {
        return getWidgetLinesInRegion(
            TablistTutorial.TabListWidget("CURRENT_REGION", widgetName),
            addToQueue = false,
            showNotification = false
        )
    }

    enum class WidgetNames(val regex: Regex?) {
        COMMISSIONS(null),
        /*
            '§e§lSkills:'
            ' Farming 50: §r§a43.3%'
            ' Mining 60: §r§c§lMAX'
            ' Combat 46: §r§a21.7%'
            ' Foraging 23: §r§a43.5%'
        * */
        SKILLS(Regex("Skills:( .*)?")),
        /*
        * '§e§lSkills: §r§aCombat 46: §r§321.7%'
        * */
        DUNGEON_SKILLS(Regex("Skills: (.*)")),
        TRAPPER(null),
        FORGE(Regex("Forges:( \\(\\d/\\d\\))?")),
        POWDER(Regex.fromLiteral("Powders:")),
        PROFILE(Regex("Profile: ([A-Za-z]+)( .*)?"))
        ;

        override fun toString(): String {
            return this.name.lowercase().split("_").joinToString(" ") { str ->
                str.replaceFirstChar {
                    if (it.isLowerCase()) {
                        it.titlecase(
                            Locale.ROOT
                        )
                    } else {
                        it.toString()
                    }
                }
            }
        }
    }
}
