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

package io.github.moulberry.notenoughupdates.options.seperateSections;

import com.google.gson.annotations.Expose;
import io.github.moulberry.moulconfig.annotations.ConfigEditorBoolean;
import io.github.moulberry.moulconfig.annotations.ConfigOption;

//TODO jani rename message format
public class MinionHelper {
	@Expose
	@ConfigOption(
		name = "Enable gui",
		desc =
			"Shows a list in the crafted minions inventory of every available to craft or all missing minion, in multiple pages " +
				"that you need to get the next minion slot, sorted by upgrade cost"
	)

	@ConfigEditorBoolean
	public boolean gui = true;
	@Expose
	@ConfigOption(
		name = "Enable tooltip",
		desc = "Shows the price per minion at the crafted minions "
	)
	@ConfigEditorBoolean
	public boolean tooltip = true;
}
