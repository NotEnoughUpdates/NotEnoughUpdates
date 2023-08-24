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
import io.github.moulberry.notenoughupdates.core.config.Position;
import io.github.moulberry.moulconfig.annotations.ConfigEditorSlider;
import io.github.moulberry.moulconfig.annotations.ConfigOption;

public class DungeonMapConfig {
	@Expose
	@ConfigOption(
		name = "Border Size",
		desc = "Changes the size of the map border, without changing the size of the contents"
	)
	@ConfigEditorSlider(
		minValue = 0,
		maxValue = 5,
		minStep = 0.25f
	)
	public float dmBorderSize = 1;

	@Expose
	@ConfigOption(
		name = "Room Size",
		desc = "Changes the size of rooms. Useful for higher dungeons with larger maps"
	)
	@ConfigEditorSlider(
		minValue = 0,
		maxValue = 5,
		minStep = 0.25f
	)
	public float dmRoomSize = 1;

	@Expose
	@ConfigOption(
		name = "Icon Size",
		desc = "Changes the scale of room indicators and player icons"
	)
	@ConfigEditorSlider(
		minValue = 0.5f,
		maxValue = 3f,
		minStep = 0.25f
	)
	public float dmIconScale = 1.0f;

	@Expose
	@ConfigOption(
		name = "Border Style",
		desc = "Various custom borders from various talented artists.\nUse 'custom' if your texture pack has a custom border"
	)
	public int dmBorderStyle = 0;

	@Expose
	@ConfigOption(
		name = "Show Dungeon Map",
		desc = "Show/hide the NEU dungeon map"
	)
	public boolean dmEnable = true;

	@Expose
	@ConfigOption(
		name = "Map Center",
		desc = "Center on rooms, or center on your player"
	)
	public boolean dmCenterPlayer = true;

	@Expose
	@ConfigOption(
		name = "Rotate with Player",
		desc = "Rotate the map to face the same direction as your player"
	)
	public boolean dmRotatePlayer = true;

	@Expose
	@ConfigOption(
		name = "Orient Checkmarks",
		desc = "Checkmarks will always show vertically, regardless of rotation"
	)
	public boolean dmOrientCheck = true;

	@Expose
	@ConfigOption(
		name = "Center Checkmarks",
		desc = "Checkmarks will show closer to the center of rooms"
	)
	public boolean dmCenterCheck = false;

	@Expose
	@ConfigOption(
		name = "Player Icon Style",
		desc = "Various player icon styles"
	)
	public int dmPlayerHeads = 0;

	@Expose
	@ConfigOption(
		name = "Interpolate Far Players",
		desc = "Will make players far away move smoothly"
	)
	public boolean dmPlayerInterp = true;

	@Expose
	@ConfigOption(
		name = "OpenGL Compatibility",
		desc = "Compatiblity options for people with bad computers. ONLY use this if you know what you are doing, otherwise the map will look worse"
	)
	public int dmCompat = 0;

	@Expose
	@ConfigOption(
		name = "Background Colour",
		desc = "Colour of the map background. Supports opacity & chroma"
	)
	public String dmBackgroundColour = "00:170:75:75:75";

	@Expose
	@ConfigOption(
		name = "Border Colour",
		desc = "Colour of the map border. Supports opacity & chroma. Turn off custom borders to see"
	)
	public String dmBorderColour = "00:0:0:0:0";

	@Expose
	@ConfigOption(
		name = "Chroma Border Mode",
		desc = "Applies a hue offset around the map border"
	)
	public boolean dmChromaBorder = false;

	@Expose
	@ConfigOption(
		name = "Background Blur Factor",
		desc = "Changes the blur factor behind the map. Set to 0 to disable blur"
	)
	public float dmBackgroundBlur = 0;

	@Expose
	@ConfigOption(
		name = "Position",
		desc = "Change the position of the map"
	)
	public Position dmPosition = new Position(10, 10);
}
