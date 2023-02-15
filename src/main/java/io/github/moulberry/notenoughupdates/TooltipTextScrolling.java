/*
 * Copyright (C) 2023 NotEnoughUpdates contributors
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

package io.github.moulberry.notenoughupdates;

import io.github.moulberry.notenoughupdates.autosubscribe.NEUAutoSubscribe;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Mouse;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@NEUAutoSubscribe
public class TooltipTextScrolling {
	static List<String> lastRenderedTooltip = null;
	static int scrollOffset = 0;
	static boolean didRenderTooltip = false;

	public static List<String> handleTextLineRendering(List<String> tooltip) {
		didRenderTooltip = true;
		if (!Objects.equals(tooltip, lastRenderedTooltip)) {
			lastRenderedTooltip = new ArrayList<>(tooltip);
			scrollOffset = 0;
			return tooltip;
		}
		lastRenderedTooltip = new ArrayList<>(tooltip);
		List<String> modifiableTooltip = new ArrayList<>(tooltip);
		for (int i = 0; i < scrollOffset && modifiableTooltip.size() > 1; i++) {
			modifiableTooltip.remove(0);
		}
		for (int i = 0; i < -scrollOffset && modifiableTooltip.size() > 1; i++) {
			modifiableTooltip.remove(modifiableTooltip.size() - 1);
		}
		return modifiableTooltip;
	}

	@SubscribeEvent
	public void onMouse(GuiScreenEvent.MouseInputEvent.Pre event) {
		if (!NotEnoughUpdates.INSTANCE.config.tooltipTweaks.scrollableTooltips) return;
		if (Mouse.getEventDWheel() < 0) {
			scrollOffset = Math.max(
				lastRenderedTooltip == null ? 0 : -Math.max(lastRenderedTooltip.size() - 1, 0)
				, scrollOffset - 1
			);
		} else if (Mouse.getEventDWheel() > 0) {
			scrollOffset = Math.min(
				lastRenderedTooltip == null ? 0 : Math.max(lastRenderedTooltip.size() - 1, 0),
				scrollOffset + 1
			);
		}
	}

	@SubscribeEvent
	public void onTick(TickEvent.RenderTickEvent event) {
		if (event.phase == TickEvent.Phase.START) {
			didRenderTooltip = false;
		} else if (!didRenderTooltip) {
			lastRenderedTooltip = null;
		}
	}
}
