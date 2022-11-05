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
import io.github.moulberry.notenoughupdates.core.config.annotations.ConfigEditorBoolean;
import io.github.moulberry.notenoughupdates.core.config.annotations.ConfigEditorColour;
import io.github.moulberry.notenoughupdates.core.config.annotations.ConfigOption;

public class WorldConfig {
	@Expose
	@ConfigOption(
		name = "Highlight Glowing Mushrooms",
		desc = "Highlight glowing mushrooms in the mushroom gorge"
	)
	@ConfigEditorBoolean
	public boolean highlightGlowingMushrooms = false;

	@Expose
	@ConfigOption(
		name = "Glowing Mushroom Colour",
		desc = "In which colour should glowing mushrooms be highlighted"
	)
	@ConfigEditorColour
	public String glowingMushroomColor = "0:255:142:88:36";
}
