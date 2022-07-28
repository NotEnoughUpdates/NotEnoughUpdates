package io.github.moulberry.notenoughupdates.dungeons.map;

import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.core.config.Position;
import io.github.moulberry.notenoughupdates.options.customtypes.NEUDebugFlag;
import io.github.moulberry.notenoughupdates.overlays.OverlayManager;
import io.github.moulberry.notenoughupdates.overlays.TextOverlay;
import io.github.moulberry.notenoughupdates.overlays.TextOverlayStyle;
import io.github.moulberry.notenoughupdates.util.RecencyList;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DungeonMapDebugOverlay extends TextOverlay {
	public DungeonMapDebugOverlay(DungeonMap map) {
		super(new Position(10, 300), null, () -> TextOverlayStyle.BACKGROUND);
		this.map = map;
		OverlayManager.textOverlays.add(this);
	}

	PrintStream logFile = null;

	public void finishDungeon() {
		if (logFile != null) {
			logFile.close();
			logFile = null;
		}
	}

	public void newDungeon() {
		finishDungeon();
		File file = new File(NotEnoughUpdates.INSTANCE.getNeuDir(), String.format(
			"dungeonlog/log-%s.txt",
			DateTimeFormatter.ofPattern("dd-MM-yyyy-HH-mm-ss").format(LocalDateTime.now())
		));
		file.getParentFile().mkdirs();
		try {
			logFile = new PrintStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void logAssignmentChange(String info) {
		currentAssignmentChanges.add(info);
	}

	DungeonMap map;
	private RecencyList<String> recentAssignmentChanges = new RecencyList<>(Duration.ofSeconds(5));
	private List<String> currentAssignmentChanges = new ArrayList<>();

	@Override
	public void update() {
		recentAssignmentChanges.addAll(currentAssignmentChanges);
		if (!NEUDebugFlag.MAP.isSet() || map.dungeonMapPlayers == null) {
			overlayStrings = null;
			currentAssignmentChanges.clear();
			return;
		}
		overlayStrings = new ArrayList<>();
		overlayStrings.add("Icon Mappings:");
		for (Map.Entry<String, String> entry : map.dungeonMapPlayers.getPlayerNameToIconName().entrySet()) {
			overlayStrings.add(entry.getValue() + " - " + entry.getKey());
		}

		overlayStrings.add("");

		overlayStrings.add("Map Coords:");
		addMap(map.dungeonMapPlayers.getAllMapPositions());

		overlayStrings.add("");

		overlayStrings.add("Player Coords:");
		addMap(map.dungeonMapPlayers.getRunnerEntityPosition());

		DungeonMapPlayers.PlayerMarker playerMarker = map.dungeonMapPlayers.getPlayerMarker();
		overlayStrings.add("Player Marker: " + (playerMarker == null ? "None" : playerMarker.x + " " + playerMarker.z));

		overlayStrings.add(
			"Map Offset: " + map.dungeonMapPlayers.getMapOffsetX() + " " + map.dungeonMapPlayers.getMapOffsetZ());

		overlayStrings.add("");

		overlayStrings.add("Rendered Coords:");
		addMap(map.dungeonMapPlayers.getRunnerPositions());
		overlayStrings.add("");

		overlayStrings.add("Orphaned Coords:");
		addMap(map.dungeonMapPlayers.getOrphanedMarkers());

		overlayStrings.add("");
		overlayStrings.add("Recent assignments:");

		if (currentAssignmentChanges.size() > 0) {
			if (logFile == null) {
				newDungeon();
			}
			overlayStrings.forEach(logFile::println);
			currentAssignmentChanges.forEach(logFile::println);
			logFile.println("------------------------");
			logFile.flush();
		}
		currentAssignmentChanges.clear();
		overlayStrings.addAll(recentAssignmentChanges.getList());

	}

	private void addMap(Map<String, DungeonMapPlayers.PlayerMarker> map) {
		for (Map.Entry<String, DungeonMapPlayers.PlayerMarker> entry : map.entrySet()) {
			overlayStrings.add(entry.getKey() + ": " + entry.getValue().x + " " + entry.getValue().z);
		}
	}

}
