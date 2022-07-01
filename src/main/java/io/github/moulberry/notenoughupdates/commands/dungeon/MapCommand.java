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

package io.github.moulberry.notenoughupdates.commands.dungeon;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.commands.ClientCommandBase;
import io.github.moulberry.notenoughupdates.dungeons.GuiDungeonMapEditor;
import net.minecraft.block.material.MapColor;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.item.ItemMap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.MapData;

import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class MapCommand extends ClientCommandBase {

	public MapCommand() {
		super("neumap");
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) throws CommandException {
		if (!NotEnoughUpdates.INSTANCE.config.hidden.dev) {
			Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(
				EnumChatFormatting.RED + "[NEU] The map does not work right now. You can use the map from other mods, for example: SkyblockAddons, DungeonsGuide or Skytils."));
			return;
		}

		if (NotEnoughUpdates.INSTANCE.colourMap == null) {
			try (
				BufferedReader reader = new BufferedReader(new InputStreamReader(Minecraft
					.getMinecraft()
					.getResourceManager()
					.getResource(
						new ResourceLocation("notenoughupdates:maps/F1Full.json"))
					.getInputStream(), StandardCharsets.UTF_8))
			) {
				JsonObject json = NotEnoughUpdates.INSTANCE.manager.gson.fromJson(reader, JsonObject.class);

				NotEnoughUpdates.INSTANCE.colourMap = new Color[128][128];
				for (int x = 0; x < 128; x++) {
					for (int y = 0; y < 128; y++) {
						NotEnoughUpdates.INSTANCE.colourMap[x][y] = new Color(0, 0, 0, 0);
					}
				}
				for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
					int x = Integer.parseInt(entry.getKey().split(":")[0]);
					int y = Integer.parseInt(entry.getKey().split(":")[1]);

					NotEnoughUpdates.INSTANCE.colourMap[x][y] = new Color(entry.getValue().getAsInt(), true);
				}
			} catch (Exception ignored) {
			}
		}

		if (!NotEnoughUpdates.INSTANCE.config.hidden.dev) {
			NotEnoughUpdates.INSTANCE.openGui = new GuiDungeonMapEditor();
			return;
		}

		if (args.length == 1 && args[0].equals("reset")) {
			NotEnoughUpdates.INSTANCE.colourMap = null;
			return;
		}

		if (args.length != 2) {
			NotEnoughUpdates.INSTANCE.openGui = new GuiDungeonMapEditor();
			return;
		}

		if (args[0].equals("save")) {
			ItemStack stack = Minecraft.getMinecraft().thePlayer.getHeldItem();
			if (stack != null && stack.getItem() instanceof ItemMap) {
				ItemMap map = (ItemMap) stack.getItem();
				MapData mapData = map.getMapData(stack, Minecraft.getMinecraft().theWorld);

				if (mapData == null) return;

				JsonObject json = new JsonObject();
				for (int i = 0; i < 16384; ++i) {
					int x = i % 128;
					int y = i / 128;

					int j = mapData.colors[i] & 255;

					Color c;
					if (j / 4 == 0) {
						c = new Color((i + i / 128 & 1) * 8 + 16 << 24, true);
					} else {
						c = new Color(MapColor.mapColorArray[j / 4].getMapColor(j & 3), true);
					}

					json.addProperty(x + ":" + y, c.getRGB());
				}

				try {
					new File(NotEnoughUpdates.INSTANCE.manager.configLocation, "maps").mkdirs();
					NotEnoughUpdates.INSTANCE.manager.writeJson(
						json,
						new File(NotEnoughUpdates.INSTANCE.manager.configLocation, "maps/" + args[1] + ".json")
					);
				} catch (Exception e) {
					e.printStackTrace();
				}

				Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN +
					"Saved to file."));
			}

			return;
		}

		if (args[0].equals("load")) {
			JsonObject json = NotEnoughUpdates.INSTANCE.manager.getJsonFromFile(new File(
				NotEnoughUpdates.INSTANCE.manager.configLocation,
				"maps/" + args[1] + ".json"
			));

			NotEnoughUpdates.INSTANCE.colourMap = new Color[128][128];
			for (int x = 0; x < 128; x++) {
				for (int y = 0; y < 128; y++) {
					NotEnoughUpdates.INSTANCE.colourMap[x][y] = new Color(0, 0, 0, 0);
				}
			}
			for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
				int x = Integer.parseInt(entry.getKey().split(":")[0]);
				int y = Integer.parseInt(entry.getKey().split(":")[1]);

				NotEnoughUpdates.INSTANCE.colourMap[x][y] = new Color(entry.getValue().getAsInt(), true);
			}

			return;
		}

		NotEnoughUpdates.INSTANCE.openGui = new GuiDungeonMapEditor();
	}
}
