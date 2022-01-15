package io.github.moulberry.notenoughupdates.dungeons.map;

import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.util.Vec4b;

import java.awt.*;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class DungeonMapStaticParser {

    public final DungeonMap dungeonMap;
    public final Color[][] colorMap;
    public final Map<String, Vec4b> mapDecorations;
    public final String floorName;
    public Map<RoomOffset, Room> roomMap = new HashMap<>();
    int startRoomX, startRoomY;
    int connectorSize = 5;
    int roomSize = 0;

    public DungeonMapStaticParser(DungeonMap dungeonMap, String floorName, Color[][] colorMap, Map<String, Vec4b> mapDecorations) {
        this.dungeonMap = dungeonMap;
        this.floorName = floorName;
        this.colorMap = colorMap;
        this.mapDecorations = mapDecorations;
    }

    public Color getColor(int x, int y) {
        if (x < 0 || y < 0) return null;
        if (x >= colorMap.length || y >= colorMap[0].length) return null;
        return colorMap[x][y];
    }

    public Map<Color, Integer> aggregateColorInRectangle(int minX, int minY, int width, int height, int minAlpha) {
        Map<Color, Integer> map = new HashMap<>();
        int maxX = Math.min(minX + width, colorMap.length);
        int maxY = Math.min(minY + height, colorMap[0].length);
        for (int x = Math.max(0, minX); x < maxX; x++) {
            for (int y = Math.max(0, minY); y < maxY; y++) {
                Color color = colorMap[x][y];
                if (color.getAlpha() < minAlpha) continue;
                if (map.containsKey(color))
                    map.put(color, map.get(color) + 1);
                else
                    map.put(color, 1);
            }
        }

        return map;
    }

    public void updateRoomConnections(RoomOffset roomOffset) {
        if (!roomMap.containsKey(roomOffset)) return;

        Room room = roomMap.get(roomOffset);

        Map<Color, Integer> aggregateColors = aggregateColorInRectangle(
                startRoomX + roomOffset.x * (roomSize + connectorSize),
                startRoomY + roomOffset.y * (roomSize + connectorSize),
                roomSize, roomSize, 0
        );

        aggregateColors.entrySet().stream().filter(it -> !it.getKey().equals(room.colour))
                .max(Comparator.comparingInt(Map.Entry::getValue))
                .filter(it -> (float) it.getValue() / roomSize / connectorSize > 0.05)
                .ifPresent(it -> {
                    room.checkmark = DungeonResources.Checkmark.valueOfColor(it.getKey());
                });

        for (int k = 0; k < 4; k++) {
            boolean isVerticalDirection = (k % 2) == 0;

            int width = isVerticalDirection ? roomSize : connectorSize;
            int height = isVerticalDirection ? connectorSize : roomSize;
            int x = startRoomX + roomOffset.x * (roomSize + connectorSize);
            int y = startRoomY + roomOffset.y * (roomSize + connectorSize);

            if (k == 0) {
                y -= connectorSize;
            } else if (k == 1) {
                x -= connectorSize;
            } else if (k == 2) {
                y += roomSize;
            } else {
                x += roomSize;
            }

            Optional<Map.Entry<Color, Integer>> highestColor = aggregateColorInRectangle(x, y, width, height, 40).entrySet().stream().max(Comparator.comparingInt(Map.Entry::getValue));
            if (!highestColor.isPresent()) continue;

            float proportionFilled = (float) highestColor.get().getValue() / roomSize / connectorSize;

            RoomConnectionType type = RoomConnectionType.WALL;
            if (proportionFilled > 0.8) {
                type = RoomConnectionType.ROOM_DIVIDER;
            } else if (proportionFilled > 0.1) {
                type = RoomConnectionType.CORRIDOR;
            }
            Color color = highestColor.get().getKey();
            if (k == 0) {
                room.up = new RoomConnection(type, color);
            } else if (k == 1) {
                room.left = new RoomConnection(type, color);
            } else if (k == 2) {
                room.down = new RoomConnection(type, color);
            } else {
                room.right = new RoomConnection(type, color);
            }
        }

        int x = startRoomX + roomOffset.x * (roomSize + connectorSize) + roomSize + connectorSize / 2;
        int y = startRoomY + roomOffset.y * (roomSize + connectorSize) + roomSize + connectorSize / 2;

        room.fillCorner = false;
        if (x > 0 && y > 0 && x < colorMap.length && y < colorMap[x].length) {
            Color pixel = colorMap[x][y];
            if (pixel.equals(room.colour)) {
                room.fillCorner = true;
            }
        }
    }


    public boolean recalculateStartRoom() {
        for (int x = 0; x < colorMap.length; x++) {
            for (int y = 0; y < colorMap[x].length; y++) {
                Color c = colorMap[x][y];
                if (c.getAlpha() > 80) {
                    if (Utils.areRGBColorsEquals(c, DungeonResources.RoomColor.GREEN.getColor())) {
                        roomSize = 0;
                        out:
                        for (int xd = 0; xd <= 20; xd++) {
                            for (int yd = 0; yd <= 20; yd++) {
                                Color c2 = getColor(x + xd, y + yd);
                                if (c2 == null) continue;

                                if (c2.getGreen() != 124 || c2.getAlpha() <= 80) {
                                    if (xd < 10 && yd < 10) {
                                        break out;
                                    }
                                } else {
                                    roomSize = Math.max(roomSize, Math.min(xd + 1, yd + 1));
                                }
                                if (xd == 20 && yd == 20) {
                                    if (roomSize == 0) roomSize = 20;
                                    startRoomX = x;
                                    startRoomY = y;
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    public boolean parseDungeonMap() {

        if (!recalculateStartRoom())
            return false;
        recalculateConnectorSize();

        loadNeighbors(new RoomOffset(0, 0));
        for (RoomOffset offset : roomMap.keySet()) {
            updateRoomConnections(offset);
        }

        return !roomMap.isEmpty();
    }

    public void loadNeighbors(RoomOffset room) {
        if (roomMap.containsKey(room)) return;
        int x = startRoomX + room.x * (roomSize + connectorSize);
        int y = startRoomY + room.y * (roomSize + connectorSize);
        Color color = getColor(x, y);
        if (color == null || color.getAlpha() <= 100) return;
        Room newRoom = new Room();
        newRoom.colour = color;
        newRoom.posX = x;
        newRoom.posY = y;
        roomMap.put(room, newRoom);
        for (RoomOffset neighbor : room.getNeighbors()) {
            loadNeighbors(neighbor);
        }
    }

    private void recalculateConnectorSize() {
        for (int i = 0; i < roomSize; i++) {
            for (int k = 0; k < 4; k++) {
                for (int j = 1; j < 8; j++) {
                    int x;
                    int y;

                    if (k == 0) {
                        x = startRoomX + i;
                        y = startRoomY - j;
                    } else if (k == 1) {
                        x = startRoomX + roomSize + j - 1;
                        y = startRoomY + i;
                    } else if (k == 2) {
                        x = startRoomX + i;
                        y = startRoomY + roomSize + j - 1;
                    } else {
                        x = startRoomX - j;
                        y = startRoomY + i;
                    }

                    if (x > 0 && y > 0 && x < colorMap.length && y < colorMap[x].length) {
                        if (colorMap[x][y].getAlpha() > 80) {
                            if (j == 1) {
                                break;
                            }
                            connectorSize = Math.min(connectorSize, j - 1);
                        }
                    }
                }
            }
        }

        if (connectorSize <= 0) {
            connectorSize = 4;
        }
    }
}
