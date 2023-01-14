/*
 * Copyright (C) 2022 NotEnoughUpdates contributors
 *
 * This file is part of NotEnoughUpdates.
 *
 * NotEnoughUpdates is free software: you can redistribute it
 * and/or modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * NotEnoughUpdates is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with NotEnoughUpdates. If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.moulberry.notenoughupdates.miscfeatures.customblockzones;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.autosubscribe.NEUAutoSubscribe;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.ChunkCoordIntPair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static io.github.moulberry.notenoughupdates.miscfeatures.customblockzones.CustomBiomes.isMithril;
import static io.github.moulberry.notenoughupdates.miscfeatures.customblockzones.CustomBiomes.isTitanium;

@NEUAutoSubscribe
public class DwarvenMinesTextures implements IslandZoneSubdivider {

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

	private Set<ChunkCoordIntPair> ignoredChunks = null;
	private final Map<ChunkCoordIntPair, Map<ChunkCoordIntPair, IgnoreColumn>> loadedChunkData = new HashMap<>();
	private boolean error = false;

	private IBlockState getBlock(BlockPos pos) {
		return Minecraft.getMinecraft().theWorld.getBlockState(pos);
	}

	private boolean isDoubleSlab(IBlockState state) {
		return state.getBlock() == Blocks.double_stone_slab;
	}

	private Reader getUTF8Resource(ResourceLocation location) throws IOException {
		return new BufferedReader(new InputStreamReader(Minecraft
			.getMinecraft()
			.getResourceManager()
			.getResource(location)
			.getInputStream(), StandardCharsets.UTF_8));
	}

	private void loadIgnoredChunks() {
		ignoredChunks = new HashSet<>();
		try (Reader reader = getUTF8Resource(new ResourceLocation("notenoughupdates:dwarven_data/all.json"))) {
			JsonObject json = NotEnoughUpdates.INSTANCE.manager.gson.fromJson(reader, JsonObject.class);
			for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
				String coord = entry.getKey();
				String[] split = coord.split("_");
				int left = Integer.parseInt(split[0]);
				int right = Integer.parseInt(split[1]);
				ignoredChunks.add(new ChunkCoordIntPair(left, right));
			}
		} catch (IOException e) {
			ignoredChunks = null;
			error = true;
			System.out.println("NEU failed to load dwarven mines ignore chunks: ");
			e.printStackTrace();
		}
	}

	private Set<ChunkCoordIntPair> getIgnoredChunks() {
		if (ignoredChunks == null)
			synchronized (this) {
				if (ignoredChunks != null) return ignoredChunks;
				loadIgnoredChunks();
			}
		return ignoredChunks;
	}

	private IgnoreColumn parseIgnoreColumn(JsonElement element) {
		if (element.isJsonPrimitive()) {
			JsonPrimitive prim = element.getAsJsonPrimitive();
			if (prim.isBoolean()) {
				return new IgnoreColumn(true, 0, 0);
			} else if (prim.isNumber()) {
				int y = prim.getAsInt();
				return new IgnoreColumn(false, y, y);
			}
		}
		if (element.isJsonArray()) {
			JsonArray arr = element.getAsJsonArray();
			if (arr.size() == 2) {
				int min = arr.get(0).getAsInt();
				int max = arr.get(1).getAsInt();
				return new IgnoreColumn(false, min, max);
			}
		}
		return null;
	}

	private Map<ChunkCoordIntPair, IgnoreColumn> loadChunkData(ChunkCoordIntPair pair) {
		Map<ChunkCoordIntPair, IgnoreColumn> map = new HashMap<>();
		try {
			ResourceLocation loc = new ResourceLocation("notenoughupdates:dwarven_data/" +
				pair.chunkXPos + "_" + pair.chunkZPos + ".json");

			try (Reader reader = getUTF8Resource(loc)) {
				JsonObject json = NotEnoughUpdates.INSTANCE.manager.gson.fromJson(reader, JsonObject.class);
				for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
					String coord = entry.getKey();
					String[] split = coord.split(":");
					int left = Integer.parseInt(split[0]);
					int right = Integer.parseInt(split[1]);

					IgnoreColumn ignore = parseIgnoreColumn(entry.getValue());
					if (ignore != null) {
						ChunkCoordIntPair offset = new ChunkCoordIntPair(left, right);
						map.put(offset, ignore);
					}
				}
			}
		} catch (Exception e) {
		}
		return map;
	}

	private Map<ChunkCoordIntPair, IgnoreColumn> getChunkData(ChunkCoordIntPair chunkCoordinates) {
		synchronized (this) {
			return loadedChunkData.computeIfAbsent(chunkCoordinates, this::loadChunkData);
		}
	}

	@Override
	public SpecialBlockZone getSpecialZoneForBlock(String location, BlockPos pos) {
		if (error) return null;
		IBlockState block = getBlock(pos);
		boolean isTitanium = isTitanium(block);
		boolean isMithril = isMithril(block);
		if (isTitanium) {
			for (EnumFacing direction : EnumFacing.values())
				if (isDoubleSlab(getBlock(pos.offset(direction))))
					return SpecialBlockZone.DWARVEN_MINES_NON_MITHRIL;
		}
		if (!isMithril && !isTitanium) return SpecialBlockZone.DWARVEN_MINES_NON_MITHRIL;

		Set<ChunkCoordIntPair> ignoredChunks = getIgnoredChunks();
		if (ignoredChunks == null)
			return null;
		ChunkCoordIntPair pair = new ChunkCoordIntPair(
			MathHelper.floor_float(pos.getX() / 16f),
			MathHelper.floor_float(pos.getZ() / 16f)
		);

		if (ignoredChunks.contains(pair)) {
			return SpecialBlockZone.DWARVEN_MINES_NON_MITHRIL;
		}
		if (isTitanium) {
			return SpecialBlockZone.DWARVEN_MINES_MITHRIL;
		}

		Map<ChunkCoordIntPair, IgnoreColumn> chunkData = getChunkData(pair);
		if (chunkData == null || error) return null;

		int modX = pos.getX() % 16;
		int modZ = pos.getZ() % 16;
		if (modX < 0) modX += 16;
		if (modZ < 0) modZ += 16;
		ChunkCoordIntPair subChunkCoordinates = new ChunkCoordIntPair(modX, modZ);

		IgnoreColumn ignore = chunkData.get(subChunkCoordinates);
		if (ignore != null) {
			if (ignore.always) {
				return SpecialBlockZone.DWARVEN_MINES_NON_MITHRIL;
			}
			int y = pos.getY();
			if (ignore.minY <= y && y <= ignore.maxY) {
				return SpecialBlockZone.DWARVEN_MINES_NON_MITHRIL;
			}
		}
		return SpecialBlockZone.DWARVEN_MINES_MITHRIL;
	}
}
