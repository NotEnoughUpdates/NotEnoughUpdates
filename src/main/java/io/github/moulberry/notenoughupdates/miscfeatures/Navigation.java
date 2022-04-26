package io.github.moulberry.notenoughupdates.miscfeatures;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.core.util.render.RenderUtils;
import io.github.moulberry.notenoughupdates.events.RepositoryReloadEvent;
import io.github.moulberry.notenoughupdates.miscfeatures.customblockzones.LocationChangeEvent;
import io.github.moulberry.notenoughupdates.util.JsonUtils;
import io.github.moulberry.notenoughupdates.util.SBInfo;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Navigation {

	private List<Teleporter> teleporters = new ArrayList<>();
	private Map<String, String> areaNames = new HashMap<>();

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

	private NotEnoughUpdates neu;

	public Navigation(NotEnoughUpdates notEnoughUpdates) {
		neu = notEnoughUpdates;
	}

	/* JsonObject (x,y,z,island,displayname) */
	private JsonObject currentlyTrackedWaypoint = null;
	private BlockPos position = null;
	private String island = null;
	private String displayName = null;

	private Teleporter nextTeleporter = null;

	public void trackWaypoint(JsonObject trackNow) {
		if (trackNow != null && (
			!trackNow.has("x")
				|| !trackNow.has("y")
				|| !trackNow.has("z")
				|| !trackNow.has("island")
				|| !trackNow.has("displayname"))) {
			showError("Could not track waypoint. This is likely due to an outdated or broken repository.");
			return;
		}
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
				showError("Found self referencing teleporter: " + teleporter.from);
			}
		}
		this.teleporters = teleporters;
		this.areaNames = JsonUtils.transformJsonObjectToMap(obj.getAsJsonObject("area_names"), JsonElement::getAsString);
	}

	public String getNameForAreaMode(String mode) {
		return areaNames.get(mode);
	}

	public String getNameForAreaModeOrUnknown(String mode) {
		return areaNames.getOrDefault(mode, "Unknown");
	}

	public void untrackWaypoint() {
		trackWaypoint(null);
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

	private void updateData() {
		if (currentlyTrackedWaypoint == null) {
			position = null;
			island = null;
			displayName = null;
			nextTeleporter = null;
		}
		position = new BlockPos(
			currentlyTrackedWaypoint.get("x").getAsDouble(),
			currentlyTrackedWaypoint.get("y").getAsDouble(),
			currentlyTrackedWaypoint.get("z").getAsDouble()
		);
		island = currentlyTrackedWaypoint.get("island").getAsString();
		displayName = currentlyTrackedWaypoint.get("displayname").getAsString();
		recalculateNextTeleporter();
	}

	@SubscribeEvent
	public void onLocationChange(LocationChangeEvent event) {
		recalculateNextTeleporter();
	}

	public Teleporter recalculateNextTeleporter() {
		String from = SBInfo.getInstance().mode;
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
				nextTeleporter0.add(teleporter);
				minPath = nextTeleporter0;
			}
		}
		visited.remove(from);
		return minPath;
	}

	private void showError(String message) {
		EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
		if (player != null)
			player.sendChatMessage(EnumChatFormatting.DARK_RED +
				"[NEU-Waypoint] " + message);
		new RuntimeException("[NEU-Waypoint] " + message).printStackTrace();
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
					"Teleporter to " + toName + " (towards " + displayName + "§r)",
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
