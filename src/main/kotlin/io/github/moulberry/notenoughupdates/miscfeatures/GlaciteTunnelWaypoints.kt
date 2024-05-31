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

import com.google.gson.GsonBuilder
import io.github.moulberry.notenoughupdates.NotEnoughUpdates
import io.github.moulberry.notenoughupdates.autosubscribe.NEUAutoSubscribe
import io.github.moulberry.notenoughupdates.core.util.render.RenderUtils
import io.github.moulberry.notenoughupdates.events.RepositoryReloadEvent
import io.github.moulberry.notenoughupdates.options.separatesections.Mining
import io.github.moulberry.notenoughupdates.overlays.MiningOverlay
import io.github.moulberry.notenoughupdates.util.BlockPosTypeAdapterFactory
import io.github.moulberry.notenoughupdates.util.SBInfo
import io.github.moulberry.notenoughupdates.util.kotlin.KSerializable
import io.github.moulberry.notenoughupdates.util.kotlin.KotlinTypeAdapterFactory
import io.github.moulberry.notenoughupdates.util.kotlin.fromJson
import net.minecraft.client.Minecraft
import net.minecraft.util.BlockPos
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@NEUAutoSubscribe
object GlaciteTunnelWaypoints {
    val glaciteTunnelLocations = setOf(
        "Glacite Tunnels",
        "Glacite Lake",
        "Dwarven Base Camp",
        "Inside the Wall",
        "Fossil Research Center",
    )


    @KSerializable
    data class Waypoints(
        val title: String,
        val waypoints: List<BlockPos>,
    )

    val gson = GsonBuilder().registerTypeAdapterFactory(KotlinTypeAdapterFactory)
        .registerTypeAdapterFactory(BlockPosTypeAdapterFactory)
        .create()

    @SubscribeEvent
    fun onRepoReload(event: RepositoryReloadEvent) {
        val text = event.repositoryRoot.resolve("constants/glacite_tunnel_waypoints.json")
            .takeIf { it.exists() }?.readText()
        if (text != null) {
            waypointsForQuest = gson.fromJson(text)
        }
    }

    var waypointsForQuest: Map<String, Waypoints> = mapOf()

    @SubscribeEvent
    fun onRender(event: RenderWorldLastEvent) {
        if (NotEnoughUpdates.INSTANCE.config.mining.tunnelWaypoints.get() == Mining.GlaciteTunnelWaypointBehaviour.NONE)
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
            val waypointLocations = when (NotEnoughUpdates.INSTANCE.config.mining.tunnelWaypoints.get()) {
                Mining.GlaciteTunnelWaypointBehaviour.SHOW_ALL -> {
                    waypoints.waypoints
                }

                Mining.GlaciteTunnelWaypointBehaviour.SHOW_NEAREST -> {
                    listOf(waypoints.waypoints.minByOrNull { it.distanceSq(player) })
                }

                else -> break
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
