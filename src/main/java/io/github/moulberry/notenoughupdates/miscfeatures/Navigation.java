package io.github.moulberry.notenoughupdates.miscfeatures;

import com.google.gson.JsonObject;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.core.util.render.RenderUtils;
import io.github.moulberry.notenoughupdates.util.SBInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Navigation {

	private NotEnoughUpdates neu;

	public Navigation(NotEnoughUpdates notEnoughUpdates) {
		neu = notEnoughUpdates;
	}

	/* JsonObject (x,y,z,island,displayname) */
	private JsonObject currentlyTrackedWaypoint = null;
	private BlockPos position = null;
	private String island = null;
	private String displayName = null;

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
		}
		position = new BlockPos(
			currentlyTrackedWaypoint.get("x").getAsDouble(),
			currentlyTrackedWaypoint.get("y").getAsDouble(),
			currentlyTrackedWaypoint.get("z").getAsDouble()
		);
		island = currentlyTrackedWaypoint.get("island").getAsString();
		displayName = currentlyTrackedWaypoint.get("displayname").getAsString();
	}

	private void showError(String message) {
		Minecraft.getMinecraft().thePlayer.sendChatMessage(EnumChatFormatting.DARK_RED +
			"[NEU-Waypoint] " + message);
		new RuntimeException("[NEU-Waypoint] " + message).printStackTrace();
	}

	@SubscribeEvent
	public void onRenderLast(RenderWorldLastEvent event) {
		if (currentlyTrackedWaypoint != null && island.equals(SBInfo.getInstance().mode)) {
			RenderUtils.renderWayPoint(displayName, position, event.partialTicks);
		}
	}
}
