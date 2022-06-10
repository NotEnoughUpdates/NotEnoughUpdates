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

package io.github.moulberry.notenoughupdates.mbgui;

import org.lwjgl.util.vector.Vector2f;

import java.io.Serializable;

public class MBAnchorPoint implements Serializable {
	public enum AnchorPoint {
		TOPLEFT(0, 0), TOPMID(0.5f, 0), TOPRIGHT(1, 0),
		MIDRIGHT(1, 0.5f), BOTRIGHT(1, 1), BOTMID(0.5f, 1),
		BOTLEFT(0, 1), MIDLEFT(0, 0.5f), MIDMID(0.5f, 0.5f);

		public final float x;
		public final float y;

		AnchorPoint(float x, float y) {
			this.x = x;
			this.y = y;
		}
	}

	public AnchorPoint anchorPoint;
	public Vector2f offset;
	public boolean inventoryRelative;

	public MBAnchorPoint(AnchorPoint anchorPoint, Vector2f offset) {
		this(anchorPoint, offset, false);
	}

	public MBAnchorPoint(AnchorPoint anchorPoint, Vector2f offset, boolean inventoryRelative) {
		this.anchorPoint = anchorPoint;
		this.offset = offset;
		this.inventoryRelative = inventoryRelative;
	}

	public static MBAnchorPoint createFromString(String str) {
		if (str == null || str.split(":").length != 4) {
			return null;
		}

		try {
			String[] split = str.split(":");
			AnchorPoint point = AnchorPoint.valueOf(split[0].toUpperCase());
			Vector2f pos = new Vector2f(Float.parseFloat(split[1]), Float.parseFloat(split[2]));
			return new MBAnchorPoint(point, pos, Boolean.parseBoolean(split[3]));
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public String toString() {
		return anchorPoint.toString() + ":" + offset.x + ":" + offset.y + ":" + inventoryRelative;
	}
}
