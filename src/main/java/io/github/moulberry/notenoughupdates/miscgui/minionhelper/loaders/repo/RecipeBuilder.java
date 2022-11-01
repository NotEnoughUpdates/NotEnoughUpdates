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

package io.github.moulberry.notenoughupdates.miscgui.minionhelper.loaders.repo;

import com.google.common.collect.ArrayListMultimap;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.core.util.StringUtils;
import io.github.moulberry.notenoughupdates.miscgui.minionhelper.Minion;
import io.github.moulberry.notenoughupdates.miscgui.minionhelper.MinionHelperManager;
import io.github.moulberry.notenoughupdates.util.Utils;

public class RecipeBuilder {
	private final MinionHelperManager manager;
	private Minion parent = null;
	private final ArrayListMultimap<String, Integer> items = ArrayListMultimap.create();

	public RecipeBuilder(MinionHelperManager manager) {
		this.manager = manager;
	}

	public Minion getParent() {
		return parent;
	}

	public ArrayListMultimap<String, Integer> getItems() {
		return items;
	}

	public void addLine(Minion minion, String rawString) {
		String[] split = rawString.split(":");
		String itemName = split[0];

		boolean isParent = false;
		if (itemName.contains("_GENERATOR_")) {
			String minionInternalName = minion.getInternalName();
			boolean same = StringUtils.removeLastWord(itemName, "_").equals(StringUtils.removeLastWord(
				minionInternalName,
				"_"
			));
			if (same) {
				Minion recipeMinion = manager.getMinionById(itemName);
				if (recipeMinion.getTier() == minion.getTier() - 1) {
					parent = recipeMinion;
					if (parent == null) {
						if (NotEnoughUpdates.INSTANCE.config.hidden.dev) {
							Utils.addChatMessage("Parent is null for minion " + minionInternalName);
						}
					}
					isParent = true;
				}
			}
		}
		if (!isParent) {
			int amount = Integer.parseInt(split[1]);
			items.put(itemName, amount);
		}
	}
}
