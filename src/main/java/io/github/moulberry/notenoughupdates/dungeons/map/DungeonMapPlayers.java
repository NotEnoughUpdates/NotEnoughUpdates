package io.github.moulberry.notenoughupdates.dungeons.map;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import io.github.moulberry.notenoughupdates.mixins.AccessorEntityPlayerSP;
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

	public PlayerMarker getPlayerMarker() {
		return mainPlayerMarker;
	}

	public float getMapOffsetX() {
		return mapXOffset;
	}

	public float getMapOffsetZ() {
		return mapZOffset;
	}

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
	private PlayerMarker mainPlayerMarker;
	private float mainPlayerRotation;
	private float mainPlayerX;
	private float mainPlayerZ;
	private float mapXOffset;
	private float mapZOffset;
	public static float mapScale = 0.66F;

	private boolean isRealPlayer(String playerName) {
		return Minecraft.getMinecraft().thePlayer
			.getWorldScoreboard().getTeams().stream()
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
			mapXOffset = last.mapXOffset;
			mapZOffset = last.mapZOffset;
		}

		if (mapData == null) return;
		Map<String, Vec4b> mapDecorations = mapData.mapDecorations;

		if (mapDecorations != null) {
			parseMapPositions(mapDecorations);
			if (mainPlayerMarker != null)
				calculateMapOffset(mainPlayerMarker);
		}
		parseEntityPositions(mapData);

		if (mapDecorations != null) {
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

	private void calculateMapOffset(PlayerMarker marker) {
		AccessorEntityPlayerSP thePlayer = ((AccessorEntityPlayerSP) Minecraft.getMinecraft().thePlayer);

		mapXOffset = (float) (marker.x - (mapScale * thePlayer.getLastReportedPosX()));
		mapZOffset = (float) (marker.z - (mapScale * thePlayer.getLastReportedPosZ()));
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
			PlayerMarker marker = new PlayerMarker(mapX, mapZ, angle);
			allMapPositions.put(entry.getKey(), marker);
			if (id == 1) {
				mainPlayerMarker = marker;
			}
		}
	}

	private static final float DISTANCE_SQUARED_THRESHOLD = 10;

	private void assignKnownMapMarkers(DungeonMap dungeonMap) {
		for (Map.Entry<String, PlayerMarker> mapPos : allMapPositions.entrySet()) {
			String oldPlayer = playerNameToIconName.inverse().get(mapPos.getKey());
			String newPlayer = findExclusivePlayerNextToPoint(mapPos.getValue());
			if (mainPlayerMarker == mapPos.getValue()) {
				newPlayer = Minecraft.getMinecraft().thePlayer.getName();
			}
			if (newPlayer == null && oldPlayer != null) {
				dungeonMap.dungeonMapDebugOverlay.logAssignmentChange(
					EnumChatFormatting.GOLD + "Player " + oldPlayer + " is far away from their respective marker (" +
						mapPos.getKey() + "). Still keeping marker.");
			}
			if (oldPlayer != null) continue;
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
		for (EntityPlayer entity : Minecraft.getMinecraft().theWorld.playerEntities) {
			if (entity.isPlayerSleeping() || !isRealPlayer(entity.getName())) continue;
			if (entity instanceof AbstractClientPlayer) {
				ResourceLocation skin = ((AbstractClientPlayer) entity).getLocationSkin();
				if (skin != null && !skin.equals(DefaultPlayerSkin.getDefaultSkin(entity.getUniqueID())))
					skinMap.put(entity.getName(), skin);
			}

			float x = ((float) entity.posX) * mapScale + mapXOffset;
			float z = ((float) entity.posZ) * mapScale + mapZOffset;
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
