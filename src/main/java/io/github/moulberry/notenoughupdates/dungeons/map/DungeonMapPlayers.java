package io.github.moulberry.notenoughupdates.dungeons.map;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec4b;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class DungeonMapPlayers {

	public static class Linear {
		private final float slope;
		private final float intercept;

		public Linear(float slope, float intercept) {
			this.slope = slope;
			this.intercept = intercept;
		}

		public float getSlope() {
			return slope;
		}

		public float getIntercept() {
			return intercept;
		}

		public float calculate(float x) {
			return slope * x + intercept;
		}

		public float calculateInverse(float y) {
			return (y - intercept) / slope;
		}
	}

	public static Map<String, Linear> FLOOR_MAP_SCALINGS = new HashMap<String, Linear>() {{
		put("F1", new Linear(1.5F, -215F));
		put("F2", new Linear(1.5F, -215F));
		put("F3", new Linear(1.5F, -215F));
		put("F4", new Linear(1.6F, -206F));
		put("F5", new Linear(1.6F, -206F));
		put("F6", new Linear(1.6F, -206F));
		put("F7", new Linear(1.6F, -206F));
	}};

	public static class PlayerMarker {
		public final int x, z;
		public final float angle;

		public PlayerMarker(int x, int z, float angle) {
			this.x = x;
			this.z = z;
			this.angle = angle;
		}

		public int squaredDistance(PlayerMarker marker) {
			int deltaX = marker.x - x;
			int deltaZ = marker.z - z;
			return deltaX * deltaX + deltaZ * deltaZ;
		}

		@Override
		public int hashCode() {
			return Objects.hash(x, z, angle);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			PlayerMarker that = (PlayerMarker) o;

			if (x != that.x) return false;
			if (z != that.z) return false;
			return Float.compare(that.angle, angle) == 0;
		}
	}

	private final Map<String, PlayerMarker> runnerPositions = new HashMap<>();
	private final Map<String, PlayerMarker> runnerEntityPosition = new HashMap<>();
	private final BiMap<String, String> playerNameToIconName = HashBiMap.create();
	private final Map<String, ResourceLocation> skinMap = new HashMap<>();
	private final Map<String, PlayerMarker> allMapPositions = new HashMap<>();
	private final Map<String, PlayerMarker> orphanedMarkers = new HashMap<>();
	private float mainPlayerRotation;
	private float mainPlayerX;
	private float mainPlayerZ;

	private boolean isRealPlayer(String playerName) {
		return Minecraft.getMinecraft().thePlayer.getWorldScoreboard().getTeams().stream()
																						 .anyMatch(it
																							 -> it.getMembershipCollection().size() == 1
																							 && it.getMembershipCollection().contains(playerName)
																							 && it.getTeamName().startsWith("a")
																							 && it.getNameTagVisibility() == Team.EnumVisible.ALWAYS);
	}

	public void parse(DungeonMapStaticParser mapData, DungeonMapPlayers last) {
		mainPlayerRotation = Minecraft.getMinecraft().thePlayer.rotationYaw;
		if (last != null) {
			playerNameToIconName.putAll(last.playerNameToIconName);
			skinMap.putAll(last.skinMap);
		}

		if (mapData == null) return;

		parseEntityPositions(mapData);

		Map<String, Vec4b> mapDecorations = mapData.mapDecorations;
		if (mapDecorations != null) {
			parseMapPositions(mapDecorations);
			assignKnownMapMarkers(mapData.dungeonMap);
			findOrphanedMarkers();
		} else if (last != null) {
			// If the map decorations aren't present, then add the past orphaned markers, so those don't flicker.
			orphanedMarkers.putAll(last.orphanedMarkers);
		}

		playerNameToIconName.forEach((playerName, markerName) -> {
			if (runnerEntityPosition.containsKey(playerName)) {
				runnerPositions.put(playerName, runnerEntityPosition.get(playerName));
			} else if (allMapPositions.containsKey(markerName)) {
				runnerPositions.put(playerName, allMapPositions.get(markerName));
			}
		});
	}

	private void findOrphanedMarkers() {
		for (Map.Entry<String, PlayerMarker> marker : allMapPositions.entrySet()) {
			if (!playerNameToIconName.containsValue(marker.getKey())) {
				// We couldn't find a matching player, orphan this marker, so we can render it with the default marker.
				orphanedMarkers.put(marker.getKey(), marker.getValue());
			}
		}
	}

	private void parseMapPositions(Map<String, Vec4b> mapDecorations) {
		for (Map.Entry<String, Vec4b> entry : mapDecorations.entrySet()) {
			byte id = entry.getValue().func_176110_a();
			if (id != 1 && id != 3) continue;
			int mapX = (int) entry.getValue().func_176112_b() / 2 + 64;
			int mapZ = (int) entry.getValue().func_176113_c() / 2 + 64;
			float angle = entry.getValue().func_176111_d() * 360 / 16F;
			allMapPositions.put(entry.getKey(), new PlayerMarker(mapX, mapZ, angle));
		}
	}

	private static final float DISTANCE_SQUARED_THRESHOLD = 10;

	private void assignKnownMapMarkers(DungeonMap dungeonMap) {
		for (Map.Entry<String, PlayerMarker> mapPos : allMapPositions.entrySet()) {
			String oldPlayer = playerNameToIconName.inverse().get(mapPos.getKey());
			String newPlayer = findExclusivePlayerNextToPoint(mapPos.getValue());
			if (newPlayer == null && oldPlayer != null) {
				dungeonMap.dungeonMapDebugOverlay.logAssignmentChange(
					EnumChatFormatting.GOLD + "Player " + oldPlayer + " is far away from their respective marker (" +
						mapPos.getKey() + "). Still keeping marker.");
			}

			if (newPlayer != null && !Objects.equals(newPlayer, oldPlayer)) {
				if (playerNameToIconName.containsKey(newPlayer)) {
					String oldMarker = playerNameToIconName.remove(newPlayer);
					dungeonMap.dungeonMapDebugOverlay.logAssignmentChange(
						EnumChatFormatting.RED + "Unassigned " + newPlayer + " (previously " +
							oldMarker + "), but seems to be " + mapPos.getKey());
				}
				playerNameToIconName.inverse().remove(mapPos.getKey());
				playerNameToIconName.put(newPlayer, mapPos.getKey());
				dungeonMap.dungeonMapDebugOverlay.logAssignmentChange(
					EnumChatFormatting.GREEN + "Assigned " + newPlayer + " to " + mapPos.getKey() + " (previously " + oldPlayer +
						")");
			}
		}
	}

	private String findExclusivePlayerNextToPoint(PlayerMarker referencePoint) {
		String entityCandidate = null;
		for (Map.Entry<String, PlayerMarker> entityPos : runnerEntityPosition.entrySet()) {
			if (referencePoint.squaredDistance(entityPos.getValue()) > DISTANCE_SQUARED_THRESHOLD) continue;
			if (entityCandidate != null)
				return null; // Multiple candidates, people stepping on each other (kinda sus ngl)
			entityCandidate = entityPos.getKey();
		}
		return entityCandidate;
	}

	private void parseEntityPositions(DungeonMapStaticParser mapData) {
		Linear coordMapping = FLOOR_MAP_SCALINGS.get(mapData.floorName);
		if (coordMapping == null) return;
		for (EntityPlayer entity : Minecraft.getMinecraft().theWorld.playerEntities) {
			if (entity.isPlayerSleeping() || !isRealPlayer(entity.getName())) continue;
			if (entity instanceof AbstractClientPlayer) {
				ResourceLocation skin = ((AbstractClientPlayer) entity).getLocationSkin();
				if (skin != null && !skin.equals(DefaultPlayerSkin.getDefaultSkin(entity.getUniqueID())))
					skinMap.put(entity.getName(), skin);
			}

			float x = coordMapping.calculateInverse((float) entity.posX);
			float z = coordMapping.calculateInverse((float) entity.posZ);
			if (entity.getName().equals(Minecraft.getMinecraft().thePlayer.getName())) {
				mainPlayerX = x;
				mainPlayerZ = z;
			}
			runnerEntityPosition.put(entity.getName(), new PlayerMarker(
				(int) x, (int) z, entity.rotationYaw));
		}
	}

	public ResourceLocation getSkin(String name) {
		return skinMap.getOrDefault(name, DefaultPlayerSkin.getDefaultSkinLegacy());
	}

	public float getMainPlayerX() {
		return mainPlayerX;
	}

	public void setMainPlayerX(float mainPlayerX) {
		this.mainPlayerX = mainPlayerX;
	}

	public void setMainPlayerZ(float mainPlayerZ) {
		this.mainPlayerZ = mainPlayerZ;
	}

	public void setMainPlayerRotation(float mainPlayerRotation) {
		this.mainPlayerRotation = mainPlayerRotation;
	}

	public float getMainPlayerZ() {
		return mainPlayerZ;
	}

	public float getMainPlayerRotation() {
		return mainPlayerRotation;
	}

	public Map<String, PlayerMarker> getRunnerPositions() {
		return runnerPositions;
	}

	public Map<String, PlayerMarker> getAllMapPositions() {
		return allMapPositions;
	}

	public BiMap<String, String> getPlayerNameToIconName() {
		return playerNameToIconName;
	}

	public Map<String, PlayerMarker> getRunnerEntityPosition() {
		return runnerEntityPosition;
	}

	public Map<String, PlayerMarker> getOrphanedMarkers() {
		return orphanedMarkers;
	}
}
