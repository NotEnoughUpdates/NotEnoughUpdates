/*
 * Copyright (C) 2024 NotEnoughUpdates contributors
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

package io.github.moulberry.notenoughupdates.miscfeatures.world;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.autosubscribe.NEUAutoSubscribe;
import io.github.moulberry.notenoughupdates.miscfeatures.customblockzones.DwarvenMinesTextures;
import io.github.moulberry.notenoughupdates.miscfeatures.customblockzones.SpecialBlockZone;
import io.github.moulberry.notenoughupdates.util.SpecialColour;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@NEUAutoSubscribe
public class Gregreworldernd extends GenericBlockHighlighter {

	private static final Gregreworldernd INSTANCE = new Gregreworldernd();

	public static Gregreworldernd getInstance()
	{
		return INSTANCE;
	}
	@Override
	protected boolean isEnabled() {
		return true;
	}

	public Set<ChunkCoordIntPair> thingy = new HashSet<>();
	public final Map<ChunkCoordIntPair, Map<ChunkCoordIntPair, DwarvenMinesTextures.IgnoreColumn>> loadedChunkData = new HashMap<>();

	public Set<BlockPos> clickedBlocks = null;


	@SubscribeEvent
	public void onTick(InputEvent.KeyInputEvent event) {
		Minecraft mc = Minecraft.getMinecraft();
		if (!Keyboard.getEventKeyState()) return;
		int key = Keyboard.getEventKey() == 0 ? Keyboard.getEventCharacter() + 256 : Keyboard.getEventKey();
		if (key != Keyboard.KEY_F) return;

		if (mc.objectMouseOver != null
			&& mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK
			&& mc.objectMouseOver.getBlockPos() != null) {
			BlockPos pos = mc.objectMouseOver.getBlockPos();
			System.out.println(pos);

			ChunkCoordIntPair pair = new ChunkCoordIntPair(
				MathHelper.floor_float(pos.getX() / 16f),
				MathHelper.floor_float(pos.getZ() / 16f)
			);

			ResourceLocation loc = new ResourceLocation("notenoughupdates:dwarven_data/" +
				pair.chunkXPos + "_" + pair.chunkZPos + ".json");
			System.out.println(loc);
			try (Reader reader = new FileReader("C:\\Users\\nopot\\IdeaProjects\\NotEnoughUpdates\\src\\main\\resources\\assets\\notenoughupdates\\dwarven_data\\" +
				pair.chunkXPos + "_" + pair.chunkZPos + ".json")) {
				Gson gson = new GsonBuilder().setPrettyPrinting().create();
				JsonObject json = gson.fromJson(reader, JsonObject.class);
				String pretty = gson.toJson(json);
				System.out.println("\n" + pretty);

				int modX = pos.getX() % 16;
				int modZ = pos.getZ() % 16;
				if (modX < 0) modX += 16;
				if (modZ < 0) modZ += 16;
				String coord = modX + ":" + modZ;
				boolean found = false;
				JsonElement element = null;
				for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
					if (entry.getKey().equals(coord)) {
						System.out.println(entry.getKey());
						element = entry.getValue();
						found = true;
						break;
					}
				}
				if (!found) {
					System.out.println("not found");
					json.add(coord, gson.toJsonTree(pos.getY()));
					FileWriter file = new FileWriter("C:\\Users\\nopot\\IdeaProjects\\NotEnoughUpdates\\src\\main\\resources\\assets\\notenoughupdates\\dwarven_data\\" +
						pair.chunkXPos + "_" + pair.chunkZPos + ".json");
					file.write(gson.toJson(json));
					file.close();
				} else {
					System.out.println("found");
					json.remove(coord);
					/*if (element.isJsonPrimitive()) {
						JsonPrimitive prim = element.getAsJsonPrimitive();
						if (prim.isNumber()) {
							JsonArray arr = new JsonArray();
							int min = Math.min(pos.getY(), prim.getAsInt());
							int max = Math.max(pos.getY(), prim.getAsInt());
							arr.add(gson.toJsonTree(min));
							arr.add(gson.toJsonTree(max));
							json.add(coord, arr);
						}
					} else if (element.isJsonArray()) {
						JsonArray arr = element.getAsJsonArray();
						if (arr.size() == 1 || arr.size() == 0) {
							arr.add(gson.toJsonTree(pos.getY()));
							json.remove(coord);
							json.add(coord, arr);
						} else if (arr.size() == 2) {
							pos.getY();
							JsonArray arr2 = new JsonArray();
							int min = arr.get(0).getAsInt();
							int max = arr.get(1).getAsInt();
							if (pos.getY() < min) {
								arr2.add(gson.toJsonTree(pos.getY()));
								arr2.add(gson.toJsonTree(max));
							} else if (pos.getY() > max) {
								arr2.add(gson.toJsonTree(min));
								arr2.add(gson.toJsonTree(pos.getY()));
							} else {
								Utils.addChatMessage("did nothing");
								arr2.add(gson.toJsonTree(min));
								arr2.add(gson.toJsonTree(max));
							}
							json.remove(coord);
							json.add(coord, arr2);
						} else {
							Utils.addChatMessage("" + EnumChatFormatting.RED + "array error");
							System.out.println("array error");
							System.out.println(arr);
						}
					}*/
					FileWriter file = new FileWriter("C:\\Users\\nopot\\IdeaProjects\\NotEnoughUpdates\\src\\main\\resources\\assets\\notenoughupdates\\dwarven_data\\" +
						pair.chunkXPos + "_" + pair.chunkZPos + ".json");
					file.write(gson.toJson(json));
					file.close();
				}
				if (clickedBlocks == null) clickedBlocks = new HashSet<>();
				clickedBlocks.add(pos);
				Utils.addChatMessage("Saved to file");
			}
			catch (Exception e) {
				Utils.addChatMessage("error");
				System.out.println("error");
			}

			//thingy.add(pair);

		}
	}

	public Set<BlockPos> mithril_colour = new HashSet<>();

	public void register(BlockPos pos, SpecialBlockZone zone) {
		if (zone == SpecialBlockZone.DWARVEN_MINES_NON_MITHRIL) tryRegisterInterest(pos);
		if (zone == SpecialBlockZone.DWARVEN_MINES_MITHRIL) {
			mithril_colour.add(pos);
			tryRegisterInterest(pos);
		}
	}


	@Override
	protected boolean isValidHighlightSpot(BlockPos key) {
		if (clickedBlocks != null && clickedBlocks.contains(key)) return false;
		World w = Minecraft.getMinecraft().theWorld;
		if (w == null) return false;
		Block b = w.getBlockState(key).getBlock();
		return b == Blocks.prismarine || b == Blocks.wool || b == Blocks.stained_hardened_clay || b == Blocks.bedrock;
		//SpecialBlockZone specialZone = CustomBiomes.INSTANCE.getSpecialZone(key);
		//System.out.println(specialZone);
		//return true;
	}

	@Override
	protected int getColor(BlockPos blockPos) {
		return SpecialColour.specialToChromaRGB(NotEnoughUpdates.INSTANCE.config.world.enderNodeColor2);
	}
}
