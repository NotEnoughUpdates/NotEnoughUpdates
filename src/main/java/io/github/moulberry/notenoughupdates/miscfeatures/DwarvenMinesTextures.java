package io.github.moulberry.notenoughupdates.miscfeatures;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.util.SBInfo;
import net.minecraft.block.BlockColored;
import net.minecraft.block.BlockStone;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.ChunkCoordIntPair;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DwarvenMinesTextures {
    private static class IgnoreColumn {
        boolean always;
        int minY;
        int maxY;

        public IgnoreColumn(boolean always, int minY, int maxY) {
            this.always = always;
            this.minY = minY;
            this.maxY = maxY;
        }
    }

    private static HashSet<ChunkCoordIntPair> ignoredChunks = null;
    private static final HashMap<ChunkCoordIntPair, HashMap<ChunkCoordIntPair, IgnoreColumn>> loadedChunkData = new HashMap<>();
    private static final HashMap<ChunkCoordIntPair, Long> lastRetextureCheck = new HashMap<>();
    private static long time;
    private static boolean error = false;

    public static int retexture(BlockPos pos) {
        if (!NotEnoughUpdates.INSTANCE.config.mining.dwarvenTextures) return 0;
        if (error) return 0;
        if (Minecraft.getMinecraft().theWorld == null) return 0;

        String location = SBInfo.getInstance().getLocation();

        if (location == null) return 0;
        if (location.equals("crystal_hollows")) return 3;
        if (!location.equals("mining_3")) return 0;

        IBlockState state = Minecraft.getMinecraft().theWorld.getBlockState(pos);
        boolean titanium = state.getBlock() == Blocks.stone && state.getValue(BlockStone.VARIANT) == BlockStone.EnumType.DIORITE_SMOOTH;
        if (titanium) {
            IBlockState plus = Minecraft.getMinecraft().theWorld.getBlockState(pos.add(1, 0, 0));
            if (plus.getBlock() == Blocks.double_stone_slab) {
                return 1;
            }
            IBlockState minus = Minecraft.getMinecraft().theWorld.getBlockState(pos.add(-1, 0, 0));
            if (minus.getBlock() == Blocks.double_stone_slab) {
                return 1;
            }
            IBlockState above = Minecraft.getMinecraft().theWorld.getBlockState(pos.add(0, 1, 0));
            if (above.getBlock() == Blocks.stone_slab) {
                return 1;
            }
        }

        if (titanium || (state.getBlock() == Blocks.stained_hardened_clay && state.getValue(BlockColored.COLOR) == EnumDyeColor.CYAN) ||
                (state.getBlock() == Blocks.wool && state.getValue(BlockColored.COLOR) == EnumDyeColor.GRAY)) {

            if (ignoredChunks == null) {
                try {
                    ignoredChunks = new HashSet<>();
                    ResourceLocation loc = new ResourceLocation("notenoughupdates:dwarven_data/all.json");
                    InputStream is = Minecraft.getMinecraft().getResourceManager().getResource(loc).getInputStream();

                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                        JsonObject json = NotEnoughUpdates.INSTANCE.manager.gson.fromJson(reader, JsonObject.class);
                        for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                            String coord = entry.getKey();
                            String[] split = coord.split("_");
                            int left = Integer.parseInt(split[0]);
                            int right = Integer.parseInt(split[1]);
                            ignoredChunks.add(new ChunkCoordIntPair(left, right));
                        }
                    }
                } catch (Exception e) {
                    error = true;
                    return 1;
                }
            }
            if (ignoredChunks != null) {
                ChunkCoordIntPair pair = new ChunkCoordIntPair(MathHelper.floor_float(pos.getX() / 16f),
                        MathHelper.floor_float(pos.getZ() / 16f));

                lastRetextureCheck.put(pair, time);

                if (ignoredChunks.contains(pair)) {
                    return 1;
                }
                if (titanium) {
                    return 2;
                }

                if (!loadedChunkData.containsKey(pair)) {
                    try {
                        HashMap<ChunkCoordIntPair, IgnoreColumn> map = new HashMap<>();
                        loadedChunkData.put(pair, map);

                        ResourceLocation loc = new ResourceLocation("notenoughupdates:dwarven_data/" +
                                pair.chunkXPos + "_" + pair.chunkZPos + ".json");
                        InputStream is = Minecraft.getMinecraft().getResourceManager().getResource(loc).getInputStream();

                        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                            JsonObject json = NotEnoughUpdates.INSTANCE.manager.gson.fromJson(reader, JsonObject.class);
                            for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                                String coord = entry.getKey();
                                String[] split = coord.split(":");
                                int left = Integer.parseInt(split[0]);
                                int right = Integer.parseInt(split[1]);

                                IgnoreColumn ignore = null;
                                if (entry.getValue().isJsonPrimitive()) {
                                    JsonPrimitive prim = entry.getValue().getAsJsonPrimitive();
                                    if (prim.isBoolean()) {
                                        ignore = new IgnoreColumn(true, 0, 0);
                                    } else if (prim.isNumber()) {
                                        int y = prim.getAsInt();
                                        ignore = new IgnoreColumn(false, y, y);
                                    }
                                } else if (entry.getValue().isJsonArray()) {
                                    JsonArray arr = entry.getValue().getAsJsonArray();
                                    if (arr.size() == 2) {
                                        int min = arr.get(0).getAsInt();
                                        int max = arr.get(1).getAsInt();
                                        ignore = new IgnoreColumn(false, min, max);
                                    }
                                }
                                if (ignore != null) {
                                    ChunkCoordIntPair offset = new ChunkCoordIntPair(left, right);
                                    map.put(offset, ignore);
                                }
                            }
                        }
                    } catch (Exception e) {
                        loadedChunkData.put(pair, null);
                    }
                }
                if (loadedChunkData.get(pair) != null) {
                    HashMap<ChunkCoordIntPair, IgnoreColumn> map = loadedChunkData.get(pair);
                    if (map == null) {
                        return 0;
                    }

                    int modX = pos.getX() % 16;
                    int modZ = pos.getZ() % 16;
                    if (modX < 0) modX += 16;
                    if (modZ < 0) modZ += 16;
                    ChunkCoordIntPair offset = new ChunkCoordIntPair(modX, modZ);

                    IgnoreColumn ignore = map.get(offset);
                    if (ignore != null) {
                        if (ignore.always) {
                            return 1;
                        } else {
                            int y = pos.getY();
                            if (y >= ignore.minY && y <= ignore.maxY) {
                                return 1;
                            }
                        }
                    }
                }
            }
        }

        return 2;
    }

    /*@SubscribeEvent
    public void onRender(RenderWorldLastEvent event) {
        Entity viewer = Minecraft.getMinecraft().getRenderViewEntity();
        double viewerX = viewer.lastTickPosX + (viewer.posX - viewer.lastTickPosX) * event.partialTicks;
        double viewerY = viewer.lastTickPosY + (viewer.posY - viewer.lastTickPosY) * event.partialTicks;
        double viewerZ = viewer.lastTickPosZ + (viewer.posZ - viewer.lastTickPosZ) * event.partialTicks;

        int x = MathHelper.floor_double(viewer.posX / 16f);
        int z = MathHelper.floor_double(viewer.posZ / 16f);
        File file = new File("C:/Users/James/Desktop/testfolder/" + x + "_" + z + ".json");

        int col = 0xff0000;
        if (file.exists()) {
            col = 0x00ff00;
            if (Keyboard.isKeyDown(Keyboard.KEY_K)) {
                file.delete();
            }

        }

        AxisAlignedBB bb = new AxisAlignedBB(
                MathHelper.floor_double(viewerX / 16) * 16 - viewerX,
                0 - viewerY,
                MathHelper.floor_double(viewerZ / 16) * 16 - viewerZ,
                MathHelper.floor_double(viewerX / 16) * 16 + 16 - viewerX,
                255 - viewerY,
                MathHelper.floor_double(viewerZ / 16) * 16 + 16 - viewerZ).expand(0.01f, 0.01f, 0.01f);

        GlStateManager.disableCull();
        CustomItemEffects.drawFilledBoundingBox(bb, 1f, SpecialColour.special(0, 100, col));
        GlStateManager.enableCull();
        GlStateManager.enableTexture2D();
    }*/

    //Render all blocks - extremeHillsEdge
    //Don't render smooth diorite - extremeHillsPlus
    //Don't render clay - mesaPlateau_F

    public static void tick() {
        if (!NotEnoughUpdates.INSTANCE.config.mining.dwarvenTextures) return;

        time = System.currentTimeMillis();
        Set<ChunkCoordIntPair> remove = new HashSet<>();
        for (Map.Entry<ChunkCoordIntPair, Long> entry : lastRetextureCheck.entrySet()) {
            if (time - entry.getValue() > 30 * 1000) {
                remove.add(entry.getKey());
            }
        }
        lastRetextureCheck.keySet().removeAll(remove);
        loadedChunkData.keySet().removeAll(remove);

        /*if (Minecraft.getMinecraft().theWorld == null) return;

        if (SBInfo.getInstance().getLocation() == null) return;
        if (!SBInfo.getInstance().getLocation().equals("mining_3")) return;

        int playerX = (int) Minecraft.getMinecraft().thePlayer.posX;
        int playerZ = (int) Minecraft.getMinecraft().thePlayer.posZ;

        if (Keyboard.isKeyDown(Keyboard.KEY_C)) {
            ignoredBlocks.clear();
            whitelistBlocks.clear();
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_R)) {
            ignoredChunks.clear();
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_P)) {
            Gson gson = new GsonBuilder().create();
            JsonObject obj = new JsonObject();

            for (Map.Entry<ChunkCoordIntPair, HashMap<ChunkCoordIntPair, Set<BlockPos>>> entry : ignoredBlocks.entrySet()) {
                String chunkId = entry.getKey().chunkXPos + "_" + entry.getKey().chunkZPos;
                if (!whitelistBlocks.containsKey(entry.getKey()) || whitelistBlocks.get(entry.getKey()).isEmpty()) {
                    obj.addProperty(chunkId, true);
                } else {
                    HashMap<ChunkCoordIntPair, Set<BlockPos>> whitelistMap = whitelistBlocks.get(entry.getKey());
                    JsonObject subChunkObj = new JsonObject();

                    for (Map.Entry<ChunkCoordIntPair, Set<BlockPos>> columnEntry : entry.getValue().entrySet()) {
                        String columnId = columnEntry.getKey().chunkXPos + ":" + columnEntry.getKey().chunkZPos;

                        if (!whitelistMap.containsKey(columnEntry.getKey()) || whitelistMap.get(columnEntry.getKey()).isEmpty()) {
                            subChunkObj.addProperty(columnId, true);
                        } else if (!columnEntry.getValue().isEmpty()) {
                            JsonArray whitelistedBlocksInColumn = new JsonArray();

                            int min = 300;
                            int max = 0;
                            for (BlockPos pos : columnEntry.getValue()) {
                                int y = pos.getY();
                                if (y < min) {
                                    min = y;
                                }
                                if (y > max) {
                                    max = y;
                                }
                            }
                            whitelistedBlocksInColumn.add(new JsonPrimitive(min));
                            whitelistedBlocksInColumn.add(new JsonPrimitive(max));
                            if (min < max) {
                                subChunkObj.add(columnId, whitelistedBlocksInColumn);
                            } else {
                                subChunkObj.addProperty(columnId, min);
                            }
                        }
                    }
                    try {
                        File file = new File("C:/Users/James/Desktop/testfolder/" + chunkId + ".json");
                        file.createNewFile();

                        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
                            writer.write(gson.toJson(subChunkObj));
                        }
                    } catch (IOException ignored) {
                        ignored.printStackTrace();
                    }
                }
            }

            try {
                File file = new File("C:/Users/James/Desktop/testfolder/all.json");
                file.createNewFile();

                try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
                    writer.write(gson.toJson(obj));
                }
            } catch (IOException ignored) {
                ignored.printStackTrace();
            }

        }

        for (int xC = -10; xC <= 10; xC++) {
            out:
            for (int zC = -10; zC <= 10; zC++) {
                ChunkCoordIntPair pair = new ChunkCoordIntPair(playerX / 16 + xC, playerZ / 16 + zC);

                if (!ignoredChunks.contains(pair)) {
                    ignoredChunks.add(pair);

                    boolean add = false;
                    for (int x = 0; x < 16; x++) {
                        for (int y = 0; y < 255; y++) {
                            for (int z = 0; z < 16; z++) {
                                BlockPos pos = new BlockPos(pair.chunkXPos * 16 + x, y, pair.chunkZPos * 16 + z);
                                IBlockState state = Minecraft.getMinecraft().theWorld.getBlockState(pos);

                                ChunkCoordIntPair column = new ChunkCoordIntPair(x, z);

                                if (state != null && state.getBlock() != Blocks.air) add = true;

                                if (state != null && ((state.getBlock() == Blocks.stained_hardened_clay &&
                                        state.getValue(BlockColored.COLOR) == EnumDyeColor.CYAN) ||
                                        (state.getBlock() == Blocks.wool) && state.getValue(BlockColored.COLOR) == EnumDyeColor.GRAY ||
                                        (state.getBlock() == Blocks.stone && state.getValue(BlockStone.VARIANT) == BlockStone.EnumType.DIORITE_SMOOTH))) {

                                    boolean hasAir = false;
                                    for (int xO = -1; xO <= 1; xO++) {
                                        for (int yO = -1; yO <= 1; yO++) {
                                            for (int zO = -1; zO <= 1; zO++) {
                                                int tot = Math.abs(xO) + Math.abs(yO) + Math.abs(zO);
                                                if (tot == 1) {
                                                    BlockPos pos2 = pos.add(xO, yO, zO);
                                                    IBlockState state2 = Minecraft.getMinecraft().theWorld.getBlockState(pos2);

                                                    if (state2 == null) {
                                                        continue out;
                                                    } else if (state2.getBlock() == Blocks.air) {
                                                        hasAir = true;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    if (!hasAir) continue;

                                    boolean found = false;
                                    out2:
                                    for (int xO = -4; xO <= 4; xO++) {
                                        for (int yO = -4; yO <= 4; yO++) {
                                            for (int zO = -4; zO <= 4; zO++) {
                                                int distSq = xO * xO + yO * yO + zO * zO;
                                                if (distSq < 4 * 4) {
                                                    BlockPos pos2 = pos.add(xO, yO, zO);
                                                    IBlockState state2 = Minecraft.getMinecraft().theWorld.getBlockState(pos2);

                                                    if (state2 == null) {
                                                        continue out;
                                                    } else if (state2.getBlock() == Blocks.prismarine) {
                                                        ignoredBlocks.computeIfAbsent(pair, k -> new HashMap<>())
                                                                .computeIfAbsent(column, k -> new HashSet<>()).remove(pos);
                                                        whitelistBlocks.computeIfAbsent(pair, k -> new HashMap<>())
                                                                .computeIfAbsent(column, k -> new HashSet<>()).add(pos);
                                                        found = true;
                                                        break out2;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    if (!found) {
                                        ignoredBlocks.computeIfAbsent(pair, k -> new HashMap<>())
                                                .computeIfAbsent(column, k -> new HashSet<>()).add(pos);
                                    }

                                }
                            }
                        }
                    }
                }
            }
        }*/
    }
}
