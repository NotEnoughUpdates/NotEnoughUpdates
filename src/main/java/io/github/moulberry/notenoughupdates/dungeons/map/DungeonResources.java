package io.github.moulberry.notenoughupdates.dungeons.map;

import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.util.ResourceLocation;

import java.awt.Color;
import java.util.Locale;

public class DungeonResources {
    public enum Checkmark {
        CROSS("cross", 255, 0, 0),
        GREEN("green_check", 0, 124, 0),
        QUESTION("question", 13, 13, 13),
        WHITE("white_check", 255, 255, 255);

        public final ResourceLocation texture;
        public final int r, g, b;

        Checkmark(String name, int r, int g, int b) {
            texture = new ResourceLocation("notenoughupdates", "dungeon_map/" + name + ".png");
            this.r = r;
            this.g = g;
            this.b = b;
        }

        public Color getColor() {
            return new Color(r, g, b);
        }

        public static Checkmark valueOfColor(Color color) {
            for (Checkmark checkmark : values()) {
                if (Utils.areRGBColorsEquals(checkmark.getColor(), color))
                    return checkmark;
            }
            return null;
        }
    }

    public enum RoomColor {
        BROWN(114, 67, 27),
        GRAY(65, 65, 65),
        GREEN(0, 124, 0),
        ORANGE(216, 127, 51),
        PINK(242, 127, 165),
        PURPLE(178, 76, 216),
        RED(255, 0, 0),
        YELLOW(229, 229, 51);

        private final int r, g, b;
        private final ResourceLocation roomTexture, corridorTexture;

        RoomColor(int r, int g, int b) {
            this.r = r;
            this.g = g;
            this.b = b;
            this.roomTexture = new ResourceLocation("notenoughupdates", "dungeon_map/rooms_default/" + name().toLowerCase(Locale.ROOT) + "_room.png");
            this.corridorTexture = new ResourceLocation("notenoughupdates", "dungeon_map/corridors_default/" + name().toLowerCase(Locale.ROOT) + "_corridor.png");
        }

        public Color getColor() {
            return new Color(r, g, b);
        }

        public static RoomColor valueOfColor(Color color) {
            for (DungeonResources.RoomColor roomColor : DungeonResources.RoomColor.values()) {
                if (Utils.areRGBColorsEquals(roomColor.getColor(), color)) {
                    return roomColor;
                }
            }
            return null;
        }

        public ResourceLocation getRoomTexture() {
            return roomTexture;
        }

        public ResourceLocation getCorridorTexture() {
            return corridorTexture;
        }
    }

    public static final ResourceLocation MAP_ICONS = new ResourceLocation("textures/map/map_icons.png");

    public static final ResourceLocation DIVIDER_BROWN = new ResourceLocation("notenoughupdates:dungeon_map/dividers_default/brown_divider.png");
    public static final ResourceLocation CORNER_BROWN = new ResourceLocation("notenoughupdates:dungeon_map/corners_default/brown_corner.png");
}
