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

package io.github.moulberry.notenoughupdates.miscgui.minionhelper.render;

import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.core.util.StringUtils;
import io.github.moulberry.notenoughupdates.miscgui.minionhelper.Minion;
import io.github.moulberry.notenoughupdates.miscgui.minionhelper.MinionHelperManager;
import io.github.moulberry.notenoughupdates.miscgui.minionhelper.sources.MinionSource;
import io.github.moulberry.notenoughupdates.util.ItemUtils;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;

import java.util.List;

public class MinionHelperTooltips {
	private final MinionHelperManager manager;
	private boolean pressedShiftLast = false;
	private boolean showFullCost = false;

	public MinionHelperTooltips(MinionHelperManager manager) {
		this.manager = manager;
	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public void onItemTooltip(ItemTooltipEvent event) {
		if (!manager.inCraftedMinionsInventory()) return;
		if (!NotEnoughUpdates.INSTANCE.config.minionHelper.tooltip) return;
		if (manager.notReady()) return;

		boolean shift = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
		if (!pressedShiftLast && shift) {
			showFullCost = !showFullCost;
		}
		pressedShiftLast = shift;

		ItemStack itemStack = event.itemStack;
		if (itemStack == null) return;
		String displayName = itemStack.getDisplayName();
		if (!displayName.endsWith(" Minion")) return;
		displayName = StringUtils.cleanColour(displayName);

		List<String> lore = ItemUtils.getLore(itemStack);
		if (lore.get(0).equals("§7You haven't crafted this minion.")) return;

		int index = 0;
		for (String line : lore) {
			index++;
			if (!line.contains("Tier")) continue;

			Minion minion = manager.getMinionByName(displayName, index);
			if (minion == null) {
				System.err.println("minion is null for displayName '" + displayName + "' and tier " + index);
				continue;
			}
			MinionSource minionSource = minion.getMinionSource();
			if (minionSource == null) {
				System.err.println("minionSource is null for " + minion.getInternalName());
				continue;
			}
			String format = manager.getPriceCalculation().calculateUpgradeCostsFormat(minion, !showFullCost);
			event.toolTip.set(index, line + " §8- " + format);
		}

		if (showFullCost) {
			event.toolTip.add("§8[Press SHIFT to show upgrade cost]");
		} else {
			event.toolTip.add("§8[Press SHIFT to show full cost]");
		}
	}
}
