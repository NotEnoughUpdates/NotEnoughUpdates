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

package io.github.moulberry.notenoughupdates.recipes;

import net.minecraft.client.gui.GuiScreen;

import java.util.ArrayList;

public class RecipeHistory {

	private static final int MAX_HISTORY_SIZE = 50;

	private static ArrayList<GuiScreen> history = new ArrayList<>();
	private static int historyIndex = 0;

	public static void add(GuiScreen recipe) {
		if (history.size() == MAX_HISTORY_SIZE) {
			history.remove(0);
			historyIndex--;
		} else {
			if (history.size() == 0) {
				history.add(recipe);
			} else {
				if (historyIndex < history.size() - 1) {
					history = new ArrayList<>(history.subList(0, historyIndex + 1));
				}
				history.add(recipe);
				historyIndex++;
			}
		}
	}

	public static GuiScreen getPrevious() {
		if (history.size() > 0) {
			if (historyIndex - 1 < 0) {
				return null;
			}
			historyIndex--;
			return history.get(historyIndex);
		}
		return null;
	}

	public static GuiScreen getNext() {
		if (historyIndex < history.size() - 1) {
			historyIndex++;
			return history.get(historyIndex);
		}
		return null;
	}

	public static void clear() {
		history = new ArrayList<>();
		historyIndex = 0;
	}

}
