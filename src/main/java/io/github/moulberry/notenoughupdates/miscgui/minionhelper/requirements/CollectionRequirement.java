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

package io.github.moulberry.notenoughupdates.miscgui.minionhelper.requirements;

public class CollectionRequirement extends MinionRequirement {

	private final String collection;
	private final int level;

	public CollectionRequirement(String collection, int level) {
		this.collection = collection;
		this.level = level;
	}

	public int getLevel() {
		return level;
	}

	public String getCollection() {
		return collection;
	}

	@Override
	public String printDescription(String color) {
		return "Collection: " + color + collection + " level " + level;
	}
}
