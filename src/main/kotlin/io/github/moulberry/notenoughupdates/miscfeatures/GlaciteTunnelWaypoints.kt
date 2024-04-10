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

package io.github.moulberry.notenoughupdates.miscfeatures

import io.github.moulberry.notenoughupdates.NotEnoughUpdates
import io.github.moulberry.notenoughupdates.autosubscribe.NEUAutoSubscribe
import io.github.moulberry.notenoughupdates.core.util.render.RenderUtils
import io.github.moulberry.notenoughupdates.options.separatesections.Mining
import io.github.moulberry.notenoughupdates.overlays.MiningOverlay
import io.github.moulberry.notenoughupdates.util.SBInfo
import net.minecraft.client.Minecraft
import net.minecraft.util.BlockPos
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@NEUAutoSubscribe
class GlaciteTunnelWaypoints {
    val glaciteTunnelLocations = setOf(
        "Glacite Tunnels",
        "Glacite Lake",
        "Dwarven Base Camp",
    )


    data class Waypoints(
        val title: String,
        val waypoints: List<BlockPos>,
    )

    val waypointsForQuest: Map<String, Waypoints> = mapOf(
        "Onyx Gemstone Collector" to Waypoints(
            "§0Onyx Gemstone Mine",
            listOf(
                BlockPos(-68, 130, 407),
                BlockPos(9, 137, 412),
                BlockPos(-17, 133, 393),
                BlockPos(12, 137, 365),
                BlockPos(23, 137, 386),
                BlockPos(79, 119, 412),
            )
        ),
        "Aquamarine Gemstone Collector" to Waypoints(
            "§3Aquamarine Gemstone Mine",
            listOf(
                BlockPos(-3, 139, 437),
                BlockPos(72, 151, 387),
                BlockPos(86, 150, 323),
            )
        ),
        "Peridot Gemstone Collector" to Waypoints(
            "§2Peridot Gemstone Mine",
            listOf(
                BlockPos(-62, 47, 301),
                BlockPos(-76, 120, 281),
                BlockPos(91, 122, 393),
            )
        ),
        "Citrine Gemstone Collector" to Waypoints(
            "§6Citrine Gemstone Mine",
            listOf(
                BlockPos(-95, 145, 258),
                BlockPos(-57, 144, 422),
                BlockPos(37, 119, 387),
            )
        ),
    )

    @SubscribeEvent
    fun onRender(event: RenderWorldLastEvent) {
        if (NotEnoughUpdates.INSTANCE.config.mining.tunnelWaypoints == Mining.GlaciteTunnelWaypointBehaviour.NONE)
            return

        if (SBInfo.getInstance().scoreboardLocation !in glaciteTunnelLocations)
            return

        val wantedGemstones = if (NotEnoughUpdates.INSTANCE.config.mining.alwaysShowTunnelWaypoints)
            waypointsForQuest.keys
        else
            MiningOverlay.commissionProgress.entries.filter { it.value < 1 }.map { it.key }
        val player = Minecraft.getMinecraft().thePlayer?.position ?: return
        for (entry in wantedGemstones) {
            val waypoints = waypointsForQuest[entry] ?: continue
            val waypointLocations = when (NotEnoughUpdates.INSTANCE.config.mining.tunnelWaypoints) {
                Mining.GlaciteTunnelWaypointBehaviour.SHOW_ALL -> {
                    waypoints.waypoints
                }

                Mining.GlaciteTunnelWaypointBehaviour.SHOW_NEAREST -> {
                    listOf(waypoints.waypoints.minByOrNull { it.distanceSq(player) })
                }

                Mining.GlaciteTunnelWaypointBehaviour.NONE -> break
            }
            for (waypoint in waypointLocations) {
                if (waypoint == null) continue
                RenderUtils.renderWayPoint(
                    waypoints.title,
                    waypoint,
                    event.partialTicks
                )
            }
        }
    }
}
