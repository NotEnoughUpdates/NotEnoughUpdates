/*
 * Copyright (C) 2022 NotEnoughUpdates contributors
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

package io.github.moulberry.notenoughupdates.miscfeatures;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.core.util.StringUtils;
import io.github.moulberry.notenoughupdates.core.util.render.RenderUtils;
import io.github.moulberry.notenoughupdates.events.RegisterBrigadierCommandEvent;
import io.github.moulberry.notenoughupdates.events.RepositoryReloadEvent;
import io.github.moulberry.notenoughupdates.miscfeatures.customblockzones.LocationChangeEvent;
import io.github.moulberry.notenoughupdates.miscgui.GuiNavigation;
import io.github.moulberry.notenoughupdates.options.NEUConfig;
import io.github.moulberry.notenoughupdates.util.JsonUtils;
import io.github.moulberry.notenoughupdates.util.NotificationHandler;
import io.github.moulberry.notenoughupdates.util.SBInfo;
import io.github.moulberry.notenoughupdates.util.Utils;
import io.github.moulberry.notenoughupdates.util.brigadier.DslKt;
import kotlin.Unit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.Vec3i;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Navigation {

	private List<Teleporter> teleporters = new ArrayList<>();
	private Map<String, String> areaNames = new HashMap<>();
	private Map<String, WarpPoint> warps = new HashMap<>();
	private Map<String, JsonObject> waypoints = new HashMap<>();

	public Map<String, JsonObject> getWaypoints() {
		return waypoints;
	}

	public static class WarpPoint {
		public final BlockPos blockPos;
		public final String warpName, modeName;

		public WarpPoint(double x, double y, double z, String warpName, String modeName) {
			this.blockPos = new BlockPos(x, y, z);
			this.warpName = warpName;
			this.modeName = modeName;
		}
	}

	public static class Teleporter {
		public final double x, y, z;
		public final String from, to;

		public Teleporter(double x, double y, double z, String from, String to) {
			this.x = x;
			this.y = y;
			this.z = z;
			this.from = from;
			this.to = to;
		}
	}

	private final NotEnoughUpdates neu;

	public Navigation(NotEnoughUpdates notEnoughUpdates) {
		neu = notEnoughUpdates;
	}

	/* JsonObject (x,y,z,island,displayname) */
	private JsonObject currentlyTrackedWaypoint = null;
	private BlockPos position = null;
	private String island = null;
	private String displayName = null;
	private String internalname = null;
	private String warpAgainTo = null;
	private Instant lastWarpAttemptedTime = null;
	private String lastWarpAttempted;
	private Instant warpAgainTiming = null;

	private Teleporter nextTeleporter = null;

	public boolean isValidWaypoint(JsonObject object) {
		return object != null
			&& object.has("x")
			&& object.has("y")
			&& object.has("z")
			&& object.has("island")
			&& object.has("displayname")
			&& object.has("internalname");
	}

	public void trackWaypoint(String trackNow) {
		if (trackNow == null) {
			trackWaypoint((JsonObject) null);
		} else {
			JsonObject jsonObject = waypoints.get(trackNow);
			if (jsonObject == null) {
				showError(
					"Could not track waypoint " + trackNow + ". This is likely due to an outdated or broken repository.",
					true
				);
				return;
			}
			trackWaypoint(jsonObject);
		}
	}

	public void trackWaypoint(JsonObject trackNow) {
		if (trackNow != null && !isValidWaypoint(trackNow)) {
			showError("Could not track waypoint. This is likely due to an outdated or broken repository.", true);
			return;
		}
		if (!neu.config.hidden.hasOpenedWaypointMenu)
			NotificationHandler.displayNotification(Arrays.asList(
				"You just tracked a waypoint.",
				"Press [N] to open the waypoint menu to untrack it",
				"or to find other waypoints to track.",
				"Press [X] to close this message."
			), true, false);
		currentlyTrackedWaypoint = trackNow;
		updateData();
	}

	@SubscribeEvent
	public void onRepositoryReload(RepositoryReloadEvent event) {
		JsonObject obj = Utils.getConstant("islands", neu.manager.gson);
		List<Teleporter> teleporters = JsonUtils.getJsonArrayOrEmpty(obj, "teleporters", jsonElement -> {
			JsonObject teleporterObj = jsonElement.getAsJsonObject();
			return new Teleporter(
				teleporterObj.get("x").getAsDouble(),
				teleporterObj.get("y").getAsDouble(),
				teleporterObj.get("z").getAsDouble(),
				teleporterObj.get("from").getAsString(),
				teleporterObj.get("to").getAsString()
			);
		});
		for (Teleporter teleporter : teleporters) {
			if (teleporter.from.equals(teleporter.to)) {
				showError("Found self referencing teleporter: " + teleporter.from, true);
			}
		}
		this.teleporters = teleporters;
		this.waypoints = NotEnoughUpdates.INSTANCE.manager
			.getItemInformation().values().stream()
			.filter(this::isValidWaypoint)
			.collect(Collectors.toMap(it -> it.get("internalname").getAsString(), it -> it));
		this.areaNames = JsonUtils.transformJsonObjectToMap(obj.getAsJsonObject("area_names"), JsonElement::getAsString);
		this.warps = JsonUtils.getJsonArrayOrEmpty(obj, "island_warps", jsonElement -> {
			JsonObject warpObject = jsonElement.getAsJsonObject();
			return new WarpPoint(
				warpObject.get("x").getAsDouble(),
				warpObject.get("y").getAsDouble(),
				warpObject.get("z").getAsDouble(),
				warpObject.get("warp").getAsString(),
				warpObject.get("mode").getAsString()
			);
		}).stream().collect(Collectors.toMap(it -> it.warpName, it -> it));
	}

	@SubscribeEvent
	public void onKeybindPressed(InputEvent.KeyInputEvent event) {
		if (!Keyboard.getEventKeyState()) return;
		int key = Keyboard.getEventKey() == 0 ? Keyboard.getEventCharacter() + 256 : Keyboard.getEventKey();
		if (neu.config.misc.keybindWaypoint == key) {
			if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
				if (currentlyTrackedWaypoint != null) {
					useWarpCommand();
				}
			} else {
				Minecraft.getMinecraft().displayGuiScreen(new GuiNavigation());
			}
		}
	}

	public Map<String, WarpPoint> getWarps() {
		return warps;
	}

	@SubscribeEvent
	public void onChatMessage(ClientChatReceivedEvent event) {
		if (event.type == 2) return;
		if (StringUtils.cleanColour(event.message.getUnformattedText()).startsWith("§r§eYou may now fast travel to")) {
			getNonUnlockedWarpScrolls().clear();
			return;
		}
		if (!"§r§cYou haven't unlocked this fast travel destination!§r".equals(event.message.getFormattedText())) return;
		Instant lwa = lastWarpAttemptedTime;
		if (lwa == null) return;
		if (Duration.between(lwa, Instant.now()).compareTo(Duration.ofSeconds(1)) > 0) return;
		lastWarpAttemptedTime = null;
		getNonUnlockedWarpScrolls().add(lastWarpAttempted);
		Utils.addChatMessage("§e[NEU] This warp has been marked as non available.");
		Utils.addChatMessage(
			"§e[NEU] Try navigating to that same position again to check if you have another warp available on that island.");
		Utils.addChatMessage("§e[NEU] To reset, type /neuclearwarps");
	}

	@SubscribeEvent
	public void onCommands(RegisterBrigadierCommandEvent event) {
		event.command("neuclearwarps", builder -> DslKt.thenExecute(builder, context -> {
			getNonUnlockedWarpScrolls().clear();
			DslKt.reply(context, "Reset all blocked warps.");
			return Unit.INSTANCE;
		}));
	}

	private Set<String> getNonUnlockedWarpScrolls() {
		NEUConfig.HiddenProfileSpecific profileSpecific = neu.config.getProfileSpecific();
		if (profileSpecific == null) return new HashSet<>();
		return profileSpecific.nonUnlockedWarpScrolls;
	}

	public WarpPoint getClosestWarp(String mode, Vec3i position, boolean checkAvailable) {
		double minDistance = -1;
		Set<String> nonUnlockedWarpScrolls = getNonUnlockedWarpScrolls();
		WarpPoint minWarp = null;
		for (WarpPoint value : warps.values()) {
			if (value.modeName.equals(mode)) {
				if (checkAvailable && nonUnlockedWarpScrolls.contains(value.warpName))
					continue;
				double distance = value.blockPos.distanceSq(position);
				if (distance < minDistance || minWarp == null) {
					minDistance = distance;
					minWarp = value;
				}
			}
		}
		return minWarp;
	}

	public void useWarpCommand() {
		EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;
		if (currentlyTrackedWaypoint == null || thePlayer == null) return;
		WarpPoint closestWarp = getClosestWarp(island, position, true);
		if (closestWarp == null) {
			showError("Could not find an unlocked warp that could be used.", false);
			return;
		}

		if (!island.equals(SBInfo.getInstance().mode)) {
			warpAgainTiming = Instant.now();
			warpAgainTo = closestWarp.warpName;
		} else if (thePlayer.getDistanceSq(position) < closestWarp.blockPos.distanceSq(position)) {
			showError("You are already on the same island and nearer than the closest unlocked warp scroll.", false);
			return;
		}
		lastWarpAttemptedTime = Instant.now();
		lastWarpAttempted = closestWarp.warpName;
		thePlayer.sendChatMessage("/warp " + closestWarp.warpName);
	}

	@SubscribeEvent
	public void onTeleportDone(EntityJoinWorldEvent event) {
		if (neu.config.misc.warpTwice
			&& event.entity == Minecraft.getMinecraft().thePlayer
			&& warpAgainTo != null
			&& warpAgainTiming != null
			&& warpAgainTiming.plusSeconds(1).isAfter(Instant.now())) {
			warpAgainTiming = null;
			String savedWarpAgain = warpAgainTo;
			warpAgainTo = null;
			Minecraft.getMinecraft().thePlayer.sendChatMessage("/warp " + savedWarpAgain);
		}
	}

	public String getNameForAreaMode(String mode) {
		return areaNames.get(mode);
	}

	public String getNameForAreaModeOrUnknown(String mode) {
		return areaNames.getOrDefault(mode, "Unknown");
	}

	public void untrackWaypoint() {
		trackWaypoint((JsonObject) null);
	}

	public JsonObject getTrackedWaypoint() {
		return currentlyTrackedWaypoint;
	}

	public String getIsland() {
		return island;
	}

	public String getDisplayName() {
		return displayName;
	}

	public BlockPos getPosition() {
		return position;
	}

	public String getInternalname() {
		return internalname;
	}

	private void updateData() {
		if (currentlyTrackedWaypoint == null) {
			position = null;
			island = null;
			displayName = null;
			nextTeleporter = null;
			internalname = null;
			return;
		}
		position = new BlockPos(
			currentlyTrackedWaypoint.get("x").getAsDouble(),
			currentlyTrackedWaypoint.get("y").getAsDouble(),
			currentlyTrackedWaypoint.get("z").getAsDouble()
		);
		internalname = currentlyTrackedWaypoint.get("internalname").getAsString();
		island = currentlyTrackedWaypoint.get("island").getAsString();
		displayName = currentlyTrackedWaypoint.get("displayname").getAsString();
		recalculateNextTeleporter(SBInfo.getInstance().mode);
	}

	@SubscribeEvent
	public void onLocationChange(LocationChangeEvent event) {
		recalculateNextTeleporter(event.newLocation);
	}

	public Teleporter recalculateNextTeleporter(String from) {
		String to = island;
		if (from == null || to == null) return null;
		List<Teleporter> nextTeleporter = findNextTeleporter0(from, to, new HashSet<>());
		if (nextTeleporter == null || nextTeleporter.isEmpty()) {
			this.nextTeleporter = null;
		} else {
			this.nextTeleporter = nextTeleporter.get(0);
		}
		return this.nextTeleporter;
	}

	private List<Teleporter> findNextTeleporter0(String from, String to, Set<String> visited) {
		if (from.equals(to)) return new ArrayList<>();
		if (visited.contains(from)) return null;
		visited.add(from);
		int minPathLength = 0;
		List<Teleporter> minPath = null;
		for (Teleporter teleporter : teleporters) {
			if (!teleporter.from.equals(from)) continue;
			List<Teleporter> nextTeleporter0 = findNextTeleporter0(teleporter.to, to, visited);
			if (nextTeleporter0 == null) continue;
			if (minPath == null || nextTeleporter0.size() < minPathLength) {
				minPathLength = nextTeleporter0.size();
				nextTeleporter0.add(0, teleporter);
				minPath = nextTeleporter0;
			}
		}
		visited.remove(from);
		return minPath;
	}

	private void showError(String message, boolean log) {
		EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
		if (player != null)
			Utils.addChatMessage(EnumChatFormatting.DARK_RED + "[NEU-Waypoint] " + message);
		if (log)
			new RuntimeException("[NEU-Waypoint] " + message).printStackTrace();
	}

	@SubscribeEvent
	public void onEvent(TickEvent.ClientTickEvent event) {
		if (event.phase == TickEvent.Phase.END && currentlyTrackedWaypoint != null
			&& NotEnoughUpdates.INSTANCE.config.misc.untrackCloseWaypoints
			&& island.equals(SBInfo.getInstance().mode)) {
			EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;
			if (thePlayer != null && thePlayer.getDistanceSq(position) < 16) {
				untrackWaypoint();
				AbiphoneContactHelper.getInstance().resetMarker();
			}
		}
	}

	@SubscribeEvent
	public void onRenderLast(RenderWorldLastEvent event) {
		if (currentlyTrackedWaypoint != null) {
			if (island.equals(SBInfo.getInstance().mode)) {
				RenderUtils.renderWayPoint(displayName, position, event.partialTicks);
			} else if (nextTeleporter != null) {
				String to = nextTeleporter.to;
				String toName = getNameForAreaModeOrUnknown(to);
				RenderUtils.renderWayPoint(
					Arrays.asList("Teleporter to " + toName, "(towards " + displayName + "§r)"),
					new BlockPos(
						nextTeleporter.x,
						nextTeleporter.y,
						nextTeleporter.z
					), event.partialTicks
				);
			}
		}
	}
}
