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

package io.github.moulberry.notenoughupdates.util;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Ordering;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.world.WorldSettings;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TabListUtils {

	private static final Ordering<NetworkPlayerInfo> playerOrdering = Ordering.from(new PlayerComparator());

	@SideOnly(Side.CLIENT)
	static class PlayerComparator implements Comparator<NetworkPlayerInfo> {
		private PlayerComparator() {}

		public int compare(NetworkPlayerInfo o1, NetworkPlayerInfo o2) {
			ScorePlayerTeam team1 = o1.getPlayerTeam();
			ScorePlayerTeam team2 = o2.getPlayerTeam();
			return ComparisonChain.start().compareTrueFirst(
															o1.getGameType() != WorldSettings.GameType.SPECTATOR,
															o2.getGameType() != WorldSettings.GameType.SPECTATOR
														)
														.compare(
															team1 != null ? team1.getRegisteredName() : "",
															team2 != null ? team2.getRegisteredName() : ""
														)
														.compare(o1.getGameProfile().getName(), o2.getGameProfile().getName()).result();
		}
	}

	public static List<String> getTabList() {
		List<NetworkPlayerInfo> players =
			playerOrdering.sortedCopy(Minecraft.getMinecraft().thePlayer.sendQueue.getPlayerInfoMap());

		List<String> result = new ArrayList<>();

		for (NetworkPlayerInfo info : players) {
			String name = Minecraft.getMinecraft().ingameGUI.getTabList().getPlayerName(info);
			result.add(name);
		}
		return result;
	}
}
