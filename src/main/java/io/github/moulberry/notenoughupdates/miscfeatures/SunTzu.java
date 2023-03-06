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

package io.github.moulberry.notenoughupdates.miscfeatures;

import io.github.moulberry.notenoughupdates.autosubscribe.NEUAutoSubscribe;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Random;

@NEUAutoSubscribe
public class SunTzu {
	private static boolean enabled = false;
	private static int quoteNum = 0;

	private static final Random rand = new Random();

	private static final String[] quotes = new String[]{
		"Appear weak when you are strong, and strong when you are weak.",
		"The supreme art of war is to subdue the enemy without fighting.",
		"If you know the enemy and know yourself, you need not fear the result of a hundred battles.",
		"Let your plans be dark and impenetrable as night, and when you move, fall like a thunderbolt.",
		"All warfare is based on deception.",
		"In the midst of chaos, there is also opportunity.",
		"The greatest victory is that which requires no battle.",
		"To know your Enemy, you must become your Enemy.",
		"There is no instance of a nation benefitting from prolonged warfare.",
		"Even the finest sword plunged into salt water will eventually rust.",
		"Opportunities multiply as they are seized.",
		"When the enemy is relaxed, make them toil. When full, starve them. When settled, make them move.",
		"He who wishes to fight must first count the cost",
		"If you wait by the river long enough, the bodies of your enemies will float by.",
		"Be extremely subtle even to the point of formlessness. Be extremely mysterious even to the point of soundlessness. Thereby you can be the director of the opponent's fate.",
		"Build your opponent a golden bridge to retreat across.",
		"The wise warrior avoids the battle.",
		"Great results, can be achieved with small forces.",
		"Attack is the secret of defense; defense is the planning of an attack.",
		"Subscribe to Moulberry on YouTube.",
		"Technoblade never dies!"
	};

	public static void setEnabled(boolean enabled) {
		SunTzu.enabled = enabled;
	}

	public static void randomizeQuote() {
		for (int i = 0; i < 3; i++) {
			int newQuote = rand.nextInt(quotes.length);

			if (newQuote != quoteNum) {
				quoteNum = newQuote;
				return;
			}
		}
	}

	private static String getQuote() {
		return quotes[quoteNum];
	}

	@SubscribeEvent
	public void onOverlayDrawn(RenderGameOverlayEvent event) {
		if (enabled && ((event.type == null && Loader.isModLoaded("labymod")) ||
			event.type == RenderGameOverlayEvent.ElementType.ALL)) {
			if (Minecraft.getMinecraft().gameSettings.showDebugInfo ||
				(Minecraft.getMinecraft().gameSettings.keyBindPlayerList.isKeyDown() &&
					(!Minecraft.getMinecraft().isIntegratedServerRunning() ||
						Minecraft.getMinecraft().thePlayer.sendQueue.getPlayerInfoMap().size() > 1))) {
				return;
			}

			ScaledResolution sr = Utils.pushGuiScale(2);

			int height = Utils.renderStringTrimWidth(
				EnumChatFormatting.YELLOW + getQuote(),
				true,
				sr.getScaledWidth() / 2 - 100,
				5,
				200,
				-1,
				-1
			);
			String sunTzu = "- Sun Tzu, The Art of War";
			int sunTzuLength = Minecraft.getMinecraft().fontRendererObj.getStringWidth(sunTzu);
			Minecraft.getMinecraft().fontRendererObj.drawString(EnumChatFormatting.GOLD + sunTzu,
				sr.getScaledWidth() / 2f + 100 - sunTzuLength, 15 + height, 0, true
			);

			Utils.pushGuiScale(-1);
		}
	}

	@SubscribeEvent
	public void switchWorld(WorldEvent.Load event) {
		randomizeQuote();
	}
}
