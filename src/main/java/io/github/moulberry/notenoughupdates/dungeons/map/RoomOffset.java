package io.github.moulberry.notenoughupdates.dungeons.map;

import java.util.Objects;

class RoomOffset {
    int x;
    int y;

    public RoomOffset(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public RoomOffset left() {
        return new RoomOffset(x - 1, y);
    }

    public RoomOffset right() {
        return new RoomOffset(x + 1, y);
    }

    public RoomOffset up() {
        return new RoomOffset(x, y - 1);
    }

    public RoomOffset down() {
        return new RoomOffset(x, y + 1);
    }

    public RoomOffset[] getNeighbors() {
        return new RoomOffset[]{left(), right(), up(), down()};
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RoomOffset that = (RoomOffset) o;
        return x == that.x && y == that.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}
