package io.github.moulberry.notenoughupdates.dungeons.map;

import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.core.config.Position;
import io.github.moulberry.notenoughupdates.options.seperateSections.DungeonMapConfig;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.block.material.MapColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemMap;
import net.minecraft.item.ItemStack;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.world.storage.MapData;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DungeonMap {

	public DungeonMapRenderer renderer = new DungeonMapRenderer(this, NotEnoughUpdates.INSTANCE.config.dungeonMap);

	public DungeonMapStaticParser parsedDungeon;
	public DungeonMapPlayers dungeonMapPlayers;
	public DungeonMapDebugOverlay dungeonMapDebugOverlay = new DungeonMapDebugOverlay(this);

	public void renderMap(
		DungeonMapConfig config,
		DungeonMapStaticParser parsedDungeonData,
		DungeonMapPlayers players,
		int centerX,
		int centerY
	) {
		if (!config.dmEnable) return;
		if (parsedDungeonData == null) return;
		if (players == null) return;
		renderer.render(centerX, centerY, parsedDungeonData, players);
	}

	@SubscribeEvent
	public void onWorldChange(WorldEvent.Load event) {
		this.parsedDungeon = null;
		this.dungeonMapDebugOverlay.logFile = null;
		this.dungeonMapPlayers = null;
	}

	// \(([MASC]\d?)\)
	// nea stop being so genderbrained
	private static final Pattern FLOOR_REGEX = Pattern.compile("\\(([FEM]\\d?)\\)");

	@SubscribeEvent
	public void onTickEvent(TickEvent.ClientTickEvent event) {
		EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;
		if (thePlayer == null) return;
		ItemStack stack = thePlayer.inventory.mainInventory[8];

		Scoreboard scoreboard = Minecraft.getMinecraft().thePlayer.getWorldScoreboard();

		ScoreObjective sidebarObjective = scoreboard.getObjectiveInDisplaySlot(1);

		List<Score> scores = new ArrayList<>(scoreboard.getSortedScores(sidebarObjective));

		String floorName = null;
		for (int i = scores.size() - 1; i >= 0; i--) {
			Score score = scores.get(i);
			ScorePlayerTeam scoreplayerteam1 = scoreboard.getPlayersTeam(score.getPlayerName());
			String line = ScorePlayerTeam.formatPlayerName(scoreplayerteam1, score.getPlayerName());
			line = Utils.cleanColour(line).replaceAll("[^ -~]+", "");

			if (line.contains("The Catacombs")) {
				Matcher matcher = FLOOR_REGEX.matcher(line);
				if (matcher.find()) {
					floorName = matcher.group(1);
					break;
				}
			}

		}
		Item item = stack != null ? stack.getItem() : null;
		if (item instanceof ItemMap) {
			ItemMap map = (ItemMap) stack.getItem();
			MapData mapData = map.getMapData(stack, Minecraft.getMinecraft().theWorld);

			if (mapData == null) return;

			Color[][] colors = new Color[128][128];
			for (int i = 0; i < 128 * 128; ++i) {
				int x = i % 128;
				int y = i / 128;

				int j = mapData.colors[i] & 255;

				Color c;
				if (j / 4 == 0) {
					c = new Color(((((i + i / 128) & 1) * 8) + 16) << 24, true);
				} else {
					c = new Color(MapColor.mapColorArray[j / 4].getMapColor(j & 3), true);
				}

				colors[x][y] = c;
			}
			// TODO: do not parse static again if the colors are the same.

			DungeonMapStaticParser parser = new DungeonMapStaticParser(this, floorName, colors, mapData.mapDecorations);
			if (parser.parseDungeonMap())
				parsedDungeon = parser;
			DungeonMapPlayers players = new DungeonMapPlayers();
			players.parse(parser, this.dungeonMapPlayers);
			this.dungeonMapPlayers = players;
		} else if (item != Items.arrow) {
			// This should clear the map if you're in the dungeon boss room
			// The check for arrows is so that holding a bow will not hide the map (but it will stop the map from updating)
			this.parsedDungeon = null;
		}

	}

	@SubscribeEvent
	public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
		if (!NotEnoughUpdates.INSTANCE.hasSkyblockScoreboard()) return;
		if (event.type == RenderGameOverlayEvent.ElementType.ALL) {
			if (!NotEnoughUpdates.INSTANCE.config.dungeonMap.dmEnable) return;

			if (Minecraft.getMinecraft().gameSettings.showDebugInfo ||
				(Minecraft.getMinecraft().gameSettings.keyBindPlayerList.isKeyDown() &&
					(!Minecraft.getMinecraft().isIntegratedServerRunning() ||
						Minecraft.getMinecraft().thePlayer.sendQueue.getPlayerInfoMap().size() > 1))) {
				return;
			}

			DungeonMapStaticParser parsed = parsedDungeon;
			if (parsed != null) {
				Position pos = NotEnoughUpdates.INSTANCE.config.dungeonMap.dmPosition;

				int size = 80 + Math.round(40 * NotEnoughUpdates.INSTANCE.config.dungeonMap.dmBorderSize);
				ScaledResolution scaledResolution = Utils.pushGuiScale(2);
				renderMap(
					NotEnoughUpdates.INSTANCE.config.dungeonMap,
					parsed,
					dungeonMapPlayers,
					pos.getAbsX(scaledResolution, size / 2) + size / 2,
					pos.getAbsY(scaledResolution, size / 2) + size / 2
				);
				Utils.pushGuiScale(-1);

			}
		}
	}
}
