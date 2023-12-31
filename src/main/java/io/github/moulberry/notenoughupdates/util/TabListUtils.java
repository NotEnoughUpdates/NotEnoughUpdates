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
import io.github.moulberry.notenoughupdates.autosubscribe.NEUAutoSubscribe;
import io.github.moulberry.notenoughupdates.events.TabListChangeEvent;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.world.WorldSettings;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@NEUAutoSubscribe
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

	@Getter
	public static List<String> tabList = new ArrayList<>();
	public static List<String> tabListLastTick = new ArrayList<>();

	@SubscribeEvent
	public void onTick(TickEvent.ClientTickEvent event) {
		if (Minecraft.getMinecraft().thePlayer == null) return;
		if (event.phase != TickEvent.Phase.END) return;
		tabListLastTick = tabList;
		tabList = getTabList0();
		new TabListChangeEvent(tabList, tabListLastTick).post();
	}

	private List<String> getTabList0() {
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
