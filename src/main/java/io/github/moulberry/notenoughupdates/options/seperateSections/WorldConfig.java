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
import io.github.moulberry.moulconfig.annotations.ConfigAccordionId;
import io.github.moulberry.moulconfig.annotations.ConfigEditorAccordion;
import io.github.moulberry.moulconfig.annotations.ConfigEditorBoolean;
import io.github.moulberry.moulconfig.annotations.ConfigEditorColour;
import io.github.moulberry.moulconfig.annotations.ConfigOption;

public class WorldConfig {

	@Expose
	@ConfigOption(
		name = "Glowing Mushrooms",
		desc = ""
	)
	@ConfigEditorAccordion(id = 1)
	public boolean glowingMushroomAccordion = true;

	@Expose
	@ConfigOption(
		name = "Highlight Glowing Mushrooms",
		desc = "Highlight glowing mushrooms in the mushroom gorge"
	)
	@ConfigEditorBoolean
	@ConfigAccordionId(id = 1)
	public boolean highlightGlowingMushrooms = false;

	@Expose
	@ConfigOption(
		name = "Glowing Mushroom Colour",
		desc = "In which colour should glowing mushrooms be highlighted"
	)
	@ConfigEditorColour
	@ConfigAccordionId(id = 1)
	public String glowingMushroomColor2 = "0:100:142:88:36";

	@Expose
	@ConfigOption(
		name = "Ender Nodes",
		desc = ""
	)
	@ConfigEditorAccordion(id = 2)
	public boolean enderNodeAccordion = true;

	@Expose
	@ConfigOption(
		name = "Highlight Ender Nodes",
		desc = "Highlight ender nodes in the end"
	)
	@ConfigEditorBoolean
	@ConfigAccordionId(id = 2)
	public boolean highlightEnderNodes = false;

	@Expose
	@ConfigOption(
		name = "Ender Nodes Color",
		desc = "In which color should ender nodes be highlighted"
	)
	@ConfigEditorColour
	@ConfigAccordionId(id = 2)
	public String enderNodeColor2 = "0:100:0:255:0";

	@Expose
	@ConfigOption(
		name = "Frozen Treasures",
		desc = ""
	)
	@ConfigEditorAccordion(id = 3)
	public boolean frozenTreasuresAccordion = true;

	@Expose
	@ConfigOption(
		name = "Highlight Frozen Treasures",
		desc = "Highlight frozen treasures in a glacial cave"
	)
	@ConfigEditorBoolean
	@ConfigAccordionId(id = 3)
	public boolean highlightFrozenTreasures = false;

	@Expose
	@ConfigOption(
		name = "Frozen Treasures Color",
		desc = "In which color should frozen treasures be highlighted"
	)
	@ConfigEditorColour
	@ConfigAccordionId(id = 3)
	public String frozenTreasuresColor2 = "0:100:0:255:0";

}
