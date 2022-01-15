package io.github.moulberry.notenoughupdates.dungeons.map;

import java.awt.Color;
import java.util.Objects;

class RoomConnection {
    RoomConnectionType type;
    Color colour;

    public RoomConnection(RoomConnectionType type, Color colour) {
        this.type = type;
        this.colour = colour;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RoomConnection that = (RoomConnection) o;
        return type == that.type &&
                Objects.equals(colour, that.colour);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, colour);
    }
}
