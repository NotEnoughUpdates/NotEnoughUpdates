package io.github.moulberry.notenoughupdates.dungeons.map;

import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.awt.Color;

class Room {
    Color colour = new Color(0, 0, 0, 0);
    DungeonResources.Checkmark checkmark = null;
    boolean fillCorner = false;
    int posX, posY;

    RoomConnection left = new RoomConnection(RoomConnectionType.NONE, new Color(0, true));
    RoomConnection up = new RoomConnection(RoomConnectionType.NONE, new Color(0, true));
    RoomConnection right = new RoomConnection(RoomConnectionType.NONE, new Color(0, true));
    RoomConnection down = new RoomConnection(RoomConnectionType.NONE, new Color(0, true));

    public void renderCheckmark(float roomSize, float connectorSize, float rotation) {
        if (checkmark == null) return;
        Minecraft.getMinecraft().getTextureManager().bindTexture(checkmark.texture);
        float x = roomSize / 2;
        float y = roomSize / 2;

        if (NotEnoughUpdates.INSTANCE.config.dungeonMap.dmCenterCheck) { // TODO extract config option
            if (fillCorner) {
                x += (roomSize + connectorSize) / 2F;
                y += (roomSize + connectorSize) / 2F;
            }
            if (down.type == RoomConnectionType.ROOM_DIVIDER && right.type != RoomConnectionType.ROOM_DIVIDER) {
                y += (roomSize + connectorSize) / 2f;
            } else if (down.type != RoomConnectionType.ROOM_DIVIDER && right.type == RoomConnectionType.ROOM_DIVIDER) {
                x += (roomSize + connectorSize) / 2f;
            }
        }
        GlStateManager.translate(x, y, 0);
        if (NotEnoughUpdates.INSTANCE.config.dungeonMap.dmOrientCheck) {
            GlStateManager.rotate(rotation, 0, 0, 1);
        }
        GlStateManager.scale(NotEnoughUpdates.INSTANCE.config.dungeonMap.dmIconScale,
                NotEnoughUpdates.INSTANCE.config.dungeonMap.dmIconScale, 1);
        Utils.drawTexturedRect(-5, -5, 10, 10, GL11.GL_NEAREST);
    }

    public void render(float roomSize, float connectorSize) {
        DungeonResources.RoomColor roomColor = DungeonResources.RoomColor.valueOfColor(this.colour);
        if (roomColor != null) {
            Minecraft.getMinecraft().getTextureManager().bindTexture(roomColor.getRoomTexture());
            GlStateManager.color(1, 1, 1, 1);
            Utils.drawTexturedRect(0, 0, roomSize, roomSize, GL11.GL_LINEAR);
        } else {
            Gui.drawRect(0, 0, (int) roomSize, (int) roomSize, colour.getRGB());
        }

        if (fillCorner) {
            GlStateManager.color(1, 1, 1, 1);
            Minecraft.getMinecraft().getTextureManager().bindTexture(DungeonResources.CORNER_BROWN);
            Utils.drawTexturedRect(roomSize, roomSize, connectorSize, connectorSize, GL11.GL_NEAREST);
        }

        for (RoomConnection connection : new RoomConnection[]{down, right}) {
            ResourceLocation corridorTex = null;
            switch (connection.type) {
                case CORRIDOR:
                    DungeonResources.RoomColor corridorColor = DungeonResources.RoomColor.valueOfColor(connection.colour);
                    if (corridorColor != null)
                        corridorTex = corridorColor.getCorridorTexture();
                    break;
                case ROOM_DIVIDER:
                    corridorTex = DungeonResources.DIVIDER_BROWN;
                    break;
                default:
                    continue;
            }

            if (corridorTex == null) {
                int xOffset = 0;
                int yOffset = 0;
                int width = 0;
                int height = 0;

                if (connection == right) {
                    xOffset = (int) roomSize;
                    width = (int) connectorSize;
                    height = (int) roomSize;

                    if (connection.type == RoomConnectionType.CORRIDOR) {
                        height = 8;
                        yOffset += 4;
                    }
                } else if (connection == down) {
                    yOffset = (int) roomSize;
                    width = (int) roomSize;
                    height = (int) connectorSize;

                    if (connection.type == RoomConnectionType.CORRIDOR) {
                        width = 8;
                        xOffset += 4;
                    }
                }

                Gui.drawRect(xOffset, yOffset, xOffset + width + 1, yOffset + height + 1, connection.colour.getRGB());
            } else {
                GlStateManager.color(1, 1, 1, 1);
                Minecraft.getMinecraft().getTextureManager().bindTexture(corridorTex);
                GlStateManager.pushMatrix();
                if (connection == right) {
                    GlStateManager.translate(roomSize / 2f, roomSize / 2f, 0);
                    GlStateManager.rotate(-90, 0, 0, 1);
                    GlStateManager.translate(-roomSize / 2f, -roomSize / 2f, 0);
                }
                Utils.drawTexturedRect(0, roomSize, roomSize, connectorSize, GL11.GL_NEAREST);
                GlStateManager.popMatrix();
            }
        }
    }
}
