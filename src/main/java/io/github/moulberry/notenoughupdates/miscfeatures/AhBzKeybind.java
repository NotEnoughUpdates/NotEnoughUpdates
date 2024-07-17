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

package io.github.moulberry.notenoughupdates.miscfeatures;

import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.util.ItemResolutionQuery;
import io.github.moulberry.notenoughupdates.util.ItemUtils;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class AhBzKeybind {
	public static void onKeyPressed(String displayName, List<String> lore, String internalName) {
		if (!CookieWarning.hasActiveBoosterCookie()) return;

		String cleanName = Utils.cleanColour(displayName).replace("[Lvl {LVL}]", "]").trim();

		if (displayName.endsWith("Enchanted Book") && lore != null) {
			String loreName = Utils.cleanColour(lore.get(0));

			String bookName = loreName.substring(0, loreName.lastIndexOf(' '));
			NotEnoughUpdates.INSTANCE.trySendCommand("/bz " + bookName);
		} else if (NotEnoughUpdates.INSTANCE.manager.auctionManager.getBazaarInfo(internalName) == null) {
			NotEnoughUpdates.INSTANCE.trySendCommand("/ahs " + cleanName);
		} else {
			NotEnoughUpdates.INSTANCE.trySendCommand("/bz " + cleanName);
		}
	}

	public static void onKeyPressed(ItemStack hoveredStack) {
		if (hoveredStack != null) {
			String displayName = hoveredStack.getDisplayName();
			List<@NotNull String> lore = ItemUtils.getLore(hoveredStack);
			ItemResolutionQuery query =
				NotEnoughUpdates.INSTANCE.manager.createItemResolutionQuery().withItemStack(hoveredStack);
			String internalName = query.resolveInternalName();
			if (displayName != null) {
				onKeyPressed(displayName, lore, internalName);
			}
		}
	}
}
